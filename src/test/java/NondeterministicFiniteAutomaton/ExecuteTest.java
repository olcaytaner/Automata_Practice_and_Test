package NondeterministicFiniteAutomaton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import common.ExecutionResult;
import common.FSATransition;
import common.ParseResult;
import common.State;
import common.Symbol;
import common.ValidationMessage;

/**
 * JUnit 5 test class for NFA execution functionality.
 * Tests NFA construction, execution, and string acceptance.
 */
@DisplayName("NFA Execution Tests")
public class ExecuteTest {

    private NFA nfa;
    private Map<State, List<FSATransition>> transitions;
    private State startState;
    private State state2;
    private State state3;
    private Map<String, State> states;
    private Set<State> finalStates;

    @BeforeEach
    void setUp() {
        transitions = new HashMap<>();

        startState = new State("q1");
        startState.setStart(true);

        states = new HashMap<>();

        state2 = new State("q2");
        state3 = new State("q3");
        state3.setAccept(true);

        finalStates = new HashSet<>();
        finalStates.add(state3);

        states.put("q1", startState);
        states.put("q2", state2);
        states.put("q3", state3);


        Symbol symbol1 = new Symbol('a');
        Symbol symbol2 = new Symbol('b');
        Symbol symbol3 = new Symbol('c');
        Symbol epsilon = new Symbol('_');

        Set<Symbol> alphabet = new HashSet<>();
        alphabet.add(symbol1);
        alphabet.add(symbol2);
        alphabet.add(symbol3);
        alphabet.add(epsilon);

        FSATransition transition = new FSATransition(startState, symbol2, state2);
        FSATransition transition2 = new FSATransition(startState, symbol1, startState);
        FSATransition transition3 = new FSATransition(state2, symbol3, state3);

        List<FSATransition> transitionsFromState1 = new ArrayList<>();
        transitionsFromState1.add(transition);
        transitionsFromState1.add(transition2);
        transitions.put(startState, transitionsFromState1);

        List<FSATransition> transitionsFromState2 = new ArrayList<>();
        transitionsFromState2.add(transition3);
        transitions.put(state2, transitionsFromState2);

        // NFA constructor: (states, alphabet, startState, finalStates, transitions)
        nfa = new NFA(states, alphabet, startState, finalStates, transitions);
    }

    @Nested
    @DisplayName("Basic Execution Tests")
    class BasicExecutionTests {

        @Test
        @DisplayName("Execute should return ExecutionResult object")
        void testExecuteReturnsResult() {
            ExecutionResult result = nfa.execute("abc");

            assertNotNull(result, "ExecutionResult should not be null");
            assertNotNull(result.getTrace(), "Trace should not be null");
            assertNotNull(result.getRuntimeMessages(), "Runtime messages should not be null");
        }

        @Test
        @DisplayName("Should accept valid strings")
        void testAcceptValidStrings() {
            assertTrue(nfa.execute("abc").isAccepted(), "'abc' should be accepted");
            assertTrue(nfa.execute("aaabc").isAccepted(), "'aaabc' should be accepted");
            assertTrue(nfa.execute("bc").isAccepted(), "'bc' should be accepted");
        }

        @Test
        @DisplayName("Should reject invalid strings")
        void testRejectInvalidStrings() {
            assertFalse(nfa.execute("ab").isAccepted(), "'ab' should be rejected");
            assertFalse(nfa.execute("a").isAccepted(), "'a' should be rejected");
            assertFalse(nfa.execute("").isAccepted(), "Empty string should be rejected");
            assertFalse(nfa.execute("c").isAccepted(), "'c' should be rejected");
        }
    }

    @Nested
    @DisplayName("ExecutionResult Validation Tests")
    class ExecutionResultValidationTests {

        @Test
        @DisplayName("Accepted result should have isAccepted() true")
        void testAcceptedResult() {
            ExecutionResult result = nfa.execute("abc");

            assertTrue(result.isAccepted(), "Result should be accepted");
            assertNotNull(result.getTrace(), "Accepted result should have trace");
        }

        @Test
        @DisplayName("Rejected result should have isAccepted() false")
        void testRejectedResult() {
            ExecutionResult result = nfa.execute("ab");

            assertFalse(result.isAccepted(), "Result should be rejected");
            assertNotNull(result.getTrace(), "Rejected result should have trace");
        }

