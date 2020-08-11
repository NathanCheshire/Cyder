package com.cyder.utilities;

import com.cyder.ui.CyderButton;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class Phone {
    private JFrame phoneFrame;
    private JLabel numberLabel;
    private String phoneNum;

    private Util phoneUtil = new Util();

    public Phone() {
        if (phoneFrame != null)
            phoneUtil.closeAnimation(phoneFrame);

        phoneFrame = new JFrame();

        phoneFrame.setTitle("Phone");

        phoneFrame.setLocationRelativeTo(null);

        phoneFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel MyPanel = (JPanel) phoneFrame.getContentPane();

        MyPanel.setLayout(new BoxLayout(MyPanel, BoxLayout.Y_AXIS));

        JPanel topPanel = new JPanel();

        numberLabel = new JLabel("#");

        topPanel.add(numberLabel);

        numberLabel.setFont(phoneUtil.weatherFontSmall);

        numberLabel.setBorder(new LineBorder(phoneUtil.navy,5,false));

        JPanel ButtonsPanel = new JPanel();

        ButtonsPanel.setLayout(new GridLayout(4, 3, 5, 5));

        MyPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        CyderButton zero = new CyderButton("0");
        zero.setBorder(new LineBorder(phoneUtil.navy,5,false));
        CyderButton one = new CyderButton("1");
        one.setBorder(new LineBorder(phoneUtil.navy,5,false));
        CyderButton two = new CyderButton("2");
        two.setBorder(new LineBorder(phoneUtil.navy,5,false));
        CyderButton three = new CyderButton("3");
        three.setBorder(new LineBorder(phoneUtil.navy,5,false));
        CyderButton four = new CyderButton("4");
        four.setBorder(new LineBorder(phoneUtil.navy,5,false));
        CyderButton five = new CyderButton("5");
        five.setBorder(new LineBorder(phoneUtil.navy,5,false));
        CyderButton six = new CyderButton("6");
        six.setBorder(new LineBorder(phoneUtil.navy,5,false));
        CyderButton seven = new CyderButton("7");
        seven.setBorder(new LineBorder(phoneUtil.navy,5,false));
        CyderButton eight = new CyderButton("8");
        eight.setBorder(new LineBorder(phoneUtil.navy,5,false));
        CyderButton nine = new CyderButton("9");
        nine.setBorder(new LineBorder(phoneUtil.navy,5,false));
        CyderButton back = new CyderButton("<X");
        back.setBorder(new LineBorder(phoneUtil.navy,5,false));
        CyderButton dialNumber = new CyderButton("Call");
        dialNumber.setBorder(new LineBorder(phoneUtil.navy,5,false));

        ButtonsPanel.add(one);
        one.setColors(phoneUtil.regularRed);
        ButtonsPanel.add(two);
        two.setColors(phoneUtil.regularRed);
        ButtonsPanel.add(three);
        three.setColors(phoneUtil.regularRed);
        ButtonsPanel.add(four);
        four.setColors(phoneUtil.regularRed);
        ButtonsPanel.add(five);
        five.setColors(phoneUtil.regularRed);
        ButtonsPanel.add(six);
        six.setColors(phoneUtil.regularRed);
        ButtonsPanel.add(seven);
        seven.setColors(phoneUtil.regularRed);
        ButtonsPanel.add(eight);
        eight.setColors(phoneUtil.regularRed);
        ButtonsPanel.add(nine);
        nine.setColors(phoneUtil.regularRed);
        ButtonsPanel.add(dialNumber);
        dialNumber.setColors(phoneUtil.regularRed);
        ButtonsPanel.add(zero);
        zero.setColors(phoneUtil.regularRed);
        ButtonsPanel.add(back);
        back.setColors(phoneUtil.regularRed);
        MyPanel.add(topPanel);

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

        MyPanel.add(ButtonsPanel);

        phoneFrame.setIconImage(phoneUtil.getCyderIcon().getImage());

        phoneFrame.setVisible(true);

        phoneFrame.setAlwaysOnTop(true);

        phoneFrame.setAlwaysOnTop(false);

        one.addActionListener(e -> {
            phoneNum = phoneNum + "1";
            numberLabel.setText(phoneNumFormat(phoneNum));
            checkForSuicideHotline();
        });

        two.addActionListener(e -> {
            phoneNum = phoneNum + "2";
            numberLabel.setText(phoneNumFormat(phoneNum));
            checkForSuicideHotline();
        });

        three.addActionListener(e -> {
            phoneNum = phoneNum + "3";
            numberLabel.setText(phoneNumFormat(phoneNum));
            checkForSuicideHotline();
        });

        four.addActionListener(e -> {
            phoneNum = phoneNum + "4";
            numberLabel.setText(phoneNumFormat(phoneNum));
            checkForSuicideHotline();
        });

        five.addActionListener(e -> {
            phoneNum = phoneNum + "5";
            numberLabel.setText(phoneNumFormat(phoneNum));
            checkForSuicideHotline();
        });

        six.addActionListener(e -> {
            phoneNum = phoneNum + "6";
            numberLabel.setText(phoneNumFormat(phoneNum));
            checkForSuicideHotline();
        });

        seven.addActionListener(e -> {
            phoneNum = phoneNum + "7";
            numberLabel.setText(phoneNumFormat(phoneNum));
            checkForSuicideHotline();
        });

        eight.addActionListener(e -> {
            phoneNum = phoneNum + "8";
            numberLabel.setText(phoneNumFormat(phoneNum));
            checkForSuicideHotline();
        });

        nine.addActionListener(e -> {
            phoneNum = phoneNum + "9";
            numberLabel.setText(phoneNumFormat(phoneNum));
            checkForSuicideHotline();
        });

        zero.addActionListener(e -> {
            phoneNum = phoneNum + "0";
            numberLabel.setText(phoneNumFormat(phoneNum));
            checkForSuicideHotline();
        });

        back.addActionListener(e -> {
            if (phoneNum.length() > 0) {
                phoneNum = phoneNum.substring(0, phoneNum.length() - 1);
                numberLabel.setText(phoneNumFormat(phoneNum));
                checkForSuicideHotline();
            }
        });

        one.setBackground(new Color(223, 85, 83));

        one.setFont(phoneUtil.weatherFontBig);

        two.setFocusPainted(false);

        two.setBackground(new Color(223, 85, 83));

        two.setFont(phoneUtil.weatherFontBig);

        three.setFocusPainted(false);

        three.setBackground(new Color(223, 85, 83));

        three.setFont(phoneUtil.weatherFontBig);

        four.setFocusPainted(false);

        four.setBackground(new Color(223, 85, 83));

        four.setFont(phoneUtil.weatherFontBig);

        five.setFocusPainted(false);

        five.setBackground(new Color(223, 85, 83));

        five.setFont(phoneUtil.weatherFontBig);

        six.setFocusPainted(false);

        six.setBackground(new Color(223, 85, 83));

        six.setFont(phoneUtil.weatherFontBig);

        seven.setFocusPainted(false);

        seven.setBackground(new Color(223, 85, 83));

        seven.setFont(phoneUtil.weatherFontBig);

        eight.setFocusPainted(false);

        eight.setBackground(new Color(223, 85, 83));

        eight.setFont(phoneUtil.weatherFontBig);

        nine.setFocusPainted(false);

        nine.setBackground(new Color(223, 85, 83));

        nine.setFont(phoneUtil.weatherFontBig);

        zero.setFocusPainted(false);

        zero.setBackground(new Color(223, 85, 83));

        zero.setFont(phoneUtil.weatherFontBig);

        back.setFocusPainted(false);

        back.setBackground(new Color(223, 85, 83));

        back.setFont(phoneUtil.weatherFontBig);

        dialNumber.setFocusPainted(false);

        dialNumber.setBackground(new Color(223, 85, 83));

        dialNumber.setFont(phoneUtil.weatherFontBig);

        dialNumber.addActionListener(e -> {
            if (phoneNum.length() > 0) {
                phoneUtil.inform("Dialing: " + phoneNum,"", 200, 200);
                phoneNum = "";
            }
        });

        phoneFrame.pack();
        phoneFrame.setVisible(true);
        phoneFrame.setResizable(false);
        phoneFrame.setLocationRelativeTo(null);
    }

    private String phoneNumFormat(String num) {
        num = num.replaceAll("[^\\d.]", "");
        int len = num.length();

        if (len == 0) {
            return "#";
        }

        else if (len > 0 && len <= 4) {
            return num;
        }

        else if (len == 5) {
            return (num.substring(0,1) + "-" + num.substring(1,len));
        }

        else if (len == 6) {
            return (num.substring(0,2) + "-" + num.substring(2,len));
        }

        else if (len == 7) {
            return (num.substring(0,3) + "-" + num.substring(3,len));
        }

        else if (len == 8) {
            return ("(" + num.substring(0,1) + ") " + num.substring(1,4) + " " + num.substring(4,len));
        }

        else if (len == 9) {
            return ("(" + num.substring(0,2) + ") " + num.substring(2,5) + " " + num.substring(5,len));
        }

        else if (len == 10) {
            return ("(" + num.substring(0,3) + ") " + num.substring(3,6) + " " + num.substring(6,len));
        }

        else if (len > 10) {
            String leadingDigits = num.substring(0, len - 10);
            int offset = leadingDigits.length();

            return (leadingDigits + " (" + num.substring(offset,3 + offset) + ") " + num.substring(3 + offset,6 + offset) + " " + num.substring(6 + offset,len));
        }

        else {
            return null;
        }
    }

    private void checkForSuicideHotline() {
        String num = numberLabel.getText().replace("-","").replace("(","").replace(")","").replace(" ","").trim();
        if (num.equals("18002738255")) {
            phoneUtil.playMusic("src\\com\\cyder\\io\\audio\\1800.mp3");
        }
    }
}
