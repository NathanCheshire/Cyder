package cyder.widgets.objects;

public class PathNode {
    private int x;
    private int y;
    private double g = Integer.MAX_VALUE;
    private double h = Integer.MAX_VALUE;
    private PathNode parent;

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
        return h + g;
    }

    public PathNode getParent() {
        return parent;
    }

    public void setParent(PathNode parent) {
        this.parent = parent;
    }

    public PathNode(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public PathNode() {
        x = 0;
        y = 0;
    }

    @Override
    public boolean equals(Object n) {
        if (n == null)
            return false;
        if (!(n instanceof PathNode))
            return false;
        else {
            return ((PathNode) n).getX() == x && ((PathNode) n).getY() == y;
        }
    }

    @Override
    public String toString() {
        return x + "," + y;
    }
}
