package cyder.utilities;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.consts.CyderStrings;
import cyder.handler.ErrorHandler;
import cyder.ui.ConsoleFrame;
import cyder.ui.CyderButton;
import cyder.ui.CyderCaret;
import cyder.ui.CyderFrame;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.io.File;

public class GetterUtil {
    private static String frameTitle;
    private static String tooltipText;
    private static String buttonText;
    private static String returnString = null;

    public GetterUtil() {}

    public static String getString(String title, String tooltip, String button) {
        frameTitle = title;
        tooltipText = tooltip;
        buttonText = button;

        new Thread(() -> {
            try {
                File currentBackground = ConsoleFrame.getCurrentBackgroundFile().getAbsoluteFile();

                CyderFrame inputFrame = new CyderFrame(400,170,new ImageIcon(CyderStrings.DEFAULT_BACKGROUND_PATH));
                inputFrame.setTitle(getFrameTitle());

                JTextField inputField = new JTextField(20);
                inputField.setSelectionColor(CyderColors.selectionColor);
                inputField.setToolTipText(getTooltipText());
                inputField.setFont(CyderFonts.weatherFontSmall);
                inputField.setForeground(CyderColors.navy);
                inputField.setCaretColor(CyderColors.navy);
                inputField.setCaret(new CyderCaret(CyderColors.navy));
                inputField.setBorder(new LineBorder(CyderColors.navy,5,false));
                inputField.setBounds(40,40,320,40);
                inputFrame.getContentPane().add(inputField);

                CyderButton submit = new CyderButton(getButtonText());
                submit.setBackground(CyderColors.regularRed);
                submit.setColors(CyderColors.regularRed);
                inputField.addActionListener(e1 -> submit.doClick());
                submit.setBorder(new LineBorder(CyderColors.navy,5,false));
                submit.setFont(CyderFonts.weatherFontSmall);
                submit.setForeground(CyderColors.navy);
                submit.addActionListener(e12 -> {
                    returnString = (inputField.getText() == null || inputField.getText().length() == 0 ? null : inputField.getText());
                    inputFrame.closeAnimation();
                });
                submit.setBounds(40,100,320,40);
                inputFrame.getContentPane().add(submit);

                inputFrame.setVisible(true);
                inputFrame.setAlwaysOnTop(true);
                inputFrame.setLocationRelativeTo(null);
            } catch (Exception e) {
                ErrorHandler.handle(e);
            }
        }, "getString thread").start();

        try {
            while (returnString == null) {
                Thread.onSpinWait();
            }
        } catch (Exception ex) {
            ErrorHandler.handle(ex);
        } finally {
            String ret = returnString;
            clear();
            return ret;
        }
    }

    public static void setFrameTitle(String title) {
        frameTitle = title;
    }

    public static void setTooltipText(String text) {
        tooltipText = text;
    }

    public static void setButtonText(String text) {
        buttonText = text;
    }

    public static String getFrameTitle() {
        return frameTitle;
    }

    public static String getTooltipText() {
        return tooltipText;
    }

    public static String getButtonText() {
        return buttonText;
    }

    public static void clear() {
        returnString = null;
        frameTitle = null;
        tooltipText = null;
        buttonText = null;
    }
}
