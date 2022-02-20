package cyder.enums;

public enum IgnoreData {
    TypingAnimation("typinganimation"),
    ShowSeconds("showseconds"),
    RoundedWindows("roundedwindows"),
    WindowColor("windowcolor"),
    AudioLength("audiolength"),
    CapsMode("capsmode"),
    TypingSound("typingsound"),
    ShowBusyIcon("showbusyicon");

    /**
     * The ID associated with the user data to not log upon access calls.
     */
    private final String id;

    /**
     * Constructs a new ignore data.
     *
     * @param id the id of the data
     */
    IgnoreData(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
