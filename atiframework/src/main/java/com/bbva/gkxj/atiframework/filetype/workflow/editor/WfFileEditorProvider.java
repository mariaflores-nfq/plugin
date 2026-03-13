package com.bbva.gkxj.atiframework.filetype.workflow.editor;

import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Proveedor de editores para archivos de flujo de trabajo (Workflow).
 * <p>
 * Esta clase se encarga de registrar y crear la interfaz de edición para archivos con extensión {@code .wf} o {@code .json}.
 * Implementa {@link DumbAware} para permitir que el editor esté disponible incluso mientras el IDE
 * está indexando el proyecto.
 * </p>
 * <p>
 * Genera una vista dual mediante {@link TextEditorWithPreview}, permitiendo al usuario alternar o
 * visualizar simultáneamente el código fuente (JSON) y el editor gráfico basado en JGraphX.
 * </p>
 */
public class WfFileEditorProvider implements FileEditorProvider, DumbAware {

    /**
     * Determina si este proveedor debe gestionar el archivo seleccionado.
     * * @param project El proyecto actual de IntelliJ.
     * @param file    El archivo virtual que se intenta abrir.
     * @return {@code true} si la extensión del archivo es "wf"
     */
    @Override
    public boolean accept(@NotNull Project project, @NotNull VirtualFile file) {
        return "wf".equalsIgnoreCase(file.getExtension());
    }

    /**
     * Crea una instancia de la interfaz de edición dividida.
     * <p>
     * Combina un editor de texto estándar proporcionado por IntelliJ con el editor visual
     * personalizado {@link WfFormEditor}.
     * </p>
     * * @param project El proyecto en el que se abre el archivo.
     * @param file    El archivo virtual a editar.
     * @return Una instancia de {@link TextEditorWithPreview} configurada con el layout de edición y previsualización.
     */
    @Override
    public @NotNull FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file) {

        // Obtener el editor de texto nativo de la plataforma
        TextEditor textEditor = (TextEditor) TextEditorProvider.getInstance().createEditor(project, file);

        // Instanciar el editor gráfico personalizado
        WfFormEditor formEditor = new WfFormEditor(project, file);

        // Retornar la vista combinada
        return new TextEditorWithPreview(
                textEditor,
                formEditor,
                "Work Flow Editor",
                TextEditorWithPreview.Layout.SHOW_EDITOR_AND_PREVIEW
        );
    }

    /**
     * Devuelve el identificador único para este tipo de editor.
     * * @return Un String identificativo, utilizado internamente por el IDE para persistir estados del editor.
     */
    @Override
    public @NotNull @NonNls String getEditorTypeId() {
        return "workflow-file-editor-with-preview";
    }

    /**
     * Define la política de visualización frente a otros editores.
     * <p>
     * Se utiliza {@link FileEditorPolicy#HIDE_DEFAULT_EDITOR} para asegurar que esta vista
     * combinada sea la principal y oculte el editor de texto plano por defecto.
     * </p>
     * * @return La política de jerarquía del editor.
     */
    @Override
    public @NotNull FileEditorPolicy getPolicy() {
        return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
    }
}