# Context-Free Grammar (CFG) Module

## 1. What is a CFG?

A **Context-Free Grammar** (CFG) is a formal system for describing languages where productions replace a single non-terminal with a string of terminals and/or non-terminals. CFGs generate all **context-free languages** (e.g., balanced parentheses, arithmetic expressions, palindromes).

**Formal definition:**  
A CFG is a 4-tuple `(V, Σ, R, S)` where:
- `V` : finite set of variables (non-terminals)
- `Σ` : finite set of terminals (alphabet)
- `R` : finite set of productions (`A → α`, where `A ∈ V`, `α ∈ (V ∪ Σ)*`)
- `S ∈ V` : start symbol

---

## 2. How This CFG Accepts and Rejects Input

- **Acceptance:** An input string is **accepted** if it can be derived from the start symbol using the productions (i.e., `S ⇒* w`).
- **Rejection:** If no derivation exists for the input string, it is rejected.

Parsing is performed using the **CYK algorithm** (on Chomsky Normal Form).

---

## 3. CFG Definition File Format (`.cfg`)

Each section starts with a keyword and `:`. Lines may contain comments (`# ...`) and blank lines.

**Required sections:**
- `vars:` space-separated variable names (uppercase)
- `terminals:` space-separated terminal names (lowercase)
- `start:` one variable name (must be in `vars`)
- `rules:` header followed by production lines

**Productions:**
- Left side: a variable (non-terminal)
- Right side: space-separated sequence of variables and/or terminals, or `eps` for epsilon (empty string)
- Alternatives separated by `|`

**Example**
```text
vars: S A B
terminals: a b
start: S

rules:
S -> A B
A -> a
B -> b
```

---

## 4. Components

- **`CFG.java`** – Main CFG class (parse, execute, pretty print, DOT, CNF conversion, CYK parsing). Returns rich ExecutionResult (accepted flag, messages, trace).
- **`Production.java`** – Represents a production rule (`A -> α`).
- **`NonTerminal.java`** – Non-terminal symbol object.
- **`Terminal.java`** – Terminal symbol object.
- **`GrammarParseException.java`** – Exception for parse/validation errors.

---

## 5. Visualization

- `Automaton.toGraphviz(...)` uses Graphviz to render the DOT produced by `CFG.toDotCode(...)`. Productions and variables are visualized for clarity.

---

## Directory Structure

```
ContextFreeGrammar/
├── CFG.java
├── GrammarParseException.java
├── NonTerminal.java
├── Production.java
└── Terminal.java
```

---

## Usage

- Parse a grammar from file or string.
- Validate and pretty-print the grammar.
- Convert to Chomsky Normal Form.
- Parse input strings using the CYK algorithm.
- Visualize the grammar using Graphviz.

---
