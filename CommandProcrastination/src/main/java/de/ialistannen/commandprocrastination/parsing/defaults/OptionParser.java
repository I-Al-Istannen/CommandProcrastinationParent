package de.ialistannen.commandprocrastination.parsing.defaults;

import de.ialistannen.commandprocrastination.parsing.AtomicParser;
import de.ialistannen.commandprocrastination.parsing.ParseException;
import de.ialistannen.commandprocrastination.util.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Tries a list of parsers until one matches, then returns the result of that parser.
 */
public class OptionParser<T> implements AtomicParser<T> {

  private List<AtomicParser<T>> underlying;

  /**
   * Creates a new option parser.
   */
  public OptionParser() {
    this.underlying = new ArrayList<>();
  }

  /**
   * Adds a new parser.
   *
   * @param other the parser
   * @return this parser
   */
  public OptionParser<T> or(AtomicParser<T> other) {
    underlying.add(other);
    return this;
  }

  @Override
  public T parse(StringReader input) throws ParseException {
    if (underlying.isEmpty()) {
      throw new ParseException(input, "No option given");
    }

    for (AtomicParser<T> parser : underlying) {
      int before = input.getPosition();

      try {
        return parser.parse(input);
      } catch (ParseException e) {
        input.reset(before);
      }
    }

    throw new ParseException(input, "Expected one of " + generateUsageNames());
  }

  private String generateUsageNames() {
    return underlying.stream()
        .flatMap(it -> it.getName().stream())
        .collect(Collectors.joining("|", "<", ">"));
  }

  @Override
  public Optional<String> getName() {
    return Optional.of(generateUsageNames());
  }
}
