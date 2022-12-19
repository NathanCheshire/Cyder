package main.java.cyder.files;

import com.google.common.base.Preconditions;
import main.java.cyder.annotations.CyderTest;
import main.java.cyder.exceptions.FatalException;
import main.java.cyder.handlers.internal.ExceptionHandler;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.DosFileAttributes;

/**
 * DOS attributes as referenced from {@link java.nio.file.attribute.DosFileAttributes}.
 */
public enum DosAttribute {
    /**
     * The is archive dos attribute.
     */
    IS_ARCHIVE("isArchive"),

    /**
     * The is hidden dos attribute.
     */
    IS_HIDDEN("isHidden"),

    /**
     * The is read only dos attribute.
     */
    IS_READ_ONLY("isReadOnly"),

    /**
     * The is system dos attribute.
     */
    IS_SYSTEM("isSystem"),

    /**
     * The is creation time dos attribute.
     */
    CREATION_TIME("creationTime"),

    /**
     * The is directory dos attribute.
     */
    IS_DIRECTORY("isDirectory"),

    /**
     * The is hidden dos attribute.
     */
    IS_OTHER("isOther"),

    /**
     * The is symbolic link dos attribute.
     */
    IS_SYMBOLIC_LINK("isSymbolicLink"),

    /**
     * The last access time dos attribute.
     */
    LAST_ACCESS_TIME("lastAccessTime"),

    /**
     * The last modified time dos attribute.
     */
    LAST_MODIFIED_TIME("lastModifiedTime");

    /**
     * The method name for this DOS attribute.
     */
    private final String methodName;

    DosAttribute(String methodName) {
        this.methodName = methodName;
    }

    /**
     * Returns the method name for this DOS attribute.
     *
     * @return the method name for this DOS attribute
     */
    public String getMethodName() {
        return methodName;
    }

    @CyderTest
    public static void test() {
        File f = new File(".");
        getAttribute(f, DosAttribute.CREATION_TIME);
    }

    /**
     * Returns the DOS attribute for the provided file.
     *
     * @param file      the file
     * @param attribute the DOS attribute
     * @return the DOS attribute for the provided file
     */
    public static String getAttribute(File file, DosAttribute attribute) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());
        Preconditions.checkNotNull(attribute);

        try {
            DosFileAttributes dosAttributes = Files.readAttributes(Paths.get(file.getPath()), DosFileAttributes.class);

            /* An attempt was made to use reflection for this but professor Java said no */

            return switch (attribute) {
                case IS_ARCHIVE -> String.valueOf(dosAttributes.isArchive());
                case IS_HIDDEN -> String.valueOf(dosAttributes.isHidden());
                case IS_READ_ONLY -> String.valueOf(dosAttributes.isReadOnly());
                case IS_SYSTEM -> String.valueOf(dosAttributes.isSystem());
                case CREATION_TIME -> String.valueOf(dosAttributes.creationTime());
                case IS_DIRECTORY -> String.valueOf(dosAttributes.isDirectory());
                case IS_OTHER -> String.valueOf(dosAttributes.isOther());
                case IS_SYMBOLIC_LINK -> String.valueOf(dosAttributes.isSymbolicLink());
                case LAST_ACCESS_TIME -> String.valueOf(dosAttributes.lastAccessTime());
                case LAST_MODIFIED_TIME -> String.valueOf(dosAttributes.lastModifiedTime());
            };
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        throw new FatalException("Could not get DOS attribute " + attribute + " for file: " + file.getAbsolutePath());
    }
}
