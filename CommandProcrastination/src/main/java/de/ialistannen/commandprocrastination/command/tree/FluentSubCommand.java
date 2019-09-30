package de.ialistannen.commandprocrastination.command.tree;

import static de.ialistannen.commandprocrastination.parsing.defaults.StringParsers.literal;

import de.ialistannen.commandprocrastination.command.Command;
import de.ialistannen.commandprocrastination.context.GlobalContext;
import de.ialistannen.commandprocrastination.parsing.AtomicParser;
import de.ialistannen.commandprocrastination.parsing.SuccessParser;

public class FluentSubCommand<C extends GlobalContext> {

  private CommandNode<C> target;
  private SuccessParser headParser;
  private Command<C> command;

  public FluentSubCommand(CommandNode<C> target) {
    this.target = target;
  }

  public FluentSubCommand() {
  }

  public void setTarget(CommandNode<C> target) {
    this.target = target;
  }

  public CommandNode<C> finish() {
    CommandNode<C> child = new CommandNode<>(command, headParser);
    target.addChild(child);
    return target;
  }

  public FluentSubCommand<C> head(SuccessParser parser) {
    this.headParser = parser;
    return this;
  }

  public FluentSubCommand<C> head(AtomicParser<?> parser) {
    return head(SuccessParser.wrapping(parser));
  }

  public FluentSubCommand<C> head(String literal) {
    return head(literal(literal));
  }

  public FluentSubCommand<C> executes(Command<C> command) {
    this.command = command;
    return this;
  }
}
