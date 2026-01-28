package viewmodel;

/**
 * ViewModel for automaton metrics and statistics.
 * Provides type-agnostic metrics without exposing the underlying automaton type.
 */
public class AutomatonMetrics {

    private final Integer ruleCount;        // CFG only
    private final Integer transitionCount;  // PDA/DFA/NFA/TM
    private final Integer stateCount;       // DFA/NFA/PDA/TM
    private final String automatonType;     // Machine type name

    /**
     * Private constructor - use factory methods.
     */
    private AutomatonMetrics(Integer ruleCount, Integer transitionCount,
                              Integer stateCount, String automatonType) {
        this.ruleCount = ruleCount;
        this.transitionCount = transitionCount;
        this.stateCount = stateCount;
        this.automatonType = automatonType;
    }

    /**
     * Factory method for CFG metrics.
     */
    public static AutomatonMetrics forCFG(int ruleCount) {
        return new AutomatonMetrics(ruleCount, null, null, "CFG");
    }

    /**
     * Factory method for PDA metrics.
     */
    public static AutomatonMetrics forPDA(int transitionCount, int stateCount) {
        return new AutomatonMetrics(null, transitionCount, stateCount, "PDA");
    }

    /**
     * Factory method for DFA metrics.
     */
    public static AutomatonMetrics forDFA(int transitionCount, int stateCount) {
        return new AutomatonMetrics(null, transitionCount, stateCount, "DFA");
    }

    /**
     * Factory method for NFA metrics.
     */
    public static AutomatonMetrics forNFA(int transitionCount, int stateCount) {
        return new AutomatonMetrics(null, transitionCount, stateCount, "NFA");
    }

    /**
     * Factory method for Turing Machine metrics.
     */
    public static AutomatonMetrics forTM(int transitionCount, int stateCount) {
        return new AutomatonMetrics(null, transitionCount, stateCount, "TM");
    }

    /**
     * Factory method for empty/uninitialized metrics.
     */
    public static AutomatonMetrics empty() {
        return new AutomatonMetrics(null, null, null, null);
    }

    // ═══════════════════════════════════════════════════════════════════
    // GETTERS
    // ═══════════════════════════════════════════════════════════════════

    public Integer getRuleCount() { return ruleCount; }
    public Integer getTransitionCount() { return transitionCount; }
    public Integer getStateCount() { return stateCount; }
    public String getAutomatonType() { return automatonType; }

    // ═══════════════════════════════════════════════════════════════════
    // CONVENIENCE METHODS
    // ═══════════════════════════════════════════════════════════════════

    public boolean hasRuleCount() { return ruleCount != null; }
    public boolean hasTransitionCount() { return transitionCount != null; }
    public boolean hasStateCount() { return stateCount != null; }
    public boolean isCFG() { return "CFG".equals(automatonType); }
    public boolean isPDA() { return "PDA".equals(automatonType); }
    public boolean isDFA() { return "DFA".equals(automatonType); }
    public boolean isNFA() { return "NFA".equals(automatonType); }
    public boolean isTM() { return "TM".equals(automatonType); }

    /**
     * Returns a formatted summary of the metrics.
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        if (automatonType != null) {
            sb.append(automatonType).append(": ");
        }
        if (hasRuleCount()) {
            sb.append(ruleCount).append(" rules");
        }
        if (hasTransitionCount()) {
            if (sb.length() > 0 && !sb.toString().endsWith(": ")) {
                sb.append(", ");
            }
            sb.append(transitionCount).append(" transitions");
        }
        if (hasStateCount()) {
            if (sb.length() > 0 && !sb.toString().endsWith(": ")) {
                sb.append(", ");
            }
            sb.append(stateCount).append(" states");
        }
        return sb.toString();
    }
}
