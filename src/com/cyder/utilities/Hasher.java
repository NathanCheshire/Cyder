package com.cyder.utilities;

import com.cyder.ui.CyderButton;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;

public class Hasher {
    private JFrame hashFrame;
    private JPasswordField hashField;
    private Util hashUtil = new Util();

    public Hasher() {
        if (hashFrame != null) {
            hashUtil.closeAnimation(hashFrame);
            hashFrame.dispose();
        }

        hashFrame = new JFrame();

        hashFrame.setResizable(false);

        hashFrame.setTitle("Hasher");

        hashFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        hashFrame.setIconImage(hashUtil.getCyderIcon().getImage());

        JPanel ParentPanel = new JPanel();

        ParentPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        ParentPanel.setLayout(new BoxLayout(ParentPanel, BoxLayout.Y_AXIS));

        JPanel InstPanel = new JPanel();

        InstPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        JLabel Instructions = new JLabel("Enter your password to be hashed");

        Instructions.setForeground(hashUtil.navy);

        Instructions.setFont(hashUtil.weatherFontSmall);

        InstPanel.add(Instructions);

        ParentPanel.add(InstPanel);

        hashField = new JPasswordField(15);

        hashField.setForeground(hashUtil.navy);

        hashField.setFont(hashUtil.weatherFontSmall);

        hashField.setBorder(new LineBorder(hashUtil.navy,5,false));

        hashField.addActionListener(e -> {
            char[] Hash = hashField.getPassword();

            if (Hash.length > 0) {
                String PrintHash = hashUtil.toHexString(hashUtil.getSHA(hashField.getPassword()));
                hashUtil.closeAnimation(hashFrame);
                hashFrame.dispose();
                hashUtil.inform("Your hashed password is:<br/>" + PrintHash + "<br/>It has also been copied to your clipboard.<br/>Provided by SHA256","", 900, 250);
                StringSelection selection = new StringSelection(PrintHash);
                java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, selection);
            }
        });

        JPanel FieldPanel = new JPanel();

        FieldPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        FieldPanel.add(hashField);

        ParentPanel.add(FieldPanel);

        JPanel ButtonPanel = new JPanel();

        ButtonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        CyderButton hashButton = new CyderButton("Hasher");

        hashButton.setColors(hashUtil.regularRed);

        hashButton.setBackground(hashUtil.regularRed);

        hashButton.setBorder(new LineBorder(hashUtil.navy,5,false));

        hashButton.setFont(hashUtil.weatherFontSmall);

        hashButton.addActionListener(e -> {
            String PrintHash = hashUtil.toHexString(hashUtil.getSHA(hashField.getPassword()));
            hashUtil.closeAnimation(hashFrame);
            hashFrame.dispose();
            hashUtil.inform("Your hashed password is:<br/>" + PrintHash + "<br/>It has also been copied to your clipboard.<br/>Provided by SHA256","", 900, 250);
            StringSelection selection = new StringSelection(PrintHash);
            java.awt.datatransfer.Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(selection, selection);
        });

        hashButton.setFocusPainted(false);

        ButtonPanel.add(hashButton);

        ParentPanel.add(ButtonPanel);

        hashFrame.add(ParentPanel);

        hashFrame.pack();

        hashFrame.setLocationRelativeTo(null);

        hashFrame.setVisible(true);

        hashFrame.setAlwaysOnTop(true);
    }
}
