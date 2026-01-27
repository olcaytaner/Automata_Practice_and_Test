package NondeterministicFiniteAutomaton;

import common.Automaton;
import common.ExecutionResult;
import common.MachineType;
import common.InputNormalizer;
import common.ParseResult;
import common.State;
import common.Symbol;
import common.ValidationMessage;
import common.ValidationMessage.ValidationMessageType;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a Non-deterministic Finite Automaton (NFA).
 * <p>
 * This class provides functionality for parsing a textual representation of an NFA,
 * validating its structure, and producing its DOT format for visualization.
 * It supports states, alphabet, transitions, and distinguishes between start and final states.
 * </p>
 * <p>
 * The expected input format includes:
 * <ul>
 *   <li>A "start:" line with exactly one start state.</li>
 *   <li>A "finals:" line with one or more final states.</li>
 *   <li>An "alphabet:" line with valid characters.</li>
 *   <li>A "transitions:" section with valid state-to-state transitions using symbols from the alphabet.</li>
 * </ul>
 * </p>
 */
public class NFA extends Automaton {

    private static final String transitionPattern = "(?:(q\\S+)|.) ?-> ?(?:(q\\S+)|\\S*)? ?(?:(\\((?:[a-zA-Z0-9]|eps)(?:\\s(?:[a-zA-Z0-9]|eps))*\\s?\\))|.*)?";
    private static final String transitionSymbolPattern = "\\((?:[a-zA-Z0-9]|eps)(?:\\s(?:[a-zA-Z0-9]|eps))*\\s?\\)";
    private static final String statePattern = "q\\S+";

    private Map<String, State> states;
    private Set<Symbol> alphabet;
    private State startState;
    private Set<State> finalStates;
    private Map<State, List<Transition>> transitions;

    private static final boolean TIME = false;
    private static final boolean VERBOSE = false;
    private static final int STATE_NAME_MAX_LENGTH_WARNING= 20;

    /**
     * Constructs an NFA with specified states, alphabet, start state, final states, and transitions.
     *
     * @param states       map of state names to State objects
     * @param alphabet     set of symbols in the alphabet
     * @param startState   the starting state
     * @param finalStates  set of final states
     * @param transitions  map of states to their outgoing transitions
     */
    public NFA(Map<String, State> states, Set<Symbol> alphabet, State startState, Set<State> finalStates, Map<State, List<Transition>> transitions) {
        super(MachineType.NFA);
        this.states = states;
        this.alphabet = alphabet;
        this.startState = startState;
        this.finalStates = finalStates;
        this.transitions = transitions;
    }

    /**
     * Constructs an empty NFA with initialized data structures.
     */
    public NFA(){
        super(MachineType.NFA);
        this.states = new HashMap<>();
        this.alphabet = new HashSet<>();
        this.startState = null;
        this.finalStates = new HashSet<>();
        this.transitions = new HashMap<>();
    }

    /**
     * Parses the input text to create the NFA structure.
     *
     * @param inputText textual representation of the NFA
     * @return {@link ParseResult} indicating success/failure and any validation messages
     */
    @Override
    public ParseResult parse(String inputText) {
        if (inputText == null) {
            throw new NullPointerException("Input text cannot be null");
        }
        
        this.states = new HashMap<>();
        this.alphabet = new HashSet<>();
        this.startState = null;
        this.finalStates = new HashSet<>();
        this.transitions = new HashMap<>();

        // Use InputNormalizer for consistent parsing
        InputNormalizer.NormalizedInput normalizedInput = InputNormalizer.normalize(inputText, MachineType.NFA);
        List<ValidationMessage> messages = new ArrayList<>(normalizedInput.getMessages());
        Map<String, List<String>> sections = normalizedInput.getSections();
        Map<String, Integer> sectionLineNumbers = normalizedInput.getSectionLineNumbers();

        if (normalizedInput.hasErrors()) {
            return new ParseResult(false, messages, null);
        }

        if (!InputNormalizer.validateRequiredKeywords(sections, MachineType.NFA, messages)) {
            return new ParseResult(false, messages, null);
        }

        // Process sections using a simplified approach similar to DFA
        processStatesNFA(sections.get("states"), sectionLineNumbers, messages);
        processAlphabetNFA(sections.get("alphabet"), sectionLineNumbers, messages);
        processStartStateNFA(sections.get("start"), sectionLineNumbers, messages);
        processFinalStatesNFA(sections.get("finals"), sectionLineNumbers, messages);
        processTransitionsNFA(sections.get("transitions"), sectionLineNumbers, messages);

        boolean isSuccess = messages.stream().noneMatch(m -> m.getType() == ValidationMessageType.ERROR);
        
        ParseResult parseResult = new ParseResult(isSuccess, messages, isSuccess ? this : null);
        
        // Log results
        int errorCount = (int) messages.stream().filter(m -> m.getType() == ValidationMessageType.ERROR).count();
        int warnCount = (int) messages.stream().filter(m -> m.getType() == ValidationMessageType.WARNING).count();
        int infoCount = (int) messages.stream().filter(m -> m.getType() == ValidationMessageType.INFO).count();
        
        if (errorCount > 0) {
            System.out.println("\n NFA parsing unsuccessful " + errorCount + " error(s), " + warnCount + " warning(s) and " + infoCount + " info(s).\n");
        } else {
            System.out.println("\n Successfully parsed NFA with " + errorCount + " error(s) and " +  warnCount + " warning(s) " + infoCount + " info(s).\n");
        }

        for (ValidationMessage message : messages) {
            System.out.println(message);
        }

        if (isSuccess) {
            messages.addAll(validate());
        }

        return parseResult;
    }

