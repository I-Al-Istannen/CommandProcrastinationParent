package de.ialistannen.commandprocrastination.parsing.defaults;

import de.ialistannen.commandprocrastination.parsing.AtomicParser;
import de.ialistannen.commandprocrastination.parsing.ParseException;
import java.util.regex.Pattern;

/**
 * Parsers for doubles.
 */
public class DoubleParsers {

  /**
   * A parser that reads a double.
   *
   * @return a parser that reads a double
   */
  public static AtomicParser<Double> decimal() {
    Pattern pattern = Pattern.compile("[+\\-]?(\\d|[_,.])+");

    return input -> {
      String read = input.readRegex(pattern);
      try {
        return Double.parseDouble(read.replace("_", ""));
      } catch (NumberFormatException e) {
        throw new ParseException(input, "Invalid decimal value. Maybe too large/small?", e);
      }
    };
  }

  /**
   * A parser that reads a double greater than the given minimum.
   *
   * @param min the minimum value. Inclusive.
   * @return a parser that reads a double greater than the given minimum
   */
  public static AtomicParser<Double> decimalGreaterThan(double min) {
    return decimalWithinRage(min, Double.POSITIVE_INFINITY);
  }

  /**
   * A parser that reads a double smaller than the given maximum.
   *
   * @param max the maximum value. Inclusive.
   * @return a parser that reads a double smaller than the given maximum
   */
  public static AtomicParser<Double> decimalSmallerThan(double max) {
    return decimalWithinRage(Double.NEGATIVE_INFINITY, max);
  }

  /**
   * A parser that reads a double within given bounds.
   *
   * @param min the minimum value. Inclusive.
   * @param max the maximum value. Inclusive.
   * @return a parser that reads a double smaller than the given minimum
   */
  public static AtomicParser<Double> decimalWithinRage(double min, double max) {
    return input -> {
      Double parsed = decimal().parse(input);

      if (parsed < min) {
        String message = "Decimal value too small (not between " + min + " and " + max + ")";

        if (max == Double.POSITIVE_INFINITY) {
          message = "Decimal value too small (not >= " + min + ")";
        }

        throw new ParseException(input, message);
      }
      if (parsed > max) {
        String message = "Decimal value too large (not between " + min + " and " + max + ")";

        if (min == Double.NEGATIVE_INFINITY) {
          message = "Decimal value too large (not <= " + max + ")";
        }

        throw new ParseException(input, message);
      }
      return parsed;
    };
  }
}
