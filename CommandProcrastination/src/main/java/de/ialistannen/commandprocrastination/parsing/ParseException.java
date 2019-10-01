package de.ialistannen.commandprocrastination.parsing;

import de.ialistannen.commandprocrastination.util.StringReader;

/**
 * An exception that occurred during parsing.
 */
public class ParseException extends Exception {

  private static final int CONTEXT_LENGTH = 10;
  private final String detail;

  public ParseException(StringReader reader, String detail) {
    super(getContext(reader, detail));
    this.detail = detail;
  }

  public ParseException(StringReader reader) {
    this(reader, "");
  }

  public ParseException(StringReader reader, Throwable underlying) {
    this(reader, "", underlying);
  }

  public ParseException(StringReader reader, String detail, Throwable underlying) {
    super(getContext(reader, detail), underlying);
    this.detail = detail;
  }

  /**
   * Returns the detail message without any context.
   *
   * @return the message without aby context
   */
  public String getDetail() {
    return detail;
  }

  private static String getContext(StringReader input, String detail) {
    int start = input.getPosition();
    start = Math.max(start - CONTEXT_LENGTH, 0);
    String contextString = input.getUnderlying().substring(start, input.getPosition());

    if (!detail.isBlank()) {
      return detail + " at " + contextString + "<---[HERE]";
    }

    return contextString + "<---[HERE]";
  }
}
