package com.cyder.ui;

import com.cyder.utilities.GeneralUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseEvent;

public class CyderTextField extends JTextField {
    private int limit;

    public CyderTextField(int colnum) {
        super(colnum);
        this.addMouseListener(new CyderMouseDraggable() {
            @Override
            public void mousePressed(MouseEvent me) {
                super.mousePressed(me);
            }

            @Override
            public void mouseReleased(MouseEvent me) {
                super.mouseReleased(me);
            }
        });

        this.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
            if (getText().length() > limit) {
                evt.consume();
            }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        this.setBorder(new LineBorder(new Color(26, 32, 51),5,false));
        this.setForeground(new GeneralUtil().navy);
        this.setFont(new GeneralUtil().weatherFontSmall);
        this.setBackground(new Color(0,0,0,0));
        super.paintComponent(g);
    }

    public void setCharLimit(int limit) {
        this.limit = limit;
        if (getText().length() > limit) {
            setText(getText().substring(0,limit + 1));
        }
    }

    public int getCharLimit() {
        return this.limit;
    }
}
