package common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("Test File Parser")
public class TestFileParserTest {

    @TempDir
    Path tempDir;

    private Map<String, Boolean> parse(String content) throws Exception {
        Path file = tempDir.resolve("cases.test");
        Files.write(file, content.getBytes("UTF-8"));
        List<TestCase> cases = TestFileParser.parseTestFile(file.toString()).getTestCases();
        Map<String, Boolean> byInput = new HashMap<>();
        for (TestCase tc : cases) {
            byInput.put(tc.getInput(), tc.shouldAccept());
        }
        assertEquals(cases.size(), byInput.size(), "fixture inputs are unique");
        return byInput;
    }

    @Test
    @DisplayName("Test strings starting with '#' (empty left side of u#v) are test cases, not comments")
    void hashPrefixedTestStrings() throws Exception {
        Map<String, Boolean> cases = parse(
                "#max_points=10\n" +
                "#0011,1\n" +
                "#,1\n" +
                "# 01,0\n" +
                "01001011#000010010011010010,1\n" +
                "1010010#1000110011001011010,0\n");

        assertEquals(4, cases.size());
        assertTrue(cases.get("#0011"));
        assertTrue(cases.get("#"));
        assertTrue(cases.get("01001011#000010010011010010"));
        assertFalse(cases.get("1010010#1000110011001011010"));
    }

    @Test
    @DisplayName("Ordinary '#' comments and headers are still skipped")
    void commentsAndHeadersSkipped() throws Exception {
        Map<String, Boolean> cases = parse(
                "#week1-1\n" +
                "# Format: input_string,expected_result (1=accept, 0=reject)\n" +
                "#min_points=2\n" +
                "01,1\n");

        assertEquals(1, cases.size());
        assertTrue(cases.get("01"));
    }
}
