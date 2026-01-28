package service;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ContextFreeGrammar.CFG;
import DeterministicFiniteAutomaton.DFA;
import NondeterministicFiniteAutomaton.NFA;
import PushDownAutomaton.PDA;
import RegularExpression.RegularExpression;
import TuringMachine.TM;
import common.Automaton;
import common.MachineType;
import common.ParseResult;
import common.ValidationMessage;

/**
 * Unit tests for AutomatonService.
 */
@DisplayName("AutomatonService Tests")
class AutomatonServiceTest {

    private AutomatonService service;

    @BeforeEach
    void setUp() {
        service = new AutomatonService();
    }

    // ═══════════════════════════════════════════════════════════════════
    // createAutomaton Tests
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("createAutomaton creates DFA for DFA type")
    void createAutomaton_DFA() {
        Automaton automaton = service.createAutomaton(MachineType.DFA);
        assertNotNull(automaton);
        assertInstanceOf(DFA.class, automaton);
        assertEquals(MachineType.DFA, automaton.getMachineType());
    }

    @Test
    @DisplayName("createAutomaton creates NFA for NFA type")
    void createAutomaton_NFA() {
        Automaton automaton = service.createAutomaton(MachineType.NFA);
        assertNotNull(automaton);
        assertInstanceOf(NFA.class, automaton);
        assertEquals(MachineType.NFA, automaton.getMachineType());
    }

    @Test
    @DisplayName("createAutomaton creates PDA for PDA type")
    void createAutomaton_PDA() {
        Automaton automaton = service.createAutomaton(MachineType.PDA);
        assertNotNull(automaton);
        assertInstanceOf(PDA.class, automaton);
        assertEquals(MachineType.PDA, automaton.getMachineType());
    }

    @Test
    @DisplayName("createAutomaton creates TM for TM type")
    void createAutomaton_TM() {
        Automaton automaton = service.createAutomaton(MachineType.TM);
        assertNotNull(automaton);
        assertInstanceOf(TM.class, automaton);
        assertEquals(MachineType.TM, automaton.getMachineType());
    }

    @Test
    @DisplayName("createAutomaton creates CFG for CFG type")
    void createAutomaton_CFG() {
        Automaton automaton = service.createAutomaton(MachineType.CFG);
        assertNotNull(automaton);
        assertInstanceOf(CFG.class, automaton);
        assertEquals(MachineType.CFG, automaton.getMachineType());
    }

    @Test
    @DisplayName("createAutomaton creates RegularExpression for REGEX type")
    void createAutomaton_REGEX() {
        Automaton automaton = service.createAutomaton(MachineType.REGEX);
        assertNotNull(automaton);
        assertInstanceOf(RegularExpression.class, automaton);
        assertEquals(MachineType.REGEX, automaton.getMachineType());
    }

    @Test
    @DisplayName("createAutomaton throws for null type")
    void createAutomaton_nullThrows() {
        assertThrows(IllegalArgumentException.class, () -> service.createAutomaton(null));
    }

    // ═══════════════════════════════════════════════════════════════════
    // parse Tests
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("parse returns result for valid DFA")
    void parse_validDFA() {
        String validDFA =
            "Start: q0\n" +
            "Finals: q1\n" +
            "Alphabet: 0 1\n" +
            "States: q0 q1\n" +
            "\n" +
            "Transitions:\n" +
            "q0 -> q1 (0)\n" +
            "q0 -> q0 (1)\n" +
            "q1 -> q1 (0)\n" +
            "q1 -> q0 (1)";

        Automaton dfa = service.createAutomaton(MachineType.DFA);
        ParseResult result = service.parse(dfa, validDFA);

        assertNotNull(result);
        assertTrue(result.isSuccess());
    }

    @Test
    @DisplayName("parse throws for null automaton")
    void parse_nullAutomatonThrows() {
        assertThrows(IllegalArgumentException.class, () -> service.parse(null, "test"));
    }

    // ═══════════════════════════════════════════════════════════════════
    // validate Tests
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("validate returns messages list")
    void validate_returnsList() {
        Automaton dfa = service.createAutomaton(MachineType.DFA);
        List<ValidationMessage> messages = service.validate(dfa, "");

        assertNotNull(messages);
    }

