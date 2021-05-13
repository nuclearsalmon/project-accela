package net.accela.prismatic.gui.drawabletree;

import net.accela.prismatic.Prismatic;
import net.accela.prismatic.gui.Drawable;
import net.accela.prismatic.gui.DrawableContainer;
import net.accela.prismatic.gui.geometry.Rect;
import net.accela.server.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * A DrawableTree is meant to be used by a WM as a fast and secure way to store {@link Drawable}s in a tree structure.
 * It keeps track of all {@link Node}s that get created, and can be used to create new {@link Node}s.
 * Unlike {@link Branch}es, the DrawableTree is "secret" and although a reference to it is included in all {@link Node}s,
 * it's not accessible from the outside.
 */
public class DrawableTree {
    // Static variables
    public static final NodePriority PRIORITY_MAX_ALLOWED = NodePriority.HIGH;
    public static final NodePriority PRIORITY_MIN_ALLOWED = NodePriority.LOW;

    /**
     * The {@link Prismatic} instance that was used to create this {@link DrawableTree}
     */
    public final Prismatic windowManager;

    // Node lists
    /**
     * All immediate child {@link Node}s attached to this {@link DrawableTree}.
     */
    final List<Node> childNodeList = new ArrayList<>();
    /**
     * All {@link Node}s in this {@link DrawableTree}
     */
    final Set<Node> treeNodeList = new HashSet<>();

    /**
     * All nodes in all DrawableTrees in total. Only to be used for lookups.
     * This one is static so that we can do a lookup from anywhere. It's a bit hack-ish but probably fine.
     */
    final static Map<Drawable, Node> staticDrawableNodeMap = new HashMap<>();

    // Layering and focusing
    Node treeFocusNode;
    Node childFocusNode;

    public DrawableTree(@NotNull Prismatic windowManager) {
        this.windowManager = windowManager;
    }

    /**
     * Creates a new {@link Node} in this {@link DrawableTree}
     *
     * @param drawable The {@link Drawable} data that the {@link Node} will represent
     * @param plugin   The {@link Plugin} registering this {@link Node}
     * @return A {@link Node} instance representing the provided data
     */
    public @NotNull Node newNode(@NotNull Drawable drawable, @NotNull Plugin plugin) {
        Node node;
        if (drawable instanceof DrawableContainer) {
            node = new Branch(this, null, null, (DrawableContainer) drawable, plugin);
        } else {
            node = new Node(this, null, null, drawable, plugin);
        }

        addNodeCorrectly(node, false);
        treeNodeList.add(node);
        staticDrawableNodeMap.put(drawable, node);
        setTreeFocusNode(node);
        return node;
    }

    /**
     * Kills all {@link Node}s
     *
     * @see Node#kill()
     */
    public void killNodes() {
        while (childNodeList.size() > 0) {
            childNodeList.get(0).kill();
        }
    }

    /**
     * @param drawable The data to search for
     * @return a {@link Node} representing the provided {@link Drawable} data, if found.
     */
    public static @Nullable Node getNode(@NotNull Drawable drawable) {
        return staticDrawableNodeMap.get(drawable);
    }

    /**
     * @return the {@link Node}s that are immediately connected to this SecureTree
     */
    public @NotNull List<Node> getChildNodeList() {
        return List.copyOf(childNodeList);
    }

    /**
     * @return all {@link Node}s that are connected to this SecureTree, including those of its child branches
     */
    public @NotNull List<@NotNull Node> getTreeNodeList() {
        return List.copyOf(treeNodeList);
    }

    /**
     * @param node The {@link Node} to collect from
     * @return all {@link Node}s that are attached to the {@link Node} provided
     */
    public static @NotNull List<@NotNull Node> getAllChildNodes(@NotNull Node node) {
        final List<Node> allChildNodes = new ArrayList<>();
        recursiveCollectNodes(allChildNodes, node);
        return allChildNodes;
    }

    static void recursiveCollectNodes(@NotNull List<@NotNull Node> allChildNodes, @NotNull Node node) {
        if (node instanceof Branch) {
            for (Node childNode : ((Branch) node).getChildNodeList()) {
                allChildNodes.add(childNode);
                recursiveCollectNodes(allChildNodes, childNode);
            }
        }
    }

    /**
     * @param node The {@link Node} to collect from
     * @return all {@link Drawable}s that are attached to the {@link Node} provided
     */
    public static @NotNull List<@NotNull Drawable> getAllChildDrawables(@NotNull Node node) {
        List<Node> nodeChildNodes = getAllChildNodes(node);
        List<Drawable> nodeChildDrawables = new ArrayList<>();
        for (Node childNode : nodeChildNodes) {
            nodeChildDrawables.add(childNode.getDrawable());
        }
        return nodeChildDrawables;
    }

    /**
     * @param relativeRect The {@link Rect} to look for {@link Node}s within. Relative.
     * @return All {@link Node}s that are situated within the {@link Rect} provided
     */
    public @NotNull List<@NotNull Node> getIntersectingChildNodes(@NotNull Rect relativeRect) {
        List<Node> nodes = new ArrayList<>();
        for (Node node : getChildNodeList()) {
            Drawable drawable = node.getDrawable();
            if (relativeRect.intersects(drawable.getRelativeRect())) {
                nodes.add(node);
            }
        }
        return nodes;
    }

    //
    // Focus
    //

    /**
     * @param node The {@link Node} to be focused.
     */
    // TODO: 12/18/20 proper synchronization with R/W locks
    public synchronized void setTreeFocusNode(@Nullable Node node) {
        if (node == null || (node.isAlive() && treeNodeList.contains(node))) {
            // Set tree focus
            treeFocusNode = node;

            // Set tree child focus
            if (childNodeList.contains(node)) {
                childFocusNode = node;
            } else {
                if (node != null) {
                    // Set parent focus
                    Objects.requireNonNull(node.getParent()).childFocusNode = node;
                    childFocusNode = node.getRoot();
                } else {
                    childFocusNode = null;
                }
            }
        }
    }

    /**
     * @return The currently globally focused {@link Node}.
     */
    public @Nullable Node getTreeFocusNode() {
        return treeFocusNode;
    }

    /**
     * @return The currently locally focused {@link Node}.
     */
    public @Nullable Node getChildFocusNode() {
        return childFocusNode;
    }

    /**
     * @param node The {@link Node} to be focused.
     */
    public void setChildFocusNode(@Nullable Node node) {
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
            if (priority.ordinal() < PRIORITY_MIN_ALLOWED.ordinal() || priority.ordinal() > PRIORITY_MAX_ALLOWED.ordinal()) {
                throw new IllegalArgumentException(String.format(
                        "NodePriority %s is not within the allowed range of (%s - %s)",
                        priority, PRIORITY_MIN_ALLOWED, PRIORITY_MAX_ALLOWED
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
