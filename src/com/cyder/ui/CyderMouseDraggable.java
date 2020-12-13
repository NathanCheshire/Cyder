package com.cyder.ui;

import com.cyder.utilities.GeneralUtil;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

//todo utilize
public abstract class CyderMouseDraggable extends MouseAdapter {

    private GeneralUtil gu = new GeneralUtil();
    private int xOffset;
    private int yOffset;

    public void mousePressed(MouseEvent me) {
        xOffset = me.getX();
        yOffset = me.getY();
    }

    public void mouseReleased(MouseEvent me) {
        if (gu.getDebugMode()) {
            int x = me.getX() + me.getComponent().getX() - xOffset;
            int y = me.getY() + me.getComponent().getY() - yOffset;
            System.out.println(x + "," + y);
            me.getComponent().setLocation(x,y);
            me.getComponent().getParent().revalidate();
            me.getComponent().getParent().repaint();
        }
    }
}
