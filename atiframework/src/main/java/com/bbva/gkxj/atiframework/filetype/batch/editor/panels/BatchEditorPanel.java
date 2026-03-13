package com.bbva.gkxj.atiframework.filetype.batch.editor.panels;

import com.bbva.gkxj.atiframework.filetype.batch.editor.utils.*;
import com.bbva.gkxj.atiframework.components.AtiCircularIconButton;
import com.bbva.gkxj.atiframework.components.AtiScriptPanel;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.IconUtil;
import com.intellij.util.ui.JBUI;
import icons.AtiIcons;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * Panel principal del editor de ficheros Batch.
 * Combina un lienzo interactivo para el diagrama y un panel lateral de propiedades
 * que reacciona de forma dinamica al elemento seleccionado en el grafo.
 */
public class BatchEditorPanel extends JPanel {

    /**
     * Lista que almacena las referencias a los componentes graficos dinamicos para
     * poder gestionar su estado de edicion de forma centralizada.
     */
    private final List<JComponent> mainDynamicFields = new ArrayList<>();

    /**
     * Color de fondo principal utilizado en las areas generales y contenedores del editor.
     */
    private static final Color BG_MAIN = new JBColor(new Color(0xF2F2F2), new Color(0x3C3F41));

    /**
     * Color de fondo aplicado especificamente a la barra de herramientas superior.
     */
    private static final Color BG_TOOLBAR = new JBColor(new Color(0xEAEAEA), new Color(0x313335));

    /**
     * Color estandar empleado para los bordes separadores principales de la interfaz.
     */
    private static final Color BORDER_COLOR = new JBColor(new Color(0xD1D1D1), new Color(0x555555));

    /**
     * Color destacado aplicado a los textos de los titulos principales de los paneles.
     */
    private static final Color TEXT_COLOR_TITLE = new JBColor(Color.BLACK, Color.LIGHT_GRAY);

    /**
     * Color tenue utilizado para el texto de las etiquetas secundarias y descriptivas de los campos.
     */
    private static final Color TEXT_COLOR_LABEL = new JBColor(Color.GRAY, Color.GRAY);

    /**
     * Color aplicado a los bordes de los campos de texto estaticos y areas de entrada de datos.
     */
    private static final Color STATIC_BORDER_COLOR = new JBColor(new Color(0xB0B0B0), new Color(0x5E6060));

    /**
     * Panel contenedor principal encargado de gestionar y mostrar el lienzo interactivo del diagrama BPMN.
     */
    private BpmnEditorPanel bpmnEditorPanel;

    /**
     * Contenedor base del panel lateral derecho donde se ubican las opciones de edicion.
     */
    private JPanel propertiesContainer;

    /**
     * Panel interior estructurado que alberga dinamicamente los campos de formulario autogenerados.
     */
    private JPanel propertiesContent;

    /**
     * Envoltorio de desplazamiento que permite navegar verticalmente por el contenido del panel de propiedades.
     */
    private JBScrollPane propertiesScrollPane;

    /**
     * Boton de accion interactivo destinado a alternar la visibilidad del panel lateral de configuracion.
     */
    private JButton togglePropertiesBtn;

    /**
     * Diccionario en formato JSON que custodia el estado actual y la estructura del fichero editado.
     */
    private JsonObject currentJson;

    /**
     * Identificador unico de la celda, nodo o arista, que posee el foco de seleccion actualmente en el lienzo.
     */
    private String currentSelectionId = null;

    /**
     * Bandera logica que determina si el elemento activo en la seleccion es una conexion lineal en lugar de un nodo.
     */
    private boolean isSelectionEdge = false;

    /**
     * Bandera que dicta si la interfaz grafica admite la introduccion de cambios o restringe los campos a modo lectura.
     */
    private boolean isEditMode = true;

    /**
     * Bandera de seguridad empleada para silenciar eventos y evitar bucles de recursion durante la carga o reconstruccion de datos.
     */
    private boolean isUpdatingFromJson = false;

    /**
     * Oyente de eventos almacenado en memoria para interceptar y gestionar los cambios de texto en los campos de formulario.
     */
    private DocumentListener cachedTextListener;

    /**
     * Oyente de eventos capturado globalmente para reaccionar ante interacciones de elementos como botones e interruptores.
     */
    private ActionListener cachedActionListener;

    /**
     * Campo de entrada destinado a la edicion y visualizacion del codigo global asigando al proceso Batch.
     */
    private JBTextField batchCodeField = new JBTextField();

    /**
     * Campo de entrada orientado a gestionar el identificador interno o tecnico del fichero general.
     */
    private JBTextField idField = new JBTextField();

    /**
     * Campo de texto destinado a proyectar el estado de ciclo de vida en el que se encuentra el fichero actual.
     */
    private JBTextField statusField = new JBTextField();

    /**
     * Area de texto expandida disenada para albergar notas, documentacion o descripciones largas del componente global.
     */
    private JTextArea descriptionArea = new JTextArea(3, 20);

    /**
     * Panel modular que aloja verticalmente el listado de parametros correspondientes a la ejecucion global del lote.
     */
    private JPanel batchParamsContainer;

    /**
     * Coleccion en memoria que mantiene rastreadas las referencias a las filas visuales creadas para los parametros de Batch.
     */
    private final java.util.List<BatchParamRow> batchParamRows = new ArrayList<>();

    /**
     * Campo de entrada que permite editar el nombre o codigo especifico asignado a un paso individual del flujo.
     */
    private JBTextField stepCodeField = new JBTextField();

    /**
     * Campo de entrada visualizado habitualmente en modo lectura que expone la identificacion unica del paso enfocado.
     */
    private JBTextField stepIdField = new JBTextField();

    /**
     * Interruptor grafico habilitado para catalogar y alternar si el paso seleccionado detenta un rol critico en el proceso.
     */
    private ToggleSwitch criticalStepSwitch = new ToggleSwitch();

    /**
     * Panel contenedor que engloba modularmente todos los parametros locales vinculados al paso activo.
     */
    private JPanel stepParamsContainer;

    /**
     * Divisor redimensionable que reparte el espacio en pantalla entre el area del grafo central y el menu de propiedades lateral.
     */
    private JSplitPane mainSplitPane;

    /**
     * Inventario de referencias de memoria encargado de trazar las filas de variables y propiedades de un paso especifico.
     */
    private final List<StepParamRow> stepParamRows = new ArrayList<>();

    /**
     * Campo basico dispuesto para visualizar la identificacion en sistema de un bloque de tipo bucle.
     */
    private JBTextField loopIdField = new JBTextField();

    /**
     * Interruptor visual empleado para dictaminar si el bucle seleccionado actua como cierre, colapsando su panel de rutinas.
     */
    private ToggleSwitch endLoopSwitch = new ToggleSwitch();

    /**
     * Area de redaccion de texto capacitada para recibir el conjunto de instrucciones o el script inicial de entrada al bucle.
     */
    private AtiScriptPanel initScriptArea = new AtiScriptPanel(3);

    /**
     * Area de edicion provista para alojar el algoritmo de evaluacion que certifica la ruptura y salida del bucle en curso.
     */
    private AtiScriptPanel exitScriptArea = new AtiScriptPanel(3);

    /**
     * Area destinada a albergar la logica y el script puntual encargado de manipular variables tras finalizar cada iteracion.
     */
    private AtiScriptPanel incrementScriptArea = new AtiScriptPanel(3);

