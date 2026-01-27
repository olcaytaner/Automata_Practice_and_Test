package UserInterface;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ContextFreeGrammar.CFG;
import DeterministicFiniteAutomaton.DFA;
import NondeterministicFiniteAutomaton.NFA;
import PushDownAutomaton.PDA;
import TuringMachine.TM;
import common.Automaton;
import common.ExecutionResult;
import common.MachineType;
import common.ParseResult;
import common.ValidationMessage;

/**
 * JUnit 5 test class for FileManager functionality.
 * Tests file operations, extension mapping, and panel creation.
 */
@DisplayName("FileManager Tests")
public class FileManagerTest {

    private MainPanel.FileManager fileManager;
    private MainPanel mockMainPanel;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        // Create a headless MainPanel that won't show dialogs (no UI needed for this test)
        mockMainPanel = TestMainPanelFactory.createForHeadlessTesting();
        fileManager = mockMainPanel.fileManager;
    }

    @Nested
    @DisplayName("File Extension Tests")
    class FileExtensionTests {
        
        @Test
        @DisplayName("NFA should return .nfa extension")
        void testNFAExtension() {
            NFA nfa = new NFA();
            assertEquals(".nfa", fileManager.getExtensionForAutomaton(nfa),
                "NFA should return .nfa extension");
        }
        
        @Test
        @DisplayName("DFA should return .dfa extension")
        void testDFAExtension() {
            DFA dfa = new DFA();
            assertEquals(".dfa", fileManager.getExtensionForAutomaton(dfa),
                "DFA should return .dfa extension");
        }
        
        @Test
        @DisplayName("PDA should return .pda extension")
        void testPDAExtension() {
            PDA pda = new PDA();
            assertEquals(".pda", fileManager.getExtensionForAutomaton(pda),
                "PDA should return .pda extension");
        }
        
        @Test
        @DisplayName("TM should return .tm extension")
        void testTMExtension() {
            TM tm = new TM();
            assertEquals(".tm", fileManager.getExtensionForAutomaton(tm),
                "TM should return .tm extension");
        }
        
        @Test
        @DisplayName("CFG should return .cfg extension")
        void testCFGExtension() {
            CFG cfg = new CFG();
            assertEquals(".cfg", fileManager.getExtensionForAutomaton(cfg),
                "CFG should return .cfg extension");
        }
        
        @Test
        @DisplayName("Unknown automaton should return .txt extension")
        void testUnknownExtension() {
            // Create a mock automaton that doesn't match any known types
            Automaton unknown = new Automaton(MachineType.DFA) {
                @Override
                public ParseResult parse(String inputText) { return null; }
                @Override
                public ExecutionResult execute(String inputText) { return null; }
                @Override
                public java.util.List<ValidationMessage> validate() { return null; }
                @Override
                public String toDotCode(String inputText) { return null; }
            };
            
            assertEquals(".txt", fileManager.getExtensionForAutomaton(unknown),
                "Unknown automaton should return .txt extension");
        }
    }

    @Nested
    @DisplayName("Color Mapping Tests")
    class ColorMappingTests {
        
        @Test
        @DisplayName("NFA and PDA extensions should have green color")
        void testGreenExtensions() {
            java.awt.Color expectedColor = new java.awt.Color(230, 250, 230);
            
            assertEquals(expectedColor, fileManager.getColorForExtension(".nfa"),
                ".nfa extension should have green color");
            assertEquals(expectedColor, fileManager.getColorForExtension(".pda"),
                ".pda extension should have green color");
        }
        
        @Test
        @DisplayName("DFA and TM extensions should have orange color")
        void testOrangeExtensions() {
            java.awt.Color expectedColor = new java.awt.Color(250, 240, 230);
            
            assertEquals(expectedColor, fileManager.getColorForExtension(".dfa"),
                ".dfa extension should have orange color");
            assertEquals(expectedColor, fileManager.getColorForExtension(".tm"),
                ".tm extension should have orange color");
        }
        
        @Test
        @DisplayName("CFG and REX extensions should have purple color")
        void testPurpleExtensions() {
            java.awt.Color expectedColor = new java.awt.Color(240, 230, 250);
            
            assertEquals(expectedColor, fileManager.getColorForExtension(".cfg"),
                ".cfg extension should have purple color");
            assertEquals(expectedColor, fileManager.getColorForExtension(".rex"),
                ".rex extension should have purple color");
        }
        
        @Test
        @DisplayName("Unknown extension should have light gray color")
        void testDefaultColor() {
            assertEquals(java.awt.Color.LIGHT_GRAY, fileManager.getColorForExtension(".unknown"),
                "Unknown extension should have light gray color");
        }
        
        @Test
        @DisplayName("Case insensitive color mapping should work")
        void testCaseInsensitive() {
            java.awt.Color expectedColor = new java.awt.Color(230, 250, 230);
            
            assertEquals(expectedColor, fileManager.getColorForExtension(".NFA"),
                "Uppercase .NFA should work");
            assertEquals(expectedColor, fileManager.getColorForExtension(".Nfa"),
                "Mixed case .Nfa should work");
        }
    }

    @Nested
    @DisplayName("File Creation Tests")
    class FileCreationTests {
        
        @Test
        @DisplayName("Creating NFA panel from .nfa file should succeed")
        void testCreateNFAPanel() throws IOException {
            // Create a temporary NFA file
            File nfaFile = tempDir.resolve("test.nfa").toFile();
            String nfaContent = "states: q0 q1\n" +
                               "alphabet: a b\n" +
                               "start: q0\n" +
                               "finals: q1\n" +
                               "transitions:\n" +
                               "q0 -> q1 (a)\n";
            
            Files.write(nfaFile.toPath(), nfaContent.getBytes());
            
            assertDoesNotThrow(() -> {
                javax.swing.JPanel panel = fileManager.createPanelForFile(nfaFile);
                assertNotNull(panel, "Created panel should not be null");
                assertTrue(panel instanceof NFAPanel, "Panel should be NFAPanel instance");
            }, "Creating NFA panel should not throw exception");
        }
        
        @Test
        @DisplayName("Creating DFA panel from .dfa file should succeed")
        void testCreateDFAPanel() throws IOException {
            // Create a temporary DFA file
            File dfaFile = tempDir.resolve("test.dfa").toFile();
            String dfaContent = "Start: q0\n" +
                               "Finals: q1\n" +
                               "Alphabet: a b\n" +
                               "States: q0 q1\n" +
                               "Transitions:\n" +
                               "q0 -> q1 (a)\n";
            
            Files.write(dfaFile.toPath(), dfaContent.getBytes());
            
            assertDoesNotThrow(() -> {
                javax.swing.JPanel panel = fileManager.createPanelForFile(dfaFile);
                assertNotNull(panel, "Created panel should not be null");
                assertTrue(panel instanceof DFAPanel, "Panel should be DFAPanel instance");
            }, "Creating DFA panel should not throw exception");
        }
        
        @Test
        @DisplayName("Creating REX panel from .rex file should succeed")
        void testCreateREXPanel() throws IOException {
            // Create a temporary REX file with correct format
            File rexFile = tempDir.resolve("test.rex").toFile();
            String rexContent = "(a|b)*abb\n" +
                               "a b\n";  // Use correct space-separated format
            
            Files.write(rexFile.toPath(), rexContent.getBytes());
            
            assertDoesNotThrow(() -> {
                javax.swing.JPanel panel = fileManager.createPanelForFile(rexFile);
                assertNotNull(panel, "Created panel should not be null");
                assertTrue(panel instanceof REXPanel, "Panel should be REXPanel instance");
            }, "Creating REX panel should not throw exception");
        }
        
        
        @Test
        @DisplayName("Creating panel from unsupported file should throw exception")
        void testUnsupportedFileType() throws IOException {
            // Create a temporary file with unsupported extension
            File unsupportedFile = tempDir.resolve("test.xyz").toFile();
            Files.write(unsupportedFile.toPath(), "some content".getBytes());
            
            assertThrows(IllegalArgumentException.class, () -> {
                fileManager.createPanelForFile(unsupportedFile);
            }, "Creating panel from unsupported file should throw IllegalArgumentException");
        }
        
        @Test
        @DisplayName("Creating panel from non-existent file should throw exception")
        void testNonExistentFile() {
            File nonExistentFile = tempDir.resolve("nonexistent.nfa").toFile();
            
            assertThrows(Exception.class, () -> {
                fileManager.createPanelForFile(nonExistentFile);
            }, "Creating panel from non-existent file should throw exception");
        }
    }

    @Nested
    @DisplayName("Recent Files Management Tests")
    class RecentFilesTests {
        
        @Test
        @DisplayName("Adding file to recent files should work")
        void testAddToRecentFiles() throws IOException {
            File testFile = tempDir.resolve("test.nfa").toFile();
            Files.write(testFile.toPath(), "test content".getBytes());
            
            // The addToRecentFiles method uses PreferencesManager internally
            // We can only verify it doesn't throw an exception
            assertDoesNotThrow(() -> {
                fileManager.addToRecentFiles(testFile);
            }, "Adding file to recent files should not throw exception");
        }
        
        @Test
        @DisplayName("Adding duplicate file should not throw exception")
        void testAddDuplicateFile() throws IOException {
            File testFile = tempDir.resolve("test.nfa").toFile();
            Files.write(testFile.toPath(), "test content".getBytes());
            
            // Add file twice to test duplicate handling
            assertDoesNotThrow(() -> {
                fileManager.addToRecentFiles(testFile);
                fileManager.addToRecentFiles(testFile); // Add same file again
            }, "Adding duplicate file should not throw exception");
        }
    }
}
