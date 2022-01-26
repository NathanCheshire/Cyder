package cyder.ui;

import cyder.constants.CyderColors;
import cyder.constants.CyderIcons;
import cyder.handlers.internal.SessionHandler;
import cyder.utilities.ReflectionUtil;
import cyder.utilities.StringUtil;
import cyder.utilities.SystemUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;

/**
 * Class to be used for CyderFrames, the parent is expected to be an instance of CyderFrame
 */
public class DragLabel extends JLabel {
    private int width;
    private int height;
    private CyderFrame effectFrame;

    private int xOffset;
    private int yOffset;
    private int xMouse;
    private int yMouse;

    private Color backgroundColor = CyderColors.guiThemeColor;

    private boolean draggingEnabled = true;

    public enum ButtonPosition {
        LEFT, RIGHT
    }

    private ButtonPosition buttonPosition = ButtonPosition.RIGHT;

    public DragLabel(int width, int height, CyderFrame effectFrame) {
        this.width = width;
        this.height = height;

        this.effectFrame = effectFrame;

        new JLabel();
        setSize(this.width, this.height);
        setOpaque(true);
        setFocusable(false);
        setBackground(backgroundColor);
        addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int x = e.getXOnScreen();
                int y = e.getYOnScreen();

                if (effectFrame != null && effectFrame.isFocused() && draggingEnabled) {
                    effectFrame.setLocation(x - xMouse - xOffset, y - yMouse - yOffset);
                    effectFrame.setRestoreX(effectFrame.getX());
                    effectFrame.setRestoreY(effectFrame.getY());
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                xMouse = e.getX();
                yMouse = e.getY();
            }
        });

        effectFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowDeiconified(WindowEvent e) {
                int restoreX = effectFrame.getRestoreX();
                int restoreY = effectFrame.getRestoreY();

                if (restoreX > SystemUtil.getScreenWidth())
                    restoreX = SystemUtil.getScreenWidth() - effectFrame.getWidth();
                if (restoreX < - effectFrame.getWidth())
                    restoreX = 0;

                if (restoreY > SystemUtil.getScreenHeight())
                    restoreY = SystemUtil.getScreenHeight() - effectFrame.getHeight();
                if (restoreY < - effectFrame.getHeight())
                    restoreY = 0;

                effectFrame.setLocation(restoreX, restoreY);
                effectFrame.setVisible(true);
                effectFrame.requestFocus();
            }
        });

        effectFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowIconified(WindowEvent e) {
                if (effectFrame.getRestoreX() == Integer.MAX_VALUE || effectFrame.getRestoreY() == Integer.MAX_VALUE) {
                    effectFrame.setRestoreX(effectFrame.getX());
                    effectFrame.setRestoreY(effectFrame.getY());
                }
            }
        });
    }

    //override so we can change the background color if needed
    @Override
    public void repaint() {
        super.repaint();
    }

    public void setWidth(int width) {
        super.setSize(width,getHeight());
        this.width = width;
        refreshButtons();
        revalidate();
    }

    public void setHeight(int height) {
        super.setSize(getWidth(), height);
        this.height = height;
        refreshButtons();
        revalidate();
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width,height);
        this.width = width;
        this.height = height;
        refreshButtons();
        revalidate();
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    public void setColor(Color c) {
        this.backgroundColor = c;
        this.repaint();
    }

    public JFrame getEffectFrame() {
        return this.effectFrame;
    }

    public void disableDragging() {
        draggingEnabled = false;
    }

    public void enableDragging() {
        draggingEnabled = true;
    }

    public boolean isDraggingEnabled() {
        return draggingEnabled;
    }

    //standard height is 30px
    public static int getDefaultHeight() {
        return 30;
    }

    @Override
    public String toString() {
        return ReflectionUtil.commonCyderUIReflection(this);
    }

    /*
    ADDING BUTTONS CODE
     */

    private LinkedList<JButton> buttonsList = buildDefaultButtons();
    private JButton minimize;
    private JButton pinButton;
    private JButton close;

    //default order: mini, pin, close
    private LinkedList<JButton> buildDefaultButtons() {
        LinkedList<JButton> ret = new LinkedList<>();

        minimize = new JButton("");
        minimize.setToolTipText("Minimize");
        minimize.addActionListener(e -> {
            SessionHandler.log(SessionHandler.Tag.ACTION, this);
            effectFrame.minimizeAnimation();
        });

        minimize.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                minimize.setIcon(CyderIcons.minimizeIconHover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                minimize.setIcon(CyderIcons.minimizeIcon);
            }
        });
        minimize.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                minimize.setIcon(CyderIcons.minimizeIconHover);
            }

            @Override
            public void focusLost(FocusEvent e) {
                minimize.setIcon(CyderIcons.minimizeIcon);
            }
        });

        minimize.setIcon(CyderIcons.minimizeIcon);
        minimize.setContentAreaFilled(false);
        minimize.setBorderPainted(false);
        minimize.setFocusPainted(false);
        minimize.setFocusable(false);
        ret.add(minimize);

        pinButton = new JButton("");
        pinButton.setToolTipText("Pin Window/Pin to Console");
        pinButton.addActionListener(e -> {
            SessionHandler.log(SessionHandler.Tag.ACTION, this);

            if (effectFrame.getPinned()) {
                effectFrame.setPinned(false);
                effectFrame.setConsolePinned(true);
                pinButton.setIcon(CyderIcons.pinIconHoverPink);
            } else if (effectFrame.isConsolePinned()) {
                effectFrame.setConsolePinned(false);
                pinButton.setIcon(CyderIcons.pinIcon);
            } else {
                effectFrame.setPinned(true);
                pinButton.setIcon(CyderIcons.pinIconHover);
            }
        });
        pinButton.addMouseListener(new MouseAdapter() {
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

        pinButton.setIcon(CyderIcons.pinIcon);
        pinButton.setContentAreaFilled(false);
        pinButton.setBorderPainted(false);
        pinButton.setFocusPainted(false);
        pinButton.setFocusable(false);
        ret.add(pinButton);

        close = new JButton("");
        close.setToolTipText("Close");
        close.addActionListener(e -> {
            SessionHandler.log(SessionHandler.Tag.ACTION, this);
            effectFrame.dispose();
        });
        close.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                close.setIcon(CyderIcons.closeIconHover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                close.setIcon(CyderIcons.closeIcon);
            }
        });
        close.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                close.setIcon(CyderIcons.closeIconHover);
            }

            @Override
            public void focusLost(FocusEvent e) {
                close.setIcon(CyderIcons.closeIcon);
            }
        });

        close.setIcon(CyderIcons.closeIcon);
        close.setContentAreaFilled(false);
        close.setBorderPainted(false);
        close.setFocusPainted(false);
        close.setFocusable(false);
        ret.add(close);

        return ret;
    }

    /**
     * Gets the button from the button list at the given index
     * @param index the index of the button to be returned
     * @return the button at the provided index
     */
    public JButton getButton(int index) {
        if (index < 0 || index > buttonsList.size() - 1)
            throw new IllegalArgumentException("Attempting to get button from invalid index.");

        return this.buttonsList.get(index);
    }

    /**
     * Adds the button at the given index, 0 means add to the start and {@link DragLabel#getButton(int)#getSize()}
     *  means add to the end
     * @param button the JButton with all the properties already set such as listeners, visuals, etc. to add
     *                 to the button list
     * @param addIndex the index to append the button to in the button list
     */
    public void addButton(JButton button, int addIndex) {
        //to avoid a weird visual bug, don't let a button that's already been added be added again
        for (Component c : getComponents()) {
            if (c instanceof JButton) {
                if (c == button)
                    throw new IllegalArgumentException("Attempting to add a button that is already added");
            }
        }

        buttonsList.add(addIndex, button);
        refreshButtons();
    }

    /**
     * Moves the provided button the specified index.
     * @param button the button to move to the specified index
     * @param newIndex the index to move the specified button to
     */
    public void setButtonIndex(JButton button, int newIndex) {
        if (!buttonsList.contains(button))
            throw new IllegalArgumentException("Button list does not contain provided button");
        else if (newIndex > buttonsList.size() - 1)
            throw new IndexOutOfBoundsException("Provided index does not exist within the current button list");

        int oldIndex = -1;

        for (int i = 0 ; i < buttonsList.size() ; i++) {
            if (button == buttonsList.get(i)) {
                oldIndex = i;
                break;
            }
        }

        JButton popButton = buttonsList.remove(oldIndex);
        buttonsList.add(newIndex, popButton);

        refreshButtons();
    }

    /**
     * Moves the button at the oldIndex to the new Index and pushes any other buttons out of the way.
     * @param oldIndex the position of the button to target
     * @param newIndex the index to move the targeted button to
     */
    public void setButtonIndex(int oldIndex, int newIndex) {
        JButton popButton = buttonsList.remove(oldIndex);
        buttonsList.add(newIndex, popButton);
        refreshButtons();
    }

    /**
     * Removes the button in the button list at the given index
     * @param removeIndex index of button to remove
     */
    public void removeButton(int removeIndex) {
        if (removeIndex > buttonsList.size() - 1)
            throw new IllegalArgumentException("Invalid index");
        else if (buttonsList.size() == 0)
            throw new IllegalArgumentException("Empty list");
        else {
            for (Component c : getComponents()) {
                if (c instanceof JButton && buttonsList.contains((JButton) c)) {
                    this.remove(c);
                    this.revalidate();
                    this.repaint();
                }
            }

            buttonsList.remove(removeIndex);

            refreshButtons();
        }
    }

    /**
     * Standard getter for the current button list
     * @return the current button list
     */
    public LinkedList<JButton> getButtonsList() {
        return this.buttonsList;
    }

    /**
     * Update the button list with a custom one
     * @param list the list of JButtons to use for the button list
     */
    public void setButtonsList(LinkedList<JButton> list) {
        //remove all buttons from button list
        for (Component c : getComponents()) {
            if (c instanceof JButton && buttonsList.contains((JButton) c)) {
                this.remove(c);
                this.revalidate();
                this.repaint();
            }
        }
        this.buttonsList = list;
        refreshButtons();
    }

    /**
     * Refreshes and repaints the button list
     */
    public void refreshButtons() {
        //remove all buttons to repaint them
        for (Component c : getComponents()) {
            if (c instanceof JButton && buttonsList.contains((JButton) c)) {
                this.remove(c);
                this.revalidate();
                this.repaint();
            }
        }

        if (buttonsList == null)
            return;

        switch (buttonPosition) {
            case RIGHT:
                int addWidth = width - 26;

                for (int i = buttonsList.size() - 1 ; i >= 0 ; i--) {
                    int textWidth = 0;

                    if(buttonsList.get(i).getText().length() > 0) {
                        textWidth = CyderFrame.getMinWidth(buttonsList.get(i).getText().trim(), buttonsList.get(i).getFont());
                    }

                    //might have to fix this method here depending on how many more buttons with text you add
                    buttonsList.get(i).setBounds(addWidth - textWidth, 0, textWidth == 0 ? 22 : textWidth + 26, 28);
                    add(buttonsList.get(i));
                    addWidth -= (26 + textWidth);
                }
                break;
            case LEFT:
                int leftAddWidth = 26 * (buttonsList.size() - 1) + 5;

                for (int i = buttonsList.size() - 1 ; i >= 0 ; i--) {
                    int textWidth = 0;

                    if(buttonsList.get(i).getText().length() > 0) {
                        textWidth = CyderFrame.getMinWidth(buttonsList.get(i).getText().trim(), buttonsList.get(i).getFont());
                    }

                    //might have to fix this method here depending on how many more buttons with text you add
                    buttonsList.get(i).setBounds(leftAddWidth - textWidth, 0, textWidth == 0 ? 22 : textWidth + 26, 28);
                    add(buttonsList.get(i));
                    leftAddWidth -= (26 + textWidth);
                }
                break;
        }

        this.revalidate();
        this.repaint();
    }

    public int getxOffset() {
        return xOffset;
    }

    public int getyOffset() {
        return yOffset;
    }

    public void setxOffset(int xOffset) {
        this.xOffset = xOffset;
    }

    public void setyOffset(int yOffset) {
        this.yOffset = yOffset;
    }

    public void setButtonPosition(ButtonPosition pos) {
        if (buttonPosition == pos)
            return;

        this.buttonPosition = pos;
        refreshButtons();
    }

    public ButtonPosition getButtonPosition() {
        return this.buttonPosition;
    }

    public void refreshPinButton() {
        for (JButton dragLabelButton : this.getButtonsList()) {
            String tooltipText = dragLabelButton.getToolTipText();
            if (!StringUtil.empytStr(tooltipText) && tooltipText.equals(pinButton.getToolTipText())) {
                if (this.effectFrame.isAlwaysOnTop()) {
                    pinButton.setIcon(CyderIcons.pinIconHover);
                    effectFrame.setConsolePinned(false);
                    effectFrame.setPinned(true);
                } else if (this.effectFrame.isConsolePinned()) {
                    pinButton.setIcon(CyderIcons.pinIconHoverPink);
                    effectFrame.setPinned(false);
                    effectFrame.setConsolePinned(true);
                } else {
                    pinButton.setIcon(CyderIcons.pinIcon);
                    effectFrame.setPinned(false);
                    effectFrame.setConsolePinned(false);
                }
            }
        }
    }
}
