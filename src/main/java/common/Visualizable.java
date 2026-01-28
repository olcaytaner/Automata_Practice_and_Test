package common;

/**
 * Interface for formal languages that can be visualized as graphs.
 * Implementations generate GraphViz DOT code for rendering.
 *
 * @version 2.0
 */
public interface Visualizable {

    /**
     * Generates DOT code for GraphViz visualization.
     * This is domain logic that belongs in the model layer.
     *
     * @param inputText The input text to highlight in the visualization (optional)
     * @return DOT code string for graph rendering
     */
    String toDotCode(String inputText);
}
