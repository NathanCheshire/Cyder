package cyder.ui;

import cyder.utilities.StringUtil;

import javax.swing.*;
import java.util.concurrent.Semaphore;

/**
 * Links a JTextPane, StringUtil, and Semaphore all
 * together into one thread-safe happy little entity.
 *
 * Note that this does not make the provided objedcts immutable and make defensive copies.
 * Instead this is merely a wrapper for these objects since they almost always exist together.
 */
public class CyderOutputPane {
    /**
     * The linked JTextPane.
     */
    private JTextPane jTextPane;

    //todo perhaps this can be consolidated into this class?
    /**
     * The StringUtil object to perform common operations on the JTextPane.
     */
    private StringUtil stringUtil;

    /**
     * The linked Semaphore to make appending/removing to/from the JTextPane thread-safe.
     */
    private Semaphore semaphore;

    /**
     * Instantiation not allowed unless all three arguments are provided
     */
    private CyderOutputPane() {
        throw new IllegalStateException("Instances of CyderOutputPane are not allowed unless all parameters are given at once");
    }

    public CyderOutputPane(JTextPane jTextPane, StringUtil stringUtil) {
        if (jTextPane == null)
            throw new IllegalArgumentException("Provided JTextPane is null");
        if (stringUtil == null)
            throw new IllegalArgumentException("Provided StringUtil is null");

        this.jTextPane = jTextPane;
        this.stringUtil = stringUtil;

        //ensure only one permit is granted at a time
        this.semaphore = new Semaphore(1);
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