    @Test
    @DisplayName("validate throws for null automaton")
    void validate_nullAutomatonThrows() {
        assertThrows(IllegalArgumentException.class, () -> service.validate(null, "test"));
    }

    // ═══════════════════════════════════════════════════════════════════
    // formatValidationMessages Tests
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("formatValidationMessages returns 'No warnings' for empty list")
    void formatValidationMessages_emptyList() {
        String result = service.formatValidationMessages(new ArrayList<>());
        assertEquals("No warnings or errors found!", result);
    }

    @Test
    @DisplayName("formatValidationMessages returns 'No warnings' for null list")
    void formatValidationMessages_nullList() {
        String result = service.formatValidationMessages(null);
        assertEquals("No warnings or errors found!", result);
    }

    @Test
    @DisplayName("formatValidationMessages formats multiple messages")
    void formatValidationMessages_multipleMessages() {
        List<ValidationMessage> messages = new ArrayList<>();
        messages.add(new ValidationMessage("Error 1", 1, ValidationMessage.ValidationMessageType.ERROR));
        messages.add(new ValidationMessage("Warning 1", 2, ValidationMessage.ValidationMessageType.WARNING));

        String result = service.formatValidationMessages(messages);

        assertNotNull(result);
        assertTrue(result.contains("Error 1"));
        assertTrue(result.contains("Warning 1"));
    }

    // ═══════════════════════════════════════════════════════════════════
    // getDefaultTemplate Tests
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getDefaultTemplate returns non-empty template for each type")
    void getDefaultTemplate_allTypes() {
        for (MachineType type : MachineType.values()) {
            String template = service.getDefaultTemplate(type);
            assertNotNull(template, "Template should not be null for " + type);
            assertFalse(template.isEmpty(), "Template should not be empty for " + type);
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // categorizeMessages Tests
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("categorizeMessages counts errors correctly")
    void categorizeMessages_countsErrors() {
        List<ValidationMessage> messages = new ArrayList<>();
        messages.add(new ValidationMessage("Error 1", 1, ValidationMessage.ValidationMessageType.ERROR));
        messages.add(new ValidationMessage("Error 2", 2, ValidationMessage.ValidationMessageType.ERROR));
        messages.add(new ValidationMessage("Warning 1", 3, ValidationMessage.ValidationMessageType.WARNING));

        AutomatonService.ValidationSummary summary = service.categorizeMessages(messages);

        assertEquals(2, summary.getErrorCount());
        assertEquals(1, summary.getWarningCount());
        assertEquals(0, summary.getInfoCount());
        assertTrue(summary.hasErrors());
        assertTrue(summary.hasWarnings());
        assertFalse(summary.isClean());
    }

    @Test
    @DisplayName("categorizeMessages returns clean for empty list")
    void categorizeMessages_cleanForEmpty() {
        AutomatonService.ValidationSummary summary = service.categorizeMessages(new ArrayList<>());

        assertEquals(0, summary.getErrorCount());
        assertEquals(0, summary.getWarningCount());
        assertEquals(0, summary.getInfoCount());
        assertFalse(summary.hasErrors());
        assertFalse(summary.hasWarnings());
        assertTrue(summary.isClean());
    }

    @Test
    @DisplayName("categorizeMessages handles null list")
    void categorizeMessages_handlesNull() {
        AutomatonService.ValidationSummary summary = service.categorizeMessages(null);

        assertEquals(0, summary.getErrorCount());
        assertTrue(summary.isClean());
    }

    // ═══════════════════════════════════════════════════════════════════
    // isParsed Tests
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("isParsed returns true for valid automaton")
    void isParsed_validAutomaton() {
        String validDFA =
            "Start: q0\n" +
            "Finals: q1\n" +
            "Alphabet: 0 1\n" +
            "States: q0 q1\n" +
            "\n" +
            "Transitions:\n" +
            "q0 -> q1 (0)\n" +
            "q0 -> q0 (1)\n" +
            "q1 -> q1 (0)\n" +
            "q1 -> q0 (1)";

        Automaton dfa = service.createAutomaton(MachineType.DFA);
        assertTrue(service.isParsed(dfa, validDFA));
    }

    @Test
    @DisplayName("isParsed returns false for invalid automaton")
    void isParsed_invalidAutomaton() {
        Automaton dfa = service.createAutomaton(MachineType.DFA);
        assertFalse(service.isParsed(dfa, "invalid input"));
    }
}
