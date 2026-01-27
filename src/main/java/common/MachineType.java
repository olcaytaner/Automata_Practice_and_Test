package common;

/**
 * Enumeration of supported formal language types.
 * This enum represents all the machine/language types that can be
 * created, parsed, and executed in the system.
 *
 * @version 1.0
 */
public enum MachineType {
    DFA,
    NFA,
    PDA,
    TM,
    CFG,
    REGEX
}
