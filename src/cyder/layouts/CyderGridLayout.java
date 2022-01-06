package cyder.layouts;

import cyder.ui.CyderPanel;
import cyder.utilities.ReflectionUtil;

import java.awt.*;

public class CyderGridLayout extends CyderBaseLayout {
    private int horizontalCells = DEFAULT_CELLS;
    private int vertialCells = DEFAULT_CELLS;
    public static final int DEFAULT_CELLS = 1;

    //used for where to position components/how to position them when overflow occurs
    public enum Position {
        TOP_LEFT, TOP_CENTER, TOP_RIGHT,
        MIDDLE_LEFT, MIDDLE_CENTER, MIDDLE_RIGHT,
        BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT
    }

    private GridComponent[][] components;

    public CyderGridLayout(int xCells, int yCells) {
        if (xCells < 1 || yCells < 1)
            throw new IllegalArgumentException("Provided cell length does not meet the minimum requirement");

        this.horizontalCells = xCells;
        this.vertialCells = yCells;

        components = new GridComponent[xCells][yCells];
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

                GridComponent refComponent = components[x][y];

                //if an instance of a CyderPanel give it all the space possible
                if (refComponent.getComponent() instanceof CyderPanel) {
                    refComponent.getComponent().setBounds(startX, startY, widthPartition, heightPartition);
                }

                //if it doesn't fit in bounds then give it as much space as possible
                else if (refComponent.getOriginalWidth() > widthPartition || refComponent.getOriginalHeight() > heightPartition) {
                    //todo math to figure out starting offsets and use partition for size
                    switch (refComponent.getPosition()) {

                    }

                    refComponent.getComponent().setLocation(startX, startY);
                } else {
                    //fits in bounds of designated space so center it
                    int addX = (widthPartition - refComponent.getOriginalWidth()) / 2;
                    int addY = (heightPartition - refComponent.getOriginalHeight()) / 2;

                    //todo we know it fits so use math to figure out how to add/sub from/to addX and addY
                    switch (refComponent.getPosition()) {

                    }

                    refComponent.getComponent().setBounds(startX + addX, startY + addY,
                            refComponent.getOriginalWidth(), refComponent.getOriginalHeight());
                }

                this.add(refComponent.getComponent());
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
                    components[x][y] = new GridComponent(component, //defaults here
                            component.getWidth(), component.getHeight(), Position.MIDDLE_CENTER);
                    repaint();
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Adds the provided component to the grid at the first available space
     * @param component the component to add to the grid if possible
     * @param sectionPosition the position to set the component to if it fits
     * in the partitioned space or how to position the component should it overflow the partitioned space
     * @return whether or not the component was added successfully
     */
    public boolean addComponent(Component component, Position sectionPosition) {
        for (int x = 0 ; x < horizontalCells ; x++) {
            for (int y = 0 ; y < vertialCells ; y++) {
                if (components[x][y] == null) {
                    components[x][y] = new GridComponent(component,
                            component.getWidth(), component.getHeight(), sectionPosition);
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

        components[x][y] = new GridComponent(component, component.getWidth(),
                component.getHeight(), Position.MIDDLE_CENTER);
        this.repaint();
        return true;
    }

    public boolean addComponent(Component component, int x, int y, Position sectionPosition) {
        if (components == null)
            throw new IllegalStateException("Components not yet initialized");
        if (x < 0 || x > horizontalCells - 1 || y < 0 || y > vertialCells - 1)
            throw new IllegalArgumentException("Provided grid location is invalid");

        if (components[x][y] != null) {
            //component already here, figure out how to handle this case
            return false;
        }

        components[x][y] = new GridComponent(component, component.getWidth(),
                component.getHeight(), sectionPosition);
        this.repaint();
        return true;
    }

    public boolean removeComponent(Component component) {
        if (components == null)
            throw new IllegalStateException("Components not yet initialized");

        for (int x = 0 ; x < horizontalCells ; x++) {
            for (int y = 0 ; y < vertialCells ; y++) {
                if (components[x][y].getComponent() == component) {
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

    //class so we know the original size of components for resize events
    private static class GridComponent {
        private Component component;
        private int originalWidth;
        private int originalHeight;
        private Position position;

        public GridComponent(Component component, int originalWidth, int originalHeight, Position position) {
            this.component = component;
            this.originalWidth = originalWidth;
            this.originalHeight = originalHeight;
            this.position = position;
        }

        public Component getComponent() {
            return component;
        }

        public void setComponent(Component component) {
            this.component = component;
        }

        public int getOriginalWidth() {
            return originalWidth;
        }

        public void setOriginalWidth(int originalWidth) {
            this.originalWidth = originalWidth;
        }

        public int getOriginalHeight() {
            return originalHeight;
        }

        public void setOriginalHeight(int originalHeight) {
            this.originalHeight = originalHeight;
        }

        public Position getPosition() {
            return position;
        }

        public void setPosition(Position position) {
            this.position = position;
        }
    }
}
