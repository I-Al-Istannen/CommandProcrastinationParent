package de.ialistannen.commandprocrastination.command.tree;

import de.ialistannen.commandprocrastination.context.Context;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A chain of commands that leads to the final selected one.
 *
 * @param <C> the context
 */
public class CommandChain<C extends Context> {

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
    List<String> usageParts = new ArrayList<>();

    while (currentLink != null) {
      if (currentLink.isEnd()) {
        usageParts.add(currentLink.getValue().getUsage());
      } else {
        if (currentLink.getValue().getHeadParser().getName() != null) {
          usageParts.add(currentLink.getValue().getHeadParser().getName());
        }
      }
      currentLink = currentLink.getNext();
    }

    return String.join(" ", usageParts);
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

  private static class ChainLink<E> {

    private ChainLink<E> next;
    private E value;

    ChainLink(ChainLink<E> next, E value) {
      this.next = next;
      this.value = value;
    }

    /**
     * Returns the node.
     *
     * @return the node
     */
    E getValue() {
      return value;
    }

    /**
     * Returns the child chain or null.
     *
     * @return the child cian or null
     */
    ChainLink<E> getNext() {
      return next;
    }

    /**
     * Returns whether this chain link is the last one.
     *
     * @return true if this chain link is the last one
     */
    boolean isEnd() {
      return getNext() == null;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      ChainLink<?> chainLink = (ChainLink<?>) o;
      return Objects.equals(next, chainLink.next) &&
          Objects.equals(value, chainLink.value);
    }

    @Override
    public int hashCode() {
      return Objects.hash(next, value);
    }
  }
}
