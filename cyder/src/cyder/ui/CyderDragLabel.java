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
     * The list of buttons to paint on the left of the drag label.
     * If any buttons exist in this list then the title label is restricted/moved to
     * the center position.
     */
    private LinkedList<JButton> leftButtonList;

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

    // todo use me for all

    /**
     * Generates and returns a mouse listener for a drag label text button.
     * Note that this button must already contain the text to be used.
     *
     * @param textButton the button to place on the drag label
     * @param runnable   the mouse listener to handle mouse events
     * @return the mouse listener for mouse events
     */
    public static MouseListener generateTextButtonMouseAdapter(JLabel textButton, Runnable runnable) {
        Preconditions.checkNotNull(textButton);

        String text = textButton.getText();
        Preconditions.checkNotNull(text);
        Preconditions.checkArgument(!text.isEmpty());

        Preconditions.checkNotNull(runnable);

        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                runnable.run();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                textButton.setForeground(CyderColors.regularRed);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                textButton.setForeground(CyderColors.vanilla);
            }
        };
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
     * Returns the button from the left button list at the provided index.
     *
     * @param index the index of the button to be returned
     * @return the button at the provided index
     */
    public JButton getLeftButton(int index) {
        Preconditions.checkArgument(index >= 0);
        Preconditions.checkArgument(index < leftButtonList.size());

        return leftButtonList.get(index);
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
        refreshRightButtons();
    }

    /**
     * Adds the button to the left drag label at the given index.
     *
     * @param button   the JButton with all the properties already set such as listeners,
     *                 visuals, etc. to add to the button list
     * @param addIndex the index to append the button to in the button list
     */
    public void addLeftButton(JButton button, int addIndex) {
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

        refreshRightButtons();
    }

    /**
     * Moves the provided button from the left list to the specified index.
     *
     * @param button   the button to move to the specified index
     * @param newIndex the index to move the specified button to
     */
    public void setLeftButtonIndex(JButton button, int newIndex) {
        Preconditions.checkArgument(leftButtonList.contains(button));
        Preconditions.checkArgument(newIndex < leftButtonList.size());

        int oldIndex = -1;

        for (int i = 0 ; i < leftButtonList.size() ; i++) {
            if (button == leftButtonList.get(i)) {
                oldIndex = i;
                break;
            }
        }

        JButton popButton = leftButtonList.remove(oldIndex);
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

        JButton popButton = rightButtonList.remove(oldIndex);
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

        JButton popButton = leftButtonList.remove(oldIndex);
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
    public LinkedList<JButton> getRightButtonList() {
        return rightButtonList;
    }

    /**
     * Returns the current left button list.
     *
     * @return the current left button list
     */
    public LinkedList<JButton> getLeftButtonList() {
        return leftButtonList;
    }

    /**
     * Sets the right button list to the one provided.
     *
     * @param rightButtonList the button list to use for this drag label's right list
     */
    public void setRightButtonList(LinkedList<JButton> rightButtonList) {
        removeRightButtons();
        this.rightButtonList = rightButtonList;
        refreshRightButtons();
    }

    /**
     * Sets the left button list to the one provided.
     *
     * @param leftButtonList the button list to use for this drag label's left list
     */
    public void setLeftButtonList(LinkedList<JButton> leftButtonList) {
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

        for (JButton button : rightButtonList) {
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

        for (JButton button : leftButtonList) {
            remove(button);
        }
    }

    // todo focus after background transition goes to area temporarily

    // todo console location saving doesn't work and sometimes
    //  messes up still, only save is not being disposed too

    // todo use partitioned layout for create user widget

    // todo architecture for startup subroutines needs to be like input handlers

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
        if (rightButtonList == null) {
            return;
        }

        removeRightButtons();
        effectFrame.revalidateTitlePosition();

        LinkedList<JButton> reversedRightButtons = new LinkedList<>();
        for (int i = rightButtonList.size() - 1 ; i >= 0 ; i--) {
            reversedRightButtons.add(rightButtonList.get(i));
        }

        // todo technical debt
        int buttonSpacing = 2;
        int textButtonSpacing = 10;
        int framePadding = 5;
        int currentXStart = width - framePadding;

        for (JButton rightButton : reversedRightButtons) {
            boolean isTextButton = isTextButton(rightButton);
            int spacing = isTextButton ? textButtonSpacing : buttonSpacing;

            int buttonWidth = isTextButton
                    ? StringUtil.getAbsoluteMinWidth(rightButton.getText().trim(), rightButton.getFont()) : 22;
            int buttonHeight = isTextButton
                    ? StringUtil.getAbsoluteMinHeight(rightButton.getText().trim(), rightButton.getFont()) : 20;

            int y = buttonHeight > DEFAULT_HEIGHT ? 0 : DEFAULT_HEIGHT / 2 - buttonHeight / 2;

            currentXStart -= (buttonWidth + spacing);
            rightButton.setBounds(currentXStart, y, buttonWidth, buttonHeight);
            add(rightButton);
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

        // todo technical debt
        int buttonSpacing = 2;
        int textButtonSpacing = 10;
        @SuppressWarnings("UnnecessaryLocalVariable")
        int framePadding = 5;
        int currentXStart = framePadding;

        for (JButton leftButton : leftButtonList) {
            boolean isTextButton = isTextButton(leftButton);
            int spacing = isTextButton ? textButtonSpacing : buttonSpacing;

            int buttonWidth = isTextButton
                    ? StringUtil.getAbsoluteMinWidth(leftButton.getText().trim(), leftButton.getFont()) : 22;
            int buttonHeight = isTextButton
                    ? StringUtil.getAbsoluteMinHeight(leftButton.getText().trim(), leftButton.getFont()) : 20;

            int y = buttonHeight > DEFAULT_HEIGHT ? 0 : DEFAULT_HEIGHT / 2 - buttonHeight / 2;

            leftButton.setBounds(currentXStart, y, buttonWidth, buttonHeight);
            currentXStart += buttonWidth + spacing;
            add(leftButton);
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
    private boolean isTextButton(JButton button) {
        return !button.getText().isEmpty();
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
