package cyder.genesis;

import java.util.concurrent.Semaphore;

public class GenesisShare {
    //private constructor
    private GenesisShare() {}

    private static Semaphore exitingSem = new Semaphore(1);

    public static Semaphore getExitingSem() {
        return exitingSem;
    }
}
