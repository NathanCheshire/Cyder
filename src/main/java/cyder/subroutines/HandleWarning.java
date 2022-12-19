package main.java.cyder.subroutines;

import main.java.cyder.annotations.Handle;

/**
 * The possible warnings for invalid {@link Handle}s.
 */
enum HandleWarning {
    CONTAINS_HANDLE("Found class which does not extend InputHandler with @Handle annotation"),
    MORE_THAN_ONE_HANDLE("Found class which contains more than one method annotated with @Handle"),
    MISSING_TRIGGER("Primary handle class found to be missing triggers"),
    FINAL_HANDLER_HAS_TRIGGERS("Final handle class found to contain triggers"),
    HANDLER_NOT_USED("Handle class not contained in primary or final handlers"),
    PRIMARY_AND_FINAL("Handle class found to be contained in both primary and final lists"),
    NOT_PUBLIC_STATIC_BOOLEAN("Method annotated with @Handle found to not be public static boolean"),
    EMPTY_TRIGGER("Handle annotation found to contain empty triggers"),
    DUPLICATE_TRIGGER("Found duplicate trigger, trigger");

    /**
     * The log prefix for this handle warning.
     */
    private final String logPrefix;

    HandleWarning(String logPrefix) {
        this.logPrefix = logPrefix;
    }

    /**
     * Returns the log prefix for this handle warning.
     *
     * @return the log prefix for this handle warning
     */
    public String getLogPrefix() {
        return this.logPrefix;
    }
}
