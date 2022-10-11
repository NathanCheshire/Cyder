package cyder.test;

import cyder.annotations.GuiTest;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;

/**
 * A class for calling test methods manually.
 */
public final class Test {
    /**
     * Suppress default constructor.
     */
    private Test() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * A method to invoke via the "test" keyword.
     */
    @GuiTest
    @SuppressWarnings({"EmptyTryBlock", "RedundantSuppression"})
    public static void test() {
        try {

        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }
}
