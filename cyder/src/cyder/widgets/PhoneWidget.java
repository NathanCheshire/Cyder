package cyder.widgets;

import cyder.annotations.Widget;
import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.consts.CyderIcons;
import cyder.consts.CyderStrings;
import cyder.genesis.GenesisShare;
import cyder.handlers.internal.SessionHandler;
import cyder.ui.CyderButton;
import cyder.ui.CyderFrame;
import cyder.utilities.IOUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;

public class PhoneWidget implements WidgetBase {
    private static JLabel numberLabel;
    private static String phoneNum;

    private PhoneWidget() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    @Widget(trigger = "phone", description = "A phone emulating widget")
    public static void showGUI() {
        SessionHandler.log(SessionHandler.Tag.WIDGET_OPENED, "PHONE");

        CyderFrame phoneFrame = new CyderFrame(320,500, CyderIcons.defaultBackground);
        phoneFrame.setTitle("Phone");

        numberLabel = new JLabel("#");
        numberLabel.setFont(CyderFonts.segoe20);
        numberLabel.setBorder(new LineBorder(CyderColors.navy,5,false));

        CyderButton zero = new CyderButton("0");
        zero.setBorder(new LineBorder(CyderColors.navy,5,false));
        CyderButton one = new CyderButton("1");
        one.setBorder(new LineBorder(CyderColors.navy,5,false));
        CyderButton two = new CyderButton("2");
        two.setBorder(new LineBorder(CyderColors.navy,5,false));
        CyderButton three = new CyderButton("3");
        three.setBorder(new LineBorder(CyderColors.navy,5,false));
        CyderButton four = new CyderButton("4");
        four.setBorder(new LineBorder(CyderColors.navy,5,false));
        CyderButton five = new CyderButton("5");
        five.setBorder(new LineBorder(CyderColors.navy,5,false));
        CyderButton six = new CyderButton("6");
        six.setBorder(new LineBorder(CyderColors.navy,5,false));
        CyderButton seven = new CyderButton("7");
        seven.setBorder(new LineBorder(CyderColors.navy,5,false));
        CyderButton eight = new CyderButton("8");
        eight.setBorder(new LineBorder(CyderColors.navy,5,false));
        CyderButton nine = new CyderButton("9");
        nine.setBorder(new LineBorder(CyderColors.navy,5,false));
        CyderButton back = new CyderButton("<X");
        back.setBorder(new LineBorder(CyderColors.navy,5,false));
        CyderButton dialNumber = new CyderButton("Call");
        dialNumber.setBorder(new LineBorder(CyderColors.navy,5,false));

        numberLabel.setBounds(20, 40, 320 - 40, 40);
        phoneFrame.getContentPane().add(numberLabel);

        one.setBounds(20, 100, 80, 80);
        phoneFrame.getContentPane().add(one);

        two.setBounds(120, 100, 80, 80);
        phoneFrame.getContentPane().add(two);

        three.setBounds(220, 100, 80, 80);
        phoneFrame.getContentPane().add(three);

        four.setBounds(20, 200, 80, 80);
        phoneFrame.getContentPane().add(four);

        five.setBounds(120, 200, 80, 80);
        phoneFrame.getContentPane().add(five);

        six.setBounds(220, 200, 80, 80);
        phoneFrame.getContentPane().add(six);

        seven.setBounds(20, 300, 80, 80);
        phoneFrame.getContentPane().add(seven);

        eight.setBounds(120, 300, 80, 80);
        phoneFrame.getContentPane().add(eight);

        nine.setBounds(220, 300, 80, 80);
        phoneFrame.getContentPane().add(nine);

        back.setBounds(20, 400, 80, 80);
        phoneFrame.getContentPane().add(back);

        zero.setBounds(120, 400, 80, 80);
        phoneFrame.getContentPane().add(zero);

        dialNumber.setBounds(220, 400, 80, 80);
        phoneFrame.getContentPane().add(dialNumber);


        one.setFocusPainted(false);
        two.setFocusPainted(false);
        three.setFocusPainted(false);
        four.setFocusPainted(false);
        five.setFocusPainted(false);
        six.setFocusPainted(false);
        seven.setFocusPainted(false);
        eight.setFocusPainted(false);
        nine.setFocusPainted(false);
        zero.setFocusPainted(false);
        dialNumber.setFocusPainted(false);
        back.setFocusPainted(false);

        one.addActionListener(e -> {
            phoneNum = phoneNum + "1";
            numberLabel.setText(phoneNumFormat(phoneNum));

        });

        two.addActionListener(e -> {
            phoneNum = phoneNum + "2";
            numberLabel.setText(phoneNumFormat(phoneNum));

        });

        three.addActionListener(e -> {
            phoneNum = phoneNum + "3";
            numberLabel.setText(phoneNumFormat(phoneNum));
        });

        four.addActionListener(e -> {
            phoneNum = phoneNum + "4";
            numberLabel.setText(phoneNumFormat(phoneNum));
        });

        five.addActionListener(e -> {
            phoneNum = phoneNum + "5";
            numberLabel.setText(phoneNumFormat(phoneNum));
        });

        six.addActionListener(e -> {
            phoneNum = phoneNum + "6";
            numberLabel.setText(phoneNumFormat(phoneNum));
        });

        seven.addActionListener(e -> {
            phoneNum = phoneNum + "7";
            numberLabel.setText(phoneNumFormat(phoneNum));
        });

        eight.addActionListener(e -> {
            phoneNum = phoneNum + "8";
            numberLabel.setText(phoneNumFormat(phoneNum));
        });

        nine.addActionListener(e -> {
            phoneNum = phoneNum + "9";
            numberLabel.setText(phoneNumFormat(phoneNum));
        });

        zero.addActionListener(e -> {
            phoneNum = phoneNum + "0";
            numberLabel.setText(phoneNumFormat(phoneNum));
        });

        back.addActionListener(e -> {
            if (phoneNum.length() > 0) {
                phoneNum = phoneNum.substring(0, phoneNum.length() - 1);
                numberLabel.setText(phoneNumFormat(phoneNum));
            }
        });

        one.setBackground(CyderColors.regularOrange);
        one.setFont(CyderFonts.segoe30);

        two.setFocusPainted(false);
        two.setBackground(CyderColors.regularOrange);
        two.setFont(CyderFonts.segoe30);

        three.setFocusPainted(false);
        three.setBackground(CyderColors.regularOrange);
        three.setFont(CyderFonts.segoe30);

        four.setFocusPainted(false);
        four.setBackground(CyderColors.regularOrange);
        four.setFont(CyderFonts.segoe30);

        five.setFocusPainted(false);
        five.setBackground(CyderColors.regularOrange);
        five.setFont(CyderFonts.segoe30);

        six.setFocusPainted(false);
        six.setBackground(CyderColors.regularOrange);
        six.setFont(CyderFonts.segoe30);

        seven.setFocusPainted(false);
        seven.setBackground(CyderColors.regularOrange);
        seven.setFont(CyderFonts.segoe30);

        eight.setFocusPainted(false);
        eight.setBackground(CyderColors.regularOrange);
        eight.setFont(CyderFonts.segoe30);

        nine.setFocusPainted(false);
        nine.setBackground(CyderColors.regularOrange);
        nine.setFont(CyderFonts.segoe30);

        zero.setFocusPainted(false);
        zero.setBackground(CyderColors.regularOrange);
        zero.setFont(CyderFonts.segoe30);

        back.setFocusPainted(false);
        back.setBackground(CyderColors.regularOrange);
        back.setFont(CyderFonts.segoe30);

        dialNumber.setFocusPainted(false);
        dialNumber.setBackground(CyderColors.regularOrange);
        dialNumber.setFont(CyderFonts.segoe30);
        dialNumber.addActionListener(e -> {
            if (phoneNum.length() > 0) {
                checkFor223();
                checkForSuicideHotline();

                if (checkForSuicideHotline()) {
                    IOUtil.playAudio("static/audio/1800.mp3");
                }

                else if (checkFor223()) {
                    IOUtil.playAudio("static/audio/223.mp3");
                }

                else {
                    phoneFrame.notify("Dialing: " + numberLabel.getText());
                    phoneNum = "";
                }
            }
        });

        phoneFrame.setLocationRelativeTo(GenesisShare.getDominantFrame());
        phoneFrame.setVisible(true);
    }

