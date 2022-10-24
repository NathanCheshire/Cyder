package cyder.getter;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.ui.frame.CyderFrame;

import java.awt.*;
import java.util.ArrayList;

/**
 * A builder for a get input getter method.
 */
public final class GetInputBuilder extends GetBuilder {
    /**
     * The title of the frame.
     */
    private final String frameTitle;

    /**
     * The label text.
     */
    private final String labelText;

    /**
     * The label font.
     */
    private Font labelFont = CyderFonts.DEFAULT_FONT;

    /**
     * The label color.
     */
    private Color labelColor = CyderColors.navy;

    /**
     * The submit button text.
     */
    private String submitButtonText = "Submit";

    /**
     * The submit button font.
     */
    private Font submitButtonFont = CyderFonts.SEGOE_20;

    /**
     * The submit button color
     */
    private Color submitButtonColor = CyderColors.regularRed;

    /**
     * The initial field text.
     */
    private String initialFieldText;

    /**
     * The field hint text.
     */
    private String fieldHintText;

    /**
     * The regex matcher for the field.
     */
    private String fieldRegex;

    /**
     * The field font.
     */
    private Font fieldFont = CyderFonts.SEGOE_20;

    /**
     * The field foreground.
     */
    private Color fieldForeground = CyderColors.navy;

    /**
     * Whether to disable the relative to component while the get frame is active.
     */
    private boolean disableRelativeTo;

    /**
     * The relative to frame.
     */
    private CyderFrame relativeTo;

    /**
     * The actions to invoke when the dialog is disposed.
     */
    private final ArrayList<Runnable> onDialogDisposalRunnables = new ArrayList<>();

    /**
     * Constructs a new get input builder.
     *
     * @param frameTitle the title of the get frame
     * @param labelText  the text of the prompt label
     */
    public GetInputBuilder(String frameTitle, String labelText) {
        Preconditions.checkNotNull(frameTitle);
        Preconditions.checkNotNull(labelText);
        Preconditions.checkArgument(!frameTitle.isEmpty());
        Preconditions.checkArgument(!labelText.isEmpty());

        this.frameTitle = frameTitle;
        this.labelText = labelText;
    }

    /**
     * Returns the frame title.
     *
     * @return the frame title
     */
    @Override
    public String getFrameTitle() {
        return frameTitle;
    }

    /**
     * Returns the label text.
     *
     * @return the label text
     */
    public String getLabelText() {
        return labelText;
    }

    /**
     * Returns the label font.
     *
     * @return the label font
     */
    public Font getLabelFont() {
        return labelFont;
    }

    /**
     * Sets the label font.
     *
     * @param labelFont the label font
     * @return this builder
     */
    @CanIgnoreReturnValue
    public GetInputBuilder setLabelFont(Font labelFont) {
        Preconditions.checkNotNull(labelFont);

        this.labelFont = labelFont;
        return this;
    }

    /**
     * Returns the label color.
     *
     * @return the label color
     */
    public Color getLabelColor() {
        return labelColor;
    }

    /**
     * Sets the label color.
     *
     * @param labelColor the label color
     * @return this builder
     */
    @CanIgnoreReturnValue
    public GetInputBuilder setLabelColor(Color labelColor) {
        Preconditions.checkNotNull(labelColor);

        this.labelColor = labelColor;
        return this;
    }

    /**
     * Returns the submit button text.
     *
     * @return the submit button text
     */
    public String getSubmitButtonText() {
        return submitButtonText;
    }

    /**
     * Sets the submit button text.
     *
     * @param submitButtonText the submit button text
     * @return this builder
     */
    @CanIgnoreReturnValue
    public GetInputBuilder setSubmitButtonText(String submitButtonText) {
        Preconditions.checkNotNull(submitButtonText);
        Preconditions.checkArgument(!submitButtonText.isEmpty());

        this.submitButtonText = submitButtonText;
        return this;
    }

    /**
     * Returns the submit button font.
     *
     * @return the submit button font
     */
    public Font getSubmitButtonFont() {
        return submitButtonFont;
    }

    /**
     * Sets the submit button font.
     *
     * @param submitButtonFont the submit button font
     * @return this builder
     */
    @CanIgnoreReturnValue
    public GetInputBuilder setSubmitButtonFont(Font submitButtonFont) {
        Preconditions.checkNotNull(submitButtonFont);

        this.submitButtonFont = submitButtonFont;
        return this;
    }

    /**
     * Returns the submit button color.
     *
     * @return the submit button color
     */
    public Color getSubmitButtonColor() {
        return submitButtonColor;
    }