    /**
     * Processes the 'states:' section of the NFA definition.
     * Validates each state name and adds it to the NFA's state map.
     *
     * @param stateLines the list of lines containing state names
     * @param lineNum the line number in the input where the states section begins
     * @param messages the list to collect validation messages for errors or warnings
     */
    private void processStatesNFA(List<String> stateLines, Map<String, Integer> lineNum, List<ValidationMessage> messages) {
        int lineNumber = lineNum.get("states") != null ? lineNum.get("states") : -1;
        if (stateLines == null || stateLines.isEmpty()) {
            messages.add(new ValidationMessage("The 'states:' block cannot be empty.", lineNumber, ValidationMessageType.ERROR));
            return;
        }

        String[] stateNames = String.join(" ", stateLines).split("\\s+");
        for (String name : stateNames) {
            lineNumber = findLineNumberFromSectionLines(name, lineNum, stateLines);
            if (name.length() > STATE_NAME_MAX_LENGTH_WARNING) {
                messages.add(new ValidationMessage("State Name Too Long.", lineNumber, ValidationMessageType.WARNING));
            }
            if (name.matches("^" + statePattern + "$")) {
                this.states.put(name, new State(name));
                if (VERBOSE) {
                    System.out.println("State: " + name + " added to states");
                }
            } else {
                messages.add(new ValidationMessage("Invalid state name: " + name, lineNum.getOrDefault(name, lineNumber), ValidationMessageType.ERROR));
            }
        }
    }


    /**
     * Processes the 'alphabet:' section of the NFA definition.
     * Validates each symbol, ensures no duplicates, and adds it to the NFA's alphabet set.
     *
     * @param alphabetLines the list of lines containing alphabet symbols
     * @param lineNum the line number in the input where the alphabet section begins
     * @param messages the list to collect validation messages for errors or warnings
     */
    private void processAlphabetNFA(List<String> alphabetLines, Map<String, Integer> lineNum, List<ValidationMessage> messages) {
        int lineNumber = lineNum.get("alphabet") != null ? lineNum.get("alphabet") : -1;
        if (alphabetLines == null || alphabetLines.isEmpty()) {
            messages.add(new ValidationMessage("The 'alphabet:' block cannot be empty.", lineNumber, ValidationMessageType.ERROR));
            return;
        }

        String[] symbolNames = String.join(" ", alphabetLines).split("\\s+");
        for (String name : symbolNames) {
            lineNumber = findLineNumberFromSectionLines(name, lineNum, alphabetLines);
            if (name.length() == 1 && Character.isLetterOrDigit(name.charAt(0))) {
                Symbol symbol = new Symbol(name.charAt(0));
                if (!this.alphabet.contains(symbol)) {
                    this.alphabet.add(symbol);
                    if (VERBOSE) {
                        System.out.println("Symbol: " + symbol + " added to alphabet");
                    }
                }else {
                    messages.add(new ValidationMessage("Duplicate Symbol: " + name, lineNumber, ValidationMessageType.ERROR));
                }
            } else {
                messages.add(new ValidationMessage("Invalid alphabet symbol: " + name, lineNumber, ValidationMessageType.ERROR));
            }
        }
    }


