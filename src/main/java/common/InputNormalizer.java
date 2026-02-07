package common;

import java.util.*;

/**
 * Utility class for normalizing input text across all automaton types.
 * Provides unified input parsing and validation functionality.
 *
 * @version 2.0
 */
public class InputNormalizer {

    public static class NormalizedInput {
        private final Map<String, List<String>> sections;
        private final Map<String, Integer> sectionLineNumbers;
        private final List<ValidationMessage> messages;

        public NormalizedInput(Map<String, List<String>> sections,
                               Map<String, Integer> sectionLineNumbers,
                               List<ValidationMessage> messages) {
            this.sections = sections;
            this.sectionLineNumbers = sectionLineNumbers;
            this.messages = messages;
        }

        public Map<String, List<String>> getSections() {
            return sections;
        }

        public Map<String, Integer> getSectionLineNumbers() {
            return sectionLineNumbers;
        }

        public List<ValidationMessage> getMessages() {
            return messages;
        }

        public boolean hasErrors() {
            return messages.stream().anyMatch(m ->
                    m.getType() == ValidationMessage.ValidationMessageType.ERROR);
        }
    }

    /**
     * Normalizes input text to a standard format with consistent keywords and structure.
     * Handles various input formats and converts them to the standard format.
     */
    public static NormalizedInput normalize(String inputText, MachineType machineType) {
        List<ValidationMessage> messages = new ArrayList<>();
        Map<String, List<String>> sections = new HashMap<>();
        Map<String, Integer> sectionLineNumbers = new HashMap<>();

        if (inputText == null || inputText.trim().isEmpty()) {
            messages.add(new ValidationMessage("Input text is empty", 1,
                    ValidationMessage.ValidationMessageType.ERROR));
            return new NormalizedInput(sections, sectionLineNumbers, messages);
        }

        switch (machineType) {
            case CFG:
                return normalizeCFGInput(inputText, messages);
            case REGEX:
                return normalizeRegexInput(inputText, messages);
            default:
                return normalizeStandardInput(inputText, messages);
        }
    }

    /**
     * Normalizes CFG input from Smart Text colon-based format (vars:, terminals:, start:, rules:)
     */
    private static NormalizedInput normalizeCFGInput(String inputText, List<ValidationMessage> messages) {
        Map<String, List<String>> sections = new HashMap<>();
        Map<String, Integer> sectionLineNumbers = new HashMap<>();

        String[] lines = inputText.split("\\R");
        String currentSection = null;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            int colonIndex = line.indexOf(":");
            if (colonIndex != -1) {
                String keyword = line.substring(0, colonIndex).trim().toLowerCase();
                String data = line.substring(colonIndex + 1).trim();

                // Normalize CFG keyword variations
                keyword = normalizeCFGKeyword(keyword);

                if (keyword != null) {
                    currentSection = keyword;
                    if (sections.containsKey(currentSection)) {
                        messages.add(new ValidationMessage(
                                "Duplicate keyword '" + currentSection + "'. Only the first definition will be used.",
                                i + 1, ValidationMessage.ValidationMessageType.WARNING));
                        currentSection = null;
                        continue;
                    }

                    sections.put(currentSection, new ArrayList<>());
                    sectionLineNumbers.put(currentSection, i + 1);

                    if (!data.isEmpty()) {
                        if ("productions".equals(currentSection)) {
                            sections.get(currentSection).add(data);
                        } else {
                            sections.get(currentSection).addAll(Arrays.asList(data.split("\\s+")));
                        }
                    }
                } else if (currentSection != null) {
                    // Line with colon but not a keyword — could be a production rule like S -> aA
                    sections.get(currentSection).add(line);
                }
            } else if (currentSection != null) {
                sections.get(currentSection).add(line);
            } else if (line.contains("->")) {
                // Production line before any section — add to productions
                if (!sections.containsKey("productions")) {
                    sections.put("productions", new ArrayList<>());
                    sectionLineNumbers.put("productions", i + 1);
                }
                sections.get("productions").add(line);
            }
        }

