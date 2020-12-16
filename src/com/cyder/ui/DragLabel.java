package com.cyder.ui;

import com.cyder.utilities.AnimationUtil;
import com.cyder.utilities.GeneralUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class DragLabel extends JLabel {
    private int restoreX;
    private int restoreY;
    private int width;
    private int height;
    private static JFrame effectFrame;

    GeneralUtil dragLabelGeneralUtil = new GeneralUtil();
    AnimationUtil fa = new AnimationUtil();

    private int xMouse;
    private int yMouse;

    private Color navy = new Color(26, 32, 51);

    ImageIcon mini1 = new ImageIcon("src/com/cyder/sys/pictures/minimize1.png");
    ImageIcon mini2 = new ImageIcon("src/com/cyder/sys/pictures/minimize2.png");

    ImageIcon close1 = new ImageIcon("src/com/cyder/sys/pictures/Close1.png");
    ImageIcon close2 = new ImageIcon("src/com/cyder/sys/pictures/Close2.png");

    private JButton close;
    private JButton minimize;

    private boolean draggingEnabled = true;

    public DragLabel(int w, int h, JFrame effectFrame) {
        this.width = w;
        this.height = h;

        new JLabel();
        setSize(width,height);
        setOpaque(true);
        setBackground(navy);
        addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int x = e.getXOnScreen();
                int y = e.getYOnScreen();

                if (effectFrame != null && effectFrame.isFocused() && draggingEnabled) {
                    effectFrame.setLocation(x - xMouse, y - yMouse);
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

        close = new JButton("");

        close.setToolTipText("Close");

        close.addActionListener(e -> fa.closeAnimation(effectFrame));

        close.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                close.setIcon(close2);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                close.setIcon(close1);
            }
        });

        close.setBounds(width - 26, 0, 22, 20);
        close.setIcon(close1);
        close.setContentAreaFilled(false);
        close.setBorderPainted(false);
        close.setFocusPainted(false);
        add(close);

        minimize = new JButton("");
        minimize.setToolTipText("Minimize");
        minimize.addActionListener(e -> {
            restoreX = effectFrame.getX();
            restoreY = effectFrame.getY();
            fa.minimizeAnimation(effectFrame);
        });

        minimize.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                minimize.setIcon(mini2);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                minimize.setIcon(mini1);
            }
        });

        minimize.setBounds(width - 52, 0, 22, 20);
        minimize.setIcon(mini1);
        minimize.setContentAreaFilled(false);
        minimize.setBorderPainted(false);
        minimize.setFocusPainted(false);
        add(minimize);
    }

    public int getRestoreX() {
        return this.restoreX;
    }

    public int getRestoreY() {
        return this.restoreY;
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
}