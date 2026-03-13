// Copyright 2000-2022 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package com.bbva.gkxj.atiframework.moduletemplate;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;

public class AtiModuleWizardStep extends ModuleWizardStep {
    // Propiedades a pasar al ModuleBuilder
    private AtiSettings atiSettings;

    // NOVA Fields
    private final JTextField uuaaField = new JTextField("TEST",10);
    private final JTextField servicePublicNameField = new JTextField("atiClient",10);
    private final JTextField serviceDescriptionField = new JTextField("atiClient",36);
    private final JTextField novaVersionField = new JTextField("23.08", 10);
    private final JTextField novaCliVersionField = new JTextField("7.5.0", 10);
    // ATI fields
    private final JTextField atiVersionField = new JTextField("1.3.4", 10);
    private final JTextField atiReleaseField = new JTextField("alejandria", 10);
    // ATI Included services Checkboxes could be added here
    private JCheckBox novaTransferCheckBox = new JCheckBox("Nova Transfer");
    private JCheckBox epsilonSupportCheckBox = new JCheckBox("Epsilon Support");
    // ATI Database types CheckBox
    private JCheckBox oracleCheckBox = new JCheckBox("Oracle");
    private JCheckBox mongoCheckBox = new JCheckBox("MongoDB");
    private JCheckBox postgreCheckBox = new JCheckBox("PostgreSQL");

