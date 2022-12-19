package main.java.cyder.handlers.input;

import com.google.common.collect.Range;
import main.java.cyder.annotations.Handle;
import main.java.cyder.console.Console;
import main.java.cyder.enums.Dynamic;
import main.java.cyder.enums.Extension;
import main.java.cyder.exceptions.IllegalMethodException;
import main.java.cyder.files.FileUtil;
import main.java.cyder.handlers.internal.ExceptionHandler;
import main.java.cyder.strings.CyderStrings;
import main.java.cyder.threads.CyderThreadRunner;
import main.java.cyder.user.UserFile;
import main.java.cyder.user.UserUtil;
import main.java.cyder.utils.ImageUtil;

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
                boolean isSolidColor = false;

                try {
                    isSolidColor = ImageUtil.isSolidColor(Console.INSTANCE.getCurrentBackground().getReferenceFile());
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }

                if (isSolidColor) {
                    getInputHandler().println("Silly " + UserUtil.getCyderUser().getName()
                            + "; your background " + "is a solid color :P");
                } else {
                    if (getInputHandler().checkArgsLength(1)) {
                        try {
                            int size = Integer.parseInt(getInputHandler().getArg(0));
                            attemptPixelation(size);
                        } catch (Exception e) {
                            return false;
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
     * Attempts to pixelate the Console background if the provided size is within the allowable range.
     *
     * @param size the requested pixel size
     */
    private static void attemptPixelation(int size) {
        if (pixelRange.contains(size)) {
            CyderThreadRunner.submit(() -> {
                try {
                    BufferedImage img = ImageUtil.pixelateImage(ImageUtil.read(Console.INSTANCE.
                            getCurrentBackground().getReferenceFile().getAbsoluteFile()), size);

                    String newName = FileUtil.getFilename(Console.INSTANCE
                            .getCurrentBackground().getReferenceFile().getName())
                            + "_Pixelated_Pixel_Size_" + size + Extension.PNG.getExtension();

                    File saveFile = Dynamic.buildDynamic(
                            Dynamic.USERS.getFileName(),
                            Console.INSTANCE.getUuid(),
                            UserFile.BACKGROUNDS.getName(), newName);

                    ImageIO.write(img, Extension.PNG.getExtensionWithoutPeriod(), saveFile);

                    getInputHandler().println("Background pixelated and saved as a separate background file.");

                    Console.INSTANCE.setBackgroundFile(saveFile);
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }
            }, "Console Background Pixelator");
        } else {
            getInputHandler().println("Sorry, " + UserUtil.getCyderUser().getName()
                    + ", but your pixel value must be in the range ["
                    + pixelRange.lowerEndpoint() + ", " + pixelRange.upperEndpoint() + CyderStrings.closingBracket);
        }

        getInputHandler().resetHandlers();
    }
}
