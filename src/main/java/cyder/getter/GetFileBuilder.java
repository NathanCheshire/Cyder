package cyder.getter;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.enums.SystemPropertyKey;
import cyder.strings.CyderStrings;
import cyder.ui.frame.CyderFrame;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

/**
 * A builder for a get file getter method.
 */
public final class GetFileBuilder extends GetBuilder {
    /**
     * The frame title.
     */
    private final String frameTitle;

    /**
     * The initial directory to load files from
     */
    private final File initialDirectory;

    /**
     * The initial text of the file field. For if an alternative to the initial directory name is desired.
     */
    private String initialFieldText;

    /**
     * The directory field foreground.
     */
    private Color fieldForeground = CyderColors.navy;

    /**
     * The directory field font.
     */
    private Font fieldFont = CyderFonts.SEGOE_20;

    /**
     * Whether the getter should allow submissions of files.
     */
    private boolean allowFileSubmission = true;

    /**
     * Whether the getter should allow submissions of folders.
     */
    private boolean allowFolderSubmission;

    /**
     * The text of the submit button.
     */
    private String submitButtonText = "Submit";

    /**
     * The submit button font.
     */
    private Font submitButtonFont = CyderFonts.SEGOE_20;

    /**
     * The submit button background color.
     */
    private Color submitButtonColor = CyderColors.regularPink;

    /**
     * The frame to set the getter frame relative to.
     */
    private CyderFrame relativeTo;

    /**
     * Whether the relative to component should be disabled while the getter frame is active.
     */
    private boolean disableRelativeTo;

    /**
     * The list of runnables to invoke when the getter frame is disposed.
     */
    private final ArrayList<Runnable> onDialogDisposalRunnables = new ArrayList<>();

    /**
     * The list of allowable file extensions.
     */
    private ImmutableList<String> allowableFileExtensions = ImmutableList.of();

    /**
     * Constructs a new get file builder.
     *
     * @param frameTitle the frame title
     */
    public GetFileBuilder(String frameTitle) {
        this(frameTitle, new File(SystemPropertyKey.USER_DIR.getProperty()));
    }

