package cyder.props;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;

import java.util.List;

/**
 * A list type for the value of a prop.
 */
@Immutable
public class PropValueList {
    /**
     * The list containing the values for a prop.
     */
    public final ImmutableList<String> list;

    /**
     * Constructs a new prop value list object.
     *
     * @param list the values list.
     */
    public PropValueList(List<String> list) {
        Preconditions.checkNotNull(list);

        this.list = ImmutableList.copyOf(list);
    }

    /**
     * Returns the list containing the values for a prop.
     *
     * @return the list containing the values for a prop
     */
    public ImmutableList<String> getList() {
        return list;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof PropValueList)) {
            return false;
        }

        PropValueList other = (PropValueList) o;
        return other.getList().equals(list);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return list.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "PropValueList{list=" + list + "}";
    }
}
