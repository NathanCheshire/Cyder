package cyder.widgets;

public interface WidgetBase {
    /**
     * All widgets should have a public static void showGUI() method with no params for the Widget finder
     * inside of ReflectionUtil to locate and invoke. If a widget requires parameters, there should be a neighboring
     * showGUI() method that passes in null/default params to this exceptional showGUI() method case.
     */
    static void showGUI() {}
}
