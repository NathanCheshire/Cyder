package cyder.handler;

public class InputHandler {
    //todo this class will be associated with a specific instance of ConsoleFrame to handle all input
    // and direct the resulting flow and output

    //todo be able to set super title and regular title differnetly and get/set each different
    // window title should only be for display

    //todo save coordinates when entering fullscreen to set to when exit if occurs in this session

    //todo invalid weather location throws error

    //todo work on proper exit conditions for ALL threads

    //todo make input field like the input field for login (actually a password field with the natche@cyder~$)

    //todo if cyder hasn't started in a while, say welcome back $username, did you miss me?
    // or some variation of that

    //todo fix preferences panel with CyderSliders

    //todo control an alt + f4 with a key mask so that the exit code is 25 and the program properly closes
    // alt + f4 should be forced immediate exit?

    //TODO all icons for ConsoleFrame should be drawn on the spot and not use a png in files

    //todo logout should end all executors and threads so only thing happening is login frame

    //todo fix bug: log in as different user when already logged in as nathan and then delete this new account you logged into
    // will say the first user was corrupted and say sorry + "first user name"

    //todo do away with user throws
    //todo make chat logs to replace throws and suggestions and general log like miencraft, store in
    // same dir as src and call logs

    //todo I don't want to see a UUID, minimize usage of it

    //todo trying to open weather.png from dir search bugs out, maybe resize error in photo viewer
    // fix this anyway by using a gradient and drawing most icons you have in pictures

    //todo killed boolean for ALL objects with threads to end their threads when disposed? Copy from ConsoleFrame.dispose()

    //todo pixelating background resets consoleFrame to center
    //todo console switching is too slow

    //todo restoreX and restoreY should be initialized to h/2 - consoleframeheight/2 and same for width
    // also change it on frame location set
    //todo do this for CyderFrame too, will need to play with DragLabel getters and setters

    //todo hangman is broken

    //todo convert user data to bin data

    //todo preference slider like from ThinMatrix's games, slide from left to right and change colors for on and reverse for off

    //todo be able to copy from outupt area without canceling using ctrl + c, check if selected text or not

    //todo photoviewer needs more consistnet window size and it moves the window to center every time, keep relative

    //todo fipping console resets output area position

    //todo adding a background with the same name throws error

    @Override
    public String toString() {
        return "InputHandler object (I guess this is just the todo field though... *sad pony noises*), hash=" + this.hashCode();
    }
}
