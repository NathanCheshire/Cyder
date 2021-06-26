package cyder.ui;

import cyder.consts.CyderColors;
import cyder.consts.CyderImages;
import cyder.exception.FatalException;
import cyder.utilities.AnimationUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;

public class DragLabel extends JLabel {
    private int restoreX = Integer.MAX_VALUE;
    private int restoreY = Integer.MAX_VALUE;
    private int width;
    private int height;
    private JFrame effectFrame;

    private int xMouse;
    private int yMouse;

    ImageIcon minimizeIcon = CyderImages.minimizeIcon;
    ImageIcon minimizeIconHover = CyderImages.minimizeIconHover;

    ImageIcon closeIcon = CyderImages.closeIcon;
    ImageIcon closeIconHover = CyderImages.closeIconHover;

    private boolean draggingEnabled = true;

    public DragLabel(int w, int h, JFrame effectFrame) {
        this.width = w;
        this.height = h;

        this.effectFrame = effectFrame;

        new JLabel();
        setSize(width,height);
        setOpaque(true);
        setBackground(CyderColors.navy);
        addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int x = e.getXOnScreen();
                int y = e.getYOnScreen();

                if (effectFrame != null && effectFrame.isFocused() && draggingEnabled) {
                    effectFrame.setLocation(x - xMouse, y - yMouse);
                    restoreX = effectFrame.getX();
                    restoreY = effectFrame.getY();
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
            effectFrame.setLocation(restoreX,restoreY);
            effectFrame.setVisible(true);
            effectFrame.requestFocus();
            }
        });

        effectFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowIconified(WindowEvent e) {
            if (restoreX == Integer.MAX_VALUE) {
                restoreX = effectFrame.getX();
                restoreY = effectFrame.getY();
            }
            }
        });

        refreshButtons();
    }

    public void setWidth(int width) {
        super.setSize(width,getHeight());
        this.width = width;
        refreshButtons();
        revalidate();
    }

    public int getRestoreX() {
        return this.restoreX;
    }

    public int getRestoreY() {
        return this.restoreY;
    }

    public void setRestoreX(int x) {
       this.restoreX = x;
    }

    public void setRestoreY(int y) {
        this.restoreY = y;
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

        JButton minimize = new JButton("");
        minimize.setToolTipText("Minimize");
        minimize.addActionListener(e -> {
            restoreX = effectFrame.getX();
            restoreY = effectFrame.getY();
            AnimationUtil.minimizeAnimation(effectFrame);
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

        minimize.setIcon(minimizeIcon);
        minimize.setContentAreaFilled(false);
        minimize.setBorderPainted(false);
        minimize.setFocusPainted(false);
        ret.add(minimize);

        JButton close = new JButton("");
        close.setToolTipText("Close");
        close.addActionListener(e -> {
            if (effectFrame instanceof CyderFrame) {
                ((CyderFrame) effectFrame).closeAnimation();
            } else {
                AnimationUtil.closeAnimation(effectFrame);
            }
        });
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

        close.setIcon(closeIcon);
        close.setContentAreaFilled(false);
        close.setBorderPainted(false);
        close.setFocusPainted(false);
        ret.add(close);

        return ret;
    }

    public JButton getButton(int index) throws FatalException {
        if (index < 0 || index > buttonsList.size() - 1)
            throw new FatalException("Attempting to get button from invalid index.");

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
        buttonsList.add(addIndex, button);
        refreshButtons();
    }

    public void removeButton(int removeIndex) {
        buttonsList.remove(removeIndex);
        refreshButtons();
    }

    public LinkedList<JButton> getButtonsList() {
        return this.buttonsList;
    }

    public int getButtonListSize() {
        return this.buttonsList.size();
    }

    public void setButtonsList(LinkedList<JButton> list) {
        this.buttonsList = list;
        refreshButtons();
    }

    public void refreshButtons() {
        int addWidth = width - 26;

        for (int i = buttonsList.size() - 1 ; i >= 0 ; i--) {
            buttonsList.get(i).setBounds(addWidth, 0, 22, 28);
            add(buttonsList.get(i));
            addWidth -= 26;
        }
    }
}
