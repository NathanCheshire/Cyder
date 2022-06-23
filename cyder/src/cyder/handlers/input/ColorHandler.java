package cyder.handlers.input;

import cyder.annotations.Handle;
import cyder.constants.CyderColors;
import cyder.constants.CyderStrings;
import cyder.enums.Dynamic;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.ConsoleFrame;
import cyder.handlers.internal.ExceptionHandler;
import cyder.ui.CyderCaret;
import cyder.user.Preferences;
import cyder.user.UserFile;
import cyder.utils.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * A handler for commands which change color/font throughout Cyder.
 */
public class ColorHandler extends InputHandler {
    /**
     * Suppress default constructor
     */
    private ColorHandler() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    @Handle({"backgroundcolor", "fixforeground", "foreground", "repaint"})
    public static boolean handle() {
        boolean ret = true;

        if (getInputHandler().commandIs("backgroundcolor")) {
            if (getInputHandler().checkArgsLength(1)) {
                try {
                    int w = ConsoleFrame.INSTANCE.getConsoleCyderFrame().getWidth();
                    int h = ConsoleFrame.INSTANCE.getConsoleCyderFrame().getHeight();

                    if (UserUtil.getCyderUser().getFullscreen().equals("1")) {
                        w = ScreenUtil.getScreenWidth();
                        h = ScreenUtil.getScreenHeight();
                    }

                    BufferedImage saveImage = ImageUtil.bufferedImageFromColor(
                            Color.decode("#" + getInputHandler().getArg(0)
                                    .replace("#", "")), w, h);

                    String saveName = "Solid_" + getInputHandler().getArg(0) + "Generated_Background.png";

                    File saveFile = OSUtil.buildFile(Dynamic.PATH, "users",
                            ConsoleFrame.INSTANCE.getUUID(), UserFile.BACKGROUNDS.getName(), saveName);

                    ImageIO.write(saveImage, "png", saveFile);

                    getInputHandler().println("Background generated, set, and saved as a separate background file.");

                    ConsoleFrame.INSTANCE.setBackgroundFile(saveFile);
                } catch (Exception e) {
                    getInputHandler().println("Background color command usage: backgroundcolor EC407A");
                    ExceptionHandler.silentHandle(e);
                }
            } else {
                getInputHandler().println("Background color command usage: backgroundcolor EC407A");
            }
        } else if (getInputHandler().commandIs("fixforeground")) {
            try {
                Color backgroundDominantColor = ColorUtil.getDominantColor(ImageIO.read(
                        ConsoleFrame.INSTANCE.getCurrentBackground().referenceFile()));

                if (shouldUseLightColor(backgroundDominantColor)) {
                    ConsoleFrame.INSTANCE.getOutputArea().setForeground(CyderColors.defaultLightModeTextColor);
                    ConsoleFrame.INSTANCE.getInputField().setForeground(CyderColors.defaultLightModeTextColor);
                    ConsoleFrame.INSTANCE.getInputField().setCaretColor(CyderColors.defaultLightModeTextColor);
                    ConsoleFrame.INSTANCE.getInputField()
                            .setCaret(new CyderCaret(CyderColors.defaultLightModeTextColor));
                    UserUtil.getCyderUser()
                            .setForeground(ColorUtil.rgbToHexString(CyderColors.defaultLightModeTextColor));
                } else {
                    ConsoleFrame.INSTANCE.getOutputArea().setForeground(CyderColors.defaultDarkModeTextColor);
                    ConsoleFrame.INSTANCE.getInputField().setForeground(CyderColors.defaultDarkModeTextColor);
                    ConsoleFrame.INSTANCE.getInputField().setCaretColor(CyderColors.defaultDarkModeTextColor);
                    ConsoleFrame.INSTANCE.getInputField()
                            .setCaret(new CyderCaret(CyderColors.defaultDarkModeTextColor));
                    UserUtil.getCyderUser()
                            .setForeground(ColorUtil.rgbToHexString(CyderColors.defaultDarkModeTextColor));
                }

                Preferences.invokeRefresh("foreground");
                getInputHandler().println("Foreground fixed");
            } catch (Exception e) {
                ExceptionHandler.handle(e);
                ret = false;
            }
        } else if (getInputHandler().commandIs("repaint")) {
            ConsoleFrame.INSTANCE.revalidate(false, false);
            getInputHandler().println("ConsoleFrame repainted");
        } else {
            ret = false;
        }

        return ret;
    }

    /**
     * Returns whether the text color to be layered over the
     * provided background color should be a light mode color.
     *
     * @param backgroundColor the background color to find a suitable text color for
     * @return whether the text color should be a light mode color
     */
    private static boolean shouldUseLightColor(Color backgroundColor) {
        return (backgroundColor.getRed() * 0.299 + backgroundColor.getGreen()
                * 0.587 + backgroundColor.getBlue() * 0.114) > 186;
    }
}
