package cyder.handlers.input;

import cyder.annotations.Handle;
import cyder.console.Console;
import cyder.enums.Dynamic;
import cyder.enums.Extension;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.strings.CyderStrings;
import cyder.threads.CyderThreadRunner;
import cyder.user.UserFile;
import cyder.user.UserUtil;
import cyder.utils.ImageUtil;
import cyder.utils.SecurityUtil;
import cyder.utils.SpotlightUtil;
import cyder.utils.StaticUtil;

import javax.swing.*;
import java.io.File;
import java.util.Optional;
import java.util.concurrent.Future;

/**
 * A handler for images and console background manipulation
 */
public class ImageHandler extends InputHandler {
    /**
     * Suppress default constructor.
     */
    private ImageHandler() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    @Handle({"java", "msu", "nathan", "nate", "html", "css", "docker", "redis", "blur", "unicorn", "spotlight"})
    public static boolean handle() {
        boolean ret = true;

        if (getInputHandler().commandIs("java")) {
            getInputHandler().println(new ImageIcon("static/pictures/print/duke.png"));
        } else if (getInputHandler().commandIs("msu")) {
            getInputHandler().println(new ImageIcon("static/pictures/print/msu.png"));
        } else if (getInputHandler().commandIs("nathan") || getInputHandler().commandIs("nate")) {
            getInputHandler().println(new ImageIcon("static/pictures/print/me.png"));
        } else if (getInputHandler().commandIs("html")) {
            getInputHandler().println(new ImageIcon("static/pictures/print/html5.png"));
        } else if (getInputHandler().commandIs("css")) {
            getInputHandler().println(new ImageIcon("static/pictures/print/css.png"));
        } else if (getInputHandler().commandIs("docker")) {
            getInputHandler().println(new ImageIcon("static/pictures/print/Docker.png"));
        } else if (getInputHandler().commandIs("redis")) {
            getInputHandler().println(new ImageIcon("static/pictures/print/Redis.png"));
        } else if (getInputHandler().commandIs("blur")) {
            if (getInputHandler().checkArgsLength(1)) {
                if (ImageUtil.isSolidColor(Console.INSTANCE.getCurrentBackground().generateBufferedImage())) {
                    getInputHandler().println("Silly " + UserUtil.getCyderUser().getName()
                            + ". Your background is a solid color, bluing that won't do anything :P");
                }

                attemptToBlurBackground();
            } else {
                getInputHandler().println("Blur command usage: blur [GAUSSIAN BLUR RADIUS]");
            }
        } else if (getInputHandler().commandIs("unicorn")) {
            getInputHandler().println(new ImageIcon(StaticUtil.getStaticPath("unicorn.png")));
        } else if (getInputHandler().inputIgnoringSpacesMatches("spotlight")) {
            CyderThreadRunner.submit(() -> {
                getInputHandler().println("Saving backgrounds to your backgrounds directory...");
                SpotlightUtil.saveSpotlights(Dynamic.buildDynamic(Dynamic.USERS.getDirectoryName(),
                        Console.INSTANCE.getUuid(),
                        UserFile.BACKGROUNDS.getName()));
                getInputHandler().println("Saved images to your backgrounds directory");
            }, spotlightStealerThreadName);
        } else {
            ret = false;
        }

        return ret;
    }

    /**
     * The name of the thread which saves the spotlights to the current user's backgrounds directory.
     */
    private static final String spotlightStealerThreadName = "Spotlight Saver";

    /**
     * The minimum allowable radius when blurring the background.
     */
    private static final int MIN_BLUR_SIZE = 3;

    /**
     * The name of the thread that attempts to blur the current background.
     */
    private static final String BACKGROUND_BLUR_ATTEMPT_THREAD_NAME = "Background Blur Attempt Thread";

    /**
     * Attempts to validate a blur command and if valid, blur the current console background.
     */
    private static void attemptToBlurBackground() {
        CyderThreadRunner.submit(() -> {
            try {
                int radius = Integer.parseInt(getInputHandler().getArg(0));
                boolean isEven = radius % 2 == 0;

                if (isEven) {
                    getInputHandler().println("Blur radius must be an odd number");
                    return;
                } else if (radius < MIN_BLUR_SIZE) {
                    getInputHandler().println("Minimum blur radius is " + MIN_BLUR_SIZE);
                    return;
                }

                File currentBackgroundFile = Console.INSTANCE.getCurrentBackground().getReferenceFile();

                if (currentBackgroundFile == null || !currentBackgroundFile.exists()) {
                    String name = SecurityUtil.generateUuid();
                    boolean saved = ImageUtil.saveImageToTemporaryDirectory(Console.INSTANCE
                            .getCurrentBackground().generateBufferedImage(), name);

                    if (!saved) {
                        getInputHandler().println("Could not blur background at this time");
                        return;
                    }

                    currentBackgroundFile = Dynamic.buildDynamic(
                            Dynamic.TEMP.getDirectoryName(), name + Extension.PNG.getExtension());
                }

                Future<Optional<File>> futureImage = ImageUtil.gaussianBlur(currentBackgroundFile, radius);
                while (!futureImage.isDone()) Thread.onSpinWait();

                if (futureImage.get().isPresent()) {
                    Console.INSTANCE.setBackgroundFile(futureImage.get().get(), true);
                    getInputHandler().println("Background blurred, set, and saved as a separate background file.");
                } else {
                    getInputHandler().println("Could not blur background at this time");
                }
            } catch (NumberFormatException ignored) {
                getInputHandler().println("Invalid input for radius: " + getInputHandler().getArg(0));
            } catch (Exception e) {
                getInputHandler().println("Blur command usage: blur [GAUSSIAN BLUR RADIUS]");
                ExceptionHandler.handle(e);
            }
        }, BACKGROUND_BLUR_ATTEMPT_THREAD_NAME);
    }
}
