package cyder.ui;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.handlers.internal.ErrorHandler;
import cyder.utilities.StringUtil;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;

public class CyderLabel extends JLabel {
    public CyderLabel() {
        setText("<html>CyderLabel default text</html>");
        setForeground(CyderColors.navy);
        setFont(CyderFonts.weatherFontSmall);
        setHorizontalAlignment(JLabel.CENTER);
        setVerticalAlignment(JLabel.CENTER);
    }

    public CyderLabel(String text) {
        setText(text);
        setForeground(CyderColors.navy);
        setFont(CyderFonts.defaultFontSmall);
        setHorizontalAlignment(JLabel.CENTER);
        setVerticalAlignment(JLabel.CENTER);
    }

    public CyderLabel(String text, int horizontalAlignment) {
        setText(text);
        setForeground(CyderColors.navy);
        setFont(CyderFonts.defaultFontSmall);
        setHorizontalAlignment(JLabel.CENTER);
        setVerticalAlignment(JLabel.CENTER);
        setHorizontalAlignment(horizontalAlignment);
    }

    @Override
    public void setText(String text) {
        if (!text.startsWith("<html>")) {
            super.setText("<html><div style='text-align: center;'>" + text + "</html>");
        } else {
            super.setText(text);
        }
    }

    @Override
    public String toString() {
        return "CyderLabel object, hash=" + this.hashCode() + ", parent=" +
                (this.getParent() == null ? this.getParent() : " no parent");
    }

    //rippling
    private Color rippleColor = CyderColors.regularRed;
    private long rippleMsTimeout = 100;
    private int rippleChars = 1;
    private boolean isRippling;

    public int getRippleChars() {
        return rippleChars;
    }

    public void setRippleChars(int rippleChars) {
        this.rippleChars = rippleChars;
    }

    public Color getRippleColor() {
        return rippleColor;
    }

    public void setRippleColor(Color rippleColor) {
        this.rippleColor = rippleColor;
    }

    public long getRippleMsTimeout() {
        return rippleMsTimeout;
    }

    public void setRippleMsTimeout(long rippleMsTimeout) {
        this.rippleMsTimeout = rippleMsTimeout;
    }

    public boolean isRippling() {
        return isRippling;
    }

    public void setRippling(boolean rippling) {
        isRippling = rippling;

        if (rippling)
            beginRippleSequence();
    }

    private void beginRippleSequence() {
        new Thread(() -> {
            try {
                //restore color so everything goes back to original foreground
                Color restoreColor = this.getForeground();

                final String originalText = this.getText();

                //used to insert color properly
                String parsedChars = Jsoup.clean(this.getText(), Safelist.none());

                //init list for strings by tag
                LinkedList<StringUtil.TaggedString> taggedStrings = StringUtil.getTaggedStrings(originalText);

                while (isRippling && this.getParent() != null) {
                    //still used parsed chars here since that's all we care about rippling anyway
                    for (int i = 0 ; i < parsedChars.length() ; i++) {
                        //init builder for this iteration where the ith char
                        // (could be from any non-html tag), is ripple color
                        StringBuilder builder = new StringBuilder();

                        int charSum = 0;
                        int rippled = 0;

                        for (StringUtil.TaggedString ts : taggedStrings) {
                            if (!isRippling)
                                return;

                            if (ts.getTag() == StringUtil.Tag.HTML) {
                                builder.append(ts.getText());
                            } else {
                                for (char c : ts.getText().toCharArray()) {
                                    if (!isRippling)
                                        return;

                                    //make sure it's i or above, and is as many chars as we want to ripple
                                    // and it's not a space
                                    if (charSum >= i && rippled < rippleChars && String.valueOf(c).trim().length() > 0) {
                                        System.out.println("rippling:" + c);
                                        builder.append(getColoredText(String.valueOf(c), rippleColor));
                                        rippled++;
                                    } else {
                                        builder.append(c);
                                    }

                                    charSum++;
                                }
                            }
                        }

                        //set text, repaint, sleep
                        if (builder.toString().startsWith("<html>"))
                            this.setText(builder.toString());
                        else
                            this.setText("<html>" + builder + "</html>");

                        this.repaint();
                        Thread.sleep(rippleMsTimeout);
                        this.setText(originalText);
                    }
                }

                this.setForeground(restoreColor);
            } catch (Exception e) {
                ErrorHandler.handle(e);
            }
        }, "Ripple thread for: " + this).start();
    }

    private String getColoredText(String text, Color c) {
        return "<font color = rgb(" + c.getRed() + "," + c.getGreen() + "," + c.getBlue() + ")>" + text + "</font>";
    }
}
