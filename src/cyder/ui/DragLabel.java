package cyder.ui;

import cyder.consts.CyderColors;
import cyder.consts.CyderImages;
import cyder.utilities.AnimationUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class DragLabel extends JLabel {
    private int restoreX;
    private int restoreY;
    private int width;
    private int height;
    private static JFrame effectFrame;

    private int xMouse;
    private int yMouse;

    ImageIcon minimizeIcon = CyderImages.minimizeIcon;
    ImageIcon minimizeIconHover = CyderImages.minimizeIconHover;

    ImageIcon closeIcon = CyderImages.closeIcon;
    ImageIcon closeIconHover = CyderImages.closeIconHover;

    private JButton close;
    private JButton minimize;

    private boolean draggingEnabled = true;

    public DragLabel(int w, int h, JFrame effectFrame) {
        this.width = w;
        this.height = h;

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
                restoreX = effectFrame.getX();
                restoreY = effectFrame.getY();
            }
        });

        close = new JButton("");
        close.setToolTipText("Close");
        close.addActionListener(e -> {
            if (effectFrame instanceof CyderFrame) {
                ((CyderFrame) effectFrame).closeAnimation();
            } else {
                //dispose is called by this
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

        close.setBounds(width - 26, 0, 22, 20);
        close.setIcon(closeIcon);
        close.setContentAreaFilled(false);
        close.setBorderPainted(false);
        close.setFocusPainted(false);
        add(close);

        minimize = new JButton("");
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

        minimize.setBounds(width - 52, 0, 22, 20);
        minimize.setIcon(minimizeIcon);
        minimize.setContentAreaFilled(false);
        minimize.setBorderPainted(false);
        minimize.setFocusPainted(false);
        add(minimize);
    }

    public void setWidth(int width) {
        super.setSize(width,getHeight());
        this.width = width;
        minimize.setBounds(width - 52, 0, 22, 20);
        close.setBounds(width - 26, 0, 22, 20);
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
}
