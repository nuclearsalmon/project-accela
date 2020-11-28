package net.accela.prisma.util.drawabletree;

import net.accela.prisma.Drawable;
import net.accela.prisma.DrawableContainer;
import net.accela.prisma.PrismaWM;
import net.accela.prisma.geometry.Rect;
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
    /**
     * All immediate child {@link Node}s attached to this {@link DrawableTree}.
     */
    final List<Node> childNodes = new ArrayList<>();
    /**
     * All {@link Node}s in this {@link DrawableTree}
     */
    final List<Node> allNodes = new ArrayList<>();

    /**
     * All nodes in all DrawableTrees in total. Only to be used for lookups.
     * This one is static so that we can do a lookup from anywhere. It's a bit hack-ish but probably fine.
     */
    final static Map<Drawable, Node> globalAllNodes = new HashMap<>();

    final PrismaWM windowManager;

    Node focusedNode;

    public DrawableTree(@NotNull PrismaWM windowManager) {
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

        childNodes.add(node);
        allNodes.add(node);
        globalAllNodes.put(drawable, node);
        setFocusedNode(node);
        return node;
    }

    /**
     * Kills all {@link Node}s
     *
     * @see Node#kill()
     */
    public void killNodes() {
        while (childNodes.size() > 0) {
            childNodes.get(0).kill();
        }
    }

    /**
     * @param drawable The data to search for
     * @return a {@link Node} representing the provided {@link Drawable} data, if found.
     */
    public static @Nullable Node getNode(@NotNull Drawable drawable) {
        return globalAllNodes.get(drawable);
    }

    /**
     * @return the {@link Node}s that are immediately connected to this SecureTree
     */
    public @NotNull List<Node> getChildNodes() {
        return List.copyOf(childNodes);
    }

    /**
     * @return all {@link Node}s that are connected to this SecureTree, including those of its child branches
     */
    public @NotNull List<@NotNull Node> getAllNodes() {
        return List.copyOf(allNodes);
    }

    /**
     * @return The {@link PrismaWM} instance that was used to create this {@link DrawableTree}
     */
    public @NotNull PrismaWM getWindowManager() {
        return windowManager;
    }

    /**
     * @return The currently focused {@link Node}.
     */
    public @Nullable Node getFocusedNode() {
        return focusedNode;
    }

    /**
     * @param node The currently focused {@link Node}.
     */
    public void setFocusedNode(@Nullable Node node) {
        if (node == null || (node.isAlive() && allNodes.contains(node))) focusedNode = node;
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
            for (Node childNode : ((Branch) node).getChildNodes()) {
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
        List<Node> childNodes = getAllChildNodes(node);
        List<Drawable> childDrawables = new ArrayList<>();
        for (Node childNode : childNodes) {
            childDrawables.add(childNode.getDrawable());
        }
        return childDrawables;
    }

    /**
     * @param rect The {@link Rect} to look for {@link Node}s within. Relative.
     * @return All {@link Node}s that are situated within the {@link Rect} provided
     */
    public @NotNull List<@NotNull Node> getIntersectingNodes(@NotNull Rect rect) {
        List<Node> nodes = new ArrayList<>();
        for (Node node : getChildNodes()) {
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
        for (Node node : getChildNodes()) {
            Drawable drawable = node.getDrawable();
            if (rect.intersects(drawable.getRelativeRect())) {
                drawables.add(drawable);
            }
        }
        return drawables;
    }
}
