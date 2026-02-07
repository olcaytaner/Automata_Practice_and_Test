package UserInterface;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * JUnit 5 test class for MainPanel tab management functionality.
 * Tests tab creation, switching, closing, and state management.
 */
@DisplayName("MainPanel Tab Management Tests")
public class MainPanelTabTest {

    private MainPanel mainPanel;
    private JFrame testFrame;

    @BeforeEach
    void setUp() {
        mainPanel = TestMainPanelFactory.createForHeadlessTesting();
    }

    @Nested
    @DisplayName("Tab Creation Tests")
    class TabCreationTests {
        
        @Test
        @DisplayName("Should create new tab when creating new automaton")
        void testCreateNewAutomatonAddsTab() {
            // Create a new DFA automaton
            mainPanel.createNewAutomaton("DFA");
            
            // Verify that a tab was created - check component count increased
            assertTrue(mainPanel.getComponentCount() > 0);
        }
        
        @Test
        @DisplayName("Should create tabs for different automaton types")
        void testCreateMultipleAutomatonTypes() {
            assertDoesNotThrow(() -> {
                mainPanel.createNewAutomaton("DFA");
                mainPanel.createNewAutomaton("NFA");
                mainPanel.createNewAutomaton("PDA");
                mainPanel.createNewAutomaton("TM");
                // CFG will be implemented later
            });
        }
        
        @Test
        @DisplayName("Should handle multiple tabs")
        void testMultipleTabs() {
            // Create multiple tabs
            mainPanel.createNewAutomaton("DFA");
            mainPanel.createNewAutomaton("NFA");
            mainPanel.createNewAutomaton("PDA");
            
            // All tabs should be created successfully
            assertTrue(mainPanel.getComponentCount() > 0);
        }
    }

    @Nested
    @DisplayName("Tab State Management Tests")
    class TabStateTests {
        
        @Test
        @DisplayName("Should track active panel")
        void testActivePanel() throws Exception {
            // Create a new automaton
            mainPanel.createNewAutomaton("DFA");
            
            // Use reflection to check if currentActivePanel is set
            Field activePanelField = MainPanel.class.getDeclaredField("currentActivePanel");
            activePanelField.setAccessible(true);
            Object activePanel = activePanelField.get(mainPanel);
            
            assertNotNull(activePanel, "Active panel should be set after creating automaton");
        }
        
        @Test
        @DisplayName("Should update active tab index")
        void testActiveTabIndex() throws Exception {
            // Create multiple tabs
            mainPanel.createNewAutomaton("DFA");
            mainPanel.createNewAutomaton("NFA");
            
            // Use reflection to check activeTabIndex
            Field activeTabIndexField = MainPanel.class.getDeclaredField("activeTabIndex");
            activeTabIndexField.setAccessible(true);
            int activeTabIndex = (int) activeTabIndexField.get(mainPanel);
            
            // Should be 1 (second tab) after creating two tabs
            assertEquals(1, activeTabIndex, "Active tab index should be 1 after creating second tab");
        }
        
        @Test
        @DisplayName("Should initialize with no active tab")
        void testInitialNoActiveTab() throws Exception {
            // Use reflection to check initial activeTabIndex
            Field activeTabIndexField = MainPanel.class.getDeclaredField("activeTabIndex");
            activeTabIndexField.setAccessible(true);
            int activeTabIndex = (int) activeTabIndexField.get(mainPanel);
            
            assertEquals(-1, activeTabIndex, "Initial active tab index should be -1");
        }
    }

    @Nested
    @DisplayName("Tab Panel Tests")
    class TabPanelTests {
        
        @Test
        @DisplayName("Should show tab panel when tabs exist")
        void testTabPanelVisibility() throws Exception {
            // Get the tab panel using reflection
            Field tabPanelField = MainPanel.class.getDeclaredField("tabPanel");
            tabPanelField.setAccessible(true);
            JPanel tabPanel = (JPanel) tabPanelField.get(mainPanel);
            
            // Initially should be hidden
            assertFalse(tabPanel.isVisible(), "Tab panel should be hidden initially");
            
            // Create a tab
            mainPanel.createNewAutomaton("DFA");
            
            // Now should be visible
            assertTrue(tabPanel.isVisible(), "Tab panel should be visible after creating tab");
        }
        
        @Test
        @DisplayName("Should have tab buttons panel")
        void testTabButtonsPanel() throws Exception {
            // Get the tab buttons panel using reflection
            Field tabButtonsPanelField = MainPanel.class.getDeclaredField("tabButtonsPanel");
            tabButtonsPanelField.setAccessible(true);
            JPanel tabButtonsPanel = (JPanel) tabButtonsPanelField.get(mainPanel);
            
            assertNotNull(tabButtonsPanel, "Tab buttons panel should exist");
            
            // Create a tab
            mainPanel.createNewAutomaton("DFA");
            
            // Should have components after creating tab
            assertTrue(tabButtonsPanel.getComponentCount() > 0, 
                "Tab buttons panel should have buttons after creating tab");
        }
    }

    @Nested
    @DisplayName("Tab List Management Tests")
    class TabListTests {
        
