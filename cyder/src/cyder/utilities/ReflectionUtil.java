package cyder.utilities;

import com.google.gson.Gson;
import cyder.annotations.Widget;
import cyder.constants.CyderStrings;
import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.CyderThreadFactory;
import cyder.ui.ConsoleFrame;
import cyder.ui.CyderFrame;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
                    ExceptionHandler.handle(e);
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
           ExceptionHandler.handle(e);
        }

        String build = "Component name = [" + superName + "], bounds = [(" + obj.getX() + ", "
                + obj.getY() + ", " + obj.getWidth() + ", " + obj.getHeight() + ")], hash = [" + hash + "], " +
                "parentFrame = [" + frameRep + "], associated text = [" + getTextResult + "], tooltip text = [" +
                getTooltipResult + "], title = [" + getTitleResult + "]";

        return build;
    }

    /**
     * Finds all classes annotated with the Widget annotation within the widgets package
     */
    public static ArrayList<WidgetInformation> findWidgets() {
        ArrayList<WidgetInformation> ret = new ArrayList<>();

        for (String widget : widgets.getPack()) {
            for (Class classer : findAllClassesUsingClassLoader(widget)) {
                for (Method m : classer.getMethods()) {
                    if (m.isAnnotationPresent(Widget.class)) {
                        String description = m.getAnnotation(Widget.class).description();
                        String[] widgetTriggers = m.getAnnotation(Widget.class).trigger();
                        ret.add(new WidgetInformation(widgetTriggers, description));
                    }
                }
            }
        }

        return ret;
    }

    /**
     * Opens a widget with the same trigger as the one provided.
     *
     * @param trigger the trigger for the widget to open
     * @return whether or not a widget was opened
     */
    public static boolean openWidget(String trigger) {
        for (String widget : widgets.getPack()) {
            for (Class classer : findAllClassesUsingClassLoader(widget)) {
                for (Method m : classer.getMethods()) {
                    if (m.isAnnotationPresent(Widget.class)) {
                        String[] widgetTriggers = m.getAnnotation(Widget.class).trigger();

                        for (String widgetTrigger : widgetTriggers) {
                            if (widgetTrigger.equalsIgnoreCase(trigger)) {
                                ConsoleFrame.getConsoleFrame().getInputHandler().println("Opening widget: "
                                        + classer.getName());
                                try {
                                    if (m.getParameterCount() == 0) {
                                        m.invoke(classer);
                                        return true;
                                    } else throw new IllegalStateException("Found widget showGUI()" +
                                            " method with parameters: " + m.getName() + ", class: " + classer);
                                } catch (Exception e) {
                                    ExceptionHandler.handle(e);
                                }
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    //todo replace with Guava so that package name passing isn't necessary
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
            return Class.forName(packageName + "." + className.substring(0, className.lastIndexOf('.')));
        } catch (ClassNotFoundException e) {
            ExceptionHandler.handle(e);
        }

        return null;
    }

    /**
     * Executor service used to find a similar command utilizing commandFinder.py
     */
    private static ExecutorService executor = Executors.newSingleThreadExecutor(
            new CyderThreadFactory("Similar Command Finder"));

    /**
     * Finds the most similar command to the unrecognized one the user provided.
     *
     * @param command the command to find a similar one to
     * @return the most similar command to the one provided
     */
    public static Future<Optional<String>> getSimilarCommand(String command) {
        return executor.submit(() -> {
            Optional<String> ret = Optional.empty();

            try {
                Runtime rt = Runtime.getRuntime();
                String[] commands = {"python",
                        OSUtil.buildPath("cyder","src","cyder","scripts","commandFinder.py"), command};
                Process proc = rt.exec(commands);

                BufferedReader stdInput = new BufferedReader(new
                        InputStreamReader(proc.getInputStream()));

                String s = null;
                while ((s = stdInput.readLine()) != null) {
                    ret = Optional.of(s);
                    break;
                }

            } catch (Exception e) {
                ExceptionHandler.silentHandle(e);
            }

            return ret;
        });
    }

    public static WidgetHolder widgets = null;

    static {
        loadWidgets();
    }

    public static void loadWidgets() {
        Gson gson = new Gson();

        try (Reader reader = new FileReader("static/json/widgetpackages.json")) {
            WidgetHolder ret = gson.fromJson(reader, WidgetHolder.class);

            //if successful set as our suggestions object
            widgets = ret;
        } catch (IOException e) {
            ExceptionHandler.handle(e);
        }
    }

    //inner classes ---------------------
    public static class WidgetHolder {
        private ArrayList<String> pack;

        public WidgetHolder(ArrayList<String> pack) {
            this.pack = pack;
        }

        public ArrayList<String> getPack() {
            return pack;
        }

        public void setPack(ArrayList<String> pack) {
            this.pack = pack;
        }
    }

    public static class WidgetInformation {
        private String[] triggers;
        private String description;

        public WidgetInformation(String[] triggers, String description) {
            this.triggers = triggers;
            this.description = description;
        }

        public String[] getTriggers() {
            return triggers;
        }

        public void setTriggers(String[] triggers) {
            this.triggers = triggers;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
