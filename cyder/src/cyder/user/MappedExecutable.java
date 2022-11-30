package cyder.user;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.Immutable;
import cyder.console.Console;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.utils.StringUtil;

/**
 * Class representing a name and a path to an executable/file/link to open.
 * Instances of this class are immutable.
 */
@SuppressWarnings("ClassCanBeRecord") /* GSON serialization */
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
     * @param name     the display name of the mapped executable
     * @param filepath the path to the file to open
     */
    public MappedExecutable(String name, String filepath) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(filepath);
        Preconditions.checkArgument(!name.isEmpty());
        Preconditions.checkArgument(!filepath.isEmpty());

        this.name = name;
        this.filepath = filepath;

        Logger.log(LogTag.OBJECT_CREATION, this);
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
     * Displays a notification on the console informing the user that this mapped exe was invoked.
     */
    public void displayInvokedNotification() {
        Preconditions.checkState(!Console.INSTANCE.isClosed());

        Console.INSTANCE.getConsoleCyderFrame().notify("Invoking mapped exe: " + name);
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

        return other.getName().equals(getName())
                && other.getFilepath().equals(getFilepath());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return StringUtil.commonCyderToString(this);
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
