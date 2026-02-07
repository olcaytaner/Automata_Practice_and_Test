# Turing Machine Module

## 1. What is a Turing Machine?

A Turing Machine (TM) is a theoretical model of computation that manipulates symbols on an infinitely long strip of tape. The machine consists of:

-   A **tape**, divided into cells, each holding a single symbol.
-   A **head** that can read the symbol from the current cell, write a new symbol to it, and move one cell to the left or right.
-   A **state register** that stores the machine's current state.
-   A **transition function** (a set of rules) that tells the machine what to do based on its current state and the symbol it just read. A rule typically says: "In state X, if you read symbol Y, then write symbol Z, move in direction D, and change to state W."

A TM computes by starting in an initial state with an input string on the tape. It then follows the transition rules, step by step, until it reaches a special halting state.

---

**Formal Definition**

A Turing Machine is formally defined as a 7-tuple `(Q, Σ, Γ, δ, q₀, q_accept, q_reject)` where:

-   `Q` is a finite set of states.
-   `Σ` is the input alphabet, which is a finite set of symbols.
-   `Γ` is the tape alphabet, a finite set of symbols where `Σ ⊆ Γ`.
-   `δ: Q × Γ → Q × Γ × {L, R}` is the transition function.
-   `q₀ ∈ Q` is the start state.
-   `q_accept ∈ Q` is the accept state.
-   `q_reject ∈ Q` is the reject state, where `q_accept ≠ q_reject`.

---

## 2. How This TM Accepts and Rejects Input

This module implements a deterministic, single-tape Turing Machine. When you provide an input string, the machine begins execution and will run until it halts. Halting occurs in one of two ways:

### Acceptance
An input string is **accepted** if the machine, during its execution, enters the designated **accept state** (e.g., `q_accept`). Once the accept state is entered, the computation stops immediately, and the input is considered part of the language the TM recognizes.

### Rejection
An input string is **rejected** if either of these conditions is met:

1.  The machine enters the designated **reject state** (e.g., `q_reject`).
2.  The machine is in a configuration (a combination of a state and a read symbol) for which **no transition rule is defined**. This implementation treats such cases as an implicit transition to the reject state, causing the machine to halt and reject the input.

**Note:** It is also possible to define a Turing Machine that never halts on certain inputs (i.e., it enters an infinite loop). This implementation does not have a mechanism to detect or stop such loops.

## 3. TM Definition File Format

To use the module, you must define a TM in a text file with specific sections. Each section starts with a keyword followed by a colon (e.g., `states:`).

### Required Sections
-   `states`: A space-separated list of all state names (e.g., `q0 q1 q_accept`).
-   `input`: Space-separated symbols for the input.
-   `tape`: Space-separated symbols allowed on the tape (must include input alphabet symbols).
-   `start`: The name of the start state.
-   `accept`: The name of the accept state.
-   `reject`: The name of the reject state.
-   `transitions`: The header for the transition rules.

### Transitions
Each transition rule must be on a new line in the format:
`<current_state>, <read_symbol> -> <next_state>, <write_symbol>, <direction>`
(Example: `q0, 0 -> q1, X, R`)

### Example `.tm` File
This TM accepts strings with an even number of zeros.
```
states: q0 q1 q_accept q_reject
input: 0 1
tape: 0 1 _
start: q0
accept: q_accept
reject: q_reject

transitions:
q0, 0 -> q1, 0, R
q0, 1 -> q0, 1, R
q0, _ -> q_accept, _, R
q1, 0 -> q0, 0, R
q1, 1 -> q1, 1, R
q1, _ -> q_reject, _, R
```

## 4. Core Java Components

-   **`TM.java`**: The main class representing the Turing Machine.
-   **`TMFileValidator.java`**: Validates the syntax of a `.tm` file.
-   **`TMParser.java`**: Parses a valid `.tm` file into a `TM` object.
-   **`Tape.java`**: Simulates the TM's tape.
-   **`Transition.java`**: Represents a single transition rule.
