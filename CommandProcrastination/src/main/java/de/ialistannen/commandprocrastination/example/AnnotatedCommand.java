package de.ialistannen.commandprocrastination.example;

import static de.ialistannen.commandprocrastination.parsing.defaults.StringParsers.greedyPhrase;
import static de.ialistannen.commandprocrastination.parsing.defaults.StringParsers.literal;
import static de.ialistannen.commandprocrastination.parsing.defaults.StringParsers.word;

import de.ialistannen.commandprocrastination.autodiscovery.ActiveCommand;
import de.ialistannen.commandprocrastination.command.tree.CommandNode;
import de.ialistannen.commandprocrastination.context.Context;
import de.ialistannen.commandprocrastination.parsing.SuccessParser;

@ActiveCommand(name = "test")
public class AnnotatedCommand extends CommandNode<Context> {

  /**
   * Creates a new command node.
   */
  public AnnotatedCommand() {
    super(
        context -> System.out.println(context.shift(word())),
        SuccessParser.wrapping(literal("test"))
    );
  }

  @ActiveCommand(name = "child", parentClass = AnnotatedCommand.class)
  public static class AnnotatedChild extends CommandNode<Context> {

    /**
     * Creates a new command node.
     */
    public AnnotatedChild() {
      super(
          context -> System.out.println("Child: " + context.shift(greedyPhrase())),
          SuccessParser.wrapping(literal("child"))
      );
    }
  }

  @ActiveCommand(name = "sibling", parentClass = AnnotatedChild.class)
  public static class AnnotatedSibling extends CommandNode<Context> {

    /**
     * Creates a new command node.
     */
    public AnnotatedSibling() {
      super(
          context -> System.out.println("Sibling: " + context.shift(greedyPhrase())),
          SuccessParser.wrapping(literal("sibling"))
      );
    }
  }
}
