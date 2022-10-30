package cyder.layouts;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.ui.CyderPanel;
import cyder.utils.StringUtil;

import java.awt.*;
import java.util.ArrayList;

/**
 * A grid layout specific for {@link CyderPanel}s.
 */
public class CyderGridLayout extends CyderLayout {
    /**
     * The amount of horizontal cells allowable for this grid layout.
     */
    private final int horizontalCells;

    /**
     * The amount of vertical cells allowable for this grid layout.
     */
    private final int verticalCells;

    /**
     * The list of components to be linked to the CyderPanel this LayoutManager is managing.
     */
    private final GridComponent[][] components;

    /**
     * {@inheritDoc}
     */
    @Override
    public ArrayList<Component> getLayoutComponents() {
        ArrayList<Component> ret = new ArrayList<>();

        for (GridComponent[] component : components) {
            for (int j = 0 ; j < components[0].length ; j++) {
                if (component[j] != null)
                    ret.add(component[j].getComponent());
            }
        }

        return ret;
    }

    /**
     * Constructs a new CyderGridLayout with a singular grid cell.
     */
    public CyderGridLayout() {
        this(1, 1);
    }

    /**
     * Constructs a new CyderGridLayout with the provided grid dimensions.
     *
     * @param horizontalCells the amount of horizontal cells to have in the Layout
     * @param verticalCells   the amount of vertical cells to have in the Layout
     */
    public CyderGridLayout(int horizontalCells, int verticalCells) {
        Preconditions.checkArgument(horizontalCells >= 1);
        Preconditions.checkArgument(verticalCells >= 1);

        this.horizontalCells = horizontalCells;
        this.verticalCells = verticalCells;

        components = new GridComponent[horizontalCells][verticalCells];
    }

    /**
     * The CyderPanel this layout manager will manage.
     */
    private CyderPanel associatedPanel;

    /**
     * Sets the CyderPanel to manage. Components this LM has been given thus far
     * will be evaluated and added to the Panel.
     *
     * @param associatedPanel the CyderPanel to manage
     */
    public void setAssociatedPanel(CyderPanel associatedPanel) {
        this.associatedPanel = Preconditions.checkNotNull(associatedPanel);

        revalidateComponents();
    }

    /**
     * Revalidates the bounds of all components on the grid.
     */
    public void revalidateComponents() {
        if (associatedPanel == null)
            return;

        int widthPartition = associatedPanel.getWidth() / horizontalCells;
        int heightPartition = associatedPanel.getHeight() / verticalCells;

        Component focusOwner = null;

        for (int xCell = 0 ; xCell < horizontalCells ; xCell++) {
            for (int yCell = 0 ; yCell < verticalCells ; yCell++) {
                // If no component exists at this location then continue
                if (components[xCell][yCell] == null)
                    continue;

                // Figure out the starting values for this cell
                int startX = xCell * widthPartition;
                int startY = yCell * heightPartition;

                //  Instantiate a reference component for the current component
                GridComponent refComponent = components[xCell][yCell];

                // If this is the first focused component we have come across
                // set it as the focus owner
                if (refComponent.getComponent().isFocusOwner() && focusOwner == null)
                    focusOwner = refComponent.getComponent();

                // If the component is a CyderPanel, give it as much space to work with as possible
                if (refComponent.getComponent() instanceof CyderPanel) {
                    refComponent.getComponent().setBounds(startX, startY, widthPartition, heightPartition);
                    ((CyderPanel) (refComponent.getComponent())).revalidateComponents();
                }
                // Otherwise if it doesn't fit in the partitioned space,
                // set the size to as big as we can let it be so now overflow is visible
                else if (refComponent.getOriginalWidth() >= widthPartition ||
                        refComponent.getOriginalHeight() >= heightPartition) {
                    refComponent.getComponent().setBounds(startX, startY,
                            // Only one might be over the max value so take the min of partition and len
                            Math.min(widthPartition, refComponent.getOriginalWidth()),
                            Math.min(heightPartition, refComponent.getOriginalHeight()));
                }
                // Otherwise it fits so calculate how to place it in the grid space
                else {
                    // Figure out values to center the component in the grid space
                    int addX = (widthPartition - refComponent.getOriginalWidth()) / 2;
                    int addY = (heightPartition - refComponent.getOriginalHeight()) / 2;

                    // Figure out values for how to move the component from the centered
                    // position to any possible Position value
                    int adjustX = 0;
                    int adjustY = 0;

                    switch (refComponent.getPosition()) {
                        case TOP_LEFT:
                            adjustX = -(widthPartition - refComponent.getOriginalWidth()) / 2;
                            adjustY = -(heightPartition - refComponent.getOriginalHeight()) / 2;
                            break;
                        case TOP:
                            adjustY = -(heightPartition - refComponent.getOriginalHeight()) / 2;
                            break;
                        case TOP_RIGHT:
                            adjustX = (widthPartition - refComponent.getOriginalWidth()) / 2;
                            adjustY = -(heightPartition - refComponent.getOriginalHeight()) / 2;
                            break;
                        case LEFT:
                            adjustX = -(widthPartition - refComponent.getOriginalWidth()) / 2;
                            break;
                        case MIDDLE:
                            break;
                        case RIGHT:
                            adjustX = (widthPartition - refComponent.getOriginalWidth()) / 2;
                            break;
                        case BOTTOM_LEFT:
                            adjustX = -(widthPartition - refComponent.getOriginalWidth()) / 2;
                            adjustY = (heightPartition - refComponent.getOriginalHeight()) / 2;
                            break;
                        case BOTTOM:
                            adjustY = (heightPartition - refComponent.getOriginalHeight()) / 2;
                            break;
                        case BOTTOM_RIGHT:
                            adjustX = (widthPartition - refComponent.getOriginalWidth()) / 2;
                            adjustY = (heightPartition - refComponent.getOriginalHeight()) / 2;
                            break;
                    }

                    // Set the component bounds with the original size since the component fits
                    refComponent.getComponent().setBounds(startX + addX + adjustX,
                            startY + addY + adjustY,
                            refComponent.getOriginalWidth(),
                            refComponent.getOriginalHeight());
                }

                associatedPanel.add(refComponent.getComponent());
            }
        }

        if (focusOwner != null) {
            focusOwner.requestFocus();
        }
    }

