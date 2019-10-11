# About

This is yet another command library that is independent from the actual underlying program (Discord, IRC, Minecraft, ...).

## Features

* Annotation driven autodiscovery or java based half-fluent API

* Subcommand support

* Argument parsing is done in *java code* not via method parameters. This is much more flexible and allows for custom parsing logic.

* Extensible argument system: Arguments are parsed by "shifting" them from the context. All you need is a new `ArgumentParser` and you are good to go. Example:
  `String name = context.shift(greedyPhrase());`, `Player player = context.shift(onlinePlayer(server)`, etc.  
  `greedyPhrase` and `onlinePlayer` are static methods imported statically that return an ArgumentParser.

* Exception driven parsing flow allows for planning for the happy path -- should the unhappy path be hit `shift` throws an exception 
  which can be converted to an error message in your executor

* Commands are matched by their head `SuccessParser`. This allows command keywords to not be a keyword *but any parser you can imagine*.
  A simple usae case are commands that have multiple words as their keyword: `fetch in package` could be the keyword for a command,
  but something like `<any number>` or `<online player>` can also be used as a keyword.

* Auto-generated usage is available for subcommands and can be used as a fallback or augmentation for your messages

* Command nodes can be annotated with custom data and a few keys for permissions, usage and so on are provided.
  These can be read and used by anything with access to the command nodes but are especially nice for help commands or permission checks.

# Architecture overview

## Classes

### The `AtomicParser`
An atomic parser is a functional interface taking a `StringReader` and returning a parsed value.
Instances of this interface take care of parsing numbers, names and any other argument you can imagine.
Their primary use is in conjunction with a `Context`.

### The `SuccessParser`
This is an atomic parser that wraps around a parser and returns `true` if the underlying parser is able to parse the input.
If the underlying parser throws an exception, the success parser returns `false`.

### The `Command`
A command is a simple functional interface that has an `execute` method taking a context.
This is the actual action that is executed when running a command.

### The `CommandNode`
All command nodes are laid out in a tree structure. Each single command node has a `SuccessParser` which acts as the command keyword and a
`Command` that is associated with it.  
Furthermore you can store arbitrary data implementing `CommandDataKey` (a few default keys are provided in `DefaultDataKey`).
This data can be used to implement usage messages, descriptions, permissions and more.

### The `CommandFinder`
As commands are laid out in a tree structure (so subcommands are children), you need a way to take some input and walk it along the tree, until you hit the final node:
`first second third` should return the command `third`, if the command tree is `first->second->third`.  
The command finder takes care of that for you, as it walks down the tree of matching children and returns the deepest matching one.
Additionally it is able to build a usage message (and prefix) based on the command hierarchy, albeit not the dynamic command arguments.

### The `CommandExecutor`
You also need some way to get input, find the command for it, execute it and handle the result.  
This is the job of the command executor. It takes care of creating a context instance to pass to the `Command`,
catches any exceptions and abnormal command exists and can be modified to display a helpful message in those cases.

### The `GlobalContext` and the `RequestContext`
Many commands will need some information about the global state (databases, configurations, etc.) and about the current request (the message that triggered it, the user, etc.).
The global state is provided by the `GlobalContext` and is your main point for dependency injection. Your commands do not need to take take of anything, as they get passed a context on every request which they can query for databases et al.  
The request context has the more modest goal of providing information about the current request that triggered the command execution.
It is helpful for referring back to the user, sending messages in the same channel and many more things.

### The `CommandDiscovery`
Specifying and wiring up complex command hierarchies by hand is tedious and error prone, so this library has an alternative:
Any class annotated with `@ActiveCommand` that extends `CommandNode` will be discovered by the `CommandDiscovery`, if you invoke it.
You can and must specify some identifier for the node and can specify the parent either by its class (if all of your commands are auto discovered) or its name.


