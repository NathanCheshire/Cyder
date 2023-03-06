package cyder.strings;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.ui.frame.CyderFrame;
import cyder.utils.ReflectionUtil;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Method;

import static cyder.strings.CyderStrings.quote;

/**
 * Utilities for casting things to strings.
 */
public final class ToStringUtil {
    /**
     * Special methods which should be attempted to be found
     * and invoked if found when reflecting on a {@link Component}.
     */
    private static final ImmutableList<GetterMethodResult> getterMethods = ImmutableList.of(
            new GetterMethodResult("getText"),
            new GetterMethodResult("getTooltipText"),
            new GetterMethodResult("getTitle")
    );

    /**
     * Suppress default constructor.
     */
    private ToStringUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * A class used for reflection to find special methods within an object.
     */
    private static class GetterMethodResult {
        /**
         * The prefix for the method name to start with.
         */
        private final String startsWith;

        /**
         * The result of invoking the getter method.
         */
        private String methodResult;

        /**
         * Constructs a new getter method result.
         *
         * @param startsWith what the method should start with
         */
        public GetterMethodResult(String startsWith) {
            this.startsWith = Preconditions.checkNotNull(startsWith);
        }

        /**
         * Returns what the method should start with.
         *
         * @return what the method should start with
         */
        public String getStartsWith() {
            return startsWith;
        }

        /**
         * returns the method result if set.
         *
         * @return the method result if set
         */
        public String getMethodResult() {
            return methodResult;
        }

        /**
         * Sets the method result.
         *
         * @param methodResult the method result
         */
        public void setMethodResult(String methodResult) {
            Preconditions.checkNotNull(methodResult);

            this.methodResult = methodResult;
        }
    }

    /**
     * Returns a string representation of the provided component's top level frame parent if found.
     *
     * @param component the component
     * @return a string representation of the provided component's top level frame parent
     */
    public static String getComponentParentFrameRepresentation(Component component) {
        Preconditions.checkNotNull(component);

        CyderFrame topFrame = (CyderFrame) SwingUtilities.getWindowAncestor(component);
        if (topFrame != null) return topFrame.getTitle();

        if (component instanceof CyderFrame) {
            return "Component itself is a CyderFrame";
        } else {
            return "No parent frame found";
        }
    }

    /**
     * A string representation of {@link Component}s used by most Cyder ui classes for logging.
     *
     * @param component the component
     * @return a string representation of the provided component
     */
    public static String commonUiComponentToString(Component component) {
        Preconditions.checkNotNull(component);

        String parentFrame = getComponentParentFrameRepresentation(component);

        try {
            for (Method method : component.getClass().getMethods()) {
                for (GetterMethodResult getterMethod : getterMethods) {
                    if (method.getName().startsWith(getterMethod.startsWith) && method.getParameterCount() == 0) {
                        Object localInvokeResult = method.invoke(component);

                        if (localInvokeResult instanceof String localInvokeResultString) {
                            if (!localInvokeResultString.isEmpty()
                                    && !StringUtil.isNullOrEmpty(localInvokeResultString)) {
                                getterMethod.setMethodResult(localInvokeResultString);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        StringBuilder ret = new StringBuilder();

        ret.append("Component: ").append(ReflectionUtil.getBottomLevelClass(component.getClass()))
                .append(", ").append("hash: ").append(component.hashCode())
                .append(", ").append("bounds: (").append(component.getX())
                .append(", ").append(component.getY())
                .append(", ").append(component.getWidth())
                .append(", ").append(component.getHeight())
                .append("), ").append("parent frame: ").append(parentFrame);

        getterMethods.forEach(specialMethod -> {
            String result = specialMethod.getMethodResult();
            if (result != null && !result.isEmpty()) {
                ret.append(", ").append(specialMethod.getStartsWith())
                        .append(": ").append(quote).append(result).append(quote);
            }
        });

        return ret.toString();
    }

    /**
     * A common method utilized by near all top-level Cyder classes
     * as the overridden logic for their {@link Object#toString()} implementation.
     *
     * @param object the object
     * @return the String representation for the provided object
     * detailing the class name, hashcode, and reflected data detailed by the {@link GetterMethodResult}s
     */
    public static String commonCyderToString(Object object) {
        Preconditions.checkNotNull(object);

        return ReflectionUtil.getBottomLevelClass(object.getClass())
                + ", hash: " + object.hashCode()
                + ", reflection data: " + ReflectionUtil.buildGetterString(object);
    }
}