    /**
     * Constructs a new get file builder.
     *
     * @param frameTitle       the frame title
     * @param initialDirectory the initial directory to load the list of files from
     */
    public GetFileBuilder(String frameTitle, File initialDirectory) {
        Preconditions.checkNotNull(frameTitle);
        Preconditions.checkArgument(!frameTitle.isEmpty());
        Preconditions.checkNotNull(initialDirectory);
        Preconditions.checkArgument(initialDirectory.exists());

        this.frameTitle = frameTitle;
        this.initialDirectory = initialDirectory;
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
     * Returns the initial directory to load the files from.
     *
     * @return the initial directory to load the files from
     */
    public File getInitialDirectory() {
        return initialDirectory;
    }

    /**
     * Returns the initial directory field text.
     *
     * @return the initial directory field text
     */
    public String getInitialFieldText() {
        return initialFieldText;
    }

    /**
     * Sets the initial directory field text.
     *
     * @param initialFieldText the initial directory field text
     * @return this builder
     */
    public GetFileBuilder setInitialFieldText(String initialFieldText) {
        this.initialFieldText = Preconditions.checkNotNull(initialFieldText);
        return this;
    }

    /**
     * Returns the foreground of the directory field.
     *
     * @return the foreground of the directory field
     */
    public Color getFieldForeground() {
        return fieldForeground;
    }

    /**
     * Sets the foreground of the directory field.
     *
     * @param fieldForeground the foreground of the directory field
     * @return this builder
     */
    public GetFileBuilder setFieldForeground(Color fieldForeground) {
        this.fieldForeground = Preconditions.checkNotNull(fieldForeground);
        return this;
    }

    /**
     * Returns the font of the directory field.
     *
     * @return the font of the directory field
     */
    public Font getFieldFont() {
        return fieldFont;
    }

    /**
     * Sets the font of the directory field.
     *
     * @param fieldFont the font of the directory field
     * @return this builder
     */
    public GetFileBuilder setFieldFont(Font fieldFont) {
        this.fieldFont = Preconditions.checkNotNull(fieldFont);
        return this;
    }

    /**
     * Returns whether this file getter should allow the submission of files.
     *
     * @return whether this file getter should allow the submission of files
     */
    public boolean isAllowFileSubmission() {
        return allowFileSubmission;
    }

    /**
     * Sets whether this file getter should allow the submission of files.
     *
     * @param allowFileSubmission whether this file getter should allow the submission of files
     * @return this builder
     */
    public GetFileBuilder setAllowFileSubmission(boolean allowFileSubmission) {
        this.allowFileSubmission = allowFileSubmission;
        return this;
    }

    /**
     * Returns whether this file getter should allow the submission of folders.
     *
     * @return whether this file getter should allow the submission of folders
     */
    public boolean isAllowFolderSubmission() {
        return allowFolderSubmission;
    }

    /**
     * Sets whether this file getter should allow the submission of folders.
     *
     * @param allowFolderSubmission whether this file getter should allow the submission of folders
     * @return this builder
     */
    public GetFileBuilder setAllowFolderSubmission(boolean allowFolderSubmission) {
        this.allowFolderSubmission = allowFolderSubmission;
        return this;
    }

    /**
     * Returns the text of the submit button.
     *
     * @return the text of the submit button
     */
    public String getSubmitButtonText() {
        return submitButtonText;
    }

    /**
     * Sets the text of the submit button.
     *
     * @param submitButtonText the text of the submit button
     * @return this  builder
     */
    public GetFileBuilder setSubmitButtonText(String submitButtonText) {
        this.submitButtonText = Preconditions.checkNotNull(submitButtonText);
        return this;
    }

    /**
     * Returns the font of the submit button.
     *
     * @return the font of the submit button
     */
    public Font getSubmitButtonFont() {
        return submitButtonFont;
    }

    /**
     * Sets the font of the submit button.
     *
     * @param submitButtonFont the font of the submit button
     * @return this builder
     */
    public GetFileBuilder setSubmitButtonFont(Font submitButtonFont) {
        this.submitButtonFont = Preconditions.checkNotNull(submitButtonFont);
        return this;
    }

    /**
     * Returns the color of the submit button.
     *
     * @return the color of the submit button
     */
    public Color getSubmitButtonColor() {
        return submitButtonColor;
    }

    /**
     * Sets the color of the submit button.
     *
     * @param submitButtonColor the color of the submit button
     * @return this builder
     */
    public GetFileBuilder setSubmitButtonColor(Color submitButtonColor) {
        this.submitButtonColor = Preconditions.checkNotNull(submitButtonColor);
        return this;
    }

    /**
     * Returns the frame to set the getter frame relative to.
     *
     * @return the frame to set the getter frame relative to
     */
    @Override
    public CyderFrame getRelativeTo() {
        return relativeTo;
    }

    /**
     * Returns the frame to set the getter frame relative to.
     *
     * @param relativeTo the frame to set the getter frame relative to
     * @return this builder
     */
    @Override
    public GetFileBuilder setRelativeTo(CyderFrame relativeTo) {
        this.relativeTo = relativeTo;
        return this;
    }

    /**
     * Adds the provided runnable as an action to invoke when the getter frame is disposed.
     *
     * @param onDialogDisposalRunnable the on dialog disposal runnable
     * @return this builder
     */
    @Override
    public GetBuilder addOnDialogDisposalRunnable(Runnable onDialogDisposalRunnable) {
        onDialogDisposalRunnables.add(Preconditions.checkNotNull(onDialogDisposalRunnable));
        return this;
    }

    /**
     * Whether the relative to component should be disabled while the getter frame is active.
     *
     * @return the relative to component should be disabled while the getter frame is active
     */
    @Override
    public boolean isDisableRelativeTo() {
        return disableRelativeTo;
    }

    /**
     * Sets the relative to component should be disabled while the getter frame is active.
     *
     * @param disableRelativeTo whether to disable the relativeTo component
     * @return this builder
     */
    @Override
    public GetFileBuilder setDisableRelativeTo(boolean disableRelativeTo) {
        this.disableRelativeTo = disableRelativeTo;
        return this;
    }

    /**
     * Returns the list of runnables to invoke when the getter frame is disposed.
     *
     * @return the list of runnables to invoke when the getter frame is disposed
     */
    public ImmutableList<Runnable> getOnDialogDisposalRunnables() {
        return ImmutableList.copyOf(onDialogDisposalRunnables);
    }

    /**
     * Returns the allowable file extensions list.
     *
     * @return the allowable file extensions list
     */
    public ImmutableList<String> getAllowableFileExtensions() {
        return ImmutableList.copyOf(allowableFileExtensions);
    }

    /**
     * Sets the allowable file extensions list.
     *
     * @param allowableFileExtensions the allowable file extensions list
     * @return this builder
     */
    public GetFileBuilder setAllowableFileExtensions(ImmutableList<String> allowableFileExtensions) {
        this.allowableFileExtensions = Preconditions.checkNotNull(allowableFileExtensions);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = frameTitle.hashCode();
        ret = ret * 31 + initialDirectory.hashCode();
        if (initialFieldText != null) ret = ret * 31 + initialFieldText.hashCode();
        ret = ret * 31 + fieldForeground.hashCode();
        ret = ret * 31 + fieldFont.hashCode();
        ret = ret * 31 + Boolean.hashCode(allowFileSubmission);
        ret = ret * 31 + Boolean.hashCode(allowFolderSubmission);
        ret = ret * 31 + submitButtonText.hashCode();
        ret = ret * 31 + submitButtonColor.hashCode();
        ret = ret * 31 + submitButtonFont.hashCode();
        if (this.relativeTo != null) ret = ret * 31 + relativeTo.hashCode();
        ret = ret * 31 + Boolean.hashCode(disableRelativeTo);
        ret = ret * 31 + onDialogDisposalRunnables.hashCode();
        ret = ret * 31 + allowableFileExtensions.hashCode();
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof GetFileBuilder)) {
            return false;
        }

