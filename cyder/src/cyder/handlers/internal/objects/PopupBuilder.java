package cyder.handlers.internal.objects;

import cyder.ui.CyderFrame;

import java.awt.*;

/**
 * PopupBuilder for Cyder popups as opposed to telescoping patterns.
 */
public final class PopupBuilder {
    /**
     * The minimum allowable text length for a popup.
     */
    public static final int MINIMUM_TEXT_LENGTH = 4;

    /**
     * The default title for popups which are provided no title.
     */
    public static final String DEFAULT_TITLE = "Cyder Popup";

    //required params
    private final String htmlText;

    //optional params
    private String title = DEFAULT_TITLE;
    private Component relativeTo = null;
    private CyderFrame.PreCloseAction preCloseAction = null;
    private CyderFrame.PostCloseAction postCloseAction = null;

    /**
     * Default constructor for a Popup with the required parameters.
     *
     * @param htmlText the html styled text to display on the popup
     */
    public PopupBuilder(String htmlText) {
        if (htmlText == null || htmlText.length() < MINIMUM_TEXT_LENGTH)
            throw new IllegalArgumentException("Html text is null or less than " + MINIMUM_TEXT_LENGTH + " chars");

        this.htmlText = htmlText;
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