    /**
     * Contenedor que amalgama las tres areas de codificacion del bucle con el fin de aislarlas visualmente segun requiera la configuracion.
     */
    private JPanel loopScriptsContainer;

    /**
     * Area de texto concebida para integrar la regla o script condicional que debe satisfacerse para transitar a traves de la conexion.
     */
    private AtiScriptPanel conditionScriptArea = new AtiScriptPanel(3);

    /**
     * Campo que externaliza en la interfaz la identificacion elemental de nodos sin parametros profundos de edicion.
     */
    private JBTextField simpleNodeIdField = new JBTextField();

    /**
     * Representacion abstracta del documento o archivo original abierto dentro del ecosistema del entorno de desarrollo.
     */
    private VirtualFile virtualFile;

    /**
     * Referencia al proyecto global de IntelliJ en el que se integra el editor, utilizada para operaciones de busqueda y apertura de archivos relacionados.
     */
    private final Project project;

    /**
     * Etiqueta referenciada de la cabecera dedicada a llevar el conteo de elementos alojados en la seccion de parametros del Batch.
     */
    private JLabel batchParamsHeaderLabel;

    /**
     * Etiqueta capturada en memoria cuyo cometido es contabilizar y rotular la seccion replegable de parametros correspondientes al paso.
     */
    private JLabel stepParamsHeaderLabel;

    /**
     * Constructor del panel de edicion global.
     * Recupera el documento en memoria del archivo y lanza la inicializacion de la interfaz.
     *
     * @param project Proyecto actual de IntelliJ.
     * @param virtualFile Archivo fisico o virtual que se esta editando.
     */
    public BatchEditorPanel(@Nullable Project project, VirtualFile virtualFile) {
        super(new BorderLayout());
        this.virtualFile = virtualFile;
        this.project = project;
        Document myDocument = FileDocumentManager.getInstance().getDocument(virtualFile);
        initComponents();
    }

