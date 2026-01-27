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
     * Normalizes CFG input from "Variables = ..." format to "variables: ..." format
     */
    private static NormalizedInput normalizeCFGInput(String inputText, List<ValidationMessage> messages) {
        Map<String, List<String>> sections = new HashMap<>();
        Map<String, Integer> sectionLineNumbers = new HashMap<>();

        String[] lines = inputText.split("\\R");
        List<String> productionLines = new ArrayList<>();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            String lowercaseLine = line.toLowerCase();

            if (lowercaseLine.startsWith("variables =") || lowercaseLine.startsWith("variables=")) {
                String content = extractAfterEquals(line);
                sections.put("variables", Arrays.asList(content.split("\\s+")));
                sectionLineNumbers.put("variables", i + 1);
            } else if (lowercaseLine.startsWith("terminals =") || lowercaseLine.startsWith("terminals=")) {
                String content = extractAfterEquals(line);
                sections.put("terminals", Arrays.asList(content.split("\\s+")));
                sectionLineNumbers.put("terminals", i + 1);
            } else if (lowercaseLine.startsWith("start =") || lowercaseLine.startsWith("start=")) {
                String content = extractAfterEquals(line);
                sections.put("start", Arrays.asList(content.trim()));
                sectionLineNumbers.put("start", i + 1);
            } else if (line.contains("->")) {
                productionLines.add(line);
                if (!sectionLineNumbers.containsKey("productions")) {
                    sectionLineNumbers.put("productions", i + 1);
                }
            }
        }

        if (!productionLines.isEmpty()) {
            sections.put("productions", productionLines);
        }

        return new NormalizedInput(sections, sectionLineNumbers, messages);
    }

    /**
     * Normalizes Regular Expression input to standard format
     */
    private static NormalizedInput normalizeRegexInput(String inputText, List<ValidationMessage> messages) {
        Map<String, List<String>> sections = new HashMap<>();
        Map<String, Integer> sectionLineNumbers = new HashMap<>();

        String[] lines = inputText.split("\\R");
        List<String> nonEmptyLines = new ArrayList<>();

        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty() && !line.startsWith("#")) {
                nonEmptyLines.add(line);
            }
        }

        if (nonEmptyLines.size() >= 1) {
            sections.put("regex", Arrays.asList(nonEmptyLines.get(0)));
            sectionLineNumbers.put("regex", 1);
        }

        if (nonEmptyLines.size() >= 2) {
            // Parse alphabet from comma or space separated format
            String alphabetLine = nonEmptyLines.get(1);
            String[] alphabetChars;
            if (alphabetLine.contains(",")) {
                alphabetChars = alphabetLine.split(",");
            } else {
                alphabetChars = alphabetLine.split("\\s+");
            }

            List<String> cleanedAlphabet = new ArrayList<>();
            for (String ch : alphabetChars) {
                String cleaned = ch.trim();
                if (!cleaned.isEmpty()) {
                    cleanedAlphabet.add(cleaned);
                }
            }

            sections.put("alphabet", cleanedAlphabet);
            sectionLineNumbers.put("alphabet", 2);
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
     * Normalizes keyword variations to standard form
     */
    private static String normalizeKeyword(String keyword) {
        switch (keyword.toLowerCase()) {
            case "finals":
            case "final":
            case "accepting":
            case "accept":
                return "finals";
            case "alphabet":
            case "sigma":
                return "alphabet";
            case "input_alphabet":
            case "inputalphabet":
                return "alphabet";
            case "tape_alphabet":
            case "tapealphabet":
                return "tape_alphabet";
            case "stack_alphabet":
            case "stackalphabet":
                return "stack_alphabet";
            case "stack_start":
            case "stackstart":
                return "stack_start";
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
