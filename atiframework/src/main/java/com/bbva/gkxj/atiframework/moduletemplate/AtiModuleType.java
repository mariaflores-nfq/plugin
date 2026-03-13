// Copyright 2000-2023 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.bbva.gkxj.atiframework.moduletemplate;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleTypeManager;
import com.intellij.openapi.roots.ui.configuration.ModulesProvider;
import icons.AtiIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

final class AtiModuleType extends ModuleType<AtiModuleBuilder> {

    private static final String ID = "ATI_MODULE_TYPE";

    AtiModuleType() {
    super(ID);
    }

    public static AtiModuleType getInstance() {
    return (AtiModuleType) ModuleTypeManager.getInstance().findByID(ID);
    }

    @NotNull
    @Override
    public AtiModuleBuilder createModuleBuilder() {
        AtiSettings atiSettings = new AtiSettings();
        return new AtiModuleBuilder(atiSettings);
    }

    @NotNull
    @Override
    public String getName() {
    return "ATI Client Generator";
    }

    @NotNull
    @Override
    public String getDescription() {
    return "Generator for a ATI client project";
    }

    @NotNull
    @Override
    public Icon getNodeIcon(@Deprecated boolean b) {
    return AtiIcons.ATI_default_icon;
    }

    @Override
    public ModuleWizardStep @NotNull [] createWizardSteps(@NotNull WizardContext wizardContext,
                                                        @NotNull AtiModuleBuilder moduleBuilder,
                                                        @NotNull ModulesProvider modulesProvider) {
        return super.createWizardSteps(wizardContext, moduleBuilder, modulesProvider);
    }

}
