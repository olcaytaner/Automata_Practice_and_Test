package service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import DeterministicFiniteAutomaton.DFA;
import RegularExpression.RegularExpression;
import common.MachineType;

/**
 * Unit tests for VisualizationService.
 */
@DisplayName("VisualizationService Tests")
class VisualizationServiceTest {

    private VisualizationService service;
    private AutomatonService automatonService;

    // Valid DFA definition for testing
    private static final String VALID_DFA =
        "states: q0 q1\n" +
        "alphabet: 0 1\n" +
        "start: q0\n" +
        "accept: q1\n" +
        "\n" +
        "transitions:\n" +
        "q0, 0 -> q1\n" +
        "q0, 1 -> q0\n" +
        "q1, 0 -> q1\n" +
        "q1, 1 -> q0";

    @BeforeEach
    void setUp() {
        service = new VisualizationService();
        automatonService = new AutomatonService();
    }

    // ═══════════════════════════════════════════════════════════════════
    // supportsVisualization Tests
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("supportsVisualization returns true for DFA")
    void supportsVisualization_DFA() {
        assertTrue(service.supportsVisualization(MachineType.DFA));
    }

    @Test
    @DisplayName("supportsVisualization returns true for NFA")
    void supportsVisualization_NFA() {
        assertTrue(service.supportsVisualization(MachineType.NFA));
    }

    @Test
    @DisplayName("supportsVisualization returns true for PDA")
    void supportsVisualization_PDA() {
        assertTrue(service.supportsVisualization(MachineType.PDA));
    }

    @Test
    @DisplayName("supportsVisualization returns true for TM")
    void supportsVisualization_TM() {
        assertTrue(service.supportsVisualization(MachineType.TM));
    }

    @Test
    @DisplayName("supportsVisualization returns true for CFG")
    void supportsVisualization_CFG() {
        assertTrue(service.supportsVisualization(MachineType.CFG));
    }

    @Test
    @DisplayName("supportsVisualization returns false for REGEX")
    void supportsVisualization_REGEX() {
        assertFalse(service.supportsVisualization(MachineType.REGEX));
    }

    @Test
    @DisplayName("supportsVisualization returns false for null")
    void supportsVisualization_null() {
        assertFalse(service.supportsVisualization(null));
    }

