package cyder.handler;

public class InputHandler {
    //todo this class will be associated with a specific instance of ConsoleFrame to handle all input
    // and direct the resulting flow and output

    //todo scrollbar on dir messes up when selecting, same with prefs, fix by making not opqeue or not fading away?

    //todo test signatures against files and make a widget for this

    //todo file space for overall program when cypherlenovo, deletes folder if not cypherlenovo
    // todo userfile space too, (be able to point to files with a -f option via command line args such as)
    // todo hexdump userdata.bin
    // todo bindump userdata.bin

    //todo use displayText = "<html><div style='text-align: center;'>" + displayText + "</div></html>";
    // to center text for components throughout.

    //todo bin dump should dump user's binary data to console and ide console
    //todo hex dump should dump user's binary data in hex format to console and ide console

    //todo make bletchy have mandarin and random unicode chars if toggled on, make it not reset output area

    //todo setting to fullscreen breaks lol, I assume since refreshConsoleframe isn't setting consoleframe bounds anyomore,
    // put it back in

    //todo be able to set super title and regular title differnetly and get/set each different
    // window title should only be for display

    //todo background image changing doesn't move just the background

    //todo do background images change on alternate background event? look in file explorer

    //todo save coordinates when entering fullscreen to set to when exit if occurs in this session

    //todo work on proper exit conditions for ALL threads

    //todo make input field like the input field for login (actually a password field with the natche@cyder~$)

    //todo if cyder hasn't started in a while, say welcome back $username, did you miss me?
    // or some variation of that

    //todo fix preferences panel with CyderSliders

    //todo logout should end all executors and threads so only thing happening is login frame

    //todo fix bug: log in as different user when already logged in as nathan and then delete this new account you logged into
    // will say the first user was corrupted and say sorry + "first user name"

    //todo make chat logs to replace throws and suggestions and general log like miencraft, store in
    // same dir as src and call logs

    //todo I don't want to see a UUID, minimize usage of it

    //todo trying to open weather.png from dir search bugs out, maybe resize error in photo viewer
    // fix this anyway by using a gradient and drawing most icons you have in pictures

    //todo killed boolean for ALL objects with threads to end their threads when disposed? Copy from ConsoleFrame.dispose()

    //todo preference slider like from ThinMatrix's games, slide from left to right and change colors for on and reverse for off

    //todo be able to copy from outupt area without canceling using ctrl + c, check if selected text or not

    //todo photoviewer needs more consistnet window size and it moves the window to center every time, keep relative

    //todo fipping console resets output area position

    @Override
    public String toString() {
        return "InputHandler object (I guess this is just the todo field though... *sad pony noises*), hash=" + this.hashCode();
    }
}
