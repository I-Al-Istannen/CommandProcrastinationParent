package de.ialistannen.commandprocrastination.parsing.defaults;

import de.ialistannen.commandprocrastination.parsing.AtomicParser;
import de.ialistannen.commandprocrastination.parsing.ParseException;
import java.util.regex.Pattern;

public class IntegerParsers {

  /**
   * A parser that reads an integer.
   *
   * @return a parser that reads an integer
   */
  public static AtomicParser<Integer> integer() {
    Pattern pattern = Pattern.compile("[+\\-]?(\\d|[_])+");

    return input -> {
      String read = input.readRegex(pattern);
      try {
        return Integer.parseInt(read.replace("_", ""));
      } catch (NumberFormatException e) {
        throw new ParseException(input, "Invalid integer. Maybe too large/small?", e);
      }
    };
  }

  /**
   * A parser that reads an integer greater than the given minimum.
   *
   * @param min the minimum value. Inclusive.
   * @return a parser that reads an integer greater than the given minimum
   */
  public static AtomicParser<Integer> intGreaterThan(int min) {
    return intWithinRage(min, Integer.MAX_VALUE);
  }

  /**
   * A parser that reads an integer smaller than the given maximum.
   *
   * @param max the maximum value. Inclusive.
   * @return a parser that reads an integer smaller than the given maximum
   */
  public static AtomicParser<Integer> intSmallerThan(int max) {
    return intWithinRage(Integer.MIN_VALUE, max);
  }

  /**
   * A parser that reads an integer within given bounds.
   *
   * @param min the minimum value. Inclusive.
   * @param max the maximum value. Inclusive.
   * @return a parser that reads an integer smaller than the given minimum
   */
  public static AtomicParser<Integer> intWithinRage(int min, int max) {
    AtomicParser<Integer> parser = input -> {
      Integer integer = integer().parse(input);

      if (integer < min) {
        String message = "Integer too small (not between " + min + " and " + max + ")";

        if (max == Integer.MAX_VALUE) {
          message = "Integer too small (not >= " + min + ")";
        }

        throw new ParseException(input, message);
      }
      if (integer > max) {
        String message = "Integer too large (not between " + min + " and " + max + ")";

        if (min == Integer.MIN_VALUE) {
          message = "Integer too large (not <= " + max + ")";
        }

        throw new ParseException(input, message);
      }
      return integer;
    };

    String name = "Integer between " + min + " and " + max;
    if (min == Integer.MIN_VALUE) {
      name = "Integer smaller than " + max;
    } else if (max == Integer.MAX_VALUE) {
      name = "Integer bigger than " + min;
    }
    if (min == Integer.MIN_VALUE && max == Integer.MAX_VALUE) {
      name = "Decimal value";
    }

    return AtomicParser.named(name, parser);
  }
}
