package net.accela.prisma.util.securetree;

import net.accela.prisma.Drawable;
import net.accela.prisma.DrawableContainer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A Branch is a {@link Node} that can store other {@link Node}s.
 */
public class Branch extends Node {
    final List<Node> nodes = new ArrayList<>();

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
     * @see Node
     */
    public Branch(@NotNull SecureTree stack,
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
        Node node = new Node(stack, isRoot() ? this : null, this, data);
        nodes.add(node);
        stack.allNodes.put(data, node);
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
     * @return the {@link Node}s that are immediately connected to this SecureTree
     */
    public @NotNull List<Node> getNodes() {
        return List.copyOf(nodes);
    }
}
