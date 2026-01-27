package NondeterministicFiniteAutomaton;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import common.ExecutionResult;
import common.ParseResult;
import common.Symbol;
import common.ValidationMessage;
import common.ValidationMessage.ValidationMessageType;

public class ManualTest {

    public static void main(String[] args) throws IOException {
        //2 symbol - 16
        //3 symbol - 10

        String week = "week3-2";

        NFA nfa = new NFA();
        ParseResult parseResult = nfa.parse(readFile(getExercisePath(week)));

        if (parseResult.isSuccess()){
            nfa = (NFA) parseResult.getAutomaton();
        }else {
            System.out.println(parseResult.getValidationMessages());
            return;
        }

        //set test file
        String testFile = getTestPath(week);

        double generateTotalMs = 0;
        double testTotalMs = 0;
        double testCount = 10;

        double maxTestTime = Integer.MIN_VALUE;
        double minTestTime = Integer.MAX_VALUE;

        generateTotalMs += generateTests(nfa, 16, testFile, true);

        for (int i = 0; i < testCount; i++) {
            System.out.println("Test: " + i);

            double current = testFromFile(nfa, testFile);
            testTotalMs += current;

            if (current > maxTestTime) {
                maxTestTime = current;
            }else if (current < minTestTime) {
                minTestTime = current;
            }

        }

        System.out.println("\nTook: " + generateTotalMs + "ms on average to generate");
        System.out.println("Took: " + testTotalMs/testCount + "ms on average to execute");
        System.out.println("Took: " + maxTestTime/1_000.0 + "s at maximum to execute");
        System.out.println("Took: " + minTestTime/1_000.0 + "s at minimum to execute");
    }

    public static String parseTest(String input, boolean verbose) {

        NFA nfa = new NFA();
        ParseResult parseResult = nfa.parse(input);

        int errorCount = 0;
        int warningCount = 0;
        int infoCount = 0;

        for (ValidationMessage warning : parseResult.getValidationMessages()){
            if (verbose){
                System.out.println(warning);
            }
            if (warning.getType() == ValidationMessageType.ERROR){
                errorCount++;
            }else if (warning.getType() == ValidationMessageType.WARNING){
                warningCount++;
            }else if (warning.getType() == ValidationMessageType.INFO){
                infoCount++;
            }
        }

        if (errorCount == 0){
            nfa = (NFA) parseResult.getAutomaton();
            //System.out.println(nfa.prettyPrint());
        }

        if (verbose){
            System.out.println("\n Syntax: " + errorCount + " error(s), " + warningCount + " warning(s) and " + infoCount + " info(s). ");
        }

        int validationErrors = 0;
        int validationWarnings = 0;
        int validationInfos = 0;

        if (parseResult.isSuccess()) {
            if (verbose) {
                System.out.println(nfa.toDotCode(null));
            }

            List<ValidationMessage> warnings = nfa.validate();
            for (ValidationMessage warning : warnings){
                if (verbose){
                    System.out.println(warning);
                }
                if (warning.getType() == ValidationMessageType.ERROR){
                    validationErrors++;
                }else if (warning.getType() == ValidationMessageType.WARNING){
                    validationWarnings++;
                }else if (warning.getType() == ValidationMessageType.INFO){
                    validationInfos++;
                }
            }

            if (verbose) {
                System.out.println("\n Validation: " + validationErrors + " error(s), " + validationWarnings + " warning(s) and " + validationInfos + " info(s). ");
            }
            if (validationErrors == 0){
                return " Passed";
            }
        }
        return " Failed";
    }

    private static String readFile(String file) throws IOException {

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

            String line;
            StringBuilder stringBuilder = new StringBuilder();
            String ls = System.lineSeparator();
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }

            return stringBuilder.toString();
        }

    }

    private static String getExercisePath(String week){
        String s = "src/test/manual-testing/exercises/%s/nfa/%s.nfa";
        return String.format(s, week.substring(0, 5), week);
    }

    private static String getTestPath(String week){
        String s = "src/test/manual-testing/exercises/%s/nfa/%s.test";
        return String.format(s, week.substring(0, 5), week);
    }

    private static void generateTests(NFA nfa, int n, String outputPath){
        generateTests(nfa, n, outputPath, false);
    }

    private static double generateTests(NFA nfa, int n, String outputPath, boolean random) {
        long t = System.nanoTime();
        Random rand = new Random();
        StringBuilder sb = new StringBuilder();

        for (Symbol symbol : nfa.getAlphabet()) {
            sb.append(symbol.getValue());
        }

        List<String> result = new ArrayList<>();

        for (int i = 0; i <= n; i++) {

            result.addAll(recursivePerm(sb.toString(), "", i));

        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            for (String s : result) {

                boolean b = rand.nextBoolean();
                if (!random){
                    b = nfa.execute(s).isAccepted();
                }

                writer.write(s+"," + (b ? 1 : 0));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Generated file: " + outputPath + " with " + result.size() + " inputs " + (random?"by random ":"by executing ") + "in " + (System.nanoTime() - t) / 1_000_000.0 + " ms");
        return (System.nanoTime() - t)/1_000_000.0;
    }

    private static List<String> recursivePerm(String s, String curr, int n){
        List<String> perms = new ArrayList<>();
        if (curr.length() == n){
            perms.add(curr);
            return perms;
        }

        for (int i = 0; i < s.length(); i++){
            perms.addAll(recursivePerm(s, curr + s.charAt(i), n));
        }

        return perms;
    }

    private static double testFromFile(NFA nfa, String inputPath){

        int accepted = 0;
        int falsePos = 0;
        int rejected = 0;
        int falseNeg = 0;
        int lineCount = 0;

        long t = System.nanoTime();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(inputPath));
            String line;
            while ((line = reader.readLine()) != null){
                lineCount++;
                String acc = line.substring(line.indexOf(",")+1);
                line = line.substring(0, line.indexOf(","));
                ExecutionResult result = nfa.execute(line);
                if (result.isAccepted()){
                    if (acc.equals("1")){
                        accepted++;
                    }else {
                        falsePos++;
                    }
                }else  {
                    if (acc.equals("0")){
                        rejected++;
                    }else {
                        falseNeg++;
                    }
                }

            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        long elapsed = System.nanoTime() - t;
        System.out.println("Line Count: " + lineCount);
        System.out.println("True Accepted: " + accepted);
        System.out.println("False Accepted: " + falsePos);
        System.out.println("True Rejected: " + rejected);
        System.out.println("False Rejected: " + falseNeg);
        System.out.println("Success Rate: %" + (double)(accepted + rejected)*100 / lineCount);
        System.out.println("Took " + elapsed/1_000_000_000.0 + " second to execute file");

        return elapsed/1_000_000.0;
    }

}
