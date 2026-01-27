package grader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import ContextFreeGrammar.CFG;
import DeterministicFiniteAutomaton.DFA;
import NondeterministicFiniteAutomaton.NFA;
import PushDownAutomaton.PDA;
import RegularExpression.RegularExpression;
import TuringMachine.TM;
import common.Automaton;
import common.MachineType;
import common.ParseResult;
import common.TestRunner;
import common.ValidationMessage;

/**
 * Command-line tool for grading individual student exam submissions.
 *
 * Usage: java grader.ExamGrader <student_folder> <question_id>
 * Example: java grader.ExamGrader "exams/CS410 Mock Exam/john_doe_s123456" Q1a
 */
public class ExamGrader {


    /**
     * Result object for JSON serialization
     */
    public static class GradingResult {
        public String studentFolder;
        public String questionId;
        public boolean success;
        public String errorMessage;
        public Double score;
        public Integer minPoints;
        public Integer maxPoints;
        public Integer totalTests;
        public Integer passedTests;
        public Integer truePositives;
        public Integer trueNegatives;
        public Integer falsePositives;
        public Integer falseNegatives;
        public Integer timeoutCount;
        public Double accuracy;
        public Double precision;
        public Double recall;
        public Double f1Score;
        public String detailedReport;
        public Boolean regexLengthViolation;
        public Integer actualRegexLength;
        public Integer maxAllowedRegexLength;
        public Boolean rulesLimitViolation;
        public Integer actualRulesCount;
        public Integer maxAllowedRules;
        public Boolean transitionsLimitViolation;
        public Integer actualTransitionsCount;
        public Integer maxAllowedTransitions;

        public GradingResult(String studentFolder, String questionId) {
            this.studentFolder = studentFolder;
            this.questionId = questionId;
            this.success = false;
            this.regexLengthViolation = false;
            this.rulesLimitViolation = false;
            this.transitionsLimitViolation = false;
        }
    }

    /**
     * Helper class to hold detected file information
     */
    private static class DetectedFile {
        public final File file;
        public final String extension;

        public DetectedFile(File file, String extension) {
            this.file = file;
            this.extension = extension;
        }
    }

    /**
     * Detect which automaton file exists in the student folder for the given question ID.
     * Checks all supported file extensions and returns the first match found.
     *
     * @param studentFolder Path to student's submission folder
     * @param questionId Question ID (e.g., "Q1a", "Q2b")
     * @return DetectedFile containing the file and extension, or null if not found
     */
    private static DetectedFile detectAutomatonFile(String studentFolder, String questionId) {
        String[] extensions = MachineType.getAllExtensions();

        for (String ext : extensions) {
            File file = new File(Paths.get(studentFolder, questionId + ext).toString());
            if (file.exists()) {
                return new DetectedFile(file, ext);
            }
        }

        return null;
    }

    /**
     * Load and parse a formal language from a file based on its extension.
     * Follows the same pattern as the UI application.
     *
     * @param file The automaton file
     * @param extension File extension (e.g., ".dfa", ".nfa", ".rex")
     * @return Successfully parsed Automaton instance
     * @throws IOException if file reading fails
     * @throws IllegalArgumentException if parsing fails or extension is not supported
     */
    private static Automaton loadAutomaton(File file, String extension) throws IOException {
        // Check file size
        long fileSize = file.length();
        if (fileSize == 0) {
            throw new IllegalArgumentException("Empty file (0 bytes): " + file.getAbsolutePath());
        }

        // Minimum file size validation (looser for regex which can be very short)
        int minSize = ".rex".equals(extension) ? 3 : 25;
        if (fileSize < minSize) {
            throw new IllegalArgumentException(String.format(
                "File too small (%d bytes, likely incomplete): %s", fileSize, file.getAbsolutePath()));
        }

        // Read file content
        String content = new String(Files.readAllBytes(file.toPath()));

        // Create automaton instance based on extension (same as UI)
        Automaton automaton;
        switch (extension) {
            case ".dfa":
                automaton = new DFA();
                break;
            case ".nfa":
                automaton = new NFA();
                break;
            case ".pda":
                automaton = new PDA();
                break;
            case ".tm":
                automaton = new TM();
                break;
            case ".cfg":
                automaton = new CFG();
                break;
            case ".rex":
                automaton = new RegularExpression();
                break;
            default:
                throw new IllegalArgumentException("Unsupported file extension: " + extension);
        }

        // Parse the automaton (same as UI: automaton.parse() → parseResult.getAutomaton())
        // InputNormalizer handles format conversion for each type (including .rex)
        ParseResult parseResult = automaton.parse(content);

        if (!parseResult.isSuccess()) {
            StringBuilder errorMsg = new StringBuilder("Parse error:\n");
            for (ValidationMessage msg : parseResult.getValidationMessages()) {
                if (msg.getType() == ValidationMessage.ValidationMessageType.ERROR) {
                    errorMsg.append(msg.toString()).append("\n");
                }
            }
            throw new IllegalArgumentException(errorMsg.toString());
        }

        // Return the successfully parsed automaton
        return parseResult.getAutomaton();
    }

