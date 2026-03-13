package com.bbva.gkxj.atiframework.components;

import com.bbva.gkxj.atiframework.filetype.scheduler.utils.SchedulerTheme;

import javax.swing.*;

import static com.bbva.gkxj.atiframework.filetype.scheduler.utils.UiUtils.applyBlueFocusBorder;

/**
 * Campo de texto personalizado
 */
public class AtiTextField extends JTextField {

    /**
     * Constructor por defecto
     */
    public AtiTextField() {
        super();
        applyBlueFocusBorder(this);
        this.setBackground(SchedulerTheme.BG_CARD);
    }

}
