package common;

import java.util.List;

/**
 * Abstract base class for all formal languages (DFA, NFA, PDA, TM, CFG, RegularExpression).
 * Provides common parsing/execution contract and Visualizable for GraphViz DOT code generation.
 *
 * @version 4.0
 */
public abstract class Automaton implements Visualizable {

  protected MachineType type;
  protected String inputText;

  protected Automaton(MachineType type) {
    this.type = type;
  }

  public MachineType getMachineType() {
    return type;
  }

  public String getFileExtension() {
    return type != null ? type.getExtension() : ".txt";
  }

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

  /**
   * Generates DOT code for GraphViz visualization.
   * This is domain logic that belongs in the model layer.
   *
   * @param inputText The input text to highlight in the visualization (optional)
   * @return DOT code string for graph rendering
   */
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
