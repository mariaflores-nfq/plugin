package com.bbva.gkxj.atiframework.copilot;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.concurrency.AppExecutorUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Servicio que gestiona la actualización remota de las instrucciones de Copilot.
 *
 * Permite configurar una URL remota desde donde descargar las instrucciones actualizadas.
 * Soporta múltiples fuentes:
 * - GitHub raw content
 * - GitLab raw files
 * - Servidor interno HTTP
 * - Artifactory / Nexus
 *
 * Ejemplo de URLs válidas:
 * - https://raw.githubusercontent.com/org/repo/main/.github/scheduler-instructions.md
 * - https://gitlab.com/api/v4/projects/{id}/repository/files/.github%2Fscheduler-instructions.md/raw?ref=main
 * - https://artifactory.internal.com/repo/ati-framework/scheduler-instructions.md
 */
@Service(Service.Level.APP)
@State(name = "AtiCopilotRemoteInstructions", storages = @Storage("atiCopilotRemote.xml"))
public final class AtiCopilotRemoteInstructionsService implements PersistentStateComponent<AtiCopilotRemoteInstructionsService.State> {

    private static final Logger LOG = Logger.getInstance(AtiCopilotRemoteInstructionsService.class);

    // URL por defecto - configurable por el equipo central
    private static final String DEFAULT_REMOTE_URL = "https://lwnov621.igrupobbva:36381/GKXJ/plugin/-/raw/nfq/atiframework/src/main/resources/copilot/scheduler-instructions.md?inline=false";

    // Tiempo de cache en horas
    private static final int CACHE_HOURS = 24;

    // Timeout de conexión en segundos
    private static final int CONNECTION_TIMEOUT_SECONDS = 10;

    // Directorio y nombre del fichero de instrucciones
    private static final String GITHUB_COPILOT_DIR = ".github";
    private static final String INSTRUCTIONS_FILE_NAME = "scheduler-instructions.md";

    private State myState = new State();
    private String cachedRemoteInstructions = null;
    private Instant lastFetchTime = null;

    public static AtiCopilotRemoteInstructionsService getInstance() {
        return ApplicationManager.getApplication().getService(AtiCopilotRemoteInstructionsService.class);
    }

    /**
     * Estado persistente del servicio.
     */
    public static class State {
        public String remoteUrl = DEFAULT_REMOTE_URL;
        public boolean autoUpdateEnabled = true;
        public int cacheHours = CACHE_HOURS;
        public String lastKnownInstructions = null;
        public String lastFetchTimestamp = null;
        public String authToken = null; // Para repos privados
    }

    @Override
    public @Nullable State getState() {
        return myState;
    }

    @Override
    public void loadState(@NotNull State state) {
        myState = state;
        if (state.lastKnownInstructions != null) {
            cachedRemoteInstructions = state.lastKnownInstructions;
        }
        if (state.lastFetchTimestamp != null) {
            try {
                lastFetchTime = Instant.parse(state.lastFetchTimestamp);
            } catch (Exception e) {
                lastFetchTime = null;
            }
        }
    }

    /**
     * Obtiene las instrucciones, priorizando las remotas si están disponibles y actualizadas.
     */
    @NotNull
    public String getInstructions() {
        // Si tenemos cache válida, usarla
        if (cachedRemoteInstructions != null && isCacheValid()) {
            return cachedRemoteInstructions;
        }

        // Si hay URL configurada y auto-update está habilitado, intentar actualizar en background
        if (myState.autoUpdateEnabled && !myState.remoteUrl.isEmpty()) {
            fetchRemoteInstructionsAsync();
        }

        // Devolver cache si existe, sino las instrucciones locales del plugin
        if (cachedRemoteInstructions != null) {
            return cachedRemoteInstructions;
        }

        return AtiCopilotInstructionsProvider.getInstance().getInstructions();
    }

    /**
     * Fuerza la actualización desde el servidor remoto.
     */
    public CompletableFuture<Boolean> forceUpdate() {
        return fetchRemoteInstructionsAsync();
    }

