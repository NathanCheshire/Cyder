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
import cyder.ui.CyderTextField;
import cyder.utilities.OSUtil;
import cyder.utilities.SecurityUtil;

import javax.swing.*;
import java.util.ArrayList;

public class HashingWidget {
    private CyderButton hashButton;
    private CyderPasswordField hashField;
    private int algorithmIndex = 0;
    private final ArrayList<String> algorithms = new ArrayList<>();

    //empty constructor
    public HashingWidget() {
        //multiple widgets should be allowed
    }

    @Widget(triggers = {"hash", "hasher"}, description =
            "A hashing widget to hash any string using multiple algorithms such as MD5, SHA256, and SHA1")
    public static void showGUI() {
        new HashingWidget().innerShowGUI();
    }

    public void innerShowGUI() {
        Logger.log(Logger.Tag.WIDGET_OPENED, "HASH");

        algorithms.add("SHA-256");
        algorithms.add("SHA-1");
        algorithms.add("MD5");

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

                switch (algorithmIndex) {
                    case 0:
                        algorithm = "SHA256";
                        hashResult = SecurityUtil.toHexString(SecurityUtil.getSHA256(hashField.getPassword()));
                        break;
                    case 1:
                        algorithm = "SHA1";
                        hashResult = SecurityUtil.toHexString(SecurityUtil.getSHA1(hashField.getPassword()));
                        break;
                    case 2:
                        algorithm = "MD5";
                        hashResult = SecurityUtil.toHexString(SecurityUtil.getMD5(hashField.getPassword()));
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid algorithm index: " + algorithmIndex);
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

        //todo abstract this field with switch button out

        CyderTextField hashAlgorithmField = new CyderTextField(0);
        hashAlgorithmField.setFocusable(false);
        hashAlgorithmField.setBounds(240,140,180,40);
        hashAlgorithmField.setEditable(false);
        hashFrame.getContentPane().add(hashAlgorithmField);
        hashAlgorithmField.setText(algorithms.get(algorithmIndex));

        CyderButton hashDropDown = new CyderButton("▼");
        hashDropDown.setBounds(240 + 170,140,40,40);
        hashFrame.getContentPane().add(hashDropDown);
        hashDropDown.addActionListener(e -> {
            algorithmIndex++;
            if (algorithmIndex == algorithms.size())
                algorithmIndex = 0;

            hashAlgorithmField.setText(algorithms.get(algorithmIndex));
        });

        hashFrame.setLocationRelativeTo(CyderCommon.getDominantFrame());
        hashFrame.setVisible(true);
    }
}
