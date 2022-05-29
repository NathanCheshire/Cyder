package cyder.handlers.input;

import cyder.annotations.Handle;
import cyder.constants.CyderColors;
import cyder.constants.CyderStrings;
import cyder.enums.ExitCondition;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.ConsoleFrame;
import cyder.ui.CyderSliderUI;
import cyder.user.UserCreator;
import cyder.utilities.FrameUtil;
import cyder.utilities.OSUtil;
import cyder.utilities.UserUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;

/**
 * A handler for handling things related to the ui and painting.
 */
public class UiHandler extends InputHandler {
    /**
     * Suppress default constructor.
     */
    private UiHandler() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    @Handle({"toast", "opacity", "originalchams", "screenshot", "monitors",
            "createuser", "panic", "quit", "logout", "clearclip", "mouse"})
    public static boolean handle() {
        boolean ret = true;

        if (getInputHandler().commandIs("toast")) {
            ConsoleFrame.INSTANCE.getConsoleCyderFrame().toast("A toast to you, sir/madam");
        } else if (getInputHandler().commandIs("freeze")) {
            //noinspection StatementWithEmptyBody
            while (true) {
            }
        } else if (getInputHandler().commandIs("opacity")) {
            if (opacitySlider == null) {
                initializeOpacitySlider();
            }

            getInputHandler().println(opacitySlider);
        } else if (getInputHandler().commandIs("originalchams")) {
            ConsoleFrame.INSTANCE.originalChams();
        } else if (getInputHandler().commandIs("screenshot")) {
            if (getInputHandler().getArgsSize() != 0) {
                if (getInputHandler().getArg(0).equalsIgnoreCase("frames")) {
                    FrameUtil.screenshotCyderFrames();
                    getInputHandler().println("Successfully saved to your Files directory");
                } else {
                    if (!FrameUtil.screenshotCyderFrame(getInputHandler().argsToString())) {
                        getInputHandler().println("CyderFrame not found");
                    } else {
                        getInputHandler().println("Successfully saved to your Files directory");
                    }
                }
            } else {
                getInputHandler().println("Screenshot command usage: screenshot [FRAMES or FRAME_NAME]");
            }
        } else if (getInputHandler().commandIs("monitors")) {
            getInputHandler().println(OSUtil.getMonitorStatsString());
        } else if (getInputHandler().commandIs("createuser")) {
            UserCreator.showGui();
        } else if (getInputHandler().commandIs("panic")) {
            if (UserUtil.getCyderUser().getMinimizeonclose().equals("1")) {
                FrameUtil.minimizeAllFrames();
            } else {
                OSUtil.exit(ExitCondition.GenesisControlledExit);
            }
        } else if (getInputHandler().commandIs("quit") ||
                getInputHandler().commandIs("exit") ||
                getInputHandler().commandIs("leave") ||
                getInputHandler().commandIs("close")) {
            if (UserUtil.getCyderUser().getMinimizeonclose().equals("1")) {
                FrameUtil.minimizeAllFrames();
            } else {
                ConsoleFrame.INSTANCE.closeConsoleFrame(true, false);
            }
        } else if (getInputHandler().commandIs("logout")) {
            ConsoleFrame.INSTANCE.logout();
        } else if (getInputHandler().commandIs("mouse")) {
            if (getInputHandler().checkArgsLength(2)) {
                OSUtil.setMouseLoc(Integer.parseInt(getInputHandler().getArg(0)),
                        Integer.parseInt(getInputHandler().getArg(1)));
            } else {
                getInputHandler().println("Mouse command usage: mouse X_PIXEL, Y_PIXEL");
            }
        } else if (getInputHandler().commandIs("clearclip")) {
            StringSelection selection = new StringSelection(null);
            java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);
            getInputHandler().println("Clipboard has been reset.");
        } else {
            ret = false;
        }

        return ret;
    }

    /**
     * The slider used to change the opacity of the ConsoleFrame.
     */
    private static JSlider opacitySlider;

    /**
     * Sets up the opacity slider.
     */
    private static void initializeOpacitySlider() {
        opacitySlider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 100);
        opacitySlider.setBounds(0, 0, 300, 50);
        CyderSliderUI UI = new CyderSliderUI(opacitySlider);
        UI.setThumbStroke(new BasicStroke(2.0f));
        UI.setSliderShape(CyderSliderUI.SliderShape.CIRCLE);
        UI.setThumbDiameter(35);
        UI.setFillColor(CyderColors.navy);
        UI.setOutlineColor(CyderColors.vanila);
        UI.setNewValColor(CyderColors.vanila);
        UI.setOldValColor(CyderColors.regularPink);
        UI.setTrackStroke(new BasicStroke(3.0f));
        opacitySlider.setUI(UI);
        opacitySlider.setMinimum(0);
        opacitySlider.setMaximum(100);
        opacitySlider.setPaintTicks(false);
        opacitySlider.setPaintLabels(false);
        opacitySlider.setVisible(true);
        opacitySlider.setValue(100);
        opacitySlider.addChangeListener(e -> {
            ConsoleFrame.INSTANCE.getConsoleCyderFrame()
                    .setOpacity(opacitySlider.getValue() / 100.0f);
            opacitySlider.repaint();
        });
        opacitySlider.setOpaque(false);
        opacitySlider.setToolTipText("Opacity");
        opacitySlider.setFocusable(false);
        opacitySlider.repaint();
    }
}
