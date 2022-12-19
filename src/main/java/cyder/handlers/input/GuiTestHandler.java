package main.java.cyder.handlers.input;

import com.google.common.reflect.ClassPath;
import main.java.cyder.annotations.GuiTest;
import main.java.cyder.annotations.Handle;
import main.java.cyder.exceptions.IllegalMethodException;
import main.java.cyder.handlers.internal.ExceptionHandler;
import main.java.cyder.strings.CyderStrings;
import main.java.cyder.utils.ReflectionUtil;

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
