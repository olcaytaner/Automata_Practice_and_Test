# CS410 Exam Grading System

A comprehensive, automated grading system for CS410 formal automata exam submissions. This module provides batch processing of student submissions, automated testing against reference test cases, and generation of detailed grading reports in both CSV and HTML formats.

## Overview

The grading system automates the evaluation of student implementations of formal automata (DFA, NFA, PDA, TM, CFG, and Regular Expressions). It extracts student submissions, runs them against standardized test cases, calculates sophisticated metrics using logarithmic scoring, and generates detailed reports suitable for instructor review and student feedback.

### Key Features

- **Batch Processing**: Grade entire cohorts of students automatically
- **Multi-Format Support**: Handles all 6 automaton types (.dfa, .nfa, .rex, .pda, .tm, .cfg)
- **ZIP Extraction**: Automatically extracts and organizes student submission archives
- **Sophisticated Scoring**: Uses logarithmic grading formula for fair evaluation
- **Classification Metrics**: Tracks True/False Positives/Negatives, Accuracy, Precision, Recall, F1-Score
- **Dual Report Format**: Generates both CSV summaries and email-ready HTML reports
- **Timeout Protection**: Prevents infinite loops with configurable timeouts (5 seconds default)
- **Progress Tracking**: Real-time progress updates during batch grading

## Architecture

The grading system consists of four main components:

### 1. BatchGrader (`BatchGrader.java`)

The main orchestrator for batch grading operations.

**Responsibilities:**
- Extracts ZIP files containing student submissions
- Fixes nested folder structures (common submission issue)
- Coordinates grading across all students and questions
- Generates CSV and HTML reports
- Provides progress tracking and timing estimates

**Entry Point:**
```bash
java -cp CS410-Exam.jar grader.BatchGrader <exam_folder> <test_cases_folder> <output_folder>
```

### 2. ExamGrader (`ExamGrader.java`)

Handles grading of individual questions for a single student.

**Responsibilities:**
- Auto-detects automaton type by file extension
- Loads and parses automaton files (reuses UI parsing logic)
- Validates file size and content
- Runs test cases using TestRunner
- Returns comprehensive GradingResult with all metrics

**Key Methods:**
- `gradeQuestion(studentFolder, questionId, testCasesFolder)`: Main grading method
- `detectAutomatonFile(studentFolder, questionId)`: Finds student's submission file
- `loadAutomaton(file, extension)`: Parses automaton based on type

### 3. TestFileParser (`TestFileParser.java`)

Parses CSV test files with optional configuration headers.

**Test File Format:**
```csv
#min_points=4
#max_points=10
inputString,expectedResult
,0
10,1
01,1
11,0
```

**Features:**
- Supports optional `#min_points` and `#max_points` headers
- Validates CSV format (input,expected)
- Expected result: `1` for accept, `0` for reject
- Defaults: min_points=4, max_points=10

### 4. TestRunner (`TestRunner.java`)

Executes test cases against automaton implementations and calculates metrics.

**Responsibilities:**
- Runs test cases with timeout protection
- Tracks classification metrics (TP, TN, FP, FN)
- Calculates accuracy, precision, recall, F1-score
- Applies logarithmic grading formula
- Generates detailed student-friendly reports

## Usage

### Quick Start

The fastest way to get started grading:

```bash
# Run batch grading with Maven
mvn exec:java@grade \
  -Dexam.folder="exams/CS410 Mock Exam" \
  -Dtest.cases="reference_tests" \
  -Doutput="grading_results"

# Check the results
open grading_results/grading_summary.csv
open grading_results/reports/
```

### Batch Grading (All Students)

Grade an entire exam folder containing multiple student submissions:

```bash
mvn exec:java@grade \
  -Dexam.folder="exams/CS410 Mock Exam" \
  -Dtest.cases="reference_tests" \
  -Doutput="grading_results"
```

**Command Parameters:**
- `-Dexam.folder` - Folder containing student submissions (ZIP files or extracted folders)
- `-Dtest.cases` - Folder containing reference test files (*.test)
- `-Doutput` - Folder where results will be saved

**Expected Folder Structure:**
```
exams/CS410 Mock Exam/
├── john_doe_s123456.zip          # Student ZIP files
├── jane_smith_s234567.zip
└── ... (or already extracted folders)
    ├── john_doe_s123456/
    │   ├── Q1a.dfa
    │   ├── Q1b.nfa
    │   ├── Q2a.rex
    │   ├── Q2b.pda
    │   ├── Q3a.tm
    │   └── Q3b.cfg
    └── jane_smith_s234567/
        └── ...

reference_tests/
├── Q1a.test
├── Q1b.test
├── Q2a.test
├── Q2b.test
├── Q3a.test
└── Q3b.test
```

