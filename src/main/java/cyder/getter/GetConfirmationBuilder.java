package cyder.getter;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.strings.CyderStrings;
import cyder.ui.frame.CyderFrame;

import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;

/**
 * A builder for a get confirmation getter method.
 */
public final class GetConfirmationBuilder extends GetBuilder {
    /**
     * The frame title.
     */
    private final String frameTitle;

    /**
     * The prompt label text.
     */
    private final String labelText;

    /**
     * The prompt label font.
     */
    private Font labelFont = CyderFonts.DEFAULT_FONT_SMALL;

    /**
     * The prompt label foreground color.
     */
    private Color labelColor = CyderColors.navy;

    /**
     * The text of the yes button.
     */
    private String yesButtonText = "Yes";

    /**
     * The color of the yes button.
     */
    private Color yesButtonColor = CyderColors.regularRed;

    /**
     * The font of the yes button.
     */
    private Font yesButtonFont = CyderFonts.SEGOE_20;

    /**
     * The text of the no button.
     */
    private String noButtonText = "No";

    /**
     * The foreground color of the no button
     */
    private Color noButtonColor = CyderColors.regularRed;

    /**
     * The font of the no button.
     */
    private Font noButtonFont = CyderFonts.SEGOE_20;

    /**
     * The component to set the frame relative to.
     */
    private CyderFrame relativeTo;

    /**
     * Whether the relativeTo component should be disabled while the get frame is active.
     */
    private boolean disableRelativeTo;

    /**
     * The list of actions to invoke when the get frame is disposed.
     */
    private final ArrayList<Runnable> onDialogDisposalRunnables = new ArrayList<>();

