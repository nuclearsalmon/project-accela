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
    // It needs to be impossible to resurrect a node, hence why this is a private variable
    private boolean alive = true;

    // These are openly accessible, do whatever you want with them
    /**
     * The root {@link Node} of the whole branch
     */
    public final @Nullable Branch root;

    /** The parent of this {@link Node} */
    public final @Nullable Branch parent;

    /** The {@link Drawable} data this {@link Node} represents */
    public final @NotNull Drawable drawable;

    /** The {@link Plugin} that created this node */
    public final @NotNull Plugin plugin;

    /** Represents layering and focus priority */
    @NotNull Priority priority = Priority.DEFAULT_VALUE;

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
                @NotNull Drawable drawable,
                @NotNull Plugin plugin) {
        this.tree = tree;
        this.root = root;
        this.parent = parent;
        this.drawable = drawable;
        this.plugin = plugin;
    }

    /**
     * Figuratively "kills" this Node, removing it from the parent {@link Branch} and {@link DrawableTree} Node sets.
     * From the perspective of the stack, this node no longer exists.
     */
    public void kill() {
        // First unfocus
        if (tree.getTreeFocusNode() == this) tree.setTreeFocusNode(null);

        // Change flag
        alive = false;

        // Remove references
        if (parent != null) parent.childNodeList.remove(this);
        tree.treeNodeList.remove(this);
        tree.childNodeList.remove(this);
        DrawableTree.staticDrawableNodeMap.remove(drawable, this);
    }

    /**
     * @return The data that this {@link Node} represents
     */
    public @NotNull Drawable getDrawable() {
        return drawable;
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
        return tree.windowManager;
    }

    /**
     * @return The {@link Plugin} that was used to create the provided {@link Node}
     */
    public final @NotNull Plugin getPlugin() {
        return plugin;
    }

    //
    // Priority
    //

    /**
     * Asks the parent or tree to set this {@link Node}'s priority.
     *
     * @param priority The priority requested
     * @see Branch#setPriority(Node, Priority)
     * @see Branch#setPriority(Node, Priority, boolean)
     * @see DrawableTree#setPriority(Node, Priority)
     * @see DrawableTree#setPriority(Node, Priority, boolean)
     */
    public final void setPriority(@NotNull Priority priority) {
        if (parent != null) {
            parent.setPriority(this, priority);
        } else {
            tree.setPriority(this, priority);
        }
    }

    public final @NotNull Priority getPriority() {
        return priority;
    }
}
