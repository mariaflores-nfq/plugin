package com.bbva.gkxj.atiframework.copilot;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
 * Panel de configuración para las instrucciones remotas de Copilot.
 * Accesible desde Settings → Tools → ATI Framework → Copilot.
 */
public class AtiCopilotSettingsConfigurable implements Configurable {

    private JPanel mainPanel;
    private JBTextField remoteUrlField;
    private JBCheckBox autoUpdateCheckbox;
    private JBPasswordField authTokenField;
    private JButton testConnectionButton;
    private JButton forceUpdateButton;
    private JTextArea statusArea;

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Copilot";
    }

    @Override
    public @Nullable JComponent createComponent() {
        remoteUrlField = new JBTextField();
        remoteUrlField.setToolTipText("URL del fichero scheduler-instructions.md remoto");

        autoUpdateCheckbox = new JBCheckBox("Actualizar automáticamente al iniciar");
        autoUpdateCheckbox.setToolTipText("Si está habilitado, el plugin descargará las instrucciones actualizadas periódicamente");

        authTokenField = new JBPasswordField();
        authTokenField.setToolTipText("Token de autenticación para repositorios privados (opcional)");

        testConnectionButton = new JButton("Probar Conexión");
        testConnectionButton.addActionListener(e -> testConnection());

        forceUpdateButton = new JButton("Actualizar Ahora");
        forceUpdateButton.addActionListener(e -> forceUpdate());

        statusArea = new JTextArea(5, 50);
        statusArea.setEditable(false);
        statusArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(testConnectionButton);
        buttonPanel.add(forceUpdateButton);

        mainPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(new JBLabel("URL Remota:"), remoteUrlField, 1, false)
                .addComponent(createHelpLabel())
                .addComponent(autoUpdateCheckbox)
                .addLabeledComponent(new JBLabel("Token Auth (opcional):"), authTokenField, 1, false)
                .addComponent(buttonPanel)
                .addSeparator()
                .addLabeledComponent(new JBLabel("Estado:"), new JScrollPane(statusArea), 1, true)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();

        // Cargar valores actuales
        reset();
        updateStatus();

        return mainPanel;
    }

    private JComponent createHelpLabel() {
        JTextArea helpText = new JTextArea(
                "Ejemplos de URLs válidas:\n" +
                "• GitHub: https://raw.githubusercontent.com/org/repo/main/.github/scheduler-instructions.md\n" +
                "• GitLab: https://gitlab.com/api/v4/projects/{id}/repository/files/.github%2Fscheduler-instructions.md/raw?ref=main\n" +
                "• Servidor interno: https://artifactory.internal.com/repo/ati/scheduler-instructions.md"
        );
        helpText.setEditable(false);
        helpText.setBackground(null);
        helpText.setFont(helpText.getFont().deriveFont(Font.ITALIC, 11f));
        helpText.setBorder(BorderFactory.createEmptyBorder(5, 20, 10, 0));
        return helpText;
    }

    private void testConnection() {
        String url = remoteUrlField.getText().trim();
        if (url.isEmpty()) {
            Messages.showWarningDialog("Por favor, introduce una URL remota.", "URL Vacía");
            return;
        }

        AtiCopilotRemoteInstructionsService service = AtiCopilotRemoteInstructionsService.getInstance();
        String originalUrl = service.getRemoteUrl();
        String originalToken = service.getState() != null ? service.getState().authToken : null;

        // Aplicar temporalmente los nuevos valores
        service.setRemoteUrl(url);
        if (authTokenField.getPassword().length > 0) {
            service.setAuthToken(new String(authTokenField.getPassword()));
        }

        service.forceUpdate().thenAccept(success -> {
            SwingUtilities.invokeLater(() -> {
                if (success) {
                    Messages.showInfoMessage("Conexión exitosa. Las instrucciones se han descargado correctamente.", "Conexión OK");
                    updateStatus();
                } else {
                    Messages.showErrorDialog("No se pudo descargar el fichero. Verifica la URL y los permisos.", "Error de Conexión");
                    // Restaurar valores originales si falló
                    service.setRemoteUrl(originalUrl);
                    service.setAuthToken(originalToken);
                }
            });
        });
    }

    private void forceUpdate() {
        apply();
        AtiCopilotRemoteInstructionsService service = AtiCopilotRemoteInstructionsService.getInstance();

        service.forceUpdate().thenAccept(success -> {
            SwingUtilities.invokeLater(() -> {
                if (success) {
                    Messages.showInfoMessage("Instrucciones actualizadas correctamente.", "Actualización Exitosa");
                } else {
                    Messages.showWarningDialog("No se pudieron actualizar las instrucciones.", "Actualización Fallida");
                }
                updateStatus();
            });
        });
    }

    private void updateStatus() {
        AtiCopilotRemoteInstructionsService service = AtiCopilotRemoteInstructionsService.getInstance();
        statusArea.setText(service.getStatusInfo());
    }

    @Override
    public boolean isModified() {
        AtiCopilotRemoteInstructionsService service = AtiCopilotRemoteInstructionsService.getInstance();

        boolean urlModified = !remoteUrlField.getText().trim().equals(service.getRemoteUrl());
        boolean autoUpdateModified = autoUpdateCheckbox.isSelected() != service.isAutoUpdateEnabled();
        boolean tokenModified = authTokenField.getPassword().length > 0;

        return urlModified || autoUpdateModified || tokenModified;
    }

    @Override
    public void apply() {
        AtiCopilotRemoteInstructionsService service = AtiCopilotRemoteInstructionsService.getInstance();

        service.setRemoteUrl(remoteUrlField.getText().trim());
        service.setAutoUpdateEnabled(autoUpdateCheckbox.isSelected());

        char[] password = authTokenField.getPassword();
        if (password.length > 0) {
            service.setAuthToken(new String(password));
        }

        updateStatus();
    }

    @Override
    public void reset() {
        AtiCopilotRemoteInstructionsService service = AtiCopilotRemoteInstructionsService.getInstance();

        remoteUrlField.setText(service.getRemoteUrl());
        autoUpdateCheckbox.setSelected(service.isAutoUpdateEnabled());
        authTokenField.setText(""); // No mostramos el token por seguridad

        updateStatus();
    }
}

