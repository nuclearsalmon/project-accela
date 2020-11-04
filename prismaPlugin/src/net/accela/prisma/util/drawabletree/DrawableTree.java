package net.accela.prisma.util.drawabletree;

import net.accela.prisma.Drawable;
import net.accela.prisma.DrawableContainer;
import net.accela.prisma.PrismaWM;
import net.accela.server.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A DrawableTree is meant to be used by a WM as a fast and secure way to store {@link Drawable}s in a tree structure.
 * It keeps track of all {@link Node}s that get created, and can be used to create new {@link Node}s.
 * Unlike {@link Branch}es, the DrawableTree is "secret" and although a reference to it is included in all {@link Node}s,
 * it's not accessible from the outside.
 */
public final class DrawableTree {
    final List<Node> nodes = new ArrayList<>();
    final Map<Node, Plugin> nodesPluginMap = new HashMap<>();
    final Map<Drawable, Node> allNodes = new HashMap<>();

    // This one is static so that we can do a lookup from anywhere. It's a bit hack-ish but probably fine.
    final static Map<Drawable, Node> staticAllNodes = new HashMap<>();

    final PrismaWM windowManager;

    public DrawableTree(@NotNull PrismaWM windowManager) {
        this.windowManager = windowManager;
    }

    /**
     * Creates a new {@link Node} in this {@link DrawableTree}
     *
     * @param data   The data that the {@link Node} will represent
     * @param plugin The plugin registering this {@link Node}
     * @return A {@link Node} instance representing the provided data
     */
    public @NotNull Node newNode(@NotNull Drawable data, @NotNull Plugin plugin) {
        Node node;
        if (data instanceof DrawableContainer) {
            node = new Branch(this, null, null, (DrawableContainer) data);
        } else {
            node = new Node(this, null, null, data);
        }

        nodes.add(node);
        allNodes.put(data, node);
        staticAllNodes.put(data, node);
        nodesPluginMap.put(node, plugin);
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
    public static @Nullable Node getNode(@NotNull Drawable drawable) {
        return staticAllNodes.get(drawable);
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

    /**
     * @return The {@link PrismaWM} instance that was used to create this {@link DrawableTree}
     */
    public @NotNull PrismaWM getWindowManager() {
        return windowManager;
    }
}
