package cyder.handlers.internal.objects;

import cyder.enums.LoggerTag;
import cyder.handlers.internal.Logger;
import cyder.ui.CyderFrame;

import java.awt.*;

/**
 * InformBuilder for Cyder inform frames as opposed to telescoping patterns.
 */
public final class InformBuilder {
    /**
     * The minimum allowable text length for an information pane.
     */
    public static final int MINIMUM_TEXT_LENGTH = 4;

    /**
     * The default title for an information pane which are provided no title.
     */
    public static final String DEFAULT_TITLE = "Information";

    //required params
    private final String htmlText;

    //optional params
    private String title = DEFAULT_TITLE;
    private Component relativeTo;
    private CyderFrame.PreCloseAction preCloseAction;
    private CyderFrame.PostCloseAction postCloseAction;

    /**
     * Default constructor for an inform pane with the required parameters.
     *
     * @param htmlText the html styled text to display on the inform pane
     */
    public InformBuilder(String htmlText) {
        if (htmlText == null || htmlText.length() < MINIMUM_TEXT_LENGTH)
            throw new IllegalArgumentException("Html text is null or less than " + MINIMUM_TEXT_LENGTH + " chars");

        this.htmlText = htmlText;
        Logger.log(LoggerTag.OBJECT_CREATION, this);
    }

    public String getHtmlText() {
        return htmlText;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Component getRelativeTo() {
        return relativeTo;
    }

    public void setRelativeTo(Component relativeTo) {
        this.relativeTo = relativeTo;
    }

    public CyderFrame.PreCloseAction getPreCloseAction() {
        return preCloseAction;
    }

    public void setPreCloseAction(CyderFrame.PreCloseAction preCloseAction) {
        this.preCloseAction = preCloseAction;
    }

    public CyderFrame.PostCloseAction getPostCloseAction() {
        return postCloseAction;
    }

    public void setPostCloseAction(CyderFrame.PostCloseAction postCloseAction) {
        this.postCloseAction = postCloseAction;
    }
}
