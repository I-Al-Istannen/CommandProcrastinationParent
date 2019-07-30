package de.ialistannen.commandprocrastination.parsing;

import de.ialistannen.commandprocrastination.util.StringReader;

/**
 * Parses a single structure.
 */
public interface AtomicParser<T> {

  /**
   * Parses a single string.
   *
   * @param input the input
   * @return the parsed result
   * @throws ParseException if an error occurred while parsing
   */
  T parse(StringReader input) throws ParseException;

  /**
   * Returns the name of the parser.
   *
   * @return the name of the parser
   */
  default String getName() {
    return "null";
  }

  /**
   * Creates a named parser.
   *
   * @param name the name
   * @param parser the parser
   * @param <T> the type of the parser
   * @return a named parser
   */
  static <T> AtomicParser<T> named(String name, AtomicParser<T> parser) {
    return new AtomicParser<T>() {
      @Override
      public T parse(StringReader input) throws ParseException {
        return parser.parse(input);
      }

      @Override
      public String getName() {
        return name;
      }
    };
  }
}
