package cyder.ui;

import cyder.utilities.IOUtil;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public abstract class CyderMouseDraggable extends MouseAdapter {

    private static boolean movingComponents = IOUtil.getSystemData("UILOC").equals("1");
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
