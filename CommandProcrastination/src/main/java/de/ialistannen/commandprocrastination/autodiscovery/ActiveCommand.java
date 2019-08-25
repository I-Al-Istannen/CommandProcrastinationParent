package de.ialistannen.commandprocrastination.autodiscovery;

import de.ialistannen.commandprocrastination.command.tree.CommandNode;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An active command that is discovered and loaded.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ActiveCommand {

  /**
   * The name of the command.
   *
   * @return the name of the command
   */
  String name();

  /**
   * The parent command.
   *
   * @return the parent command#s name. Will return "no-parent" if there is none
   */
  String parent() default "no-parent";

  /**
   * The class of the parent command node. Useful if it is also its own class.
   *
   * Note that the {@link #parent()} name takes precedence over this
   *
   * @return the parent class or {@link CommandNode} if none
   */
  Class<? extends CommandNode> parentClass() default CommandNode.class;
}
