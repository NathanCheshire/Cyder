package cyder.ui;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.handlers.internal.ErrorHandler;
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
    private boolean isRippling;

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

    //todo more than one char rippling at a time, specify n

    private void beginRippleSequence() {
        new Thread(() -> {
            try {
                //restore color so everything goes back to original foreground
                Color restoreColor = this.getForeground();

                //used to insert color properly
                String parsedChars = Jsoup.clean(this.getText(), Safelist.none());

                //init list for strings by tag
                LinkedList<TaggedString> taggedStrings = new LinkedList<>();

                //todo figure out what a substring is an add it to the ll

                while (isRippling) {
                    //still used parsed chars here since that's all we care about rippling anyway
                    for (int i = 0 ; i < parsedChars.length() ; i++) {
                        //init builder for this iteration where the ith char
                        // (could be from any non-html tag), is ripple color
                        StringBuilder builder = new StringBuilder();

                        for (int j = 0 ; j < parsedChars.length() ; j++) {
                            if (j == i)
                                builder.append(getColoredText(String.valueOf(parsedChars.charAt(j)),rippleColor));
                            else
                                builder.append(parsedChars.charAt(j));
                        }

                        //set text, repaint, sleep
                        this.setText("<html>" + builder + "</html>");
                        this.repaint();
                        Thread.sleep(rippleMsTimeout);
                    }
                }

                this.setForeground(restoreColor);
            } catch (Exception e) {
                ErrorHandler.handle(e);
            }
        }, "Rippled thread for CyderLabel: " + this).start();
    }

    private String getColoredText(String text, Color c) {
        return "<font color = rgb(" + c.getRed() + "," + c.getGreen() + "," + c.getBlue() + ")>" + text + "</font>";
    }

    private enum Tag {
        HTML,TEXT
    }

    private static class TaggedString {
        private String text;
        private Tag tag;

        public TaggedString(String text, Tag tag) {
            this.text = text;
            this.tag = tag;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public Tag getTag() {
            return tag;
        }

        public void setTag(Tag tag) {
            this.tag = tag;
        }
    }
}
