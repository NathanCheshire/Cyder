package cyder.threads;

/**
 * Thread names to ignore when determining if Cyder should be classified as busy.
 */
public enum IgnoreThread {
    CyderBusyChecker("Cyder Busy Checker"),
    AwtEventQueue0("AWT-EventQueue-0"),
    ConsoleClockUpdater("Console Clock Updater"),
    HourlyChimeChecker("Hourly Chime Checker"),
    HighPingChecker("High Ping Checker"),
    DestroyJavaVm("DestroyJavaVM"),
    ConsolePrintingAnimation("Console Printing Animation"),
    SingularInstanceEnsurer("Singular Cyder Instance Ensurer"),
    ObjectCreationLogger("Object Creation Logger"),
    CyderWatchdog("Cyder Watchdog"),
    WatchdogInitializer("Watchdog Initializer"),
    Backend("Backend");

    /**
     * The name associated with the thread to ignore.
     */
    private final String name;

    /**
     * Constructs a new thread to ignore when counting worker threads.
     *
     * @param name the name of the thread to ignore
     */
    IgnoreThread(String name) {
        this.name = name;
    }

    /**
     * Returns the name associated with this ignore thread.
     *
     * @return the name associated with this ignore thread
     */
    public String getName() {
        return name;
    }
}
