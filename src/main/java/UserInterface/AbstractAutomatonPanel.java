package UserInterface;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.io.*;
import java.util.List;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.undo.UndoManager;
import common.Automaton;
import common.ExecutionResult;
import common.MachineType;
import common.ParseResult;
import common.TestRunner;
import common.ValidationMessage;
import controller.AutomatonController;
import service.SessionService;
import service.TestService;
import viewmodel.TestResultViewModel;
import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.svg.SVGDocument;

/**
 * Abstract base class for all automaton panels to eliminate code duplication.
 * Contains all common UI components and functionality.
 */
public abstract class AbstractAutomatonPanel extends JPanel implements AutomatonPanel {

    // Configuration flag for inline testing feature
    private static final boolean ENABLE_INLINE_TESTING = true; // Set to false for production
    
    protected JPanel textEditorPanel, graphPanel, topPanel;
    protected JTextArea textArea;
    protected JScrollPane scrollPane;
    protected JTextArea warningField;
    protected File file;
    protected MainPanel mainPanel;
    protected Automaton automaton;
    protected UndoManager undoManager;
    protected JButton settingsButton;
    protected TestSettingsPopup settingsPopup;

    // Inline testing components
    protected JPanel inlineTestPanel;
    protected JTextField inlineTestInput;
    protected JLabel inlineTestResult;
    protected JButton inlineTestButton;
    protected JButton clearGraphButton;
    // Loading indicator components
    private JPanel loadingPanel;
    private JLabel loadingSpinner;
    private JLabel loadingText;
    private Timer spinnerTimer;
    private int spinnerAngle = 0;
    
    /**
     * Result class to pass data from background thread to UI thread
     */
    private static class GraphGenerationResult {
        public final ParseResult parseResult;
        public final JSVGCanvas imageCanvas;
        public final String inputText;

        public GraphGenerationResult(ParseResult parseResult, JSVGCanvas imageCanvas, String inputText) {
            this.parseResult = parseResult;
            this.imageCanvas = imageCanvas;
            this.inputText = inputText;
        }
    }

    /**
     * Constructor that sets up the common UI layout
     */
    public AbstractAutomatonPanel(MainPanel mainPanel, Automaton automaton) {
        this.mainPanel = mainPanel;
        this.automaton = automaton;

        initializePanel();
        createTopPanel();
        if (ENABLE_INLINE_TESTING) {
            createInlineTestPanel();
        }
        createTextEditorPanel();
        createGraphPanel();
        createWarningPanel();
        assembleLayout();
    }

    /**
     * Abstract method to get the tab label text for this automaton type
     */
    protected abstract String getTabLabelText();

    /**
     * Gets the controller from the main panel for delegating operations.
     */
    protected AutomatonController getController() {
        return mainPanel.getController();
    }

    /**
     * Initialize the main panel properties
     */
    private void initializePanel() {
        this.setLayout(new BorderLayout());
        this.setSize(600, 400);
    }

    /**
     * Creates the top panel with tab label, settings button, and action buttons.
     */
    private void createTopPanel() {
        topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        topPanel.setBackground(Color.WHITE);

        // Tab label on the left
        JLabel tabLabel = new JLabel(getTabLabelText());
        tabLabel.setFont(new Font("Arial", Font.BOLD, 14));
        tabLabel.setForeground(new Color(102, 133, 102));

        // Settings button (gear icon)
        settingsButton = new JButton("\u2699");
        settingsButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        settingsButton.setPreferredSize(new Dimension(40, 30));
        settingsButton.setMaximumSize(new Dimension(40, 30));
        settingsButton.setToolTipText("Test Settings");
        settingsButton.setFocusPainted(false);
        settingsButton.addActionListener(e -> showSettingsPopup());

        // Run button
        JButton runButton = new JButton("\u25B6 Run");
        runButton.setPreferredSize(new Dimension(80, 30));
        runButton.setMaximumSize(new Dimension(80, 30));
        runButton.setToolTipText("Run test file");
        runButton.addActionListener(e -> runTestFile());

        // Clear Graph button
        clearGraphButton = new JButton("Clear");
        clearGraphButton.setPreferredSize(new Dimension(80, 30));
        clearGraphButton.setMaximumSize(new Dimension(80, 30));
        clearGraphButton.setToolTipText("Clear the current graph visualization (Ctrl+G)");
        clearGraphButton.addActionListener(e -> clearGraph());

        // Assemble the panel: [Label] ─────── [⚙] [▶ Run] [Clear]
        topPanel.add(tabLabel);
        topPanel.add(Box.createHorizontalGlue());
        topPanel.add(settingsButton);
        topPanel.add(Box.createHorizontalStrut(10));
        topPanel.add(runButton);
        topPanel.add(Box.createHorizontalStrut(5));
        topPanel.add(clearGraphButton);
    }

    /**
     * Shows the settings popup anchored to the settings button.
     */
    public void showSettingsPopup() {
        if (settingsPopup == null) {
            settingsPopup = new TestSettingsPopup(mainPanel.getController());
        }
        settingsPopup.showRelativeTo(settingsButton);
    }

