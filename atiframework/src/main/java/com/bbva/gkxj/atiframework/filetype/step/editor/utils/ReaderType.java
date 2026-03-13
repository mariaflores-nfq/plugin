package com.bbva.gkxj.atiframework.filetype.step.editor.utils;

public enum ReaderType {
    API_REQUEST("Api Request"),
    CSV_FILE("CSV File"),
    FIXED_FILE("Fixed File"),
    XML_FILE("XML File"),
    QUERIES("Queries");

    private final String displayName;

    ReaderType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() { return displayName; }

    public static ReaderType fromDisplayName(String name) {
        for (ReaderType type : values()) {
            if (type.displayName.equals(name)) return type;
        }
        return null;
    }
}
