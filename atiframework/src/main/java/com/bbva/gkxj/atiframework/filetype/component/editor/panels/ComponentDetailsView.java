package com.bbva.gkxj.atiframework.filetype.component.editor.panels;

import com.bbva.gkxj.atiframework.components.*;
import com.bbva.gkxj.atiframework.filetype.workflow.utils.WorkflowThemeUtils;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;

import static com.bbva.gkxj.atiframework.filetype.component.utils.ComponentConstants.*;

/**
 * Vista principal de detalles generales del componente.
 * <p>
 * Representa la tarjeta superior del editor, donde se configuran los metadatos esenciales
 * del archivo (código, versión, estado, tipo y descripción).
 * <br><br>
 * <b>Comportamiento Dinámico:</b> Implementa una zona reactiva ({@code dynamicPanel}) que
 * se reconstruye automáticamente según el "Type" seleccionado. Esto permite cambiar
 * la etiqueta del campo Subtipo (ej. "Adapter Type" vs "Enricher Type") o mostrar
 * un conjunto de campos totalmente distinto (como las estrategias en el caso de Aggregators).
 * </p>
 */
public class ComponentDetailsView extends JPanel {

    // --- Campos Principales ---
    /** Campo de texto para el código identificador del componente. */
    private final AtiTextField componentCodeField = new AtiTextField();

    /** Campo de texto para la versión del componente. */
    private final AtiTextField versionField = new AtiTextField();

    /** Campo de texto para el estado del componente (ej. Draft, Final). */
    private final AtiTextField statusField = new AtiTextField();

    /** Desplegable para seleccionar la tipología principal del nodo (Input, Output, Filter, etc.). */
    private final AtiComboBox nodeTypeField = new AtiComboBox(ALL_TYPES);

    /** Área de texto redimensionable para introducir la descripción del componente. */
    private final AtiResizableTextArea descriptionArea;

    // --- Campos Dinámicos ---
    /** Desplegable que contiene las opciones de subtipo, dependientes del tipo principal seleccionado. */
    private final AtiComboBox subtypeField = new AtiComboBox(new Object[0]);

    /** Envoltorio visual que permite mutar la etiqueta del subtipo dinámicamente. */
    private AtiLabeledComponent subtypeWrapper;

    // --- Estrategias (Exclusivas para el tipo Aggregator) ---
    private final AtiComboBox correlationStrategyField = new AtiComboBox(CORRELATION_STRATEGIES);
    private final AtiComboBox aggregationStrategyField = new AtiComboBox(AGGREGATION_STRATEGIES);
    private final AtiComboBox releaseStrategyField = new AtiComboBox(RELEASE_STRATEGIES);

    /** Contenedor cuyo contenido se vacía y repinta dependiendo del {@code nodeTypeField}. */
    private JPanel dynamicPanel;

    // --- Constantes de Diseño ---
    private static final Color COLOR_WHITE = Color.WHITE;
    private static final Color COLOR_NAVY = new Color(0, 51, 102);
    private static final Color COLOR_BORDER_LIGHT = new Color(210, 210, 210);

    /**
     * Construye la vista de detalles generales.
     * Inicializa los componentes, aplica los estilos corporativos y ensambla la estructura visual.
     */
    public ComponentDetailsView() {
        JTextArea textArea = new JTextArea(3, 40);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        descriptionArea = new AtiResizableTextArea("Description", textArea, true);

        applyWorkflowStyles();
        createUIComponents();
    }

    /**
     * Aplica la temática y estilos del framework a todos los componentes interactivos del formulario.
     */
    private void applyWorkflowStyles() {
        WorkflowThemeUtils.applyWorkflowTheme(componentCodeField);
        WorkflowThemeUtils.applyWorkflowTheme(versionField);
        WorkflowThemeUtils.applyWorkflowTheme(statusField);
        WorkflowThemeUtils.applyWorkflowTheme(nodeTypeField);
        WorkflowThemeUtils.applyWorkflowTheme(subtypeField);
        WorkflowThemeUtils.applyWorkflowTheme(correlationStrategyField);
        WorkflowThemeUtils.applyWorkflowTheme(aggregationStrategyField);
        WorkflowThemeUtils.applyWorkflowTheme(releaseStrategyField);
        WorkflowThemeUtils.applyWorkflowTheme(descriptionArea);
    }

