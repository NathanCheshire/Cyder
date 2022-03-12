package cyder.user.objects;

import cyder.handlers.internal.Logger;
import cyder.utilities.ReflectionUtil;

/**
 * Class representing a name and a path to an executable/file/link to open.
 */
public class MappedExecutable {
    private final String name;
    private final String filepath;

    public MappedExecutable(String name, String filepath) {
        this.name = name;
        this.filepath = filepath;

        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    public String getName() {
        return name;
    }

    public String getFilepath() {
        return filepath;
    }

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

    @Override
    public String toString() {
        return ReflectionUtil.commonCyderToString(this);
    }

    @Override
    public int hashCode() {
        int ret = name.hashCode();
        ret = 31 * ret + filepath.hashCode();
        return ret;
    }
}
