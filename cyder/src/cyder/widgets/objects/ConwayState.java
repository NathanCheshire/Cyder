package cyder.widgets.objects;

import com.google.errorprone.annotations.Immutable;
import cyder.utilities.ReflectionUtil;

import java.awt.*;
import java.util.LinkedList;

/**
 * An object used to store a Conway's game of life grid state.
 */
@Immutable
public class ConwayState {
    /**
     * The name of the state.
     */
    private final String name;

    /**
     * The grid length of the state.
     */
    private final int gridSize;

    /**
     * The list of nodes
     */
    private final LinkedList<Point> nodes;

    /**
     * Constructs a new ConwayState object.
     *
     * @param name     the name of the state
     * @param gridSize the length of the state
     * @param nodes    the list of nodes
     */
    public ConwayState(String name, int gridSize, LinkedList<Point> nodes) {
        this.name = name;
        this.gridSize = gridSize;
        this.nodes = nodes;
    }

    /**
     * Returns the name of the state.
     *
     * @return the name of the state
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the length of the state.
     *
     * @return the length of the state
     */
    public int getGridSize() {
        return gridSize;
    }

    /**
     * Returns the list of nodes for the state.
     *
     * @return the list of nodes for the state
     */
    public LinkedList<Point> getNodes() {
        return nodes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return ReflectionUtil.commonCyderToString(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = name.hashCode();
        ret = 31 * ret + Integer.hashCode(gridSize);
        ret = 31 * ret + nodes.hashCode();
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof ConwayState)) {
            return false;
        }

        ConwayState other = (ConwayState) o;

        return other.getName().equals(getName()) && other.getGridSize() == getGridSize()
                && other.getNodes().equals(getNodes());
    }
}
