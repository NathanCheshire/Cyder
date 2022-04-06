package cyder.handlers.internal.objects;

import com.google.common.base.Preconditions;
import cyder.enums.LoggerTag;
import cyder.handlers.internal.Logger;
import cyder.utilities.StringUtil;

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
        Preconditions.checkNotNull(htmlText);
        Preconditions.checkArgument( StringUtil.getRawTextLength(htmlText) >= MINIMUM_TEXT_LENGTH);

        this.htmlText = htmlText;
        Logger.log(LoggerTag.OBJECT_CREATION, this);
    }

    /**
     * Returns the text associated with this builder, possibly containing html style tags.
     *
     * @return the text associated with this builder, possibly containing html style tags
     */
    public String getHtmlText() {
        return htmlText;
    }

    /**
     * Returns the title for the frame.
     *
     * @return the title for the frame
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title for the frame.
     *
     * @param title the title for the frame
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the component to set the frame relative to.
     *
     * @return the component to set the frame relative to
     */
    public Component getRelativeTo() {
        return relativeTo;
    }

    /**
     * Sets the component to set the frame relative to.
     *
     * @param relativeTo the component to set the frame relative to
     */
    public void setRelativeTo(Component relativeTo) {
        this.relativeTo = relativeTo;
    }

    /**
     * Returns the pre close action to invoke before disposing the frame.
     *
     * @return the pre close action to invoke before disposing the frame
     */
    public Runnable getPreCloseAction() {
        return preCloseAction;
    }

    /**
     * Sets the pre close action to invoke before disposing the frame.
     *
     * @param preCloseAction the pre close action to invoke before disposing the frame
     */
    public void setPreCloseAction(Runnable preCloseAction) {
        this.preCloseAction = preCloseAction;
    }

    /**
     * Returns the post close action to invoke before disposing the frame.
     *
     * @return the post close action to invoke before disposing the frame
     */
    public Runnable getPostCloseAction() {
        return postCloseAction;
    }

    /**
     * Sets the post close action to invoke before closing the frame.
     *
     * @param postCloseAction the post close action to invoke before closing the frame
     */
    public void setPostCloseAction(Runnable postCloseAction) {
        this.postCloseAction = postCloseAction;
    }

    /**
     * Returns the container to use for the frame's pane.
     *
     * @return the container to use for the frame's pane
     */
    public JLabel getContainer() {
        return container;
    }

    /**
     * Sets the container to use for the frame's pane.
     *
     * @param container the container to use for the frame's pane
     */
    public void setContainer(JLabel container) {
        this.container = container;
    }
}
