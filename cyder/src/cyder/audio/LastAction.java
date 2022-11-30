package cyder.audio;

/** Possible ways a user can interact with the audio player. */
enum LastAction {
    /** The user pressed play. */
    Play,

    /** The user pressed skip back or skip forward. */
    Skip,

    /** The audio was skipped */
    Pause,

    /** The user changed the audio location. */
    Scrub,

    /** An audio file was chosen using the file chooser menu option. */
    FileChosen,

    /** Something else not yet handled. */
    Unknown,
}
