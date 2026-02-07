package service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import viewmodel.TestResultViewModel;

/**
 * Tests for TestService.runTestsWithValidation method.
 * Verifies that ViewModel-based test execution correctly handles
 * limit violations and successful test runs.
 *
 * Test file format is CSV: input,expected where expected is 1 (accept) or 0 (reject)
 */
@DisplayName("TestService Validation Tests")
class TestServiceValidationTest {

    private TestService testService;

    @TempDir
    File tempDir;

    @BeforeEach
    void setUp() {
        testService = new TestService();
    }

    @Nested
    @DisplayName("Successful Test Runs")
    class SuccessfulTests {

        @Test
        @DisplayName("should return success ViewModel for DFA tests")
        void testDfaSuccess() throws IOException {
            // Create DFA that accepts strings ending in 'a'
            String dfaInput =
                "states: q0 q1\n" +
                "alphabet: a b\n" +
                "start: q0\n" +
                "accept: q1\n" +
                "\n" +
                "transitions:\n" +
                "q0, a -> q1\n" +
                "q0, b -> q0\n" +
                "q1, a -> q1\n" +
                "q1, b -> q0\n";

            DFA dfa = new DFA();
            dfa.parse(dfaInput);

            // Create test file in CSV format: input,expected (1=accept, 0=reject)
            File testFile = createTestFile(
                "a,1\n" +
                "aa,1\n" +
                "b,0\n" +
                "bb,0\n"
            );

            // Create settings
            SessionService.TestSettings settings = new SessionService.TestSettings(
                0, 100, 30, null, null, null);

            // Run tests
            TestResultViewModel result = testService.runTestsWithValidation(
                dfa, testFile.getAbsolutePath(), settings, null);

            // Verify
            assertFalse(result.hasLimitViolation());
            assertEquals(4, result.getTotalTests());
            assertTrue(result.getPassedTests() > 0);
            assertNotNull(result.getDetailedReport());
        }

        @Test
        @DisplayName("should return success ViewModel for NFA tests")
        void testNfaSuccess() throws IOException {
            // Create NFA that accepts strings containing 'a'
            String nfaInput =
                "states: q0 q1\n" +
                "alphabet: a b\n" +
                "start: q0\n" +
                "accept: q1\n" +
                "transitions:\n" +
                "q0, a -> q0\n" +
                "q0, b -> q0\n" +
                "q0, a -> q1\n" +
                "q1, a -> q1\n" +
                "q1, b -> q1\n";

            NFA nfa = new NFA();
            nfa.parse(nfaInput);

            // Create test file in CSV format: input,expected (1=accept, 0=reject)
            File testFile = createTestFile(
                "a,1\n" +
                "aa,1\n" +
                "ba,1\n" +
                "b,0\n"
            );

            // Create settings
            SessionService.TestSettings settings = new SessionService.TestSettings(
                0, 100, 30, null, null, null);

            // Run tests
            TestResultViewModel result = testService.runTestsWithValidation(
                nfa, testFile.getAbsolutePath(), settings, null);

            // Verify
            assertFalse(result.hasLimitViolation());
            assertEquals(4, result.getTotalTests());
            assertTrue(result.getPassedTests() > 0);
            assertNotNull(result.getDetailedReport());
        }

        @Test
        @DisplayName("should return success ViewModel for TM tests")
        void testTmSuccess() throws IOException {
            // Create TM that accepts any input (immediately transitions to accept state)
            String tmInput =
                "states: q0 q_accept q_reject\n" +
                "input: a b\n" +
                "tape: a b _\n" +
                "start: q0\n" +
                "accept: q_accept\n" +
                "reject: q_reject\n" +
                "\n" +
                "transitions:\n" +
                "q0, a -> q_accept, a, R\n" +
                "q0, b -> q_accept, b, R\n" +
                "q0, _ -> q_accept, _, R\n";

            // TM.parse() returns a new TM in the ParseResult, doesn't update the original
            TM tmTemplate = new TM();
            common.ParseResult parseResult = tmTemplate.parse(tmInput);
            assertTrue(parseResult.isSuccess(), "TM should parse successfully");
            TM tm = (TM) parseResult.getAutomaton();

            // Create test file in CSV format: input,expected (1=accept, 0=reject)
            File testFile = createTestFile(
                "a,1\n" +
                "b,1\n"
            );

            // Create settings
            SessionService.TestSettings settings = new SessionService.TestSettings(
                0, 100, 30, null, null, null);

            // Run tests
            TestResultViewModel result = testService.runTestsWithValidation(
                tm, testFile.getAbsolutePath(), settings, null);

            // Verify
            assertFalse(result.hasLimitViolation());
            assertEquals(2, result.getTotalTests());
            assertTrue(result.getPassedTests() > 0);
            assertNotNull(result.getDetailedReport());
        }
    }

