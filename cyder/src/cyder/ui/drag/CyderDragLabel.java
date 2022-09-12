package cyder.ui.drag;

import com.google.common.base.Preconditions;
import cyder.annotations.ForReadability;
import cyder.console.Console;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.handlers.internal.Logger;
import cyder.ui.frame.CyderFrame;
import cyder.utils.ReflectionUtil;
import cyder.utils.StringUtil;
import cyder.utils.UiUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class to be used for CyderFrames, the parent is expected to be an instance of CyderFrame.
 */
public class CyderDragLabel extends JLabel {
    /**
     * The possible types of drag labels.
     */
    public enum Type {
        /**
         * The top of the frame. This is the only drag label that builds the default right button list.
         */
        TOP,
        /**
         * The bottom of the frame.
         */
        BOTTOM,
        /**
         * The left of the frame.
         */
        LEFT,
        /**
         * The right of the frame.
         */
        RIGHT,
        /**
         * The drag label takes up the full content pane or is the content pane.
         */
        FULL
    }

    /**
     * The default height for drag labels. The Cyder standard for top labels is 30 pixels.
     */
    public static final int DEFAULT_HEIGHT = 30;

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
    private final AtomicInteger xOffset;

    /**
     * The y offset used for dragging.
     */
    private final AtomicInteger yOffset;

    /**
     * The background color of this drag label.
     */
    private Color backgroundColor;

    /**
     * Whether dragging is currently enabled.
     */
    private final AtomicBoolean draggingEnabled;

    /**
     * The pin button for this drag label.
     */
    private PinButton pinButton;

    /**
     * The list of buttons to paint on the right of the drag label.
     */
    private LinkedList<Component> rightButtonList;

    /**
     * The list of buttons to paint on the left of the drag label.
     * If any buttons exist in this list then the title label is restricted/moved to
     * the center position.
     */
    private LinkedList<Component> leftButtonList;

    /**
     * The type of drag label this drag label is.
     */
    private final Type type;

    /**
     * Constructs a new drag label with the provided bounds and frame to effect.
     *
     * @param width       the width of the drag label, typically the width of the effect frame
     * @param height      the height of the drag label, typically {@link CyderDragLabel#DEFAULT_HEIGHT}
     * @param effectFrame the cyder frame object to control
     * @param type        the type of drag label this drag label should be
     */
    public CyderDragLabel(int width, int height, CyderFrame effectFrame, Type type) {
        this.width = width;
        this.height = height;
        this.effectFrame = Preconditions.checkNotNull(effectFrame);
        this.backgroundColor = CyderColors.getGuiThemeColor();
        this.type = Preconditions.checkNotNull(type);

        leftButtonList = new LinkedList<>();

        /* This is better for readability */
        if (type == Type.TOP) {
            rightButtonList = buildRightButtonList();
        } else {
            rightButtonList = new LinkedList<>();
        }

        setSize(width, height);
        setOpaque(true);
        setFocusable(false);
        setBackground(backgroundColor);

        xOffset = new AtomicInteger();
        yOffset = new AtomicInteger();

        draggingEnabled = new AtomicBoolean(true);

        addMouseMotionListener(createDraggingMouseMotionListener(
                effectFrame, draggingEnabled, xOffset, yOffset));
        addMouseListener(createOpacityAnimationMouseListener(effectFrame));

        effectFrame.addWindowListener(createWindowListener(effectFrame));

        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    /**
     * Creates a mouse motion listener to allow the provided frame to be dragged.
     *
     * @param effectFrame     the frame the motion listener will be applied to
     * @param draggingEnabled whether dragging should be allowed
     * @param xOffset         the current frame x offset
     * @param yOffset         the current frame y offset
     * @return a mouse motion listener to allow the provided frame to be dragged
     */
    private static MouseMotionListener createDraggingMouseMotionListener(
            CyderFrame effectFrame, AtomicBoolean draggingEnabled,
            AtomicInteger xOffset, AtomicInteger yOffset) {
        AtomicInteger mouseX = new AtomicInteger();
        AtomicInteger mouseY = new AtomicInteger();

        return new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int x = e.getXOnScreen();
                int y = e.getYOnScreen();

                if (effectFrame != null && effectFrame.isFocused() && draggingEnabled.get()) {
                    int setX = x - mouseX.get() - xOffset.get();
                    int setY = y - mouseY.get() - yOffset.get();

                    effectFrame.setLocation(setX, setY);

                    effectFrame.setRestoreX(setX);
                    effectFrame.setRestoreY(setY);
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX.set(e.getX());
                mouseY.set(e.getY());
            }
        };
    }

