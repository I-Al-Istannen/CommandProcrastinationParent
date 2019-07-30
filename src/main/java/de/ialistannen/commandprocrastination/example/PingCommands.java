package de.ialistannen.commandprocrastination.example;

import static de.ialistannen.commandprocrastination.parsing.defaults.StringParsers.greedyPhrase;
import static de.ialistannen.commandprocrastination.parsing.defaults.StringParsers.literal;

import de.ialistannen.commandprocrastination.command.Command;
import de.ialistannen.commandprocrastination.command.execution.CommandException;
import de.ialistannen.commandprocrastination.command.execution.CommandExecutor;
import de.ialistannen.commandprocrastination.command.execution.CommandNotFoundException;
import de.ialistannen.commandprocrastination.command.tree.CommandFinder;
import de.ialistannen.commandprocrastination.command.tree.CommandNode;
import de.ialistannen.commandprocrastination.command.tree.data.DefaultDataKey;
import de.ialistannen.commandprocrastination.context.Context;
import de.ialistannen.commandprocrastination.parsing.ParseException;
import de.ialistannen.commandprocrastination.parsing.SuccessParser;
import de.ialistannen.commandprocrastination.util.StringReader;
import java.time.LocalTime;
import java.util.Scanner;
import java.util.function.Consumer;

public class PingCommands {

  public static void main(String[] args) {
    CommandNode<PingContext> root = new CommandNode<>(Command.nop(), SuccessParser.alwaysTrue());

    CommandNode<PingContext> pingBase = new CommandNode<>(
        Command.nop(), SuccessParser.wrapping(literal("ping"))
    );
    CommandNode<PingContext> pingInfo = new CommandNode<>(
        context -> context.getPrinter().accept("Info!"),
        SuccessParser.wrapping(literal("info"))
    );
    CommandNode<PingContext> pingTime = new CommandNode<>(
        context -> context.getPrinter().accept(LocalTime.now().toString()),
        SuccessParser.wrapping(literal("time"))
    );
    CommandNode<PingContext> pingEcho = new CommandNode<>(
        context -> context.getPrinter().accept("Pong: " + context.shift(greedyPhrase())),
        SuccessParser.wrapping(literal("echo"))
    );

    pingBase.addChild(pingInfo);
    pingBase.addChild(pingTime);
    pingBase.addChild(pingEcho);

    root.addChild(pingBase);

    CommandFinder<PingContext> finder = new CommandFinder<>(root);
    PingExecutor pingExecutor = new PingExecutor(
        finder,
        SuccessParser.wrapping(literal(" ")),
        s -> System.out.println("INFO: " + s)
    );

    Scanner scanner = new Scanner(System.in);
    String read = null;
    while (!"exit".equals(read)) {
      read = scanner.nextLine();
      try {
        pingExecutor.execute(read);
      } catch (ParseException e) {
        System.err.println("Syntax error: " + e.getMessage());
      } catch (CommandNotFoundException e) {
        System.err.println("Command not found!");
      } catch (CommandException e) {
        System.err.println("Got: " + e.getMessage());
      }
    }
  }

  private static class PingExecutor extends CommandExecutor<PingContext> {

    private Consumer<String> printer;

    public PingExecutor(CommandFinder<PingContext> finder, SuccessParser commandArgumentSeparator,
        Consumer<String> printer) {
      super(finder, commandArgumentSeparator);
      this.printer = printer;
    }

    @Override
    protected PingContext createContext(StringReader input, CommandNode<PingContext> node) {
      return new PingContext(input, node, printer);
    }
  }

  private static class PingContext extends Context {

    private Consumer<String> printer;

    public PingContext(StringReader reader, CommandNode<PingContext> node,
        Consumer<String> printer) {
      super(reader, node);
      this.printer = printer;
    }

    public Consumer<String> getPrinter() {
      return printer;
    }
  }
}
