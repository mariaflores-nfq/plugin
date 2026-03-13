package com.bbva.gkxj.atiframework.filetype.scheduler.create;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;

public class CreateSchFileAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        VirtualFile dir = e.getDataContext().getData(com.intellij.openapi.actionSystem.CommonDataKeys.VIRTUAL_FILE);

        CreateSchDialog dialog = new CreateSchDialog(project);
        if (!dialog.showAndGet()) {
            return; // User cancel
        }
        String planCode = dialog.getPlanCode();
        if (planCode == null || planCode.trim().isEmpty()){
            Messages.showErrorDialog(project, "Plan Code is required", "Error");
            return;
        }

        String description = dialog.getDescription();
        if (description == null) description = "";

        String fileName = planCode + ".sch";
        String fileContent = " {\n  \"planCode\": \"" + planCode + "\",\n  \"description\": \"" + description + "\"\n}";

        WriteCommandAction.runWriteCommandAction(project, () -> {
            try {
                VirtualFile file = dir.createChildData(this, fileName);
                file.setBinaryContent(fileContent.getBytes());
            } catch (Exception ex) {
                Messages.showErrorDialog(project, "Error creating the new Scheduler: " + ex.getMessage(), "Error");
            }
        });
    }
}
