package com.cyder.widgets;

import com.cyder.Constants.CyderColors;
import com.cyder.Constants.CyderFonts;
import com.cyder.enums.TitlePosition;
import com.cyder.ui.CyderButton;
import com.cyder.ui.CyderFrame;
import com.cyder.utilities.GeneralUtil;
import com.cyder.utilities.SecurityUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;

public class Hasher {
    private JPasswordField hashField;
    private GeneralUtil hashGeneralUtil = new GeneralUtil();

    public Hasher() {
        CyderFrame hashFrame = new CyderFrame(500,200,new ImageIcon("src/com/cyder/sys/pictures/DebugBackground.png"));
        hashFrame.setTitlePosition(TitlePosition.CENTER);
        hashFrame.setTitle("Hasher");

        JLabel Instructions = new JLabel("Enter your password to be hashed");
        Instructions.setForeground(CyderColors.navy);
        Instructions.setFont(CyderFonts.weatherFontSmall);

        Instructions.setBounds(65,40, 400, 30);
        hashFrame.getContentPane().add(Instructions);

        hashField = new JPasswordField(15);
        hashField.setForeground(CyderColors.navy);
        hashField.setFont(CyderFonts.weatherFontSmall);
        hashField.setBorder(new LineBorder(CyderColors.navy,5,false));
        hashField.addActionListener(e -> {
            char[] Hash = hashField.getPassword();

            if (Hash.length > 0) {
                String PrintHash = SecurityUtil.toHexString(SecurityUtil.getSHA(hashField.getPassword()));
                hashFrame.closeAnimation();
                GenericInform.inform("Your hashed password is:<br/>" + PrintHash + "<br/>It has also been copied to your clipboard.<br/>Provided by SHA256","", 900, 250);
                StringSelection selection = new StringSelection(PrintHash);
                java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, selection);
            }
        });

        hashField.setBounds(50,90, 400, 40);
        hashFrame.getContentPane().add(hashField);

        CyderButton hashButton = new CyderButton("Hash");
        hashButton.setColors(CyderColors.regularRed);
        hashButton.setBackground(CyderColors.regularRed);
        hashButton.setBorder(new LineBorder(CyderColors.navy,5,false));
        hashButton.setFont(CyderFonts.weatherFontSmall);
        hashButton.addActionListener(e -> {
            String PrintHash = SecurityUtil.toHexString(SecurityUtil.getSHA(hashField.getPassword()));
            hashFrame.closeAnimation();
            GenericInform.inform("Your hashed password is:<br/>" + PrintHash + "<br/>It has also been copied to your clipboard.<br/>Provided by SHA256","", 900, 250);
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
