package cyder.layouts;

import javax.swing.*;

/**
 * All CyderLayouts should extend this class which will force them
 * to have the required methods of both a CyderLayout and also a JLabel.
 */
public abstract class CyderBaseLayout extends JLabel implements ICyderLayout {}
