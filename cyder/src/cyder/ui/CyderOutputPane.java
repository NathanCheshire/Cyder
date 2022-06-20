package cyder.ui;

import com.google.common.base.Preconditions;
import cyder.handlers.internal.Logger;
import cyder.utils.StringUtil;

import javax.swing.*;
import java.util.concurrent.Semaphore;

/**
 * Links a JTextPane, StringUtil, and Semaphore all
 * together into one thread-safe happy little entity.
 * <p>
 * Note that this does not make the provided objects immutable and make defensive copies.
 * Instead this is merely a wrapper for these objects since they almost always exist together.
 */
public class CyderOutputPane {
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
    @SuppressWarnings("unused")
    private CyderOutputPane() {
        throw new IllegalStateException("Instances of CyderOutputPane are not allowed " +
                "unless all parameters are given at once");
    }

    /**
     * Constructor for CyderOutputPane that takes in the JTextPane and StringUtil and
     * creates its own Semaphore.
     *
     * @param jTextPane  the JTextPane to link
     * @param stringUtil the StringUtil to use for the JTextPane
     */
    public CyderOutputPane(JTextPane jTextPane, StringUtil stringUtil) {
        Preconditions.checkNotNull(jTextPane);
        Preconditions.checkNotNull(stringUtil);

        this.jTextPane = jTextPane;
        this.stringUtil = stringUtil;

        //ensure only one permit is granted at a time
        semaphore = new Semaphore(1);

        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    /**
     * Constructor for CyderOutputPane that takes in the JTextPane and creates its own
     * StringUtil and Semaphore.
     *
     * @param jTextPane the JTextPane to link to this instance of CyderOutputPane
     */
    public CyderOutputPane(JTextPane jTextPane) {
        Preconditions.checkNotNull(jTextPane);

        this.jTextPane = jTextPane;
        stringUtil = new StringUtil(this);

        //ensure only one permit is granted at a time
        semaphore = new Semaphore(1);

        Logger.log(Logger.Tag.OBJECT_CREATION, this);
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
     * @return the linked StringUtil object.
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
}
