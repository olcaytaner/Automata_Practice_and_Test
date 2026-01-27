package common;

import javax.swing.JLabel;

/**
 * Interface for formal languages that can be visualized as graphs.
 * Implementations typically generate GraphViz DOT code for rendering.
 *
 * @version 1.0
 */
public interface Visualizable {

    /**
     * Generates DOT code for GraphViz visualization.
     *
     * @param inputText The input text to highlight in the visualization (optional)
     * @return DOT code string for graph rendering
     */
    String toDotCode(String inputText);

    /**
     * Generates a JLabel containing the rendered graph visualization.
     *
     * @param inputText The input text to highlight in the visualization (optional)
     * @return JLabel with SVG/image content
     */
    JLabel toGraphviz(String inputText);
}