    /**
     * Creates the opacity animation mouse listener for the provided frame.
     *
     * @param effectFrame the frame to be used for the opacity animation
     * @return the mouse listener for the opacity animation
     */
    private static MouseListener createOpacityAnimationMouseListener(CyderFrame effectFrame) {
        return new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                effectFrame.startDragEvent();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                effectFrame.endDragEvent();
            }
        };
    }

    /**
     * Create the common window listener for drag labels to handle minimizing and restoring frame positions.
     *
     * @param effectFrame the frame  the window listener will be applied to
     * @return the constructed window listener
     */
    private static WindowListener createWindowListener(CyderFrame effectFrame) {
        return new WindowAdapter() {
            @Override
            public void windowDeiconified(WindowEvent e) {
                int restoreX = effectFrame.getRestoreX();
                int restoreY = effectFrame.getRestoreY();

                effectFrame.setVisible(true);
                effectFrame.requestFocus();
                UiUtil.requestFramePosition(restoreX, restoreY, effectFrame);
            }

            @Override
            public void windowIconified(WindowEvent e) {
                if (effectFrame.getRestoreX() == Integer.MAX_VALUE
                        || effectFrame.getRestoreY() == Integer.MAX_VALUE) {
                    effectFrame.setRestoreX(effectFrame.getX());
                    effectFrame.setRestoreY(effectFrame.getY());
                }
            }
        };
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
        backgroundColor = color;
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
        return ReflectionUtil.commonCyderUiToString(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = Integer.hashCode(width);
        ret = 31 * ret + Integer.hashCode(height);
        ret = 31 * ret + backgroundColor.hashCode();
        ret = 31 * ret + Objects.hashCode(leftButtonList);
        ret = 31 * ret + Objects.hashCode(rightButtonList);
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof CyderDragLabel)) {
            return false;
        }

        CyderDragLabel other = (CyderDragLabel) o;

        return other.getWidth() == getWidth()
                && other.getHeight() == getHeight()
                && other.getBackground() == getBackground();
    }

    /**
     * Builds and returns the default right button list which contains the buttons
     * in the following order: minimize, pin window, close.
     *
     * @return the default right button list
     */
    private LinkedList<Component> buildRightButtonList() {
        LinkedList<Component> ret = new LinkedList<>();

        MinimizeButton minimizeButton = new MinimizeButton(effectFrame);
        ret.add(minimizeButton);

        pinButton = new PinButton(effectFrame, getInitialPinButtonState());
        ret.add(pinButton);

        CloseButton closeButton = new CloseButton(effectFrame);
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
            boolean weAreConsole = consoleFrame.equals(effectFrame);
            if (consolePinned && !weAreConsole) {
                ret = PinButton.PinState.FRAME_PINNED;
            }
        }

        return ret;
    }

    /**
     * The font to use for text buttons.
     */
    private static final Font TEXT_BUTTON_FONT = CyderFonts.DEFAULT_FONT_SMALL;

    /**
     * Generates a drag label text button.
     *
     * @param text        the text of the button
     * @param tooltip     the tooltip of the button
     * @param clickAction the action to invoke when the button is clicked
     * @return the text button
     */
    public static JLabel generateTextButton(String text, String tooltip, Runnable clickAction) {
        Preconditions.checkNotNull(text);
        text = StringUtil.getTrimmedText(text);

        Preconditions.checkArgument(!text.isEmpty());
        Preconditions.checkNotNull(tooltip);
        Preconditions.checkArgument(!tooltip.isEmpty());
        Preconditions.checkNotNull(clickAction);

        JLabel ret = new JLabel(text);
        ret.setForeground(CyderColors.vanilla);
        ret.setFont(TEXT_BUTTON_FONT);
        ret.setToolTipText(tooltip);
        ret.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                ret.setForeground(CyderColors.regularRed);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ret.setForeground(CyderColors.vanilla);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                clickAction.run();
            }
        });

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
        Preconditions.checkArgument(oldIndex >= 0);
        Preconditions.checkArgument(oldIndex < rightButtonList.size());
        Preconditions.checkArgument(newIndex >= 0);
        Preconditions.checkArgument(newIndex < rightButtonList.size());

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
        Preconditions.checkArgument(oldIndex >= 0);
        Preconditions.checkArgument(oldIndex < leftButtonList.size());
        Preconditions.checkArgument(newIndex >= 0);
        Preconditions.checkArgument(newIndex < leftButtonList.size());

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
     * Returns the current right button list.
     *
     * @return the current right button list
     */
    public LinkedList<Component> getRightButtonList() {
        return rightButtonList;
    }

    /**
     * Returns the current left button list.
     *
     * @return the current left button list
     */
    public LinkedList<Component> getLeftButtonList() {
        return leftButtonList;
    }

    /**
     * Sets the right button list to the one provided.
     *
     * @param rightButtonList the button list to use for this drag label's right list
     */
    public void setRightButtonList(LinkedList<Component> rightButtonList) {
        removeRightButtons();
        this.rightButtonList = rightButtonList;
        refreshRightButtons();
    }

    /**
     * Sets the left button list to the one provided.
     *
     * @param leftButtonList the button list to use for this drag label's left list
     */
    public void setLeftButtonList(LinkedList<Component> leftButtonList) {
        removeLeftButtons();
        this.leftButtonList = leftButtonList;
        refreshLeftButtons();
    }

    /**
     * Removes all buttons from the right button list from this drag label.
     */
    public void removeRightButtons() {
        if (rightButtonList == null) {
            return;
        }

        for (Component button : rightButtonList) {
            remove(button);
        }
    }

    /**
     * Removes all buttons from the left button list from this drag label.
     */
    public void removeLeftButtons() {
        if (leftButtonList == null) {
            return;
        }

        for (Component button : leftButtonList) {
            remove(button);
        }
    }

    /**
     * Refreshes and repaints the button list.
     */
    public void refreshButtons() {
        refreshRightButtons();
        refreshLeftButtons();
    }

    /**
     * The spacing between drag label buttons.
     */
    private static final int BUTTON_SPACING = 2;

    /**
     * The padding between the left and right of the drag label buttons.
     */
    private static final int BUTTON_PADDING = 5;

    /**
     * Refreshes all right buttons and their positions.
     */
    public void refreshRightButtons() {
        if (rightButtonList == null) {
            return;
        }

        removeRightButtons();
        effectFrame.revalidateTitlePosition();

        LinkedList<Component> reversedRightButtons = new LinkedList<>();
        for (int i = rightButtonList.size() - 1 ; i >= 0 ; i--) {
            reversedRightButtons.add(rightButtonList.get(i));
        }

        int currentXStart = width - BUTTON_PADDING;

        for (Component rightButtonComponent : reversedRightButtons) {
            Dimension dimension = determineButtonDimensions(rightButtonComponent);
            int buttonWidth = (int) dimension.getWidth();
            int buttonHeight = (int) dimension.getHeight();

            int y = buttonHeight > DEFAULT_HEIGHT ? 0 : DEFAULT_HEIGHT / 2 - buttonHeight / 2;

            currentXStart -= (buttonWidth + 2 * BUTTON_SPACING);
            rightButtonComponent.setBounds(currentXStart, y, buttonWidth, buttonHeight);
            add(rightButtonComponent);
        }

        revalidate();
        repaint();
    }

    /**
     * Refreshes all left buttons and their positions.
     */
    public void refreshLeftButtons() {
        if (leftButtonList == null) {
            return;
        }

        removeLeftButtons();
        effectFrame.revalidateTitlePosition();

        int currentXStart = BUTTON_PADDING;

        for (Component leftButtonComponent : leftButtonList) {
            Dimension dimension = determineButtonDimensions(leftButtonComponent);
            int buttonWidth = (int) dimension.getWidth();
            int buttonHeight = (int) dimension.getHeight();

            int y = buttonHeight > DEFAULT_HEIGHT ? 0 : DEFAULT_HEIGHT / 2 - buttonHeight / 2;

            leftButtonComponent.setBounds(currentXStart, y, buttonWidth, buttonHeight);
            currentXStart += buttonWidth + 2 * BUTTON_SPACING;
            add(leftButtonComponent);
        }

        revalidate();
        repaint();
    }

    /**
     * The error message to display if a component not allowed is attempted to be added to the drag label button list.
     */
    private static final String BUTTON_DIMENSION_ERROR_MESSAGE = "A component other than JLabel/PinButton/"
            + "MinimizeButton found its way into the right button list: ";

    /**
     * Returns the dimension for the provided component being added to the drag label.
     *
     * @param component the component being added to the drag label.
     * @return the dimension for the component
     */
    @ForReadability
    private Dimension determineButtonDimensions(Component component) {
        Preconditions.checkNotNull(component);
        Preconditions.checkState(this.type == Type.TOP);

        int buttonWidth;
        int buttonHeight;

        switch (component) {
            case MenuButton menuButton -> {
                buttonWidth = menuButton.getWidth();
                buttonHeight = menuButton.getHeight();
            }
            case ChangeSizeButton changeSizeButton -> {
                buttonWidth = changeSizeButton.getWidth();
                buttonHeight = changeSizeButton.getHeight();
            }
            case CloseButton closeButton -> {
                buttonWidth = closeButton.getWidth();
                buttonHeight = closeButton.getHeight();
            }
            case MinimizeButton minimizeButton -> {
                buttonWidth = minimizeButton.getWidth();
                buttonHeight = minimizeButton.getHeight();
            }
            case PinButton pinButton -> {
                buttonWidth = pinButton.getWidth();
                buttonHeight = pinButton.getHeight();
            }
            case JLabel label -> {
                buttonWidth = StringUtil.getMinWidth(label.getText().trim(), label.getFont());
                buttonHeight = StringUtil.getAbsoluteMinHeight(label.getText().trim(), label.getFont());
            }
            case JButton ignored -> {
                buttonWidth = 22; // todo these should go away and we just use the button's width and height as it is
                buttonHeight = 20;
            }
            case default -> throw new IllegalArgumentException(BUTTON_DIMENSION_ERROR_MESSAGE + component);
        }

        return new Dimension(buttonWidth, buttonHeight);
    }

    // todo by default drag label buttons shouldn't be focusable, console is exception
    // todo console menu add frames and removing whne not showing not working?
    // todo remove icon button

    /**
     * Returns whether the provided button is a text button indicated by containing text and not an icon.
     * This means that for bounds calculations the dimensions of the button will be determined by the contained
     * text dimensions.
     *
     * @param button the button to test
     * @return whether the provided button is a text button
     */
    private boolean isTextButton(Component button) {
        return button instanceof JLabel label && !label.getText().isEmpty();
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
        this.xOffset.set(xOffset);
    }

    /**
     * Sets the y offset of this drag label.
     *
     * @param yOffset the y offset of this drag label
     */
    public void setYOffset(int yOffset) {
        this.yOffset.set(yOffset);
    }

    /**
     * Returns the type of drag label this drag label is.
     *
     * @return the type of drag label this drag label is
     */
    public Type getType() {
        return type;
    }
}
