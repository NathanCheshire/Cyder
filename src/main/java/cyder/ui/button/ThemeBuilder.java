package main.java.cyder.ui.button;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import main.java.cyder.logging.LogTag;
import main.java.cyder.logging.Logger;

import java.awt.*;

/**
 * A builder for constructing a theme for a {@link CyderModernButton}.
 */
public class ThemeBuilder {
    /**
     * The background color for the button.
     */
    private Color backgroundColor;

    /**
     * The foreground color for the button.
     */
    private Color foregroundColor;

    /**
     * The font for the button.
     */
    private Font font;

    /**
     * The border length for the button.
     */
    private int borderLength = CyderModernButton.DEFAULT_BORDER_LENGTH;

    /**
     * The border color for this button.
     */
    private Color borderColor;

    /**
     * The hover color for this button.
     */
    private Color hoverColor;

    /**
     * The pressed color for this button.
     */
    private Color pressedColor;

    /**
     * Whether this button's corners are rounded.
     */
    private boolean roundedCorners;

    /**
     * The disabled foreground color for this button.
     */
    private Color disabledForeground;

    /**
     * The disabled background color for this button.
     */
    private Color disabledBackground;

    /**
     * Constructs a new theme builder.
     */
    public ThemeBuilder() {
        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Returns the background color for this button.
     *
     * @return the background color for this button
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets the background color for this button.
     *
     * @param backgroundColor the background color for this button
     * @return this builder
     */
    @CanIgnoreReturnValue
    public ThemeBuilder setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = Preconditions.checkNotNull(backgroundColor);
        return this;
    }

    /**
     * Returns the foreground color for this button.
     *
     * @return the foreground color for this button
     */
    public Color getForegroundColor() {
        return foregroundColor;
    }

    /**
     * Sets the foreground color for this button.
     *
     * @param foregroundColor the foreground color for this button
     * @return this builder
     */
    @CanIgnoreReturnValue
    public ThemeBuilder setForegroundColor(Color foregroundColor) {
        this.foregroundColor = Preconditions.checkNotNull(foregroundColor);
        return this;
    }

    /**
     * Returns the font for this button.
     *
     * @return the font for this button
     */
    public Font getFont() {
        return font;
    }

    /**
     * Sets the font for this button.
     *
     * @param font the font for this button
     * @return this builder
     */
    @CanIgnoreReturnValue
    public ThemeBuilder setFont(Font font) {
        this.font = Preconditions.checkNotNull(font);
        return this;
    }

    /**
     * Returns the border length for this button.
     *
     * @return the border length for this button
     */
    public int getBorderLength() {
        return borderLength;
    }

    /**
     * Sets the border length for this button.
     *
     * @param borderLength the border length for this button
     * @return this builder
     */
    @CanIgnoreReturnValue
    public ThemeBuilder setBorderLength(int borderLength) {
        Preconditions.checkArgument(borderLength >= 0);
        this.borderLength = borderLength;
        return this;
    }

    /**
     * Returns the hover color for this button.
     *
     * @return the hover color for this button
     */
    public Color getHoverColor() {
        return hoverColor;
    }

    /**
     * Sets the hover color for this button.
     *
     * @param hoverColor the hover color for this button
     * @return this builder
     */
    @CanIgnoreReturnValue
    public ThemeBuilder setHoverColor(Color hoverColor) {
        this.hoverColor = Preconditions.checkNotNull(hoverColor);
        return this;
    }

    /**
     * Returns the pressed color for this button.
     *
     * @return the pressed color for this button
     */
    public Color getPressedColor() {
        return pressedColor;
    }

    /**
     * Sets the pressed color for this button.
     *
     * @param pressedColor the pressed color for this button
     * @return this builder
     */
    @CanIgnoreReturnValue
    public ThemeBuilder setPressedColor(Color pressedColor) {
        this.pressedColor = Preconditions.checkNotNull(pressedColor);
        return this;
    }

    /**
     * Returns whether this button should have rounded corners.
     *
     * @return whether this button should have rounded corners
     */
    public boolean isRoundedCorners() {
        return roundedCorners;
    }

    /**
     * Sets whether this button should have rounded corners.
     *
     * @param roundedCorners whether this button should have rounded corners
     * @return this builder
     */
    @CanIgnoreReturnValue
    public ThemeBuilder setRoundedCorners(boolean roundedCorners) {
        this.roundedCorners = roundedCorners;
        return this;
    }

    /**
     * Returns the disabled foreground color for this button.
     *
     * @return the disabled foreground color for this button
     */
    public Color getDisabledForeground() {
        return disabledForeground;
    }

    /**
     * Sets the disabled foreground color for this button.
     *
     * @param disabledForeground the disabled foreground color for this button
     * @return this builder
     */
    @CanIgnoreReturnValue
    public ThemeBuilder setDisabledForeground(Color disabledForeground) {
        this.disabledForeground = Preconditions.checkNotNull(disabledForeground);
        return this;
    }

    /**
     * Returns the disabled background color for this button.
     *
     * @return the disabled background color for this button
     */
    public Color getDisabledBackground() {
        return disabledBackground;
    }

    /**
     * Sets the disabled background color for this button.
     *
     * @param disabledBackground the disabled background color for this button
     * @return this builder
     */
    @CanIgnoreReturnValue
    public ThemeBuilder setDisabledBackground(Color disabledBackground) {
        this.disabledBackground = Preconditions.checkNotNull(disabledBackground);
        return this;
    }

    /**
     * Returns the border color for this button.
     *
     * @return the border color for this button
     */
    public Color getBorderColor() {
        return borderColor;
    }

    /**
     * Sets the border color for this button.
     *
     * @param borderColor the border color for this button
     * @return this builder
     */
    @CanIgnoreReturnValue
    public ThemeBuilder setBorderColor(Color borderColor) {
        this.borderColor = Preconditions.checkNotNull(borderColor);
        return this;
    }
}
