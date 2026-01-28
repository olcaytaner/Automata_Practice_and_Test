package service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import DeterministicFiniteAutomaton.DFA;
import NondeterministicFiniteAutomaton.NFA;
import PushDownAutomaton.PDA;
import common.MachineType;

/**
 * Unit tests for FileService.
 */
@DisplayName("FileService Tests")
class FileServiceTest {

    private FileService service;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        service = new FileService();
    }

    // ═══════════════════════════════════════════════════════════════════
    // saveToFile / loadFromFile Tests
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("saveToFile and loadFromFile round-trip works")
    void saveAndLoad_roundTrip() throws IOException {
        File file = tempDir.resolve("test.dfa").toFile();
        String content = "states: q0, q1\nalphabet: 0, 1";

        boolean saved = service.saveToFile(file, content);
        assertTrue(saved);
        assertTrue(file.exists());

        String loaded = service.loadFromFile(file);
        assertEquals(content, loaded);
    }

    @Test
    @DisplayName("saveToFile handles empty content")
    void saveToFile_emptyContent() throws IOException {
        File file = tempDir.resolve("empty.dfa").toFile();

        boolean saved = service.saveToFile(file, "");
        assertTrue(saved);

        String loaded = service.loadFromFile(file);
        assertEquals("", loaded);
    }

    @Test
    @DisplayName("saveToFile handles null content as empty")
    void saveToFile_nullContent() throws IOException {
        File file = tempDir.resolve("null.dfa").toFile();

        boolean saved = service.saveToFile(file, null);
        assertTrue(saved);

        String loaded = service.loadFromFile(file);
        assertEquals("", loaded);
    }

    @Test
    @DisplayName("saveToFile throws for null file")
    void saveToFile_nullFile() {
        assertThrows(IllegalArgumentException.class, () -> service.saveToFile(null, "content"));
    }

    @Test
    @DisplayName("loadFromFile throws for null file")
    void loadFromFile_nullFile() {
        assertThrows(IllegalArgumentException.class, () -> service.loadFromFile(null));
    }

    @Test
    @DisplayName("loadFromFile throws for non-existent file")
    void loadFromFile_nonExistentFile() {
        File nonExistent = tempDir.resolve("nonexistent.dfa").toFile();
        assertThrows(IOException.class, () -> service.loadFromFile(nonExistent));
    }

    // ═══════════════════════════════════════════════════════════════════
    // getMachineTypeForFile Tests
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getMachineTypeForFile returns correct type for .dfa")
    void getMachineTypeForFile_dfa() {
        File file = new File("test.dfa");
        assertEquals(MachineType.DFA, service.getMachineTypeForFile(file));
    }

    @Test
    @DisplayName("getMachineTypeForFile returns correct type for .nfa")
    void getMachineTypeForFile_nfa() {
        File file = new File("test.nfa");
        assertEquals(MachineType.NFA, service.getMachineTypeForFile(file));
    }

    @Test
    @DisplayName("getMachineTypeForFile returns correct type for .pda")
    void getMachineTypeForFile_pda() {
        File file = new File("test.pda");
        assertEquals(MachineType.PDA, service.getMachineTypeForFile(file));
    }

    @Test
    @DisplayName("getMachineTypeForFile returns correct type for .tm")
    void getMachineTypeForFile_tm() {
        File file = new File("test.tm");
        assertEquals(MachineType.TM, service.getMachineTypeForFile(file));
    }

    @Test
    @DisplayName("getMachineTypeForFile returns correct type for .cfg")
    void getMachineTypeForFile_cfg() {
        File file = new File("test.cfg");
        assertEquals(MachineType.CFG, service.getMachineTypeForFile(file));
    }

    @Test
    @DisplayName("getMachineTypeForFile returns correct type for .rex")
    void getMachineTypeForFile_rex() {
        File file = new File("test.rex");
        assertEquals(MachineType.REGEX, service.getMachineTypeForFile(file));
    }

    @Test
    @DisplayName("getMachineTypeForFile is case insensitive")
    void getMachineTypeForFile_caseInsensitive() {
        File file = new File("test.DFA");
        assertEquals(MachineType.DFA, service.getMachineTypeForFile(file));
    }

    @Test
    @DisplayName("getMachineTypeForFile returns null for unknown extension")
    void getMachineTypeForFile_unknownExtension() {
        File file = new File("test.txt");
        assertNull(service.getMachineTypeForFile(file));
    }

    @Test
    @DisplayName("getMachineTypeForFile returns null for null file")
    void getMachineTypeForFile_nullFile() {
        assertNull(service.getMachineTypeForFile(null));
    }

    // ═══════════════════════════════════════════════════════════════════
    // getExtensionForAutomaton Tests
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getExtensionForAutomaton returns .dfa for DFA")
    void getExtensionForAutomaton_dfa() {
        DFA dfa = new DFA();
        assertEquals(".dfa", service.getExtensionForAutomaton(dfa));
    }

    @Test
    @DisplayName("getExtensionForAutomaton returns .nfa for NFA")
    void getExtensionForAutomaton_nfa() {
        NFA nfa = new NFA();
        assertEquals(".nfa", service.getExtensionForAutomaton(nfa));
    }

    @Test
    @DisplayName("getExtensionForAutomaton returns .pda for PDA")
    void getExtensionForAutomaton_pda() {
        PDA pda = new PDA();
        assertEquals(".pda", service.getExtensionForAutomaton(pda));
    }

    @Test
    @DisplayName("getExtensionForAutomaton throws for null")
    void getExtensionForAutomaton_null() {
        assertThrows(IllegalArgumentException.class, () -> service.getExtensionForAutomaton(null));
    }

    // ═══════════════════════════════════════════════════════════════════
    // getFileExtension Tests
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getFileExtension returns extension with dot")
    void getFileExtension_withDot() {
        File file = new File("test.dfa");
        assertEquals(".dfa", service.getFileExtension(file));
    }

    @Test
    @DisplayName("getFileExtension returns empty for no extension")
    void getFileExtension_noExtension() {
        File file = new File("testfile");
        assertEquals("", service.getFileExtension(file));
    }

    @Test
    @DisplayName("getFileExtension returns empty for null")
    void getFileExtension_null() {
        assertEquals("", service.getFileExtension(null));
    }

    // ═══════════════════════════════════════════════════════════════════
    // isSupportedFile Tests
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("isSupportedFile returns true for supported extensions")
    void isSupportedFile_supportedExtensions() {
        assertTrue(service.isSupportedFile(new File("test.dfa")));
        assertTrue(service.isSupportedFile(new File("test.nfa")));
        assertTrue(service.isSupportedFile(new File("test.pda")));
        assertTrue(service.isSupportedFile(new File("test.tm")));
        assertTrue(service.isSupportedFile(new File("test.cfg")));
        assertTrue(service.isSupportedFile(new File("test.rex")));
    }

    @Test
    @DisplayName("isSupportedFile returns false for unsupported extensions")
    void isSupportedFile_unsupportedExtensions() {
        assertFalse(service.isSupportedFile(new File("test.txt")));
        assertFalse(service.isSupportedFile(new File("test.java")));
        assertFalse(service.isSupportedFile(new File("test")));
    }

    // ═══════════════════════════════════════════════════════════════════
    // ensureCorrectExtension Tests
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("ensureCorrectExtension adds extension if missing")
    void ensureCorrectExtension_addsIfMissing() {
        File file = new File("/path/to/file.txt");
        DFA dfa = new DFA();

        File result = service.ensureCorrectExtension(file, dfa);

        assertEquals("/path/to/file.txt.dfa", result.getPath());
    }

    @Test
    @DisplayName("ensureCorrectExtension keeps file if extension matches")
    void ensureCorrectExtension_keepsIfMatches() {
        File file = new File("/path/to/file.dfa");
        DFA dfa = new DFA();

        File result = service.ensureCorrectExtension(file, dfa);

        assertEquals(file, result);
    }

    // ═══════════════════════════════════════════════════════════════════
    // getBaseName Tests
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getBaseName returns name without extension")
    void getBaseName_withExtension() {
        File file = new File("myfile.dfa");
        assertEquals("myfile", service.getBaseName(file));
    }

    @Test
    @DisplayName("getBaseName returns full name if no extension")
    void getBaseName_noExtension() {
        File file = new File("myfile");
        assertEquals("myfile", service.getBaseName(file));
    }

    @Test
    @DisplayName("getBaseName returns empty for null")
    void getBaseName_null() {
        assertEquals("", service.getBaseName(null));
    }

    // ═══════════════════════════════════════════════════════════════════
    // withExtension Tests
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("withExtension creates new file with different extension")
    void withExtension_changesExtension() {
        File original = new File("/path/to/file.dfa");
        File result = service.withExtension(original, ".test");

        assertEquals("file.test", result.getName());
        assertEquals("/path/to", result.getParent());
    }

    @Test
    @DisplayName("withExtension handles extension without dot")
    void withExtension_withoutDot() {
        File original = new File("/path/to/file.dfa");
        File result = service.withExtension(original, "test");

        assertEquals("file.test", result.getName());
    }

    // ═══════════════════════════════════════════════════════════════════
    // getFileTypeDescription Tests
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getFileTypeDescription returns correct descriptions")
    void getFileTypeDescription_allTypes() {
        assertEquals("Deterministic Finite Automaton",
                    service.getFileTypeDescription(new File("test.dfa")));
        assertEquals("Nondeterministic Finite Automaton",
                    service.getFileTypeDescription(new File("test.nfa")));
        assertEquals("Push-Down Automaton",
                    service.getFileTypeDescription(new File("test.pda")));
        assertEquals("Turing Machine",
                    service.getFileTypeDescription(new File("test.tm")));
        assertEquals("Context-Free Grammar",
                    service.getFileTypeDescription(new File("test.cfg")));
        assertEquals("Regular Expression",
                    service.getFileTypeDescription(new File("test.rex")));
    }

    @Test
    @DisplayName("getFileTypeDescription returns unknown for unsupported files")
    void getFileTypeDescription_unknown() {
        assertEquals("Unknown file type", service.getFileTypeDescription(new File("test.txt")));
    }

    // ═══════════════════════════════════════════════════════════════════
    // getSupportedExtensions Tests
    // ═══════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("getSupportedExtensions contains all extensions")
    void getSupportedExtensions_containsAll() {
        String extensions = service.getSupportedExtensions();

        assertTrue(extensions.contains(".dfa"));
        assertTrue(extensions.contains(".nfa"));
        assertTrue(extensions.contains(".pda"));
        assertTrue(extensions.contains(".tm"));
        assertTrue(extensions.contains(".cfg"));
        assertTrue(extensions.contains(".rex"));
    }
}
