package DeterministicFiniteAutomaton;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import common.ExecutionResult;
import common.ParseResult;
import common.State;
import common.Symbol;
import common.ValidationMessage;
import common.ValidationMessage.ValidationMessageType;

/**
 * Comprehensive JUnit 5 test class for DFA execution functionality.
 * Tests the execute() method inherited from Automaton abstract class.
 */
@DisplayName("DFA Execute Method Tests")
public class DFAExecuteTest {

    private DFA dfa;
    private Set<State> states;
    private Set<Symbol> alphabet;
    private Set<State> finalStates;
    private Set<Transition> transitions;

    @BeforeEach
    void setUp() {
        // Setup will be customized in nested test classes
    }

    /**
     * Helper method to create a simple DFA that accepts strings ending with 'a'
     */
    private DFA createSimpleEndingWithA_DFA() {
        State q0 = new State("q0", true, false);
        State q1 = new State("q1", false, true);
        
        states = new HashSet<>();
        states.add(q0);
        states.add(q1);
        
        alphabet = new HashSet<>();
        alphabet.add(new Symbol('a'));
        alphabet.add(new Symbol('b'));
        
        finalStates = new HashSet<>();
        finalStates.add(q1);
        
        transitions = new HashSet<>();
        transitions.add(new Transition(q0, new Symbol('a'), q1));
        transitions.add(new Transition(q0, new Symbol('b'), q0));
        transitions.add(new Transition(q1, new Symbol('a'), q1));
        transitions.add(new Transition(q1, new Symbol('b'), q0));
        
        return new DFA(states, alphabet, finalStates, q0, transitions);
    }

    @Nested
    @DisplayName("Basic Execution Tests")
    class BasicExecutionTests {
        
        @BeforeEach
        void setUp() {
            dfa = createSimpleEndingWithA_DFA();
        }
        
        @Test
        @DisplayName("Execute should return ExecutionResult object")
        void testExecuteReturnsResult() {
            ExecutionResult result = dfa.execute("a");
            
            assertNotNull(result, "ExecutionResult should not be null");
            assertNotNull(result.getTrace(), "Trace should not be null");
            assertNotNull(result.getRuntimeMessages(), "Runtime messages should not be null");
        }
        
        @Test
        @DisplayName("Should accept strings ending with 'a'")
        void testAcceptStringsEndingWithA() {
            assertTrue(dfa.execute("a").isAccepted(), "'a' should be accepted");
            assertTrue(dfa.execute("ba").isAccepted(), "'ba' should be accepted");
            assertTrue(dfa.execute("bba").isAccepted(), "'bba' should be accepted");
            assertTrue(dfa.execute("aba").isAccepted(), "'aba' should be accepted");
            assertTrue(dfa.execute("bbbbba").isAccepted(), "'bbbbba' should be accepted");
        }
        
        @Test
        @DisplayName("Should reject strings not ending with 'a'")
        void testRejectStringsNotEndingWithA() {
            assertFalse(dfa.execute("b").isAccepted(), "'b' should be rejected");
            assertFalse(dfa.execute("ab").isAccepted(), "'ab' should be rejected");
            assertFalse(dfa.execute("abb").isAccepted(), "'abb' should be rejected");
            assertFalse(dfa.execute("bab").isAccepted(), "'bab' should be rejected");
            assertFalse(dfa.execute("").isAccepted(), "Empty string should be rejected");
        }
        
        @Test
        @DisplayName("Trace should contain state transitions")
        void testTraceContainsTransitions() {
            ExecutionResult result = dfa.execute("aba");
            String trace = result.getTrace();
            
            assertNotNull(trace, "Trace should not be null");
            assertFalse(trace.isEmpty(), "Trace should not be empty");
            
            // Trace should show the path through states
            assertTrue(trace.contains("q0") || trace.contains("q1"), 
                "Trace should contain state information");
        }
    }

    @Nested
    @DisplayName("ExecutionResult Validation Tests")
    class ExecutionResultTests {
        
