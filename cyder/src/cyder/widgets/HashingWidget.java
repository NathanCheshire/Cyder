package cyder.widgets;

import cyder.annotations.Widget;
import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.consts.CyderImages;
import cyder.genesis.GenesisShare;
import cyder.handlers.internal.PopupHandler;
import cyder.ui.CyderButton;
import cyder.ui.CyderFrame;
import cyder.ui.CyderPasswordField;
import cyder.ui.CyderTextField;
import cyder.utilities.SecurityUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;

public class HashingWidget {
    private CyderButton hashButton;
    private CyderPasswordField hashField;
    private int algorithmIndex = 0;
    private ArrayList<String> algorithms = new ArrayList<>();

    //empty constructor
    public HashingWidget() {
        //multiple widgets should be allowed
    }

    @Widget(trigger = "hash", description = "A hashing widget to hash any string using multiple algorithms such as MD5, SHA256, and SHA1")
    public void showGUI() {
        algorithms.add("SHA-256");
        algorithms.add("SHA-1");
        algorithms.add("MD5");

        CyderFrame hashFrame = new CyderFrame(500,200, CyderImages.defaultBackgroundLarge);
        hashFrame.setTitlePosition(CyderFrame.TitlePosition.CENTER);
        hashFrame.setTitle("Hasher");

        JLabel Instructions = new JLabel("Enter your password to be hashed");
        Instructions.setForeground(CyderColors.navy);
        Instructions.setFont(CyderFonts.weatherFontSmall);

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
                if (algorithmIndex == 0) {
                    String PrintHash = SecurityUtil.toHexString(SecurityUtil.getSHA256(hashField.getPassword()));
                    PopupHandler.inform("Your hashed password is:<br/>" + PrintHash + "<br/>It has also been copied to your clipboard.<br/>Provided by SHA256","", hashFrame);
                    StringSelection selection = new StringSelection(PrintHash);
                    java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(selection, selection);
                } else if (algorithmIndex == 1) {
                    String PrintHash = SecurityUtil.toHexString(SecurityUtil.getSHA1(hashField.getPassword()));
                    PopupHandler.inform("Your hashed password is:<br/>" + PrintHash + "<br/>It has also been copied to your clipboard.<br/>Provided by SHA1","", hashFrame);
                    StringSelection selection = new StringSelection(PrintHash);
                    java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(selection, selection);
                } else if (algorithmIndex == 2) {
                    String PrintHash = SecurityUtil.toHexString(SecurityUtil.getMD5(hashField.getPassword()));
                    PopupHandler.inform("Your hashed password is:<br/>" + PrintHash + "<br/>It has also been copied to your clipboard.<br/>Provided by MD5","", hashFrame);
                    StringSelection selection = new StringSelection(PrintHash);
                    java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(selection, selection);
                }

                hashField.setText("");
            }
        });
        hashButton.setBounds(50,140, 180, 40);
        hashFrame.getContentPane().add(hashButton);

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

        hashFrame.setLocationRelativeTo(GenesisShare.getDominantFrame());
        hashFrame.setVisible(true);
    }
}
