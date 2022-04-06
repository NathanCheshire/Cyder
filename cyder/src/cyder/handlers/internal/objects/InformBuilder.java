package cyder.handlers.internal.objects;

import cyder.enums.LoggerTag;
import cyder.handlers.internal.Logger;

import javax.swing.*;
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

    /*
    Required params.
     */

    /**
     * The text, possibly styled with html elements, to display on the information pane.
     */
    private final String htmlText;

    /*
    Optional params.
     */

    /**
     * The title of this information pane.
     */
    private String title = DEFAULT_TITLE;

    /**
     * The component to set this inform frame relative to before the call to setVisible().
     */
    private Component relativeTo;

    /**
     * The action to invoke before the frame is closed.
     */

    private Runnable preCloseAction;

    /**
     * The action to invoke after the frame is closed.
     */
    private Runnable postCloseAction;

    /**
     * The custom container component to layer on the CyderFrame content pane.
     */
    private JLabel container;

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

    public Runnable getPreCloseAction() {
        return preCloseAction;
    }

    public void setPreCloseAction(Runnable preCloseAction) {
        this.preCloseAction = preCloseAction;
    }

    public Runnable getPostCloseAction() {
        return postCloseAction;
    }

    public void setPostCloseAction(Runnable postCloseAction) {
        this.postCloseAction = postCloseAction;
    }

    public JLabel getContainer() {
        return container;
    }

    public void setContainer(JLabel container) {
        this.container = container;
    }
}
