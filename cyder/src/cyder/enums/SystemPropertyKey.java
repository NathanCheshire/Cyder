package cyder.enums;

// todo search for all keys in project and use these enums instead

/**
 * {@link System#getProperty(String)} keys.
 */
public enum SystemPropertyKey {
    JAVA_VERSION("java.version", "Java Runtime Environment version"),
    JAVA_VENDOR("java.vendor", "Java Runtime Environment vendor"),
    JAVA_VENDOR_URL("java.vendor.url", "Java vendor URL"),
    JAVA_HOME("java.home", "Java installation directory"),
    JAVA_VM_SPECIFICATION_VERSION("java.vm.specification.version", "Java Virtual Machine specification version"),
    JAVA_VM_SPECIFICATION_VENDOR("java.vm.specification.vendor", "Java Virtual Machine specification vendor"),
    JAVA_VM_SPECIFICATION_NAME("java.vm.specification.name", "Java Virtual Machine specification name"),
    JAVA_VM_VERSION("java.vm.version", "Java Virtual Machine implementation version"),
    JAVA_VM_VENDOR("java.vm.vendor", "Java Virtual Machine implementation vendor"),
    JAVA_VM_NAME("java.vm.name", "Java Virtual Machine implementation name"),
    JAVA_SPECIFICATION_VERSION("java.specification.version", "Java Runtime Environment specification version"),
    JAVA_SPECIFICATION_VENDOR("java.specification.vendor", "Java Runtime Environment specification vendor"),
    JAVA_SPECIFICATION_NAME("java.specification.name", "Java Runtime Environment specification name"),
    JAVA_CLASS_VERSION("java.class.version", "Java class format version number"),
    JAVA_CLASS_PATH("java.class.path", "Java class path"),
    JAVA_LIBRARY_PATH("java.library.path", "List of paths to search when loading libraries"),
    JAVA_IO_TMPDIR("java.io.tmpdir", "Default temp file path"),
    JAVA_COMPILER("java.compiler", "Name of JIT compiler to use"),
    JAVA_EXT_DIRS("java.ext.dirs", "Path of extension directory or directories Deprecated. This property,"
            + " and the mechanism which implements it, may be removed in a future release."),
    OS_NAME("os.name", "Operating system name"),
    OS_ARCH("os.arch", "Operating system architecture"),
    OS_VERSION("os.version", "Operating system version"),
    FILE_SEPARATOR("file.separator", "File separator (/on UNIX)"),
    PATH_SEPARATOR("path.separator", "Path separator (: on UNIX)"),
    LINE_SEPARATOR("line.separator", "Line separator (\\n on UNIX)"),
    USER_NAME("user.name", "User's account name"),
    USER_HOME("user.home", "User's home directory"),
    USER_DIR("user.dir", "User's current working directory");

    /**
     * The key of this system property.
     */
    private final String key;

    /**
     * The description of this system property.
     */
    private final String description;

    SystemPropertyKey(String key, String description) {
        this.key = key;
        this.description = description;
    }

    /**
     * Returns the key of this system property.
     *
     * @return the key of this system property
     */
    public String getKey() {
        return key;
    }

    /**
     * Returns the description of this system property.
     *
     * @return the description of this system property
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the result of invoking {@link System#getProperty(String)} using this key.
     *
     * @return the property of this system property key
     */
    public String getProperty() {
        return System.getProperty(this.key);
    }
}
