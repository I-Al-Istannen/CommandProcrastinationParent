package de.ialistannen.commandprocrastination.autodiscovery;

import de.ialistannen.commandprocrastination.command.tree.CommandNode;
import de.ialistannen.commandprocrastination.context.GlobalContext;

/**
 * Creates instances of classes.
 *
 * @param <C> the type of the context the commands have
 */
public interface Instantiator<C extends GlobalContext> {

  /**
   * Creates a new instance of the given class.
   *
   * @param clazz the class
   * @param <T> the type of the class
   * @return the instance
   */
  <T extends CommandNode<C>> T newInstance(Class<T> clazz);
}
