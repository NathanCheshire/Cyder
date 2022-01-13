package cyder.layouts;

import javax.swing.JLabel;

/**
 * All CyderLayouts should extend this class which will force them to have the required methods
 * of both a CyderLayout and also a JLabel
 */
public abstract class CyderBaseLayout extends JLabel implements CyderLayout {}
