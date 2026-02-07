# Deterministic Finite Automaton (DFA) Module

## 1. What is a DFA?

A Deterministic Finite Automaton (DFA) is a finite state machine that accepts or rejects strings of symbols. For each state and input symbol, there is exactly one transition to the next state. DFAs recognize **regular languages** (e.g., strings with an even number of 'a's, strings ending with '01').

**Formal Definition**: A DFA can be given as a 5-tuple `(Q, Σ, δ, q₀, F)` where:
- `Q` : finite set of states
- `Σ` : input alphabet (finite set of symbols)
- `δ` : transition function `Q × Σ → Q`
- `q₀ ∈ Q` : start state
- `F ⊆ Q` : set of accepting (final) states

---

## 2. How This DFA Accepts and Rejects Input

- **Acceptance**: An input is **accepted** if after consuming the entire input, the automaton is in a state that is a member of `F` (final states).
- **Rejection**: The input is rejected if the automaton is not in a final state after consuming the entire input or if there is no valid transition for the current state and input symbol.

---

## 3. DFA Definition File Format (`.dfa`)

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

**Example**
```text
states: q0 q1 q2
alphabet: a b
start: q0
accept: q2

transitions:
q0, a -> q1
q0, b -> q0
q1, a -> q1
q1, b -> q2
q2, a -> q1
q2, b -> q0
```

## 4. Components
- **`DFA.java`** – Main DFA class that handles parsing, execution, and visualization. The execution processes input strings and determines acceptance based on the final state.

- **`DFATransition.java`** – Represents a transition in the DFA with `fromState`, `inputSymbol`, and `toState`.

## 5. Visualization
- The DFA can be visualized using `Automaton.toGraphviz(...)` which generates a DOT representation of the automaton. The visualization clearly shows states, transitions, and marks the start and final states appropriately.
