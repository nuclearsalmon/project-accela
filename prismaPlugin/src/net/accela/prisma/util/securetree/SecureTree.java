package net.accela.prisma.util.securetree;

import net.accela.prisma.Drawable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Stack is meant to be used by a WM as a fast and secure way to store {@link Drawable} trees/stacks.
 * It keeps track of all {@link Node}s that get created, and can be used to create new {@link Node}s.
 * Unlike {@link Branch}es, the Stack is "secret" and although a reference to it is included in all {@link Node}s,
 * it's not accessible from the outside.
 */
public final class SecureTree {
    final List<Node> nodes = new ArrayList<>();
    final Map<Drawable, Node> allNodes = new HashMap<>();

    /**
     * Creates a new {@link Branch} {@link Node node} on this Stack.
     *
     * @param data The data that the {@link Branch} {@link Node node} will represent
     * @return A {@link Node} instance representing the provided data
     */
    public @NotNull Node newNode(@NotNull Drawable data) {
        Node node = new Node(this, null, null, data);
        nodes.add(node);
        allNodes.put(data, node);
        return node;
    }

    /**
     * Kills all {@link Node}s
     *
     * @see Node#kill()
     */
    public void killNodes() {
        for (Node node : nodes) {
            node.kill();
        }
    }

    /**
     * @param drawable The data to search for
     * @return a {@link Node} representing the provided {@link Drawable} data, if found.
     */
    public @Nullable Node getNode(@NotNull Drawable drawable) {
        return allNodes.get(drawable);
    }

    /**
     * @return the {@link Node}s that are immediately connected to this SecureTree
     */
    public @NotNull List<Node> getNodes() {
        return List.copyOf(nodes);
    }

    /**
     * @return all {@link Node}s that are connected to this SecureTree, including those of its child branches
     */
    public @NotNull Map<@NotNull Drawable, @NotNull Node> getAllNodes() {
        return Map.copyOf(allNodes);
    }
}
