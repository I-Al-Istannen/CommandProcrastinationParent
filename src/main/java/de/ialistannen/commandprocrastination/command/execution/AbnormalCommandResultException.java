package de.ialistannen.commandprocrastination.command.execution;

/**
 * An abnormal command result with a handler.
 */
public class AbnormalCommandResultException extends RuntimeException {

  private Runnable action;

  public AbnormalCommandResultException(Runnable action) {
    this.action = action;
  }

  /**
   * Returns the action to run.
   *
   * @return the action to run
   */
  public Runnable getAction() {
    return action;
  }
}
