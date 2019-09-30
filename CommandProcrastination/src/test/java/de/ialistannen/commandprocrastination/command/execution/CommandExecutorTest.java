package de.ialistannen.commandprocrastination.command.execution;

import static de.ialistannen.commandprocrastination.parsing.defaults.IntegerParsers.integer;
import static de.ialistannen.commandprocrastination.parsing.defaults.StringParsers.greedyPhrase;
import static de.ialistannen.commandprocrastination.parsing.defaults.StringParsers.literal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.ialistannen.commandprocrastination.command.Command;
import de.ialistannen.commandprocrastination.command.tree.CommandFinder;
import de.ialistannen.commandprocrastination.command.tree.CommandNode;
import de.ialistannen.commandprocrastination.context.GlobalContext;
import de.ialistannen.commandprocrastination.context.RequestContext;
import de.ialistannen.commandprocrastination.parsing.ParseException;
import de.ialistannen.commandprocrastination.parsing.SuccessParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CommandExecutorTest {

  private CommandExecutor<GlobalContext, RequestContext> executor;

  private String fooResult;
  private String fooBarResult;
  private int integerResult;
  private int abnormallyExitRunResult;

  @BeforeEach
  void setUp() {
    CommandNode<GlobalContext> root = new CommandNode<>(Command.nop(), SuccessParser.alwaysTrue());

    CommandNode<GlobalContext> foo = new CommandNode<>(
        it -> fooResult = it.shift(greedyPhrase()), SuccessParser.wrapping(literal("foo"))
    );
    CommandNode<GlobalContext> integer = new CommandNode<>(
        it -> integerResult = it.shift(integer()), SuccessParser.wrapping(integer())
    );
    CommandNode<GlobalContext> fooBar = new CommandNode<>(
        it -> fooBarResult = it.shift(greedyPhrase()), SuccessParser.wrapping(literal("bar"))
    );
    CommandNode<GlobalContext> abnormalExit = new CommandNode<>(
        it -> {
          throw new AbnormalCommandResultException("Hello");
        },
        SuccessParser.wrapping(literal("error"))
    );
    CommandNode<GlobalContext> commandException = new CommandNode<>(
        it -> {
          throw new CommandException("Test");
        },
        SuccessParser.wrapping(literal("command_exception"))
    );
    CommandNode<GlobalContext> parseException = new CommandNode<>(
        it -> it.shift(literal("Hello")),
        SuccessParser.wrapping(literal("parse_exception"))
    );

    root.addChild(foo);
    root.addChild(integer);
    root.addChild(abnormalExit);
    root.addChild(commandException);
    root.addChild(parseException);

    foo.addChild(fooBar);

    CommandFinder<GlobalContext> finder = new CommandFinder<>(root);
    executor = new SimpleExecutor(finder, SuccessParser.wrapping(literal(" ")));
  }

  @Test
  public void testFooSetsArgument() throws ParseException {
    executor.execute("foo Is this", null);
    assertEquals(
        "Is this",
        fooResult
    );
  }

  @Test
  public void testIntegerSetsArgument() throws ParseException {
    // 2000 is the node, 20 the argument
    executor.execute("2000 20", null);
    assertEquals(
        20,
        integerResult
    );
  }

  @Test
  public void testNestedSetsArgument() throws ParseException {
    executor.execute("foo bar is this", null);
    assertEquals(
        "is this",
        fooBarResult
    );
  }

  @Test
  public void testAbnormalExitRuns() {
    AbnormalCommandResultException error = assertThrows(
        AbnormalCommandResultException.class,
        () -> executor.execute("error", null)
    );
    assertEquals(
        "Hello",
        error.getKey()
    );
  }

  @Test
  public void testCommandExceptionIsPropagated() {
    CommandException exception = assertThrows(
        CommandException.class,
        () -> executor.execute("command_exception", null)
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
        () -> executor.execute("parse_exception", null)
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
        () -> executor.execute("whatever is not registered", null)
    );
    assertEquals(
        "Command for 'whatever is not registered' not found!",
        exception.getMessage()
    );
  }

  private static class SimpleExecutor extends CommandExecutor<GlobalContext, RequestContext> {

    SimpleExecutor(CommandFinder<GlobalContext> finder, SuccessParser commandArgumentSeparator) {
      super(finder, commandArgumentSeparator);
    }

    @Override
    protected GlobalContext createContext(RequestContext requestContext) {
      return new GlobalContext(requestContext);
    }
  }
}