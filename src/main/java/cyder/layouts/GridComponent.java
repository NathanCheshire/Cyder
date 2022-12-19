package main.java.cyder.layouts;

import java.awt.*;

/**
 * A class to use to keep track of the original sizes of
 * components as well as their linked Position values.
 */
class GridComponent {
    /**
     * The component being managed.
     */
    private Component component;

    /**
     * The originally set width of the component.
     */
    private int originalWidth;

    /**
     * The originally set height of the component.
     */
    private int originalHeight;

    /**
     * The position this grid component should be kept in relative to its provided grid space.
     */
    private GridPosition gridPosition;

    /**
     * Constructs a new GridComponent.
     *
     * @param component      the component to manage
     * @param originalWidth  the originally set width of the component
     * @param originalHeight the originally set height of the component
     * @param gridPosition   the position this grid component should be kept in relative to its provided grid space
     */
    public GridComponent(Component component, int originalWidth, int originalHeight, GridPosition gridPosition) {
        this.component = component;
        this.originalWidth = originalWidth;
        this.originalHeight = originalHeight;
        this.gridPosition = gridPosition;
    }

    /**
     * Returns the managed component.
     *
     * @return the managed component
     */
    public Component getComponent() {
        return component;
    }

    /**
     * Sets the managed component.
     *
     * @param component the managed component
     */
    public void setComponent(Component component) {
        this.component = component;
    }

    /**
     * Returns the original component width.
     *
     * @return the original component width
     */
    public int getOriginalWidth() {
        return originalWidth;
    }

    /**
     * Sets the original component width.
     *
     * @param originalWidth the original component width
     */
    public void setOriginalWidth(int originalWidth) {
        this.originalWidth = originalWidth;
    }

    /**
     * Returns the original component height.
     *
     * @return the original component height
     */
    public int getOriginalHeight() {
        return originalHeight;
    }

    /**
     * Sets the original component height.
     *
     * @param originalHeight the original component height
     */
    public void setOriginalHeight(int originalHeight) {
        this.originalHeight = originalHeight;
    }

    /**
     * Returns the position this grid component should be kept in.
     *
     * @return the position this grid component should be kept in
     */
    public GridPosition getPosition() {
        return gridPosition;
    }

    /**
     * Sets the position this grid component should be kept in.
     *
     * @param gridPosition the position this grid component should be kept in
     */
    public void setPosition(GridPosition gridPosition) {
        this.gridPosition = gridPosition;
    }
}
