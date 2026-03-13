package com.bbva.gkxj.atiframework.filetype.workflow.editor.propertieseditor;

import com.bbva.gkxj.atiframework.components.AtiTextField;
import com.bbva.gkxj.atiframework.filetype.workflow.model.WorkflowJsonData;
import com.bbva.gkxj.atiframework.filetype.workflow.utils.TaskExecutorCombo;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Editor especializado para componentes de tipo Output Adapter.
 * <p>
 * Gestiona la configuración de los puntos de salida del flujo de trabajo, permitiendo
 * definir destinos como colas de mensajería (JMS), bases de datos o clases Java personalizadas.
 * Utiliza los componentes visuales personalizados Ati (como {@link AtiTextField}).
 * </p>
 * <p>
 * Esta clase destaca por implementar una <b>visibilidad dinámica condicionada</b>:
 * el formulario se reconfigura en tiempo real mientras el usuario escribe en el campo "Subtype",
 * mostrando u ocultando los parámetros (filas completas con sus etiquetas) según sean
 * relevantes para el protocolo detectado.
 * </p>
 */
public class OutputAdapterEditor extends TaskExecutorCombo {

    /** Campo de texto personalizado para especificar el subtipo del adaptador (ej. JMS, DATABASE_ASYNC, etc.). */
    private AtiTextField subtypeField;

    /** Campo de texto personalizado para el nombre de la cola de destino (relevante para JMS). */
    private AtiTextField queueNameField;

    /** Campo de texto personalizado para especificar el tipo de mensaje saliente. */
    private AtiTextField messageTypeField;

    /** Campo de texto personalizado para indicar la clase Java encargada de la lógica de salida (relevante para ASYNC). */
    private AtiTextField javaClassNameField;

    /** Contenedor de la fila del selector de ejecutor, usado para alternar su visibilidad dinámica. */
    private JPanel taskExecutorRow;

    /** Contenedor de la fila del nombre de la cola, usado para alternar su visibilidad dinámica. */
    private JPanel queueNameRow;

    /** Contenedor de la fila del tipo de mensaje, usado para alternar su visibilidad dinámica. */
    private JPanel messageTypeRow;

    /** Contenedor de la fila de la clase Java, usado para alternar su visibilidad dinámica. */
    private JPanel javaClassRow;

    /** * Escudo local para bloquear eventos residuales de la clase padre
     * o de los listeners durante la fase de población programática de datos.
     */
    private boolean isLoading = false;

