package UserInterface;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * Test utility class that provides methods to create MainPanel instances
 * safe for testing without triggering UI dialogs.
 */
public class TestMainPanelFactory {
    
    /**
     * Creates a MainPanel instance for testing that prevents file dialogs.
     * This method does NOT enable headless mode, but overrides dialog methods.
     */
    public static MainPanel createForTesting() {
        MainPanel panel = new MainPanel();

        // Clear any previous session files to prevent restoration via controller
        panel.getController().getSessionService().setLastOpenedFiles(new java.util.ArrayList<>());
        
        // Override the file manager with a mock version that doesn't show dialogs
        panel.fileManager = new TestFileManager(panel);
        
        // Clear any tabs that might have been created during initialization using reflection
        try {
            Field openTabsField = MainPanel.class.getDeclaredField("openTabs");
            openTabsField.setAccessible(true);
            ArrayList<?> openTabs = (ArrayList<?>) openTabsField.get(panel);
            if (openTabs != null) {
                openTabs.clear();
            }
            
            Field activeTabIndexField = MainPanel.class.getDeclaredField("activeTabIndex");
            activeTabIndexField.setAccessible(true);
            activeTabIndexField.set(panel, -1);
            
            // Update the UI to reflect the cleared state
            Method updateTabButtonsMethod = MainPanel.class.getDeclaredMethod("updateTabButtons");
            updateTabButtonsMethod.setAccessible(true);
            updateTabButtonsMethod.invoke(panel);
        } catch (Exception e) {
            System.err.println("Warning: Could not clear tabs during test setup: " + e.getMessage());
        }
        
        return panel;
    }
    
    /**
     * Creates a MainPanel instance for headless testing (no UI components).
     * This is useful for tests that don't need actual UI interaction.
     */
    public static MainPanel createForHeadlessTesting() {
        // Enable headless mode to prevent UI dialogs
        System.setProperty("java.awt.headless", "true");
        
        MainPanel panel = new MainPanel();
        
        // Override the file manager with a mock version that doesn't show dialogs
        panel.fileManager = new TestFileManager(panel);
        
        return panel;
    }
    
    /**
     * Test-specific FileManager that prevents actual dialogs from showing
     */
    private static class TestFileManager extends MainPanel.FileManager {
        private MainPanel parent;
        
        TestFileManager(MainPanel parent) {
            parent.super();
            this.parent = parent;
        }
        
        @Override
        public void showOpenDialog() {
            // Do nothing - prevent actual file dialog from showing
            System.out.println("TestFileManager: showOpenDialog() called - no actual dialog shown");
        }
        
        @Override
        public java.io.File showSaveDialog(common.Automaton automaton, String currentFileName) {
            // Return null to simulate user canceling the dialog
            System.out.println("TestFileManager: showSaveDialog() called - returning null (canceled)");
            return null;
        }
    }
}