package com.bbva.gkxj.atiframework.filetype.step.editor.utils;

public enum EtlStepType {

    ETL_CHUNK("ETL Chunk", "CHUNK"),
    JASPER_REPORT("Jasper Report", "TASKLET_JASPER"),
    FILE_MAINTENANCE("File Maintenance", "TASKLET_FILE"),
    QUERIES_TASK("Queries Task", "TASKLET_QUERY");

    public final String label;
    public final String value;

    EtlStepType(String label, String value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }

    /**
     * Helper para cargar tu JComboBox fácilmente.
     * En lugar de tener un String[] separado, el enum te lo genera.
     * Uso: new JComboBox<>(ETL_STEP_TYPE.getLabels());
     */
    public static String[] getLabels() {
        EtlStepType[] values = values();
        String[] labels = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            labels[i] = values[i].getLabel();
        }
        return labels;
    }

    /**
     * Helper para convertir el String del JSON de vuelta al Enum (a partir del label).
     */
    public static EtlStepType fromString(String text) {
        if (text == null) return null;
        for (EtlStepType type : EtlStepType.values()) {
            if (type.label.equalsIgnoreCase(text)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Helper para convertir desde el valor interno (TASKLET_*, CHUNK, ...).
     */
    public static EtlStepType fromValue(String value) {
        if (value == null) return null;
        for (EtlStepType type : EtlStepType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return null;
    }
}