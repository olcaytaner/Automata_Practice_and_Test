package PushDownAutomaton;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import common.ParseResult;
import common.TestCase;
import common.TestFileParser;

public class PDABench {
    private static final double THRESHOLD_SECONDS = 1.0;
    private static final int    MAX_REPEATS       = 1 << 14; // safety

    // {name, pdaPath, testPath}
    private static final String[][] CASES = new String[][]{
            {"palindrome",
                    "src/test/manual-testing/examples/pda/palindrome.pda",
                    "src/test/manual-testing/examples/pda/palindrome.test"},
    };

    public static void main(String[] args) throws Exception {
        System.out.println("scenario   tests  repeats    total(s)   avg(ms)");
        System.out.println("-----------------------------------------------");
        for (String[] sc : CASES) runScenario(sc[0], sc[1], sc[2]);
    }

    private static void runScenario(String name, String pdaPath, String testPath) throws Exception {
        String pdaText = new String(Files.readAllBytes(Paths.get(pdaPath)), StandardCharsets.UTF_8);
        PDA pda = new PDA();
        ParseResult pr = pda.parse(pdaText);
        if (!pr.isSuccess()) throw new IllegalStateException("PDA parse failed: " + name);

        List<TestCase> tests = TestFileParser.parseTestFile(testPath).getTestCases();
        if (tests.isEmpty()) throw new IllegalStateException("Empty test file: " + testPath);

        // warm-up
        for (int i = 0; i < Math.min(50, tests.size()); i++) pda.execute(tests.get(i).getInput());

        int repeats = 1;
        while (true) {
            long t0 = System.nanoTime();
            int total = tests.size() * repeats;

            for (int r = 0; r < repeats; r++) {
                for (TestCase tc : tests) {
                    pda.execute(tc.getInput());
                }
            }
            long t1 = System.nanoTime();

            double totalSec = (t1 - t0) / 1e9;
            double avgMs = (totalSec * 1000.0) / total;

            System.out.printf("%-10s %5d %8d %10.3f %8.3f%n",
                    name, tests.size(), repeats, totalSec, avgMs);

            if (totalSec > THRESHOLD_SECONDS || repeats >= MAX_REPEATS) break;
            repeats <<= 1;
        }
    }
}
