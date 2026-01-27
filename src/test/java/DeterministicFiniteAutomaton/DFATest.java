package DeterministicFiniteAutomaton;

import common.FSATransition;
import common.State;
import common.Symbol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;

public class DFATest {

    private DFA dfa;
    private Set<State> states;
    private Set<Symbol> alphabet;
    private Set<State> finalStates;
    private Set<FSATransition> transitions;
    private State q0, q1, q2, q3, q4, q5, q6, q7;

    @BeforeEach
    void setUp() {
        setupComplexDFA();
    }

    private void setupComplexDFA() {
        // Create states - using common.State constructor with name only
        q0 = new State("q0");
        q1 = new State("q1");
        q2 = new State("q2");
        q3 = new State("q3");
        q4 = new State("q4");
        q5 = new State("q5");
        q6 = new State("q6");
        q7 = new State("q7");

        states = new HashSet<>();
        states.add(q0);
        states.add(q1);
        states.add(q2);
        states.add(q3);
        states.add(q4);
        states.add(q5);
        states.add(q6);
        states.add(q7);

        // Create alphabet - using common.Symbol constructor with char
        alphabet = new HashSet<>();
        alphabet.add(new Symbol('a'));
        alphabet.add(new Symbol('b'));
        alphabet.add(new Symbol('c'));

        // Set start state - mark as start state in the constructor
        State startState = new State("q0", true, false);

        // Set final states - mark as accept states in the constructor
        finalStates = new HashSet<>();
        finalStates.add(new State("q5", false, true));
        finalStates.add(new State("q7", false, true));

        // Update the states set with the actual state objects that have the correct flags
        states.remove(q0);
        states.remove(q5);
        states.remove(q7);
        states.add(startState);
        states.addAll(finalStates);

        // Create transitions based on dfa.txt
        transitions = new HashSet<>();
        transitions.add(new FSATransition(startState, new Symbol('c'), startState));
        transitions.add(new FSATransition(startState, new Symbol('a'), q1));
        transitions.add(new FSATransition(startState, new Symbol('b'), q1));
        transitions.add(new FSATransition(q1, new Symbol('a'), q2));
        transitions.add(new FSATransition(q1, new Symbol('b'), q3));
        transitions.add(new FSATransition(q1, new Symbol('c'), q1));
        transitions.add(new FSATransition(q2, new Symbol('a'), q2));
        transitions.add(new FSATransition(q2, new Symbol('c'), q2));
        transitions.add(new FSATransition(q2, new Symbol('b'), q4));
        transitions.add(new FSATransition(q3, new Symbol('a'), q4));
        transitions.add(new FSATransition(q3, new Symbol('b'), q3));
        transitions.add(new FSATransition(q3, new Symbol('c'), q3));
        transitions.add(new FSATransition(q4, new Symbol('a'), new State("q5", false, true)));
        transitions.add(new FSATransition(q4, new Symbol('b'), q6));
        transitions.add(new FSATransition(q4, new Symbol('c'), q6));
        transitions.add(new FSATransition(new State("q5", false, true), new Symbol('a'), new State("q5", false, true)));
        transitions.add(new FSATransition(new State("q5", false, true), new Symbol('b'), new State("q5", false, true)));
        transitions.add(new FSATransition(new State("q5", false, true), new Symbol('c'), new State("q5", false, true)));
        transitions.add(new FSATransition(q6, new Symbol('a'), new State("q7", false, true)));
        transitions.add(new FSATransition(q6, new Symbol('b'), startState));
        transitions.add(new FSATransition(q6, new Symbol('c'), startState));
        transitions.add(new FSATransition(new State("q7", false, true), new Symbol('a'), new State("q7", false, true)));
        transitions.add(new FSATransition(new State("q7", false, true), new Symbol('b'), new State("q7", false, true)));
        transitions.add(new FSATransition(new State("q7", false, true), new Symbol('c'), new State("q7", false, true)));

        // Create DFA
        dfa = new DFA(states, alphabet, finalStates, startState, transitions);
    }

