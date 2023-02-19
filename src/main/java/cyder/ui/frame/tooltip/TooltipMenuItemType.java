package cyder.ui.frame.tooltip;

/**
 * The supported menu item choices for any tooltip menu.
 */
enum TooltipMenuItemType {
    TO_BACK("To back"),
    FRAME_LOCATION("Frame location"),
    FRAME_SIZE("Frame size"),
    SCREENSHOT("Screenshot");

    /**
     * The label text for this menu item.
     */
    private final String labelText;

    TooltipMenuItemType(String labelText) {
        this.labelText = labelText;
    }

    /**
     * Returns the label text.
     *
     * @return the label text
     */
    public String getLabelText() {
        return labelText;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return labelText;
    }
}
