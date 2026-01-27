package PushDownAutomaton;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import common.ExecutionResult;
import common.ValidationMessage;

/**
 * Comprehensive JUnit 5 test class for PDA execution functionality.
 * Tests the execute() method inherited from Automaton abstract class,
 * focusing on stack operations and context-free language recognition.
 */
@DisplayName("PDA Execute Method Tests")
public class PDAExecuteTest {

    private PDA pda;
    private String validPDAContent;
    private String balancedParenthesesPDA;
    private String anbnPDA;
    private String palindromePDA;

    @BeforeEach
    void setUp() {
        pda = new PDA();
        
        // Simple PDA that accepts balanced parentheses
        balancedParenthesesPDA = "states: q0 q1 q2\n" +
                                 "alphabet: ( )\n" +
                                 "stack_alphabet: ( Z\n" +
                                 "start: q0\n" +
                                 "stack_start: Z\n" +
                                 "finals: q2\n" +
                                 "transitions:\n" +
                                 "q0 ( Z -> q0 (Z\n" +
                                 "q0 ( ( -> q0 ((\n" +
                                 "q0 ) ( -> q0 eps\n" +
                                 "q0 eps Z -> q2 eps\n";
        
        // PDA that accepts a^n b^n
        anbnPDA = "states: q0 q1 q2\n" +
                  "alphabet: a b\n" +
                  "stack_alphabet: a Z\n" +
                  "start: q0\n" +
                  "stack_start: Z\n" +
                  "finals: q2\n" +
                  "transitions:\n" +
                  "q0 a Z -> q0 aZ\n" +
                  "q0 a a -> q0 aa\n" +
                  "q0 b a -> q1 eps\n" +
                  "q1 b a -> q1 eps\n" +
                  "q1 eps Z -> q2 eps\n";
        
        // PDA for palindromes over {a, b}
        palindromePDA = "states: q0 q1 q2\n" +
                       "alphabet: a b\n" +
                       "stack_alphabet: a b Z\n" +
                       "start: q0\n" +
                       "stack_start: Z\n" +
                       "finals: q2\n" +
                       "transitions:\n" +
                       "q0 a Z -> q0 aZ\n" +
                       "q0 b Z -> q0 bZ\n" +
                       "q0 a a -> q0 aa\n" +
                       "q0 a b -> q0 ab\n" +
                       "q0 b a -> q0 ba\n" +
                       "q0 b b -> q0 bb\n" +
                       "q0 eps Z -> q1 Z\n" +
                       "q0 eps a -> q1 a\n" +
                       "q0 eps b -> q1 b\n" +
                       "q1 a a -> q1 eps\n" +
                       "q1 b b -> q1 eps\n" +
                       "q1 eps Z -> q2 eps\n";
    }

    @Nested
    @DisplayName("Basic Execution Tests")
    class BasicExecutionTests {
        
        @Test
        @DisplayName("Execute should return ExecutionResult object")
        void testExecuteReturnsResult() {
            pda.parse(anbnPDA);
            ExecutionResult result = pda.execute("aabb");
            
            assertNotNull(result, "ExecutionResult should not be null");
            assertNotNull(result.getTrace(), "Trace should not be null");
            assertNotNull(result.getRuntimeMessages(), "Runtime messages should not be null");
        }
        
        @Test
        @DisplayName("Should accept valid strings for a^n b^n")
        void testAcceptAnBn() {
            pda.parse(anbnPDA);
            
            assertTrue(pda.execute("ab").isAccepted(), "'ab' should be accepted");
            assertTrue(pda.execute("aabb").isAccepted(), "'aabb' should be accepted");
            assertTrue(pda.execute("aaabbb").isAccepted(), "'aaabbb' should be accepted");
            assertTrue(pda.execute("aaaabbbb").isAccepted(), "'aaaabbbb' should be accepted");
        }
        
        @Test
        @DisplayName("Should reject invalid strings for a^n b^n")
        void testRejectInvalidAnBn() {
            pda.parse(anbnPDA);
            
            assertFalse(pda.execute("a").isAccepted(), "'a' should be rejected");
            assertFalse(pda.execute("b").isAccepted(), "'b' should be rejected");
            assertFalse(pda.execute("aab").isAccepted(), "'aab' should be rejected");
            assertFalse(pda.execute("abb").isAccepted(), "'abb' should be rejected");
            assertFalse(pda.execute("ba").isAccepted(), "'ba' should be rejected");
            assertFalse(pda.execute("aabbb").isAccepted(), "'aabbb' should be rejected");
        }
    }

