# About

This is yet another command library that is independent from the actual underlying program (Discord, IRC, Minecraft, ...).

# TOC
- [About](#about)
  * [Features](#features)
- [Request handling -- An overview](#request-handling-an-overview)
- [Getting started](#getting-started)
- [Class overview](#class-overview)
    + [The `AtomicParser`](#the-atomicparser)
    + [The `SuccessParser`](#the-successparser)
    + [The `Command`](#the-command)
    + [The `CommandNode`](#the-commandnode)
    + [The `CommandFinder`](#the-commandfinder)
    + [The `CommandExecutor`](#the-commandexecutor)
    + [The `GlobalContext` and the `RequestContext`](#the-globalcontext-and-the-requestcontext)
    + [The `CommandDiscovery`](#the-commanddiscovery)
  * [Argument fetching workflow](#argument-fetching-workflow)
  * [Creating your own context](#creating-your-own-context)
  * [Creating your own `CommandExecutor`](#creating-your-own-commandexecutor)
- [Examples](#examples)
  * [Slightly more involved command creating tags](#slightly-more-involved-command-creating-tags)
  * [Command with mixed discovery and manual adding](#command-with-mixed-discovery-and-manual-adding)
  * [A help command for discord](#a-help-command-for-discord)

## Features

* **Annotation driven autodiscovery** or **java based** half-fluent **API**

* **Subcommand support**

* Argument parsing is done in *java code*, not via method parameters. This is much more flexible and allows for custom parsing logic.

* **Extensible argument/parameter system**  
  As opposed to many other command frameworks, command arguments are not defined in the method signature or auxiliary structures like annotations.
  Instead, each command will receive a small context which stores the underlying input string and can be modified and consumed using `AtomicParser`s.
  These parsers take a StringReader, read whatever they need, and return the result. Quite a few of those are already provided out of the box and you can easily write your own.
  
  If that didn't make much sense, have a look at some examples.
  All of those use statically imported methods that return an `ArgumentParser`:  
  ```java
  String name = context.shift(greedyPhrase()); // greedyPhrase is an inbuilt parser
  Player player = context.shift(onlinePlayer(server)); // give the parser some context
  ```

* **Exception driven parsing flow**  
  This allows for planning for the happy path -- should an error occur, `shift` will abort with an exception.
  How you handle those exceptions is up to you.
  Sending error messages, usages or staying silent are a few ways to react.
  All of this can be done centrally in your `CommandExecutor`.

* **Flexible command keywords**  
  Commands are matched by an arbitrary parser, which allows keywords to not only be a simple string *but any parser you can imagine*.  
  Using this you can easily deal with aliases, translated commands or even commands that match based on some outside context.  
  A very simple use case are commands triggered by an entire sentence, like `fetch in package`.
  Depending on the user experience you want, this could be infinitely more pleasant than enforcing weird capitalization or delimiting rules.  

* **Auto-generated usage**  
  Due to the dynamic structure this is only available for the subcommand structure and can be used as a fallback or augmentation for your own help or error messages.  
  To make that distinction a bit more clear:  
  If you have a command `parent` with two subcommands `a` and `b`, the library can generate a usage of the form `parent [a|b]`, but it can *not incorporate the arguments* `a`, `b` or the `parent` require.  
  However, this isn't too much of a hindrance -- just provide your own messages for that and build the usage as you see fit.

* **Custom data on command nodes**  
  You can store arbitrary key-value pairs on command nodes, and a few keys are already provided.
  These can be read and used by anything with access to the command nodes but are especially useful for writing a help command, as they can store descriptions or usages, but are also useful for permission checks.

# Request handling -- An overview
1. The user enters a command. How this is done is dependent on the system you use this library with.
2. You create a fitting `RequestContext` that contains everything you need about the request.
  If you are writing a discord bot, you will want to store the channel, message and maybe the user in there.  
  If you are writing a Minecraft plugin, you'll probably want to just store the player.
3. You call the `execute` method on your `CommandExecutor` and give it the user input and the created RequestContext.
4. Internally the executor will now call the `CommandFinder#find` method to figure out what command node should handle the request.
  This is done by taking the root command, going over every sub command and checking if their head parser matches the input.
  If this is the case, we recursively call `find` with this sub command as the new root and continue on.
  The exact find logic is a bit more involved, but that is the gist of it.  
  If we can not parse another separator after a child command, we are done. At this point, we stop the recursive descent and return the command.
5. If a command was found in the step above, the executor creates a *global* context using the passed request context and its abstract `createContext` method.
  With that context we are finally ready to invoke the found command and do just that.
6. The called command can now use the global context to parse arguments, use dependencies and perform whatever it was created for.

# Getting started
1. Create your own contexts and CommandExecutor
2. Add all your commands and create a `CommandFinder`.
   If you want to auto-discover them, use the `CommandDiscovery`:
   ```java
   CommandNode<CommandContext> rootCommand = new CommandDiscovery().findCommands(
       createBaseContext() // you need a global context here, as the commands can have a constructor
   );
   commandFinder = new CommandFinder<>(rootCommand);
   ```
3. Create a command executor using that command finder:
   ```java
   executor = new YourExecutor(commandFinder, any needed dependencies);
   ```
4. Create a request context in your command listener and start executing commands:
   ```java
   YourRequestContext context = new YourRequestContext(
      // any needed dependencies. E.g. the user, channel, message, ...
   );
   executor.execute(content, context);
   ```

# Class overview

### The `AtomicParser`
An atomic parser is a functional interface taking a `StringReader` and returning a parsed value.
Instances of this interface take care of parsing numbers, names and any other argument you can imagine.  
They are mainly used as a parameter in a context's `shift` method or as a head parser for a command.

### The `SuccessParser`
This is an atomic parser that wraps around a parser and returns `true` if the underlying parser is able to parse the input.
If the underlying parser throws an exception the success parser returns `false` *and resets the StringReader* it read from.  
Mostly due to the last property, it is the type required for a command's head parser.  
Instances are mostly created by using `SuccessParser#wrapping(AtomicParser)`.

### The `Command`
A command is a simple functional interface that has an `execute` method taking a context.
It is the actual action that is executed when running a command.

### The `CommandNode`
All command nodes are laid out in a tree structure. Each single command node has a `SuccessParser` which acts as the command keyword (the head parser) and a `Command` that is associated with it.

Furthermore you can store arbitrary key-value pairs if the key implements `CommandDataKey` (a few default keys are provided in `DefaultDataKey`).
This data can be used to implement usage messages, descriptions, permissions and more.

### The `CommandFinder`
As commands are laid out in a tree structure (so subcommands are children),
you need a way to take some input and walk along the tree:  
`first second third` should return the command `third` if the command tree is `first->second->third`.

The command finder takes care of that for you, as it walks down the tree, checks which children match and finally returns the deepest one.  
Additionally it is able to build a usage message (and prefix) based on the command hierarchy, albeit not the dynamic command arguments.

### The `CommandExecutor`
You also need some way to get input, find the command for it, execute it and handle the result.  
This is the job of the command executor. It takes care of creating a context instance to pass to the `Command`,
catches any exceptions and abnormal command exits and can be modified to display a helpful message in those cases.

To do its job it requires a `CommandFinder`, as it delegates the searching.

### The `GlobalContext` and the `RequestContext`
Many commands will need some information about the global state (databases, configurations, etc.) and about the current request (for example the message that triggered it or the user).

The global state is provided by the `GlobalContext` and is your main point for dependency injection.
Your commands do not need to create everything themselves, be created by a DI framework like Guice or Dagger or have a *really* large constructor.
Instead they get passed a context on every request which they can query for databases and any other dependencies.  

The `RequestContext` has a more modest goal: Providing information about the current request that triggered the command execution.
It is helpful for referring back to the user, sending messages in the same channel,...

### The `CommandDiscovery`
Specifying and wiring up complex command hierarchies by hand is tedious and error prone, so we offer an alternative:
Any class annotated with `@ActiveCommand` that extends `CommandNode` on the ClassPath will be discovered by the `CommandDiscovery`.
You can - and must - specify some identifier for the node and optionally the parent either by its class (if all of your commands are auto discovered) or its name.

To use the auto-discovery, just create an instance of `CommandDiscovery` and call `findCommands`. As each command is permitted to take a `GlobalContext` (your subytype of it, to be more specific) as its lone constructor parameter, you need to pass one to `findCommands`.  
This context can be used by the commands to create resources they will need across all requests or configure their internal state.


## Argument fetching workflow
The library provides a set of default atomic parsers for Strings, doubles, integers and other numbers.
Those parses can be passed to `GlobalContext#shift(AtomicParser)` to parse an argument.
To parse the name and age from `test <name> <age>`, do:
```java
String name = context.shift(phrase()); // phrase is from StringParsers and parses an optionally quoted word
int age = conext.shift(intWithinRage(1, 130)); // from IntegerParsers. Throws an exception exiting the command if the integer is out of bounds or invalid
```

## Creating your own context
Just subclass `GlobalContext` and `RequestContext` when needed and add the fields you want. For example:
```java
public class CommandContext extends GlobalContext {

  private Toml config;
  private Database database;

  /**
   * Creates a new command context.
   *
   * @param config the config
   * @param database the database
   */
  public CommandContext(JdaRequestContext requestContext, Toml config, Database database) {
    super(requestContext);
    this.config = config;
    this.database = database;
  }

  // Overriden to specialize the return type to a JdaRequestContext
  @Override
  public JdaRequestContext getRequestContext() {
    return requestContext;
  }

  public Toml getConfig() {
    return config;
  }

  public Database getDatabase() {
    return database;
  }

  /**
   * The context for a single request.
   */
  public static class JdaRequestContext extends RequestContext {

    private Message message;
    private User user;
    private Guild guild;
    private MessageChannel channel;

    JdaRequestContext(Message message, User user, Guild guild) {
      this.message = message;
      this.user = user;
      this.guild = guild;
      this.channel = message.getChannel();
    }

    public Message getMessage() {
      return message;
    }

    public MessageChannel getChannel() {
      return channel;
    }

    public User getUser() {
      return user;
    }

    public Guild getGuild() {
      return guild;
    }
  }

}
```

## Creating your own `CommandExecutor`
Just subclass `CommandExecutor`. For example:
```java
class JdaExecutor extends CommandExecutor<CommandContext, JdaRequestContext> {

  private final Toml config;
  private final Database database;

  JdaExecutor(CommandFinder<CommandContext> finder, Toml config, Database database) {
    // the argument separator is a space
    super(finder, SuccessParser.wrapping(literal(" ")));
    this.config = config;
    this.database = database;
  }

  @Override
  protected CommandContext createContext(JdaRequestContext requestContext) {
    return new CommandContext(requestContext, config, database);
  }
}
```

# Examples

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
    // more verbose
    addSubCommand() // helper method in "CommandNode" to ease creating a subcommand
        // The head parser - a literal string "error"
        .head("error")
        // Adds some data. Here a description
        .data(DefaultDataKey.SHORT_DESCRIPTION, "A message with error level")
        // the command to run when it is called
        .executes(context -> sendMessage(context, MessageCategory.ERROR))
        // add the subcommand
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


## A help command for discord

This command also fetches usages and descriptions from a messages config.

```java
@ActiveCommand(name = "help", parentClass = PrefixedBaseCommand.class)
public class HelpCommand extends CommandNode<CommandContext> {

  public HelpCommand() {
    super(SuccessParser.wrapping(literal("help")));
    setCommand(this::execute);
  }

  private void execute(CommandContext context) throws ParseException {
    String path = context.shift(greedyPhrase());

    FindResult<CommandContext> foundCommands = context.getCommandFinder()
        // Remove the need to specify the prefix
        .find(getParent().orElseThrow(), new StringReader(path));

    CommandNode<CommandContext> finalNode = foundCommands.getChain().getFinalNode();

    ComplexMessage message = new ComplexMessage(MessageCategory.INFORMATION);

    finalNode.getOptionalData(DefaultDataKey.IDENTIFIER).ifPresent(name ->
        message.editEmbed(it -> it.setTitle(name.toString()))
    );

    finalNode.getHeadParser().getName().ifPresent(name ->
        message.editEmbed(it -> it.addField("Keyword", '`' + name + '`', true))
    );

    finalNode.getOptionalData(DefaultDataKey.USAGE)
        .map(usage -> "`" + usage + "`")
        .or(() -> fetchFromMessages("usage", context, finalNode))
        .or(() -> Optional.of("*(approx)* `" + foundCommands.getChain().buildUsage() + "`"))
        .ifPresent(usage ->
            message.editEmbed(it -> it.addField("Usage", usage, true))
        );

    finalNode.getOptionalData(DefaultDataKey.SHORT_DESCRIPTION)
        .or(() -> fetchFromMessages("short-description", context, finalNode))
        .ifPresent(desc ->
            message.editEmbed(it -> it.addField("Short description", desc.toString(), true))
        );
    finalNode.getOptionalData(DefaultDataKey.LONG_DESCRIPTION)
        .or(() -> fetchFromMessages("long-description", context, finalNode))
        .ifPresent(desc ->
            message.editEmbed(it -> it.setDescription(desc.toString()))
        );

    finalNode.getOptionalData(DefaultDataKey.PERMISSION).ifPresent(perm ->
        message.editEmbed(it -> it.addField("Permission", "`" + perm + "`", true))
    );

    context.getMessageSender().sendMessage(
        message, context.getRequestContext().getChannel()
    );
  }

  private Optional<String> fetchFromMessages(String restPath, CommandContext commandContext,
      CommandNode<CommandContext> node) {
    if (!node.hasOptionalData(DefaultDataKey.IDENTIFIER)) {
      return Optional.empty();
    }
    String identifier = node.<String>getOptionalData(DefaultDataKey.IDENTIFIER).orElseThrow();
    String lookupPath = "commands." + identifier + "." + restPath;

    return commandContext.getMessages().trOptional(lookupPath);
  }
}
```
