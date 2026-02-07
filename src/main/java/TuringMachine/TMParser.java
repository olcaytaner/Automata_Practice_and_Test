package TuringMachine;


import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses a Turing Machine definition from a string.
 */
public class TMParser {

    private static final Pattern TRANSITION_LINE = Pattern.compile("\\s*(\\S+)\\s*,\\s*(\\S+)\\s*->\\s*(\\S+)\\s*,\\s*(\\S+)\\s*,\\s*([LR])\\s*");

    /**
     * Parses a Turing Machine definition from a string.
     * @param content The string content to parse.
     * @return A new TM object.
     */
    public static TM parse(String content) {
        ParseContext context = new ParseContext();
        parseContent(content, context);

        createStates(context);
        createAlphabets(context);
        createTransitions(context);

        return new TM(
                context.states,
                context.inputAlphabet,
                context.tapeAlphabet,
                context.transitionFunction,
                context.startState,
                context.acceptState,
                context.rejectState
        );
    }

    private static void parseContent(String content, ParseContext context) {
        String[] lines = content.split("\\R");
        boolean inTransitions = false;

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;

            String[] parts = trimmed.split(":", 2);
            if (parts.length == 2) {
                String header = parts[0].trim().toLowerCase();
                String sectionContent = parts[1].trim();
                // Normalize Smart Text keywords to internal keys
                if (header.equals("input")) {
                    header = "input_alphabet";
                } else if (header.equals("tape")) {
                    header = "tape_alphabet";
                }
                context.sectionContent.put(header, sectionContent);
                inTransitions = header.equals("transitions");
            } else if (inTransitions) {
                context.transitionLines.add(trimmed);
            }
        }
    }

    private static void createStates(ParseContext context) {
        String statesRaw = context.sectionContent.get("states");
        String acceptStateName = context.sectionContent.get("accept");
        String startStateName = context.sectionContent.get("start");

        for (String s : statesRaw.split("\\s+")) {
            State newState = new State(s, s.equalsIgnoreCase("q_accept"), s.equalsIgnoreCase("q_reject"));
            if (s.equals(startStateName)) {
                newState.setStart(true);
            }
            context.stateMap.put(s, newState);
            context.states.add(newState);
        }

        context.startState = context.stateMap.get(startStateName);
        context.acceptState = context.stateMap.get(acceptStateName);

        // The reject state is optional
        if (context.sectionContent.containsKey("reject")) {
            String rejectStateName = context.sectionContent.get("reject");
            context.rejectState = context.stateMap.get(rejectStateName);
        } else {
            // If no reject state is specified, create a default one.
            State defaultReject = new State("q_reject", false, true);
            if (!context.stateMap.containsKey("q_reject")) {
                context.stateMap.put("q_reject", defaultReject);
                context.states.add(defaultReject);
            }
            context.rejectState = context.stateMap.get("q_reject");
        }
    }

    private static void createAlphabets(ParseContext context) {
        String inputAlphabetRaw = context.sectionContent.get("input_alphabet");
        for (String s : inputAlphabetRaw.split("\\s+")) {
            context.inputAlphabet.addSymbol(s.charAt(0));
        }

        String tapeAlphabetRaw = context.sectionContent.get("tape_alphabet");
        for (String s : tapeAlphabetRaw.split("\\s+")) {
            context.tapeAlphabet.addSymbol(s.charAt(0));
        }
    }

    private static void createTransitions(ParseContext context) {
        for (String line : context.transitionLines) {
            Matcher m = TRANSITION_LINE.matcher(line);
            if (m.matches()) {
                String fromStateName = m.group(1);
                char symbolToRead = m.group(2).charAt(0);
                String toStateName = m.group(3);
                char symbolToWrite = m.group(4).charAt(0);
                TMTransition.Direction direction = m.group(5).equalsIgnoreCase("R") ? TMTransition.Direction.RIGHT : TMTransition.Direction.LEFT;

                State fromState = context.stateMap.get(fromStateName);
                State toState = context.stateMap.get(toStateName);

                ConfigurationKey key = new ConfigurationKey(fromState, symbolToRead);
                TMTransition transition = new TMTransition(fromState, symbolToRead, toState, symbolToWrite, direction);

                context.transitionFunction.put(key, transition);
            }
        }
    }

    private static class ParseContext {
        final Map<String, String> sectionContent = new HashMap<>();
        final List<String> transitionLines = new ArrayList<>();
        final Map<String, State> stateMap = new HashMap<>();
        final Set<State> states = new HashSet<>();
        final Alphabet inputAlphabet = new Alphabet();
        final Alphabet tapeAlphabet = new Alphabet();
        final Map<ConfigurationKey, TMTransition> transitionFunction = new HashMap<>();
        State startState, acceptState, rejectState;
    }
}