    /**
     * Ensambla la estructura del panel utilizando una combinación de BorderLayout para el marco
     * principal y GridBagLayout para la alineación precisa del formulario interno.
     */
    private void createUIComponents() {
        setLayout(new BorderLayout());
        setOpaque(false);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(COLOR_WHITE);
        card.setBorder(BorderFactory.createLineBorder(COLOR_BORDER_LIGHT, 1));

        // Cabecera superior (Franja azul y título)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(COLOR_WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(6, 0, 0, 0, COLOR_NAVY),
                BorderFactory.createEmptyBorder(15, 20, 10, 20)
        ));

        AtiJLabel titleLabel = new AtiJLabel("Component Details");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLabel.setForeground(COLOR_NAVY);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        card.add(headerPanel, BorderLayout.NORTH);

        // Contenedor principal del formulario
        JPanel formContainer = new JPanel(new GridBagLayout());
        formContainer.setBackground(COLOR_WHITE);
        formContainer.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 15, 5);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHWEST;

        // Fila 0: Campos Obligatorios
        c.gridy = 0;
        c.gridx = 0; c.weightx = 1.0;
        formContainer.add(new AtiLabeledComponent("Component Code", componentCodeField), c);
        c.gridx = 1; c.weightx = 0.3;
        formContainer.add(new AtiLabeledComponent("Version", versionField), c);
        c.gridx = 2; c.weightx = 0.5;
        formContainer.add(new AtiLabeledComponent("Status", statusField), c);
        c.gridx = 3; c.weightx = 0.5;
        formContainer.add(new AtiLabeledComponent("Type", nodeTypeField), c);

        // Fila 1: Panel Dinámico (Subtipo o Estrategias)
        dynamicPanel = new JPanel(new GridBagLayout());
        dynamicPanel.setBackground(COLOR_WHITE);
        c.gridx = 0; c.gridy = 1; c.gridwidth = 4;
        formContainer.add(dynamicPanel, c);

        // Fila 2: Descripción
        c.gridy = 2; c.gridwidth = 4;
        formContainer.add(descriptionArea, c);

        card.add(formContainer, BorderLayout.CENTER);
        add(card, BorderLayout.CENTER);
    }

    /**
     * Vacía y repuebla el panel dinámico aplicando la matriz de tipos y subtipos
     * definida por la documentación funcional.
     * <p>
     * Se invoca automáticamente cuando cambia el valor del combo "Type" o durante
     * la carga inicial de datos desde el JSON.
     * </p>
     *
     * @param type El tipo de componente seleccionado (ej. "Input Adapter", "Aggregator").
     */
    public void updateDynamicFields(String type) {
        dynamicPanel.removeAll();
        GridBagConstraints dc = new GridBagConstraints();
        dc.insets = new Insets(0, 0, 0, 20);
        dc.fill = GridBagConstraints.HORIZONTAL;
        dc.weightx = 1.0;
        dc.gridy = 0;

        String subtypeLabel = "Subtype";
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();

        switch (type != null ? type : "") {
            case TYPE_INPUT_ADAPTER:
            case TYPE_OUTPUT_ADAPTER:
                subtypeLabel = "Adapter Type";
                model.addElement(SUBTYPE_JMS);
                model.addElement(SUBTYPE_ASYNC_API);
                if (TYPE_OUTPUT_ADAPTER.equals(type)) model.addElement(SUBTYPE_DATABASE);
                break;

            case TYPE_ENRICHER:
                subtypeLabel = "Enricher Type";
                model.addElement(SUBTYPE_WORKSTATEMENT);
                break;

            case TYPE_SPLITTER:
                subtypeLabel = "Splitter Type";
                model.addElement("Java Class");
                model.addElement("Javascript");
                model.addElement("By Root Element");
                break;

            case TYPE_AGGREGATOR:
                dc.gridx = 0; dynamicPanel.add(new AtiLabeledComponent("Correlation Strategy", correlationStrategyField), dc);
                dc.gridx = 1; dynamicPanel.add(new AtiLabeledComponent("Aggregation Strategy", aggregationStrategyField), dc);
                dc.gridx = 2; dynamicPanel.add(new AtiLabeledComponent("Release Strategy", releaseStrategyField), dc);
                break;
        }

        // Si no es un Aggregator y el modelo tiene elementos, dibujamos el combo de subtipo
        if (!TYPE_AGGREGATOR.equals(type) && model.getSize() > 0) {
            subtypeField.setModel(model);
            subtypeWrapper = new AtiLabeledComponent(subtypeLabel, subtypeField);
            dc.gridx = 0;
            dynamicPanel.add(subtypeWrapper, dc);
        }

        dynamicPanel.revalidate();
        dynamicPanel.repaint();
    }

    // =================================================================================
    // GETTERS PARA EL CONTROLADOR
    // =================================================================================

    public AtiTextField getComponentCodeField() { return componentCodeField; }
    public AtiTextField getVersionField() { return versionField; }
    public AtiTextField getStatusField() { return statusField; }
    public AtiComboBox getNodeTypeField() { return nodeTypeField; }

    /** Devuelve el desplegable de subtipo, cuyo contenido varía dependiendo del tipo principal. */
    public AtiComboBox getSubtypeField() { return subtypeField; }

    public AtiComboBox getCorrelationStrategyField() { return correlationStrategyField; }
    public AtiComboBox getAggregationStrategyField() { return aggregationStrategyField; }
    public AtiComboBox getReleaseStrategyField() { return releaseStrategyField; }
    public JTextArea getDescriptionField() { return descriptionArea.getTextArea(); }
}