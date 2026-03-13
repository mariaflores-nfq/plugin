package com.bbva.gkxj.atiframework.filetype.workflow.editor.propertieseditor;

import javax.swing.*;

public interface ComponentEditorStrategy {
    /**
     * Construye y devuelve el panel visual.
     * @param onChangeCallback Función que se ejecutará cada vez que el usuario escriba algo (para guardar en el grafo).
     */
    JPanel buildUI(Runnable onChangeCallback);

    /**
     * Rellena los campos de texto visuales usando los datos del objeto.
     */
    void loadData(Object data);

    /**
     * Lee los textos del formulario y los guarda en el objeto.
     */
    void saveData(Object data);
}