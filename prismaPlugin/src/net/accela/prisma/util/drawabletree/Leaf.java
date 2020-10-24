package net.accela.prisma.util.drawabletree;

import net.accela.prisma.Drawable;
import net.accela.server.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A simple {@link Node} implementation.
 */
public class Leaf extends Node {
    public final @NotNull Drawable data;

    /**
     * DO NOT instantiate manually.
     * Let a {@link Branch} or {@link DrawableTree} instantiate this using the
     * {@link Branch#newNode(Drawable)} or {@link DrawableTree#newNode(Drawable, Plugin)} method.
     *
     * @param tree   The {@link DrawableTree} to connect to
     * @param root   The {@link Branch} that's at the bottom of the {@link DrawableTree}
     * @param parent The {@link Branch} that created this Node
     * @param data   The {@link Drawable} data this Node represents
     * @see DrawableTree
     * @see Branch
     */
    public Leaf(@NotNull DrawableTree tree,
                @Nullable Branch root,
                @Nullable Branch parent,
                @NotNull Drawable data) {
        super(tree, root, parent);
        this.data = data;
    }

    /**
     * Figuratively "kills" this Node, removing it from the parent {@link Branch} and {@link DrawableTree} Node sets.
     * From the perspective of the stack, this node no longer exists.
     */
    @Override
    public void kill() {
        super.kill();
        if (parent != null) parent.nodes.remove(this);
        tree.allNodes.remove(data, this);
        DrawableTree.allNodesGlobally.remove(data, this);
    }

    /**
     * @return The data that this {@link Node} represents
     */
    public final @NotNull Drawable getData() {
        return data;
    }
}
