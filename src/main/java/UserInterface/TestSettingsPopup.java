package UserInterface;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import controller.AutomatonController;
import service.SessionService;

/**
 * Popup panel for configuring global test settings.
 * Appears anchored below the settings button.
 * Uses controller for settings access instead of singleton.
 */
public class TestSettingsPopup extends JPopupMenu {

    // Controller for settings access
    private final AutomatonController controller;

    // ═══════════════════════════════════════════════════════════════════
    // COLORS
    // ═══════════════════════════════════════════════════════════════════

    private static final Color BACKGROUND_COLOR = new Color(255, 255, 255);
    private static final Color SECTION_BG_COLOR = new Color(248, 249, 250);
    private static final Color SECTION_BORDER_COLOR = new Color(224, 224, 224);
    private static final Color HEADER_COLOR = new Color(73, 80, 87);
    private static final Color INFO_COLOR = new Color(108, 117, 125);

    // ═══════════════════════════════════════════════════════════════════
    // UI COMPONENTS
    // ═══════════════════════════════════════════════════════════════════

    private JSpinner minPointsSpinner;
    private JSpinner maxPointsSpinner;
    private JSpinner timeoutSpinner;
    private JTextField maxRulesField;
    private JTextField maxTransitionsField;
    private JTextField maxRegexLengthField;

    private boolean isUpdating = false; // Prevent recursive updates

    // ═══════════════════════════════════════════════════════════════════
    // CONSTRUCTOR
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Creates a test settings popup.
     *
     * @param controller The controller for accessing settings
     */
    public TestSettingsPopup(AutomatonController controller) {
        this.controller = controller;

        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(SECTION_BORDER_COLOR, 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        createUI();
        loadFromSettings();
        setupAutoSave();
    }

    // ═══════════════════════════════════════════════════════════════════
    // UI CREATION
    // ═══════════════════════════════════════════════════════════════════

    private void createUI() {
        // Header
        JLabel headerLabel = new JLabel("\u2699  Test Settings");
        headerLabel.setFont(new Font("Arial", Font.BOLD, 14));
        headerLabel.setForeground(HEADER_COLOR);
        headerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(headerLabel);
        add(Box.createVerticalStrut(15));

        // Grading section
        add(createGradingSection());
        add(Box.createVerticalStrut(10));

        // Execution section
        add(createExecutionSection());
        add(Box.createVerticalStrut(10));

        // Limits section
        add(createLimitsSection());
        add(Box.createVerticalStrut(10));

        // Info section
        add(createInfoSection());
        add(Box.createVerticalStrut(15));

        // Buttons
        add(createButtonSection());
    }

    private JPanel createGradingSection() {
        JPanel panel = createSectionPanel("\uD83D\uDCCA Grading");

        // Min points
        JPanel minRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        minRow.setOpaque(false);
        minRow.add(createLabel("Min Points"));
        minPointsSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
        minPointsSpinner.setPreferredSize(new Dimension(70, 25));
        minRow.add(minPointsSpinner);
        panel.add(minRow);

        // Max points
        JPanel maxRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        maxRow.setOpaque(false);
        maxRow.add(createLabel("Max Points"));
        maxPointsSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
        maxPointsSpinner.setPreferredSize(new Dimension(70, 25));
        maxRow.add(maxPointsSpinner);
        panel.add(maxRow);

        return panel;
    }

    private JPanel createExecutionSection() {
        JPanel panel = createSectionPanel("\u23F1 Execution");

        JPanel timeoutRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        timeoutRow.setOpaque(false);
        timeoutRow.add(createLabel("Timeout (sec)"));
        timeoutSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 300, 1));
        timeoutSpinner.setPreferredSize(new Dimension(70, 25));
        timeoutRow.add(timeoutSpinner);
        panel.add(timeoutRow);

