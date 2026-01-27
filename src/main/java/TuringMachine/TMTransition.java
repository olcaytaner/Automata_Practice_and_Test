package TuringMachine;

import java.util.Objects;

import common.BaseTransition;
import common.State;
import common.Symbol;

/**
 * Transition class for Turing Machines.
 *
 * <p>Extends the base transition with tape operations:</p>
 * <ul>
 *   <li><strong>Read Symbol:</strong> Symbol that must be under the tape head</li>
 *   <li><strong>Write Symbol:</strong> Symbol to write to the current tape cell</li>
 *   <li><strong>Move Direction:</strong> Direction to move the tape head</li>
 * </ul>
 *
 * <h3>Transition Notation:</h3>
 * <pre>
 * δ(q, a) = (p, b, D)
 * where:
 *   q = source state
 *   a = symbol read from tape
 *   p = target state
 *   b = symbol to write to tape
 *   D = direction (L or R)
 * </pre>
 *
 * <h3>Usage Example:</h3>
 * <pre>{@code
 * TMTransition t = new TMTransition(
 *     q0,                         // from state
 *     new Symbol('0'),            // read symbol
 *     q1,                         // to state
 *     new Symbol('1'),            // write symbol
 *     TMTransition.Direction.RIGHT // move direction
 * );
 * }</pre>
 */
public class TMTransition extends BaseTransition {

    /**
     * Direction for tape head movement.
     */
    public enum Direction {
        LEFT("L"),
        RIGHT("R");

        private final String symbol;

        Direction(String symbol) {
            this.symbol = symbol;
        }

        @Override
        public String toString() {
            return symbol;
        }

        /**
         * Parse direction from character.
         * @param c 'L' or 'R' (case insensitive)
         * @return Corresponding Direction
         * @throws IllegalArgumentException if character is not L or R
         */
        public static Direction fromChar(char c) {
            switch (Character.toUpperCase(c)) {
                case 'L': return LEFT;
                case 'R': return RIGHT;
                default:
                    throw new IllegalArgumentException(
                        "Invalid direction: " + c + ". Must be 'L' or 'R'");
            }
        }
    }

    private final Symbol readSymbol;
    private final Symbol writeSymbol;
    private final Direction moveDirection;

    /**
     * Constructs a TM transition with full tape operations.
     *
     * @param fromState The source state
     * @param readSymbol The symbol that must be read (under tape head)
     * @param toState The target state
     * @param writeSymbol The symbol to write to the tape
     * @param moveDirection The direction to move the tape head
     * @throws IllegalArgumentException if any parameter is null
     */
    public TMTransition(State fromState, Symbol readSymbol, State toState,
                        Symbol writeSymbol, Direction moveDirection) {
        super(fromState, toState);
        if (readSymbol == null) {
            throw new IllegalArgumentException("Read symbol cannot be null");
        }
        if (writeSymbol == null) {
            throw new IllegalArgumentException("Write symbol cannot be null");
        }
        if (moveDirection == null) {
            throw new IllegalArgumentException("Move direction cannot be null");
        }
        this.readSymbol = readSymbol;
        this.writeSymbol = writeSymbol;
        this.moveDirection = moveDirection;
    }

    /**
     * Convenience constructor using char values (backward compatible).
     *
     * @param fromState The source state
     * @param readSymbol The character to read from tape
     * @param toState The target state
     * @param writeSymbol The character to write to tape
     * @param moveDirection The direction to move
     */
    public TMTransition(State fromState, char readSymbol, State toState,
                        char writeSymbol, Direction moveDirection) {
        this(fromState, new Symbol(readSymbol), toState,
             new Symbol(writeSymbol), moveDirection);
    }

    /**
     * Gets the symbol that must be read for this transition to apply.
     * @return The read symbol
     */
    public Symbol getReadSymbol() {
        return readSymbol;
    }

    /**
     * Gets the symbol to write to the tape.
     * @return The write symbol
     */
    public Symbol getWriteSymbol() {
        return writeSymbol;
    }

    /**
     * Gets the direction to move the tape head.
     * @return The move direction
     */
    public Direction getMoveDirection() {
        return moveDirection;
    }

    /**
     * Gets the target state of this transition.
     * Alias for {@link #getToState()} for backward compatibility.
     * @return The next state
     */
    public State getNextState() {
        return getToState();
    }

    /**
     * Gets the character value of the symbol to write.
     * Alias for backward compatibility.
     * @return The character to write
     */
    public char getSymbolToWrite() {
        return writeSymbol.getValue();
    }

    /**
     * Checks if this transition modifies the tape.
     * @return true if write symbol differs from read symbol
     */
    public boolean modifiesTape() {
        return !readSymbol.equals(writeSymbol);
    }

    @Override
    protected String getLabel() {
        return String.format("%s/%s,%s",
            readSymbol, writeSymbol, moveDirection);
    }

    @Override
    public String prettyPrint() {
        return String.format("%s -> %s [read: %s, write: %s, move: %s]\n",
            fromState.getName(),
            toState.getName(),
            readSymbol,
            writeSymbol,
            moveDirection);
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        TMTransition that = (TMTransition) o;
        return Objects.equals(readSymbol, that.readSymbol)
            && Objects.equals(writeSymbol, that.writeSymbol)
            && moveDirection == that.moveDirection;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), readSymbol, writeSymbol, moveDirection);
    }
}
