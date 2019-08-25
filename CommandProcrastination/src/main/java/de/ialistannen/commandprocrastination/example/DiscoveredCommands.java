package de.ialistannen.commandprocrastination.example;

import static de.ialistannen.commandprocrastination.parsing.defaults.StringParsers.literal;

import de.ialistannen.commandprocrastination.autodiscovery.CommandDiscovery;
import de.ialistannen.commandprocrastination.command.execution.AbnormalCommandResultException;
import de.ialistannen.commandprocrastination.command.execution.CommandException;
import de.ialistannen.commandprocrastination.command.execution.CommandExecutor;
import de.ialistannen.commandprocrastination.command.execution.CommandNotFoundException;
import de.ialistannen.commandprocrastination.command.tree.CommandChain;
import de.ialistannen.commandprocrastination.command.tree.CommandFinder;
import de.ialistannen.commandprocrastination.command.tree.CommandNode;
import de.ialistannen.commandprocrastination.command.tree.data.DefaultDataKey;
import de.ialistannen.commandprocrastination.context.Context;
import de.ialistannen.commandprocrastination.parsing.ParseException;
import de.ialistannen.commandprocrastination.parsing.SuccessParser;
import de.ialistannen.commandprocrastination.util.StringReader;
import java.util.Scanner;

public class DiscoveredCommands {

  public static void main(String[] args) {
    CommandNode<Context> root = new CommandDiscovery().findCommands();

    CommandFinder<Context> finder = new CommandFinder<>(root);
    CommandExecutor<Context> executor = new SimpleExecutor(
        finder,
        SuccessParser.wrapping(literal(" "))
    );

    Scanner reader = new Scanner(System.in);
    String read = reader.nextLine();
    while (!read.equals("exit")) {
      try {
        executor.execute(read);
      } catch (CommandNotFoundException e) {
        System.err.println("Command not found");
        System.err.println("Usage: " + e.getResult().getChain().buildUsage().trim());
      } catch (ParseException | CommandException | AbnormalCommandResultException e) {
        System.err.println(e.getMessage());
      }
      read = reader.nextLine();
    }
  }

  private static class SimpleExecutor extends CommandExecutor<Context> {

    public SimpleExecutor(CommandFinder<Context> finder,
        SuccessParser commandArgumentSeparator) {
      super(finder, commandArgumentSeparator);
    }

    @Override
    protected Context createContext(StringReader input, CommandNode<Context> node) {
      return new Context(input, node);
    }

    @Override
    protected void handleAbnormalResult(CommandChain<Context> chain,
        AbnormalCommandResultException e) {
      if (e.getKey() == DefaultDataKey.USAGE) {
        System.err.println("Usage: " + chain.buildUsage());
      } else {
        super.handleAbnormalResult(chain, e);
      }
    }
  }

}
