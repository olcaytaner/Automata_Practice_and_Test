package RegularExpression;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import static RegularExpression.RegexOperator.CONCAT;
import static RegularExpression.RegexOperator.OR;
import static RegularExpression.RegexOperator.STAR;
import static common.SymbolConstants.*;

/**
 * Internal syntax tree representation for regular expressions.
 * This class handles parsing and compilation of regex patterns into an AST.
 * <p>
 * Package-private - use {@link RegularExpression} as the public API.
 * </p>
 * Renamed from SyntaxTree and no longer extends Automaton.
 *
 * @version 2.0
 */
class RegexSyntaxTree {
    private final Map<Character, Integer> precedence;

    {
        precedence = new HashMap<>();
        precedence.put(STAR, 3);
        precedence.put(CONCAT, 2);
        precedence.put(OR, 1);
    }

    char[] alphabet;
    RegexNode root;
    private String sanitizedRegex;

    RegexSyntaxTree() {
        this.alphabet = new char[0];
    }

    RegexSyntaxTree(String regex, char[] alphabet) {
        this.alphabet = alphabet;
        String sanitizedReg = sanitize(regex);
        this.sanitizedRegex = sanitizedReg;
        String postfix = shuntingYard(sanitizedReg);
        compile(postfix);
    }

    /**
     * Checks for malformations that may occur in the regular expression text.
     */
    String sanitize(String regex) {
        regex = regex.replaceAll("\\s+", ""); // delete whitespace from input
        regex = regex.replace(EPSILON_INPUT, EPSILON_DISPLAY); // This will make things much easier
        StringBuilder sanitized = new StringBuilder();
        int parenthesisCount = 0;
        for (char c : regex.toCharArray()) {
            if ((c == '(' || alphabetHas(c)) && sanitized.length() > 0) {
                char prev = sanitized.charAt(sanitized.length() - 1);
                if (prev == ')' || alphabetHas(prev) || prev == STAR)
                    sanitized.append(CONCAT);
            }
            sanitized.append(c);

            if (c == '(') parenthesisCount++;
            else if (c == ')') parenthesisCount--;

            if (parenthesisCount < 0)
                throw new IllegalArgumentException("Unbalanced parenthesis");

            if (!alphabetHas(c) && !precedence.containsKey(c) && c != '(' && c != ')')
                throw new IllegalArgumentException("Invalid character in regex: " + c);

        }
        if (parenthesisCount != 0)
            throw new IllegalArgumentException("Unbalanced parenthesis");
        return sanitized.toString();
    }

    /**
     * Applies the shunting yard algorithm to convert the regex into its postfix representation.
     */
    String shuntingYard(String regex) {
        StringBuilder postfix = new StringBuilder();
        Deque<Character> stk = new ArrayDeque<>();
        for (char c : regex.toCharArray()) {
            if (alphabetHas(c)) {
                postfix.append(c);
                continue;
            }
            if (c == '(') {
                stk.push(c);
                continue;
            }
            if (c == ')') {
                while (stk.peek() != '(')
                    postfix.append(stk.pop());
                stk.pop();
                continue;
            }
            while (!stk.isEmpty() && stk.peek() != '(' && precedence.get(stk.peek()) >= precedence.get(c))
                postfix.append(stk.pop());
            stk.push(c);
        }
        while (!stk.isEmpty())
            postfix.append(stk.pop());
        return postfix.toString();
    }

    /**
     * Builds the AST of the regular expression using its postfix representation.
     */
    void compile(String postfix) {
        Deque<RegexNode> stk = new ArrayDeque<>();
        RegexNode r, l;
        for (char c : postfix.toCharArray()) {
            if (alphabetHas(c)) {
                stk.push(new RegexLeafNode(c));
            } else {
                switch (c) {
                    case STAR:
                        stk.push(new RegexStarNode(stk.pop()));
                        break;
                    case CONCAT:
                        r = stk.pop();
                        l = stk.pop();
                        stk.push(new RegexConcatNode(l, r));
                        break;
                    case OR:
                        r = stk.pop();
                        l = stk.pop();
                        stk.push(new RegexOrNode(l, r));
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + c);
                }
            }
        }
        if (stk.size() != 1)
            throw new IllegalArgumentException("Malformed postfix: " + postfix + ", \ncheck regex");
        root = stk.pop();
    }

    /**
     * Matches the input string against the regex pattern.
     *
     * @param input The string to match
     * @return true if the string matches the pattern
     */
    boolean match(String input) {
        if (root == null) return false;
        return root.match(input, 0).contains(input.length());
    }

    /**
     * Utility method to check whether the alphabet has a certain char.
     */
    boolean alphabetHas(char c) {
        if (c == EPSILON_DISPLAY.charAt(0))
            return true;
        for (char ch : alphabet)
            if (ch == c)
                return true;
        return false;
    }

    /**
     * Gets the length of the sanitized regex.
     *
     * @return Length of sanitized regex, or 0 if not parsed
     */
    int getSanitizedRegexLength() {
        return sanitizedRegex != null ? sanitizedRegex.length() : 0;
    }

    /**
     * Gets the sanitized regex string.
     *
     * @return The sanitized regex or null if not parsed
     */
    String getSanitizedRegex() {
        return sanitizedRegex;
    }
}
