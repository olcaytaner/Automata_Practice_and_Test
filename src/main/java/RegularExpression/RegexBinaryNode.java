package RegularExpression;

/**
 * Abstract base class for binary regex operators (operators with two children).
 * <p>
 * For example, in the regex <code>1{@value RegexOperator#OR}2</code>,
 * the {@value RegexOperator#OR} operator would be a {@code RegexBinaryNode}
 * with <code>'1'</code> as the left child and <code>'2'</code> as the right child.
 * </p>
 * Renamed from BinaryNode for clarity.
 *
 * @version 2.0
 */
public abstract class RegexBinaryNode extends RegexNode {
    public RegexNode leftChild;
    public RegexNode rightChild;

    public RegexBinaryNode(RegexNode l, RegexNode r, char sym) {
        super(sym);
        leftChild = l;
        rightChild = r;
    }
}
