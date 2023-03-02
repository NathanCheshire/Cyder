package cyder.ui.label;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.bounds.HtmlString;
import cyder.bounds.PlainString;
import cyder.bounds.StringContainer;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.HtmlTags;
import cyder.handlers.internal.ExceptionHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.strings.StringUtil;
import cyder.strings.ToStringUtil;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;
import cyder.ui.UiUtil;
import cyder.ui.frame.CyderFrame;
import cyder.utils.HtmlUtil;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import javax.swing.*;
import java.awt.*;

/**
 * A label styled for Cyder.
 */
public class CyderLabel extends JLabel {
    /**
     * The default text for a cyder label.
     */
    public static final String DEFAULT_TEXT = "I miss you";

    /**
     * The center alignment left tag.
     */
    private static final String alignTextTagLeft = HtmlTags.openingHtml + "<div style='text-align: center;'>";

    /**
     * The color used for the rippling text animation.
     */
    private Color rippleColor = CyderColors.regularRed;

    /**
     * The delay between ripple animation increments in milliseconds.
     */
    private long rippleMsTimeout = 100;

    /**
     * The number of chars during any singular frame of the ripple animation.
     */
    private int rippleChars = 1;

    /**
     * Whether the ripple animation is currently active.
     */
    private boolean isRippling;

    /**
     * Constructs a new CyderLabel.
     */
    public CyderLabel() {
        this(DEFAULT_TEXT);
    }

    /**
     * Constructs a new CyderLabel.
     *
     * @param text the initial text
     */
    public CyderLabel(String text) {
        Preconditions.checkNotNull(text);

        setText(text);
        setForeground(CyderColors.navy);
        setFont(CyderFonts.DEFAULT_FONT_SMALL);
        setHorizontalAlignment(JLabel.CENTER);
        setVerticalAlignment(JLabel.CENTER);

        addMouseListener(UiUtil.generateUiActionLoggingMouseAdapter());

        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setText(String text) {
        if (text == null || text.isEmpty()) {
            super.setText(text);
        } else if (!text.startsWith(HtmlTags.openingHtml)) {
            super.setText(alignTextTagLeft + text + HtmlTags.closingHtml);
        } else {
            super.setText(text);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return ToStringUtil.commonUiComponentToString(this);
    }

    /**
     * Returns the raw, non-html styled text length of this component's current text.
     *
     * @return the raw, non-html styled text length of this component's current text
     */
    public int getRawTextLength() {
        return Jsoup.clean(getText(), Safelist.none()).length();
    }

    /**
     * Returns the number of chars active during the ripple animation.
     *
     * @return the number of chars active during the ripple animation
     */
    public int getRippleChars() {
        return rippleChars;
    }

    /**
     * Sets the number of chars to ripple at any one time.
     *
     * @param rippleChars the number of chars to ripple at any one time
     */
    public void setRippleChars(int rippleChars) {
        if (getText() != null && rippleChars > getRawTextLength() / 2) {
            this.rippleChars = getRawTextLength() / 2;
        } else {
            this.rippleChars = rippleChars;
        }
    }

    /**
     * Returns the color used for the ripple animation.
     *
     * @return the color used for the ripple animation
     */
    public Color getRippleColor() {
        return rippleColor;
    }

    /**
     * Sets the color used for the ripple animation.
     *
     * @param rippleColor the color used for the ripple animation
     */
    public void setRippleColor(Color rippleColor) {
        Preconditions.checkNotNull(rippleColor);

        this.rippleColor = rippleColor;
    }

    /**
     * Returns the timeout between ripple animation frames.
     *
     * @return the timeout between ripple animation frames
     */
    public long getRippleMsTimeout() {
        return rippleMsTimeout;
    }

    /**
     * Sets the timeout between ripple animation frames.
     *
     * @param rippleMsTimeout the timeout between ripple animation frames
     */
    public void setRippleMsTimeout(long rippleMsTimeout) {
        Preconditions.checkArgument(rippleMsTimeout >= 0);

        this.rippleMsTimeout = rippleMsTimeout;
    }

    /**
     * Returns whether the ripple animation is currently active.
     *
     * @return whether the ripple animation is currently active
     */
    public boolean isRippling() {
        return isRippling;
    }

    /**
     * Sets whether the ripple animation is currently active.
     *
     * @param rippling whether the ripple animation is currently active
     */
    public void setRippling(boolean rippling) {
        isRippling = rippling;

        if (rippling) {
            startRippleAnimation();
        }
    }

    /**
     * Starts the ripple animation.
     */
    private void startRippleAnimation() {
        String threadName = "CyderLabel Ripple Animator, text = " + this.getText();
        CyderThreadRunner.submit(() -> {
            try {
                //restore color so everything goes back to original foreground
                Color restoreColor = getForeground();

                String originalText = getText();

                //used to insert color properly
                String parsedChars = Jsoup.clean(getText(), Safelist.none());

                //init list for strings by tag
                ImmutableList<StringContainer> taggedStrings = StringUtil.splitToHtmlTagsAndContent(originalText);

                //init ripple iterations list
                ArrayList<String> rippleTextIterations = new ArrayList<>();

                //find ripple steps: this takes < 1ms usually

                //still used parsed chars here since that's all we care about rippling anyway
                for (int i = 0 ; i < parsedChars.length() ; i++) {
                    //init builder for this iteration where the ith char
                    // (could be from any non-html tag), is ripple color
                    StringBuilder builder = new StringBuilder();

                    //charSum is how many chars we have passed of the Text tagged string
                    int charSum = 0;

                    //how many characters we've set to the rippling char
                    int rippled = 0;

                    //loop through all our tagged string
                    for (StringContainer stringContainer : taggedStrings) {
                        //if it's html simply add it to the builder
                        if (stringContainer instanceof HtmlString htmlString) {
                            builder.append(htmlString.getString());
                        } else if (stringContainer instanceof PlainString plainString) {
                            //loop through all the chars of this Text tagged string
                            for (char c : plainString.getString().toCharArray()) {
                                //first we need to pass as many raw chars
                                // as the iteration "i" we are on, next we need to make sure
                                // we haven't used up all the ripple chars for this iteration
                                if (charSum >= i && rippled < rippleChars) {
                                    //ripple this char and inc rippled
                                    builder.append(HtmlUtil.generateColoredHtmlText(String.valueOf(c), rippleColor));
                                    rippled++;
                                }
                                //otherwise append the char normal, without extra styling
                                else {
                                    builder.append(c);
                                }

                                //increment our position in the Text tagged strings
                                charSum++;
                            }
                        }
                    }

                    if (builder.toString().startsWith(HtmlTags.openingHtml)) {
                        rippleTextIterations.add(builder.toString());
                    } else {
                        rippleTextIterations.add(HtmlTags.openingHtml + builder + HtmlTags.closingHtml);
                    }
                }

                //now ripple through our ripple iterations
                RIPPLING:
                while (isRippling && !((((CyderFrame) SwingUtilities.getWindowAncestor(this))).isDisposed())) {
                    for (String rippleText : rippleTextIterations) {
                        setText(rippleText);

                        repaint();
                        ThreadUtil.sleep(rippleMsTimeout);

                        //check for break to free resources quickly
                        if (!isRippling)
                            break RIPPLING;
                    }

                    if (!isRippling)
                        break;
                }

                //fix foreground and text
                setText(originalText);
                setForeground(restoreColor);
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, threadName);
    }

    /**
     * Updates the label font to use the provided size.
     *
     * @param size the size for the label font
     */
    public void setFontSize(int size) {
        setFont(getFont().deriveFont(size));
    }
}
