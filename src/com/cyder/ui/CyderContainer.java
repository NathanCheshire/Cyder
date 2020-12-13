package com.cyder.ui;

import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;

public abstract class CyderContainer extends MouseInputAdapter {
    //todo allow a component to be dragged around and print the location you dragged to

    Point location;
    MouseEvent pressed;

    public void mousePressed(MouseEvent me) {
        pressed = me;
    }

    public void mouseDragged(MouseEvent me) {
        Component component = me.getComponent();

        location = component.getLocation(location);

        int x = location.x - pressed.getX() + me.getX();
        int y = location.y - pressed.getY() + me.getY();

        component.setLocation(x, y);
    }
}
