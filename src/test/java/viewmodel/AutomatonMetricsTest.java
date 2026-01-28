package viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for AutomatonMetrics ViewModel.
 * Verifies factory methods, getters, and type detection.
 */
@DisplayName("AutomatonMetrics Tests")
class AutomatonMetricsTest {

    @Nested
    @DisplayName("CFG Metrics")
    class CfgMetricsTests {

        @Test
        @DisplayName("should create CFG metrics with rule count")
        void testCfgMetrics() {
            AutomatonMetrics metrics = AutomatonMetrics.forCFG(15);

            assertTrue(metrics.isCFG());
            assertEquals(15, metrics.getRuleCount());
            assertTrue(metrics.hasRuleCount());
            assertNull(metrics.getTransitionCount());
            assertFalse(metrics.hasTransitionCount());
            assertNull(metrics.getStateCount());
            assertFalse(metrics.hasStateCount());
            assertEquals("CFG", metrics.getAutomatonType());
        }

        @Test
        @DisplayName("should generate correct summary for CFG")
        void testCfgSummary() {
            AutomatonMetrics metrics = AutomatonMetrics.forCFG(10);

            String summary = metrics.getSummary();
            assertTrue(summary.contains("CFG"));
            assertTrue(summary.contains("10 rules"));
        }
    }

    @Nested
    @DisplayName("PDA Metrics")
    class PdaMetricsTests {

        @Test
        @DisplayName("should create PDA metrics with transitions and states")
        void testPdaMetrics() {
            AutomatonMetrics metrics = AutomatonMetrics.forPDA(20, 5);

            assertTrue(metrics.isPDA());
            assertEquals(20, metrics.getTransitionCount());
            assertTrue(metrics.hasTransitionCount());
            assertEquals(5, metrics.getStateCount());
            assertTrue(metrics.hasStateCount());
            assertNull(metrics.getRuleCount());
            assertFalse(metrics.hasRuleCount());
            assertEquals("PDA", metrics.getAutomatonType());
        }

        @Test
        @DisplayName("should generate correct summary for PDA")
        void testPdaSummary() {
            AutomatonMetrics metrics = AutomatonMetrics.forPDA(20, 5);

            String summary = metrics.getSummary();
            assertTrue(summary.contains("PDA"));
            assertTrue(summary.contains("20 transitions"));
            assertTrue(summary.contains("5 states"));
        }
    }

    @Nested
    @DisplayName("DFA Metrics")
    class DfaMetricsTests {

        @Test
        @DisplayName("should create DFA metrics correctly")
        void testDfaMetrics() {
            AutomatonMetrics metrics = AutomatonMetrics.forDFA(12, 4);

            assertTrue(metrics.isDFA());
            assertEquals(12, metrics.getTransitionCount());
            assertEquals(4, metrics.getStateCount());
            assertEquals("DFA", metrics.getAutomatonType());
        }

        @Test
        @DisplayName("should generate correct summary for DFA")
        void testDfaSummary() {
            AutomatonMetrics metrics = AutomatonMetrics.forDFA(12, 4);

            String summary = metrics.getSummary();
            assertTrue(summary.contains("DFA"));
            assertTrue(summary.contains("12 transitions"));
            assertTrue(summary.contains("4 states"));
        }
    }

    @Nested
    @DisplayName("NFA Metrics")
    class NfaMetricsTests {

        @Test
        @DisplayName("should create NFA metrics correctly")
        void testNfaMetrics() {
            AutomatonMetrics metrics = AutomatonMetrics.forNFA(18, 6);

            assertTrue(metrics.isNFA());
            assertEquals(18, metrics.getTransitionCount());
            assertEquals(6, metrics.getStateCount());
            assertEquals("NFA", metrics.getAutomatonType());
        }
    }

    @Nested
    @DisplayName("TM Metrics")
    class TmMetricsTests {

        @Test
        @DisplayName("should create TM metrics correctly")
        void testTmMetrics() {
            AutomatonMetrics metrics = AutomatonMetrics.forTM(25, 8);

            assertTrue(metrics.isTM());
            assertEquals(25, metrics.getTransitionCount());
            assertEquals(8, metrics.getStateCount());
            assertEquals("TM", metrics.getAutomatonType());
        }
    }

    @Nested
    @DisplayName("Empty Metrics")
    class EmptyMetricsTests {

        @Test
        @DisplayName("should create empty metrics with all nulls")
        void testEmptyMetrics() {
            AutomatonMetrics metrics = AutomatonMetrics.empty();

            assertFalse(metrics.isCFG());
            assertFalse(metrics.isPDA());
            assertFalse(metrics.isDFA());
            assertFalse(metrics.isNFA());
            assertFalse(metrics.isTM());
            assertNull(metrics.getRuleCount());
            assertNull(metrics.getTransitionCount());
            assertNull(metrics.getStateCount());
            assertNull(metrics.getAutomatonType());
        }

        @Test
        @DisplayName("should generate empty summary for empty metrics")
        void testEmptySummary() {
            AutomatonMetrics metrics = AutomatonMetrics.empty();

            String summary = metrics.getSummary();
            assertTrue(summary.isEmpty());
        }
    }

    @Nested
    @DisplayName("Type Detection")
    class TypeDetectionTests {

        @Test
        @DisplayName("should correctly identify CFG type")
        void testCfgTypeDetection() {
            AutomatonMetrics metrics = AutomatonMetrics.forCFG(10);

            assertTrue(metrics.isCFG());
            assertFalse(metrics.isPDA());
            assertFalse(metrics.isDFA());
            assertFalse(metrics.isNFA());
            assertFalse(metrics.isTM());
        }

        @Test
        @DisplayName("should correctly identify PDA type")
        void testPdaTypeDetection() {
            AutomatonMetrics metrics = AutomatonMetrics.forPDA(10, 5);

            assertFalse(metrics.isCFG());
            assertTrue(metrics.isPDA());
            assertFalse(metrics.isDFA());
            assertFalse(metrics.isNFA());
            assertFalse(metrics.isTM());
        }
    }
}
