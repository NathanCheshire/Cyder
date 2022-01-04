package cyder.ui;

import cyder.consts.CyderColors;
import cyder.utilities.ReflectionUtil;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;

public class CyderGrid extends JLabel {
    //how many nodes should be drawn on the grid
    private int length = 0;
    public static final int MIN_LENGTH = 2;
    public static final int DEFAULT_LENGTH = 20;

    //the physical bounds of this
    public static final int DEFAULT_WIDTH = 400;
    public static final int MIN_WIDTH = 50;
    private int width;

    //whether or not to allow resizing of the grid via mouse zoom in/out
    //todo implement me getters/setters/redraw/test
    private boolean resizable = false;

    //todo implement me, getters/setters/repaint/test
    private Color backgroundColor = null;

    //the actual grid data structure
    private LinkedList<GridNode> grid;

    public CyderGrid() {
        this(DEFAULT_LENGTH, DEFAULT_WIDTH);
    }

    /**
     * Default constructor for CyderGrid.
     * @param len the amount of nodes to initially draw: len x len
     * @param width the physical width of this component on its parent container
     */
    public CyderGrid(int len, int width) {
        this.length = len;
        this.width = width;

        grid = new LinkedList<>() {
            @Override
            public boolean add(GridNode e) {
                if (!grid.contains(e))
                    return super.add(e);
                else return false;
            }
        };

        //fixme remove this
        grid.add(new GridNode(CyderColors.intellijPink, 10, 10));
    }

    @Override
    public String toString() {
        return ReflectionUtil.commonCyderUIReflection(this);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        if (this != null) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(Color.darkGray);
            g2d.setStroke(new BasicStroke(2));

            //in order to fit this many nodes, we need to figure out the length
            int squareLen = (int) Math.floor(this.width / this.length);
            System.out.println(this.length);
            System.out.println(this.width);
            System.out.println(squareLen);

            //bounds of drawing that we cannot draw over since it may be less if we
            // can't fit an even number of square on the grid
            int drawTo = squareLen * this.length;

            //fill the background in if it is set
            if (backgroundColor != null) {
                g2d.setColor(backgroundColor);
                g2d.fillRect(0,0, drawTo, drawTo);
            }

            g2d.setColor(CyderColors.navy);

            //draw vertical lines
            for (int x = 1 ; x <= drawTo - 2 ; x += squareLen) {
                g2d.drawLine(x, 1, x, drawTo - 2);
            }

            //draw horizontal lines
            for (int y = 1 ; y <= drawTo - 2 ; y += squareLen) {
                g2d.drawLine(1, y, drawTo - 2, y);
            }

            for (GridNode node : grid) {
                g2d.setColor(node.getColor());
                g2d.fillRect(2 + node.getX() * squareLen, 2 + node.getY() * squareLen,
                        squareLen - 2, squareLen - 2);
            }

            g2d.setColor(CyderColors.navy);

            //draw borders
            g2d.drawLine(1, 1, 1, drawTo);
            g2d.drawLine(1, 1, drawTo, 1);
            g2d.drawLine(drawTo, 1, drawTo, drawTo);
            g2d.drawLine(1, drawTo, drawTo, drawTo);
        }
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        this.repaint();
    }

    //nodes used for the Grid's 2D Array
    public static final class GridNode {
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

        @Override
        public boolean equals(Object node) {
            if (node instanceof GridNode) {
                return (this.x == ((GridNode) node).x && this.y == ((GridNode) node).y);
            } else return false;
        }
    }
}
