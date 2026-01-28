package viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import common.TestRunner;

/**
 * Unit tests for TestResultViewModel.
 * Verifies factory methods, getters, and computed properties.
 */
@DisplayName("TestResultViewModel Tests")
class TestResultViewModelTest {

    @Nested
    @DisplayName("Success Factory Method")
    class SuccessTests {

        @Test
        @DisplayName("should create success ViewModel with correct data")
        void testSuccessCreation() {
            // Create a TestRunner.TestResult using setters
            TestRunner.TestResult result = createTestResult(8, 2, 10, 0);

            TestResultViewModel vm = TestResultViewModel.success(result, 0, 100);

            assertFalse(vm.hasLimitViolation());
            assertEquals(8, vm.getPassedTests());
            assertEquals(2, vm.getFailedTests());
            assertEquals(10, vm.getTotalTests());
            assertEquals(0, vm.getTimeoutCount());
            assertEquals(0, vm.getMinPoints());
            assertEquals(100, vm.getMaxPoints());
            assertNull(vm.getLimitViolationType());
            assertNull(vm.getLimitViolationMessage());
        }

        @Test
        @DisplayName("should return correct dialog title for success")
        void testSuccessDialogTitle() {
            TestRunner.TestResult result = createTestResult(10, 0, 10, 0);
            TestResultViewModel vm = TestResultViewModel.success(result, 0, 100);

            assertEquals("Test Results", vm.getDialogTitle());
        }

        @Test
        @DisplayName("should detect timeouts correctly")
        void testTimeoutDetection() {
            TestRunner.TestResult result = createTestResult(5, 3, 10, 2);
            TestResultViewModel vm = TestResultViewModel.success(result, 0, 100);

            assertTrue(vm.hasTimeouts());
            assertEquals(2, vm.getTimeoutCount());
        }

        @Test
        @DisplayName("should return correct summary for success")
        void testSuccessSummary() {
            TestRunner.TestResult result = createTestResult(8, 2, 10, 0);
            TestResultViewModel vm = TestResultViewModel.success(result, 0, 100);

            String summary = vm.getSummary();
            assertTrue(summary.contains("Passed"));
            assertTrue(summary.contains("8/10"));
        }
    }

    @Nested
    @DisplayName("CFG Rules Violation Factory Method")
    class CfgViolationTests {

        @Test
        @DisplayName("should create CFG rules violation ViewModel correctly")
        void testCfgRulesViolation() {
            TestResultViewModel vm = TestResultViewModel.cfgRulesViolation(15, 10, 100);

            assertTrue(vm.hasLimitViolation());
            assertEquals("CFG_RULES", vm.getLimitViolationType());
            assertEquals(15, vm.getActualCount());
            assertEquals(10, vm.getAllowedCount());
            assertEquals(100, vm.getMaxPoints());
            assertEquals(0, vm.getPassedTests());
            assertEquals(0.0, vm.getEarnedPoints());
        }

        @Test
        @DisplayName("should include correct message for CFG rules violation")
        void testCfgViolationMessage() {
            TestResultViewModel vm = TestResultViewModel.cfgRulesViolation(15, 10, 100);

            String message = vm.getLimitViolationMessage();
            assertNotNull(message);
            assertTrue(message.contains("CFG RULES LIMIT VIOLATION"));
            assertTrue(message.contains("15"));
            assertTrue(message.contains("10"));
            assertTrue(message.contains("5")); // exceeded by
        }

        @Test
        @DisplayName("should return correct dialog title for CFG violation")
        void testCfgViolationDialogTitle() {
            TestResultViewModel vm = TestResultViewModel.cfgRulesViolation(15, 10, 100);

            assertEquals("Rules Limit Violation", vm.getDialogTitle());
        }

        @Test
        @DisplayName("should return correct summary for CFG violation")
        void testCfgViolationSummary() {
            TestResultViewModel vm = TestResultViewModel.cfgRulesViolation(15, 10, 100);

            String summary = vm.getSummary();
            assertTrue(summary.contains("Limit Violation"));
            assertTrue(summary.contains("0.0/100"));
        }
    }

    @Nested
    @DisplayName("PDA Transitions Violation Factory Method")
    class PdaViolationTests {

        @Test
        @DisplayName("should create PDA transitions violation ViewModel correctly")
        void testPdaTransitionsViolation() {
            TestResultViewModel vm = TestResultViewModel.pdaTransitionsViolation(25, 20, 50);

            assertTrue(vm.hasLimitViolation());
            assertEquals("PDA_TRANSITIONS", vm.getLimitViolationType());
            assertEquals(25, vm.getActualCount());
            assertEquals(20, vm.getAllowedCount());
            assertEquals(50, vm.getMaxPoints());
            assertEquals(0, vm.getPassedTests());
            assertEquals(0.0, vm.getEarnedPoints());
        }

        @Test
        @DisplayName("should include correct message for PDA transitions violation")
        void testPdaViolationMessage() {
            TestResultViewModel vm = TestResultViewModel.pdaTransitionsViolation(25, 20, 50);

            String message = vm.getLimitViolationMessage();
            assertNotNull(message);
            assertTrue(message.contains("PDA TRANSITIONS LIMIT VIOLATION"));
            assertTrue(message.contains("25"));
            assertTrue(message.contains("20"));
            assertTrue(message.contains("5")); // exceeded by
        }

        @Test
        @DisplayName("should return correct dialog title for PDA violation")
        void testPdaViolationDialogTitle() {
            TestResultViewModel vm = TestResultViewModel.pdaTransitionsViolation(25, 20, 50);

            assertEquals("Transitions Limit Violation", vm.getDialogTitle());
        }
    }

    @Nested
    @DisplayName("Generic Limit Violation Factory Method")
    class GenericViolationTests {

        @Test
        @DisplayName("should create generic limit violation correctly")
        void testGenericLimitViolation() {
            TestResultViewModel vm = TestResultViewModel.limitViolation(
                "CUSTOM_TYPE", "Custom violation message", 30, 25, 75);

            assertTrue(vm.hasLimitViolation());
            assertEquals("CUSTOM_TYPE", vm.getLimitViolationType());
            assertEquals("Custom violation message", vm.getLimitViolationMessage());
            assertEquals(30, vm.getActualCount());
            assertEquals(25, vm.getAllowedCount());
            assertEquals(75, vm.getMaxPoints());
        }

        @Test
        @DisplayName("should return generic dialog title for unknown type")
        void testGenericViolationDialogTitle() {
            TestResultViewModel vm = TestResultViewModel.limitViolation(
                "UNKNOWN_TYPE", "Some message", 30, 25, 75);

            assertEquals("Limit Violation", vm.getDialogTitle());
        }
    }

    /**
     * Helper method to create TestRunner.TestResult with specific values.
     * Uses the actual TestRunner.TestResult class and its setters.
     */
    private TestRunner.TestResult createTestResult(int passed, int failed, int total, int timeouts) {
        TestRunner.TestResult result = new TestRunner.TestResult();
        result.setTotalTests(total);
        result.setPassedTests(passed);

        // Set classification metrics to match passed/failed counts
        // For simplicity: passed = true positives, failed = false negatives
        for (int i = 0; i < passed; i++) {
            result.incrementTruePositives();
        }
        for (int i = 0; i < failed; i++) {
            result.incrementFalseNegatives();
        }
        for (int i = 0; i < timeouts; i++) {
            result.incrementTimeoutCount();
        }

        return result;
    }
}