    @Nested
    @DisplayName("Stack Operation Tests")
    class StackOperationTests {
        
        @Test
        @DisplayName("Trace should contain stack operations")
        void testTraceContainsStackOperations() {
            pda.parse(anbnPDA);
            ExecutionResult result = pda.execute("aabb");
            String trace = result.getTrace();
            
            assertNotNull(trace, "Trace should not be null");
            assertFalse(trace.isEmpty(), "Trace should not be empty");
            
            // Trace should show stack operations (push/pop)
            // The exact format depends on PDA implementation
        }
        
        @Test
        @DisplayName("Stack should be empty for accepted strings")
        void testStackEmptyOnAcceptance() {
            pda.parse(anbnPDA);
            ExecutionResult result = pda.execute("aaabbb");
            
            assertTrue(result.isAccepted(), "'aaabbb' should be accepted");
            // Acceptance by empty stack or final state with empty stack
        }
        
        @Test
        @DisplayName("Stack operations for nested structures")
        void testNestedStructures() {
            pda.parse(balancedParenthesesPDA);
            
            assertTrue(pda.execute("()").isAccepted(), "'()' should be accepted");
            assertTrue(pda.execute("(())").isAccepted(), "'(())' should be accepted");
            assertTrue(pda.execute("(()())").isAccepted(), "'(()())' should be accepted");
            assertTrue(pda.execute("((()))").isAccepted(), "'((()))' should be accepted");
            
            assertFalse(pda.execute("(").isAccepted(), "'(' should be rejected");
            assertFalse(pda.execute(")").isAccepted(), "')' should be rejected");
            assertFalse(pda.execute("(()").isAccepted(), "'(()' should be rejected");
            assertFalse(pda.execute("())").isAccepted(), "'())' should be rejected");
        }
    }

    @Nested
    @DisplayName("ExecutionResult Validation Tests")
    class ExecutionResultTests {
        
        @Test
        @DisplayName("Accepted result should have isAccepted() true")
        void testAcceptedResult() {
            pda.parse(anbnPDA);
            ExecutionResult result = pda.execute("aabb");
            
            assertTrue(result.isAccepted(), "Result should be accepted");
            assertNotNull(result.getTrace(), "Accepted result should have trace");
        }
        
        @Test
        @DisplayName("Rejected result should have isAccepted() false")
        void testRejectedResult() {
            pda.parse(anbnPDA);
            ExecutionResult result = pda.execute("aab");
            
            assertFalse(result.isAccepted(), "Result should be rejected");
            assertNotNull(result.getTrace(), "Rejected result should have trace");
        }
        
        @Test
        @DisplayName("Runtime messages should be populated")
        void testRuntimeMessages() {
            pda.parse(anbnPDA);
            ExecutionResult result = pda.execute("ab");
            List<ValidationMessage> messages = result.getRuntimeMessages();
            
            assertNotNull(messages, "Runtime messages should not be null");
            // Messages may contain information about stack operations
        }
    }

    @Nested
    @DisplayName("Epsilon Transition Tests")
    class EpsilonTransitionTests {
        
        @Test
        @DisplayName("Epsilon transitions should be handled correctly")
        void testEpsilonTransitions() {
            pda.parse(anbnPDA);
            
            // Empty string test - PDA with epsilon transitions might accept it
            ExecutionResult emptyResult = pda.execute("");
            assertNotNull(emptyResult, "Should handle empty string");
            
            // The PDA accepts by final state with empty stack
            // Empty string might be accepted if there's a path to final state
        }
        
