package common;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TestRunner.TestResult.calculatePoints().
 */
public class TestRunnerPointsTest {

    private static TestRunner.TestResult result(int tp, int tn, int fp, int fn) {
        TestRunner.TestResult r = new TestRunner.TestResult();
        for (int i = 0; i < tp; i++) r.incrementTruePositives();
        for (int i = 0; i < tn; i++) r.incrementTrueNegatives();
        for (int i = 0; i < fp; i++) r.incrementFalsePositives();
        for (int i = 0; i < fn; i++) r.incrementFalseNegatives();
        return r;
    }

    @Test
    void testRejectsEveryStringGetsZero() {
        assertEquals(0.0, result(0, 5, 0, 4).calculatePoints());
    }

    @Test
    void testAcceptsEveryStringGetsZero() {
        assertEquals(0.0, result(4, 0, 5, 0).calculatePoints());
    }

    @Test
    void testAcceptsEveryStringWithoutStringsToRejectIsPerfect() {
        assertEquals(10.0, result(4, 0, 0, 0).calculatePoints());
    }

    @Test
    void testSingleRequiredStringAllCorrectIsPerfect() {
        assertEquals(10.0, result(1, 5, 0, 0).calculatePoints());
    }

    @Test
    void testSingleRequiredStringRejectedGetsZero() {
        assertEquals(0.0, result(0, 5, 0, 1).calculatePoints());
    }

    @Test
    void testNoStringsToAcceptGetsZero() {
        assertEquals(0.0, result(0, 5, 0, 0).calculatePoints());
    }

    @Test
    void testPartialAcceptsWithoutFalsePositives() {
        // log2(2) * (10 - 4) / log2(4) + 4 = 7.0
        assertEquals(7.0, result(2, 3, 0, 2).calculatePoints());
    }

    @Test
    void testFalsePositivesUsePenaltyFormula() {
        // 4 / log10(1 + 1 + 10) = 3.7
        assertEquals(3.7, result(3, 1, 1, 1).calculatePoints());
    }
}
