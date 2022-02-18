package cyder.widgets;

/**
 * Abstract class to force widgets to override certain methods.
 */
public interface WidgetBase {
    /**
     * All widgets should have a "public static void showGUI()" method with no params for the Widget finder
     * inside ReflectionUtil to locate and invoke. If a widget requires parameters, there should be a neighboring
     * showGUI() method with the required parameters. Then inside the default showGUI() method, pass default
     * parameters to the showGUI() method which requires them.
     */
    static void showGUI() {
        throw new UnsupportedOperationException("Widget's showGUI() method not yet implemented.");
    }
}