    /**
     * Processes the 'start:' section of the NFA definition.
     * Validates and sets the start state for the NFA.
     *
     * @param startLines the list of lines containing the start state
     * @param lineNum the line number in the input where the start section begins
     * @param messages the list to collect validation messages for errors or warnings
     */
    private void processStartStateNFA(List<String> startLines, Map<String, Integer> lineNum, List<ValidationMessage> messages) {
        int lineNumber = lineNum.get("start") != null ? lineNum.get("start") : -1;
        if (startLines == null || startLines.isEmpty()) {
            messages.add(new ValidationMessage("The 'start:' block cannot be empty.", lineNumber, ValidationMessageType.ERROR));
            return;
        }

        if (startLines.size() != 1) {
            messages.add(new ValidationMessage("There cannot be more than 1 start state",  lineNumber, ValidationMessageType.ERROR));
        }

        String startStateName = startLines.get(0).trim();
        lineNumber = lineNum.getOrDefault(startStateName, lineNumber);
        if (startStateName.matches("^" + statePattern.trim() + "$")) {
            if (this.states.containsKey(startStateName)) {
                this.states.get(startStateName).setStart(true);
                this.startState = this.states.get(startStateName);
                if (VERBOSE) {
                    System.out.println("State " + startStateName + " added to start state");
                }
            }else {
                messages.add(new ValidationMessage("States does not contain start state",  lineNumber, ValidationMessageType.ERROR));
            }
        } else {
            messages.add(new ValidationMessage("Invalid start state name: " + startStateName, lineNumber, ValidationMessageType.ERROR));
        }
    }


    /**
     * Processes the 'finals:' section of the NFA definition.
     * Validates each final state and marks it as accepting in the NFA.
     *
     * @param finalLines the list of lines containing final state names
     * @param lineNum the line number in the input where the finals section begins
     * @param messages the list to collect validation messages for errors or warnings
     */
    private void processFinalStatesNFA(List<String> finalLines, Map<String, Integer> lineNum, List<ValidationMessage> messages) {
        int lineNumber = lineNum.get("finals") != null ? lineNum.get("finals") : -1;
        if (finalLines == null) {
            messages.add(new ValidationMessage("The 'finals:' block cannot be empty.", lineNumber, ValidationMessageType.ERROR));
            return;
        }

        if (!finalLines.isEmpty()) {
            String[] finalStateNames = String.join(" ", finalLines).split("\\s+");
            for (String name : finalStateNames) {
                lineNumber = findLineNumberFromSectionLines(name, lineNum, finalLines);
                if (name.matches("^" + statePattern + "$")) {
                    if (this.states.containsKey(name)) {
                        this.states.get(name).setAccept(true);
                        this.finalStates.add(this.states.get(name));
                        if (VERBOSE) {
                            System.out.println("State " + name + " added to final states");
                        }
                    } else {
                        messages.add(new ValidationMessage("States does not contain final state: " + name, lineNumber, ValidationMessageType.ERROR));
                    }
                } else {
                    messages.add(new ValidationMessage("Invalid final state name: " + name, lineNumber, ValidationMessageType.ERROR));
                }
            }
        }

    }


    /**
     * Processes the 'transitions:' section of the NFA definition.
     * Validates the syntax and semantics of each transition line.
     *
     * @param transitionLines the list of lines defining transitions
     * @param lineNum the line number in the input where the transitions section begins
     * @param messages the list to collect validation messages for errors or warnings
     */
    private void processTransitionsNFA(List<String> transitionLines, Map<String, Integer> lineNum, List<ValidationMessage> messages) {
        if (transitionLines == null) return;

        List<ValidationMessage> lMessages = new ArrayList<>();

        for (int i = 0; i < transitionLines.size(); i++) {
            String line = transitionLines.get(i);
            int currentLineNumber = findLineNumberFromSectionLines(line, lineNum, transitionLines);

            lMessages.addAll(handleTransitionLines(line, currentLineNumber));
        }
        messages.addAll(lMessages);
    }

