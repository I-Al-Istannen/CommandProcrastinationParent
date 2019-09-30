package de.ialistannen.commandprocrastination.autodiscovery;

import de.ialistannen.commandprocrastination.autodiscovery.processor.CollectedCommands;
import de.ialistannen.commandprocrastination.command.tree.CommandNode;
import de.ialistannen.commandprocrastination.command.tree.data.DefaultDataKey;
import de.ialistannen.commandprocrastination.context.Context;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Discovers commands on the classpath.
 */
public class CommandDiscovery {

  private final CollectedCommands collectedCommands;

  public CommandDiscovery(CollectedCommands collectedCommands) {
    this.collectedCommands = collectedCommands;
  }

  /**
   * Finds all commands, instantiates them, tries to order them and returns the root.
   *
   * @param <C> the type of the context
   * @return the found commands
   */
  public <C extends Context> CommandNode<C> findCommands() {
    DiscoveryRootCommand<C> root = new DiscoveryRootCommand<>();
    List<GraphNode<CommandNode<C>>> nodes = findAllCommands();

    // Connect the graph
    for (GraphNode<CommandNode<C>> node : nodes) {
      findNode(node.relation, nodes)
          .ifPresent(parent -> parent.addNeighbour(node));
    }

    for (GraphNode<CommandNode<C>> node : topologicalSort(nodes)) {
      NodeRelation relation = node.getRelation();
      root.addChild(node.getValue(), relation.getParent(), relation.getParentClass());
    }

    return root;
  }

  private <C extends Context> List<GraphNode<CommandNode<C>>> findAllCommands() {
    List<GraphNode<CommandNode<C>>> nodes = new ArrayList<>();

    Class[] classes = collectedCommands.getCollectedCommands();
    for (Class<?> aClass : classes) {
      if (!aClass.isAnnotationPresent(ActiveCommand.class)) {
        continue;
      }
      ActiveCommand activeCommand = aClass.getAnnotation(ActiveCommand.class);

      CommandNode<C> node;
      try {
        @SuppressWarnings("unchecked")
        CommandNode<C> temp = (CommandNode<C>) aClass.getConstructor().newInstance();
        node = temp;
      } catch (ReflectiveOperationException e) {
        throw new RuntimeException(e);
      }

      String parent = activeCommand.parent().equals("no-parent") ? null : activeCommand.parent();
      String name = activeCommand.name();

      node.setData(DefaultDataKey.IDENTIFIER, name);
      nodes.add(new GraphNode<>(
          new NodeRelation(name, parent, activeCommand.parentClass()),
          node
      ));
    }

    return nodes;
  }

  private <E> Optional<GraphNode<E>> findNode(NodeRelation relation, List<GraphNode<E>> nodes) {
    return nodes.stream()
        .filter(parent ->
            parent.relation.name.equals(relation.parent)
                || parent.getValue().getClass() == relation.parentClass
        )
        .findFirst();
  }

  /**
   * Topologically sorts the input graph nodes.
   *
   * <br><a href="https://en.wikipedia.org/wiki/Topological_ordering#Depth-first_search">Algorithm
   * used.</a>
   *
   * @param nodes the nodes
   * @param <E> the type of the graph value
   * @return the sorted list
   * @throws CycleException if a cycle is detected
   */
  private <E extends CommandNode<?>> List<GraphNode<E>> topologicalSort(List<GraphNode<E>> nodes) {
    List<GraphNode<E>> result = new ArrayList<>();

    for (GraphNode<E> node : nodes) {
      visit(node, result);
    }

    return result;
  }

  private <E extends CommandNode<?>> void visit(GraphNode<E> node, List<GraphNode<E>> result) {
    try {
      if (node.permanentMark) {
        return;
      }
      if (node.tempMark) {
        throw new CycleException();
      }

      node.setTempMark(true);
      for (GraphNode<E> neighbour : node.getNeighbours()) {
        visit(neighbour, result);
      }
      node.setTempMark(false);

      node.setPermanentMark(true);
      result.add(0, node);
    } catch (CycleException e) {
      e.prependPathNode(node.getValue());
      throw e;
    }
  }

  @RequiredArgsConstructor
  @Getter
  @Setter
  private static class GraphNode<E> {

    private final NodeRelation relation;
    private final E value;
    private List<GraphNode<E>> neighbours = new ArrayList<>();
    private boolean tempMark = false;
    private boolean permanentMark = false;

    void addNeighbour(GraphNode<E> node) {
      neighbours.add(node);
    }

    @Override
    public String toString() {
      return value + " with " + relation.toString();
    }
  }

  @Data
  private static class NodeRelation {

    private final String name;
    private final String parent;
    private final Class<? extends CommandNode> parentClass;
  }

  @Getter
  public static class CycleException extends RuntimeException {

    private final List<CommandNode<?>> commands = new ArrayList<>();

    /**
     * Prepends a node to the path that caused the cycle.
     *
     * @param node the node to prepend
     */
    private void prependPathNode(CommandNode<?> node) {
      commands.add(0, node);
    }

    @Override
    public String getMessage() {
      return "Cycle caused by " + commands.stream()
          .map(this::getName)
          .collect(Collectors.joining(" -> "));
    }

    private String getName(CommandNode<?> node) {
      return node.<String>getOptionalData(DefaultDataKey.IDENTIFIER)
          .or(() -> node.getHeadParser().getName())
          .orElse("N/A");
    }
  }

}
