package com.bbva.gkxj.atiframework.filetype.step.editor.utils;

public enum StepType {
    READER("reader"),
    WRITER("writer");

    private final String value;

    StepType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
