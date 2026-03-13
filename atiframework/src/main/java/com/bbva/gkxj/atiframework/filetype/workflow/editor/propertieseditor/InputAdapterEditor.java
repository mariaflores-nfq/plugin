package com.bbva.gkxj.atiframework.filetype.workflow.editor.propertieseditor;

import com.bbva.gkxj.atiframework.components.AtiTextField;
import com.bbva.gkxj.atiframework.filetype.workflow.model.WorkflowJsonData;
import com.bbva.gkxj.atiframework.filetype.workflow.utils.TaskExecutorCombo;

import javax.swing.*;

/**
 * Editor especializado para componentes de tipo Input Adapter.
 * <p>
 * Este editor gestiona la configuración de los puntos de entrada del workflow.
 * Implementa una lógica de interfaz de usuario dinámica que muestra u oculta
 * campos técnicos basándose en el subtipo del adaptador (por ejemplo, si es ASYNC o JMS).
 * Utiliza los componentes visuales personalizados Ati (como {@link AtiTextField}).
 * </p>
 * <p>
 * Al heredar de {@link TaskExecutorCombo}, permite asignar el pool de hilos
 * encargado de procesar los mensajes entrantes, utilizando la lista global de ejecutores.
 * </p>
 */
public class InputAdapterEditor extends TaskExecutorCombo {

    /** Campo de texto personalizado de solo lectura para el subtipo del adaptador (ej. JMS_ASYNC, HTTP, etc.). */
    private AtiTextField subtypeField;

    /** Campo de texto personalizado de solo lectura para el nombre de la cola, específico para adaptadores de mensajería (JMS). */
    private AtiTextField queueNameField;

    /** Campo de texto personalizado de solo lectura para la clase Java encargada de procesar la entrada en modo asíncrono (ASYNC). */
    private AtiTextField javaClassNameField;

    /** Campo de texto personalizado de solo lectura que indica el tipo de mensaje que maneja el adaptador. */
    private AtiTextField messageTypeField;

    /** Contenedor de la fila de la Clase Java, utilizado para controlar su visibilidad dinámicamente. */
    private JPanel javaClassRow;

    /** Contenedor de la fila del nombre de la cola, utilizado para controlar su visibilidad dinámicamente. */
    private JPanel queueNameRow;

    /**
     * Construye la interfaz visual específica del adaptador de entrada.
     * <p>
     * Inicializa los campos técnicos como elementos de solo lectura (dado que un input
     * adapter suele venir preconfigurado desde la paleta) y guarda las referencias
     * a las filas (rows) generadas por la clase padre para permitir el cambio de
     * visibilidad (ocultar etiqueta y campo) durante la carga de datos.
     * </p>
     */
    @Override
    protected void buildSpecificUI() {
        buildExecutableUI("InputAdapter Details", "Component Code");

        // Instanciamos usando AtiTextField para mantener el diseño
        subtypeField = new AtiTextField();
        setReadOnlyStyle(subtypeField);
        addFormField("Subtype", subtypeField);

        messageTypeField = new AtiTextField();
        setReadOnlyStyle(messageTypeField);
        addFormField("Message Type", messageTypeField);

        javaClassNameField = new AtiTextField();
        setReadOnlyStyle(javaClassNameField);
        // Guardamos el contenedor (wrapper) completo que nos devuelve addFormField
        javaClassRow = addFormField("Java Class Name", javaClassNameField);

        queueNameField = new AtiTextField();
        setReadOnlyStyle(queueNameField);
        // Guardamos el contenedor (wrapper) completo que nos devuelve addFormField
        queueNameRow = addFormField("Queue Name", queueNameField);
    }

    /**
     * Carga los datos del modelo en la interfaz de usuario y aplica las reglas de visibilidad.
     * <p>
     * Lógica de visibilidad implementada:
     * <ul>
     * <li>Si el subtipo contiene la palabra <b>ASYNC</b>: Se muestra la fila completa de 'Java Class Name'.</li>
     * <li>Si el subtipo contiene la palabra <b>JMS</b>: Se muestra la fila completa de 'Queue Name'.</li>
     * </ul>
     * </p>
     * * @param data Objeto {@link WorkflowJsonData} con la configuración del nodo seleccionado.
     */
    @Override
    public void loadData(WorkflowJsonData data) {
        // Llama a la carga base, que ahora usará el "this.globalExecutors" inyectado previamente
        loadExecutableData(data);

        isUpdating = true;
        try {
            String subtype = data.getSubtype() != null ? data.getSubtype().toUpperCase() : "";
            subtypeField.setText(data.getSubtype() != null ? data.getSubtype() : "");
            messageTypeField.setText(data.getMessageType() != null ? data.getMessageType() : "");

            // Determinar visibilidad basada en palabras clave del subtipo
            boolean isAsync = subtype.contains("ASYNC");
            boolean isJms = subtype.contains("JMS");

            javaClassNameField.setText(data.getJavaClassName() != null ? data.getJavaClassName() : "");
            // Oculta/Muestra el AtiLabeledComponent completo junto con su espaciado
            javaClassRow.setVisible(isAsync);

            queueNameField.setText(data.getQueueName() != null ? data.getQueueName() : "");
            // Oculta/Muestra el AtiLabeledComponent completo junto con su espaciado
            queueNameRow.setVisible(isJms);

        } finally {
            isUpdating = false;
        }
    }

    /**
     * Persiste los datos de la interfaz de vuelta al modelo de datos del nodo.
     * <p>
     * Delega en {@link TaskExecutorCombo#saveExecutableData(WorkflowJsonData)}
     * para guardar la configuración de ejecución (el Task Executor seleccionado).
     * Los demás campos no se guardan explícitamente aquí porque son de solo lectura.
     * </p>
     * * @param data Objeto {@link WorkflowJsonData} donde se guardarán los cambios.
     */
    @Override
    public void saveData(WorkflowJsonData data) {
        saveExecutableData(data);
    }
}