package common;

/**
 * Represents a validation message with type, content, and line number.
 * Used to communicate parsing errors, warnings, and informational messages.
 *
 * @version 1.0
 */
public class ValidationMessage {

    /**
     * Types of validation messages.
     */
    public enum ValidationMessageType {
        ERROR,
        WARNING,
        INFO
    }

    private String message;
    private int lineNumber;
    private ValidationMessageType type;

    /**
     * Creates a new validation message.
     *
     * @param message    The message text
     * @param lineNumber The line number where the issue was found (-1 if not applicable)
     * @param type       The type of message (ERROR, WARNING, INFO)
     */
    public ValidationMessage(String message, int lineNumber, ValidationMessageType type) {
        this.message = message;
        this.lineNumber = lineNumber;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public ValidationMessageType getType() {
        return type;
    }

    @Override
    public String toString() {
        return String.format("%s: %s in line %d", type, message, lineNumber);
    }
}