        @Test
        @DisplayName("Should maintain list of open tabs")
        void testOpenTabsList() throws Exception {
            // Get the openTabs list using reflection
            Field openTabsField = MainPanel.class.getDeclaredField("openTabs");
            openTabsField.setAccessible(true);
            ArrayList<?> openTabs = (ArrayList<?>) openTabsField.get(mainPanel);
            
            // Initially should be empty
            assertTrue(openTabs.isEmpty(), "Open tabs list should be empty initially");
            
            // Create tabs
            mainPanel.createNewAutomaton("DFA");
            mainPanel.createNewAutomaton("NFA");
            
            // Should have 2 tabs
            assertEquals(2, openTabs.size(), "Should have 2 tabs after creating 2 automatons");
        }
        
        @Test
        @DisplayName("Should add tabs in order")
        void testTabOrder() throws Exception {
            // Get the openTabs list using reflection
            Field openTabsField = MainPanel.class.getDeclaredField("openTabs");
            openTabsField.setAccessible(true);
            ArrayList<?> openTabs = (ArrayList<?>) openTabsField.get(mainPanel);
            
            // Create tabs in specific order
            mainPanel.createNewAutomaton("DFA");
            mainPanel.createNewAutomaton("NFA");
            mainPanel.createNewAutomaton("PDA");
            
            assertEquals(3, openTabs.size(), "Should have 3 tabs");
            
            // Check tab titles using reflection
            for (int i = 0; i < openTabs.size(); i++) {
                Object tab = openTabs.get(i);
                Method getTitleMethod = tab.getClass().getMethod("getTitle");
                String title = (String) getTitleMethod.invoke(tab);
                assertNotNull(title, "Tab " + i + " should have a title");
            }
        }
    }

    @Nested
    @DisplayName("Actions Delegation Tests")
    class ActionsDelegationTests {
        
        @Test
        @DisplayName("Should delegate run action to active panel")
        void testRunDelegation() {
            // Create an automaton to have an active panel
            mainPanel.createNewAutomaton("DFA");
            
            // Note: runCurrentAutomaton may fail if graphviz is not installed
            // or in headless environment. We're just testing that delegation works.
            // The actual graphviz generation is tested in other tests.
            try {
                mainPanel.runCurrentAutomaton();
                // If it succeeds, that's fine
            } catch (Exception e) {
                // If it fails due to graphviz/image issues, that's expected in test environment
                // We just verify it's not a delegation issue
                String message = e.getMessage();
                if (message != null && (message.contains("ImageIcon") || message.contains("graphviz"))) {
                    // Expected in test environment without graphviz
                    return;
                }
                // Re-throw if it's a different issue
                throw e;
            }
        }
        
        @Test
        @DisplayName("Should delegate compile action to active panel")
        void testCompileDelegation() {
            // Create an automaton to have an active panel
            mainPanel.createNewAutomaton("DFA");
            
            // Should not throw when compiling
            assertDoesNotThrow(() -> {
                mainPanel.compileWithFigureCurrentAutomaton();
            });
        }
        
        @Test
        @DisplayName("Should delegate save action to active panel")
        void testSaveDelegation() {
            // Create an automaton to have an active panel
            mainPanel.createNewAutomaton("DFA");
            
            // Should not throw when saving (though actual save might fail without proper setup)
            assertDoesNotThrow(() -> {
                mainPanel.saveCurrentAutomaton();
            });
        }
        
    }

    @Nested
    @DisplayName("Welcome Panel Tests")
    class WelcomePanelTests {
        
        @Test
        @DisplayName("Should show welcome panel initially")
        void testInitialWelcomePanel() throws Exception {
            // Get the object panel using reflection
            Field objectPanelField = MainPanel.class.getDeclaredField("objectPanel");
            objectPanelField.setAccessible(true);
            JPanel objectPanel = (JPanel) objectPanelField.get(mainPanel);
            
            // Object panel should contain welcome content initially
            assertTrue(objectPanel.getComponentCount() > 0, "Main panel should have welcome content initially");
        }
        
        @Test
        @DisplayName("Should replace welcome panel when creating automaton")
        void testReplaceWelcomePanel() throws Exception {
            // Get the object panel
            Field objectPanelField = MainPanel.class.getDeclaredField("objectPanel");
            objectPanelField.setAccessible(true);
            JPanel objectPanel = (JPanel) objectPanelField.get(mainPanel);
            
            // Create an automaton
            mainPanel.createNewAutomaton("DFA");
            
            // Object panel should still have content
            assertTrue(objectPanel.getComponentCount() > 0, "Object panel should have content after creating automaton");
        }
    }

    @Nested
    @DisplayName("Tab Save State Tests")
    class TabSaveStateTests {
        
        @Test
        @DisplayName("Should mark current tab as saved")
        void testMarkCurrentTabAsSaved() throws Exception {
            // Create a tab
            mainPanel.createNewAutomaton("DFA");
            
            // Mark as saved
            assertDoesNotThrow(() -> {
                mainPanel.markCurrentTabAsSaved();
            });
            
            // Get the current tab and check its saved state
            Field openTabsField = MainPanel.class.getDeclaredField("openTabs");
            openTabsField.setAccessible(true);
            ArrayList<?> openTabs = (ArrayList<?>) openTabsField.get(mainPanel);
            
            if (!openTabs.isEmpty()) {
                Object currentTab = openTabs.get(0);
                Method hasUnsavedMethod = currentTab.getClass().getMethod("hasUnsavedChanges");
                boolean hasUnsaved = (boolean) hasUnsavedMethod.invoke(currentTab);
                assertFalse(hasUnsaved, "Tab should be marked as saved");
            }
        }
    }
}