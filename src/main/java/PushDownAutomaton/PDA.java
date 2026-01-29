package PushDownAutomaton;

import common.Automaton;
import common.ExecutionResult;
import common.MachineType;
import common.InputNormalizer;
import common.ParseResult;
import common.State;
import common.Symbol;
import common.ValidationMessage;
import common.ValidationMessage.ValidationMessageType;

import static common.SymbolConstants.*;

import java.util.*;

/**
 * Push-down Automaton (PDA) implementation.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Parse a PDA definition from text (states, alphabets, start/final states, transitions)</li>
 *   <li>Execute the (possibly non-deterministic) PDA on a given input string</li>
 *   <li>Produce Graphviz DOT code for visualization</li>
 * </ul>
 *
 * <p><strong>Execution model</strong>: BFS on configurations {@code (state, inputPos, stackString)}.
 * Top of stack is kept at {@code stackString.charAt(0)}. Acceptance is by final state (all input consumed
 * and current state ∈ finals).</p>
 *
 * <p><strong>Safety controls</strong>:
 * <ul>
 *   <li>Expansion cap (number of explored configurations) is configurable via
 *       {@code -Dpda.maxExpansions=<int>} (default: 500_000).</li>
 *   <li>Optional wall-clock timeout is configurable via {@code -Dpda.timeoutMs=<long>} (default: disabled / 0).</li>
 * </ul>
 * If either cap/timeout is hit, execution stops with a WARNING log and returns rejection.</p>
 */
public class PDA extends Automaton {

    /* ---------------- Configuration knobs (system properties) ---------------- */

    /** Max number of configurations to explore before aborting (default 500k). */
    private static final int MAX_EXPANSIONS_CAP =
            Integer.getInteger("pda.maxExpansions", 500_000);

    /** Optional wall-clock timeout in nanoseconds (0 = disabled). */
    private static final long TIMEOUT_NS =
            Long.getLong("pda.timeoutMs", 0L) * 1_000_000L;

    /* ---------------- Parsed PDA components ---------------- */

    private Set<State> states;
    private Set<Symbol> inputAlphabet;
    private Set<Symbol> stackAlphabet;
    private State startState;
    private Set<State> finalStates;
    private Symbol stackStartSymbol;
    private Map<State, List<PDATransition>> transitionMap;

    public PDA() {
        super(MachineType.PDA);
    }

    /**
     * Parse a PDA definition text into internal structures.
     *
     * <p>Expected sections: {@code states, alphabet, stack_alphabet, start, stack_start, finals, transitions}.
     * Transitions use the format:
     * <pre>
     *   fromState inputOrEps stackPopOrEps -> toState stackPushOrEps
     * </pre>
     * where {@code eps} denotes epsilon; {@code stackPushOrEps} may be a multi-character string or {@code eps}.</p>
     *
     * @param inputText PDA definition text (not null)
     * @return {@link ParseResult} with success flag, validation messages, and reference to this PDA instance
     * @throws NullPointerException if {@code inputText} is null
     */
    @Override
    public ParseResult parse(String inputText) {
        if (inputText == null) {
            throw new NullPointerException("Input text cannot be null");
        }
        this.states = new HashSet<>();
        this.inputAlphabet = new HashSet<>();
        this.stackAlphabet = new HashSet<>();
        this.startState = null;
        this.finalStates = new HashSet<>();
        this.stackStartSymbol = null;
        this.transitionMap = new HashMap<>();
        Map<String, State> stateMap = new HashMap<>();

        InputNormalizer.NormalizedInput normalized =
                InputNormalizer.normalize(inputText, MachineType.PDA);

        List<ValidationMessage> messages = new ArrayList<>(normalized.getMessages());
        Map<String, List<String>> sections = normalized.getSections();
        Map<String, Integer> sectionLineNumbers = normalized.getSectionLineNumbers();

        if (normalized.hasErrors()
                || !InputNormalizer.validateRequiredKeywords(sections, MachineType.PDA, messages)) {
            return new ParseResult(false, messages, null);
        }

        processStates(sections.get("states"), sectionLineNumbers.get("states"), this.states, stateMap, messages);
        processAlphabet(sections.get("alphabet"), sectionLineNumbers.getOrDefault("alphabet", 0), this.inputAlphabet, messages);
        processAlphabet(sections.get("stack_alphabet"), sectionLineNumbers.getOrDefault("stack_alphabet", 0), this.stackAlphabet, messages);
        this.startState = processStartState(sections.get("start"), sectionLineNumbers.get("start"), stateMap, messages);
        this.stackStartSymbol = processStackStartSymbol(
                sections.get("stack_start"),
                sectionLineNumbers.getOrDefault("stack_start", 0),
                this.stackAlphabet,
                messages);
        this.finalStates.addAll(processFinalStates(sections.get("finals"), sectionLineNumbers.get("finals"), stateMap, messages));
        this.transitionMap.putAll(processTransitions(sections.get("transitions"), sectionLineNumbers.get("transitions"), stateMap, this.inputAlphabet, this.stackAlphabet, messages));

        boolean isSuccess = messages.stream().noneMatch(m -> m.getType() == ValidationMessageType.ERROR);
        if (!isSuccess) {
            return new ParseResult(false, messages, this);
        }

        checkForUnreachableStates(this.states, this.startState, this.transitionMap, messages);
        checkForDeadEndStates(this.states, this.finalStates, this.transitionMap, messages);
        return new ParseResult(true, messages, this);
    }