    /**
     * Adds the provided component to the grid at the first available space.
     *
     * @param component the component to add to the grid if possible
     */
    public void addComponent(Component component) {
        for (int x = 0 ; x < horizontalCells ; x++) {
            for (int y = 0 ; y < verticalCells ; y++) {
                if (components[x][y] == null) {
                    components[x][y] = new GridComponent(component, //defaults here
                            component.getWidth(), component.getHeight(), GridPosition.MIDDLE);
                    repaint();
                    return;
                }
            }
        }

    }

    /**
     * Adds the provided component to the grid at the first available space.
     *
     * @param component       the component to add to the grid if possible
     * @param sectionPosition the position to set the component to if it fits
     *                        in the partitioned space or how to position the component should it overflow the partitioned space
     * @return whether the component was added successfully
     */
    @CanIgnoreReturnValue
    public boolean addComponent(Component component, GridPosition sectionPosition) {
        for (int x = 0 ; x < horizontalCells ; x++) {
            for (int y = 0 ; y < verticalCells ; y++) {
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

    /**
     * Adds the provided component to the grid at the specified location.
     *
     * @param component the component to add to the grid
     * @param x         the x value to add the component to
     * @param y         the y value to add the component to
     * @return whether the component was added to the panel
     */
    @CanIgnoreReturnValue
    public boolean addComponent(Component component, int x, int y) {
        Preconditions.checkNotNull(component);
        Preconditions.checkNotNull(components);
        Preconditions.checkArgument(x >= 0);
        Preconditions.checkArgument(y >= 0);
        Preconditions.checkArgument(x <= horizontalCells - 1);
        Preconditions.checkArgument(y <= verticalCells - 1);

        if (components[x][y] != null) {
            return false;
        }

        components[x][y] = new GridComponent(component, component.getWidth(),
                component.getHeight(), GridPosition.MIDDLE);
        repaint();

        return true;
    }

    /**
     * Adds the component to the grid at the specified location with the provided Position value.
     *
     * @param component       the component to add to the grid
     * @param x               the x value to add the component to
     * @param y               the y value to add the component to
     * @param sectionPosition the position value to use to
     *                        figure out how to place the component in its cell
     * @return whether the component was added to the panel
     */
    @CanIgnoreReturnValue
    public boolean addComponent(Component component, int x, int y, GridPosition sectionPosition) {
        Preconditions.checkNotNull(component);
        Preconditions.checkNotNull(components);
        Preconditions.checkNotNull(sectionPosition);
        Preconditions.checkArgument(x >= 0);
        Preconditions.checkArgument(y >= 0);
        Preconditions.checkArgument(x < horizontalCells);
        Preconditions.checkArgument(y < verticalCells);

        if (components[x][y] != null) {
            return false;
        }

        components[x][y] = new GridComponent(component, component.getWidth(), component.getHeight(), sectionPosition);
        repaint();

        return true;
    }

    /**
     * Removes the specified component from the grid.
     *
     * @param component the component to remove from the panel
     */
    public void removeComponent(Component component) {
        Preconditions.checkNotNull(components);
        Preconditions.checkNotNull(component);

        for (int x = 0 ; x < horizontalCells ; x++) {
            for (int y = 0 ; y < verticalCells ; y++) {
                if (components[x][y].getComponent() == component) {
                    components[x][y] = null;
                    return;
                }
            }
        }
    }

    /**
     * Removes the component at the specified location from the grid.
     *
     * @param x the x value of the component to remove
     * @param y the y value of the component to remove
     * @return whether the component was successfully removed
     */
    public boolean removeComponent(int x, int y) {
        Preconditions.checkNotNull(components);
        Preconditions.checkArgument(x >= 0);
        Preconditions.checkArgument(y >= 0);
        Preconditions.checkArgument(x <= horizontalCells - 1);
        Preconditions.checkArgument(y <= verticalCells - 1);

        if (components[x][y] == null) {
            return false;
        } else {
            components[x][y] = null;
            return true;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dimension getPackSize() {
        int maxLineWidth = 0;

        for (GridComponent[] componentRow : components) {
            int acc = 0;

            for (GridComponent component : componentRow) {
                acc += component.getOriginalWidth();
            }

            maxLineWidth = Math.max(maxLineWidth, acc);
        }

        // Rotate to temporary array
        GridComponent[][] tempComponents = new GridComponent[components.length][components[0].length];
        for (int i = 0 ; i < components[0].length ; i++) {
            for (int j = components.length - 1 ; j >= 0 ; j--) {
                tempComponents[i][j] = components[j][i];
            }
        }

        int maxColumnHeight = 0;

        for (GridComponent[] componentColumn : tempComponents) {
            int acc = 0;

            for (GridComponent component : componentColumn) {
                acc += component.getOriginalHeight();
            }

            maxColumnHeight = Math.max(maxColumnHeight, acc);
        }

        return new Dimension(maxLineWidth, maxColumnHeight);
    }

    /**
     * Standard overridden toString() method to use reflection.
     *
     * @return a String representation of this LM
     */
    @Override
    public String toString() {
        return StringUtil.commonCyderUiToString(this);
    }

}
