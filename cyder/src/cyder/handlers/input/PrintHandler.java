package cyder.handlers.input;

public class PrintHandler implements Handler {
    @Override
    public <T> boolean handle(T[] tees, boolean userTriggered) {


        return false;
    }

    @Override
    public String getLogMessage() {
        return "General print handler succeeded";
    }
}
