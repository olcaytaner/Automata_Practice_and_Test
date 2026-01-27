package RegularExpression.SyntaxTree;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Represents the Kleene star (<code>{@value RegularExpression.SyntaxTree.RegexOperator#STAR}</code>)
 * operator in a regular expression syntax tree.
 * <p>
 * The Kleene star applies to a single child node, matching zero or more
 * repetitions of that pattern. For example, in the regex
 * <code>(10){@value RegularExpression.SyntaxTree.RegexOperator#STAR}</code>,
 * the sequence <code>10</code> is the child of the
 * <code>{@value RegularExpression.SyntaxTree.RegexOperator#STAR}</code> operator.
 * </p>
 */
public class StarNode extends UnaryNode {

    private static final int DEFAULT_MAX_REPEAT = 4;

    public StarNode(SyntaxTreeNode child) {
        super(child);
    }

    @Override
    public Set<Integer> match(String s, int pos) {
        Set<Integer> res = new HashSet<>();
        Deque<Integer> stack = new ArrayDeque<>();

        res.add(pos); // because * allows 0 repetitions
        stack.push(pos);

        while (!stack.isEmpty()) {
            int p = stack.pop();
            Set<Integer> nextEnds = child.match(s, p);
            for (int nxt : nextEnds) {
                if (!res.contains(nxt)) {
                    res.add(nxt);
                    stack.push(nxt);
                }
            }
        }
        return res;
    }

    @Override
    public String generateOneCase() {
        int repeat = ThreadLocalRandom.current().nextInt(DEFAULT_MAX_REPEAT);
        String base = child.generateOneCase();
        if (repeat == 0 || base.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(base.length() * repeat);
        for (int i = 0; i < repeat; i++) {
            sb.append(base);
        }
        return sb.toString();
    }

    @Override
    public String generateOneCase(int maxStarRepeat) {
        int repeat = ThreadLocalRandom.current().nextInt(maxStarRepeat + 1);
        String base = child.generateOneCase(maxStarRepeat);
        if (repeat == 0 || base.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(base.length() * repeat);
        for (int i = 0; i < repeat; i++) {
            sb.append(base);
        }
        return sb.toString();
    }

    @Override
    public Set<String> generateCasesExhaustive(int maxLen) {
        Set<String> base = child.generateCasesExhaustive(maxLen);
        Set<String> res = new HashSet<>();

        Deque<String> q = new ArrayDeque<>();
        q.add("");

        while (!q.isEmpty()) {
            String prefix = q.poll();
            res.add(prefix);
            for (String s : base) {
                String combined = prefix + s;
                if (combined.length() <= maxLen && !res.contains(combined)) {
                    q.add(combined);
                }
            }
        }

        return res;
    }
}