    /**
     * Construye la interfaz de usuario específica para el adaptador de salida.
     * <p>
     * Además de inicializar los campos mediante componentes {@link AtiTextField}, registra un
     * {@link DocumentListener} reactivo en el campo {@code subtypeField}. Esto garantiza que cualquier
     * pulsación de tecla evalúe inmediatamente las reglas de visibilidad ({@link #updateVisibility()}) y
     * notifique al editor principal que hay cambios sin guardar.
     * </p>
     */
    @Override
    protected void buildSpecificUI() {
        // Inicializa la base común (ID, Component Code, Description) heredada
        buildCommonUI("OutputAdapter Details", "Component Code");

        subtypeField = new AtiTextField();

        // Listener reactivo: evalúa la visibilidad al escribir y bloquea notificaciones erróneas
        subtypeField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { onSubtypeChanged(); }
            public void removeUpdate(DocumentEvent e) { onSubtypeChanged(); }
            public void changedUpdate(DocumentEvent e) { onSubtypeChanged(); }

            private void onSubtypeChanged() {
                // Siempre actualizamos la visibilidad para que la UI reaccione
                updateVisibility();

                // Solo notificamos al framework para guardar si NO estamos en fase de carga inicial
                if (!isUpdating && !isLoading) {
                    notifyChange();
                }
            }
        });
        addFormField("Subtype", subtypeField);

        // Configuración de los campos condicionales y almacenamiento de sus wrappers
        addComboListener(taskExecutorCombo);
        taskExecutorRow = addFormField("Task Executor", taskExecutorCombo);

        queueNameField = new AtiTextField();
        addChangeListener(queueNameField);
        queueNameRow = addFormField("Queue Name", queueNameField);

        messageTypeField = new AtiTextField();
        addChangeListener(messageTypeField);
        messageTypeRow = addFormField("Message Type", messageTypeField);

        javaClassNameField = new AtiTextField();
        addChangeListener(javaClassNameField);
        javaClassRow = addFormField("Java Class Name", javaClassNameField);
    }

    /**
     * Evalúa las palabras clave contenidas en el campo "Subtype" para determinar
     * qué filas de configuración (etiqueta + campo) deben ser visibles para el usuario.
     * <p>
     * Lógica aplicada:
     * <ul>
     * <li><b>JMS o DATABASE:</b> Requieren seleccionar un Task Executor.</li>
     * <li><b>JMS:</b> Requiere configurar el nombre de la cola (Queue Name) y el tipo de mensaje (Message Type).</li>
     * <li><b>ASYNC:</b> Requiere configurar la clase Java encargada (Java Class Name).</li>
     * </ul>
     * </p>
     */
    private void updateVisibility() {
        String subtype = subtypeField.getText().toUpperCase();

        boolean isJms = subtype.contains("JMS");
        boolean isDb = subtype.contains("DATABASE");
        boolean isAsync = subtype.contains("ASYNC");

        taskExecutorRow.setVisible(isJms || isDb);
        queueNameRow.setVisible(isJms);
        messageTypeRow.setVisible(isJms);
        javaClassRow.setVisible(isAsync);

        // Refrescar el panel principal para aplicar los cambios de layout inmediatamente
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    /**
     * Carga los datos del modelo en la interfaz gráfica y refresca la visibilidad de los paneles.
     * <p>
     * Activa el escudo {@code isLoading} durante el volcado de datos. Una vez insertados
     * los textos, invoca a {@link #updateVisibility()} para que el formulario adopte
     * la forma correcta según el subtipo recién cargado.
     * </p>
     * * @param data Objeto {@link WorkflowJsonData} con la configuración del nodo a cargar.
     */
    @Override
    public void loadData(WorkflowJsonData data) {
        isLoading = true; // ACTIVAMOS EL ESCUDO

        try {
            // Si el padre dispara eventos al cargar, nuestro escudo en saveData los detendrá
            loadExecutableData(data);

            // Reforzamos isUpdating por si el padre lo apagó en su propio bloque finally
            isUpdating = true;

            subtypeField.setText(data.getSubtype() != null ? data.getSubtype() : "");
            queueNameField.setText(data.getQueueName() != null ? data.getQueueName() : "");
            messageTypeField.setText(data.getMessageType() != null ? data.getMessageType() : "");
            javaClassNameField.setText(data.getJavaClassName() != null ? data.getJavaClassName() : "");

            // Aplicar las reglas visuales tras cargar los textos para montar la UI correcta
            updateVisibility();

        } finally {
            isUpdating = false;
            isLoading = false; // DESACTIVAMOS EL ESCUDO
        }
    }

    /**
     * Persiste los datos introducidos en la interfaz de vuelta al modelo subyacente.
     * <p>
     * <b>Nota importante:</b> Se utiliza lógica de negocio (evaluando el subtipo)
     * en lugar de 'isVisible()' de los paneles para decidir qué campos guardar, evitando la pérdida
     * de datos cuando el panel se desmonta o se recarga en la vista.
     * </p>
     * @param data Objeto {@link WorkflowJsonData} de destino donde se volcará la información.
     */
    @Override
    public void saveData(WorkflowJsonData data) {
        // ¡LA CLAVE DE LA SOLUCIÓN!
        // Si el panel se está construyendo/cargando, ignoramos cualquier petición de guardado.
        if (isLoading) {
            return;
        }

        // Guarda los datos comunes (ID, Component Code, Description)
        saveCommonData(data);

        String currentSubtype = subtypeField.getText() != null ? subtypeField.getText() : "";
        data.setSubtype(currentSubtype);

        // Evaluamos la lógica de negocio directamente
        String subtypeUpper = currentSubtype.toUpperCase();
        boolean isJms = subtypeUpper.contains("JMS");
        boolean isDb = subtypeUpper.contains("DATABASE");
        boolean isAsync = subtypeUpper.contains("ASYNC");

        // Guardado condicional de Task Executor
        if ((isJms || isDb) && taskExecutorCombo.getSelectedItem() != null) {
            String selected = taskExecutorCombo.getSelectedItem().toString();
            data.setTaskExecutor(selected.isEmpty() ? null : selected);
        } else {
            data.setTaskExecutor(null);
        }

        // Guardado condicional y limpieza del resto de propiedades según el subtipo actual
        data.setQueueName(isJms ? queueNameField.getText() : null);
        data.setMessageType(isJms ? messageTypeField.getText() : null);
        data.setJavaClassName(isAsync ? javaClassNameField.getText() : null);
    }
}