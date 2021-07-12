package cyder.widgets;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;

import cyder.consts.CyderImages;
import cyder.ui.ConsoleFrame;
import cyder.ui.CyderButton;
import cyder.ui.CyderFrame;
import cyder.utilities.SecurityUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;

public class Hasher {
    private JPasswordField hashField;

    public Hasher() {
        CyderFrame hashFrame = new CyderFrame(500,200, CyderImages.defaultBackground);
        hashFrame.setTitlePosition(CyderFrame.TitlePosition.CENTER);
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
                GenericInform.inform("Your hashed password is:<br/>" + PrintHash + "<br/>It has also been copied to your clipboard.<br/>Provided by SHA256","");
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
            GenericInform.inform("Your hashed password is:<br/>" + PrintHash + "<br/>It has also been copied to your clipboard.<br/>Provided by SHA256","");
            StringSelection selection = new StringSelection(PrintHash);
            java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);
        });

        hashButton.setFocusPainted(false);

        hashButton.setBounds(200,140, 100, 40);
        hashFrame.getContentPane().add(hashButton);

        ConsoleFrame.getConsoleFrame().setFrameRelative(hashFrame);
        hashFrame.setVisible(true);
        hashFrame.setAlwaysOnTop(true);
    }
}
