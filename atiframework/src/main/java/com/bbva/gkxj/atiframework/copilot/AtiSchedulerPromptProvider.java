package com.bbva.gkxj.atiframework.copilot;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * Proveedor de contexto experto para Schedulers de ATI Framework.
 *
 * Esta clase proporciona el conocimiento experto definido en scheduler.agent.md
 * para que pueda ser utilizado por cualquier integración con LLMs.
 *
 * Uso:
 * - Desde el plugin: AtiSchedulerPromptProvider.getInstance().getExpertPrompt()
 * - El prompt se cachea automáticamente para mejor rendimiento
 */
@Service(Service.Level.APP)
public final class AtiSchedulerPromptProvider {

    private String cachedPrompt = null;

    public AtiSchedulerPromptProvider() {
        // Constructor público requerido para ApplicationService
    }

    @NotNull
    public static AtiSchedulerPromptProvider getInstance() {
        return ApplicationManager.getApplication().getService(AtiSchedulerPromptProvider.class);
    }

    /**
     * Verifica si el contexto actual es aplicable para usar el prompt de schedulers.
     */
    public boolean isApplicable(@Nullable VirtualFile file) {
        return file != null && "sch".equalsIgnoreCase(file.getExtension());
    }

    /**
     * Obtiene el prompt experto para configuración de Schedulers.
     * El contenido se cachea para evitar lecturas repetidas del disco.
     */
    @NotNull
    public String getExpertPrompt() {
        if (cachedPrompt == null) {
            cachedPrompt = loadPromptFromResources();
        }
        return cachedPrompt;
    }

    /**
     * Construye un prompt contextualizado incluyendo el contenido del fichero actual.
     */
    @NotNull
    public String buildContextualPrompt(@Nullable VirtualFile currentFile) {
        StringBuilder prompt = new StringBuilder();
        prompt.append(getExpertPrompt());

        if (currentFile != null && isApplicable(currentFile)) {
            try {
                String fileContent = new String(currentFile.contentsToByteArray(), StandardCharsets.UTF_8);
                prompt.append("\n\n---\n");
                prompt.append("El usuario está editando el fichero: ").append(currentFile.getName()).append("\n");
                prompt.append("Contenido actual del scheduler:\n```json\n");
                prompt.append(fileContent);
                prompt.append("\n```\n");
            } catch (Exception e) {
                // Ignorar errores de lectura
            }
        }

        return prompt.toString();
    }

    /**
     * Invalida el caché del prompt (útil para desarrollo/testing).
     */
    public void invalidateCache() {
        cachedPrompt = null;
    }

    private String loadPromptFromResources() {
        try (InputStream is = getClass().getResourceAsStream("/copilot/scheduler.agent.md")) {
            if (is == null) {
                System.err.println("ATI Framework: No se encontró /copilot/scheduler.agent.md");
                return getDefaultPrompt();
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                String content = reader.lines().collect(Collectors.joining("\n"));
                System.out.println("ATI Framework: Prompt de scheduler cargado correctamente (" + content.length() + " caracteres)");
                return content;
            }
        } catch (Exception e) {
            System.err.println("ATI Framework: Error cargando prompt - " + e.getMessage());
            return getDefaultPrompt();
        }
    }

    private String getDefaultPrompt() {
        return """
            Eres un asistente experto en ATI Framework especializado en la configuración de Schedulers.
            Los ficheros .sch contienen configuraciones JSON para planificar tareas batch.
            
            Campos obligatorios del esquema CGKXJ_CFG_SCHEDULER:
            - _id: Identificador único de MongoDB
            - planCode: Nombre de la planificación
            - version: Versión del scheduler
            - status: Estado (NOT_PUBLISHED, PUBLISHED, INACTIVE, ARCHIVED)
            - uuaa: Código de la aplicación
            - recordVersion: Número de versión del registro
            - checkSum: Código de verificación
            - paramAuditList: Lista de auditoría de cambios
            - trigger: Configuración de cuándo se ejecuta (type: DAILY, WEEKLY, MONTHLY)
            - batch: Información del batch a ejecutar (batchCode, parameterQueryList)
            
            Genera siempre JSON válido y estructurado siguiendo este esquema.
            """;
    }
}



