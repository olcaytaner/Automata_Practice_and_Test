package grader;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import ContextFreeGrammar.CFG;
import DeterministicFiniteAutomaton.DFA;
import NondeterministicFiniteAutomaton.NFA;
import PushDownAutomaton.PDA;
import RegularExpression.RegularExpression;
import TuringMachine.TM;
import common.Automaton;
import common.ParseResult;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.GraphvizJdkEngine;

/**
 * Exports all student exam results to a single comprehensive PDF.
 * Organized by student, showing all questions with answers and grades.
 */
public class StudentPdfExporter {

    private static final float MARGIN = 50;
    private static final float TITLE_FONT_SIZE = 18;
    private static final float HEADING_FONT_SIZE = 14;
    private static final float SUBHEADING_FONT_SIZE = 12;
    private static final float NORMAL_FONT_SIZE = 10;
    private static final float CODE_FONT_SIZE = 9;
    private static final float LINE_SPACING = 1.2f;

    private static final String[] QUESTION_IDS = {"Q1a", "Q1b", "Q2a", "Q2b", "Q3a", "Q3b"};

    /**
     * Extract student number from folder name (e.g., "ulas_baran_s033428" -> "s033428")
     */
    private static String extractStudentNumber(String studentName) {
        Pattern pattern = Pattern.compile("([sS]\\d{6})");
        Matcher matcher = pattern.matcher(studentName);
        if (matcher.find()) {
            return matcher.group(1).toUpperCase();
        }
        return "UNKNOWN";
    }

    /**
     * Extract display name from folder name (e.g., "ulas_baran_s033428" -> "ulas_baran")
     */
    private static String extractDisplayName(String studentName) {
        String number = extractStudentNumber(studentName);
        if (!number.equals("UNKNOWN")) {
            return studentName.replace("_" + number.toLowerCase(), "")
                             .replace("_" + number.toUpperCase(), "");
        }
        return studentName;
    }

    /**
     * Read answer file content
     */
    private static String readAnswerFile(String studentFolder, String questionId) {
        // Try different extensions
        String[] extensions = {".dfa", ".nfa", ".pda", ".tm", ".cfg", ".rex"};

        for (String ext : extensions) {
            File file = new File(studentFolder, questionId + ext);
            if (file.exists()) {
                try {
                    return new String(Files.readAllBytes(file.toPath()));
                } catch (IOException e) {
                    return "[Error reading file: " + e.getMessage() + "]";
                }
            }
        }

        return "[Answer file not found]";
    }

    /**
     * Truncate string to max length
     */
    private static String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    /**
     * Sanitize text for PDF encoding (remove characters that PDFBox can't handle with standard fonts)
     */
    private static String sanitizeForPdf(String text) {
        if (text == null) return "";
        // Replace newlines and problematic characters with safe alternatives
        return text.replace("\r\n", " ")
                   .replace("\n", " ")
                   .replace("\r", " ")
                   .replace("\t", " ")
                   .replaceAll("[^\u0020-\u007E\u00A0-\u00FF]", "?");
    }

    /**
     * Export PDF for each question with all students' answers, sorted by grade
     */
    public static void exportQuestionPdfs(List<BatchGrader.StudentResult> results, String outputFolder) throws IOException {
        File pdfDir = new File(outputFolder, "question_pdfs");
        pdfDir.mkdirs();

        // Initialize GraphViz engine once
        try {
            GraphvizJdkEngine jdkEngine = new GraphvizJdkEngine();
            Graphviz.useEngine(jdkEngine);
        } catch (Exception e) {
            System.err.println("Warning: Could not initialize GraphViz engine: " + e.getMessage());
        }

        int count = 0;
        for (String questionId : QUESTION_IDS) {
            File pdfFile = new File(pdfDir, questionId + ".pdf");
            exportSingleQuestionPdf(questionId, results, pdfFile.getAbsolutePath());
            count++;
        }

        System.out.println("Generated " + count + " question PDFs in: " + pdfDir.getPath());
    }

