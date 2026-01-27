package RegularExpression;

import java.util.Collections;
import java.util.Set;

/**
 * Represents a leaf node in a regular expression syntax tree.
 * <p>
 * A {@code RegexLeafNode} corresponds to a single symbol from the
 * regular expression's alphabet (for example, 'a' or '1').
 * It has no children and matches exactly one occurrence of its symbol
 * in the input string.
 * </p>
 * Renamed from LeafNode for clarity.
 *
 * @version 2.0
 */
public class RegexLeafNode extends RegexNode {

    public RegexLeafNode(char sym) {
        super(sym);
    }

    @Override
    public Set<Integer> match(String s, int pos) {
        if (sym == 'ε')
            return Collections.singleton(pos);
        if (pos < s.length() && s.charAt(pos) == sym)
            return Collections.singleton(pos + 1);
        return Collections.emptySet();
    }

    @Override
    public String generateOneCase() {
        return String.valueOf(sym);
    }

    @Override
    public String generateOneCase(int maxStarRepeat) {
        return String.valueOf(sym);
    }

    @Override
    public Set<String> generateCasesExhaustive(int maxLen) {
        if (sym == 'ε')
            return Collections.emptySet();
        if (maxLen >= 1)
            return Collections.singleton(String.valueOf(sym));
        return Collections.emptySet();
    }
}
