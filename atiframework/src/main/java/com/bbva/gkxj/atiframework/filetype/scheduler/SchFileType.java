// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.bbva.gkxj.atiframework.filetype.scheduler;

import com.intellij.openapi.fileTypes.LanguageFileType;
import icons.AtiIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public final class SchFileType extends LanguageFileType {

    public static final SchFileType INSTANCE = new SchFileType();

    private SchFileType() {
        super(SchLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "ATI Scheduler File";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "ATI Scheduler language file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "sch";
    }

    @Override
    public Icon getIcon() {
        return AtiIcons.ATI_SCH_FILE_ICON;
    }

}
