package cyder.widgets;

import cyder.annotations.Widget;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderIcons;
import cyder.genesis.CyderCommon;
import cyder.handlers.internal.Logger;
import cyder.handlers.internal.PopupHandler;
import cyder.handlers.internal.objects.PopupBuilder;
import cyder.ui.CyderButton;
import cyder.ui.CyderFrame;
import cyder.ui.CyderPasswordField;
import cyder.ui.CyderSwitcher;
import cyder.ui.objects.SwitchState;
import cyder.utilities.OSUtil;
import cyder.utilities.SecurityUtil;

import javax.swing.*;
import java.util.ArrayList;

public class HashingWidget {
    private CyderButton hashButton;
    private CyderPasswordField hashField;
    private CyderSwitcher switcher;

    /**
     * Constructs a new hashing widget.
     */
    private HashingWidget() {
        //multiple widgets should be allowed
    }

    @Widget(triggers = {"hash", "hasher"}, description =
            "A hashing widget to hash any string using multiple algorithms such as MD5, SHA256, and SHA1")
    public static void showGUI() {
        new HashingWidget().innerShowGUI();
    }

    public void innerShowGUI() {
        Logger.log(Logger.Tag.WIDGET_OPENED, "HASH");

        CyderFrame hashFrame = new CyderFrame(500,200, CyderIcons.defaultBackgroundLarge);
        hashFrame.setTitlePosition(CyderFrame.TitlePosition.CENTER);
        hashFrame.setTitle("Hasher");

        JLabel Instructions = new JLabel("Enter your password to be hashed");
        Instructions.setForeground(CyderColors.navy);
        Instructions.setFont(CyderFonts.segoe20);

        Instructions.setBounds(65,40, 400, 30);
        hashFrame.getContentPane().add(Instructions);

        hashField = new CyderPasswordField();
        hashField.addActionListener(e -> hashButton.doClick());
        hashField.setBounds(50,90, 400, 40);
        hashFrame.getContentPane().add(hashField);

        hashButton = new CyderButton("Hash");
        hashButton.addActionListener(e -> {
            char[] Hash = hashField.getPassword();

            if (Hash.length > 0) {
                String hashResult;
                String inform;
                String algorithm;

                algorithm = switcher.getCurrentState().getDisplayValue();

                if (algorithm.equals("SHA-256")) {
                    hashResult = SecurityUtil.toHexString(SecurityUtil.getSHA256(hashField.getPassword()));
                } else if (algorithm.equals("SHA-1")) {
                    hashResult = SecurityUtil.toHexString(SecurityUtil.getSHA1(hashField.getPassword()));
                } else {
                    hashResult = SecurityUtil.toHexString(SecurityUtil.getMD5(hashField.getPassword()));
                }

                inform = "Your hashed password is:<br/>" + hashResult
                        + "<br/>It has also been copied to your clipboard.<br/>Provided by " + algorithm;

                PopupBuilder builder = new PopupBuilder(inform);
                builder.setTitle(algorithm + " Hash Result");
                builder.setRelativeTo(hashFrame);
                PopupHandler.inform(builder);

                OSUtil.setClipboard(hashResult);

                hashField.setText("");
            }
        });
        hashButton.setBounds(50,140, 180, 40);
        hashFrame.getContentPane().add(hashButton);

        ArrayList<SwitchState> states = new ArrayList<>();
        states.add(new SwitchState("SHA-256", "SHA256 Algorithm"));
        states.add(new SwitchState("SHA-1", "SHA-1 Algorithm"));
        states.add(new SwitchState("MD5", "MD5 Algorithm (Not secure)"));

        SwitchState startingState = states.get(0);

        switcher = new CyderSwitcher(210,40, states, startingState);
        switcher.setBounds(240,140,210,40);
        hashFrame.getContentPane().add(switcher);

        hashFrame.setLocationRelativeTo(CyderCommon.getDominantFrame());
        hashFrame.setVisible(true);
    }
}
