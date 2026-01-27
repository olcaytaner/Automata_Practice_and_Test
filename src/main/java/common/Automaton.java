package common;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.GraphvizJdkEngine;

/**
 * Abstract base class for all formal languages (DFA, NFA, PDA, TM, CFG, RegularExpression).
 * Provides common parsing/execution contract and Visualizable for GraphViz rendering support.
 *
 * @version 3.0
 */
public abstract class Automaton implements Visualizable {

  protected MachineType type;

  public MachineType getMachineType() {
    return type;
  }

  protected Automaton(MachineType type) {
    this.type = type;
  }

  public String getFileExtension() {
    switch (type) {
      case DFA:
        return ".dfa";
      case NFA:
        return ".nfa";
      case PDA:
        return ".pda";
      case TM:
        return ".tm";
      case CFG:
        return ".cfg";
      case REGEX:
        return ".rex";
      default:
        return ".txt";
    }
  }

  @Override
  public JLabel toGraphviz(String inputText) {
    // Parse the input first to populate the automaton
    ParseResult parseResult = parse(inputText);

    // Check if parsing was successful
    if (!parseResult.isSuccess()) {
      // Return an error label if parsing failed
      JLabel errorLabel = new JLabel("<html><body style='text-align: center;'>"
          + "<h2>Parsing Failed</h2>"
          + "<p>Check warnings for details</p>"
          + "</body></html>");
      errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
      return errorLabel;
    }

    // Use the successfully parsed automaton for DOT generation
    Automaton parsedAutomaton = parseResult.getAutomaton();
    if (parsedAutomaton == null) {
      parsedAutomaton = this;
    }

    String dotCode;
    try {
      dotCode = parsedAutomaton.toDotCode(inputText);
    } catch (IllegalStateException e) {
      // Handle validation errors from toDotCode (e.g., missing transitions)
      JLabel errorLabel = new JLabel("<html><body style='text-align: center; padding: 20px;'>"
          + "<h2 style='color: #d32f2f;'>Visualization Not Available</h2>"
          + "<p style='margin: 10px 0;'>" + e.getMessage() + "</p>"
          + "<p style='color: #666;'>Check the warnings panel below for details</p>"
          + "</body></html>");
      errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
      return errorLabel;
    }

    // Guard against null DOT code (defensive programming)
    if (dotCode == null || dotCode.trim().isEmpty()) {
      JLabel errorLabel = new JLabel("<html><body style='text-align: center;'>"
          + "<h2>Visualization Not Available</h2>"
          + "<p>The automaton structure cannot be visualized</p>"
          + "<p>Check warnings for validation errors</p>"
          + "</body></html>");
      errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
      return errorLabel;
    }

    try {
      // Log Java and GraalVM version info
      String javaVersion = System.getProperty("java.version");
      String javaVendor = System.getProperty("java.vendor");
      System.out.println("[GraphViz] Java Version: " + javaVersion + " (" + javaVendor + ")");

      try {
        GraphvizJdkEngine jdkEngine = new GraphvizJdkEngine();
        Graphviz.useEngine(jdkEngine);
        System.out.println("[GraphViz] Forced GraalVM JDK engine initialization");
      } catch (Exception engineError) {
        System.err.println("[GraphViz] Failed to force JDK engine: " + engineError.getMessage());
        engineError.printStackTrace();
      }

      // Generate graph image directly in memory - no files created
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      Graphviz.fromString(dotCode)
          .render(Format.SVG)
          .toOutputStream(baos);

      baos.close();

      String svgText = new String(baos.toByteArray(), StandardCharsets.UTF_8);

      // Batik cannot parse "transparent"
      svgText = svgText.replaceAll("stroke=\"transparent\"", "stroke=\"none\"");

      if (svgText.contains("<svg") && svgText.contains("</svg>")) {
        System.out.println("[GraphViz] Graph rendered successfully using GraalVM JDK engine");
      } else {
        System.out.println("[GraphViz] Graph rendering failed");
      }

      return new JLabel(svgText);

    } catch (Exception e) {
      System.err.println("[GraphViz] Error generating graph: " + e.getMessage());
      e.printStackTrace();

      String errorDetails = e.getMessage();
      if (errorDetails == null) {
        errorDetails = e.getClass().getSimpleName();
      }

      String helpMessage;
      if (errorDetails.contains("None of the provided engines could be initialized")) {
        helpMessage = "<p>No JavaScript engine could be initialized.</p>"
            + "<p>Please ensure GraalVM JS dependencies are in your classpath.</p>"
            + "<p>Check that org.graalvm.js:js and org.graalvm.js:js-scriptengine are installed.</p>";
      } else if (errorDetails.contains("native library")) {
        helpMessage = "<p>Native library loading failed.</p>"
            + "<p>This is expected on Apple Silicon (ARM64) - GraalVM fallback should work.</p>";
      } else {
        helpMessage = "<p>An unexpected error occurred during graph generation.</p>"
            + "<p>Check console output for details.</p>";
      }

      // Return a more informative error label
      JLabel errorLabel = new JLabel("<html><body style='text-align: center; padding: 20px;'>"
          + "<h2>Graph Generation Failed</h2>"
          + "<p><b>Error:</b> " + errorDetails + "</p>"
          + helpMessage
          + "</body></html>");
      errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
      return errorLabel;
    }
  }

  public String inputText;

  public void setInputText(String inputText) {
    this.inputText = inputText;
  }

  public String getInputText() {
    return this.inputText;
  }

  /**
   * Parses the input text to build the formal language structure.
   *
   * @param inputText The text representation of the formal language
   * @return ParseResult containing success status, validation messages, and parsed object
   */
  public abstract ParseResult parse(String inputText);

  /**
   * Executes the formal language on the given input string.
   *
   * @param inputText The string to test against the formal language
   * @return ExecutionResult containing acceptance status, messages, and execution trace
   */
  public abstract ExecutionResult execute(String inputText);

  /**
   * Validates the current state of the formal language structure.
   *
   * @return List of validation messages (errors, warnings, info)
   */
  public abstract List<ValidationMessage> validate();

  @Override
  public abstract String toDotCode(String inputText);

  /**
   * Gets a default template for this formal language type.
   *
   * @return Default template string
   */
  public String getDefaultTemplate() {
    return "";
  }
}
