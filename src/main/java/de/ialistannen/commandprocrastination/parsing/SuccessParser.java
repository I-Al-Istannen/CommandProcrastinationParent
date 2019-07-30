package de.ialistannen.commandprocrastination.parsing;

import de.ialistannen.commandprocrastination.util.StringReader;

/**
 * A parser that wraps around another and returns true if the underlying parser is successful.
 *
 * <p><br>This will reset the position if parsing fails!</p>
 * <p><br>Use {@link #wrapping(AtomicParser)} to get an instance.</p>
 */
public class SuccessParser implements AtomicParser<Boolean> {

  private AtomicParser<?> underlying;

  private SuccessParser(AtomicParser<?> underlying) {
    this.underlying = underlying;
  }

  @Override
  public Boolean parse(StringReader input) {
    int start = input.getPosition();
    try {
      underlying.parse(input);
      return true;
    } catch (ParseException e) {
      input.reset(start);
      return false;
    }
  }

  @Override
  public String getName() {
    return underlying.getName();
  }

  /**
   * Creates a head parser that returns true if the given parser completes without error.
   *
   * @param parser the parser to wrap
   * @param <T> the type of the parser
   * @return the head parser
   */
  public static <T> SuccessParser wrapping(AtomicParser<T> parser) {
    return new SuccessParser(parser);
  }

  /**
   * Returns a parser that always succeeds.
   *
   * @return a parser that always succeeds
   */
  public static SuccessParser alwaysTrue() {
    return new SuccessParser(input -> true);
  }
}
