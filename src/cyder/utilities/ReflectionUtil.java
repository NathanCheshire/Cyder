package cyder.utilities;

import cyder.consts.CyderStrings;
import cyder.handlers.internal.ErrorHandler;

import java.lang.reflect.Method;

public class ReflectionUtil {
    private ReflectionUtil() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

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
        //remove last two chars (space and ,) and add the closing parenthesis
        retString = retString.substring(0, retString.length() - 3) + ")";
        return retString;
    }

    /**
     * A common method utilized by near all top-level Cyder classes as the overridden logic for their toString() methods
     * @param obj the obj to return a String representation for
     * @return the String representation for the provided object detailing the classname, hashcode, and reflected data
     */
    public static String commonCyderToString(Object obj) {
        String superName = obj.getClass().getName();
        int hash = obj.hashCode();

        String reflectedFields = ReflectionUtil.toStringReflection(obj);

        if (reflectedFields == null || reflectedFields.length() == 0)
            reflectedFields = "No reflection data acquied";

        //remove anything after the $int if superName contains a $
        if (superName.contains("$")) {
            superName = superName.split("\\$")[0];
        }

        return superName + ",hash = " + hash + ", reflection data = " + reflectedFields;
    }

    //todo method for ui components to log them and their parent and their name when clicked
    //todo fix and finish rippling for label
    //todo make sure widgets are annotated properly and utilize reflection to find them and inform the user of available widgets
    //todo add a description to the @Widget annotation
    //todo standards for widget such as base size
}