    /**
     * Executes the NFA on the given input text.
     *
     * @param inputText input string to execute on the NFA
     * @return {@link ExecutionResult}
     */
    public ExecutionResult execute(String inputText) {

        long time = System.nanoTime();
        List<ValidationMessage> runtimeMessages = new ArrayList<>();
        StringBuilder trace = new StringBuilder();

        Set<State> currentStates = new LinkedHashSet<>();

        if (this.startState == null) {
            runtimeMessages.add(new ValidationMessage("Start state is not defined", -1, ValidationMessageType.ERROR));
            if (TIME){
                System.out.println("Failed");
                System.out.println("Took " + (System.nanoTime() - time)/1_000_000.0 + " ms to execute NFA with " + inputText.length() + " character input.");
            }
            return new ExecutionResult(false, runtimeMessages, "");
        }

        currentStates.add(this.startState);
        trace.append("Start state: ").append(this.startState.getName()).append("\n");


        for (State s : getEpsilonClosure(currentStates)) {
            trace.append("Epsilon-closure includes: ").append(s.getName()).append("\n");
            currentStates.add(s);
        }

        for (char c : inputText.toCharArray()) {
            Symbol inputSymbol = new Symbol(c);
            Set<State> nextStates = new LinkedHashSet<>();

            if (!this.alphabet.contains(inputSymbol)) {
                runtimeMessages.add(new ValidationMessage("Symbol not in alphabet: " + c, -1, ValidationMessageType.ERROR));
                if (TIME){
                    System.out.println("Failed");
                    System.out.println("Took " + (System.nanoTime() - time)/1_000_000.0 + " ms to execute NFA with " + inputText.length() + " character input.");
                }
                return new ExecutionResult(false, runtimeMessages, trace.toString());
            }

            for (State state : currentStates) {

                for (Transition t : this.transitions.getOrDefault(state, Collections.emptyList())) {
                    if (t.getSymbol().equals(inputSymbol)) {
                        nextStates.add(t.getTo());
                        trace.append("Transition: ").append(state.getName())
                                .append(" --").append(c).append("--> ")
                                .append(t.getTo().getName()).append("\n");
                    }
                }

            }

            for (State s : getEpsilonClosure(nextStates)) {
                trace.append("Epsilon-closure includes: ").append(s.getName()).append("\n");
                nextStates.add(s);
            }

            currentStates = new LinkedHashSet<>(nextStates);
        }

        currentStates.addAll(getEpsilonClosure(currentStates));

        for (State state : currentStates) {
            if (state.isAccept()) {
                if (TIME) {
                    System.out.println("Took " + (System.nanoTime() - time)/1_000_000.0 + " ms to execute NFA with " + inputText.length() + " character input.");
                }
                return new ExecutionResult(true, runtimeMessages, trace.toString());
            }
        }
        if (TIME){
            System.out.println("Took " + (System.nanoTime() - time)/1_000_000.0 + " ms to execute NFA with " + inputText.length() + " character input.");
        }
        return new ExecutionResult(false, runtimeMessages, trace.toString());
    }

    /**
     * Computes the epsilon-closure of a set of states.
     * The epsilon-closure is the set of states reachable from the given states via epsilon transitions.
     *
     * @param states the set of states from which to compute the epsilon-closure
     * @return the set of states reachable via epsilon transitions
     */
    private Set<State> getEpsilonClosure(Set<State> states) {
        Set<State> queue = new LinkedHashSet<>(states);
        Set<State> visited = new HashSet<>();
        Set<State> returnQueue = new LinkedHashSet<>();

        while (!queue.isEmpty()){

            Iterator<State> it = queue.iterator();
            State state = it.next();
            it.remove();

            for (Transition t : this.transitions.getOrDefault(state, Collections.emptyList())) {
                if (t.getSymbol().isEpsilon() && !visited.contains(t.getTo())) {
                    visited.add(t.getFrom());
                    queue.add(t.getTo());
                    returnQueue.add(t.getTo());
                }
            }

        }

        return returnQueue;
    }

    /**
     * Computes the epsilon-closure of a single state.
     *
     * @param state the state from which to compute the epsilon-closure
     * @return the set of states reachable via epsilon transitions
     */
    private Set<State> getEpsilonClosure(State state){
        Set<State> set = new LinkedHashSet<>();
        set.add(state);
        return getEpsilonClosure(set);
    }

    private int findLineNumberFromSectionLines(String line, Map<String, Integer> lineMap, List<String> sectionLines) {
        String s = "";
        for (String sectionLine : sectionLines) {
            if (sectionLine.contains(line)) {
                s = sectionLine;
                break;
            }
        }

        return lineMap.getOrDefault(s, -1);
    }

