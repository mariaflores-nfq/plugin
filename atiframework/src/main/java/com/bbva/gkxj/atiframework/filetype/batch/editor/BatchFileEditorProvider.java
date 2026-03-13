package com.bbva.gkxj.atiframework.filetype.batch.editor;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class BatchFileEditorProvider implements FileEditorProvider, DumbAware {

    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        return "batch".equalsIgnoreCase(file.getExtension());
    }

    @Override
    public @NotNull FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        TextEditor textEditor = (TextEditor) TextEditorProvider.getInstance().createEditor(project, file);
        BatchFormEditor formEditor = new BatchFormEditor(project, file);

        // Sincronizar cambios del texto al formulario
        Document document = textEditor.getEditor().getDocument();
        document.addDocumentListener(new DocumentListener() {
            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
                if (!formEditor.isUpdating()) {
                    ApplicationManager.getApplication().invokeLater(() -> {
                        formEditor.updateForm();
                    });
                }
            }
        });

        return new TextEditorWithPreview(textEditor, formEditor,
                "ATI Batch Editor",
                TextEditorWithPreview.Layout.SHOW_EDITOR_AND_PREVIEW);
    }

    @Override
    public @NotNull @NonNls String getEditorTypeId() {
        return "ati-batch-file-editor-with-preview";
    }

    @Override
    public @NotNull FileEditorPolicy getPolicy() {
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
    }
}
