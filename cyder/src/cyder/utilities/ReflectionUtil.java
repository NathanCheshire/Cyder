package cyder.utilities;

import cyder.annotations.Widget;
import cyder.consts.CyderStrings;
import cyder.handlers.internal.ErrorHandler;
import cyder.ui.CyderFrame;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Collectors;

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

        if (topFrame != null) {
            frameRep = topFrame.getTitle();
        } else {
            if (obj instanceof CyderFrame) {
                frameRep = "Object itself is the top level frame";
            } else {
                frameRep = "No associated frame";
            }
        }

        String superName = obj.getClass().getName();
        int hash = obj.hashCode();

        //remove anything after the $int if superName contains a $
        if (superName.contains("$")) {
            superName = superName.split("\\$")[0];
        }

        String getTitleResult = "No getTitle() method found";
        String getTextResult = "No getText() method found";
        String getTooltipResult = "No getTooltipText() method found";

        try {
           for (Method method : obj.getClass().getMethods()) {
               if (method.getName().startsWith("getText") && method.getParameterCount() == 0) {
                   Object locGetText = method.invoke(obj);

                   if (locGetText instanceof String) {
                       String locGetTextString = (String) locGetText;

                       if (locGetTextString != null && locGetTextString.length() > 0
                               && !locGetTextString.equalsIgnoreCase("null")) {
                           getTextResult = locGetTextString;
                       }
                   }
               } else if (method.getName().startsWith("getTooltipText") && method.getParameterCount() == 0) {
                   Object locGetTooltipText = method.invoke(obj);

                   if (locGetTooltipText instanceof String) {
                       String locGetTooltipTextString = (String) locGetTooltipText;

                       if (locGetTooltipTextString != null && locGetTooltipTextString.length() > 0
                               && !locGetTooltipTextString.equalsIgnoreCase("null")) {
                           getTooltipResult = locGetTooltipTextString;
                       }
                   }
               } else if (method.getName().startsWith("getTitle") && method.getParameterCount() == 0) {
                   Object locGetTitle = method.invoke(obj);

                   if (locGetTitle instanceof String) {
                       String locGetTitleString = (String) locGetTitle;

                       if (locGetTitleString != null && locGetTitleString.length() > 0
                               && !locGetTitleString.equalsIgnoreCase("null")) {
                           getTitleResult = locGetTitleString;
                       }
                   }
               }
           }
        } catch (Exception e) {
           ErrorHandler.handle(e);
        }

        String build = "Component name = [" + superName + "], bounds = [(" + obj.getX() + ", "
                + obj.getY() + ", " + obj.getWidth() + ", " + obj.getHeight() + ")], hash = [" + hash + "], " +
                "parentFrame = [" + frameRep + "], associated text = [" + getTextResult + "], tooltip text = [" +
                getTooltipResult + "], title = [" + getTitleResult + "]";

        return build;
    }

    public static void findWidgets(String providedName) {
        for (Class classer : findAllClassesUsingClassLoader(providedName)) {
            for (Method m : classer.getMethods()) {
                if (m.isAnnotationPresent(Widget.class)) {
                    String desc = m.getAnnotation(Widget.class).description();
                    String trigger = m.getAnnotation(Widget.class).description();
                    System.out.println(trigger + "," + desc);
                }
            }
        }
    }

    private static Set<Class> findAllClassesUsingClassLoader(String packageName) {
        InputStream stream = ClassLoader.getSystemClassLoader()
                .getResourceAsStream(packageName.replaceAll("[.]", "/"));
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        return reader.lines()
                .filter(line -> line.endsWith(".class"))
                .map(line -> getClass(line, packageName))
                .collect(Collectors.toSet());
    }

    private static Class getClass(String className, String packageName) {
        try {
            return Class.forName(packageName + "."
                    + className.substring(0, className.lastIndexOf('.')));
        } catch (ClassNotFoundException e) {
            // handle the exception
        }
        return null;
    }
}
