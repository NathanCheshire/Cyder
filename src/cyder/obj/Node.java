package cyder.obj;

public class Node {
    private int x;
    private int y;
    private double g = Double.POSITIVE_INFINITY;
    private double h = Double.POSITIVE_INFINITY;
    private double f = Double.POSITIVE_INFINITY;
    private Node parent;

    public enum NodeType {
        WALL,
        START,
        END,
        PATH, //final path in blue
        CHECKED_PATHABLE, //green squares that have been checked
        CHECKING_PATHABLE //edges of checked nodes in different color
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

    public double getG() {
        return g;
    }

    public void setG(double g) {
        this.g = g;
    }

    public double getH() {
        return h;
    }

    public void setH(double h) {
        this.h = h;
    }

    public double getF() {
        return f;
    }

    public void setF(double f) {
        this.f = f;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public Node(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Node() {
        this.x = 0;
        this.y = 0;
    }

    @Override
    public boolean equals(Object n) {
        if (n == null)
            return false;
        if (!(n instanceof Node))
            return false;
        else {
            return ((Node) n).getX() == x && ((Node) n).getY() == y;
        }
    }

    @Override
    public String toString() {
        return x + "," + y;
    }
}
