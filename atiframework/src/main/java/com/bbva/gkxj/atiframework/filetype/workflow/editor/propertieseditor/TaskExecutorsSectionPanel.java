package com.bbva.gkxj.atiframework.filetype.workflow.editor.propertieseditor;

import com.bbva.gkxj.atiframework.filetype.workflow.model.WorkflowJsonData;
import com.bbva.gkxj.atiframework.filetype.workflow.model.WorkflowJsonData.TaskExecutorData;
import com.intellij.icons.AllIcons;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel de sección encargado de la gestión dinámica de los Task Executors del Workflow.
 * <p>
 * Esta clase proporciona una interfaz de usuario para visualizar, añadir y eliminar
 * configuraciones de pools de hilos (Task Executors). Coordina una lista de componentes
 * {@link TaskExecutorItemPanel} y mantiene la sincronización entre la vista y el modelo de datos.
 * </p>
 * <p>
 * <b>Características principales:</b>
 * <ul>
 * <li>Numeración correlativa automática que se actualiza tras añadir o borrar elementos.</li>
 * <li>Capacidad de expandir y contraer la lista visualmente (Toggle).</li>
 * <li>Alineación estricta a la izquierda para los elementos de la cabecera usando {@link JLabel} para evitar márgenes nativos.</li>
 * <li>Prevención de salto de foco durante la edición de campos de texto.</li>
 * </ul>
 * </p>
 */
public class TaskExecutorsSectionPanel extends JPanel {

    /** Contenedor vertical donde se inyectan los sub-paneles de edición de cada ejecutor. */
    private final JPanel listContainer;

    /** Etiqueta de título que muestra el nombre de la sección y la cantidad actual de ejecutores definidos. */
    private final JLabel titleLabel;

    /** Lista interna que mantiene las referencias a los sub-paneles activos en la interfaz gráfica. */
    private final List<TaskExecutorItemPanel> itemPanels = new ArrayList<>();

    /** Callback para notificar cambios de estructura o contenido al editor principal y disparar el guardado. */
    private final Runnable onModelChange;

    /** Estado actual del panel ({@code true} si la lista está visible, {@code false} si está oculta). */
    private boolean isExpanded = true;

