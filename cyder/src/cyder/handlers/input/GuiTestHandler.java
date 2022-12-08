package cyder.handlers.input;

import com.google.common.reflect.ClassPath;
import cyder.annotations.GuiTest;
import cyder.annotations.Handle;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.strings.CyderStrings;
import cyder.utils.ReflectionUtil;

import java.lang.reflect.Method;

/**
 * A handler for invoking {@link GuiTest}s.
 */
public class GuiTestHandler extends InputHandler {
    /**
     * Suppress default constructor.
     */
    private GuiTestHandler() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    @Handle
    public static boolean handle() {
        boolean ret = false;

        for (ClassPath.ClassInfo classInfo : ReflectionUtil.getCyderClasses()) {
            Class<?> classer = classInfo.load();

            for (Method method : classer.getMethods()) {
                if (method.isAnnotationPresent(GuiTest.class)) {
                    String trigger = method.getAnnotation(GuiTest.class).value();
                    if (trigger.equalsIgnoreCase(getInputHandler().commandAndArgsToString())) {
                        try {
                            getInputHandler().println("Invoking gui test " + CyderStrings.quote
                                    + method.getName() + CyderStrings.quote);
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
