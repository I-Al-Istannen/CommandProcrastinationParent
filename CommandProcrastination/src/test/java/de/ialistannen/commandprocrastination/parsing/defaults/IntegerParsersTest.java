package de.ialistannen.commandprocrastination.parsing.defaults;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.ialistannen.commandprocrastination.parsing.AtomicParser;
import de.ialistannen.commandprocrastination.parsing.ParseException;
import de.ialistannen.commandprocrastination.util.StringReader;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class IntegerParsersTest {

  @ParameterizedTest(name = "\"{0}\" should be \"{1}\" and error {2}")
  @CsvSource(value = {
      "hey,0,true",
      "-2147483649,0,true",
      "2147483648,0,true",
      "-1000,-1000,false",
      "+20,20,false",
      "+200_000,200000,false",
      "2147483647,2147483647,false",
      "-2147483648,-2147483648,false",
  })
  public void testInteger(String input, Integer expected, boolean failure) throws ParseException {
    AtomicParser<Integer> parser = IntegerParsers.integer();

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
  public void testIntegerMin(String input, Integer expected, boolean failure)
      throws ParseException {
    AtomicParser<Integer> parser = IntegerParsers.intGreaterThan(20);

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
  public void testIntegerMax(String input, Integer expected, boolean failure)
      throws ParseException {
    AtomicParser<Integer> parser = IntegerParsers.intSmallerThan(20);

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
  public void testIntegerInRange(String input, Integer expected, boolean failure)
      throws ParseException {
    AtomicParser<Integer> parser = IntegerParsers.intWithinRage(-20, 20);

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