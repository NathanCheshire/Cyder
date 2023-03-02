package cyder.ui.drag;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.console.Console;
import cyder.constants.CyderColors;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.props.Props;
import cyder.strings.ToStringUtil;
import cyder.ui.UiUtil;
import cyder.ui.drag.button.CloseButton;
import cyder.ui.drag.button.MinimizeButton;
import cyder.ui.drag.button.PinButton;
import cyder.ui.frame.CyderFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A drag label to be used for CyderFrames borders.
 */
public class CyderDragLabel extends JLabel {
    /**
     * The default height for drag labels.
     * The Cyder standard is set for top drag labels and is 30px.
     */
    public static final int DEFAULT_HEIGHT = Props.dragLabelHeight.getValue();

    /**
     * The spacing between drag label buttons.
     */
    private static final int BUTTON_SPACING = 2;

    /**
     * The padding between the left and right of the drag label buttons.
     */
    private static final int BUTTON_PADDING = 5;

    /**
     * The width of this DragLabel.
     */
    private int width;

    /**
     * The height of this DragLabel.
     */
    private int height;

    /**
     * The associated frame for this label.
     */
    private final CyderFrame effectFrame;

    /**
     * The x offset used for dragging.
     */
    private final AtomicInteger xOffset = new AtomicInteger();

    /**
     * The y offset used for dragging.
     */
    private final AtomicInteger yOffset = new AtomicInteger();

    /**
     * The background color of this drag label.
     */
    private Color backgroundColor = CyderColors.getGuiThemeColor();

    /**
     * Whether dragging is currently enabled.
     */
    private final AtomicBoolean draggingEnabled = new AtomicBoolean(true);

    /**
     * The pin button for this drag label.
     */
    private PinButton pinButton;

    /**
     * The list of buttons to paint on the right of the drag label.
     */
    private ArrayList<Component> rightButtonList;

    /**
     * The list of buttons to paint on the left of the drag label.
     * If any buttons exist in this list then the title label is restricted/moved to
     * the center position.
     */
    private final ArrayList<Component> leftButtonList = new ArrayList<>();

    /**
     * The type of drag label this drag label is.
     */
    private final DragLabelType type;

