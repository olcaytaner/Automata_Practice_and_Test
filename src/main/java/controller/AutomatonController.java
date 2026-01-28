package controller;

import java.io.File;
import java.io.IOException;
import java.util.List;

import common.Automaton;
import common.ExecutionResult;
import common.MachineType;
import common.ParseResult;
import common.TestRunner;
import common.ValidationMessage;
import service.AutomatonService;
import service.FileService;
import service.SessionService;
import service.TestService;
import service.VisualizationService;
import viewmodel.TestResultViewModel;

/**
 * Controller for automaton operations.
 * Coordinates between services and views, handling the application logic flow.
 * Designed to work with any view layer (Swing, JavaFX, etc.)
 */
public class AutomatonController {

    private final AutomatonService automatonService;
    private final FileService fileService;
    private final TestService testService;
    private final VisualizationService visualizationService;
    private final SessionService sessionService;

    /**
     * Creates a controller with all services.
     */
    public AutomatonController(AutomatonService automatonService,
                               FileService fileService,
                               TestService testService,
                               VisualizationService visualizationService,
                               SessionService sessionService) {
        this.automatonService = automatonService;
        this.fileService = fileService;
        this.testService = testService;
        this.visualizationService = visualizationService;
        this.sessionService = sessionService;
    }

    /**
     * Creates a controller with default services.
     */
    public AutomatonController() {
        this(new AutomatonService(),
             new FileService(),
             new TestService(),
             new VisualizationService(),
             new SessionService());
    }

    // ═══════════════════════════════════════════════════════════════════
    // AUTOMATON OPERATIONS
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Creates a new automaton of the specified type.
     *
     * @param type The type of automaton to create
     * @return The new automaton
     */
    public Automaton createAutomaton(MachineType type) {
        return automatonService.createAutomaton(type);
    }

    /**
     * Gets the default template for an automaton type.
     *
     * @param type The machine type
     * @return The default template string
     */
    public String getDefaultTemplate(MachineType type) {
        return automatonService.getDefaultTemplate(type);
    }

    /**
     * Parses the automaton definition.
     *
     * @param automaton The automaton to parse
     * @param inputText The definition text
     * @return ParseResult with success/failure and messages
     */
    public ParseResult parse(Automaton automaton, String inputText) {
        return automatonService.parse(automaton, inputText);
    }

    /**
     * Executes the automaton on an input string.
     *
     * @param automaton The automaton to execute
     * @param input The input string to process
     * @return ExecutionResult with acceptance and trace
     */
    public ExecutionResult execute(Automaton automaton, String input) {
        return automatonService.execute(automaton, input);
    }

    /**
     * Validates an automaton definition.
     *
     * @param automaton The automaton
     * @param inputText The definition text
     * @return List of validation messages
     */
    public List<ValidationMessage> validate(Automaton automaton, String inputText) {
        return automatonService.validate(automaton, inputText);
    }

    /**
     * Formats validation messages for display.
     *
     * @param messages The validation messages
     * @return Formatted string
     */
    public String formatValidationMessages(List<ValidationMessage> messages) {
        return automatonService.formatValidationMessages(messages);
    }

    // ═══════════════════════════════════════════════════════════════════
    // COMPILATION WITH VISUALIZATION
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Result of a compile operation with visualization.
     */
    public static class CompileResult {
        private final boolean success;
        private final List<ValidationMessage> validationMessages;
        private final VisualizationService.VisualizationResult visualizationResult;
        private final String errorMessage;

        public CompileResult(boolean success,
                            List<ValidationMessage> validationMessages,
                            VisualizationService.VisualizationResult visualizationResult,
                            String errorMessage) {
            this.success = success;
            this.validationMessages = validationMessages;
            this.visualizationResult = visualizationResult;
            this.errorMessage = errorMessage;
        }

        public boolean isSuccess() { return success; }
        public List<ValidationMessage> getValidationMessages() { return validationMessages; }
        public VisualizationService.VisualizationResult getVisualizationResult() { return visualizationResult; }
        public String getErrorMessage() { return errorMessage; }
        public boolean hasVisualization() { return visualizationResult != null && visualizationResult.isSuccess(); }
        public String getSvgContent() { return hasVisualization() ? visualizationResult.getSvgContent() : null; }
    }

