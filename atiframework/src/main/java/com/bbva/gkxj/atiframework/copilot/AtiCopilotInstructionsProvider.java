package com.bbva.gkxj.atiframework.copilot;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.stream.Collectors;

/**
 * Proveedor de instrucciones de Copilot empaquetadas en el plugin ATI Framework.
 *
 * Este servicio permite a los proyectos anfitriones acceder a las instrucciones
 * de Copilot sin necesidad de mantenerlas manualmente en sus repositorios.
 *
 * Uso:
 * - Obtener instrucciones: AtiCopilotInstructionsProvider.getInstance().getInstructions()
 * - Exportar al proyecto: AtiCopilotInstructionsProvider.getInstance().exportToProject(project)
 * - Verificar si están actualizadas: AtiCopilotInstructionsProvider.getInstance().isUpToDate(project)
 */
@Service(Service.Level.APP)
public final class AtiCopilotInstructionsProvider {

    private static final String INSTRUCTIONS_RESOURCE_PATH = "/copilot/scheduler-instructions.md";
    private static final String INSTRUCTIONS_FILE_NAME = "scheduler-instructions.md";
    private static final String GITHUB_COPILOT_DIR = ".github";

    private String cachedInstructions = null;
    private String cachedVersion = null;

    public AtiCopilotInstructionsProvider() {
        // Constructor público requerido para ApplicationService
    }

    @NotNull
    public static AtiCopilotInstructionsProvider getInstance() {
        return ApplicationManager.getApplication().getService(AtiCopilotInstructionsProvider.class);
    }

    /**
     * Obtiene las instrucciones de Copilot.
     * Prioriza las instrucciones remotas si están configuradas y disponibles,
     * de lo contrario usa las empaquetadas en el plugin.
     */
    @NotNull
    public String getInstructions() {
        // Primero intentar obtener instrucciones remotas si están configuradas
        try {
            AtiCopilotRemoteInstructionsService remoteService = AtiCopilotRemoteInstructionsService.getInstance();
            if (remoteService.hasRemoteInstructions()) {
                String remoteInstructions = remoteService.getInstructions();
                if (remoteInstructions != null && !remoteInstructions.isEmpty()) {
                    return remoteInstructions;
                }
            }
        } catch (Exception e) {
            // Si falla el servicio remoto, usar las locales
        }

        // Fallback a las instrucciones locales empaquetadas en el plugin
        if (cachedInstructions == null) {
            cachedInstructions = loadInstructionsFromResources();
        }
        return cachedInstructions;
    }

    /**
     * Obtiene las instrucciones locales empaquetadas (ignora las remotas).
     */
    @NotNull
    public String getLocalInstructions() {
        if (cachedInstructions == null) {
            cachedInstructions = loadInstructionsFromResources();
        }
        return cachedInstructions;
    }

    /**
     * Obtiene la versión de las instrucciones (basada en hash del contenido).
     */
    @NotNull
    public String getVersion() {
        if (cachedVersion == null) {
            cachedVersion = String.valueOf(getInstructions().hashCode());
        }
        return cachedVersion;
    }

