package cyder.threads;

import java.util.concurrent.ThreadFactory;

public class CyderThreadFactory implements ThreadFactory {

    public CyderThreadFactory(String name) {
        this.name = name;
    }

    private String name = "Un-named service";
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public Thread newThread(Runnable r) {
        return new Thread(r, this.name);
    }
}
