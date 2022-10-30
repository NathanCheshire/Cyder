package cyder.ui.grid;

import com.google.common.base.Preconditions;
import cyder.constants.CyderColors;
import cyder.handlers.internal.ExceptionHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.utils.StringUtil;
import cyder.widgets.PaintWidget;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Stack;
import java.util.concurrent.Semaphore;

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
     * The maximum number of nodes associated with the current grid.
     */
    private int maxNodes = Integer.MAX_VALUE;

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
    private final int gridComponentLength;

    /**
     * Whether the grid is resizable via mouse actions.
     */
    private boolean resizable;

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
        ADD,
        DELETE,
        SELECTION,
        NONE,
        COLOR_SELECTION
    }

    /**
     * The current node placing mode.
     */
    public Mode mode = Mode.ADD;

    /**
     * The width of the brush.
     */
    private int drawWidth = 1;

    /**
     * The list of nodes which evenly divide the grid component length.
     */
    private final ArrayList<Integer> increments;

    /**
     * The offset of pixels by which we must translate to center the grid in it's provided area.
     */
    private float centeringDrawOffset;

    /**
     * The linked list of callables to invoke when the next node is placed.
     */
    private final LinkedList<Runnable> runnablesForWhenNextNodePlaced = new LinkedList<>();

    /**
     * The semaphore used to ensure thread-safety.
     */
    private final Semaphore semaphore = new Semaphore(1);

    /**
     * Acquires the semaphore.
     */
    private void lock() {
        try {
            semaphore.acquire();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Releases the semaphore.
     */
    private void unlock() {
        semaphore.release();
    }

    /**
     * The list of callbacks to invoke when the grid is resized.
     */
    private final ArrayList<Runnable> onResizeCallbacks = new ArrayList<>();

    /**
     * Constructs a CyderGrid object using {@link CyderGrid#DEFAULT_NODES} and {@link CyderGrid#DEFAULT_LENGTH}.
     */
    public CyderGrid() {
        this(DEFAULT_NODES, DEFAULT_LENGTH);
    }

    /**
     * Default constructor for CyderGrid.
     *
     * @param nodes               the amount of nodes to initially draw: nodes x nodes
     * @param gridComponentLength the physical length of this component on its parent container
     */
    public CyderGrid(int nodes, int gridComponentLength) {
        Preconditions.checkArgument(nodes > 0);
        Preconditions.checkArgument(gridComponentLength >= MIN_LENGTH);

        this.nodes = nodes;
        this.gridComponentLength = gridComponentLength;

        // override add and remove methods to ensure duplicates aren't added
        grid = new LinkedList<>() {
            @Override
            public boolean add(GridNode gridNode) {
                // most recently added node with x,y pair is what will show up
                if (grid.contains(gridNode))
                    super.remove(gridNode);

                return super.add(gridNode);
            }

            @Override
            public boolean remove(Object o) {
                if (!(o instanceof GridNode other))
                    throw new IllegalArgumentException("Attempting to add non GridNode to grid");

                if (!grid.contains(other)) {
                    return false;
                } else {
                    return super.remove(other);
                }
            }
        };

        increments = getNodesForMaxWidth(gridComponentLength);

        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Finds whether the grid contains the specified node.
     *
     * @param node the node to search for
     * @return whether the provided node was found on the grid
     */
    public boolean contains(GridNode node) {
        Preconditions.checkNotNull(node);

        return grid.contains(node);
    }

    /**
     * Adds the specified node to the grid if it is not already in the grid.
     *
     * @param node the node to add to the grid if it not already on the grid
     */
    public void addNode(GridNode node) {
        Preconditions.checkNotNull(node);

        lock();
        grid.add(node);
        unlock();
    }

    /**
     * Adds a node at the provided location if it is not already on the grid.
     * The color given to the node is {@link #nodeColor}.
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
     * @param x     the x value of the grid node to add
     * @param y     the y value of the grid node to add
     * @param color the color of the node
     */
    public void addNode(int x, int y, Color color) {
        Preconditions.checkNotNull(color);

        addNode(new GridNode(color, x, y));
    }

    /**
     * Removes the specified node from the grid if it exists.
     *
     * @param node the node to remove
     */
    public void removeNode(GridNode node) {
        Preconditions.checkNotNull(node);

        lock();
        grid.remove(node);
        unlock();
    }

    /**
     * Returns the number of nodes on the grid.
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
        return grid;
    }

    /**
     * Returns the nodes on the current grid.
     *
     * @param newGrid the nodes for the current grid
     */
    public void setGridNodes(LinkedList<GridNode> newGrid) {
        Preconditions.checkNotNull(newGrid);

        grid.clear();
        lock();
        grid.addAll(newGrid);
        unlock();
    }

    /**
     * Returns the node length of a single dimension of nodes.
     *
     * @return the node length of a single dimension of nodes
     */
    public int getNodeDimensionLength() {
        return nodes;
    }

    /**
     * Sets the node dimension length.
     *
     * @param len the new node dimensional length
     */
    public void setNodeDimensionLength(int len) {
        Preconditions.checkArgument(len > 0, "Dimensional length must be at least 1");
        nodes = len;
        repaint();
        onResizeCallbacks.forEach(Runnable::run);
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
        return StringUtil.commonCyderUiToString(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);

        // failsafe
        if (nodes < minNodes || nodes > maxNodes) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(2));

        //in order to fit this many nodes, we need to figure out the length
        int squareLen = gridComponentLength / nodes;

        //bounds of drawing that we cannot draw over since it may be less if we
        // can't fit an even number of square on the grid
        int drawTo = squareLen * nodes;

        // you can't split x 1's into y (y < x) places, pigeonhole principle isn't met
        // this is why there might seem to be a lot of space left over sometimes when
        // the number of nodes is relatively high

        //keep the grid centered on its parent
        int offset = (gridComponentLength - drawTo) / 2;
        g2d.translate(offset, offset);
        centeringDrawOffset = offset;

        //fill the background in if it is set
        if (getBackground() != null) {
            g2d.setColor(getBackground());
            g2d.fillRect(0, 0, drawTo, drawTo);
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

        // try since concurrent modification exceptions are sometimes thrown
        // this will help debugging
        try {
            lock();
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
            unlock();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
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
                addMouseWheelListener(zoomListener);
            } else {
                removeMouseWheelListener(zoomListener);
            }
        }

        this.resizable = resizable;
    }

    /**
     * Adds the listener which allows nodes to be placed via click on the grid.
     */
    public void installClickListener() {
        addMouseListener(clickListener);
    }

    /**
     * Installs the click and drag placer to this grid.
     */
    public void installClickAndDragPlacer() {
        removeMouseListener(clickListener);
        removeMouseMotionListener(dragListener);
        installDragListener();
        installClickListener();
    }

    /**
     * Removes the click and drag placers from this grid.
     */
    public void uninstallClickAndDragPlacer() {
        removeMouseListener(clickListener);
        removeMouseMotionListener(dragListener);
    }

    /**
     * Used for grid operations triggered by a user mouse event to account for
     * the possible relative node.
     *
     * @param event     the mouse event of where the user clicked/dragged on
     * @param dragEvent whether the event was a drag event
     */
    private void handleEventAccountingForOffset(MouseEvent event, boolean dragEvent) {
        // get regular x and y not accounting for any zoom
        int x = (int) ((event.getX() - centeringDrawOffset) / (gridComponentLength / nodes));
        int y = (int) ((event.getY() - centeringDrawOffset) / (gridComponentLength / nodes));

        GridNode node = new GridNode(nodeColor, x, y);
        invokeRunnables();

        if (dragEvent) {
            lastNodePlacedViaDrag = node;
        }

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

        if (!dragEvent && grid.contains(node)) {
            removeNode(node);
        }
        // otherwise add/remove as normal
        else {
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
            } else if (mode == Mode.SELECTION) {
                handleCropMovement(new Point(event.getX(), event.getY()));
            } else if (mode == Mode.COLOR_SELECTION) {
                for (GridNode gridNode : grid) {
                    if (gridNode.getX() == x && gridNode.getY() == y) {
                        PaintWidget.setNewPaintColor(gridNode.getColor());
                    }
                }
            } else throw new IllegalStateException("Unaccounted for mode: " + mode);
        }

        // redraw grid
        revalidate();
        repaint();
    }

    /**
     * Sets the state of the grid to the provided state.
     *
     * @param nextState the new grid state
     */
    public void setGridState(LinkedList<GridNode> nextState) {
        Preconditions.checkNotNull(nextState);

        if (backwardStates.isEmpty() || !backwardStates.peek().equals(nextState)) {
            saveState(new LinkedList<>(grid));
        }

        grid = nextState;

        // new history so clear forward traversal
        forwardStates.clear();

        // clear selection
        point1Selection = null;
        point2Selection = null;
    }

    /**
     * The listener which allows nodes to be placed on the grid via click.
     */
    private final MouseAdapter clickListener = new MouseAdapter() {
        // only on click for region selection and state saving
        @Override
        public void mousePressed(MouseEvent e) {
            // push grid as a past state if it is not equal to the last one
            if (backwardStates.isEmpty() || !backwardStates.peek().equals(grid))
                saveState(new LinkedList<>(grid));

            // new history so clear forward traversal
            forwardStates.clear();

            // set new starting point for selection
            point1Selection = new Point(e.getX(), e.getY());
            point2Selection = null;
        }

        // handle the placement or removal of the node
        @Override
        public void mouseClicked(MouseEvent e) {
            handleEventAccountingForOffset(e, false);
        }
    };

    /**
     * Adds the listener which allows nodes to be placed via drag events on the grid.
     */
    public void installDragListener() {
        addMouseMotionListener(dragListener);
    }

    private GridNode lastNodePlacedViaDrag;

    /**
     * The listener which allows nodes to be placed during drag events.
     */
    private final MouseMotionListener dragListener = new MouseMotionAdapter() {
        @Override
        public void mouseDragged(MouseEvent e) {
            // if in the same square as the last node placed by a drag, skip
            int x = (int) ((e.getX() - centeringDrawOffset) / (gridComponentLength / nodes));
            int y = (int) ((e.getY() - centeringDrawOffset) / (gridComponentLength / nodes));

            if (lastNodePlacedViaDrag != null &&
                    x == lastNodePlacedViaDrag.getX() &&
                    y == lastNodePlacedViaDrag.getY())
                return;

            handleEventAccountingForOffset(e, true);
        }
    };

    /**
     * The number returned by {@link MouseWheelEvent#getWheelRotation()} for scroll in events.
     */
    private static final int zoomingInWheelRotation = -1;

    /**
     * The number returned by {@link MouseWheelEvent#getWheelRotation()} for scroll out events.
     */
    private static final int zoomingOutWheelRotation = 1;

    /**
     * The listener used to increase/decrease the number of nodes on the grid.
     */
    private final MouseWheelListener zoomListener = new MouseAdapter() {
        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            if (e.isControlDown()) {
                if (e.getWheelRotation() == zoomingInWheelRotation) {
                    if (nodes - 1 < minNodes)
                        return;

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
                } else if (e.getWheelRotation() == zoomingOutWheelRotation) {
                    if (smoothScrolling) {
                        for (Integer increment : increments) {
                            if (increment > nodes) {
                                if (increment > maxNodes)
                                    return;

                                nodes = increment;
                                break;
                            }
                        }
                    } else {
                        nodes += 1;
                    }
                } else {
                    return;
                }

                repaint();
                onResizeCallbacks.forEach(Runnable::run);
            }
        }
    };

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
     * Returns the maximum number of nodes for a dimension of this instance.
     *
     * @return the maximum number of nodes for a dimension of this instance
     */
    public int getMaxNodes() {
        return maxNodes;
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
     * Sets the maximum number of nodes for a dimension of this instance.
     *
     * @param maxNodes the maximum number of nodes for a dimension of this instance
     */
    public void setMaxNodes(int maxNodes) {
        this.maxNodes = maxNodes;
    }

    /**
     * Sets the node color the provided color.
     *
     * @param nodeColor the color of the next nodes to place on the grid
     */
    public void setNodeColor(Color nodeColor) {
        this.nodeColor = Preconditions.checkNotNull(nodeColor);
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
     * Whether grid zooming should only be allowed in increments which result in perfect divisibility.
     */
    private boolean smoothScrolling;

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
        this.mode = Preconditions.checkNotNull(mode);

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
     * Whether states should be saved for possible traversal.
     */
    private boolean saveStates = true;

    /**
     * Returns whether save states is toggled on.
     *
     * @return whether save states is toggled on
     */
    public boolean isSaveStates() {
        return saveStates;
    }

    /**
     * Sets whether states should be saved.
     *
     * @param saveStates whether states should be saved
     */
    public void setSaveStates(boolean saveStates) {
        this.saveStates = saveStates;
    }

    /**
     * Pushes the provided state to the backward states list.
     * This method exists so that state saving can be easily turned on or off.
     *
     * @param pushState the state to push to the backward states
     */
    private void saveState(LinkedList<GridNode> pushState) {
        Preconditions.checkNotNull(pushState);

        if (saveStates) {
            backwardStates.push(pushState);
        }
    }

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
            saveState(new LinkedList<>(grid));

            // set to next state
            grid = forwardStates.pop();

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
            forwardStates.push(new LinkedList<>(grid));

            // set to last state
            grid = backwardStates.pop();

            // repaint grid
            repaint();
        }
    }

    // --------------
    // Cropping logic
    // --------------

    /**
     * The first point of selection.
     */
    private Point point1Selection;

    /**
     * The second point of selection. This point can be updated as the mouse moves if it is being held down.
     */
    private Point point2Selection;

    /**
     * Handles a crop action and updates the highlighted region if intended to be drawn.
     */
    private void handleCropMovement(Point node) {
        Preconditions.checkNotNull(node);

        // no region selected so assign the start to both, when the movement ends
        // we can figure out which corner is which
        if (point1Selection == null) {
            point1Selection = new Point(node.x, node.y);
        }

        point2Selection = new Point(node.x, node.y);
    }

    /**
     * Crops the grid to the currently selected region.
     */
    public void cropToSelectedRegion() {
        if (point1Selection != null && point2Selection != null
                && point1Selection != point2Selection) {

            // get points
            int firstX = (int) ((point1Selection.getX() - centeringDrawOffset) / (gridComponentLength / nodes));
            int firstY = (int) ((point1Selection.getY() - centeringDrawOffset) / (gridComponentLength / nodes));
            int secondX = (int) ((point2Selection.getX() - centeringDrawOffset) / (gridComponentLength / nodes));
            int secondY = (int) ((point2Selection.getY() - centeringDrawOffset) / (gridComponentLength / nodes));

            // find min and max
            int minX = Math.min(firstX, secondX);
            int minY = Math.min(firstY, secondY);
            int maxX = Math.max(firstX, secondX);
            int maxY = Math.max(firstY, secondY);

            nodes = Math.max(maxX - minX, maxY - minY);

            for (Integer increment : increments) {
                if (increment > nodes) {
                    nodes = increment;
                    break;
                }
            }

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
            saveState(new LinkedList<>(grid));
            grid = croppedOffsetNodes;

            // reset selection
            point1Selection = null;
            point2Selection = null;

            repaint();
        }
    }

    /**
     * Delete the nodes in the selected region.
     */
    public void deleteSelectedRegion() {
        if (point1Selection != null && point2Selection != null
                && point1Selection != point2Selection) {

            // get points
            int firstX = (int) ((point1Selection.getX() - centeringDrawOffset) / (gridComponentLength / nodes));
            int firstY = (int) ((point1Selection.getY() - centeringDrawOffset) / (gridComponentLength / nodes));
            int secondX = (int) ((point2Selection.getX() - centeringDrawOffset) / (gridComponentLength / nodes));
            int secondY = (int) ((point2Selection.getY() - centeringDrawOffset) / (gridComponentLength / nodes));

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

            // now invert cropped nodes, remove them from the new grid
            LinkedList<GridNode> deletedState = new LinkedList<>(grid);
            deletedState.removeAll(croppedNodes);

            // push current state and set new grid
            saveState(new LinkedList<>(grid));
            grid = deletedState;

            // reset selection
            point1Selection = null;
            point2Selection = null;

            // repaint
            repaint();
        }
        // no region so delete everything
        else {
            saveState(new LinkedList<>(grid));
            grid = new LinkedList<>();
        }
    }

    /**
     * Rotates the nodes in the selected region by 90 degrees to the left.
     */
    @SuppressWarnings("SuspiciousNameCombination") /* parameters seem reversed for matrix rotation logic */
    public void rotateRegion() {
        int firstX = 0;
        int firstY = 0;
        int secondX = (int) (gridComponentLength - centeringDrawOffset) / (gridComponentLength / nodes);
        int secondY = (int) (gridComponentLength - centeringDrawOffset) / (gridComponentLength / nodes);

        // find min and max
        int topLeftX = Math.min(firstX, secondX);
        int topLeftY = Math.min(firstY, secondY);
        int bottomRightX = Math.max(firstX, secondX);
        int bottomRightY = Math.max(firstY, secondY);

        if (point1Selection != null && point2Selection != null
                && point1Selection != point2Selection) {

            // get points
            firstX = (int) ((point1Selection.getX() - centeringDrawOffset) / (gridComponentLength / nodes));
            firstY = (int) ((point1Selection.getY() - centeringDrawOffset) / (gridComponentLength / nodes));
            secondX = (int) ((point2Selection.getX() - centeringDrawOffset) / (gridComponentLength / nodes));
            secondY = (int) ((point2Selection.getY() - centeringDrawOffset) / (gridComponentLength / nodes));

            // find min and max
            topLeftX = Math.min(firstX, secondX);
            topLeftY = Math.min(firstY, secondY);
            bottomRightX = Math.max(firstX, secondX);
            bottomRightY = Math.max(firstY, secondY);
        }

        // the new state to push/add to
        LinkedList<GridNode> newState = new LinkedList<>();

        // center of rotation is the average of the min/max points
        Point centerOfRotation = new Point((topLeftX + bottomRightX) / 2,
                (topLeftY + bottomRightY) / 2);

        for (GridNode refNode : grid) {
            // if in bounds of selected region
            if (refNode.getX() >= topLeftX && refNode.getX() < bottomRightX
                    && refNode.getY() >= topLeftY && refNode.getY() < bottomRightY) {

                // if not the origin
                if (!refNode.getPoint().equals(centerOfRotation)) {
                    // subtract point of rotation
                    Point newPoint = new Point(refNode.getX() - centerOfRotation.x,
                            refNode.getY() - centerOfRotation.y);

                    // rotate around the new origin
                    newPoint = new Point(newPoint.y, newPoint.x * -1);

                    // add point of rotation back
                    newPoint = new Point(newPoint.x + centerOfRotation.x,
                            newPoint.y + centerOfRotation.y);

                    // construct new node and add to new state
                    newState.add(new GridNode(refNode.getColor(), newPoint.x, newPoint.y));
                } else {
                    // origin
                    newState.add(refNode);
                }
            }
            // otherwise add to new state regularly
            else {
                newState.add(refNode);
            }
        }

        // push current state and set new grid
        saveState(new LinkedList<>(grid));
        grid = newState;

        if (point1Selection != null && point2Selection != null
                && point1Selection != point2Selection) {
            // init new selection points,
            // point1Selection is topLeft, point2Selection is bottomRight
            Point newTopLeft = new Point(point2Selection.x, point1Selection.y);
            Point newBottomRight = new Point(point1Selection.x, point2Selection.y);

            // lossless conversion to mouse space
            double halfNodeLen = (gridComponentLength / (float) nodes) / 2.0;
            double rotationX = (((gridComponentLength * centerOfRotation.x) / (float) nodes)
                    + centeringDrawOffset) + halfNodeLen;
            double rotationY = (((gridComponentLength * centerOfRotation.y) / (float) nodes)
                    + centeringDrawOffset) + halfNodeLen;

            // subtract point of rotation
            newTopLeft.setLocation(newTopLeft.x - rotationX, newTopLeft.y - rotationY);
            newBottomRight.setLocation(newBottomRight.x - rotationX, newBottomRight.y - rotationY);

            // rotate around new origin
            newTopLeft.setLocation(newTopLeft.y, newTopLeft.x * -1);
            newBottomRight.setLocation(newBottomRight.y, newBottomRight.x * -1);

            // add point of rotation back
            newTopLeft.setLocation(newTopLeft.x + rotationX, newTopLeft.y + rotationY);
            newBottomRight.setLocation(newBottomRight.x + rotationX, newBottomRight.y + rotationY);

            // set to global points used for selected region
            point1Selection = newTopLeft;
            point2Selection = newBottomRight;
        }

        repaint();
    }

    /**
     * Reflects the selected region across the horizontal axis.
     */
    public void reflectRegionHorizontally() {
        int firstX = 0;
        int firstY = 0;
        int secondX = (int) (gridComponentLength - centeringDrawOffset) / (gridComponentLength / nodes);
        int secondY = (int) (gridComponentLength - centeringDrawOffset) / (gridComponentLength / nodes);

        // find min and max
        int topLeftX = Math.min(firstX, secondX);
        int topLeftY = Math.min(firstY, secondY);
        int bottomRightX = Math.max(firstX, secondX);
        int bottomRightY = Math.max(firstY, secondY);

        if (point1Selection != null && point2Selection != null
                && point1Selection != point2Selection) {

            // get points
            firstX = (int) ((point1Selection.getX() - centeringDrawOffset) / (gridComponentLength / nodes));
            firstY = (int) ((point1Selection.getY() - centeringDrawOffset) / (gridComponentLength / nodes));
            secondX = (int) ((point2Selection.getX() - centeringDrawOffset) / (gridComponentLength / nodes));
            secondY = (int) ((point2Selection.getY() - centeringDrawOffset) / (gridComponentLength / nodes));

            // find min and max
            topLeftX = Math.min(firstX, secondX);
            topLeftY = Math.min(firstY, secondY);
            bottomRightX = Math.max(firstX, secondX);
            bottomRightY = Math.max(firstY, secondY);
        }

        // the new state to push/add to
        LinkedList<GridNode> newState = new LinkedList<>();

        // center of reflection
        int centerLine = (bottomRightX - topLeftX) / 2 + topLeftX;

        // for nodes in current grid
        for (GridNode refNode : grid) {
            // if in bounds of selected region
            if (refNode.getX() >= topLeftX && refNode.getX() < bottomRightX
                    && refNode.getY() >= topLeftY && refNode.getY() < bottomRightY) {
                // on left so flip to right
                if (refNode.getX() < centerLine) {
                    newState.add(new GridNode(refNode.getColor(),
                            centerLine + (centerLine - refNode.getX()), refNode.getY()));
                }
                // on right so flip to left
                else if (refNode.getX() > centerLine) {
                    newState.add(new GridNode(refNode.getColor(),
                            centerLine - (refNode.getX() - centerLine), refNode.getY()));
                }
                // else on line so no action
                else {
                    newState.add(refNode);
                }
            }
            // otherwise add to new state regularly
            else {
                newState.add(refNode);
            }
        }

        // push current state and set new grid
        saveState(new LinkedList<>(grid));
        grid = newState;

        // repaint
        repaint();
    }

    /**
     * Converts the provided point in mouse space to the equivalent
     * grid node based on the current node count and length.
     *
     * @param mousePoint the mouse point of a dimension
     * @return the converted grid point of the dimension
     */
    private float mouseToGridSpace(int mousePoint) {
        return (mousePoint - centeringDrawOffset) / (gridComponentLength / (float) nodes);
    }

    /**
     * Converts the provided grid node to it's mouse space equivalent
     * based on the current node count and component length. The value returned is the
     * node's center point. Subtract half the current node's length to obtain the top
     * left corner of the node.
     *
     * @param gridPoint the point on the grid to convert to mouse point
     * @return the grid point converted to mouse point
     */
    public float gridToMouseSpace(int gridPoint) {
        float halfNodeLen = (gridComponentLength / (float) nodes) / 2.0f;

        // account for node length and shift to node's center
        return (((gridComponentLength * gridPoint) / (float) nodes) + centeringDrawOffset) + halfNodeLen;
    }

    /**
     * Adds the provided runnable to invoke when the next node is placed.
     *
     * @param runnable the runnable to invoke when the next node is placed
     */
    public void invokeWhenNodePlaced(Runnable runnable) {
        runnablesForWhenNextNodePlaced.add(Preconditions.checkNotNull(runnable));
    }

    /**
     * Invokes all the runnables currently added to the list.
     */
    private void invokeRunnables() {
        for (Runnable runnable : runnablesForWhenNextNodePlaced) {
            runnable.run();
        }

        runnablesForWhenNextNodePlaced.clear();
    }

    /**
     * Returns a linked list of all nodes with the provided color.
     *
     * @param color the color of the nodes to find on the grid
     * @return a linked list of all nodes with the provided color
     */
    public LinkedList<GridNode> getNodesOfColor(Color color) {
        Preconditions.checkNotNull(color);

        LinkedList<GridNode> ret = new LinkedList<>();

        lock();
        for (GridNode node : grid) {
            if (node.getColor().equals(color)) {
                ret.add(node);
            }
        }
        unlock();

        return ret;
    }

    /**
     * Returns the grid node at the provided point if it exists, empty optional else.
     *
     * @param point the point to find a grid node at
     * @return the grid node at the point if found
     */
    public Optional<GridNode> getNodeAtPoint(Point point) {
        Preconditions.checkNotNull(point);

        lock();
        for (GridNode gridNode : grid) {
            if (gridNode.getX() == point.getX() && gridNode.getY() == point.getY()) {
                unlock();
                return Optional.of(gridNode);
            }
        }

        unlock();
        return Optional.empty();
    }

    /**
     * Removes all nodes of the provided color.
     *
     * @param color the color of the nodes to remove from the grid
     */
    public void removeNodesOfColor(Color color) {
        Preconditions.checkNotNull(color);

        LinkedList<GridNode> remove = new LinkedList<>();

        lock();
        for (GridNode node : grid) {
            if (node.getColor().equals(color)) {
                remove.add(node);
            }
        }

        grid.removeAll(remove);
        unlock();
    }

    /**
     * Adds the provided callback to invoke when the grid is resized.
     *
     * @param callback the callback
     */
    public void addOnResizeCallback(Runnable callback) {
        onResizeCallbacks.add(Preconditions.checkNotNull(callback));
    }

    /**
     * Removes the provided callback from the callbacks to invoke when the grid is resized.
     *
     * @param callback the callback to remove
     */
    public void removeOnResizeCallback(Runnable callback) {
        onResizeCallbacks.remove(Preconditions.checkNotNull(callback));
    }

    /**
     * Removes all on resize callbacks.
     */
    public void removeAllOnResizeCallbacks() {
        onResizeCallbacks.clear();
    }

    /**
     * A node component for a {@link CyderGrid}.
     */
    public static final class GridNode {
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
}
