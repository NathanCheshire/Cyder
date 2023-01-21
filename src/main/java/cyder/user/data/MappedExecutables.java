package cyder.user.data;

import com.google.common.base.Preconditions;
import cyder.logging.LogTag;
import cyder.logging.Logger;

import java.util.ArrayList;
import java.util.Collection;

/**
 * A class representing a list of mapped executables for a user data object.
 */
public class MappedExecutables {
    /**
     * The list of mapped executables.
     */
    private ArrayList<MappedExecutable> mappedExecutables;

    /**
     * Constructs a new mapped executables.
     */
    public MappedExecutables() {
        this(new ArrayList<>());
    }

    /**
     * Constructs a new mapped executables.
     */
    private MappedExecutables(Collection<MappedExecutable> mappedExecutables) {
        Preconditions.checkNotNull(mappedExecutables);
        this.mappedExecutables = new ArrayList<>(mappedExecutables);

        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Returns the list of mapped executables.
     *
     * @return the list of mapped executables
     */
    public ArrayList<MappedExecutable> getExecutables() {
        return mappedExecutables;
    }

    /**
     * Sets the list of mapped executables.
     *
     * @param mappedExecutables the list of mapped executables
     */
    public void setMappedExecutables(ArrayList<MappedExecutable> mappedExecutables) {
        Preconditions.checkNotNull(mappedExecutables);

        this.mappedExecutables = mappedExecutables;
    }

    /**
     * Returns the size of the encapsulated mapped executables list.
     *
     * @return the size of the encapsulated mapped executables list
     */
    public int size() {
        return mappedExecutables.size();
    }

    /**
     * Constructs and returns a new mapped executables object.
     *
     * @param collection the collection to construct the mapped executables object from
     * @return a new mapped executables object
     */
    public static MappedExecutables from(Collection<MappedExecutable> collection) {
        return new MappedExecutables(collection);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof MappedExecutables)) {
            return false;
        }

        MappedExecutables other = (MappedExecutables) o;
        return mappedExecutables.equals(other.mappedExecutables);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return mappedExecutables.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "MappedExecutables{"
                + "mappedExecutables=" + mappedExecutables
                + "}";
    }
}