**Output:**
```
grading_results/
├── grading_summary.csv           # CSV with all scores
└── reports/
    ├── john_doe_s123456.html     # Individual HTML reports
    ├── jane_smith_s234567.html
    └── ...
```

### Individual Question Grading

For programmatic access (e.g., custom grading scripts or re-grading specific questions):

```java
import grader.ExamGrader;

ExamGrader.GradingResult result = ExamGrader.gradeQuestion(
    "exams/CS410 Mock Exam/john_doe_s123456",  // Student folder
    "Q1a",                                       // Question ID
    "reference_tests"                            // Test cases folder
);

System.out.println("Score: " + result.score);
System.out.println("Accuracy: " + result.accuracy + "%");
```

## Test File Format

Test files use a CSV format with optional configuration headers.

### Basic Format

```csv
inputString,expectedResult
```

- **inputString**: The input to test (can be empty for epsilon/empty string)
- **expectedResult**: `1` for accept, `0` for reject

### Optional Headers

```csv
#min_points=2
#max_points=10
#max_regex_length=50
```

- **#min_points**: Minimum points achievable (default: 4)
- **#max_points**: Maximum points achievable (default: 10)
- **#max_regex_length**: Maximum allowed regex length for `.rex` files (default: no limit)

### Complete Example

```csv
#min_points=2
#max_points=10
,0
1,0
0,0
11,0
10,1
01,1
00,0
111,0
110,1
101,1
100,1
011,1
010,1
001,1
000,0
```

This example tests a language that accepts binary strings ending in `10`, `01`, or `11` (but not `00`).

## Regex Length Validation

For Regular Expression (`.rex`) questions, you can enforce a maximum regex length limit to prevent overly complex or brute-force solutions.

### How It Works

1. **Configure Limit**: Add `#max_regex_length=N` to your test file
2. **Automatic Enforcement**: Length is checked after parsing, before running tests
3. **Sanitized Length**: Length is measured after:
   - Removing all whitespace
   - Converting `eps` to `ε`
   - Keeping all operators, symbols, and parentheses
4. **Automatic Zero**: If limit is exceeded, student receives 0 points automatically

### Example Test File

```csv
#min_points=4
#max_points=10
#max_regex_length=50
inputString,expectedResult
,0
a,1
aa,1
aaa,0
```

### Length Violation Reporting

When a student exceeds the regex length limit:

**CSV Report:**
```csv
Student,Q1a,Q1b,Q2a,Q2b,Q3a,Q3b,Total,Max,Notes
john_doe_s123456,10.0,8.5,0.0,9.0,5.0,10.0,42.5,60,"Q2a LENGTH VIOLATION (75/50)"
```

**HTML Report:**
```
REGEX LENGTH VIOLATION
Your regex exceeds the maximum allowed length.

Actual length:  75 characters
Maximum allowed: 50 characters
Exceeded by:    25 characters

Grade: 0.0/10 points (automatic zero for length violation)

Note: Length is measured after removing whitespace and normalizing 'eps' to 'ε'.
```

**Student Detail Report:**
```
REGEX LENGTH VIOLATION
══════════════════════════════════════════════════
Your regex exceeds the maximum allowed length.

Actual length:  75 characters
Maximum allowed: 50 characters
Exceeded by:    25 characters

Grade: 0.0/10 points (automatic zero for length violation)

Note: Length is measured after removing whitespace and normalizing 'eps' to 'ε'.
```

### Best Practices

- **Set Appropriate Limits**: Choose limits that allow elegant solutions but prevent brute-force approaches
- **Test Your Limit**: Verify reference solutions fit within the limit
- **Document in Instructions**: Tell students about length limits in exam instructions
- **Optional Feature**: Don't add `#max_regex_length` if you don't need length enforcement

### Why Enforce Length Limits?

1. **Encourage Elegance**: Students must find concise, well-structured solutions
2. **Prevent Brute Force**: Blocks solutions that simply enumerate all valid strings
3. **Fair Assessment**: Ensures students understand pattern construction, not just exhaustive enumeration
4. **Realistic Constraints**: Mirrors real-world scenarios where concise patterns are preferred

## Grading Formula

