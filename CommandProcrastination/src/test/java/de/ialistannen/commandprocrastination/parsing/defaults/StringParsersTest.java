package de.ialistannen.commandprocrastination.parsing.defaults;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.ialistannen.commandprocrastination.parsing.AtomicParser;
import de.ialistannen.commandprocrastination.parsing.ParseException;
import de.ialistannen.commandprocrastination.util.StringReader;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StringParsersTest {

  @ParameterizedTest(name = "\"{0}\" should be \"{1}\"")
  @CsvSource({
      "Hello you,Hello",
      "Hello_you,Hello_you",
      "Hello!,Hello!",
      "Hello*,Hello*",
      "Hello[,Hello[",
      "Hello],Hello]",
      "Hello\tMy,Hello",
      "Hello\\nMy,Hello",
  })
  public void readWord(String input, String expected) throws ParseException {
    AtomicParser<String> parser = StringParsers.word();

    assertEquals(
        expected,
        parser.parse(new StringReader(input.replace("\\n", "\n")))
    );
  }

  @ParameterizedTest(name = "\"{0}\" should be \"{1}\"")
  @CsvSource({
      "Hello my friend,Hello",
      "'''Hello my friend''',Hello my friend",
      "\"Hello my friend\",Hello my friend",
      "\"Hello my friend,Hello my friend",
      "Hello my friend\",Hello",
      "\"Hello\tmy friend\",Hello\tmy friend",
  })
  public void readPhrase(String input, String expected) throws ParseException {
    AtomicParser<String> parser = StringParsers.phrase();

    assertEquals(
        expected,
        parser.parse(new StringReader(input))
    );
  }

  @ParameterizedTest(name = "\"{0}\" should be \"{1}\"")
  @CsvSource({
      "'''Hello my friend''','''Hello my friend'''",
      "\"Hello my friend\",\"Hello my friend\"",
      "\"Hello\tmy friend\",\"Hello\tmy friend\"",
      "Long thing with stuff,Long thing with stuff",
  })
  public void readGreedy(String input, String expected) throws ParseException {
    AtomicParser<String> parser = StringParsers.greedyPhrase();

    assertEquals(
        expected,
        parser.parse(new StringReader(input))
    );
  }
}