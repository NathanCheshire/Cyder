package cyder.ui.resizing;

import com.google.common.base.Preconditions;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.props.Props;
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
    private static final int DEFAULT_INSET = Props.frameBorderLength.getValue();

    /**
     * A list of the focusable states of all components on the frame prior to the current resize event.
     */
    private final ArrayList<FocusWrappedComponent> focusWrappedComponents = new ArrayList<>();

    /**
     * The drag insets to apply to the applied component.
     */
    private final Insets dragInsets = new Insets(DEFAULT_INSET, DEFAULT_INSET, DEFAULT_INSET, DEFAULT_INSET);

    /**
     * The size to snap to when resizing a component.
     */
    private Dimension snapSize;

    /**
     * The direction of the current drag.
     */
    private DragDirection dragDirection;

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
     * Whether resizing is allowed.
     */
    private boolean resizingAllowed = true;

    /**
     * Constructs a new component resizer.
     */
    public CyderComponentResizer() {
        setSnapSize(DEFAULT_SNAP_SIZE);

        Logger.log(LogTag.OBJECT_CREATION, this);
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
     * Registers the provided component to resize events managed by this resizer.
     *
     * @param component the component to register
     */
    public void registerComponent(Component component) {
        Preconditions.checkNotNull(component);

        component.addMouseListener(this);
        component.addMouseMotionListener(this);
    }

    /**
     * De-registers the provided component from resize events managed by this resizer.
     *
     * @param component the component to de-register
     */
    public void deRegisterComponent(Component component) {
        Preconditions.checkNotNull(component);

        component.removeMouseListener(this);
        component.removeMouseMotionListener(this);
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
    public void mouseMoved(MouseEvent e) {
        Component source = e.getComponent();
        Point location = e.getPoint();

        int dragDirectionSum = 0;
        if (location.x < dragInsets.left) {
            dragDirectionSum += DragDirection.WEST.getDragOrdinal();
        }
        if (location.x > source.getWidth() - dragInsets.right - 1) {
            dragDirectionSum += DragDirection.EAST.getDragOrdinal();
        }
        if (location.y < dragInsets.top) {
            dragDirectionSum += DragDirection.NORTH.getDragOrdinal();
        }
        if (location.y > source.getHeight() - dragInsets.bottom - 1) {
            dragDirectionSum += DragDirection.SOUTH.getDragOrdinal();
        }

        dragDirection = DragDirection.getFromDragOrdinal(dragDirectionSum);
        source.setCursor(dragDirection.getPredefinedCursor());
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
        if (dragDirection == DragDirection.NO_DRAG) return;

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
        changeBounds(source, dragDirection.getDragOrdinal(), currentBounds, pressedPoint, dragPoint);

        if (source instanceof CyderFrame frame && refreshBackgroundOnResize) {
            frame.revalidateLayout();
        }
    }

    /**
     * Updates the component bounds on drag events.
     *
     * @param source       the source component
     * @param direction    the direction of the drag
     * @param bounds       the old bounds of the component
     * @param pressedPoint the point at which the component was pressed
     * @param dragPoint    the current press point during a drag event
     */
    private void changeBounds(Component source, int direction, Rectangle bounds, Point pressedPoint, Point dragPoint) {
        if (!resizingAllowed) return;
        Preconditions.checkNotNull(source);
        Preconditions.checkNotNull(bounds);
        Preconditions.checkNotNull(pressedPoint);
        Preconditions.checkNotNull(dragPoint);

        int x = bounds.x;
        int y = bounds.y;
        int width = bounds.width;
        int height = bounds.height;

        if (DragDirection.WEST.getDragOrdinal() == (direction & DragDirection.WEST.getDragOrdinal())) {
            int drag = getDragDistance(pressedPoint.x, dragPoint.x, snapSize.width);
            int maximum = Math.min(width + x, maximumSize.width);
            drag = getBoundedDragLength(drag, snapSize.width, width, minimumSize.width, maximum);
            x -= drag;
            width += drag;
        }
        if (DragDirection.NORTH.getDragOrdinal() == (direction & DragDirection.NORTH.getDragOrdinal())) {
            int drag = getDragDistance(pressedPoint.y, dragPoint.y, snapSize.height);
            int maximum = Math.min(height + y, maximumSize.height);
            drag = getBoundedDragLength(drag, snapSize.height, height, minimumSize.height, maximum);
            y -= drag;
            height += drag;
        }
        if (DragDirection.EAST.getDragOrdinal() == (direction & DragDirection.EAST.getDragOrdinal())) {
            int drag = getDragDistance(dragPoint.x, pressedPoint.x, snapSize.width);
            Dimension boundingSize = getBoundingSize(source);
            int maximum = Math.min(boundingSize.width - x, maximumSize.width);
            drag = getBoundedDragLength(drag, snapSize.width, width, minimumSize.width, maximum);
            width += drag;
        }
        if (DragDirection.SOUTH.getDragOrdinal() == (direction & DragDirection.SOUTH.getDragOrdinal())) {
            int drag = getDragDistance(dragPoint.y, pressedPoint.y, snapSize.height);
            Dimension boundingSize = getBoundingSize(source);
            int maximum = Math.min(boundingSize.height - y, maximumSize.height);
            drag = getBoundedDragLength(drag, snapSize.height, height, minimumSize.height, maximum);
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
     * @param dragDistance        the current drag distance
     * @param dimensionalSnapSize the snap size
     * @param dimensionalLength   the length of the dimension, i.e. width or height
     * @param minimum             the minimum of the dimensionalLength
     * @param maximum             the maximum of the dimensionalLength
     * @return the bounded drag after accounting for the outlined conditions
     */
    private int getBoundedDragLength(int dragDistance, int dimensionalSnapSize,
                                     int dimensionalLength,
                                     int minimum, int maximum) {
        while (dimensionalLength + dragDistance < minimum) dragDistance += dimensionalSnapSize;
        while (dimensionalLength + dragDistance > maximum) dragDistance -= dimensionalSnapSize;

        return dragDistance;
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
