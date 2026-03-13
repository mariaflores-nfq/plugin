package com.bbva.gkxj.atiframework.copilot;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Grupo contenedor para Settings → Tools → ATI Framework.
 * No tiene panel propio, solo agrupa sub-configurables.
 */
public class AtiFrameworkSettingsGroup implements Configurable {

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "ATI Framework";
    }

    @Override
    public @Nullable JComponent createComponent() {
        // Grupo contenedor sin panel propio
        return null;
    }

    @Override
    public boolean isModified() {
        return false;
    }

    @Override
    public void apply() throws ConfigurationException {
        // No hay nada que aplicar en el grupo contenedor
    }
}

