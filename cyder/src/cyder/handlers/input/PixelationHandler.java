package cyder.handlers.input;

import cyder.annotations.Handle;
import cyder.handlers.ConsoleFrame;

public class PixelationHandler implements Handleable {

    @Override
    @Handle({"pixelate", "pixelation"})
    public boolean handle() {
        switch (ConsoleFrame.INSTANCE.getInputHandler().getHandleIterations()) {
            case 0 -> {
                ConsoleFrame.INSTANCE.getInputHandler().setRedirectionHandler(this);
                ConsoleFrame.INSTANCE.getInputHandler().setHandleIterations(1);
                ConsoleFrame.INSTANCE.getInputHandler().println("Enter pixel size");
                return true;
            }
            case 1 -> {
                try {
                    int size = Integer.parseInt("todo get thing");
                } catch (Exception ignored) {
                    ConsoleFrame.INSTANCE.getInputHandler().println("Could not parse input as an integer");
                }
                return true;
            }
            default -> throw new IllegalArgumentException(
                    "Illegal handle index for handler: " + this.getClass().getName());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLogMessage() {
        return "Pixelation handler succeeded";
    }
}
