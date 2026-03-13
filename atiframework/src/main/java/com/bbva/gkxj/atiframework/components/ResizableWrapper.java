package com.bbva.gkxj.atiframework.components;

import com.intellij.ui.components.JBScrollPane;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Wrapper que dibuja las dos rayas en la esquina inferior derecha  e intercepta el ratón para redimensionar
 * de forma libre.
 */
public class ResizableWrapper extends JPanel {
    private Point dragStartPoint;
    private Dimension startSize;
    private final int HANDLE_SIZE = 15;

    public ResizableWrapper(JBScrollPane scrollPane) {
        this.setLayout(new BorderLayout());
        this.add(scrollPane, BorderLayout.CENTER);

        MouseAdapter resizeAdapter = new MouseAdapter() {
            private boolean resizing = false;

            @Override
            public void mousePressed(MouseEvent e) {
                if (isInHandle(e)) {
                    resizing = true;
                    dragStartPoint = SwingUtilities.convertPoint((Component) e.getSource(), e.getPoint(), ResizableWrapper.this);
                    startSize = ResizableWrapper.this.getSize();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                resizing = false;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (resizing) {
                    Point currentPoint = SwingUtilities.convertPoint((Component) e.getSource(), e.getPoint(), ResizableWrapper.this);

                    int deltaX = currentPoint.x - dragStartPoint.x;
                    int deltaY = currentPoint.y - dragStartPoint.y;

                    int newWidth = Math.max(100, startSize.width + deltaX);
                    int newHeight = Math.max(40, startSize.height + deltaY);

                    ResizableWrapper.this.setPreferredSize(new Dimension(newWidth, newHeight));
                    ResizableWrapper.this.revalidate();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (isInHandle(e)) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
                } else {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        };

        this.addMouseListener(resizeAdapter);
        this.addMouseMotionListener(resizeAdapter);
        scrollPane.getViewport().getView().addMouseMotionListener(resizeAdapter);
        scrollPane.getViewport().getView().addMouseListener(resizeAdapter);
    }

    private boolean isInHandle(MouseEvent e) {
        Point converted = SwingUtilities.convertPoint((Component) e.getSource(), e.getPoint(), this);
        return converted.x >= getWidth() - HANDLE_SIZE && converted.y >= getHeight() - HANDLE_SIZE;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(130, 130, 130));

        int w = getWidth();
        int h = getHeight();

        g2.drawLine(w - 10, h - 3, w - 3, h - 10);
        g2.drawLine(w - 6, h - 3, w - 3, h - 6);

        g2.dispose();
    }
}
