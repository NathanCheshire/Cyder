package cyder.utilities.objects;

import cyder.enums.LoggerTag;
import cyder.handlers.internal.Logger;

/**
 * Associated name of a file and it's size.
 */
public final class FileSize {
    /**
     * The size of the file.
     */
    private long size;

    /**
     * The name of the file.
     */
    private String name;

    /**
     * Creates a new file size object.
     *
     * @param name the name of the file
     * @param size the size of the file in bytes
     */
    public FileSize(String name, long size) {
        this.size = size;
        this.name = name;

        Logger.log(LoggerTag.OBJECT_CREATION, this);
    }

    /**
     * Returns the size of the file in bytes.
     *
     * @return the size of the file in bytes
     */
    public long getSize() {
        return size;
    }

    /**
     * Sets the size of the file in bytes.
     *
     * @param size the size of the file in bytes
     */
    public void setSize(long size) {
        this.size = size;
    }

    /**
     * Returns the name of the file.
     *
     * @return the name of the file
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the file.
     *
     * @param name the name of the file
     */
    public void setName(String name) {
        this.name = name;
    }
}