The system uses a sophisticated logarithmic grading formula that rewards correct implementations while heavily penalizing false positives.

### Classification Metrics

- **TP (True Positives)**: Correctly accepted strings
- **TN (True Negatives)**: Correctly rejected strings
- **FP (False Positives)**: Incorrectly accepted strings (should be rejected)
- **FN (False Negatives)**: Incorrectly rejected strings (should be accepted)

### Derived Metrics

- **Accuracy** = (TP + TN) / (TP + TN + FP + FN) × 100%
- **Precision** = TP / (TP + FP) × 100%
- **Recall** = TP / (TP + FN) × 100%
- **Specificity** = TN / (TN + FP) × 100%
- **F1-Score** = 2 × (Precision × Recall) / (Precision + Recall)

### Point Calculation

#### Case 1: No False Positives (FP = 0)

**Formula:**
```
points = (log₂(TP) × (max - min)) / log₂(TP + FN) + min
```

**Explanation:**
- Uses base-2 logarithm (intuitive for CS students - doublings/powers of 2)
- Calculates ratio of accepted strings to total required
- Scales this ratio across the point range [min, max]
- TP + FN = total strings that should be accepted

**Examples:**
- 2/1024 correct → ~5.0 points
- 1024/1024 correct → 10.0 points
- 512/1024 correct → ~9.0 points

#### Case 2: With False Positives (FP > 0)

**Formula:**
```
points = min / log₁₀(FN + FP + 10)
```

**Explanation:**
- Penalizes based on total errors (FN + FP)
- Typically results in very low scores (< 1.0 point)
- Discourages overly permissive implementations

**Example:**
- FP=127, FN=2492 → ~0.4 points

#### Trivial Answers

A submission that rejects every test string (TP = FP = 0), or accepts every test string (TN = FN = 0) when the suite contains strings to reject, gets 0 points. Without this rule, the penalty formula above gives them partial credit, for example 2.0 points for rejecting all 90 strings that should be accepted.

### Why Logarithmic?

The logarithmic formula provides several pedagogical benefits:

1. **Exponential Reward**: Getting twice as many test cases correct significantly increases your score
2. **Fairness**: Partial credit for partial solutions (not all-or-nothing)
3. **Strictness**: False positives are heavily penalized (accepting wrong strings is worse than rejecting correct ones)
4. **Scalability**: Works well regardless of test suite size

## Output Formats

### CSV Report (`grading_summary.csv`)

```csv
Student,Q1a,Q1b,Q2a,Q2b,Q3a,Q3b,Total,Max
john_doe_s123456,10.0,8.5,7.2,9.0,5.0,10.0,49.7,60
jane_smith_s234567,9.5,10.0,8.0,7.5,6.0,9.0,50.0,60
```

**Features:**
- One row per student
- Individual question scores
- Total score and maximum possible
- Easy to import into spreadsheets

### HTML Report (`student_name.html`)

Email-friendly HTML format with:

- **Header Section**: Student name, timestamp
- **Overall Score**: Total points / Maximum points
- **Question Breakdown Table**:
  - Color-coded scores (green ≥8, orange ≥4, red <4)
  - Classification metrics for each question
  - Error messages for failed submissions
- **Footer**: Instructions for grade appeals

**Use Cases:**
- Email directly to students
- Upload to LMS
- Print for record keeping

## Question IDs

The system expects these standard question IDs:

- `Q1a`, `Q1b` - Question 1, parts a and b
- `Q2a`, `Q2b` - Question 2, parts a and b
- `Q3a`, `Q3b` - Question 3, parts a and b

Each question ID maps to a test file and student submission file.

## Supported Automaton Types

The system auto-detects file types by extension:

| Extension | Type | Class |
|-----------|------|-------|
| `.dfa` | Deterministic Finite Automaton | `DeterministicFiniteAutomaton.DFA` |
| `.nfa` | Nondeterministic Finite Automaton | `NondeterministicFiniteAutomaton.NFA` |
| `.rex` | Regular Expression | `RegularExpression.SyntaxTree.SyntaxTree` |
| `.pda` | Push-down Automaton | `PushDownAutomaton.PDA` |
| `.tm` | Turing Machine | `TuringMachine.TM` |
| `.cfg` | Context-Free Grammar | `ContextFreeGrammar.CFG` |

## Workflow Example

### 1. Prepare Reference Tests

Create test files for each question:

```bash
reference_tests/
├── Q1a.test  # Binary strings ending in 1
├── Q1b.test  # Even number of zeros
├── Q2a.test  # a^n b^n
└── ...
```

