package RegularExpression.SyntaxTree;

import java.util.HashSet;
import java.util.Set;

import static RegularExpression.SyntaxTree.RegexOperator.*;

/**
 * Represents a concatenation operation in a regular expression syntax tree.
 * <p>
 *     A {@code ConcatNode} is a {@link BinaryNode} whose left and right children
 *     are matched in sequence. It corresponds to placing two patterns next to
 *     each other in a regular expression. For example, in the regex
 *     <code>AB</code>, the concatenation of <code>A</code> and <code>B</code>
 *     would be represented by a {@code ConcatNode} with <code>A</code> as the
 *     left child and <code>B</code> as the right child.
 * </p>
 */
public class ConcatNode extends BinaryNode {

    public ConcatNode(SyntaxTreeNode l, SyntaxTreeNode r) {
        super(l, r, CONCAT);
    }

    @Override
    public Set<Integer> match(String s, int pos) {
        Set<Integer> res = new HashSet<>();
        Set<Integer> mids = leftChild.match(s, pos);
        for (int mid : mids) {
            res.addAll(rightChild.match(s, mid));
        }
        return res;
    }

    @Override
    public String generateOneCase() {
        return leftChild.generateOneCase() + rightChild.generateOneCase();
    }

    @Override
    public String generateOneCase(int maxStarRepeat) {
        return leftChild.generateOneCase(maxStarRepeat) + rightChild.generateOneCase(maxStarRepeat);
    }

    @Override
    public Set<String> generateCasesExhaustive(int maxLen) {
        Set<String> left = leftChild.generateCasesExhaustive(maxLen);
        Set<String> right = rightChild.generateCasesExhaustive(maxLen);
        Set<String> res = new HashSet<>();
        for (String s1 : left) {
            for (String s2 : right) {
                if (s1.length() + s2.length() <= maxLen) {
                    res.add(s1 + s2);
                }
            }
        }
        return res;
    }
}
