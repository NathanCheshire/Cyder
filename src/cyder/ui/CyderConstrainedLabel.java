package cyder.ui;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import javax.swing.*;

public class CyderConstrainedLabel extends JLabel {
    private CyderConstrainedLabel() {}

    public CyderConstrainedLabel(String text) {

    }

    @Override
    public void setText(String text) {
        super.setText(innerLogic(text));
    }

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
}
