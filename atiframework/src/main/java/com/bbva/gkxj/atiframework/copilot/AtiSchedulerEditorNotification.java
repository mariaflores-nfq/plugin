package com.bbva.gkxj.atiframework.copilot;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorNotificationPanel;
import com.intellij.ui.EditorNotificationProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.function.Function;

/**
 * Muestra una notificación en la parte superior del editor cuando se abre un fichero .sch
 * ofreciendo asistencia con IA/Copilot para la configuración del Scheduler.
 */
public class AtiSchedulerEditorNotification implements EditorNotificationProvider {

    @Override
    public @Nullable Function<? super @NotNull FileEditor, ? extends @Nullable JComponent> collectNotificationData(
            @NotNull Project project, @NotNull VirtualFile file) {

        // Solo mostrar para ficheros .sch
        if (!"sch".equalsIgnoreCase(file.getExtension())) {
            return null;
        }

        return fileEditor -> {
            EditorNotificationPanel panel = new EditorNotificationPanel(fileEditor, EditorNotificationPanel.Status.Info);
            panel.setText("💡 ATI Scheduler: Usa Copilot Chat con el contexto experto para generar configuraciones válidas");

            panel.createActionLabel("Copiar prompt experto", () -> {
                String prompt = AtiSchedulerPromptProvider.getInstance()
                    .buildContextualPrompt(file);
                copyToClipboard(prompt);

                // Actualizar el texto del panel temporalmente
                panel.setText("✅ Prompt copiado - Pégalo en Copilot Chat para obtener asistencia");
            });

            panel.createActionLabel("Ver esquema JSON", () -> {
                showSchemaInfo(project);
            });

            panel.createActionLabel("Ocultar", () -> {
                panel.setVisible(false);
            });

            return panel;
        };
    }

    private void copyToClipboard(String text) {
        java.awt.datatransfer.StringSelection selection = new java.awt.datatransfer.StringSelection(text);
        java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
    }

    private void showSchemaInfo(Project project) {
        String schemaInfo = """
            📋 Esquema del Scheduler (CGKXJ_CFG_SCHEDULER)
            
            ═══════════════════════════════════════════════
            CAMPOS OBLIGATORIOS:
            ═══════════════════════════════════════════════
            • _id: String (ObjectId de MongoDB)
            • planCode: String (nombre único del plan)
            • version: String (ej: "1.0.0")
            • status: NOT_PUBLISHED | PUBLISHED | INACTIVE | ARCHIVED
            • uuaa: String (código de aplicación)
            • recordVersion: Integer
            • checkSum: String
            • paramAuditList: Array de auditoría
            • trigger: Configuración de horario
            • batch: Información del batch a ejecutar
            
            ═══════════════════════════════════════════════
            TIPOS DE TRIGGER:
            ═══════════════════════════════════════════════
            • DAILY: Ejecución diaria
            • WEEKLY: Ejecución semanal (requiere weekDays)
            • MONTHLY: Ejecución mensual (requiere months, days/weekDays)
            
            ═══════════════════════════════════════════════
            CONDICIONES (conditionList):
            ═══════════════════════════════════════════════
            • fileWatcher: Vigilar existencia de ficheros
            • query: Consulta MongoDB/SQL
            • novaTransferWatcher: Vigilar transferencias
            
            Para más detalles, usa Copilot Chat con el prompt experto.
            """;

        com.intellij.openapi.ui.Messages.showInfoMessage(project, schemaInfo, "ATI Scheduler - Esquema JSON");
    }
}

