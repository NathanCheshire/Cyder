package cyder.widgets.objects;

import java.awt.*;

/**
 * A node object used for the pathfinding widget.
 */
public class PathNode {
    /**
     * The node's x value.
     */
    private int x;

    /**
     * The node's y value.
     */
    private int y;

    /**
     * The node's g value.
     */
    private double g = Integer.MAX_VALUE;

    /**
     * The node's heuristic value.
     */
    private double h = Integer.MAX_VALUE;

    /**
     * The node's parent.
     */
    private PathNode parent;

    /**
     * Constructs a new path node.
     *
     * @param x the initial x value
     * @param y the initial y value
     */
    public PathNode(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Constructs a new path node with 0,0 as the x,y.
     */
    public PathNode() {
        this(0,0);
    }

    /**
     * Constructs a new path node.
     *
     * @param p the point to use as the initial x,y
     */
    public PathNode(Point p) {
        this(p.x, p.y);
    }

    /**
     * Returns the x of the node.
     *
     * @return the x of the node
     */
    public int getX() {
        return x;
    }

    /**
     * Sets the x of the node.
     *
     * @param x the x of the node
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * Returns the y of the node.
     *
     * @return the y of the node
     */
    public int getY() {
        return y;
    }

    /**
     * Sets the y of the node.
     *
     * @param y the y of the node
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * Returns the g cost of the node.
     *
     * @return the g cost of the node
     */
    public double getG() {
        return g;
    }

    /**
     * Sets the g cost of the node.
     *
     * @param g the g cost of the node
     */
    public void setG(double g) {
        this.g = g;
    }

    /**
     * Returns the h cost of the node.
     *
     * @return the h cost of the node
     */
    public double getH() {
        return h;
    }

    /**
     * Sets the h cost of the node.
     *
     * @param h the h cost of the node
     */
    public void setH(double h) {
        this.h = h;
    }

    /**
     * Returns the f cost of the node.
     *
     * @return the f cost of the node
     */
    public double getF() {
        return h + g;
    }

    /**
     * Returns the parent of the node.
     *
     * @return the parent of the node
     */
    public PathNode getParent() {
        return parent;
    }

    /**
     * Sets the parent of the node.
     *
     * @param parent the parent of the node
     */
    public void setParent(PathNode parent) {
        this.parent = parent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (!(o instanceof PathNode))
            return false;
        else {
            PathNode other = (PathNode) o;

            return other.getX() == x && other.getY() == y;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return x + ", " + y;
    }
}
