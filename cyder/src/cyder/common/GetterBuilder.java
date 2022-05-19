package cyder.common;

import cyder.constants.CyderColors;
import cyder.handlers.internal.Logger;

import java.awt.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A modified builder pattern for the getter methods within {@link cyder.utilities.GetterUtil}.
 */
public class GetterBuilder {
    /*
    Required params
     */

    /**
     * The title of the frame.
     */
    private final String title;

    /*
    Optional params
     */

    /**
     * The button text for the submit button for some getter frames.
     */
    private String submitButtonText = "Submit";

    /**
     * The field tooltip to display for getter frames which contain a text field.
     */
    private String fieldTooltip = "Input";

    /**
     * The text field regex to use for getter frames which contain a text field.
     */
    private String fieldRegex;

    /**
     * Te component to set the getter frame relative to.
     */
    private Component relativeTo;

    /**
     * The color of the submit button for most getter frames.
     */
    private Color submitButtonColor = CyderColors.regularRed;

    /**
     * The initial text of the field for getter framds which have a text field.
     */
    private String initialString = "";

    /**
     * The label text for getter frames which use a label.
     */
    private String labelText = "";

    /**
     * The text for confirming an operation.
     */
    private String yesButtonText = "Yes";

    /**
     * the text for denying an operation.
     */
    private String noButtonText = "No";

    /**
     * Whether to disable the component the getter frame was
     * set relative to while the relative frame is open.
     */
    private boolean disableRelativeTo;

    /**
     * The minimum text length of a getter frame title.
     */
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

        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    /**
     * Returns the title of the getter frame.
     *
     * @return the title of the getter frame
     */
    public String getTitle() {
        return title;
    }

    /**
     * Returns the submit button text for getter frames which get field input from the user.
     *
     * @return the submit button text for getter frames which get field input from the user
     */
    public String getSubmitButtonText() {
        return submitButtonText;
    }

    /**
     * Sets the submit button text for getter frames which get field input from the user.
     *
     * @param submitButtonText the submit button text for getter frames which get field input from the user
     */
    public void setSubmitButtonText(String submitButtonText) {
        this.submitButtonText = submitButtonText;
    }

    /**
     * Returns the field tooltip text for getter frames which get field input from the user.
     *
     * @return the field tooltip text for getter frames which get field input from the user
     */
    public String getFieldTooltip() {
        return fieldTooltip;
    }

    /**
     * Sets the field tooltip text for getter frames which get field input from the user.
     *
     * @param fieldTooltip the field tooltip text for getter frames which get field input from the user
     */
    public void setFieldTooltip(String fieldTooltip) {
        this.fieldTooltip = fieldTooltip;
    }

    /**
     * returns the field regex for getter frames which get field input from the user.
     *
     * @return the field regex for getter frames which get field input from the user
     */
    public String getFieldRegex() {
        return fieldRegex;
    }

    /**
     * Sets the field regex for getter frames which get field input from the user.
     *
     * @param fieldRegex the field regex for getter frames which get field input from the user
     */
    public void setFieldRegex(String fieldRegex) {
        this.fieldRegex = fieldRegex;
    }

    /**
     * Returns the relative to component to set the getter frame relative to.
     *
     * @return the relative to component to set the getter frame relative to
     */
    public Component getRelativeTo() {
        return relativeTo;
    }

    /**
     * Sets the relative to component to set the getter frame relative to.
     *
     * @param relativeTo the relative to component to set the getter frame relative to
     */
    public void setRelativeTo(Component relativeTo) {
        this.relativeTo = relativeTo;
    }

    /**
     * Returns the button background color for the submit button for getter frames which get input from a user.
     *
     * @return the button background color for the submit button for getter frames which get input from a user
     */
    public Color getSubmitButtonColor() {
        return submitButtonColor;
    }

    /**
     * Sets the button background color for the submit button for getter frames which get input from a user.
     *
     * @param submitButtonColor the button background color for the
     *                          submit button for getter frames which get input from a user
     */
    public void setSubmitButtonColor(Color submitButtonColor) {
        this.submitButtonColor = submitButtonColor;
    }

    /**
     * Returns the initial field text for getter frames which have an input field.
     *
     * @return the initial field text for getter frames which have an input field
     */
    public String getInitialString() {
        return initialString;
    }

    /**
     * Sets the initial field text for getter frames which have an input field.
     *
     * @param initialString the initial field text for getter frames which have an input field
     */
    public void setInitialString(String initialString) {
        this.initialString = initialString;
    }

    /**
     * Returns the text to display on the button for approving a requested operation.
     *
     * @return the text to display on the button for approving a requested operation
     */
    public String getYesButtonText() {
        return yesButtonText;
    }

    /**
     * Sets the text to display on the button for approving a requested operation.
     *
     * @param yesButtonText the text to display on the button for approving a requested operation
     */
    public void setYesButtonText(String yesButtonText) {
        this.yesButtonText = yesButtonText;
    }

    /**
     * Returns the text to display on the button for denying a requested operation.
     *
     * @return the text to display on the button for denying a requested operation
     */
    public String getNoButtonText() {
        return noButtonText;
    }

    /**
     * Sets the text to display on the button for denying a requested operation.
     *
     * @param noButtonText the text to display on the button for denying a requested operation
     */
    public void setNoButtonText(String noButtonText) {
        this.noButtonText = noButtonText;
    }

    /**
     * Returns the label text for getter frames which have a primary information label.
     *
     * @return the label text for getter frames which have a primary information label
     */
    public String getLabelText() {
        return labelText;
    }

    /**
     * Sets the label text for getter frames which have a primary information label.
     *
     * @param labelText the label text for getter frames which have a primary information label
     */
    public void setLabelText(String labelText) {
        this.labelText = labelText;
    }

    /**
     * Returns whether to disable the relativeTo component while the getter frame is active.
     *
     * @return whether to disable the relativeTo component while the getter frame is active
     */
    public boolean isDisableRelativeTo() {
        return disableRelativeTo;
    }

    /**
     * Sets whether to disable the relativeTo component while the getter frame is active.
     *
     * @param disableRelativeTo whether to disable the relativeTo component while the getter frame is active
     */
    public void setDisableRelativeTo(boolean disableRelativeTo) {
        this.disableRelativeTo = disableRelativeTo;
    }
}
