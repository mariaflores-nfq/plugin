package com.bbva.gkxj.atiframework.filetype.workflow.editor;

import com.bbva.gkxj.atiframework.filetype.workflow.model.WorkFlowStyles;
import com.intellij.ui.JBColor;
import icons.AtiIcons;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.dnd.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Panel lateral que actúa como la paleta de componentes y herramientas del editor.
 * <p>
 * Esta clase gestiona dos tipos de elementos:
 * <ul>
 * <li><b>Herramientas de interacción:</b> Como la mano para desplazarse (HAND) o las herramientas de conexión (DIRECT, QUEUE).</li>
 * <li><b>Componentes de negocio:</b> Nodos del flujo (FILTER, ROUTER, etc.) que pueden ser seleccionados o arrastrados al lienzo.</li>
 * </ul>
 * Implementa un sistema visual de selección para resaltar la herramienta activa y utiliza
 * {@link DragSource} para permitir el arrastre de tipos de nodos hacia el grafo.
 */
public class EditorPalette extends JPanel {

    /** Etiqueta que representa la entrada (herramienta o nodo) actualmente seleccionada en la paleta. */
    protected JLabel selectedEntry = null;

    /**
     * Interfaz de escucha para eventos de interacción en la paleta.
     */
    public interface PaletteToolListener {
        /** Invocado cuando se selecciona una herramienta de utilidad (Hand, Connect, etc.). */
        void onToolSelected(String toolId);
        /** Invocado cuando se selecciona un tipo de nodo para insertar. */
        void onNodeSelected(WorkFlowStyles.WfNodeType nodeType);
    }

    /** Listener encargado de propagar las acciones de la paleta al controlador principal. */
    private PaletteToolListener toolListener;

    /**
     * Constructor de la paleta.
     * Configura el diseño vertical, el color de fondo y el margen interno.
     *
     * @param listener Implementación de la interfaz para manejar las selecciones.
     */
    public EditorPalette(PaletteToolListener listener) {
        this.toolListener = listener;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(JBColor.WHITE);
        setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
    }

    /**
     * Inicializa y construye la interfaz visual de la paleta.
     * <p>
     * Crea los botones de herramientas generales y recorre los valores de {@link WorkFlowStyles.WfNodeType}
     * para generar las entradas de los componentes de negocio, habilitando el soporte de arrastre (Drag)
     * para cada uno de ellos.
     * </p>
     */
    public void addTemplate() {
        // --- SECCIÓN DE HERRAMIENTAS ---
        add(createSectionLabel(""));

        JLabel handBtn = createToolButton("", AtiIcons.HAND_TOOL_ICON, "HAND");
        add(handBtn);
        setSelectionEntry(handBtn); // Selección por defecto

        add(Box.createRigidArea(new Dimension(0, 5)));
        add(createToolButton("", AtiIcons.GLOBAL_CONNECT_TOOL_ICON, "DIRECT"));
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(createToolButton("", AtiIcons.QUEUE_EDGE_ICON, "QUEUE"));

        add(Box.createRigidArea(new Dimension(0, 15)));
        add(new JSeparator(SwingConstants.HORIZONTAL));
        add(Box.createRigidArea(new Dimension(0, 10)));

        // --- SECCIÓN DE COMPONENTES (NODOS) ---
        add(createSectionLabel(""));

        for (WorkFlowStyles.WfNodeType nodeType : WorkFlowStyles.WfNodeType.values()) {
            final JLabel entry = new JLabel(nodeType.getLabel());
            Icon scaledIcon;
            if(nodeType == WorkFlowStyles.WfNodeType.INPUT||nodeType == WorkFlowStyles.WfNodeType.OUTPUT){
                 scaledIcon = AtiIcons.getScaledIcon(nodeType.getIcon(), 15);

            }else {
                 scaledIcon = AtiIcons.getScaledIcon(nodeType.getIcon(), 30);
            }
            stylePaletteEntry(entry, scaledIcon);


            // Listener para selección mediante clic
            entry.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    setSelectionEntry(entry);
                    if (toolListener != null) toolListener.onNodeSelected(nodeType);
                }
            });

            // Configuración del Drag and Drop (Arrastre)
            new DragSource().createDefaultDragGestureRecognizer(entry, DnDConstants.ACTION_COPY, event -> {
                // Transportamos el nombre del label como un StringSelection
                event.startDrag(DragSource.DefaultCopyDrop, new StringSelection(nodeType.getLabel()));
            });

            add(entry);
            add(Box.createRigidArea(new Dimension(0, 5)));
        }

        add(Box.createVerticalGlue());
    }

    /**
     * Crea un botón de herramienta estilizado con un icono.
     *
     * @param text   Texto opcional (etiqueta).
     * @param icon   Icono de la herramienta.
     * @param toolId Identificador de la herramienta que se enviará al listener.
     * @return El componente JLabel configurado como botón.
     */
    private JLabel createToolButton(String text, Icon icon, String toolId) {
        JLabel btn = new JLabel(text);
        Icon scaledIcon = AtiIcons.getScaledIcon(icon, 25);
        stylePaletteEntry(btn, scaledIcon);
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                setSelectionEntry(btn);
                if (toolListener != null) toolListener.onToolSelected(toolId);
            }
        });
        return btn;
    }

    /**
     * Aplica el estilo visual común a las entradas de la paleta.
     * Configura dimensiones, alineación del texto bajo el icono, fuente y cursor.
     *
     * @param label Componente a estilizar.
     * @param icon  Icono que se asignará a la etiqueta.
     */
    private void stylePaletteEntry(JLabel label, Icon icon) {
        label.setPreferredSize(new Dimension(72, 65));
        label.setMaximumSize(new Dimension(72, 65));
        label.setIcon(icon);
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setVerticalTextPosition(JLabel.BOTTOM);
        label.setHorizontalTextPosition(JLabel.CENTER);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    /**
     * Crea una etiqueta de sección minimalista (ej: para agrupar herramientas).
     *
     * @param text Texto de la sección.
     * @return El componente JLabel configurado.
     */
    private JLabel createSectionLabel(String text) {
        JLabel l = new JLabel(text.toUpperCase());
        l.setFont(new Font("Segoe UI", Font.BOLD, 9));
        l.setForeground(JBColor.GRAY);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    /**
     * Gestiona el resaltado visual de la herramienta seleccionada.
     * Limpia el borde y fondo de la selección anterior y aplica un estilo azul suave
     * con borde corporativo a la nueva selección.
     *
     * @param entry El componente que se desea marcar como seleccionado.
     */
    private void setSelectionEntry(JLabel entry) {
        if (selectedEntry != null) {
            selectedEntry.setOpaque(false);
            selectedEntry.setBackground(null);
            selectedEntry.setBorder(null);
        }
        selectedEntry = entry;
        if (selectedEntry != null) {
            selectedEntry.setOpaque(true);
            // Color de fondo azul claro compatible con temas Darcula y Light
            selectedEntry.setBackground(new JBColor(new Color(230, 240, 250), new Color(40, 45, 50)));
            selectedEntry.setBorder(BorderFactory.createLineBorder(new JBColor(0x004481, 0x004481), 1));
        }
    }
}