    /**
     * Inicializa y ensambla todas las piezas visuales del panel, integrando la barra superior,
     * el lienzo del grafo, el panel de propiedades lateral y configurando el divisor ajustable.
     */
    private void initComponents() {
        this.add(createTopStack(), BorderLayout.NORTH);

        this.bpmnEditorPanel = new BpmnEditorPanel();
        this.bpmnEditorPanel.setOnOpenSubProcess(nodeId -> {
            if (currentJson == null || !currentJson.has("workflowNodeList")) return;

            String batchNameToFind = null;

            JsonArray nodes = currentJson.getAsJsonArray("workflowNodeList");
            for (JsonElement el : nodes) {
                if (el.isJsonObject()) {
                    JsonObject node = el.getAsJsonObject();
                    if (node.has("workflowNodeCode") && node.get("workflowNodeCode").getAsString().equals(nodeId)) {
                        if (node.has("batchCode") && !node.get("batchCode").isJsonNull()) {
                            batchNameToFind = node.get("batchCode").getAsString();
                        }
                        break;
                    }
                }
            }

            if (batchNameToFind != null && !batchNameToFind.isEmpty() && project != null) {
                String extension = (virtualFile != null && virtualFile.getExtension() != null) ? virtualFile.getExtension() : "batch";
                String fileName = batchNameToFind + "." + extension;

                Collection<VirtualFile> foundFiles = FilenameIndex.getVirtualFilesByName(
                        fileName,
                        GlobalSearchScope.allScope(project)
                );

                if (!foundFiles.isEmpty()) {
                    VirtualFile fileToOpen = foundFiles.iterator().next();
                    FileEditorManager.getInstance(project).openFile(fileToOpen, true);
                } else {
                    JOptionPane.showMessageDialog(this, "File not found: " + fileName, "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        this.add(new BpmnEditorPanel(), BorderLayout.CENTER);

        propertiesContainer = new JPanel(new BorderLayout());
        propertiesContainer.setPreferredSize(new Dimension(350, -1));
        propertiesContainer.setMinimumSize(new Dimension(200, -1));
        propertiesContainer.setBorder(new MatteBorder(0, 1, 0, 0, BORDER_COLOR));

        propertiesContent = new JPanel();
        propertiesContent.setLayout(new BoxLayout(propertiesContent, BoxLayout.Y_AXIS));
        propertiesContent.setBackground(new JBColor(new Color(0xF8F8F8), new Color(0xF8F8F8)));
        propertiesContent.setBorder(JBUI.Borders.empty(10, 15));
        propertiesContent.setAlignmentX(Component.LEFT_ALIGNMENT);

        propertiesScrollPane = new JBScrollPane(propertiesContent);
        propertiesScrollPane.setBorder(null);
        propertiesScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        propertiesScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        propertiesContainer.add(propertiesScrollPane, BorderLayout.CENTER);

        mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, bpmnEditorPanel, propertiesContainer);
        mainSplitPane.setResizeWeight(1.0);
        mainSplitPane.setDividerSize(6);
        mainSplitPane.setContinuousLayout(true);
        mainSplitPane.setBorder(null);

        this.add(mainSplitPane, BorderLayout.CENTER);

        refreshPropertiesPanel();
    }

    /**
     * Construye la cabecera superior del editor, que incluye el titulo del fichero
     * y la barra de herramientas principal con la opcion de ocultar el panel de propiedades.
     *
     * @return El panel contenedor de la parte superior ensamblado.
     */
    private JPanel createTopStack() {
        JPanel stack = new JPanel();
        stack.setLayout(new BoxLayout(stack, BoxLayout.Y_AXIS));

        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(BG_MAIN);
        titleBar.setBorder(JBUI.Borders.empty(5, 20, 10, 20));

        JLabel mainTitleLabel = new JLabel(this.virtualFile.getNameWithoutExtension());
        mainTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        mainTitleLabel.setForeground(TEXT_COLOR_TITLE);
        titleBar.add(mainTitleLabel, BorderLayout.WEST);
        stack.add(titleBar);

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(BG_TOOLBAR);
        toolbar.setBorder(new CompoundBorder(new MatteBorder(1, 0, 1, 0, BORDER_COLOR), JBUI.Borders.empty(5, 20)));

        JPanel rightTools = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        rightTools.setOpaque(false);
        togglePropertiesBtn = new JButton("Properties Panel", AllIcons.General.Menu);
        togglePropertiesBtn.setBorderPainted(false);
        togglePropertiesBtn.setContentAreaFilled(false);
        togglePropertiesBtn.setFocusPainted(false);
        togglePropertiesBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        togglePropertiesBtn.setForeground(TEXT_COLOR_LABEL);
        togglePropertiesBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        togglePropertiesBtn.addActionListener(e -> toggleSidebar());
        rightTools.add(togglePropertiesBtn);
        toolbar.add(rightTools, BorderLayout.EAST);

        stack.add(toolbar);
        return stack;
    }

    /**
     * Alterna la visibilidad del panel lateral derecho de propiedades, actualizando
     * el icono del boton asociado y forzando la reorganizacion del divisor central.
     */
    private void toggleSidebar() {
        boolean isVisible = propertiesContainer.isVisible();
        propertiesContainer.setVisible(!isVisible);
        togglePropertiesBtn.setIcon(isVisible ? AllIcons.General.Menu : AllIcons.Actions.Close);
        mainSplitPane.resetToPreferredSizes();
    }

    /**
     * Modifica el estado global de edicion y lo propaga a todos los campos dinamicos registrados.
     *
     * @param enableEditing Verdadero para permitir la escritura, falso para establecer modo solo lectura.
     */
    private void updateEditableState(boolean enableEditing) {
        this.isEditMode = enableEditing;
        for (JComponent f : mainDynamicFields) {
            applyEditableStyle(f, enableEditing);
        }
        for (BatchParamRow r : batchParamRows) {
            r.updateEditableState(enableEditing);
        }
        for (StepParamRow r : stepParamRows) {
            r.updateEditableState(enableEditing);
        }
    }

    /**
     * Ajusta individualmente las propiedades de visualizacion y habilitacion de un componente
     * segun su configuracion previa y el estado global de edicion.
     *
     * @param field Componente grafico a modificar.
     * @param isGlobalEditMode Estado de edicion actual del formulario completo.
     */
    private void applyEditableStyle(JComponent field, boolean isGlobalEditMode) {
        Boolean p = (Boolean) field.getClientProperty("canBeEdited");
        boolean editable = (p != null && p) && isGlobalEditMode;
        if (field instanceof JTextComponent) {
            ((JTextComponent) field).setEditable(editable);
            field.setForeground(editable ? UIManager.getColor("TextField.foreground") : JBColor.GRAY);
        } else {
            field.setEnabled(editable);
        }
    }

    /**
     * Limpia y reconstruye integramente el panel de propiedades lateral adaptandolo al
     * elemento actualmente seleccionado en el grafo visual.
     */
    private void refreshPropertiesPanel() {
        Set<Integer> expandedBatchParams = new HashSet<>();
        for (int i = 0; i < batchParamRows.size(); i++) if (batchParamRows.get(i).isExpanded()) expandedBatchParams.add(i);
        Set<Integer> expandedStepParams = new HashSet<>();
        for (int i = 0; i < stepParamRows.size(); i++) if (stepParamRows.get(i).isExpanded()) expandedStepParams.add(i);

        Point savedViewPosition = (propertiesScrollPane != null) ? propertiesScrollPane.getViewport().getViewPosition() : new Point(0, 0);

        if (propertiesContainer.getComponentCount() > 0 && propertiesContainer.getLayout() instanceof BorderLayout) {
            Component north = ((BorderLayout) propertiesContainer.getLayout()).getLayoutComponent(BorderLayout.NORTH);
            if (north != null) propertiesContainer.remove(north);
        }

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0xEAEAEA));
        header.setBorder(JBUI.Borders.empty(8, 15));

        String title = "Properties panel";

        BpmnConstants.BpmnPaletteItem item = null;
        if (currentSelectionId != null && currentJson != null && !isSelectionEdge) {
            item = resolveNodeItem(currentSelectionId, currentJson);
        }

        if (currentSelectionId != null) {
            if (isSelectionEdge) {
                title = isConditionalEdge(currentSelectionId, currentJson) ? "Flow Details" : "Flow Properties";
            } else if (item != null) {
                title = item.getLabel();
            } else {
                title = "Node Properties";
            }
        }

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        JButton closeBtn = new JButton(AllIcons.Actions.Close);
        closeBtn.setBorder(null); closeBtn.setContentAreaFilled(false);
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> {
            if (currentSelectionId != null) {
                currentSelectionId = null;
                isSelectionEdge = false;
                refreshPropertiesPanel();
                updateForm(currentJson, false);
            } else {
                toggleSidebar();
            }
        });

        header.add(titleLabel, BorderLayout.WEST);
        header.add(closeBtn, BorderLayout.EAST);
        propertiesContainer.add(header, BorderLayout.NORTH);

        propertiesContent.removeAll();

        if (currentSelectionId == null) {
            renderBatchProperties(propertiesContent, expandedBatchParams);
        } else if (isSelectionEdge) {
            if (isConditionalEdge(currentSelectionId, currentJson)) renderConditionalFlowProperties(propertiesContent);
            else renderSimpleFlowProperties(propertiesContent);
        } else if (item != null) {
            if (item == BpmnConstants.BpmnPaletteItem.LOOP) {
                renderLoopProperties(propertiesContent);
            } else if (item.getNodeType() == BpmnConstants.NODETYPE.STEP) {
                renderStepProperties(propertiesContent, item, expandedStepParams);
            } else {
                renderSimpleNodeProperties(propertiesContent);
            }
        }

        propertiesContent.add(Box.createVerticalGlue());
        propertiesContainer.validate();
        propertiesScrollPane.validate();
        if (propertiesScrollPane != null) propertiesScrollPane.getViewport().setViewPosition(savedViewPosition);
        propertiesContainer.repaint();
        updateEditableState(isEditMode);
    }

    /**
     * Localiza la configuracion de un nodo dentro del documento JSON y deduce el elemento
     * de la paleta correspondiente para poder renderizar las opciones adecuadas.
     *
     * @param id Identificador unico del nodo a buscar.
     * @param json Estructura de datos global en la que se realizara la busqueda.
     * @return El elemento de la paleta correspondiente, o nulo si no se encuentra.
     */
    private BpmnConstants.BpmnPaletteItem resolveNodeItem(String id, JsonObject json) {
        if (json == null || !json.has("workflowNodeList")) return null;
        for (JsonElement el : json.getAsJsonArray("workflowNodeList")) {
            JsonObject node = el.getAsJsonObject();
            if (node.has("workflowNodeCode") && node.get("workflowNodeCode").getAsString().equals(id)) {
                String typeStr = node.has("type") && !node.get("type").isJsonNull()
                        ? node.get("type").getAsString() : null;

                if (typeStr == null) {
                    boolean hasStepCode = node.has("stepCode") && !node.get("stepCode").getAsString().isEmpty();
                    return hasStepCode ? BpmnConstants.BpmnPaletteItem.ETL_STEP : BpmnConstants.BpmnPaletteItem.ETL_STEP;
                }
                return BpmnConstants.fromJsonType(typeStr);
            }
        }
        return null;
    }

    /**
     * Analiza si una arista del grafo representa un flujo de secuencia condicional comprobando
     * el tipo del nodo de origen.
     *
     * @param edgeId Identificador de la arista a evaluar.
     * @param json Estructura de datos global utilizada para resolver los tipos de los nodos vinculados.
     * @return Verdadero si la arista parte de una compuerta que permite evaluar condiciones, falso en caso contrario.
     */
    private boolean isConditionalEdge(String edgeId, JsonObject json) {
        String s = bpmnEditorPanel.getSourceNodeId(edgeId);
        String t = bpmnEditorPanel.getTargetNodeId(edgeId);
        if (s == null && t == null) return false;

        BpmnConstants.BpmnPaletteItem sourceItem = resolveNodeItem(s, json);
        return sourceItem == BpmnConstants.BpmnPaletteItem.INCLUSIVE ||
                sourceItem == BpmnConstants.BpmnPaletteItem.EXCLUSIVE;
    }

    /**
     * Construye y agrupa en el panel proporcionado los campos de formulario globales
     * relacionados con la configuracion del archivo Batch en general.
     *
     * @param content Panel contenedor donde se añadiran los controles.
     * @param expandedIndices Conjunto de indices que determinan que parametros estaban expandidos previamente.
     */
    private void renderBatchProperties(JPanel content, Set<Integer> expandedIndices) {
        mainDynamicFields.clear();
        JPanel general = createCollapsibleSection(content, "General", 0, true, null,  null
        );
        addPropertyField(general, "Batch code", batchCodeField, false);
        addPropertyField(general, "ID", idField, false);
        addPropertyField(general, "Status", statusField, false);
        addTextAreaField(general, "Description", descriptionArea, true);
        content.add(Box.createVerticalStrut(5));

        batchParamsContainer = new JPanel();
        batchParamsContainer.setLayout(new BoxLayout(batchParamsContainer, BoxLayout.Y_AXIS));
        batchParamsContainer.setBackground(content.getBackground());
        batchParamsContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        batchParamsContainer.removeAll();
        batchParamRows.clear();

        if (currentJson != null && currentJson.has("parameterList")) {
            int i = 0;
            for (JsonElement el : currentJson.getAsJsonArray("parameterList")) {
                if (el.isJsonObject()) {
                    BatchParamRow row = addNewBatchParamRow(el.getAsJsonObject());
                    if (expandedIndices != null && expandedIndices.contains(i)) {
                        row.setExpanded(true);
                    }
                    i++;
                }
            }
        }
        JPanel paramsSection = createCollapsibleSection(
                content,
                "Batch Parameters",
                batchParamRows.size(),
                true,
                this::addNewBatchParam,
                lbl -> this.batchParamsHeaderLabel = lbl

        );
        paramsSection.add(batchParamsContainer);
        if (cachedTextListener != null) {
            stepCodeField.getDocument().removeDocumentListener(cachedTextListener);
            stepCodeField.getDocument().addDocumentListener(cachedTextListener);
        }
    }

    /**
     * Construye e inyecta en el panel lateral los campos de formulario correspondientes
     * a las configuraciones especificas de un paso de ejecucion.
     *
     * @param content Panel contenedor base para ubicar los elementos.
     * @param item El tipo concreto de paso que se esta mostrando en el editor.
     * @param expandedIndices Conjunto de parametros individuales que deben restaurarse abiertos.
     */
    private void renderStepProperties(JPanel content, BpmnConstants.BpmnPaletteItem item, Set<Integer> expandedIndices) {
        mainDynamicFields.clear();
        JPanel general = createCollapsibleSection(content, "General", 0, true, null,  null
        );

        String codeLabel = (item == BpmnConstants.BpmnPaletteItem.BATCH_STEP) ? "batch Code" : "step Code";
        addPropertyField(general, codeLabel, stepCodeField, true);
        addSwitchField(general, "Critical Step", criticalStepSwitch, true);
        addPropertyField(general, "ID", stepIdField, false);
        content.add(Box.createVerticalStrut(5));

        stepParamsContainer = new JPanel();
        stepParamsContainer.setLayout(new BoxLayout(stepParamsContainer, BoxLayout.Y_AXIS));
        stepParamsContainer.setBackground(content.getBackground());
        stepParamsContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        stepParamsContainer.removeAll();
        stepParamRows.clear();

        if (currentJson != null && currentSelectionId != null && currentJson.has("workflowNodeList")) {
            for (JsonElement el : currentJson.getAsJsonArray("workflowNodeList")) {
                if (el.isJsonObject()
                        && el.getAsJsonObject().get("workflowNodeCode").getAsString().equals(currentSelectionId)) {
                    if (el.getAsJsonObject().has("wfnParameterList")) {
                        int i = 0;
                        for (JsonElement p : el.getAsJsonObject().getAsJsonArray("wfnParameterList")) {
                            StepParamRow row = addNewStepParamRow(p.getAsJsonObject());
                            if (expandedIndices != null && expandedIndices.contains(i)) {
                                row.setExpanded(true);
                            }
                            i++;
                        }
                    }
                    break;
                }
            }
        }
        JPanel paramsSection = createCollapsibleSection(
                content,
                "Step Parameters",
                stepParamRows.size(),
                true,
                this::addNewStepParam,
                lbl -> this.stepParamsHeaderLabel = lbl
        );
        paramsSection.add(stepParamsContainer);
        if (cachedTextListener != null) {
            stepCodeField.getDocument().removeDocumentListener(cachedTextListener);
            stepCodeField.getDocument().addDocumentListener(cachedTextListener);
        }
        if (cachedActionListener != null) {
            criticalStepSwitch.setOnStateChanged(b -> cachedActionListener.actionPerformed(null));
        }
    }

    /**
     * Dibuja los componentes visuales relacionados a las configuraciones de un bucle,
     * gestionando ademas la ocultacion interactiva de sus areas de scripts asociados.
     *
     * @param content Panel donde se agregaran los campos.
     */
    private void renderLoopProperties(JPanel content) {
        mainDynamicFields.clear();
        JPanel general = createCollapsibleSection(content, "General", 0, true, null, null);
        addPropertyField(general, "ID", loopIdField, false);
        addSwitchField(general, "End Loop", endLoopSwitch, true);

        loopScriptsContainer = new JPanel();
        loopScriptsContainer.setLayout(new BoxLayout(loopScriptsContainer, BoxLayout.Y_AXIS));
        loopScriptsContainer.setBackground(content.getBackground());
        loopScriptsContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        addScriptField(loopScriptsContainer, "Initialization script", initScriptArea, true);
        addScriptField(loopScriptsContainer, "Exit script", exitScriptArea, true);
        addScriptField(loopScriptsContainer, "Increment script", incrementScriptArea, true);
        content.add(loopScriptsContainer);

        endLoopSwitch.setOnStateChanged(isEnd -> {
            loopScriptsContainer.setVisible(!isEnd);
            forceSave();
        });
        loopScriptsContainer.setVisible(!endLoopSwitch.selected);
        if (cachedTextListener != null) {
            initScriptArea.getDocument().addDocumentListener(cachedTextListener);
            exitScriptArea.getDocument().addDocumentListener(cachedTextListener);
            incrementScriptArea.getDocument().addDocumentListener(cachedTextListener);
        }
    }

    /**
     * Renderiza un panel sencillo destinado a configuraciones de nodos que unicamente requieren mostrar su ID.
     *
     * @param content Panel contenedor.
     */
    private void renderSimpleNodeProperties(JPanel content) {
        mainDynamicFields.clear();
        JPanel general = createCollapsibleSection(content, "General", 0, true, null,null);
        addPropertyField(general, "ID", simpleNodeIdField, false);
    }

    /**
     * Inicializa y adjunta un campo de area de texto para definir las expresiones
     * vinculadas a un flujo de secuencia condicional.
     *
     * @param content Panel de propiedades de destino.
     */
    private void renderConditionalFlowProperties(JPanel content) {
        mainDynamicFields.clear();
        JPanel flow = createCollapsibleSection(content, "Flow Details", 0, true, null,null);
        addScriptField(flow, "Script Condition", conditionScriptArea, true);
        if (cachedTextListener != null) {
            conditionScriptArea.getDocument().addDocumentListener(cachedTextListener);
        }
    }

    /**
     * Pinta la informacion basica requerida para representar un flujo simple entre nodos,
     * consistente de manera predeterminada en un mero indicador textual estatico.
     *
     * @param content Panel lateral contenedor.
     */
    private void renderSimpleFlowProperties(JPanel content) {
        mainDynamicFields.clear();
        JPanel flowSection = createCollapsibleSection(content, "General", 0, true, null,null);
        JLabel l = new JLabel("Sequence Flow");
        l.setForeground(TEXT_COLOR_LABEL);
        l.setBorder(JBUI.Borders.empty(5));
        flowSection.add(l);
    }

    /**
     * Crea e inserta una nueva fila vacia en el formulario de parametros globales del lote
     * y emite la orden de guardado.
     */
    private void addNewBatchParam() {
        addNewBatchParamRow(null);
        forceSave();
    }

    /**
     * Ensambla una nueva fila en el panel con los campos para un parametro especifico de Batch,
     * enlazandola con sus datos JSON iniciales si se suministran.
     *
     * @param data Objeto con la informacion previa a volcar, nulo si es una fila nueva y vacia.
     * @return El componente visual que modela la nueva fila de configuracion del parametro.
     */
    private BatchParamRow addNewBatchParamRow(JsonObject data) {
        int idx = batchParamRows.size() + 1;
        BatchParamRow row = new BatchParamRow(batchParamsContainer, idx, r -> {
            batchParamsContainer.remove(r.rootPanel);
            batchParamRows.remove(r);
            for (int k = 0; k < batchParamRows.size(); k++) {
                batchParamRows.get(k).setIndex(k + 1);
            }
            if (batchParamsHeaderLabel != null) {
                batchParamsHeaderLabel.setText("Batch Parameters (" + batchParamRows.size() + ")");
            }
            batchParamsContainer.revalidate();
            batchParamsContainer.repaint();
            forceSave();
        });

        batchParamRows.add(row);

        if (data != null) {
            if (data.has("name")) {
                row.paramNameField.setText(data.get("name").getAsString());
                row.updateHeader();
            }
            if (data.has("schedulerParamName")) {
                row.schedulerNameField.setText(data.get("schedulerParamName").getAsString());
            }
            if (data.has("type")) {
                row.typeCombo.setSelectedItem(data.get("type").getAsString());
            }
            if (data.has("ioType")) {
                row.ioTypeCombo.setSelectedItem(data.get("ioType").getAsString());
            }
            if (data.has("isMandatory")) {
                row.mandatorySwitch.selected = data.get("isMandatory").getAsBoolean();
            }
            if (data.has("description")) {
                row.descriptionArea.setText(data.get("description").getAsString());
            }
            if (data.has("scriptValue")) {
                row.scriptArea.setText(data.get("scriptValue").getAsString());
            }
        }

        if (cachedTextListener != null) {
            row.addListeners(cachedTextListener, cachedActionListener);
        }

        row.updateEditableState(isEditMode);
        batchParamsContainer.revalidate();
        batchParamsContainer.repaint();
        if (batchParamsHeaderLabel != null) {
            batchParamsHeaderLabel.setText("Batch Parameters (" + batchParamRows.size() + ")");
        }
        return row;
    }

    /**
     * Agrega un nuevo componente de parametro vacio al panel de parametros del paso activo
     * y obliga a reevaluar y guardar el documento.
     */
    private void addNewStepParam() {
        addNewStepParamRow(null);
        forceSave();
    }

    /**
     * Construye una linea completa de formulario para un parametro vinculado a un paso,
     * volcando el contexto si este existe e integrando funcionalidad de eliminacion.
     *
     * @param data JsonObject con la informacion que debe inyectarse en los componentes.
     * @return El manejador visual de la fila insertada en el entorno grafico.
     */
    private StepParamRow addNewStepParamRow(JsonObject data) {
        int idx = stepParamRows.size() + 1;
        StepParamRow row = new StepParamRow(stepParamsContainer, idx, r -> {
            stepParamsContainer.remove(r.rootPanel);
            stepParamRows.remove(r);
            for (int k = 0; k < stepParamRows.size(); k++) {
                stepParamRows.get(k).setIndex(k + 1);
            }
            if (stepParamsHeaderLabel != null) {
                stepParamsHeaderLabel.setText("Step Parameters (" + stepParamRows.size() + ")");
            }
            stepParamsContainer.revalidate();
            stepParamsContainer.repaint();
            forceSave();
        });

        stepParamRows.add(row);

        if (data != null) {
            if (data.has("name")) {
                row.paramNameField.setText(data.get("name").getAsString());
                row.updateHeader();
            }
            if (data.has("batchParamName")) {
                row.batchParamNameField.setText(data.get("batchParamName").getAsString());
            }
            if (data.has("fixedValue")) {
                row.fixedValueField.setText(data.get("fixedValue").getAsString());
            }
            if (data.has("scriptValue")) {
                row.scriptArea.setText(data.get("scriptValue").getAsString());
            }
        }

        if (cachedTextListener != null) {
            row.addListeners(cachedTextListener, cachedActionListener);
        }

        row.updateEditableState(isEditMode);
        stepParamsContainer.revalidate();
        stepParamsContainer.repaint();
        if (stepParamsHeaderLabel != null) {
            stepParamsHeaderLabel.setText("Step Parameters (" + stepParamRows.size() + ")");
        }
        return row;
    }

    /**
     * Utilidad para crear e incrustar un campo de texto estandar y su etiqueta descriptiva
     * con formato homogeneo dentro del panel especificado.
     *
     * @param p Componente contenedor principal al que se añade el campo.
     * @param l Texto a renderizar como encabezado.
     * @param f Componente de campo que se mostrara en la vista.
     * @param e Verdadero si el usuario tendra la capacidad de manipular este campo manualmente.
     */
    private void addPropertyField(JPanel p, String l, JComponent f, boolean e) {
        JPanel pp = new JPanel(new BorderLayout());
        pp.setBackground(p.getBackground());
        pp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        pp.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lbl = new JLabel(l);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(TEXT_COLOR_LABEL);
        pp.add(lbl, BorderLayout.NORTH);
        f.setBorder(new CompoundBorder(JBUI.Borders.customLine(STATIC_BORDER_COLOR), JBUI.Borders.empty(2, 6)));
        f.putClientProperty("canBeEdited", e);
        mainDynamicFields.add(f);
        pp.add(f, BorderLayout.CENTER);
        p.add(pp);
        p.add(Box.createVerticalStrut(10));
    }

    /**
     * Envuelve y adjunta un componente JTextArea dotandolo de margenes preestablecidos
     * y de una etiqueta explicativa.
     *
     * @param p Recipiente visual contenedor de la interfaz a enriquecer.
     * @param l Etiqueta mostrada sobre el area de texto proporcionada.
     * @param a Area de texto a incorporar en el entorno.
     * @param e Condicion de edicion predeterminada permitida al usuario final.
     */
    private void addTextAreaField(JPanel p, String l, JTextArea a, boolean e) {
        JPanel pp = new JPanel(new BorderLayout());
        pp.setBackground(p.getBackground());
        pp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        pp.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lbl = new JLabel(l);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setForeground(TEXT_COLOR_LABEL);
        pp.add(lbl, BorderLayout.NORTH);
        a.setBorder(new CompoundBorder(JBUI.Borders.customLine(STATIC_BORDER_COLOR), JBUI.Borders.empty(5, 8)));
        a.putClientProperty("canBeEdited", e);
        mainDynamicFields.add(a);
        pp.add(a, BorderLayout.CENTER);
        p.add(pp);
        p.add(Box.createVerticalStrut(10));
    }

    /**
     * Despliega un componente del tipo area de texto destinado en especifico a alojar pequeños guiones o algoritmos incrustados.
     *
     * @param p Sector receptor en el que se ubica.
     * @param panel AtiScriptPanel a configurar y añadir al panel.
     * @param e Permite activar o aislar el componente segun logica superior de permisos.
     */
    private void addScriptField(JPanel p, String label, AtiScriptPanel panel, boolean e) {
        JPanel pp = new JPanel(new BorderLayout());
        pp.setBackground(p.getBackground());
        pp.setPreferredSize(new Dimension(300, 150));
        pp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        pp.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(TEXT_COLOR_LABEL);
        l.setBorder(JBUI.Borders.emptyBottom(5));
        p.add(l, BorderLayout.NORTH);

        panel.putClientProperty("canBeEdited", e);
        mainDynamicFields.add(panel);
        pp.add(panel, BorderLayout.CENTER);

        p.add(pp);
        p.add(Box.createVerticalStrut(10));
    }


    /**
     * Construye un bloque con alineacion horizontal que incrusta un control conmutador (switch)
     * e inyecta su respectivo nombre visible en el contenedor asignado.
     *
     * @param p Contenedor sobre el que se anclaran los items creados.
     * @param text Explicacion descriptiva que acompana visualmente al boton.
     * @param s Instancia funcional de tipo interruptor.
     * @param e Estado de escritura configurable por el invocador.
     */
    private void addSwitchField(JPanel p, String text, ToggleSwitch s, boolean e) {
        JPanel pp = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pp.setBackground(p.getBackground());
        pp.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        pp.setAlignmentX(Component.LEFT_ALIGNMENT);
        s.putClientProperty("canBeEdited", e);
        mainDynamicFields.add(s);
        pp.add(s);
        pp.add(new JLabel("  " + text));
        p.add(pp);
        p.add(Box.createVerticalStrut(10));
    }

    /**
     * Elabora una seccion de interfaz modular diseñada para mostrar u ocultar agililmente su contenido,
     * apoyada por opciones como un boton lateral con callback y un contador de items.
     *
     * @param p Contenedor general en el que se incluira este bloque.
     * @param t Titulo visible mostrado en el margen superior de la subdivision.
     * @param c Numero representativo de la cantidad de elementos listados en esta categoria.
     * @param open Expresion afirmativa para renderizar este bloque abierto desde el principio.
     * @param onAdd Callback logico invocado tras interactuar sobre la accion de insercion.
     * @param labelSaver Bloque de inyeccion que captura la referencia del componente encargado de mostrar el titulo.
     * @return El subpanel grafico dispuesto para albergar los futuros hijos insertados de manera reactiva.
     */
    private JPanel createCollapsibleSection(JPanel p, String t, int c, boolean open, Runnable onAdd, Consumer<JLabel> labelSaver
    ) {
        JPanel s = new JPanel();
        s.setLayout(new BoxLayout(s, BoxLayout.Y_AXIS));
        s.setBackground(p.getBackground());
        s.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel cont = new JPanel();
        cont.setLayout(new BoxLayout(cont, BoxLayout.Y_AXIS));
        cont.setBackground(p.getBackground());
        cont.setVisible(open);
        cont.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel h = new JPanel(new BorderLayout());
        h.setBackground(p.getBackground());
        h.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        h.setAlignmentX(Component.LEFT_ALIGNMENT);

        String title = (c > 0) ? t + " (" + c + ")" : t;
        JLabel titleLbl = new JLabel(title);
        if (labelSaver != null) {
            labelSaver.accept(titleLbl);
        }
        h.add(titleLbl, BorderLayout.WEST);
        if (onAdd != null) {
            AtiCircularIconButton btn = new AtiCircularIconButton(
                    IconUtil.colorize(AtiIcons.ADD_ICON, JBColor.WHITE),
                    e -> onAdd.run()
            );
            JPanel btnCont = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            btnCont.setOpaque(false);
            btnCont.add(btn);
            h.add(btnCont, BorderLayout.EAST);
        }
        s.add(h);
        s.add(cont);
        p.add(s);
        return cont;
    }


    /**
     * Vincula una serie de monitores y espias delegados a registrar cualquier interaccion del usuario
     * con los campos mostrados, el grafo visual u otros botones, orquestando el guardado centralizado a continuacion.
     *
     * @param textListener Oyente pasivo ante cualquier mutacion textual originada en teclados.
     * @param actionListener Oidor atento a acciones concretas impulsadas desde elementos pulsables o graficos.
     * @param changeListener Intermediario diseñado para observar conmutadores de un solo bit en la vista general.
     */
    public void addFieldListeners(DocumentListener textListener,
                                  ActionListener actionListener,
                                  ChangeListener changeListener) {

        this.cachedTextListener = new DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                if (!isUpdatingFromJson) textListener.insertUpdate(e);
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                if (!isUpdatingFromJson) textListener.removeUpdate(e);
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                if (!isUpdatingFromJson) textListener.changedUpdate(e);
            }
        };