    /**
     * Constructs a new drag label with the provided bounds and frame to effect.
     *
     * @param width       the width of the drag label, typically the width of the effect frame
     * @param height      the height of the drag label, typically {@link CyderDragLabel#DEFAULT_HEIGHT}
     * @param effectFrame the cyder frame object to control
     * @param type        the type of drag label this drag label should be
     */
    public CyderDragLabel(int width, int height, CyderFrame effectFrame, DragLabelType type) {
        Preconditions.checkNotNull(effectFrame);
        Preconditions.checkNotNull(type);

        this.width = width;
        this.height = height;
        this.effectFrame = effectFrame;
        this.type = type;

        initializeRightButtonList();

        setSize(width, height);
        setOpaque(true);
        setFocusable(false);
        setBackground(backgroundColor);

        addListeners();

        invokeSpecialActionsBasedOnType();

        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Creates and adds the necessary {@link MouseListener}s,
     * {@link MouseMotionListener}s, and {@link WindowListener}s.
     */
    private void addListeners() {
        AtomicBoolean leftMouseButtonPressed = new AtomicBoolean(false);
        AtomicInteger mouseX = new AtomicInteger();
        AtomicInteger mouseY = new AtomicInteger();

        addMouseMotionListener(new MouseMotionListener() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!leftMouseButtonPressed.get()) return;

                int x = e.getXOnScreen();
                int y = e.getYOnScreen();

                if (effectFrame.isFocused() && draggingEnabled.get()) {
                    int setX = x - mouseX.get() - xOffset.get();
                    int setY = y - mouseY.get() - yOffset.get();

                    effectFrame.setLocation(setX, setY);

                    effectFrame.setRestorePoint(new Point(setX, setY));
                }
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX.set(e.getX());
                mouseY.set(e.getY());
            }
        });

        addMouseListener(new MouseAdapter() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    leftMouseButtonPressed.set(true);
                    effectFrame.startDragEvent();
                }
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    leftMouseButtonPressed.set(false);
                    effectFrame.endDragEvent();
                }
            }
        });

        effectFrame.addWindowListener(new WindowAdapter() {
            /**
             * {@inheritDoc}
             */
            @Override
            public void windowDeiconified(WindowEvent e) {
                effectFrame.setVisible(true);
                effectFrame.requestFocus();
                UiUtil.requestFramePosition(effectFrame.getRestorePoint(), effectFrame);
            }
        });
    }

    /**
     * Invokes any special setup actions based on the type of this drag label.
     */
    private void invokeSpecialActionsBasedOnType() {
        if (type != DragLabelType.TOP) {
            removeRightButtons();
            refreshRightButtons();
        }

        if (type != DragLabelType.FULL) {
            CyderDragLabel generatingLabel = this;
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (e.getButton() != MouseEvent.BUTTON1) {
                        Point generatingPoint = e.getPoint();
                        effectFrame.getTooltipMenuController().show(generatingPoint, generatingLabel);
                    }
                }
            });
        }
    }

    /**
     * Determines the contents of the right button list depending on the type of this drag label.
     */
    private void initializeRightButtonList() {
        if (type == DragLabelType.TOP) {
            rightButtonList = buildRightButtonList();
        } else {
            rightButtonList = new ArrayList<>();
        }
    }

    /**
     * Sets the width of this drag label.
     *
     * @param width the width of this drag label
     */
    public void setWidth(int width) {
        super.setSize(width, getHeight());
        this.width = width;

        refreshButtons();
        revalidate();
    }

    /**
     * Sets the height of this drag label.
     *
     * @param height the height of this drag label
     */
    public void setHeight(int height) {
        super.setSize(getWidth(), height);
        this.height = height;

        refreshButtons();
        revalidate();
    }

    /**
     * Sets the size of this drag label.
     *
     * @param width  the width of this drag label
     * @param height the height of this drag label
     */
    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        this.width = width;
        this.height = height;

        refreshButtons();
        revalidate();
    }

    /**
     * Returns the width of this drag label.
     *
     * @return the width of this drag label
     */
    @Override
    public int getWidth() {
        return width;
    }

    /**
     * Returns the height of this drag label.
     *
     * @return the height of this drag label
     */
    @Override
    public int getHeight() {
        return height;
    }

    /**
     * Sets the background color of this drag label.
     *
     * @param color the background color of this drag label
     */
    public void setColor(Color color) {
        backgroundColor = Preconditions.checkNotNull(color);
        repaint();
    }

    /**
     * Returns the associated CyderFrame.
     *
     * @return the associated CyderFrame
     */
    public CyderFrame getEffectFrame() {
        return effectFrame;
    }

    /**
     * Disables dragging.
     */
    public void disableDragging() {
        draggingEnabled.set(false);
    }

    /**
     * Enables dragging.
     */
    public void enableDragging() {
        draggingEnabled.set(true);
    }

    /**
     * Returns whether dragging is enabled.
     *
     * @return whether dragging is enabled
     */
    public boolean isDraggingEnabled() {
        return draggingEnabled.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return ToStringUtil.commonUiComponentToString(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = Integer.hashCode(width);
        ret = 31 * ret + Integer.hashCode(height);
        ret = 31 * ret + backgroundColor.hashCode();
        ret = 31 * ret + type.hashCode();
        ret = 31 * ret + leftButtonList.hashCode();
        ret = 31 * ret + rightButtonList.hashCode();
        return ret;
    }

    /**
     * Builds and returns the default right button list which contains the buttons
     * in the following order: minimize, pin window, close.
     *
     * @return the default right button list
     */
    private ArrayList<Component> buildRightButtonList() {
        ArrayList<Component> ret = new ArrayList<>();

        MinimizeButton minimizeButton = new MinimizeButton(effectFrame);
        ret.add(minimizeButton);

        pinButton = new PinButton(effectFrame, getInitialPinButtonState());
        ret.add(pinButton);

        CloseButton closeButton = new CloseButton();
        closeButton.setClickAction(effectFrame::dispose);
        ret.add(closeButton);

        return ret;
    }

    /**
     * Returns the initial state of the pin button.
     *
     * @return the initial state of the pin button
     */
    private PinButton.PinState getInitialPinButtonState() {
        PinButton.PinState ret = PinButton.PinState.DEFAULT;

        CyderFrame consoleFrame = Console.INSTANCE.getConsoleCyderFrame();
        if (consoleFrame != null) {
            boolean consolePinned = consoleFrame.getTopDragLabel()
                    .getPinButton().getCurrentState() == PinButton.PinState.CONSOLE_PINNED;
            boolean thisIsConsole = consoleFrame.equals(effectFrame);
            if (consolePinned && !thisIsConsole) {
                ret = PinButton.PinState.FRAME_PINNED;
            }
        }

        return ret;
    }

    /**
     * Returns the button from the right button list at the provided index.
     *
     * @param index the index of the button to be returned
     * @return the button at the provided index
     */
    public Component getRightButton(int index) {
        Preconditions.checkArgument(index >= 0);
        Preconditions.checkArgument(index < rightButtonList.size());

        return rightButtonList.get(index);
    }

    /**
     * Returns the button from the left button list at the provided index.
     *
     * @param index the index of the button to be returned
     * @return the button at the provided index
     */
    public Component getLeftButton(int index) {
        Preconditions.checkArgument(index >= 0);
        Preconditions.checkArgument(index < leftButtonList.size());

        return leftButtonList.get(index);
    }

    /**
     * Adds the button to the right drag label at the given index.
     *
     * @param button   the button with all the properties already set such as listeners,
     *                 visuals, etc. to add to the button list
     * @param addIndex the index to append the button to in the button list
     */
    public void addRightButton(Component button, int addIndex) {
        Preconditions.checkNotNull(button);
        Preconditions.checkArgument(!rightButtonList.contains(button));

        rightButtonList.add(addIndex, button);
        refreshRightButtons();
    }

    /**
     * Adds the button to the left drag label at the given index.
     *
     * @param button   the button with all the properties already set such as listeners,
     *                 visuals, etc. to add to the button list
     * @param addIndex the index to append the button to in the button list
     */
    public void addLeftButton(Component button, int addIndex) {
        Preconditions.checkNotNull(button);
        Preconditions.checkArgument(!leftButtonList.contains(button));

        leftButtonList.add(addIndex, button);
        refreshLeftButtons();
    }

    /**
     * Moves the provided button from the right list to the specified index.
     *
     * @param button   the button to move to the specified index
     * @param newIndex the index to move the specified button to
     */
    public void setRightButtonIndex(Component button, int newIndex) {
        Preconditions.checkNotNull(button);
        Preconditions.checkArgument(rightButtonList.contains(button));
        Preconditions.checkArgument(newIndex < rightButtonList.size());

        int oldIndex = -1;

        for (int i = 0 ; i < rightButtonList.size() ; i++) {
            if (button == rightButtonList.get(i)) {
                oldIndex = i;
                break;
            }
        }

        Component popButton = rightButtonList.remove(oldIndex);
        rightButtonList.add(newIndex, popButton);

        refreshRightButtons();
    }

    /**
     * Moves the provided button from the left list to the specified index.
     *
     * @param button   the button to move to the specified index
     * @param newIndex the index to move the specified button to
     */
    public void setLeftButtonIndex(Component button, int newIndex) {
        Preconditions.checkNotNull(button);
        Preconditions.checkArgument(leftButtonList.contains(button));
        Preconditions.checkArgument(newIndex < leftButtonList.size());

        int oldIndex = -1;

        for (int i = 0 ; i < leftButtonList.size() ; i++) {
            if (button == leftButtonList.get(i)) {
                oldIndex = i;
                break;
            }
        }

        Component popButton = leftButtonList.remove(oldIndex);
        leftButtonList.add(newIndex, popButton);

        refreshLeftButtons();
    }

    /**
     * Moves the button at the oldIndex from the right button list
     * to the new index and pushes any other buttons out of the way.
     *
     * @param oldIndex the old position of the right button
     * @param newIndex the index to move the targeted right button to
     */
    public void setRightButtonIndex(int oldIndex, int newIndex) {
        Preconditions.checkArgument(oldIndex >= 0 && oldIndex < rightButtonList.size());
        Preconditions.checkArgument(newIndex >= 0 && newIndex < rightButtonList.size());

        Component popButton = rightButtonList.remove(oldIndex);
        rightButtonList.add(newIndex, popButton);
        refreshRightButtons();
    }

    /**
     * Moves the button at the oldIndex from the left button list
     * to the new index and pushes any other buttons out of the way.
     *
     * @param oldIndex the old position of the left button
     * @param newIndex the index to move the targeted right button to
     */
    public void setLeftButtonIndex(int oldIndex, int newIndex) {
        Preconditions.checkArgument(oldIndex >= 0 && oldIndex < leftButtonList.size());
        Preconditions.checkArgument(newIndex >= 0 && newIndex < leftButtonList.size());

        Component popButton = leftButtonList.remove(oldIndex);
        leftButtonList.add(newIndex, popButton);
        refreshLeftButtons();
    }

    /**
     * Removes the button in the right button list at the provided index.
     *
     * @param removeIndex index of button to remove
     */
    public void removeRightButton(int removeIndex) {
        Preconditions.checkArgument(removeIndex >= 0);
        Preconditions.checkArgument(removeIndex < rightButtonList.size());

        remove(rightButtonList.get(removeIndex));
        rightButtonList.remove(removeIndex);
        refreshRightButtons();
    }

    /**
     * Removes the button in the let button list at the provided index.
     *
     * @param removeIndex index of button to remove
     */
    public void removeLeftButton(int removeIndex) {
        Preconditions.checkArgument(removeIndex >= 0);
        Preconditions.checkArgument(removeIndex < leftButtonList.size());

        remove(leftButtonList.get(removeIndex));
        leftButtonList.remove(removeIndex);
        refreshLeftButtons();
    }

    /**
     * Removes the provided button from the left button list.
     *
     * @param component the drag label button or text button to remove from the left button list
     */
    public void removeLeftButton(Component component) {
        Preconditions.checkNotNull(component);

        leftButtonList.remove(component);
        refreshLeftButtons();
    }

    /**
     * Returns the current right button list.
     *
     * @return the current right button list
     */
    public ImmutableList<Component> getRightButtonList() {
        return ImmutableList.copyOf(rightButtonList);
    }

    /**
     * Returns the current left button list.
     *
     * @return the current left button list
     */
    public ImmutableList<Component> getLeftButtonList() {
        return ImmutableList.copyOf(leftButtonList);
    }

    /**
     * Returns whether the right button list contains buttons.
     *
     * @return whether the right button list contains buttons
     */
    public boolean hasRightButtons() {
        return rightButtonList.size() > 0;
    }

    /**
     * Returns whether the left button list contains buttons.
     *
     * @return whether the left button list contains buttons
     */
    public boolean hasLeftButtons() {
        return leftButtonList.size() > 0;
    }

    /**
     * Removes all buttons contained in the right button list from this drag label.
     * The contents of the right button list remains the same.
     */
    public void removeRightButtons() {
        rightButtonList.forEach(this::remove);
    }

    /**
     * Removes all buttons contained in the left button list from this drag label.
     * The contents of the left button list remains the same.
     */
    public void removeLeftButtons() {
        leftButtonList.forEach(this::remove);
    }

    /**
     * Refreshes and repaints the button list.
     */
    public void refreshButtons() {
        refreshRightButtons();
        refreshLeftButtons();
    }

    /**
     * Refreshes all right buttons and their positions.
     */
    public void refreshRightButtons() {
        removeRightButtons();
        effectFrame.revalidateTitlePosition();

        ArrayList<Component> reversedRightButtons = new ArrayList<>();
        for (int i = rightButtonList.size() - 1 ; i >= 0 ; i--) {
            reversedRightButtons.add(rightButtonList.get(i));
        }

        int currentXStart = width - BUTTON_PADDING;

        for (Component rightButtonComponent : reversedRightButtons) {
            int buttonWidth = rightButtonComponent.getWidth();
            int buttonHeight = rightButtonComponent.getHeight();

            int y = buttonHeight > DEFAULT_HEIGHT ? 0 : DEFAULT_HEIGHT / 2 - buttonHeight / 2;

            currentXStart -= (buttonWidth + 2 * BUTTON_SPACING);
            rightButtonComponent.setBounds(currentXStart, y, buttonWidth, buttonHeight);
            add(rightButtonComponent);

            rightButtonComponent.repaint();
        }

        revalidate();
        repaint();
    }

    /**
     * Refreshes all left buttons and their positions.
     */
    public void refreshLeftButtons() {
        if (leftButtonList == null) return;

        removeLeftButtons();
        effectFrame.revalidateTitlePosition();

        int currentXStart = BUTTON_PADDING;

        for (Component leftButtonComponent : leftButtonList) {
            int buttonWidth = leftButtonComponent.getWidth();
            int buttonHeight = leftButtonComponent.getHeight();

            int y = buttonHeight > DEFAULT_HEIGHT ? 0 : DEFAULT_HEIGHT / 2 - buttonHeight / 2;

            leftButtonComponent.setBounds(currentXStart, y, buttonWidth, buttonHeight);
            currentXStart += buttonWidth + 2 * BUTTON_SPACING;
            add(leftButtonComponent);

            leftButtonComponent.repaint();
        }

        revalidate();
        repaint();
    }

    /**
     * Returns the pin button for this CyderFrame.
     *
     * @return the pin button for this CyderFrame
     */
    public PinButton getPinButton() {
        return pinButton;
    }

    /**
     * Sets the pin button of this drag label.
     *
     * @param pinButton the pin button of this drag label
     */
    public void setPinButton(PinButton pinButton) {
        this.pinButton = Preconditions.checkNotNull(pinButton);
    }

    /**
     * Returns the x offset of this drag label.
     *
     * @return the x offset of this drag label
     */
    public int getXOffset() {
        return xOffset.get();
    }

    /**
     * Returns the y offset of this drag label.
     *
     * @return the y offset of this drag label
     */
    public int getYOffset() {
        return yOffset.get();
    }

    /**
     * Sets the x offset of this drag label.
     *
     * @param xOffset the x offset of this drag label
     */
    public void setXOffset(int xOffset) {
        Preconditions.checkArgument(xOffset >= 0);

        this.xOffset.set(xOffset);
    }

    /**
     * Sets the y offset of this drag label.
     *
     * @param yOffset the y offset of this drag label
     */
    public void setYOffset(int yOffset) {
        Preconditions.checkArgument(yOffset >= 0);

        this.yOffset.set(yOffset);
    }

    /**
     * Returns the type of drag label this drag label is.
     *
     * @return the type of drag label this drag label is
     */
    public DragLabelType getType() {
        return type;
    }
}
