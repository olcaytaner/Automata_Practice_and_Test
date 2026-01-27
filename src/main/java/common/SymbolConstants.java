package common;

/**
 * Centralized constants for special symbols used across all automata.
 *
 * <p>This class provides a single source of truth for epsilon and blank symbols
 * used in DFA, NFA, PDA, Turing Machines, CFG, and Regular Expressions.</p>
 *
 * <h3>Symbol Representations:</h3>
 * <ul>
 *   <li><strong>User Input</strong>: What users type in definition files (e.g., "eps")</li>
 *   <li><strong>Internal</strong>: Character representation used internally (e.g., '_')</li>
 *   <li><strong>Display</strong>: Unicode representation for visualization (e.g., "ε")</li>
 * </ul>
 *
 * <h3>Usage Example:</h3>
 * <pre>{@code
 * import static common.SymbolConstants.*;
 *
 * // Check if user input is epsilon
 * if (isEpsilonInput(userString)) {
 *     Symbol eps = new Symbol(EPSILON_CHAR);
 * }
 *
 * // Display epsilon in visualization
 * String label = symbol.isEpsilon() ? EPSILON_DISPLAY : symbol.toString();
 * }</pre>
 */
public final class SymbolConstants {

    private SymbolConstants() {
        // Prevent instantiation
    }

    // ═══════════════════════════════════════════════════════════════════════
    // EPSILON - Empty string/transition symbol
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * What users type in definition files for epsilon.
     * Users type "eps" since ε is hard to type on a standard keyboard.
     */
    public static final String EPSILON_INPUT = "eps";

    /**
     * Internal character representation for epsilon.
     * Used in Symbol objects and internal processing.
     */
    public static final char EPSILON_CHAR = '_';

    /**
     * Unicode epsilon for display/visualization.
     * Used in DOT code generation and UI display.
     */
    public static final String EPSILON_DISPLAY = "\u03B5"; // ε

    // ═══════════════════════════════════════════════════════════════════════
    // TURING MACHINE - Blank symbol for tape cells
    // ═══════════════════════════════════════════════════════════════════════

    /** What users type for blank tape symbol. */
    public static final String BLANK_INPUT = "_";

    /** Internal character for blank tape cells. */
    public static final char BLANK_CHAR = '_';

    /** Display representation for blank (open box symbol). */
    public static final String BLANK_DISPLAY = "\u2423"; // ␣

    // ═══════════════════════════════════════════════════════════════════════
    // REGULAR EXPRESSION - Operators
    // ═══════════════════════════════════════════════════════════════════════

    /** Union operator (a|b) - user types 'u'. */
    public static final char REGEX_OR = 'u';

    /** Concatenation operator (ab). */
    public static final char REGEX_CONCAT = '.';

    /** Kleene star operator (a*). */
    public static final char REGEX_STAR = '*';

    // ═══════════════════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Check if input string represents epsilon.
     * Case-insensitive comparison with the epsilon input string.
     *
     * @param s the string to check
     * @return true if the string represents epsilon
     */
    public static boolean isEpsilonInput(String s) {
        return s != null && EPSILON_INPUT.equalsIgnoreCase(s);
    }

    /**
     * Check if character is the internal epsilon representation.
     *
     * @param c the character to check
     * @return true if the character is epsilon
     */
    public static boolean isEpsilonChar(char c) {
        return c == EPSILON_CHAR;
    }

    /**
     * Check if character is blank (for Turing Machine tape).
     *
     * @param c the character to check
     * @return true if the character is blank
     */
    public static boolean isBlankChar(char c) {
        return c == BLANK_CHAR;
    }

    /**
     * Check if string represents a blank/epsilon push in PDA.
     * Used for stack operations where "eps" or "_" means no push.
     *
     * @param s the string to check
     * @return true if the string represents no push operation
     */
    public static boolean isNoPush(String s) {
        return s == null || s.isEmpty() || isEpsilonInput(s);
    }
}
