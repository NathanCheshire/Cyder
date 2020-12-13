package com.cyder.ui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public abstract class CyderMouseDraggable extends MouseAdapter {

    private static boolean movingComponents = false; //todo set if trying to play with comp. locations
    private int xOffset;
    private int yOffset;

    public void mousePressed(MouseEvent me) {
        xOffset = me.getX();
        yOffset = me.getY();
    }

    public void mouseReleased(MouseEvent me) {
        if (movingComponents) {
            int x = me.getX() + me.getComponent().getX() - xOffset;
            int y = me.getY() + me.getComponent().getY() - yOffset;
            System.out.println(x + "," + y);
            me.getComponent().setLocation(x,y);
            me.getComponent().getParent().revalidate();
            me.getComponent().getParent().repaint();
        }
    }
}
