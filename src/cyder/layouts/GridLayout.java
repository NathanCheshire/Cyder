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

        //todo override our CyderFrame's getContentPane() method and then figure out where to pass it
        // and this class will figure out where it goes and update the view, IF a layout is set for the CyderFrame
    }

    @Override
    public void paint(Graphics g) {
        //no components means no need to draw
        if (components == null) {
            super.paint(g);
            return;
        }

        int xOff = defaultInsets.left;
        int yOff = defaultInsets.top;
        int width = this.getWidth() - xOff - defaultInsets.right;
        int height = this.getHeight() - yOff - defaultInsets.bottom;

        //partition width into how many grid spaces we have
        int widthPartition = (int) Math.floor(width / horizontalCells);

        //partition height into how many grid spaces we have
        int heightPartition = (int) Math.floor((height / vertialCells));

        //now accounting for offsets we can draw our components using the bounds provided
        // components themselves take care of their own insets by being smaller than the
        // partitioned area they're given or be placed on a label to be used as spacing
        // and then passed to the GridLayout

        for (int x = 0 ; x < horizontalCells ; x++) {
            for (int y = 0 ; y < vertialCells ; y++) {
                //base case of no component is at this position
                if (components[x][y] == null)
                    continue;

                int startX = xOff + x * widthPartition;
                int startY = yOff + y * heightPartition;

                Component refComponent = components[x][y];

                //does it not fit in bounds?
                if (refComponent.getWidth() > widthPartition || refComponent.getHeight() > heightPartition) {
                    refComponent.setBounds(startX, startY, widthPartition, heightPartition);
                } else {
                    //fits in bounds so center it
                    int addX = (widthPartition - refComponent.getWidth()) / 2;
                    int addY = (heightPartition - refComponent.getHeight()) / 2;
                    refComponent.setBounds(startX + addX, startY + addY,
                            refComponent.getWidth(), refComponent.getHeight());
                }

                //now add the component to ourselves
                add(refComponent);
            }
        }
    }

    /**
     * Adds the provided component to the grid at the first available space
     * @param component the component to add to the grid if possible
     * @return whether or not the component was added successfully
     */
    public boolean addComponent(Component component) {
        for (int x = 0 ; x < horizontalCells ; x++) {
            for (int y = 0 ; y < vertialCells ; y++) {
                if (components[x][y] == null) {
                    components[x][y] = component;
                    repaint();
                    return true;
                }
            }
        }

        return false;
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
