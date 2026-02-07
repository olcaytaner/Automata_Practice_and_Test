# Nondeterministic Finite Automaton (NFA) Module

## 1. What is a NFA?

A Nondeterministic Finite Automaton (NFA) is a finite state machine that, unlike a DFA, can have multiple possible transitions for a given state and input symbol.
NFAs can also include ε-transitions (epsilon transitions), which allow the automaton to move between states without consuming an input symbol.
NFAs also recognize regular languages, just like DFAs. Every NFA has an equivalent DFA (though the DFA may have exponentially more states).

**Formal Definition**: A NFA can be given as a 5-tuple `(Q, Σ, δ, q₀, F)` where:
- `Q` : finite set of states
- `q₀ ∈ Q` : start state
- `F ⊆ Q` : set of accepting (final) states
- `Σ` : input alphabet (finite set of symbols)
- `δ` : transition function `Q × Σ → Q`

---

## 2. How This NFA Accepts and Rejects Input

- **Acceptance**: An input is **accepted** if there exists at least one computation path such that, after consuming the input (and possible ε-moves), the automaton ends in a state that belongs to F.
####
- **Rejection**: The input is rejected if all possible computation paths end in non-final states or get stuck with no valid transitions.

---

## 3. NFA Definition File Format (`.nfa`)

Each section begins with a keyword and a colon. Lines may contain comments (`# ...`) and blank lines.

**Required sections**
- `states:` space-separated state names
- `alphabet:` space-separated input symbols (single chars)
- `start:` one state name
- `accept:` space-separated accepting states
- `transitions:` one transition per line (see below)

**Transitions**
```
<fromState>, <inputSymbol> -> <toState>
```
- \<inputSymbol> can be ***eps*** to indicate an epsilon transition.
- Each transition is on its own line with exactly one symbol.

**Example**
```text
states: q0 q1 q2
alphabet: a b
start: q0
accept: q2

transitions:
q0, a -> q1
q0, eps -> q1
q0, b -> q0
q1, eps -> q1
q1, b -> q2
q2, a -> q1
q2, b -> q0
q2, eps -> q0
```

## 4. Components
- **`NFA.java`** – Main NFA class that handles parsing, execution, and visualization. The execution processes input strings and determines acceptance based on the final state.

- **`Transition.java`** – Represents a transition in the NFA with `fromState`, `inputSymbol`, and `toState`.

## 5. Visualization
- The NFA can be visualized using `Automaton.toGraphviz(...)` which generates a DOT representation of the automaton. The visualization clearly shows states, transitions, and marks the start and final states appropriately.
