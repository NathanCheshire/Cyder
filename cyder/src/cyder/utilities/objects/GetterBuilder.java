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
    private final String text;

    //optional params
    private String submitButtonText = "Submit";
    private String fieldTooltip = "";
    private Component relativeTo;
    private Color submitButtonColor = CyderColors.regularRed;
    private String initialString = "";

    /**
     * Constructs a new GetterBuilder.
     *
     * @param text the frame title/the text for confirmations.
     */
    public GetterBuilder(String text) {
        checkNotNull(text, "title/text is null");
        checkArgument(text.length() > 2, "Text length is less than three");

        this.text = text;
    }

    public String getText() {
        return text;
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