    /**
     * Parses and validates a single transition line.
     * Ensures correct syntax for state names and symbols, verifies alphabet inclusion, and adds transitions.
     *
     * @param line   the input line representing a transition
     * @param lineNo the line number for error reporting
     * @return a list of ValidationMessages representing any issues found in the transition
     */
    private List<ValidationMessage> handleTransitionLines(String line, int lineNo){
        List<ValidationMessage> warnings = new ArrayList<>();

        Pattern fullPattern = Pattern.compile("^" + transitionPattern + "$");
        Pattern partialPattern = Pattern.compile(transitionPattern);
        Matcher matcher = fullPattern.matcher(line);
        String wrongPart = line;
        String message = "";
        String fromStateName = null;
        String toStateName = null;
        if (matcher.find()) {
            wrongPart = wrongPart.replace("->", "");
            if (matcher.group(1) != null && matcher.group(1).matches(statePattern)) {
                fromStateName = matcher.group(1);
                wrongPart = wrongPart.replaceFirst(Pattern.quote(matcher.group(1)),"");
            }else {
                message += "First state incorrect \n";
            }

            if (matcher.group(2) != null && matcher.group(2).matches(statePattern)) {
                toStateName = matcher.group(2);
                wrongPart = wrongPart.replaceFirst(Pattern.quote(matcher.group(2)),"");
            }else {
                message += "Second state incorrect \n";
            }

            if (matcher.group(3) != null && matcher.group(3).matches(transitionSymbolPattern)) {
                wrongPart = wrongPart.replaceFirst(Pattern.quote(matcher.group(3)),"");
            }else {
                message += "Transition letters incorrect \n";
            }
            wrongPart = wrongPart.trim();

        }else {
            matcher = partialPattern.matcher(line);
            if (matcher.find()) {
                message = "Valid transition found, but line has extra/invalid content";
            }else {
                message = "Whole line is wrong ";
            }
        }

        if (!message.isEmpty() || !wrongPart.isEmpty()) {
            warnings.add(new ValidationMessage("Wrong transition syntax: " + message + " Wrong part: " + "\"" + wrongPart + "\"",
                    lineNo, ValidationMessageType.ERROR));
        }else {
            //syntax correct, check for duplicate
            boolean alreadyExists = false;
            if (fromStateName != null && this.states.get(fromStateName) != null) {
                State fromState = this.states.get(fromStateName);
                if (this.transitions.get(fromState) != null) {
                    for (Transition t : this.transitions.get(fromState)) {
                        if (t.getTo().getName().equals(toStateName)) {
                            alreadyExists = true;
                            break;
                        }
                    }
                }
            }

            if (alreadyExists) {
                warnings.add(new ValidationMessage("There is already this transition: " + fromStateName + " -> " + toStateName,
                        lineNo, ValidationMessageType.ERROR));
            }

            if (fromStateName != null && toStateName != null) {

                State fromState = this.states.getOrDefault(fromStateName, null);
                State toState = this.states.getOrDefault(toStateName, null);

                String transitionName = matcher.group(3);

                if (fromState != null && toState != null && transitionName != null) {

                    String[] symbols = transitionName.substring(1, transitionName.length()-1).split("\\s");

                    List<Transition> transitionList = new ArrayList<>();
                    for (String symbol : symbols) {
                        Symbol symbolTemp;
                        if (symbol.equals("eps")) {
                            symbolTemp = new Symbol('_');
                        }else{
                            symbolTemp = new Symbol(symbol.charAt(0));
                        }
                        if (!this.alphabet.contains(symbolTemp) && !symbol.equals("eps")) {
                            warnings.add(new ValidationMessage("Alphabet does not contain transition symbol: " + symbol,
                                    lineNo, ValidationMessageType.ERROR));
                            continue;
                        }
                        Transition transition = new Transition(fromState,toState,symbolTemp);
                        if (!alreadyExists){
                            if (transitionList.contains(transition)) {
                                warnings.add(new ValidationMessage("Duplicate transition symbol: " + symbolTemp.getValue(), lineNo, ValidationMessageType.ERROR));
                            }else {
                                //unique transition and unique symbol
                                transitionList.add(transition);
                            }
                        }
                    }

                    if (!transitionList.isEmpty()) {
                        if (this.transitions.containsKey(fromState)){
                            this.transitions.get(fromState).addAll(transitionList);
                        }else {
                            this.transitions.put(fromState,transitionList);
                        }
                        if (VERBOSE){
                            System.out.println("Transition correct: " + line);
                        }
                        //correct
                    }
                }else {
                    if (transitionName == null) {
                        warnings.add(new ValidationMessage("Transition symbol is wrong", lineNo, ValidationMessageType.ERROR));
                    }
                    String s = "";
                    if (fromState == null){
                        s += "From state is not in states: " + fromStateName + "\n";
                    }
                    if (toState == null){
                        s += "To state is not in states: " + toStateName + "\n";
                    }
                    warnings.add(new ValidationMessage(s, lineNo, ValidationMessageType.ERROR));
                }
            }

        }

        return warnings;
    }

