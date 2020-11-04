package net.accela.prisma.util.drawabletree;

import net.accela.prisma.Drawable;
import net.accela.prisma.DrawableContainer;
import net.accela.server.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Node} implementation that can store other {@link Node}s.
 */
public class Branch extends Node {
    final List<Node> nodes = new ArrayList<>();

    /**
     * DO NOT instantiate manually.
     * Let a {@link Branch} or {@link DrawableTree} instantiate this using the
     * {@link Branch#newNode(Drawable)} or {@link DrawableTree#newNode(Drawable, Plugin)} method.
     *
     * @param stack  The {@link DrawableTree} to connect to
     * @param root   The {@link Branch} that's at the bottom of the {@link DrawableTree}
     * @param parent The {@link Branch} that created this Node
     * @param data   The {@link Drawable} data this Node represents
     * @see DrawableTree
     * @see Node
     */
    public Branch(@NotNull DrawableTree stack,
                  @Nullable Branch root,
                  @Nullable Branch parent,
                  @NotNull DrawableContainer data) {
        super(stack, root, parent, data);
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
            node = new Branch(tree, getRoot(), this, (DrawableContainer) data);
        } else {
            node = new Node(tree, getRoot(), this, data);
        }

        nodes.add(node);
        tree.allNodes.put(data, node);
        DrawableTree.staticAllNodes.put(data, node);
        return node;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void kill() {
        killNodes();
        super.kill();
        if (parent != null) parent.nodes.remove(this);
        tree.allNodes.remove(data, this);
        DrawableTree.staticAllNodes.remove(data, this);
    }

    /**
     * Unlike {@link #kill()}, this will not kill the Branch itself, only the nodes that are attached to it.
     *
     * @see Node#kill()
     */
    public void killNodes() {
        for (Node node : nodes) {
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
    public @NotNull List<Node> getNodes() {
        return List.copyOf(nodes);
    }


    /**
     * @return The data that this {@link Branch} represents
     */
    public final @NotNull DrawableContainer getData() {
        return (DrawableContainer) data;
    }
}