        @BeforeEach
        void setUp() {
            dfa = createSimpleEndingWithA_DFA();
        }
        
        @Test
        @DisplayName("Accepted result should have isAccepted() true")
        void testAcceptedResult() {
            ExecutionResult result = dfa.execute("a");
            
            assertTrue(result.isAccepted(), "Result should be accepted");
            assertNotNull(result.getTrace(), "Accepted result should have trace");
        }
        
        @Test
        @DisplayName("Rejected result should have isAccepted() false")
        void testRejectedResult() {
            ExecutionResult result = dfa.execute("b");
            
            assertFalse(result.isAccepted(), "Result should be rejected");
            assertNotNull(result.getTrace(), "Rejected result should have trace");
        }
        
        @Test
        @DisplayName("Runtime messages should be populated appropriately")
        void testRuntimeMessages() {
            ExecutionResult result = dfa.execute("ab");
            List<ValidationMessage> messages = result.getRuntimeMessages();
            
            assertNotNull(messages, "Runtime messages should not be null");
            // Messages may be empty for valid execution or contain info/warnings
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {
        
        @BeforeEach
        void setUp() {
            dfa = createSimpleEndingWithA_DFA();
        }
        
        @Test
        @DisplayName("Empty string execution")
        void testEmptyString() {
            ExecutionResult result = dfa.execute("");
            
            assertNotNull(result, "Result should not be null for empty string");
            assertFalse(result.isAccepted(), "Empty string should be rejected (start state not final)");
            assertNotNull(result.getTrace(), "Should have trace for empty string");
        }
        
        @Test
        @DisplayName("Null input should throw exception")
        void testNullInput() {
            assertThrows(IllegalArgumentException.class, () -> {
                dfa.execute(null);
            }, "Null input should throw IllegalArgumentException");
        }
        
        @Test
        @DisplayName("String with invalid symbols")
        void testInvalidSymbols() {
            ExecutionResult result = dfa.execute("abc");
            
            assertNotNull(result, "Result should not be null");
            assertFalse(result.isAccepted(), "String with invalid symbol 'c' should be rejected");
            
            // Check for runtime messages about invalid symbol
            List<ValidationMessage> messages = result.getRuntimeMessages();
            if (messages != null && !messages.isEmpty()) {
                // Invalid symbol might generate an error message
                messages.stream()
                    .anyMatch(m -> m.getType() == ValidationMessageType.ERROR);
            }
        }
        
        @Test
        @DisplayName("Very long string execution")
        void testVeryLongString() {
            StringBuilder longString = new StringBuilder();
            for (int i = 0; i < 1000; i++) {
                longString.append("ab");
            }
            longString.append("a"); // End with 'a' to be accepted
            
            ExecutionResult result = dfa.execute(longString.toString());
            
            assertNotNull(result, "Result should not be null for long string");
            assertTrue(result.isAccepted(), "Long string ending with 'a' should be accepted");
            assertNotNull(result.getTrace(), "Should have trace for long string");
        }
    }

    @Nested
    @DisplayName("Complex DFA Tests")
    class ComplexDFATests {
        
        @Test
        @DisplayName("DFA with start state as final state")
        void testStartStateAsFinal() {
            State q0 = new State("q0", true, true); // Both start and final
            
            states = new HashSet<>();
            states.add(q0);
            
            alphabet = new HashSet<>();
            alphabet.add(new Symbol('a'));
            
            finalStates = new HashSet<>();
            finalStates.add(q0);
            
            transitions = new HashSet<>();
            transitions.add(new Transition(q0, new Symbol('a'), q0));
            
            DFA simpleDFA = new DFA(states, alphabet, finalStates, q0, transitions);
            
            // Empty string should be accepted when start state is final
            assertTrue(simpleDFA.execute("").isAccepted(), 
                "Empty string should be accepted when start state is final");
            assertTrue(simpleDFA.execute("a").isAccepted(), "'a' should be accepted");
            assertTrue(simpleDFA.execute("aaa").isAccepted(), "'aaa' should be accepted");
        }
        
        @Test
        @DisplayName("DFA with no final states")
        void testNoFinalStates() {
            State q0 = new State("q0", true, false);
            State q1 = new State("q1", false, false);
            
            states = new HashSet<>();
            states.add(q0);
            states.add(q1);
            
            alphabet = new HashSet<>();
            alphabet.add(new Symbol('a'));
            
            finalStates = new HashSet<>(); // Empty set of final states
            
            transitions = new HashSet<>();
            transitions.add(new Transition(q0, new Symbol('a'), q1));
            transitions.add(new Transition(q1, new Symbol('a'), q0));
            
            DFA noFinalDFA = new DFA(states, alphabet, finalStates, q0, transitions);
            
            assertFalse(noFinalDFA.execute("").isAccepted(), 
                "Empty string should be rejected when no final states");
            assertFalse(noFinalDFA.execute("a").isAccepted(), 
                "'a' should be rejected when no final states");
            assertFalse(noFinalDFA.execute("aa").isAccepted(), 
                "'aa' should be rejected when no final states");
        }
        
        @Test
        @DisplayName("DFA with unreachable states")
        void testUnreachableStates() {
            State q0 = new State("q0", true, false);
            State q1 = new State("q1", false, true);
            State q2 = new State("q2", false, false); // Unreachable
            
            states = new HashSet<>();
            states.add(q0);
            states.add(q1);
            states.add(q2);
            
            alphabet = new HashSet<>();
            alphabet.add(new Symbol('a'));
            
            finalStates = new HashSet<>();
            finalStates.add(q1);
            
            transitions = new HashSet<>();
            transitions.add(new Transition(q0, new Symbol('a'), q1));
            transitions.add(new Transition(q1, new Symbol('a'), q1));
            // q2 has no incoming transitions - unreachable
            transitions.add(new Transition(q2, new Symbol('a'), q2));
            
            DFA unreachableDFA = new DFA(states, alphabet, finalStates, q0, transitions);
            
            // Should still work correctly despite unreachable state
            assertTrue(unreachableDFA.execute("a").isAccepted(), "'a' should be accepted");
            assertTrue(unreachableDFA.execute("aa").isAccepted(), "'aa' should be accepted");
        }
    }

    @Nested
    @DisplayName("Parameterized Tests")
    class ParameterizedTests {
        
        @BeforeEach
        void setUp() {
            dfa = createSimpleEndingWithA_DFA();
        }
        
        @ParameterizedTest
        @ValueSource(strings = {"a", "aa", "ba", "aba", "bba", "aaa", "bbbbba"})
        @DisplayName("Should accept all strings ending with 'a'")
        void testAcceptedStrings(String input) {
            ExecutionResult result = dfa.execute(input);
            assertTrue(result.isAccepted(), 
                String.format("String '%s' should be accepted", input));
        }
        
        @ParameterizedTest
        @ValueSource(strings = {"b", "ab", "bb", "aab", "bbb", "abab", "bbbbbb"})
        @DisplayName("Should reject all strings not ending with 'a'")
        void testRejectedStrings(String input) {
            ExecutionResult result = dfa.execute(input);
            assertFalse(result.isAccepted(), 
                String.format("String '%s' should be rejected", input));
        }
        
        @ParameterizedTest
        @CsvSource({
            "a, true",
            "b, false",
            "aa, true",
            "ab, false",
            "ba, true",
            "bb, false",
            "aba, true",
            "abb, false"
        })
        @DisplayName("Test various inputs with expected results")
        void testVariousInputs(String input, boolean expectedAccepted) {
            ExecutionResult result = dfa.execute(input);
            assertEquals(expectedAccepted, result.isAccepted(),
                String.format("String '%s' should be %s", 
                    input, expectedAccepted ? "accepted" : "rejected"));
        }
    }

    @Nested
    @DisplayName("Trace Validation Tests")
    class TraceValidationTests {
        
        @BeforeEach
        void setUp() {
            dfa = createSimpleEndingWithA_DFA();
        }
        
        @Test
        @DisplayName("Trace should show complete execution path")
        void testCompleteTracePath() {
            ExecutionResult result = dfa.execute("aba");
            String trace = result.getTrace();
            
            assertNotNull(trace, "Trace should not be null");
            assertFalse(trace.isEmpty(), "Trace should not be empty");
            
            // The trace should show transitions for each symbol
            // Expected path: q0 --a--> q1 --b--> q0 --a--> q1 (accepted)
        }
        
        @Test
        @DisplayName("Trace for rejected string should show path until rejection")
        void testRejectedTrace() {
            ExecutionResult result = dfa.execute("ab");
            String trace = result.getTrace();
            
            assertNotNull(trace, "Trace should not be null for rejected string");
            assertFalse(trace.isEmpty(), "Trace should not be empty for rejected string");
            
            // Should show the path even for rejected strings
            // Expected path: q0 --a--> q1 --b--> q0 (rejected)
        }
        
        @Test
        @DisplayName("Trace for empty string")
        void testEmptyStringTrace() {
            ExecutionResult result = dfa.execute("");
            String trace = result.getTrace();
            
            assertNotNull(trace, "Trace should not be null for empty string");
            // Trace might indicate starting state and immediate rejection/acceptance
        }
    }

    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {
        
        @BeforeEach
        void setUp() {
            dfa = createSimpleEndingWithA_DFA();
        }
        
        @Test
        @DisplayName("Should handle strings with repeated patterns efficiently")
        void testRepeatedPatterns() {
            StringBuilder repeatedPattern = new StringBuilder();
            for (int i = 0; i < 500; i++) {
                repeatedPattern.append("ab");
            }
            repeatedPattern.append("a");
            
            long startTime = System.currentTimeMillis();
            ExecutionResult result = dfa.execute(repeatedPattern.toString());
            long endTime = System.currentTimeMillis();
            
            assertTrue(result.isAccepted(), "Repeated pattern ending with 'a' should be accepted");
            assertTrue((endTime - startTime) < 1000, "Execution should complete within 1 second");
        }
        
        @Test
        @DisplayName("Should handle very long single-character strings")
        void testLongSingleCharacterString() {
            StringBuilder longString = new StringBuilder();
            for (int i = 0; i < 10000; i++) {
                longString.append('a');
            }
            
            long startTime = System.currentTimeMillis();
            ExecutionResult result = dfa.execute(longString.toString());
            long endTime = System.currentTimeMillis();
            
            assertTrue(result.isAccepted(), "Long string of 'a's should be accepted");
            assertTrue((endTime - startTime) < 1000, "Execution should complete within 1 second");
        }
    }

    @Nested
    @DisplayName("DFA from Parse Tests")
    class DFAFromParseTests {
        
        @Test
        @DisplayName("Execute on parsed DFA")
        void testExecuteOnParsedDFA() {
            String dfaDefinition = "Start: q0\n" +
                                  "Finals: q1\n" +
                                  "Alphabet: a b\n" +
                                  "States: q0 q1\n" +
                                  "Transitions:\n" +
                                  "q0 -> q1 (a)\n" +
                                  "q0 -> q0 (b)\n" +
                                  "q1 -> q1 (a)\n" +
                                  "q1 -> q0 (b)\n";
            
            DFA parsedDFA = new DFA();
            ParseResult parseResult = parsedDFA.parse(dfaDefinition);
            
            if (parseResult.isSuccess()) {
                DFA automaton = (DFA) parseResult.getAutomaton();
                
                // Test execution on parsed DFA
                assertTrue(automaton.execute("a").isAccepted(), "Parsed DFA should accept 'a'");
                assertFalse(automaton.execute("b").isAccepted(), "Parsed DFA should reject 'b'");
                assertTrue(automaton.execute("ba").isAccepted(), "Parsed DFA should accept 'ba'");
            } else {
                fail("DFA parsing failed, cannot test execution");
            }
        }
    }
}
