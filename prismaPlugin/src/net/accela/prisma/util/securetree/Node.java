package net.accela.prisma.util.securetree;

import net.accela.prisma.Drawable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A Node represents {@link Drawable} data, and it's hierarchical position.
 */
public class Node {
    // This needs to be hidden from outside classes
    @NotNull
    final SecureTree stack;

    // These are openly accessible, do whatever you want with them
    public final Branch root;
    public final Branch parent;
    @NotNull
    public final Drawable data;

    // It needs to be impossible to resurrect a node, hence why this is a private variable
    private boolean alive = true;

    /**
     * DO NOT instantiate manually.
     * Let a {@link Branch} or {@link SecureTree} instantiate this using the
     * {@link Branch#newNode(Drawable)} or {@link SecureTree#newNode(Drawable)} method.
     *
     * @param stack  The {@link SecureTree} to connect to
     * @param root   The {@link Branch} that's at the bottom of the {@link SecureTree}
     * @param parent The {@link Branch} that created this Node
     * @param data   The {@link Drawable} data this Node represents
     * @see SecureTree
     * @see Branch
     */
    public Node(@NotNull SecureTree stack,
                @Nullable Branch root,
                @Nullable Branch parent,
                @NotNull Drawable data) {
        this.stack = stack;
        this.root = root;
        this.parent = parent;
        this.data = data;
    }

    /**
     * Figuratively "kills" this Node, removing it from the parent {@link Branch} and {@link SecureTree} Node sets.
     * From the perspective of the stack, this node no longer exists.
     */
    public void kill() {
        alive = false;
        if (parent != null) parent.nodes.remove(this);
        stack.allNodes.remove(data, this);
    }

    /**
     * @return The root branch
     */
    public final Branch getRoot() {
        return root;
    }

    /**
     * @return The parent branch
     */
    public final Branch getParent() {
        return parent;
    }

    /**
     * @return The {@link Drawable} data that this Node represents
     */
    public final @NotNull Drawable getData() {
        return data;
    }

    /**
     * @return True if alive, false if dead. Changes to false once the {@link Node#kill()} method has been executed
     */
    public final boolean isAlive() {
        return alive;
    }
}