    /**
     * Construye el panel de la sección de Task Executors inicializando su interfaz gráfica.
     * Configura el encabezado con los controles de expansión y adición.
     * @param onModelChange Acción a ejecutar cuando se detecta un cambio estructural en la lista
     * (añadir o borrar un ejecutor) o en los datos internos de sus elementos.
     */
    public TaskExecutorsSectionPanel(Runnable onModelChange) {
        this.onModelChange = onModelChange;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setOpaque(false);

        // --- Configuración del Encabezado usando BorderLayout para anclaje estricto ---
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        headerRow.setBorder(JBUI.Borders.empty(5, 0));

        // Sub-panel izquierdo (Flecha + Texto)
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.X_AXIS));
        leftPanel.setOpaque(false);

        JLabel btnToggle = new JLabel(AllIcons.General.ArrowDown);
        btnToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Título de la sección
        titleLabel = new JLabel("Workflow Task Executor (0)");
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        leftPanel.add(btnToggle);
        leftPanel.add(Box.createRigidArea(new Dimension(6, 0))); // Separación exacta de 6px
        leftPanel.add(titleLabel);

        JLabel btnAdd = new JLabel(AllIcons.General.Add);
        btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Anclamos los bloques a los extremos
        headerRow.add(leftPanel, BorderLayout.WEST);
        headerRow.add(btnAdd, BorderLayout.EAST);

        // --- Contenedor de la lista dinámica ---
        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        listContainer.setOpaque(false);

        // Evento de clic para la flecha (Expandir/Contraer)
        btnToggle.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                isExpanded = !isExpanded;
                listContainer.setVisible(isExpanded);
                btnToggle.setIcon(isExpanded ? AllIcons.General.ArrowDown : AllIcons.General.ArrowRight);
                revalidate();
                repaint();
            }
        });

        // Evento de clic para el botón de añadir (+)
        btnAdd.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!isExpanded) {
                    isExpanded = true;
                    listContainer.setVisible(true);
                    btnToggle.setIcon(AllIcons.General.ArrowDown);
                }
                addNewItem(new TaskExecutorData());
                triggerSave();
            }
        });

        add(headerRow);
        add(listContainer);
    }

    /**
     * Carga la lista de ejecutores desde el modelo de datos hacia la interfaz de usuario.
     * Limpia cualquier panel previo antes de generar los nuevos componentes, a menos que
     * la carga sea fruto de una actualización de texto, en cuyo caso previene la pérdida de foco.
     * @param dataList Lista de configuraciones {@link TaskExecutorData} a cargar.
     */
    public void loadData(List<TaskExecutorData> dataList) {

        // --- ESCUDO ANTI-PÉRDIDA DE FOCO (UI Thrashing) ---
        // Si el número de elementos es el mismo que ya tenemos pintado en pantalla,
        // asumimos que el usuario está escribiendo y no debemos destruir la UI.
        if (dataList != null && itemPanels.size() == dataList.size()) {
            // (Opcional) Si quieres que los campos se actualicen por detrás sin perder el foco,
            // tendrías que llamar a un método "updateFields" en cada ItemPanel.
            // Para la mayoría de los casos de autoguardado, con ignorar el repintado es suficiente.
            return;
        }
        // --------------------------------------------------

        listContainer.removeAll();
        itemPanels.clear();

        if (dataList != null) {
            for (TaskExecutorData data : dataList) {
                addNewItem(data);
            }
        }
        updateTitle();
        revalidate();
        repaint();
    }

    /**
     * Recopila los datos introducidos en todos los paneles de ítems activos
     * y los guarda en el objeto de datos global proporcionado.
     * @param globalData Objeto {@link WorkflowJsonData} donde se persistirá la lista actual de ejecutores.
     */
    public void saveData(WorkflowJsonData globalData) {
        List<TaskExecutorData> currentData = new ArrayList<>();
        for (TaskExecutorItemPanel panel : itemPanels) {
            currentData.add(panel.getData());
        }
        globalData.setTaskExecutors(currentData.isEmpty() ? null : currentData);
    }

    /**
     * Instancia un nuevo panel de ejecutor, lo añade a la lista interna y a la interfaz gráfica.
     * @param data Objeto con los datos del ejecutor. Puede ser un ejecutor existente o uno nuevo por defecto.
     */
    private void addNewItem(TaskExecutorData data) {
        int index = itemPanels.size() + 1;
        TaskExecutorItemPanel itemPanel = new TaskExecutorItemPanel(data, index, this::removeItem, onModelChange);
        itemPanels.add(itemPanel);
        listContainer.add(itemPanel);

        updateTitle();
        revalidate();
        repaint();
    }

    /**
     * Elimina un panel de ejecutor específico de la lista y actualiza la interfaz.
     * Posteriormente, recalcula los índices de los elementos restantes para mantener una numeración secuencial.
     * @param panel El componente {@link TaskExecutorItemPanel} que ha solicitado ser eliminado.
     */
    private void removeItem(TaskExecutorItemPanel panel) {
        itemPanels.remove(panel);
        listContainer.remove(panel);

        // Recalcular la numeración de los elementos que quedan (ej. índice 1, 2, 3...)
        for (int i = 0; i < itemPanels.size(); i++) {
            itemPanels.get(i).updateIndex(i + 1);
        }

        updateTitle();
        revalidate();
        repaint();
        triggerSave();
    }

    /**
     * Actualiza el texto de la etiqueta principal del encabezado para reflejar
     * la cantidad actual de ejecutores registrados en la lista.
     */
    private void updateTitle() {
        titleLabel.setText("Workflow Task Executor (" + itemPanels.size() + ")");
    }

    /**
     * Dispara la notificación de cambio ejecutando el callback proporcionado en el constructor.
     * Esto asegura que cualquier cambio estructural notifique al editor para marcar el archivo como modificado.
     */
    private void triggerSave() {
        if (onModelChange != null) {
            onModelChange.run();
        }
    }
}