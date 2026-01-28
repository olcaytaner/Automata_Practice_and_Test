package controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import common.Automaton;
import common.ExecutionResult;
import common.MachineType;
import common.ParseResult;
import common.ValidationMessage;
import service.AutomatonService;
import service.FileService;
import service.SessionService;
import service.TestService;
import service.VisualizationService;

/**
 * Integration tests for AutomatonController.
 */
@DisplayName("AutomatonController Tests")
class AutomatonControllerTest {

    private AutomatonController controller;

    @TempDir
    Path tempDir;

    // Valid DFA definition
    private static final String VALID_DFA =
        "Start: q0\n" +
        "Finals: q1\n" +
        "Alphabet: 0 1\n" +
        "States: q0 q1\n" +
        "\n" +
        "Transitions:\n" +
        "q0 -> q1 (0)\n" +
        "q0 -> q0 (1)\n" +
        "q1 -> q1 (0)\n" +
        "q1 -> q0 (1)";

    @BeforeEach
    void setUp() {
        // Create controller with custom session service for testing
        File prefsFile = tempDir.resolve("test_prefs.properties").toFile();
        SessionService sessionService = new SessionService(prefsFile);

        controller = new AutomatonController(
            new AutomatonService(),
            new FileService(),
            new TestService(),
            new VisualizationService(),
            sessionService
        );
    }

    // ═══════════════════════════════════════════════════════════════════
    // Automaton Creation Tests
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("createAutomaton creates correct types")
    void createAutomaton_correctTypes() {
        for (MachineType type : MachineType.values()) {
            Automaton automaton = controller.createAutomaton(type);
            assertNotNull(automaton);
            assertEquals(type, automaton.getMachineType());
        }
    }

