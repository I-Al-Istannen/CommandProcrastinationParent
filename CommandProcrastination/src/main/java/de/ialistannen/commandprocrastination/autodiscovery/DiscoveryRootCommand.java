package de.ialistannen.commandprocrastination.autodiscovery;

import de.ialistannen.commandprocrastination.command.Command;
import de.ialistannen.commandprocrastination.command.tree.CommandNode;
import de.ialistannen.commandprocrastination.command.tree.data.DefaultDataKey;
import de.ialistannen.commandprocrastination.context.Context;
import de.ialistannen.commandprocrastination.parsing.SuccessParser;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The root command for discovered commands.
 *
 * @param <C> the type of the context
 */
public class DiscoveryRootCommand<C extends Context> extends CommandNode<C> {

  private List<CommandNode<C>> allCommands;

  /**
   * Creates a new command node serving as the root for discovered commands.
   */
  public DiscoveryRootCommand() {
    super(Command.nop(), SuccessParser.alwaysTrue());

    allCommands = new ArrayList<>();
  }

  /**
   * Adds a child trying to respect the parent.
   *
   * @param node the node
   * @param parent the parent
   * @param parentClass the class of the parent
   * @return true if the node was given to the parent, false if the parent could not be found
   */
  public boolean addChild(CommandNode<C> node, String parent,
      Class<? extends CommandNode> parentClass) {
    Optional<CommandNode<C>> parentNode = allCommands.stream()
        .filter(it -> hasName(it, parent))
        .findFirst()
        .or(() -> allCommands.stream()
            .filter(it -> it.getClass() == parentClass)
            .findFirst()
        );

    allCommands.add(node);

    if (parentNode.isPresent()) {
      parentNode.get().addChild(node);
      return true;
    }
    addChild(node);
    return false;
  }

  private boolean hasName(CommandNode<C> node, String name) {
    return getName(node).map(it -> it.equals(name)).orElse(false);
  }

  private Optional<String> getName(CommandNode<C> node) {
    return node.<String>getOptionalData(DefaultDataKey.IDENTIFIER)
        .or(() -> node.getHeadParser().getName());
  }
}