    @Nested
    @DisplayName("CFG Rules Limit Violations")
    class CfgRulesViolationTests {

        @Test
        @DisplayName("should return CFG violation ViewModel when rules exceed limit")
        void testCfgRulesViolation() throws IOException {
            // Create CFG with 8 production rules using correct format
            // S -> aB | bA, A -> a | aS | bAA, B -> b | bS | aBB
            String cfgInput =
                "vars: S A B\n" +
                "terminals: a b\n" +
                "start: S\n" +
                "\n" +
                "rules:\n" +
                "S -> a B | b A\n" +
                "A -> a | a S | b A A\n" +
                "B -> b | b S | a B B\n";

            CFG cfg = new CFG();
            cfg.parse(cfgInput);

            // Verify CFG parsed correctly and has 8 productions
            assertTrue(cfg.getProductions().size() > 5,
                "CFG should have more than 5 productions, has " + cfg.getProductions().size());

            // Create test file in CSV format
            File testFile = createTestFile("ab,1\n");

            // Create settings with max 5 rules (CFG has 8)
            SessionService.TestSettings settings = new SessionService.TestSettings(
                0, 100, 30, 5, null, null);

            // Run tests
            TestResultViewModel result = testService.runTestsWithValidation(
                cfg, testFile.getAbsolutePath(), settings, null);

            // Verify violation
            assertTrue(result.hasLimitViolation());
            assertEquals("CFG_RULES", result.getLimitViolationType());
            assertNotNull(result.getLimitViolationMessage());
            assertTrue(result.getLimitViolationMessage().contains("CFG RULES LIMIT VIOLATION"));
            assertEquals(0.0, result.getEarnedPoints());
        }

        @Test
        @DisplayName("should pass when CFG rules are within limit")
        void testCfgRulesWithinLimit() throws IOException {
            // Create simple CFG with 2 rules using correct format
            String cfgInput =
                "vars: S\n" +
                "terminals: a b\n" +
                "start: S\n" +
                "\n" +
                "rules:\n" +
                "S -> a | b\n";

            CFG cfg = new CFG();
            cfg.parse(cfgInput);

            // Verify CFG parsed correctly
            assertTrue(cfg.getProductions().size() > 0, "CFG should have productions");

            // Create test file in CSV format
            File testFile = createTestFile("a,1\nb,1\n");

            // Create settings with max 5 rules
            SessionService.TestSettings settings = new SessionService.TestSettings(
                0, 100, 30, 5, null, null);

            // Run tests
            TestResultViewModel result = testService.runTestsWithValidation(
                cfg, testFile.getAbsolutePath(), settings, null);

            // Verify no violation
            assertFalse(result.hasLimitViolation());
            assertTrue(result.getPassedTests() > 0);
        }

        @Test
        @DisplayName("should skip CFG check when maxRules is null")
        void testCfgNoLimitCheck() throws IOException {
            // Create CFG with 4 rules using correct format
            String cfgInput =
                "vars: S A B\n" +
                "terminals: a b\n" +
                "start: S\n" +
                "\n" +
                "rules:\n" +
                "S -> a B | b A\n" +
                "A -> a\n" +
                "B -> b\n";

            CFG cfg = new CFG();
            cfg.parse(cfgInput);

            // Create test file in CSV format
            File testFile = createTestFile("ab,1\n");

            // Create settings with no rules limit
            SessionService.TestSettings settings = new SessionService.TestSettings(
                0, 100, 30, null, null, null);

            // Run tests
            TestResultViewModel result = testService.runTestsWithValidation(
                cfg, testFile.getAbsolutePath(), settings, null);

            // Verify no violation (limit not checked)
            assertFalse(result.hasLimitViolation());
        }
    }

