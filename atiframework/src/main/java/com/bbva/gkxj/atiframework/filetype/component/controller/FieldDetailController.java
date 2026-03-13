package com.bbva.gkxj.atiframework.filetype.component.editor.panels.tabs;

import com.bbva.gkxj.atiframework.filetype.component.model.ComponentJsonData.FieldData;
import com.bbva.gkxj.atiframework.filetype.component.model.ComponentJsonData.FormattingConfig;

public class FieldDetailController {

    private final FieldDetailView view;

    public FieldDetailController(FieldDetailView view) {
        this.view = view;
    }

    public void loadDataToView(FieldData data) {
        if (data == null) return;

        // Bloquear notificaciones de la vista mientras rellenamos
        view.setPopulating(true);
        try {
            view.setFieldNameText(getNonNull(data.fieldName));
            view.setPayloadPathText(getNonNull(data.payloadPath));
            view.setPriorityText(data.priority != null ? String.valueOf(data.priority) : "1");
            view.setFieldTypeSelection(data.type);

            // Cargar extracción
            String extractionVal = "";
            String comboVal = "";
            if (data.xpath != null) { extractionVal = data.xpath; comboVal = "XPath"; }
            else if (data.jsonPath != null) { extractionVal = data.jsonPath; comboVal = "JSON Path"; }
            else if (data.outputMessageFixedValue != null) { extractionVal = data.outputMessageFixedValue; comboVal = "Fixed Value"; }
            else if (data.outputMessagePath != null) { extractionVal = data.outputMessagePath; comboVal = "Payload Path"; }

            view.setExtractionTypeSelection(comboVal);
            view.setExtractionValueText(extractionVal);

            // Cargar FormattingConfig
            FormattingConfig format = data.fieldExtraConfig;
            if (format != null) {
                view.setFieldFormatText(getNonNull(format.fieldFormat));
                view.setDecimalDelimiterText(getNonNull(format.decimalDelimiter));
                view.setGroupingDelimiterText(getNonNull(format.groupingDelimiter));
                view.setLanguageText(getNonNull(format.language));
                view.setCountryText(getNonNull(format.country));
                view.setTimeZoneText(getNonNull(format.timeZone));
                view.setRegexText(getNonNull(format.fieldRegex));
            } else {
                view.clearConfigFields();
                view.setRegexText("");
            }

            view.setFieldLengthText(data.getFieldLength() != null ? String.valueOf(data.getFieldLength()) : "");
            view.setScriptText(getNonNull(data.shouldBeExecuted));

            view.updateConfigVisibility();
        } finally {
            view.setPopulating(false);
        }
    }

    public void saveViewToData(FieldData data) {
        if (data == null) return;

        data.fieldName = getNullIfEmpty(view.getFieldNameText());
        data.payloadPath = getNullIfEmpty(view.getPayloadPathText());
        try { data.priority = Integer.parseInt(view.getPriorityText()); } catch (Exception e) { data.priority = 1; }

        String type = view.getFieldTypeText();
        data.type = type;

        // Limpiamos extracción
        data.xpath = null; data.jsonPath = null; data.outputMessageFixedValue = null; data.outputMessagePath = null;

        String extractionType = view.getExtractionTypeText();
        String extractionValue = getNullIfEmpty(view.getExtractionValueText());

        if ("XPath".equals(extractionType)) data.xpath = extractionValue;
        else if ("JSON Path".equals(extractionType)) data.jsonPath = extractionValue;
        else if ("Fixed Value".equals(extractionType)) data.outputMessageFixedValue = extractionValue;
        else if ("Payload Path".equals(extractionType)) data.outputMessagePath = extractionValue;

        // Gestionamos FormattingConfig
        if (data.fieldExtraConfig == null) data.fieldExtraConfig = new FormattingConfig();
        FormattingConfig format = data.fieldExtraConfig;

        try {
            data.setFieldLength(Integer.parseInt(view.getFieldLengthText()));
        } catch (Exception e) {
            data.setFieldLength(null);
        }

        format.fieldRegex = getNullIfEmpty(view.getRegexText());

        boolean isNumeric = type != null && (type.contains("INTEGER") || type.contains("LONG") || type.contains("DOUBLE"));
        boolean isDate = type != null && type.contains("DATE");

        format.fieldFormat = (isNumeric || isDate) ? getNullIfEmpty(view.getFieldFormatText()) : null;

        if (isNumeric) {
            format.decimalDelimiter = getNullIfEmpty(view.getDecimalDelimiterText());
            format.groupingDelimiter = getNullIfEmpty(view.getGroupingDelimiterText());
        } else {
            format.decimalDelimiter = null; format.groupingDelimiter = null;
        }

        if (isDate) {
            format.language = getNullIfEmpty(view.getLanguageText());
            format.country = getNullIfEmpty(view.getCountryText());
            format.timeZone = getNullIfEmpty(view.getTimeZoneText());
        } else {
            format.language = null; format.country = null; format.timeZone = null;
        }

        data.shouldBeExecuted = getNullIfEmpty(view.getScriptText());
    }

    private String getNullIfEmpty(String text) {
        return (text == null || text.isEmpty()) ? null : text;
    }

    private String getNonNull(String text) {
        return text != null ? text : "";
    }
}