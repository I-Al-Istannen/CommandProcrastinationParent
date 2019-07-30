package de.ialistannen.commandprocrastination.command.tree;

import static de.ialistannen.commandprocrastination.parsing.defaults.IntegerParsers.integer;
import static de.ialistannen.commandprocrastination.parsing.defaults.StringParsers.literal;
import static org.junit.jupiter.api.Assertions.assertEquals;

import de.ialistannen.commandprocrastination.command.Command;
import de.ialistannen.commandprocrastination.context.Context;
import de.ialistannen.commandprocrastination.parsing.SuccessParser;
import de.ialistannen.commandprocrastination.util.StringReader;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CommandFinderTest {

  private CommandFinder<Context> finder;
  private CommandNode<Context> foo;
  private CommandNode<Context> integer;
  private CommandNode<Context> fooBar;

  @BeforeEach
  void setUp() {
    CommandNode<Context> root = new CommandNode<>(Command.nop(), SuccessParser.alwaysTrue());

    foo = new CommandNode<>(
        Command.nop(), SuccessParser.wrapping(literal("foo"))
    );
    integer = new CommandNode<>(
        Command.nop(), SuccessParser.wrapping(integer())
    );
    fooBar = new CommandNode<>(
        Command.nop(), SuccessParser.wrapping(literal("bar"))
    );

    root.addChild(foo);
    root.addChild(integer);

    foo.addChild(fooBar);

    finder = new CommandFinder<>(root);
  }

  @Test
  void findsLiteral() {
    assertFinds(foo, "foo");
  }

  @Test
  void findsArbitraryInteger() {
    assertFinds(integer, "200");
  }

  @Test
  void findsIntegerWhenStartingWithInteger() {
    StringReader reader = assertFinds(integer, "200s");
    assertEquals(
        "s",
        reader.readRemaining()
    );
  }

  @Test
  void doesNotFindOtherInvalidInteger() {
    assertNotFound("s200");
  }

  @Test
  void findsNested() {
    assertFinds(fooBar, "foo bar");
  }

  private StringReader assertFinds(CommandNode<Context> node, String input) {
    StringReader reader = new StringReader(input);
    assertEquals(
        Optional.of(node),
        finder.find(reader)
    );
    return reader;
  }

  private void assertNotFound(String input) {
    assertEquals(
        Optional.empty(),
        finder.find(new StringReader(input))
    );
  }
}