package common;

import static common.SymbolConstants.*;

import java.util.Objects;

/**
 * Represents a symbol in formal language theory, such as those used in automata,
 * grammars, and regular expressions. A symbol can be any character, including
 * a special epsilon symbol represented internally by the underscore character '_'.
 * 
 * <p>This class is immutable and provides methods for symbol comparison,
 * epsilon checking, and string representation.</p>
 * 
 */
public class Symbol {
  private final char value;

  /**
   * Constructs a new Symbol with the specified character value.
   * 
   * @param value the character value of this symbol
   */
  public Symbol(char value) {
    this.value = value;
  }

  /**
   * Returns the character value of this symbol.
   * 
   * @return the character value of this symbol
   */
  public char getValue() { return value; }

  /**
   * Returns the name of this symbol.
   *
   * @return the name of this symbol as a string
   */
  public String getName() {
    return Character.toString(value);
  }

  /**
   * Checks if this symbol represents the epsilon (empty string) symbol.
   * Epsilon is represented internally by the underscore character '_'.
   *
   * @return {@code true} if this symbol is epsilon, {@code false} otherwise
   */
  public boolean isEpsilon() { return value == EPSILON_CHAR; }


  /**
   * Returns a human-readable string representation of this symbol.
   * For epsilon symbols, returns "Symbol: eps". For all other symbols,
   * returns "Symbol: " followed by the character value.
   *
   * @return a formatted string representation of this symbol
   */
  public String prettyPrint() {
    return "Symbol: " + (isEpsilon() ? EPSILON_INPUT : value);
  }

  /**
   * Indicates whether some other object is "equal to" this symbol.
   * Two symbols are considered equal if they have the same character value.
   * 
   * @param o the reference object with which to compare
   * @return {@code true} if this object is the same as the o argument;
   *         {@code false} otherwise
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Symbol)) return false;
    Symbol symbol = (Symbol) o;
    return Objects.equals(value, symbol.value);
  }

  /**
   * Returns a hash code value for this symbol.
   * The hash code is computed based on the character value.
   * 
   * @return a hash code value for this object
   */
  @Override
  public int hashCode() { return Objects.hash(value); }

  /**
   * Returns a string representation of this symbol.
   * This returns the character value as a string.
   * 
   * @return the string representation of the character value
   */
  @Override
  public String toString() { return Character.toString(value); }
}
