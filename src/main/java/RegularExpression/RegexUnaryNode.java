package RegularExpression;

import static RegularExpression.RegexOperator.STAR;

/**
 * Abstract base class for unary regex operators (operators with one child).
 * Renamed from UnaryNode for clarity.
 *
 * @version 2.0
 */
public abstract class RegexUnaryNode extends RegexNode {
    public RegexNode child;

    public RegexUnaryNode(RegexNode child) {
        super(STAR);
        this.child = child;
    }
}