    /**
     * Export PDF for a single question with all students' answers
     */
    private static void exportSingleQuestionPdf(String questionId, List<BatchGrader.StudentResult> results, String outputFile) throws IOException {
        // Collect all students' answers for this question
        List<StudentQuestionData> studentAnswers = new ArrayList<>();
        for (BatchGrader.StudentResult result : results) {
            for (ExamGrader.GradingResult gradingResult : result.questions) {
                if (gradingResult.questionId.equals(questionId)) {
                    StudentQuestionData sqd = new StudentQuestionData();
                    sqd.studentNumber = extractStudentNumber(result.studentName);
                    sqd.studentName = extractDisplayName(result.studentName);
                    sqd.studentFolder = result.studentFolder;
                    sqd.questionId = questionId;
                    sqd.score = gradingResult.score != null ? gradingResult.score : 0.0;
                    sqd.maxPoints = gradingResult.maxPoints != null ? gradingResult.maxPoints : 10.0;
                    sqd.success = gradingResult.success;
                    sqd.errorMessage = gradingResult.errorMessage;
                    sqd.totalTests = gradingResult.totalTests;
                    sqd.passedTests = gradingResult.passedTests;
                    sqd.truePositives = gradingResult.truePositives;
                    sqd.trueNegatives = gradingResult.trueNegatives;
                    sqd.falsePositives = gradingResult.falsePositives;
                    sqd.falseNegatives = gradingResult.falseNegatives;
                    sqd.timeoutCount = gradingResult.timeoutCount;
                    sqd.answerContent = readAnswerFile(result.studentFolder, questionId);
                    studentAnswers.add(sqd);
                    break;
                }
            }
        }

        // Sort by grade (descending - highest first)
        Collections.sort(studentAnswers, (a, b) -> Double.compare(b.score, a.score));

        // Create PDF
        PDDocument document = new PDDocument();

        try {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            float yPosition = page.getMediaBox().getHeight() - MARGIN;
            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            // Question header
            contentStream.setNonStrokingColor(0.2f, 0.4f, 0.6f);
            contentStream.addRect(MARGIN, yPosition - 5, page.getMediaBox().getWidth() - 2 * MARGIN, 45);
            contentStream.fill();

            yPosition -= 25;

            contentStream.beginText();
            contentStream.setNonStrokingColor(1f, 1f, 1f);
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, HEADING_FONT_SIZE);
            contentStream.newLineAtOffset(MARGIN + 10, yPosition);
            contentStream.showText(sanitizeForPdf("CS410 Exam - Question " + questionId));
            contentStream.endText();

            yPosition -= 20;

            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, SUBHEADING_FONT_SIZE);
            contentStream.newLineAtOffset(MARGIN + 10, yPosition);
            contentStream.showText(sanitizeForPdf("Total Students: " + studentAnswers.size() + " (sorted by grade)"));
            contentStream.endText();

            contentStream.setNonStrokingColor(0f, 0f, 0f);
            yPosition -= 40;

            // Add each student's answer for this question
            int rank = 1;
            for (StudentQuestionData studentAnswer : studentAnswers) {
                // Check if we need a new page
                if (yPosition < 250) {
                    contentStream.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    yPosition = page.getMediaBox().getHeight() - MARGIN;
                }

                try {
                    yPosition = addStudentAnswerWithDiagram(document, contentStream, page, studentAnswer, yPosition, rank);
                    yPosition -= 20;
                    rank++;
                } catch (Exception e) {
                    System.err.println("Warning: Error adding answer for " + questionId + " student " + studentAnswer.studentNumber + ": " + e.getMessage());
                    yPosition -= 30;
                    rank++;
                }
            }

