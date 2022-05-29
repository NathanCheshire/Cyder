package cyder.handlers.input;

import com.google.common.reflect.ClassPath;
import cyder.annotations.Handle;
import cyder.annotations.ManualTest;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.utilities.ReflectionUtil;

import java.lang.reflect.Method;

/**
 * A handle for invoking manual tests.
 */
public class ManualTestHandler extends InputHandler {
    /**
     * Suppress default constructor
     */
    private ManualTestHandler() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    @Handle()
    public static boolean handle() {
        for (ClassPath.ClassInfo classInfo : ReflectionUtil.CYDER_CLASSES) {
            Class<?> classer = classInfo.load();

            for (Method m : classer.getMethods()) {
                if (m.isAnnotationPresent(ManualTest.class)) {
                    String trigger = m.getAnnotation(ManualTest.class).value();
                    if (trigger.equalsIgnoreCase(getInputHandler().commandAndArgsToString())) {
                        try {
                            getInputHandler().println("Invoking manual test " + m.getName());
                            m.invoke(classer);
                            return true;
                        } catch (Exception e) {
                            ExceptionHandler.handle(e);
                        }
                    }
                }
            }
        }

        return false;
    }
}
