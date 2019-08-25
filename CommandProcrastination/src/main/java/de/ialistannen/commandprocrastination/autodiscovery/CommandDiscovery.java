package de.ialistannen.commandprocrastination.autodiscovery;

import de.ialistannen.commandprocrastination.command.tree.CommandNode;
import de.ialistannen.commandprocrastination.command.tree.data.DefaultDataKey;
import de.ialistannen.commandprocrastination.context.Context;
import de.ialistannen.generated_commands.CommandClasses;

/**
 * Discovers commands on the classpath.
 */
public class CommandDiscovery {

  /**
   * Finds all commands, instantiates them, tries to order them and returns the root.
   *
   * @param <C> the type of the context
   * @return the found commands
   */
  public <C extends Context> CommandNode<C> findCommands() {
    Class[] classes = CommandClasses.COMMAND_CLASSES;

    DiscoveryRootCommand<C> root = new DiscoveryRootCommand<>();

    for (Class<?> aClass : classes) {
      if (!aClass.isAnnotationPresent(ActiveCommand.class)) {
        continue;
      }
      ActiveCommand activeCommand = aClass.getAnnotation(ActiveCommand.class);

      CommandNode<C> node;
      try {
        @SuppressWarnings("unchecked")
        CommandNode<C> temp = (CommandNode<C>) aClass.getConstructor().newInstance();
        node = temp;
      } catch (ReflectiveOperationException e) {
        throw new RuntimeException(e);
      }

      String parent = activeCommand.parent().equals("no-parent") ? null : activeCommand.parent();
      String name = activeCommand.name();

      node.setData(DefaultDataKey.IDENTIFIER, name);
      root.addChild(node, parent, activeCommand.parentClass());
    }

    return root;
  }
}