        return new NormalizedInput(sections, sectionLineNumbers, messages);
    }

    /**
     * Normalizes CFG-specific keywords to canonical form.
     */
    private static String normalizeCFGKeyword(String keyword) {
        switch (keyword) {
            case "vars":
            case "variables":
                return "variables";
            case "terminals":
                return "terminals";
            case "start":
                return "start";
            case "rules":
            case "productions":
                return "productions";
            default:
                return null;
        }
    }

    /**
     * Normalizes Regular Expression input from Smart Text format (alphabet: + pattern:)
     */
    private static NormalizedInput normalizeRegexInput(String inputText, List<ValidationMessage> messages) {
        Map<String, List<String>> sections = new HashMap<>();
        Map<String, Integer> sectionLineNumbers = new HashMap<>();

        String[] lines = inputText.split("\\R");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            int colonIndex = line.indexOf(":");
            if (colonIndex != -1) {
                String keyword = line.substring(0, colonIndex).trim().toLowerCase();
                String data = line.substring(colonIndex + 1).trim();

                if ("alphabet".equals(keyword) || "sigma".equals(keyword)) {
                    String[] alphabetChars;
                    if (data.contains(",")) {
                        alphabetChars = data.split(",");
                    } else {
                        alphabetChars = data.split("\\s+");
                    }
                    List<String> cleanedAlphabet = new ArrayList<>();
                    for (String ch : alphabetChars) {
                        String cleaned = ch.trim();
                        if (!cleaned.isEmpty()) {
                            cleanedAlphabet.add(cleaned);
                        }
                    }
                    sections.put("alphabet", cleanedAlphabet);
                    sectionLineNumbers.put("alphabet", i + 1);
                } else if ("pattern".equals(keyword) || "regex".equals(keyword)) {
                    sections.put("regex", Arrays.asList(data));
                    sectionLineNumbers.put("regex", i + 1);
                }
            }
        }

        return new NormalizedInput(sections, sectionLineNumbers, messages);
    }

    /**
     * Normalizes standard input format (DFA, NFA, PDA, TM)
     */
    private static NormalizedInput normalizeStandardInput(String inputText, List<ValidationMessage> messages) {
        Map<String, List<String>> sections = new HashMap<>();
        Map<String, Integer> sectionLineNumbers = new HashMap<>();

        String[] lines = inputText.split("\\R");
        String currentSection = null;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            int colonIndex = line.indexOf(":");
            if (colonIndex != -1) {
                currentSection = line.substring(0, colonIndex).trim().toLowerCase();
                String data = line.substring(colonIndex + 1).trim();

                // Normalize keyword variations
                currentSection = normalizeKeyword(currentSection);

                if (sections.containsKey(currentSection)) {
                    messages.add(new ValidationMessage(
                            "Duplicate keyword '" + currentSection + "'. Only the first definition will be used.",
                            i + 1, ValidationMessage.ValidationMessageType.WARNING));
                    currentSection = null;
                    continue;
                }

                sections.put(currentSection, new ArrayList<>());
                sectionLineNumbers.put(currentSection, i + 1);
                sectionLineNumbers.put(line, i + 1);

                if (!data.isEmpty()) {
                    sections.get(currentSection).add(data);
                    sectionLineNumbers.put(data, i + 1);
                }
            } else if (currentSection != null) {
                sections.get(currentSection).add(line);
                sectionLineNumbers.put(line, i + 1);
            } else {
                messages.add(new ValidationMessage(
                        "Undefined content. All content must be under a keyword section.",
                        i + 1, ValidationMessage.ValidationMessageType.ERROR));
            }
        }

        return new NormalizedInput(sections, sectionLineNumbers, messages);
    }

    /**
     * Normalizes keyword variations to standard form.
     * Smart Text canonical keywords: states, alphabet, start, accept/finals,
     * input (→alphabet), tape (→tape_alphabet), stack (→stack_alphabet),
     * reject, transitions, stack_start
     */
    private static String normalizeKeyword(String keyword) {
        switch (keyword.toLowerCase()) {
            // Accept/finals variations
            case "finals":
            case "final":
            case "accepting":
            case "accept":
                return "finals";
            // Input alphabet variations
            case "alphabet":
            case "sigma":
            case "input":
            case "input_alphabet":
            case "inputalphabet":
                return "alphabet";
            // Tape alphabet variations
            case "tape":
            case "tape_alphabet":
            case "tapealphabet":
                return "tape_alphabet";
            // Stack alphabet variations
            case "stack":
            case "stack_alphabet":
            case "stackalphabet":
                return "stack_alphabet";
            // Stack start variations
            case "stack_start":
            case "stackstart":
                return "stack_start";
            // Reject variations
            case "reject":
            case "rejecting":
                return "reject";
            default:
                return keyword.toLowerCase();
        }
    }

    /**
     * Extracts content after '=' character, handling various formats
     */
    private static String extractAfterEquals(String line) {
        int equalsIndex = line.indexOf('=');
        if (equalsIndex != -1 && equalsIndex < line.length() - 1) {
            return line.substring(equalsIndex + 1).trim();
        }
        return "";
    }

    /**
     * Validates that required keywords are present for a specific machine type
     */
    public static boolean validateRequiredKeywords(Map<String, List<String>> sections,
                                                   MachineType machineType,
                                                   List<ValidationMessage> messages) {
        String[] requiredKeys;

        switch (machineType) {
            case DFA:
            case NFA:
                requiredKeys = new String[]{"states", "alphabet", "start", "finals", "transitions"};
                break;
            case PDA:
                requiredKeys = new String[]{"states", "alphabet", "stack_alphabet", "start", "finals", "transitions"};
                break;
            case TM:
                requiredKeys = new String[]{"states", "alphabet", "tape_alphabet", "start", "accept", "reject", "transitions"};
                break;
            case CFG:
                requiredKeys = new String[]{"variables", "terminals", "start", "productions"};
                break;
            case REGEX:
                requiredKeys = new String[]{"regex", "alphabet"};
                break;
            default:
                return true;
        }

        boolean allFound = true;
        for (String key : requiredKeys) {
            if (!sections.containsKey(key)) {
                messages.add(new ValidationMessage(
                        "Missing required keyword definition for '" + key + ":'.",
                        0, ValidationMessage.ValidationMessageType.ERROR));
                allFound = false;
            }
        }

        return allFound;
    }
}
