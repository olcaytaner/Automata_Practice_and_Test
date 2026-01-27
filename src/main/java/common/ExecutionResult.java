package common;

import java.util.List;

/**
 * Represents the result of executing a formal language on an input string.
 * Contains acceptance status, runtime messages, and execution trace.
 *
 * @version 1.0
 */
public class ExecutionResult {
    private boolean accepted;
    private List<ValidationMessage> runtimeMessages;
    private String trace;

    /**
     * Creates a new execution result.
     *
     * @param accepted        Whether the input was accepted
     * @param runtimeMessages List of runtime messages (errors, warnings, info)
     * @param trace           Execution trace for debugging/visualization
     */
    public ExecutionResult(boolean accepted, List<ValidationMessage> runtimeMessages, String trace) {
        this.accepted = accepted;
        this.runtimeMessages = runtimeMessages;
        this.trace = trace;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public List<ValidationMessage> getRuntimeMessages() {
        return runtimeMessages;
    }

    public String getTrace() {
        return trace;
    }
}
