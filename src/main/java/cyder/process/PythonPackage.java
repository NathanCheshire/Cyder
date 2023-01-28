package cyder.process;

import cyder.snakes.PythonUtil;

import java.util.Optional;
import java.util.concurrent.Future;

/**
 * Python packages utilized by Cyder.
 */
public enum PythonPackage {
    /**
     * Pillow package for image utilities.
     */
    PILLOW("Pillow");

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
        PythonUtil.installPipDependency(this);
    }

    /**
     * Returns whether the python package is installed.
     *
     * @return whether the python package is installed
     */
    public Future<Boolean> isInstalled() {
        return PythonUtil.isPipDependencyPresent(this);
    }

    /**
     * Returns the installed version of the python package.
     *
     * @return the installed version of the python package
     */
    public Future<Optional<String>> getInstalledVersion() {
        return PythonUtil.getPipDependencyVersion(this);
    }
}
