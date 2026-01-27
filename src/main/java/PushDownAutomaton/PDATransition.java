package PushDownAutomaton;

import common.BaseTransition;
import common.State;
import common.Symbol;

import static common.SymbolConstants.*;

import java.util.Objects;

/**
 * Transition class for Push-Down Automata (PDA).
 *
 * <p>Extends the basic FSA transition with stack operations:</p>
 * <ul>
 *   <li><strong>Stack Pop:</strong> Symbol to be popped before transition</li>
 *   <li><strong>Stack Push:</strong> String to be pushed after transition</li>
 * </ul>
 *
 * <h3>Transition Notation:</h3>
 * <pre>
 * δ(q, a, X) = (p, γ)
 * where:
 *   q = source state
 *   a = input symbol (or ε for epsilon)
 *   X = stack symbol to pop
 *   p = target state
 *   γ = string to push onto stack
 * </pre>
 *
 * <h3>Usage Example:</h3>
 * <pre>{@code
 * // Push 'A' when reading 'a' and stack is empty
 * PDATransition t = new PDATransition(
 *     q0,                    // from state
 *     new Symbol('a'),       // input symbol
 *     new Symbol('_'),       // stack pop (epsilon = no pop)
 *     q1,                    // to state
 *     "A"                    // stack push
 * );
 * }</pre>
 */
public class PDATransition extends BaseTransition {

    private final Symbol inputSymbol;
    private final Symbol stackPop;
    private final String stackPush;

    /**
     * Constructs a PDA transition with full stack operations.
     *
     * @param fromState The source state
     * @param inputSymbol The input symbol (use epsilon symbol for ε-transitions)
     * @param stackPop The symbol to pop from stack (use epsilon for no pop)
     * @param toState The target state
     * @param stackPush The string to push onto stack (empty string for no push)
     * @throws IllegalArgumentException if fromState, toState, inputSymbol, or stackPop is null
     */
    public PDATransition(State fromState, Symbol inputSymbol, Symbol stackPop,
                         State toState, String stackPush) {
        super(fromState, toState);
        if (inputSymbol == null) {
            throw new IllegalArgumentException("Input symbol cannot be null");
        }
        if (stackPop == null) {
            throw new IllegalArgumentException("Stack pop symbol cannot be null");
        }
        this.inputSymbol = inputSymbol;
        this.stackPop = stackPop;
        this.stackPush = stackPush != null ? stackPush : "";
    }

    /**
     * Gets the input symbol that triggers this transition.
     * @return The input symbol
     */
    public Symbol getInputSymbol() {
        return inputSymbol;
    }

    /**
     * Gets the symbol to be popped from the stack.
     * @return The stack pop symbol
     */
    public Symbol getStackPop() {
        return stackPop;
    }

    /**
     * Gets the string to be pushed onto the stack.
     * @return The stack push string
     */
    public String getStackPush() {
        return stackPush;
    }

    /**
     * Checks if this transition reads input (non-epsilon).
     * @return true if this transition consumes input
     */
    public boolean consumesInput() {
        return !inputSymbol.isEpsilon();
    }

    /**
     * Checks if this transition pops from the stack.
     * @return true if this transition pops a symbol
     */
    public boolean popsFromStack() {
        return !stackPop.isEpsilon();
    }

    /**
     * Checks if this transition pushes to the stack.
     * @return true if this transition pushes symbols
     */
    public boolean pushesToStack() {
        return stackPush != null && !stackPush.isEmpty() && !isEpsilonInput(stackPush);
    }

    @Override
    protected String getLabel() {
        String input = inputSymbol.isEpsilon() ? EPSILON_DISPLAY : inputSymbol.toString();
        String pop = stackPop.isEpsilon() ? EPSILON_DISPLAY : stackPop.toString();
        String push = isNoPush(stackPush) ? EPSILON_DISPLAY : stackPush;
        return String.format("%s, %s/%s", input, pop, push);
    }

    @Override
    public String prettyPrint() {
        return String.format("%s -> %s [%s]\n",
            fromState.getName(),
            toState.getName(),
            getLabel());
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        PDATransition that = (PDATransition) o;
        return Objects.equals(inputSymbol, that.inputSymbol)
            && Objects.equals(stackPop, that.stackPop)
            && Objects.equals(stackPush, that.stackPush);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), inputSymbol, stackPop, stackPush);
    }
}
