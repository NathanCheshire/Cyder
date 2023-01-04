package cyder.ui.pane;

import com.google.common.base.Preconditions;
import cyder.constants.CyderColors;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.strings.CyderStrings;
import cyder.strings.StringUtil;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.Semaphore;

/**
 * A wrapper to associated a {@link JTextPane}, {@link StringUtil},
 * and {@link Semaphore} into a thread-safe happy little entity.
 * Note that this does not make the provided objects immutable or thread-safe.
 */
public class CyderOutputPane {
    /**
     * The text used to generate a menu separation label.
     */
    private static final String magicMenuSepText = "NateCheshire";

    /**
     * The starting x value for a menu separation.
     */
    private static final int menuSepX = 0;

    /**
     * The starting y value for a menu separation.
     */
    private static final int menuSepY = 7;

    /**
     * The width of menu separation components.
     */
    private static final int menuSepWidth = 175;

    /**
     * The height of menu separation components.
     */
    private static final int menuSepHeight = 5;

    /**
     * The bounds for a menu separation label.
     */
    private static final Rectangle menuSepBounds = new Rectangle(menuSepX, menuSepY, menuSepWidth, menuSepHeight);

    /**
     * The default color of menu separator components.
     */
    private static final Color DEFAULT_MENU_SEP_COLOR = CyderColors.vanilla;

    /**
     * The linked JTextPane.
     */
    private final JTextPane jTextPane;

    /**
     * The StringUtil object to perform common operations on the JTextPane.
     */
    private final StringUtil stringUtil;

    /**
     * The linked Semaphore to make appending/removing to/from the JTextPane thread-safe.
     */
    private final Semaphore semaphore;

    /**
     * Instantiation not allowed unless all three arguments are provided
     */
    private CyderOutputPane() {
        throw new IllegalStateException(CyderStrings.ILLEGAL_CONSTRUCTOR);
    }

    /**
     * Constructs a new CyderOutputPane.
     *
     * @param jTextPane the JTextPane to link to this instance of CyderOutputPane
     */
    public CyderOutputPane(JTextPane jTextPane) {
        this.jTextPane = Preconditions.checkNotNull(jTextPane);

        stringUtil = new StringUtil(this);
        semaphore = new Semaphore(1);

        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Returns the linked JTextPane object.
     *
     * @return the linked JTextPane object
     */
    public JTextPane getJTextPane() {
        return jTextPane;
    }

    /**
     * Returns the linked StringUtil object.
     *
     * @return the linked StringUtil object
     */
    public StringUtil getStringUtil() {
        return stringUtil;
    }

    /**
     * Returns the Semaphore which controls the JTextPane.
     *
     * @return the Semaphore which controls the JTextPane
     */
    public Semaphore getSemaphore() {
        return semaphore;
    }

    /**
     * Prints a menu separator to the {@link JTextPane}.
     */
    public void printMenuSeparator() {
        stringUtil.printlnComponent(getMenuSeparator());
        stringUtil.newline();
    }

    /**
     * Returns a menu separator label.
     *
     * @return a menu separator label
     */
    private JLabel getMenuSeparator() {
        return getMenuSeparator(DEFAULT_MENU_SEP_COLOR);
    }

    /**
     * Returns a menu separator label.
     *
     * @return a menu separator label
     */
    @SuppressWarnings("SameParameterValue")
    private JLabel getMenuSeparator(Color color) {
        Preconditions.checkNotNull(color);

        JLabel sepLabel = new JLabel(magicMenuSepText) {
            @Override
            public void paintComponent(Graphics g) {
                g.setColor(getForeground());
                g.fillRect((int) menuSepBounds.getX(), (int) menuSepBounds.getY(),
                        (int) menuSepBounds.getWidth(), (int) menuSepBounds.getHeight());
                g.dispose();
            }
        };
        sepLabel.setForeground(color);
        return sepLabel;
    }
}
