package RegularExpression;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import common.Automaton;
import common.ExecutionResult;
import common.InputNormalizer;
import common.MachineType;
import common.ParseResult;
import common.ValidationMessage;

/**
 * Represents a regular expression that extends Automaton.
 * This is the main entry point for working with regular expressions.
 * <p>
 * Internally, this class uses a {@link RegexSyntaxTree} for parsing and matching.
 * </p>
 *
 * <p><strong>Example usage:</strong></p>
 * <pre>{@code
 * // Regex matches strings like "0(01)*1" — multiples of 3 in this case
 * String regex = "0(01)*1";
 * char[] alphabet = {'0', '1'};
 * RegularExpression re = new RegularExpression(regex, alphabet);
 *
 * // Test matching
 * boolean matches = re.match("00101");
 * }</pre>
 *
 * @version 3.0
 */
public class RegularExpression extends Automaton {
    private RegexSyntaxTree syntaxTree;
    private char[] alphabet;
    private String regex;

    /**
     * Creates an empty RegularExpression.
     * Use {@link #parse(String)} to initialize from a text definition.
     */
    public RegularExpression() {
        super(MachineType.REGEX);
        this.syntaxTree = new RegexSyntaxTree();
    }

    /**
     * Creates a RegularExpression from a regex string and alphabet.
     *
     * @param regex    The regular expression pattern
     * @param alphabet The alphabet of valid characters
     */
    public RegularExpression(String regex, char[] alphabet) {
        super(MachineType.REGEX);
        this.regex = regex;
        this.alphabet = alphabet;
        this.syntaxTree = new RegexSyntaxTree(regex, alphabet);
    }

    /**
     * Matches the input string against the regex pattern.
     *
     * @param input The string to match
     * @return true if the string matches the pattern
     */
    public boolean match(String input) {
        return syntaxTree.match(input);
    }

    @Override
    public String getDefaultTemplate() {
        return "alphabet: a b c d\npattern: a*b(c u d)\n";
    }

    /**
     * RegularExpression does not support graphical visualization.
     * Returns an empty string as there is no meaningful DOT representation.
     *
     * @param inputText The input text (unused)
     * @return Empty string - no visualization available
     */
    @Override
    public String toDotCode(String inputText) {
        return "";  // No visualization for regex
    }

    @Override
    public ParseResult parse(String inputText) {
        if (inputText == null) {
            throw new NullPointerException("Input text cannot be null");
        }

        InputNormalizer.NormalizedInput normalizedInput = InputNormalizer.normalize(inputText, MachineType.REGEX);
        List<ValidationMessage> messages = new ArrayList<>(normalizedInput.getMessages());
        Map<String, List<String>> sections = normalizedInput.getSections();
        Map<String, Integer> sectionLineNumbers = normalizedInput.getSectionLineNumbers();

        if (normalizedInput.hasErrors()) {
            return new ParseResult(false, messages, null);
        }

        if (!InputNormalizer.validateRequiredKeywords(sections, MachineType.REGEX, messages)) {
            return new ParseResult(false, messages, null);
        }

        // Parse alphabet
        List<String> alphabetTokens = sections.get("alphabet");
        int alphabetLine = sectionLineNumbers.getOrDefault("alphabet", 0);
        Set<Character> alphabetSet = new LinkedHashSet<>();
        if (alphabetTokens == null || alphabetTokens.isEmpty()) {
            messages.add(new ValidationMessage("The 'alphabet' line cannot be empty.", alphabetLine, ValidationMessage.ValidationMessageType.ERROR));
        } else {
            for (String token : alphabetTokens) {
                if (token == null) continue;
                String t = token.trim();
                if (t.length() != 1) {
                    messages.add(new ValidationMessage("Invalid alphabet symbol: '" + t + "' (must be a single character)", alphabetLine, ValidationMessage.ValidationMessageType.ERROR));
                    continue;
                }
                char ch = t.charAt(0);
                if (!alphabetSet.add(ch)) {
                    messages.add(new ValidationMessage("Duplicate alphabet symbol: '" + ch + "'", alphabetLine, ValidationMessage.ValidationMessageType.WARNING));
                }
            }
        }

        boolean hasErrors = messages.stream().anyMatch(m -> m.getType() == ValidationMessage.ValidationMessageType.ERROR);
        if (hasErrors) {
            return new ParseResult(false, messages, null);
        }

        // Build alphabet array
        char[] parsedAlphabet = new char[alphabetSet.size()];
        int idx = 0;
        for (char c : alphabetSet) parsedAlphabet[idx++] = c;

        // Parse regex and compile syntax tree
        String regexStr = sections.get("regex").get(0);
        int regexLine = sectionLineNumbers.getOrDefault("regex", 0);
        try {
            this.alphabet = parsedAlphabet;
            this.regex = regexStr;
            this.syntaxTree = new RegexSyntaxTree(regexStr, parsedAlphabet);
        } catch (IllegalArgumentException e) {
            messages.add(new ValidationMessage(e.getMessage(), regexLine, ValidationMessage.ValidationMessageType.ERROR));
        } catch (Exception e) {
            messages.add(new ValidationMessage("Failed to compile regex: " + e.getMessage(), regexLine, ValidationMessage.ValidationMessageType.ERROR));
        }

        boolean isSuccess = messages.stream().noneMatch(m -> m.getType() == ValidationMessage.ValidationMessageType.ERROR);

        if (isSuccess) {
            messages.addAll(validate());
            isSuccess = messages.stream().noneMatch(m -> m.getType() == ValidationMessage.ValidationMessageType.ERROR);
        }

        return new ParseResult(isSuccess, messages, isSuccess ? this : null);
    }

