package cyder.handler;

public class InputHandler {
    //todo this class will be associated with a specific instance of ConsoleFrame to handle all input
    // and direct the resulting flow and output

    //todo work on proper exit conditions for ALL threads

    //todo make input field like the input field for login (actually a password field with the natche@cyder~$)

    //todo if cyder hasn't started in a while, say welcome back $username, did you miss me?
    // or some variation of that

    //todo rick and morty references

    //todo redo pref check section to allow for more prefs added easily
    //todo toggle chat filter on and off

    //TODO make testing easier with a testing widget, when in debug mode, this automatically opens up

    //TODO all icons for ConsoleFrame should be drawn on the spot and not use a png in files

    //todo start log got reset somehow, probably from error being thrown while re-writing
    // change to adding new lines to bottom instead
    //todo hide location if not relased (in debug mode)

    //todo logout should end all executors and threads so only thing happening is login frame

    //todo at end of if statements before saying unrecognized input, try parsing it as a mathematical expression

    //todo fix bug: log in as different user when already logged in as nathan and then delete this new account you logged into
    // will say the first user was corrupted and say sorry + "first user name"

    //todo corrupted user is disabled until I think of a better check so it rarely fires

    //todo do away with user throws
    //todo make chat logs to replace throws and suggestions and general log like miencraft, store in
    // same dir as src and call logs

    //todo I don't want to see a UUID, minimize usage of it

    //todo trying to open weather.png from dir search bugs out, maybe resize error in photo viewer
    // fix this anyway by using a gradient and drawing most icons you have in pictures

    //todo fix up animation util

    //todo killed boolean for ALL objects with threads to end their threads when disposed? Copy from ConsoleFrame.dispose()
}
