package cyder.threads;

/**
 * Thread names to ignore when determining if Cyder should be classified as busy.
 */
public enum IgnoreThread {
    /* non-cyder threads */
    AwtEventQueue0("AWT-EventQueue-0", false),
    DestroyJavaVm("DestroyJavaVM", false),

    CyderBusyChecker("Cyder Busy Checker"),
    ConsoleClockUpdater("Console Clock Updater"),
    HourlyChimeChecker("Hourly Chime Checker"),
    HighPingChecker("High Ping Checker"),
    ConsolePrintingAnimation("Console Printing Animation"),
    SingularInstanceEnsurer("Singular Cyder Instance Ensurer"),
    ObjectCreationLogger("Object Creation Logger"),
    CyderWatchdog("Cyder Watchdog"),
    WatchdogInitializer("Watchdog Initializer"),
    LatencyHostnameFinder("Latency Hostname finder");

    /**
     * The name associated with the thread to ignore.
     */
    private final String name;

    /**
     * Whether the thread is launched by Cyder internally or is a JVM process thread.
     */
    private final boolean isCyderThread;

    IgnoreThread(String name) {
        this(name, true);
    }

    IgnoreThread(String name, boolean isCyderThread) {
        this.name = name;
        this.isCyderThread = isCyderThread;
    }

    /**
     * Returns the name associated with this ignore thread.
     *
     * @return the name associated with this ignore thread
     */
    public String getName() {
        return name;
    }

    /**
     * Returns whether this thread is a cyder thread.
     *
     * @return whether this thread is a cyder thread
     */
    public boolean isCyderThread() {
        return isCyderThread;
    }
}
