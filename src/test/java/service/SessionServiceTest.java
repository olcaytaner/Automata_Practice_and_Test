package service;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Unit tests for SessionService.
 */
@DisplayName("SessionService Tests")
class SessionServiceTest {

    private SessionService service;
    private File prefsFile;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        prefsFile = tempDir.resolve("test_prefs.properties").toFile();
        service = new SessionService(prefsFile);
    }

    // ═══════════════════════════════════════════════════════════════════
    // Directory Management Tests
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getLastDirectory returns null when not set")
    void getLastDirectory_notSet() {
        assertNull(service.getLastDirectory());
    }

    @Test
    @DisplayName("setLastDirectory and getLastDirectory work correctly")
    void setAndGetLastDirectory() {
        String path = tempDir.toString();
        service.setLastDirectory(path);

        assertEquals(path, service.getLastDirectory());
    }

    @Test
    @DisplayName("getLastDirectoryAsFile returns valid directory")
    void getLastDirectoryAsFile_valid() {
        service.setLastDirectory(tempDir.toString());

        File dir = service.getLastDirectoryAsFile();

        assertNotNull(dir);
        assertTrue(dir.exists());
        assertTrue(dir.isDirectory());
    }

    @Test
    @DisplayName("getLastDirectoryAsFile returns null for invalid path")
    void getLastDirectoryAsFile_invalid() {
        service.setLastDirectory("/nonexistent/path/that/does/not/exist");

        assertNull(service.getLastDirectoryAsFile());
    }

    // ═══════════════════════════════════════════════════════════════════
    // Recent Files Tests
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getRecentFiles returns empty list initially")
    void getRecentFiles_empty() {
        List<String> recent = service.getRecentFiles();

        assertNotNull(recent);
        assertTrue(recent.isEmpty());
    }

    @Test
    @DisplayName("addRecentFile adds file to list")
    void addRecentFile_adds() {
        String path = "/path/to/file.dfa";
        service.addRecentFile(path);

        List<String> recent = service.getRecentFiles();

        assertEquals(1, recent.size());
        assertEquals(path, recent.get(0));
    }

    @Test
    @DisplayName("addRecentFile moves existing file to top")
    void addRecentFile_movesToTop() {
        service.addRecentFile("/path/file1.dfa");
        service.addRecentFile("/path/file2.dfa");
        service.addRecentFile("/path/file1.dfa"); // Add again

        List<String> recent = service.getRecentFiles();

        assertEquals(2, recent.size());
        assertEquals("/path/file1.dfa", recent.get(0));
        assertEquals("/path/file2.dfa", recent.get(1));
    }

    @Test
    @DisplayName("addRecentFile ignores null")
    void addRecentFile_ignoresNull() {
        service.addRecentFile((String) null);

        assertTrue(service.getRecentFiles().isEmpty());
    }

    @Test
    @DisplayName("addRecentFile ignores empty string")
    void addRecentFile_ignoresEmpty() {
        service.addRecentFile("");

        assertTrue(service.getRecentFiles().isEmpty());
    }

    @Test
    @DisplayName("removeRecentFile removes file")
    void removeRecentFile_removes() {
        service.addRecentFile("/path/file1.dfa");
        service.addRecentFile("/path/file2.dfa");

        service.removeRecentFile("/path/file1.dfa");

        List<String> recent = service.getRecentFiles();
        assertEquals(1, recent.size());
        assertEquals("/path/file2.dfa", recent.get(0));
    }

    @Test
    @DisplayName("clearRecentFiles clears all")
    void clearRecentFiles_clearsAll() {
        service.addRecentFile("/path/file1.dfa");
        service.addRecentFile("/path/file2.dfa");

        service.clearRecentFiles();

        assertTrue(service.getRecentFiles().isEmpty());
    }

    // ═══════════════════════════════════════════════════════════════════
    // Last Opened Files Tests
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getLastOpenedFiles returns empty list initially")
    void getLastOpenedFiles_empty() {
        List<String> opened = service.getLastOpenedFiles();

        assertNotNull(opened);
        assertTrue(opened.isEmpty());
    }

    @Test
    @DisplayName("setLastOpenedFiles saves file paths")
    void setLastOpenedFiles_saves() {
        List<String> paths = Arrays.asList("/path/file1.dfa", "/path/file2.nfa");
        service.setLastOpenedFiles(paths);

        List<String> opened = service.getLastOpenedFiles();

        assertEquals(2, opened.size());
        assertTrue(opened.contains("/path/file1.dfa"));
        assertTrue(opened.contains("/path/file2.nfa"));
    }

    @Test
    @DisplayName("setLastOpenedFiles handles null")
    void setLastOpenedFiles_handlesNull() {
        service.setLastOpenedFiles(null);

        assertTrue(service.getLastOpenedFiles().isEmpty());
    }

    @Test
    @DisplayName("saveOpenFiles saves valid files")
    void saveOpenFiles_savesValid() throws Exception {
        File file1 = tempDir.resolve("test1.dfa").toFile();
        File file2 = tempDir.resolve("test2.nfa").toFile();
        file1.createNewFile();
        file2.createNewFile();

        service.saveOpenFiles(Arrays.asList(file1, file2));

        List<String> opened = service.getLastOpenedFiles();
        assertEquals(2, opened.size());
    }

    // ═══════════════════════════════════════════════════════════════════
    // Test Settings Tests
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getTestSettings returns defaults initially")
    void getTestSettings_defaults() {
        SessionService.TestSettings settings = service.getTestSettings();

        assertEquals(SessionService.DEFAULT_MIN_POINTS, settings.getMinPoints());
        assertEquals(SessionService.DEFAULT_MAX_POINTS, settings.getMaxPoints());
        assertEquals(SessionService.DEFAULT_TIMEOUT_SECONDS, settings.getTimeoutSeconds());
        assertNull(settings.getMaxRules());
        assertNull(settings.getMaxTransitions());
        assertNull(settings.getMaxRegexLength());
    }

    @Test
    @DisplayName("setTestSettings persists values")
    void setTestSettings_persists() {
        SessionService.TestSettings settings = new SessionService.TestSettings(
            5, 20, 30, 100, 200, 50
        );

        service.setTestSettings(settings);

        SessionService.TestSettings loaded = service.getTestSettings();
        assertEquals(5, loaded.getMinPoints());
        assertEquals(20, loaded.getMaxPoints());
        assertEquals(30, loaded.getTimeoutSeconds());
        assertEquals(Integer.valueOf(100), loaded.getMaxRules());
        assertEquals(Integer.valueOf(200), loaded.getMaxTransitions());
        assertEquals(Integer.valueOf(50), loaded.getMaxRegexLength());
    }

    @Test
    @DisplayName("getTimeoutMs returns correct milliseconds")
    void getTimeoutMs() {
        SessionService.TestSettings settings = new SessionService.TestSettings(
            4, 15, 20, null, null, null
        );

        assertEquals(20000L, settings.getTimeoutMs());
    }

    @Test
    @DisplayName("individual test setting getters work")
    void individualTestSettingGetters() {
        service.setTestMinPoints(10);
        service.setTestMaxPoints(25);
        service.setTestTimeout(60);
        service.setTestMaxRules(50);
        service.setTestMaxTransitions(100);
        service.setTestMaxRegexLength(200);

        assertEquals(10, service.getTestMinPoints());
        assertEquals(25, service.getTestMaxPoints());
        assertEquals(60, service.getTestTimeout());
        assertEquals(Integer.valueOf(50), service.getTestMaxRules());
        assertEquals(Integer.valueOf(100), service.getTestMaxTransitions());
        assertEquals(Integer.valueOf(200), service.getTestMaxRegexLength());
    }

    @Test
    @DisplayName("resetTestSettings resets to defaults")
    void resetTestSettings_resetsToDefaults() {
        service.setTestMinPoints(10);
        service.setTestMaxRules(50);

        service.resetTestSettings();

        assertEquals(SessionService.DEFAULT_MIN_POINTS, service.getTestMinPoints());
        assertNull(service.getTestMaxRules());
    }

    // ═══════════════════════════════════════════════════════════════════
    // Persistence Tests
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("preferences persist across sessions")
    void preferencesPersist() {
        // First session
        service.setLastDirectory(tempDir.toString());
        service.addRecentFile("/path/file.dfa");
        service.setTestMinPoints(10);

        // Create new service with same file (simulating restart)
        SessionService service2 = new SessionService(prefsFile);

        assertEquals(tempDir.toString(), service2.getLastDirectory());
        assertEquals(1, service2.getRecentFiles().size());
        assertEquals(10, service2.getTestMinPoints());
    }
}
