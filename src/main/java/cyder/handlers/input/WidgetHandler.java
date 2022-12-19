package main.java.cyder.handlers.input;

import com.google.common.reflect.ClassPath;
import main.java.cyder.annotations.Handle;
import main.java.cyder.annotations.Widget;
import main.java.cyder.console.Console;
import main.java.cyder.constants.CyderRegexPatterns;
import main.java.cyder.exceptions.IllegalMethodException;
import main.java.cyder.handlers.internal.ExceptionHandler;
import main.java.cyder.logging.LogTag;
import main.java.cyder.logging.Logger;
import main.java.cyder.strings.CyderStrings;
import main.java.cyder.utils.ReflectionUtil;

import java.lang.reflect.Method;

/**
 * A handler for opening classes tagged as a widget via the @Widget annotation.
 */
public class WidgetHandler extends InputHandler {
    /**
     * Suppress default constructor.
     */
    private WidgetHandler() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    @Handle
    public static boolean handle() {
        for (ClassPath.ClassInfo classInfo : ReflectionUtil.getCyderClasses()) {
            Class<?> clazz = classInfo.load();

            for (Method m : clazz.getMethods()) {
                if (m.isAnnotationPresent(Widget.class)) {
                    String[] widgetTriggers = m.getAnnotation(Widget.class).triggers();

                    for (String widgetTrigger : widgetTriggers) {
                        widgetTrigger = widgetTrigger.replaceAll(CyderRegexPatterns.whiteSpaceRegex, "");
                        String userInput = getInputHandler().commandAndArgsToString()
                                .replaceAll(CyderRegexPatterns.whiteSpaceRegex, "");

                        if (widgetTrigger.equalsIgnoreCase(userInput)) {
                            String shortWidgetName = ReflectionUtil.getBottomLevelClass(clazz);
                            Console.INSTANCE.getInputHandler().println("Opening widget: " + shortWidgetName);
                            try {
                                if (m.getParameterCount() == 0) {
                                    m.invoke(clazz);

                                    Logger.log(LogTag.WIDGET_OPENED,
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
