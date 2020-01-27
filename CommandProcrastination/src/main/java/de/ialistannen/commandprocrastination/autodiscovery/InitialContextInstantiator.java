package de.ialistannen.commandprocrastination.autodiscovery;

import de.ialistannen.commandprocrastination.command.tree.CommandNode;
import de.ialistannen.commandprocrastination.context.GlobalContext;
import java.lang.reflect.Constructor;

/**
 * Instantiates a command by passing it an initial context.
 *
 * @param <C> the type of the context
 */
public class InitialContextInstantiator<C extends GlobalContext> implements Instantiator<C> {

  private C context;

  public InitialContextInstantiator(C context) {
    this.context = context;
  }

  @Override
  public <T extends CommandNode<C>> T newInstance(Class<T> clazz) {
    for (Constructor<?> constructor : clazz.getConstructors()) {
      if (constructor.getParameterCount() == 0) {
        @SuppressWarnings("unchecked")
        T t = (T) doUnchecked(constructor::newInstance);
        return t;
      }
      if (constructor.getParameterCount() == 1) {
        if (!context.getClass().isAssignableFrom(constructor.getParameterTypes()[0])) {
          continue;
        }
        @SuppressWarnings("unchecked")
        T t = (T) doUnchecked(() -> constructor.newInstance(context));
        return t;
      }
    }
    throw new IllegalArgumentException(
        "Could not instantiate " + clazz
            + "!. It has no no-args constructor and none taking a single context!"
    );
  }

  private <T> T doUnchecked(UncheckedReflectiveOp<T> operation) {
    try {
      return operation.execute();
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  private interface UncheckedReflectiveOp<T> {

    T execute() throws ReflectiveOperationException;
  }
}
