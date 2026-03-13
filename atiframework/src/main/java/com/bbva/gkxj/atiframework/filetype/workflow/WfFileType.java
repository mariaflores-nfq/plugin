// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package com.bbva.gkxj.atiframework.filetype.workflow;
import com.intellij.openapi.fileTypes.LanguageFileType;
import icons.AtiIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public final class WfFileType extends LanguageFileType {

    public static final WfFileType INSTANCE = new WfFileType();

    private WfFileType() {
        super(WfLanguage.INSTANCE);
    }

    @NotNull
    @Override
    public String getName() {
        return "Workflow File";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Workflow language file";
    }

    @NotNull
    @Override
    public String getDefaultExtension() {
        return "wf";
    }

    @Override
    public Icon getIcon() {
        return AtiIcons.ATI_SCH_FILE_ICON;
    }

}
