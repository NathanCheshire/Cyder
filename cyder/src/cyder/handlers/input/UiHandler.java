package cyder.handlers.input;

import cyder.annotations.Handle;
import cyder.console.Console;
import cyder.constants.CyderColors;
import cyder.constants.CyderStrings;
import cyder.enums.ExitCondition;
import cyder.exceptions.IllegalMethodException;
import cyder.ui.frame.CyderFrame;
import cyder.ui.slider.CyderSliderUi;
import cyder.user.UserCreator;
import cyder.user.UserUtil;
import cyder.utils.OSUtil;
import cyder.utils.UiUtil;

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
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    @Handle({"toast", "opacity", "original chams", "screenshot", "monitors",
            "create user", "panic", "quit", "logout", "clear clip", "mouse", "frames"})
    public static boolean handle() {
        boolean ret = true;

        if (getInputHandler().commandIs("toast")) {
            Console.INSTANCE.getConsoleCyderFrame().toast("A toast to you, sir/madam");
        } else if (getInputHandler().commandIs("freeze")) {
            //noinspection StatementWithEmptyBody
            while (true) {
            }
        } else if (getInputHandler().commandIs("opacity")) {
            if (opacitySlider == null) {
                initializeOpacitySlider();
            }

            getInputHandler().println(opacitySlider);
        } else if (getInputHandler().inputIgnoringSpacesMatches("originalchams")) {
            Console.INSTANCE.originalChams();
        } else if (getInputHandler().inputIgnoringSpacesMatches("screenshot")) {
            if (getInputHandler().getArgsSize() != 0) {
                if (getInputHandler().getArg(0).equalsIgnoreCase("frames")) {
                    UiUtil.screenshotCyderFrames();
                    getInputHandler().println("Successfully saved to your Files directory");
                } else {
                    if (!UiUtil.screenshotCyderFrame(getInputHandler().argsToString())) {
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
                UiUtil.minimizeAllFrames();
            } else {
                OSUtil.exit(ExitCondition.GenesisControlledExit);
            }
        } else if (getInputHandler().commandIs("quit") ||
                getInputHandler().commandIs("exit") ||
                getInputHandler().commandIs("leave") ||
                getInputHandler().commandIs("close")) {
            if (UserUtil.getCyderUser().getMinimizeonclose().equals("1")) {
                UiUtil.minimizeAllFrames();
            } else {
                Console.INSTANCE.closeFrame(true, false);
            }
        } else if (getInputHandler().commandIs("logout")) {
            Console.INSTANCE.logout();
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
     * The slider used to change the opacity of the Console.
     */
    private static JSlider opacitySlider;

    /**
     * Sets up the opacity slider.
     */
    private static void initializeOpacitySlider() {
        opacitySlider = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 100);
        opacitySlider.setBounds(0, 0, 300, 50);
        CyderSliderUi UI = new CyderSliderUi(opacitySlider);
        UI.setThumbStroke(new BasicStroke(2.0f));
        UI.setThumbShape(CyderSliderUi.ThumbShape.CIRCLE);
        UI.setThumbRadius(35);
        UI.setThumbFillColor(CyderColors.navy);
        UI.setThumbOutlineColor(CyderColors.vanilla);
        UI.setRightThumbColor(CyderColors.vanilla);
        UI.setLeftThumbColor(CyderColors.regularPink);
        UI.setTrackStroke(new BasicStroke(3.0f));
        opacitySlider.setUI(UI);
        opacitySlider.setMinimum(0);
        opacitySlider.setMaximum(100);
        opacitySlider.setPaintTicks(false);
        opacitySlider.setPaintLabels(false);
        opacitySlider.setVisible(true);
        opacitySlider.setValue(100);
        opacitySlider.addChangeListener(e -> {
            Console.INSTANCE.getConsoleCyderFrame()
                    .setOpacity(opacitySlider.getValue() / 100.0f);
            opacitySlider.repaint();
        });
        opacitySlider.setOpaque(false);
        opacitySlider.setToolTipText("Opacity");
        opacitySlider.setFocusable(false);
        opacitySlider.repaint();
    }
}