    /**
     * Execute the PDA on the given input string.
     *
     * <p>Performs a BFS over configurations. Acceptance occurs if we reach a final state after consuming
     * the whole input and the stack is empty (acceptance by final state + empty stack). To prevent
     * pathological blow-ups, execution respects:
     * <ul>
     *   <li>{@code pda.maxExpansions} (expansion cap)</li>
     *   <li>{@code pda.timeoutMs} (wall-clock timeout), if set</li>
     * </ul>
     * On abort, a WARNING is logged and the result is rejection with a best-effort trace.</p>
     *
     * @param inputText input string (may be null → treated as empty)
     * @return {@link ExecutionResult} with acceptance flag, info/warning logs, and a transition trace
     */
    @Override
    public ExecutionResult execute(String inputText) {
        List<ValidationMessage> logs = new ArrayList<>();

        if (this.startState == null) {
            logs.add(new ValidationMessage("Automaton not parsed.", 0, ValidationMessageType.ERROR));
            return new ExecutionResult(false, logs, "Automaton not parsed.");
        }

        final String input = (inputText == null) ? "" : inputText;
        final int n = input.length();

        // initial stack: start symbol if not epsilon
        final String initStack =
                (this.stackStartSymbol != null && !this.stackStartSymbol.isEpsilon())
                        ? Character.toString(this.stackStartSymbol.getValue())
                        : "";

        Deque<Conf> queue = new ArrayDeque<>();
        Set<Conf> visited = new HashSet<>();
        Map<Conf, Step> parent = new HashMap<>();

        Conf start = new Conf(this.startState, 0, initStack);
        queue.add(start);
        visited.add(start);

        int expansions = 0;
        final long t0 = System.nanoTime();

        while (!queue.isEmpty()) {
            // Timeout check
            if (TIMEOUT_NS > 0 && System.nanoTime() - t0 > TIMEOUT_NS) {
                logs.add(new ValidationMessage(
                        "Search aborted due to timeout (" + (TIMEOUT_NS / 1_000_000) + " ms).",
                        0, ValidationMessageType.WARNING));
                break;
            }
            // Expansion cap check
            if (expansions++ > MAX_EXPANSIONS_CAP) {
                logs.add(new ValidationMessage(
                        "Search aborted after exploring " + expansions +
                                " configurations (cap via pda.maxExpansions).",
                        0, ValidationMessageType.WARNING));
                break;
            }

            Conf cur = queue.poll();

            // Accept when input fully consumed and in a final state
            if (cur.pos == n
                    && this.finalStates.contains(cur.state)) {
                String trace = reconstructTrace(parent, cur);
                logs.add(new ValidationMessage(
                        "Accepted at state '" + cur.state.getName() + "' with stack='" + cur.stack + "'.",
                        0, ValidationMessageType.INFO));
                return new ExecutionResult(true, logs, trace);
            }

            // Expand transitions from current state
            List<PDATransition> outgoing = this.transitionMap.getOrDefault(cur.state, Collections.emptyList());
            for (PDATransition t : outgoing) {
                // Stack pop condition
                boolean popEps = t.getStackPop().isEpsilon();
                char popCh = t.getStackPop().getValue();
                if (!popEps) {
                    if (cur.stack.isEmpty() || cur.stack.charAt(0) != popCh) continue;
                }

                // Input consume condition
                boolean inEps = t.getInputSymbol().isEpsilon();
                if (!inEps) {
                    if (cur.pos >= n || input.charAt(cur.pos) != t.getInputSymbol().getValue()) continue;
                }

                // Apply transition
                String newStack = cur.stack;
                if (!popEps) {
                    newStack = newStack.substring(1); // pop from left (top)
                }

                String push = t.getStackPush();
                if (!isEpsilonInput(push)) {
                    // push sequence to left (top at index 0)
                    newStack = push + newStack;
                }

                int newPos = inEps ? cur.pos : (cur.pos + 1);
                Conf nxt = new Conf(t.getToState(), newPos, newStack);

                if (visited.add(nxt)) {
                    parent.put(nxt, new Step(cur, t));
                    queue.add(nxt);
                }
            }
        }

        // Not accepted: produce a best-effort trace from the farthest progressed configuration
        Conf farthest = parent.keySet().stream()
                .max(Comparator.comparingInt(c -> c.pos))
                .orElse(null);

        String trace = (farthest != null) ? reconstructTrace(parent, farthest) : "No steps taken.";
        logs.add(new ValidationMessage("No accepting configuration found.", 0, ValidationMessageType.INFO));
        return new ExecutionResult(false, logs, trace);
    }

