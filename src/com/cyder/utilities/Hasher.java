package com.cyder.utilities;

import com.cyder.ui.CyderButton;
import com.cyder.ui.CyderFrame;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;

public class Hasher {
    private JPasswordField hashField;
    private GeneralUtil hashGeneralUtil = new GeneralUtil();

    public Hasher() {
        CyderFrame hashFrame = new CyderFrame(500,200,new ImageIcon("src/com/cyder/io/pictures/DebugBackground.png"));
        hashFrame.setTitlePosition(CyderFrame.CENTER_TITLE);
        hashFrame.setTitle("Hasher");

        JLabel Instructions = new JLabel("Enter your password to be hashed");
        Instructions.setForeground(hashGeneralUtil.navy);
        Instructions.setFont(hashGeneralUtil.weatherFontSmall);

        Instructions.setBounds(65,40, 400, 30);
        hashFrame.getContentPane().add(Instructions);

        hashField = new JPasswordField(15);
        hashField.setForeground(hashGeneralUtil.navy);
        hashField.setFont(hashGeneralUtil.weatherFontSmall);
        hashField.setBorder(new LineBorder(hashGeneralUtil.navy,5,false));
        hashField.addActionListener(e -> {
            char[] Hash = hashField.getPassword();

            if (Hash.length > 0) {
                String PrintHash = hashGeneralUtil.toHexString(hashGeneralUtil.getSHA(hashField.getPassword()));
                hashGeneralUtil.closeAnimation(hashFrame);
                hashGeneralUtil.inform("Your hashed password is:<br/>" + PrintHash + "<br/>It has also been copied to your clipboard.<br/>Provided by SHA256","", 900, 250);
                StringSelection selection = new StringSelection(PrintHash);
                java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, selection);
            }
        });

        hashField.setBounds(50,90, 400, 40);
        hashFrame.getContentPane().add(hashField);

        CyderButton hashButton = new CyderButton("Hash");
        hashButton.setColors(hashGeneralUtil.regularRed);
        hashButton.setBackground(hashGeneralUtil.regularRed);
        hashButton.setBorder(new LineBorder(hashGeneralUtil.navy,5,false));
        hashButton.setFont(hashGeneralUtil.weatherFontSmall);
        hashButton.addActionListener(e -> {
            String PrintHash = hashGeneralUtil.toHexString(hashGeneralUtil.getSHA(hashField.getPassword()));
            hashGeneralUtil.closeAnimation(hashFrame);
            hashGeneralUtil.inform("Your hashed password is:<br/>" + PrintHash + "<br/>It has also been copied to your clipboard.<br/>Provided by SHA256","", 900, 250);
            StringSelection selection = new StringSelection(PrintHash);
            java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);
        });

        hashButton.setFocusPainted(false);

        hashButton.setBounds(200,140, 100, 40);
        hashFrame.getContentPane().add(hashButton);

        hashFrame.setLocationRelativeTo(null);
        hashFrame.setVisible(true);
        hashFrame.setAlwaysOnTop(true);
    }
}