        this.cachedActionListener = actionListener;

        descriptionArea.getDocument().removeDocumentListener(this.cachedTextListener);
        descriptionArea.getDocument().addDocumentListener(this.cachedTextListener);

        if (bpmnEditorPanel != null) {
            bpmnEditorPanel.addSelectionListener(cell -> {
                if (isUpdatingFromJson) return;

                if (currentJson != null) updateDocument(currentJson);

                String newId = (cell != null) ? cell.getId() : null;
                boolean newIsEdge = (cell != null) && cell.isEdge();

                SwingUtilities.invokeLater(() -> {
                    currentSelectionId = newId;
                    isSelectionEdge = newIsEdge;

                    if (currentSelectionId != null && !propertiesContainer.isVisible()) {
                        propertiesContainer.setVisible(true);
                        togglePropertiesBtn.setIcon(AllIcons.Actions.Close);
                    }

                    refreshPropertiesPanel();
                    updateForm(currentJson, false);
                });
            });

            bpmnEditorPanel.addGraphChangeListener(() -> {
                if (actionListener != null) {
                    actionListener.actionPerformed(
                            new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "GRAPH_CHANGED")
                    );
                }
            });
        }
    }

    /**
     * Ejecuta una instruccion forzada apoyada en los escuchadores vinculados de manera previa para
     * empujar el volcado completo de la configuracion hacia su fichero.
     */
    private void forceSave() {
        if (cachedActionListener != null) {
            cachedActionListener.actionPerformed(new ActionEvent(this, 0, "SAVE"));
        }
    }

    /**
     * Evalua cual de los componentes mostrados y listados dentro del marco grafico actual posee
     * mayores aptitudes para reclamar la atencion predominante de teclado por defecto tras renderizarse.
     *
     * @return El componente que debe poseer el foco tactil inmediato una vez que este panel sea mostrado.
     */
    public @Nullable JComponent getPreferredFocusedComponent() {
        return batchCodeField;
    }

    /**
     * Sobreescribe e inyecta informacion en todos los componentes del sistema basandose en el json pasado,
     * optando de antemano por redibujar asimismo la representacion del lienzo grafico adosada al formulario.
     *
     * @param json Recipiente original estructurado conteniendo las definiciones y propiedades requeridas en escena.
     */
    public void updateForm(JsonObject json) {
        updateForm(json, true);
    }

    /**
     * Aplica la carga intensiva logica desde un contenedor json especifico hacia la red completa de entidades del panel,
     * evaluando selectivamente si propagar esta informacion hasta el generador del grafo.
     *
     * @param json Diccionario raiz importado para actualizar dinamicamente los detalles visualizados.
     * @param redrawGraph Verificador que instruye repintar la version visual e interactiva del propio grafo segun estos datos.
     */
    public void updateForm(JsonObject json, boolean redrawGraph) {
        if (json == null) return;
        this.currentJson = json;
        isUpdatingFromJson = true;

        try {
            if (redrawGraph && bpmnEditorPanel != null) {
                String previousSelection = currentSelectionId;
                bpmnEditorPanel.renderGraphFromJSON(json);
                if (previousSelection != null) {
                    bpmnEditorPanel.selectNodeById(previousSelection);
                    currentSelectionId = previousSelection;
                }
            }

            Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
            boolean isTypingInProperties = focusOwner instanceof JTextComponent && SwingUtilities.isDescendingFrom(focusOwner, propertiesContainer);

            if (isTypingInProperties) return;

            if (currentSelectionId == null) {
                refreshPropertiesPanel();
                fillBatchProperties(json);
            } else if (isSelectionEdge) {
                if (isConditionalEdge(currentSelectionId, json)) {
                    fillFlowProperties(json, currentSelectionId);
                }
            } else {
                if (isValidNode(currentSelectionId, json)) {
                    refreshPropertiesPanel();
                    BpmnConstants.BpmnPaletteItem item = resolveNodeItem(currentSelectionId, json);

                    if (item == BpmnConstants.BpmnPaletteItem.LOOP) {
                        fillLoopProperties(json, currentSelectionId);
                    } else if (item != null && item.getNodeType() == BpmnConstants.NODETYPE.STEP) {
                        fillStepProperties(json, currentSelectionId);
                    } else {
                        simpleNodeIdField.setText(currentSelectionId);
                    }
                }
            }
        } finally {
            isUpdatingFromJson = false;
        }
        updateEditableState(isEditMode);
    }

    /**
     * Analiza el json matriz en busca de una lista valida de entidades, comparando individualmente el codigo
     * pasado por parametro para atestiguar que el nodo todavia prevalezca entre los disponibles.
     *
     * @param id Llave maestra asignada inicialmente de forma unitaria al nodo que pretende validarse.
     * @param json Coleccion de objetos serializados conformando el mapa principal del fichero.
     * @return Confirmacion booleana positiva solo si se evidencia la existencia coincidente de dicha llave alocada.
     */
    private boolean isValidNode(String id, JsonObject json) {
        if (!json.has("workflowNodeList")) return false;
        for (JsonElement el : json.getAsJsonArray("workflowNodeList")) {
            if (el.isJsonObject() && el.getAsJsonObject().has("workflowNodeCode") && el.getAsJsonObject().get("workflowNodeCode").getAsString().equals(id)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Navega hasta los descriptores primitivos expuestos dentro del arreglo raiz para mapear de manera directa
     * el estado inicial sobre la bateria global de cajas de texto que representan las peculiaridades generales.
     *
     * @param json Diccionario base sobre el que operar la diseccion.
     */
    private void fillBatchProperties(JsonObject json) {
        if (json.has("batchCode")) batchCodeField.setText(json.get("batchCode").getAsString());
        if (json.has("_id")) idField.setText(getCleanId(json.get("_id")));
        if (json.has("status")) statusField.setText(json.get("status").getAsString());
        if (json.has("description")) descriptionArea.setText(json.get("description").getAsString());
    }

    /**
     * Rastrea, aisla e importa la red de particularidades anexadas unicamente a un tramo procedimental determinado,
     * infundiendo los atributos encontrados dentro de la cuadricula frontal designada.
     *
     * @param json Macroconjunto descriptivo sobre el que delegar la extraccion.
     * @param nodeId Llave local de pertenencia sobre la que se rige el objetivo escrutado en este instante.
     */
    private void fillStepProperties(JsonObject json, String nodeId) {
        JsonObject node = findNode(json, nodeId);
        if (node != null) {
            stepIdField.setText(nodeId);

            // 1. Empezamos asumiendo que está vacío (para limpiar "PRUEBA" si es nuevo)
            String newVal = "";

            String type = node.has("type") ? node.get("type").getAsString() : "ETL";

            if ("BATCH".equals(type)) {
                if (node.has("batchCode") && !node.get("batchCode").isJsonNull()) {
                    newVal = node.get("batchCode").getAsString();
                }
            } else {
                if (node.has("stepCode") && !node.get("stepCode").isJsonNull()) {
                    newVal = node.get("stepCode").getAsString();
                }
            }

            // 2. FORZAMOS la actualización del campo.
            // Si newVal es "" (nodo nuevo), esto borrará el texto antiguo.
            if (!stepCodeField.getText().equals(newVal)) {
                stepCodeField.setText(newVal);
            }

            if (node.has("isCritical")) {
                try { criticalStepSwitch.selected = node.get("isCritical").getAsBoolean(); } catch (Exception e) { criticalStepSwitch.selected = false; }
            } else {
                criticalStepSwitch.selected = false;
            }
            criticalStepSwitch.repaint();
        }
    }

    /**
     * Localiza los bloques condicionales estandarizados destinados a delimitar el ambito repetitivo iterativo
     * extrayendo su contenido y alojandolo en los sectores predispuestos del panel interactivo lateral.
     *
     * @param json Fuente central de almacenamiento consultada de inmediato.
     * @param nodeId Llave o coordenada del bloque anidado donde reside esta entidad de iteracion continua.
     */
    private void fillLoopProperties(JsonObject json, String nodeId) {
        JsonObject node = findNode(json, nodeId);
        if (node != null) {
            loopIdField.setText(nodeId);
            if (node.has("isEndLoop")) endLoopSwitch.selected = node.get("isEndLoop").getAsBoolean(); else endLoopSwitch.selected = false;
            endLoopSwitch.repaint();
            loopScriptsContainer.setVisible(!endLoopSwitch.selected);

            if (node.has("initializeScript")) initScriptArea.setText(node.get("initializeScript").getAsString()); else initScriptArea.setText("");
            if (node.has("exitScript")) exitScriptArea.setText(node.get("exitScript").getAsString()); else exitScriptArea.setText("");
            if (node.has("incrementScript")) incrementScriptArea.setText(node.get("incrementScript").getAsString()); else incrementScriptArea.setText("");
        }
    }

    /**
     * Navega hasta la ubicacion remota trazada en el json para identificar el punto central de emision,
     * comprobando asi la sintaxis de las directrices inyectadas en la union a traves del tunel vinculante.
     *
     * @param json Origen documental de configuraciones expuestas en disco.
     * @param edgeId Denominador unico perteneciente a la rampa de transicion condicional a estudiar.
     */
    private void fillFlowProperties(JsonObject json, String edgeId) {
        String sourceId = bpmnEditorPanel.getSourceNodeId(edgeId);
        String targetId = bpmnEditorPanel.getTargetNodeId(edgeId);

        if (sourceId != null && targetId != null) {
            JsonObject sourceNode = findNode(json, sourceId);
            if (sourceNode != null && sourceNode.has("nextWorkflowNodeList")) {
                for(JsonElement el : sourceNode.getAsJsonArray("nextWorkflowNodeList")) {
                    JsonObject nextObj = el.getAsJsonObject();
                    if(nextObj.has("workflowNodeCode") && nextObj.get("workflowNodeCode").getAsString().equals(targetId)) {
                        if(nextObj.has("scriptCondition")) {
                            conditionScriptArea.setText(nextObj.get("scriptCondition").getAsString());
                        }
                        break;
                    }
                }
            }
        }
    }

    /**
     * Interroga al diccionario global examinando las celdas expuestas buscando coincidir de manera identitaria
     * con la etiqueta parametrizada, retornando el cajon de propiedades adosado de forma indivisible.
     *
     * @param json Recipiente inicial donde discurre el algoritmo de escaneo.
     * @param id Marcador absoluto perteneciente a la unidad meta que ansia capturarse.
     * @return El grupo json interno vinculado a dicha etiqueta o un estado puramente nulo si no es hallado.
     */
    private JsonObject findNode(JsonObject json, String id) {
        if (!json.has("workflowNodeList")) return null;
        for (JsonElement el : json.getAsJsonArray("workflowNodeList")) {
            if (el.isJsonObject() && el.getAsJsonObject().get("workflowNodeCode").getAsString().equals(id)) {
                return el.getAsJsonObject();
            }
        }
        return null;
    }

    /**
     * Traduce una raiz json purista hacia una representacion homologable string extirpando metadatos colaterales
     * asegurando un marco comparativo inquebrantable tras la conversion abstracta.
     *
     * @param idEl Elemento singular en representacion abstracta expuesto desde la libreria Gson.
     * @return Transcripcion directa garantizada hacia marco leible por la cadena de ejecucion interna.
     */
    private String getCleanId(JsonElement idEl) { return idEl.isJsonObject() ? idEl.toString() : idEl.getAsString(); }

    /**
     * Acapara todos los registros temporales diseminados mediante interaccion para fundirlos, sanearlos
     * y volcarlos solidamente actualizados en el marco json subyacente acoplado al fichero en curso.
     *
     * @param json Raiz fundamental que sera reconstruida integralmente apoyandose en los metadatos dinamicos del lienzo y el editor lateral.
     */
    public void updateDocument(JsonObject json) {
        if (isUpdatingFromJson) return;

        Map<String, String> nodeTypeBackup = new HashMap<>();
        if (json.has("workflowNodeList")) {
            for (JsonElement el : json.getAsJsonArray("workflowNodeList")) {
                JsonObject n = el.getAsJsonObject();
                if (n.has("workflowNodeCode")) {
                    String id = n.get("workflowNodeCode").getAsString();
                    BpmnConstants.BpmnPaletteItem item = resolveNodeItem(id, json);
                    if (item != null) {
                        nodeTypeBackup.put(id, BpmnConstants.toBackendType(item));
                    }
                }
            }
        }

        if (bpmnEditorPanel != null) {
            bpmnEditorPanel.updateJsonWithGraphData(json);
        }

        if (json.has("workflowNodeList")) {
            for (JsonElement el : json.getAsJsonArray("workflowNodeList")) {
                JsonObject n = el.getAsJsonObject();
                if (n.has("workflowNodeCode")) {
                    String id = n.get("workflowNodeCode").getAsString();

                    String typeStr;
                    if (nodeTypeBackup.containsKey(id)) {
                        typeStr = nodeTypeBackup.get(id);
                        n.addProperty("type", typeStr);
                    } else {
                        typeStr = n.has("type") ? n.get("type").getAsString() : "ETL";
                    }

                    BpmnConstants.BpmnPaletteItem item = BpmnConstants.fromJsonType(typeStr);

                    if (item.getNodeType() == BpmnConstants.NODETYPE.GATEWAY ||
                            item.getNodeType() == BpmnConstants.NODETYPE.INITIAL ||
                            item.getNodeType() == BpmnConstants.NODETYPE.FINAL) {

                        if (item != BpmnConstants.BpmnPaletteItem.LOOP) {
                            n.remove("stepCode");
                            n.remove("wfnParameterList");
                            n.remove("isCritical");
                        }
                    }

                    if (item.getNodeType() == BpmnConstants.NODETYPE.FINAL) {
                        n.remove("nextWorkflowNodeList");
                    } else if (!n.has("nextWorkflowNodeList")) {
                        n.add("nextWorkflowNodeList", new JsonArray());
                    }
                }
            }
        }

        if (currentSelectionId == null) {
            json.addProperty("batchCode", batchCodeField.getText());
            json.addProperty("description", descriptionArea.getText());
            JsonArray newArr = new JsonArray();
            for (BatchParamRow row : batchParamRows) {
                JsonObject pObj = new JsonObject();
                pObj.addProperty("name", row.paramNameField.getText());
                pObj.addProperty("schedulerParamName", row.schedulerNameField.getText());
                pObj.addProperty("type", (String) row.typeCombo.getSelectedItem());
                pObj.addProperty("ioType", (String) row.ioTypeCombo.getSelectedItem());
                pObj.addProperty("isMandatory", row.mandatorySwitch.selected);
                pObj.addProperty("description", row.descriptionArea.getText());
                pObj.addProperty("scriptValue", row.scriptArea.getText());
                newArr.add(pObj);
            }
            json.add("parameterList", newArr);
        } else if (isSelectionEdge) {
            if (isConditionalEdge(currentSelectionId, json)) {
                String s = bpmnEditorPanel.getSourceNodeId(currentSelectionId);
                String t = bpmnEditorPanel.getTargetNodeId(currentSelectionId);
                if (s != null && t != null && json.has("workflowNodeList")) {
                    for (JsonElement el : json.getAsJsonArray("workflowNodeList")) {
                        if (el.isJsonObject() && el.getAsJsonObject().get("workflowNodeCode").getAsString().equals(s)) {
                            JsonObject node = el.getAsJsonObject();
                            if(node.has("nextWorkflowNodeList")) {
                                for(JsonElement next : node.getAsJsonArray("nextWorkflowNodeList")) {
                                    JsonObject nextObj = next.getAsJsonObject();
                                    if(nextObj.get("workflowNodeCode").getAsString().equals(t)) {
                                        nextObj.addProperty("scriptCondition", conditionScriptArea.getText());
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            }
        } else {
            if (json.has("workflowNodeList")) {
                JsonArray nodes = json.getAsJsonArray("workflowNodeList");
                for (JsonElement el : nodes) {
                    if (el.isJsonObject() && el.getAsJsonObject().get("workflowNodeCode").getAsString().equals(currentSelectionId)) {
                        JsonObject node = el.getAsJsonObject();
                        BpmnConstants.BpmnPaletteItem item = resolveNodeItem(currentSelectionId, json);

                        if (item != null && item.getNodeType() == BpmnConstants.NODETYPE.STEP) {
                            if (stepCodeField.hasFocus() || !stepCodeField.getText().isEmpty()) {
                                String nuevoTexto = stepCodeField.getText();
                                if (item == BpmnConstants.BpmnPaletteItem.BATCH_STEP) {
                                    node.addProperty("batchCode", nuevoTexto);
                                    node.remove("stepCode");
                                } else {
                                    node.addProperty("stepCode", nuevoTexto);
                                    node.remove("batchCode");
                                }

                                if (bpmnEditorPanel != null) {
                                    bpmnEditorPanel.updateNodeLabel(currentSelectionId, nuevoTexto);
                                }
                            }
                            node.addProperty("isCritical", criticalStepSwitch.selected);
                            JsonArray params = new JsonArray();
                            for (StepParamRow row : stepParamRows) {
                                JsonObject pObj = new JsonObject();
                                pObj.addProperty("name", row.paramNameField.getText());
                                pObj.addProperty("batchParamName", row.batchParamNameField.getText());
                                pObj.addProperty("fixedValue", row.fixedValueField.getText());
                                pObj.addProperty("scriptValue", row.scriptArea.getText());
                                params.add(pObj);
                            }
                            node.add("wfnParameterList", params);
                        } else if (item == BpmnConstants.BpmnPaletteItem.LOOP) {
                            node.addProperty("isEndLoop", endLoopSwitch.selected);
                            node.addProperty("initializeScript", initScriptArea.getText());
                            node.addProperty("exitScript", exitScriptArea.getText());
                            node.addProperty("incrementScript", incrementScriptArea.getText());
                        }
                        break;
                    }
                }
            }
        }
    }
}