    // ═══════════════════════════════════════════════════════════════════
    // generateVisualization Tests
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("generateVisualization returns failure for null automaton")
    void generateVisualization_nullAutomaton() {
        VisualizationService.VisualizationResult result = service.generateVisualization(null, "test");

        assertFalse(result.isSuccess());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("null"));
    }

    @Test
    @DisplayName("generateVisualization returns failure for unsupported type")
    void generateVisualization_unsupportedType() {
        RegularExpression regex = new RegularExpression();

        VisualizationService.VisualizationResult result = service.generateVisualization(regex, "a*");

        assertFalse(result.isSuccess());
        assertNotNull(result.getErrorMessage());
        assertTrue(result.getErrorMessage().contains("not supported"));
    }

    @Test
    @DisplayName("generateVisualization returns failure for invalid input")
    void generateVisualization_invalidInput() {
        DFA dfa = new DFA();

        VisualizationService.VisualizationResult result = service.generateVisualization(dfa, "invalid input");

        assertFalse(result.isSuccess());
        assertNotNull(result.getErrorMessage());
    }

    @Test
    @DisplayName("generateVisualization returns success for valid DFA")
    void generateVisualization_validDFA() {
        DFA dfa = new DFA();

        VisualizationService.VisualizationResult result = service.generateVisualization(dfa, VALID_DFA);

        assertTrue(result.isSuccess());
        assertNotNull(result.getSvgContent());
        assertNotNull(result.getDotCode());
        assertNull(result.getErrorMessage());
        assertTrue(result.getSvgContent().contains("<svg"));
    }

    // ═══════════════════════════════════════════════════════════════════
    // generateDotCode Tests
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("generateDotCode returns null for null automaton")
    void generateDotCode_nullAutomaton() {
        assertNull(service.generateDotCode(null, "test"));
    }

    @Test
    @DisplayName("generateDotCode returns null for invalid input")
    void generateDotCode_invalidInput() {
        DFA dfa = new DFA();
        assertNull(service.generateDotCode(dfa, "invalid"));
    }

    @Test
    @DisplayName("generateDotCode returns DOT code for valid DFA")
    void generateDotCode_validDFA() {
        DFA dfa = new DFA();

        String dotCode = service.generateDotCode(dfa, VALID_DFA);

        assertNotNull(dotCode);
        assertTrue(dotCode.contains("digraph"));
        assertTrue(dotCode.contains("q0"));
        assertTrue(dotCode.contains("q1"));
    }

    // ═══════════════════════════════════════════════════════════════════
    // generateDotCodeWithHighlight Tests
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("generateDotCodeWithHighlight returns null for null automaton")
    void generateDotCodeWithHighlight_nullAutomaton() {
        assertNull(service.generateDotCodeWithHighlight(null, "test", "0"));
    }

    @Test
    @DisplayName("generateDotCodeWithHighlight returns DOT code for valid DFA")
    void generateDotCodeWithHighlight_validDFA() {
        DFA dfa = new DFA();

        String dotCode = service.generateDotCodeWithHighlight(dfa, VALID_DFA, "01");

        assertNotNull(dotCode);
        assertTrue(dotCode.contains("digraph"));
    }

    @Test
    @DisplayName("generateDotCodeWithHighlight handles null highlight input")
    void generateDotCodeWithHighlight_nullHighlight() {
        DFA dfa = new DFA();

        String dotCode = service.generateDotCodeWithHighlight(dfa, VALID_DFA, null);

        assertNotNull(dotCode);
    }

    // ═══════════════════════════════════════════════════════════════════
    // renderToSvg Tests
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("renderToSvg throws for null input")
    void renderToSvg_nullInput() {
        assertThrows(IllegalArgumentException.class, () -> service.renderToSvg(null));
    }

    @Test
    @DisplayName("renderToSvg throws for empty input")
    void renderToSvg_emptyInput() {
        assertThrows(IllegalArgumentException.class, () -> service.renderToSvg(""));
    }

    @Test
    @DisplayName("renderToSvg throws for whitespace-only input")
    void renderToSvg_whitespaceInput() {
        assertThrows(IllegalArgumentException.class, () -> service.renderToSvg("   "));
    }

    @Test
    @DisplayName("renderToSvg renders simple graph")
    void renderToSvg_simpleGraph() throws Exception {
        String dotCode = "digraph { a -> b }";

        String svg = service.renderToSvg(dotCode);

        assertNotNull(svg);
        assertTrue(svg.contains("<svg"));
        assertTrue(svg.contains("</svg>"));
    }

    // ═══════════════════════════════════════════════════════════════════
    // validateVisualization Tests
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("validateVisualization returns error for null automaton")
    void validateVisualization_nullAutomaton() {
        String error = service.validateVisualization(null, "test");

        assertNotNull(error);
        assertTrue(error.contains("null"));
    }

    @Test
    @DisplayName("validateVisualization returns error for unsupported type")
    void validateVisualization_unsupportedType() {
        RegularExpression regex = new RegularExpression();

        String error = service.validateVisualization(regex, "a*");

        assertNotNull(error);
        assertTrue(error.contains("not supported"));
    }

    @Test
    @DisplayName("validateVisualization returns error for invalid input")
    void validateVisualization_invalidInput() {
        DFA dfa = new DFA();

        String error = service.validateVisualization(dfa, "invalid");

        assertNotNull(error);
    }

    @Test
    @DisplayName("validateVisualization returns null for valid input")
    void validateVisualization_validInput() {
        DFA dfa = new DFA();

        String error = service.validateVisualization(dfa, VALID_DFA);

        assertNull(error);
    }

    // ═══════════════════════════════════════════════════════════════════
    // VisualizationResult Tests
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("VisualizationResult.success creates successful result")
    void visualizationResult_success() {
        VisualizationService.VisualizationResult result =
            VisualizationService.VisualizationResult.success("<svg></svg>", "digraph {}");

        assertTrue(result.isSuccess());
        assertEquals("<svg></svg>", result.getSvgContent());
        assertEquals("digraph {}", result.getDotCode());
        assertNull(result.getErrorMessage());
    }

    @Test
    @DisplayName("VisualizationResult.failure creates failed result")
    void visualizationResult_failure() {
        VisualizationService.VisualizationResult result =
            VisualizationService.VisualizationResult.failure("Error message");

        assertFalse(result.isSuccess());
        assertNull(result.getSvgContent());
        assertNull(result.getDotCode());
        assertEquals("Error message", result.getErrorMessage());
    }

    @Test
    @DisplayName("VisualizationResult.failure with dotCode creates partial result")
    void visualizationResult_failureWithDotCode() {
        VisualizationService.VisualizationResult result =
            VisualizationService.VisualizationResult.failure("Error message", "digraph {}");

        assertFalse(result.isSuccess());
        assertNull(result.getSvgContent());
        assertEquals("digraph {}", result.getDotCode());
        assertEquals("Error message", result.getErrorMessage());
    }
}