    /**
     * Sets the submit button color.
     *
     * @param submitButtonColor the submit button color
     * @return this builder
     */
    @CanIgnoreReturnValue
    public GetInputBuilder setSubmitButtonColor(Color submitButtonColor) {
        Preconditions.checkNotNull(submitButtonColor);

        this.submitButtonColor = submitButtonColor;
        return this;
    }

    /**
     * Returns the initial field text.
     *
     * @return the initial field text
     */
    public String getInitialFieldText() {
        return initialFieldText;
    }

    /**
     * Sets the initial field text.
     *
     * @param initialFieldText the initial field text
     * @return this builder
     */
    @CanIgnoreReturnValue
    public GetInputBuilder setInitialFieldText(String initialFieldText) {
        Preconditions.checkNotNull(initialFieldText);
        Preconditions.checkArgument(!initialFieldText.isEmpty());

        this.initialFieldText = initialFieldText;
        return this;
    }

    /**
     * Returns the field hint text.
     *
     * @return the field hint text
     */
    public String getFieldHintText() {
        return fieldHintText;
    }

    /**
     * Sets the field hint text.
     *
     * @param fieldHintText the field hint text
     * @return this builder
     */
    @CanIgnoreReturnValue
    public GetInputBuilder setFieldHintText(String fieldHintText) {
        Preconditions.checkNotNull(fieldHintText);
        Preconditions.checkArgument(!fieldHintText.isEmpty());

        this.fieldHintText = fieldHintText;
        return this;
    }

    /**
     * Returns the field regex.
     *
     * @return the field regex
     */
    public String getFieldRegex() {
        return fieldRegex;
    }

    /**
     * Sets the field regex.
     *
     * @param fieldRegex the field regex
     * @return this builder
     */
    @CanIgnoreReturnValue
    public GetInputBuilder setFieldRegex(String fieldRegex) {
        Preconditions.checkNotNull(fieldRegex);
        Preconditions.checkArgument(!fieldRegex.isEmpty());

        this.fieldRegex = fieldRegex;
        return this;
    }

    /**
     * Returns the field font.
     *
     * @return the field font
     */
    public Font getFieldFont() {
        return fieldFont;
    }

    /**
     * Sets the field font.
     *
     * @param fieldFont the field font
     * @return this builder
     */
    @CanIgnoreReturnValue
    public GetInputBuilder setFieldFont(Font fieldFont) {
        this.fieldFont = Preconditions.checkNotNull(fieldFont);
        return this;
    }

    /**
     * Returns the field foreground.
     *
     * @return the field foreground
     */
    public Color getFieldForeground() {
        return fieldForeground;
    }

    /**
     * Sets the field foreground.
     *
     * @param fieldForeground the field foreground
     * @return this builder
     */
    @CanIgnoreReturnValue
    public GetInputBuilder setFieldForeground(Color fieldForeground) {
        this.fieldForeground = Preconditions.checkNotNull(fieldForeground);
        return this;
    }

    /**
     * Returns the relative to frame.
     *
     * @return the relative to frame
     */
    @Override
    public CyderFrame getRelativeTo() {
        return relativeTo;
    }

    /**
     * Sets the relative to frame.
     *
     * @param relativeTo the relative to frame
     * @return this builder
     */
    @Override
    @CanIgnoreReturnValue
    public GetInputBuilder setRelativeTo(CyderFrame relativeTo) {
        this.relativeTo = relativeTo;
        return this;
    }

    /**
     * Returns whether the relative component should be disabled while the get frame is active.
     *
     * @return whether the relative component should be disabled while the get frame is active
     */
    @Override
    public boolean isDisableRelativeTo() {
        return disableRelativeTo;
    }

    /**
     * Sets whether the relative component should be disabled while the get frame is active.
     *
     * @param disableRelativeTo whether to disable the relativeTo component
     * @return this builder
     */
    @CanIgnoreReturnValue
    public GetInputBuilder setDisableRelativeTo(boolean disableRelativeTo) {
        this.disableRelativeTo = disableRelativeTo;
        return this;
    }

    /**
     * Returns the list of on dialog disposal runnables.
     *
     * @return the list of on dialog disposal runnables
     */
    public ImmutableList<Runnable> getOnDialogDisposalRunnables() {
        return ImmutableList.copyOf(onDialogDisposalRunnables);
    }

    /**
     * Adds the runnable as an on dialog disposal runnable.
     *
     * @param runnable the on dialog disposal runnable
     * @return this builder
     */
    @CanIgnoreReturnValue
    public GetInputBuilder addOnDialogDisposalRunnable(Runnable runnable) {
        onDialogDisposalRunnables.add(Preconditions.checkNotNull(runnable));
        return this;
    }
}
