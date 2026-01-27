package common;

import java.util.Arrays;

/**
 * Enumeration of supported formal language types.
 * This enum represents all the machine/language types that can be
 * created, parsed, and executed in the system.
 *
 * @version 1.1
 */
public enum MachineType {
    DFA(".dfa"),
    NFA(".nfa"),
    PDA(".pda"),
    TM(".tm"),
    CFG(".cfg"),
    REGEX(".rex");

    private final String extension;

    MachineType(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }

    /**
     * Returns all supported file extensions.
     * @return Array of file extensions (e.g., ".dfa", ".nfa", etc.)
     */
    public static String[] getAllExtensions() {
        return Arrays.stream(values())
            .map(MachineType::getExtension)
            .toArray(String[]::new);
    }
}
