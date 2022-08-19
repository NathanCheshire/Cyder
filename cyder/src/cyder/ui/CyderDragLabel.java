package cyder.ui;

import com.google.common.base.Preconditions;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderIcons;
import cyder.handlers.internal.Logger;
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
     * The default height for drag labels. The Cyder standard for top labels is 30 pixels.
     */
    public static final int DEFAULT_HEIGHT = 30;

    private static final String MINIMIZE = "Minimize";
    private static final String PIN = "Pin";
    private static final String UNPIN_FROM_CONSOLE = "Unpin from console";
    private static final String PIN_TO_CONSOLE = "Pin to console";
    private static final String CLOSE = "Close";

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
     * The current x location of the mouse relative to this label.
     */
    @SuppressWarnings("FieldCanBeLocal")
    private final AtomicInteger mouseX;

    /**
     * The current y location of the mouse relative to this label.
     */
    @SuppressWarnings("FieldCanBeLocal")
    private final AtomicInteger mouseY;

    /**
     * The list of buttons to paint on the right of the drag label.
     */
    private LinkedList<Component> rightButtonList = buildRightButtonList();

    /**
     * The list of buttons to paint on the left of the drag label.
     * If any buttons exist in this list then the title label is restricted/moved to
     * the center position.
     */
    private LinkedList<Component> leftButtonList;

    /**
     * Constructs a new drag label with the provided bounds and frame to effect.
     *
     * @param width       the width of the drag label, typically the width of the effect frame.
     * @param height      the height of the drag label, typically {@link CyderDragLabel#DEFAULT_HEIGHT}
     * @param effectFrame the cyder frame object to control
     */
    public CyderDragLabel(int width, int height, CyderFrame effectFrame) {
        this.width = width;
        this.height = height;
        this.effectFrame = effectFrame;
        this.backgroundColor = CyderColors.getGuiThemeColor();

        leftButtonList = new LinkedList<>();

        setSize(width, height);
        setOpaque(true);
        setFocusable(false);
        setBackground(backgroundColor);

        xOffset = new AtomicInteger();
        yOffset = new AtomicInteger();
        mouseX = new AtomicInteger();
        mouseY = new AtomicInteger();

        draggingEnabled = new AtomicBoolean(true);

        addMouseMotionListener(createDraggingMouseMotionListener(effectFrame, draggingEnabled,
                mouseX, mouseY, xOffset, yOffset));
        addMouseListener(createOpacityAnimationMouseListener(effectFrame));

        effectFrame.addWindowListener(createWindowListener(effectFrame));

        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    /**
     * Creates a mouse motion listener to allow the provided frame to be dragged.
     *
     * @param effectFrame     the frame the motion listener will be applied to
     * @param draggingEnabled whether dragging should be allowed
     * @param mouseX          the current x location relative to the component
     * @param mouseY          the current y location relative to the component
     * @param xOffset         the current frame x offset
     * @param yOffset         the current frame y offset
     * @return a mouse motion listener to allow the provided frame to be dragged
     */
    private static MouseMotionListener createDraggingMouseMotionListener(
            CyderFrame effectFrame, AtomicBoolean draggingEnabled,
            AtomicInteger mouseX, AtomicInteger mouseY,
            AtomicInteger xOffset, AtomicInteger yOffset) {
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
     * The pin button used for the default drag label.
     */
    private JButton pinButton;

    /**
     * Builds and returns the default right button list which contains the buttons
     * in the following order: minimize, pin window, close.
     *
     * @return the default right button list
     */
    private LinkedList<Component> buildRightButtonList() {
        LinkedList<Component> ret = new LinkedList<>();

        CyderIconButton minimize = new CyderIconButton(
                MINIMIZE,
                CyderIcons.minimizeIcon,
                CyderIcons.minimizeIconHover,
                null);
        minimize.addActionListener(e -> {
            Logger.log(Logger.Tag.UI_ACTION, this);
            effectFrame.minimizeAndIconify();
        });
        ret.add(minimize);

        pinButton = new CyderIconButton(PIN, CyderIcons.pinIcon, null,
                new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if (effectFrame.getPinned()) {
                            pinButton.setIcon(CyderIcons.pinIconHoverPink);
                        } else if (effectFrame.isConsolePinned()) {
                            pinButton.setIcon(CyderIcons.pinIcon);
                        } else {
                            pinButton.setIcon(CyderIcons.pinIconHover);

                        }
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        if (effectFrame.getPinned()) {
                            pinButton.setIcon(CyderIcons.pinIconHover);
                        } else if (effectFrame.isConsolePinned()) {
                            pinButton.setIcon(CyderIcons.pinIconHoverPink);
                        } else {
                            pinButton.setIcon(CyderIcons.pinIcon);
                        }
                    }
                });
        pinButton.addActionListener(e -> {
            Logger.log(Logger.Tag.UI_ACTION, this);
            onPinButtonClick();
        });
        ret.add(pinButton);

        CyderIconButton close = new CyderIconButton(CLOSE, CyderIcons.closeIcon,
                CyderIcons.closeIconHover, null);
        close.addActionListener(e -> {
            Logger.log(Logger.Tag.UI_ACTION, this);
            effectFrame.dispose();
        });
        ret.add(close);

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

    private static final int BUTTON_SPACING = 2;
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
            int buttonWidth;
            int buttonHeight;

            if (rightButtonComponent instanceof JLabel label) {
                buttonWidth = StringUtil.getMinWidth(label.getText().trim(), label.getFont());
                buttonHeight = StringUtil.getAbsoluteMinHeight(label.getText().trim(), label.getFont());
            } else if (rightButtonComponent instanceof JButton) {
                buttonWidth = 22;
                buttonHeight = 20;
            } else {
                throw new IllegalArgumentException("A component other than JLabel/JButton found "
                        + "its way into the right button list: " + rightButtonComponent);
            }

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
            int buttonWidth;
            int buttonHeight;

            if (leftButtonComponent instanceof JLabel label) {
                buttonWidth = StringUtil.getMinWidth(label.getText().trim(), label.getFont());
                buttonHeight = StringUtil.getAbsoluteMinHeight(label.getText().trim(), label.getFont());
            } else if (leftButtonComponent instanceof JButton) {
                buttonWidth = 22;
                buttonHeight = 20;
            } else {
                throw new IllegalArgumentException("A component other than JLabel/JButton found "
                        + "its way into the right button list: " + leftButtonComponent);
            }

            int y = buttonHeight > DEFAULT_HEIGHT ? 0 : DEFAULT_HEIGHT / 2 - buttonHeight / 2;

            leftButtonComponent.setBounds(currentXStart, y, buttonWidth, buttonHeight);
            currentXStart += buttonWidth + 2 * BUTTON_SPACING;
            add(leftButtonComponent);
        }

        revalidate();
        repaint();
    }

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
     * Performs the logic needed for advancing pin state and
     * refreshing when the pin button is pressed.
     */
    private void onPinButtonClick() {
        if (effectFrame.getPinned()) {
            effectFrame.setPinned(false);
            effectFrame.setConsolePinned(true);
        } else if (effectFrame.isConsolePinned()) {
            effectFrame.setConsolePinned(false);
        } else {
            effectFrame.setPinned(true);
        }

        refreshPinIconAndTooltip();
    }

    /**
     * Refreshes the pin icon and tooltip.
     */
    public void refreshPinIconAndTooltip() {
        if (pinButton != null) {
            refreshPinTooltip();
            refreshPinIcon();
        }
    }

    /**
     * Refreshes the pin icon.
     */
    public void refreshPinIcon() {
        if (effectFrame.getPinned()) {
            pinButton.setIcon(CyderIcons.pinIconHover);
        } else if (effectFrame.isConsolePinned()) {
            pinButton.setIcon(CyderIcons.pinIconHoverPink);
        } else {
            pinButton.setIcon(CyderIcons.pinIcon);
        }
    }

    /**
     * Refreshes the pin tooltip.
     */
    public void refreshPinTooltip() {
        if (effectFrame.getPinned()) {
            pinButton.setToolTipText(PIN_TO_CONSOLE);
        } else if (effectFrame.isConsolePinned()) {
            pinButton.setToolTipText(UNPIN_FROM_CONSOLE);
        } else {
            pinButton.setToolTipText(PIN);
        }
    }
}
