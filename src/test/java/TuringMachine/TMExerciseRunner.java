package TuringMachine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import common.ParseResult;
import common.TestRunner;

public class TMExerciseRunner {

    public static void main(String[] args) {
        String exercisesTmDir = "src/test/manual-testing/exercises/week10/";
        String exercisesTestDir = "src/test/manual-testing/exercises/week10/"; // Updated path

        List<String> exercises = Arrays.asList(
                "week10_tm1",
                "week10_tm2",
                "week10_tm3",
                "week10_tm4"
        );

        for (String exerciseBaseName : exercises) {
            System.out.println("====================================================");
            System.out.println("Running exercise: " + exerciseBaseName);
            System.out.println("====================================================");

            Path tmPath;
            tmPath = Paths.get(exercisesTmDir, exerciseBaseName + ".tm");
            
            Path testPath = Paths.get(exercisesTestDir, exerciseBaseName + ".test");

            if (!Files.exists(tmPath) || !Files.exists(testPath)) {
                System.out.println("Could not find TM definition file or test file for " + exerciseBaseName);
                System.out.println("TM Path: " + tmPath.toAbsolutePath());
                System.out.println("Test Path: " + testPath.toAbsolutePath());
                System.out.println();
                continue;
            }

            try {
                String tmContent = new String(Files.readAllBytes(tmPath));
                TM tm = new TM(null, null, null, null, null, null, null);
                ParseResult parseResult = tm.parse(tmContent);

                if (!parseResult.isSuccess()) {
                    System.out.println("Failed to parse TM: " + exerciseBaseName);
                    System.out.println("Validation messages: " + parseResult.getValidationMessages());
                    System.out.println();
                    continue;
                }

                TM parsedTM = (TM) parseResult.getAutomaton();

                TestRunner.TestResult testResult = TestRunner.runTests(parsedTM, testPath.toString());
                System.out.println(testResult.getDetailedReport());

            } catch (IOException e) {
                System.err.println("Error running exercise " + exerciseBaseName + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
