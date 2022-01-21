package cyder.objects;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A simple string data structure to hold multiple strings that are related.
 * Size is immutable for this class.
 */
public class MultiString {
    private ArrayList<String> strings;
    private int size = 0;

    /**
     * Constructor to initialize the size of this multistring.
     *
     * @param size the size of the multistring
     */
    public MultiString(int size) {
        if (size < 0)
            throw new IllegalArgumentException("Provided size is invalid: " + size);

        this.size = size;
        this.strings = new ArrayList<>();
    }

    /**
     * Constructor to initialize the size and provide an initial data point.
     *
     * @param size the size of the multistring
     * @param initialData the initial data
     */
    public MultiString(int size, String initialData) {
        if (size < 0)
            throw new IllegalArgumentException("Provided size is invalid: " + size);
        if (initialData == null)
            throw new IllegalArgumentException("Provided initial data is null");

        this.size = size;
        this.strings = new ArrayList<>();
        strings.add(initialData);
    }

    /**
     * Constructor to initialize the size and multiple initial data points.
     *
     * @param size the size of the multistring
     * @param initialData the initial data
     */
    public MultiString(int size, String[] initialData) {
        if (size < 0)
            throw new IllegalArgumentException("Provided size is invalid: " + size);
        if (initialData.length == 0)
            throw new IllegalArgumentException("Provided initial data is empty");
        if (initialData.length < size)
            throw new IllegalArgumentException("Provided initial data exceeds provided size");

        this.size = size;
        this.strings = new ArrayList<>();

        strings.addAll(Arrays.asList(initialData));
    }

    /**
     * Returns the strings associated with this MultiString object.
     *
     * @return the strings associated with this MultiString object
     */
    public ArrayList<String> getStrings() {
        return strings;
    }

    /**
     * Returns the size of the Strings asscoiated with this MultiString object.
     *
     * @return the size of the Strings asscoiated with this MultiString object
     */
    public int getSize() {
        return size;
    }

    /**
     * Returns the string at the provided index if it exists.
     *
     * @param index the index of the data to return
     * @return the string at the provided index if it exists
     */
    public String get(int index) {
        if (strings == null || strings.isEmpty())
            throw new IllegalArgumentException("No strings to index");
        if (index < 0 || index > size)
            throw new IllegalArgumentException("Provided index is out of bounds");

        return strings.get(index);
    }
}
