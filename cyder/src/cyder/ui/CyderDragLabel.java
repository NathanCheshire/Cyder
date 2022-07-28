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

/**
 * Class to be used for CyderFrames, the parent is expected to be an instance of CyderFrame.
 */
public class CyderDragLabel extends JLabel {
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
    private int xOffset;

    /**
     * The y offset used for dragging.
     */
    private int yOffset;

    /**
     * The background color of this drag label.
     */
    private Color backgroundColor;

    /**
     * Whether dragging is currently enabled.
     */
    private boolean draggingEnabled = true;

    /**
     * The possible positions for buttons.
     */
    public enum ButtonPosition {
        LEFT, RIGHT
    }

    /**
     * The current button position.
     */
    private ButtonPosition buttonPosition = DEFAULT_BUTTON_POSITION;

    /**
     * The default button position.
     */
    public static final ButtonPosition DEFAULT_BUTTON_POSITION = ButtonPosition.RIGHT;

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

        setSize(this.width, this.height);
        setOpaque(true);
        setFocusable(false);
        setBackground(CyderColors.getGuiThemeColor());

        this.backgroundColor = CyderColors.getGuiThemeColor();

        // todo use atomic ints
        int[] mousePoints = {0, 0};

        addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int x = e.getXOnScreen();
                int y = e.getYOnScreen();

