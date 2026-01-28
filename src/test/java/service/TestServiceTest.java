package service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import ContextFreeGrammar.CFG;
import DeterministicFiniteAutomaton.DFA;
import PushDownAutomaton.PDA;
import common.TestFileParser;

/**
 * Unit tests for TestService.
 */
@DisplayName("TestService Tests")
class TestServiceTest {

    private TestService service;
    private AutomatonService automatonService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        service = new TestService();
        automatonService = new AutomatonService();
    }

    // ═══════════════════════════════════════════════════════════════════
    // findTestFile Tests
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("findTestFile returns test file when it exists")
    void findTestFile_existingTestFile() throws Exception {
        // Create automaton file and test file
        File automatonFile = tempDir.resolve("test.dfa").toFile();
        File testFile = tempDir.resolve("test.test").toFile();

        writeFile(automatonFile, "content");
        writeFile(testFile, "test content");

        File result = service.findTestFile(automatonFile);

        assertNotNull(result);
        assertEquals("test.test", result.getName());
    }

    @Test
    @DisplayName("findTestFile returns null when test file doesn't exist")
    void findTestFile_noTestFile() throws Exception {
        File automatonFile = tempDir.resolve("test.dfa").toFile();
        writeFile(automatonFile, "content");

        File result = service.findTestFile(automatonFile);

        assertNull(result);
    }

    @Test
    @DisplayName("findTestFile returns null for null input")
    void findTestFile_nullInput() {
        assertNull(service.findTestFile(null));
    }

    @Test
    @DisplayName("findTestFile returns null for non-existent file")
    void findTestFile_nonExistentFile() {
        File nonExistent = new File("/nonexistent/path/file.dfa");
        assertNull(service.findTestFile(nonExistent));
    }

    // ═══════════════════════════════════════════════════════════════════
    // hasTestFile Tests
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("hasTestFile returns true when test file exists")
    void hasTestFile_exists() throws Exception {
        File automatonFile = tempDir.resolve("test.dfa").toFile();
        File testFile = tempDir.resolve("test.test").toFile();

        writeFile(automatonFile, "content");
        writeFile(testFile, "test content");

        assertTrue(service.hasTestFile(automatonFile));
    }

    @Test
    @DisplayName("hasTestFile returns false when test file doesn't exist")
    void hasTestFile_notExists() throws Exception {
        File automatonFile = tempDir.resolve("test.dfa").toFile();
        writeFile(automatonFile, "content");

        assertFalse(service.hasTestFile(automatonFile));
    }

    // ═══════════════════════════════════════════════════════════════════
    // runTests Parameter Validation Tests
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("runTests throws for null automaton")
    void runTests_nullAutomaton() {
        assertThrows(IllegalArgumentException.class,
            () -> service.runTests(null, "path/to/test"));
    }

    @Test
    @DisplayName("runTests throws for null test file path")
    void runTests_nullPath() {
        DFA dfa = new DFA();
        assertThrows(IllegalArgumentException.class,
            () -> service.runTests(dfa, null));
    }

    @Test
    @DisplayName("runTests throws for empty test file path")
    void runTests_emptyPath() {
        DFA dfa = new DFA();
        assertThrows(IllegalArgumentException.class,
            () -> service.runTests(dfa, ""));
    }

    // ═══════════════════════════════════════════════════════════════════
    // validateCFGRulesLimit Tests
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("validateCFGRulesLimit returns null for non-CFG")
    void validateCFGRulesLimit_nonCFG() {
        DFA dfa = new DFA();
        assertNull(service.validateCFGRulesLimit(dfa, 10));
    }

    @Test
    @DisplayName("validateCFGRulesLimit returns null when maxRules is null")
    void validateCFGRulesLimit_nullMaxRules() {
        CFG cfg = new CFG();
        assertNull(service.validateCFGRulesLimit(cfg, null));
    }

    // ═══════════════════════════════════════════════════════════════════
    // validatePDATransitionsLimit Tests
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("validatePDATransitionsLimit returns null for non-PDA")
    void validatePDATransitionsLimit_nonPDA() {
        DFA dfa = new DFA();
        assertNull(service.validatePDATransitionsLimit(dfa, 10));
    }

    @Test
    @DisplayName("validatePDATransitionsLimit returns null when maxTransitions is null")
    void validatePDATransitionsLimit_nullMaxTransitions() {
        PDA pda = new PDA();
        assertNull(service.validatePDATransitionsLimit(pda, null));
    }

    // ═══════════════════════════════════════════════════════════════════
    // validateLimits Tests
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("validateLimits returns null for DFA")
    void validateLimits_DFA() {
        DFA dfa = new DFA();
        assertNull(service.validateLimits(dfa, 10, 10));
    }

    // ═══════════════════════════════════════════════════════════════════
    // formatTestResults Tests
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("formatTestResults returns message for null result")
    void formatTestResults_null() {
        String result = service.formatTestResults(null);
        assertEquals("No test results available.", result);
    }

    // ═══════════════════════════════════════════════════════════════════
    // getTestSummary Tests
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getTestSummary returns message for null result")
    void getTestSummary_null() {
        String result = service.getTestSummary(null);
        assertEquals("No results", result);
    }

    // ═══════════════════════════════════════════════════════════════════
    // parseTestFile Tests
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("parseTestFile returns null for invalid path")
    void parseTestFile_invalidPath() {
        TestFileParser.TestFileResult result = service.parseTestFile("/nonexistent/path");
        assertNull(result);
    }

    // ═══════════════════════════════════════════════════════════════════
    // formatLimitViolation Tests
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("formatLimitViolation returns null for DFA")
    void formatLimitViolation_DFA() {
        DFA dfa = new DFA();
        assertNull(service.formatLimitViolation(dfa, 10, 10, 100));
    }

    @Test
    @DisplayName("formatLimitViolation returns null for CFG under limit")
    void formatLimitViolation_CFGUnderLimit() {
        CFG cfg = new CFG();
        // Empty CFG has no productions, should not violate any limit
        assertNull(service.formatLimitViolation(cfg, 100, null, 100));
    }

    @Test
    @DisplayName("formatLimitViolation returns null when maxTransitions is null for PDA")
    void formatLimitViolation_PDANullMaxTransitions() {
        PDA pda = new PDA();
        // When maxTransitions is null, validation should return null
        assertNull(service.formatLimitViolation(pda, null, null, 100));
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
