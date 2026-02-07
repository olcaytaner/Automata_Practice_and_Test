# Push-down Automaton (PDA) Module

## 1. What is a PDA?

A Push-down Automaton extends finite automata with a **stack**. It can read an input symbol and (optionally) pop from the stack and push a (possibly multi-character) string onto the stack in one transition. PDAs recognize **context-free languages** (e.g., `a^n b^n`, balanced parentheses, many palindromes).

**Formal (high-level)**: A PDA can be given as a 7-tuple `(Q, Σ, Γ, δ, q₀, Z₀, F)` where:
- `Q` : finite set of states
- `Σ` : input alphabet
- `Γ` : stack alphabet
- `δ` : transition relation `Q × (Σ ∪ {ε}) × (Γ ∪ {ε}) → P(Q × Γ* )`
- `q₀ ∈ Q` : start state
- `Z₀ ∈ Γ` : initial stack symbol
- `F ⊆ Q` : set of accepting (final) states

This implementation accepts **by final state**.

---

## 2. How This PDA Accepts and Rejects Input

- **Acceptance**: An input is **accepted** if after consuming the entire input, the automaton reaches a state in `F` (finals).
- **Rejection**: If no accepting configuration is reachable (including when the search aborts due to safety limits), the input is rejected.

---

## 3. PDA Definition File Format (`.pda`)

Each section begins with a keyword and a colon. Lines may contain comments (`# ...`) and blank lines.

**Required sections**
- `states:` space-separated state names
- `input:` space-separated input symbols (single chars)
- `stack:` space-separated stack symbols (single chars)
- `start:` one state name
- `stack_start:` one stack symbol (in `stack`)
- `accept:` space-separated accepting states
- `transitions:` one transition per line (see below)

**Transitions** (comma-separated Smart Text format)
`fromState, inputOrEps, stackPopOrEps -> toState, stackPushOrEps`

- `eps` denotes epsilon (no input consumed / no stack pop)
- `stackPushOrEps` may be a **string** over the stack alphabet (multi-character push), or `eps`.

**Example**
```text
states: q0 q1 q2
input: a b
stack: Z a
start: q0
stack_start: Z
accept: q2
transitions:
q0, a, Z -> q0, aZ
q0, a, a -> q0, aa
q0, b, a -> q1, eps
q1, b, a -> q1, eps
q1, eps, Z -> q2, eps

```

## 4. Components
-   **`PDA.java`**– Main PDA class (parse, execute, DOT). Execution is BFS with visited-set, accepts by final state. Returns rich ExecutionResult (accepted flag, runtime messages, trace).

-   **`PDATransition.java `**– Transition object: (from, inputSymbol, stackPop, to, stackPush). Input/pop are Symbol, push is String.

-   **`Stack.java / Node.java`** – Lightweight linked-list stack used by earlier versions; current execution internally uses immutable String for the stack snapshot but these classes remain for compatibility and parsing helpers.

## 5. Visualization
- `Automaton.toGraphviz(...)` uses Graphviz (Java engine) to render the DOT produced by `PDA.toDotCode(...)`. Self-loops are drawn using distinct node ports for readability.