## Argument fetching workflow
The library provides a set of default atomic parsers for Strings, doubles, integers and other numbers.
Those parses can be passed to `GlobalContext#shift(AtomicParser)` to parse an argument.
To parse the name and age from this command: `test <name> <age>` you would do:
```java
String name = context.shift(phrase()); // phrase is from StringParsers and parses an optionally quoted word
int age = conext.shift(intWithinRage(1, 130)); // from IntegerParsers. Throws an exception exiting the command if the integer is out of bounds or invalid
```

## Slightly more involved command creating tags
```java
@ActiveCommand(name = "tag-create", parentClass = TagCommand.class)
public class TagCreateCommand extends CommandNode<CommandContext> {

  public TagCreateCommand() {
    // set my head parser - just the keyword "create"
    super(SuccessParser.wrapping(literal("create")));
    // If executed it should run the "execute" method
    setCommand(this::execute);

    // This command has a permission: "tag.create"
    setData(DefaultDataKey.PERMISSION, "tag.create");
  }

  private void execute(CommandContext context) throws ParseException {
    // The name is a single word or a quoted phrase
    String name = context.shift(phrase());
    // The description as well
    String description = context.shift(phrase());
    // The content is whatever input is left, but not empty (greedyPhrase)
    String value = context.shift(greedyPhrase());

    //Build a new tag
    MessageTag tag = ImmutableMessageTag.builder()
        .keyword(name)
        .description(description)
        .value(value)
        // Simple DI: The specialized global context contains a request context with a user
        .creator(context.getRequestContext().getUser().getIdLong())
        .build();

    // Simple DI: The specialized global context contains a database
    context.getDatabase().getTagDao().addOrUpdate(tag);


    // Simple DI: The specialized global context contains a message sender
    context.getMessageSender().sendMessage(
        SimpleMessage.success("Added the tag " + name),
        context.getRequestContext().getChannel()
    );
  }
}
```

## Command with mixed discovery and manual adding
```java
// command is auto discovered and a subcommand of the "PrefixedBaseCommand"
// The "PrefixedBaseCommand" just enforces a prefix like "!" before all commands
@ActiveCommand(name = "ping", parentClass = PrefixedBaseCommand.class)
public class PingCommand extends CommandNode<CommandContext> {

  public PingCommand() {
    // React to a simple keyword
    super(SuccessParser.wrapping(literal("ping")));
    // It can not be invoked directly as only the child commands make sense.
    // Always show the usage when it is called.
    setCommand(context -> {
      throw AbnormalCommandResultException.showUsage();
    });

    // Manual addition of a sub command. A new class with "@ActiveCommand" would have worked, but is
    // just more verbose
    addSubCommand() // helper method in "CommandNode" to ease creating a subcommand
        // The head parser - a literal string "error"
        .head("error")
        // Adds some data. Here a description
        .data(DefaultDataKey.SHORT_DESCRIPTION, "A message with error level")
        // the command to run when it is called
        .executes(context -> sendMessage(context, MessageCategory.ERROR))
        // the subcommand was succssfully created
        .finish();
    addSubCommand()
        .head("information")
        .data(DefaultDataKey.SHORT_DESCRIPTION, "A message with information level")
        .executes(context -> sendMessage(context, MessageCategory.INFORMATION))
        .finish();
    addSubCommand()
        .head("success")
        .data(DefaultDataKey.SHORT_DESCRIPTION, "A message with success level")
        .executes(context -> sendMessage(context, MessageCategory.SUCCESS))
        .finish();
    addSubCommand()
        .head("none")
        .data(DefaultDataKey.SHORT_DESCRIPTION, "A message with no level")
        .executes(context -> sendMessage(context, MessageCategory.NONE))
        .finish();
  }

  private void sendMessage(CommandContext context, MessageCategory category) {
    context.getMessageSender().sendMessage(
        new SimpleMessage(category, "Pong!"),
        context.getRequestContext().getChannel()
    );
  }
}
```
