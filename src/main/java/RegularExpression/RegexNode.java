package RegularExpression;

import java.util.Set;

/**
 * Abstract base class for regex syntax tree nodes.
 * Renamed from SyntaxTreeNode for clarity.
 *
 * @version 2.0
 */
public abstract class RegexNode {
    public char sym;

    public RegexNode(char sym) {
        this.sym = sym;
    }

    /**
     * Recursive method that performs a DFS on the AST of the regular expression.
     *
     * @param s   The input string to match
     * @param pos The current position in the string
     * @return Set of possible end positions after matching
     */
    public abstract Set<Integer> match(String s, int pos);

    /**
     * Generates a single random string matching this node's pattern.
     *
     * @return A random matching string
     */
    public abstract String generateOneCase();

    /**
     * Generates a single random string matching this node's pattern with controlled star repetition.
     *
     * @param maxStarRepeat Maximum number of repetitions for Kleene star
     * @return A random matching string
     */
    public abstract String generateOneCase(int maxStarRepeat);

    /**
     * Generates all possible strings matching this node's pattern up to maxLen.
     *
     * @param maxLen Maximum length of generated strings
     * @return Set of all matching strings up to maxLen
     */
    public abstract Set<String> generateCasesExhaustive(int maxLen);
}