    /**
     * Validates the internal consistency of the NFA structure.
     *
     * @return list of {@link ValidationMessage} objects indicating errors or warnings
     */
    @Override
    public List<ValidationMessage> validate(){
        List<ValidationMessage> validationWarnings = new ArrayList<>();

        validationWarnings.addAll(validateStartState());

        validationWarnings.addAll(validateFinalStates());

        validationWarnings.addAll(validateAlphabet());

        validationWarnings.addAll(validateTransitions());

        validationWarnings.addAll(validateStates());

        for (ValidationMessage warning : validationWarnings){
            System.out.println(warning);
        }

        return validationWarnings;
    }

    /**
     * Validates the start state of the NFA.
     * Checks whether it is properly defined and consistent with the states map.
     *
     * @return list of {@link ValidationMessage} objects indicating any issues with the start state
     */
    private List<ValidationMessage> validateStartState(){
        List<ValidationMessage> validationWarnings = new ArrayList<>();

        Map<String, State> states = this.states;
        State startState = this.startState;

        if (startState == null){
            validationWarnings.add(new ValidationMessage("No start state in NFA object", -1, ValidationMessageType.ERROR));
        }else{
            if (!startState.isStart()){
                validationWarnings.add(new ValidationMessage("Start state is not a start state", -1, ValidationMessageType.ERROR));
            }
            if (!startState.getName().matches(statePattern)){
                validationWarnings.add(new ValidationMessage("Start state does not match valid state name", -1, ValidationMessageType.ERROR));
            }
            if (!states.containsKey(startState.getName())){
                validationWarnings.add(new ValidationMessage("States map does not contain the start state", -1, ValidationMessageType.ERROR));
            }else {
                if (!states.get(startState.getName()).equals(startState)){
                    validationWarnings.add(new ValidationMessage("Start state in states map is not equal to the start state", -1, ValidationMessageType.ERROR));
                }
            }
        }

        return validationWarnings;
    }

    /**
     * Validates the final states of the NFA.
     * Ensures they are marked correctly and present in the states map.
     *
     * @return list of {@link ValidationMessage} objects indicating any issues with final states
     */
    private List<ValidationMessage> validateFinalStates(){
        List<ValidationMessage> validationWarnings = new ArrayList<>();

        Set<State> finalStates = this.finalStates;
        if (finalStates == null || finalStates.isEmpty()){
            validationWarnings.add(new ValidationMessage("No final state in NFA object", -1, ValidationMessageType.ERROR));
        }else {
            for (State finalState : finalStates){
                if (!finalState.isAccept()){
                    validationWarnings.add(new ValidationMessage("Final state: " + finalState.getName() + " is not a final state", -1, ValidationMessageType.ERROR));
                }
                if (!finalState.getName().matches(statePattern)){
                    validationWarnings.add(new ValidationMessage("Final state: " + finalState.getName() + " does not match valid state name", -1, ValidationMessageType.ERROR));
                }
                if (!states.containsKey(finalState.getName())){
                    validationWarnings.add(new ValidationMessage("States map does not contain the final state: " + finalState.getName(), -1, ValidationMessageType.ERROR));
                }else {
                    if (!states.get(finalState.getName()).equals(finalState)){
                        validationWarnings.add(new ValidationMessage("Final state in states map is not equal to the final state: " + finalState.getName(), -1, ValidationMessageType.ERROR));
                    }
                }
            }
        }

        return validationWarnings;
    }

