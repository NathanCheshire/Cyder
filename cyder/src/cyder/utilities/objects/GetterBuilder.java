package cyder.utilities.objects;

import cyder.constants.CyderColors;
import cyder.enums.LoggerTag;
import cyder.handlers.internal.Logger;

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
    private String labelText = "";

    private String yesButtonText = "Yes";
    private String noButtonText = "No";

    private boolean disableRelativeTo;

    public static final int MINIMUM_TITLE_LENGTH = 3;

    /**
     * Constructs a new GetterBuilder.
     *
     * @param title the frame title/the text for confirmations.
     */
    public GetterBuilder(String title) {
        checkNotNull(title, "title is null");
        checkArgument(title.length() >= MINIMUM_TITLE_LENGTH,
                "Title length is less than " + MINIMUM_TITLE_LENGTH);

        this.title = title;

        Logger.log(LoggerTag.OBJECT_CREATION, this);
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

    public String getYesButtonText() {
        return yesButtonText;
    }

    public void setYesButtonText(String yesButtonText) {
        this.yesButtonText = yesButtonText;
    }

    public String getNoButtonText() {
        return noButtonText;
    }

    public void setNoButtonText(String noButtonText) {
        this.noButtonText = noButtonText;
    }

    public String getLabelText() {
        return labelText;
    }

    public void setLabelText(String labelText) {
        this.labelText = labelText;
    }

    public boolean isDisableRelativeTo() {
        return disableRelativeTo;
    }

    public void setDisableRelativeTo(boolean disableRelativeTo) {
        this.disableRelativeTo = disableRelativeTo;
    }
}
