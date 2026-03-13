package com.bbva.gkxj.atiframework.components;

import com.intellij.openapi.ui.ComboBox;
import org.jetbrains.annotations.NotNull;

import static com.bbva.gkxj.atiframework.filetype.scheduler.utils.UiUtils.applyBlueFocusBorder;

public class AtiComboBox extends ComboBox {

    public AtiComboBox(@NotNull Object[] items) {
        super(items);
        applyBlueFocusBorder(this);
    }
}
