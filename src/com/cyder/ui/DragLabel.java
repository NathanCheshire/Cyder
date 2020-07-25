package com.cyder.ui;

import com.cyder.handler.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class DragLabel extends JLabel {
    private int restoreX;
    private int restoreY;
    private int width;
    private int height;
    private static JFrame effectFrame;

    Util dragLabelUtil = new Util();

    private int xMouse;
    private int yMouse;

    private Color navy = new Color(26, 32, 51);

    ImageIcon mini1 = new ImageIcon("src\\com\\cyder\\io\\pictures\\minimize1.png");
    ImageIcon mini2 = new ImageIcon("src\\com\\cyder\\io\\pictures\\minimize2.png");

    ImageIcon close1 = new ImageIcon("src\\com\\cyder\\io\\pictures\\Close1.png");
    ImageIcon close2 = new ImageIcon("src\\com\\cyder\\io\\pictures\\Close2.png");

    private JButton close;
    private JButton minimize;

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

                if (effectFrame != null && effectFrame.isFocused()) {
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

        close.addActionListener(e -> {
            dragLabelUtil.closeAnimation(effectFrame);
            effectFrame.dispose();
        });

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
            dragLabelUtil.minimizeAnimation(effectFrame);
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
}