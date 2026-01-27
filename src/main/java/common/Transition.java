package common;

/**
 * Base interface for all automaton transitions.
 * Defines the minimal contract that all transitions must fulfill.
 *
 * <p>A transition represents a directed edge in the automaton's state graph,
 * connecting a source state to a target state under certain conditions.</p>
 *
 * <p>This interface is implemented by:</p>
 * <ul>
 *   <li>{@link FSATransition} - for DFA and NFA transitions</li>
 *   <li>{@link PDATransition} - for Push-Down Automata transitions</li>
 *   <li>{@link TMTransition} - for Turing Machine transitions</li>
 * </ul>
 */
public interface Transition {

    /**
     * Gets the source state of this transition.
     * @return The state from which this transition originates
     */
    State getFromState();

    /**
     * Gets the target state of this transition.
     * @return The state to which this transition leads
     */
    State getToState();

    /**
     * Returns a human-readable representation of the transition.
     * @return Formatted string describing the transition
     */
    String prettyPrint();

    /**
     * Returns a DOT language representation for GraphViz visualization.
     * @return DOT format edge string
     */
    String toDotEdge();
}
