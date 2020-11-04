package net.accela.prisma.util.drawabletree;

import net.accela.prisma.Drawable;
import net.accela.prisma.PrismaWM;
import net.accela.server.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents {@link Drawable} data, and it's hierarchical position in the {@link DrawableTree}.
 */
public class Node {
    // This needs to be hidden from outside classes
    final @NotNull DrawableTree tree;

    // These are openly accessible, do whatever you want with them
    /**
     * The root {@link Node} of the whole branch
     */
    public final @Nullable Branch root;
    /**
     * The parent of this {@link Node}
     */
    public final @Nullable Branch parent;

    // It needs to be impossible to resurrect a node, hence why this is a private variable
    private boolean alive = true;

    /**
     * The {@link Drawable} data this {@link Node} represents
     */
    public final @NotNull Drawable data;

    /**
     * DO NOT instantiate manually.
     * Let a {@link Branch} or {@link DrawableTree} instantiate this using the
     * {@link Branch#newNode(Drawable)} or {@link DrawableTree#newNode(Drawable, Plugin)} method.
     *
     * @param tree   The {@link DrawableTree} to connect to
     * @param root   The {@link Branch} that's at the bottom of the {@link DrawableTree}
     * @param parent The {@link Branch} that created this Node
     * @see DrawableTree
     * @see Branch
     */
    public Node(@NotNull DrawableTree tree,
                @Nullable Branch root,
                @Nullable Branch parent,
                @NotNull Drawable data) {
        this.tree = tree;
        this.root = root;
        this.parent = parent;
        this.data = data;
    }

    /**
     * Figuratively "kills" this Node, removing it from the parent {@link Branch} and {@link DrawableTree} Node sets.
     * From the perspective of the stack, this node no longer exists.
     */
    public void kill() {
        alive = false;
        if (parent != null) parent.nodes.remove(this);
        tree.allNodes.remove(data, this);
        DrawableTree.staticAllNodes.remove(data, this);
    }

    /**
     * @return The data that this {@link Node} represents
     */
    public @NotNull Drawable getData() {
        return data;
    }

    /**
     * @return The root {@link Node}
     */
    public @NotNull Node getRoot() {
        return root == null ? this : root;
    }

    /**
     * @return The parent branch
     */
    public final @Nullable Branch getParent() {
        return parent;
    }

    /**
     * @return True if alive, false if dead. Changes to false once the {@link Node#kill()} method has been executed
     */
    public final boolean isAlive() {
        return alive;
    }

    /**
     * @return The {@link PrismaWM} instance that was used to create this {@link DrawableTree}
     */
    public final @NotNull PrismaWM getWindowManager() {
        return tree.getWindowManager();
    }

    /**
     * @return The {@link Plugin} that was used to create the provided {@link Node}
     */
    public final @NotNull Plugin getPlugin() {
        System.out.println("root: " + root);
        System.out.println("getRoot: " + getRoot());
        Plugin plugin = tree.nodesPluginMap.get(getRoot());
        System.out.println("plugin: " + plugin);
        System.out.println("nodesPluginMap:");
        tree.nodesPluginMap.forEach((key, value) -> System.out.println(key + " : " + value));
        return plugin;
    }
}
