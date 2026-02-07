package RegularExpression;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import RegularExpression.Checker.Check;

public class TestCaseGen {
    public static void main(String[] args) throws FileNotFoundException {
        File[] dirs = {
                new File("src/test/manual-testing/exercises/week4/rex"),
                new File("src/test/manual-testing/exercises/week5/rex")
        };

        for (File dir : dirs) {
            File[] files = dir.listFiles();
            assert files != null;
            for (File f : files) {
                if (f.getName().endsWith(".rex")) {

                    try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                        String regex = null;
                        char[] alphabet = null;
                        String ln;
                        while ((ln = br.readLine()) != null) {
                            String trimmed = ln.trim();
                            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue;
                            int colonIdx = trimmed.indexOf(":");
                            if (colonIdx != -1) {
                                String keyword = trimmed.substring(0, colonIdx).trim().toLowerCase();
                                String data = trimmed.substring(colonIdx + 1).trim();
                                if ("pattern".equals(keyword) || "regex".equals(keyword)) {
                                    regex = data;
                                } else if ("alphabet".equals(keyword) || "sigma".equals(keyword)) {
                                    String[] parts = data.split("\\s+");
                                    alphabet = new char[parts.length];
                                    for (int i = 0; i < parts.length; i++)
                                        alphabet[i] = parts[i].charAt(0);
                                }
                            }
                        }
                        System.out.println(regex);

                        RegularExpression re = new RegularExpression(regex, alphabet);

                        String test_path = f.toString().replaceFirst("\\.rex$", ".test");

                        re.generateCorrectCasesExhaustive(12, test_path, false);
                        re.generateWrongCasesExhaustive(6, test_path, true);

                        Check.check(f.toString(), test_path);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
