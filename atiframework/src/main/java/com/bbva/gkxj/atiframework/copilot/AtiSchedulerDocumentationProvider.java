package com.bbva.gkxj.atiframework.copilot;

import com.intellij.lang.documentation.AbstractDocumentationProvider;
import com.intellij.lang.documentation.DocumentationMarkup;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Proveedor de documentación para ficheros .sch de ATI Framework.
 *
 * Este provider inyecta el conocimiento experto del scheduler.agent.md
 * en el contexto de documentación, lo cual es usado por GitHub Copilot
 * y otros asistentes de IA para entender el esquema de los schedulers.
 */
public class AtiSchedulerDocumentationProvider extends AbstractDocumentationProvider {

    @Override
    public @Nullable String generateDoc(PsiElement element, @Nullable PsiElement originalElement) {
        if (!isSchedulerFile(element)) {
            return null;
        }

        StringBuilder doc = new StringBuilder();
        doc.append(DocumentationMarkup.DEFINITION_START);
        doc.append("ATI Scheduler Configuration");
        doc.append(DocumentationMarkup.DEFINITION_END);

        doc.append(DocumentationMarkup.CONTENT_START);
        doc.append(getContextualHelp(element));
        doc.append(DocumentationMarkup.CONTENT_END);

        doc.append(DocumentationMarkup.SECTIONS_START);
        doc.append(DocumentationMarkup.SECTION_HEADER_START);
        doc.append("Esquema del Scheduler:");
        doc.append(DocumentationMarkup.SECTION_SEPARATOR);
        doc.append("<pre>");
        doc.append(getSchemaSnippet());
        doc.append("</pre>");
        doc.append(DocumentationMarkup.SECTIONS_END);

        return doc.toString();
    }

    @Override
    public @Nullable String getQuickNavigateInfo(PsiElement element, PsiElement originalElement) {
        if (!isSchedulerFile(element)) {
            return null;
        }
        return "ATI Scheduler - Configuración de planificación de tareas batch";
    }

    @Override
    public @Nullable String generateHoverDoc(@NotNull PsiElement element, @Nullable PsiElement originalElement) {
        return generateDoc(element, originalElement);
    }

    /**
     * Proporciona el prompt experto completo para integraciones con IA.
     * Este método es llamado por GitHub Copilot cuando necesita contexto adicional.
     */
    @NotNull
    public String getExpertKnowledge() {
        return AtiSchedulerPromptProvider.getInstance().getExpertPrompt();
    }

    /**
     * Construye un prompt contextualizado para el archivo actual.
     */
    @NotNull
    public String getContextualPrompt(@Nullable VirtualFile file) {
        return AtiSchedulerPromptProvider.getInstance().buildContextualPrompt(file);
    }

    private boolean isSchedulerFile(@Nullable PsiElement element) {
        if (element == null) {
            return false;
        }
        PsiFile file = element.getContainingFile();
        if (file == null) {
            return false;
        }
        VirtualFile virtualFile = file.getVirtualFile();
        return virtualFile != null && "sch".equalsIgnoreCase(virtualFile.getExtension());
    }

    private String getContextualHelp(PsiElement element) {
        String text = element.getText();

        if (text.contains("trigger")) {
            return "El objeto <b>trigger</b> define cuándo se ejecuta el scheduler. " +
                   "Tipos válidos: DAILY, WEEKLY, MONTHLY. " +
                   "Para días específicos de la semana, usar nodeType=WEEKLY con weekDays en repeat.";
        }
        if (text.contains("conditionList")) {
            return "La lista <b>conditionList</b> define condiciones previas. " +
                   "Puede incluir: query (consulta SQL/Mongo), fileWatcher (existencia de fichero), script.";
        }
        if (text.contains("batch")) {
            return "El objeto <b>batch</b> define qué proceso ejecutar. " +
                   "Incluye: batchCode (nombre), jobParameterList (parámetros), parameterQueryList (consultas).";
        }
        if (text.contains("fileWatcher")) {
            return "El objeto <b>fileWatcher</b> verifica la existencia de un fichero. " +
                   "Campos: path (directorio), filePattern (patrón), fileParameterName (variable de salida).";
        }

        return "Configuración de Scheduler de ATI Framework. " +
               "Consulta el esquema CGKXJ_CFG_SCHEDULER para campos válidos.";
    }

    private String getSchemaSnippet() {
        return """
            {
              "_id": "String (Obligatorio)",
              "planCode": "String (Obligatorio)",
              "version": "String (Obligatorio)",
              "status": "NOT_PUBLISHED|PUBLISHED|INACTIVE|ARCHIVED",
              "trigger": {
                "nodeType": "DAILY|WEEKLY|MONTHLY",
                "repeat": {
                  "executionTime": "HH:MM:SS",
                  "maxExecutionTime": "HH:MM:SS",
                  "weekDays": ["MONDAY"..."SUNDAY"]
                }
              },
              "conditionList": [...],
              "batch": {
                "batchCode": "String",
                "jobParameterList": [...]
              }
            }
            """;
    }
}

