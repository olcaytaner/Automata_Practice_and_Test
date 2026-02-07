package common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import DeterministicFiniteAutomaton.DFA;
import NondeterministicFiniteAutomaton.NFA;
import service.VisualizationService;

/**
 * Tests GraphViz rendering across different Java versions.
 * This test verifies that the GraphvizJdkEngine (GraalVM) works
 * correctly on Java 8, 11, 17, 21, and 24.
 *
 * Uses VisualizationService (service layer) for rendering as per MVC architecture.
 */
@DisplayName("GraphViz Multi-Version Compatibility Tests")
class GraphvizEngineTest {

    private static VisualizationService visualizationService;

    @BeforeAll  
    static void setup() {
        visualizationService = new VisualizationService();

        String separator = generateSeparator(60);
        System.out.println("\n" + separator);
        System.out.println("JAVA ENVIRONMENT INFO");
        System.out.println(separator);
        System.out.println("Java Version: " + System.getProperty("java.version"));
        System.out.println("Java Vendor: " + System.getProperty("java.vendor"));
        System.out.println("Java Home: " + System.getProperty("java.home"));
        System.out.println("OS: " + System.getProperty("os.name") +
                " (" + System.getProperty("os.arch") + ")");
        System.out.println(separator + "\n");
    }

    /**
     * Helper method to generate separator string (Java 8 compatible).
     * String.repeat() was added in Java 11, so we use StringBuilder for compatibility.
     */
    private static String generateSeparator(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append("=");
        }
        return sb.toString();
    }

    @Test
    @DisplayName("DFA GraphViz Rendering - Simple Automaton")
    void testDFASimpleRendering() {
        String dfaInput =
                "states: q0 q1 q2\n" +
                        "alphabet: a b\n" +
                        "start: q0\n" +
                        "accept: q2\n" +
                        "\n" +
                        "transitions:\n" +
                        "q0, a -> q1\n" +
                        "q0, b -> q1\n" +
                        "q1, b -> q2\n" +
                        "q1, a -> q1\n" +
                        "q2, a -> q0\n" +
                        "q2, b -> q2\n";

        DFA dfa = new DFA();

        // Test parsing
        ParseResult parseResult = dfa.parse(dfaInput);
        assertTrue(parseResult.isSuccess(),
                "DFA parsing should succeed");
        assertEquals(0, parseResult.getValidationMessages().stream()
                        .filter(m -> m.getType() == ValidationMessage.ValidationMessageType.ERROR)
                        .count(),
                "Should have no parse errors");

        // Test GraphViz rendering using VisualizationService (MVC-compliant)
        VisualizationService.VisualizationResult result = visualizationService.generateVisualization(dfa, dfaInput);
        assertTrue(result.isSuccess(), "Visualization should succeed");
        assertNotNull(result.getSvgContent(), "Should have SVG content");
        assertFalse(result.getSvgContent().isEmpty(), "SVG content should not be empty");

        // Verify it's valid SVG content
        String svgText = result.getSvgContent();
        assertTrue(svgText.contains("<svg") || svgText.contains("<?xml"),
                "Text should contain SVG markup");
        assertTrue(svgText.contains("</svg>"), "SVG should be properly closed");

        System.out.println("DFA rendering successful - SVG text length: " +
                svgText.length() + " characters");
    }

    @Test
    @DisplayName("NFA GraphViz Rendering - With Epsilon Transitions")
    void testNFAEpsilonRendering() {
        String nfaInput =
                "states: q0 q1 q2 q3\n" +
                        "alphabet: a b\n" +
                        "start: q0\n" +
                        "accept: q3\n" +
                        "transitions:\n" +
                        "q0, a -> q1\n" +
                        "q0, eps -> q1\n" +
                        "q1, b -> q2\n" +
                        "q2, eps -> q3\n" +
                        "q0, a -> q3\n" +
                        "q0, b -> q3\n";

        NFA nfa = new NFA();

        // Test parsing
        ParseResult parseResult = nfa.parse(nfaInput);
        assertTrue(parseResult.isSuccess(),
                "NFA parsing should succeed");

        // Test GraphViz rendering using VisualizationService (MVC-compliant)
        VisualizationService.VisualizationResult result = visualizationService.generateVisualization(nfa, nfaInput);
        assertTrue(result.isSuccess(), "Visualization should succeed");
        assertNotNull(result.getSvgContent(), "Should have SVG content");
        assertFalse(result.getSvgContent().isEmpty(), "SVG content should not be empty");

        // Verify it's valid SVG content
        String svgText = result.getSvgContent();
        assertTrue(svgText.contains("<svg") || svgText.contains("<?xml"),
                "Text should contain SVG markup");
        assertTrue(svgText.contains("</svg>"), "SVG should be properly closed");

        System.out.println("NFA rendering successful - SVG text length: " +
                svgText.length() + " characters");
    }

    @Test
    @DisplayName("Error Case - Invalid Automaton")
    void testInvalidAutomatonRendering() {
        String invalidInput = "this is not valid";

        DFA dfa = new DFA();
        ParseResult parseResult = dfa.parse(invalidInput);

        // Parsing should fail
        assertFalse(parseResult.isSuccess(),
                "Invalid input should fail parsing");

        // VisualizationService should return failure result for invalid input
        VisualizationService.VisualizationResult result = visualizationService.generateVisualization(dfa, invalidInput);
        assertFalse(result.isSuccess(), "Visualization should fail for invalid input");
        assertNotNull(result.getErrorMessage(), "Should have error message");

        System.out.println("Error case handled correctly: " + result.getErrorMessage());
    }
}
