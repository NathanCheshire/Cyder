package cyder.ui;

import cyder.consts.CyderColors;
import cyder.consts.CyderImages;

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

    private JButton minimize;
    private JButton close;

    private int xOffset;
    private int yOffset;
    private int xMouse;
    private int yMouse;

    ImageIcon minimizeIcon = CyderImages.minimizeIcon;
    ImageIcon minimizeIconHover = CyderImages.minimizeIconHover;

    ImageIcon closeIcon = CyderImages.closeIcon;
    ImageIcon closeIconHover = CyderImages.closeIconHover;

    private boolean draggingEnabled = true;

    public enum ButtonPosition {
        LEFT,RIGHT
    }

    private ButtonPosition buttonPosition = ButtonPosition.RIGHT;

    public DragLabel(int w, int h, CyderFrame effectFrame) {
        this.width = w;
        this.height = h;

        this.effectFrame = effectFrame;

        new JLabel();
        setSize(width,height);
        setOpaque(true);
        setFocusable(false);
        setBackground(CyderColors.navy);
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
            effectFrame.setLocation(effectFrame.getRestoreX(),effectFrame.getRestoreY());
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

    public void setSize(int width, int height) {
        super.setSize(width,height);
        this.width = width;
        this.height = height;
        refreshButtons();
        revalidate();
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public void setColor(Color c) {this.setBackground(c);}

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

    public static int getDefaultHeight() {
        return 30;
    }

    @Override
    public String toString() {
        return "DragLabel object, hash=" + this.hashCode();
    }

    /*
    ADDING BUTTONS CODE
     */

    private LinkedList<JButton> buttonsList = buildDefaultButtons();

    private LinkedList<JButton> buildDefaultButtons() {
        LinkedList<JButton> ret = new LinkedList<>();

        minimize = new JButton("");
        minimize.setToolTipText("Minimize");
        minimize.addActionListener(e -> {
            effectFrame.setRestoreX(effectFrame.getX());
            effectFrame.setRestoreY(effectFrame.getY());
            effectFrame.minimizeAnimation();
        });

        minimize.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                minimize.setIcon(minimizeIconHover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                minimize.setIcon(minimizeIcon);
            }
        });
        minimize.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                minimize.setIcon(minimizeIconHover);
            }

            @Override
            public void focusLost(FocusEvent e) {
                minimize.setIcon(minimizeIcon);
            }
        });

        minimize.setIcon(minimizeIcon);
        minimize.setContentAreaFilled(false);
        minimize.setBorderPainted(false);
        minimize.setFocusPainted(false);
        minimize.setFocusable(false);
        ret.add(minimize);

        close = new JButton("");
        close.setToolTipText("Close");
        close.addActionListener(e -> effectFrame.closeAnimation());
        close.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                close.setIcon(closeIconHover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                close.setIcon(closeIcon);
            }
        });
        close.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                close.setIcon(closeIconHover);
            }

            @Override
            public void focusLost(FocusEvent e) {
                close.setIcon(closeIcon);
            }
        });

        close.setIcon(closeIcon);
        close.setContentAreaFilled(false);
        close.setBorderPainted(false);
        close.setFocusPainted(false);
        close.setFocusable(false);
        ret.add(close);

        return ret;
    }

    public JButton getButton(int index) {
        if (index < 0 || index > buttonsList.size() - 1)
            throw new IllegalArgumentException("Attempting to get button from invalid index.");

        return this.buttonsList.get(index);
    }

    /**
     * Adds the button at the given index, 0 means add to the start and {@link DragLabel#getButton(int)#getSize()}
     *  means add to the end
     * @param button - the JButton with all the properties already set such as listeners, visuals, etc. to add
     *                 to the button list
     * @param addIndex - the index to append the button to in the button list
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

    public void removeButton(int removeIndex) {
        if (removeIndex > buttonsList.size() - 1)
            throw new IllegalArgumentException("Invalid index");
        else if (buttonsList.size() == 0)
            throw new IllegalArgumentException("Empty list");
        else {
            buttonsList.remove(removeIndex);
            refreshButtons();
        }
    }

    public LinkedList<JButton> getButtonsList() {
        return this.buttonsList;
    }

    public int getButtonListSize() {
        return this.buttonsList.size();
    }

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

    public void refreshButtons() {
        //remove all buttons to repaint them
        for (Component c : getComponents()) {
            if (c instanceof JButton && buttonsList.contains((JButton) c)) {
                this.remove(c);
                this.revalidate();
                this.repaint();
            }
        }

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

    protected void addCloseListener(ActionListener al) {
        close.addActionListener(al);
    }

    protected void addMinimizeListener(ActionListener al) {
        minimize.addActionListener(al);
    }
}