    /**
     * Grade a single question for a student
     * @param studentFolder Path to student's submission folder
     * @param questionId Question ID (e.g., "Q1a", "Q2b")
     * @param testCasesFolder Path to reference test cases folder (use reference tests, not student's tests)
     */
    public static GradingResult gradeQuestion(String studentFolder, String questionId, String testCasesFolder) {
        GradingResult result = new GradingResult(studentFolder, questionId);

        try {
            // Detect which automaton file exists (extension-based detection)
            DetectedFile detected = detectAutomatonFile(studentFolder, questionId);
            if (detected == null) {
                result.errorMessage = String.format(
                    "No automaton file found for question %s in %s\nExpected one of: %s.dfa, %s.nfa, %s.rex, %s.pda, %s.tm, %s.cfg",
                    questionId, studentFolder, questionId, questionId, questionId, questionId, questionId, questionId);
                return result;
            }

            // Build test file path (from reference folder)
            String testFilePath = Paths.get(testCasesFolder, questionId + ".test").toString();
            File testFile = new File(testFilePath);

            if (!testFile.exists()) {
                result.errorMessage = "Test file not found: " + testFilePath;
                return result;
            }

            // Load and parse the automaton (reuses UI logic)
            Automaton automaton = loadAutomaton(detected.file, detected.extension);

            // Run tests to get test configuration (including max regex length)
            TestRunner.TestResult testResult = TestRunner.runTests(automaton, testFilePath);

            // For regex files, check length limit BEFORE awarding any points
            if (".rex".equals(detected.extension) && automaton instanceof RegularExpression) {
                RegularExpression regex = (RegularExpression) automaton;
                Integer maxRegexLength = testResult.getMaxRegexLength();

                if (maxRegexLength != null) {
                    ValidationMessage lengthValidation = regex.validateRegexLength(maxRegexLength);

                    if (lengthValidation != null) {
                        // Length violation - automatic zero points
                        result.success = true; // File was processed successfully
                        result.regexLengthViolation = true;
                        result.actualRegexLength = regex.getSanitizedRegexLength();
                        result.maxAllowedRegexLength = maxRegexLength;
                        result.score = 0.0;
                        result.minPoints = testResult.getMinPoints();
                        result.maxPoints = testResult.getMaxPoints();
                        result.totalTests = testResult.getTotalTests();
                        result.errorMessage = lengthValidation.getMessage();
                        result.detailedReport = String.format(
                            "REGEX LENGTH VIOLATION\n" +
                            "══════════════════════════════════════════════════\n" +
                            "Your regex exceeds the maximum allowed length.\n\n" +
                            "Actual length:  %d characters\n" +
                            "Maximum allowed: %d characters\n" +
                            "Exceeded by:    %d characters\n\n" +
                            "Grade: 0.0/%d points (automatic zero for length violation)\n\n" +
                            "Note: Length is measured after removing whitespace and normalizing 'eps' to 'ε'.\n",
                            result.actualRegexLength, result.maxAllowedRegexLength,
                            result.actualRegexLength - result.maxAllowedRegexLength,
                            result.maxPoints
                        );
                        return result;
                    }
                }
            }

            // For CFG files, check rules limit BEFORE awarding any points
            if (".cfg".equals(detected.extension) && automaton instanceof CFG) {
                CFG cfg = (CFG) automaton;
                Integer maxRules = testResult.getMaxRules();

                if (maxRules != null) {
                    ValidationMessage rulesValidation = cfg.validateRulesCount(maxRules);

                    if (rulesValidation != null) {
                        // Rules limit violation - automatic zero points
                        result.success = true;
                        result.rulesLimitViolation = true;
                        result.actualRulesCount = cfg.getProductions().size();
                        result.maxAllowedRules = maxRules;
                        result.score = 0.0;
                        result.minPoints = testResult.getMinPoints();
                        result.maxPoints = testResult.getMaxPoints();
                        result.totalTests = testResult.getTotalTests();
                        result.errorMessage = rulesValidation.getMessage();
                        result.detailedReport = String.format(
                            "CFG RULES LIMIT VIOLATION\n" +
                            "══════════════════════════════════════════════════\n" +
                            "Your CFG exceeds the maximum allowed production rules.\n\n" +
                            "Actual rules:   %d\n" +
                            "Maximum allowed: %d\n" +
                            "Exceeded by:    %d\n\n" +
                            "Grade: 0.0/%d points (automatic zero for rules limit violation)\n",
                            result.actualRulesCount, result.maxAllowedRules,
                            result.actualRulesCount - result.maxAllowedRules,
                            result.maxPoints
                        );
                        return result;
                    }
                }
            }

            // For PDA files, check transitions limit BEFORE awarding any points
            if (".pda".equals(detected.extension) && automaton instanceof PDA) {
                PDA pda = (PDA) automaton;
                Integer maxTransitions = testResult.getMaxTransitions();

                if (maxTransitions != null) {
                    ValidationMessage transitionsValidation = pda.validateTransitionsCount(maxTransitions);

                    if (transitionsValidation != null) {
                        // Transitions limit violation - automatic zero points
                        result.success = true;
                        result.transitionsLimitViolation = true;
                        result.actualTransitionsCount = pda.getTransitionCount();
                        result.maxAllowedTransitions = maxTransitions;
                        result.score = 0.0;
                        result.minPoints = testResult.getMinPoints();
                        result.maxPoints = testResult.getMaxPoints();
                        result.totalTests = testResult.getTotalTests();
                        result.errorMessage = transitionsValidation.getMessage();
                        result.detailedReport = String.format(
                            "PDA TRANSITIONS LIMIT VIOLATION\n" +
                            "══════════════════════════════════════════════════\n" +
                            "Your PDA exceeds the maximum allowed transitions.\n\n" +
                            "Actual transitions: %d\n" +
                            "Maximum allowed:    %d\n" +
                            "Exceeded by:        %d\n\n" +
                            "Grade: 0.0/%d points (automatic zero for transitions limit violation)\n",
                            result.actualTransitionsCount, result.maxAllowedTransitions,
                            result.actualTransitionsCount - result.maxAllowedTransitions,
                            result.maxPoints
                        );
                        return result;
                    }
                }
            }

            // Populate result with test results (no limit violations)
            result.success = true;
            result.score = testResult.getPoints();
            result.minPoints = testResult.getMinPoints();
            result.maxPoints = testResult.getMaxPoints();
            result.totalTests = testResult.getTotalTests();
            result.passedTests = testResult.getPassedTests();
            result.truePositives = testResult.getTruePositives();
            result.trueNegatives = testResult.getTrueNegatives();
            result.falsePositives = testResult.getFalsePositives();
            result.falseNegatives = testResult.getFalseNegatives();
            result.timeoutCount = testResult.getTimeoutCount();
            result.accuracy = testResult.getAccuracy();
            result.precision = testResult.getPrecision();
            result.recall = testResult.getRecall();
            result.f1Score = testResult.getF1Score();
            result.detailedReport = testResult.getDetailedReport();

        } catch (IOException e) {
            result.errorMessage = "IO Error: " + e.getMessage();
        } catch (Exception e) {
            result.errorMessage = "Error: " + e.getClass().getSimpleName() + ": " + e.getMessage();
            e.printStackTrace(System.err);
        }

        return result;
    }

}