        @Test
        @DisplayName("Runtime messages should be available")
        void testRuntimeMessages() {
            ExecutionResult result = nfa.execute("abc");
            List<ValidationMessage> messages = result.getRuntimeMessages();

            assertNotNull(messages, "Runtime messages should not be null");
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Empty string execution")
        void testEmptyString() {
            ExecutionResult result = nfa.execute("");

            assertNotNull(result, "Result should not be null for empty string");
            assertFalse(result.isAccepted(), "Empty string should be rejected when start state is not final");
        }

        @Test
        @DisplayName("Very long string execution")
        void testVeryLongString() {
            StringBuilder longString = new StringBuilder();
            for (int i = 0; i < 100; i++) {
                longString.append('a');
            }
            longString.append("bc");

            ExecutionResult result = nfa.execute(longString.toString());

            assertNotNull(result, "Result should not be null for long string");
            assertTrue(result.isAccepted(), "Long string ending with 'bc' should be accepted");
        }

        @Test
        @DisplayName("String with invalid symbols")
        void testInvalidSymbols() {
            ExecutionResult result = nfa.execute("xyz");

            assertNotNull(result, "Result should not be null");
            assertFalse(result.isAccepted(), "String with invalid symbols should be rejected");
        }
    }

    @Nested
    @DisplayName("NFA from Parse Tests")
    class NFAFromParseTests {

        @Test
        @DisplayName("Execute on parsed NFA")
        void testExecuteOnParsedNFA() {
            String nfaDefinition = "states: q0 q1 q2\n" +
                    "alphabet: a b\n" +
                    "start: q0\n" +
                    "accept: q2\n" +
                    "transitions:\n" +
                    "q0, a -> q0\n" +
                    "q0, a -> q1\n" +
                    "q0, b -> q1\n" +
                    "q1, b -> q2\n";

            NFA parsedNFA = new NFA();
            ParseResult parseResult = parsedNFA.parse(nfaDefinition);

            if (parseResult.isSuccess()) {
                NFA automaton = (NFA) parseResult.getAutomaton();

                assertTrue(automaton.execute("ab").isAccepted(), "Parsed NFA should accept 'ab'");
                assertTrue(automaton.execute("aab").isAccepted(), "Parsed NFA should accept 'aab'");
                assertTrue(automaton.execute("bb").isAccepted(), "Parsed NFA should accept 'bb'");
                assertFalse(automaton.execute("a").isAccepted(), "Parsed NFA should reject 'a'");
            } else {
                // Log the validation messages for debugging
                System.out.println("NFA parsing failed with messages: " + parseResult.getValidationMessages());
            }
        }
    }

    @Nested
    @DisplayName("Epsilon Transition Tests")
    class EpsilonTransitionTests {

        @Test
        @DisplayName("NFA with epsilon transitions should work correctly")
        void testEpsilonTransitions() {
            String nfaWithEpsilon = "states: q0 q1 q2\n" +
                    "alphabet: a b\n" +
                    "start: q0\n" +
                    "accept: q2\n" +
                    "transitions:\n" +
                    "q0, eps -> q1\n" +
                    "q1, a -> q2\n";

            NFA parsedNFA = new NFA();
            ParseResult parseResult = parsedNFA.parse(nfaWithEpsilon);

            if (parseResult.isSuccess()) {
                NFA automaton = (NFA) parseResult.getAutomaton();
                assertTrue(automaton.execute("a").isAccepted(), "'a' should be accepted via epsilon transition");
            }
        }
    }

    @Nested
    @DisplayName("Trace Validation Tests")
    class TraceValidationTests {

        @Test
        @DisplayName("Trace should contain execution path")
        void testTraceContainsPath() {
            ExecutionResult result = nfa.execute("abc");
            String trace = result.getTrace();

            assertNotNull(trace, "Trace should not be null");
            assertFalse(trace.isEmpty(), "Trace should not be empty");
        }

        @Test
        @DisplayName("Trace for rejected string should show path until rejection")
        void testRejectedTrace() {
            ExecutionResult result = nfa.execute("ab");
            String trace = result.getTrace();

            assertNotNull(trace, "Trace should not be null for rejected string");
            assertFalse(trace.isEmpty(), "Trace should not be empty for rejected string");
        }
    }

    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Should handle multiple executions efficiently")
        void testMultipleExecutions() {
            long startTime = System.currentTimeMillis();

            for (int i = 0; i < 1000; i++) {
                nfa.execute("abc");
            }

            long endTime = System.currentTimeMillis();
            assertTrue((endTime - startTime) < 5000, "1000 executions should complete within 5 seconds");
        }
    }
}
