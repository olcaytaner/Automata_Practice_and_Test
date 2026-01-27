package common;

import java.util.Objects;

/**
 * Abstract base class providing common transition functionality.
 * All concrete transition types should extend this class.
 *
 * <p>Provides:</p>
 * <ul>
 *   <li>Immutable from/to state storage</li>
 *   <li>Standard null validation</li>
 *   <li>Base equals/hashCode implementation</li>
 *   <li>Default toString implementation</li>
 *   <li>DOT edge generation for GraphViz</li>
 * </ul>
 */
public abstract class BaseTransition implements Transition {

    protected final State fromState;
    protected final State toState;

    /**
     * Constructs a transition between two states.
     *
     * @param fromState The source state, must not be null
     * @param toState The target state, must not be null
     * @throws IllegalArgumentException if either state is null
     */
    protected BaseTransition(State fromState, State toState) {
        if (fromState == null) {
            throw new IllegalArgumentException("Source state (fromState) cannot be null");
        }
        if (toState == null) {
            throw new IllegalArgumentException("Target state (toState) cannot be null");
        }
        this.fromState = fromState;
        this.toState = toState;
    }

    @Override
    public State getFromState() {
        return fromState;
    }

    @Override
    public State getToState() {
        return toState;
    }

    /**
     * Returns the transition label for display purposes.
     * Subclasses should override to provide type-specific labels.
     *
     * @return The label string for this transition
     */
    protected abstract String getLabel();

    @Override
    public String toDotEdge() {
        return String.format("  \"%s\" -> \"%s\" [label=\"%s\"];",
            fromState.getName(),
            toState.getName(),
            escapeDotLabel(getLabel()));
    }

    /**
     * Escapes special characters for DOT format.
     * @param label The label to escape
     * @return The escaped label
     */
    protected String escapeDotLabel(String label) {
        return label.replace("\"", "\\\"")
                    .replace("\n", "\\n");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseTransition that = (BaseTransition) o;
        return Objects.equals(fromState, that.fromState)
            && Objects.equals(toState, that.toState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromState, toState);
    }

    @Override
    public String toString() {
        return String.format("%s --%s--> %s",
            fromState.getName(),
            getLabel(),
            toState.getName());
    }
}
