package TuringMachine;


import java.util.ArrayList;
import java.util.List;

/**
 * Represents the tape of a Turing Machine.
 */
public class Tape {
    private final List<Character> tape;
    private int headPosition;
    private static final char BLANK = '_';

    /**
     * Constructs a new empty tape.
     */
    public Tape() {
        tape = new ArrayList<>();
        headPosition = 0;
    }

    /**
     * Initializes the tape with a given input string.
     * @param input The input string to write to the tape.
     */
    public void initialize(String input) {
        tape.clear();
        for (char ch : input.toCharArray()) {
            tape.add(ch);
        }
        headPosition = 0;
    }

    /**
     * Reads the symbol at the current head position.
     * @return The symbol at the current head position.
     */
    public char read() {
        ensureWithinBounds();
        return tape.get(headPosition);
    }

    /**
     * Writes a symbol to the tape at the current head position.
     * @param symbol The symbol to write.
     */
    public void write(char symbol) {
        ensureWithinBounds();
        tape.set(headPosition, symbol);
    }

    /**
     * Moves the tape head in the specified direction.
     * @param direction The direction to move the head.
     */
    public void move(TMTransition.Direction direction) {
        switch (direction) {
            case LEFT:
                if (headPosition > 0) {
                    headPosition--;
                }
                break;
            case RIGHT:
                headPosition++;
                break;
            default:
                throw new IllegalArgumentException("Invalid direction: " + direction);
        }
    }

    private void ensureWithinBounds() {
        if (headPosition >= tape.size()) {
            tape.add(BLANK);
        }
    }

    /**
     * Clears the tape and resets the head position.
     */
    public void clear() {
        tape.clear();
        headPosition = 0;
    }

    /**
     * Prints the contents of the tape to the console.
     */
    public void printTape() {
        for (int i = 0; i < tape.size(); i++) {
            if (i == headPosition) System.out.print("[");
            System.out.print(tape.get(i));
            if (i == headPosition) System.out.print("]");
        }
        System.out.println();
    }

    /**
     * Returns the contents of the tape as a string.
     * @return The contents of the tape.
     */
    public String getTapeContents() {
        StringBuilder sb = new StringBuilder();
        for (char ch : tape) {
            sb.append(ch);
        }
        return sb.toString();
    }

    /**
     * Appends the tape contents to a StringBuilder, with the head position indicated by brackets.
     * @param sb The StringBuilder to append to.
     */
    public void appendTapeTo(StringBuilder sb) {
        for (int i = 0; i < tape.size(); i++) {
            if (i == headPosition) sb.append("[");
            sb.append(tape.get(i));
            if (i == headPosition) sb.append("]");
        }
    }
}