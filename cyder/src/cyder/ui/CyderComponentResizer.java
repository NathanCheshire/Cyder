package cyder.ui;

import com.google.common.collect.ImmutableMap;
import cyder.handlers.internal.Logger;
import cyder.ui.objects.FocusWrappedComponent;
import cyder.utilities.ReflectionUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Map;

/**
 * A listener to allow custom, undecorated frames to be resizable.
 */
public class CyderComponentResizer extends MouseAdapter {
    /**
     * The default minimum size to use for a resizable component.
     */
    private final Dimension MINIMUM_SIZE = new Dimension(10, 10);

    /**
     * The default maximum size to use for a resizable component.
     */
    private final Dimension MAXIMUM_SIZE = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);

    /**
     * A collection of cursors to use for border cursors to indicate that the component is resizable.
     */
    private final Map<Integer, Integer> cursors = ImmutableMap.of(
            1, Cursor.N_RESIZE_CURSOR,
            2, Cursor.W_RESIZE_CURSOR,
            4, Cursor.S_RESIZE_CURSOR,
            8, Cursor.E_RESIZE_CURSOR,
            3, Cursor.NW_RESIZE_CURSOR,
            9, Cursor.NE_RESIZE_CURSOR,
            6, Cursor.SW_RESIZE_CURSOR,
            12, Cursor.SE_RESIZE_CURSOR
    );

    /**
     * The drag insets to apply to the applied component.
     */
    private Insets dragInsets;

    /**
     * The size to snap to when resizing a component.
     */
    private Dimension snapSize;

    /**
     * The direction of the current drag.
     */
    private int dragDirection;

    /**
     * The northern drag direction integer.
     */
    protected static final int NORTH = 1;

    /**
     * The western drag direction integer.
     */
    protected static final int WEST = 2;

    /**
     * The southern drag direction integer.
     */
    protected static final int SOUTH = 4;

    /**
     * The eastern drag direction integer.
     */
    protected static final int EAST = 8;

    /**
     * The source cursor from the dragging component.
     */
    private Cursor sourceCursor;

    /**
     * Whether resizing is currently underway.
     */
    private boolean currentlyResizing;

    /**
     * The current bounds of the component to resize.
     * A singular dimension is resized at a time using the direction.
     */
    private Rectangle currentBounds;

    /**
     * The point on the dragging component that was pressed.
     */
    private Point pressed;

    /**
     * Whether the dragging component has auto scrolling enabled so we can re-enable it.
     */
    private boolean componentIsAutoscroll;

    /**
     * The current minimum size for the resizable component.
     */
    private Dimension minimumSize = MINIMUM_SIZE;

    /**
     * The current maximum size for the resizable component.
     */
    private Dimension maximumSize = MAXIMUM_SIZE;

    /**
     * Constructs a new resizable component
     */
    public CyderComponentResizer() {
        this(new Insets(5, 5, 5, 5), new Dimension(1, 1));
    }

    /**
     * Constructs a new component resizer
     *
     * @param dragInsets the drag insets for the component
     * @param snapSize   the snap size for the component
     */
    private CyderComponentResizer(Insets dragInsets, Dimension snapSize) {
        setDragInsets(dragInsets);
        setSnapSize(snapSize);

        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    /**
     * Returns the current drag insets.
     *
     * @return the current drag insets
     */
    public Insets getDragInsets() {
        return dragInsets;
    }

    /**
     * Sets the current drag insets.
     *
     * @param dragInsets the current drag insets
     */
    public void setDragInsets(Insets dragInsets) {
        this.dragInsets = dragInsets;
    }

    /**
     * Returns the current maximum size.
     *
     * @return the current maximum size
     */
    public Dimension getMaximumSize() {
        return maximumSize;
    }

    /**
     * Sets the current maximum size.
     *
     * @param maximumSize the current maximum size
     */
    public void setMaximumSize(Dimension maximumSize) {
        this.maximumSize = maximumSize;
    }

    /**
     * Returns the current minimum size.
     *
     * @return the current minimum size
     */
    public Dimension getMinimumSize() {
        return minimumSize;
    }

    /**
     * Sets the current minimum size.
     *
     * @param minimumSize the current minimum size
     */
    public void setMinimumSize(Dimension minimumSize) {
        this.minimumSize = minimumSize;
    }

    /**
     * Registers the following component for resizing, adding
     * the mouse listener and mouse motion listeners to it.
     *
     * @param component the component to register
     */
    public void deregisterComponent(Component component) {
        component.removeMouseListener(this);
        component.removeMouseMotionListener(this);
    }

    /**
     * Deregisters the following component of resizing, removing
     * the mouse listener and mouse motion listeners from it.
     *
     * @param component the component to deregister
     */
    public void registerComponent(Component component) {
        component.addMouseListener(this);
        component.addMouseMotionListener(this);
    }

    /**
     * Returns the current snap size.
     *
     * @return the current snap size
     */
    public Dimension getSnapSize() {
        return snapSize;
    }

    /**
     * Sets the current snap size.
     *
     * @param snapSize the current snap size
     */
    public void setSnapSize(Dimension snapSize) {
        this.snapSize = snapSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseMoved(MouseEvent e) {
        Component source = e.getComponent();
        Point location = e.getPoint();
        dragDirection = 0;

        if (location.x < dragInsets.left)
            dragDirection += WEST;

        if (location.x > source.getWidth() - dragInsets.right - 1)
            dragDirection += EAST;

        if (location.y < dragInsets.top)
            dragDirection += NORTH;

        if (location.y > source.getHeight() - dragInsets.bottom - 1)
            dragDirection += SOUTH;

        if (dragDirection == 0) {
            source.setCursor(sourceCursor);
        } else {
            int cursorType = cursors.get(dragDirection);
            Cursor cursor = Cursor.getPredefinedCursor(cursorType);
            source.setCursor(cursor);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseEntered(MouseEvent e) {
        if (!currentlyResizing) {
            Component source = e.getComponent();
            sourceCursor = source.getCursor();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseExited(MouseEvent e) {
        if (!currentlyResizing) {
            Component source = e.getComponent();
            source.setCursor(sourceCursor);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mousePressed(MouseEvent e) {
        if (dragDirection == 0)
            return;

        currentlyResizing = true;

        Component source = e.getComponent();
        pressed = e.getPoint();
        SwingUtilities.convertPointToScreen(pressed, source);
        currentBounds = source.getBounds();

        // if source uses a layout
        if (source instanceof CyderFrame && ((CyderFrame) source).isUsingCyderLayout()) {
            // wipe past components and original focus owner
            focusableComponents.clear();
            originalFocusOwner = null;

            // save focusable state of components and disable focusing
            for (Component component : ((CyderFrame) source).getLayoutComponents()) {
                // if a layout is the component, recursively find components
                if (component instanceof CyderPanel) {
                    for (Component realComponent : recursivelyFindComponents((CyderPanel) component)) {
                        foundComponentSubroutine(realComponent);
                    }
                } else {
                    foundComponentSubroutine(component);
                }
            }
        }

        if (source instanceof JComponent && !(source instanceof CyderFrame)) {
            JComponent jc = (JComponent) source;
            componentIsAutoscroll = jc.getAutoscrolls();
            jc.setAutoscrolls(false);
        }
    }

    /**
     * Subroutine for method above
     *
     * @param component the component to perform calls and checks on
     */
    private void foundComponentSubroutine(Component component) {
        focusableComponents.add(new FocusWrappedComponent(component));

        if (component.isFocusOwner() && originalFocusOwner == null) {
            originalFocusOwner = component;
        }

        component.setFocusable(false);
    }

    /**
     * Generates a list of all components on the provided layout.
     * If a component is a layout itself, the components of that
     * layout are returned as well.
     *
     * @param parentLayout the top-level panel that holds a layout
     * @return a list of all components recusively found if one child happens to be a layout itself
     */
    private static ArrayList<Component> recursivelyFindComponents(CyderPanel parentLayout) {
        ArrayList<Component> ret = new ArrayList<>();

        for (Component child : parentLayout.getLayoutComponents()) {
            if (child instanceof CyderPanel) {
                ret.addAll(recursivelyFindComponents((CyderPanel) child));
            }

            ret.add(child);
        }

        return ret;
    }

    private Component originalFocusOwner;
    private final ArrayList<FocusWrappedComponent> focusableComponents = new ArrayList<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        currentlyResizing = false;

        Component source = e.getComponent();
        source.setCursor(sourceCursor);

        if (source instanceof JComponent && !(source instanceof CyderFrame)) {
            ((JComponent) source).setAutoscrolls(componentIsAutoscroll);
        }

        // if CyderFrame then refresh background when dragging is done and not as it is being resized
        if (source instanceof CyderFrame) {
            if (backgroundRefreshOnResize) {
                ((CyderFrame) source).refreshBackground();
            }
        }

        // for all focus wrapped components, restore their state
        for (FocusWrappedComponent component : focusableComponents) {
            component.restore();

            // restore original focus owner
            if (component.getComp().equals(originalFocusOwner)) {
                component.getComp().requestFocus();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseDragged(MouseEvent e) {
        if (!currentlyResizing)
            return;

        Component source = e.getComponent();
        Point dragged = e.getPoint();
        SwingUtilities.convertPointToScreen(dragged, source);

        changeBounds(source, dragDirection, currentBounds, pressed, dragged);

        // if a cyderframe with a panel, refresh always
        if (source instanceof CyderFrame) {
            if (backgroundRefreshOnResize) {
                ((CyderFrame) source).refreshLayout();
            }
        }
    }

    /**
     * Updates the component bounds on drag events.
     *
     * @param source    the source component
     * @param direction the direction of the drag
     * @param bounds    the old bounds of the component
     * @param pressed   the point at which the component was pressed
     * @param current   the current point at which the component is pressed
     */
    protected void changeBounds(Component source, int direction, Rectangle bounds, Point pressed, Point current) {
        if (!resizingAllowed)
            return;

        int x = bounds.x;
        int y = bounds.y;
        int width = bounds.width;
        int height = bounds.height;

        if (WEST == (direction & WEST)) {
            int drag = getDragDistance(pressed.x, current.x, snapSize.width);
            int maximum = Math.min(width + x, maximumSize.width);
            drag = getDragBounded(drag, snapSize.width, width, minimumSize.width, maximum);

            x -= drag;
            width += drag;
        }

        if (NORTH == (direction & NORTH)) {
            int drag = getDragDistance(pressed.y, current.y, snapSize.height);
            int maximum = Math.min(height + y, maximumSize.height);
            drag = getDragBounded(drag, snapSize.height, height, minimumSize.height, maximum);

            y -= drag;
            height += drag;
        }

        if (EAST == (direction & EAST)) {
            int drag = getDragDistance(current.x, pressed.x, snapSize.width);
            Dimension boundingSize = getBoundingSize(source);
            int maximum = Math.min(boundingSize.width - x, maximumSize.width);
            drag = getDragBounded(drag, snapSize.width, width, minimumSize.width, maximum);
            width += drag;
        }

        if (SOUTH == (direction & SOUTH)) {
            int drag = getDragDistance(current.y, pressed.y, snapSize.height);
            Dimension boundingSize = getBoundingSize(source);
            int maximum = Math.min(boundingSize.height - y, maximumSize.height);
            drag = getDragBounded(drag, snapSize.height, height, minimumSize.height, maximum);
            height += drag;
        }

        source.setBounds(x, y, width, height);
        source.validate();
    }

    /**
     * Whether background resizing is currently allowed.
     */
    private boolean backgroundRefreshOnResize;

    /**
     * Returns whether background resizing is enabled.
     *
     * @return whether background resizing is enabled
     */
    public boolean backgroundResizingEnabled() {
        return backgroundRefreshOnResize;
    }

    /**
     * Sets whether background resizing is enabled.
     *
     * @param b whether background resizing is enabled
     */
    public void setBackgroundResizing(Boolean b) {
        backgroundRefreshOnResize = b;
    }

    /**
     * Returns the dragged distance rounding to the nearest snap size.
     *
     * @param larger   the larger of the two values
     * @param smaller  the smaller of the two values
     * @param snapSize the current snap size
     * @return the distance dragged between the two values
     * rounded to the nearest snap size increment.
     */
    private int getDragDistance(int larger, int smaller, int snapSize) {
        int halfway = snapSize / 2;
        int drag = larger - smaller;
        drag += (drag < 0) ? -halfway : halfway;
        drag = (drag / snapSize) * snapSize;

        return drag;
    }

    /**
     * Returns the drag after accounting for possible out of bounds dragging and the snap size.
     *
     * @param drag      the current drag distance
     * @param snapSize  the current snap size
     * @param dimension the current dimension we are working with such as height
     * @param minimum   the minimum of the dimension
     * @param maximum   the maximum of the dimension
     * @return the bounded drag after accounting for the outlined conditions
     */
    private int getDragBounded(int drag, int snapSize, int dimension, int minimum, int maximum) {
        while (dimension + drag < minimum)
            drag += snapSize;
        while (dimension + drag > maximum)
            drag -= snapSize;

        return drag;
    }

    /**
     * Returns the current maximum size of the window the component is located on.
     *
     * @param source the source component
     * @return the current maximum size of the window the component is located on
     */
    private Dimension getBoundingSize(Component source) {
        if (source instanceof Window) {
            Rectangle bounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
            return new Dimension(bounds.width, bounds.height);
        } else {
            return source.getParent().getSize();
        }
    }

    /**
     * Whether resizing is allowed.
     */
    private boolean resizingAllowed = true;

    /**
     * Sets whether resizing should be allowed.
     *
     * @param b whether resizing should be allowed
     */
    public void setResizingAllowed(Boolean b) {
        resizingAllowed = b;
    }

    /**
     * Returns whether resizing is allowed.
     *
     * @return whether resizing is allowed
     */
    public boolean isResizingEnabled() {
        return resizingAllowed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return ReflectionUtil.commonCyderToString(this);
    }
}