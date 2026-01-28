package service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import common.Automaton;
import common.MachineType;
import common.ParseResult;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.GraphvizJdkEngine;

/**
 * Service for automaton visualization.
 * Generates DOT code and renders to SVG using GraphViz.
 * Extracts visualization logic from UI layer for MVC separation.
 */
public class VisualizationService {

    /**
     * Result of a visualization operation.
     */
    public static class VisualizationResult {
        private final boolean success;
        private final String svgContent;
        private final String dotCode;
        private final String errorMessage;

        private VisualizationResult(boolean success, String svgContent, String dotCode, String errorMessage) {
            this.success = success;
            this.svgContent = svgContent;
            this.dotCode = dotCode;
            this.errorMessage = errorMessage;
        }

        public static VisualizationResult success(String svgContent, String dotCode) {
            return new VisualizationResult(true, svgContent, dotCode, null);
        }

        public static VisualizationResult failure(String errorMessage) {
            return new VisualizationResult(false, null, null, errorMessage);
        }

        public static VisualizationResult failure(String errorMessage, String dotCode) {
            return new VisualizationResult(false, null, dotCode, errorMessage);
        }

        public boolean isSuccess() { return success; }
        public String getSvgContent() { return svgContent; }
        public String getDotCode() { return dotCode; }
        public String getErrorMessage() { return errorMessage; }
    }

    /**
     * Generates a visualization for the automaton.
     *
     * @param automaton The automaton to visualize
     * @param inputText The input text defining the automaton
     * @return VisualizationResult containing SVG content or error
     */
    public VisualizationResult generateVisualization(Automaton automaton, String inputText) {
        if (automaton == null) {
            return VisualizationResult.failure("Automaton cannot be null");
        }

        // Check if this type supports visualization
        if (!supportsVisualization(automaton.getMachineType())) {
            return VisualizationResult.failure(
                "Visualization is not supported for " + automaton.getMachineType().name());
        }

        // Parse first
        ParseResult parseResult = automaton.parse(inputText);
        if (!parseResult.isSuccess()) {
            return VisualizationResult.failure("Parsing failed - check warnings for details");
        }

        // Get the parsed automaton
        Automaton parsedAutomaton = parseResult.getAutomaton();
        if (parsedAutomaton == null) {
            parsedAutomaton = automaton;
        }

        // Generate DOT code
        String dotCode;
        try {
            dotCode = parsedAutomaton.toDotCode(inputText);
        } catch (IllegalStateException e) {
            return VisualizationResult.failure("Visualization not available: " + e.getMessage());
        }

        // Check for empty DOT code
        if (dotCode == null || dotCode.trim().isEmpty()) {
            return VisualizationResult.failure("The automaton structure cannot be visualized");
        }

        // Render to SVG
        try {
            String svgContent = renderToSvg(dotCode);
            return VisualizationResult.success(svgContent, dotCode);
        } catch (Exception e) {
            return VisualizationResult.failure("Graph rendering failed: " + e.getMessage(), dotCode);
        }
    }

    /**
     * Checks if visualization is supported for the given machine type.
     *
     * @param type The machine type
     * @return true if visualization is supported
     */
    public boolean supportsVisualization(MachineType type) {
        if (type == null) {
            return false;
        }

        // RegularExpression doesn't support visualization
        return type != MachineType.REGEX;
    }

    /**
     * Generates DOT code for an automaton without rendering.
     *
     * @param automaton The automaton
     * @param inputText The input text defining the automaton
     * @return The DOT code string, or null if generation fails
     */
    public String generateDotCode(Automaton automaton, String inputText) {
        if (automaton == null) {
            return null;
        }

        // Parse first
        ParseResult parseResult = automaton.parse(inputText);
        if (!parseResult.isSuccess()) {
            return null;
        }

        Automaton parsedAutomaton = parseResult.getAutomaton();
        if (parsedAutomaton == null) {
            parsedAutomaton = automaton;
        }

        try {
            return parsedAutomaton.toDotCode(inputText);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Generates DOT code with a highlighted input for step-by-step visualization.
     *
     * @param automaton The automaton
     * @param inputText The input text defining the automaton
     * @param highlightInput The input string to highlight in the visualization
     * @return The DOT code with highlighting, or null if generation fails
     */
    public String generateDotCodeWithHighlight(Automaton automaton, String inputText, String highlightInput) {
        if (automaton == null) {
            return null;
        }

        // Parse first
        ParseResult parseResult = automaton.parse(inputText);
        if (!parseResult.isSuccess()) {
            return null;
        }

        Automaton parsedAutomaton = parseResult.getAutomaton();
        if (parsedAutomaton == null) {
            parsedAutomaton = automaton;
        }

        try {
            // Use the highlight input if provided, otherwise use the regular input
            String codeInput = (highlightInput != null) ? highlightInput : inputText;
            return parsedAutomaton.toDotCode(codeInput);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Renders DOT code to SVG format.
     *
     * @param dotCode The DOT code to render
     * @return SVG content as a string
     * @throws Exception if rendering fails
     */
    public String renderToSvg(String dotCode) throws Exception {
        if (dotCode == null || dotCode.trim().isEmpty()) {
            throw new IllegalArgumentException("DOT code cannot be null or empty");
        }

        // Initialize GraphViz engine
        try {
            GraphvizJdkEngine jdkEngine = new GraphvizJdkEngine();
            Graphviz.useEngine(jdkEngine);
        } catch (Exception e) {
            // Engine may already be initialized, continue
        }

        // Render to SVG
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Graphviz.fromString(dotCode)
                .render(Format.SVG)
                .toOutputStream(baos);
        baos.close();

        String svgText = new String(baos.toByteArray(), StandardCharsets.UTF_8);

        // Fix Batik compatibility issue - Batik cannot parse "transparent"
        svgText = svgText.replaceAll("stroke=\"transparent\"", "stroke=\"none\"");

        return svgText;
    }

    /**
     * Validates that the automaton can be visualized.
     *
     * @param automaton The automaton to validate
     * @param inputText The input text
     * @return null if valid, error message if invalid
     */
    public String validateVisualization(Automaton automaton, String inputText) {
        if (automaton == null) {
            return "Automaton cannot be null";
        }

        if (!supportsVisualization(automaton.getMachineType())) {
            return "Visualization not supported for " + automaton.getMachineType().name();
        }

        ParseResult parseResult = automaton.parse(inputText);
        if (!parseResult.isSuccess()) {
            return "Parsing failed - automaton definition is invalid";
        }

        return null;
    }
}
