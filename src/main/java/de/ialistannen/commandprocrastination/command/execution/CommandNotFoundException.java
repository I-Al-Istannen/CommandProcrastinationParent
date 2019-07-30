package de.ialistannen.commandprocrastination.command.execution;

/**
 * Indicates that a command was not found.
 */
public class CommandNotFoundException extends CommandException {

  public CommandNotFoundException(String commandName) {
    super("Command for '" + commandName + "' not found!");
  }
}
