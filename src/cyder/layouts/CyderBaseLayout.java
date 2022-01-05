package cyder.layouts;

import javax.swing.*;

public abstract class CyderBaseLayout extends JLabel implements CyderLayout {
    //no implementation required, this looks better than extending JLabel and this also forces
    // CyderLayouts to implement the base required methods for layout managing.
}
