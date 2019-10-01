package de.ialistannen.commandprocrastination.command.execution;

import de.ialistannen.commandprocrastination.parsing.ParseException;
import de.ialistannen.commandprocrastination.util.StringReader;

public class NoSeparatorException extends ParseException {

  public NoSeparatorException(StringReader reader) {
    super(reader, "No separator after command");
  }
}
