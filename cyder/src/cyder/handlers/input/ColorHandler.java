package cyder.handlers.input;

import cyder.annotations.Handle;
import cyder.console.Console;
import cyder.constants.CyderColors;
import cyder.enums.Dynamic;
import cyder.enums.Extension;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.strings.CyderStrings;
import cyder.ui.field.CyderCaret;
import cyder.user.Preference;
import cyder.user.UserFile;
import cyder.user.UserUtil;
import cyder.utils.ColorUtil;
import cyder.utils.ImageUtil;

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

    @Handle({"backgroundcolor", "fix foreground", "foreground", "repaint"})
    public static boolean handle() {
        boolean ret = true;

        if (getInputHandler().inputIgnoringSpacesAndCaseStartsWith("backgroundcolor")) {
            if (getInputHandler().checkArgsLength(1)) {
                try {
                    int w = Console.INSTANCE.getConsoleCyderFrame().getWidth();
                    int h = Console.INSTANCE.getConsoleCyderFrame().getHeight();

                    if (UserUtil.getCyderUser().getFullscreen().equals("1")) {
                        Rectangle monitorBounds = Console.INSTANCE.getConsoleCyderFrame()
                                .getMonitorBounds().getBounds();

                        w = (int) monitorBounds.getWidth();
                        h = (int) monitorBounds.getHeight();
                    }

                    String colorString = getInputHandler().getArg(0);
                    Color color = ColorUtil.hexStringToColor(colorString);
                    BufferedImage saveImage = ImageUtil.bufferedImageFromColor(color, w, h);

                    String saveName = "Solid_" + getInputHandler().getArg(0)
                            + "_Background" + Extension.PNG.getExtension();

                    File saveFile = Dynamic.buildDynamic(Dynamic.USERS.getDirectoryName(),
                            Console.INSTANCE.getUuid(), UserFile.BACKGROUNDS.getName(), saveName);

                    ImageIO.write(saveImage, Extension.PNG.getExtensionWithoutPeriod(), saveFile);

                    getInputHandler().println("Background generated, set, and saved as a separate background file.");

                    Console.INSTANCE.setBackgroundFile(saveFile);
                } catch (Exception ignored) {
                    getInputHandler().println("Background color command usage: backgroundcolor EC407A");
                }
            } else {
                getInputHandler().println("Background color command usage: backgroundcolor EC407A");
            }
        } else if (getInputHandler().inputIgnoringSpacesMatches("fixforeground")) {
            try {
                Color backgroundDominantColor = ColorUtil.getDominantColor(ImageUtil.read(
                        Console.INSTANCE.getCurrentBackground().getReferenceFile()));

                if (shouldUseLightColor(backgroundDominantColor)) {
                    Console.INSTANCE.getOutputArea().setForeground(CyderColors.defaultLightModeTextColor);
                    Console.INSTANCE.getInputField().setForeground(CyderColors.defaultLightModeTextColor);
                    Console.INSTANCE.getInputField().setCaretColor(CyderColors.defaultLightModeTextColor);
                    Console.INSTANCE.getInputField()
                            .setCaret(new CyderCaret(CyderColors.defaultLightModeTextColor));
                    UserUtil.getCyderUser()
                            .setForeground(ColorUtil.rgbToHexString(CyderColors.defaultLightModeTextColor));
                } else {
                    Console.INSTANCE.getOutputArea().setForeground(CyderColors.defaultDarkModeTextColor);
                    Console.INSTANCE.getInputField().setForeground(CyderColors.defaultDarkModeTextColor);
                    Console.INSTANCE.getInputField().setCaretColor(CyderColors.defaultDarkModeTextColor);
                    Console.INSTANCE.getInputField()
                            .setCaret(new CyderCaret(CyderColors.defaultDarkModeTextColor));
                    UserUtil.getCyderUser()
                            .setForeground(ColorUtil.rgbToHexString(CyderColors.defaultDarkModeTextColor));
                }

                Preference.invokeRefresh(Preference.FOREGROUND);
                getInputHandler().println("Foreground fixed");
            } catch (Exception e) {
                ExceptionHandler.handle(e);
                ret = false;
            }
        } else if (getInputHandler().commandIs("repaint")) {
            Console.INSTANCE.revalidate(false, false);
            getInputHandler().println("Console repainted");
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
