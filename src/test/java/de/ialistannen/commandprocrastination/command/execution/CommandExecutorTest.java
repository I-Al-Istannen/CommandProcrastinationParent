package de.ialistannen.commandprocrastination.command.execution;

import static de.ialistannen.commandprocrastination.parsing.defaults.IntegerParsers.integer;
import static de.ialistannen.commandprocrastination.parsing.defaults.StringParsers.greedyPhrase;
import static de.ialistannen.commandprocrastination.parsing.defaults.StringParsers.literal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.ialistannen.commandprocrastination.command.Command;
import de.ialistannen.commandprocrastination.command.tree.CommandFinder;
import de.ialistannen.commandprocrastination.command.tree.CommandNode;
import de.ialistannen.commandprocrastination.context.Context;
import de.ialistannen.commandprocrastination.parsing.ParseException;
import de.ialistannen.commandprocrastination.parsing.SuccessParser;
import de.ialistannen.commandprocrastination.util.StringReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CommandExecutorTest {

  private CommandExecutor<Context> executor;

  private String fooResult;
  private String fooBarResult;
  private int integerResult;
  private int abnormallyExitRunResult;

  @BeforeEach
  void setUp() {
    CommandNode<Context> root = new CommandNode<>(Command.nop(), SuccessParser.alwaysTrue());

    CommandNode<Context> foo = new CommandNode<>(
        it -> fooResult = it.shift(greedyPhrase()), SuccessParser.wrapping(literal("foo"))
    );
    CommandNode<Context> integer = new CommandNode<>(
        it -> integerResult = it.shift(integer()), SuccessParser.wrapping(integer())
    );
    CommandNode<Context> fooBar = new CommandNode<>(
        it -> fooBarResult = it.shift(greedyPhrase()), SuccessParser.wrapping(literal("bar"))
    );
    CommandNode<Context> abnormalExit = new CommandNode<>(
        it -> {
          throw new AbnormalCommandResultException(() -> abnormallyExitRunResult = 1);
        },
        SuccessParser.wrapping(literal("error"))
    );
    CommandNode<Context> commandException = new CommandNode<>(
        it -> {
          throw new CommandException("Test");
        },
        SuccessParser.wrapping(literal("command_exception"))
    );
    CommandNode<Context> parseException = new CommandNode<>(
        it -> it.shift(literal("Hello")),
        SuccessParser.wrapping(literal("parse_exception"))
    );

    root.addChild(foo);
    root.addChild(integer);
    root.addChild(abnormalExit);
    root.addChild(commandException);
    root.addChild(parseException);

    foo.addChild(fooBar);

    CommandFinder<Context> finder = new CommandFinder<>(root);
    executor = new SimpleExecutor(finder, SuccessParser.wrapping(literal(" ")));
  }

  @Test
  public void testFooSetsArgument() throws ParseException {
    executor.execute("foo Is this");
    assertEquals(
        "Is this",
        fooResult
    );
  }

  @Test
  public void testIntegerSetsArgument() throws ParseException {
    // 2000 is the node, 20 the argument
    executor.execute("2000 20");
    assertEquals(
        20,
        integerResult
    );
  }

  @Test
  public void testNestedSetsArgument() throws ParseException {
    executor.execute("foo bar is this");
    assertEquals(
        "is this",
        fooBarResult
    );
  }

  @Test
  public void testAbnormalExitRuns() throws ParseException {
    executor.execute("error");
    assertEquals(
        1,
        abnormallyExitRunResult
    );
  }

  @Test
  public void testCommandExceptionIsPropagated() {
    CommandException exception = assertThrows(
        CommandException.class,
        () -> executor.execute("command_exception")
    );
    assertEquals(
        "Test",
        exception.getMessage()
    );
  }

  @Test
  public void testParseExceptionIsPropagated() {
    ParseException exception = assertThrows(
        ParseException.class,
        () -> executor.execute("parse_exception")
    );
    assertEquals(
        "Expected 'Hello' at _exception<---[HERE]",
        exception.getMessage()
    );
  }

  @Test
  public void testCommandNotFoundException() {
    CommandNotFoundException exception = assertThrows(
        CommandNotFoundException.class,
        () -> executor.execute("whatever is not registered")
    );
    assertEquals(
        "Command for 'whatever is not registered' not found!",
        exception.getMessage()
    );
  }

  private static class SimpleExecutor extends CommandExecutor<Context> {

    SimpleExecutor(CommandFinder<Context> finder, SuccessParser commandArgumentSeparator) {
      super(finder, commandArgumentSeparator);
    }

    @Override
    protected Context createContext(StringReader input, CommandNode<Context> node) {
      return new Context(input, node);
    }
  }
}