        GetFileBuilder other = (GetFileBuilder) o;

        return frameTitle.equals(other.getFrameTitle())
                && initialDirectory.equals(other.getInitialDirectory())
                && Objects.equals(initialFieldText, other.getInitialFieldText())
                && fieldForeground.equals(other.getFieldForeground())
                && fieldFont.equals(other.getFieldFont())
                && allowFileSubmission == other.isAllowFileSubmission()
                && allowFolderSubmission == other.isAllowFolderSubmission()
                && submitButtonText.equals(other.getSubmitButtonText())
                && submitButtonColor.equals(other.getSubmitButtonColor())
                && Objects.equals(relativeTo, other.getRelativeTo())
                && disableRelativeTo == other.isDisableRelativeTo()
                && onDialogDisposalRunnables.equals(other.getOnDialogDisposalRunnables())
                && allowableFileExtensions == other.getAllowableFileExtensions()
                && submitButtonFont == other.getSubmitButtonFont();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "GetFileBuilder{"
                + "frameTitle="
                + CyderStrings.quote
                + frameTitle
                + CyderStrings.quote
                + ", initialDirectory="
                + initialDirectory
                + ", initialFieldText="
                + CyderStrings.quote
                + initialFieldText
                + CyderStrings.quote
                + ", fieldForeground="
                + fieldForeground
                + ", fieldFont="
                + fieldFont
                + ", allowFileSubmission="
                + allowFileSubmission
                + ", allowFolderSubmission="
                + allowFolderSubmission
                + ", submitButtonText="
                + CyderStrings.quote
                + submitButtonText
                + CyderStrings.quote
                + ", submitButtonFont="
                + submitButtonFont
                + ", submitButtonColor="
                + submitButtonColor
                + ", relativeTo="
                + relativeTo
                + ", disableRelativeTo="
                + disableRelativeTo
                + ", onDialogDisposalRunnables="
                + onDialogDisposalRunnables
                + ", allowableFileExtensions="
                + allowableFileExtensions
                + "}";
    }
}