    /**
     * Create the inline test panel for quick testing
     */
    private void createInlineTestPanel() {
        inlineTestPanel = new JPanel();
        inlineTestPanel.setLayout(new BoxLayout(inlineTestPanel, BoxLayout.X_AXIS));
        inlineTestPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
        inlineTestPanel.setBackground(new Color(245, 245, 245));
        
        // Create components
        JLabel testLabel = new JLabel("Quick Test: ");
        testLabel.setFont(new Font("Arial", Font.BOLD, 12));
        
        inlineTestInput = new JTextField();
        inlineTestInput.setMaximumSize(new Dimension(300, 30));
        inlineTestInput.setPreferredSize(new Dimension(200, 30));
        inlineTestInput.setToolTipText("Enter input string to test (empty for epsilon)");
        
        inlineTestButton = new JButton("Test");
        inlineTestButton.setPreferredSize(new Dimension(70, 30));
        
        inlineTestResult = new JLabel("");
        inlineTestResult.setFont(new Font("Arial", Font.BOLD, 12));
        inlineTestResult.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));
        
        // Add action listener for test button
        inlineTestButton.addActionListener(e -> runInlineTest());
        
        // Add enter key listener for input field
        inlineTestInput.addActionListener(e -> runInlineTest());
        
        // Assemble the panel
        inlineTestPanel.add(testLabel);
        inlineTestPanel.add(Box.createHorizontalStrut(10));
        inlineTestPanel.add(inlineTestInput);
        inlineTestPanel.add(Box.createHorizontalStrut(10));
        inlineTestPanel.add(inlineTestButton);
        inlineTestPanel.add(Box.createHorizontalStrut(15));
        inlineTestPanel.add(inlineTestResult);
        inlineTestPanel.add(Box.createHorizontalGlue());
    }
    
    /**
     * Run inline test for the entered input.
     * Uses the controller for parsing and execution.
     */
    private void runInlineTest() {
        String input = inlineTestInput.getText();
        String automatonText = textArea.getText();

        // Parse the automaton using the controller
        ParseResult parseResult = getController().parse(automaton, automatonText);

        if (!parseResult.isSuccess()) {
            inlineTestResult.setText("⚠ Parse Error");
            inlineTestResult.setForeground(new Color(200, 100, 0));
            updateWarningDisplayWithParseResult(parseResult, automatonText);
            return;
        }

        // Execute the test using the controller
        try {
            Automaton parsedAutomaton = parseResult.getAutomaton();
            ExecutionResult execResult = getController().execute(parsedAutomaton, input);

            boolean accepted = execResult.isAccepted();
            String displayInput = input.isEmpty() ? "ε" : "\"" + input + "\"";

            if (accepted) {
                inlineTestResult.setText("✓ " + displayInput + " → ACCEPT");
                inlineTestResult.setForeground(new Color(0, 150, 0));
            } else {
                inlineTestResult.setText("✗ " + displayInput + " → REJECT");
                inlineTestResult.setForeground(new Color(200, 0, 0));
            }

            // Update warning field with execution trace
            String traceInfo = "Quick Test Result:\n";
            traceInfo += "Input: " + displayInput + "\n";
            traceInfo += "Result: " + (accepted ? "ACCEPTED" : "REJECTED") + "\n";
            if (execResult.getTrace() != null && !execResult.getTrace().isEmpty()) {
                traceInfo += "\nExecution Trace:\n" + execResult.getTrace();
            }
            warningField.setText(traceInfo);
            warningField.setCaretPosition(0);

        } catch (Exception e) {
            inlineTestResult.setText("⚠ Execution Error");
            inlineTestResult.setForeground(new Color(200, 100, 0));
            warningField.setText("Execution Error: " + e.getMessage());
        }
    }

    /**
     * Create the text editor panel with line numbers
     */
    private void createTextEditorPanel() {
        textEditorPanel = new JPanel();
        textEditorPanel.setLayout(new BorderLayout());
        textEditorPanel.setPreferredSize(new Dimension(300, 300));
        textEditorPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        textArea = new JTextArea();
        scrollPane = new JScrollPane(textArea);

        TextLineNumber lineNumbering = new TextLineNumber(textArea);
        scrollPane.setRowHeaderView(lineNumbering);

        textEditorPanel.add(scrollPane, BorderLayout.CENTER);

        // Set up undo/redo functionality
        setupUndoRedo();
    }

    /**
     * Set up undo/redo functionality for the text area
     */
    private void setupUndoRedo() {
        // Create and configure UndoManager
        undoManager = new UndoManager();
        undoManager.setLimit(1000); // Limit undo history to prevent memory issues

        // Add UndoManager directly as the listener (it implements UndoableEditListener)
        textArea.getDocument().addUndoableEditListener(undoManager);

        // Get the platform-specific menu shortcut key mask (Command on Mac, Control on Windows/Linux)
        // Handle headless environment for testing
        int menuShortcutKeyMask;
        if (java.awt.GraphicsEnvironment.isHeadless()) {
            menuShortcutKeyMask = java.awt.event.InputEvent.CTRL_DOWN_MASK;
        } else {
            menuShortcutKeyMask = java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        }

        // Define unique action key names to avoid conflicts with parent component action maps
        final String UNDO_ACTION_KEY = "cs410-text-undo";
        final String REDO_ACTION_KEY = "cs410-text-redo";
        final String REDO_ALT_ACTION_KEY = "cs410-text-redo-alt";

        // Get both WHEN_FOCUSED and WHEN_ANCESTOR_OF_FOCUSED_COMPONENT input maps
        // We register in both to handle all focus scenarios (direct focus and wrapped in JScrollPane)
        InputMap inputMapFocused = textArea.getInputMap(JComponent.WHEN_FOCUSED);
        InputMap inputMapAncestor = textArea.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap actionMap = textArea.getActionMap();

        // Remove any existing conflicting actions from ActionMap
        actionMap.remove("undo");
        actionMap.remove("redo");
        actionMap.remove("redo-alt");

        // Create KeyStroke objects (reuse for consistency)
        KeyStroke undoKeyStroke = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, menuShortcutKeyMask);
        KeyStroke redoKeyStroke = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, menuShortcutKeyMask);
        KeyStroke redoAltKeyStroke = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z,
            menuShortcutKeyMask | java.awt.event.InputEvent.SHIFT_DOWN_MASK);

        // Remove any existing bindings for these keystrokes in both InputMaps
        inputMapFocused.remove(undoKeyStroke);
        inputMapFocused.remove(redoKeyStroke);
        inputMapFocused.remove(redoAltKeyStroke);
        inputMapAncestor.remove(undoKeyStroke);
        inputMapAncestor.remove(redoKeyStroke);
        inputMapAncestor.remove(redoAltKeyStroke);

        // Install new bindings in BOTH InputMaps for redundancy
        // WHEN_FOCUSED - when textArea has direct focus
        inputMapFocused.put(undoKeyStroke, UNDO_ACTION_KEY);
        inputMapFocused.put(redoKeyStroke, REDO_ACTION_KEY);
        inputMapFocused.put(redoAltKeyStroke, REDO_ALT_ACTION_KEY);

        // WHEN_ANCESTOR_OF_FOCUSED_COMPONENT - when wrapped in JScrollPane
        inputMapAncestor.put(undoKeyStroke, UNDO_ACTION_KEY);
        inputMapAncestor.put(redoKeyStroke, REDO_ACTION_KEY);
        inputMapAncestor.put(redoAltKeyStroke, REDO_ALT_ACTION_KEY);

        // Undo: Ctrl+Z (or Cmd+Z on Mac)
        actionMap.put(UNDO_ACTION_KEY, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (undoManager.canUndo()) {
                    try {
                        undoManager.undo();
                    } catch (Exception ex) {
                        // Silently handle undo errors to prevent disrupting user workflow
                        System.err.println("Error during undo: " + ex.getMessage());
                    }
                }
            }
        });

        // Redo: Ctrl+Y (or Cmd+Y on Mac)
        actionMap.put(REDO_ACTION_KEY, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (undoManager.canRedo()) {
                    try {
                        undoManager.redo();
                    } catch (Exception ex) {
                        // Silently handle redo errors to prevent disrupting user workflow
                        System.err.println("Error during redo: " + ex.getMessage());
                    }
                }
            }
        });

        // Alternative redo: Ctrl+Shift+Z (or Cmd+Shift+Z on Mac)
        actionMap.put(REDO_ALT_ACTION_KEY, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (undoManager.canRedo()) {
                    try {
                        undoManager.redo();
                    } catch (Exception ex) {
                        // Silently handle redo errors to prevent disrupting user workflow
                        System.err.println("Error during redo: " + ex.getMessage());
                    }
                }
            }
        });

        // Clear any initialization edits from the undo stack
        undoManager.discardAllEdits();
    }

    /**
     * Create the graph visualization panel
     */
    private void createGraphPanel() {
        graphPanel = new JPanel();
        graphPanel.setLayout(new BorderLayout());
        // Remove fixed size to allow dynamic resizing
        graphPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Graph Visualization"),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        graphPanel.setBackground(Color.WHITE);
    }

    /**
     * Create the warning/messages panel
     */
    private void createWarningPanel() {
        JPanel warningPanel = new JPanel();
        warningPanel.setLayout(new BorderLayout());
        warningPanel.setPreferredSize(new Dimension(300, 100));
        warningPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel warningLabel = new JLabel("Warnings and Messages:");
        warningLabel.setFont(new Font("Arial", Font.BOLD, 12));
        warningLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        warningField = new JTextArea("Warnings will be displayed here after using 'Compile with Figure' or 'Run' from the main Actions menu");
        warningField.setEditable(false);
        warningField.setBackground(new Color(255, 255, 255));
        warningField.setLineWrap(true);
        warningField.setWrapStyleWord(true);
        warningField.setBorder(BorderFactory.createLoweredBevelBorder());
        
        JScrollPane warningScrollPane = new JScrollPane(warningField);
        warningScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        warningScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        warningPanel.add(warningLabel, BorderLayout.NORTH);
        warningPanel.add(warningScrollPane, BorderLayout.CENTER);
        
        this.add(warningPanel, BorderLayout.EAST);
    }

    /**
     * Assemble the final layout
     */
    private void assembleLayout() {
        // Create a north panel that contains both top panel and inline test panel
        if (ENABLE_INLINE_TESTING && inlineTestPanel != null) {
            JPanel northContainer = new JPanel();
            northContainer.setLayout(new BoxLayout(northContainer, BoxLayout.Y_AXIS));
            northContainer.add(topPanel);
            northContainer.add(inlineTestPanel);
            this.add(northContainer, BorderLayout.NORTH);
        } else {
            this.add(topPanel, BorderLayout.NORTH);
        }

        // Create a horizontal split pane for text editor and graph panel with resizable divider
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, textEditorPanel, graphPanel);
        splitPane.setDividerLocation(350); // Initial divider position in pixels
        splitPane.setDividerSize(8); // Width of the divider - made more prominent
        splitPane.setContinuousLayout(true); // Smooth updates while dragging
        splitPane.setOneTouchExpandable(true); // Show expand/collapse arrows for better visibility
        splitPane.setResizeWeight(0.3); // 30% to left panel when window resizes

        // Customize the divider appearance for better visibility
        splitPane.setUI(new javax.swing.plaf.basic.BasicSplitPaneUI() {
            @Override
            public javax.swing.plaf.basic.BasicSplitPaneDivider createDefaultDivider() {
                return new javax.swing.plaf.basic.BasicSplitPaneDivider(this) {
                    @Override
                    public void paint(Graphics g) {
                        super.paint(g);
                        // Draw a more visible divider with grip lines
                        Graphics2D g2d = (Graphics2D) g.create();
                        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                        int width = getWidth();
                        int height = getHeight();

                        // Draw background
                        g2d.setColor(new Color(200, 200, 200));
                        g2d.fillRect(0, 0, width, height);

                        // Draw grip lines in the middle
                        g2d.setColor(new Color(120, 120, 120));
                        int centerX = width / 2;
                        int centerY = height / 2;
                        int gripHeight = 40;
                        int gripSpacing = 3;

                        for (int i = -2; i <= 2; i++) {
                            int y = centerY + (i * gripSpacing) - gripHeight / 2;
                            g2d.drawLine(centerX - 1, y, centerX - 1, y + gripHeight);
                            g2d.setColor(new Color(180, 180, 180));
                            g2d.drawLine(centerX, y, centerX, y + gripHeight);
                            g2d.setColor(new Color(120, 120, 120));
                        }

                        g2d.dispose();
                    }

                    @Override
                    public Cursor getCursor() {
                        return Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
                    }
                };
            }
        });

        this.add(splitPane, BorderLayout.CENTER);
    }

    // AutomatonPanel interface implementations
    @Override
    public void compileWithFigure() {
        final String inputText = textArea.getText();

        // Check if this is a machine type that doesn't support visualization
        boolean skipVisualization = !getController().supportsVisualization(automaton.getMachineType());

        final String[] errorText = {""};

        // Show loading indicator immediately
        showLoadingIndicator();

        // Create SwingWorker to handle parsing and GraphViz processing in background
        SwingWorker<GraphGenerationResult, Void> worker = new SwingWorker<GraphGenerationResult, Void>() {
            @Override
            protected GraphGenerationResult doInBackground() throws Exception {
                // Use controller for parsing and visualization generation
                AutomatonController.CompileResult compileResult =
                    getController().compileWithVisualization(automaton, inputText);

                ParseResult parseResult = new ParseResult(
                    compileResult.isSuccess(),
                    compileResult.getValidationMessages(),
                    compileResult.isSuccess() ? automaton : null
                );

                JSVGCanvas imageCanvas = null;
                if (compileResult.isSuccess() && compileResult.hasVisualization()) {
                    String svgContent = compileResult.getSvgContent();

                    boolean validSVG = svgContent != null &&
                        svgContent.startsWith("<svg") &&
                        svgContent.contains("xmlns=\"http://www.w3.org/2000/svg\"");

                    if (!validSVG && svgContent != null) {
                        errorText[0] = svgContent;
                    }

                    if (validSVG) {
                        // Create SVG canvas from the SVG content
                        StringReader svgTextReader = new StringReader(svgContent);
                        String parser = XMLResourceDescriptor.getXMLParserClassName();
                        SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);
                        SVGDocument svgDocument = factory.createSVGDocument(null, svgTextReader);
                        svgTextReader.close();

                        imageCanvas = new JSVGCanvas();
                        imageCanvas.setSVGDocument(svgDocument);

                        JSVGCanvas svgCanvas = createJSVGCanvas(imageCanvas);
                        updateGraphPanelWithImage(svgCanvas);
                    }
                } else if (!compileResult.isSuccess()) {
                    errorText[0] = compileResult.getErrorMessage();
                }

                return new GraphGenerationResult(parseResult, imageCanvas, inputText);
            }
            
            @Override
            protected void done() {
                // This runs on EDT when background work is complete
                hideLoadingIndicator();

                try {
                    GraphGenerationResult result = get(); // Get result from doInBackground()

                    // Always update warnings first to show parsing errors
                    updateWarningDisplayWithParseResult(result.parseResult, result.inputText);

                    if (!result.parseResult.isSuccess() || result.imageCanvas == null) {

                        // Parsing failed or no image generated
                        String errorMessage;
                        if (!result.parseResult.isSuccess()) {
                            errorMessage = "<h3>Parsing Failed</h3><p>Check the warnings panel for syntax errors</p>";
                        } else if (skipVisualization) {
                            // Visualization was intentionally skipped for CFG/REGEX
                            errorMessage = "<h3>Visualization Not Available</h3><p>Graph visualization is not supported for this automaton type</p>";
                        } else if (result.imageCanvas == null) {
                            errorMessage = errorText[0];
                        } else {
                            errorMessage = "<h3>Graph generation failed</h3><p>Check the warnings panel for details</p>";
                        }

                        JLabel errorLabel = new JLabel("<html><body style='text-align: center;'>" + errorMessage + "</body></html>");
                        errorLabel.setHorizontalAlignment(JLabel.CENTER);
                        errorLabel.setVerticalAlignment(JLabel.CENTER);

                        // Use a neutral color for skipped visualization, red for actual errors
                        if (skipVisualization && result.parseResult.isSuccess()) {
                            errorLabel.setForeground(new Color(100, 100, 100));
                        } else {
                            errorLabel.setForeground(new Color(150, 50, 50));
                        }

                        graphPanel.removeAll();
                        graphPanel.add(errorLabel, BorderLayout.CENTER);
                        graphPanel.revalidate();
                        graphPanel.repaint();

                    }
                } catch (Exception e) {
                    // Handle any exceptions that occurred during processing
                    e.printStackTrace();
                    hideLoadingIndicator();
                    
                    // Show error message
                    JLabel errorLabel = new JLabel("<html><body style='text-align: center;'>" +
                        "<h3>Unexpected Error</h3>" +
                        "<p>Error: " + e.getMessage() + "</p>" +
                        "</body></html>");
                    errorLabel.setHorizontalAlignment(JLabel.CENTER);
                    errorLabel.setVerticalAlignment(JLabel.CENTER);
                    errorLabel.setForeground(new Color(150, 50, 50));
                    
                    graphPanel.removeAll();
                    graphPanel.add(errorLabel, BorderLayout.CENTER);
                    graphPanel.revalidate();
                    graphPanel.repaint();
                    
                    // Update warnings to show the exception - fallback to basic validation
                    updateWarningDisplay();
                }
            }
        };
        
        // Start the background work
        worker.execute();
    }

    /**
     * Creates and configures a {@link JSVGCanvas} component that supports zooming and panning
     * interactions for displaying SVG-based automaton graphs.
     *
     * <p>This method enables users to zoom in and out with the mouse wheel and pan
     * the view by dragging with the mouse. The zooming behavior centers around
     * the mouse pointer for intuitive navigation. The transformation is constrained
     * using {@link #boundTransform(AffineTransform, JSVGCanvas, double)} to ensure
     * the image stays within reasonable view bounds.</p>
     *
     * @param imageCanvas the JSVGCanvas instance to configure for interactive zooming and panning
     * @return the configured JSVGCanvas ready for display in the graph panel
     */
    private JSVGCanvas createJSVGCanvas(JSVGCanvas imageCanvas) {

        final AffineTransform[] at = {new AffineTransform()};
        final double[] scale = {1.0};
        final double minScale = 0.5;
        final double maxScale = 3.0;

        imageCanvas.addMouseWheelListener(e -> {
            double zoomFactor = 0.1;
            double newScale = scale[0] * (e.getPreciseWheelRotation() < 0 ? 1 + zoomFactor : 1 - zoomFactor);

            newScale = Math.max(minScale, Math.min(maxScale, newScale));

            double scaleChange = newScale / scale[0];
            scale[0] = newScale;

            Point mouse = e.getPoint();
            at[0].translate(mouse.x, mouse.y);
            at[0].scale(scaleChange, scaleChange);
            at[0].translate(-mouse.x, -mouse.y);

            imageCanvas.setRenderingTransform(at[0], true);
        });

        final Point[] lastPoint = {null};
        imageCanvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastPoint[0] = e.getPoint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                lastPoint[0] = null;
            }
        });

        imageCanvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastPoint[0] != null) {
                    double dx = e.getX() - lastPoint[0].x;
                    double dy = e.getY() - lastPoint[0].y;
                    at[0].translate(dx, dy);

                    at[0] = boundTransform(at[0], imageCanvas, scale[0]);
                    imageCanvas.setRenderingTransform(at[0], true);

                    imageCanvas.setRenderingTransform(at[0], true);
                    lastPoint[0] = e.getPoint();
                }
            }
        });

        return imageCanvas;
    }

    /**
     * Constrains the given {@link AffineTransform} to ensure the SVG visualization
     * remains within reasonable view boundaries inside the {@link JSVGCanvas}.
     *
     * <p>This method prevents the user from panning the automaton graph completely
     * out of view during interactive navigation (zooming and dragging). It calculates
     * minimum and maximum translation values based on the current scale and the size
     * of both the SVG and canvas, clamping the transform's translation components
     * accordingly.</p>
     *
     * @param at the current transformation matrix (scale and translation) applied to the canvas
     * @param canvas the {@link JSVGCanvas} currently displaying the SVG visualization
     * @param scale the current zoom scale factor
     * @return a new {@link AffineTransform} adjusted so that the view remains within valid bounds
     */
    private AffineTransform boundTransform(AffineTransform at, JSVGCanvas canvas, double scale) {

        int svgWidth = graphPanel.getWidth();
        int svgHeight = graphPanel.getHeight();

        double tx = at.getTranslateX();
        double ty = at.getTranslateY();

        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight();

        double minTx = Math.min(0, canvasWidth - svgWidth * scale);
        double maxTx = Math.max(0, canvasWidth - svgWidth * scale);
        double minTy = Math.min(0, canvasHeight - svgHeight * scale);
        double maxTy = Math.max(0, canvasHeight - svgHeight * scale);

        tx = Math.min(Math.max(tx, minTx), maxTx);
        ty = Math.min(Math.max(ty, minTy), maxTy);

        AffineTransform bounded = new AffineTransform(at);
        bounded.setTransform(at.getScaleX(), at.getShearY(), at.getShearX(), at.getScaleY(), tx, ty);
        return bounded;
    }

    // Deprecated compileAutomaton - no longer needed as validation happens in compileWithFigure

    /**
     * Run tests for the current automaton using a corresponding test file
     */
    protected void runTestFile() {
        // First compile/parse the current automaton using controller
        String inputText = textArea.getText();
        automaton.setInputText(inputText);

        ParseResult parseResult = getController().parse(automaton, inputText);
        if (!parseResult.isSuccess()) {
            JOptionPane.showMessageDialog(this,
                "Cannot run tests: Automaton has parsing errors. Check warnings panel.",
                "Test Error", JOptionPane.ERROR_MESSAGE);
            updateWarningDisplay();
            return;
        }
        
        // Look for test file
        String testFilePath = findTestFile();
        if (testFilePath == null) {
            String message = "No test file found. Expected a .test file with the same name as your automaton file.\n\n";
            
            if (file != null) {
                String expectedTestFile = file.getName().replaceFirst("\\.[^.]*$", ".test");
                message += "Current file: " + file.getName() + "\n";
                message += "Expected test file: " + expectedTestFile + "\n\n";
            } else {
                message += "Please save your automaton file first, then try testing.\n\n";
            }
            
            message += "Example: if your file is q1.nfa, create q1.test with test cases.";
            
            JOptionPane.showMessageDialog(this, message, 
                "Test File Not Found", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Run tests in background
        runTestsInBackground(parseResult.getAutomaton(), testFilePath);
    }

    /**
     * Runs tests from the matching .test file (interface implementation)
     */
    @Override
    public void run() {
        runTestFile();
    }

    /**
     * Runs tests from a user-selected test file (interface implementation)
     */
    @Override
    public void runWithFile() {
        // First compile/parse the current automaton using controller
        String inputText = textArea.getText();
        automaton.setInputText(inputText);

        ParseResult parseResult = getController().parse(automaton, inputText);
        if (!parseResult.isSuccess()) {
            JOptionPane.showMessageDialog(this,
                "Cannot run tests: Automaton has parsing errors. Check warnings panel.",
                "Test Error", JOptionPane.ERROR_MESSAGE);
            updateWarningDisplay();
            return;
        }
        
        // Show file chooser for test file
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Test File");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Test Files (*.test)", "test"));
        
        // Set current directory to same as automaton file if available
        if (file != null && file.getParent() != null) {
            fileChooser.setCurrentDirectory(new File(file.getParent()));
        }
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedTestFile = fileChooser.getSelectedFile();
            
            // Run tests with selected file in background
            runTestsInBackground(parseResult.getAutomaton(), selectedTestFile.getAbsolutePath());
        }
    }

    /**
     * Run tests in background with progress dialog.
     * Uses controller to get test settings and returns ViewModels.
     * All type-specific logic (instanceof checks) is handled in the service layer.
     */
    private void runTestsInBackground(Automaton testAutomaton, String testFilePath) {
        // Get settings from controller (which uses SessionService)
        SessionService.TestSettings settings = getController().getTestSettings();

        // Validate settings
        if (settings.getMinPoints() >= settings.getMaxPoints()) {
            JOptionPane.showMessageDialog(this,
                "Minimum points must be less than maximum points.\nPlease check Test Settings.",
                "Invalid Point Configuration",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Check if test file has overrides and create effective settings
        SessionService.TestSettings effectiveSettings = createEffectiveSettings(settings, testFilePath);

        // Create progress dialog using extracted component
        TestProgressDialog progressDialog = new TestProgressDialog(
            SwingUtilities.getWindowAncestor(this),
            effectiveSettings.getTimeoutMs() / 1000
        );

        // Create SwingWorker that returns ViewModel (no instanceof checks needed in done())
        SwingWorker<TestResultViewModel, TestRunner.TestProgress> worker =
            new SwingWorker<TestResultViewModel, TestRunner.TestProgress>() {

            @Override
            protected TestResultViewModel doInBackground() throws Exception {
                // Create progress callback that publishes updates
                TestService.TestProgressCallback progressCallback = new TestService.TestProgressCallback() {
                    @Override
                    public void onTestStarted(int currentTest, int totalTests, String input) {
                        publish(TestRunner.TestProgress.started(currentTest, totalTests, input));
                    }

                    @Override
                    public void onTestCompleted(int currentTest, int totalTests, String input, boolean passed) {
                        publish(TestRunner.TestProgress.completed(currentTest, totalTests, input, passed));
                    }
                };

                // Use controller's ViewModel-returning method (all type checks happen in service)
                return getController().getTestService().runTestsWithValidation(
                    testAutomaton, testFilePath, effectiveSettings, progressCallback);
            }

            @Override
            protected void process(java.util.List<TestRunner.TestProgress> chunks) {
                // Update progress dialog with latest progress
                if (!chunks.isEmpty()) {
                    TestRunner.TestProgress latest = chunks.get(chunks.size() - 1);
                    progressDialog.updateProgress(latest);
                }
            }

            @Override
            protected void done() {
                progressDialog.close();

                try {
                    TestResultViewModel result = get();

                    // No instanceof checks needed - ViewModel tells us what to display
                    if (result.hasLimitViolation()) {
                        showLimitViolationDialog(result);
                    } else {
                        showTestResults(result);
                    }
                } catch (InterruptedException e) {
                    JOptionPane.showMessageDialog(AbstractAutomatonPanel.this,
                        "Test execution was cancelled.",
                        "Test Cancelled",
                        JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(AbstractAutomatonPanel.this,
                        "Error running tests: " + e.getMessage(),
                        "Test Execution Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        // Associate worker with dialog for cancel handling
        progressDialog.setWorker(worker);

        // Start the worker
        worker.execute();

        // Show progress dialog (blocks until disposed)
        progressDialog.setVisible(true);
    }

    /**
     * Creates effective test settings by merging UI settings with test file overrides.
     */
    private SessionService.TestSettings createEffectiveSettings(
            SessionService.TestSettings settings, String testFilePath) {

        int timeoutSeconds = settings.getTimeoutSeconds();
        Integer maxRules = settings.getMaxRules();
        Integer maxTransitions = settings.getMaxTransitions();

        try {
            common.TestFileParser.TestFileResult testFileResult =
                common.TestFileParser.parseTestFile(testFilePath);
            if (testFileResult.hasTimeout()) {
                timeoutSeconds = testFileResult.getTimeout();
            }
            if (testFileResult.hasMaxRules()) {
                maxRules = testFileResult.getMaxRules();
            }
            if (testFileResult.hasMaxTransitions()) {
                maxTransitions = testFileResult.getMaxTransitions();
            }
        } catch (Exception e) {
            // If we can't parse the test file, continue with UI values
        }

        return new SessionService.TestSettings(
            settings.getMinPoints(),
            settings.getMaxPoints(),
            timeoutSeconds,
            maxRules,
            maxTransitions,
            settings.getMaxRegexLength()
        );
    }

    /**
     * Display limit violation dialog using ViewModel data.
     */
    private void showLimitViolationDialog(TestResultViewModel result) {
        JOptionPane.showMessageDialog(this,
            result.getLimitViolationMessage(),
            result.getDialogTitle(),
            JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Display test results in a dialog using ViewModel.
     */
    private void showTestResults(TestResultViewModel result) {
        StringBuilder message = new StringBuilder();

        // Add timeout warning if any tests timed out
        if (result.hasTimeouts()) {
            int displayTimeout = getController().getTestSettings().getTimeoutSeconds();
            message.append("\u26A0\uFE0F WARNING: Test suite timed out after ")
                   .append(displayTimeout)
                   .append(" seconds total.\n")
                   .append("This may indicate infinite loops or very long computations.\n\n");
        }

        // Use the detailed report from ViewModel
        message.append(result.getDetailedReport());

        // Determine dialog type based on results
        int messageType;
        String title;
        if (result.hasTimeouts()) {
            messageType = JOptionPane.WARNING_MESSAGE;
            title = "Tests Completed with Timeouts";
        } else if (result.getFailedTests() == 0) {
            messageType = JOptionPane.INFORMATION_MESSAGE;
            title = "All Tests Passed!";
        } else if (result.getPassedTests() > 0) {
            messageType = JOptionPane.WARNING_MESSAGE;
            title = "Some Tests Failed";
        } else {
            messageType = JOptionPane.ERROR_MESSAGE;
            title = "All Tests Failed";
        }

        // Create scrollable text area for long results
        JTextArea resultArea = new JTextArea(message.toString());
        resultArea.setEditable(false);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JScrollPane resultScrollPane = new JScrollPane(resultArea);
        resultScrollPane.setPreferredSize(new Dimension(500, 350));

        JOptionPane.showMessageDialog(this, resultScrollPane, title, messageType);
    }
    
    /**
     * Find the corresponding test file for the current automaton.
     * Delegates to the controller's TestService.
     */
    private String findTestFile() {
        if (file != null) {
            File testFile = getController().findTestFile(file);
            if (testFile != null) {
                return testFile.getAbsolutePath();
            }
        }
        return null;
    }

    @Override
    public void saveAutomaton() {
        if (file != null) {
            // File already exists, just save directly (quick save)
            saveFileContent(file);
            mainPanel.markCurrentTabAsSaved();
        } else {
            // No file associated, show save dialog (Save As)
            saveAsAutomaton();
        }
    }
    
    /**
     * Save As - always shows save dialog
     */
    @Override
    public void saveAsAutomaton() {
        File savedFile = mainPanel.fileManager.showSaveDialog(automaton, file != null ? file.getName() : null);
        if (savedFile != null) {
            saveFileContent(savedFile);
            
            // Update file reference
            file = savedFile;
            
            // Add to recent files
            mainPanel.fileManager.addToRecentFiles(file);
            
            // Update tab file association and title
            mainPanel.updateCurrentTabFile(savedFile);
            mainPanel.markCurrentTabAsSaved();
        }
    }
    
    @Override
    public String getTextAreaContent() {
        return textArea.getText();
    }
    
    @Override
    public Automaton getAutomaton() {
        return automaton;
    }
    
    @Override
    public File getCurrentFile() {
        return file;
    }
    
    @Override
    public void setCurrentFile(File file) {
        this.file = file;
    }
    
    @Override
    public void addTextChangeListener(Runnable onChange) {
        textArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                onChange.run();
            }
            
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                onChange.run();
            }
            
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                onChange.run();
            }
        });
    }
    
    /**
     * Sets the initial content of the text area (used for templates)
     */
    public void setInitialContent(String content) {
        if (content != null && textArea != null) {
            textArea.setText(content);
            textArea.setCaretPosition(0);
        }
    }

    /**
     * Generate DOT code for the given input text
     */
    private String generateDotCodeForInput(String inputText) {
        try {
            automaton.setInputText(inputText);
            return automaton.toDotCode(inputText);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Updates the warning display with current validation messages.
     * Uses the controller's AutomatonService for validation and formatting.
     */
    protected void updateWarningDisplay() {
        String inputText = textArea.getText();
        List<ValidationMessage> messages = getController().validate(automaton, inputText);
        String warningText = getController().formatValidationMessages(messages);
        warningField.setText(warningText);
        warningField.setCaretPosition(0);
    }
    
    /**
     * Update warning display for a successfully parsed automaton.
     * Parses first, then validates and formats the combined messages.
     */
    protected void updateWarningDisplayForParsedAutomaton(String inputText) {
        // Parse and validate using the controller
        ParseResult parseResult = getController().parse(automaton, inputText);
        updateWarningDisplayWithParseResult(parseResult, inputText);
    }
    
    /**
     * Update warning display using an existing parse result.
     * Combines parse messages with validation messages from the controller.
     */
    protected void updateWarningDisplayWithParseResult(ParseResult parseResult, String inputText) {
        StringBuilder result = new StringBuilder();

        // Always show parsing messages first (includes syntax errors)
        List<ValidationMessage> parseMessages = parseResult.getValidationMessages();
        if (parseMessages != null && !parseMessages.isEmpty()) {
            for (ValidationMessage msg : parseMessages) {
                result.append(msg.toString()).append("\n");
            }
        }

        // If parsing succeeded, also show validation messages
        if (parseResult.isSuccess() && parseResult.getAutomaton() != null) {
            List<ValidationMessage> validationMessages = getController().validate(
                parseResult.getAutomaton(), inputText);
            if (validationMessages != null && !validationMessages.isEmpty()) {
                for (ValidationMessage msg : validationMessages) {
                    result.append(msg.toString()).append("\n");
                }
            }
        }

        String warningText = result.length() == 0
            ? "No warnings or errors found!"
            : result.toString();

        warningField.setText(warningText);
        warningField.setCaretPosition(0);
    }

    /**
     * Saves the current content to a file.
     * Delegates to the controller's FileService.
     */
    protected void saveFileContent(File file) {
        String text = textArea.getText();
        boolean success = getController().saveToFile(file, text);
        if (success) {
            JOptionPane.showMessageDialog(this, "File saved successfully!");
        } else {
            JOptionPane.showMessageDialog(this, "Error saving file.");
        }
    }

    /**
     * Loads content from a file into the text area
     */
    public void loadFile(File file) {
        try {
            // Read file content as string
            // Using setText() instead of read() to preserve the document and UndoManager connection
            String content = new String(java.nio.file.Files.readAllBytes(file.toPath()),
                                      java.nio.charset.StandardCharsets.UTF_8);
            textArea.setText(content);

            // Clear the setText edit from undo history (we don't want to undo file loading)
            if (undoManager != null) {
                undoManager.discardAllEdits();
            }

            // Set the current file so test discovery works
            this.file = file;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading file: " + e.getMessage());
        }
    }
    
    /**
     * Update the graph panel with a new image
     */
    private void updateGraphPanelWithImage(JSVGCanvas svgCanvas) {
        graphPanel.removeAll();
        graphPanel.add(svgCanvas, BorderLayout.CENTER);
        graphPanel.revalidate();
        graphPanel.repaint();
    }

    /**
     * Clear the current graph visualization from the graph panel
     */
    private void clearGraph() {
        graphPanel.removeAll();
        JLabel clearMessage = new JLabel("<html><body style='text-align: center;'>" +
                "<p>Graph cleared. Compile with Figure to generate a new visualization.</p>" +
                "</body></html>");
        clearMessage.setHorizontalAlignment(JLabel.CENTER);
        clearMessage.setVerticalAlignment(JLabel.CENTER);
        clearMessage.setForeground(new Color(100, 100, 100));

        graphPanel.add(clearMessage, BorderLayout.CENTER);
        graphPanel.revalidate();
        graphPanel.repaint();
    }
    
    /**
     * Initialize the loading indicator components
     */
    private void initializeLoadingComponents() {
        // Create loading panel
        loadingPanel = new JPanel(new BorderLayout());
        loadingPanel.setBackground(Color.WHITE);
        
        // Create spinner with custom painting
        loadingSpinner = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;
                int radius = 20;
                
                // Draw spinning dots
                for (int i = 0; i < 8; i++) {
                    double angle = (spinnerAngle + i * 45) * Math.PI / 180;
                    int x = centerX + (int) (radius * Math.cos(angle));
                    int y = centerY + (int) (radius * Math.sin(angle));
                    
                    int alpha = 255 - (i * 30);
                    if (alpha < 50) alpha = 50;
                    
                    g2d.setColor(new Color(0, 100, 200, alpha));
                    g2d.fillOval(x - 3, y - 3, 6, 6);
                }
                g2d.dispose();
            }
        };
        loadingSpinner.setPreferredSize(new Dimension(80, 80));
        loadingSpinner.setHorizontalAlignment(JLabel.CENTER);
        
        // Create loading text
        loadingText = new JLabel("Generating visualization...");
        loadingText.setHorizontalAlignment(JLabel.CENTER);
        loadingText.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        loadingText.setForeground(new Color(60, 60, 60));
        
        // Create spinner animation timer
        spinnerTimer = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                spinnerAngle = (spinnerAngle + 45) % 360;
                loadingSpinner.repaint();
            }
        });
        
        // Assemble loading panel
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(Color.WHITE);
        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(loadingSpinner);
        centerPanel.add(Box.createVerticalStrut(10));
        centerPanel.add(loadingText);
        centerPanel.add(Box.createVerticalGlue());
        
        loadingPanel.add(centerPanel, BorderLayout.CENTER);
    }
    
    /**
     * Show the loading indicator
     */
    private void showLoadingIndicator() {
        if (loadingPanel == null) {
            initializeLoadingComponents();
        }
        
        graphPanel.removeAll();
        graphPanel.add(loadingPanel, BorderLayout.CENTER);
        graphPanel.revalidate();
        graphPanel.repaint();
        
        spinnerTimer.start();
    }
    
    /**
     * Hide the loading indicator
     */
    private void hideLoadingIndicator() {
        if (spinnerTimer != null) {
            spinnerTimer.stop();
        }
    }
}