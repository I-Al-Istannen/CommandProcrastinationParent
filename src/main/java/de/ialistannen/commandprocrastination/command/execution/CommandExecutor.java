package de.ialistannen.commandprocrastination.command.execution;

import de.ialistannen.commandprocrastination.command.tree.CommandFinder;
import de.ialistannen.commandprocrastination.command.tree.CommandNode;
import de.ialistannen.commandprocrastination.context.Context;
import de.ialistannen.commandprocrastination.parsing.ParseException;
import de.ialistannen.commandprocrastination.parsing.SuccessParser;
import de.ialistannen.commandprocrastination.util.StringReader;
import java.util.Optional;

/**
 * A command executor.
 *
 * @param <C> the context type
 */
public abstract class CommandExecutor<C extends Context> {

  private CommandFinder<C> finder;
  private SuccessParser commandArgumentSeparator;

  /**
   * Creates a new command executor.
   *
   * @param finder the command finder
   * @param commandArgumentSeparator the separator between command and arguments
   */
  public CommandExecutor(CommandFinder<C> finder, SuccessParser commandArgumentSeparator) {
    this.finder = finder;
    this.commandArgumentSeparator = commandArgumentSeparator;
  }

  /**
   * Finds and executes a command.
   *
   * <p><br>Delegates to {@link #execute(StringReader)}.</p>
   *
   * @param input the input
   * @throws AbnormalCommandResultException if the command throws one and no handler is
   *     registered
   * @throws CommandNotFoundException if the command was not found
   * @throws CommandException if there was an error executing the command
   * @throws ParseException if the input format is wrong
   * @see #execute(StringReader)
   */
  public void execute(String input) throws ParseException {
    execute(new StringReader(input));
  }

  /**
   * Finds and executes a command.
   *
   * @param input the input
   * @throws AbnormalCommandResultException if the command throws one and no handler is
   *     registered
   * @throws CommandNotFoundException if the command was not found
   * @throws CommandException if there was an error executing the command
   * @throws ParseException if the input format is wrong
   */
  public void execute(StringReader input) throws ParseException {
    Optional<CommandNode<C>> node = finder.find(input);

    if (node.isEmpty()) {
      throw new CommandNotFoundException(input.readRemaining());
    }

    boolean parsedSeparator = commandArgumentSeparator.parse(input);

    if (!parsedSeparator && input.canRead()) {
      throw new CommandException(
          new ParseException(input, "No separator after command!").getMessage()
      );
    }

    CommandNode<C> commandNode = node.get();

    try {
      executeImpl(input, commandNode);
    } catch (AbnormalCommandResultException e) {
      handleAbnormalResult(commandNode, e);
    }
  }

  /**
   * Executes the given command using the given context.
   *
   * @param input the input
   * @param commandNode the command node
   * @throws ParseException if an error occurs
   * @throws AbnormalCommandResultException if the command throws one
   * @throws CommandNotFoundException if the command was not found
   * @throws CommandException if there was an error executing the command
   */
  protected void executeImpl(StringReader input, CommandNode<C> commandNode) throws ParseException {
    commandNode.getCommand().execute(createContext(input, commandNode));
  }

  /**
   * Handles an abnormal result exception.
   *
   * @param node the source node
   * @param e the exception
   */
  protected void handleAbnormalResult(CommandNode<C> node, AbnormalCommandResultException e) {
    throw e;
  }

  /**
   * Creates a fitting context for an input and node.
   *
   * @param input the input
   * @param node the command node
   * @return a fitting context
   */
  protected abstract C createContext(StringReader input, CommandNode<C> node);
}
