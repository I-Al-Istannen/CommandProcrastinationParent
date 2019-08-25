package de.ialistannen.commandprocrastination.context;

import static de.ialistannen.commandprocrastination.parsing.defaults.IntegerParsers.integer;
import static de.ialistannen.commandprocrastination.parsing.defaults.StringParsers.phrase;
import static de.ialistannen.commandprocrastination.parsing.defaults.StringParsers.word;
import static org.junit.jupiter.api.Assertions.assertEquals;

import de.ialistannen.commandprocrastination.parsing.ParseException;
import de.ialistannen.commandprocrastination.util.StringReader;
import org.junit.jupiter.api.Test;

class ContextTest {

  @Test
  public void shiftAFew() throws ParseException {
    Context context = new Context(new StringReader("20 You \"My friend\""), null);

    assertEquals(
        20,
        context.shift(integer())
    );

    assertEquals(
        "You",
        context.shift(word())
    );

    assertEquals(
        "My friend",
        context.shift(phrase())
    );
  }


}