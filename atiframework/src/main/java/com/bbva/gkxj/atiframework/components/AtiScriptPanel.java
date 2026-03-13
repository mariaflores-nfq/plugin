package com.bbva.gkxj.atiframework.components;

import com.bbva.gkxj.atiframework.filetype.scheduler.utils.SchedulerTheme;
import com.intellij.icons.AllIcons;
import com.intellij.ui.Gray;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.autocomplete.ShorthandCompletion;
import org.fife.ui.autocomplete.TemplateCompletion;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.io.IOException;
import java.util.List;

/**
 * Panel de scripting profesional basado en {@link RSyntaxTextArea}.
 *
 * <p>Refactorización de {@link AtiScriptPanel} que sustituye el {@code JTextArea} por
 * {@code RSyntaxTextArea} para obtener las capacidades de un editor moderno:</p>
 *
 * <ul>
 *   <li><b>Syntax highlighting</b> configurable (Groovy, Java, Python, XML, JSON, etc.).</li>
 *   <li><b>Autocompletado</b> con activación automática, ventana de descripción y soporte
 *       de snippets / templates.</li>
 *   <li><b>Números de línea</b> integrados en el gutter de {@code RTextScrollPane}.</li>
 *   <li><b>Code folding</b> con indicador en el gutter.</li>
 *   <li><b>Cierre automático</b> de llaves {@code {}} y tags de marcado.</li>
 *   <li><b>Mark occurrences</b>: resalta todas las ocurrencias de la palabra seleccionada.</li>
 *   <li><b>Toolbar</b> con botones de copiar al portapapeles y expandir en diálogo.</li>
 *   <li><b>Tirador</b> en la esquina inferior derecha para redimensionar verticalmente.</li>
 *   <li><b>Bordes focus/blur</b> coherentes con el resto del plugin.</li>
 * </ul>
 *
 * <h3>Uso básico</h3>
 * <pre>{@code
 * ScriptArea scriptArea = new ScriptArea(SyntaxConstants.SYNTAX_STYLE_GROOVY, true);
 * scriptArea.addTemplate("if", "if statement", "if (${condition}) {\n\t${cursor}\n}");
 * scriptArea.addShorthand("sout", "System.out.println(${cursor});", "Print to stdout");
 * panel.add(scriptArea);
 * }</pre>
 */
public class AtiScriptPanel extends JPanel {

    // ----------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------

    private static final int HANDLE_SIZE = 24;
    private static final int DEFAULT_INITIAL_ROWS = 6;

    // ----------------------------------------------------------------
    // Fields
    // ----------------------------------------------------------------

    /** Área de texto con syntax highlighting. */
    private final RSyntaxTextArea textArea;

    /** Controla si se muestra el botón de expansión en la toolbar. */
    private final boolean showExpandButton;

    /** Número de filas visibles inicialmente. */
    private final int initialRows;

    /** Proveedor de sugerencias de autocompletado. */
    private DefaultCompletionProvider completionProvider;

    /** Motor de autocompletado; guardado para reinstalar si se cambia el provider. */
    private AutoCompletion autoCompletion;

    // Resize drag state
    private Point dragStartPoint;
    private Dimension startSize;

    // ----------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------

    /**
     * Constructor principal con número de filas por defecto (6).
     */
    public AtiScriptPanel() {
        this(DEFAULT_INITIAL_ROWS);
    }

