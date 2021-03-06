package de.ialistannen.commandprocrastination.context;

import de.ialistannen.commandprocrastination.parsing.AtomicParser;
import de.ialistannen.commandprocrastination.parsing.ParseException;
import java.util.List;
import java.util.Optional;

/**
 * The context for command parsing.
 */
public class GlobalContext {

  private RequestContext requestContext;

  public GlobalContext(RequestContext requestContext) {
    this.requestContext = requestContext;
  }

  /**
   * Returns the request context.
   *
   * @return the request context
   */
  public RequestContext getRequestContext() {
    return requestContext;
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
    return getRequestContext().shift(parser);
  }

  /**
   * Uses the given parser to extract an argument. Also reads all trailing whitespace, after the
   * parser is done.
   *
   * @param parser the parser to use
   * @param <T> the type of the resulting argument
   * @return the parsed argument or an empty optional if an error occurred
   * @see #shift(AtomicParser)
   */
  public <T> Optional<T> shiftOptionally(AtomicParser<T> parser) {
    return getRequestContext().shiftOptionally(parser);
  }

  /**
   * Uses the given parsers to extract an argument. All parsers are tried one after the other, in
   * order. Also reads all trailing whitespace, after the parser is done.
   *
   * @param parser the parsers to use
   * @param <T> the type of the resulting argument
   * @return the parsed argument
   * @throws ParseException if an error occurred. The last exception will be rethrown
   */
  public <T> T shiftAny(List<AtomicParser<T>> parser) throws ParseException {
    return getRequestContext().shiftAny(parser);
  }
}
