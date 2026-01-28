package viewmodel;

import common.TestRunner;

/**
 * ViewModel for test results that encapsulates all data needed by the UI.
 * Eliminates the need for UI layer to perform instanceof checks or access model directly.
 */
public class TestResultViewModel {

    // Test result data
    private final int passedTests;
    private final int failedTests;
    private final int totalTests;
    private final int timeoutCount;
    private final double accuracy;

    // Points configuration
    private final int minPoints;
    private final int maxPoints;
    private final double earnedPoints;

    // Limit violation data
    private final boolean hasLimitViolation;
    private final String limitViolationType;    // "CFG_RULES" | "PDA_TRANSITIONS" | null
    private final String limitViolationMessage;
    private final Integer actualCount;
    private final Integer allowedCount;

    // Detailed report
    private final String detailedReport;

    /**
     * Private constructor - use factory methods.
     */
    private TestResultViewModel(int passedTests, int failedTests, int totalTests, int timeoutCount,
                                 double accuracy, int minPoints, int maxPoints, double earnedPoints,
                                 boolean hasLimitViolation, String limitViolationType,
                                 String limitViolationMessage, Integer actualCount, Integer allowedCount,
                                 String detailedReport) {
        this.passedTests = passedTests;
        this.failedTests = failedTests;
        this.totalTests = totalTests;
        this.timeoutCount = timeoutCount;
        this.accuracy = accuracy;
        this.minPoints = minPoints;
        this.maxPoints = maxPoints;
        this.earnedPoints = earnedPoints;
        this.hasLimitViolation = hasLimitViolation;
        this.limitViolationType = limitViolationType;
        this.limitViolationMessage = limitViolationMessage;
        this.actualCount = actualCount;
        this.allowedCount = allowedCount;
        this.detailedReport = detailedReport;
    }

    /**
     * Factory method for successful test execution.
     */
    public static TestResultViewModel success(TestRunner.TestResult result, int minPoints, int maxPoints) {
        result.setMinPoints(minPoints);
        result.setMaxPoints(maxPoints);

        return new TestResultViewModel(
            result.getPassedTests(),
            result.getFailedTests(),
            result.getTotalTests(),
            result.getTimeoutCount(),
            result.getAccuracy(),
            minPoints,
            maxPoints,
            result.getPoints(),
            false,  // no limit violation
            null,
            null,
            null,
            null,
            result.getDetailedReport()
        );
    }

    /**
     * Factory method for CFG rules limit violation.
     */
    public static TestResultViewModel cfgRulesViolation(int actualRules, int maxRules, int maxPoints) {
        String message = String.format(
            "CFG RULES LIMIT VIOLATION\n" +
            "══════════════════════════════════════════════════\n\n" +
            "Your CFG exceeds the maximum allowed production rules.\n\n" +
            "Actual rules:    %d\n" +
            "Maximum allowed: %d\n" +
            "Exceeded by:     %d\n\n" +
            "Grade: 0.0/%d points (automatic zero for rules limit violation)\n",
            actualRules, maxRules, actualRules - maxRules, maxPoints
        );

        return new TestResultViewModel(
            0, 0, 0, 0,  // no tests run
            0.0,
            0, maxPoints, 0.0,
            true,  // has limit violation
            "CFG_RULES",
            message,
            actualRules,
            maxRules,
            null  // no detailed report
        );
    }

    /**
     * Factory method for PDA transitions limit violation.
     */
    public static TestResultViewModel pdaTransitionsViolation(int actualTransitions, int maxTransitions, int maxPoints) {
        String message = String.format(
            "PDA TRANSITIONS LIMIT VIOLATION\n" +
            "══════════════════════════════════════════════════\n\n" +
            "Your PDA exceeds the maximum allowed transitions.\n\n" +
            "Actual transitions: %d\n" +
            "Maximum allowed:    %d\n" +
            "Exceeded by:        %d\n\n" +
            "Grade: 0.0/%d points (automatic zero for transitions limit violation)\n",
            actualTransitions, maxTransitions, actualTransitions - maxTransitions, maxPoints
        );

        return new TestResultViewModel(
            0, 0, 0, 0,  // no tests run
            0.0,
            0, maxPoints, 0.0,
            true,  // has limit violation
            "PDA_TRANSITIONS",
            message,
            actualTransitions,
            maxTransitions,
            null  // no detailed report
        );
    }

    /**
     * Factory method for generic limit violation.
     */
    public static TestResultViewModel limitViolation(String type, String message,
                                                      int actualCount, int allowedCount, int maxPoints) {
        return new TestResultViewModel(
            0, 0, 0, 0,
            0.0,
            0, maxPoints, 0.0,
            true,
            type,
            message,
            actualCount,
            allowedCount,
            null
        );
    }

    // ═══════════════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════════════

    public int getPassedTests() { return passedTests; }
    public int getFailedTests() { return failedTests; }
    public int getTotalTests() { return totalTests; }
    public int getTimeoutCount() { return timeoutCount; }
    public double getAccuracy() { return accuracy; }

    public int getMinPoints() { return minPoints; }
    public int getMaxPoints() { return maxPoints; }
    public double getEarnedPoints() { return earnedPoints; }

    public boolean hasLimitViolation() { return hasLimitViolation; }
    public String getLimitViolationType() { return limitViolationType; }
    public String getLimitViolationMessage() { return limitViolationMessage; }
    public Integer getActualCount() { return actualCount; }
    public Integer getAllowedCount() { return allowedCount; }

    public String getDetailedReport() { return detailedReport; }

    public boolean hasTimeouts() { return timeoutCount > 0; }

    /**
     * Returns a summary suitable for display in UI.
     */
    public String getSummary() {
        if (hasLimitViolation) {
            return String.format("Limit Violation: 0.0/%d points", maxPoints);
        }
        return String.format("Passed: %d/%d (%.1f%%), Points: %.1f/%d",
            passedTests, totalTests, accuracy, earnedPoints, maxPoints);
    }

    /**
     * Returns the dialog title based on result type.
     */
    public String getDialogTitle() {
        if (hasLimitViolation) {
            if ("CFG_RULES".equals(limitViolationType)) {
                return "Rules Limit Violation";
            } else if ("PDA_TRANSITIONS".equals(limitViolationType)) {
                return "Transitions Limit Violation";
            }
            return "Limit Violation";
        }
        return "Test Results";
    }
}
