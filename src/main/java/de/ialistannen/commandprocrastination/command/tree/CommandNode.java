package de.ialistannen.commandprocrastination.command.tree;

import de.ialistannen.commandprocrastination.command.Command;
import de.ialistannen.commandprocrastination.command.tree.data.CommandDataKey;
import de.ialistannen.commandprocrastination.context.Context;
import de.ialistannen.commandprocrastination.parsing.SuccessParser;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A command node.
 *
 * @param <C> the type of the context
 */
public class CommandNode<C extends Context> {

  private Command<C> command;
  private SuccessParser headParser;
  private Map<CommandDataKey, Object> userData;

  private List<CommandNode<C>> children;

  /**
   * Creates a new command node.
   *
   * @param command the command of this node
   * @param headParser the head parser to detect that this node is the correct one
   */
  public CommandNode(Command<C> command, SuccessParser headParser) {
    this.command = command;
    this.headParser = headParser;
    this.userData = new HashMap<>();
    this.children = new ArrayList<>();
  }

  /**
   * Sets some data.
   *
   * @param key the key
   * @param data the data
   * @param <T> the type of the data
   */
  public <T> void setData(CommandDataKey key, T data) {
    userData.put(key, data);
  }

  /**
   * Returns user data.
   *
   * @param key the key to look up
   * @param <T> the type of the value
   * @return the value or null if none
   */
  public <T> T getData(CommandDataKey key) {
    @SuppressWarnings("unchecked")
    T t = (T) userData.get(key);
    return t;
  }

  /**
   * Returns user data.
   *
   * @param <T> the type of the value
   * @param key the key to look up
   * @return the value or an empty optional if none
   */
  public <T> Optional<T> getOptionalData(CommandDataKey key) {
    return Optional.ofNullable(getData(key));
  }

  /**
   * Adds a child.
   *
   * @param child the child to add
   */
  public void addChild(CommandNode<C> child) {
    children.add(child);
  }

  /**
   * Removes a child.
   *
   * @param child the child to remove
   */
  public void removeChild(CommandNode<C> child) {
    children.remove(child);
  }

  /**
   * Returns all children.
   *
   * @return the children
   */
  public List<CommandNode<C>> getChildren() {
    return Collections.unmodifiableList(children);
  }

  /**
   * Returns the command for this node.
   *
   * @return the command for this node
   */
  public Command<C> getCommand() {
    return command;
  }

  /**
   * Returns the head parser for this node. If the parser matches, the command should be taken.
   *
   * @return the head parser
   */
  public SuccessParser getHeadParser() {
    return headParser;
  }

  @Override
  public String toString() {
    return "CommandNode{" +
        "userData=" + userData +
        ", child_count=" + children.size() +
        ", hashcode=" + hashCode() +
        '}';
  }
}
