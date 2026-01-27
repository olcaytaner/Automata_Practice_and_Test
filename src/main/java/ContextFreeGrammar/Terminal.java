package ContextFreeGrammar;

import common.Symbol;

import static common.SymbolConstants.*;

/**
 * Represents a terminal symbol in a context-free grammar.
 * Terminal symbols are the basic symbols from which strings are formed.
 */
public class Terminal extends Symbol {
    private final String name;

    public Terminal(String name) {
        super(name.isEmpty() ? 'e' : name.charAt(0));
        this.name = name;
    }

    public Terminal(char value) {
        super(value);
        this.name = Character.toString(value);
    }

    @Override
    public boolean isEpsilon() {
        return isEpsilonInput(name);
    }

    @Override
    public String prettyPrint() {
        return "Terminal: " + (isEpsilon() ? EPSILON_INPUT : name);
    }

    @Override
    public String getName() {
        return isEpsilon() ? "e" : name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Terminal)) return false;
        Terminal terminal = (Terminal) o;
        return name.equals(terminal.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return isEpsilon() ? EPSILON_INPUT : name;
    }
}