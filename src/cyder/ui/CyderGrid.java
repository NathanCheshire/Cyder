package cyder.ui;

import cyder.consts.CyderColors;
import cyder.utilities.ReflectionUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.LinkedList;

public class CyderGrid extends JLabel {
    //how many nodes should be drawn on the grid
    private int length = 0;
    public static final int MIN_LENGTH = 2;
    public static final int DEFAULT_LENGTH = 20;
    //todo rename these to better names
    //the physical bounds of this
    public static final int DEFAULT_WIDTH = 400;
    public static final int MIN_WIDTH = 50;
    private int width;

    //whether or not to allow resizing of the grid via mouse zoom in/out
    private boolean resizable = false;

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
    }

    /**
     * Finds whether or not the grid contains the specified node
     * @param node the node to search for
     * @return whether or not the provided node was found on the grid
     */
    public boolean contains(GridNode node) {
        return grid.contains(node);
    }

    /**
     * Adds the specified node to the grid if it is not already in the grid
     * @param node the node to add to the grid if it not already on the grid
     */
    public void addNode(GridNode node) {
        grid.add(node);
    }

    /**
     * Removes the specified node from the grid if it exists
     * @param node the node to remove
     */
    public void removeNode(GridNode node) {
        grid.remove(node);
    }

    //standard
    @Override
    public String toString() {
        return ReflectionUtil.commonCyderUIReflection(this);
    }

    //duh
    @Override
    public void paint(Graphics g) {
        super.paint(g);

        if (this != null) {
            //failsafe
            if (this.length < MIN_LENGTH)
                return;

            Graphics2D g2d = (Graphics2D) g;
            g2d.setColor(Color.darkGray);
            g2d.setStroke(new BasicStroke(2));

            //in order to fit this many nodes, we need to figure out the length
            int squareLen = (int) Math.floor(this.width / this.length);

            //bounds of drawing that we cannot draw over since it may be less if we
            // can't fit an even number of square on the grid
            int drawTo = squareLen * this.length;

            //fill the background in if it is set
            if (backgroundColor != null) {
                g2d.setColor(backgroundColor);
                g2d.fillRect(0,0, drawTo, drawTo);
            }

            g2d.setColor(CyderColors.navy);

            if (drawGridLines) {
                //draw vertical lines
                for (int x = 1 ; x <= drawTo - 2 ; x += squareLen) {
                    g2d.drawLine(x, 1, x, drawTo - 2);
                }

                //draw horizontal lines
                for (int y = 1 ; y <= drawTo - 2 ; y += squareLen) {
                    g2d.drawLine(1, y, drawTo - 2, y);
                }
            }

            for (GridNode node : grid) {
                //never draw nodes over the current limit, NEVER
                if (2 + node.getX() * squareLen + squareLen - 2 > this.width ||
                        2 + node.getY() * squareLen  + squareLen - 2 > this.getHeight())
                    continue;

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

    public boolean isResizable() {
        return resizable;
    }

    public void setResizable(boolean resizable) {
        if (this.resizable != resizable) {
            if (resizable) {
                this.addMouseWheelListener(zoomListener);
            } else {
                this.removeMouseWheelListener(zoomListener);
            }
        }

        this.resizable = resizable;
    }

    //used for incrementing/decrementing the grid size
    private final MouseWheelListener zoomListener = new MouseAdapter() {
        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            if (e.isControlDown()) {
                if (e.getWheelRotation() == -1 && length > MIN_LENGTH) {
                    length -= 1;
                } else {
                    length += 1;
                }

                repaint();
            }
        }
    };

    //todo set center automatically? setCenter methods too for x,y so that on repainting we can place ourselves there based on actual drawn
    // width and not max width

    //todo boolean for draw actual border, self explanitory

    private boolean drawGridLines = true;

    public boolean isDrawGridLines() {
        return drawGridLines;
    }

    public void setDrawGridLines(boolean drawGridLines) {
        this.drawGridLines = drawGridLines;
        repaint();
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

        //a node is equal to another one if their x and y coordinates are equal
        @Override
        public boolean equals(Object node) {
            if (node instanceof GridNode) {
                return (this.x == ((GridNode) node).x && this.y == ((GridNode) node).y);
            } else return false;
        }
    }
}
