package cyder.snakes;

import com.google.common.base.Preconditions;

/** Supported commands for the python_functions script. */
public enum PythonCommand {
    BLUR("blur", "Blurred: "),
    AUDIO_LENGTH("audio_length", "Audio length: ");

    /** The command for this python functions command. */
    private final String command;

    /**
     * The response prefix for this command to indicate the operation
     * was successful and communicate any necessary information.
     */
    private final String responsePrefix;

    PythonCommand(String command, String responsePrefix) {
        this.command = command;
        this.responsePrefix = responsePrefix;
    }

    /**
     * Returns the command.
     *
     * @return the command
     */
    public String getCommand() {
        return command;
    }

    /**
     * Returns the response prefix for this command.
     *
     * @return the response prefix for this command
     */
    public String getResponsePrefix() {
        return responsePrefix;
    }

    /**
     * Parses the command response provided if valid.
     *
     * @param response the response from the functions script.
     * @return the python functions script response
     */
    public String parseResponse(String response) {
        Preconditions.checkNotNull(response);
        Preconditions.checkArgument(!response.isEmpty());
        Preconditions.checkArgument(response.startsWith(responsePrefix));

        return response.substring(responsePrefix.length()).trim();
    }
}