    @Override
    public ExecutionResult execute(String inputText) {
        List<ValidationMessage> runtimeMessages = new ArrayList<>();
        StringBuilder trace = new StringBuilder();

        if (syntaxTree == null || syntaxTree.root == null || alphabet == null) {
            runtimeMessages.add(new ValidationMessage("Regex is not parsed/compiled.", -1, ValidationMessage.ValidationMessageType.ERROR));
            return new ExecutionResult(false, runtimeMessages, trace.toString());
        }

        // Check if input chars belong to the alphabet
        for (int i = 0; i < inputText.length(); i++) {
            char c = inputText.charAt(i);
            if (!syntaxTree.alphabetHas(c)) {
                runtimeMessages.add(new ValidationMessage("Symbol not in alphabet: " + c, -1, ValidationMessage.ValidationMessageType.ERROR));
                return new ExecutionResult(false, runtimeMessages, trace.toString());
            }
        }

        Set<Integer> ends = syntaxTree.root.match(inputText, 0);
        boolean accepted = ends.contains(inputText.length());
        trace.append("Ends: ").append(ends).append("\n");
        trace.append(accepted ? "ACCEPT" : "REJECT");

        return new ExecutionResult(accepted, runtimeMessages, trace.toString());
    }

    @Override
    public List<ValidationMessage> validate() {
        List<ValidationMessage> validationWarnings = new ArrayList<>();

        if (alphabet == null || alphabet.length == 0) {
            validationWarnings.add(new ValidationMessage("Alphabet is empty", -1, ValidationMessage.ValidationMessageType.ERROR));
        }

        if (syntaxTree == null || syntaxTree.root == null) {
            validationWarnings.add(new ValidationMessage("Regex syntax tree is not built", -1, ValidationMessage.ValidationMessageType.ERROR));
        }

        return validationWarnings;
    }

    /**
     * Validates the sanitized regex length against a maximum allowed length.
     *
     * @param maxLength Maximum allowed length for the sanitized regex (null means no limit)
     * @return Validation message if length exceeds limit, null otherwise
     */
    public ValidationMessage validateRegexLength(Integer maxLength) {
        if (maxLength == null || syntaxTree == null) {
            return null;
        }

        int actualLength = syntaxTree.getSanitizedRegexLength();
        if (actualLength > maxLength) {
            String message = String.format(
                    "Regex exceeds maximum allowed length: %d characters (limit: %d). " +
                            "The sanitized regex has %d characters after whitespace removal and normalization.",
                    actualLength, maxLength, actualLength
            );
            return new ValidationMessage(message, -1, ValidationMessage.ValidationMessageType.ERROR);
        }

        return null;
    }

