package cyder.layouts;

import javax.swing.*;
import java.awt.*;

public class GridLayout extends JLabel {
    private int horizontalCells = DEFAULT_CELLS;
    private int vertialCells = DEFAULT_CELLS;
    public static final int DEFAULT_CELLS = 1;

    //default insets surrounding the CyderFrame content pane
    private Insets defaultInsets = new Insets(10,10,10,10);
    //todo getter and setter/updater

    private Component[][] components;

    public GridLayout(int xCells, int yCells) {
        if (xCells < 1 || yCells < 1)
            throw new IllegalArgumentException("Provided cell length does not meet the minimum requirement");

        this.horizontalCells = xCells;
        this.vertialCells = yCells;

        components = new Component[xCells][yCells];

        //todo somehow this needs to take over the content pane of the container

        //todo how are we going to store the components?

        //todo actually I guess this should extend a JLabel so that we can add stuff to this if a layout is set
        // and override our CyderFrame's getContentPane() method and then figure out where to pass it
        // and this class will figure out where it goes and update the view
    }

    public boolean addComponent(Component component, int x, int y) {
        if (components == null)
            throw new IllegalStateException("Components not yet initialized");
        if (x < 0 || x > horizontalCells - 1 || y < 0 || y > vertialCells - 1)
            throw new IllegalArgumentException("Provided grid location is invalid");

        if (components[x][y] != null) {
            //component already here, figure out how to handle this case
            return false;
        }

        components[x][y] = component;
        this.repaint();
        return true;
    }

    public boolean removeComponent(Component c) {
        if (components == null)
            throw new IllegalStateException("Components not yet initialized");

        for (int x = 0 ; x < horizontalCells ; x++) {
            for (int y = 0 ; y < vertialCells ; y++) {
                if (components[x][y] == c) {
                    components[x][y] = null;
                    return true;
                }
            }
        }

        return false;
    }

    public boolean removeComponent(int x, int y) {
        if (components == null)
            throw new IllegalStateException("Components not yet initialized");
        if (x < 0 || x > horizontalCells - 1 || y < 0 || y > vertialCells - 1)
            throw new IllegalArgumentException("Provided grid location is invalid");

        //no component there
        if (components[x][y] == null)
            return false;

        //found component so remove and return true
        components[x][y] = null;
        return true;
    }
}
