package com.bbva.gkxj.atiframework.filetype.scheduler.utils;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

public class HourDocumentFilter extends DocumentFilter {

    @Override
    public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr)
            throws BadLocationException {
        replace(fb, offset, 0, text, attr);
    }

    @Override
    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
            throws BadLocationException {

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

        if (value.isEmpty()) return true;
        if (value.length() > 5) return false;


        if (!value.matches("^\\d{0,2}:?\\d{0,2}$")) {
            return false;
        }


        if (value.matches("^\\d{2}:\\d{2}$")) {
            String[] p = value.split(":");
            int h = Integer.parseInt(p[0]);
            int m = Integer.parseInt(p[1]);

            return h >= 0 && h <= 23 && m >= 0 && m <= 59;
        }

        return true;
    }
}
