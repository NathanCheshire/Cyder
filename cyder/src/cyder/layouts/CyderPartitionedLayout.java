package cyder.layouts;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import cyder.ui.CyderPanel;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * A layout designed to allow partitioning of a row or column into different parts each taking up
 * a certain percent of the whole partitioned area. One might want 30%, 20%, and 50% for a row
 * and this will allow the user to do so.
 */
public class CyderPartitionedLayout extends CyderLayout {
    /**
     * The range all partitions must fall within.
     */
    public static final Range<Integer> PARTITION_RANGE = Range.closed(0, 100);

    /**
     * The maximum partition value which all partitions and
     * the sum of all partitions must be less than or equal to.
     */
    public static final int MAX_PARTITION = PARTITION_RANGE.upperEndpoint();

    /**
     * The amount of partition space to assign a component added without a provided partition space.
     */
    public static final int DEFAULT_PARTITION_SPACE_PERCENT = 10;

    /**
     * The partition space to partition to a new component being added without a specified partition space.
     */
    private int newComponentPartitionSpace = DEFAULT_PARTITION_SPACE_PERCENT;

    /**
     * The sum of all partitions from {@link #partitions}.
     */
    private int partitionsSum = 0;

    /**
     * The list of all added partitions.
     */
    private final LinkedList<Integer> partitions;

    /**
     * The possible directions to lay components out.
     */
    public enum PartitionDirection {
        ROW, COLUMN
    }

    /**
     * The current direction to lay the partitioned components out.
     */
    private PartitionDirection partitionDirection = PartitionDirection.COLUMN;

    /**
     * The alignment of a component within a partition space provided it is smaller than the partitioned area.
     */
    public enum PartitionAlignment {
        TOP_LEFT, TOP, TOP_RIGHT,
        LEFT, CENTER, RIGHT,
        BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT
    }

    /**
     * The default component alignment for new components.
     */
    private PartitionAlignment newComponentPartitionAlignment = PartitionAlignment.CENTER;

    /**
     * Constructs a new partitioned layout.
     */
    public CyderPartitionedLayout() {
        this(0);
    }

    /**
     * The direct components managed by this layout.
     */
    private final ArrayList<PartitionedComponent> components;

    /**
     * Constructs a new partitioned layout.
     *
     * @param firstPartition the partition for the first component
     */
    public CyderPartitionedLayout(int firstPartition) {
        Preconditions.checkArgument(PARTITION_RANGE.contains(firstPartition));

        partitions = new LinkedList<>();
        components = new ArrayList<>();

        partitions.add(firstPartition);
        partitionsSum += firstPartition;
    }

    /**
     * Constructs a new partitioned layout.
     *
     * @param firstPartition  the partition for the first component
     * @param otherPartitions the partitions for any other components
     */
    public CyderPartitionedLayout(int firstPartition, int... otherPartitions) {
        this(firstPartition);

        for (int partition : otherPartitions) {
            if (PARTITION_RANGE.contains(partition + partitionsSum)) {
                partitions.add(partition);
                partitionsSum += partition;
            } else {
                throw new IllegalArgumentException("Could not add partition as will cause overflow. Partition: "
                        + partition + ", totalPercent: " + partitionsSum);
            }
        }
    }

    /**
     * Returns a list of all the partitions.
     *
     * @return a list of all the partitions
     */
    public ImmutableList<Integer> getPartitions() {
        return ImmutableList.copyOf(partitions);
    }

    /**
     * Returns the sum of all partitions.
     *
     * @return the sum of all partitions
     */
    public int getPartitionsSum() {
        return partitionsSum;
    }

    /**
     * Returns the partition for the component at the specified index.
     *
     * @param index the index of the component to return the partition of
     * @return the partition for the component at the specified index
     */
    public int getPartition(int index) {
        Preconditions.checkArgument(index >= 0);
        Preconditions.checkArgument(index < partitions.size());

        return partitions.get(index);
    }

    /**
     * Sets the partition for the component at the specified index.
     *
     * @param index     the index of the partition to update
     * @param partition the new partition value
     */
    public void setPartition(int index, int partition) {
        Preconditions.checkArgument(index >= 0);
        Preconditions.checkArgument(index < partitions.size());

        int oldPartition = partitions.get(index);

        if (partition > oldPartition) {
            int newPartitionSum = partitionsSum + partition - oldPartition;

            if (newPartitionSum > MAX_PARTITION) {
                throw new IllegalArgumentException("Requested partition overriding old partition at"
                        + " provided index will cause partition sum to be: " + newPartitionSum);
            }

        }

        partitions.set(index, partition);
    }

