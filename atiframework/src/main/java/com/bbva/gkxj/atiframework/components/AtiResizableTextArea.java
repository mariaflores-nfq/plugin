package com.bbva.gkxj.atiframework.components;

import com.bbva.gkxj.atiframework.filetype.scheduler.utils.SchedulerTheme;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Componente compuesto que incluye un JTextArea con scroll, una etiqueta superior,
 * y un tirador en la esquina inferior derecha para redimensionar de forma vertical.
 * En caso de no poder mostrar el texto aparecerá de forma automática la posibilidad de hacer Scroll.
 */
public class AtiResizableTextArea extends JPanel {

    /**
     * Área de texto
     */
    private final JTextArea textArea;
    /**
     * Tamaño de la herramienta de redimensión
     */
    private final int HANDLE_SIZE = 15;
    /**
     * Etiqueta superior
     */
    private final JLabel label;
    /**
     * Punto de arrastre
     */
    private Point dragStartPoint;
    /**
     * Tamaño estándar
     */
    private Dimension startSize;


    /**
     * Constructor por defecto
     */
    public AtiResizableTextArea() {
        this("", new JTextArea(), true);
    }

    /**
     * Constructor parametrizado
     *
     * @param labelText:   Texto para label
     * @param area:        TextArea asociado
     * @param canBeEdited: Flag para activar/desactivar edición
     */
    public AtiResizableTextArea(String labelText, JTextArea area, boolean canBeEdited) {
        this.textArea = area;
        this.setLayout(new BorderLayout(0, 5));
        this.setOpaque(false);

        label = new JLabel(labelText);
        label.setFont(label.getFont().deriveFont(Font.BOLD));
        this.add(label, BorderLayout.NORTH);

        area.setBorder(JBUI.Borders.empty(6, 8));
        area.setFont(new Font("JetBrains Mono", Font.PLAIN, 12));
        area.setTabSize(4);
        area.setLineWrap(false);
        //area.putClientProperty(canBeEdited);
        area.setBackground(SchedulerTheme.BG_CARD);

        JBScrollPane scrollPane = new JBScrollPane(area);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(JBUI.Borders.empty());

        JPanel resizablePart = new JPanel(new BorderLayout()) {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(SchedulerTheme.BORDER_SOFT);

                int w = getWidth();
                int h = getHeight();

                g2.drawLine(w - 10, h - 3, w - 3, h - 10);
                g2.drawLine(w - 6, h - 3, w - 3, h - 6);

                g2.dispose();
            }
        };

        Border defaultBorder = new CompoundBorder(
                JBUI.Borders.customLine(SchedulerTheme.BORDER_SOFT, 1),
                JBUI.Borders.empty(1)
        );
        Border focusedBorder = new CompoundBorder(
                JBUI.Borders.customLine(SchedulerTheme.BBVA_NAVY, 2),
                JBUI.Borders.empty()
        );

        resizablePart.setBorder(defaultBorder);
        resizablePart.add(scrollPane, BorderLayout.CENTER);

        area.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                resizablePart.setBorder(focusedBorder);
                resizablePart.repaint();
            }

            @Override
            public void focusLost(FocusEvent e) {
                resizablePart.setBorder(defaultBorder);
                resizablePart.repaint();
            }
        });

        MouseAdapter resizeAdapter = new MouseAdapter() {
            private boolean resizing = false;

            @Override
            public void mousePressed(MouseEvent e) {
                if (isInHandle(e, resizablePart)) {
                    resizing = true;
                    dragStartPoint = SwingUtilities.convertPoint((Component) e.getSource(), e.getPoint(), resizablePart);
                    startSize = resizablePart.getSize();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                resizing = false;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (resizing) {
                    Point currentPoint = SwingUtilities.convertPoint((Component) e.getSource(), e.getPoint(), resizablePart);

                    int deltaY = currentPoint.y - dragStartPoint.y;
                    int newHeight = Math.max(40, startSize.height + deltaY);

                    resizablePart.setPreferredSize(new Dimension(resizablePart.getWidth(), newHeight));
                    resizablePart.setMinimumSize(new Dimension(100, newHeight));

                    int totalHeight = newHeight + 25;
                    AtiResizableTextArea.this.setPreferredSize(new Dimension(AtiResizableTextArea.this.getWidth(), totalHeight));
                    AtiResizableTextArea.this.setMinimumSize(new Dimension(100, totalHeight));

                    Container parent = AtiResizableTextArea.this.getParent();
                    if (parent != null) {
                        parent.revalidate();
                        parent.repaint();
                    }

                    resizablePart.revalidate();
                    resizablePart.repaint();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (isInHandle(e, resizablePart)) {
                    resizablePart.setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
                } else {
                    resizablePart.setCursor(Cursor.getDefaultCursor());
                }
            }
        };

        resizablePart.addMouseListener(resizeAdapter);
        resizablePart.addMouseMotionListener(resizeAdapter);
        area.addMouseMotionListener(resizeAdapter);
        area.addMouseListener(resizeAdapter);

        this.add(resizablePart, BorderLayout.CENTER);
    }

    private boolean isInHandle(MouseEvent e, JComponent wrapper) {
        Point converted = SwingUtilities.convertPoint((Component) e.getSource(), e.getPoint(), wrapper);
        return converted.x >= wrapper.getWidth() - HANDLE_SIZE && converted.y >= wrapper.getHeight() - HANDLE_SIZE;
    }

    /**
     * Permite acceder al Área de texto si es necesario.
     */
    public JTextArea getTextArea() {
        return textArea;
    }

    /**
     * Obtiene el texto del área de texto.
     *
     * @return El texto contenido en el área de texto.
     */
    public String getText() {
        return textArea.getText();
    }

    /**
     * Establece el texto del área de texto.
     *
     * @param text El texto a establecer.
     */
    public void setText(String text) {
        textArea.setText(text);
    }

    /**
     * Obtiene el documento del área de texto para añadir listeners.
     *
     * @return El documento del área de texto.
     */
    public javax.swing.text.Document getDocument() {
        return textArea.getDocument();
    }

    /**
     * Establece el título de la etiqueta superior.
     *
     * @param title El texto a mostrar en la etiqueta.
     */
    public void setTitle(String title) {
        this.label.setText(title);
    }
}