    /* ----------------- Helpers for BFS trace & conf identity ----------------- */

    /** Immutable configuration for BFS search. */
    private static final class Conf {
        final State state;
        final int pos;        // input index
        final String stack;   // top-of-stack = stack.charAt(0)

        Conf(State state, int pos, String stack) {
            this.state = state;
            this.pos = pos;
            this.stack = (stack == null) ? "" : stack;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Conf)) return false;
            Conf c = (Conf) o;
            return pos == c.pos
                    && Objects.equals(state.getName(), c.state.getName())
                    && Objects.equals(stack, c.stack);
        }

        @Override
        public int hashCode() {
            return Objects.hash(state.getName(), pos, stack);
        }
    }

    /** Edge used to reconstruct a successful path. */
    private static final class Step {
        final Conf prev;
        final PDATransition tr;

        Step(Conf prev, PDATransition tr) {
            this.prev = prev;
            this.tr = tr;
        }
    }

    private static String symToStr(Symbol s) {
        return (s == null || s.isEpsilon()) ? EPSILON_INPUT : Character.toString(s.getValue());
    }

    /** Reconstruct a human-readable transition trace from parents map. */
    private String reconstructTrace(Map<Conf, Step> parent, Conf end) {
        List<String> lines = new ArrayList<>();
        Conf cur = end;

        while (parent.containsKey(cur)) {
            Step st = parent.get(cur);
            PDATransition t = st.tr;

            String in = symToStr(t.getInputSymbol());
            String pop = symToStr(t.getStackPop());
            String push = t.getStackPush();
            String from = st.prev.state.getName();
            String to = t.getToState().getName();

            lines.add(String.format("%s -- (%s, %s/%s) --> %s",
                    from, in, pop, push, to));

            cur = st.prev;
        }
        Collections.reverse(lines);
        return String.join(" | ", lines);
    }

    /* ----------------- DOT generation ----------------- */

    /**
     * Produce Graphviz DOT code for the current PDA.
     *
     * @param inputText unused for DOT generation (kept for interface compatibility)
     * @return DOT string ready to render
     */
    @Override
    public String toDotCode(String inputText) {
        if (this.states == null || this.states.isEmpty()) {
            return "digraph PDA {\n\tlabel=\"Automaton not parsed or is empty\";\n}";
        }
        StringBuilder dot = new StringBuilder("digraph PDA {\n");
        dot.append("  rankdir=LR;\n");
        dot.append("  node [shape = circle];\n");

        for (State state : finalStates) {
            dot.append(String.format("  \"%s\" [shape=doublecircle];\n", state.getName()));
        }

        if (startState != null) {
            dot.append("  \"\" [shape=point];\n");
            dot.append(String.format("  \"\" -> \"%s\";\n", startState.getName()));
        }

        Map<String, List<PDATransition>> groupedTransitions = new HashMap<>();
        for (List<PDATransition> transitions : transitionMap.values()) {
            for (PDATransition t : transitions) {
                String key = t.getFromState().getName() + " -> " + t.getToState().getName();
                groupedTransitions.computeIfAbsent(key, k -> new ArrayList<>()).add(t);
            }
        }

        String[] selfLoopPorts = {"n", "w", "s", "e", "nw", "ne", "sw", "se"};
        for (Map.Entry<String, List<PDATransition>> entry : groupedTransitions.entrySet()) {
            List<PDATransition> group = entry.getValue();
            State fromState = group.get(0).getFromState();
            State toState = group.get(0).getToState();
            boolean isSelfLoop = fromState.equals(toState);

            for (int i = 0; i < group.size(); i++) {
                PDATransition t = group.get(i);
                String input = t.getInputSymbol().isEpsilon() ? EPSILON_INPUT : t.getInputSymbol().toString();
                String pop = t.getStackPop().isEpsilon() ? EPSILON_INPUT : t.getStackPop().toString();
                String label = String.format("%s, %s / %s", input, pop, t.getStackPush());

                if (isSelfLoop) {
                    String port1 = selfLoopPorts[i * 2 % selfLoopPorts.length];
                    String port2 = selfLoopPorts[(i * 2 + 1) % selfLoopPorts.length];
                    dot.append(String.format("  \"%s\":%s -> \"%s\":%s [label=\"%s\"];\n",
                            fromState.getName(), port1, toState.getName(), port2, label));
                } else {
                    dot.append(String.format("  \"%s\" -> \"%s\" [label=\"%s\"];\n",
                            fromState.getName(), toState.getName(), label));
                }
            }
        }
        dot.append("}");
        return dot.toString();
    }

    /* ----------------- Validation hooks ----------------- */

    @Override
    public List<ValidationMessage> validate() {
        return new ArrayList<>();
    }

    /**
     * Returns the total number of transitions in this PDA.
     *
     * @return the total transition count
     */
    public int getTransitionCount() {
        return transitionMap.values().stream().mapToInt(List::size).sum();
    }

    /**
     * Returns the set of states in this PDA.
     *
     * @return the set of states
     */
    public Set<State> getStates() {
        return states != null ? Collections.unmodifiableSet(states) : Collections.emptySet();
    }

    /**
     * Validates the number of transitions against a maximum limit.
     *
     * @param maxTransitions the maximum allowed number of transitions
     * @return ValidationMessage with error if limit exceeded, null otherwise
     */
    public ValidationMessage validateTransitionsCount(int maxTransitions) {
        int count = getTransitionCount();
        if (count > maxTransitions) {
            return new ValidationMessage(
                String.format("PDA has %d transitions, exceeds maximum of %d", count, maxTransitions),
                0,
                ValidationMessageType.ERROR
            );
        }
        return null;
    }

    /* ----------------- Parsing helpers ----------------- */

    private void processStates(List<String> stateLines, int lineNum, Set<State> states,
                               Map<String, State> stateMap, List<ValidationMessage> messages) {
        if (stateLines == null || stateLines.isEmpty()) {
            messages.add(new ValidationMessage("The 'states:' block cannot be empty.", lineNum, ValidationMessageType.ERROR));
            return;
        }
        for (String name : stateLines.get(0).split("\\s+")) {
            State newState = new State(name);
            states.add(newState);
            stateMap.put(name, newState);
        }
    }

    private void processAlphabet(List<String> lines, int lineNum, Set<Symbol> alphabet,
                                 List<ValidationMessage> messages) {
        if (lines == null || lines.isEmpty()) {
            messages.add(new ValidationMessage("Alphabet definition cannot be empty.", lineNum, ValidationMessageType.ERROR));
            return;
        }
        for (String s : lines.get(0).split("\\s+")) {
            if (s.length() != 1) {
                messages.add(new ValidationMessage("Alphabet symbol '" + s + "' must be a single character.", lineNum, ValidationMessageType.ERROR));
                continue;
            }
            alphabet.add(new Symbol(s.charAt(0)));
        }
    }

    private State processStartState(List<String> lines, int lineNum,
                                    Map<String, State> stateMap, List<ValidationMessage> messages) {
        if (lines == null || lines.isEmpty()) {
            messages.add(new ValidationMessage("Start state definition is missing.", lineNum, ValidationMessageType.ERROR));
            return null;
        }
        String startStateName = lines.get(0).trim();
        State startState = stateMap.get(startStateName);
        if (startState == null) {
            messages.add(new ValidationMessage("Start state '" + startStateName + "' is not defined in 'states'.", lineNum, ValidationMessageType.ERROR));
            return null;
        }
        startState.setStart(true);
        return startState;
    }

    private Symbol processStackStartSymbol(List<String> lines, int lineNum,
                                           Set<Symbol> stackAlphabet, List<ValidationMessage> messages) {
        if (lines == null || lines.isEmpty()) {
            // Allow missing stack_start: stack begins empty (epsilon)
            return new Symbol(EPSILON_CHAR);
        }
        String stackStartStr = lines.get(0).trim();
        if (isEpsilonInput(stackStartStr)) {
            // Explicit epsilon → stack begins empty
            return new Symbol(EPSILON_CHAR);
        }
        if (stackStartStr.length() != 1) {
            messages.add(new ValidationMessage("Stack start symbol must be a single character.", lineNum, ValidationMessageType.ERROR));
            return null;
        }
        Symbol stackStartSymbol = new Symbol(stackStartStr.charAt(0));
        if (!stackAlphabet.contains(stackStartSymbol)) {
            messages.add(new ValidationMessage("Stack start symbol '" + stackStartStr + "' is not defined in 'stack_alphabet'.", lineNum, ValidationMessageType.ERROR));
        }
        return stackStartSymbol;
    }

    private Set<State> processFinalStates(List<String> lines, int lineNum,
                                          Map<String, State> stateMap, List<ValidationMessage> messages) {
        Set<State> finalStates = new HashSet<>();
        if (lines == null || lines.isEmpty()) {
            messages.add(new ValidationMessage("Final states definition is missing.", lineNum, ValidationMessageType.ERROR));
            return finalStates;
        }
        String[] finalStateNames = lines.get(0).split("\\s+");
        for (String name : finalStateNames) {
            State finalState = stateMap.get(name);
            if (finalState == null) {
                messages.add(new ValidationMessage("Final state '" + name + "' is not defined in 'states'.", lineNum, ValidationMessageType.ERROR));
                continue;
            }
            finalState.setAccept(true);
            finalStates.add(finalState);
        }
        return finalStates;
    }

    private Map<State, List<PDATransition>> processTransitions(List<String> lines, int startLine,
                                                               Map<String, State> stateMap,
                                                               Set<Symbol> inputAlphabet, Set<Symbol> stackAlphabet,
                                                               List<ValidationMessage> messages) {
        Map<State, List<PDATransition>> transitionMap = new HashMap<>();
        if (lines == null) return transitionMap;

        for (int i = 0; i < lines.size(); i++) {
            int currentLine = startLine + i + 1;
            String line = lines.get(i);

            String[] parts = line.split("->");
            if (parts.length != 2) {
                messages.add(new ValidationMessage("Invalid transition format. Rule must contain '->' separator.", currentLine, ValidationMessageType.ERROR));
                continue;
            }

            String[] left = parts[0].trim().split("\\s+");
            String[] right = parts[1].trim().split("\\s+");

            if (left.length != 3) {
                messages.add(new ValidationMessage("Left side of transition must have 3 components: state, input, stack_pop.", currentLine, ValidationMessageType.ERROR));
                continue;
            }
            if (right.length != 2) {
                messages.add(new ValidationMessage("Right side of transition must have 2 components: state, stack_push.", currentLine, ValidationMessageType.ERROR));
                continue;
            }

            State fromState = validateState(left[0], stateMap, currentLine, messages);
            Symbol input = validateInputSymbol(left[1], inputAlphabet, currentLine, messages);
            Symbol pop = validateStackSymbol(left[2], stackAlphabet, currentLine, messages);
            State toState = validateState(right[0], stateMap, currentLine, messages);
            String push = right[1];

            if (!validatePushString(push, stackAlphabet, currentLine, messages)) {
                continue;
            }
            if (fromState == null || input == null || pop == null || toState == null) {
                continue;
            }

            PDATransition transition = new PDATransition(fromState, input, pop, toState, push);
            transitionMap.computeIfAbsent(fromState, k -> new ArrayList<>()).add(transition);
        }
        return transitionMap;
    }

    private State validateState(String name, Map<String, State> stateMap, int line, List<ValidationMessage> messages) {
        State state = stateMap.get(name);
        if (state == null) {
            messages.add(new ValidationMessage("State '" + name + "' is not defined in 'states'.", line, ValidationMessageType.ERROR));
        }
        return state;
    }

    private Symbol validateInputSymbol(String s, Set<Symbol> alphabet, int line, List<ValidationMessage> messages) {
        if (isEpsilonInput(s)) return new Symbol(EPSILON_CHAR);
        if (s.length() != 1) {
            messages.add(new ValidationMessage("Input symbol '" + s + "' must be a single character or 'eps'.", line, ValidationMessageType.ERROR));
            return null;
        }
        Symbol symbol = new Symbol(s.charAt(0));
        if (!alphabet.contains(symbol)) {
            messages.add(new ValidationMessage("Input symbol '" + s + "' is not defined in 'alphabet'.", line, ValidationMessageType.ERROR));
            return null;
        }
        return symbol;
    }

    private Symbol validateStackSymbol(String s, Set<Symbol> alphabet, int line, List<ValidationMessage> messages) {
        if (isEpsilonInput(s)) return new Symbol(EPSILON_CHAR);
        if (s.length() != 1) {
            messages.add(new ValidationMessage("Stack symbol '" + s + "' must be a single character or 'eps'.", line, ValidationMessageType.ERROR));
            return null;
        }
        Symbol symbol = new Symbol(s.charAt(0));
        if (!alphabet.contains(symbol)) {
            messages.add(new ValidationMessage("Stack symbol '" + s + "' is not defined in 'stack_alphabet'.", line, ValidationMessageType.ERROR));
            return null;
        }
        return symbol;
    }

    private boolean validatePushString(String pushString, Set<Symbol> alphabet, int line, List<ValidationMessage> messages) {
        if (!isEpsilonInput(pushString)) {
            if (pushString.length() != 1) {
                messages.add(new ValidationMessage(
                        "Stack push '" + pushString + "' must be a single character or 'eps'.",
                        line, ValidationMessageType.ERROR));
                return false;
            }
            char c = pushString.charAt(0);
            if (!alphabet.contains(new Symbol(c))) {
                messages.add(new ValidationMessage(
                        "Stack symbol '" + c + "' is not defined in 'stack_alphabet'.",
                        line, ValidationMessageType.ERROR));
                return false;
            }
        }
        return true;
    }


    private void checkForUnreachableStates(Set<State> allStates, State startState,
                                           Map<State, List<PDATransition>> transitions,
                                           List<ValidationMessage> messages) {
        if (startState == null || allStates.isEmpty()) return;

        Set<State> reachableStates = new HashSet<>();
        Queue<State> queue = new LinkedList<>();
        reachableStates.add(startState);
        queue.add(startState);

        while (!queue.isEmpty()) {
            State currentState = queue.poll();
            if (transitions.containsKey(currentState)) {
                for (PDATransition t : transitions.get(currentState)) {
                    if (t.getToState() != null && reachableStates.add(t.getToState())) {
                        queue.add(t.getToState());
                    }
                }
            }
        }

        for (State state : allStates) {
            if (!reachableStates.contains(state)) {
                messages.add(new ValidationMessage("State '" + state.getName() + "' is unreachable from the start state.", 0, ValidationMessageType.WARNING));
            }
        }
    }

    private void checkForDeadEndStates(Set<State> allStates, Set<State> finalStates,
                                       Map<State, List<PDATransition>> transitions,
                                       List<ValidationMessage> messages) {
        for (State state : allStates) {
            if (!finalStates.contains(state) && transitions.getOrDefault(state, Collections.emptyList()).isEmpty()) {
                messages.add(new ValidationMessage("State '" + state.getName() + "' is a non-final state with no outgoing transitions (dead-end state).", 0, ValidationMessageType.WARNING));
            }
        }
    }

    /**
     * Default template content for a new PDA file in the editor.
     * <p><strong>Note:</strong> Kept as-is on purpose (UI relies on this exact text/shape).</p>
     */
    public String getDefaultTemplate() {
        return "states: q0 q1\n" +
                "alphabet: a b\n" +
                "stack_alphabet: Z\n" +
                "start: q0\n" +
                "stack_start: eps\n" +
                "finals: q1\n" +
                "\n" +
                "transitions:\n" +
                "q0 eps eps -> q1 Z\n" +
                "q1 a Z -> q1 eps\n" +
                "q1 b Z -> q1 eps\n";
    }
}
