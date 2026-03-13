// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.bbva.gkxj.atiframework.filetype.component;

import com.intellij.openapi.fileTypes.LanguageFileType;
import icons.AtiIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public final class CmpFileType extends LanguageFileType {

    public static final CmpFileType INSTANCE = new CmpFileType();

    private CmpFileType() {
        super(CmpLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "Component File";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Component language file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "comp";
    }

    @Override
    public Icon getIcon() {
        return AtiIcons.ATI_BATCH_FILE_ICON;
    }

}
