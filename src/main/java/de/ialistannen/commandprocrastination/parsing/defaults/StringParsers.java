package de.ialistannen.commandprocrastination.parsing.defaults;

import de.ialistannen.commandprocrastination.parsing.AtomicParser;
import de.ialistannen.commandprocrastination.parsing.ParseException;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Common string parsers.
 */
public class StringParsers {

  private static Set<Character> QUOTE_CHARS = Set.of('"', '\'');

  /**
   * A parser that only matches the given literal. Everything else throws an exception.
   *
   * @param expected the expected literal
   * @return a parser that only matches the given literal
   */
  public static AtomicParser<Void> literal(String expected) {
    int length = expected.length();
    return input -> {
      if (!input.canRead(length) || !expected.equals(input.readChars(length))) {
        throw new ParseException(input, "Expected '" + expected + "'");
      }
      return null;
    };
  }

  /**
   * A parser that reads a single word (i.e. until a space character).
   *
   * @return a parser that reads a single word
   */
  public static AtomicParser<String> word() {
    final Pattern pattern = Pattern.compile("[\\S]*");

    return input -> input.readRegex(pattern);
  }

  /**
   * A parser that reads a single word or a quoted phrase.
   *
   * @return a parser that reads a single word or a quoted phrase
   */
  public static AtomicParser<String> phrase() {
    return input -> {
      if (!QUOTE_CHARS.contains(input.peek())) {
        return word().parse(input);
      }

      char quoteChar = input.readChar();

      StringBuilder readString = new StringBuilder();

      boolean escaped = false;
      while (input.canRead()) {
        char read = input.readChar();

        if (escaped) {
          escaped = false;
          readString.append(read);
          continue;
        }

        if (read == '\\') {
          escaped = true;
        } else if (read == quoteChar) {
          break;
        } else {
          readString.append(read);
        }
      }
      return readString.toString();
    };
  }

  /**
   * A parser that reads the whole left over input.
   *
   * @return a parser that reads the whole left over input
   */
  public static AtomicParser<String> greedyPhrase() {
    return input -> input.readWhile(it -> true);
  }
}