    /**
     * Returns the {@link PartitionDirection}, that of {@link PartitionDirection#COLUMN}
     * or {@link PartitionDirection#ROW}
     *
     * @return the partition direction
     */
    public PartitionDirection getPartitionDirection() {
        return partitionDirection;
    }

    /**
     * Returns the {@link PartitionDirection}, that of {@link PartitionDirection#COLUMN}
     * or {@link PartitionDirection#ROW}
     *
     * @param partitionDirection the new partition direction
     */
    public void setPartitionDirection(PartitionDirection partitionDirection) {
        if (this.partitionDirection == partitionDirection) {
            return;
        }

        this.partitionDirection = partitionDirection;

        revalidateComponents();
    }

    /**
     * {@inheritDoc}
     */
    public ArrayList<Component> getLayoutComponents() {
        ArrayList<Component> ret = new ArrayList<>(components.size());

        for (PartitionedComponent component : components) {
            ret.add(component.getComponent());
        }

        return ret;
    }

    /**
     * {@inheritDoc}
     */
    public Dimension getPackSize() {
        throw new UnsupportedOperationException("Not implemented");
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
     * Returns the partition space given to a new component if none is specified.
     *
     * @return the partition space given to a new component if none is specified
     */
    public int getNewComponentPartitionSpace() {
        return newComponentPartitionSpace;
    }

    /**
     * Sets the partition space given to a new component if none is specified.
     *
     * @param newComponentPartitionSpace the partition space given to a new component if none is specified
     */
    public void setNewComponentPartitionSpace(int newComponentPartitionSpace) {
        this.newComponentPartitionSpace = newComponentPartitionSpace;
    }

    /**
     * Returns the partition alignment for new components.
     *
     * @return the partition alignment for new components
     */
    public PartitionAlignment getNewComponentPartitionAlignment() {
        return newComponentPartitionAlignment;
    }

    /**
     * Sets the partition alignment for new components.
     *
     * @param newComponentPartitionAlignment the partition alignment for new components
     */
    public void setNewComponentPartitionAlignment(PartitionAlignment newComponentPartitionAlignment) {
        this.newComponentPartitionAlignment = newComponentPartitionAlignment;
    }

    /**
     * {@inheritDoc}
     */
    public void revalidateComponents() {
        // todo
    }

    /**
     * {@inheritDoc}
     */
    public void removeComponent(Component component) {
        // todo add and give default partition space which can be set
    }

    public void removeComponent(int index) {
        Preconditions.checkArgument(index >= 0);
        Preconditions.checkArgument(index < components.size());

        // todo remove component but also partition area
    }

    /**
     * {@inheritDoc}
     */
    public void addComponent(Component component) {
        Preconditions.checkNotNull(component);

        boolean in = false;
        for (PartitionedComponent partitionedComponent : components) {
            if (partitionedComponent.getComponent().equals(component)) {
                in = true;
                break;
            }
        }
        Preconditions.checkArgument(!in, "Layout already contains component");

        addComponent(component, newComponentPartitionSpace);
    }

    /**
     * Adds the provided component to the end of the components
     * list and provides the partition space requested.
     *
     * @param component      the component to add to the components list
     * @param partitionSpace the space to be partitioned for this component
     */
    public void addComponent(Component component, int partitionSpace) {
        Preconditions.checkNotNull(component);
        Preconditions.checkArgument(PARTITION_RANGE.contains(partitionSpace));
        Preconditions.checkArgument(partitionSpace + partitionsSum < MAX_PARTITION);

        addComponent(component, partitionSpace, newComponentPartitionAlignment);
    }

    /**
     * Adds the provided component to the end of the components
     * list and provides the partition space requested.
     *
     * @param component          the component to add to the components list
     * @param partitionSpace     the space to be partitioned for this component
     * @param partitionAlignment the alignment for the partitioned component if it
     *                           does not precisely fit the partitioned area
     */
    public void addComponent(Component component, int partitionSpace, PartitionAlignment partitionAlignment) {
        Preconditions.checkNotNull(component);
        Preconditions.checkArgument(PARTITION_RANGE.contains(partitionSpace));
        Preconditions.checkArgument(partitionSpace + partitionsSum < MAX_PARTITION);

        // todo
    }
}
