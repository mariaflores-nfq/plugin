package com.bbva.gkxj.atiframework.copilot;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * Intention Action que ofrece acciones asistidas por IA para ficheros .sch
 *
 * Aparece en el menú de intenciones (Alt+Enter) cuando se está editando un fichero .sch
 * Ofrece opciones como:
 * - Generar scheduler completo
 * - Añadir condición FileWatcher
 * - Añadir condición Query
 * - Validar estructura JSON
 */
public class AtiSchedulerIntentionAction extends PsiElementBaseIntentionAction implements IntentionAction {

    @Override
    @NotNull
    public String getText() {
        return "ATI: Asistente de Scheduler (IA)";
    }

    @Override
    @NotNull
    public String getFamilyName() {
        return "ATI Framework AI Assistant";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element) {
        PsiFile file = element.getContainingFile();
        if (file == null) return false;

        String fileName = file.getName();
        return fileName.endsWith(".sch");
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        List<String> options = Arrays.asList(
            "🆕 Generar nuevo Scheduler desde descripción",
            "📁 Añadir condición FileWatcher",
            "🔍 Añadir condición Query MongoDB",
            "⏰ Configurar Trigger (horario)",
            "✅ Validar estructura del Scheduler",
            "📋 Copiar prompt experto al portapapeles"
        );

        JBPopupFactory.getInstance()
            .createListPopup(new BaseListPopupStep<>("Asistente ATI Scheduler", options) {
                @Override
                public @Nullable PopupStep<?> onChosen(String selectedValue, boolean finalChoice) {
                    return doFinalStep(() -> handleSelection(project, editor, element, selectedValue));
                }
            })
            .showInBestPositionFor(editor);
    }

    private void handleSelection(Project project, Editor editor, PsiElement element, String selection) {
        AtiSchedulerPromptProvider promptProvider = AtiSchedulerPromptProvider.getInstance();

        if (selection.contains("Copiar prompt")) {
            // Copiar el prompt experto al portapapeles para uso manual
            String prompt = promptProvider.getExpertPrompt();
            copyToClipboard(prompt);
            Messages.showInfoMessage(project,
                "El prompt experto ha sido copiado al portapapeles.\n\n" +
                "Puedes pegarlo en Copilot Chat para obtener asistencia contextualizada.",
                "ATI Scheduler");
            return;
        }

        // Para las demás opciones, mostrar instrucciones de uso con Copilot
        String instruction = getInstructionForSelection(selection);
        String fullPrompt = promptProvider.getExpertPrompt() + "\n\n" + instruction;

        copyToClipboard(fullPrompt);

        Messages.showInfoMessage(project,
            "Se ha copiado al portapapeles un prompt preparado para Copilot Chat.\n\n" +
            "Pasos:\n" +
            "1. Abre Copilot Chat (View → Tool Windows → GitHub Copilot)\n" +
            "2. Pega el contenido (Ctrl+V)\n" +
            "3. Añade tu descripción específica y envía\n\n" +
            "El asistente generará la configuración JSON siguiendo el esquema de ATI Framework.",
            "ATI Scheduler - Instrucciones");
    }

    private String getInstructionForSelection(String selection) {
        if (selection.contains("Generar nuevo")) {
            return """
                
                ### TAREA SOLICITADA
                El usuario quiere crear un nuevo Scheduler completo.
                Genera un JSON válido con todos los campos obligatorios.
                Pregunta al usuario por: planCode, batchCode, tipo de trigger (DAILY/WEEKLY/MONTHLY), 
                horario de ejecución y uuaa.
                """;
        } else if (selection.contains("FileWatcher")) {
            return """
                
                ### TAREA SOLICITADA
                El usuario quiere añadir una condición de tipo FileWatcher al array conditionList.
                Genera el objeto JSON para la condición con los campos: name, fileWatcher (path, filePattern, fileParameterName), 
                checkEvery y forceAtEnd.
                Pregunta al usuario por el path y el patrón de ficheros a vigilar.
                """;
        } else if (selection.contains("Query MongoDB")) {
            return """
                
                ### TAREA SOLICITADA
                El usuario quiere añadir una condición de tipo Query MongoDB al array conditionList.
                Genera el objeto JSON para la condición con los campos: name, query (dbSource, mongoQuery con collection y filter),
                checkEvery, forceAtEnd y script.
                Pregunta al usuario por la colección y los filtros de la query.
                """;
        } else if (selection.contains("Trigger")) {
            return """
                
                ### TAREA SOLICITADA
                El usuario quiere configurar el objeto trigger del Scheduler.
                Pregunta si es DAILY, WEEKLY o MONTHLY y genera la configuración apropiada con:
                - repeat: executionTime, maxExecutionTime, repeatEvery
                - weekDays (si es WEEKLY o MONTHLY)
                - monthly.months, monthly.days, monthly.eventWeek (si es MONTHLY)
                """;
        } else if (selection.contains("Validar")) {
            return """
                
                ### TAREA SOLICITADA
                El usuario quiere validar la estructura de su fichero .sch actual.
                Revisa que tenga todos los campos obligatorios:
                - _id, planCode, version, status, uuaa, recordVersion, checkSum
                - paramAuditList (array con status, audTs, audUser, comments)
                - trigger (type, repeat con executionTime y maxExecutionTime)
                - batch (batchCode, parameterQueryList)
                
                Indica los campos que faltan o tienen valores incorrectos.
                """;
        }
        return "";
    }

    private void copyToClipboard(String text) {
        java.awt.datatransfer.StringSelection selection = new java.awt.datatransfer.StringSelection(text);
        java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
    }
}

