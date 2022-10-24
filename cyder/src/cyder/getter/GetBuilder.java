package cyder.getter;

import cyder.ui.frame.CyderFrame;

/**
 * A base getter util builder for all specific get method builders to extend.
 */
public abstract class GetBuilder {
    /**
     * Returns the title of the get frame.
     *
     * @return the title of the get frame
     */
    public abstract String getFrameTitle();

    /**
     * Returns whether to disable the relativeTo component.
     *
     * @return whether to disable the relativeTo component
     */
    public abstract boolean isDisableRelativeTo();

    /**
     * Sets whether to disable the relativeTo component.
     *
     * @param disableRelativeTo whether to disable the relativeTo component
     * @return this builder
     */
    public abstract GetBuilder setDisableRelativeTo(boolean disableRelativeTo);

    /**
     * Returns the component to set the getter frame relative to.
     *
     * @return the component to set the getter frame relative to
     */
    public abstract CyderFrame getRelativeTo();

    /**
     * Sets the component to set the getter frame relative to.
     *
     * @param frame the frame to set the getter frame relative to
     */
    public abstract GetBuilder setRelativeTo(CyderFrame frame);

    /**
     * Adds the runnable as an on dialog disposal runnable.
     *
     * @param onDialogDisposalRunnable the on dialog disposal runnable
     * @return this builder
     */
    public abstract GetBuilder addOnDialogDisposalRunnable(Runnable onDialogDisposalRunnable);
}
