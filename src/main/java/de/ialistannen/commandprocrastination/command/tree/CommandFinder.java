package de.ialistannen.commandprocrastination.command.tree;

import static de.ialistannen.commandprocrastination.parsing.defaults.StringParsers.literal;

import de.ialistannen.commandprocrastination.context.Context;
import de.ialistannen.commandprocrastination.parsing.SuccessParser;
import de.ialistannen.commandprocrastination.util.StringReader;
import java.util.Optional;

/**
 * A command finder.
 *
 * @param <C> the type of the context
 */
public class CommandFinder<C extends Context> {

  private CommandNode<C> root;
  private SuccessParser argumentSeparator;

  /**
   * Creates a new command finder with the given root.
   *
   * <p>Uses a space as the argument separator</p>
   *
   * @param root the root command node
   * @see #CommandFinder(CommandNode, SuccessParser)
   */
  public CommandFinder(CommandNode<C> root) {
    this(root, SuccessParser.wrapping(literal(" ")));
  }

  /**
   * Creates a new command finder with the given root.
   *
   * @param root the root command node
   * @param argumentSeparator the argument separator
   */
  public CommandFinder(CommandNode<C> root, SuccessParser argumentSeparator) {
    this.root = root;
    this.argumentSeparator = argumentSeparator;
  }

  /**
   * Finds the deepest matching command node, starting at the root.
   *
   * @param reader the string reader to use. Will be positioned after the last matching child
   * @return the deepest found command node. Will never be root, but may be descendant of it
   */
  public Optional<CommandChain<C>> find(StringReader reader) {
    return find(root, reader);
  }

  /**
   * Finds the deepest matching command node, starting at root.
   *
   * @param root the start node
   * @param reader the string reader to use. Will be positioned after the last matching child
   * @return the deepest found command node. Will never be root, but may be descendant of it
   */
  public Optional<CommandChain<C>> find(CommandNode<C> root, StringReader reader) {
    for (CommandNode<C> child : root.getChildren()) {
      // the success parser resets the position if parsing fails, so we don't need to
      // save it again
      boolean childMatches = child.getHeadParser().parse(reader);

      if (!childMatches) {
        continue;
      }

      int beforeArgument = reader.getPosition();
      boolean separatorParsed = argumentSeparator.parse(reader);

      CommandChain<C> chain = new CommandChain<>(child);

      // Nothing will follow, as there was no proper separator. Treat the rest as arguments and
      // end the descent here
      if (!separatorParsed) {
        return Optional.of(chain);
      }

      Optional<CommandChain<C>> childResult = find(child, reader);
      if (childResult.isPresent()) {
        chain.append(childResult.get());
        return Optional.of(chain);
      }

      // It was the last command, leave the separator, It is only consumed if it happens to be the
      // same as the command-argument separator
      reader.reset(beforeArgument);

      return Optional.of(chain);
    }

    return Optional.empty();
  }
}