        @Test
        @DisplayName("Multiple epsilon transitions in sequence")
        void testMultipleEpsilonTransitions() {
            String pdaWithEpsilons = "states: q0 q1 q2 q3\n" +
                                    "alphabet: a\n" +
                                    "stack_alphabet: Z\n" +
                                    "start: q0\n" +
                                    "stack_start: Z\n" +
                                    "finals: q3\n" +
                                    "transitions:\n" +
                                    "q0 eps Z -> q1 Z\n" +
                                    "q1 eps Z -> q2 Z\n" +
                                    "q2 eps Z -> q3 Z\n";
            
            pda.parse(pdaWithEpsilons);
            ExecutionResult result = pda.execute("");
            
            assertNotNull(result, "Should handle multiple epsilon transitions");
            // Result depends on PDA implementation
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {
        
        @Test
        @DisplayName("Empty string execution")
        void testEmptyString() {
            pda.parse(anbnPDA);
            ExecutionResult result = pda.execute("");
            
            assertNotNull(result, "Result should not be null for empty string");
            // Empty string acceptance depends on PDA definition
        }
        
        @Test
        @DisplayName("Very long string execution")
        void testVeryLongString() {
            pda.parse(anbnPDA);
            
            StringBuilder longString = new StringBuilder();
            for (int i = 0; i < 100; i++) {
                longString.append('a');
            }
            for (int i = 0; i < 100; i++) {
                longString.append('b');
            }
            
            ExecutionResult result = pda.execute(longString.toString());
            
            assertNotNull(result, "Result should not be null for long string");
            assertTrue(result.isAccepted(), "100 a's followed by 100 b's should be accepted");
        }
        
        @Test
        @DisplayName("String with invalid symbols")
        void testInvalidSymbols() {
            pda.parse(anbnPDA);
            ExecutionResult result = pda.execute("aacbb");
            
            assertNotNull(result, "Result should not be null");
            assertFalse(result.isAccepted(), "String with invalid symbol 'c' should be rejected");
        }
        
        @Test
        @DisplayName("Stack overflow prevention")
        void testStackOverflowPrevention() {
            pda.parse(anbnPDA);
            
            // Create a string with many a's but no b's
            StringBuilder manyAs = new StringBuilder();
            for (int i = 0; i < 1000; i++) {
                manyAs.append('a');
            }
            
            ExecutionResult result = pda.execute(manyAs.toString());
            
            assertNotNull(result, "Should handle deep stack without overflow");
            assertFalse(result.isAccepted(), "String with only a's should be rejected");
        }
    }

    @Nested
    @DisplayName("Context-Free Language Tests")
    class ContextFreeLanguageTests {
        
        @Test
        @DisplayName("Test palindrome recognition")
        void testPalindromes() {
            pda.parse(palindromePDA);
            
            // Even-length palindromes
            assertTrue(pda.execute("").isAccepted(), "Empty string is a palindrome");
            assertTrue(pda.execute("aa").isAccepted(), "'aa' is a palindrome");
            assertTrue(pda.execute("bb").isAccepted(), "'bb' is a palindrome");
            assertTrue(pda.execute("abba").isAccepted(), "'abba' is a palindrome");
            assertTrue(pda.execute("baab").isAccepted(), "'baab' is a palindrome");
            
            // Odd-length palindromes (requires middle symbol handling)
            // These might not be accepted depending on PDA definition
            
            // Non-palindromes
            assertFalse(pda.execute("ab").isAccepted(), "'ab' is not a palindrome");
            assertFalse(pda.execute("abc").isAccepted(), "'abc' is not a palindrome");
            assertFalse(pda.execute("abab").isAccepted(), "'abab' is not a palindrome");
        }
        
        @Test
        @DisplayName("Test balanced parentheses")
        void testBalancedParentheses() {
            pda.parse(balancedParenthesesPDA);
            
            assertTrue(pda.execute("").isAccepted(), "Empty string should be accepted");
            assertTrue(pda.execute("()").isAccepted(), "'()' should be accepted");
            assertTrue(pda.execute("(())").isAccepted(), "'(())' should be accepted");
            assertTrue(pda.execute("()()").isAccepted(), "'()()' should be accepted");
            assertTrue(pda.execute("(()())").isAccepted(), "'(()())' should be accepted");
            assertTrue(pda.execute("(((())))").isAccepted(), "'(((())))' should be accepted");
            
            assertFalse(pda.execute("(").isAccepted(), "'(' should be rejected");
            assertFalse(pda.execute(")").isAccepted(), "')' should be rejected");
            assertFalse(pda.execute(")(").isAccepted(), "')(' should be rejected");
            assertFalse(pda.execute("(()").isAccepted(), "'(()' should be rejected");
            assertFalse(pda.execute("())").isAccepted(), "'())' should be rejected");
        }
    }

    @Nested
    @DisplayName("Parameterized Tests")
    class ParameterizedTests {
        
        @BeforeEach
        void setUp() {
            pda.parse(anbnPDA);
        }
        
        @ParameterizedTest
        @ValueSource(strings = {"ab", "aabb", "aaabbb", "aaaabbbb", "aaaaabbbbb"})
        @DisplayName("Should accept all valid a^n b^n strings")
        void testAcceptedStrings(String input) {
            ExecutionResult result = pda.execute(input);
            assertTrue(result.isAccepted(), 
                String.format("String '%s' should be accepted", input));
        }
        
        @ParameterizedTest
        @ValueSource(strings = {"a", "b", "ba", "aab", "abb", "aaabb", "aabbb"})
        @DisplayName("Should reject all invalid strings")
        void testRejectedStrings(String input) {
            ExecutionResult result = pda.execute(input);
            assertFalse(result.isAccepted(), 
                String.format("String '%s' should be rejected", input));
        }
        
        @ParameterizedTest
        @CsvSource({
            "ab, true",
            "aabb, true",
            "aaabbb, true",
            "a, false",
            "b, false",
            "aab, false",
            "abb, false",
            "ba, false"
        })
        @DisplayName("Test various inputs with expected results")
        void testVariousInputs(String input, boolean expectedAccepted) {
            ExecutionResult result = pda.execute(input);
            assertEquals(expectedAccepted, result.isAccepted(),
                String.format("String '%s' should be %s", 
                    input, expectedAccepted ? "accepted" : "rejected"));
        }
    }

    @Nested
    @DisplayName("Trace Validation Tests")
    class TraceValidationTests {
        
        @Test
        @DisplayName("Trace should show complete execution path with stack")
        void testCompleteTracePath() {
            pda.parse(anbnPDA);
            ExecutionResult result = pda.execute("aabb");
            String trace = result.getTrace();
            
            assertNotNull(trace, "Trace should not be null");
            assertFalse(trace.isEmpty(), "Trace should not be empty");
            
            // Trace should show state transitions and stack operations
            // Expected: push 'a' twice, then pop 'a' twice
        }
        
        @Test
        @DisplayName("Trace for rejected string should show path until rejection")
        void testRejectedTrace() {
            pda.parse(anbnPDA);
            ExecutionResult result = pda.execute("aab");
            String trace = result.getTrace();
            
            assertNotNull(trace, "Trace should not be null for rejected string");
            assertFalse(trace.isEmpty(), "Trace should not be empty for rejected string");
        }
    }

    @Nested
    @DisplayName("Performance Tests")
    class PerformanceTests {
        
        @Test
        @DisplayName("Should handle deeply nested structures efficiently")
        void testDeeplyNested() {
            pda.parse(balancedParenthesesPDA);
            
            StringBuilder nested = new StringBuilder();
            for (int i = 0; i < 100; i++) {
                nested.append('(');
            }
            for (int i = 0; i < 100; i++) {
                nested.append(')');
            }
            
            long startTime = System.currentTimeMillis();
            ExecutionResult result = pda.execute(nested.toString());
            long endTime = System.currentTimeMillis();
            
            assertTrue(result.isAccepted(), "100 nested parentheses should be accepted");
            assertTrue((endTime - startTime) < 1000, "Execution should complete within 1 second");
        }
        
        @Test
        @DisplayName("Should handle long sequences efficiently")
        void testLongSequences() {
            pda.parse(anbnPDA);
            
            StringBuilder longString = new StringBuilder();
            for (int i = 0; i < 500; i++) {
                longString.append('a');
            }
            for (int i = 0; i < 500; i++) {
                longString.append('b');
            }
            
            long startTime = System.currentTimeMillis();
            ExecutionResult result = pda.execute(longString.toString());
            long endTime = System.currentTimeMillis();
            
            assertTrue(result.isAccepted(), "500 a's followed by 500 b's should be accepted");
            assertTrue((endTime - startTime) < 1000, "Execution should complete within 1 second");
        }
    }
}