    /**
     * Gets the length of the sanitized regex.
     *
     * @return Length of sanitized regex, or 0 if not parsed
     */
    public int getSanitizedRegexLength() {
        return syntaxTree != null ? syntaxTree.getSanitizedRegexLength() : 0;
    }

    /**
     * Gets the internal syntax tree (for backward compatibility with old code).
     *
     * @return The internal RegexSyntaxTree
     */
    public RegexSyntaxTree getSyntaxTree() {
        return syntaxTree;
    }

    // ============================================
    // Case generation methods (for testing/grading)
    // ============================================

    /**
     * Generates a single random string matching this regex.
     *
     * @return A random matching string
     */
    public String generateOneCase() {
        return syntaxTree.root.generateOneCase();
    }

    /**
     * Generates a single random string with controlled star repetition.
     *
     * @param maxStarRepeat Maximum number of repetitions for Kleene star
     * @return A random matching string
     */
    public String generateOneCase(int maxStarRepeat) {
        return syntaxTree.root.generateOneCase(maxStarRepeat);
    }

    /**
     * Generates multiple random matching strings.
     *
     * @param cnt Number of cases to generate
     * @return Set of matching strings
     */
    public Set<String> generateCases(int cnt) {
        Set<String> cases = new HashSet<>();
        while (cases.size() != cnt)
            cases.add(generateOneCase());
        return cases;
    }

    /**
     * Generates multiple random matching strings with controlled star repetition.
     *
     * @param cnt           Number of cases to generate
     * @param maxStarRepeat Maximum number of repetitions for Kleene star
     * @return Set of matching strings
     */
    public Set<String> generateCases(int cnt, int maxStarRepeat) {
        Set<String> cases = new HashSet<>();
        while (cases.size() != cnt)
            cases.add(generateOneCase(maxStarRepeat));
        return cases;
    }

    /**
     * Generates all correct cases exhaustively up to maxLen.
     *
     * @param maxLen Maximum length of generated strings
     * @return Set of all matching strings
     */
    public Set<String> generateCorrectCasesExhaustive(int maxLen) {
        return syntaxTree.root.generateCasesExhaustive(maxLen);
    }

    /**
     * Generate correct cases for the regex and save to file.
     *
     * @param maxLen Max length of the case
     * @param path   Path where you wish to save the cases to
     * @param append Whether to append to file
     * @return Number of cases generated
     */
    public int generateCorrectCasesExhaustive(int maxLen, String path, boolean append) {
        Set<String> cases = syntaxTree.root.generateCasesExhaustive(maxLen);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path, append))) {
            for (String case_ : cases) {
                bw.write(case_);
                bw.write(",1");
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Write unsuccessful");
            e.printStackTrace();
        }
        return cases.size();
    }

    /**
     * Generate wrong cases for the regex and save to file.
     *
     * @param maxLen Max length of the case
     * @param path   Path where you wish to save the cases to
     * @param append Whether to append to file
     * @return Number of cases generated
     */
    public int generateWrongCasesExhaustive(int maxLen, String path, boolean append) {
        Set<String> cases = new HashSet<>();
        generateWrongCasesRec(maxLen, new StringBuilder(), cases);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path, append))) {
            for (String case_ : cases) {
                bw.write(case_);
                bw.write(",0");
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Write unsuccessful");
            e.printStackTrace();
        }
        return cases.size();
    }

    private void generateWrongCasesRec(int maxLen, StringBuilder curCase, Set<String> cases) {
        String s = curCase.toString();
        if (!match(s))
            cases.add(s);
        if (curCase.length() == maxLen) {
            curCase.deleteCharAt(curCase.length() - 1);
            return;
        }
        for (char c : alphabet) {
            curCase.append(c);
            generateWrongCasesRec(maxLen, curCase, cases);
        }
        if (curCase.length() > 0)
            curCase.deleteCharAt(curCase.length() - 1);
    }
}