    /**
     * Descarga las instrucciones de forma asíncrona.
     */
    public CompletableFuture<Boolean> fetchRemoteInstructionsAsync() {
        if (myState.remoteUrl == null || myState.remoteUrl.isEmpty()) {
            return CompletableFuture.completedFuture(false);
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                String content = downloadFromUrl(myState.remoteUrl);
                if (content != null && !content.isEmpty()) {
                    cachedRemoteInstructions = content;
                    lastFetchTime = Instant.now();

                    // Persistir para uso offline
                    myState.lastKnownInstructions = content;
                    myState.lastFetchTimestamp = lastFetchTime.toString();

                    // Guardar el contenido en el fichero scheduler-instructions.md de todos los proyectos abiertos
                    saveInstructionsToProjects(content);

                    LOG.info("ATI Framework: Instrucciones remotas actualizadas desde " + myState.remoteUrl);
                    return true;
                }
            } catch (Exception e) {
                LOG.warn("ATI Framework: Error descargando instrucciones remotas: " + e.getMessage());
            }
            return false;
        }, AppExecutorUtil.getAppExecutorService());
    }

    /**
     * Configura la URL remota para las instrucciones.
     */
    public void setRemoteUrl(@NotNull String url) {
        myState.remoteUrl = url;
        // Invalidar cache para forzar nueva descarga
        cachedRemoteInstructions = null;
        lastFetchTime = null;
    }

    /**
     * Obtiene la URL remota configurada.
     */
    @NotNull
    public String getRemoteUrl() {
        return myState.remoteUrl != null ? myState.remoteUrl : "";
    }

    /**
     * Habilita o deshabilita la actualización automática.
     */
    public void setAutoUpdateEnabled(boolean enabled) {
        myState.autoUpdateEnabled = enabled;
    }

    /**
     * Verifica si la actualización automática está habilitada.
     */
    public boolean isAutoUpdateEnabled() {
        return myState.autoUpdateEnabled;
    }

    /**
     * Configura el token de autenticación para repositorios privados.
     */
    public void setAuthToken(@Nullable String token) {
        myState.authToken = token;
    }

    /**
     * Verifica si hay instrucciones remotas configuradas.
     */
    public boolean hasRemoteInstructions() {
        return myState.remoteUrl != null && !myState.remoteUrl.isEmpty();
    }

    /**
     * Obtiene información sobre el estado actual.
     */
    @NotNull
    public String getStatusInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("URL Remota: ").append(myState.remoteUrl.isEmpty() ? "(no configurada)" : myState.remoteUrl).append("\n");
        sb.append("Auto-actualización: ").append(myState.autoUpdateEnabled ? "Habilitada" : "Deshabilitada").append("\n");
        sb.append("Cache válida: ").append(isCacheValid() ? "Sí" : "No").append("\n");
        if (lastFetchTime != null) {
            sb.append("Última actualización: ").append(lastFetchTime.toString()).append("\n");
        }
        return sb.toString();
    }

    /**
     * Guarda el contenido de las instrucciones en el fichero .github/scheduler-instructions.md
     * de todos los proyectos abiertos en el IDE.
     */
    private void saveInstructionsToProjects(@NotNull String content) {
        Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
        for (Project project : openProjects) {
            if (project.isDisposed()) {
                continue;
            }
            String basePath = project.getBasePath();
            if (basePath == null) {
                continue;
            }
            try {
                Path githubDir = Path.of(basePath, GITHUB_COPILOT_DIR);
                if (!Files.exists(githubDir)) {
                    Files.createDirectories(githubDir);
                }
                Path instructionsPath = githubDir.resolve(INSTRUCTIONS_FILE_NAME);
                Files.writeString(instructionsPath, content,
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING);
                LOG.info("ATI Framework: Instrucciones remotas guardadas en " + instructionsPath);
            } catch (Exception e) {
                LOG.warn("ATI Framework: Error guardando instrucciones en proyecto " + basePath + ": " + e.getMessage());
            }
        }
    }

    private boolean isCacheValid() {
        if (lastFetchTime == null) {
            return false;
        }
        Instant expireTime = lastFetchTime.plus(myState.cacheHours, ChronoUnit.HOURS);
        return Instant.now().isBefore(expireTime);
    }

    @Nullable
    private String downloadFromUrl(String urlString) throws Exception {
        URI uri = URI.create(urlString);
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();

        try {
            connection.setRequestMethod("GET");
            connection.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(CONNECTION_TIMEOUT_SECONDS));
            connection.setReadTimeout((int) TimeUnit.SECONDS.toMillis(CONNECTION_TIMEOUT_SECONDS));
            connection.setRequestProperty("Accept", "text/plain, text/markdown, */*");
            connection.setRequestProperty("User-Agent", "ATI-Framework-Plugin");

            // Añadir token de autenticación si está configurado
            if (myState.authToken != null && !myState.authToken.isEmpty()) {
                // Soporta formato "Bearer token" o "token" directo
                String authHeader = myState.authToken.startsWith("Bearer ")
                        ? myState.authToken
                        : "Bearer " + myState.authToken;
                connection.setRequestProperty("Authorization", authHeader);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    return reader.lines().collect(Collectors.joining("\n"));
                }
            } else {
                LOG.warn("ATI Framework: HTTP " + responseCode + " al descargar instrucciones de " + urlString);
                return null;
            }
        } finally {
            connection.disconnect();
        }
    }
}

