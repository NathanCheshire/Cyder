package cyder.structures;

import java.util.Arrays;

public class CyderHeap {
    private Object[] heapStruct;
    private int size = 0;

    private int maxSize;

    public static int ABSOLUTE_MIN_SIZE = 1;
    public static int ABSOLUTE_MAX_SIZE = Integer.MAX_VALUE;

    /**
     * Standard getter for the current size of this heap
     * @return the heap size
     */
    public int getSize() {
        return this.size;
    }

    /**
     * Initializes the heap structure with an initial data point and a maximum allowable size.
     * @param data the initial data of the heap
     * @param maxSize the maximum allowable size of this heap instance
     */
    public CyderHeap(Object data, int maxSize) {
        if (maxSize < ABSOLUTE_MIN_SIZE)
            throw new IllegalStateException("Provided length does not allow the provided data to exist within the heap");

        this.maxSize = maxSize;
        heapStruct = new Object[maxSize];
        heapStruct[0] = data;

        if (data != null)
            this.size = 0;
        else
            this.size = 1;
    }

    /**
     * Initializes the heap structure with default data and the maximum allowable size of this instance of CyderHeap.
     * @param data the data to populate the first "data.length" slots wit
     * @param maxSize the maximu allowable size of this heap instance
     */
    public CyderHeap(Object[] data, int maxSize) {
        if (maxSize < 0)
            throw new IllegalArgumentException("Max size must be above 0");

        if (data == null || data.length == 0){
            this.maxSize = maxSize;
            heapStruct = new Object[maxSize];
            this.size = 0;
        } else {
            if (data.length > maxSize)
                throw new IllegalStateException("Provided data exceeds provided max length");
            else if (maxSize < ABSOLUTE_MIN_SIZE)
                throw new IllegalStateException("Absolute min size exceeded");

            System.arraycopy(data, 0, heapStruct, 0, data.length);

            this.size = data.length;
        }
    }

    /**
     * Finds the left child index of the node at the provided index.
     * @param position the index of the parent to find the left child of
     * @return the index of the left child
     */
    private int leftChildIndex(int position) {
        return 2 * position;
    }

    /**
     * Finds the right child index of the node at the provided index.
     * @param position the index of the parent to find the right child of
     * @return the index of the right child
     */
    private int rightChildIndex(int position) {
        return 2 * position + 1;
    }

    /**
     * Finds the index of the parent of the node at the provided index.
     * @param position the index to find the parent of
     * @return the index of the parent
     */
    private int parent(int position) {
        return position / 2;
    }

    /**
     * Determins if the node at the given index is a leaf, leafs have no children.
     * @param position the position of the node
     * @return whether or not the provided index node was a leaf
     */
    private boolean isLeafNode(int position) {
        //if it's greater than the size/2 and the position is still less than the size of our heap
        return (position > (size / 2) && position <= size);
    }

    /**
     * Swaps the two elements at the provided indicies
     * @param index1 the first index
     * @param index2 the second index
     */
    public void swap(int index1, int index2) {
        if (index1 < 0 || index2 < 0 || index1 > size || index2 > size)
            throw new IndexOutOfBoundsException("Provided index is out of bounds");
        if (index1 == index2)
            return;

        Object uno = heapStruct[index1];
        heapStruct[index1] = heapStruct[index2];
        heapStruct[index2] = uno;
    }

    /**
     * Inserts the provided object into the back of the heap
     * @param data the object to insert
     */
    public void insert(Object data) {
        if (this.size + 1 > maxSize)
            throw new IllegalStateException("CyderHeap cannot accept any more elements as the maximum sized has been reached");

        heapStruct[size] = data;

        this.size++;
    }

    /**
     * Removes the root from the heap as is the point of a heap data structure.
     */
    public Object remove() {
        if (this.size - 1 < 0)
            throw new IllegalStateException("CyderHeap is empty");

        Object ret = heapStruct[0];
        heapStruct[0] = heapStruct[size - 1];
        heapStruct[size - 1] = null;

        this.size--;

        return ret;
    }

    /**
     * Returns a string representation of the CyderHeap data structure.
     * @return the string representation promised
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0 ; i < this.size ; i++) {
            sb.append(heapStruct[i]);

            if (i != this.size - 1)
                sb.append(" -> ");
        }

        return sb.toString();
    }

    /**
     * Standard getter for the current maximum allowable size of the heap structure
     * @return the currently set max size of the heap structure
     */
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * Sets a new max size for the heap. Any elements outside of the inclusive range [0, maxSize] will be lost forever.
     * @param maxSize the new maximum size of the heap
     */
    public void setMaxSize(int maxSize) {
        if (maxSize == this.maxSize)
            return;

        //set new size
        this.maxSize = maxSize;

        //propogate changes by changing the array length
        heapStruct = Arrays.copyOf(heapStruct, maxSize);
    }
}
