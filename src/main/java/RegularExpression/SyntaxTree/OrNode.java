package RegularExpression.SyntaxTree;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import static RegularExpression.SyntaxTree.RegexOperator.*;

/**
 * Represents the "or" ({@value RegularExpression.SyntaxTree.RegexOperator#OR}) operation in a regular expression.
 * Matches either the left or the right child pattern (or both, if applicable).
 * <p>
 * For example, in the regex <code>1{@value RegularExpression.SyntaxTree.RegexOperator#OR}2</code>,
 * the operator would be the {@code OrNode}, with
 * <code>'1'</code> as the left child and <code>'2'</code> as the right child.
 * </p>
 */
public class OrNode extends BinaryNode {

    public OrNode(SyntaxTreeNode l, SyntaxTreeNode r) {
        super(l, r, OR);
    }

    @Override
    public Set<Integer> match(String s, int pos) {
        Set<Integer> res = new HashSet<>();
        res.addAll(leftChild.match(s, pos));
        res.addAll(rightChild.match(s, pos));
        return res;
    }

    @Override
    public String generateOneCase() {
        return ThreadLocalRandom.current().nextBoolean()
                ? leftChild.generateOneCase()
                : rightChild.generateOneCase();
    }

    @Override
    public String generateOneCase(int maxStarRepeat) {
        return ThreadLocalRandom.current().nextBoolean()
                ? leftChild.generateOneCase(maxStarRepeat)
                : rightChild.generateOneCase(maxStarRepeat);
    }

    @Override
    public Set<String> generateCasesExhaustive(int maxLen) {
        Set<String> res = new HashSet<>();
        res.addAll(leftChild.generateCasesExhaustive(maxLen));
        res.addAll(rightChild.generateCasesExhaustive(maxLen));
        return res;
    }
}