### 2. Collect Student Submissions

Students submit ZIP files containing their automaton files:

```
john_doe_s123456.zip:
  ├── Q1a.dfa
  ├── Q1b.nfa
  └── ...
```

### 3. Run Batch Grading

```bash
mvn exec:java@grade \
  -Dexam.folder="exams/Midterm 2024" \
  -Dtest.cases="reference_tests" \
  -Doutput="results_midterm_2024"
```

### 4. Review Results

**Console Output:**
```
======================================================================
CS410 Exam Batch Grader (Pure Java)
======================================================================

Extracted 45 ZIP files

Starting batch grading...

Found 45 student folders

Progress: 5/45 students (11.1%) - Est. remaining: 120 seconds
Progress: 10/45 students (22.2%) - Est. remaining: 105 seconds
...
Progress: 45/45 students (100.0%) - Est. remaining: 0 seconds

Graded 45 students in 135.2 seconds

Generating reports...
CSV report saved: results_midterm_2024/grading_summary.csv
Generated 45 HTML reports in: results_midterm_2024/reports/

======================================================================
Grading complete! All done!
======================================================================
```

### 5. Distribute Results

- Open `grading_summary.csv` in Excel for quick overview
- Email individual HTML reports to students
- Upload to LMS if needed

## Error Handling

The system handles various error conditions gracefully:

### File Not Found
```
Error: No automaton file found for question Q1a in /path/to/student
Expected one of: Q1a.dfa, Q1a.nfa, Q1a.rex, Q1a.pda, Q1a.tm, Q1a.cfg
```

### Empty File
```
Error: Empty file (0 bytes): /path/to/Q1a.dfa
```

### Parse Error
```
Error: Parse error:
Line 5: Invalid transition format
Line 12: Unknown state 'q5'
```

### Timeout
```
TIMEOUT: Test suite execution exceeded 5000ms
```

## Performance Considerations

- **Default Timeout**: 5 seconds per test suite (configurable)
- **Batch Processing**: Progress updates every 5 students
- **Typical Speed**: ~3 seconds per student (6 questions)
- **Memory**: Minimal - processes one student at a time

## Customization

### Changing Question IDs

Edit `BatchGrader.java:19`:

```java
private static final String[] QUESTION_IDS = {"Q1a", "Q1b", "Q2a", "Q2b", "Q3a", "Q3b"};
```

### Changing Point Defaults

Edit `TestFileParser.java:57-58`:

```java
int minPoints = 4;  // Default minimum
int maxPoints = 10; // Default maximum
```

Or use per-file configuration via headers in `.test` files.

### Changing Timeout

Modify `TestRunner.java:74`:

```java
public static final long DEFAULT_TIMEOUT_MS = 5000; // 5 seconds
```

## Integration with Main Application

The grading system reuses core components from the main CS410-Exam application:

- **Parsing**: Uses same `Automaton.parse()` as the UI
- **Execution**: Uses same `Automaton.execute()` as the UI
- **Validation**: Ensures grading matches student's UI experience

This guarantees that grading results match what students see when testing locally.

## Troubleshooting

### GUI Opens Instead of Grading

**Problem:** Running the command opens the UI application instead of grading

**Solution:** Make sure you're using the `@grade` execution:
```bash
mvn exec:java@grade \
  -Dexam.folder="..." \
  -Dtest.cases="..." \
  -Doutput="..."
```

### Compilation Errors

**Problem:** Maven compilation fails

**Solution:** Clean and compile:
```bash
mvn clean compile
```

Check Java version (requires Java 8+):
```bash
java -version
mvn -version
```

### No ZIP files extracted
- Verify ZIP files are in the exam folder
- Check ZIP files are valid archives
- Ensure write permissions on exam folder

### All students score 0
- Verify reference test files exist in test_cases_folder
- Check test file format (CSV with proper headers)
- Ensure test file names match question IDs

### Timeouts on every test
- Check automaton implementations for infinite loops
- Increase timeout if dealing with large test suites
- Verify automaton files parse correctly

### Nested folder issues
- System automatically detects and fixes double-nested folders
- If issues persist, manually check folder structure

## Related Files

- `src/main/java/common/TestRunner.java` - Test execution engine
- `src/main/java/common/TestFileParser.java` - Test file parsing
- `src/main/java/common/Automaton.java` - Base automaton interface
- `src/test/java/` - Unit tests for grading components

## License

Part of the CS410 Formal Automata course materials.
