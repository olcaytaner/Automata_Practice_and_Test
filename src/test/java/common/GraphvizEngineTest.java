package common;

import javax.swing.JLabel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import DeterministicFiniteAutomaton.DFA;
import NondeterministicFiniteAutomaton.NFA;

/**
 * Tests GraphViz rendering across different Java versions.
 * This test verifies that the GraphvizJdkEngine (GraalVM) works
 * correctly on Java 8, 11, 17, 21, and 24.
 */
@DisplayName("GraphViz Multi-Version Compatibility Tests")
class GraphvizEngineTest {

    @BeforeAll
    static void printJavaInfo() {
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
                        "finals: q2\n" +
                        "transitions:\n" +
                        "q0 -> q1 (a)\n" +
                        "q0 -> q1 (b)\n" +
                        "q1 -> q2 (b)\n" +
                        "q1 -> q1 (a)\n" +
                        "q2 -> q0 (a)\n" +
                        "q2 -> q2 (b)\n";

        DFA dfa = new DFA();

        // Test parsing
        ParseResult parseResult = dfa.parse(dfaInput);
        assertTrue(parseResult.isSuccess(),
                "DFA parsing should succeed");
        assertEquals(0, parseResult.getValidationMessages().stream()
                        .filter(m -> m.getType() == ValidationMessage.ValidationMessageType.ERROR)
                        .count(),
                "Should have no parse errors");

        // Test GraphViz rendering - now returns SVG text in JLabel
        JLabel result = dfa.toGraphviz(dfaInput);
        assertNotNull(result, "GraphViz should return a JLabel");
        assertNotNull(result.getText(), "JLabel should contain SVG text");
        assertFalse(result.getText().isEmpty(), "SVG text should not be empty");

        // Verify it's valid SVG content
        String svgText = result.getText();
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
                        "finals: q3\n" +
                        "transitions:\n" +
                        "q0 -> q1 (a eps)\n" +
                        "q1 -> q2 (b)\n" +
                        "q2 -> q3 (eps)\n" +
                        "q0 -> q3 (a b)\n";

        NFA nfa = new NFA();

        // Test parsing
        ParseResult parseResult = nfa.parse(nfaInput);
        assertTrue(parseResult.isSuccess(),
                "NFA parsing should succeed");

        // Test GraphViz rendering - now returns SVG text in JLabel
        JLabel result = nfa.toGraphviz(nfaInput);
        assertNotNull(result, "GraphViz should return a JLabel");
        assertNotNull(result.getText(), "JLabel should contain SVG text");
        assertFalse(result.getText().isEmpty(), "SVG text should not be empty");

        // Verify it's valid SVG content
        String svgText = result.getText();
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

        // GraphViz should still return a label (with error message)
        JLabel result = dfa.toGraphviz(invalidInput);
        assertNotNull(result, "Should return error label");

        System.out.println("Error case handled correctly");
    }
}
