package de.ialistannen.commandprocrastination.autodiscovery.processor;

/**
 * Marks the generated class to ease getting all relevant commands.
 */
public interface CollectedCommands {

  /**
   * Returns all collected commands.
   *
   * @return the collected commands
   */
  Class<?>[] getCollectedCommands();
}
