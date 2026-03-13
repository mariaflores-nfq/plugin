package com.bbva.gkxj.atiframework.filetype.step;


import com.intellij.openapi.fileTypes.LanguageFileType;
import icons.AtiIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class StepFileType extends LanguageFileType {

    public static final StepFileType INSTANCE = new StepFileType();

    private StepFileType() {
        super(StepLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "ATI Step File";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "ATI Step language file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "step";
    }

    @Override
    public Icon getIcon() {
        return AtiIcons.ATI_BATCH_FILE_ICON;
    }

}
