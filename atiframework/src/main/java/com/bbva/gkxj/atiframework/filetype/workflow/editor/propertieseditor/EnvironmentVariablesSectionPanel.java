package com.bbva.gkxj.atiframework.filetype.workflow.editor.propertieseditor;

import com.bbva.gkxj.atiframework.filetype.workflow.model.WorkflowJsonData;
import com.bbva.gkxj.atiframework.filetype.workflow.model.WorkflowJsonData.EnvironmentVariableData;
import com.intellij.icons.AllIcons;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel de sección encargado de gestionar la lista dinámica de variables de entorno del Workflow.
 * <p>
 * Proporciona una interfaz con un encabezado funcional que permite visualizar, añadir y eliminar
 * configuraciones de variables. Coordina una lista de componentes {@link EnvironmentVariableItemPanel}
 * y mantiene la sincronización entre la vista y el modelo de datos.
 * </p>
 * <p>
 * <b>Características principales:</b>
 * <ul>
 * <li>Numeración correlativa automática que se actualiza tras añadir o borrar elementos.</li>
 * <li>Capacidad de expandir y contraer la lista visualmente (Toggle).</li>
 * <li>Alineación estricta a la izquierda para los elementos de la cabecera usando {@link JLabel} para evitar márgenes nativos.</li>
 * </ul>
 * </p>
 */
public class EnvironmentVariablesSectionPanel extends JPanel {

    /** Contenedor interno donde se añaden dinámicamente los sub-paneles de edición de cada variable. */
    private final JPanel listContainer;

    /** Etiqueta que muestra el título de la sección y el conteo actualizado de variables definidas. */
    private final JLabel titleLabel;

    /** Lista de referencias a los paneles de ítems activos en la interfaz gráfica para la recolección de datos. */
    private final List<EnvironmentVariableItemPanel> itemPanels = new ArrayList<>();

    /** Callback para notificar cambios estructurales o de contenido al editor principal y disparar el guardado. */
    private final Runnable onModelChange;

    /** Estado actual del panel ({@code true} si la lista está visible, {@code false} si está oculta). */
    private boolean isExpanded = true;

    /**
     * Construye el panel de la sección de variables de entorno inicializando su interfaz gráfica.
     * Configura el encabezado con los controles de expansión y adición.
     * * @param onModelChange Callback para disparar la persistencia cuando se añade, elimina o modifica un ítem.
     */
    public EnvironmentVariablesSectionPanel(Runnable onModelChange) {
        this.onModelChange = onModelChange;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setAlignmentX(Component.LEFT_ALIGNMENT);
        setOpaque(false);

        // --- Configuración del Encabezado usando BorderLayout para anclaje estricto ---
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerRow.setBorder(JBUI.Borders.empty(5, 0));
        headerRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        // Sub-panel izquierdo (Flecha + Texto) anclado firmemente
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.X_AXIS));
        leftPanel.setOpaque(false);

        // USAMOS JLABEL: Cero márgenes nativos, alineación perfecta a la izquierda
        JLabel btnToggle = new JLabel(AllIcons.General.ArrowDown);
        btnToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnToggle.setToolTipText("Expand/Collapse");

        // Título de la sección
        titleLabel = new JLabel("Workflow Environment variables (0)");
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        leftPanel.add(btnToggle);
        leftPanel.add(Box.createRigidArea(new Dimension(6, 0))); // Separación exacta de 6px
        leftPanel.add(titleLabel);

        // USAMOS JLABEL para el botón de añadir (+)
        JLabel btnAdd = new JLabel(AllIcons.General.Add);
        btnAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAdd.setToolTipText("Add Environment Variable");

        // Anclamos los bloques a los extremos
        headerRow.add(leftPanel, BorderLayout.WEST);
        headerRow.add(btnAdd, BorderLayout.EAST);
        add(headerRow);

        // --- Contenedor de la Lista de ítems ---
        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        listContainer.setOpaque(false);
        add(listContainer);

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
                // Autodespliegue si la lista estaba contraída
                if (!isExpanded) {
                    isExpanded = true;
                    listContainer.setVisible(true);
                    btnToggle.setIcon(AllIcons.General.ArrowDown);
                }
                addNewItem(new EnvironmentVariableData());
                triggerSave();
            }
        });
    }

    /**
     * Limpia la lista visual actual y carga los datos de variables de entorno desde el modelo proporcionado.
     * * @param dataList Lista de objetos {@link EnvironmentVariableData} a renderizar. Si es nula, limpia la lista.
     */
    public void loadData(List<EnvironmentVariableData> dataList) {
        listContainer.removeAll();
        itemPanels.clear();

        if (dataList != null) {
            for (EnvironmentVariableData data : dataList) {
                addNewItem(data);
            }
        }
        updateTitle();
        revalidate();
        repaint();
    }

    /**
     * Recolecta la información introducida en todos los paneles de ítems activos y la guarda
     * en el objeto de datos global.
     * * @param globalData Objeto {@link WorkflowJsonData} donde se inyectará la lista de variables actualizada.
     */
    public void saveData(WorkflowJsonData globalData) {
        List<EnvironmentVariableData> currentData = new ArrayList<>();
        for (EnvironmentVariableItemPanel panel : itemPanels) {
            currentData.add(panel.getData());
        }
        // Evita guardar un array vacío en el JSON si no hay elementos
        globalData.setEnvironmentVariables(currentData.isEmpty() ? null : currentData);
    }

    /**
     * Crea un nuevo panel de ítem para una variable, lo registra en la lista interna y lo añade a la UI.
     * * @param data Los datos de la variable (puede ser una instancia vacía para nuevos registros o una existente).
     */
    private void addNewItem(EnvironmentVariableData data) {
        int index = itemPanels.size() + 1;
        EnvironmentVariableItemPanel itemPanel = new EnvironmentVariableItemPanel(data, index, this::removeItem, onModelChange);
        itemPanels.add(itemPanel);
        listContainer.add(itemPanel);

        updateTitle();
        revalidate();
        repaint();
    }

    /**
     * Elimina un panel de ítem específico de la lista y actualiza la interfaz.
     * Recalcula los índices de los paneles restantes para mantener la numeración correlativa (ej. 01, 02...).
     * * @param panel El componente {@link EnvironmentVariableItemPanel} que ha solicitado su eliminación.
     */
    private void removeItem(EnvironmentVariableItemPanel panel) {
        itemPanels.remove(panel);
        listContainer.remove(panel);

        // Re-indexar los ítems restantes
        for (int i = 0; i < itemPanels.size(); i++) {
            itemPanels.get(i).updateIndex(i + 1);
        }

        updateTitle();
        revalidate();
        repaint();
        triggerSave();
    }

    /**
     * Actualiza el texto del título principal del encabezado reflejando el número actual de variables registradas.
     */
    private void updateTitle() {
        titleLabel.setText("Workflow Environment variables (" + itemPanels.size() + ")");
    }

    /**
     * Dispara la notificación de cambio ejecutando el callback proporcionado en el constructor.
     * Esto asegura que el editor principal marque el archivo como modificado.
     */
    private void triggerSave() {
        if (onModelChange != null) {
            onModelChange.run();
        }
    }
}