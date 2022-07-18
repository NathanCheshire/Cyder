package cyder.enums;

public enum Suggestion {
    Pathfinder("pathfinder", "open an A* and Dijkstra's algorithm path finding visualizer"),
    Math("e^pi", "calculate e^pi using the command line mathematical expression parser"),
    Audio("mp3", "open Cyder's custom audio player"),
    Prefs("prefs", "open up the preference editor"),
    Weather("weather", "open up the weather widget"),
    Curl("curl", "curl a url just like linux"),
    Play("play", "downloads a youtube video's audio and plays it using the audio player"),
    Conway("Conway's Game of Life", "Conway's game of life re-created in Cyder");

    /**
     * The trigger for the command for this suggestion.
     */
    private final String command;

    /**
     * The description of this command
     */
    private final String description;

    /**
     * Constructs a new Suggestion from the provided arguments.
     *
     * @param command     the command which will trigger the suggestion
     * @param description a description of what the trigger results in
     */
    Suggestion(String command, String description) {
        this.command = command;
        this.description = description;
    }

    /**
     * Returns the command for this suggestion.
     *
     * @return the command for this suggestion
     */
    public String getCommand() {
        return command;
    }

    /**
     * Returns the description for this suggestion.
     *
     * @return the description for this suggestion
     */
    public String getDescription() {
        return description;
    }
}
