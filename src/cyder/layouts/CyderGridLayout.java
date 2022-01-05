package cyder.layouts;

import cyder.utilities.ReflectionUtil;

import java.awt.*;

public class CyderGridLayout extends CyderBaseLayout {
    private int horizontalCells = DEFAULT_CELLS;
    private int vertialCells = DEFAULT_CELLS;
    public static final int DEFAULT_CELLS = 1;

    private Component[][] components;

    public CyderGridLayout(int xCells, int yCells) {
        if (xCells < 1 || yCells < 1)
            throw new IllegalArgumentException("Provided cell length does not meet the minimum requirement");

        this.horizontalCells = xCells;
        this.vertialCells = yCells;

        components = new Component[xCells][yCells];
    }

    @Override
    public void paint(Graphics g) {
        //no components means no need to draw
        if (components == null || this == null) {
            return;
        }

        if (this.getWidth() == 0 || this.getHeight() == 0) {
            super.paint(g);
            return;
        }

        //partition width into how many grid spaces we have
        int widthPartition = (int) Math.floor(this.getWidth() / horizontalCells);

        //partition height into how many grid spaces we have
        int heightPartition = (int) Math.floor((this.getHeight() / vertialCells));

        //now accounting for offsets we can draw our components using the bounds provided
        // components themselves take care of their own insets by being smaller than the
        // partitioned area they're given or be placed on a label to be used as spacing
        // and then passed to the GridLayout

        for (int x = 0 ; x < horizontalCells ; x++) {
            for (int y = 0 ; y < vertialCells ; y++) {
                //base case of no component is at this position
                if (components[x][y] == null)
                    continue;

                int startX = x * widthPartition;
                int startY = y * heightPartition;

                Component refComponent = components[x][y];

                //does it not fit in bounds?
                if (refComponent.getWidth() > widthPartition || refComponent.getHeight() > heightPartition) {
                    refComponent.setBounds(startX, startY, widthPartition, heightPartition);
                } else {
                    //fits in bounds of designated space so center it
                    int addX = (widthPartition - refComponent.getWidth()) / 2;
                    int addY = (heightPartition - refComponent.getHeight()) / 2;
                    refComponent.setBounds(startX + addX, startY + addY,
                            refComponent.getWidth(), refComponent.getHeight());
                }

                this.add(refComponent);
            }
        }

       super.paint(g);
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

    public boolean removeComponent(Component component) {
        if (components == null)
            throw new IllegalStateException("Components not yet initialized");

        for (int x = 0 ; x < horizontalCells ; x++) {
            for (int y = 0 ; y < vertialCells ; y++) {
                if (components[x][y] == component) {
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

    //standard
    @Override
    public String toString() {
        return ReflectionUtil.commonCyderUIReflection(this);
    }
}