    /**
     * Compiles the automaton definition and generates visualization.
     *
     * @param automaton The automaton
     * @param inputText The definition text
     * @return CompileResult with validation and visualization
     */
    public CompileResult compileWithVisualization(Automaton automaton, String inputText) {
        // Parse the automaton
        ParseResult parseResult = parse(automaton, inputText);

        // Get validation messages
        List<ValidationMessage> messages = validate(automaton, inputText);

        if (!parseResult.isSuccess()) {
            return new CompileResult(false, messages, null, "Parsing failed");
        }

        // Generate visualization if supported
        VisualizationService.VisualizationResult vizResult = null;
        if (visualizationService.supportsVisualization(automaton.getMachineType())) {
            vizResult = visualizationService.generateVisualization(automaton, inputText);
        }

        return new CompileResult(true, messages, vizResult, null);
    }

    // ═══════════════════════════════════════════════════════════════════
    // FILE OPERATIONS
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Loads an automaton from a file.
     *
     * @param file The file to load
     * @return LoadResult with automaton and content
     */
    public LoadResult loadFromFile(File file) {
        try {
            MachineType type = fileService.getMachineTypeForFile(file);
            if (type == null) {
                return LoadResult.failure("Unsupported file type: " + fileService.getFileExtension(file));
            }

            String content = fileService.loadFromFile(file);
            Automaton automaton = automatonService.createAutomaton(type);

            // Add to recent files
            sessionService.addRecentFile(file);

            return LoadResult.success(automaton, content, file);
        } catch (IOException e) {
            return LoadResult.failure("Failed to load file: " + e.getMessage());
        }
    }

    /**
     * Result of a load operation.
     */
    public static class LoadResult {
        private final boolean success;
        private final Automaton automaton;
        private final String content;
        private final File file;
        private final String errorMessage;

        private LoadResult(boolean success, Automaton automaton, String content,
                          File file, String errorMessage) {
            this.success = success;
            this.automaton = automaton;
            this.content = content;
            this.file = file;
            this.errorMessage = errorMessage;
        }

        public static LoadResult success(Automaton automaton, String content, File file) {
            return new LoadResult(true, automaton, content, file, null);
        }

        public static LoadResult failure(String errorMessage) {
            return new LoadResult(false, null, null, null, errorMessage);
        }

        public boolean isSuccess() { return success; }
        public Automaton getAutomaton() { return automaton; }
        public String getContent() { return content; }
        public File getFile() { return file; }
        public String getErrorMessage() { return errorMessage; }
    }

