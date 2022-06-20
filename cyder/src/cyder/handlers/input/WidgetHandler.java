package cyder.handlers.input;

import com.google.common.reflect.ClassPath;
import cyder.annotations.Handle;
import cyder.annotations.Widget;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.ConsoleFrame;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;
import cyder.utils.ReflectionUtil;

import java.lang.reflect.Method;

/**
 * A handler for opening classes tagged as a widget via the @Widget annotation.
 */
public class WidgetHandler extends InputHandler {
    /**
     * Suppress default constructor.
     */
    private WidgetHandler() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    @Handle()
    public static boolean handle() {
        for (ClassPath.ClassInfo classInfo : ReflectionUtil.CYDER_CLASSES) {
            Class<?> clazz = classInfo.load();

            for (Method m : clazz.getMethods()) {
                if (m.isAnnotationPresent(Widget.class)) {
                    String[] widgetTriggers = m.getAnnotation(Widget.class).triggers();

                    for (String widgetTrigger : widgetTriggers) {
                        if (widgetTrigger.equalsIgnoreCase(getInputHandler().commandAndArgsToString())) {
                            String shortWidgetName = ReflectionUtil.getBottomLevelClass(clazz);
                            ConsoleFrame.INSTANCE.getInputHandler().println("Opening widget: " + shortWidgetName);
                            try {
                                if (m.getParameterCount() == 0) {
                                    m.invoke(clazz);

                                    Logger.log(Logger.Tag.WIDGET_OPENED,
                                            shortWidgetName + ", trigger = " +
                                                    getInputHandler().commandAndArgsToString());

                                    return true;
                                } else
                                    throw new IllegalStateException("Found widget showGui()" +
                                            " annotated method with parameters: " + m.getName() + ", class: " + clazz);
                            } catch (Exception e) {
                                ExceptionHandler.handle(e);
                            }
                        }
                    }
                }
            }
        }

        return false;
    }
}
