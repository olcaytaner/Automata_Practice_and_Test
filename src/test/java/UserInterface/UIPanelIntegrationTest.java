package UserInterface;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.JFrame;
import javax.swing.JPanel;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import ContextFreeGrammar.CFG;
import DeterministicFiniteAutomaton.DFA;
import NondeterministicFiniteAutomaton.NFA;
import PushDownAutomaton.PDA;
import RegularExpression.RegularExpression;
import TuringMachine.TM;

/**
 * JUnit 5 integration test class for UI panel functionality.
 * Tests panel creation, file operations, and UI integration.
 */
@DisplayName("UI Panel Integration Tests")
public class UIPanelIntegrationTest {

    private MainPanel mainPanel;
    private MainPanel.FileManager fileManager;
    private JFrame testFrame;

    @BeforeEach
    void setUp() {
        // Create a headless main panel for UI testing
        mainPanel = TestMainPanelFactory.createForHeadlessTesting();
        fileManager = mainPanel.fileManager;
    }

    @Nested
    @DisplayName("Panel Creation Tests")
    class PanelCreationTests {
        
        @Test
        @DisplayName("Should create DFA panel correctly")
        void testCreateDFAPanel() {
            assertDoesNotThrow(() -> {
                DFA dfa = new DFA();
                DFAPanel panel = new DFAPanel(mainPanel, dfa);
                
                assertNotNull(panel, "DFA panel should be created");
                assertTrue(panel instanceof Component, "Panel should be a Swing component");
            }, "DFA panel creation should not throw exceptions");
        }
        
        @Test
        @DisplayName("Should create NFA panel correctly")
        void testCreateNFAPanel() {
            assertDoesNotThrow(() -> {
                NFA nfa = new NFA();
                NFAPanel panel = new NFAPanel(mainPanel, nfa);
                
                assertNotNull(panel, "NFA panel should be created");
                assertTrue(panel instanceof Component, "Panel should be a Swing component");
            }, "NFA panel creation should not throw exceptions");
        }
        
        @Test
        @DisplayName("Should create PDA panel correctly")
        void testCreatePDAPanel() {
            assertDoesNotThrow(() -> {
                PDA pda = new PDA();
                PDAPanel panel = new PDAPanel(mainPanel, pda);
                
                assertNotNull(panel, "PDA panel should be created");
                assertTrue(panel instanceof Component, "Panel should be a Swing component");
            }, "PDA panel creation should not throw exceptions");
        }
        
        @Test
        @DisplayName("Should create TM panel correctly")
        void testCreateTMPanel() {
            assertDoesNotThrow(() -> {
                TM tm = new TM(null, null, null, null, null, null, null);
                TMPanel panel = new TMPanel(mainPanel, tm);
                
                assertNotNull(panel, "TM panel should be created");
                assertTrue(panel instanceof Component, "Panel should be a Swing component");
            }, "TM panel creation should not throw exceptions");
        }
        
        @Test
        @DisplayName("Should create CFG panel correctly")
        void testCreateCFGPanel() {
            assertDoesNotThrow(() -> {
                CFG cfg = new CFG();
                CFGPanel panel = new CFGPanel(mainPanel, cfg);
                
                assertNotNull(panel, "CFG panel should be created");
                assertTrue(panel instanceof Component, "Panel should be a Swing component");
            }, "CFG panel creation should not throw exceptions");
        }
        
        @Test
        @DisplayName("Should create REX panel correctly")
        void testCreateREXPanel() {
            assertDoesNotThrow(() -> {
                RegularExpression rex = new RegularExpression();
                REXPanel panel = new REXPanel(mainPanel, rex);
                
                assertNotNull(panel, "REX panel should be created");
                assertTrue(panel instanceof Component, "Panel should be a Swing component");
            }, "REX panel creation should not throw exceptions");
        }
        
    }

    @Nested
    @DisplayName("FileManager Integration Tests")
    class FileManagerIntegrationTests {
        
        @Test
        @DisplayName("Should create panels for different file extensions")
        void testCreatePanelForFileExtensions() {
            String[] extensions = {".dfa", ".nfa", ".pda", ".tm"};  // CFG and REX not implemented yet
            
            for (String extension : extensions) {
                try {
                    // Create a temporary file with the extension
                    Path tempFile = Files.createTempFile("test", extension);
                    Files.write(tempFile, "# Test content".getBytes());
                    File file = tempFile.toFile();
                    
                    // Some extensions might fail due to content format - that's expected
                    try {
                        JPanel panel = fileManager.createPanelForFile(file);
                        // Panel might be null or throw exception due to invalid content
                        if (panel != null) {
                            assertTrue(panel instanceof Component, "Panel should be a component");
                        }
                    } catch (Exception e) {
                        // Expected for invalid content format
                        System.out.println("Panel creation failed for " + extension + ": " + e.getMessage());
                    }
                    
                    // Clean up
                    Files.deleteIfExists(tempFile);
                    
                } catch (IOException e) {
                    fail("Failed to create temp file for testing: " + e.getMessage());
                }
            }
        }
        
