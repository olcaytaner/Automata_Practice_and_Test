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
                        String regex = br.readLine();
                        System.out.println(regex);
                        String[] alphabet_ = br.readLine().split(" ");

                        char[] alphabet = new char[alphabet_.length];
                        for (int i = 0; i < alphabet_.length; ++i)
                                alphabet[i] = alphabet_[i].charAt(0);

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
