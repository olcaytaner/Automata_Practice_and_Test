package service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import common.Automaton;
import common.MachineType;

/**
 * Service for file I/O operations and type detection.
 * Extracts file-related business logic from UI layer for MVC separation.
 */
public class FileService {

    /**
     * Saves content to a file.
     *
     * @param file The file to save to
     * @param content The content to write
     * @return true if save was successful
     * @throws IOException if an I/O error occurs
     */
    public boolean saveToFile(File file, String content) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }
        if (content == null) {
            content = "";
        }

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            writer.write(content);
            return true;
        }
    }

    /**
     * Loads content from a file.
     *
     * @param file The file to load from
     * @return The file content as a string
     * @throws IOException if an I/O error occurs or file doesn't exist
     */
    public String loadFromFile(File file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }
        if (!file.exists()) {
            throw new IOException("File does not exist: " + file.getAbsolutePath());
        }

        return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
    }

    /**
     * Determines the MachineType based on file extension.
     *
     * @param file The file to analyze
     * @return The corresponding MachineType, or null if unknown
     */
    public MachineType getMachineTypeForFile(File file) {
        if (file == null) {
            return null;
        }

        String extension = getFileExtension(file);
        return getMachineTypeForExtension(extension);
    }

    /**
     * Gets the file extension for an automaton type.
     *
     * @param automaton The automaton
     * @return The file extension (including the dot)
     */
    public String getExtensionForAutomaton(Automaton automaton) {
        if (automaton == null) {
            throw new IllegalArgumentException("Automaton cannot be null");
        }
        return automaton.getFileExtension();
    }

    /**
     * Gets the file extension from a file name.
     *
     * @param file The file
     * @return The extension (including the dot), or empty string if none
     */
    public String getFileExtension(File file) {
        if (file == null) {
            return "";
        }
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        return lastDot > 0 ? name.substring(lastDot).toLowerCase() : "";
    }

    /**
     * Gets the MachineType for a file extension.
     *
     * @param extension The file extension (with or without dot)
     * @return The corresponding MachineType, or null if unknown
     */
    public MachineType getMachineTypeForExtension(String extension) {
        if (extension == null || extension.isEmpty()) {
            return null;
        }

        // Normalize extension to include dot
        String normalizedExt = extension.startsWith(".") ? extension.toLowerCase() : "." + extension.toLowerCase();

        for (MachineType type : MachineType.values()) {
            if (type.getExtension().equals(normalizedExt)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Checks if a file has a supported automaton extension.
     *
     * @param file The file to check
     * @return true if the file has a supported extension
     */
    public boolean isSupportedFile(File file) {
        return getMachineTypeForFile(file) != null;
    }

    /**
     * Ensures a file has the correct extension for the automaton type.
     * If the file doesn't have the correct extension, a new file with the correct extension is returned.
     *
     * @param file The original file
     * @param automaton The automaton to get the extension from
     * @return A file with the correct extension
     */
    public File ensureCorrectExtension(File file, Automaton automaton) {
        if (file == null || automaton == null) {
            throw new IllegalArgumentException("File and automaton cannot be null");
        }

        String requiredExt = getExtensionForAutomaton(automaton);
        String currentExt = getFileExtension(file);

        if (!currentExt.equalsIgnoreCase(requiredExt)) {
            return new File(file.getAbsolutePath() + requiredExt);
        }
        return file;
    }

    /**
     * Gets the base name of a file (without extension).
     *
     * @param file The file
     * @return The base name without extension
     */
    public String getBaseName(File file) {
        if (file == null) {
            return "";
        }
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        return lastDot > 0 ? name.substring(0, lastDot) : name;
    }

    /**
     * Creates a file with a different extension in the same directory.
     *
     * @param originalFile The original file
     * @param newExtension The new extension (with or without dot)
     * @return A new file with the same base name but different extension
     */
    public File withExtension(File originalFile, String newExtension) {
        if (originalFile == null) {
            throw new IllegalArgumentException("Original file cannot be null");
        }
        if (newExtension == null) {
            newExtension = "";
        }

        String normalizedExt = newExtension.startsWith(".") ? newExtension : "." + newExtension;
        String baseName = getBaseName(originalFile);
        File parent = originalFile.getParentFile();

        return new File(parent, baseName + normalizedExt);
    }

    /**
     * Checks if a file exists.
     *
     * @param file The file to check
     * @return true if the file exists
     */
    public boolean exists(File file) {
        return file != null && file.exists();
    }

    /**
     * Gets a description of the file type.
     *
     * @param file The file
     * @return A human-readable description of the file type
     */
    public String getFileTypeDescription(File file) {
        MachineType type = getMachineTypeForFile(file);
        if (type == null) {
            return "Unknown file type";
        }

        switch (type) {
            case DFA:
                return "Deterministic Finite Automaton";
            case NFA:
                return "Nondeterministic Finite Automaton";
            case PDA:
                return "Push-Down Automaton";
            case TM:
                return "Turing Machine";
            case CFG:
                return "Context-Free Grammar";
            case REGEX:
                return "Regular Expression";
            default:
                return "Unknown automaton type";
        }
    }

    /**
     * Gets all supported file extensions as a comma-separated string.
     *
     * @return Comma-separated list of extensions
     */
    public String getSupportedExtensions() {
        StringBuilder sb = new StringBuilder();
        for (MachineType type : MachineType.values()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(type.getExtension());
        }
        return sb.toString();
    }
}
