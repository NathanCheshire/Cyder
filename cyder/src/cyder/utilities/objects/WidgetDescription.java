package cyder.utilities.objects;

public class WidgetDescription {
    private final String name;
    private final String description;
    private final String[] triggers;

    public WidgetDescription(String name, String description, String[] triggers) {
        this.name = name;
        this.description = description;
        this.triggers = triggers;
    }

    public final String getName() {
        return name;
    }

    public final String getDescription() {
        return description;
    }

    public final String[] getTriggers() {
        return triggers;
    }
}
