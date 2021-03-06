package de.ialistannen.commandprocrastination.command.tree;

import de.ialistannen.commandprocrastination.command.tree.data.DefaultDataKey;
import de.ialistannen.commandprocrastination.context.GlobalContext;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * A chain of commands that leads to the final selected one.
 *
 * @param <C> the context
 */
public class CommandChain<C extends GlobalContext> {

  private ChainLink<CommandNode<C>> start;
  private ChainLink<CommandNode<C>> end;

  /**
   * Creates a new Command chain.
   *
   * @param node the initial node
   */
  public CommandChain(CommandNode<C> node) {
    append(node);
  }

  /**
   * Appends the entire chain.
   *
   * @param chain the chain to append
   */
  public void append(CommandChain<C> chain) {
    for (CommandNode<C> node : chain.toList()) {
      append(node);
    }
  }

  /**
   * Appends a command node to this chain.
   *
   * @param node the node to append
   */
  public void append(CommandNode<C> node) {
    if (start == null) {
      start = new ChainLink<>(null, node);
      end = start;
    } else {
      end.next = new ChainLink<>(null, node);
      end = end.next;
    }
  }

  /**
   * Prepends a node.
   *
   * @param node the node to prepend.
   */
  public void prepend(CommandNode<C> node) {
    if (start == null) {
      append(node);
    } else {
      start = new ChainLink<>(start, node);
    }
  }

  /**
   * Builds the usage string.
   *
   * @return the usage string
   */
  public String buildUsage() {
    var currentLink = start;
    StringBuilder usage = new StringBuilder();

    while (currentLink != null) {
      if (currentLink.isEnd()) {
        usage.append(currentLink.getValue().getUsage());
      } else {
        currentLink.getValue().getHeadParser().getName().ifPresent(usage::append);
      }
      if (currentLink.getNext() != null) {
        boolean requiresNoSeparator = currentLink.getValue()
            .<Boolean>getOptionalData(DefaultDataKey.NO_ARGUMENT_SEPARATOR)
            .orElse(false);
        if (!requiresNoSeparator) {
          usage.append(" ");
        }
      }
      currentLink = currentLink.getNext();
    }

    return usage.toString();
  }

  /**
   * Converts this chain to a list.
   *
   * @return the created list
   */
  public List<CommandNode<C>> toList() {
    List<CommandNode<C>> list = new ArrayList<>();

    var currentLink = start;
    while (currentLink != null) {
      list.add(currentLink.getValue());
      currentLink = currentLink.next;
    }

    return list;
  }

  /**
   * Returns the final node in this chain.
   *
   * @return the final node in this chain
   */
  public CommandNode<C> getFinalNode() {
    return end.getValue();
  }

  @AllArgsConstructor
  @EqualsAndHashCode
  @ToString
  @Getter
  private static class ChainLink<E> {

    private ChainLink<E> next;
    private final E value;

    /**
     * Returns whether this chain link is the last one.
     *
     * @return true if this chain link is the last one
     */
    boolean isEnd() {
      return getNext() == null;
    }
  }
}
