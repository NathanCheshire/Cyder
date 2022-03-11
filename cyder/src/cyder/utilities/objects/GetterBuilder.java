package cyder.utilities.objects;

import cyder.constants.CyderColors;

import java.awt.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A builder pattern for the getter methods within {@link cyder.utilities.GetterUtil}.
 */
public class GetterBuilder {
    //required params
    private final String title;

    //optional params
    private String submitButtonText = "Submit";

    private String fieldTooltip = "Input";
    private String fieldRegex;

    private Component relativeTo;
    private Color submitButtonColor = CyderColors.regularRed;
    private String initialString = "";

    /**
     * Constructs a new GetterBuilder.
     *
     * @param title the frame title/the text for confirmations.
     */
    public GetterBuilder(String title) {
        checkNotNull(title, "title is null");
        checkArgument(title.length() > 2, "Title length is less than three");

        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public String getSubmitButtonText() {
        return submitButtonText;
    }

    public void setSubmitButtonText(String submitButtonText) {
        this.submitButtonText = submitButtonText;
    }

    public String getFieldTooltip() {
        return fieldTooltip;
    }

    public void setFieldTooltip(String fieldTooltip) {
        this.fieldTooltip = fieldTooltip;
    }

    public String getFieldRegex() {
        return fieldRegex;
    }

    public void setFieldRegex(String fieldRegex) {
        this.fieldRegex = fieldRegex;
    }

    public Component getRelativeTo() {
        return relativeTo;
    }

    public void setRelativeTo(Component relativeTo) {
        this.relativeTo = relativeTo;
    }

    public Color getSubmitButtonColor() {
        return submitButtonColor;
    }

    public void setSubmitButtonColor(Color submitButtonColor) {
        this.submitButtonColor = submitButtonColor;
    }

    public String getInitialString() {
        return initialString;
    }

    public void setInitialString(String initialString) {
        this.initialString = initialString;
    }
}