    @Nested
    @DisplayName("PDA Transitions Limit Violations")
    class PdaTransitionsViolationTests {

        @Test
        @DisplayName("should return PDA violation ViewModel when transitions exceed limit")
        void testPdaTransitionsViolation() throws IOException {
            // Create PDA with 5 transitions using correct format
            // Format: states, input, stack, start, stack_start, accept, transitions
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

            PDA pda = new PDA();
            pda.parse(pdaInput);

            // Verify PDA has transitions (should be 5 based on format, but could vary)
            int transitionCount = pda.getTransitionCount();
            assertTrue(transitionCount > 0,
                "PDA should have transitions, has " + transitionCount);

            // Create test file in CSV format
            File testFile = createTestFile("ab,1\n");

            // Create settings with max 2 transitions (to ensure PDA exceeds limit)
            SessionService.TestSettings settings = new SessionService.TestSettings(
                0, 100, 30, null, 2, null);

            // Run tests
            TestResultViewModel result = testService.runTestsWithValidation(
                pda, testFile.getAbsolutePath(), settings, null);

            // Verify violation
            assertTrue(result.hasLimitViolation());
            assertEquals("PDA_TRANSITIONS", result.getLimitViolationType());
            assertNotNull(result.getLimitViolationMessage());
            assertTrue(result.getLimitViolationMessage().contains("PDA TRANSITIONS LIMIT VIOLATION"));
            assertEquals(0.0, result.getEarnedPoints());
        }

        @Test
        @DisplayName("should pass when PDA transitions are within limit")
        void testPdaTransitionsWithinLimit() throws IOException {
            // Create simple PDA with 2 transitions using correct format
            String pdaInput =
                "states: q0 q1 q2\n" +
                "input: a\n" +
                "stack: Z\n" +
                "start: q0\n" +
                "stack_start: Z\n" +
                "accept: q2\n" +
                "transitions:\n" +
                "q0, a, Z -> q1, Z\n" +
                "q1, eps, Z -> q2, eps\n";

            PDA pda = new PDA();
            pda.parse(pdaInput);

            // Create test file in CSV format
            File testFile = createTestFile("a,1\n");

            // Create settings with max 5 transitions (PDA has 2)
            SessionService.TestSettings settings = new SessionService.TestSettings(
                0, 100, 30, null, 5, null);

            // Run tests
            TestResultViewModel result = testService.runTestsWithValidation(
                pda, testFile.getAbsolutePath(), settings, null);

            // Verify no violation
            assertFalse(result.hasLimitViolation());
        }
    }

    @Nested
    @DisplayName("Input Validation")
    class InputValidationTests {

        @Test
        @DisplayName("should throw exception for null automaton")
        void testNullAutomaton() throws IOException {
            File testFile = createTestFile("a,1\n");
            SessionService.TestSettings settings = new SessionService.TestSettings(
                0, 100, 30, null, null, null);

            assertThrows(IllegalArgumentException.class, () ->
                testService.runTestsWithValidation(null, testFile.getAbsolutePath(), settings, null));
        }

        @Test
        @DisplayName("should throw exception for null test file path")
        void testNullTestFilePath() {
            DFA dfa = new DFA();
            dfa.parse("states: q0\nalphabet: a\nstart: q0\naccept: q0\n\ntransitions:\nq0, a -> q0\n");
            SessionService.TestSettings settings = new SessionService.TestSettings(
                0, 100, 30, null, null, null);

            assertThrows(IllegalArgumentException.class, () ->
                testService.runTestsWithValidation(dfa, null, settings, null));
        }

        @Test
        @DisplayName("should throw exception for null settings")
        void testNullSettings() throws IOException {
            DFA dfa = new DFA();
            dfa.parse("states: q0\nalphabet: a\nstart: q0\naccept: q0\n\ntransitions:\nq0, a -> q0\n");
            File testFile = createTestFile("a,1\n");

            assertThrows(IllegalArgumentException.class, () ->
                testService.runTestsWithValidation(dfa, testFile.getAbsolutePath(), null, null));
        }
    }

    // Helper method to create test files in CSV format
    private File createTestFile(String content) throws IOException {
        File testFile = new File(tempDir, "test.test");
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write(content);
        }
        return testFile;
    }
}