                if (effectFrame != null && effectFrame.isFocused() && draggingEnabled) {
                    effectFrame.setLocation(
                            x - mousePoints[0] - xOffset,
                            y - mousePoints[1] - yOffset);
                    effectFrame.setRestoreX(effectFrame.getX());
                    effectFrame.setRestoreY(effectFrame.getY());
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                mousePoints[0] = e.getX();
                mousePoints[1] = e.getY();
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                effectFrame.startDragEvent();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                effectFrame.endDragEvent();
            }
        });

        effectFrame.addWindowListener(new WindowAdapter() {
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
        });

        Logger.log(Logger.Tag.OBJECT_CREATION, this);
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
        draggingEnabled = false;
    }

    /**
     * Enables dragging.
     */
    public void enableDragging() {
        draggingEnabled = true;
    }

    /**
     * Returns whether dragging is enabled.
     *
     * @return whether dragging is enabled
     */
    public boolean isDraggingEnabled() {
        return draggingEnabled;
    }

    /**
     * The default height for drag labels.
     * The Cyder standard is 30 pixels.
     */
    public static final int DEFAULT_HEIGHT = 30;

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
        ret = 31 * ret + Objects.hashCode(buttonList);
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
     * The list of buttons to paint for the drag label.
     */
    private LinkedList<JButton> buttonList = buildDefaultButtons();

    /**
     * The pin button used for the default drag label.
     */
    private JButton pinButton;

    private static final String MINIMIZE = "Minimize";
    private static final String PIN = "Pin";
    private static final String UNPIN_FROM_CONSOLE = "Unpin from console";
    private static final String PIN_TO_CONSOLE = "Pin to console";
    private static final String CLOSE = "Close";

    /**
     * Returns the default button list which contains the buttons
     * in the following order: minimize, pin window, close.
     *
     * @return the default button list
     */
    private LinkedList<JButton> buildDefaultButtons() {
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
     * Returns the button from the button list at the provided index.
     *
     * @param index the index of the button to be returned
     * @return the button at the provided index
     */
    public JButton getButton(int index) {
        Preconditions.checkArgument(index >= 0);
        Preconditions.checkArgument(index < buttonList.size());

        return buttonList.get(index);
    }

    /**
     * Adds the button at the given index, 0 means add to the start
     * and {@link CyderDragLabel#getButton(int)#getSize()} means add to the end.
     *
     * @param button   the JButton with all the properties already set such as listeners,
     *                 visuals, etc. to add to the button list
     * @param addIndex the index to append the button to in the button list
     */
    public void addButton(JButton button, int addIndex) {
        Preconditions.checkArgument(!buttonList.contains(button));
        Preconditions.checkArgument(!labelContainsButton(button));

        buttonList.add(addIndex, button);
        refreshButtons();
    }

    /**
     * Returns whether this drag label contains the provided button.
     *
     * @param button the button
     * @return whether this drag label contains the provided button
     */
    private boolean labelContainsButton(JButton button) {
        for (Component c : getComponents()) {
            if (c instanceof JButton) {
                if (c == button) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Moves the provided button to the specified index.
     *
     * @param button   the button to move to the specified index
     * @param newIndex the index to move the specified button to
     */
    public void setButtonIndex(JButton button, int newIndex) {
        Preconditions.checkArgument(buttonList.contains(button));
        Preconditions.checkArgument(newIndex < buttonList.size());

        int oldIndex = -1;

        for (int i = 0 ; i < buttonList.size() ; i++) {
            if (button == buttonList.get(i)) {
                oldIndex = i;
                break;
            }
        }

        JButton popButton = buttonList.remove(oldIndex);
        buttonList.add(newIndex, popButton);

        refreshButtons();
    }

    /**
     * Moves the button at the oldIndex to the new Index and
     * pushes any other buttons out of the way.
     *
     * @param oldIndex the position of the button to target
     * @param newIndex the index to move the targeted button to
     */
    public void setButtonIndex(int oldIndex, int newIndex) {
        Preconditions.checkArgument(oldIndex >= 0);
        Preconditions.checkArgument(oldIndex < buttonList.size());
        Preconditions.checkArgument(newIndex >= 0);
        Preconditions.checkArgument(newIndex < buttonList.size());

        JButton popButton = buttonList.remove(oldIndex);
        buttonList.add(newIndex, popButton);
        refreshButtons();
    }

    /**
     * Removes the button in the button list at the provided index.
     *
     * @param removeIndex index of button to remove
     */
    public void removeButton(int removeIndex) {
        Preconditions.checkArgument(removeIndex >= 0);
        Preconditions.checkArgument(removeIndex < buttonList.size());

        // todo should this loop through components?
        for (Component component : getComponents()) {
            if (component instanceof JButton button && buttonList.contains(button)) {
                remove(component);
                revalidate();
                repaint();
            }
        }

        buttonList.remove(removeIndex);

        refreshButtons();
    }

    /**
     * Returns the current button list.
     *
     * @return the current button list
     */
    public LinkedList<JButton> getButtonList() {
        return buttonList;
    }

    /**
     * Sets the button list to the one provided.
     *
     * @param list the button list to use for this drag label
     */
    public void setButtonList(LinkedList<JButton> list) {
        // todo should this loop through components?
        for (Component component : getComponents()) {
            if (component instanceof JButton button && buttonList.contains(button)) {
                remove(component);
                revalidate();
                repaint();
            }
        }

        buttonList = list;
        refreshButtons();
    }

    // todo this method is hideous
    /**
     * Refreshes and repaints the button list.
     */
    public void refreshButtons() {
        for (Component component : getComponents()) {
            if (component instanceof JButton button && buttonList.contains(button)) {
                remove(component);
                revalidate();
                repaint();
            }
        }

        if (buttonList == null) {
            return;
        }

        switch (buttonPosition) {
            case RIGHT -> {
                int addWidth = width - 26;
                for (int i = buttonList.size() - 1 ; i >= 0 ; i--) {
                    int textWidth = 0;

                    if (!buttonList.get(i).getText().isEmpty()) {
                        textWidth =
                                StringUtil.getMinWidth(buttonList.get(i).getText().trim(), buttonList.get(i).getFont());
                    }

                    //might have to fix this method here depending on how many more buttons with text you add
                    buttonList.get(i).setBounds(addWidth - textWidth,
                            0, textWidth == 0 ? 22 : textWidth + 26, 28);
                    add(buttonList.get(i));
                    addWidth -= (26 + textWidth);
                }
            }
            case LEFT -> {
                int leftAddWidth = 26 * (buttonList.size() - 1) + 5;
                for (int i = buttonList.size() - 1 ; i >= 0 ; i--) {
                    int textWidth = 0;

                    if (!buttonList.get(i).getText().isEmpty()) {
                        textWidth =
                                StringUtil.getMinWidth(buttonList.get(i).getText().trim(), buttonList.get(i).getFont());
                    }

                    //might have to fix this method here depending on how many more buttons with text you add
                    buttonList.get(i).setBounds(leftAddWidth - textWidth, 0,
                            textWidth == 0 ? 22 : textWidth + 26, 28);
                    add(buttonList.get(i));
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
        return xOffset;
    }

    /**
     * Returns the y offset of this drag label.
     *
     * @return the y offset of this drag label
     */
    public int getYOffset() {
        return yOffset;
    }

    /**
     * Sets the x offset of this drag label.
     *
     * @param xOffset the x offset of this drag label
     */
    public void setXOffset(int xOffset) {
        this.xOffset = xOffset;
    }

    /**
     * Sets the y offset of this drag label.
     *
     * @param yOffset the y offset of this drag label
     */
    public void setYOffset(int yOffset) {
        this.yOffset = yOffset;
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
