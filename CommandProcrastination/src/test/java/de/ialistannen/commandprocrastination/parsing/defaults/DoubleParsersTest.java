package de.ialistannen.commandprocrastination.parsing.defaults;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.ialistannen.commandprocrastination.parsing.AtomicParser;
import de.ialistannen.commandprocrastination.parsing.ParseException;
import de.ialistannen.commandprocrastination.util.StringReader;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DoubleParsersTest {


  @ParameterizedTest(name = "\"{0}\" should be \"{1}\" and error {2}")
  @CsvSource(value = {
      "hey,0,true",
      "NaN,0,true",
      "Infinity,0,true",
      "-214748364.9,-214748364.9,false",
      "2147483.648,2147483.648,false",
      "-1000,-1000,false",
      "+20,20,false",
      "+200_000,200000,false",
      "2147483647,2147483647,false",
      "-2147483648,-2147483648,false",
  })
  public void testDouble(String input, Double expected, boolean failure) throws ParseException {
    AtomicParser<Double> parser = DoubleParsers.decimal();

    if (failure) {
      assertThrows(ParseException.class, () -> parser.parse(new StringReader(input)));
    } else {
      assertEquals(
          expected,
          parser.parse(new StringReader(input))
      );
    }
  }

  @ParameterizedTest(name = "\"{0}\" should be \"{1}\" and error \"{2}\"")
  @CsvSource(value = {
      "hey,0,true",
      "-1000,0,true",
      "19,0,true",
      "-20,0,true",
      "20,20,false",
      "21,21,false",
  })
  public void testDoubleMin(String input, Double expected, boolean failure)
      throws ParseException {
    AtomicParser<Double> parser = DoubleParsers.decimalGreaterThan(20);

    if (failure) {
      assertThrows(ParseException.class, () -> parser.parse(new StringReader(input)));
    } else {
      assertEquals(
          expected,
          parser.parse(new StringReader(input))
      );
    }
  }

  @ParameterizedTest(name = "\"{0}\" should be \"{1}\" and error \"{2}\"")
  @CsvSource(value = {
      "hey,0,true",
      "21,0,true",
      "-1000,-1000,false",
      "20,20,false",
      "19,19,false",
      "-20,-20,false",
  })
  public void testDoubleMax(String input, Double expected, boolean failure)
      throws ParseException {
    AtomicParser<Double> parser = DoubleParsers.decimalSmallerThan(20);

    if (failure) {
      assertThrows(ParseException.class, () -> parser.parse(new StringReader(input)));
    } else {
      assertEquals(
          expected,
          parser.parse(new StringReader(input))
      );
    }
  }

  @ParameterizedTest(name = "\"{0}\" should be \"{1}\" and error \"{2}\"")
  @CsvSource(value = {
      "hey,0,true",
      "-1000,0,true",
      "20,20,false",
      "19,19,false",
      "21,0,true",
      "-20,-20,false",
  })
  public void testDoubleInRange(String input, Double expected, boolean failure)
      throws ParseException {
    AtomicParser<Double> parser = DoubleParsers.decimalWithinRage(-20, 20);

    if (failure) {
      assertThrows(ParseException.class, () -> parser.parse(new StringReader(input)));
    } else {
      assertEquals(
          expected,
          parser.parse(new StringReader(input))
      );
    }
  }
}