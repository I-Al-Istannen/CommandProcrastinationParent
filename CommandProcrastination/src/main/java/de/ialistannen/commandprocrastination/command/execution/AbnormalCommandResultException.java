package de.ialistannen.commandprocrastination.command.execution;

import de.ialistannen.commandprocrastination.command.tree.data.DefaultDataKey;

/**
 * An abnormal command result with a status code.
 */
public class AbnormalCommandResultException extends RuntimeException {

  private Object key;

  /**
   * Creates a new exception.
   *
   * @param key the key
   */
  public AbnormalCommandResultException(Object key) {
    this.key = key;
  }

  /**
   * Returns the key for this result.
   *
   * @return the key for this result, cast to whatever you assign it to
   */
  public <T> T getKey() {
    @SuppressWarnings("unchecked")
    T t = (T) this.key;
    return t;
  }

  /**
   * Returns the key for this result.
   *
   * @param type the type of the key. A type token
   * @return the key for this result, cast to the type token
   */
  public <T> T getKey(Class<T> type) {
    return type.cast(key);
  }

  /**
   * Throws an {@link AbnormalCommandResultException} that indicates that the usage should be
   * shown.
   *
   * <p><br>This is a normal exception with {@link DefaultDataKey#USAGE} as key</p>>
   *
   * @return the exception
   */
  public static AbnormalCommandResultException showUsage() {
    throw new AbnormalCommandResultException(DefaultDataKey.USAGE);
  }
}
