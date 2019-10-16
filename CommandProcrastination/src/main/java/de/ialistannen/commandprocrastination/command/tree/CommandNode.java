package de.ialistannen.commandprocrastination.command.tree;

import static de.ialistannen.commandprocrastination.parsing.defaults.StringParsers.literal;

import de.ialistannen.commandprocrastination.command.Command;
import de.ialistannen.commandprocrastination.command.tree.data.CommandDataKey;
import de.ialistannen.commandprocrastination.context.GlobalContext;
import de.ialistannen.commandprocrastination.parsing.AtomicParser;
import de.ialistannen.commandprocrastination.parsing.SuccessParser;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A command node.
 *
 * @param <C> the type of the context
 */
public class CommandNode<C extends GlobalContext> {

  private Command<C> command;
  private SuccessParser headParser;
  private Map<CommandDataKey, Object> userData;

  private List<CommandNode<C>> children;
  private CommandNode<C> parent;

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
   * Creates a new command node.
   *
   * @param command the command of this node
   * @param headParser the head parser to detect that this node is the correct one. It will be
   *     wrapped in a {@link SuccessParser}.
   * @see CommandNode#CommandNode(Command, SuccessParser)
   */
  public CommandNode(Command<C> command, AtomicParser<?> headParser) {
    this(command, SuccessParser.wrapping(headParser));
  }

  /**
   * Creates a new command node.
   *
   * @param command the command of this node
   * @param literalKeyword the literal keyword. It will be wrapped in a {@link
   *     de.ialistannen.commandprocrastination.parsing.defaults.StringParsers#literal literal}
   * @see CommandNode#CommandNode(Command, AtomicParser)
   */
  public CommandNode(Command<C> command, String literalKeyword) {
    this(command, literal(literalKeyword));
  }

  /**
   * Creates a new command node with a {@link Command#nop()} command.
   *
   * @param headParser the head parser to detect that this node is the correct one
   */
  public CommandNode(SuccessParser headParser) {
    this(Command.nop(), headParser);
  }

  /**
   * Sets the used command.
   *
   * @param command the command
   */
  protected void setCommand(Command<C> command) {
    this.command = command;
  }

  /**
   * Returns the parent node.
   *
   * @return the parent npde
   */
  public Optional<CommandNode<C>> getParent() {
    return Optional.ofNullable(parent);
  }

  /**
   * Sets the parent node.
   *
   * @param parent the parent node
   */
  void setParent(CommandNode<C> parent) {
    this.parent = parent;
  }

  /**
   * Sets some data.
   *
   * @param key the key
   * @param data the data
   * @param <T> the type of the data
   * @return this node
   */
  public <T> CommandNode<C> setData(CommandDataKey key, T data) {
    userData.put(key, data);
    return this;
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
   * Returns whether user data is prsent.
   *
   * @param key the key to look up
   * @return true if the value is present
   */
  public boolean hasOptionalData(CommandDataKey key) {
    return getOptionalData(key).isPresent();
  }

  /**
   * Adds a child.
   *
   * @param child the child to add
   */
  public void addChild(CommandNode<C> child) {
    children.add(child);
    child.setParent(this);
  }

  /**
   * Removes a child.
   *
   * @param child the child to remove
   */
  public void removeChild(CommandNode<C> child) {
    children.remove(child);
    child.setParent(null);
  }

  public FluentSubCommand<C> addSubCommand() {
    return new FluentSubCommand<>(this);
  }

  public CommandNode<C> addSubCommand(FluentSubCommand<C> sub) {
    sub.setTarget(this);
    sub.finish();
    return this;
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

  /**
   * Returns the usage for this command.
   *
   * @return the usage for the command
   */
  public String getUsage() {
    StringBuilder usage = new StringBuilder(headParser.getName().orElse(""));

    if (!children.isEmpty()) {
      String childUsages = children.stream()
          .map(CommandNode::getUsage)
          .collect(Collectors.joining("|", "[", "]"));
      usage.append(" ").append(childUsages);
    }

    return usage.toString();
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