    private static String phoneNumFormat(String num) {
        num = num.replaceAll("[^\\d.]", "");
        int len = num.length();

        if (len == 0) {
            return "#";
        } else if (len > 0 && len <= 4) {
            return num;
        } else if (len == 5) {
            return (num.charAt(0) + "-" + num.substring(1,len));
        } else if (len == 6) {
            return (num.substring(0,2) + "-" + num.substring(2,len));
        } else if (len == 7) {
            return (num.substring(0,3) + "-" + num.substring(3,len));
        } else if (len == 8) {
            return ("(" + num.charAt(0) + ") " + num.substring(1,4) + " " + num.substring(4,len));
        } else if (len == 9) {
            return ("(" + num.substring(0,2) + ") " + num.substring(2,5) + " " + num.substring(5,len));
        } else if (len == 10) {
            return ("(" + num.substring(0,3) + ") " + num.substring(3,6) + " " + num.substring(6,len));
        } else if (len > 10) {
            if (len > 15) {
                phoneNum = numberLabel.getText();
                return numberLabel.getText();
            }


            String leadingDigits = num.substring(0, len - 10);
            int offset = leadingDigits.length();

            return (leadingDigits + " (" + num.substring(offset,3 + offset) + ") " + num.substring(3 + offset,6 + offset) + " " + num.substring(6 + offset,len));
        } else {
            return null;
        }
    }

    private static boolean checkForSuicideHotline() {
        String num = numberLabel.getText().replace("-","").replace("(","").replace(")","").replace(" ","").trim();
        return num.equals("18002738255");
    }

    private static boolean checkFor223() {
        String num = numberLabel.getText().replace("-","").replace("(","").replace(")","").replace(" ","").trim();
        return num.equals("223");
    }
}
