package cyder.user.data;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import cyder.logging.LogTag;
import cyder.logging.Logger;

import java.util.Collection;

/**
 * A class representing a list of mapped executables for a user data object.
 */
@Immutable
public class MappedExecutables {
    /**
     * The list of mapped executables.
     */
    private final ImmutableList<MappedExecutable> mappedExecutables;

    /**
     * Constructs a new mapped executables.
     */
    public MappedExecutables() {
        this(ImmutableList.of());
    }

    /**
     * Constructs a new mapped executables.
     */
    private MappedExecutables(ImmutableList<MappedExecutable> mappedExecutables) {
        this.mappedExecutables = Preconditions.checkNotNull(mappedExecutables);

        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Returns the list of mapped executables.
     *
     * @return the list of mapped executables
     */
    public ImmutableList<MappedExecutable> getExecutables() {
        return mappedExecutables;
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
        return new MappedExecutables(ImmutableList.copyOf(collection));
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
