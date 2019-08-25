package de.ialistannen.commandprocrastination.command.tree.data;

/**
 * Contains a few default {@link CommandDataKey}s.
 */
public enum DefaultDataKey implements CommandDataKey {
  /**
   * Command usage. A String.
   */
  USAGE,
  /**
   * A short description. A String
   */
  SHORT_DESCRIPTION,
  /**
   * A long description. A String
   */
  LONG_DESCRIPTION,
  /**
   * The permission required for the command. A String
   */
  PERMISSION,
  /**
   * The identifier for the node. E.g. used by auto discovery.
   */
  IDENTIFIER;
}
