package cyder.utilities;

import cyder.consts.CyderStrings;
import cyder.handlers.internal.ErrorHandler;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import java.lang.reflect.Method;

public class ReflectionUtil {
    private ReflectionUtil() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    //todo utilize
    public static String toStringReflection(Object obj) {
        StringBuilder ret = new StringBuilder();

        ret.append(obj.getClass().getName());
        ret.append("(");

        for (Method m : obj.getClass().getMethods()) {
            if (m.getName().startsWith("get") && m.getParameterTypes().length == 0) {
                try {
                    ret.append(m.getName());
                    ret.append(" = ");
                    ret.append(m.invoke(obj));
                    ret.append(", ");
                } catch (Exception e) {
                    ErrorHandler.handle(e);
                }
            }
        }

        String retString = ret.toString();
        retString = retString.substring(0, retString.length() - 3) + ")";
        return retString;
    }

    public static String toStringReflectionApache(Object obj) {
        return ReflectionToStringBuilder.toString(obj);
    }
}
