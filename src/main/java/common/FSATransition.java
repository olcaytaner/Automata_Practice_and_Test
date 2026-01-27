package common;

import java.util.Objects;

/**
 * Transition class for Finite State Automata (both DFA and NFA).
 *
 * <p>Represents a transition that occurs when reading a specific input symbol.
 * Supports epsilon transitions (where symbol.isEpsilon() returns true) for NFAs.</p>
 *
 * <h3>Usage Examples:</h3>
 * <pre>{@code
 * // DFA transition: q0 --a--> q1
 * FSATransition t1 = new FSATransition(q0, new Symbol('a'), q1);
 *
 * // NFA epsilon transition: q0 --ε--> q1
 * FSATransition t2 = new FSATransition(q0, new Symbol('_'), q1);
 * }</pre>
 *
 * <h3>Parameter Order Convention:</h3>
 * <p>This follows the standard notation δ(q, a) = q' where:</p>
 * <ul>
 *   <li>q is the source state (from)</li>
 *   <li>a is the input symbol</li>
 *   <li>q' is the target state (to)</li>
 * </ul>
 */
public class FSATransition extends BaseTransition {

    private final Symbol inputSymbol;

    /**
     * Constructs a FSA transition.
     *
     * @param fromState The source state
     * @param inputSymbol The input symbol triggering this transition
     * @param toState The target state
     * @throws IllegalArgumentException if any parameter is null
     */
    public FSATransition(State fromState, Symbol inputSymbol, State toState) {
        super(fromState, toState);
        if (inputSymbol == null) {
            throw new IllegalArgumentException("Input symbol cannot be null");
        }
        this.inputSymbol = inputSymbol;
    }

    /**
     * Gets the input symbol that triggers this transition.
     * @return The input symbol
     */
    public Symbol getInputSymbol() {
        return inputSymbol;
    }

    /**
     * Checks if this is an epsilon (empty string) transition.
     * Epsilon transitions are only valid in NFAs.
     * @return true if this is an epsilon transition
     */
    public boolean isEpsilonTransition() {
        return inputSymbol.isEpsilon();
    }

    /**
     * Gets the source state of this transition.
     * Alias for {@link #getFromState()} for backward compatibility.
     * @return The state from which this transition originates
     */
    public State getFrom() {
        return getFromState();
    }

    /**
     * Gets the target state of this transition.
     * Alias for {@link #getToState()} for backward compatibility.
     * @return The state to which this transition leads
     */
    public State getTo() {
        return getToState();
    }

    /**
     * Gets the input symbol that triggers this transition.
     * Alias for {@link #getInputSymbol()} for backward compatibility.
     * @return The input symbol
     */
    public Symbol getSymbol() {
        return getInputSymbol();
    }

    @Override
    protected String getLabel() {
        return inputSymbol.isEpsilon() ? "ε" : inputSymbol.toString();
    }

    @Override
    public String prettyPrint() {
        String s = String.valueOf(inputSymbol.getValue());
        if (inputSymbol.isEpsilon()) {
            s = "ε";
        }
        return fromState.getName() + " -> " + toState.getName() + " (" + s + ")" + "\n";
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        FSATransition that = (FSATransition) o;
        return Objects.equals(inputSymbol, that.inputSymbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), inputSymbol);
    }
}
