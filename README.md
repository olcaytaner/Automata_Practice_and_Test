# Automata Practice and Test

<div align="center">

**Master computational theory through hands-on practice with automata, grammars, and formal languages.**

[![Java](https://img.shields.io/badge/Java-8%2B-orange?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![Release](https://img.shields.io/github/v/release/olcaytaner/Automata_Practice_and_Test?style=flat-square)](https://github.com/olcaytaner/Automata_Practice_and_Test/releases)
[![Buy Me A Coffee](https://img.shields.io/badge/Buy%20Me%20A%20Coffee-support-yellow?style=flat-square&logo=buy-me-a-coffee)](https://buymeacoffee.com/automatapracticetest)

[Download](#-quick-start) | [Features](#-features) | [Documentation](#-supported-automata) | [Contributing](#-development-team)

</div>

---

## Why This Tool?

Learning automata theory from textbooks can be challenging. **Automata Practice and Test** bridges the gap between theory and understanding by letting you:

- **See** your automata come to life as interactive state diagrams
- **Test** your solutions instantly with custom input strings
- **Debug** step-by-step to understand exactly how your machine processes input
- **Practice** with ready-to-use exercises and exam questions

Whether you're a student preparing for exams or an educator teaching computational theory, this tool makes abstract concepts tangible.

---

## What Can You Build?

<table>
<tr>
<td width="33%" align="center">

**DFA & NFA**

Finite Automata

*Pattern matching, lexical analysis*

</td>
<td width="33%" align="center">

**PDA**

Push-down Automata

*Balanced brackets, nested structures*

</td>
<td width="33%" align="center">

**TM**

Turing Machines

*Any computable function*

</td>
</tr>
<tr>
<td width="33%" align="center">

**CFG**

Context-Free Grammars

*Programming language syntax*

</td>
<td width="33%" align="center">

**REX**

Regular Expressions

*Text pattern matching*

</td>
<td width="33%" align="center">

**Grading**

Batch Testing

*Automated exam grading*

</td>
</tr>
</table>

---

## Features

### For Students
- **Visual Learning**: See automata rendered as beautiful state diagrams
- **Instant Feedback**: Test inputs and see Accept/Reject immediately
- **Step-by-Step Traces**: Understand exactly how your machine processes each symbol
- **Practice Files**: Includes exercises from real CS theory courses

### For Educators
- **Batch Grading**: Grade entire classes with automated test suites
- **PDF Reports**: Generate detailed grading reports per student
- **Configurable Limits**: Set max rules, transitions, regex length, and timeouts
- **Fair Scoring**: Sophisticated scoring algorithm handles edge cases

### For Everyone
- **Cross-Platform**: Works on Windows, macOS, and Linux
- **No Installation**: Just download the JAR and run
- **Auto-Updates**: Get notified when new versions are available
- **Syntax Help**: Built-in documentation for all file formats

---

## Quick Start

### Requirements
- **Java 8** or higher (that's it!)

### Option 1: Download & Run (Recommended)

1. **Download** the latest JAR from [Releases](https://github.com/olcaytaner/Automata_Practice_and_Test/releases)
2. **Run** with double-click or:
   ```bash
   java -jar CS410-Exam-1.4.3.jar
   ```

**That's it!** No Maven, no compilation, no setup.

### Option 2: Build from Source

```bash
git clone https://github.com/olcaytaner/Automata_Practice_and_Test.git
cd Automata_Practice_and_Test
mvn clean package
java -jar target/CS410-Exam-1.4.3.jar
```

---

## Your First Automaton

Let's build a DFA that accepts strings ending with "ab":

### 1. Create New File
**File → New → DFA**

### 2. Enter Definition
```text
states: q0 q1 q2
alphabet: a b
start: q0
accept: q2

transitions:
q0, b -> q0
q0, a -> q1
q1, a -> q1
q1, b -> q2
q2, b -> q0
q2, a -> q1
```

### 3. Compile & Visualize
Press `Ctrl+B` (or `Cmd+B` on Mac) to see your automaton:

### 4. Test It!
Press `Ctrl+R` and try these inputs:
- `ab` → **Accepted** ✓
- `aab` → **Accepted** ✓
- `ba` → **Rejected** ✗
- `abb` → **Rejected** ✗

---

## Keyboard Shortcuts

| Action | Windows/Linux | macOS |
|--------|:-------------:|:-----:|
| Compile & Visualize | `Ctrl+B` | `Cmd+B` |
| Run Test | `Ctrl+R` | `Cmd+R` |
| Run with File | `Ctrl+Shift+R` | `Cmd+Shift+R` |
| Clear Graph | `Ctrl+G` | `Cmd+G` |
| Save | `Ctrl+S` | `Cmd+S` |
| Open | `Ctrl+O` | `Cmd+O` |
| Recent Files | `Ctrl+1-9` | `Cmd+1-9` |

---

## Supported Automata

### DFA (Deterministic Finite Automaton)

**Use case**: Pattern matching with deterministic rules

```text
states: q0 q1 q2
alphabet: a b
start: q0
accept: q2

transitions:
q0, a -> q1
q0, b -> q0
q1, b -> q2
q2, a -> q1
```

**File extension**: `.dfa`

---

### NFA (Nondeterministic Finite Automaton)

**Use case**: Pattern matching with multiple possibilities and ε-transitions

```text
states: q0 q1 q2
alphabet: a b
start: q0
accept: q2

transitions:
q0, a -> q1
q0, eps -> q1
q0, b -> q0
q1, b -> q2
q2, a -> q1
```

**Note**: Use `eps` for epsilon (ε) transitions

**File extension**: `.nfa`

---

### PDA (Push-down Automaton)

**Use case**: Context-free languages like balanced parentheses, `a^n b^n`

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

**Format**: `<state>, <input>, <stack_pop> -> <next_state>, <stack_push>`

**Acceptance**: Input consumed + final state + empty stack

**File extension**: `.pda`

---

### TM (Turing Machine)

**Use case**: Any computable function

```text
states: q0 q1 q_accept q_reject
input: 0 1
tape: 0 1 X _
start: q0
accept: q_accept
reject: q_reject

transitions:
q0, 0 -> q1, X, R
q0, 1 -> q0, 1, R
q0, _ -> q_accept, _, R
q1, 0 -> q0, 0, R
q1, 1 -> q1, 1, R
q1, _ -> q_reject, _, R
```

**Format**: `<state>, <read> -> <next_state>, <write>, <direction>`

**Requirements**:
- Accept state must be `q_accept`
- Reject state must be `q_reject`
- Use `_` for blank, `L`/`R` for direction

**File extension**: `.tm`

---

### CFG (Context-Free Grammar)

**Use case**: Programming language syntax, nested structures

```text
vars: S A B
terminals: a b
start: S

rules:
S -> A B | a S b
A -> a | a A
B -> b | b B
```

**Note**: Use `eps` for epsilon productions. Parsed with CYK algorithm.

**File extension**: `.cfg`

---

### REX (Regular Expression)

**Use case**: Pattern matching, input validation

```text
alphabet: a b c d
pattern: (ab*c) u d u eps
```

**Format**: `alphabet:` defines valid symbols, `pattern:` defines the regex

**Operators**: `*` (Kleene star), `u` (union), `()` (grouping)

**File extension**: `.rex`

---

## File Format Quick Reference

| Type | Extension | Key Sections |
|:----:|:---------:|--------------|
| DFA | `.dfa` | states, alphabet, start, accept, transitions |
| NFA | `.nfa` | states, alphabet, start, accept, transitions |
| PDA | `.pda` | states, input, stack, start, stack_start, accept, transitions |
| TM | `.tm` | states, input, tape, start, accept, reject, transitions |
| CFG | `.cfg` | vars, terminals, start, rules |
| REX | `.rex` | alphabet, pattern |

**Tip**: Use `#` for comments in all formats (except REX)

---

## For Educators: Batch Grading

Grade entire classes with a single command:

```bash
mvn exec:java@grade -Dexam.folder=submissions/ -Dtest.cases=tests/ -Doutput=results/
```

### Test File Format
```text
#timeout=5
#max_rules=10
accept: aab
accept: ab
reject: ba
reject: a
```

### Output
- **CSV**: Summary with scores for all students
- **HTML**: Detailed report with pass/fail per test
- **PDF**: Individual student reports

---

## Project Structure

```
Automata_Practice_and_Test/
├── src/main/java/
│   ├── common/                    # Base classes
│   ├── DeterministicFiniteAutomaton/
│   ├── NondeterministicFiniteAutomaton/
│   ├── PushDownAutomaton/
│   ├── TuringMachine/
│   ├── ContextFreeGrammar/
│   ├── RegularExpression/
│   ├── UserInterface/             # GUI
│   └── grader/                    # Batch grading
├── src/test/java/                 # Unit tests
└── pom.xml
```

---

## Dependencies

| Library | Version | Purpose |
|---------|---------|---------|
| JUnit | 5.8.2 | Testing |
| GraphViz Java | 0.18.1 | Visualization |
| GraalVM JS | 20.0.0 | Graph rendering |
| Apache Batik | 1.19 | SVG rendering |
| Apache PDFBox | 2.0.27 | PDF generation |

All managed by Maven — no manual installation needed.

---

## Troubleshooting

**Graph not showing?**
```bash
mvn clean compile
```

**JAR won't start?**
```bash
java -version  # Must be 8+
```

**Tests failing?**
```bash
mvn test -X  # Verbose output
```

---

## Development Team

**Automata Practice and Test** was developed at Özyeğin University:

- [Ege Yenen](https://github.com/yenennn)
- [Bora Baran](https://github.com/Bor4brn)
- [Berre Delikara](https://github.com/BerreDelikara)
- [Eren Yemşen](https://github.com/ErenYemsen)
- [Berra Eğcin](https://github.com/berraegcin)
- [Hakan Akbıyık](https://github.com/xHkn10)
- [Hakan Çildaş](https://github.com/atahakancildas)
- [Selim Özyılmaz](https://github.com/NurettinSelim)
- [Olcay Taner Yıldız](https://github.com/olcaytaner)

---

## Documentation

- [CHANGELOG.md](CHANGELOG.md) — Version history

---

## Support the Project

If this tool helped you learn or teach automata theory, consider supporting its development:

<div align="center">

[![Buy Me A Coffee](https://img.shields.io/badge/Buy%20Me%20A%20Coffee-support-yellow?style=for-the-badge&logo=buy-me-a-coffee)](https://buymeacoffee.com/automatapracticetest)

</div>

---

## Getting Help

- **In-app**: Help → Syntax Help for file format documentation
- **Issues**: [GitHub Issues](https://github.com/olcaytaner/Automata_Practice_and_Test/issues)
- **Updates**: The app checks for updates automatically on startup

---

<div align="center">

**Happy automaton building!** 🤖

*Made with ❤️ for computer science students everywhere*

</div>
