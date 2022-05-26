package cyder.handlers.input;

import com.google.common.collect.Range;
import cyder.annotations.Handle;
import cyder.constants.CyderStrings;
import cyder.enums.DynamicDirectory;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.ConsoleFrame;
import cyder.handlers.internal.ExceptionHandler;
import cyder.user.UserFile;
import cyder.utilities.FileUtil;
import cyder.utilities.ImageUtil;
import cyder.utilities.OSUtil;
import cyder.utilities.UserUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * A handler for handling when images or the console background should be pixelated.
 */
public class PixelationHandler implements Handleable {
    /**
     * Suppress default constructor.
     */
    private PixelationHandler() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    /**
     * The range of allowable user-entered pixelation values.
     */
    private static final Range<Integer> pixelRange = Range.closed(2, 500);

    @Override
    @Handle({"pixelate", "pixelation"})
    public boolean handle() {
        switch (ConsoleFrame.INSTANCE.getInputHandler().getHandleIterations()) {
            case 0 -> {
                if (ImageUtil.solidColor(ConsoleFrame.INSTANCE.getCurrentBackground().getReferenceFile())) {
                    getInputHandler().println("Silly " + UserUtil.getCyderUser().getName()
                            + "; your background " + "is a solid color :P");
                } else {
                    if (checkArgsLength(1)) {
                        try {
                            int size = Integer.parseInt(getArg(0));
                            attemptPixelation(size);
                        } catch (Exception e) {
                            ExceptionHandler.handle(e);
                            getInputHandler().println("Could not parse argument as an integer");

                            getInputHandler().resetHandlers();
                        }
                    } else {
                        getInputHandler().setRedirectionHandler(this);
                        getInputHandler().setHandleIterations(1);
                        getInputHandler().println("Enter pixel size");
                    }
                }

                return true;
            }
            case 1 -> {
                try {
                    int size = Integer.parseInt(getArg(0));
                    attemptPixelation(size);
                } catch (Exception ignored) {
                    getInputHandler().println("Could not parse input as an integer");
                }

                getInputHandler().resetHandlers();
                return true;
            }
            default -> throw new IllegalArgumentException(
                    "Illegal handle index for handler: " + this.getClass().getName());
        }
    }

    /**
     * Attempts to pixelate the ConsoleFrame background if the provided size is within the allowable range.
     *
     * @param size the requested pixel size
     */
    private void attemptPixelation(int size) {
        if (pixelRange.contains(size)) {
            try {
                BufferedImage img = ImageUtil.pixelate(ImageIO.read(ConsoleFrame.INSTANCE.
                        getCurrentBackground().getReferenceFile().getAbsoluteFile()), size);

                String newName = FileUtil.getFilename(ConsoleFrame.INSTANCE
                        .getCurrentBackground().getReferenceFile().getName())
                        + "_Pixelated_Pixel_Size_" + size + ".png";

                File saveFile = OSUtil.buildFile(DynamicDirectory.DYNAMIC_PATH, "users",
                        ConsoleFrame.INSTANCE.getUUID(), UserFile.BACKGROUNDS.getName(), newName);

                ImageIO.write(img, "png", saveFile);

                getInputHandler().println("Background pixelated and saved as a separate background file.");

                ConsoleFrame.INSTANCE.setBackgroundFile(saveFile);
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        } else {
            getInputHandler().println("Sorry, " + UserUtil.getCyderUser().getName()
                    + ", but your pixel value must be in the range ["
                    + pixelRange.lowerEndpoint() + ", " + pixelRange.upperEndpoint() + "]");
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
