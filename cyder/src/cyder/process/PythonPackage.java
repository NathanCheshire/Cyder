package cyder.process;

import java.util.Optional;
import java.util.concurrent.Future;

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

    /**
     * Returns whether the python package is installed.
     *
     * @return whether the python package is installed
     */
    public Future<Boolean> isInstalled() {
        return ProcessUtil.isPipDependencyPresent(packageName);
    }

    /**
     * Returns the installed version of the python package.
     *
     * @return the installed version of the python package
     */
    public Future<Optional<String>> getInstalledVersion() {
        return ProcessUtil.getPipDependencyVersion(packageName);
    }
}
