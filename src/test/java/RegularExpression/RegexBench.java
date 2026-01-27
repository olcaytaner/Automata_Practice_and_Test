package RegularExpression;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

import RegularExpression.Checker.Check;

public class RegexBench {
    public static void main(String[] args) throws FileNotFoundException {

        String check_regex_loc = "src/test/manual-testing/examples/rex/regex-correct.rex";
        String cases_loc = "src/test/manual-testing/examples/rex/rex.test";

        new PrintWriter(cases_loc).close(); // clear left over cases from rex.test

        String generator_regex = "a*b*c*"; // to illustrate how high a score can a wrong regex give
        RegularExpression re = new RegularExpression(generator_regex, new char[]{'a', 'b', 'c'});

        int max_len_c = 15, max_len_w = 6;

        long start = System.nanoTime();

        int cnt_c = re.generateCorrectCasesExhaustive(max_len_c, cases_loc, false);
        long correct_case_gen_finish = System.nanoTime();

        int cnt_w = re.generateWrongCasesExhaustive(max_len_w, cases_loc, true);
        long wrong_case_gen_finish = System.nanoTime();

        Check.check(check_regex_loc, cases_loc);
        long check_finish = System.nanoTime();

        float div = 1_000_000_000F; // convert from ns to s

        System.out.printf("Time it took to generate %d correct cases with max length %d: %.2f seconds\n", cnt_c, max_len_c,
                (correct_case_gen_finish - start) / div);

        System.out.printf("Time it took to generate %d wrong cases with max length %d: %.2f seconds\n", cnt_w, max_len_w,
                (wrong_case_gen_finish - correct_case_gen_finish) / div);

        System.out.printf("Time it took to check %d cases: %.2f seconds\n", cnt_c + cnt_w,
                (check_finish - wrong_case_gen_finish) / div);

        System.out.printf("Total: %.2f seconds\n", (check_finish - start) / div);
    }
}
