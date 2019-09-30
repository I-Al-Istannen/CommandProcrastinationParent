package de.ialistannen.commandprocrastination.command.execution;

import de.ialistannen.commandprocrastination.command.tree.CommandChain;
import de.ialistannen.commandprocrastination.command.tree.CommandFinder;
import de.ialistannen.commandprocrastination.command.tree.CommandFinder.FindResult;
import de.ialistannen.commandprocrastination.command.tree.CommandNode;
import de.ialistannen.commandprocrastination.context.GlobalContext;
import de.ialistannen.commandprocrastination.context.RequestContext;
import de.ialistannen.commandprocrastination.parsing.ParseException;
import de.ialistannen.commandprocrastination.parsing.SuccessParser;
import de.ialistannen.commandprocrastination.util.StringReader;

/**
 * A command executor.
 *
 * @param <C> the context type
 */
public abstract class CommandExecutor<C extends GlobalContext, R extends RequestContext> {

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
   * <p><br>Delegates to {@link #execute(StringReader, RequestContext)}.</p>
   *
   * @param input the input
   * @throws AbnormalCommandResultException if the command throws one and no handler is
   *     registered
   * @throws CommandNotFoundException if the command was not found
   * @throws CommandException if there was an error executing the command
   * @throws ParseException if the input format is wrong
   * @see #execute(StringReader, RequestContext)
   */
  public void execute(String input, R requestContext) throws ParseException {
    execute(new StringReader(input), requestContext);
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
  public void execute(StringReader input, R requestContext) throws ParseException {
    FindResult<C> findResult = finder.find(input);

    if (!findResult.isSuccess()) {
      throw new CommandNotFoundException(input.readRemaining(), findResult);
    }

    boolean parsedSeparator = commandArgumentSeparator.parse(input);

    if (!parsedSeparator && input.canRead()) {
      throw new CommandException(
          new ParseException(input, "No separator after command!").getMessage()
      );
    }

    CommandChain<C> commandChain = findResult.getChain();
    requestContext.setReader(input);
    requestContext.setFinalNode(commandChain.getFinalNode());

    try {
      executeImpl(requestContext);
    } catch (AbnormalCommandResultException e) {
      handleAbnormalResult(commandChain, requestContext, e);
    }
  }

  /**
   * Executes the given command using the given context.
   *
   * @param requestContext the request context
   * @throws ParseException if an error occurs
   * @throws AbnormalCommandResultException if the command throws one
   * @throws CommandNotFoundException if the command was not found
   * @throws CommandException if there was an error executing the command
   */
  protected void executeImpl(R requestContext) throws ParseException {
    CommandNode<C> commandNode = requestContext.getFinalNode();
    commandNode.getCommand().execute(createContext(requestContext));
  }

  /**
   * Handles an abnormal result exception.
   *
   * @param chain the command chain
   * @param requestContext the request context
   * @param e the exception
   */
  protected void handleAbnormalResult(CommandChain<C> chain, R requestContext,
      AbnormalCommandResultException e) {
    throw e;
  }

  /**
   * Creates a fitting context for an input and node.
   *
   * @param requestContext the request context
   * @return a fitting context
   */
  protected abstract C createContext(R requestContext);
}
