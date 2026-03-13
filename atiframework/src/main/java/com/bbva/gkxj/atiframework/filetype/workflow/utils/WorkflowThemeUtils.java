package com.bbva.gkxj.atiframework.filetype.workflow.utils;

import com.bbva.gkxj.atiframework.components.AtiResizableTextArea;
import com.bbva.gkxj.atiframework.components.AtiTextField;
import com.bbva.gkxj.atiframework.filetype.workflow.model.WorkFlowStyles;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;

/**
 * Utilidad para sobrescribir los estilos por defecto de los componentes Ati
 * y adaptarlos a la paleta de colores específica del editor de Workflows.
 */
public class WorkflowThemeUtils {

    public static void applyWorkflowTheme(JTextField field) {


        Color normalColor = WorkFlowStyles.UI_BORDER;
        Color focusColor = new Color(100, 150, 200); // Color de foco del workflow

        javax.swing.border.Border defaultBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(normalColor, 1),
                JBUI.Borders.empty(4, 8)
        );
        javax.swing.border.Border focusedBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(focusColor, 2),
                JBUI.Borders.empty(3, 7)
        );

        field.setBorder(defaultBorder);
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) { field.setBorder(focusedBorder); }
            @Override public void focusLost(java.awt.event.FocusEvent e) { field.setBorder(defaultBorder); }
        });
    }

    public static void applyWorkflowTheme(JComboBox<?> combo) {
        for (java.awt.event.FocusListener fl : combo.getFocusListeners()) {
            combo.removeFocusListener(fl);
        }

        Color normalColor = WorkFlowStyles.UI_BORDER;
        Color focusColor = new Color(100, 150, 200);

        javax.swing.border.Border defaultBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(normalColor, 1),
                JBUI.Borders.empty(2, 4)
        );
        javax.swing.border.Border focusedBorder = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(focusColor, 2),
                JBUI.Borders.empty(1, 3)
        );

        combo.setBorder(defaultBorder);
        combo.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) { combo.setBorder(focusedBorder); }
            @Override public void focusLost(java.awt.event.FocusEvent e) { combo.setBorder(defaultBorder); }
        });
    }

    public static void applyWorkflowTheme(AtiResizableTextArea atiArea) {
        JTextArea internalArea = atiArea.getTextArea();

        BorderLayout layout = (BorderLayout) atiArea.getLayout();
        Component centerComp = layout.getLayoutComponent(BorderLayout.CENTER);

        if (centerComp instanceof JPanel) {
            JPanel resizablePart = (JPanel) centerComp;

            Color normalColor = WorkFlowStyles.UI_BORDER;
            Color focusColor = new Color(100, 150, 200);

            javax.swing.border.Border defaultBorder = BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(normalColor, 1),
                    JBUI.Borders.empty(1)
            );
            javax.swing.border.Border focusedBorder = BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(focusColor, 2),
                    JBUI.Borders.empty(0)
            );

            resizablePart.setBorder(defaultBorder);
            internalArea.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override public void focusGained(java.awt.event.FocusEvent e) { resizablePart.setBorder(focusedBorder); }
                @Override public void focusLost(java.awt.event.FocusEvent e) { resizablePart.setBorder(defaultBorder); }
            });
        }
    }
    // =========================================================
    // MÉTODOS FACTORY PARA COMPONENTES RESPONSIVOS
    // =========================================================

    /**
     * Crea un AtiTextField con el tema de Workflow aplicado y su ancho preferido
     * capado a 50px para evitar que empuje los JSplitPanes al escribir mucho texto.
     */
    public static AtiTextField createThemedTextField() {
        AtiTextField field = new AtiTextField() {
            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.width = 50; // Mentimos a Swing sobre el ancho
                return d;
            }

            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }
        };
        applyWorkflowTheme(field);
        return field;
    }

    /**
     * Crea un AtiResizableTextArea con el tema de Workflow aplicado y su ancho preferido
     * capado a 50px para evitar que expanda layouts al escribir líneas largas.
     */
    public static AtiResizableTextArea createThemedResizableTextArea() {
        AtiResizableTextArea area = new AtiResizableTextArea() {
            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.width = 50;
                return d;
            }

            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }
        };
        applyWorkflowTheme(area);
        return area;
    }

}