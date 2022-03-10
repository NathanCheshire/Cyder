package cyder.enums;

public enum IgnoreThread {
    CyderBusyChecker("Cyder Busy Checker"),
    AwtEventQueue0("AWT-EventQueue-0"),
    ConsoleClockUpdator("Console Clock Updater"),
    HourlyChimeChecker("Hourly Chime Checker"),
    HighPingChecker("High Ping Checker"),
    DestroyJavaVm("DestroyJavaVM"),
    ConsoleInputCaret("Console Input Caret Position Updater"),
    ConsolePrintingAnimation("Console Printing Animation"),
    ConsoleDataSaver("ConsoleFrame Stat Saver"),
    SingularInstanceEnsurer("Singular Cyder Instance Ensurer"),
    SimilarCommandFinder("Similar Command Finder"),
    GitRepoCloner("Git Repo Cloner"),
    YoutubeAudioExtractor("Youtube Audio Extractor");

    /**
     * The name associated with the thread to ignore.
     */
    private final String name;

    /**
     * Constrcuts a new thread to ignore when counting worker threads.
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
