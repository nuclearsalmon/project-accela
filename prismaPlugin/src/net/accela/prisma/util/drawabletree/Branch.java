package net.accela.prisma.util.drawabletree;

import net.accela.prisma.Drawable;
import net.accela.prisma.DrawableContainer;
import net.accela.prisma.geometry.Rect;
import net.accela.server.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Node} implementation that can store other {@link Node}s.
 */
public final class Branch extends Node {
    final List<Node> childNodes = new ArrayList<>();


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
     * @see Node
     */
    public Branch(@NotNull DrawableTree tree,
                  @Nullable Branch root,
                  @Nullable Branch parent,
                  @NotNull DrawableContainer data,
                  @NotNull Plugin plugin) {
        super(tree, root, parent, data, plugin);
    }

    /**
     * Creates a new {@link Node} on this Branch.
     *
     * @param data The data that the {@link Node} will represent
     * @return A {@link Node} instance representing the provided data
     */
    public @NotNull Node newNode(@NotNull Drawable data) {
        Node node;
        if (data instanceof DrawableContainer) {
            node = new Branch(this.tree, getRoot(), this, (DrawableContainer) data, this.plugin);
        } else {
            node = new Node(this.tree, getRoot(), this, data, this.plugin);
        }

        childNodes.add(node);
        tree.allNodes.put(data, node);
        DrawableTree.globalAllNodes.put(data, node);
        return node;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void kill() {
        killNodes();
        super.kill();
    }

    /**
     * Unlike {@link #kill()}, this will not kill the Branch itself, only the nodes that are attached to it.
     *
     * @see Node#kill()
     */
    public void killNodes() {
        for (Node node : childNodes) {
            node.kill();
        }
    }

    /**
     * @return True if this Branch is a root Branch
     */
    public final boolean isRoot() {
        // If root for this branch is null, it must mean that this branch IS the root branch.
        return this.root == null;
    }

    /**
     * @return The root {@link Node}
     */
    @Override
    public @NotNull Branch getRoot() {
        return (Branch) super.getRoot();
    }

    /**
     * @return the {@link Node}s that are immediately connected to this SecureTree
     */
    public @NotNull List<Node> getChildNodes() {
        return List.copyOf(childNodes);
    }


    /**
     * @return The data that this {@link Branch} represents
     */
    public final @NotNull DrawableContainer getData() {
        return (DrawableContainer) data;
    }

    /**
     * @param rect The {@link Rect} to look for {@link Node}s within. Relative.
     * @return All {@link Node}s that are situated within the {@link Rect} provided
     */
    public @NotNull List<@NotNull Node> getIntersectingNodes(@NotNull Rect rect) {
        List<Node> nodes = new ArrayList<>();
        for (Node node : getChildNodes()) {
            Drawable drawable = node.getData();
            if (rect.intersects(drawable.getRelativeRect())) {
                nodes.add(node);
            }
        }
        return nodes;
    }

    /**
     * @param rect The {@link Rect} to look for {@link Drawable}s within. Relative.
     * @return All {@link Drawable}s that are situated within the {@link Rect} provided
     */
    public @NotNull List<@NotNull Drawable> getIntersectingDrawables(@NotNull Rect rect) {
        List<Drawable> drawables = new ArrayList<>();
        for (Node node : getChildNodes()) {
            Drawable drawable = node.getData();
            if (rect.intersects(drawable.getRelativeRect())) {
                drawables.add(drawable);
            }
        }
        return drawables;
    }
}
