package cyder.ui;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.handlers.internal.SessionHandler;
import cyder.utilities.ReflectionUtil;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CyderConstrainedLabel extends JLabel {
    private CyderConstrainedLabel() {
        this("NULL");
    }

    public CyderConstrainedLabel(String text) {
        super(text);
        this.setText(innerLogic(text));
        installDefaults();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                SessionHandler.log(SessionHandler.Tag.ACTION, e.getComponent());
            }
        });
    }

    @Override
    public void setText(String text) {
        super.setText(innerLogic(text));
    }

    //strips away any existing html and adds in the width or height constraints
    private String innerLogic(String text) {
        String ret = "";

        if (constrainedWidth == 0 && constrainedHeight != 0)
            throw new IllegalArgumentException("constrained width and height not set");
        else if (constrainedWidth != 0 && constrainedWidth != 0)
            throw new IllegalArgumentException("This shouldn't be possible, but constrainedWidth and " +
                    "constrainedHeight cannot both be set at the same time");

        text = Jsoup.clean(text, Safelist.none());

        if (constrainedWidth != 0) {
            ret = "<html><div style=\"width:" + constrainedWidth + "px;\">" + text + "</div></html>";
        } else {
            ret = "<html><div style=\"height:" + constrainedHeight + "px;\">" + text + "</div></html>";
        }

        return ret;
    }

    //zero indicates any value needed
    private int constrainedWidth = 0;
    private int constrainedHeight = 0;

    public int getConstrainedWidth() {
        return constrainedWidth;
    }

    public void setConstrainedWidth(int constrainedWidth) {
        this.constrainedWidth = constrainedWidth;
        this.constrainedHeight = 0;
    }

    public int getConstrainedHeight() {
        return constrainedHeight;
    }

    public void setConstrainedHeight(int constrainedHeight) {
        this.constrainedHeight = constrainedHeight;
        this.constrainedWidth = 0;
    }

    /**
     * Sets the color, font, and alignment defaults
     */
    public void installDefaults() {
        setForeground(CyderColors.navy);
        setFont(CyderFonts.segoe20);
        setHorizontalAlignment(JLabel.CENTER);
        setVerticalAlignment(JLabel.CENTER);
    }

    @Override
    public String toString() {
        return ReflectionUtil.commonCyderUIReflection(this);
    }
}
