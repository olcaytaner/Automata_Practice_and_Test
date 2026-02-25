# Changelog

## [1.4.1] - 2026-02-25

### Changed
- **Test Case Sorting**: Test cases are now sorted by input string length (ascending) after parsing (#50)
  - Shortest strings execute and display first, making it easier for students to trace failures manually
  - Sorting applied in `TestFileParser` so both normal and timeout execution paths benefit

## [1.3.3] - 2025-12-23

### Fixed
- **TM Tape**: Fixed Turing Machine tape to be left-bounded (tape head cannot move left of initial position)

## [1.3.2] - 2025-12-15

### Added
- **Version Management System**: Centralized version management with automatic update notifications
  - New `AppVersion` class for programmatic version access and comparison
  - `VersionChecker` service that checks GitHub releases for newer versions
  - `UpdateDialog` showing update availability with changelog and download link
  - `version.properties` resource file for build-time version injection
  - Automatic update check on application startup (non-blocking)
- **GitHub Release Workflow**: Enhanced release automation
  - Changelog extraction step automatically includes relevant version notes in GitHub releases

### Changed
- `MainFrame` and `SplashScreen` now use centralized `AppVersion` for version display
- Version information consolidated in single source of truth (`version.properties`)

## [1.3.1] - 2025-12-14

### Fixed
- **TM Validation**: Enhanced Turing Machine file validation
  - Input alphabet cannot contain blank symbol '_'
  - Input symbols must be included in tape_alphabet
  - Accept state must be named 'q_accept'
  - Reject state must be named 'q_reject'
- **CFG**: Minor fix for production results handling (null safety improvements)

### Added
- **TM Test Files**: Past exam questions for Turing Machine exercises
  - Final exams: 2021-q3, 2023-q5, 2024-q3, 2025-q1
  - MT3 exams: 2024-q1, 2024-q2, partb_2022-q2, partb_2024-q2

## [1.3.0] - 2025-12-04

### Performance
- **CFG Major Performance Optimization**: Complete rewrite of CYK parsing algorithm
  - Replaced object-based Sets with primitive BitSet arrays for parsing table
  - Added O(1) lookup maps for variables, terminals, and productions
  - Implemented integer ID system for NonTerminals enabling array-based indexing
  - Array-based binary production results replacing object collections
  - Added CNF caching (`cachedCNF`) to avoid repeated Chomsky Normal Form conversion
  - Grammar string caching for repeated string operations
  - Net reduction of ~384 lines while significantly improving performance

## [1.2.11] - 2025-12-03

### Added
- **CFG Rules Limit**: Configurable maximum production rules for CFG
  - New "Max Rules:" UI field in CFG panel
  - Test file header `#max_rules=N` support
  - Automatic zero points if CFG exceeds rule limit
- **PDA Transitions Limit**: Configurable maximum transitions for PDA
  - New "Max Trans:" UI field in PDA panel
  - Test file header `#max_transitions=N` support
  - Automatic zero points if PDA exceeds transitions limit
- Validation integrated in both UI test runner and ExamGrader

### Changed
- Multi-character push to stack is no longer allowed in PDA (single character only)

## [1.2.10] - 2025-12-01

### Changed
- Stack does not have to be empty for acceptance anymore.
- Minor .test file fixes for PDA exercises.

## [1.2.9] - 2025-11-30

### Added
- **Configurable Test Timeout**: UI field and test file header support for custom timeout values (#48)
  - New "Timeout(s):" field in automaton panels alongside Min/Max points
  - Default timeout: 5 seconds (configurable by user)
  - Test file header `#timeout=N` support to override UI setting per test file
  - Validation for positive integer values with user-friendly error messages
  - Progress dialog displays effective timeout value during test execution

### Changed
- Test runner now accepts custom timeout from UI instead of always using DEFAULT_TIMEOUT_MS
- TestFileParser.TestFileResult class extended with timeout field and getters

## [1.2.8] - 2025-11-30

### Changed
- Updated PDA execution semantics to match lecture model:
  - Acceptance now requires fully consumed input, a final state, and an empty stack.
  - Made `stack_start` optional; when omitted or set to `eps`, the stack truly starts empty.
- Adjusted the default PDA template and UI example to use the “epsilon transition to push Z as bottom-of-stack” pattern.

### Fixed
- Rewrote all week 8 PDA exercise `.pda` files to match the new execution semantics.

## [1.2.7] - 2025-11-29

### Added
- Week 9 mock exam CFG and test files, adding ~12K test lines for manual CFG testing.

### Changed
- Updated the default PDA template to match the PDA syntax format.
- Updated the PDA example in the Syntax Help dialog so it is consistent with the new default PDA template shown in the editor.

### Fixed
- Improved CFG parser robustness by normalizing whitespace (including non-breaking spaces) before parsing, fixing issues with malformed input files.


## [1.2.6] - 2025-11-17

### Added
- **PDF Report Generation**: Individual student exam reports with detailed grading information
  - New StudentPdfExporter class for generating professional PDF reports
  - Integration with BatchGrader for automated PDF generation per student
  - Comprehensive report sections: student info, question breakdown, grading summary
  - Added iText library dependency for PDF creation
- **Clear Graph Button**: UI enhancement for graph visualization workflow
  - New "Clear" button in automaton panels to clear graph without recompiling
  - Keyboard shortcut support (Ctrl+G) for quick graph clearing
  - Improves iterative automaton design workflow
- **PDA Test Files**: Comprehensive test suite for week 8 exercises
  - Added test files for exercises week8-1 through week8-10
  - Over 47,000 lines of test cases for PDA validation
  - Complete coverage of push-down automaton exercises
- **Regex Length Limiting**: Dynamic regex length validation during grading
  - New `#max_regex_length` parameter in test files for per-question length limits
  - Automatic zero points awarded if student's regex exceeds the specified limit
  - Length measured after sanitization (whitespace removed, eps→ε conversion)
  - Length violation tracking in GradingResult with actualLength and maxAllowedLength fields
  - CSV reports now include "Notes" column showing length violations (e.g., "Q2a LENGTH VIOLATION (75/50)")
  - HTML reports display highlighted warning sections for length violations with yellow background
  - Detailed error messages showing actual vs. allowed length and characters exceeded

### Changed
- **CFG Epsilon Representation**: Standardized epsilon notation from "_" to "eps" (#46)
  - Updated CFG parser to recognize "eps" as epsilon in productions
  - Modified Terminal class epsilon handling for new notation
  - Updated all CFG test files and examples to use "eps" notation
  - More consistent with standard context-free grammar notation
  - **Breaking Change**: Old CFG files using "_" for epsilon must be updated to "eps"

### Enhanced
- TestFileParser now supports `#max_regex_length` configuration header
- TestRunner propagates regex length limits from test files to grading results
- SyntaxTree class stores sanitized regex and provides length validation method
- ExamGrader enforces length limits before running tests for .rex files
- BatchGrader report generation includes length violation information in both CSV and HTML formats
- Graph visualization panel with improved user control and keyboard shortcuts

### Fixed
- Issue #32: Added clear graph functionality for better UI workflow
- Issue #46: Standardized epsilon representation in context-free grammars

### Documentation
- Updated grader README with comprehensive Regex Length Validation section
- Added usage examples and best practices for setting length limits
- Documented length violation reporting formats
- Updated CFG README with new epsilon notation and usage examples

## [1.2.5] - 2025-11-04

### Added
- DFA validation in execute() method to check for complete transition function before execution
- Validation error detection in TestRunner to prevent grading of invalid automata

### Fixed
- Invalid DFAs no longer receive partial credit during batch grading
- GraphViz visualization error handling for DFAs with missing transitions

### Improved
- Clear error messages when DFA is incomplete or improperly configured
- Better separation between validation errors and execution errors

## [1.2.4] - 2025-10-24

### Changed
- Simplified release workflow to build single universal JAR instead of multiple Java-specific versions
- Extended Java testing support to Java 24 and 25 (GA releases)
- Added Java 26 (Early Access) testing support
- Restructured test workflow to verify Java 8-built JAR compatibility across all supported Java versions
- Release workflow now includes JAR verification step on all supported Java versions before publishing

### Technical Details
- Test workflow now builds JAR once with Java 8, then tests that specific JAR on Java 8, 11, 17, 21, 24, and 25
- Release workflow includes new `verify-jar` job that validates the release artifact on all supported Java versions
- Ensures released JAR actually works on all claimed supported versions (8+)

## [1.2.3] - 2025-10-23

### Added
- Syntax Help dialog and menu item in Help menu
- DFA test files for exercises
- Default template for SyntaxTree class
- Epsilon support for regex with revised regex exercises

### Changed
- GraalVM plugin configuration refactored to use arguments instead of system properties
- GraalVM dependency version updated with enhanced graph compilation checks

### Fixed
- Regex README documentation
- Issue #38 (compilation fix)
- Merge pull request #40 for compilation improvements

## [1.2.2] - 2025-10-16

### Changed
- Improved false positive handling in scoring algorithm
- Adjusted log10 calculation for penalty scoring (changed denominator from FN+FP+2 to FN+FP+10)
- Refined conditional logic for zero false positive cases in point calculation

### Improved
- More accurate grading with better handling of edge cases
- Optimized import statements in TestRunner

## [1.2.1] - 2025-10-16

### Added
- Resizable divider between text editor and graph visualization panels
- Custom divider styling with grip lines for better visibility
- One-touch expandable arrows for quick panel collapse/expand
- Resize cursor on hover to indicate draggable divider

### Improved
- Enhanced UI usability with more obvious resizing controls
- Proportional panel resizing when window is resized (30/70 split)

## [1.2.0] - 2025-10-16

### Changed
- Refactored calculatePoints and getPoints methods to return double values for more precise point calculations
- Updated documentation for grading methods to reflect double-precision scoring

## [1.1.6] - 2025-10-12

### Changed
- Enhanced compileWithFigure method to skip visualization for CFG and REGEX types

### Added
- Regex test files and week 5 regex exercises
- README updates for regex documentation

## [1.1.5] - 2025-10-12

### Changed
- Refactored scoring logic to remove minimum point constraint in TestRunner

## [1.1.4] - 2025-10-12

### Added
- Grading functionality for exams with automatic points calculation
- Grading configuration fields (minimum points, per-test points, penalty points) in exam interface
- Student-friendly test results reporting with detailed grading summary
- Enhanced test results display showing points earned vs total points

### Fixed
- NFA epsilon transition bug causing incorrect acceptance/rejection
- Test case generation producing wrong solutions

### Improved
- Additional NFA execution tests for better coverage
- Test result validation and error reporting

## [1.1.3] - 2025-10-09

### Fixed
- Regex replacement in state and transition validation to use replaceFirst instead of replace

### Changed
- Added exclusions for xml-apis and xalan in batik-all dependency to avoid conflicts

## [1.1.2] - 2025-10-09

### Fixed
- CFG bug caused by epsilon productions

### Changed
- Updated default TM template

## [1.1.1] - 2025-10-05

### Added
- NFA test case generation for exercises
- Warning when NFA state name is too long

### Changed
- NFA now accepts alphanumerical state names (not just numerical)

### Fixed
- JAR artifact download path in release workflow

## [1.1.0] - 2025-10-05

### Changed
- Improved SVG rendering with JSVGCanvas replacing previous image handling
- Removed custom resize logic as JSVGCanvas handles dynamic resizing internally
- Added zoom and pan interactions for figure visualization

### Fixed
- Pixelated figure rendering issues (#24)
- Incomplete DFA compilation bug
- Dialog options handling in save confirmation
- File closing issues (#25)
- Exception handling improvements
- GraphViz rendering tests now validate SVG output instead of image icons

### Improved
- NFA tests enabled and fixed

## [1.0.2] - 2025-09-30

### Added
- Undo/redo functionality in AbstractAutomatonPanel
- Automaton validation bug report template

### Fixed
- Multiple transition with the same symbol from the same state bug (#16)

### Changed
- Update version retrieval method in MainFrame and SplashScreen to read from Maven properties
- Refactor MockAutomatonPanel methods for clarity and consistency

## [1.0.1] - Previous Release