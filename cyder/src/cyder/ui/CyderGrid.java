package cyder.ui;

import cyder.constants.CyderColors;
import cyder.utilities.ReflectionUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class CyderGrid extends JLabel {
    //how many nodes should be drawn on the grid
    private int nodes;
    public static final int DEFAULT_MIN_NODES = 2;
    private int minNodes = DEFAULT_MIN_NODES;
    public static final int DEFAULT_NODES = 20;

    //the physical bounds of this
    public static final int DEFAULT_LENGTH = 400;
    public static final int MIN_LENGTH = 50;
    private final int length;

    //whether or not to allow resizing of the grid via mouse zoom in/out
    private boolean resizable = false;

    //the actual grid data structure
    private final ArrayList<GridNode> grid;

    public enum Mode {
        ADD, DELETE
    }

    public Mode mode = Mode.ADD;

    public CyderGrid() {
        this(DEFAULT_NODES, DEFAULT_LENGTH);
    }

    /**
     * Default constructor for CyderGrid.
     * @param nodes the amount of nodes to initially draw: nodes x nodes
     * @param length the physical length of this component on its parent container
     */
    public CyderGrid(int nodes, int length) {
        if (length < MIN_LENGTH)
            throw new IllegalArgumentException("Minimum length not met: length = "
                    + length + ", length width = " + MIN_LENGTH);

        this.nodes = nodes;
        this.length = length;

        grid = new ArrayList<>();
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
     * Adds the specified node to the grid if it is not already in the grid.
     *
     * @param node the node to add to the grid if it not already on the grid
     */
    public void addNode(GridNode node) {
        grid.add(node);
    }

    /**
     * Adds a node at the provided location if it is not already on the grid.
     *
     * @param x the x value of the grid node to add
     * @param y the y value of the grid node to add
     */
    public void addNode(int x, int y) {
        addNode(x, y, CyderColors.navy);
    }

    /**
     * Adds a node at the provided location if it is not already on the grid.
     *
     * @param x the x value of the grid node to add
     * @param y the y value of the grid node to add
     * @param color the color of the node
     */
    public void addNode(int x, int y, Color color) {
        grid.add(new GridNode(color, x, y));
    }

    /**
     * Removes the specified node from the grid if it exists
     * @param node the node to remove
     */
    public void removeNode(GridNode node) {
        grid.remove(node);
    }

    /**
     * Returns the count of nodes on the grid.
     *
     * @return the number of nodes on the grid
     */
    public int getNodeCount() {
        return grid.size();
    }

    /**
     * Returns the node length.
     *
     * @return the node length
     */
    public int getNodes() {
        return this.nodes;
    }

    /**
     * Clears the grid of all nodes.
     */
    public void clear() {
        grid.clear();
        repaint();
    }

    //standard
    @Override
    public String toString() {
        return ReflectionUtil.commonCyderUIReflection(this);
    }

    private float offset = 0.0f;

    //duh
    @Override
    public void paint(Graphics g) {
        super.paint(g);

        if (this != null) {
            //failsafe
            if (this.nodes < minNodes)
                return;

            Graphics2D g2d = (Graphics2D) g;
            g2d.setStroke(new BasicStroke(2));

            //in order to fit this many nodes, we need to figure out the length
            int squareLen = this.length / this.nodes;

            //bounds of drawing that we cannot draw over since it may be less if we
            // can't fit an even number of square on the grid
            int drawTo = squareLen * this.nodes;

            // you can't split x 1's into y (y < x) places, pigeonhole principle isn't met
            // this is why there might seem to be a lot of space left over sometimes when
            // the number of nodes is relatively high

            //keep the grid centered on its parent
            int offset = (this.length - drawTo) / 2;
            g2d.translate(offset, offset);
            this.offset = offset;

            //fill the background in if it is set
            if (this.getBackground() != null) {
                g2d.setColor(this.getBackground());
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
                //never draw nodes over the current limit
                if (node.getX() < 0 || node.getY() < 0 || node.getX() > nodes - 1 || node.getY() > nodes - 1)
                    continue;

                g2d.setColor(node.getColor());

                g2d.fillRect((drawGridLines ? 2 : 0) + node.getX() * squareLen,
                        (drawGridLines ? 2 : 0) + node.getY() * squareLen,
                        squareLen - (drawGridLines ? 2 : 0),
                        squareLen - (drawGridLines ? 2 : 0));
            }

            g2d.setColor(CyderColors.navy);

            //draw borders
            g2d.drawLine(1, 1, 1, drawTo);
            g2d.drawLine(1, 1, drawTo, 1);
            g2d.drawLine(drawTo, 1, drawTo, drawTo);
            g2d.drawLine(1, drawTo, drawTo, drawTo);

            //draw extended border if enabled
            if (drawExtendedBorder) {
                super.setBorder(new LineBorder(CyderColors.navy, 3));
            }
        }
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

    public void installClickPlacer() {
        this.addMouseListener(clickPlacer);
    }

    private final MouseAdapter clickPlacer = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            int clickX = e.getX();
            int clickY = e.getY();

            int x = (int) ((clickX - offset) / (length / nodes));
            int y = (int) ((clickY - offset) / (length / nodes));

            // don't add nodes if out of bounds
            if (x < 0 || y < 0 || x >= nodes || y >= nodes)
                return;

            GridNode node = new GridNode(CyderColors.navy, x, y);

            if (grid.contains(node)) {
                grid.remove(node);
            } else {
                grid.add(node);
            }

            System.out.println(grid.contains(node));

            revalidate();
            repaint();
        }
    };

    public void installDragPlacer() {
        this.addMouseMotionListener(dragPlacer);
    }

    private final MouseMotionListener dragPlacer = new MouseMotionAdapter() {
        @Override
        public void mouseDragged(MouseEvent e) {
            int clickX = e.getX();
            int clickY = e.getY();

            System.out.println(clickX + ", " + clickY);

            int x = (int) ((clickX - offset) / (length / nodes));
            int y = (int) ((clickY - offset) / (length / nodes));

            // don't add nodes if out of bounds
            if (x < 0 || y < 0 || x >= nodes || y >= nodes)
                return;

            GridNode node = new GridNode(CyderColors.navy, x, y);

            if (grid.contains(node)) {
                if (mode == Mode.DELETE)
                    grid.remove(node);
            } else {
                grid.add(node);
            }

            revalidate();
            repaint();
        }
    };

    /**
     * The listener used to increase/decrease the number of nodes on the grid.
     */
    private final MouseWheelListener zoomListener = new MouseAdapter() {
        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            if (e.isControlDown()) {
                int startNodes = nodes;

                if (e.getWheelRotation() == -1 && nodes > minNodes) {
                    nodes -= 1;
                } else {
                    nodes += 1;
                }

                if (nodes != startNodes)
                    repaint();
            }
        }
    };

    private boolean drawExtendedBorder = false;

    public boolean isDrawExtendedBorder() {
        return drawExtendedBorder;
    }

    public void setDrawExtendedBorder(boolean drawExtendedBorder) {
        this.drawExtendedBorder = drawExtendedBorder;
        repaint();
    }

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

    public int getMinNodes() {
        return minNodes;
    }

    public void setMinNodes(int minNodes) {
        this.minNodes = minNodes;
    }
}
