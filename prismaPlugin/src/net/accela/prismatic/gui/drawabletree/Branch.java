package net.accela.prismatic.gui.drawabletree;

import net.accela.prismatic.gui.Drawable;
import net.accela.prismatic.gui.DrawableContainer;
import net.accela.prismatic.gui.geometry.Rect;
import net.accela.server.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Node} implementation that can store other {@link Node}s.
 */
public class Branch extends Node {
    final List<Node> childNodeList = new ArrayList<>();

    Node childFocusNode;

    /**
     * DO NOT instantiate manually.
     * Let a {@link Branch} or {@link DrawableTree} instantiate this using the
     * {@link Branch#newNode(Drawable)} or {@link DrawableTree#newNode(Drawable, Plugin)} method.
     *
     * @param tree     The {@link DrawableTree} to connect to
     * @param root     The {@link Branch} that's at the bottom of the {@link DrawableTree}
     * @param parent   The {@link Branch} that created this {@link Node}
     * @param drawable The {@link Drawable} ({@link DrawableContainer}) data this {@link Node} represents
     * @see DrawableTree
     * @see Node
     */
    public Branch(@NotNull DrawableTree tree,
                  @Nullable Branch root,
                  @Nullable Branch parent,
                  @NotNull DrawableContainer drawable,
                  @NotNull Plugin plugin) {
        super(tree, root, parent, drawable, plugin);
    }

    /**
     * Creates a new {@link Node} on this Branch.
     *
     * @param drawable The {@link Drawable} data that the {@link Node} will represent
     * @return A {@link Node} instance representing the provided drawable
     */
    public @NotNull Node newNode(@NotNull Drawable drawable) {
        Node node;
        if (drawable instanceof DrawableContainer) {
            node = new Branch(this.tree, getRoot(), this, (DrawableContainer) drawable, this.plugin);
        } else {
            node = new Node(this.tree, getRoot(), this, drawable, this.plugin);
        }

        addNodeCorrectly(node, true);
        tree.treeNodeList.add(node);
        DrawableTree.staticDrawableNodeMap.put(drawable, node);
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
        while (childNodeList.size() > 0) {
            childNodeList.get(0).kill();
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
     * @return A list of {@link Node}s that are immediately connected to this SecureTree
     */
    public @NotNull List<@NotNull Node> getChildNodeList() {
        return List.copyOf(childNodeList);
    }

    /**
     * @return A list of {@link Drawable}s,
     * extracted from {@link Node}s that are immediately connected to this branch.
     */
    public @NotNull List<@NotNull Drawable> getChildDrawables() {
        List<Drawable> childDrawables = new ArrayList<>();
        for (Node childNode : childNodeList) {
            childDrawables.add(childNode.getDrawable());
        }
        return childDrawables;
    }


    /**
     * @return The {@link Drawable} data that this {@link Branch} represents
     */
    public final @NotNull DrawableContainer getDrawable() {
        return (DrawableContainer) drawable;
    }

    /**
     * @param rect The {@link Rect} to look for {@link Node}s within. Relative.
     * @return All {@link Node}s that are situated within the {@link Rect} provided
     */
    public @NotNull List<@NotNull Node> getIntersectingNodes(@NotNull Rect rect) {
        List<Node> nodes = new ArrayList<>();
        for (Node node : getChildNodeList()) {
            Drawable drawable = node.getDrawable();
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
        for (Node node : getChildNodeList()) {
            Drawable drawable = node.getDrawable();
            if (rect.intersects(drawable.getRelativeRect())) {
                drawables.add(drawable);
            }
        }
        return drawables;
    }

    //
    // Focus
    //

    /**
     * @return The currently locally focused {@link Node}.
     */
    public @Nullable Node getFocusedNode() {
        return childFocusNode;
    }

    /**
     * @param node The {@link Node} to be focused.
     */
    public void setFocusedNode(@Nullable Node node) {
        if (node == null || (node.isAlive() && childNodeList.contains(node))) {
            // Set child focus
            childFocusNode = node;
        }
    }

    //
    // NodePriority
    //

    /**
     * @param priority The priority to search for
     * @param top      True for top index, false for bottom index
     * @return -1 means not found, anything over that is a valid index.
     */
    int getIndexByPriority(final @NotNull NodePriority priority, final boolean top) {
        synchronized (childNodeList) {
            int lastIndex = 0;
            if (top) {
                for (int i = childNodeList.size() - 1; i >= 0; i--) {
                    NodePriority currentPriority = childNodeList.get(i).priority;

                    if (currentPriority.ordinal() < priority.ordinal()) return lastIndex;
                    else lastIndex = i;
                }
            } else {
                for (int i = 0; i < childNodeList.size(); i++) {
                    NodePriority currentPriority = childNodeList.get(i).priority;

                    if (currentPriority.ordinal() > priority.ordinal()) return lastIndex;
                    else lastIndex = i;
                }
            }
        }
        return 0;
    }

    /**
     * Assigns a {@link NodePriority} to a {@link Node}, and moves it accordingly.
     *
     * @param node     the {@link Node} to set a {@link NodePriority} for.
     * @param priority the {@link NodePriority} to set.
     */
    public void setPriority(@NotNull Node node, @NotNull NodePriority priority) {
        setPriority(node, priority, false);
    }

    /**
     * Assigns a {@link NodePriority} to a {@link Node}, and moves it accordingly.
     *
     * @param node      the {@link Node} to set a {@link NodePriority} for.
     * @param priority  the {@link NodePriority} to set.
     * @param moveToTop whether to move to top or bottom.
     */
    public void setPriority(@NotNull Node node, @NotNull NodePriority priority, boolean moveToTop) {
        synchronized (childNodeList) {
            if (priority.ordinal() < DrawableTree.PRIORITY_MIN_ALLOWED.ordinal()
                    || priority.ordinal() > DrawableTree.PRIORITY_MAX_ALLOWED.ordinal()) {
                throw new IllegalArgumentException(String.format(
                        "NodePriority %s is not within the allowed range of (%s - %s)",
                        priority, DrawableTree.PRIORITY_MIN_ALLOWED, DrawableTree.PRIORITY_MAX_ALLOWED
                ));
            } else {
                node.priority = priority;
            }

            // List changes
            childNodeList.remove(node);
            // Add to bottom
            childNodeList.add(getIndexByPriority(priority, moveToTop), node);
        }
    }

    //
    // Focusing and priority - internal methods
    //

    /**
     * Adds a {@link Node} to the child {@link Node} list,
     * at the correct index. The index is derived based on the {@link Node}'s {@link NodePriority}.
     *
     * @param node the node to add.
     * @param top  whether to prefer adding to the top or bottom of the matching priorities.
     */
    @SuppressWarnings("SameParameterValue")
    void addNodeCorrectly(@NotNull Node node, boolean top) {
        NodePriority priority = node.getPriority();

        synchronized (childNodeList) {
            childNodeList.add(getIndexByPriority(priority, top), node);
        }
    }
}
