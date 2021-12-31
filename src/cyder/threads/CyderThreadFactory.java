package cyder.threads;

import cyder.utilities.ReflectionUtil;

import java.util.concurrent.ThreadFactory;

public class CyderThreadFactory implements ThreadFactory {
    public CyderThreadFactory(String name) {
        this.name = name;
    }

    private String name = "Void name service";
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public Thread newThread(Runnable r) {
        return new Thread(r, this.name);
    }

    @Override
    public String toString() {
        return ReflectionUtil.commonCyderToString(this);
    }
}