    /**
     * Saves content to a file.
     *
     * @param file The file to save to
     * @param content The content to save
     * @return true if successful
     */
    public boolean saveToFile(File file, String content) {
        try {
            boolean result = fileService.saveToFile(file, content);
            if (result) {
                sessionService.addRecentFile(file);
            }
            return result;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Ensures a file has the correct extension for the automaton type.
     *
     * @param file The original file
     * @param automaton The automaton
     * @return File with correct extension
     */
    public File ensureCorrectExtension(File file, Automaton automaton) {
        return fileService.ensureCorrectExtension(file, automaton);
    }

    /**
     * Gets the MachineType for a file.
     *
     * @param file The file
     * @return The MachineType, or null if unknown
     */
    public MachineType getMachineTypeForFile(File file) {
        return fileService.getMachineTypeForFile(file);
    }

    // ═══════════════════════════════════════════════════════════════════
    // TEST OPERATIONS
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Finds the test file for an automaton file.
     *
     * @param automatonFile The automaton file
     * @return The test file, or null if not found
     */
    public File findTestFile(File automatonFile) {
        return testService.findTestFile(automatonFile);
    }

    /**
     * Runs tests from a file with callback for progress updates.
     *
     * @param automaton The automaton to test
     * @param testFilePath Path to the test file
     * @param progressCallback Callback for progress updates (can be null)
     * @return TestResult
     */
    public TestRunner.TestResult runTests(Automaton automaton, String testFilePath,
                                          TestService.TestProgressCallback progressCallback) {
        SessionService.TestSettings settings = sessionService.getTestSettings();
        return testService.runTests(automaton, testFilePath, settings.getTimeoutMs(), progressCallback);
    }

    /**
     * Runs tests with default settings and no callback.
     *
     * @param automaton The automaton to test
     * @param testFilePath Path to the test file
     * @return TestResult
     */
    public TestRunner.TestResult runTests(Automaton automaton, String testFilePath) {
        return runTests(automaton, testFilePath, null);
    }

    /**
     * Runs tests with validation, returning a ViewModel that encapsulates all results.
     * This method is the preferred way to run tests as it handles all validation
     * and eliminates the need for UI layer to perform instanceof checks.
     *
     * @param automaton The automaton to test
     * @param testFilePath Path to the test file
     * @param callback Optional progress callback
     * @return TestResultViewModel containing either limit violation or test results
     */
    public TestResultViewModel runTestsWithViewModel(
            Automaton automaton,
            String testFilePath,
            TestService.TestProgressCallback callback) {

        SessionService.TestSettings settings = sessionService.getTestSettings();
        return testService.runTestsWithValidation(automaton, testFilePath, settings, callback);
    }

    /**
     * Validates automaton limits (CFG rules, PDA transitions).
     *
     * @param automaton The automaton to validate
     * @return ValidationMessage if limit exceeded, null otherwise
     */
    public ValidationMessage validateLimits(Automaton automaton) {
        SessionService.TestSettings settings = sessionService.getTestSettings();
        return testService.validateLimits(automaton, settings.getMaxRules(), settings.getMaxTransitions());
    }

    /**
     * Formats test results for display.
     *
     * @param result The test result
     * @return Formatted string
     */
    public String formatTestResults(TestRunner.TestResult result) {
        return testService.formatTestResults(result);
    }

    // ═══════════════════════════════════════════════════════════════════
    // SESSION MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Gets recent files list.
     *
     * @return List of recent file paths
     */
    public List<String> getRecentFiles() {
        return sessionService.getRecentFiles();
    }

    /**
     * Clears recent files list.
     */
    public void clearRecentFiles() {
        sessionService.clearRecentFiles();
    }

    /**
     * Removes a file from the recent files list.
     *
     * @param filePath The file path to remove
     */
    public void removeRecentFile(String filePath) {
        sessionService.removeRecentFile(filePath);
    }

    /**
     * Adds a file to the recent files list.
     *
     * @param file The file to add
     */
    public void addRecentFile(File file) {
        sessionService.addRecentFile(file);
    }

    /**
     * Adds a file path to the recent files list.
     *
     * @param filePath The file path to add
     */
    public void addRecentFile(String filePath) {
        sessionService.addRecentFile(filePath);
    }

    /**
     * Gets the last used directory.
     *
     * @return The last directory, or null
     */
    public File getLastDirectory() {
        return sessionService.getLastDirectoryAsFile();
    }

    /**
     * Sets the last used directory.
     *
     * @param directory The directory
     */
    public void setLastDirectory(File directory) {
        if (directory != null) {
            sessionService.setLastDirectory(directory.getAbsolutePath());
        }
    }

    /**
     * Saves the list of currently open files for session restore.
     *
     * @param openFiles List of open files
     */
    public void saveOpenFiles(List<File> openFiles) {
        sessionService.saveOpenFiles(openFiles);
    }

    /**
     * Gets the list of files that were open in the last session.
     *
     * @return List of file paths
     */
    public List<String> getLastOpenedFiles() {
        return sessionService.getLastOpenedFiles();
    }

    /**
     * Gets the current test settings.
     *
     * @return TestSettings
     */
    public SessionService.TestSettings getTestSettings() {
        return sessionService.getTestSettings();
    }

    /**
     * Updates test settings.
     *
     * @param settings The new settings
     */
    public void setTestSettings(SessionService.TestSettings settings) {
        sessionService.setTestSettings(settings);
    }

    // ═══════════════════════════════════════════════════════════════════
    // VISUALIZATION
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Checks if visualization is supported for a machine type.
     *
     * @param type The machine type
     * @return true if visualization is supported
     */
    public boolean supportsVisualization(MachineType type) {
        return visualizationService.supportsVisualization(type);
    }

    /**
     * Generates DOT code for an automaton.
     *
     * @param automaton The automaton
     * @param inputText The definition text
     * @return DOT code string
     */
    public String generateDotCode(Automaton automaton, String inputText) {
        return visualizationService.generateDotCode(automaton, inputText);
    }

    // ═══════════════════════════════════════════════════════════════════
    // SERVICE ACCESSORS (for advanced usage)
    // ═══════════════════════════════════════════════════════════════════

    public AutomatonService getAutomatonService() { return automatonService; }
    public FileService getFileService() { return fileService; }
    public TestService getTestService() { return testService; }
    public VisualizationService getVisualizationService() { return visualizationService; }
    public SessionService getSessionService() { return sessionService; }
}
