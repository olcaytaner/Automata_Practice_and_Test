package service;

import java.io.File;

import ContextFreeGrammar.CFG;
import PushDownAutomaton.PDA;
import common.Automaton;
import common.TestFileParser;
import common.TestRunner;
import common.ValidationMessage;
import viewmodel.TestResultViewModel;

/**
 * Service for test execution and discovery.
 * Extracts test-related business logic from UI layer for MVC separation.
 */
public class TestService {

    /**
     * Callback interface for test progress updates.
     */
    public interface TestProgressCallback {
        void onTestStarted(int currentTest, int totalTests, String input);
        void onTestCompleted(int currentTest, int totalTests, String input, boolean passed);
    }

    /**
     * Finds the corresponding test file for an automaton file.
     * Looks for a .test file with the same base name in the same directory.
     *
     * @param automatonFile The automaton file
     * @return The test file, or null if not found
     */
    public File findTestFile(File automatonFile) {
        if (automatonFile == null || !automatonFile.exists()) {
            return null;
        }

        String fileName = automatonFile.getName();
        String baseName;
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            baseName = fileName.substring(0, lastDot);
        } else {
            baseName = fileName;
        }

        File testFile = new File(automatonFile.getParent(), baseName + ".test");
        return testFile.exists() ? testFile : null;
    }

    /**
     * Runs tests from a file against an automaton.
     *
     * @param automaton The automaton to test
     * @param testFilePath Path to the test file
     * @param timeoutMs Total timeout in milliseconds
     * @param callback Optional progress callback
     * @return The test result
     */
    public TestRunner.TestResult runTests(Automaton automaton, String testFilePath,
                                          long timeoutMs, TestProgressCallback callback) {
        if (automaton == null) {
            throw new IllegalArgumentException("Automaton cannot be null");
        }
        if (testFilePath == null || testFilePath.isEmpty()) {
            throw new IllegalArgumentException("Test file path cannot be null or empty");
        }

        // Convert our callback to TestRunner's callback
        TestRunner.TestProgressCallback runnerCallback = null;
        if (callback != null) {
            runnerCallback = new TestRunner.TestProgressCallback() {
                @Override
                public void onTestStarted(int currentTest, int totalTests, String input) {
                    callback.onTestStarted(currentTest, totalTests, input);
                }

                @Override
                public void onTestCompleted(int currentTest, int totalTests, String input, boolean passed) {
                    callback.onTestCompleted(currentTest, totalTests, input, passed);
                }
            };
        }

        return TestRunner.runTests(automaton, testFilePath, timeoutMs, runnerCallback);
    }

    /**
     * Runs tests from a file against an automaton with default timeout.
     *
     * @param automaton The automaton to test
     * @param testFilePath Path to the test file
     * @return The test result
     */
    public TestRunner.TestResult runTests(Automaton automaton, String testFilePath) {
        return runTests(automaton, testFilePath, TestRunner.DEFAULT_TIMEOUT_MS, null);
    }

    /**
     * Runs tests with validation, returning a ViewModel that encapsulates all results.
     * This method combines limit validation and test execution into a single operation,
     * eliminating the need for UI layer to perform instanceof checks.
     *
     * @param automaton The automaton to test
     * @param testFilePath Path to the test file
     * @param settings Test settings containing limits and configuration
     * @param callback Optional progress callback
     * @return TestResultViewModel containing either limit violation or test results
     */
    public TestResultViewModel runTestsWithValidation(
            Automaton automaton,
            String testFilePath,
            SessionService.TestSettings settings,
            TestProgressCallback callback) {

        if (automaton == null) {
            throw new IllegalArgumentException("Automaton cannot be null");
        }
        if (testFilePath == null || testFilePath.isEmpty()) {
            throw new IllegalArgumentException("Test file path cannot be null or empty");
        }
        if (settings == null) {
            throw new IllegalArgumentException("Test settings cannot be null");
        }

        // 1. Check CFG rules limit
        if (automaton instanceof CFG && settings.getMaxRules() != null) {
            CFG cfg = (CFG) automaton;
            ValidationMessage validation = cfg.validateRulesCount(settings.getMaxRules());
            if (validation != null) {
                int actualRules = cfg.getProductions().size();
                return TestResultViewModel.cfgRulesViolation(
                    actualRules,
                    settings.getMaxRules(),
                    settings.getMaxPoints()
                );
            }
        }

        // 2. Check PDA transitions limit
        if (automaton instanceof PDA && settings.getMaxTransitions() != null) {
            PDA pda = (PDA) automaton;
            ValidationMessage validation = pda.validateTransitionsCount(settings.getMaxTransitions());
            if (validation != null) {
                int actualTransitions = pda.getTransitionCount();
                return TestResultViewModel.pdaTransitionsViolation(
                    actualTransitions,
                    settings.getMaxTransitions(),
                    settings.getMaxPoints()
                );
            }
        }

        // 3. Run tests (no limit violations)
        TestRunner.TestResult result = runTests(
            automaton,
            testFilePath,
            settings.getTimeoutMs(),
            callback
        );

        // 4. Return success ViewModel
        return TestResultViewModel.success(
            result,
            settings.getMinPoints(),
            settings.getMaxPoints()
        );
    }

    /**
     * Validates that a CFG doesn't exceed the maximum rules limit.
     *
     * @param automaton The automaton to validate (must be CFG)
     * @param maxRules The maximum allowed rules
     * @return ValidationMessage if limit exceeded, null otherwise
     */
    public ValidationMessage validateCFGRulesLimit(Automaton automaton, Integer maxRules) {
        if (maxRules == null || !(automaton instanceof CFG)) {
            return null;
        }

        CFG cfg = (CFG) automaton;
        return cfg.validateRulesCount(maxRules);
    }

    /**
     * Validates that a PDA doesn't exceed the maximum transitions limit.
     *
     * @param automaton The automaton to validate (must be PDA)
     * @param maxTransitions The maximum allowed transitions
     * @return ValidationMessage if limit exceeded, null otherwise
     */
    public ValidationMessage validatePDATransitionsLimit(Automaton automaton, Integer maxTransitions) {
        if (maxTransitions == null || !(automaton instanceof PDA)) {
            return null;
        }

        PDA pda = (PDA) automaton;
        return pda.validateTransitionsCount(maxTransitions);
    }

    /**
     * Validates limits based on automaton type.
     *
     * @param automaton The automaton to validate
     * @param maxRules Maximum rules for CFG (null if no limit)
     * @param maxTransitions Maximum transitions for PDA (null if no limit)
     * @return ValidationMessage if limit exceeded, null otherwise
     */
    public ValidationMessage validateLimits(Automaton automaton, Integer maxRules, Integer maxTransitions) {
        ValidationMessage cfgValidation = validateCFGRulesLimit(automaton, maxRules);
        if (cfgValidation != null) {
            return cfgValidation;
        }

        return validatePDATransitionsLimit(automaton, maxTransitions);
    }

    /**
     * Formats test results into a human-readable string.
     *
     * @param result The test result
     * @return Formatted test result string
     */
    public String formatTestResults(TestRunner.TestResult result) {
        if (result == null) {
            return "No test results available.";
        }
        return result.getDetailedReport();
    }

    /**
     * Gets a summary of the test results.
     *
     * @param result The test result
     * @return A summary string
     */
    public String getTestSummary(TestRunner.TestResult result) {
        if (result == null) {
            return "No results";
        }
        return String.format("Passed: %d/%d (%.1f%%), Points: %.1f/%d",
                result.getPassedTests(),
                result.getTotalTests(),
                result.getAccuracy(),
                result.getPoints(),
                result.getMaxPoints());
    }

    /**
     * Parses test file settings without running tests.
     *
     * @param testFilePath Path to the test file
     * @return TestFileResult containing settings, or null on error
     */
    public TestFileParser.TestFileResult parseTestFile(String testFilePath) {
        try {
            return TestFileParser.parseTestFile(testFilePath);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Checks if test file exists for the given automaton file.
     *
     * @param automatonFile The automaton file
     * @return true if a corresponding test file exists
     */
    public boolean hasTestFile(File automatonFile) {
        return findTestFile(automatonFile) != null;
    }

    /**
     * Creates a limit violation message for display.
     *
     * @param automaton The automaton that violated the limit
     * @param maxRules The max rules limit (for CFG)
     * @param maxTransitions The max transitions limit (for PDA)
     * @param maxPoints The maximum points
     * @return Formatted violation message, or null if no violation
     */
    public String formatLimitViolation(Automaton automaton, Integer maxRules,
                                       Integer maxTransitions, int maxPoints) {
        if (automaton instanceof CFG && maxRules != null) {
            CFG cfg = (CFG) automaton;
            ValidationMessage validation = cfg.validateRulesCount(maxRules);
            if (validation != null) {
                int actualRules = cfg.getProductions().size();
                return String.format(
                    "CFG RULES LIMIT VIOLATION\n" +
                    "══════════════════════════════════════════════════\n\n" +
                    "Your CFG exceeds the maximum allowed production rules.\n\n" +
                    "Actual rules:    %d\n" +
                    "Maximum allowed: %d\n" +
                    "Exceeded by:     %d\n\n" +
                    "Grade: 0.0/%d points (automatic zero for rules limit violation)\n",
                    actualRules, maxRules,
                    actualRules - maxRules,
                    maxPoints
                );
            }
        }

        if (automaton instanceof PDA && maxTransitions != null) {
            PDA pda = (PDA) automaton;
            ValidationMessage validation = pda.validateTransitionsCount(maxTransitions);
            if (validation != null) {
                int actualTransitions = pda.getTransitionCount();
                return String.format(
                    "PDA TRANSITIONS LIMIT VIOLATION\n" +
                    "══════════════════════════════════════════════════\n\n" +
                    "Your PDA exceeds the maximum allowed transitions.\n\n" +
                    "Actual transitions: %d\n" +
                    "Maximum allowed:    %d\n" +
                    "Exceeded by:        %d\n\n" +
                    "Grade: 0.0/%d points (automatic zero for transitions limit violation)\n",
                    actualTransitions, maxTransitions,
                    actualTransitions - maxTransitions,
                    maxPoints
                );
            }
        }

        return null;
    }
}
