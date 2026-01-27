package ContextFreeGrammar;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import common.ExecutionResult;
import common.ParseResult;
import common.ValidationMessage;

/**
 * Main class for the Context-Free Grammar parser application.
 * This class demonstrates the usage of the CFG parser by reading a grammar file,
 * validating it, and displaying the results.
 *
 * @author yenennn
 * @version 1.0
 */
public class CFGTest {

    /**
     * Main method that runs the CFG parser demonstration.
     * Reads a grammar file, parses it, validates the grammar, and displays
     * the results in a readable format.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        String filePath = "src/test/java/ContextFreeGrammar/test.txt";

        try {
            // Read the grammar file
            System.out.println("Reading grammar from file: " + filePath);
            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            
            // Create CFG and parse using integrated method
            CFG grammar = new CFG();
            ParseResult parseResult = grammar.parse(content);
            
            if (parseResult.isSuccess()) {
                System.out.println("Parsing successful!");
                
                // Validate the grammar
                System.out.println("\nValidating grammar...");
                boolean isValid = grammar.validateGrammar();
                System.out.println("Grammar is " + (isValid ? "valid" : "invalid"));

                // Pretty print the grammar
                System.out.println("\nGrammar in readable format:");
                grammar.prettyPrint();

                System.out.println("\nGrammar toString:");
                System.out.println(grammar);
                
                // Test execution with a simple string
                System.out.println("\nTesting execution with '01':");
                ExecutionResult execResult = grammar.execute("01");
                System.out.println("Result: " + (execResult.isAccepted() ? "ACCEPTED" : "REJECTED"));
                System.out.println("Trace:\n" + execResult.getTrace());
            } else {
                System.err.println("Parsing failed:");
                for (ValidationMessage msg : parseResult.getValidationMessages()) {
                    System.err.println(msg);
                }
            }

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        } catch (GrammarParseException e) {
            System.err.println("Error parsing grammar: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
