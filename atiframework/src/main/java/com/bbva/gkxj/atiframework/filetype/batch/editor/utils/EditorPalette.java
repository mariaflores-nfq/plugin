package com.bbva.gkxj.atiframework.filetype.batch.editor.utils;

import com.intellij.ui.JBColor;
import com.mxgraph.model.mxCell;
import com.mxgraph.model.mxGeometry;
import com.mxgraph.swing.util.mxGraphTransferable;
import com.mxgraph.swing.util.mxSwingConstants;
import com.mxgraph.util.*;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * Panel que actua como paleta de componentes para el editor.
 * Permite añadir plantillas visuales que los usuarios pueden arrastrar y soltar hacia el lienzo principal.
 */
public class EditorPalette extends JPanel {

    /**
     * Selección
     */
    protected JLabel selectedEntry = null;

    /**
     * Fuente de eventos
     */
    protected mxEventSource eventSource = new mxEventSource(this);

    /**
     * Color de gradiente
     */
    protected Color gradientColor = JBColor.WHITE;

    /**
     * Constructor por defecto.
     * Inicializa el panel configurando el color de fondo, el layout principal, un escuchador de eventos
     * de raton para limpiar la seleccion al pulsar en un area vacia, y un manejador de transferencias basico.
     */
    @SuppressWarnings("serial")
    public EditorPalette() {
        setBackground(JBColor.WHITE);
        setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        addMouseListener(new MouseListener() {

            public void mousePressed(MouseEvent e) {
                clearSelection();
            }

            public void mouseClicked(MouseEvent e) {}

            public void mouseEntered(MouseEvent e) {}

            public void mouseExited(MouseEvent e) {}

            public void mouseReleased(MouseEvent e) {}

        });
        setTransferHandler(new TransferHandler() {
            public boolean canImport(JComponent comp, DataFlavor[] flavors) {
                return true;
            }
        });
    }

    /**
     * Sobrescribe el metodo de pintado del componente para aplicar un color de fondo degradado.
     * Si no hay un color de degradado definido, delega el pintado a la clase padre.
     *
     * @param g Objeto Graphics utilizado para realizar las operaciones de dibujado.
     */
    public void paintComponent(Graphics g) {
        if (gradientColor == null) {
            super.paintComponent(g);
        } else {
            Rectangle rect = getVisibleRect();

            if (g.getClipBounds() != null) {
                rect = rect.intersection(g.getClipBounds());
            }

            Graphics2D g2 = (Graphics2D) g;

            g2.setPaint(new GradientPaint(0, 0, getBackground(), getWidth(), 0,
                    gradientColor));
            g2.fill(rect);
        }
    }

    /**
     * Elimina la seleccion actual en la paleta, desmarcando cualquier entrada previamente seleccionada.
     */
    public void clearSelection() {
        setSelectionEntry(null, null);
    }

    /**
     * Establece una nueva entrada como la seleccion actual en la paleta.
     * Actualiza visualmente los bordes y la opacidad para destacar el elemento seleccionado
     * frente a la seleccion anterior, y emite un evento indicando el cambio.
     *
     * @param entry Etiqueta visual que representa el nuevo elemento seleccionado.
     * @param t Objeto de transferencia asociado al elemento seleccionado.
     */
    public void setSelectionEntry(JLabel entry, mxGraphTransferable t) {
        JLabel previous = selectedEntry;
        selectedEntry = entry;

        if (previous != null) {
            previous.setBorder(null);
            previous.setOpaque(false);
        }

        if (selectedEntry != null) {
            selectedEntry.setBorder(ShadowBorder.getSharedInstance());
            selectedEntry.setOpaque(true);
        }

        eventSource.fireEvent(new mxEventObject(mxEvent.SELECT, "entry",
                selectedEntry, "transferable", t, "previous", previous));
    }

    /**
     * Calcula y establece el ancho y alto preferido del panel basandose en el numero de
     * componentes hijos y el ancho proporcionado.
     *
     * @param width Ancho deseado para realizar el calculo de distribucion de las entradas.
     */
    public void setPreferredWidth(int width) {
        int cols = Math.max(1, width / 55);
        setPreferredSize(new Dimension(width,
                (getComponentCount() * 55 / cols) + 30));
        revalidate();
    }

    /**
     * Crea y añade una nueva plantilla a la paleta generando un nuevo vertice interno.
     *
     * @param name Nombre identificativo de la plantilla.
     * @param icon Icono visual a mostrar en la paleta.
     * @param style Cadena de texto que define los estilos visuales del vertice generado.
     * @param width Ancho base que tendra el elemento al ser soltado en el lienzo.
     * @param height Alto base que tendra el elemento al ser soltado en el lienzo.
     * @param value Objeto de valor asociado a la celda creada.
     */
    public void addTemplate(final String name, ImageIcon icon, String style,
                            int width, int height, Object value) {
        mxCell cell = new mxCell(value, new mxGeometry(0, 0, width, height),
                style);
        cell.setVertex(true);

        addTemplate(name, icon, cell, name);
    }

    /**
     * Añade una nueva entrada a la paleta a partir de un vertice ya construido.
     * Configura el componente visual, su texto, el evento al pasar el raton, y enlaza
     * un mecanismo de arrastrar y soltar para permitir moverlo hacia el lienzo principal.
     *
     * @param name Nombre o etiqueta que se mostrara bajo el icono en la paleta.
     * @param icon Icono visual a mostrar para esta entrada. Se redimensionara automaticamente si excede 32x32.
     * @param cell Celda que encapsula las propiedades estructurales y visuales a clonar al arrastrar.
     * @param onHover Texto a mostrar como ayuda al posar el cursor sobre el elemento en la paleta.
     */
    public void addTemplate(final String name, ImageIcon icon, mxCell cell, String onHover) {
        mxRectangle bounds = (mxGeometry) cell.getGeometry().clone();
        final mxGraphTransferable t = new mxGraphTransferable(
                new Object[]{cell}, bounds);

        if (icon != null) {
            if (icon.getIconWidth() > 32 || icon.getIconHeight() > 32) {
                icon = new ImageIcon(icon.getImage().getScaledInstance(32, 32,
                        0));
            }
        }

        final JLabel entry = new JLabel(icon);
        entry.setPreferredSize(new Dimension(50, 50));
        entry.setBackground(EditorPalette.this.getBackground().brighter());
        entry.setFont(new Font(entry.getFont().getFamily(), Font.BOLD, 10));

        entry.setVerticalTextPosition(JLabel.BOTTOM);
        entry.setHorizontalTextPosition(JLabel.CENTER);
        entry.setIconTextGap(0);

        entry.setToolTipText(onHover);
        entry.setText(name);

        entry.addMouseListener(new MouseListener() {

            public void mousePressed(MouseEvent e) {
                setSelectionEntry(entry, t);
            }

            public void mouseClicked(MouseEvent e) {}

            public void mouseEntered(MouseEvent e) {}

            public void mouseExited(MouseEvent e) {}

            public void mouseReleased(MouseEvent e) {}

        });

        DragGestureListener dragGestureListener = new DragGestureListener() {
            public void dragGestureRecognized(DragGestureEvent e) {
                e
                        .startDrag(null, mxSwingConstants.EMPTY_IMAGE, new Point(),
                                t, null);
            }

        };

        DragSource dragSource = new DragSource();
        dragSource.createDefaultDragGestureRecognizer(entry,
                DnDConstants.ACTION_COPY, dragGestureListener);

        add(entry);
    }

}