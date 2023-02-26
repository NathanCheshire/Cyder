package cyder.handlers.input;

import com.google.common.collect.ImmutableList;
import cyder.annotations.Handle;
import cyder.console.Console;
import cyder.constants.CyderColors;
import cyder.enumerations.ExitCondition;
import cyder.exceptions.IllegalMethodException;
import cyder.strings.CyderStrings;
import cyder.ui.UiUtil;
import cyder.ui.frame.CyderFrame;
import cyder.ui.slider.CyderSliderUi;
import cyder.ui.slider.ThumbShape;
import cyder.user.UserDataManager;
import cyder.user.creation.UserCreator;
import cyder.utils.OsUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.stream.IntStream;

/**
 * A handler for handling things related to the ui and painting.
 */
public class UiHandler extends InputHandler {
    /**
     * The slider used to change the opacity of the Console.
     */
    private static JSlider opacitySlider;

    /**
     * Suppress default constructor.
     */
    private UiHandler() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    @Handle({"toast", "opacity", "original chams", "screenshot frames", "monitors",
            "create user", "panic", "quit", "logout", "clear clipboard", "mouse", "frames", "freeze"})
    public static boolean handle() {
        boolean ret = true;

        if (getInputHandler().commandIs("toast")) {
            Console.INSTANCE.getConsoleCyderFrame().toast("A toast to you, my liege");
        } else if (getInputHandler().commandIs("freeze")) {
            //noinspection StatementWithEmptyBody
            while (true) {}
        } else if (getInputHandler().commandIs("opacity")) {
            if (opacitySlider == null) initializeOpacitySlider();
            getInputHandler().println(opacitySlider);
        } else if (getInputHandler().inputIgnoringSpacesMatches("original chams")) {
            Console.INSTANCE.originalChams();
        } else if (getInputHandler().inputIgnoringSpacesMatches("screenshot frames")) {
            UiUtil.screenshotCyderFrames();
            getInputHandler().println("Successfully saved to your Files directory");
        } else if (getInputHandler().commandIs("monitors")) {
            StringBuilder printString = new StringBuilder("Monitor display modes: ").append(CyderStrings.newline);
            ImmutableList<DisplayMode> modes = UiUtil.getMonitorDisplayModes();
            IntStream.range(0, modes.size()).forEach(index -> {
                printString.append("Mode ").append(index + 1).append(CyderStrings.newline);

                DisplayMode displayMode = modes.get(index);
                printString.append("Width: ").append(displayMode.getWidth()).append(CyderStrings.newline);
                printString.append("Height: ").append(displayMode.getHeight()).append(CyderStrings.newline);
                printString.append("Bit depth: ").append(displayMode.getBitDepth()).append(CyderStrings.newline);
                printString.append("Refresh rate: ").append(displayMode.getRefreshRate()).append(CyderStrings.newline);
            });

            getInputHandler().println(printString.toString().trim());
        } else if (getInputHandler().inputIgnoringSpacesMatches("create user")) {
            UserCreator.showGui();
        } else if (getInputHandler().commandIs("panic")) {
            if (UserDataManager.INSTANCE.shouldMinimizeOnClose()) {
                UiUtil.minimizeAllFrames();
            } else {
                OsUtil.exit(ExitCondition.StandardControlledExit);
            }
        } else if (getInputHandler().commandIs("quit")
                || getInputHandler().commandIs("exit")
                || getInputHandler().commandIs("leave")
                || getInputHandler().commandIs("close")) {
            if (UserDataManager.INSTANCE.shouldMinimizeOnClose()) {
                UiUtil.minimizeAllFrames();
            } else {
                Console.INSTANCE.releaseResourcesAndCloseFrame(true);
            }
        } else if (getInputHandler().commandIs("logout")) {
            Console.INSTANCE.logoutCurrentUserAndShowLoginFrame();
        } else if (getInputHandler().commandIs("mouse")) {
            if (getInputHandler().checkArgsLength(2)) {
                OsUtil.setMouseLocation(Integer.parseInt(getInputHandler().getArg(0)),
                        Integer.parseInt(getInputHandler().getArg(1)));
            } else {
                getInputHandler().println("Mouse command usage: mouse xPixelLocation, yPixelLocation");
            }
        } else if (getInputHandler().inputIgnoringSpacesMatches("clear clipboard")) {
            StringSelection selection = new StringSelection(null);
            java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);
            getInputHandler().println("Clipboard has been reset.");
        } else if (getInputHandler().commandIs("frames")) {
            for (CyderFrame frame : UiUtil.getCyderFrames()) {
                getInputHandler().println(frame);
            }
        } else {
            ret = false;
        }

        return ret;
    }

    /**
     * Sets up the opacity slider.
     */
    private static void initializeOpacitySlider() {
        opacitySlider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 100);
        opacitySlider.setBounds(0, 0, 300, 50);
        CyderSliderUi ui = new CyderSliderUi(opacitySlider);
        ui.setThumbStroke(new BasicStroke(2.0f));
        ui.setThumbShape(ThumbShape.CIRCLE);
        ui.setThumbRadius(35);
        ui.setThumbFillColor(CyderColors.navy);
        ui.setThumbOutlineColor(CyderColors.vanilla);
        ui.setRightThumbColor(CyderColors.vanilla);
        ui.setLeftThumbColor(CyderColors.regularPink);
        ui.setTrackStroke(new BasicStroke(3.0f));
        opacitySlider.setUI(ui);
        opacitySlider.setPaintTicks(false);
        opacitySlider.setPaintLabels(false);
        opacitySlider.setVisible(true);
        opacitySlider.setValue((int) (Console.INSTANCE.getConsoleCyderFrame().getOpacity()
                * opacitySlider.getMaximum()));
        opacitySlider.addChangeListener(e -> {
            float opacity = opacitySlider.getValue() / (float) opacitySlider.getMaximum();
            Console.INSTANCE.getConsoleCyderFrame().setOpacity(opacity);
            opacitySlider.repaint();
        });
        opacitySlider.setOpaque(false);
        opacitySlider.setToolTipText("Opacity");
        opacitySlider.setFocusable(false);
        opacitySlider.repaint();
    }
}
