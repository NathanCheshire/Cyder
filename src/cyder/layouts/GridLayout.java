package cyder.layouts;

import java.awt.*;

public class GridLayout {
    private int horizontalCells = DEFAULT_CELLS;
    private int vertialCells = DEFAULT_CELLS;
    public static final int DEFAULT_CELLS = 1;

    //default insets surrounding the CyderFrame content pane
    Insets defaultInsets = new Insets(10,10,10,10);

    public GridLayout(int xCells, int yCells) {
        if (xCells < 1 || yCells < 1)
            throw new IllegalArgumentException("Provided cell length does not meet the minimum requirement");

        this.horizontalCells = xCells;
        this.vertialCells = yCells;

        //todo somehow this needs to take over the content pane of the container

        //todo how are we going to store the components?

        //todo actually I guess this should extend a JLabel so that we can add stuff to this if a layout is set
        // and override our CyderFrame's getContentPane() method and then figure out where to pass it
        // and this class will figure out where it goes and update the view
    }

    public boolean addComponent(Component component, int x, int y) {

        return false;
    }
}
