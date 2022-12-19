package main.java.cyder.ui.grid;

import com.google.common.base.Preconditions;
import main.java.cyder.constants.CyderColors;
import main.java.cyder.logging.LogTag;
import main.java.cyder.logging.Logger;

import java.awt.*;

/**
 * A node component for a {@link CyderGrid}.
 */
public final class GridNode {
    /**
     * The default color of a GridNode.
     */
    private static final Color DEFAULT_COLOR = CyderColors.navy;

    /**
     * The color of this node.
     */
    private Color color;

    /**
     * The x value of this node.
     */
    private int x;

    /**
     * The y value of this node.
     */
    private int y;

    /**
     * Constructs a new grid node.
     *
     * @param x the x value of this node
     * @param y the y value of this node
     */
    public GridNode(int x, int y) {
        this(DEFAULT_COLOR, x, y);
    }

    /**
     * Constructs a new GridNode.
     *
     * @param color the color of this node
     * @param x     the x value of this node
     * @param y     the y value of this node
     */
    public GridNode(Color color, int x, int y) {
        this.color = Preconditions.checkNotNull(color);
        this.x = x;
        this.y = y;

        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Returns the color of this node.
     *
     * @return the color of this node
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets the color of this node.
     *
     * @param color the color of this node
     */
    public void setColor(Color color) {
        this.color = Preconditions.checkNotNull(color);
    }

    /**
     * Returns the x of this node.
     *
     * @return the x of this node
     */
    public int getX() {
        return x;
    }

    /**
     * Sets the x of this node.
     *
     * @param x the x of this node
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Returns the y of this node.
     *
     * @return the y of this node
     */
    public int getY() {
        return y;
    }

    /**
     * Sets the y of this node.
     *
     * @param y the y of this node
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * Returns the point of this node.
     *
     * @return the point of this node
     */
    public Point getPoint() {
        return new Point(x, y);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object node) {
        if (node == this) {
            return true;
        } else if (!(node instanceof GridNode)) {
            return false;
        }

        GridNode other = (GridNode) node;
        return (x == other.x && y == other.y);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = Integer.hashCode(x);
        ret = 31 * ret + Integer.hashCode(y);
        ret = 31 * ret + color.hashCode();
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "GridNode{" + "color=" + color
                + ", x=" + x + ", y=" + y + "}";
    }
}
