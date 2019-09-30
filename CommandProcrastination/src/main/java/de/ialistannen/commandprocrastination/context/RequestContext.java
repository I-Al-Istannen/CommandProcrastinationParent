package de.ialistannen.commandprocrastination.context;

import de.ialistannen.commandprocrastination.command.tree.CommandNode;
import de.ialistannen.commandprocrastination.parsing.AtomicParser;
import de.ialistannen.commandprocrastination.parsing.ParseException;
import de.ialistannen.commandprocrastination.util.StringReader;
import java.util.List;

/**
 * Some context that is only valid for a single request. E.g. metadata or specific objects like
 * Messages or Players, etc.
 */
public class RequestContext {

  private StringReader reader;
  private CommandNode<?> finalNode;

  public RequestContext() {
  }

  /**
   * Creates a new request context with the given reader and node.
   *
   * @param reader the reader
   * @param finalNode the final node
   */
  RequestContext(StringReader reader, CommandNode<?> finalNode) {
    this.reader = reader;
    this.finalNode = finalNode;
  }

  /**
   * Sets the string reader.
   *
   * @param reader the string reader
   */
  public void setReader(StringReader reader) {
    this.reader = reader;
  }

  /**
   * Sets the final node.
   *
   * @param finalNode the final node
   */
  public void setFinalNode(CommandNode<?> finalNode) {
    this.finalNode = finalNode;
  }

  /**
   * Uses the given parser to extract an argument. Also reads all trailing whitespace, after the
   * parser is done.
   *
   * @param parser the parser to use
   * @param <T> the type of the resulting argument
   * @return the parsed argument
   * @throws ParseException if an error occurred
   */
  public <T> T shift(AtomicParser<T> parser) throws ParseException {
    T parsed = parser.parse(reader);
    reader.readWhile(Character::isWhitespace);
    return parsed;
  }

  /**
   * Uses the given parser to extract an argument.
   *
   * @param parser the parser to use
   * @param <T> the type of the resulting argument
   * @return the parsed argument
   * @throws ParseException if an error occurred. The last exception will be rethrown
   */
  public <T> T shiftAny(List<AtomicParser<T>> parser) throws ParseException {
    if (parser.isEmpty()) {
      throw new IllegalArgumentException("The parser list may not be empty!");
    }

    ParseException exception = null;
    for (AtomicParser<T> atomicParser : parser) {
      try {
        return atomicParser.parse(reader);
      } catch (ParseException e) {
        exception = e;
      }
    }

    throw exception;
  }

  /**
   * Returns the underlying string reader.
   *
   * @return the string reader
   */
  public StringReader getReader() {
    return reader;
  }

  /**
   * Returns the final node.
   *
   * @param <C> t he type of the context
   * @return the final node
   */
  public <C extends GlobalContext> CommandNode<C> getFinalNode() {
    @SuppressWarnings("unchecked")
    CommandNode<C> node = (CommandNode<C>) this.finalNode;
    return node;
  }

}
