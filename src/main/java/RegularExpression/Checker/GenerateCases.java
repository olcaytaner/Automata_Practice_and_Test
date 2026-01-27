package RegularExpression.Checker;

import RegularExpression.RegularExpression;


public class GenerateCases {
    public static void main(String[] args) {
        String regex = "10(1u0)*0";
        RegularExpression re = new RegularExpression(regex, new char[]{'0', '1'});
        re.generateCorrectCasesExhaustive(5, "src/main/java/RegularExpression/Checker/cases_match.txt", false);
        re.generateWrongCasesExhaustive(5, "src/main/java/RegularExpression/Checker/cases_not_match.txt", true);
    }
}
