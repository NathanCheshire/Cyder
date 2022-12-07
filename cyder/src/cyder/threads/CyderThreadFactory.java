package cyder.threads;

import com.google.common.base.Preconditions;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.utils.StringUtil;

import java.util.concurrent.ThreadFactory;

/**
 * A custom thread factory for Cyder.
 */
public class CyderThreadFactory implements ThreadFactory {
    /**
     * Constructs a new thread factory using the provided name
     *
     * @param name the name of the thread factory
     */
    public CyderThreadFactory(String name) {
        this.name = name;
        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * The name of this thread factory.
     */
    private String name;

    /**
     * Sets the name of this thread factory.
     *
     * @param name the name of this thread factory
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the name of this thread factory.
     *
     * @return the name of this thread factory
     */
    public String getName() {
        return name;
    }

    /**
     * Returns a new thread using the provided runnable and name.
     *
     * @param runnable the runnable to use for the thread
     * @return a new thread using the provided runnable and name
     */
    public Thread newThread(Runnable runnable) {
        return new Thread(Preconditions.checkNotNull(runnable), name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return StringUtil.commonCyderToString(this);
    }
}
