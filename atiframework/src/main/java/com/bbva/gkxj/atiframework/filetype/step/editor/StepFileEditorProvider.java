package com.bbva.gkxj.atiframework.filetype.step.editor;

import com.bbva.gkxj.atiframework.filetype.scheduler.editor.SchFormEditor;
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

public class StepFileEditorProvider  implements FileEditorProvider, DumbAware {

        /**
         * Indica si este proveedor acepta el archivo dado.
         *
         * @param project proyecto actual de IntelliJ IDEA
         * @param file    archivo virtual a evaluar
         * @return true si la extensión del archivo es sch, false en caso contrario
         */
        @Override
        public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
            return "step".equalsIgnoreCase(file.getExtension());
        }

        /**
         * Crea un editor compuesto para archivos .step, formado por:
         *
         *  - Un TextEditor estándar para el contenido del archivo.
         *  - Un StepFormEditor como vista previa/formulario asociado.
         * Además, registra un DocumentListener para sincronizar los cambios realizados en el editor de texto con el formulario.
         *
         * @param project proyecto actual de IntelliJ IDEA
         * @param file    archivo virtual que se va a editar
         * @return instancia de TextEditorWithPreview que combina editor y vista previa
         */
        @Override
        public @NotNull FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
            TextEditor textEditor = (TextEditor) TextEditorProvider.getInstance().createEditor(project, file);
            StpFormEditor formEditor = new StpFormEditor(project, file);

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
                    "ATI Step Editor",
                    TextEditorWithPreview.Layout.SHOW_EDITOR_AND_PREVIEW);
        }

        /**
         * Devuelve el identificador único del tipo de editor.
         *
         * @return cadena identificadora del editor de archivos Scheduler
         */
        @Override
        public @NotNull @NonNls String getEditorTypeId() {
            return "ati-step-file-editor-with-preview";
        }

        /**
         * Especifica la política de integración con los editores por defecto.
         * En este caso, se oculta el editor de texto estándar para archivos .sch.
         *
         * @return FileEditorPolicy.HIDE_DEFAULT_EDITOR para reemplazar al editor por defecto
         */
        @Override
        public @NotNull FileEditorPolicy getPolicy() {
            return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
        }
}

