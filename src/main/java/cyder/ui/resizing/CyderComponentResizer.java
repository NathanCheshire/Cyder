package cyder.ui.resizing;

import com.google.common.base.Preconditions;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.ui.frame.CyderFrame;
import cyder.ui.pane.CyderPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;

/**
 * A listener to allow custom, undecorated frames (typically {@link CyderFrame}s) to be resizable.
 */
public final class CyderComponentResizer extends MouseAdapter {
    /**
     * The default minimum size to use for a resizable component.
     */
    private static final Dimension defaultMinSize = new Dimension(10, 10);

    /**
     * The default maximum size to use for a resizable component.
     */
    private static final Dimension defaultMaxSize = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);

    /**
     * The default snap size.
     */
    private static final Dimension DEFAULT_SNAP_SIZE = new Dimension(1, 1);

    /**
     * The default inset value.
     */
    private static final int DEFAULT_INSET = 5;

    /**
     * A list of the focusable states of all components on the frame prior to the current resize event.
     */
    private final ArrayList<FocusWrappedComponent> focusWrappedComponents = new ArrayList<>();

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
    private Point pressedPoint;

    /**
     * Whether the dragging component has auto scrolling enabled so we can re-enable it.
     */
    private boolean componentIsAutoscroll;

    /**
     * The current minimum size for the resizable component.
     */
    private Dimension minimumSize = defaultMinSize;

    /**
     * The current maximum size for the resizable component.
     */
    private Dimension maximumSize = defaultMaxSize;

    /**
     * The original focus owner of the frame prior to a resize event.
     */
    private Component originalFocusOwner;

    /**
     * Whether background resizing is currently allowed.
     */
    private boolean refreshBackgroundOnResize;

    /**
     * Constructs a new resizable component
     */
    public CyderComponentResizer() {
        this(generateDefaultInset(), DEFAULT_SNAP_SIZE);
    }

    /**
     * Generates and returns a new default inset.
     *
     * @return a new default inset
     */
    private static Insets generateDefaultInset() {
        return new Insets(DEFAULT_INSET, DEFAULT_INSET, DEFAULT_INSET, DEFAULT_INSET);
    }

    /**
     * Constructs a new component resizer
     *
     * @param dragInsets the drag insets for the component
     * @param snapSize   the snap size for the component
     */
    private CyderComponentResizer(Insets dragInsets, Dimension snapSize) {
        setDragInsets(Preconditions.checkNotNull(dragInsets));
        setSnapSize(Preconditions.checkNotNull(snapSize));

        Logger.log(LogTag.OBJECT_CREATION, this);
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
        this.dragInsets = Preconditions.checkNotNull(dragInsets);
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
     * Sets the maximum size.
     *
     * @param maximumSize the maximum size
     */
    public void setMaximumSize(Dimension maximumSize) {
        Preconditions.checkNotNull(maximumSize);

        this.maximumSize = new Dimension(maximumSize.width, maximumSize.height);
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
     * Sets the minimum size.
     *
     * @param minimumSize the minimum size
     */
    public void setMinimumSize(Dimension minimumSize) {
        Preconditions.checkNotNull(minimumSize);
        Preconditions.checkArgument(minimumSize.width > 0);
        Preconditions.checkArgument(minimumSize.height > 0);

        this.minimumSize = new Dimension(minimumSize.width, minimumSize.height);
    }

    /**
     * Registers the following component of resizing.
     *
     * @param component the component to register
     */
    public void registerComponent(Component component) {
        Preconditions.checkNotNull(component);

        component.addMouseListener(this);
        component.addMouseMotionListener(this);
    }

    /**
     * Returns the snap size.
     *
     * @return the snap size
     */
    public Dimension getSnapSize() {
        return snapSize;
    }

    /**
     * Sets the snap size.
     *
     * @param snapSize the snap size
     */
    public void setSnapSize(Dimension snapSize) {
        Preconditions.checkNotNull(snapSize);

        this.snapSize = new Dimension(snapSize.width, snapSize.height);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("MagicConstant") /* Cursor types are safe */
    public void mouseMoved(MouseEvent e) {
        Component source = e.getComponent();
        Point location = e.getPoint();

        dragDirection = 0;

        if (location.x < dragInsets.left) {
            dragDirection += DragDirection.WEST.getDragOrdinal();
        }

        if (location.x > source.getWidth() - dragInsets.right - 1) {
            dragDirection += DragDirection.EAST.getDragOrdinal();
        }

        if (location.y < dragInsets.top) {
            dragDirection += DragDirection.NORTH.getDragOrdinal();
        }

        if (location.y > source.getHeight() - dragInsets.bottom - 1) {
            dragDirection += DragDirection.SOUTH.getDragOrdinal();
        }

        if (dragDirection == 0) {
            source.setCursor(sourceCursor);
        } else {
            int cursorType = DragDirection.get(dragDirection).getCursorOrdinal();
            Cursor cursor = Cursor.getPredefinedCursor(cursorType);
            source.setCursor(cursor);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseEntered(MouseEvent e) {
        if (currentlyResizing) return;
        Component source = e.getComponent();
        sourceCursor = source.getCursor();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseExited(MouseEvent e) {
        if (currentlyResizing) return;
        Component source = e.getComponent();
        source.setCursor(sourceCursor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mousePressed(MouseEvent e) {
        if (dragDirection == DragDirection.NO_DRAG.getDragOrdinal()) return;

        currentlyResizing = true;
        Component source = e.getComponent();
        pressedPoint = e.getPoint();
        SwingUtilities.convertPointToScreen(pressedPoint, source);
        currentBounds = source.getBounds();

        switch (source) {
            case CyderFrame frame -> {
                if (frame.isManagedUsingCyderLayout()) {
                    focusWrappedComponents.clear();
                    originalFocusOwner = null;

                    frame.getLayoutComponents().forEach(component -> {
                        if (component instanceof CyderPanel panel) {
                            findPanelComponents(panel).forEach(this::storeFocusableStateAndSetNotFocusable);
                        } else {
                            storeFocusableStateAndSetNotFocusable(component);
                        }
                    });
                }
            }
            case JComponent jComponent -> {
                componentIsAutoscroll = jComponent.getAutoscrolls();
                jComponent.setAutoscrolls(false);
            }
            default -> {}
        }
    }

    /**
     * Checks the focusable state of the provided component and saves the state
     * to {@link #focusWrappedComponents}. The component's focus state is then set to false.
     *
     * @param component the component
     */
    private void storeFocusableStateAndSetNotFocusable(Component component) {
        Preconditions.checkNotNull(component);
        focusWrappedComponents.add(new FocusWrappedComponent(component, component.isFocusable()));
        if (originalFocusOwner == null && component.isFocusOwner()) originalFocusOwner = component;
        component.setFocusable(false);
    }

    /**
     * Finds all layout components of the provided {@link CyderPanel}.
     * If one component is another panel itself (nested layouts) those components
     * are also found and added to the returned list.
     *
     * @param panel the top-level panel which holds the layout managing the frame
     * @return a list of all components/sub-components found within {@link CyderPanel}s on the frame
     */
    private static Collection<Component> findPanelComponents(CyderPanel panel) {
        Preconditions.checkNotNull(panel);

        ArrayList<Component> ret = new ArrayList<>();

        panel.getLayoutComponents().forEach(child -> {
            if (child instanceof CyderPanel childPanel) ret.addAll(findPanelComponents(childPanel));
            ret.add(child);
        });

        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        currentlyResizing = false;
        Component source = e.getComponent();
        source.setCursor(sourceCursor);

        switch (source) {
            case CyderFrame frame && refreshBackgroundOnResize -> frame.refreshBackground();
            case JComponent jComponent -> jComponent.setAutoscrolls(componentIsAutoscroll);
            default -> {}
        }

        focusWrappedComponents.forEach(FocusWrappedComponent::restoreOriginalFocusableState);
        focusWrappedComponents.stream()
                .filter(focusWrappedComponent -> focusWrappedComponent.getComponent().equals(originalFocusOwner))
                .findFirst().ifPresent(component -> component.getComponent().requestFocus());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseDragged(MouseEvent e) {
        if (!currentlyResizing) return;

        Component source = e.getComponent();
        Point dragPoint = e.getPoint();
        SwingUtilities.convertPointToScreen(dragPoint, source);
        changeBounds(source, dragDirection, currentBounds, pressedPoint, dragPoint);

        if (source instanceof CyderFrame frame && refreshBackgroundOnResize) {
            frame.revalidateLayout();
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
    private void changeBounds(Component source, int direction, Rectangle bounds, Point pressed, Point current) {
        if (!resizingAllowed) return;
        Preconditions.checkNotNull(source);
        Preconditions.checkNotNull(bounds);
        Preconditions.checkNotNull(pressed);
        Preconditions.checkNotNull(current);

        int x = bounds.x;
        int y = bounds.y;
        int width = bounds.width;
        int height = bounds.height;

        if (DragDirection.WEST.getDragOrdinal() == (direction & DragDirection.WEST.getDragOrdinal())) {
            int drag = getDragDistance(pressed.x, current.x, snapSize.width);
            int maximum = Math.min(width + x, maximumSize.width);
            drag = getDragBounded(drag, snapSize.width, width, minimumSize.width, maximum);

            x -= drag;
            width += drag;
        }

        if (DragDirection.NORTH.getDragOrdinal() == (direction & DragDirection.NORTH.getDragOrdinal())) {
            int drag = getDragDistance(pressed.y, current.y, snapSize.height);
            int maximum = Math.min(height + y, maximumSize.height);
            drag = getDragBounded(drag, snapSize.height, height, minimumSize.height, maximum);

            y -= drag;
            height += drag;
        }

        if (DragDirection.EAST.getDragOrdinal() == (direction & DragDirection.EAST.getDragOrdinal())) {
            int drag = getDragDistance(current.x, pressed.x, snapSize.width);
            Dimension boundingSize = getBoundingSize(source);
            int maximum = Math.min(boundingSize.width - x, maximumSize.width);
            drag = getDragBounded(drag, snapSize.width, width, minimumSize.width, maximum);
            width += drag;
        }

        if (DragDirection.SOUTH.getDragOrdinal() == (direction & DragDirection.SOUTH.getDragOrdinal())) {
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
     * Returns whether background resizing is enabled.
     *
     * @return whether background resizing is enabled
     */
    public boolean shouldRefreshBackgroundOnResize() {
        return this.refreshBackgroundOnResize;
    }

    /**
     * Sets whether background resizing is enabled.
     *
     * @param refreshBackgroundOnResize whether background resizing is enabled
     */
    public void setShouldRefreshBackgroundOnResize(boolean refreshBackgroundOnResize) {
        this.refreshBackgroundOnResize = refreshBackgroundOnResize;
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
        int drag = larger - smaller;
        if (drag >= 0) {
            drag += snapSize / 2;
        } else {
            drag -= snapSize / 2;
        }

        return (drag / snapSize) * snapSize;
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
        // todo need to short circuit this using math to prevent
        //  spending a while here even though that is a rare case
        while (dimension + drag < minimum) {
            drag += snapSize;
        }
        while (dimension + drag > maximum) {
            drag -= snapSize;
        }

        return drag;
    }

    /**
     * Returns the current maximum size of the {@link Window} the component is located on.
     *
     * @param component the component
     * @return the current maximum size of the {@link Window} the component is located on
     */
    private Dimension getBoundingSize(Component component) {
        Preconditions.checkNotNull(component);

        return component instanceof Window
                ? GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().getSize()
                : component.getParent().getSize();
    }

    /**
     * Whether resizing is allowed.
     */
    private boolean resizingAllowed = true;

    /**
     * Sets whether resizing should be allowed.
     *
     * @param allowed whether resizing should be allowed
     */
    public void setResizingAllowed(boolean allowed) {
        resizingAllowed = allowed;
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
        return "CyderComponentResizer{"
                + "dragInsets=" + dragInsets
                + ", snapSize=" + snapSize
                + ", dragDirection=" + dragDirection
                + ", currentlyResizing=" + currentlyResizing
                + ", currentBounds=" + currentBounds
                + ", pressedPoint=" + pressedPoint
                + ", minimumSize=" + minimumSize
                + ", maximumSize=" + maximumSize
                + ", backgroundRefreshOnResize=" + refreshBackgroundOnResize
                + ", resizingAllowed=" + resizingAllowed
                + "}";
    }
}