package cyder.ui;

import com.google.common.base.Preconditions;
import cyder.constants.CyderColors;
import cyder.constants.CyderIcons;
import cyder.handlers.internal.Logger;
import cyder.utils.FrameUtil;
import cyder.utils.ReflectionUtil;
import cyder.utils.StringUtil;

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
     * The possible positions for buttons.
     */
    public enum ButtonPosition {
        LEFT, RIGHT
    }

    /**
     * The current button position.
     */
    private ButtonPosition buttonPosition = ButtonPosition.RIGHT;

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
    private LinkedList<JButton> rightButtonList = buildRightButtonList();

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

                    effectFrame.setRestoreX(effectFrame.getX());
                    effectFrame.setRestoreY(effectFrame.getY());
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
                FrameUtil.requestFramePosition(effectFrame.getMonitor(), restoreX, restoreY, effectFrame);
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
    private LinkedList<JButton> buildRightButtonList() {
        LinkedList<JButton> ret = new LinkedList<>();

        CyderIconButton minimize = new CyderIconButton(
                MINIMIZE,
                CyderIcons.minimizeIcon,
                CyderIcons.minimizeIconHover,
                null);
        minimize.addActionListener(e -> {
            Logger.log(Logger.Tag.UI_ACTION, this);
            effectFrame.minimizeAnimation();
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
     * Returns the button from the right button list at the provided index.
     *
     * @param index the index of the button to be returned
     * @return the button at the provided index
     */
    public JButton getRightButton(int index) {
        Preconditions.checkArgument(index >= 0);
        Preconditions.checkArgument(index < rightButtonList.size());

        return rightButtonList.get(index);
    }

    /**
     * Adds the button to the right drag label at the given index.
     *
     * @param button   the JButton with all the properties already set such as listeners,
     *                 visuals, etc. to add to the button list
     * @param addIndex the index to append the button to in the button list
     */
    public void addRightButton(JButton button, int addIndex) {
        Preconditions.checkArgument(!rightButtonList.contains(button));

        rightButtonList.add(addIndex, button);
        refreshButtons();
    }

    /**
     * Moves the provided button from the right list to the specified index.
     *
     * @param button   the button to move to the specified index
     * @param newIndex the index to move the specified button to
     */
    public void setRightButtonIndex(JButton button, int newIndex) {
        Preconditions.checkArgument(rightButtonList.contains(button));
        Preconditions.checkArgument(newIndex < rightButtonList.size());

        int oldIndex = -1;

        for (int i = 0 ; i < rightButtonList.size() ; i++) {
            if (button == rightButtonList.get(i)) {
                oldIndex = i;
                break;
            }
        }

        JButton popButton = rightButtonList.remove(oldIndex);
        rightButtonList.add(newIndex, popButton);

        refreshButtons();
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

        JButton popButton = rightButtonList.remove(oldIndex);
        rightButtonList.add(newIndex, popButton);
        refreshButtons();
    }

    /**
     * Removes the button in the button list at the provided index.
     *
     * @param removeIndex index of button to remove
     */
    public void removeRightButton(int removeIndex) {
        Preconditions.checkArgument(removeIndex >= 0);
        Preconditions.checkArgument(removeIndex < rightButtonList.size());

        rightButtonList.remove(removeIndex);
        refreshButtons();
    }

    /**
     * Returns the current right button list.
     *
     * @return the current right button list
     */
    public LinkedList<JButton> getRightButtonList() {
        return rightButtonList;
    }

    /**
     * Sets the right button list to the one provided.
     *
     * @param rightButtonList the button list to use for this drag label's right list
     */
    public void setRightButtonList(LinkedList<JButton> rightButtonList) {
        removeRightButtons();
        this.rightButtonList = rightButtonList;
        refreshButtons();
    }

    /**
     * Removes all buttons from the button list from this drag label.
     */
    public void removeRightButtons() {
        if (rightButtonList == null) {
            return;
        }

        for (JButton button : rightButtonList) {
            remove(button);
        }
    }

    // todo focus after background transition goes to area temporarily

    // todo console location saving doesn't work and sometimes
    //  messes up still, only save is not being disposed too

    // todo use partitioned layout for create user widget
    // todo architecture for startup subroutines needs to be like input handlers

    // todo factory to create label button with runnable too

    // todo add left button list

    // todo this method is hideous

    /**
     * Refreshes and repaints the button list.
     */
    public void refreshButtons() {
        if (rightButtonList == null) {
            return;
        }

        removeRightButtons();

        //        // todo method or even common method since duplicated throughout cyder
        //        LinkedList<JButton> reversedButtonList = new LinkedList<>();
        //        for (int i = buttonList.size() - 1 ; i >= 0 ; i--) {
        //            reversedButtonList.add(buttonList.get(i));
        //        }
        //
        //        for (JButton button : reversedButtonList) {
        //            switch (buttonPosition) {
        //                case LEFT -> {
        //
        //                }
        //                case RIGHT -> {
        //
        //                }
        //                default -> throw new IllegalArgumentException("Invalid button position: " + buttonPosition);
        //            }
        //        }

        switch (buttonPosition) {
            case RIGHT -> {
                int addWidth = width - 26;
                for (int i = rightButtonList.size() - 1 ; i >= 0 ; i--) {
                    int textWidth = 0;

                    if (!rightButtonList.get(i).getText().isEmpty()) {
                        textWidth =
                                StringUtil.getMinWidth(rightButtonList.get(i).getText().trim(),
                                        rightButtonList.get(i).getFont());
                    }

                    //might have to fix this method here depending on how many more buttons with text you add
                    rightButtonList.get(i).setBounds(addWidth - textWidth,
                            0, textWidth == 0 ? 22 : textWidth + 26, 28);
                    add(rightButtonList.get(i));
                    addWidth -= (26 + textWidth);
                }
            }
            case LEFT -> {
                int leftAddWidth = 26 * (rightButtonList.size() - 1) + 5;
                for (int i = rightButtonList.size() - 1 ; i >= 0 ; i--) {
                    int textWidth = 0;

                    if (!rightButtonList.get(i).getText().isEmpty()) {
                        textWidth =
                                StringUtil.getMinWidth(rightButtonList.get(i).getText().trim(),
                                        rightButtonList.get(i).getFont());
                    }

                    //might have to fix this method here depending on how many more buttons with text you add
                    rightButtonList.get(i).setBounds(leftAddWidth - textWidth, 0,
                            textWidth == 0 ? 22 : textWidth + 26, 28);
                    add(rightButtonList.get(i));
                    leftAddWidth -= (26 + textWidth);
                }
            }
            default -> throw new IllegalArgumentException("Illegal button position: " + buttonPosition);
        }

        revalidate();
        repaint();
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
     * Sets the button position of the button list.
     *
     * @param pos the button position of the button list
     */
    public void setButtonPosition(ButtonPosition pos) {
        if (buttonPosition == pos)
            return;

        buttonPosition = pos;
        refreshButtons();
    }

    /**
     * Returns the current button position.
     *
     * @return the current button position
     */
    public ButtonPosition getButtonPosition() {
        return buttonPosition;
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
