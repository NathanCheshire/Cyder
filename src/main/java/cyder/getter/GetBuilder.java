package cyder.getter;

import cyder.ui.frame.CyderFrame;

/**
 * A base getter util builder for all specific get method builders to extend.
 */
public interface GetBuilder {
    /**
     * Returns the title of the get frame.
     *
     * @return the title of the get frame
     */
    String getFrameTitle();

    /**
     * Returns whether to disable the relativeTo component.
     *
     * @return whether to disable the relativeTo component
     */
    boolean isDisableRelativeTo();

    /**
     * Sets whether to disable the relativeTo component.
     *
     * @param disableRelativeTo whether to disable the relativeTo component
     * @return this builder
     */
    GetBuilder setDisableRelativeTo(boolean disableRelativeTo);

    /**
     * Returns the component to set the getter frame relative to.
     *
     * @return the component to set the getter frame relative to
     */
    CyderFrame getRelativeTo();

    /**
     * Sets the component to set the getter frame relative to.
     *
     * @param frame the frame to set the getter frame relative to
     */
    GetBuilder setRelativeTo(CyderFrame frame);

    /**
     * Adds the runnable as an on dialog disposal runnable.
     *
     * @param onDialogDisposalRunnable the on dialog disposal runnable
     * @return this builder
     */
    GetBuilder addOnDialogDisposalRunnable(Runnable onDialogDisposalRunnable);
}
