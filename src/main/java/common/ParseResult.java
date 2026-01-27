package common;

import java.util.List;

/**
 * Represents the result of parsing a formal language definition.
 * Contains success status, validation messages, and the parsed object.
 *
 * @version 2.0
 */
public class ParseResult {
    private boolean success;
    private List<ValidationMessage> validationMessages;
    private Automaton automaton;

    /**
     * Creates a new parse result.
     *
     * @param success            Whether parsing was successful
     * @param validationMessages List of validation messages (errors, warnings, info)
     * @param automaton          The parsed Automaton object (null if parsing failed)
     */
    public ParseResult(boolean success, List<ValidationMessage> validationMessages, Automaton automaton) {
        this.success = success;
        this.validationMessages = validationMessages;
        this.automaton = automaton;
    }

    public boolean isSuccess() {
        return success;
    }

    public List<ValidationMessage> getValidationMessages() {
        return validationMessages;
    }

    /**
     * Gets the parsed automaton object.
     *
     * @return The parsed Automaton, or null if parsing failed
     */
    public Automaton getAutomaton() {
        return automaton;
    }

    /**
     * Gets the parsed formal language object.
     * @deprecated Use {@link #getAutomaton()} instead.
     * @return The parsed Automaton, or null if parsing failed
     */
    @Deprecated
    public Automaton getFormalLanguage() {
        return automaton;
    }
}
