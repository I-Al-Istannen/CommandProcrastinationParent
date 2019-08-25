package de.ialistannen.commandprocrastination.util;

import java.util.function.Consumer;

/**
 * A classic functional either value, which is either the left or right value.
 *
 * <p><br>Use one of the static factory methods to instantiate this class.</p>
 *
 * @param <E> the type of the left value (typically an error)
 * @param <T> the type of the right value
 */
public final class Either<E, T> {

  private E left;
  private T right;

  /**
   * Creates a new either value.
   *
   * @param left the left value
   * @param right the right value
   */
  private Either(E left, T right) {
    this.left = left;
    this.right = right;
  }

  /**
   * Accepts the right value, if it exists. Otherwise passes the left value on to the left
   * consumer.
   *
   * @param rightConsumer the consumer for the right item
   * @param leftConsumer the consumer for the left item
   */
  public void acceptOrElse(Consumer<T> rightConsumer, Consumer<E> leftConsumer) {
    if (right != null) {
      rightConsumer.accept(right);
    } else {
      leftConsumer.accept(left);
    }
  }

  /**
   * Creates a new either value with only the left value given.
   *
   * @param value the value
   * @param <E> the type of the left side
   * @param <T> the type fo the right side
   * @return the created either value
   */
  public static <E, T> Either<E, T> left(E value) {
    return new Either<>(value, null);
  }

  /**
   * Creates a new either value with only the right value given.
   *
   * @param value the value
   * @param <E> the type of the left side
   * @param <T> the type fo the right side
   * @return the created either value
   */
  public static <E, T> Either<E, T> right(T value) {
    return new Either<>(null, value);
  }
}