    /**
     * Constructs a new get confirmation builder.
     *
     * @param frameTitle the title of the frame
     * @param labelText  the label text
     */
    public GetConfirmationBuilder(String frameTitle, String labelText) {
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
    public GetConfirmationBuilder setLabelFont(Font labelFont) {
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
    public GetConfirmationBuilder setLabelColor(Color labelColor) {
        this.labelColor = labelColor;
        return this;
    }

    /**
     * Returns the text of the yes button.
     *
     * @return the text of the yes button
     */
    public String getYesButtonText() {
        return yesButtonText;
    }

    /**
     * Sets the text of the yes button.
     *
     * @param yesButtonText the text of the yes button
     * @return the builder
     */
    @CanIgnoreReturnValue
    public GetConfirmationBuilder setYesButtonText(String yesButtonText) {
        this.yesButtonText = yesButtonText;
        return this;
    }

    /**
     * Returns the color of the yes button.
     *
     * @return the color of the yes button
     */
    public Color getYesButtonColor() {
        return yesButtonColor;
    }

    /**
     * Sets the color of the yes button.
     *
     * @param yesButtonColor the color of the yes button
     * @return this builder
     */
    @CanIgnoreReturnValue
    public GetConfirmationBuilder setYesButtonColor(Color yesButtonColor) {
        this.yesButtonColor = yesButtonColor;
        return this;
    }

    /**
     * Returns the yes button font.
     *
     * @return the yes button font
     */
    public Font getYesButtonFont() {
        return yesButtonFont;
    }

    /**
     * Sets the yes button font.
     *
     * @param yesButtonFont the yes button font
     * @return this builder.
     */
    @CanIgnoreReturnValue
    public GetConfirmationBuilder setYesButtonFont(Font yesButtonFont) {
        this.yesButtonFont = yesButtonFont;
        return this;
    }

    /**
     * Returns the text of the no button.
     *
     * @return the text of the no button
     */
    public String getNoButtonText() {
        return noButtonText;
    }

    /**
     * Sets the text of the no button.
     *
     * @param noButtonText the text of the no button
     * @return this builder
     */
    @CanIgnoreReturnValue
    public GetConfirmationBuilder setNoButtonText(String noButtonText) {
        this.noButtonText = noButtonText;
        return this;
    }

    /**
     * Returns the no button color.
     *
     * @return the no button color
     */
    public Color getNoButtonColor() {
        return noButtonColor;
    }

    /**
     * Sets the no button color.
     *
     * @param noButtonColor the no button color
     * @return this builder
     */
    @CanIgnoreReturnValue
    public GetConfirmationBuilder setNoButtonColor(Color noButtonColor) {
        this.noButtonColor = noButtonColor;
        return this;
    }

    /**
     * Returns the no button font.
     *
     * @return the no button font
     */
    public Font getNoButtonFont() {
        return noButtonFont;
    }

    /**
     * Sets the no button font.
     *
     * @param noButtonFont the no button font
     * @return this builder.
     */
    @CanIgnoreReturnValue
    public GetConfirmationBuilder setNoButtonFont(Font noButtonFont) {
        this.noButtonFont = noButtonFont;
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
    public GetConfirmationBuilder setRelativeTo(CyderFrame relativeTo) {
        this.relativeTo = relativeTo;
        return this;
    }

    /**
     * Adds the provided runnable to invoke when the get frame is disposed.
     *
     * @param onDialogDisposalRunnable the on dialog disposal runnable
     * @return this builder.
     */
    @Override
    @CanIgnoreReturnValue
    public GetBuilder addOnDialogDisposalRunnable(Runnable onDialogDisposalRunnable) {
        onDialogDisposalRunnables.add(Preconditions.checkNotNull(onDialogDisposalRunnable));
        return this;
    }

    /**
     * Returns whether the relative to frame should be disabled while the getter frame is active.
     *
     * @return whether the relative to frame should be disabled while the getter frame is active
     */
    @Override
    public boolean isDisableRelativeTo() {
        return disableRelativeTo;
    }

    /**
     * Sets whether the relative to frame should be disabled while the getter frame is active.
     *
     * @param disableRelativeTo whether to disable the relativeTo component
     * @return this builder
     */
    @Override
    @CanIgnoreReturnValue
    public GetConfirmationBuilder setDisableRelativeTo(boolean disableRelativeTo) {
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
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof GetConfirmationBuilder)) {
            return false;
        }

        GetConfirmationBuilder other = (GetConfirmationBuilder) o;
        boolean ret = frameTitle.equals(other.getFrameTitle())
                && labelText.equals(other.getLabelText())
                && labelFont.equals(other.getLabelFont())
                && labelColor.equals(other.getLabelColor())
                && yesButtonText.equals(other.getYesButtonText())
                && yesButtonColor.equals(other.getYesButtonColor())
                && yesButtonFont.equals(other.getYesButtonFont())
                && noButtonText.equals(other.getNoButtonText())
                && noButtonColor.equals(other.getNoButtonColor())
                && noButtonFont.equals(other.getNoButtonFont())
                && disableRelativeTo == other.isDisableRelativeTo()
                && onDialogDisposalRunnables.equals(other.getOnDialogDisposalRunnables());

        if (relativeTo != null) {
            ret = ret && relativeTo.equals(other.getRelativeTo());
        }

        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int ret = frameTitle.hashCode();
        ret = ret * 31 + labelText.hashCode();
        ret = ret * 31 + labelFont.hashCode();
        ret = ret * 31 + labelColor.hashCode();
        ret = ret * 31 + yesButtonText.hashCode();
        ret = ret * 31 + yesButtonColor.hashCode();
        ret = ret * 31 + yesButtonFont.hashCode();
        ret = ret * 31 + noButtonText.hashCode();
        ret = ret * 31 + noButtonColor.hashCode();
        ret = ret * 31 + noButtonFont.hashCode();
        ret = ret * 31 + Objects.hashCode(relativeTo);
        ret = ret * 31 + Boolean.hashCode(disableRelativeTo);
        ret = ret * 31 + onDialogDisposalRunnables.hashCode();
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "GetConfirmationBuilder{"
                + "frameTitle="
                + CyderStrings.quote
                + frameTitle
                + CyderStrings.quote
                + ", labelText="
                + CyderStrings.quote
                + labelText
                + CyderStrings.quote
                + ", labelFont="
                + labelFont
                + ", labelColor="
                + labelColor
                + ", yesButtonText="
                + CyderStrings.quote
                + yesButtonText
                + CyderStrings.quote
                + ", yesButtonColor="
                + yesButtonColor
                + ", yesButtonFont="
                + yesButtonFont
                + ", noButtonText="
                + CyderStrings.quote
                + noButtonText
                + CyderStrings.quote
                + ", noButtonColor="
                + noButtonColor
                + ", noButtonFont="
                + noButtonFont
                + ", relativeTo="
                + relativeTo
                + ", disableRelativeTo="
                + disableRelativeTo
                + ", onDialogDisposalRunnables="
                + onDialogDisposalRunnables
                + "}";
    }
}
