package com.bbva.gkxj.atiframework.components;

import com.intellij.ui.JBColor;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * Botón personalizado para subir archivos CSV en la interfaz de usuario.
 */
public class AtiUploadCsvButton extends JButton {

    public AtiUploadCsvButton() {
        super("Upload CSV File");
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setFont(getFont().deriveFont(Font.PLAIN, 14f));
        setFocusPainted(false);

        setContentAreaFilled(false);
        setOpaque(true);
        setBackground(new JBColor(new Color(30, 115, 185), new Color(30, 115, 185)));
        setForeground(Color.WHITE);
        setBorderPainted(false);
        setBorder(BorderFactory.createEmptyBorder());
        setMargin(new Insets(0, 0, 0, 0));

        addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Seleccionar archivo CSV");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV Files", "csv"));
            int result = fileChooser.showOpenDialog(AtiUploadCsvButton.this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                // TODO: Handle the selected CSV file
            }
        });
    }

    /**
     * Pinta el botón con un fondo personalizado que cambia de color al interactuar con él (presionado o al pasar el mouse).
     * @param g El objeto Graphics utilizado para dibujar el componente.
     */
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (getModel().isPressed()) {
            g2.setColor(getBackground().darker());
        } else if (getModel().isRollover()) {
            g2.setColor(getBackground().brighter());
        } else {
            g2.setColor(getBackground());
        }

        g2.fillRect(0, 0, getWidth(), getHeight());

        FontMetrics fm = g2.getFontMetrics();
        String text = getText();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getAscent();
        int x = (getWidth() - textWidth) / 2;
        int y = (getHeight() + textHeight) / 2 - 2;
        g2.setColor(getForeground());
        g2.drawString(text, x, y);

        g2.dispose();
    }
}