    /**
     * Validates the alphabet symbols of the NFA.
     * Ensures each symbol is valid and properly formatted.
     *
     * @return list of {@link ValidationMessage} objects indicating issues with alphabet symbols
     */
    private List<ValidationMessage> validateAlphabet(){
        List<ValidationMessage> validationWarnings = new ArrayList<>();

        Set<Symbol> alphabet = this.alphabet;
        if (alphabet == null || alphabet.isEmpty()){
            validationWarnings.add(new ValidationMessage("Alphabet is empty", -1, ValidationMessageType.ERROR));
        }else {
            for (Symbol symbol : alphabet){
                validationWarnings.addAll(validateSymbol(symbol));
            }
        }

        return validationWarnings;
    }

    /**
     * Validates the transitions of the NFA.
     * Checks for valid source and target states and verifies that symbols exist in the alphabet.
     *
     * @return list of {@link ValidationMessage} objects indicating any transition-related issues
     */
    private List<ValidationMessage> validateTransitions(){
        List<ValidationMessage> validationWarnings = new ArrayList<>();

        Map<State, List<Transition>> transitions = this.transitions;
        for (State fromState : transitions.keySet()){
            if (!states.containsKey(fromState.getName())){
                validationWarnings.add(new ValidationMessage("States map does not contain the transition fromState: " + fromState.getName(), -1, ValidationMessageType.ERROR));
            }
            if (!fromState.getName().matches(statePattern)){
                validationWarnings.add(new ValidationMessage("Transition fromState: " + fromState.getName() + " does not match valid state name", -1, ValidationMessageType.ERROR));
            }

            if (transitions.get(fromState) != null) {
                for (Transition t : transitions.get(fromState)){
                    List<ValidationMessage> transitionSymbolWarnings = validateSymbol(t.getSymbol());
                    validationWarnings.addAll(transitionSymbolWarnings);

                    if (alphabet != null && !t.getSymbol().isEpsilon() && !alphabet.contains(t.getSymbol())){
                        validationWarnings.add(new ValidationMessage("Alphabet does not contain transition symbol: " + t.getSymbol().getValue(), -1, ValidationMessageType.ERROR));
                    }

                    State toState = t.getTo();
                    if (!states.containsKey(toState.getName())){
                        validationWarnings.add(new ValidationMessage("States map does not contain the transition toState: " + toState.getName(), -1, ValidationMessageType.ERROR));
                    }
                    if (!toState.getName().matches(statePattern)){
                        validationWarnings.add(new ValidationMessage("Transition toState: " + toState.getName() + " does not match valid state name", -1, ValidationMessageType.ERROR));
                    }
                }
            }
        }
        return validationWarnings;
    }

    /**
     * Validates the consistency and correctness of state definitions.
     * Checks for proper naming, counts of start/final states, and reports errors accordingly.
     *
     * @return list of {@link ValidationMessage} objects indicating any issues found
     */
    private List<ValidationMessage> validateStates(){
        List<ValidationMessage> validationWarnings = new ArrayList<>();

        int startCount = 0;
        int finalCount = 0;
        for (State state : states.values()){
            if (!state.getName().matches(statePattern)){
                validationWarnings.add(new ValidationMessage("State name: " + state.getName() + " does not match valid state name", -1, ValidationMessageType.ERROR));
            }
            if (state.isStart()){
                startCount++;
            }
            if (state.isAccept()){
                finalCount++;
            }
        }

        if (startCount == 0){
            validationWarnings.add(new ValidationMessage("No start state in states map", -1, ValidationMessageType.ERROR));
        }else if (startCount > 1){
            validationWarnings.add(new ValidationMessage("More than one start state in states map", -1, ValidationMessageType.ERROR));
        }

        if (finalCount == 0){
            validationWarnings.add(new ValidationMessage("No final state in states map", -1, ValidationMessageType.ERROR));
        }

        return validationWarnings;
    }


    /**
     * Validates a given symbol to ensure it is either a letter or an epsilon symbol.
     * Symbols must be alphabetic characters or represent an epsilon transition.
     *
     * @param symbol the symbol to validate
     * @return a list of ValidationMessages indicating any validation errors
     */
    private List<ValidationMessage> validateSymbol(Symbol symbol){
        List<ValidationMessage> warnings = new ArrayList<>();

        if (!(symbol.isEpsilon() || Character.isLetterOrDigit(symbol.getValue()))) {
            warnings.add(new ValidationMessage("Symbol is not valid: " + symbol.getValue(), -1, ValidationMessage.ValidationMessageType.ERROR));
        }
        return warnings;
    }

