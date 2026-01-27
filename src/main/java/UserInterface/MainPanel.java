package UserInterface;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import ContextFreeGrammar.CFG;
import DeterministicFiniteAutomaton.DFA;
import NondeterministicFiniteAutomaton.NFA;
import PushDownAutomaton.PDA;
import RegularExpression.RegularExpression;
import TuringMachine.TM;
import common.Automaton;

public class MainPanel extends JPanel {
    public JPanel recentFilesPanel;
    private MainFrame parentFrame;

    private JPanel objectPanel;
    private AutomatonPanel currentActivePanel;
    
    // Tab system
    private ArrayList<AutomatonTab> openTabs;
    private int activeTabIndex;
    private JPanel tabPanel;
    private JPanel tabButtonsPanel;
    
    // Split pane for resizable sidebar
    private JSplitPane mainSplitPane; 
    
    // Preferences manager for persistent storage
    private static PreferencesManager preferencesManager = new PreferencesManager();

    // Initialize TestSettings with the preferences manager on class load
    static {
        TestSettings.setPreferencesManager(preferencesManager);
    }

    public class FileManager {
        public String getExtensionForAutomaton(Automaton automaton) {
            return automaton.getFileExtension();
        }

        public Color getColorForExtension(String extension) {
            switch(extension.toLowerCase()) {
                case ".nfa":
                case ".pda":
                    return new Color(230, 250, 230);
                case ".dfa": 
                case ".tm":
                    return new Color(250, 240, 230);
                case ".cfg":
                case ".rex":
                    return new Color(240, 230, 250);
                default:
                    return Color.LIGHT_GRAY;
            }
        }
        
       
        public JPanel createPanelForFile(File file) throws Exception {
            String content = new String(java.nio.file.Files.readAllBytes(file.toPath()));
            String extension = getFileExtension(file);
            Automaton automaton = null;
            JPanel panel = null;

            switch(extension) {
                case ".nfa":
                    automaton = new NFA();
                    automaton.setInputText(content);
                    panel = new NFAPanel(MainPanel.this, automaton);
                    ((NFAPanel)panel).loadFile(file);
                    break;
                case ".pda":
                    automaton = new PDA();
                    automaton.setInputText(content);
                    panel = new PDAPanel(MainPanel.this, automaton);
                    ((PDAPanel)panel).loadFile(file);
                    break;
                case ".dfa":
                    automaton = new DFA();
                    automaton.setInputText(content);
                    panel = new DFAPanel(MainPanel.this, automaton);
                    ((DFAPanel)panel).loadFile(file);
                    break;
                case ".tm":
                    automaton = new TM();
                    automaton.setInputText(content);
                    panel = new TMPanel(MainPanel.this, automaton);
                    ((TMPanel)panel).loadFile(file);
                    break;
                case ".cfg":
                    automaton = new CFG();
                    automaton.setInputText(content);
                    panel = new CFGPanel(MainPanel.this, automaton);
                    ((CFGPanel)panel).loadFile(file);
                    break;
                case ".rex":
                    automaton = new RegularExpression();
                    panel = new REXPanel(MainPanel.this, automaton);
                    ((REXPanel)panel).loadFile(file);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported file type: " + extension);
            }

            return panel;
        }
        
        /**
         * Adds a file to recent files if not already present
         */
        public void addToRecentFiles(File file) {
            preferencesManager.addRecentFile(file.getAbsolutePath());
            refreshRecentFilesList();
            if (parentFrame != null) {
                parentFrame.updateRecentFilesMenu();
            }
        }
        
