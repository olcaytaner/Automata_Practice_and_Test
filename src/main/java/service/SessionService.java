package service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Service for session management and preferences.
 * Handles recent files, open tabs, and user preferences.
 * Extracts session logic from UI layer for MVC separation.
 */
public class SessionService {

    private static final String PREFERENCES_FILE = ".cs410_preferences.properties";
    private static final String LAST_DIRECTORY_KEY = "lastUsedDirectory";
    private static final String RECENT_FILES_KEY = "recentFiles";
    private static final String LAST_OPENED_FILES_KEY = "lastOpenedFiles";
    private static final String FILE_SEPARATOR = "|";
    private static final int MAX_RECENT_FILES = 10;

    // Test settings keys
    private static final String TEST_MIN_POINTS = "test.minPoints";
    private static final String TEST_MAX_POINTS = "test.maxPoints";
    private static final String TEST_TIMEOUT = "test.timeoutSeconds";
    private static final String TEST_MAX_RULES = "test.maxRules";
    private static final String TEST_MAX_TRANSITIONS = "test.maxTransitions";
    private static final String TEST_MAX_REGEX_LENGTH = "test.maxRegexLength";

    // Default test values
    public static final int DEFAULT_MIN_POINTS = 4;
    public static final int DEFAULT_MAX_POINTS = 15;
    public static final int DEFAULT_TIMEOUT_SECONDS = 20;

    private Properties properties;
    private File preferencesFile;

    public SessionService() {
        this.properties = new Properties();
        String userHome = System.getProperty("user.home");
        this.preferencesFile = new File(userHome, PREFERENCES_FILE);
        loadPreferences();
    }

    /**
     * Constructor for testing with custom preferences file.
     */
    public SessionService(File preferencesFile) {
        this.properties = new Properties();
        this.preferencesFile = preferencesFile;
        loadPreferences();
    }

    // ═══════════════════════════════════════════════════════════════════
    // PREFERENCES PERSISTENCE
    // ═══════════════════════════════════════════════════════════════════

    private void loadPreferences() {
        if (preferencesFile != null && preferencesFile.exists()) {
            try (FileInputStream fis = new FileInputStream(preferencesFile)) {
                properties.load(fis);
            } catch (IOException e) {
                System.err.println("Error loading preferences: " + e.getMessage());
            }
        }
    }