            contentStream.close();
            document.save(outputFile);

        } finally {
            document.close();
        }
    }

    /**
     * Add a student's answer with automaton diagram to the PDF (for question-based PDFs)
     */
    private static float addStudentAnswerWithDiagram(PDDocument document, PDPageContentStream contentStream,
                                                    PDPage page, StudentQuestionData studentAnswer, float yPosition,
                                                    int rank) throws IOException {
        // Student header with rank
        contentStream.setNonStrokingColor(0.9f, 0.9f, 0.9f);
        contentStream.addRect(MARGIN, yPosition - 5, page.getMediaBox().getWidth() - 2 * MARGIN, 25);
        contentStream.fill();
        contentStream.setNonStrokingColor(0f, 0f, 0f);

        yPosition -= 18;

        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, SUBHEADING_FONT_SIZE);
        contentStream.newLineAtOffset(MARGIN + 5, yPosition);
        contentStream.showText(sanitizeForPdf(String.format("#%d: %s (%s) - Grade: %.1f/%.0f",
            rank, studentAnswer.studentNumber, studentAnswer.studentName, studentAnswer.score, studentAnswer.maxPoints)));
        contentStream.endText();

        yPosition -= 25;

        // Add automaton diagram if parsing succeeded
        if (studentAnswer.success && studentAnswer.answerContent != null && !studentAnswer.answerContent.isEmpty()) {
            byte[] diagramBytes = generateAutomatonDiagram(studentAnswer.studentFolder, studentAnswer.questionId, studentAnswer.answerContent);

            if (diagramBytes != null && diagramBytes.length > 0) {
                try {
                    PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, diagramBytes, "diagram");

                    // Scale image to fit page width
                    float maxWidth = page.getMediaBox().getWidth() - 2 * MARGIN - 10;
                    float maxHeight = 200;
                    float scale = Math.min(maxWidth / pdImage.getWidth(), maxHeight / pdImage.getHeight());
                    float imageWidth = pdImage.getWidth() * scale;
                    float imageHeight = pdImage.getHeight() * scale;

                    // Draw image (page management handled by caller)
                    contentStream.drawImage(pdImage, MARGIN + 5, Math.max(50, yPosition - imageHeight), imageWidth, imageHeight);
                    yPosition -= imageHeight + 10;

                } catch (Exception e) {
                    // If image embedding fails, just show error text
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, NORMAL_FONT_SIZE);
                    contentStream.newLineAtOffset(MARGIN + 5, yPosition);
                    contentStream.showText(sanitizeForPdf("[Diagram generation failed: " + e.getMessage() + "]"));
                    contentStream.endText();
                    yPosition -= 20;
                }
            }
        }

        // Test results summary
        if (studentAnswer.success && studentAnswer.totalTests != null) {
            int tp = studentAnswer.truePositives != null ? studentAnswer.truePositives : 0;
            int tn = studentAnswer.trueNegatives != null ? studentAnswer.trueNegatives : 0;
            int fp = studentAnswer.falsePositives != null ? studentAnswer.falsePositives : 0;
            int fn = studentAnswer.falseNegatives != null ? studentAnswer.falseNegatives : 0;
            int timeouts = studentAnswer.timeoutCount != null ? studentAnswer.timeoutCount : 0;
            int total = studentAnswer.totalTests;

            contentStream.setFont(PDType1Font.HELVETICA, NORMAL_FONT_SIZE);

            // Expected to Accept
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN + 5, yPosition);
            contentStream.showText(sanitizeForPdf(String.format("Expected to Accept: %d", tp)));
            contentStream.endText();
            yPosition -= 15;

            // Expected to Reject
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN + 5, yPosition);
            contentStream.showText(sanitizeForPdf(String.format("Expected to Reject: %d", tn)));
            contentStream.endText();
            yPosition -= 15;

            // Incorrectly Accepted
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN + 5, yPosition);
            contentStream.showText(sanitizeForPdf(String.format("Incorrectly Accepted: %d", fp)));
            contentStream.endText();
            yPosition -= 15;

            // Incorrectly Rejected
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN + 5, yPosition);
            contentStream.showText(sanitizeForPdf(String.format("Incorrectly Rejected: %d", fn)));
            contentStream.endText();
            yPosition -= 15;

            // Timeouts
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN + 5, yPosition);
            contentStream.showText(sanitizeForPdf(String.format("Timeouts: %d", timeouts)));
            contentStream.endText();
            yPosition -= 15;

            // Total Tests
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN + 5, yPosition);
            contentStream.showText(sanitizeForPdf(String.format("Total Tests: %d", total)));
            contentStream.endText();
            yPosition -= 25; // Extra space after stats
        }

        // Error message if any
        if (!studentAnswer.success && studentAnswer.errorMessage != null) {
            contentStream.setNonStrokingColor(0.7f, 0f, 0f);
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, NORMAL_FONT_SIZE);
            contentStream.newLineAtOffset(MARGIN + 5, yPosition);
            contentStream.showText(sanitizeForPdf("Error: " + truncate(studentAnswer.errorMessage, 80)));
            contentStream.endText();
            contentStream.setNonStrokingColor(0f, 0f, 0f);
            yPosition -= 20;
        }

        return yPosition;
    }

    /**
     * Generate automaton diagram as PNG bytes
     */
    private static byte[] generateAutomatonDiagram(String studentFolder, String questionId, String answerContent) {
        try {
            // Determine automaton type from file extension
            Automaton automaton = createAutomatonFromFile(studentFolder, questionId);
            if (automaton == null) {
                return null;
            }

            // Parse the answer content
            ParseResult parseResult = automaton.parse(answerContent);
            if (!parseResult.isSuccess()) {
                return null;
            }

            // Generate DOT code
            String dotCode = parseResult.getAutomaton().toDotCode(answerContent);

            // Render to PNG
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Graphviz.fromString(dotCode)
                    .width(600)
                    .render(Format.PNG)
                    .toOutputStream(outputStream);

            return outputStream.toByteArray();

        } catch (Exception e) {
            System.err.println("Warning: Could not generate diagram for " + questionId + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Create appropriate automaton instance based on actual file extension
     */
    private static Automaton createAutomatonFromFile(String studentFolder, String questionId) {
        // Try all possible extensions to find which file exists
        String[] extensions = {".dfa", ".nfa", ".pda", ".tm", ".cfg", ".rex"};

        for (String ext : extensions) {
            File file = new File(studentFolder, questionId + ext);
            if (file.exists()) {
                // Return appropriate automaton based on file extension
                switch (ext) {
                    case ".dfa":
                        return new DFA();
                    case ".nfa":
                        return new NFA();
                    case ".pda":
                        return new PDA();
                    case ".tm":
                        return new TM();
                    case ".cfg":
                        return new CFG();
                    case ".rex":
                        return new RegularExpression();
                }
            }
        }
        return null; // No matching file found
    }

    /**
     * Student question data container for question-based PDF generation
     */
    private static class StudentQuestionData {
        String studentNumber;
        String studentName;
        String studentFolder;
        String questionId;
        double score;
        double maxPoints;
        boolean success;
        String errorMessage;
        Integer totalTests;
        Integer passedTests;
        Integer truePositives;
        Integer trueNegatives;
        Integer falsePositives;
        Integer falseNegatives;
        Integer timeoutCount;
        String answerContent;
    }
}
