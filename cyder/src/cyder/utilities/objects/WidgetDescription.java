package cyder.utilities.objects;

import cyder.constants.CyderStrings;
import cyder.enums.LoggerTag;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.Logger;

public class WidgetDescription {
    private final String name;
    private final String description;
    private final String[] triggers;

    /**
     * Suppress instantiation without all params.
     */
    private WidgetDescription() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    public WidgetDescription(String name, String description, String[] triggers) {
        this.name = name;
        this.description = description;
        this.triggers = triggers;

        Logger.log(LoggerTag.OBJECT_CREATION, this);
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
