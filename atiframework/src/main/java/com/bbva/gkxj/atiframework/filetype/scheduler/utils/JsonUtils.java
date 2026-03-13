package com.bbva.gkxj.atiframework.filetype.scheduler.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import javax.swing.*;
import java.util.List;
import java.util.Objects;

public class JsonUtils {
    // ------------------------------------------------
    // --- HELPERS PARA EXTRAER DATOS DE CHECKBOXES ---
    // ------------------------------------------------

    /**
     * Extrae una lista de Strings
     */
    public static JsonArray getJsonArrayFromCheckBoxes(List<JCheckBox> checkBoxes) {
        JsonArray array = new JsonArray();
        if (checkBoxes != null) {
            checkBoxes.stream()
                    .filter(JCheckBox::isSelected)
                    .map(cb -> cb.getText().toUpperCase().trim())
                    .forEach(array::add);
        }
        return array;
    }

    /**
     * Extrae una lista de Enteros
     */
    public static JsonArray getJsonIntArrayFromCheckBoxes(List<JCheckBox> checkBoxes) {
        JsonArray array = new JsonArray();
        if (checkBoxes != null) {
            checkBoxes.stream()
                    .filter(JCheckBox::isSelected)
                    .map(cb -> {
                        try {
                            return Integer.parseInt(cb.getText().trim());
                        } catch (NumberFormatException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .forEach(array::add);
        }
        return array;
    }

    // =================================================================================================
    // HELPERS DE CARGA (JSON -> FORM)
    // =================================================================================================

    /**
     * Helper genérico para marcar CheckBoxes desde un JsonArray (Strings o Integers).
     *
     * @param jsonArray Array del JSON
     * @param checkBoxes Lista de JCheckBox a marcar
     * @param displayField El JTextField donde se muestra el resumen
     */
    public static void loadCheckBoxesFromJson(JsonArray jsonArray, List<JCheckBox> checkBoxes, JTextField displayField, String emptyMsg) {
        // Limpiar selección previa
        checkBoxes.forEach(cb -> cb.setSelected(false));

        if (jsonArray == null || jsonArray.size() == 0) {
            updateSelectedOptionsText(displayField, checkBoxes, emptyMsg);
            return;
        }

        // Recorrer JSON y marcar coincidencias
        for (JsonElement element : jsonArray) {
            String val = element.getAsString();

            for (JCheckBox cb : checkBoxes) {
                if (cb.getText().equalsIgnoreCase(val)) {
                    cb.setSelected(true);
                    break;
                }
            }
        }
        // Actualizar texto visual del input
        updateSelectedOptionsText(displayField, checkBoxes, emptyMsg);
    }


    public static void updateSelectedOptionsText(
            JTextField display,
            List<JCheckBox> checkBoxes,
            String emptyMsg
    ) {
        String selected = checkBoxes.stream()
                .filter(JCheckBox::isSelected)
                .map(AbstractButton::getText)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");

        if (selected.isEmpty()) {
            display.setText(emptyMsg);
            display.setToolTipText(emptyMsg);
        } else {
            display.setText(selected);
            display.setToolTipText(selected);
        }
    }
}
