package cyder.process;

/**
 * Python packages utilized by Cyder.
 */
public enum PythonPackage {
    /**
     * Pillow package for image utilities.
     */
    PILLOW("Pillow"),

    /**
     * Mutagen package for audio metadata.
     */
    MUTAGEN("Mutagen");

    /**
     * The package name for this python package.
     */
    private final String packageName;

    PythonPackage(String packageName) {
        this.packageName = packageName;
    }

    /**
     * Returns the package name for this dependency.
     *
     * @return the package name for this dependency
     */
    public String getPackageName() {
        return packageName;
    }

    /**
     * Installs this python package using pip if not already present.
     */
    public void install() {
        ProcessUtil.installPipDependency(packageName);
    }
}
