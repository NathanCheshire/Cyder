package cyder.ui;

import cyder.constants.CyderColors;
import cyder.ui.objects.GridNode;
import cyder.utilities.ReflectionUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;

/**
 * A custom UI grid component.
 */
public class CyderGrid extends JLabel {
    /**
     * The number of one dimension of nodes on the grid.
     */
    private int nodes;

    /**
     * The default dimensional number of nodes.
     */
    public static final int DEFAULT_MIN_NODES = 2;

    /**
     * The minimum nodes associated with the current grid.
     */
    private int minNodes = DEFAULT_MIN_NODES;

    /**
     * The dimension of nodes associated with the current grid.
     */
    public static final int DEFAULT_NODES = 20;

    /**
     * The default physical length of the grid component.
     */
    public static final int DEFAULT_LENGTH = 400;

    /**
     * The minimum length of the grid.
     */
    public static final int MIN_LENGTH = 50;

    /**
     * The physical length of the grid component.
     */
    private final int length;

    /**
     * Whether the grid is resizable via mouse actions.
     */
    private boolean resizable = false;

    /**
     * The list which holds the nodes to display on the grid.
     */
    private final LinkedList<GridNode> grid;

    /**
     * The color to use for new nodes added to the grid.
     */
    private Color defultNodeColor = CyderColors.navy;

    /**
     * An enum for adding/removing nodes from the grid.
     */
    public enum Mode {
        ADD, DELETE
    }

    /**
     * The current node placing mode.
     */
    public Mode mode = Mode.ADD;

    /**
     * Constructs a CyderGrid object using {@link CyderGrid#DEFAULT_NODES} and {@link CyderGrid#DEFAULT_LENGTH}.
     */
    public CyderGrid() {
        this(DEFAULT_NODES, DEFAULT_LENGTH);
    }

    /**
     * Default constructor for CyderGrid.
     *
     * @param nodes the amount of nodes to initially draw: nodes x nodes
     * @param length the physical length of this component on its parent container
     */
    public CyderGrid(int nodes, int length) {
        if (length < MIN_LENGTH)
            throw new IllegalArgumentException("Minimum length not met: length = "
                    + length + ", length width = " + MIN_LENGTH);

        this.nodes = nodes;
        this.length = length;

        // override add and remove methods to ensure duplicates aren't added
        grid = new LinkedList<>() {
            @Override
            public boolean add(GridNode gridNode) {
                if (grid.contains(gridNode))
                    return false;

                return super.add(gridNode);
            }

            @Override
            public boolean remove(Object o) {
                if (!(o instanceof GridNode))
                    throw new IllegalArgumentException("Attempting to add non GridNode to grid");

                GridNode other = (GridNode) o;

                if (!grid.contains(other)) {
                    return false;
                } else {
                    return super.remove(other);
                }
            }
        };
    }

    /**
     * Finds whether or not the grid contains the specified node.
     *
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
     * The color given to the node is {@link CyderColors#navy}.
     *
     * @param x the x value of the grid node to add
     * @param y the y value of the grid node to add
     */
    public void addNode(int x, int y) {
        addNode(x, y, defultNodeColor);
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
     * Removes the specified node from the grid if it exists.
     *
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return ReflectionUtil.commonCyderUIReflection(this);
    }

    /**
     * The offset of pixels by which we must translate to center the grid in it's provided area.
     */
    private float offset = 0.0f;

    /**
     * {@inheritDoc}
     */
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

    /**
     * Returns whether the grid is resizable.
     *
     * @return whether the grid is resizable
     */
    public boolean isResizable() {
        return resizable;
    }

    /**
     * Sets whether the grid is resizable.
     *
     * @param resizable whether the grid is resizable
     */
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

    /**
     * Adds the listener which allows nodes to be placed via click on the grid.
     */
    public void installClickPlacer() {
        this.addMouseListener(clickPlacer);
    }

    /**
     * The listener which allows nodes to be placed on the grid via click.
     */
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

            GridNode node = new GridNode(defultNodeColor, x, y);

            if (grid.contains(node)) {
                grid.remove(node);
            } else {
                grid.add(node);
            }

            revalidate();
            repaint();
        }
    };

    /**
     * Adds the listener which allows nodes to be placed via drag events on the grid.
     */
    public void installDragPlacer() {
        this.addMouseMotionListener(dragPlacer);
    }

    /**
     * The listener which allows nodes to be placed during drag events.
     */
    private final MouseMotionListener dragPlacer = new MouseMotionAdapter() {
        @Override
        public void mouseDragged(MouseEvent e) {
            int clickX = e.getX();
            int clickY = e.getY();

            int x = (int) ((clickX - offset) / (length / nodes));
            int y = (int) ((clickY - offset) / (length / nodes));

            // don't add nodes if out of bounds
            if (x < 0 || y < 0 || x >= nodes || y >= nodes)
                return;

            GridNode node = new GridNode(defultNodeColor, x, y);

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

    /**
     * Whether to draw the actual bounds border of the component.
     */
    private boolean drawExtendedBorder = false;

    /**
     * Returns whether to draw the extended border.
     *
     * @return whether to draw the extended border
     */
    public boolean isDrawExtendedBorder() {
        return drawExtendedBorder;
    }

    /**
     * Sets whether to draw the extended border.
     *
     * @param drawExtendedBorder whether to draw the extended border
     */
    public void setDrawExtendedBorder(boolean drawExtendedBorder) {
        this.drawExtendedBorder = drawExtendedBorder;
        repaint();
    }

    /**
     * Whether grid lines should be drawn.
     */
    private boolean drawGridLines = true;

    /**
     * Returns whether grid lines should be drawn.
     *
     * @return whether grid lines should be drawn
     */
    public boolean isDrawGridLines() {
        return drawGridLines;
    }

    /**
     * Sets whether grid lines should be drawn.
     *
     * @param drawGridLines whether grid lines should be drawn
     */
    public void setDrawGridLines(boolean drawGridLines) {
        this.drawGridLines = drawGridLines;
        repaint();
    }


    /**
     * Returns the minimum number of nodes for a dimension for this instance.
     *
     * @return the minimum number of nodes for a dimension for this instance
     */
    public int getMinNodes() {
        return minNodes;
    }

    /**
     * Sets the minimum number of nodes for a dimension of this instance.
     *
     * @param minNodes the minimum number of nodes for a dimension of this instance
     */
    public void setMinNodes(int minNodes) {
        this.minNodes = minNodes;
    }

    /**
     * Returns the default color to use for new nodes.
     *
     * @return the default color to use for new nodes
     */
    public Color getDefultNodeColor() {
        return defultNodeColor;
    }

    /**
     * Sets the default color to use for new nodes.
     *
     * @param defultNodeColor the default color to use for new nodes
     */
    public void setDefultNodeColor(Color defultNodeColor) {
        this.defultNodeColor = defultNodeColor;
    }
}
