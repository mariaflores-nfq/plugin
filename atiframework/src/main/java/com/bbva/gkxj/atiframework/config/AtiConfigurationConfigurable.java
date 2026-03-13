package com.bbva.gkxj.atiframework.config;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Objects;

/**
 * Panel de configuración de ATI accesible desde
 * Settings → Tools → ATI Framework → ATI Configuration.
 * <p>
 * Permite configurar:
 * <ul>
 *   <li><b>ATI BBDD</b>: Entorno de base de datos (Integrado / Preproducción).</li>
 *   <li><b>UUAA</b>: Unidad de negocio, obtenida dinámicamente desde MongoDB.</li>
 *   <li><b>Usuario</b>: Usuario disponible para la UUAA seleccionada.</li>
 *   <li><b>Branch</b>: Branch activa para el usuario en la UUAA seleccionada.</li>
 * </ul>
 */
public class AtiConfigurationConfigurable implements Configurable {

    private JPanel mainPanel;
    private ComboBox<String> bbddCombo;
    private ComboBox<String> uuaaCombo;
    private ComboBox<String> usuarioCombo;
    private ComboBox<String> branchCombo;
    private JButton refreshButton;
    private JButton testConnectionButton;
    private JTextArea statusArea;

    /** Flag para evitar disparar listeners durante la carga programática */
    private boolean loading = false;

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "ATI Configuration";
    }

    @Override
    public @Nullable JComponent createComponent() {
        // ---- ATI BBDD ----
        bbddCombo = new ComboBox<>(new String[]{"Integrado", "Preproducción"});
        bbddCombo.setToolTipText("Seleccione el entorno de base de datos de ATI (MongoDB)");
        bbddCombo.addActionListener(e -> {
            if (!loading) {
                onBbddChanged();
            }
        });

        // ---- UUAA ----
        uuaaCombo = new ComboBox<>();
        uuaaCombo.setEditable(false);
        uuaaCombo.setToolTipText("UUAA disponible en el entorno seleccionado (obtenida desde MongoDB)");
        uuaaCombo.addActionListener(e -> {
            if (!loading) {
                onUuaaChanged();
            }
        });

        // ---- Usuario ----
        usuarioCombo = new ComboBox<>();
        usuarioCombo.setEditable(false);
        usuarioCombo.setToolTipText("Usuario disponible para la UUAA seleccionada");

        // ---- Branch ----
        branchCombo = new ComboBox<>();
        branchCombo.setEditable(false);
        branchCombo.setToolTipText("Branch activa para el usuario en la UUAA seleccionada");

        // ---- Botones ----
        refreshButton = new JButton("Actualizar datos");
        refreshButton.setToolTipText("Refresca las UUAAs, usuarios y branches desde MongoDB");
        refreshButton.addActionListener(e -> refreshAllData());

        testConnectionButton = new JButton("Probar conexión");
        testConnectionButton.setToolTipText("Prueba la conexión al MongoDB del entorno seleccionado");
        testConnectionButton.addActionListener(e -> testConnection());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttonPanel.add(refreshButton);
        buttonPanel.add(testConnectionButton);

        // ---- Status ----
        statusArea = new JTextArea(3, 50);
        statusArea.setEditable(false);
        statusArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        statusArea.setBorder(JBUI.Borders.empty(4));

        // ---- Separador informativo ----
        JTextArea infoLabel = new JTextArea(
                "La configuración de ATI se conecta a la base de datos MongoDB del entorno\n" +
                "seleccionado para obtener las UUAAs, usuarios y branches disponibles.\n" +
                "Al cambiar la UUAA se verificará si el usuario actual existe en ella."
        );
        infoLabel.setEditable(false);
        infoLabel.setBackground(null);
        infoLabel.setFont(infoLabel.getFont().deriveFont(Font.ITALIC, 11f));
        infoLabel.setBorder(JBUI.Borders.empty(0, 4, 8, 0));

        // ---- Montaje del formulario ----
        mainPanel = FormBuilder.createFormBuilder()
                .addComponent(infoLabel)
                .addSeparator()
                .addLabeledComponent(new JBLabel("ATI BBDD:"), bbddCombo, 1, false)
                .addVerticalGap(8)
                .addLabeledComponent(new JBLabel("UUAA:"), uuaaCombo, 1, false)
                .addLabeledComponent(new JBLabel("Usuario:"), usuarioCombo, 1, false)
                .addLabeledComponent(new JBLabel("Branch:"), branchCombo, 1, false)
                .addVerticalGap(8)
                .addComponent(buttonPanel)
                .addSeparator()
                .addLabeledComponent(new JBLabel("Estado:"), new JScrollPane(statusArea), 1, true)
                .addComponentFillVertically(new JPanel(), 0)
                .getPanel();

        // Cargar valores guardados
        reset();

        return mainPanel;
    }

    // =====================================================================
    //  Lógica de cambios en combos
    // =====================================================================

    /**
     * Cuando cambia el entorno BBDD se recargan las UUAAs disponibles.
     */
    private void onBbddChanged() {
        setStatus("Cambiando entorno a: " + bbddCombo.getSelectedItem() + " ...");
        loadUuaas();
    }

    /**
     * Cuando cambia la UUAA:
     * 1. Recarga los usuarios disponibles.
     * 2. Comprueba si el usuario actual existe en la nueva UUAA.
     * 3. Recarga las branches y selecciona la branch activa del usuario.
     */
    private void onUuaaChanged() {
        String selectedUuaa = getSelectedUuaa();
        if (selectedUuaa == null || selectedUuaa.isEmpty()) {
            return;
        }

        setStatus("Cargando datos para UUAA: " + selectedUuaa + " ...");

        String currentUser = getSelectedUsuario();

        // Recargar usuarios
        loadUsersForUuaa(selectedUuaa);

        // Comprobar si el usuario anterior existe en la nueva UUAA
        if (currentUser != null && !currentUser.isEmpty()) {
            boolean exists = containsItem(usuarioCombo, currentUser);
            if (!exists) {
                Messages.showWarningDialog(
                        mainPanel,
                        "El usuario '" + currentUser + "' no existe en la UUAA '" + selectedUuaa + "'.\n" +
                        "Por favor, revise y seleccione un usuario válido.",
                        "Usuario no encontrado"
                );
                setStatus("⚠ El usuario '" + currentUser + "' no existe en la UUAA '" + selectedUuaa + "'.");
            } else {
                usuarioCombo.setSelectedItem(currentUser);
            }
        }

        // Recargar branches
        loadBranchesForUuaa(selectedUuaa);
    }

    // =====================================================================
    //  Carga de datos desde MongoDB
    // =====================================================================

    /**
     * Refresca todos los datos: UUAAs, usuarios y branches.
     */
    private void refreshAllData() {
        setStatus("Actualizando todos los datos desde MongoDB...");
        loadUuaas();
        String uuaa = getSelectedUuaa();
        if (uuaa != null && !uuaa.isEmpty()) {
            loadUsersForUuaa(uuaa);
            loadBranchesForUuaa(uuaa);
        }
        setStatus("✓ Datos actualizados correctamente.");
    }

    /**
     * Prueba la conexión a MongoDB con la configuración actual.
     */
    private void testConnection() {
        setStatus("Probando conexión a MongoDB...");
        AtiConfigurationState configState = AtiConfigurationState.getInstance();
        AtiConfigurationState.State stateData = configState.getState();
        if (stateData == null) {
            setStatus("✗ No se pudo obtener la configuración de ATI.");
            return;
        }

        // Aplicar temporalmente el entorno seleccionado en la combo
        String selectedBbdd = (String) bbddCombo.getSelectedItem();
        String host;
        int port;
        String dbName;
        if ("Preproducción".equals(selectedBbdd)) {
            host = stateData.mongoHostPreproduccion;
            dbName = stateData.mongoDbPreproduccion;
        } else {
            host = stateData.mongoHostIntegrado;
            dbName = stateData.mongoDbIntegrado;
        }

        AtiMongoService mongoService = AtiMongoService.getInstance();
        boolean ok = mongoService.testConnection(host, dbName);

        if (ok) {
            setStatus("✓ Conexión exitosa a MongoDB (" + host + "/" + dbName + ")");
            Messages.showInfoMessage(mainPanel,
                    "Conexión exitosa a MongoDB.\nHost: " + host + "\nBBDD: " + dbName,
                    "Conexión OK");
        } else {
            setStatus("✗ Error al conectar a MongoDB (" + host + "/" + dbName + ")");
            Messages.showErrorDialog(mainPanel,
                    "No se pudo conectar a MongoDB.\nHost: " + host + "\nBBDD: " + dbName +
                    "\n\nRevise que el servidor MongoDB esté accesible y la configuración sea correcta.",
                    "Error de Conexión");
        }
    }

    /**
     * Carga las UUAAs desde MongoDB y rellena el combo.
     */
    private void loadUuaas() {
        SwingWorker<List<String>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<String> doInBackground() {
                // Aplicar temporalmente el entorno para que el servicio use la BBDD correcta
                AtiConfigurationState state = AtiConfigurationState.getInstance();
                String previousBbdd = state.getAtiBbdd();
                state.setAtiBbdd((String) bbddCombo.getSelectedItem());

                List<String> result = AtiMongoService.getInstance().getUuaas();

                // Restaurar si no se ha aplicado aún
                state.setAtiBbdd(previousBbdd);
                return result;
            }

            @Override
            protected void done() {
                try {
                    List<String> uuaas = get();
                    loading = true;
                    String previousSelection = getSelectedUuaa();
                    uuaaCombo.removeAllItems();
                    for (String u : uuaas) {
                        uuaaCombo.addItem(u);
                    }
                    if (previousSelection != null && containsItem(uuaaCombo, previousSelection)) {
                        uuaaCombo.setSelectedItem(previousSelection);
                    }
                    loading = false;

                    if (uuaas.isEmpty()) {
                        setStatus("No se encontraron UUAAs. Verifique la conexión a MongoDB.");
                    } else {
                        setStatus("✓ " + uuaas.size() + " UUAAs cargadas.");
                        // Disparar la carga de usuarios y branches
                        onUuaaChanged();
                    }
                } catch (Exception e) {
                    setStatus("✗ Error al cargar UUAAs: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    /**
     * Carga los usuarios disponibles para una UUAA desde MongoDB.
     */
    private void loadUsersForUuaa(String uuaa) {
        SwingWorker<List<String>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<String> doInBackground() {
                AtiConfigurationState state = AtiConfigurationState.getInstance();
                String previousBbdd = state.getAtiBbdd();
                state.setAtiBbdd((String) bbddCombo.getSelectedItem());

                List<String> result = AtiMongoService.getInstance().getUsersForUuaa(uuaa);

                state.setAtiBbdd(previousBbdd);
                return result;
            }

            @Override
            protected void done() {
                try {
                    List<String> users = get();
                    loading = true;
                    String previousSelection = getSelectedUsuario();
                    usuarioCombo.removeAllItems();
                    for (String u : users) {
                        usuarioCombo.addItem(u);
                    }
                    if (previousSelection != null && containsItem(usuarioCombo, previousSelection)) {
                        usuarioCombo.setSelectedItem(previousSelection);
                    }
                    loading = false;
                } catch (Exception e) {
                    setStatus("✗ Error al cargar usuarios: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    /**
     * Carga las branches disponibles para una UUAA desde MongoDB
     * y selecciona la branch activa del usuario.
     */
    private void loadBranchesForUuaa(String uuaa) {
        String currentUser = getSelectedUsuario();

        SwingWorker<BranchLoadResult, Void> worker = new SwingWorker<>() {
            @Override
            protected BranchLoadResult doInBackground() {
                AtiConfigurationState state = AtiConfigurationState.getInstance();
                String previousBbdd = state.getAtiBbdd();
                state.setAtiBbdd((String) bbddCombo.getSelectedItem());

                List<String> branches = AtiMongoService.getInstance().getBranchesForUuaa(uuaa);
                String activeBranch = "";
                if (currentUser != null && !currentUser.isEmpty()) {
                    activeBranch = AtiMongoService.getInstance().getActiveBranchForUser(uuaa, currentUser);
                }

                state.setAtiBbdd(previousBbdd);
                return new BranchLoadResult(branches, activeBranch);
            }

            @Override
            protected void done() {
                try {
                    BranchLoadResult result = get();
                    loading = true;
                    branchCombo.removeAllItems();
                    for (String b : result.branches) {
                        branchCombo.addItem(b);
                    }
                    // Seleccionar la branch activa del usuario
                    if (!result.activeBranch.isEmpty() && containsItem(branchCombo, result.activeBranch)) {
                        branchCombo.setSelectedItem(result.activeBranch);
                    }
                    loading = false;
                } catch (Exception e) {
                    setStatus("✗ Error al cargar branches: " + e.getMessage());
                }
            }
        };
        worker.execute();
    }

    // =====================================================================
    //  Configurable — isModified / apply / reset
    // =====================================================================

    @Override
    public boolean isModified() {
        AtiConfigurationState state = AtiConfigurationState.getInstance();
        return !Objects.equals(bbddCombo.getSelectedItem(), state.getAtiBbdd())
                || !Objects.equals(getSelectedUuaa(), state.getUuaa())
                || !Objects.equals(getSelectedUsuario(), state.getUsuarioAti())
                || !Objects.equals(getSelectedBranch(), state.getBranch());
    }

    @Override
    public void apply() throws ConfigurationException {
        AtiConfigurationState state = AtiConfigurationState.getInstance();
        state.setAtiBbdd((String) bbddCombo.getSelectedItem());
        state.setUuaa(getSelectedUuaa());
        state.setUsuarioAti(getSelectedUsuario());
        state.setBranch(getSelectedBranch());
        setStatus("✓ Configuración guardada.");
    }

    @Override
    public void reset() {
        loading = true;
        AtiConfigurationState state = AtiConfigurationState.getInstance();

        bbddCombo.setSelectedItem(state.getAtiBbdd());

        // Intentar restaurar los valores guardados
        if (uuaaCombo.getItemCount() == 0 && !state.getUuaa().isEmpty()) {
            uuaaCombo.addItem(state.getUuaa());
        }
        if (containsItem(uuaaCombo, state.getUuaa())) {
            uuaaCombo.setSelectedItem(state.getUuaa());
        }

        if (usuarioCombo.getItemCount() == 0 && !state.getUsuarioAti().isEmpty()) {
            usuarioCombo.addItem(state.getUsuarioAti());
        }
        if (containsItem(usuarioCombo, state.getUsuarioAti())) {
            usuarioCombo.setSelectedItem(state.getUsuarioAti());
        }

        if (branchCombo.getItemCount() == 0 && !state.getBranch().isEmpty()) {
            branchCombo.addItem(state.getBranch());
        }
        if (containsItem(branchCombo, state.getBranch())) {
            branchCombo.setSelectedItem(state.getBranch());
        }

        loading = false;
        setStatus("Pulse 'Actualizar datos' para cargar la información desde MongoDB.");
    }

    // =====================================================================
    //  Utilidades
    // =====================================================================

    private String getSelectedUuaa() {
        Object item = uuaaCombo.getSelectedItem();
        return item != null ? item.toString() : "";
    }

    private String getSelectedUsuario() {
        Object item = usuarioCombo.getSelectedItem();
        return item != null ? item.toString() : "";
    }

    private String getSelectedBranch() {
        Object item = branchCombo.getSelectedItem();
        return item != null ? item.toString() : "";
    }

    private boolean containsItem(ComboBox<String> combo, String value) {
        if (value == null) return false;
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (value.equals(combo.getItemAt(i))) {
                return true;
            }
        }
        return false;
    }

    private void setStatus(String text) {
        if (statusArea != null) {
            statusArea.setText(text);
        }
    }

    /**
     * Resultado auxiliar para la carga de branches.
     */
    private static class BranchLoadResult {
        final List<String> branches;
        final String activeBranch;

        BranchLoadResult(List<String> branches, String activeBranch) {
            this.branches = branches;
            this.activeBranch = activeBranch;
        }
    }
}









