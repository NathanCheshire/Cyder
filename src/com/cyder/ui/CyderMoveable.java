package com.cyder.ui;

import java.awt.*;

public interface CyderMoveable {

    //compact getter and setter for next four methods
    Dimension getLocation();
    void setLocation();

    //get x and y locations
    int getX();
    int getY();

    //set x or y locations for component and print
    void setX(int x);
    void setY(int y);

    //every x ms, print the dimensions of the components
    void setPrintDelay(long mili);
    long getPrintDelay();
}
