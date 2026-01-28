package service;

import java.util.List;

import ContextFreeGrammar.CFG;
import DeterministicFiniteAutomaton.DFA;
import NondeterministicFiniteAutomaton.NFA;
import PushDownAutomaton.PDA;
import RegularExpression.RegularExpression;
import TuringMachine.TM;
import common.Automaton;
import common.ExecutionResult;
import common.MachineType;
import common.ParseResult;
import common.ValidationMessage;

/**
 * Service for automaton operations: parsing, execution, and validation.
 * Extracts business logic from UI layer for MVC separation.
 */
public class AutomatonService {

    /**
     * Parses the input text for the given automaton.
     *
     * @param automaton The automaton to parse
     * @param inputText The input text to parse
     * @return ParseResult containing success status and any error messages
     */
    public ParseResult parse(Automaton automaton, String inputText) {
        if (automaton == null) {
            throw new IllegalArgumentException("Automaton cannot be null");
        }
        automaton.setInputText(inputText);
        return automaton.parse(inputText);
    }

    /**
     * Executes the automaton on the given input string.
     *
     * @param automaton The automaton to execute
     * @param input The input string to process
     * @return ExecutionResult containing acceptance status and execution trace
     */
    public ExecutionResult execute(Automaton automaton, String input) {
        if (automaton == null) {
            throw new IllegalArgumentException("Automaton cannot be null");
        }
        return automaton.execute(input);
    }

    /**
     * Validates the automaton definition.
     *
     * @param automaton The automaton to validate
     * @param inputText The input text to validate
     * @return List of validation messages (errors, warnings, info)
     */
    public List<ValidationMessage> validate(Automaton automaton, String inputText) {
        if (automaton == null) {
            throw new IllegalArgumentException("Automaton cannot be null");
        }
        automaton.setInputText(inputText);
        return automaton.validate();
    }

    /**
     * Formats validation messages into a human-readable string.
     *
     * @param messages List of validation messages
     * @return Formatted string representation
     */
    public String formatValidationMessages(List<ValidationMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return "No warnings or errors found!";
        }

        StringBuilder result = new StringBuilder();
        for (ValidationMessage msg : messages) {
            result.append(msg.toString()).append("\n");
        }
        return result.toString();
    }

    /**
     * Creates a new automaton of the specified type.
     *
     * @param type The type of automaton to create
     * @return A new automaton instance
     */
    public Automaton createAutomaton(MachineType type) {
        if (type == null) {
            throw new IllegalArgumentException("MachineType cannot be null");
        }

        switch (type) {
            case DFA:
                return new DFA();
            case NFA:
                return new NFA();
            case PDA:
                return new PDA();
            case TM:
                return new TM();
            case CFG:
                return new CFG();
            case REGEX:
                return new RegularExpression();
            default:
                throw new IllegalArgumentException("Unknown machine type: " + type);
        }
    }

    /**
     * Gets the default template for an automaton type.
     *
     * @param type The type of automaton
     * @return The default template string
     */
    public String getDefaultTemplate(MachineType type) {
        Automaton automaton = createAutomaton(type);
        return automaton.getDefaultTemplate();
    }

    /**
     * Checks if the automaton has been successfully parsed.
     *
     * @param automaton The automaton to check
     * @param inputText The input text to parse
     * @return true if parsing was successful
     */
    public boolean isParsed(Automaton automaton, String inputText) {
        ParseResult result = parse(automaton, inputText);
        return result.isSuccess();
    }

    /**
     * Gets all validation messages categorized by severity.
     *
     * @param messages List of validation messages
     * @return ValidationSummary containing categorized messages
     */
    public ValidationSummary categorizeMessages(List<ValidationMessage> messages) {
        int errors = 0;
        int warnings = 0;
        int info = 0;

        if (messages != null) {
            for (ValidationMessage msg : messages) {
                switch (msg.getType()) {
                    case ERROR:
                        errors++;
                        break;
                    case WARNING:
                        warnings++;
                        break;
                    case INFO:
                        info++;
                        break;
                }
            }
        }

        return new ValidationSummary(errors, warnings, info, messages);
    }

    /**
     * Summary of validation messages by severity level.
     */
    public static class ValidationSummary {
        private final int errorCount;
        private final int warningCount;
        private final int infoCount;
        private final List<ValidationMessage> messages;

        public ValidationSummary(int errorCount, int warningCount, int infoCount,
                                List<ValidationMessage> messages) {
            this.errorCount = errorCount;
            this.warningCount = warningCount;
            this.infoCount = infoCount;
            this.messages = messages;
        }

        public int getErrorCount() { return errorCount; }
        public int getWarningCount() { return warningCount; }
        public int getInfoCount() { return infoCount; }
        public List<ValidationMessage> getMessages() { return messages; }
        public boolean hasErrors() { return errorCount > 0; }
        public boolean hasWarnings() { return warningCount > 0; }
        public boolean isClean() { return errorCount == 0 && warningCount == 0; }
    }
}
