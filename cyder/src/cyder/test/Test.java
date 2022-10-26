package cyder.test;

import cyder.annotations.GuiTest;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.CyderThreadRunner;
import cyder.youtube.YoutubeUtil;

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
                // todo log the url we are downloading when we download a youtube video/audio

                // todo Cyder test method to replace default test?

                YoutubeUtil.downloadThumbnail("https://www.youtube.com/watch?v=9S4RCAbebxA");
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, TEST_THREAD_NAME);
    }
}
