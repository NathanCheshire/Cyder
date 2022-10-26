package cyder.handlers.input;

import com.google.common.base.Preconditions;
import com.google.common.reflect.ClassPath;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.annotations.CyderTest;
import cyder.annotations.Handle;
import cyder.constants.CyderStrings;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.utils.ReflectionUtil;

import java.lang.reflect.Method;

/**
 * A handler for invoking {@link cyder.annotations.CyderTest}s.
 */
public class TestHandler extends InputHandler {
    /**
     * The name of the default parameter.
     */
    private static final String VALUE = "value";

    /**
     * Suppress default constructor.
     */
    private TestHandler() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Invokes all tests with the default trigger of "test".
     */
    public static void invokeDefaultTests() {
        Class<?> clazz = CyderTest.class;

        Method method = null;
        try {
            method = clazz.getDeclaredMethod(VALUE);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        if (method == null) {
            throw new FatalException("Failed reflection when attempting to find CyderTest's default value");
        }
        String value = (String) method.getDefaultValue();
        invokeTestsWithTrigger(value);
    }

    @Handle
    public static boolean handle() {
        boolean testTriggered = false;

        for (ClassPath.ClassInfo classInfo : ReflectionUtil.CYDER_CLASSES) {
            Class<?> classer = classInfo.load();

            for (Method method : classer.getMethods()) {
                if (method.isAnnotationPresent(CyderTest.class)) {
                    String trigger = method.getAnnotation(CyderTest.class).value();
                    testTriggered = invokeTestsWithTrigger(trigger);
                    if (testTriggered) break;
                }
            }
        }

        return testTriggered;
    }

    /**
     * Invokes any and all {@link CyderTest}s found with the provided trigger.
     *
     * @param trigger the trigger
     * @return whether a test was invoked
     */
    @CanIgnoreReturnValue
    private static boolean invokeTestsWithTrigger(String trigger) {
        Preconditions.checkNotNull(trigger);
        Preconditions.checkArgument(!trigger.isEmpty());

        boolean ret = false;

        for (ClassPath.ClassInfo classInfo : ReflectionUtil.CYDER_CLASSES) {
            Class<?> classer = classInfo.load();

            for (Method method : classer.getMethods()) {
                if (method.isAnnotationPresent(CyderTest.class)) {
                    String testTrigger = method.getAnnotation(CyderTest.class).value();
                    if (trigger.equalsIgnoreCase(testTrigger)) {
                        try {
                            method.invoke(classer);
                            ret = true;
                        } catch (Exception e) {
                            ExceptionHandler.handle(e);
                        }
                    }
                }
            }
        }

        return ret;
    }
}
