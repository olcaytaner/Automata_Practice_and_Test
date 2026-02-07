package RegularExpression.Checker;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import RegularExpression.RegularExpression;

/**
 * Check utility:
 * - Reads the regular expression from the first non-empty line of regexPath.
 * - Automatically extracts the alphabet from characters used in the regex (letters/digits),
 *   ignoring common regex meta-characters and the union operator 'u'.
 * - Reads casesPath where each non-empty line is "testString,1" or "testString,0".
 */
public class Check {
    public static void check(String regexPath, String casesPath) {
        RegularExpression re;

        String regex = null;
        char[] alphabet = null;
        try (BufferedReader br = new BufferedReader(new FileReader(regexPath))) {
            String ln;
            while ((ln = br.readLine()) != null) {
                String trimmed = ln.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;

                // Smart Text format: keyword: value
                int colonIdx = trimmed.indexOf(":");
                if (colonIdx != -1) {
                    String keyword = trimmed.substring(0, colonIdx).trim().toLowerCase();
                    String data = trimmed.substring(colonIdx + 1).trim();
                    if ("pattern".equals(keyword) || "regex".equals(keyword)) {
                        regex = data;
                    } else if ("alphabet".equals(keyword) || "sigma".equals(keyword)) {
                        String[] parts = data.split("\\s+");
                        alphabet = new char[parts.length];
                        for (int i = 0; i < parts.length; i++) {
                            alphabet[i] = parts[i].charAt(0);
                        }
                    }
                }
            }
            if (regex == null) {
                System.err.println(regexPath + " is missing 'pattern:' line");
                return;
            }

            if (alphabet == null) {
                alphabet = extractAlphabetFromRegex(regex);
            }
            if (alphabet.length == 0) {
                System.err.println("Warning: extracted alphabet is empty. Check the regex or provide an alphabet if needed.");
            }

            re = new RegularExpression(regex, alphabet);

        } catch (IOException e) {
            System.err.println(regexPath + " read unsuccessful");
            e.printStackTrace();
            return;
        } catch (Exception e) {
            System.err.println("Failed to construct RegularExpression from regex. Regex: " + regex);
            e.printStackTrace();
            return;
        }

        int correct = 0, wrong = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(casesPath))) {
            String ln;
            int lineNo = 0;
            while ((ln = br.readLine()) != null) {
                lineNo++;
                if (ln.trim().isEmpty()) continue;

                String[] parts = ln.split(",", 2);
                if (parts.length != 2) {
                    System.err.println("Malformed line " + lineNo + " (expected 'string,label'): " + ln);
                    continue;
                }

                String testString = parts[0];
                String labelStr = parts[1].trim();
                int label;
                try {
                    label = Integer.parseInt(labelStr);
                } catch (NumberFormatException nfe) {
                    System.err.println("Invalid label at line " + lineNo + ": " + labelStr);
                    continue;
                }

                boolean expectedMatch = (label == 1);
                boolean actualMatch = re.match(testString);

                if (actualMatch == expectedMatch) correct++;
                else wrong++;
            }
        } catch (IOException e) {
            System.err.println("Path: " + casesPath);
            System.err.println("Cases read unsuccessful");
            e.printStackTrace();
            return;
        }

        System.out.println("Correct: " + correct);
        System.out.println("Wrong: " + wrong);
        if (correct + wrong == 0) {
            System.out.println("Accuracy: N/A (no valid cases found)");
        } else {
            System.out.println("Accuracy: " + (double) correct / (correct + wrong));
        }
    }

    /**
     * Simple alphabet extraction:
     * - Collects distinct characters that are letters or digits (in order of appearance).
     * - Skips the character 'u' (treated as union operator here) and common meta-characters.
     *
     * If your actual alphabet uses other printable symbols, adjust this method accordingly.
     */
    private static char[] extractAlphabetFromRegex(String regex) {
        Set<Character> set = new LinkedHashSet<>();
        for (int i = 0; i < regex.length(); ++i) {
            char c = regex.charAt(i);

            if (Character.isWhitespace(c)) continue;
            if (c == '(' || c == ')' || c == '*' || c == '+' || c == '?' || c == '|' || c == '.' ) continue;
            if (c == 'u') continue;

            // treat letters/digits as alphabet symbols, except u
            if (Character.isLetterOrDigit(c))
                set.add(c);
        }

        char[] alphabet = new char[set.size()];
        int idx = 0;
        for (Character ch : set) alphabet[idx++] = ch;
        return alphabet;
    }

}
