package com.bbva.gkxj.atiframework.filetype.scheduler.utils;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

public class NumericDocumentFilter extends DocumentFilter {
    @Override
    public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr)
            throws BadLocationException {
        if (text == null) return;
        replace(fb, offset, 0, text, attr);
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
            throws BadLocationException {

        // Obtener el texto resultante tras la modificación
        Document doc = fb.getDocument();
        String current = doc.getText(0, doc.getLength());
        String next = current.substring(0, offset)
                + text
                + current.substring(offset + length);

        if (isValid(next)) {
            super.replace(fb, offset, length, text, attrs);
        }
    }

    private boolean isValid(String value) {
        // Permitir campo vacío (por si borran todo para escribir de nuevo)
        if (value.isEmpty()) return true;

        // Limitar longitud para evitar desbordamientos visuales (ej. máx 4 dígitos)
        if (value.length() > 4) return false;

        // Validar que solo contenga dígitos
        return value.matches("\\d+");
    }
}