    public AtiModuleWizardStep(AtiSettings atiSettings) {
        this.atiSettings = atiSettings;
        ((AbstractDocument) uuaaField.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if ((fb.getDocument().getLength() - length + text.length()) <= 4) {
                    super.replace(fb, offset, length, text.toUpperCase(), attrs);
                }
            }
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if ((fb.getDocument().getLength() + string.length()) <= 4) {
                    super.insertString(fb, offset, string, attr);
                }
            }
        });
        uuaaField.setPreferredSize(new Dimension(80, 24));
        novaVersionField.setPreferredSize(new Dimension(80, 24));
        novaCliVersionField.setPreferredSize(new Dimension(80, 24));
        atiVersionField.setPreferredSize(new Dimension(80, 24));
        atiReleaseField.setPreferredSize(new Dimension(80, 24));
    }

    @Override
    public JComponent getComponent() {
        JPanel novaPanel = new JPanel(new GridBagLayout());
        novaPanel.setBorder(BorderFactory.createTitledBorder("Propiedades de Nova"));
        GridBagConstraints gbcNova = new GridBagConstraints();
        gbcNova.insets = new Insets(5, 5, 5, 5);
        gbcNova.anchor = GridBagConstraints.NORTHWEST;

        gbcNova.gridx = 0; gbcNova.gridy = 0;
        novaPanel.add(new JLabel("UUAA (máx. 4 caracteres):"), gbcNova);
        gbcNova.gridx = 1;
        novaPanel.add(uuaaField, gbcNova);
        gbcNova.gridx = 2;
        novaPanel.add(new JLabel("Public Name:"), gbcNova);
        gbcNova.gridx = 3;
        novaPanel.add(servicePublicNameField, gbcNova);

        gbcNova.gridx = 0; gbcNova.gridy = 1;
        novaPanel.add(new JLabel("Service Description:"), gbcNova);
        gbcNova.gridx = 1;
        gbcNova.gridwidth = 3;
        novaPanel.add(serviceDescriptionField, gbcNova);
        gbcNova.gridwidth = 1;

        gbcNova.gridx = 0; gbcNova.gridy = 2;
        novaPanel.add(new JLabel("Versión de Nova:"), gbcNova);
        gbcNova.gridx = 1;
        novaPanel.add(novaVersionField, gbcNova);
        gbcNova.gridx = 2;
        novaPanel.add(new JLabel("Versión cliente de Nova:"), gbcNova);
        gbcNova.gridx = 3;
        novaPanel.add(novaCliVersionField, gbcNova);

        // ATI Included Services Panel
        JPanel includedServicePanel = new JPanel(new GridBagLayout());
        includedServicePanel.setBorder(BorderFactory.createTitledBorder("Included Service:"));
        GridBagConstraints gbcIncluded = new GridBagConstraints();
        gbcIncluded.insets = new Insets(5, 5, 5, 5);
        gbcIncluded.anchor = GridBagConstraints.NORTHWEST;

        // Añadir los checkboxes al panel
        gbcIncluded.gridx = 0; gbcIncluded.gridy = 0;
        includedServicePanel.add(novaTransferCheckBox, gbcIncluded);
        gbcIncluded.gridy = 1;
        includedServicePanel.add(epsilonSupportCheckBox, gbcIncluded);

        // ATI Included Services Panel
        JPanel databasePanel = new JPanel(new GridBagLayout());
        databasePanel.setBorder(BorderFactory.createTitledBorder("Database Support:"));
        GridBagConstraints gbcDatabase = new GridBagConstraints();
        gbcDatabase.insets = new Insets(5, 5, 5, 5);
        gbcDatabase.anchor = GridBagConstraints.NORTHWEST;

        // Añadir los checkboxes al panel
        gbcDatabase.gridx = 0; gbcDatabase.gridy = 0;
        databasePanel.add(oracleCheckBox, gbcDatabase);
        gbcDatabase.gridy = 1;
        databasePanel.add(mongoCheckBox, gbcDatabase);
        gbcDatabase.gridx = 1; gbcDatabase.gridy = 0;
        databasePanel.add(postgreCheckBox, gbcDatabase);

        // Panel para propiedades de ATI
        JPanel atiPanel = new JPanel(new GridBagLayout());

        atiPanel.setBorder(BorderFactory.createTitledBorder("Propiedades de ATI"));
        GridBagConstraints gbcAti = new GridBagConstraints();
        gbcAti.insets = new Insets(5, 5, 5, 5);
        gbcAti.anchor = GridBagConstraints.WEST;

        atiVersionField.setPreferredSize(new Dimension(80, 24));
        atiReleaseField.setPreferredSize(new Dimension(80, 24));

        gbcAti.gridx = 0; gbcAti.gridy = 0;
        atiPanel.add(new JLabel("ATI Version:"), gbcAti);
        gbcAti.gridx = 1;
        atiPanel.add(atiVersionField, gbcAti);
        gbcAti.gridx = 2;
        atiPanel.add(new JLabel("ATI Release:"), gbcAti);
        gbcAti.gridx = 3;
        atiPanel.add(atiReleaseField, gbcAti);

        // 4. Añadir el panel al panel de ATI
        gbcAti.gridx = 0; gbcAti.gridy = 1;
        gbcAti.gridwidth = 2;
        atiPanel.add(includedServicePanel, gbcAti);
        gbcAti.gridx = 2;
        atiPanel.add(databasePanel, gbcAti);
        gbcAti.gridwidth = 1;

        // Panel principal que contiene ambos grupos
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.NORTHWEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        gbc.gridx = 0; gbc.gridy = 0;
        mainPanel.add(novaPanel, gbc);

        gbc.gridy = 1;
        mainPanel.add(atiPanel, gbc);

        gbc.gridy = 2;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(Box.createVerticalGlue(), gbc);

        return mainPanel;
    }

  @Override
  public void updateDataModel() {
    this.atiSettings.setUuaa(uuaaField.getText());
    this.atiSettings.setServicePublicName(servicePublicNameField.getText());
    this.atiSettings.setServiceDescription(serviceDescriptionField.getText());
    this.atiSettings.setNovaVersion(novaVersionField.getText());
    this.atiSettings.setNovaCliVersion(novaCliVersionField.getText());
    this.atiSettings.setAtiVersion(atiVersionField.getText());
    this.atiSettings.setAtiRelease(atiReleaseField.getText());
    this.atiSettings.setIsNovaTransferIncluded(novaTransferCheckBox.isSelected());
    this.atiSettings.setIsEpsilonSupportIncluded(epsilonSupportCheckBox.isSelected());
    this.atiSettings.setIsOracleIncluded(oracleCheckBox.isSelected());
    this.atiSettings.setIsMongoIncluded(mongoCheckBox.isSelected());
    this.atiSettings.setIsPostgreIncluded(postgreCheckBox.isSelected());
  }

}