    @Test
    @DisplayName("getDefaultTemplate returns non-empty templates")
    void getDefaultTemplate_nonEmpty() {
        for (MachineType type : MachineType.values()) {
            String template = controller.getDefaultTemplate(type);
            assertNotNull(template);
            assertFalse(template.isEmpty());
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // Parse and Execute Tests
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("parse succeeds for valid DFA")
    void parse_validDFA() {
        Automaton dfa = controller.createAutomaton(MachineType.DFA);

        ParseResult result = controller.parse(dfa, VALID_DFA);

        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("parse fails for invalid input")
    void parse_invalidInput() {
        Automaton dfa = controller.createAutomaton(MachineType.DFA);

        ParseResult result = controller.parse(dfa, "invalid");

        assertFalse(result.isSuccess());
    }

    @Test
    @DisplayName("execute returns result for valid automaton")
    void execute_validAutomaton() {
        Automaton dfa = controller.createAutomaton(MachineType.DFA);
        controller.parse(dfa, VALID_DFA);

        ExecutionResult result = controller.execute(dfa, "0");

        assertNotNull(result);
        assertTrue(result.isAccepted());
    }

    @Test
    @DisplayName("validate returns messages list")
    void validate_returnsList() {
        Automaton dfa = controller.createAutomaton(MachineType.DFA);

        List<ValidationMessage> messages = controller.validate(dfa, VALID_DFA);

        assertNotNull(messages);
    }

    // ═══════════════════════════════════════════════════════════════════
    // Compile with Visualization Tests
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("compileWithVisualization succeeds for valid DFA")
    void compileWithVisualization_validDFA() {
        Automaton dfa = controller.createAutomaton(MachineType.DFA);

        AutomatonController.CompileResult result = controller.compileWithVisualization(dfa, VALID_DFA);

        assertTrue(result.isSuccess());
        assertNotNull(result.getValidationMessages());
        assertTrue(result.hasVisualization());
        assertNotNull(result.getSvgContent());
    }

    @Test
    @DisplayName("compileWithVisualization fails for invalid input")
    void compileWithVisualization_invalidInput() {
        Automaton dfa = controller.createAutomaton(MachineType.DFA);

        AutomatonController.CompileResult result = controller.compileWithVisualization(dfa, "invalid");

        assertFalse(result.isSuccess());
    }

    // ═══════════════════════════════════════════════════════════════════
    // File Operations Tests
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("loadFromFile succeeds for valid file")
    void loadFromFile_valid() throws Exception {
        File dfaFile = tempDir.resolve("test.dfa").toFile();
        writeFile(dfaFile, VALID_DFA);

        AutomatonController.LoadResult result = controller.loadFromFile(dfaFile);

        assertTrue(result.isSuccess());
        assertNotNull(result.getAutomaton());
        assertEquals(MachineType.DFA, result.getAutomaton().getMachineType());
        assertEquals(VALID_DFA, result.getContent());
        assertEquals(dfaFile, result.getFile());
    }

    @Test
    @DisplayName("loadFromFile fails for unsupported type")
    void loadFromFile_unsupportedType() throws Exception {
        File txtFile = tempDir.resolve("test.txt").toFile();
        writeFile(txtFile, "content");

        AutomatonController.LoadResult result = controller.loadFromFile(txtFile);

        assertFalse(result.isSuccess());
        assertNotNull(result.getErrorMessage());
    }

    @Test
    @DisplayName("loadFromFile adds to recent files")
    void loadFromFile_addsToRecent() throws Exception {
        File dfaFile = tempDir.resolve("test.dfa").toFile();
        writeFile(dfaFile, VALID_DFA);

        controller.loadFromFile(dfaFile);

        List<String> recent = controller.getRecentFiles();
        assertTrue(recent.contains(dfaFile.getAbsolutePath()));
    }

    @Test
    @DisplayName("saveToFile succeeds")
    void saveToFile_succeeds() {
        File file = tempDir.resolve("output.dfa").toFile();

        boolean result = controller.saveToFile(file, VALID_DFA);

        assertTrue(result);
        assertTrue(file.exists());
    }

    @Test
    @DisplayName("getMachineTypeForFile returns correct type")
    void getMachineTypeForFile_correctType() {
        File dfaFile = new File("test.dfa");
        File nfaFile = new File("test.nfa");
        File pdaFile = new File("test.pda");

        assertEquals(MachineType.DFA, controller.getMachineTypeForFile(dfaFile));
        assertEquals(MachineType.NFA, controller.getMachineTypeForFile(nfaFile));
        assertEquals(MachineType.PDA, controller.getMachineTypeForFile(pdaFile));
    }

    // ═══════════════════════════════════════════════════════════════════
    // Session Management Tests
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("recent files management works")
    void recentFilesManagement() throws Exception {
        File file = tempDir.resolve("test.dfa").toFile();
        writeFile(file, VALID_DFA);

        controller.loadFromFile(file);

        List<String> recent = controller.getRecentFiles();
        assertEquals(1, recent.size());

        controller.clearRecentFiles();
        assertTrue(controller.getRecentFiles().isEmpty());
    }

    @Test
    @DisplayName("last directory management works")
    void lastDirectoryManagement() {
        assertNull(controller.getLastDirectory());

        controller.setLastDirectory(tempDir.toFile());

        File lastDir = controller.getLastDirectory();
        assertNotNull(lastDir);
        assertEquals(tempDir.toFile().getAbsolutePath(), lastDir.getAbsolutePath());
    }

    @Test
    @DisplayName("saveOpenFiles and getLastOpenedFiles work")
    void openFilesManagement() throws Exception {
        File file1 = tempDir.resolve("file1.dfa").toFile();
        File file2 = tempDir.resolve("file2.nfa").toFile();
        file1.createNewFile();
        file2.createNewFile();

        controller.saveOpenFiles(Arrays.asList(file1, file2));

        List<String> opened = controller.getLastOpenedFiles();
        assertEquals(2, opened.size());
    }

    // ═══════════════════════════════════════════════════════════════════
    // Test Settings Tests
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getTestSettings returns settings")
    void getTestSettings_returnsSettings() {
        SessionService.TestSettings settings = controller.getTestSettings();

        assertNotNull(settings);
        assertEquals(SessionService.DEFAULT_MIN_POINTS, settings.getMinPoints());
        assertEquals(SessionService.DEFAULT_MAX_POINTS, settings.getMaxPoints());
    }

    @Test
    @DisplayName("setTestSettings updates settings")
    void setTestSettings_updates() {
        SessionService.TestSettings newSettings = new SessionService.TestSettings(
            10, 25, 30, 50, 100, null
        );

        controller.setTestSettings(newSettings);

        SessionService.TestSettings loaded = controller.getTestSettings();
        assertEquals(10, loaded.getMinPoints());
        assertEquals(25, loaded.getMaxPoints());
        assertEquals(30, loaded.getTimeoutSeconds());
    }

    // ═══════════════════════════════════════════════════════════════════
    // Visualization Tests
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("supportsVisualization returns correct values")
    void supportsVisualization_correctValues() {
        assertTrue(controller.supportsVisualization(MachineType.DFA));
        assertTrue(controller.supportsVisualization(MachineType.NFA));
        assertTrue(controller.supportsVisualization(MachineType.PDA));
        assertTrue(controller.supportsVisualization(MachineType.TM));
        assertTrue(controller.supportsVisualization(MachineType.CFG));
        assertFalse(controller.supportsVisualization(MachineType.REGEX));
    }

    @Test
    @DisplayName("generateDotCode returns DOT code")
    void generateDotCode_returnsDot() {
        Automaton dfa = controller.createAutomaton(MachineType.DFA);

        String dotCode = controller.generateDotCode(dfa, VALID_DFA);

        assertNotNull(dotCode);
        assertTrue(dotCode.contains("digraph"));
    }

    // ═══════════════════════════════════════════════════════════════════
    // Service Accessor Tests
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("service accessors return non-null services")
    void serviceAccessors_nonNull() {
        assertNotNull(controller.getAutomatonService());
        assertNotNull(controller.getFileService());
        assertNotNull(controller.getTestService());
        assertNotNull(controller.getVisualizationService());
        assertNotNull(controller.getSessionService());
    }

    // ═══════════════════════════════════════════════════════════════════
    // Default Constructor Test
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("default constructor creates working controller")
    void defaultConstructor_works() {
        AutomatonController defaultController = new AutomatonController();

        Automaton dfa = defaultController.createAutomaton(MachineType.DFA);
        assertNotNull(dfa);
    }

    // ═══════════════════════════════════════════════════════════════════
    // Helper Methods
    // ═══════════════════════════════════════════════════════════════════

    private void writeFile(File file, String content) throws Exception {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            writer.write(content);
        }
    }
}
