package cli;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import ContextFreeGrammar.CFG;
import DeterministicFiniteAutomaton.DFA;
import NondeterministicFiniteAutomaton.NFA;
import PushDownAutomaton.PDA;
import RegularExpression.RegularExpression;
import TuringMachine.TM;
import common.Automaton;
import common.ParseResult;
import common.ValidationMessage;

public class SimpleValidator {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java cli.SimpleValidator <file>");
            System.exit(2);
        }

        File file = new File(args[0]);
        if (!file.exists() || !file.isFile()) {
            System.err.println("File not found: " + file.getAbsolutePath());
            System.exit(2);
        }

        String name = file.getName().toLowerCase();
        int dot = name.lastIndexOf('.');
        if (dot < 0) {
            System.err.println("File has no extension: " + file.getName());
            System.exit(2);
        }
        String ext = name.substring(dot);

        Automaton automaton;
        switch (ext) {
            case ".dfa": automaton = new DFA(); break;
            case ".nfa": automaton = new NFA(); break;
            case ".pda": automaton = new PDA(); break;
            case ".tm":  automaton = new TM(); break;
            case ".cfg": automaton = new CFG(); break;
            case ".rex": automaton = new RegularExpression(); break;
            default:
                System.err.println("Unsupported extension: " + ext);
                System.exit(2);
                return;
        }

        String content;
        try {
            content = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.err.println("Read error: " + e.getMessage());
            System.exit(2);
            return;
        }

        ParseResult result = automaton.parse(content);

        if (result.isSuccess()) {
            System.out.println("OK");
            System.exit(0);
        }

        for (ValidationMessage msg : result.getValidationMessages()) {
            if (msg.getType() == ValidationMessage.ValidationMessageType.ERROR) {
                System.err.println(msg.toString());
            }
        }
        System.exit(1);
    }
}