        return panel;
    }

    private JPanel createLimitsSection() {
        JPanel panel = createSectionPanel("\uD83D\uDCCF Limits (optional)");

        // Max rules (CFG)
        JPanel rulesRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        rulesRow.setOpaque(false);
        rulesRow.add(createLabel("Max Rules (CFG)"));
        maxRulesField = createOptionalIntField();
        rulesRow.add(maxRulesField);
        panel.add(rulesRow);

        // Max transitions (PDA)
        JPanel transRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        transRow.setOpaque(false);
        transRow.add(createLabel("Max Trans (PDA)"));
        maxTransitionsField = createOptionalIntField();
        transRow.add(maxTransitionsField);
        panel.add(transRow);

        // Max regex length (REX)
        JPanel regexRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        regexRow.setOpaque(false);
        regexRow.add(createLabel("Max Length (REX)"));
        maxRegexLengthField = createOptionalIntField();
        regexRow.add(maxRegexLengthField);
        panel.add(regexRow);

        // Info label
        JLabel infoLabel = new JLabel("\u2139 Empty = no limit");
        infoLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        infoLabel.setForeground(INFO_COLOR);
        infoLabel.setBorder(new EmptyBorder(5, 5, 0, 0));
        panel.add(infoLabel);

        return panel;
    }

    private JPanel createInfoSection() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(232, 244, 253));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(179, 215, 243), 1),
            new EmptyBorder(8, 10, 8, 10)
        ));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(270, 60));

        JLabel infoLabel = new JLabel("<html>\u2139 Test file headers (#timeout, #max_rules, etc.)<br>override these settings per-file.</html>");
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        infoLabel.setForeground(new Color(49, 112, 143));
        panel.add(infoLabel);

        return panel;
    }

    private JPanel createButtonSection() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(270, 35));

        JButton resetButton = new JButton("\u21BA Reset");
        resetButton.setFont(new Font("Arial", Font.PLAIN, 12));
        resetButton.setForeground(INFO_COLOR);
        resetButton.setToolTipText("Reset all settings to defaults");
        resetButton.addActionListener(e -> resetToDefaults());

        JButton closeButton = new JButton("\u2713 Close");
        closeButton.setFont(new Font("Arial", Font.PLAIN, 12));
        closeButton.addActionListener(e -> setVisible(false));

        panel.add(resetButton);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(closeButton);

        return panel;
    }

    // ═══════════════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════════════════════

    private JPanel createSectionPanel(String title) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(SECTION_BG_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(SECTION_BORDER_COLOR, 1),
            new EmptyBorder(8, 10, 8, 10)
        ));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.setMaximumSize(new Dimension(270, 150));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        titleLabel.setForeground(HEADER_COLOR);
        titleLabel.setBorder(new EmptyBorder(0, 0, 5, 0));
        panel.add(titleLabel);

        return panel;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 12));
        label.setPreferredSize(new Dimension(120, 20));
        return label;
    }

    private JTextField createOptionalIntField() {
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(70, 25));
        field.setHorizontalAlignment(JTextField.CENTER);
        return field;
    }

    // ═══════════════════════════════════════════════════════════════════
    // SETTINGS INTEGRATION
    // ═══════════════════════════════════════════════════════════════════

    private void loadFromSettings() {
        isUpdating = true;
        try {
            SessionService.TestSettings settings = controller.getTestSettings();
            minPointsSpinner.setValue(settings.getMinPoints());
            maxPointsSpinner.setValue(settings.getMaxPoints());
            timeoutSpinner.setValue(settings.getTimeoutSeconds());
            maxRulesField.setText(integerToString(settings.getMaxRules()));
            maxTransitionsField.setText(integerToString(settings.getMaxTransitions()));
            maxRegexLengthField.setText(integerToString(settings.getMaxRegexLength()));
        } finally {
            isUpdating = false;
        }
    }

    private void saveToSettings() {
        if (isUpdating) return;

        SessionService sessionService = controller.getSessionService();
        sessionService.setTestMinPoints((Integer) minPointsSpinner.getValue());
        sessionService.setTestMaxPoints((Integer) maxPointsSpinner.getValue());
        sessionService.setTestTimeout((Integer) timeoutSpinner.getValue());
        sessionService.setTestMaxRules(parseOptionalInt(maxRulesField.getText()));
        sessionService.setTestMaxTransitions(parseOptionalInt(maxTransitionsField.getText()));
        sessionService.setTestMaxRegexLength(parseOptionalInt(maxRegexLengthField.getText()));
    }

    private void resetToDefaults() {
        controller.getSessionService().resetTestSettings();
        loadFromSettings();
    }

    private void setupAutoSave() {
        // Spinners auto-save on change
        minPointsSpinner.addChangeListener(e -> saveToSettings());
        maxPointsSpinner.addChangeListener(e -> saveToSettings());
        timeoutSpinner.addChangeListener(e -> saveToSettings());

        // Text fields auto-save on change
        DocumentListener docListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { saveToSettings(); }
            @Override
            public void removeUpdate(DocumentEvent e) { saveToSettings(); }
            @Override
            public void changedUpdate(DocumentEvent e) { saveToSettings(); }
        };

        maxRulesField.getDocument().addDocumentListener(docListener);
        maxTransitionsField.getDocument().addDocumentListener(docListener);
        maxRegexLengthField.getDocument().addDocumentListener(docListener);
    }

    // ═══════════════════════════════════════════════════════════════════
    // PARSING HELPERS
    // ═══════════════════════════════════════════════════════════════════

    private String integerToString(Integer value) {
        return value == null ? "" : String.valueOf(value);
    }

    private Integer parseOptionalInt(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            int value = Integer.parseInt(text.trim());
            return value >= 1 ? value : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // SHOW METHOD
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Shows the popup anchored below the given component.
     */
    public void showRelativeTo(Component anchor) {
        loadFromSettings(); // Refresh values when showing
        show(anchor, 0, anchor.getHeight());
    }
}
