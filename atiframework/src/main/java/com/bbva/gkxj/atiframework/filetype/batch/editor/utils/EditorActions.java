/*
 * Copyright (c) 2001-2012, JGraph Ltd
 */
package com.bbva.gkxj.atiframework.filetype.batch.editor.utils;

import com.mxgraph.model.mxCell;
import com.mxgraph.util.mxResources;
import com.mxgraph.view.mxGraph;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 *
 */
public class EditorActions {
    /**
     *
     * @param e
     * @return Returns the graph for the given action event.
     */
    public static BasicGraphEditor getEditor(ActionEvent e) {
        if (e.getSource() instanceof Component component) {

            while (component != null
                    && !(component instanceof BasicGraphEditor)) {
                component = component.getParent();
            }

            return (BasicGraphEditor) component;
        }

        return null;
    }


    /**
     *
     */
    @SuppressWarnings("serial")
    public static class HistoryAction extends AbstractAction {
        /**
         *
         */
        protected boolean undo;

        /**
         *
         */
        public HistoryAction(boolean undo) {
            this.undo = undo;
        }

        /**
         *
         */
        public void actionPerformed(ActionEvent e) {
            BasicGraphEditor editor = getEditor(e);

            if (editor != null) {
                if (undo) {
                    editor.getUndoManager().undo();
                } else {
                    editor.getUndoManager().redo();
                }
            }
        }
    }

    /**
     *
     */
    @SuppressWarnings("serial")
    public static class NewAction extends AbstractAction {
        /**
         *
         */
        public void actionPerformed(ActionEvent e) {
            BasicGraphEditor editor = getEditor(e);

            if (editor != null) {
                if (!editor.isModified()
                        || JOptionPane.showConfirmDialog(editor,
                        mxResources.get("loseChanges")) == JOptionPane.YES_OPTION) {
                    mxGraph graph = editor.getGraphComponent().getGraph();

                    // Check modified flag and display save dialog
                    mxCell root = new mxCell();
                    root.insert(new mxCell());
                    graph.getModel().setRoot(root);

                    editor.setModified(false);
                    editor.setCurrentFile(null);
                    editor.getGraphComponent().zoomAndCenter();
                }
            }
        }
    }

}
