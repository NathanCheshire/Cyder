package cyder.handlers.input;

import com.google.common.collect.Range;
import cyder.annotations.Handle;
import cyder.constants.CyderStrings;
import cyder.enums.Dynamic;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.ConsoleFrame;
import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.CyderThreadRunner;
import cyder.user.UserFile;
import cyder.utils.FileUtil;
import cyder.utils.ImageUtil;
import cyder.utils.OSUtil;
import cyder.utils.UserUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * A handler for handling when images or the console background should be pixelated.
 */
public class PixelationHandler extends InputHandler {
    /**
     * Suppress default constructor.
     */
    private PixelationHandler() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * The range of allowable user-entered pixelation values.
     */
    private static final Range<Integer> pixelRange = Range.closed(2, 500);

    @Handle({"pixelate", "pixelation"})
    public static boolean handle() {
        switch (getInputHandler().getHandleIterations()) {
            case 0 -> {
                if (ImageUtil.solidColor(ConsoleFrame.INSTANCE.getCurrentBackground().referenceFile())) {
                    getInputHandler().println("Silly " + UserUtil.getCyderUser().getName()
                            + "; your background " + "is a solid color :P");
                } else {
                    if (getInputHandler().checkArgsLength(1)) {
                        try {
                            int size = Integer.parseInt(getInputHandler().getArg(0));
                            attemptPixelation(size);
                        } catch (Exception e) {
                            ExceptionHandler.handle(e);
                            getInputHandler().println("Could not parse argument as an integer");

                            getInputHandler().resetHandlers();
                        }
                    } else {
                        getInputHandler().setRedirectionHandler(PixelationHandler.class);
                        getInputHandler().setHandleIterations(1);
                        getInputHandler().println("Enter pixel size");
                    }
                }

                return true;
            }
            case 1 -> {
                try {
                    int size = Integer.parseInt(getInputHandler().getCommand());
                    attemptPixelation(size);
                } catch (Exception ignored) {
                    getInputHandler().println("Could not parse input as an integer");
                }

                return true;
            }
            default -> throw new IllegalArgumentException("Illegal handle index for pixelation handler");
        }
    }

    /**
     * Attempts to pixelate the ConsoleFrame background if the provided size is within the allowable range.
     *
     * @param size the requested pixel size
     */
    private static void attemptPixelation(int size) {
        if (pixelRange.contains(size)) {
            CyderThreadRunner.submit(() -> {
                try {
                    BufferedImage img = ImageUtil.pixelate(ImageIO.read(ConsoleFrame.INSTANCE.
                            getCurrentBackground().referenceFile().getAbsoluteFile()), size);

                    String newName = FileUtil.getFilename(ConsoleFrame.INSTANCE
                            .getCurrentBackground().referenceFile().getName())
                            + "_Pixelated_Pixel_Size_" + size + ".png";

                    File saveFile = OSUtil.buildFile(Dynamic.PATH, "users",
                            ConsoleFrame.INSTANCE.getUUID(), UserFile.BACKGROUNDS.getName(), newName);

                    ImageIO.write(img, "png", saveFile);

                    getInputHandler().println("Background pixelated and saved as a separate background file.");

                    ConsoleFrame.INSTANCE.setBackgroundFile(saveFile);
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }
            }, "Console Background Pixelator");
        } else {
            getInputHandler().println("Sorry, " + UserUtil.getCyderUser().getName()
                    + ", but your pixel value must be in the range ["
                    + pixelRange.lowerEndpoint() + ", " + pixelRange.upperEndpoint() + "]");
        }

        getInputHandler().resetHandlers();
    }
}
