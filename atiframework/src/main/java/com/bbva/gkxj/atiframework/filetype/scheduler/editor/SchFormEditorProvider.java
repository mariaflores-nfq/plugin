package com.bbva.gkxj.atiframework.filetype.scheduler.editor;

import com.bbva.gkxj.atiframework.filetype.scheduler.SchFileType;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Proveedor de editores para ficheros de tipo {@link SchFileType}.
 *
 * Registra y crea la instancia de {@link SchFormEditor} que se mostrará
 * junto al editor por defecto de IntelliJ para este tipo de archivo.
 */
public class SchFormEditorProvider implements FileEditorProvider, DumbAware {

    /**
     * Identificador del tipo de editor usado por la plataforma.
     */
    private static final @NonNls String EDITOR_TYPE_ID = "scheduler-form-editor";

    /**
     * Indica si este proveedor acepta gestionar el fichero indicado.
     *
     * @param project proyecto actual de IntelliJ
     * @param file    fichero abierto en el editor
     * @return true si el tipo del fichero es {@link SchFileType},false en caso contrario
     */
    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        return file.getFileType() instanceof SchFileType;
    }

    /**
     * Crea una nueva instancia del editor asociado al fichero.
     *
     * @param project proyecto actual de IntelliJ
     * @param file    fichero que se va a editar
     * @return instancia de {@link SchFormEditor} para el fichero dado
     */
    @Override
    public @NotNull FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {
        return new SchFormEditor(project, file);
    }

    /**
     * Devuelve el identificador único del tipo de editor.
     *
     * @return identificador del tipo de editor
     */
    @Override
    public @NotNull @NonNls String getEditorTypeId() {
        return EDITOR_TYPE_ID;
    }

    /**
     * Indica la política de integración con el editor por defecto.
     *
     * @return {@link FileEditorPolicy}
     */
    @Override
    public @NotNull FileEditorPolicy getPolicy() {
        return FileEditorPolicy.PLACE_AFTER_DEFAULT_EDITOR;
    }
}