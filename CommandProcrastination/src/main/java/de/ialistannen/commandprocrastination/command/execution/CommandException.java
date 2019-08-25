package de.ialistannen.commandprocrastination.command.execution;

/**
 * An exception that occurred while executing a command.
 */
public class CommandException extends RuntimeException {

  public CommandException(String message) {
    super(message);
  }

  public CommandException(String message, Throwable cause) {
    super(message, cause);
  }
}