        /**
         * Opens a file and displays it in the object panel
         */
        public void openFile(File file) {
            try {
                JPanel panel = createPanelForFile(file);
                addToRecentFiles(file);
                
                // Create a new tab for this file
                if (panel instanceof AutomatonPanel) {
                    String tabTitle = file.getName();
                    AutomatonTab newTab = new AutomatonTab(tabTitle, (AutomatonPanel) panel, file);
                    addTab(newTab);
                }
                
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(MainPanel.this,
                    "Error opening file: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
        
        /**
         * Shows file chooser to select and open a file
         */
        public void showOpenDialog() {
            JFileChooser fileChooser = new JFileChooser();
            
            // Set the current directory to the last used directory if available
            File lastDir = MainPanel.preferencesManager.getLastDirectoryAsFile();
            if (lastDir != null) {
                fileChooser.setCurrentDirectory(lastDir);
            }
            
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Automaton Files", "nfa", "pda", "tm", "dfa", "rex", "cfg", "txt");
            fileChooser.setFileFilter(filter);
            
            int option = fileChooser.showOpenDialog(MainPanel.this);
            if (option == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                // Remember the directory for next time
                MainPanel.preferencesManager.setLastDirectory(file.getParent());
                openFile(file);
            }
        }
        
        /**
         * Shows save dialog and returns the selected file with proper extension
         */
        public File showSaveDialog(Automaton automaton, String currentFileName) {
            JFileChooser fileChooser = new JFileChooser();
            
            // Set the current directory to the last used directory if available
            File lastDir = MainPanel.preferencesManager.getLastDirectoryAsFile();
            if (lastDir != null) {
                fileChooser.setCurrentDirectory(lastDir);
            }
            String extension = getExtensionForAutomaton(automaton);
            String filterName = extension.substring(1).toUpperCase() + " Files (*" + extension + ")";
            fileChooser.setFileFilter(new FileNameExtensionFilter(filterName, extension.substring(1)));
            
            if (currentFileName != null) {
                fileChooser.setSelectedFile(new File(currentFileName));
            }
            
            int option = fileChooser.showSaveDialog(MainPanel.this);
            if (option == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (!file.getName().toLowerCase().endsWith(extension)) {
                    file = new File(file.toString() + extension);
                }
                // Remember the directory for next time
                MainPanel.preferencesManager.setLastDirectory(file.getParent());
                return file;
            }
            return null;
        }
        
        private String getFileExtension(File file) {
            String name = file.getName();
            int lastDot = name.lastIndexOf(".");
            return lastDot > 0 ? name.substring(lastDot) : "";
        }
    }
    
    public FileManager fileManager;

    /**
     * Creates and configures a button for a recent file
     */
    private void addRecentFileButton(File file) {
        JButton fileButton = new JButton(file.getName());
        fileButton.setMaximumSize(new Dimension(280, 40));
        fileButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        String extension = file.getName().substring(file.getName().lastIndexOf("."));
        fileButton.setBackground(fileManager.getColorForExtension(extension));
        
        fileButton.addActionListener(e -> fileManager.openFile(file));
        
        // Add right-click context menu
        JPopupMenu contextMenu = new JPopupMenu();
        JMenuItem removeItem = new JMenuItem("Remove from Recent Files");
        removeItem.addActionListener(e -> {
            preferencesManager.removeRecentFile(file.getAbsolutePath());
            refreshRecentFilesList();
            if (parentFrame != null) {
                parentFrame.updateRecentFilesMenu();
            }
        });
        contextMenu.add(removeItem);
        
        fileButton.setComponentPopupMenu(contextMenu);
        
        recentFilesPanel.add(fileButton);
        recentFilesPanel.add(Box.createVerticalStrut(5));
    }
    
    /**
     * Refreshes the recent files UI
     */
    private void refreshRecentFilesUI() {
        recentFilesPanel.revalidate();
        recentFilesPanel.repaint();
    }
    
    /**
     * Loads recent files from PreferencesManager and creates buttons for them
     */
    private void loadRecentFilesButtons() {
        for(String filePath : preferencesManager.getRecentFiles()) {
            File file = new File(filePath);
            if (file.exists()) {
                addRecentFileButton(file);
            } else {
                // Remove non-existent files from recent files
                preferencesManager.removeRecentFile(filePath);
            }
        }
    }
    
    /**
     * Refreshes the entire recent files list from PreferencesManager
     */
    private void refreshRecentFilesList() {
        // Clear current UI
        recentFilesPanel.removeAll();
        
        // Re-add header
        JLabel recentFilesLabel = new JLabel("Recent Files");
        recentFilesLabel.setFont(new Font("Arial", Font.BOLD, 16));
        recentFilesLabel.setForeground(new Color(64, 64, 64));
        recentFilesPanel.add(recentFilesLabel);
        recentFilesPanel.add(Box.createVerticalStrut(10));
        
        // Re-load all recent files
        loadRecentFilesButtons();
        
        // Refresh UI
        refreshRecentFilesUI();
    }
    
    /**
     * Creates the welcome panel with "New File" and "Open From Computer" buttons
     */
    private JPanel createWelcomePanel() {
        JPanel welcomePanel = new JPanel();
        welcomePanel.setLayout(new BorderLayout());
        welcomePanel.setBackground(Color.WHITE);
        
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel("Welcome to CS.410 Graph System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 28));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setForeground(new Color(64, 64, 64));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(50, 0, 20, 0));
        
        JLabel subtitleLabel = new JLabel("Choose an option to get started");
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        subtitleLabel.setForeground(new Color(128, 128, 128));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
        
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttonsPanel.setBackground(Color.WHITE);
        buttonsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JButton newFileButton = createStyledButton("Create New File", Color.darkGray);
        newFileButton.addActionListener(e -> showNewAutomatonMenu(newFileButton));
        
        JButton openFileButton = createStyledButton("Open From Computer", Color.darkGray);
        openFileButton.addActionListener(e -> fileManager.showOpenDialog());
        
        buttonsPanel.add(newFileButton);
        buttonsPanel.add(Box.createVerticalStrut(15));
        buttonsPanel.add(openFileButton);
        
        contentPanel.add(titleLabel);
        contentPanel.add(subtitleLabel);
        contentPanel.add(buttonsPanel);
        contentPanel.add(Box.createVerticalGlue());
        
        welcomePanel.add(contentPanel, BorderLayout.CENTER);
        
        return welcomePanel;
    }
    
    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(backgroundColor);
        button.setOpaque(true); // Required for Mac to show background color
        button.setBorder(BorderFactory.createEmptyBorder(12, 25, 12, 25));
        button.setFocusPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(250, 45));
        button.setPreferredSize(new Dimension(250, 45));
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor.darker());
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor);
            }
        });
        
        return button;
    }
    
    private void showNewAutomatonMenu(JButton parentButton) {
        JPopupMenu menu = new JPopupMenu();
        
        JMenuItem nfaItem = new JMenuItem("NFA (Nondeterministic Finite Automaton)");
        JMenuItem dfaItem = new JMenuItem("DFA (Deterministic Finite Automaton)");
        
        JMenuItem pdaItem = new JMenuItem("PDA (Push-down Automaton)");
        JMenuItem tmItem = new JMenuItem("TM (Turing Machine)");
        
        JMenuItem cfgItem = new JMenuItem("CFG (Context-Free Grammar)");
        JMenuItem rexItem = new JMenuItem("REX (Regular Expression)");
        
        nfaItem.addActionListener(e -> createNewAutomaton("NFA"));
        dfaItem.addActionListener(e -> createNewAutomaton("DFA"));
        pdaItem.addActionListener(e -> createNewAutomaton("PDA"));
        tmItem.addActionListener(e -> createNewAutomaton("TM"));
        cfgItem.addActionListener(e -> createNewAutomaton("CFG"));
        rexItem.addActionListener(e -> createNewAutomaton("REX"));
        
        menu.add(nfaItem);
        menu.add(dfaItem);
        menu.addSeparator();
        menu.add(pdaItem);
        menu.add(tmItem);
        menu.addSeparator();
        menu.add(cfgItem);
        menu.add(rexItem);
        
        // Show menu below the button
        menu.show(parentButton, 0, parentButton.getHeight());
    }

    public MainPanel() {
        this.fileManager = new FileManager();
        this.setLayout(new BorderLayout());
        this.setSize(900, 400); 
        this.setBackground(new Color(250, 250, 250)); 

        openTabs = new ArrayList<>();
        activeTabIndex = -1;

        /*
         * Recent files panel (left side).
         * This panel contains the recent files that the user has opened.
         */
        recentFilesPanel = new JPanel();
        recentFilesPanel.setLayout(new BoxLayout(recentFilesPanel, BoxLayout.Y_AXIS));
        recentFilesPanel.setPreferredSize(new Dimension(200, 400));
        recentFilesPanel.setMinimumSize(new Dimension(150, 0));
        recentFilesPanel.setBackground(Color.WHITE);
        recentFilesPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        // Create center panel with tab system and object panel
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(Color.WHITE);
        
        createTabPanel();
        centerPanel.add(tabPanel, BorderLayout.NORTH);

        /*
         * Object panel (center).
         * This panel contains the current automaton being edited.
         */
        objectPanel = new JPanel(); 
        objectPanel.setLayout(new BorderLayout());
        objectPanel.setBackground(Color.WHITE);
        objectPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        JPanel welcomePanel = createWelcomePanel();
        objectPanel.add(welcomePanel, BorderLayout.CENTER);
        
        centerPanel.add(objectPanel, BorderLayout.CENTER);

        // Recent Files header
        JLabel recentFilesLabel = new JLabel("Recent Files");
        recentFilesLabel.setFont(new Font("Arial", Font.BOLD, 16));
        recentFilesLabel.setForeground(new Color(64, 64, 64));
        recentFilesPanel.add(recentFilesLabel);
        recentFilesPanel.add(Box.createVerticalStrut(10));

        // Load existing recent files
        loadRecentFilesButtons();
        
        // Restore previous session if files were open
        restorePreviousSession();

        // Create split pane for resizable sidebar
        mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, recentFilesPanel, centerPanel);
        mainSplitPane.setDividerLocation(200); // Initial position
        mainSplitPane.setResizeWeight(0.0); // Center panel gets extra space
        mainSplitPane.setDividerSize(5); // Thin divider
        mainSplitPane.setBorder(null); // Remove default border
        
        this.add(mainSplitPane, BorderLayout.CENTER);

    }
    
    /**
     * Toggles the visibility of the sidebar (recent files panel)
     */
    public void toggleSidebar() {
        if (recentFilesPanel.isVisible()) {
            // Hide sidebar
            mainSplitPane.setDividerLocation(0);
            recentFilesPanel.setVisible(false);
        } else {
            // Show sidebar
            recentFilesPanel.setVisible(true);
            mainSplitPane.setDividerLocation(200);
        }
        revalidate();
        repaint();
    }

    /**
     * Gets the currently active automaton panel, or null if none.
     */
    private AbstractAutomatonPanel getActiveAutomatonPanel() {
        if (activeTabIndex >= 0 && activeTabIndex < openTabs.size()) {
            AutomatonTab activeTab = openTabs.get(activeTabIndex);
            if (activeTab.getPanel() instanceof AbstractAutomatonPanel) {
                return (AbstractAutomatonPanel) activeTab.getPanel();
            }
        }
        return null;
    }

    /**
     * Shows the test settings popup anchored to the current active panel's settings button.
     * Called from the Settings menu in MainFrame.
     */
    public void showTestSettingsPopup() {
        AbstractAutomatonPanel activePanel = getActiveAutomatonPanel();
        if (activePanel != null) {
            activePanel.showSettingsPopup();
        }
    }

    /**
     * Creates the tab panel with buttons for navigation
     */
    private void createTabPanel() {
        tabPanel = new JPanel(new BorderLayout());
        tabPanel.setBackground(new Color(248, 248, 248));
        tabPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(4, 8, 0, 8)
        ));
        tabPanel.setPreferredSize(new Dimension(0, 42));
        
        tabButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 8));
        tabButtonsPanel.setBackground(new Color(248, 248, 248));
        
        tabPanel.add(tabButtonsPanel, BorderLayout.WEST);
        
        // Initially hide the tab panel
        tabPanel.setVisible(false);
    }
    
    /**
     * Updates the tab buttons display
     */
    private void updateTabButtons() {
        tabButtonsPanel.removeAll();
        
        if (openTabs.isEmpty()) {
            tabPanel.setVisible(false);
            return;
        }
        
        tabPanel.setVisible(true);
        
        for (int i = 0; i < openTabs.size(); i++) {
            AutomatonTab tab = openTabs.get(i);
            final int tabIndex = i;
            
            JPanel tabButtonPanel = new JPanel(new BorderLayout());
            tabButtonPanel.setOpaque(false);
            
            JButton tabButton = new JButton(tab.getDisplayTitle());
            tabButton.setFont(new Font("Arial", Font.PLAIN, 12));
            tabButton.setBorderPainted(false);
            tabButton.setFocusPainted(false);
            tabButton.setMargin(new Insets(6, 12, 6, 12));
            
            // Subtle tab styling
            if (i == activeTabIndex) {
                tabButton.setBackground(Color.WHITE);
                tabButton.setForeground(Color.BLACK);
                tabButton.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 1, 0, 1, new Color(180, 180, 180)),
                    BorderFactory.createEmptyBorder(4, 8, 4, 8)
                ));
                tabButton.setOpaque(true);
            } else {
                tabButton.setBackground(new Color(245, 245, 245));
                tabButton.setForeground(new Color(80, 80, 80));
                tabButton.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(220, 220, 220)),
                    BorderFactory.createEmptyBorder(4, 8, 4, 8)
                ));
                tabButton.setOpaque(true);
            }
            
            // Add hover effect for inactive tabs
            if (i != activeTabIndex) {
                tabButton.addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent evt) {
                        if (tabIndex != activeTabIndex) {
                            tabButton.setBackground(new Color(235, 235, 235));
                        }
                    }
                    
                    @Override
                    public void mouseExited(java.awt.event.MouseEvent evt) {
                        if (tabIndex != activeTabIndex) {
                            tabButton.setBackground(new Color(245, 245, 245));
                        }
                    }
                });
            }
            
            tabButton.addActionListener(e -> switchToTab(tabIndex));
            
            // Enhanced close button
            JButton closeButton = new JButton("×");
            closeButton.setFont(new Font("Arial", Font.BOLD, 14));
            closeButton.setForeground(new Color(120, 120, 120));
            closeButton.setBorderPainted(false);
            closeButton.setFocusPainted(false);
            closeButton.setContentAreaFilled(false);
            closeButton.setMargin(new Insets(0, 4, 0, 4));
            closeButton.setPreferredSize(new Dimension(20, 20));
            closeButton.setToolTipText("Close tab");
            
            closeButton.addActionListener(e -> closeTab(tabIndex));
            
            // Enhanced hover effect for close button
            closeButton.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    closeButton.setForeground(new Color(150, 80, 80));
                    closeButton.setBackground(new Color(250, 245, 245));
                    closeButton.setContentAreaFilled(true);
                    closeButton.setBorder(BorderFactory.createRaisedBevelBorder());
                }
                
                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    closeButton.setForeground(new Color(120, 120, 120));
                    closeButton.setContentAreaFilled(false);
                    closeButton.setBorderPainted(false);
                }
            });
            
            tabButtonPanel.add(tabButton, BorderLayout.CENTER);
            tabButtonPanel.add(closeButton, BorderLayout.EAST);
            
            tabButtonsPanel.add(tabButtonPanel);
        }
        
        tabButtonsPanel.revalidate();
        tabButtonsPanel.repaint();
    }
    
    /**
     * Switches to the specified tab
     */
    private void switchToTab(int tabIndex) {
        if (tabIndex < 0 || tabIndex >= openTabs.size()) {
            return;
        }
        
        activeTabIndex = tabIndex;
        AutomatonTab tab = openTabs.get(tabIndex);
        
        // Update current active panel
        setCurrentActivePanel(tab.getPanel());
        
        objectPanel.removeAll();
        objectPanel.add((JPanel) tab.getPanel());
        objectPanel.revalidate();
        objectPanel.repaint();
        
        // Update tab buttons to show active state
        updateTabButtons();
    }
    
    /**
     * Closes a tab with save prompt if needed
     */
    private void closeTab(int tabIndex) {
        if (tabIndex < 0 || tabIndex >= openTabs.size()) {
            return;
        }
        
        AutomatonTab tab = openTabs.get(tabIndex);
        
        // Update unsaved status before checking
        tab.updateUnsavedStatus();
        
        // Check if tab has unsaved changes
        if (tab.hasUnsavedChanges()) {
            int result = JOptionPane.showConfirmDialog(
                this,
                "File '" + tab.getTitle() + "' has unsaved changes. Do you want to save before closing?",
                "Unsaved Changes",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE
            );
            
            if (result == JOptionPane.YES_OPTION) {
                tab.getPanel().saveAutomaton();
                tab.markAsSaved();
            } else if (result == JOptionPane.CANCEL_OPTION || result == JOptionPane.CLOSED_OPTION) {
                return;
            }
        }
        
        openTabs.remove(tabIndex);
        
        // Update active tab index
        if (activeTabIndex >= tabIndex) {
            activeTabIndex--;
        }
        
        // If no tabs left, show welcome panel
        if (openTabs.isEmpty()) {
            activeTabIndex = -1;
            currentActivePanel = null;
            objectPanel.removeAll();
            objectPanel.add(createWelcomePanel());
            objectPanel.revalidate();
            objectPanel.repaint();
        } else {
            if (activeTabIndex < 0) {
                activeTabIndex = 0;
            } else if (activeTabIndex >= openTabs.size()) {
                activeTabIndex = openTabs.size() - 1;
            }
            switchToTab(activeTabIndex);
        }
        
        updateTabButtons();
    }
    
    /**
     * Adds a new tab
     */
    private void addTab(AutomatonTab tab) {
        openTabs.add(tab);
        activeTabIndex = openTabs.size() - 1;
        
        tab.getPanel().addTextChangeListener(() -> {
            tab.updateUnsavedStatus();
            updateTabButtons();
        });
        
        switchToTab(activeTabIndex);
    }
    
    /**
     * Creates a new automaton of the specified type
     * Called from the menu bar
     */
    public void createNewAutomaton(String type) {
        Automaton automaton = null;
        JPanel panel = null;

        switch(type) {
            case "NFA":
                automaton = new NFA();
                panel = new NFAPanel(this, automaton);
                break;
            case "DFA":
                automaton = new DFA();
                panel = new DFAPanel(this, automaton);
                break;
            case "PDA":
                automaton = new PDA();
                panel = new PDAPanel(this, automaton);
                break;
            case "TM":
                automaton = new TM();
                panel = new TMPanel(this, automaton);
                break;
            case "CFG":
                automaton = new CFG();
                panel = new CFGPanel(this, automaton);
                break;
            case "REX":
                automaton = new RegularExpression();
                panel = new REXPanel(this, automaton);
                break;
            default:
                JOptionPane.showMessageDialog(this, "Unknown automaton type: " + type, "Error", JOptionPane.ERROR_MESSAGE);
                return;
        }

        // Apply template to new automaton panels
        if (automaton != null && panel instanceof AbstractAutomatonPanel) {
            ((AbstractAutomatonPanel) panel).setInitialContent(automaton.getDefaultTemplate());
        }

        if (panel != null && panel instanceof AutomatonPanel) {
            String tabTitle = "New " + type;
            AutomatonTab newTab = new AutomatonTab(tabTitle, (AutomatonPanel) panel, null);
            addTab(newTab);
        }
    }
    
    /**
     * Sets the currently active panel for Actions menu delegation
     */
    public void setCurrentActivePanel(AutomatonPanel panel) {
        this.currentActivePanel = panel;
    }
    
    /**
     * Compiles the automaton and generates GraphViz visualization
     */
    public void compileWithFigureCurrentAutomaton() {
        if (currentActivePanel != null) {
            currentActivePanel.compileWithFigure();
        } else {
            JOptionPane.showMessageDialog(this, "No active automaton panel found.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Saves the currently active automaton (quick save)
     */
    public void saveCurrentAutomaton() {
        if (currentActivePanel != null) {
            currentActivePanel.saveAutomaton();
        } else {
            JOptionPane.showMessageDialog(this, "No active automaton panel found.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Save As the currently active automaton (always shows dialog)
     */
    public void saveAsCurrentAutomaton() {
        if (currentActivePanel != null) {
            currentActivePanel.saveAsAutomaton();
        } else {
            JOptionPane.showMessageDialog(this, "No active automaton panel found.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Runs tests from the matching .test file
     */
    public void runCurrentAutomaton() {
        if (currentActivePanel != null) {
            currentActivePanel.run();
        } else {
            JOptionPane.showMessageDialog(this, "No active automaton panel found.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Runs tests from a user-selected test file
     */
    public void runCurrentAutomatonWithFile() {
        if (currentActivePanel != null) {
            currentActivePanel.runWithFile();
        } else {
            JOptionPane.showMessageDialog(this, "No active automaton panel found.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Marks the current active tab as saved
     */
    public void markCurrentTabAsSaved() {
        if (activeTabIndex >= 0 && activeTabIndex < openTabs.size()) {
            AutomatonTab activeTab = openTabs.get(activeTabIndex);
            activeTab.markAsSaved();
            
            if (activeTab.getFile() != null) {
                activeTab.setTitle(activeTab.getFile().getName());
            }
            updateTabButtons();
        }
    }
    
    /**
     * Updates the current tab's file association (used during Save As operations)
     */
    public void updateCurrentTabFile(File newFile) {
        if (activeTabIndex >= 0 && activeTabIndex < openTabs.size()) {
            AutomatonTab activeTab = openTabs.get(activeTabIndex);
            activeTab.setFile(newFile);
            activeTab.setTitle(newFile.getName());
            
            // Also update the panel's file reference
            if (activeTab.getPanel() instanceof AbstractAutomatonPanel) {
                ((AbstractAutomatonPanel) activeTab.getPanel()).setCurrentFile(newFile);
            }
            
            updateTabButtons();
        }
    }
    
    /**
     * Switches to the next tab (cycles to first if at end)
     */
    public void switchToNextTab() {
        if (openTabs.isEmpty()) return;
        
        int nextIndex = (activeTabIndex + 1) % openTabs.size();
        switchToTab(nextIndex);
    }
    
    /**
     * Switches to the previous tab (cycles to last if at beginning)
     */
    public void switchToPreviousTab() {
        if (openTabs.isEmpty()) return;
        
        int prevIndex = activeTabIndex - 1;
        if (prevIndex < 0) {
            prevIndex = openTabs.size() - 1;
        }
        switchToTab(prevIndex);
    }
    
    /**
     * Gets recent files for menu integration
     */
    public java.util.List<String> getRecentFiles() {
        return preferencesManager.getRecentFiles();
    }
    
    /**
     * Clears all recent files
     */
    public void clearRecentFiles() {
        preferencesManager.clearRecentFiles();
        refreshRecentFilesList();
        if (parentFrame != null) {
            parentFrame.updateRecentFilesMenu();
        }
    }
    
    /**
     * Opens a recent file by path
     */
    public void openRecentFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            fileManager.openFile(file);
        } else {
            preferencesManager.removeRecentFile(filePath);
            refreshRecentFilesList();
            if (parentFrame != null) {
                parentFrame.updateRecentFilesMenu();
            }
            JOptionPane.showMessageDialog(this,
                "File no longer exists: " + filePath,
                "File Not Found",
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    /**
     * Sets the parent frame for menu updates
     */
    public void setParentFrame(MainFrame frame) {
        this.parentFrame = frame;
    }
    
    /**
     * Saves currently open tabs for session restore
     */
    public void saveCurrentSession() {
        java.util.List<String> openFilePaths = new java.util.ArrayList<>();
        
        for (AutomatonTab tab : openTabs) {
            if (tab.getFile() != null) {
                openFilePaths.add(tab.getFile().getAbsolutePath());
            }
        }
        
        preferencesManager.setLastOpenedFiles(openFilePaths);
    }
    
    /**
     * Restores previously opened files from last session
     */
    private void restorePreviousSession() {
        java.util.List<String> lastOpenedFiles = preferencesManager.getLastOpenedFiles();
        
        for (String filePath : lastOpenedFiles) {
            File file = new File(filePath);
            if (file.exists()) {
                try {
                    fileManager.openFile(file);
                } catch (Exception ex) {
                    System.err.println("Error restoring file: " + filePath + " - " + ex.getMessage());
                }
            } else {
                // Remove non-existent files from last opened files
                preferencesManager.removeRecentFile(filePath);
            }
        }
    }
}
