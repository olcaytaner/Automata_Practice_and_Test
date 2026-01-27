package RegularExpression;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import RegularExpression.Checker.Check;

/**
 * This is how you generate correct and incorrect cases to check in RegularExpression, in large sizes.
 */
public class GeneratorAndChecker {
    public static void main(String[] args) throws FileNotFoundException {

        String correct_reg = "(aa*cc* u bb*cc*)(aa*cc* u bb*cc*)*";
        RegularExpression re = new RegularExpression(correct_reg, new char[]{'a', 'b', 'c'});

        new PrintWriter("src/test/manual-testing/examples/rex/rex.test").close(); // clear left over cases from rex.test

        re.generateCorrectCasesExhaustive(6, "src/test/manual-testing/examples/rex/rex.test", false);
        re.generateWrongCasesExhaustive(6, "src/test/manual-testing/examples/rex/rex.test", true);


        Check.check("src/test/manual-testing/examples/rex/regex-correct.rex",
                "src/test/manual-testing/examples/rex/rex.test");
    }
}
