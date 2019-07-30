package de.ialistannen.commandprocrastination.command;

import de.ialistannen.commandprocrastination.context.Context;
import de.ialistannen.commandprocrastination.parsing.ParseException;

/**
 * A command that can be executed.
 *
 * @param <C> the context type
 */
public interface Command<C extends Context> {

  /**
   * Executes the command.
   *
   * @param context the context to use
   * @throws de.ialistannen.commandprocrastination.command.execution.CommandException if an
   *     error occurs while executing the command
   * @throws de.ialistannen.commandprocrastination.command.execution.AbnormalCommandResultException
   *     if the command exits abnormally
   * @throws ParseException if the input data is not correctly formatted
   */
  void execute(C context) throws ParseException;

  /**
   * Returns a command that does nothing.
   *
   * @param <C> the context
   * @return the command
   */
  static <C extends Context> Command<C> nop() {
    return context -> {

    };
  }
}
