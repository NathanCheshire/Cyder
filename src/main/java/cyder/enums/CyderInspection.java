package main.java.cyder.enums;

/**
 * Inspections Cyder performs at runtime, specifically, at startup.
 */
public enum CyderInspection {
    /**
     * Inspections related to methods annotated by {@link main.java.cyder.annotations.Vanilla}.
     */
    VanillaInspection,

    /**
     * Inspections related to unit tests, {@link main.java.cyder.annotations.GuiTest}s, etc.
     */
    TestInspection,

    /**
     * Inspections related to methods annotated by {@link main.java.cyder.annotations.Widget}.
     */
    WidgetInspection,

    /**
     * Inspects related to methods annotated by {@link main.java.cyder.annotations.Handle}.
     */
    HandleInspection,
}