    /**
     * Generates a DOT language representation of the NFA for visualization.
     *
     * @param inputText the original input (not used in DOT generation)
     * @return DOT format string
     */
    @Override
    public String toDotCode(String inputText) {
        if (this.states == null || this.states.isEmpty()) {
            return "digraph NFA {\n\tlabel=\"Automaton not parsed or is empty\";\n}";
        }

        StringBuilder dot = new StringBuilder("digraph NFA {\n");
        dot.append("  rankdir=LR;\n");
        dot.append("  node [shape = circle];\n");

        for (State state : finalStates) {
            dot.append(String.format("  \"%s\" [shape=doublecircle];\n", state.getName()));
        }

        if (startState != null) {
            dot.append("  \"start\" [shape=point];\n");
            dot.append(String.format("  \"start\" -> \"%s\";\n", startState.getName()));
        }

        List<Transition> addedTransitions = new ArrayList<>();

        for (List<Transition> transitions : transitions.values()) {
            for (Transition t : transitions) {
                if (!addedTransitions.contains(t)) {

                    List<Transition> sameTransitionsFromState = getSameTransitionsFromState(t.getFrom(), t.getTo());

                    List<String> symbols = new ArrayList<>();
                    boolean epsilonExists = false;

                    for (Transition sameTransition : sameTransitionsFromState) {
                            addedTransitions.add(sameTransition);

                            if (sameTransition.getSymbol().isEpsilon()){
                                epsilonExists = true;
                            }else {
                                symbols.add(String.valueOf(sameTransition.getSymbol().getValue()));
                            }
                    }

                    String label = String.join(", ", symbols);

                    if (!label.isEmpty()){
                        dot.append(String.format("  \"%s\" -> \"%s\" [label=\"%s\"];\n",
                                t.getFrom().getName(), t.getTo().getName(), label));
                    }

                    if (epsilonExists){
                        dot.append(String.format("  \"%s\" -> \"%s\" [label=\"%s\", style=\"dashed\"];\n",
                                t.getFrom().getName(), t.getTo().getName(), "ε"));
                    }

                }

            }
        }

        dot.append("}");
        return dot.toString();
    }

    /**
     * Returns a list of transitions from a specific source state to a target state.
     *
     * @param fromState the source state
     * @param toState the destination state
     * @return list of transitions between the given states
     */
    private List<Transition> getSameTransitionsFromState(State fromState, State toState){

        List<Transition> transitions = new ArrayList<>();

        for (Transition t : this.transitions.get(fromState)){
            if (t.getTo().equals(toState)){
                transitions.add(t);
            }
        }

        return transitions;
    }

    /**
     * Returns a human-readable string representation of the NFA's components.
     *
     * @return formatted string representing the NFA
     */
    public String prettyPrint(){
        StringBuilder sb = new StringBuilder();

        sb.append("NFA\n");
        sb.append("States:\n");
        for (State state : this.states.values()){
            sb.append(state.prettyPrint()).append("\n");
        }

        sb.append("Alphabet:\n");
        for (Symbol symbol : this.alphabet){
            sb.append(symbol.prettyPrint()).append("\n");
        }

        sb.append("StartState:\n");
        sb.append(this.startState.prettyPrint()).append("\n");

        sb.append("FinalStates:\n");
        for (State state : this.finalStates){
            sb.append(state.prettyPrint()).append("\n");
        }

        sb.append("Transitions:\n");
        for (List<Transition> transitions : this.transitions.values()){
            for (Transition transition : transitions){
                sb.append(transition.prettyPrint()).append("\n");
            }
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return super.toString();
    }

    public Map<String, State> getStates() {
        return states;
    }

    public Set<Symbol> getAlphabet() {
        return alphabet;
    }

    public State getStartState() {
        return startState;
    }

    public Set<State> getFinalStates() {
        return finalStates;
    }

    public Map<State, List<Transition>> getTransitions() {
        return transitions;
    }

    @Override
    public String getDefaultTemplate() {
        return "Start: q1\n" +
               "Finals: q2\n" +
               "Alphabet: a b\n" +
               "States: q1 q2\n" +
               "\n" +
               "Transitions:\n" +
               "q1 -> q2 (a b eps)\n" +
               "q2 -> q2 (a b)\n";
    }
}
