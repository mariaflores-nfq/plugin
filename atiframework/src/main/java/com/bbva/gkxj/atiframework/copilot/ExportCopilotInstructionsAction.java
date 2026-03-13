package com.bbva.gkxj.atiframework.copilot;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

/**
 * Acción para exportar las instrucciones de Copilot al proyecto actual.
 * Crea o actualiza el fichero .github/scheduler-instructions.md en el proyecto
 * con las instrucciones empaquetadas en el plugin ATI Framework.
 */
public class ExportCopilotInstructionsAction extends AnAction {

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            return;
        }

        AtiCopilotInstructionsProvider provider = AtiCopilotInstructionsProvider.getInstance();
        boolean success = provider.exportToProject(project);

        if (success) {
            showNotification(project,
                    "Instrucciones exportadas",
                    "Las instrucciones de Copilot se han exportado a .github/scheduler-instructions.md",
                    NotificationType.INFORMATION);
        } else {
            showNotification(project,
                    "Error al exportar",
                    "No se pudieron exportar las instrucciones de Copilot. Revisa los permisos del directorio.",
                    NotificationType.ERROR);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        e.getPresentation().setEnabledAndVisible(project != null);
    }

    private void showNotification(Project project, String title, String content, NotificationType type) {
        try {
            NotificationGroupManager.getInstance()
                    .getNotificationGroup("ATI Framework Notifications")
                    .createNotification(title, content, type)
                    .notify(project);
        } catch (Exception ex) {
            // Fallback si el grupo de notificaciones no existe
            System.out.println("ATI Framework: " + title + " - " + content);
        }
    }
}