    /**
     * Verifica si el proyecto tiene las instrucciones actualizadas.
     *
     * @param project Proyecto a verificar
     * @return true si las instrucciones del proyecto coinciden con las del plugin
     */
    public boolean isUpToDate(@NotNull Project project) {
        Path projectInstructionsPath = getProjectInstructionsPath(project);
        if (projectInstructionsPath == null || !Files.exists(projectInstructionsPath)) {
            return false;
        }

        try {
            String projectContent = Files.readString(projectInstructionsPath, StandardCharsets.UTF_8);
            return projectContent.equals(getInstructions());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Exporta las instrucciones al directorio .github del proyecto.
     * Crea el directorio si no existe.
     *
     * @param project Proyecto destino
     * @return true si se exportó correctamente
     */
    public boolean exportToProject(@NotNull Project project) {
        try {
            Path githubDir = getGithubDirectoryPath(project);
            if (githubDir == null) {
                System.err.println("ATI Framework: No se pudo determinar el directorio del proyecto");
                return false;
            }

            // Crear directorio .github si no existe
            if (!Files.exists(githubDir)) {
                Files.createDirectories(githubDir);
            }

            Path instructionsPath = githubDir.resolve(INSTRUCTIONS_FILE_NAME);
            Files.writeString(instructionsPath, getInstructions(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

            System.out.println("ATI Framework: Instrucciones exportadas a " + instructionsPath);
            return true;
        } catch (Exception e) {
            System.err.println("ATI Framework: Error exportando instrucciones - " + e.getMessage());
            return false;
        }
    }

    /**
     * Verifica si el fichero actual es un tipo soportado por ATI Framework.
     */
    public boolean isAtiFile(@Nullable VirtualFile file) {
        if (file == null) {
            return false;
        }
        String extension = file.getExtension();
        return extension != null && (
                "sch".equalsIgnoreCase(extension) ||
                "batch".equalsIgnoreCase(extension) ||
                "step".equalsIgnoreCase(extension) ||
                "calendar".equalsIgnoreCase(extension)
        );
    }

    /**
     * Construye un prompt contextual combinando las instrucciones base
     * con información del fichero actual.
     */
    @NotNull
    public String buildContextualPrompt(@Nullable VirtualFile currentFile) {
        StringBuilder prompt = new StringBuilder();
        prompt.append(getInstructions());

        if (currentFile != null && isAtiFile(currentFile)) {
            try {
                String fileContent = new String(currentFile.contentsToByteArray(), StandardCharsets.UTF_8);
                prompt.append("\n\n---\n");
                prompt.append("## Contexto actual\n\n");
                prompt.append("El usuario está editando el fichero: `").append(currentFile.getName()).append("`\n\n");
                prompt.append("Contenido actual:\n```json\n");
                prompt.append(fileContent);
                prompt.append("\n```\n");
            } catch (Exception e) {
                // Ignorar errores de lectura
            }
        }

        return prompt.toString();
    }

    /**
     * Invalida el caché (útil para desarrollo/testing).
     */
    public void invalidateCache() {
        cachedInstructions = null;
        cachedVersion = null;
    }

    @Nullable
    private Path getGithubDirectoryPath(@NotNull Project project) {
        String basePath = project.getBasePath();
        if (basePath == null) {
            return null;
        }
        return Path.of(basePath, GITHUB_COPILOT_DIR);
    }

    @Nullable
    private Path getProjectInstructionsPath(@NotNull Project project) {
        Path githubDir = getGithubDirectoryPath(project);
        if (githubDir == null) {
            return null;
        }
        return githubDir.resolve(INSTRUCTIONS_FILE_NAME);
    }

    private String loadInstructionsFromResources() {
        try (InputStream is = getClass().getResourceAsStream(INSTRUCTIONS_RESOURCE_PATH)) {
            if (is == null) {
                System.err.println("ATI Framework: No se encontró " + INSTRUCTIONS_RESOURCE_PATH);
                return getDefaultInstructions();
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String content = reader.lines().collect(Collectors.joining("\n"));
                System.out.println("ATI Framework: Instrucciones de Copilot cargadas (" + content.length() + " caracteres)");
                return content;
            }
        } catch (Exception e) {
            System.err.println("ATI Framework: Error cargando instrucciones - " + e.getMessage());
            return getDefaultInstructions();
        }
    }

    private String getDefaultInstructions() {
        return """
            # Copilot Instructions para ATI Framework
            
            Este proyecto usa ATI Framework, un motor Batch Low-Code.
            
            ## Tipos de ficheros especiales
            - `.sch` - Configuraciones de Scheduler (planificaciones)
            - `.batch` - Configuraciones de Batch (pasos de ejecución)
            - `.step` - Definición de Steps individuales
            - `.calendar` - Configuraciones de calendarios
            
            Genera siempre JSON válido siguiendo los esquemas de ATI Framework.
            """;
    }
}

