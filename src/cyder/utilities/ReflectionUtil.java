package cyder.utilities;

import cyder.consts.CyderStrings;
import cyder.handlers.internal.ErrorHandler;
import cyder.ui.CyderFrame;

import javax.swing.*;
import java.awt.*;
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

    public static String commonCyderUIReflection(Component obj) {
        CyderFrame topFrame = (CyderFrame) SwingUtilities.getWindowAncestor(obj);
        String frameRep = "";

        if (topFrame != null)
            frameRep = topFrame.getTitle();
        else
            frameRep = "No associated frame";

        String superName = obj.getClass().getName();
        int hash = obj.hashCode();

        //remove anything after the $int if superName contains a $
        if (superName.contains("$")) {
            superName = superName.split("\\$")[0];
        }

        String getTextResult = "No getText() method found";

       try {
           for (Method method : obj.getClass().getMethods()) {
               if (method.getName().startsWith("getText") && method.getParameterCount() == 0) {
                   Object locGetText = method.invoke(obj);

                   if (locGetText instanceof String) {
                       String locGetTextString = (String) locGetText;

                       if (locGetTextString != null && locGetTextString.length() > 0
                               && !locGetTextString.equalsIgnoreCase("null")) {
                           getTextResult = locGetTextString;
                           break;
                       }
                   }
               }
           }
       } catch (Exception e) {
           ErrorHandler.handle(e);
       }

        String build = "Component name = [" + superName + "], bounds = [(" + obj.getX() + ", "
                + obj.getY() + ", " + obj.getWidth() + ", " + obj.getHeight() + ")], hash = [" + hash + "], " +
                "parentFrame = [" + frameRep + "], associated text = [" + getTextResult + "]";

        return build;
    }

    //todo fix and finish rippling for label

    //todo cyder icon button to be used for cyder frames and such (at least the cyderframe/console frame buttons on the drag label)

    //todo standards for widget such as base size, implement these, perhaps add a build base widget method and a widget util?
    // after this utilize it for widgets, make sure everything in widgets has a widget annotation with the showGUI method
}