    /**
     * Saves all preferences to disk.
     */
    public void savePreferences() {
        if (preferencesFile == null) {
            return;
        }
        try (FileOutputStream fos = new FileOutputStream(preferencesFile)) {
            properties.store(fos, "CS.410 Graph System Preferences");
        } catch (IOException e) {
            System.err.println("Error saving preferences: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // DIRECTORY MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Gets the last used directory path.
     *
     * @return The last directory path, or null if not set
     */
    public String getLastDirectory() {
        return properties.getProperty(LAST_DIRECTORY_KEY, null);
    }

    /**
     * Sets the last used directory path.
     *
     * @param directoryPath The directory path
     */
    public void setLastDirectory(String directoryPath) {
        if (directoryPath != null) {
            properties.setProperty(LAST_DIRECTORY_KEY, directoryPath);
            savePreferences();
        }
    }

    /**
     * Gets the last used directory as a File object.
     *
     * @return The directory as File, or null if invalid
     */
    public File getLastDirectoryAsFile() {
        String path = getLastDirectory();
        if (path != null) {
            File dir = new File(path);
            if (dir.exists() && dir.isDirectory()) {
                return dir;
            }
        }
        return null;
    }

    // ═══════════════════════════════════════════════════════════════════
    // RECENT FILES MANAGEMENT
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Gets the list of recent files.
     *
     * @return List of recent file paths
     */
    public List<String> getRecentFiles() {
        String recentFilesStr = properties.getProperty(RECENT_FILES_KEY, "");
        if (recentFilesStr.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(recentFilesStr.split(Pattern.quote(FILE_SEPARATOR))));
    }

    /**
     * Adds a file to the recent files list.
     *
     * @param file The file to add
     */
    public void addRecentFile(File file) {
        if (file == null) {
            return;
        }
        addRecentFile(file.getAbsolutePath());
    }

    /**
     * Adds a file path to the recent files list.
     *
     * @param filePath The file path to add
     */
    public void addRecentFile(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return;
        }

        List<String> recentFiles = getRecentFiles();

        // Remove if already exists to move to top
        recentFiles.remove(filePath);

        // Add to beginning
        recentFiles.add(0, filePath);

        // Limit to MAX_RECENT_FILES
        if (recentFiles.size() > MAX_RECENT_FILES) {
            recentFiles = new ArrayList<>(recentFiles.subList(0, MAX_RECENT_FILES));
        }

        // Save back to properties
        String recentFilesStr = String.join(FILE_SEPARATOR, recentFiles);
        properties.setProperty(RECENT_FILES_KEY, recentFilesStr);
        savePreferences();
    }

    /**
     * Removes a file from the recent files list.
     *
     * @param filePath The file path to remove
     */
    public void removeRecentFile(String filePath) {
        List<String> recentFiles = getRecentFiles();
        if (recentFiles.remove(filePath)) {
            String recentFilesStr = String.join(FILE_SEPARATOR, recentFiles);
            properties.setProperty(RECENT_FILES_KEY, recentFilesStr);
            savePreferences();
        }
    }

    /**
     * Clears the recent files list.
     */
    public void clearRecentFiles() {
        properties.remove(RECENT_FILES_KEY);
        savePreferences();
    }

    // ═══════════════════════════════════════════════════════════════════
    // OPEN FILES MANAGEMENT (session restore)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Gets the list of files that were open when the app last closed.
     *
     * @return List of previously open file paths
     */
    public List<String> getLastOpenedFiles() {
        String lastOpenedStr = properties.getProperty(LAST_OPENED_FILES_KEY, "");
        if (lastOpenedStr.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(lastOpenedStr.split(Pattern.quote(FILE_SEPARATOR))));
    }

    /**
     * Saves the list of currently open files for session restore.
     *
     * @param openFiles List of currently open files
     */
    public void saveOpenFiles(List<File> openFiles) {
        if (openFiles == null || openFiles.isEmpty()) {
            properties.remove(LAST_OPENED_FILES_KEY);
        } else {
            List<String> validPaths = new ArrayList<>();
            for (File file : openFiles) {
                if (file != null && file.exists()) {
                    validPaths.add(file.getAbsolutePath());
                }
            }

            if (!validPaths.isEmpty()) {
                String lastOpenedStr = String.join(FILE_SEPARATOR, validPaths);
                properties.setProperty(LAST_OPENED_FILES_KEY, lastOpenedStr);
            } else {
                properties.remove(LAST_OPENED_FILES_KEY);
            }
        }
        savePreferences();
    }

    /**
     * Saves open file paths for session restore.
     *
     * @param filePaths List of file paths
     */
    public void setLastOpenedFiles(List<String> filePaths) {
        if (filePaths == null || filePaths.isEmpty()) {
            properties.remove(LAST_OPENED_FILES_KEY);
        } else {
            List<String> validPaths = new ArrayList<>();
            for (String path : filePaths) {
                if (path != null && !path.trim().isEmpty()) {
                    validPaths.add(path);
                }
            }

            if (!validPaths.isEmpty()) {
                String lastOpenedStr = String.join(FILE_SEPARATOR, validPaths);
                properties.setProperty(LAST_OPENED_FILES_KEY, lastOpenedStr);
            } else {
                properties.remove(LAST_OPENED_FILES_KEY);
            }
        }
        savePreferences();
    }

    // ═══════════════════════════════════════════════════════════════════
    // TEST SETTINGS
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Test settings data class.
     */
    public static class TestSettings {
        private final int minPoints;
        private final int maxPoints;
        private final int timeoutSeconds;
        private final Integer maxRules;
        private final Integer maxTransitions;
        private final Integer maxRegexLength;

        public TestSettings(int minPoints, int maxPoints, int timeoutSeconds,
                           Integer maxRules, Integer maxTransitions, Integer maxRegexLength) {
            this.minPoints = minPoints;
            this.maxPoints = maxPoints;
            this.timeoutSeconds = timeoutSeconds;
            this.maxRules = maxRules;
            this.maxTransitions = maxTransitions;
            this.maxRegexLength = maxRegexLength;
        }

        public int getMinPoints() { return minPoints; }
        public int getMaxPoints() { return maxPoints; }
        public int getTimeoutSeconds() { return timeoutSeconds; }
        public long getTimeoutMs() { return timeoutSeconds * 1000L; }
        public Integer getMaxRules() { return maxRules; }
        public Integer getMaxTransitions() { return maxTransitions; }
        public Integer getMaxRegexLength() { return maxRegexLength; }
    }

    /**
     * Gets the current test settings.
     *
     * @return TestSettings with current values
     */
    public TestSettings getTestSettings() {
        return new TestSettings(
            getIntProperty(TEST_MIN_POINTS, DEFAULT_MIN_POINTS),
            getIntProperty(TEST_MAX_POINTS, DEFAULT_MAX_POINTS),
            getIntProperty(TEST_TIMEOUT, DEFAULT_TIMEOUT_SECONDS),
            getOptionalIntProperty(TEST_MAX_RULES),
            getOptionalIntProperty(TEST_MAX_TRANSITIONS),
            getOptionalIntProperty(TEST_MAX_REGEX_LENGTH)
        );
    }

    /**
     * Sets all test settings at once.
     *
     * @param settings The settings to apply
     */
    public void setTestSettings(TestSettings settings) {
        setIntProperty(TEST_MIN_POINTS, settings.getMinPoints());
        setIntProperty(TEST_MAX_POINTS, settings.getMaxPoints());
        setIntProperty(TEST_TIMEOUT, settings.getTimeoutSeconds());
        setOptionalIntProperty(TEST_MAX_RULES, settings.getMaxRules());
        setOptionalIntProperty(TEST_MAX_TRANSITIONS, settings.getMaxTransitions());
        setOptionalIntProperty(TEST_MAX_REGEX_LENGTH, settings.getMaxRegexLength());
    }

    // Individual test setting getters
    public int getTestMinPoints() {
        return getIntProperty(TEST_MIN_POINTS, DEFAULT_MIN_POINTS);
    }

    public int getTestMaxPoints() {
        return getIntProperty(TEST_MAX_POINTS, DEFAULT_MAX_POINTS);
    }

    public int getTestTimeout() {
        return getIntProperty(TEST_TIMEOUT, DEFAULT_TIMEOUT_SECONDS);
    }

    public Integer getTestMaxRules() {
        return getOptionalIntProperty(TEST_MAX_RULES);
    }

    public Integer getTestMaxTransitions() {
        return getOptionalIntProperty(TEST_MAX_TRANSITIONS);
    }

    public Integer getTestMaxRegexLength() {
        return getOptionalIntProperty(TEST_MAX_REGEX_LENGTH);
    }

    // Individual test setting setters
    public void setTestMinPoints(int value) {
        setIntProperty(TEST_MIN_POINTS, value);
    }

    public void setTestMaxPoints(int value) {
        setIntProperty(TEST_MAX_POINTS, value);
    }

    public void setTestTimeout(int value) {
        setIntProperty(TEST_TIMEOUT, value);
    }

    public void setTestMaxRules(Integer value) {
        setOptionalIntProperty(TEST_MAX_RULES, value);
    }

    public void setTestMaxTransitions(Integer value) {
        setOptionalIntProperty(TEST_MAX_TRANSITIONS, value);
    }

    public void setTestMaxRegexLength(Integer value) {
        setOptionalIntProperty(TEST_MAX_REGEX_LENGTH, value);
    }

    /**
     * Resets test settings to defaults.
     */
    public void resetTestSettings() {
        setTestMinPoints(DEFAULT_MIN_POINTS);
        setTestMaxPoints(DEFAULT_MAX_POINTS);
        setTestTimeout(DEFAULT_TIMEOUT_SECONDS);
        setTestMaxRules(null);
        setTestMaxTransitions(null);
        setTestMaxRegexLength(null);
    }

    // ═══════════════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════════════════════

    private int getIntProperty(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                // Fall through to default
            }
        }
        return defaultValue;
    }

    private void setIntProperty(String key, int value) {
        properties.setProperty(key, String.valueOf(value));
        savePreferences();
    }

    private Integer getOptionalIntProperty(String key) {
        String value = properties.getProperty(key);
        if (value != null && !value.isEmpty()) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                // Fall through to null
            }
        }
        return null;
    }

    private void setOptionalIntProperty(String key, Integer value) {
        if (value == null) {
            properties.remove(key);
        } else {
            properties.setProperty(key, String.valueOf(value));
        }
        savePreferences();
    }
}
