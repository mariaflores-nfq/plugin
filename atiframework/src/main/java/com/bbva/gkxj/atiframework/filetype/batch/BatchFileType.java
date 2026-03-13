// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.bbva.gkxj.atiframework.filetype.batch;

import com.intellij.openapi.fileTypes.LanguageFileType;
import icons.AtiIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public final class BatchFileType extends LanguageFileType {

    public static final BatchFileType INSTANCE = new BatchFileType();

    private BatchFileType() {
        super(BatchLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "ATI Batch File";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "ATI Batch language file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "batch";
    }

    @Override
    public Icon getIcon() {
        return AtiIcons.ATI_BATCH_FILE_ICON;
    }

}
