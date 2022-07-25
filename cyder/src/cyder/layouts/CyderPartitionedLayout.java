package cyder.layouts;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

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
     * Constructs a new partitioned layout.
     */
    public CyderPartitionedLayout() {
        this(0);
    }

    /**
     * Constructs a new partitioned layout.
     *
     * @param firstPartition the partition for the first component
     */
    public CyderPartitionedLayout(int firstPartition) {
        Preconditions.checkArgument(PARTITION_RANGE.contains(firstPartition));

        partitions = new LinkedList<>();
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
        Preconditions.checkArgument(PARTITION_RANGE.contains(firstPartition));

        partitions = new LinkedList<>();
        partitions.add(firstPartition);
        partitionsSum += firstPartition;

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

        // todo revalidate
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
        // todo revalidate components
    }
}
