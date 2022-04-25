package cyder.user.objects;

import com.google.errorprone.annotations.Immutable;
import cyder.handlers.internal.Logger;
import cyder.utilities.ReflectionUtil;

/**
 * Class representing a name and a path to an executable/file/link to open.
 * Instances of this class are immutable.
 */
@Immutable
public class MappedExecutable {
    /**
     * The display name of the mapped executable.
     */
    private final String name;

    /**
     * The path to the file to open for the executable.
     */
    private final String filepath;

    /**
     * Constructs a new mapped executable object.
     *
     * @param name the display name of the mapped executable
     * @param filepath the path to the file to open
     */
    public MappedExecutable(String name, String filepath) {
        this.name = name;
        this.filepath = filepath;

        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    /**
     * Returns the name of this mapped executable.
     *
     * @return the name of this mapped executable
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the file path of this executable.
     *
     * @return the file path of this executable
     */
    public String getFilepath() {
        return filepath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof MappedExecutable)) {
            return false;
        }

        MappedExecutable other = (MappedExecutable) o;

        return other.getName().equals(getName()) && other.getFilepath().equals(getFilepath());
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
        ret = 31 * ret + filepath.hashCode();
        return ret;
    }
}
