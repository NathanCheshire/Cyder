package com.cyder.utilities;

import com.cyder.ui.CyderButton;
import com.cyder.ui.CyderFrame;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;

public class Hasher {
    private JPasswordField hashField;
    private Util hashUtil = new Util();

    public Hasher() {
        CyderFrame hashFrame = new CyderFrame(500,200,new ImageIcon("src\\com\\cyder\\io\\pictures\\DebugBackground.png"));
        hashFrame.setTitle("Hasher");

        JLabel Instructions = new JLabel("Enter your password to be hashed");
        Instructions.setForeground(hashUtil.navy);
        Instructions.setFont(hashUtil.weatherFontSmall);

        Instructions.setBounds(65,40, 400, 30);
        hashFrame.getContentPane().add(Instructions);

        hashField = new JPasswordField(15);
        hashField.setForeground(hashUtil.navy);
        hashField.setFont(hashUtil.weatherFontSmall);
        hashField.setBorder(new LineBorder(hashUtil.navy,5,false));
        hashField.addActionListener(e -> {
            char[] Hash = hashField.getPassword();

            if (Hash.length > 0) {
                String PrintHash = hashUtil.toHexString(hashUtil.getSHA(hashField.getPassword()));
                hashUtil.closeAnimation(hashFrame);
                hashUtil.inform("Your hashed password is:<br/>" + PrintHash + "<br/>It has also been copied to your clipboard.<br/>Provided by SHA256","", 900, 250);
                StringSelection selection = new StringSelection(PrintHash);
                java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, selection);
            }
        });

        hashField.setBounds(50,90, 400, 40);
        hashFrame.getContentPane().add(hashField);

        CyderButton hashButton = new CyderButton("Hasher");
        hashButton.setColors(hashUtil.regularRed);
        hashButton.setBackground(hashUtil.regularRed);
        hashButton.setBorder(new LineBorder(hashUtil.navy,5,false));
        hashButton.setFont(hashUtil.weatherFontSmall);
        hashButton.addActionListener(e -> {
            String PrintHash = hashUtil.toHexString(hashUtil.getSHA(hashField.getPassword()));
            hashUtil.closeAnimation(hashFrame);
            hashUtil.inform("Your hashed password is:<br/>" + PrintHash + "<br/>It has also been copied to your clipboard.<br/>Provided by SHA256","", 900, 250);
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
