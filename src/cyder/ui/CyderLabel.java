package cyder.ui;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.handlers.internal.ErrorHandler;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import javax.swing.*;
import java.awt.*;

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

    private void beginRippleSequence() {
        new Thread(() -> {
            try {
                int charIndex = 0;
                Color restoreColor = this.getForeground();

                String parsedChars = Jsoup.clean(this.getText(), Safelist.none());

                while (isRippling) {
                    //todo assuming non-html right now, we'll need to fix this later for stuff not in tags
                    for (int i = 0 ; i < parsedChars.length() ; i++) {
                        StringBuilder builder = new StringBuilder();

                        for (int j = 0 ; j < parsedChars.length() ; j++) {
                            if (i == j)
                                builder.append(getColoredText(String.valueOf(parsedChars.charAt(j)),rippleColor));
                            else
                                builder.append(parsedChars.charAt(j));
                        }

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
}