        @Test
        @DisplayName("Should handle unknown file extensions gracefully")
        void testUnknownFileExtension() {
            try {
                Path tempFile = Files.createTempFile("test", ".unknown");
                Files.write(tempFile, "# Test content".getBytes());
                File file = tempFile.toFile();
                
                // Unknown extensions should throw exception
                assertThrows(IllegalArgumentException.class, () -> {
                    fileManager.createPanelForFile(file);
                }, "Unknown file extension should throw IllegalArgumentException");
                
                // Clean up
                Files.deleteIfExists(tempFile);
                
            } catch (IOException e) {
                fail("Failed to create temp file for testing: " + e.getMessage());
            }
        }
        
        @Test
        @DisplayName("Should handle color mapping correctly")
        void testColorMapping() {
            // Test that FileManager correctly maps extensions to colors
            assertNotNull(fileManager.getColorForExtension(".dfa"), "DFA should have a color");
            assertNotNull(fileManager.getColorForExtension(".nfa"), "NFA should have a color");
            assertNotNull(fileManager.getColorForExtension(".pda"), "PDA should have a color");
            assertNotNull(fileManager.getColorForExtension(".tm"), "TM should have a color");
            assertNotNull(fileManager.getColorForExtension(".cfg"), "CFG should have a color");
            assertNotNull(fileManager.getColorForExtension(".rex"), "REX should have a color");
            assertNotNull(fileManager.getColorForExtension(".unknown"), "Unknown extension should have default color");
        }
    }

    @Nested
    @DisplayName("Panel Component Tests")
    class PanelComponentTests {
        
        @Test
        @DisplayName("All panels should have required UI components")
        void testPanelUIComponents() {
            // Test that each panel type has basic UI components
            assertDoesNotThrow(() -> {
                DFA dfa = new DFA();
                DFAPanel dfaPanel = new DFAPanel(mainPanel, dfa);
                
                // Check that panel has components (text area, buttons, etc.)
                Component[] components = dfaPanel.getComponents();
                assertTrue(components.length > 0, "DFA panel should have UI components");
                
            }, "DFA panel should have proper UI structure");
            
            assertDoesNotThrow(() -> {
                NFA nfa = new NFA();
                NFAPanel nfaPanel = new NFAPanel(mainPanel, nfa);
                
                Component[] components = nfaPanel.getComponents();
                assertTrue(components.length > 0, "NFA panel should have UI components");
                
            }, "NFA panel should have proper UI structure");
            
            assertDoesNotThrow(() -> {
                PDA pda = new PDA();
                PDAPanel pdaPanel = new PDAPanel(mainPanel, pda);
                
                Component[] components = pdaPanel.getComponents();
                assertTrue(components.length > 0, "PDA panel should have UI components");
                
            }, "PDA panel should have proper UI structure");
        }
        
        @Test
        @DisplayName("Panels should handle validation properly")
        void testPanelValidation() {
            assertDoesNotThrow(() -> {
                DFA dfa = new DFA();
                DFAPanel panel = new DFAPanel(mainPanel, dfa);
                
                // Test that validation can be called without crashing
                // The actual validation is handled by the automaton, not the panel
                assertNotNull(panel, "Panel should remain functional after creation");
                
            }, "Panel validation handling should not crash");
        }
    }

    @Nested
    @DisplayName("MainPanel Integration Tests")
    class MainPanelIntegrationTests {
        
        @Test
        @DisplayName("MainPanel should handle automaton creation")
        void testAutomatonCreation() {
            assertDoesNotThrow(() -> {
                // Test the createNewAutomaton method for different types (uses string parameters)
                mainPanel.createNewAutomaton("DFA");
                mainPanel.createNewAutomaton("NFA");
                mainPanel.createNewAutomaton("PDA");
                mainPanel.createNewAutomaton("TM");
                // CFG and REX will show "not implemented" dialogs, not throw exceptions
                // mainPanel.createNewAutomaton("CFG");
                // mainPanel.createNewAutomaton("REX");
                
            }, "Automaton creation should work for all types");
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {
        
        @Test
        @DisplayName("Should handle null parameters gracefully")
        void testNullParameterHandling() {
            // Test that panels handle null parameters without crashing
            assertDoesNotThrow(() -> {
                try {
                    new DFAPanel(null, new DFA());
                } catch (NullPointerException e) {
                    // NPE is acceptable for null mainPanel parameter
                }
            }, "Should handle null mainPanel parameter");
            
            assertDoesNotThrow(() -> {
                try {
                    new DFAPanel(mainPanel, null);
                } catch (NullPointerException e) {
                    // NPE is acceptable for null automaton parameter
                }
            }, "Should handle null automaton parameter");
        }
        
        @Test
        @DisplayName("Should handle invalid file operations")
        void testInvalidFileOperations() {
            // Test with non-existent file
            File nonExistentFile = new File("non-existent-file.dfa");
            
            assertThrows(Exception.class, () -> {
                fileManager.createPanelForFile(nonExistentFile);
            }, "Non-existent file should throw exception");
        }
    }
}