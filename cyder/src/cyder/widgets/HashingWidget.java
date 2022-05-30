package cyder.widgets;

import cyder.annotations.CyderAuthor;
import cyder.annotations.Vanilla;
import cyder.annotations.Widget;
import cyder.builders.InformBuilder;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderIcons;
import cyder.handlers.internal.InformHandler;
import cyder.handlers.internal.Logger;
import cyder.ui.CyderButton;
import cyder.ui.CyderComboBox;
import cyder.ui.CyderFrame;
import cyder.ui.CyderPasswordField;
import cyder.utilities.OSUtil;
import cyder.utilities.SecurityUtil;

import javax.swing.*;
import java.util.ArrayList;

/**
 * A widget for computing the hash of strings.
 */
@Vanilla
@CyderAuthor
public class HashingWidget {
    /**
     * The hash frame.
     */
    private CyderFrame hashFrame;

    /**
     * The hash field.
     */
    private CyderPasswordField hashField;

    /**
     * The hash algorithm combo box.
     */
    private CyderComboBox comboBox;

    /**
     * Constructs and returns a new hashing widget.
     *
     * @return a new hashing widget
     */
    public static HashingWidget getInstance() {
        return new HashingWidget();
    }

    /**
     * Constructs a new hashing widget.
     */
    private HashingWidget() {
        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    @Widget(triggers = {"hash", "hasher"}, description =
            "A hashing widget to hash any string using multiple algorithms such as MD5, SHA256, and SHA1")
    public static void showGui() {
        getInstance().innerShowGui();
    }

    /**
     * Shows the gui for this instance of the hashing widget.
     */
    public void innerShowGui() {
        hashFrame = new CyderFrame(500, 200, CyderIcons.defaultBackgroundLarge);
        hashFrame.setTitlePosition(CyderFrame.TitlePosition.CENTER);
        hashFrame.setTitle("Hasher");

        JLabel Instructions = new JLabel("Enter your password to be hashed");
        Instructions.setForeground(CyderColors.navy);
        Instructions.setFont(CyderFonts.segoe20);

        Instructions.setBounds(65, 40, 400, 30);
        hashFrame.getContentPane().add(Instructions);

        hashField = new CyderPasswordField();
        hashField.addActionListener(e -> hash());
        hashField.setBounds(50, 90, 400, 40);
        hashFrame.getContentPane().add(hashField);

        CyderButton hashButton = new CyderButton("Hash");
        hashButton.addActionListener(e -> hash());
        hashButton.setBounds(50, 140, 180, 40);
        hashFrame.getContentPane().add(hashButton);

        ArrayList<CyderComboBox.CyderComboItem> states = new ArrayList<>();
        states.add(new CyderComboBox.CyderComboItem("SHA-256", "SHA256 Algorithm"));
        states.add(new CyderComboBox.CyderComboItem("SHA-1", "SHA-1 Algorithm"));
        states.add(new CyderComboBox.CyderComboItem("MD5", "MD5 Algorithm (Not secure)"));

        CyderComboBox.CyderComboItem startingState = states.get(0);

        comboBox = new CyderComboBox(210, 40, states, startingState);
        comboBox.setBounds(240, 140, 210, 40);
        hashFrame.getContentPane().add(comboBox);

        hashFrame.finalizeAndShow();
    }

    private void hash() {
        char[] Hash = hashField.getPassword();

        if (Hash.length > 0) {
            String hashResult;
            String inform;
            String algorithm;

            algorithm = comboBox.getCurrentState().displayValue();

            if (algorithm.equals("SHA-256")) {
                hashResult = SecurityUtil.toHexString(SecurityUtil.getSHA256(hashField.getPassword()));
            } else if (algorithm.equals("SHA-1")) {
                hashResult = SecurityUtil.toHexString(SecurityUtil.getSHA1(hashField.getPassword()));
            } else {
                hashResult = SecurityUtil.toHexString(SecurityUtil.getMD5(hashField.getPassword()));
            }

            inform = "Your hashed password is:<br/>" + hashResult
                    + "<br/>It has also been copied to your clipboard.<br/>Provided by " + algorithm;

            InformBuilder builder = new InformBuilder(inform);
            builder.setTitle(algorithm + " Hash Result");
            builder.setRelativeTo(hashFrame);
            InformHandler.inform(builder);

            OSUtil.setClipboard(hashResult);

            hashField.setText("");
        }
    }
}
