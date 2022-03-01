package cyder.ui;

import cyder.constants.CyderColors;
import cyder.ui.objects.GridNode;
import cyder.utilities.ReflectionUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Stack;

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
    private LinkedList<GridNode> grid;

    /**
     * The color to use for new nodes added to the grid.
     */
    private Color nodeColor = CyderColors.navy;

    /**
     * An enum for adding/removing nodes from the grid.
     */
    public enum Mode {
        ADD, DELETE, SELECTION
    }

    /**
     * The current node placing mode.
     */
    public Mode mode = Mode.ADD;

    private int drawWidth = 1;

    private final ArrayList<Integer> increments;

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
                    super.remove(gridNode);

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

        increments = getNodesForMaxWidth(length);
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
        addNode(x, y, nodeColor);
    }

    /**
     * Adds a node at the provided location if it is not already on the grid.
     *
     * @param x the x value of the grid node to add
     * @param y the y value of the grid node to add
     * @param color the color of the node
     */
    public void addNode(int x, int y, Color color) {
        addNode(new GridNode(color, x, y));
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
     * Returns the nodes on the current grid.
     *
     * @return the nodes on the current grid
     */
    public LinkedList<GridNode> getGridNodes() {
        return this.grid;
    }

    /**
     * Returns the nodes on the current grid.
     *
     * @param newGrid the nodes for the current grid
     */
    public void setGridNodes(LinkedList<GridNode> newGrid) {
        this.grid.clear();
        this.grid.addAll(newGrid);
    }

    /**
     * Returns the node length.
     *
     * @return the node length
     */
    public int getNodesLength() {
        return this.nodes;
    }

    /**
     * Clears the grid of all nodes.
     */
    public void clearGrid() {
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

            // draw all nodes on grid
            for (GridNode node : grid) {
                // set color for this node
                g2d.setColor(node.getColor());

                // get the x and y of this node
                int trueX = node.getX();
                int trueY = node.getY();

                // fill it
                g2d.fillRect((drawGridLines ? 2 : 0) + trueX * squareLen,
                        (drawGridLines ? 2 : 0) + trueY * squareLen,
                        squareLen - (drawGridLines ? 2 : 0),
                        squareLen - (drawGridLines ? 2 : 0));
            }

            // set color back to draw borders
            g2d.setColor(CyderColors.navy);

            // draw crop selection if specified
            if (mode == Mode.SELECTION && point1Selection != null && point2Selection != null) {
                int relX = 0;
                int relY = 0;

                g2d.drawLine((point1Selection.x + relX), point1Selection.y + relY,
                        point1Selection.x + relX, point2Selection.y + relY);
                g2d.drawLine(point1Selection.x + relX, point1Selection.y + relY,
                        point2Selection.x + relX, point1Selection.y + relY);
                g2d.drawLine(point1Selection.x + relX, point2Selection.y + relY,
                        point2Selection.x + relX, point2Selection.y + relY);
                g2d.drawLine(point2Selection.x + relX, point1Selection.y + relY,
                        point2Selection.x + relX, point2Selection.y + relY);
            }

            // draw borders borders
            g2d.drawLine(1, 1, 1, drawTo);
            g2d.drawLine(1, 1, drawTo, 1);
            g2d.drawLine(drawTo, 1, drawTo, drawTo);
            g2d.drawLine(1, drawTo, drawTo, drawTo);

            // draw extended, true border if enabled
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
     * Installs the click and drag placer to this grid.
     */
    public void installClickAndDragPlacer() {
        installDragPlacer();
        installClickPlacer();
    }

    /**
     * Used for ALL grid operations and accounts for
     * the possible relative node.
     *
     * @param event the mouse event of where the user clicked/dragged on
     * @param dragEvent whether the event was a drag event
     */
    private void handleEventAccountingForOffset(MouseEvent event, boolean dragEvent) {
        // get regular x and y not accounting for any zoom
        int x = (int) ((event.getX() - offset) / (length / nodes));
        int y = (int) ((event.getY() - offset) / (length / nodes));

        GridNode node = new GridNode(nodeColor, x, y);

        // make sure node isn't out of bounds
        if (x < 0 || y < 0 || x >= nodes || y >= nodes)
            return;

        // the nodes to add/remove
        LinkedList<GridNode> nodesInBoundsOfClick = new LinkedList<>();

        // simply add the center node
        if (drawWidth == 1) {
            nodesInBoundsOfClick.add(node);
        }
        // calculate points in the circle
        else {
            int topLeftX = x - drawWidth;
            int topLeftY = y - drawWidth;
            int bottomRightX = x + drawWidth;
            int bottomRightY = y + drawWidth;

            for (int iterativeX = topLeftX ; iterativeX < bottomRightX ; iterativeX++) {
                for (int iterativeY = topLeftY ; iterativeY < bottomRightY ; iterativeY++) {
                    int distance = (int) Math.sqrt(Math.pow(iterativeX - x, 2) + Math.pow(iterativeY - y, 2));

                    if (distance < drawWidth)
                        nodesInBoundsOfClick.add(new GridNode(nodeColor, iterativeX, iterativeY));
                }
            }
        }

        // add nodes based off of the center point and width
        if (mode == Mode.ADD) {
            for (GridNode addNode : nodesInBoundsOfClick) {
                addNode(addNode);
            }
        }
        // remove nodes based off of the center point and width
        else if (mode == Mode.DELETE) {
            for (GridNode removeNode : nodesInBoundsOfClick) {
                removeNode(removeNode);
            }
        } else {
            handleCropMovement(new Point(event.getX(), event.getY()));
        }

        // redraw grid
        revalidate();
        repaint();
    }

    /**
     * The listener which allows nodes to be placed on the grid via click.
     */
    private final MouseAdapter clickPlacer = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
            // push grid as a past state if it is not equal to the last one
            if (backwardStates.isEmpty())
                backwardStates.push(new LinkedList<>(grid));
            else if (!backwardStates.peek().equals(grid))
                backwardStates.push(new LinkedList<>(grid));

            // new history so clear forward traversal
            forwardStates.clear();

            // set new starting point for selection
            point1Selection = new Point(e.getX(), e.getY());
            point2Selection = null;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            handleEventAccountingForOffset(e, false);
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
            handleEventAccountingForOffset(e, true);
        }
    };

    /**
     * The listener used to increase/decrease the number of nodes on the grid.
     */
    private final MouseWheelListener zoomListener = new MouseAdapter() {
        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            if (e.isControlDown()) {
                // zooming in
                if (e.getWheelRotation() == -1 && nodes > minNodes) {
                    if (smoothScrolling) {
                        for (int i = increments.size() - 1 ; i >= 0 ; i--) {
                            if (increments.get(i) < nodes) {
                                nodes = increments.get(i);
                                break;
                            }
                        }
                    } else {
                        nodes -= 1;
                    }
                }
                // zooming out
                else {
                    if (smoothScrolling) {
                        for (Integer increment : increments) {
                            if (increment > nodes) {
                                nodes = increment;
                                break;
                            }
                        }
                    } else {
                        nodes += 1;
                    }
                }

                // redraw after the zoom
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
    public Color getNodeColor() {
        return nodeColor;
    }

    /**
     * Sets the default color to use for new nodes.
     *
     * @param nodeColor the default color to use for new nodes
     */
    public void setNodeColor(Color nodeColor) {
        this.nodeColor = nodeColor;
    }

    /**
     * Whether grid zooming should only be allowed in increments which result in perfect divisibility.
     */
    private boolean smoothScrolling = false;

    /**
     * Returns whether grid zooming is only allowed in perfect increments.
     *
     * @return whether grid zooming is only allowed in perfect increments
     */
    public boolean isSmoothScrolling() {
        return smoothScrolling;
    }

    /**
     * Sets whether grid zooming is only allowed in perfect increments.
     *
     * @param smoothScrolling whether grid zooming is only allowed in perfect increments
     */
    public void setSmoothScrolling(boolean smoothScrolling) {
        this.smoothScrolling = smoothScrolling;
    }

    /**
     * Returns a list containing the node length values that evenly divide the width.
     *
     * @param width the width of the associated grid
     * @return a list containing the node length values that evenly divide the width
     */
    public static ArrayList<Integer> getNodesForMaxWidth(int width) {
        ArrayList<Integer> ret = new ArrayList<>();

        for (int i = DEFAULT_MIN_NODES ; i < width ; i++) {
            if (width % i == 0)
                ret.add(i);
        }

        return ret;
    }

    /**
     * Returns the current placement mode.
     *
     * @return the current placement mode
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * Sets the current placement mode.
     *
     * @param mode the current placement mode
     */
    public void setMode(Mode mode) {
        this.mode = mode;

        // clear selection
        if (mode != Mode.SELECTION) {
            point1Selection = null;
            point2Selection = null;
        }
    }

    /**
     * Returns the current draw width.
     *
     * @return the current draw width
     */
    public int getDrawWidth() {
        return drawWidth;
    }

    /**
     * Sets the draw width.
     *
     * @param drawWidth the draw width
     */
    public void setDrawWidth(int drawWidth) {
        this.drawWidth = drawWidth;
    }

    // -------------
    // state logic
    // -------------

    /**
     * The forward states of the grid.
     */
    private final Stack<LinkedList<GridNode>> forwardStates = new Stack<>();

    /**
     * The backward states of the grid.
     */
    private final Stack<LinkedList<GridNode>> backwardStates = new Stack<>();

    /**
     * Sets the grid state to the next state if available.
     */
    public void forwardState() {
        if (!forwardStates.isEmpty()) {
            // push current state backwards
            this.backwardStates.push(new LinkedList<>(grid));

            // set to next state
            this.grid = forwardStates.pop();

            // repaint grid
            repaint();
        }
    }

    /**
     * Sets the grid state to the last state if available.
     */
    public void backwardState() {
        if (!backwardStates.isEmpty()) {
            // push current state forward
            this.forwardStates.push(new LinkedList<>(grid));

            // set to last state
            this.grid = backwardStates.pop();

            // repaint grid
            repaint();
        }
    }

    // --------------
    // croping logic
    // --------------

    private Point point1Selection = null;
    private Point point2Selection = null;

    /**
     * Handles a crop action and updates the highlighted region if intended to be drawn.
     */
    private void handleCropMovement(Point node) {
        // no region selected so assign the start to both, when the movement ends
        // we can figure out which corner is which
        if (point1Selection == null) {
            point1Selection = new Point(node.x, node.y);
        }

        point2Selection = new Point(node.x, node.y);
    }

    /**
     * Crop the grid to the currently selected region.
     */
    public void cropToRegion() {
        if (point1Selection != null && point2Selection != null
                && point1Selection != point2Selection) {

            // get points
            int firstX = (int) ((point1Selection.getX() - offset) / (length / nodes));
            int firstY = (int) ((point1Selection.getY() - offset) / (length / nodes));
            int secondX = (int) ((point2Selection.getX() - offset) / (length / nodes));
            int secondY = (int) ((point2Selection.getY() - offset) / (length / nodes));

            // find min and max
            int minX = Math.min(firstX, secondX);
            int minY = Math.min(firstY, secondY);
            int maxX = Math.max(firstX, secondX);
            int maxY = Math.max(firstY, secondY);

            LinkedList<GridNode> croppedNodes = new LinkedList<>();

            // for nodes in the current grid
            for (GridNode node : grid) {
                if (node.getX() < maxX && node.getX() >= minX && node.getY() < maxY && node.getY() >= minY) {
                    croppedNodes.add(node);
                }
            }

            LinkedList<GridNode> croppedOffsetNodes = new LinkedList<>();

            for (GridNode node : croppedNodes) {
                croppedOffsetNodes.add(new GridNode(node.getColor(),
                        node.getX() - minX, node.getY() - minY));
            }

            // push current state and set new grid
            backwardStates.push(new LinkedList<>(grid));
            grid = croppedOffsetNodes;

            // reset selection
            point1Selection = null;
            point2Selection = null;

            // todo still need to resize grid, find top left node,
            //  make that 0,0 then offset rest of nodes

            // repaint
            repaint();
        }
    }

    /**
     * Delete the nodes in the selected region.
     */
    public void deleteRegion() {
        if (point1Selection != null && point2Selection != null
                && point1Selection != point2Selection) {

            // get points
            int firstX = (int) ((point1Selection.getX() - offset) / (length / nodes));
            int firstY = (int) ((point1Selection.getY() - offset) / (length / nodes));
            int secondX = (int) ((point2Selection.getX() - offset) / (length / nodes));
            int secondY = (int) ((point2Selection.getY() - offset) / (length / nodes));

            // find min and max
            int minX = Math.min(firstX, secondX);
            int minY = Math.min(firstY, secondY);
            int maxX = Math.max(firstX, secondX);
            int maxY = Math.max(firstY, secondY);

            Rectangle boundingRect = new Rectangle(minX, minY, maxX, maxY);

            LinkedList<GridNode> croppedNodes = new LinkedList<>();

            // for nodes in the current grid
            for (GridNode node : grid) {
                if (node.getX() < maxX && node.getX() >= minX && node.getY() < maxY && node.getY() >= minY) {
                    croppedNodes.add(node);
                }
            }

            // now invert cropped nodes, remove them from the new grid
            LinkedList<GridNode> deletedState = new LinkedList<>(grid);
            deletedState.removeAll(croppedNodes);

            // push current state and set new grid
            backwardStates.push(new LinkedList<>(grid));
            grid = deletedState;

            // reset selection
            point1Selection = null;
            point2Selection = null;

            // repaint
            repaint();
        }
    }

    // todo after this re-implment zooming properly using to/from
    //  functions for pixel space vs node space
}