    /**
     * Constructor que permite especificar el número de filas visibles inicialmente.
     *
     * @param initialRows número de filas visibles al abrir el editor
     */
    public AtiScriptPanel(int initialRows) {
        this.initialRows = initialRows;
        this.showExpandButton = true;
        this.textArea = buildTextArea(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
        this.completionProvider = buildDefaultProvider();

        setLayout(new BorderLayout());
        setOpaque(false);

        RTextScrollPane scrollPane = buildScrollPane();

        JPanel toolbar = buildToolbar();
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(true);
        topBar.setBackground(new JBColor(Gray._220, Gray._70));
        topBar.add(toolbar, BorderLayout.EAST);

        JPanel resizablePart = buildResizablePanel(scrollPane, topBar);
        add(resizablePart, BorderLayout.CENTER);

        installAutoCompletion();
    }

    // ----------------------------------------------------------------
    // Private builders
    // ----------------------------------------------------------------

    private RSyntaxTextArea buildTextArea(String syntaxStyle) {
        RSyntaxTextArea area = new RSyntaxTextArea(initialRows, 80);
        area.setSyntaxEditingStyle(syntaxStyle);

        // Aspecto visual
        area.setAntiAliasingEnabled(true);
        area.setFont(new Font("JetBrains Mono", Font.PLAIN, 12));
        area.setBackground(SchedulerTheme.BG_CARD);
        area.setForeground(SchedulerTheme.TEXT_MAIN);
        area.setBorder(JBUI.Borders.empty(4, 6));

        // Funcionalidades de editor profesional
        area.setCodeFoldingEnabled(true);
        area.setAutoIndentEnabled(true);
        area.setCloseCurlyBraces(true);
        area.setCloseMarkupTags(true);
        area.setTabSize(4);
        area.setTabsEmulated(false);
        area.setLineWrap(false);
        area.setHighlightCurrentLine(true);
        area.setCurrentLineHighlightColor(new JBColor(
                new Color(232, 242, 254), new Color(50, 55, 65)));
        area.setAnimateBracketMatching(true);
        area.setMarkOccurrences(true);
        area.setMarkOccurrencesColor(new JBColor(
                new Color(255, 235, 165), new Color(80, 70, 30)));
        area.setPaintTabLines(false);

        // Tema visual "idea.xml" (paleta similar a IntelliJ Light)
        try {
            Theme theme = Theme.load(
                    AtiScriptPanel.class.getResourceAsStream(
                            "/org/fife/ui/rsyntaxtextarea/themes/idea.xml"));
            theme.apply(area);
            area.setBackground(SchedulerTheme.BG_CARD); // restaura color del plugin
        } catch (IOException | NullPointerException ignored) {
            // Sin tema → RSyntaxTextArea usa colores por defecto
        }

        return area;
    }

    private RTextScrollPane buildScrollPane() {
        RTextScrollPane sp = new RTextScrollPane(textArea);
        sp.setLineNumbersEnabled(true);
        sp.setFoldIndicatorEnabled(true);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        sp.setBorder(JBUI.Borders.empty());

        // Estilo del gutter
        sp.getGutter().setBackground(new JBColor(Gray._240, Gray._50));
        sp.getGutter().setLineNumberColor(new JBColor(Gray._130, Gray._140));
        sp.getGutter().setLineNumberFont(textArea.getFont());
        sp.getGutter().setBorderColor(new JBColor(Gray._220, Gray._60));

        return sp;
    }

    private JPanel buildResizablePanel(RTextScrollPane scrollPane, JPanel topBar) {
        Border defaultBorder = new CompoundBorder(
                JBUI.Borders.customLine(SchedulerTheme.BORDER_SOFT, 1),
                JBUI.Borders.empty(1)
        );
        Border focusedBorder = new CompoundBorder(
                JBUI.Borders.customLine(SchedulerTheme.BBVA_NAVY, 2),
                JBUI.Borders.empty()
        );

        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new JBColor(Gray._150, Gray._120));
                int w = getWidth(), h = getHeight();
                g2.drawLine(w - 10, h - 3, w - 3, h - 10);
                g2.drawLine(w - 6, h - 3, w - 3, h - 6);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(defaultBorder);
        panel.add(topBar, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Focus border
        textArea.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                panel.setBorder(focusedBorder);
                panel.repaint();
            }
            @Override public void focusLost(FocusEvent e) {
                panel.setBorder(defaultBorder);
                panel.repaint();
            }
        });

        // Drag para resize
        MouseAdapter resizeAdapter = buildResizeAdapter(panel);
        panel.addMouseListener(resizeAdapter);
        panel.addMouseMotionListener(resizeAdapter);
        textArea.addMouseListener(resizeAdapter);
        textArea.addMouseMotionListener(resizeAdapter);

        // Altura inicial exacta
        panel.addHierarchyListener(hevt -> {
            if ((hevt.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && panel.isShowing()) {
                FontMetrics fm = textArea.getFontMetrics(textArea.getFont());
                int lineH = fm.getHeight();
                int insets = textArea.getInsets().top + textArea.getInsets().bottom;
                int scrollBarH = scrollPane.getHorizontalScrollBar().getPreferredSize().height;
                int topBarH = topBar.getPreferredSize().height;
                int targetH = lineH * initialRows + insets + scrollBarH + topBarH + 4;
                panel.setPreferredSize(new Dimension(panel.getWidth(), targetH));
                panel.revalidate();
            }
        });

        return panel;
    }

    private MouseAdapter buildResizeAdapter(JPanel resizablePart) {
        return new MouseAdapter() {
            private boolean resizing = false;

            @Override
            public void mousePressed(MouseEvent e) {
                if (isInHandle(e, resizablePart)) {
                    resizing = true;
                    dragStartPoint = SwingUtilities.convertPoint(
                            (Component) e.getSource(), e.getPoint(), resizablePart);
                    startSize = resizablePart.getSize();
                }
            }

            @Override public void mouseReleased(MouseEvent e) { resizing = false; }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (!resizing) return;
                Point current = SwingUtilities.convertPoint(
                        (Component) e.getSource(), e.getPoint(), resizablePart);
                int newHeight = Math.max(60, startSize.height + (current.y - dragStartPoint.y));
                resizablePart.setPreferredSize(new Dimension(resizablePart.getWidth(), newHeight));
                resizablePart.revalidate();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                resizablePart.setCursor(isInHandle(e, resizablePart)
                        ? Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR)
                        : Cursor.getDefaultCursor());
            }
        };
    }

    private JPanel buildToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 2));
        bar.setOpaque(false);

        JButton copyBtn = createIconButton(AllIcons.Actions.Copy, "Copy to clipboard");
        copyBtn.addActionListener(e ->
                Toolkit.getDefaultToolkit()
                        .getSystemClipboard()
                        .setContents(new StringSelection(textArea.getText()), null));
        bar.add(copyBtn);

        if (showExpandButton) {
            JButton expandBtn = createIconButton(AllIcons.General.ExpandComponent, "Expand");
            expandBtn.addActionListener(e -> showExpandDialog());
            bar.add(expandBtn);
        }

        return bar;
    }

    private DefaultCompletionProvider buildDefaultProvider() {
        DefaultCompletionProvider provider = new DefaultCompletionProvider();
        // Trigger on letters (first param = true) AND on these extra chars
        // Second param null means: only trigger on letter/digit keypresses (no extra char triggers)
        provider.setAutoActivationRules(true, null);
        addJavaScriptCompletions(provider);
        return provider;
    }

    private void installAutoCompletion() {
        autoCompletion = new AutoCompletion(completionProvider);

        // Never auto-insert the single match — always require explicit user confirmation
        autoCompletion.setAutoCompleteSingleChoices(false);

        // Show popup automatically while typing after a short delay
        autoCompletion.setAutoActivationEnabled(true);
        autoCompletion.setAutoActivationDelay(200);

        // Ctrl+Space as manual trigger; popup navigable with Up/Down, confirm with Enter/Tab, dismiss with Escape
        autoCompletion.setAutoCompleteEnabled(true);

        // REQUIRED for TemplateCompletion to actually expand placeholders (${cursor}, ${varName}, etc.)
        autoCompletion.setParameterAssistanceEnabled(true);

        // Show the description side-panel next to the suggestion list
        autoCompletion.setShowDescWindow(false);

        autoCompletion.install(textArea);
    }

    private void addJavaScriptCompletions(DefaultCompletionProvider p) {

        // ── b ────────────────────────────────────────────────────────────────
        p.addCompletion(new BasicCompletion(p, "break", null, null));

        // ── c ────────────────────────────────────────────────────────────────
        p.addCompletion(new BasicCompletion(p, "case", null, null));
        p.addCompletion(new TemplateCompletion(p, "class",
                "class definition",
                "class ${name} {\n\tconstructor(${params}) {\n\t\t${cursor}\n\t}\n}",
                "class",
                null));
        p.addCompletion(new BasicCompletion(p, "const", null, null));
        p.addCompletion(new BasicCompletion(p, "continue", null, null));

        // ── d ────────────────────────────────────────────────────────────────
        p.addCompletion(new BasicCompletion(p, "default", null, null));
        p.addCompletion(new BasicCompletion(p, "delete", null, null));
        p.addCompletion(new TemplateCompletion(p, "do",
                "do Loop",
                "do {\n\t${cursor}\n} while (${condition});",
                "do",
                null));

        // ── e ────────────────────────────────────────────────────────────────
        p.addCompletion(new BasicCompletion(p, "export", null, null));
        p.addCompletion(new BasicCompletion(p, "extends", null, null));

        // ── f ────────────────────────────────────────────────────────────────
        p.addCompletion(new BasicCompletion(p, "false", null, null));
        p.addCompletion(new BasicCompletion(p, "finally", null, null));
        p.addCompletion(new TemplateCompletion(p, "for",
                "for Loop",
                "for (let ${index} = 0; ${index} < ${bound}; ${index}++) {\n\t${cursor}\n}",
                "for",
                null));
        p.addCompletion(new TemplateCompletion(p, "forof",
                "for of Loop",
                "for (let ${name} of ${collectionName}) {\n\t${cursor}\n}",
                "for of",
                null));
        p.addCompletion(new TemplateCompletion(p, "function",
                "function Definition",
                "function ${name}(${params}) {\n\t${cursor}\n}",
                "function",
                null));

        // ── i ────────────────────────────────────────────────────────────────
        p.addCompletion(new TemplateCompletion(p, "if",
                "if block",
                "if (${condition}) {\n\t${cursor}\n}",
                "if",
                null));
        p.addCompletion(new TemplateCompletion(p, "ifelse",
                "if / else block",
                "if (${condition}) {\n\t${cursor}\n} else {\n\t${cursor}\n}",
                "if / else",
                null));
        p.addCompletion(new TemplateCompletion(p, "import",
                "import named",
                "import {${names}} from \"${module}\"",
                "import named",
                null));
        p.addCompletion(new TemplateCompletion(p, "importdefault",
                "import default",
                "import ${name} from \"${module}\"",
                "import default",
                null));
        p.addCompletion(new BasicCompletion(p, "in", null, null));
        p.addCompletion(new BasicCompletion(p, "instanceof", null, null));

        // ── l ────────────────────────────────────────────────────────────────
        p.addCompletion(new BasicCompletion(p, "let", null,null));

        // ── n ────────────────────────────────────────────────────────────────
        p.addCompletion(new BasicCompletion(p, "new", null, null));

        // ── r ────────────────────────────────────────────────────────────────
        p.addCompletion(new BasicCompletion(p, "return", null, null));

        // ── s ────────────────────────────────────────────────────────────────
        p.addCompletion(new BasicCompletion(p, "static", null, null));
        p.addCompletion(new BasicCompletion(p, "super", null, null));
        p.addCompletion(new BasicCompletion(p, "switch", null, null));

        // ── t ────────────────────────────────────────────────────────────────
        p.addCompletion(new BasicCompletion(p, "this", null, null));
        p.addCompletion(new BasicCompletion(p, "throw", null, null));
        p.addCompletion(new BasicCompletion(p, "true", null, null));
        p.addCompletion(new TemplateCompletion(p, "try",
                "try / catch block",
                "try {\n\t${cursor}\n} catch (${error}) {\n\t\n}",
                "try / catch",
                null));
        p.addCompletion(new BasicCompletion(p, "typeof", null, null));

        // ── v ────────────────────────────────────────────────────────────────
        p.addCompletion(new BasicCompletion(p, "var", null, null));

        // ── w ────────────────────────────────────────────────────────────────
        p.addCompletion(new TemplateCompletion(p, "while",
                "while Loop",
                "while (${condition}) {\n\t${cursor}\n}",
                "while",
                null));

        // ── y ────────────────────────────────────────────────────────────────
        p.addCompletion(new BasicCompletion(p, "yield", null,null));
    }

    // ----------------------------------------------------------------
    // Expand dialog
    // ----------------------------------------------------------------

    private void showExpandDialog() {
        Window owner = SwingUtilities.getWindowAncestor(this);

        JDialog dialog;
        if (owner instanceof Frame) {
            dialog = new JDialog((Frame) owner, "Script", true);
        } else if (owner instanceof Dialog) {
            dialog = new JDialog((Dialog) owner, "Script", true);
        } else {
            dialog = new JDialog((Frame) null, "Script", true);
        }

        AtiScriptPanel innerPanel = new AtiScriptPanel();
        innerPanel.setText(textArea.getText());
        innerPanel.getTextArea().setCaretPosition(0);

        JButton okBtn = new JButton("OK");
        JButton cancelBtn = new JButton("Cancel");
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        btnPanel.add(cancelBtn);
        btnPanel.add(okBtn);

        JPanel content = new JPanel(new BorderLayout());
        content.setBorder(JBUI.Borders.empty(8));
        content.add(innerPanel, BorderLayout.CENTER);
        content.add(btnPanel, BorderLayout.SOUTH);

        dialog.setContentPane(content);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(900, 600);
        dialog.setLocationRelativeTo(owner);

        innerPanel.addHierarchyListener(hevt -> {
            if ((hevt.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && innerPanel.isShowing()) {
                SwingUtilities.invokeLater(() -> {
                    innerPanel.setPreferredSize(null);
                    innerPanel.revalidate();
                });
            }
        });

        content.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
        content.getActionMap().put("cancel", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { dialog.dispose(); }
        });

        final boolean[] accepted = {false};
        okBtn.addActionListener(ev -> { accepted[0] = true; dialog.dispose(); });
        cancelBtn.addActionListener(ev -> dialog.dispose());

        dialog.setVisible(true);

        if (accepted[0]) {
            textArea.setText(innerPanel.getText());
        }
    }

    // ----------------------------------------------------------------
    // Public API
    // ----------------------------------------------------------------

    /** Devuelve el {@link RSyntaxTextArea} subyacente para configuración avanzada. */
    public RSyntaxTextArea getTextArea() { return textArea; }

    /** Obtiene el texto del editor. */
    public String getText() { return textArea.getText(); }

    /** Establece el texto del editor. */
    public void setText(String text) { textArea.setText(text); }

    /** Devuelve el {@code Document} para añadir {@code DocumentListener}s externos. */
    public javax.swing.text.Document getDocument() { return textArea.getDocument(); }

    /**
     * Cambia el estilo de syntax highlighting en caliente.
     *
     * @param syntaxStyle Constante de {@link SyntaxConstants}.
     */
    public void setSyntaxStyle(String syntaxStyle) {
        textArea.setSyntaxEditingStyle(syntaxStyle);
    }

    /**
     * Añade palabras clave simples al autocompletado.
     *
     * @param words Lista de palabras clave.
     */
    public void addCompletions(List<String> words) {
        for (String word : words) {
            completionProvider.addCompletion(new BasicCompletion(completionProvider, word));
        }
    }

    /**
     * Añade un shorthand: {@code inputText} se expande a {@code replacement} al aceptar.
     *
     * @param inputText   Texto que escribe el usuario (ej. {@code "sout"}).
     * @param replacement Texto expandido (ej. {@code "System.out.println();"}).
     * @param description Texto visible en la ventana de descripción del popup.
     */
    public void addShorthand(String inputText, String replacement, String description) {
        completionProvider.addCompletion(
                new ShorthandCompletion(completionProvider, inputText, replacement, description));
    }

    /**
     * Añade un template con soporte de placeholders ({@code ${cursor}}, {@code ${varName}}).
     *
     * <p>Ejemplo:</p>
     * <pre>{@code
     * addTemplate("for", "for loop",
     *     "for (int ${i} = 0; ${i} < ${end}; ${i}++) {\n\t${cursor}\n}");
     * }</pre>
     *
     * @param name        Nombre del snippet (texto que escribe el usuario).
     * @param description Descripción visible en la ventana de ayuda del popup.
     * @param template    Cuerpo del snippet con placeholders.
     */
    public void addTemplate(String name, String description, String template) {
        completionProvider.addCompletion(
                new TemplateCompletion(completionProvider, name, description, template));
    }

    /**
     * Reemplaza el proveedor de autocompletado por uno personalizado.
     *
     * @param provider Implementación de {@link CompletionProvider}.
     */
    public void setCompletionProvider(CompletionProvider provider) {
        autoCompletion.uninstall();
        autoCompletion = new AutoCompletion(provider);
        autoCompletion.setAutoCompleteSingleChoices(false);
        autoCompletion.setAutoActivationEnabled(true);
        autoCompletion.setAutoActivationDelay(200);
        autoCompletion.setAutoCompleteEnabled(true);
        autoCompletion.setParameterAssistanceEnabled(true);
        autoCompletion.setShowDescWindow(true);
        autoCompletion.install(textArea);
    }

    /**
     * Activa o desactiva el popup de autocompletado automático al escribir.
     *
     * @param enabled {@code true} para activar.
     */
    public void setAutoActivationEnabled(boolean enabled) {
        autoCompletion.setAutoActivationEnabled(enabled);
    }

    // ----------------------------------------------------------------
    // Private helpers
    // ----------------------------------------------------------------

    private boolean isInHandle(MouseEvent e, JComponent wrapper) {
        Point p = SwingUtilities.convertPoint((Component) e.getSource(), e.getPoint(), wrapper);
        return p.x >= wrapper.getWidth() - HANDLE_SIZE && p.y >= wrapper.getHeight() - HANDLE_SIZE;
    }

    private JButton createIconButton(Icon icon, String tooltip) {
        JButton btn = new JButton(icon);
        btn.setToolTipText(tooltip);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(20, 20));
        return btn;
    }
}


