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
import org.junit.jupiter.api.Nested;
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
import viewmodel.TestResultViewModel;

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
        "states: q0 q1\n" +
        "alphabet: 0 1\n" +
        "start: q0\n" +
        "accept: q1\n" +
        "\n" +
        "transitions:\n" +
        "q0, 0 -> q1\n" +
        "q0, 1 -> q0\n" +
        "q1, 0 -> q1\n" +
        "q1, 1 -> q0";

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
    // ViewModel Test Execution Tests
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("ViewModel Test Execution")
    class ViewModelTestExecutionTests {

        // Valid CFG definition with 8 rules (S -> aB | bA, A -> a | aS | bAA, B -> b | bS | aBB)
        private static final String CFG_WITH_MANY_RULES =
            "vars: S A B\n" +
            "terminals: a b\n" +
            "start: S\n" +
            "\n" +
            "rules:\n" +
            "S -> a B | b A\n" +
            "A -> a | a S | b A A\n" +
            "B -> b | b S | a B B\n";

        // Valid PDA with 5 transitions
        private static final String PDA_WITH_TRANSITIONS =
            "states: q0 q1 q2 q3\n" +
            "input: a b\n" +
            "stack: a Z\n" +
            "start: q0\n" +
            "stack_start: Z\n" +
            "accept: q3\n" +
            "transitions:\n" +
            "q0, a, Z -> q1, aZ\n" +
            "q0, a, a -> q1, aa\n" +
            "q1, b, a -> q2, eps\n" +
            "q2, b, a -> q2, eps\n" +
            "q2, eps, Z -> q3, eps\n";

        @Test
        @DisplayName("runTestsWithViewModel returns success for DFA")
        void testRunTestsWithViewModel_DFASuccess() throws Exception {
            Automaton dfa = controller.createAutomaton(MachineType.DFA);
            controller.parse(dfa, VALID_DFA);

            // Create test file with CSV format: input,expected (1=accept, 0=reject)
            File testFile = tempDir.resolve("test.test").toFile();
            writeFile(testFile, "0,1\n1,0\n");

            TestResultViewModel result = controller.runTestsWithViewModel(dfa, testFile.getAbsolutePath(), null);

            assertNotNull(result);
            assertFalse(result.hasLimitViolation());
            assertEquals(2, result.getTotalTests());
        }

        @Test
        @DisplayName("runTestsWithViewModel returns CFG violation when rules exceed limit")
        void testRunTestsWithViewModel_CFGViolation() throws Exception {
            Automaton cfg = controller.createAutomaton(MachineType.CFG);
            controller.parse(cfg, CFG_WITH_MANY_RULES);

            // Create test file
            File testFile = tempDir.resolve("test.test").toFile();
            writeFile(testFile, "ab,1\n");

            // Set settings with max 3 rules (CFG has 8)
            controller.setTestSettings(new SessionService.TestSettings(0, 100, 30, 3, null, null));

            TestResultViewModel result = controller.runTestsWithViewModel(cfg, testFile.getAbsolutePath(), null);

            assertNotNull(result);
            assertTrue(result.hasLimitViolation());
            assertEquals("CFG_RULES", result.getLimitViolationType());
            assertEquals(0.0, result.getEarnedPoints());
        }

        @Test
        @DisplayName("runTestsWithViewModel returns PDA violation when transitions exceed limit")
        void testRunTestsWithViewModel_PDAViolation() throws Exception {
            Automaton pda = controller.createAutomaton(MachineType.PDA);
            controller.parse(pda, PDA_WITH_TRANSITIONS);

            // Create test file
            File testFile = tempDir.resolve("test.test").toFile();
            writeFile(testFile, "ab,1\n");

            // Set settings with max 2 transitions (PDA has 5)
            controller.setTestSettings(new SessionService.TestSettings(0, 100, 30, null, 2, null));

            TestResultViewModel result = controller.runTestsWithViewModel(pda, testFile.getAbsolutePath(), null);

            assertNotNull(result);
            assertTrue(result.hasLimitViolation());
            assertEquals("PDA_TRANSITIONS", result.getLimitViolationType());
            assertEquals(0.0, result.getEarnedPoints());
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // Limit Validation Tests
    // ═══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Limit Validation")
    class LimitValidationTests {

        @Test
        @DisplayName("validateLimits returns CFG violation when rules exceed limit")
        void testValidateLimits_CFGViolation() {
            String cfgInput =
                "vars: S A B\n" +
                "terminals: a b\n" +
                "start: S\n" +
                "\n" +
                "rules:\n" +
                "S -> a B | b A\n" +
                "A -> a | a S | b A A\n" +
                "B -> b | b S | a B B\n";

            Automaton cfg = controller.createAutomaton(MachineType.CFG);
            controller.parse(cfg, cfgInput);

            // Set settings with max 3 rules (CFG has 8)
            controller.setTestSettings(new SessionService.TestSettings(0, 100, 30, 3, null, null));

            ValidationMessage violation = controller.validateLimits(cfg);

            assertNotNull(violation);
            assertTrue(violation.getMessage().contains("CFG"));
        }

        @Test
        @DisplayName("validateLimits returns PDA violation when transitions exceed limit")
        void testValidateLimits_PDAViolation() {
            String pdaInput =
                "states: q0 q1 q2 q3\n" +
                "input: a b\n" +
                "stack: a Z\n" +
                "start: q0\n" +
                "stack_start: Z\n" +
                "accept: q3\n" +
                "transitions:\n" +
                "q0, a, Z -> q1, aZ\n" +
                "q0, a, a -> q1, aa\n" +
                "q1, b, a -> q2, eps\n" +
                "q2, b, a -> q2, eps\n" +
                "q2, eps, Z -> q3, eps\n";

            Automaton pda = controller.createAutomaton(MachineType.PDA);
            controller.parse(pda, pdaInput);

            // Set settings with max 2 transitions (PDA has 5)
            controller.setTestSettings(new SessionService.TestSettings(0, 100, 30, null, 2, null));

            ValidationMessage violation = controller.validateLimits(pda);

            assertNotNull(violation);
            assertTrue(violation.getMessage().contains("PDA"));
        }

        @Test
        @DisplayName("validateLimits returns null when within limits")
        void testValidateLimits_NoViolation() {
            Automaton dfa = controller.createAutomaton(MachineType.DFA);
            controller.parse(dfa, VALID_DFA);

            // Set permissive settings
            controller.setTestSettings(new SessionService.TestSettings(0, 100, 30, 100, 100, null));

            ValidationMessage violation = controller.validateLimits(dfa);

            assertNull(violation);
        }
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
