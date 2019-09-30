package de.ialistannen.commandprocrastination.command.execution;

import de.ialistannen.commandprocrastination.command.tree.CommandFinder.FindResult;
import de.ialistannen.commandprocrastination.context.GlobalContext;

/**
 * Indicates that a command was not found.
 */
public class CommandNotFoundException extends CommandException {

  private FindResult<?> result;

  public CommandNotFoundException(String commandName, FindResult<?> result) {
    super("Command for '" + commandName + "' not found!");
    this.result = result;
  }

  /**
   * Returns the find result.
   *
   * @param <C> the type of the context
   * @return the find result
   */
  public <C extends GlobalContext> FindResult<C> getResult() {
    @SuppressWarnings("unchecked")
    FindResult<C> result = (FindResult<C>) this.result;
    return result;
  }
}
