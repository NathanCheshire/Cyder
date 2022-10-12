package cyder.test;

import cyder.annotations.GuiTest;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.CyderThreadRunner;

/**
 * A class for calling test methods manually.
 */
public final class Test {
    /**
     * The name of the thread to run tests from {@link #Test()} in.
     */
    private static final String TEST_THREAD_NAME = "Test Thread";

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
        CyderThreadRunner.submit(() -> {
            try {

            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, TEST_THREAD_NAME);
    }
}