    @Nested
    @DisplayName("Basic Validation Tests")
    class BasicValidationTests {
        @Test
        @DisplayName("Valid strings should be accepted")
        void testValidStrings() {
            assertTrue(dfa.execute("aabacb").isAccepted());
            assertTrue(dfa.execute("abaaa").isAccepted());
        }

        @Test
        @DisplayName("Invalid strings should be rejected")
        void testInvalidStrings() {
            assertFalse(dfa.execute("abacba").isAccepted());
            assertFalse(dfa.execute("abacbab").isAccepted());
            assertFalse(dfa.execute("aabcb").isAccepted());
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {
        @Test
        @DisplayName("Null input should be handled gracefully")
        void testNullInput() {
            assertThrows(IllegalArgumentException.class, () -> dfa.execute(null));
        }

        @Test
        @DisplayName("Empty string should be rejected (start state not final)")
        void testEmptyString() {
            assertFalse(dfa.execute("").isAccepted());
        }

        @Test
        @DisplayName("Single character strings")
        void testSingleCharacters() {
            assertFalse(dfa.execute("a").isAccepted());
            assertFalse(dfa.execute("b").isAccepted());
            assertFalse(dfa.execute("c").isAccepted());
        }

        @Test
        @DisplayName("Invalid symbols should cause rejection")
        void testInvalidSymbols() {
            assertFalse(dfa.execute("ax").isAccepted()); // 'x' not in alphabet
            assertFalse(dfa.execute("a1b").isAccepted()); // '1' not in alphabet
            assertFalse(dfa.execute("ab!").isAccepted()); // '!' not in alphabet
        }
    }

    @Nested
    @DisplayName("Special DFA Configurations")
    class SpecialDFATests {
        @Test
        @DisplayName("DFA with start state as final state")
        void testStartStateAsFinal() {
            // Create a state that's both start and accept
            State s0 = new State("s0", true, true);
            
            Set<State> singleState = new HashSet<>();
            singleState.add(s0);

            Set<Symbol> simpleAlphabet = new HashSet<>();
            simpleAlphabet.add(new Symbol('a'));

            Set<State> finalStates = new HashSet<>();
            finalStates.add(s0);

            Set<FSATransition> selfLoop = new HashSet<>();
            selfLoop.add(new FSATransition(s0, new Symbol('a'), s0));

            DFA simpleDFA = new DFA(singleState, simpleAlphabet, finalStates, s0, selfLoop);

            assertTrue(simpleDFA.execute("").isAccepted());
            assertTrue(simpleDFA.execute("a").isAccepted());
            assertTrue(simpleDFA.execute("aa").isAccepted());
            assertTrue(simpleDFA.execute("aaa").isAccepted());
        }

        @Test
        @DisplayName("DFA with no final states")
        void testNoFinalStates() {
            State s0 = new State("s0", true, false);
            State s1 = new State("s1", false, false);
            
            Set<State> twoStates = new HashSet<>();
            twoStates.add(s0);
            twoStates.add(s1);

            Set<Symbol> simpleAlphabet = new HashSet<>();
            simpleAlphabet.add(new Symbol('a'));

            Set<State> emptyFinalStates = new HashSet<>();

            Set<FSATransition> transitions = new HashSet<>();
            transitions.add(new FSATransition(s0, new Symbol('a'), s1));
            transitions.add(new FSATransition(s1, new Symbol('a'), s0));

            DFA noFinalDFA = new DFA(twoStates, simpleAlphabet, emptyFinalStates, s0, transitions);

            assertFalse(noFinalDFA.execute("").isAccepted());
            assertFalse(noFinalDFA.execute("a").isAccepted());
            assertFalse(noFinalDFA.execute("aa").isAccepted());
        }
    }
}
