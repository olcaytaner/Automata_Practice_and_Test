package common;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Parser for CSV-format test files.
 * Expected format: inputString,expectedResult
 * Where expectedResult is 1 for accept, 0 for reject.
 *
 * Supports optional headers:
 * #min_points=N
 * #max_points=N
 * #timeout=N (timeout in seconds)
 * #max_regex_length=N (max regex length for REX)
 * #max_rules=N (max production rules for CFG)
 * #max_transitions=N (max transitions for PDA)
 */
public class TestFileParser {

    /**
     * Result of parsing a test file, including test cases and point configuration.
     */
    public static class TestFileResult {
        private final List<TestCase> testCases;
        private final int minPoints;
        private final int maxPoints;
        private final Integer maxRegexLength; // null means no limit
        private final Integer timeout; // null means use default, value is in seconds
        private final Integer maxRules; // null means no limit (for CFG)
        private final Integer maxTransitions; // null means no limit (for PDA)

        public TestFileResult(List<TestCase> testCases, int minPoints, int maxPoints,
                              Integer maxRegexLength, Integer timeout,
                              Integer maxRules, Integer maxTransitions) {
            this.testCases = testCases;
            this.minPoints = minPoints;
            this.maxPoints = maxPoints;
            this.maxRegexLength = maxRegexLength;
            this.timeout = timeout;
            this.maxRules = maxRules;
            this.maxTransitions = maxTransitions;
        }

        public List<TestCase> getTestCases() {
            return testCases;
        }

        public int getMinPoints() {
            return minPoints;
        }

        public int getMaxPoints() {
            return maxPoints;
        }

        public Integer getMaxRegexLength() {
            return maxRegexLength;
        }

        public boolean hasRegexLengthLimit() {
            return maxRegexLength != null;
        }

        public Integer getTimeout() {
            return timeout;
        }

        public boolean hasTimeout() {
            return timeout != null;
        }

        public Integer getMaxRules() {
            return maxRules;
        }

        public boolean hasMaxRules() {
            return maxRules != null;
        }

        public Integer getMaxTransitions() {
            return maxTransitions;
        }

        public boolean hasMaxTransitions() {
            return maxTransitions != null;
        }
    }

    /**
     * Parses a CSV test file and returns test cases with point configuration.
     *
     * @param filePath path to the test file
     * @return TestFileResult containing test cases and point configuration
     * @throws IOException if file cannot be read
     * @throws IllegalArgumentException if file format is invalid
     */
    public static TestFileResult parseTestFile(String filePath) throws IOException {
        List<TestCase> testCases = new ArrayList<>();
        int minPoints = 4;  // Default
        int maxPoints = 10; // Default
        Integer maxRegexLength = null; // null means no limit
        Integer timeout = null; // null means use default (in seconds)
        Integer maxRules = null; // null means no limit (for CFG)
        Integer maxTransitions = null; // null means no limit (for PDA)

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                // Skip empty lines
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                // Parse header lines
                if (line.startsWith("#")) {
                    if (line.startsWith("#min_points=")) {
                        try {
                            minPoints = Integer.parseInt(line.substring("#min_points=".length()).trim());
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException(
                                String.format("Invalid min_points value at line %d: '%s'", lineNumber, line));
                        }
                        continue;
                    } else if (line.startsWith("#max_points=")) {
                        try {
                            maxPoints = Integer.parseInt(line.substring("#max_points=".length()).trim());
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException(
                                String.format("Invalid max_points value at line %d: '%s'", lineNumber, line));
                        }
                        continue;
                    } else if (line.startsWith("#max_regex_length=")) {
                        try {
                            maxRegexLength = Integer.valueOf(line.substring("#max_regex_length=".length()).trim());
                            if (maxRegexLength < 1) {
                                throw new IllegalArgumentException(
                                    String.format("Invalid max_regex_length value at line %d: must be positive", lineNumber));
                            }
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException(
                                String.format("Invalid max_regex_length value at line %d: '%s'", lineNumber, line));
                        }
                        continue;
                    } else if (line.startsWith("#timeout=")) {
                        try {
                            timeout = Integer.valueOf(line.substring("#timeout=".length()).trim());
                            if (timeout < 1) {
                                throw new IllegalArgumentException(
                                    String.format("Invalid timeout value at line %d: must be positive", lineNumber));
                            }
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException(
                                String.format("Invalid timeout value at line %d: '%s'", lineNumber, line));
                        }
                        continue;
                    } else if (line.startsWith("#max_rules=")) {
                        try {
                            maxRules = Integer.valueOf(line.substring("#max_rules=".length()).trim());
                            if (maxRules < 1) {
                                throw new IllegalArgumentException(
                                    String.format("Invalid max_rules value at line %d: must be positive", lineNumber));
                            }
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException(
                                String.format("Invalid max_rules value at line %d: '%s'", lineNumber, line));
                        }
                        continue;
                    } else if (line.startsWith("#max_transitions=")) {
                        try {
                            maxTransitions = Integer.valueOf(line.substring("#max_transitions=".length()).trim());
                            if (maxTransitions < 1) {
                                throw new IllegalArgumentException(
                                    String.format("Invalid max_transitions value at line %d: must be positive", lineNumber));
                            }
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException(
                                String.format("Invalid max_transitions value at line %d: '%s'", lineNumber, line));
                        }
                        continue;
                    } else {
                        // Other comments are ignored
                        continue;
                    }
                }

                // Parse CSV line
                String[] parts = line.split(",", 2);
                if (parts.length != 2) {
                    throw new IllegalArgumentException(
                        String.format("Invalid format at line %d: '%s'. Expected: inputString,expectedResult",
                                    lineNumber, line));
                }

                String input = parts[0].trim();
                String expectedStr = parts[1].trim();

                // Convert expected result to boolean
                boolean shouldAccept;
                if ("1".equals(expectedStr)) {
                    shouldAccept = true;
                } else if ("0".equals(expectedStr)) {
                    shouldAccept = false;
                } else {
                    throw new IllegalArgumentException(
                        String.format("Invalid expected result at line %d: '%s'. Must be 1 (accept) or 0 (reject)",
                                    lineNumber, expectedStr));
                }

                testCases.add(new TestCase(input, shouldAccept));
            }
        }

        testCases.sort(Comparator.comparingInt(tc -> tc.getInput().length()));

        return new TestFileResult(testCases, minPoints, maxPoints, maxRegexLength, timeout, maxRules, maxTransitions);
    }
}