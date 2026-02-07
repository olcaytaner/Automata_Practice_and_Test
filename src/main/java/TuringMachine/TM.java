package TuringMachine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import common.Automaton;
import common.ExecutionResult;
import common.MachineType;
import common.ParseResult;
import common.Symbol;
import common.ValidationMessage;
import common.ValidationMessage.ValidationMessageType;

/**
 * Represents a Turing Machine.
 */
public class TM extends Automaton {
    private Set<State> states;
    private Alphabet inputAlphabet;
    private Alphabet tapeAlphabet;
    private Map<ConfigurationKey, TMTransition> transitionFunction;
    private State startState;
    private State acceptState;
    private State rejectState;
    private State currentState;
    private final Tape tape;

    public TM() {
        super(MachineType.TM);
        this.states = new HashSet<>();
        this.inputAlphabet = new Alphabet();
        this.tapeAlphabet = new Alphabet();
        this.transitionFunction = new HashMap<>();
        this.startState = null;
        this.acceptState = null;
        this.rejectState = null;
        this.tape = new Tape();
        this.currentState = null;
    }

    /**
     * Constructs a new TuringMachine.
     * @param states The set of states.
     * @param inputAlphabet The input alphabet.
     * @param tapeAlphabet The tape alphabet.
     * @param transitionFunction The transition function.
     * @param startState The start state.
     * @param acceptState The accept state.
     * @param rejectState The reject state.
     */
    public TM(Set<State> states,
                         Alphabet inputAlphabet,
                         Alphabet tapeAlphabet,
                         Map<ConfigurationKey, TMTransition> transitionFunction,
                         State startState,
                         State acceptState,
                         State rejectState) {
        super(MachineType.TM);
        this.states = states;
        this.inputAlphabet = inputAlphabet;
        this.tapeAlphabet = tapeAlphabet;
        this.transitionFunction = transitionFunction;
        this.startState = startState;
        this.acceptState = acceptState;
        this.rejectState = rejectState;
        this.tape = new Tape();
        reset();
    }

    @Override
    public String toDotCode(String inputText) {
        StringBuilder dot = new StringBuilder();
        dot.append("digraph TuringMachine {\n");
        dot.append("  rankdir=LR;\n");
        dot.append("  node [shape = circle];\n");

        for (State state : states) {
            if (state.isAccept()) {
                dot.append("  \"").append(state.getName()).append("\" [shape = doublecircle];\n");
            /*} else if (state.isReject()) {
                dot.append("  \"").append(state.getName()).append("\" [shape = square];\n");*///            } else {
                dot.append("  \"").append(state.getName()).append("\";\n");
            }
        }

        dot.append("  \"\" [shape = none];\n");
        dot.append("  \"\" -> \"").append(startState.getName()).append("\";\n");

        transitionFunction.forEach((key, value) -> {
            dot.append("  \"").append(key.getState().getName()).append("\" -> \"").append(value.getNextState().getName()).append("\" [label = \"")
               .append(key.getSymbolToRead()).append(" -> ").append(value.getSymbolToWrite()).append(", ").append(value.getMoveDirection() == TMTransition.Direction.LEFT ? "L" : "R").append("\"];\n");
        });

        dot.append("}\n");
        return dot.toString();
    }

    /**
     * Performs a single step of the Turing Machine's computation.
     */
    public void step() {
        Symbol currentSymbol = new Symbol(tape.read());
        TMTransition transition = transitionFunction.get(new ConfigurationKey(currentState, currentSymbol.getValue()));

        if (transition == null) {
            currentState = rejectState;
            return;
        }

        tape.write(transition.getSymbolToWrite());
        tape.move(transition.getMoveDirection());
        currentState = (State) transition.getNextState();
    }

    /**
     * Resets the Turing Machine to its initial state.
     */
    public void reset() {
        tape.clear();
        currentState = startState;
    }

    

    /**
     * Returns the set of states in the Turing Machine.
     * @return The set of states.
     */
    public Set<State> getStates() {
        return states;
    }

    /**
     * Returns the input alphabet of the Turing Machine.
     * @return The input alphabet.
     */
    public Alphabet getInputAlphabet() {
        return inputAlphabet;
    }

    /**
     * Returns the start state of the Turing Machine.
     * @return The start state.
     */
    public State getStartState() {
        return startState;
    }

    /**
     * Returns the current state of the Turing Machine.
     * @return The current state.
     */
    public State getCurrentState() {
        return currentState;
    }

    /**
     * Returns the tape of the Turing Machine.
     * @return The tape.
     */
    public Tape getTape() {
        return tape;
    }

    @Override
    public ParseResult parse(String inputText) {
        if (inputText == null) {
            throw new NullPointerException("Input text cannot be null");
        }
        
        // Perform validation first
        List<ValidationMessage> validationMessages = validate(inputText);
        
        boolean hasErrors = validationMessages.stream().anyMatch(i -> i.getType() == ValidationMessageType.ERROR);
        if (hasErrors) {
            return new ParseResult(false, validationMessages, null);
        }
        
        try {
            TM machine = TMParser.parse(inputText);
            return new ParseResult(true, validationMessages, machine);
        } catch (Exception e) {
            List<ValidationMessage> messages = new ArrayList<>(validationMessages);
            messages.add(new ValidationMessage("Parsing failed: " + e.getMessage(), 0, ValidationMessageType.ERROR));
            return new ParseResult(false, messages, null);
        }
    }


    @Override
    public ExecutionResult execute(String inputText) {
        StringBuilder trace = new StringBuilder();
        reset();
        tape.initialize(inputText);
        currentState = startState;

        trace.append("Initial State: ").append(currentState.getName()).append(", Tape: ");
        tape.appendTapeTo(trace);
        trace.append("\n");

        // Step limit to prevent infinite loops and memory exhaustion
        final int MAX_STEPS = 100000;
        int stepCount = 0;

        while (!currentState.isAccept() && !currentState.isReject()) {
            if (stepCount >= MAX_STEPS) {
                trace.append("Execution halted: step limit (").append(MAX_STEPS).append(") exceeded\n");
                return new ExecutionResult(false, new ArrayList<>(), trace.toString());
            }
            step();
            stepCount++;
            trace.append("State: ").append(currentState.getName()).append(", Tape: ");
            tape.appendTapeTo(trace);
            trace.append("\n");
        }
        return new ExecutionResult(currentState.isAccept(), new ArrayList<>(), trace.toString());
    }

    @Override
    public List<ValidationMessage> validate() {
        return TMFileValidator.validateFromString(inputText);
    }

    public List<ValidationMessage> validate(String inputText) {
        if (inputText == null) {
            List<ValidationMessage> messages = new ArrayList<>();
            messages.add(new ValidationMessage("Input text cannot be null", 0, ValidationMessageType.ERROR));
            return messages;
        }
        return TMFileValidator.validateFromString(inputText);
    }

    /**
     * Returns the total number of transitions in the Turing Machine.
     * @return the total transition count
     */
    public int getTransitionCount() {
        return transitionFunction.size();
    }

    @Override
    public String getDefaultTemplate() {
        return "states: q0 q_accept q_reject\n" +
               "input: a b\n" +
               "tape: a b _\n" +
               "start: q0\n" +
               "accept: q_accept\n" +
               "reject: q_reject\n" +
               "\n" +
               "transitions:\n" +
               "q0, a -> q_accept, a, R\n" +
               "q0, b -> q_accept, b, R\n" +
               "q0, _ -> q_accept, _, R\n";
    }
}
