package cyder.ui.objects;

import java.awt.*;

/**
 * A node component used for the {@link cyder.ui.CyderGrid}.
 */
public final class GridNode {
    private Color color;
    private int x;
    private int y;

    public GridNode(Color color, int x, int y) {
        this.color = color;
        this.x = x;
        this.y = y;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object node) {
        if (node == this)
            return true;
        if (!(node instanceof GridNode))
            return false;

        GridNode other = (GridNode) node;

        return (this.x == other.x && this.y == other.y);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = Integer.hashCode(this.x);
        ret = 31 * ret + Integer.hashCode(this.y);
        ret = 31 * ret + color.hashCode();
        return ret;
    }
}
