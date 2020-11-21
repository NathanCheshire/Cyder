package com.cyder.utilities;

import com.cyder.ui.CyderButton;
import com.cyder.ui.CyderFrame;

import javax.swing.*;
import javax.swing.border.LineBorder;

public class Phone {
    private JLabel numberLabel;
    private String phoneNum;

    private GeneralUtil phoneGeneralUtil = new GeneralUtil();

    public Phone() {
        CyderFrame phoneFrame = new CyderFrame(320,500,new ImageIcon("src\\com\\cyder\\io\\pictures\\DebugBackground.png"));
        phoneFrame.setTitle("Phone");

        numberLabel = new JLabel("#");
        numberLabel.setFont(phoneGeneralUtil.weatherFontSmall);
        numberLabel.setBorder(new LineBorder(phoneGeneralUtil.navy,5,false));

        CyderButton zero = new CyderButton("0");
        zero.setBorder(new LineBorder(phoneGeneralUtil.navy,5,false));
        CyderButton one = new CyderButton("1");
        one.setBorder(new LineBorder(phoneGeneralUtil.navy,5,false));
        CyderButton two = new CyderButton("2");
        two.setBorder(new LineBorder(phoneGeneralUtil.navy,5,false));
        CyderButton three = new CyderButton("3");
        three.setBorder(new LineBorder(phoneGeneralUtil.navy,5,false));
        CyderButton four = new CyderButton("4");
        four.setBorder(new LineBorder(phoneGeneralUtil.navy,5,false));
        CyderButton five = new CyderButton("5");
        five.setBorder(new LineBorder(phoneGeneralUtil.navy,5,false));
        CyderButton six = new CyderButton("6");
        six.setBorder(new LineBorder(phoneGeneralUtil.navy,5,false));
        CyderButton seven = new CyderButton("7");
        seven.setBorder(new LineBorder(phoneGeneralUtil.navy,5,false));
        CyderButton eight = new CyderButton("8");
        eight.setBorder(new LineBorder(phoneGeneralUtil.navy,5,false));
        CyderButton nine = new CyderButton("9");
        nine.setBorder(new LineBorder(phoneGeneralUtil.navy,5,false));
        CyderButton back = new CyderButton("<X");
        back.setBorder(new LineBorder(phoneGeneralUtil.navy,5,false));
        CyderButton dialNumber = new CyderButton("Call");
        dialNumber.setBorder(new LineBorder(phoneGeneralUtil.navy,5,false));

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

        one.setColors(phoneGeneralUtil.regularRed);
        two.setColors(phoneGeneralUtil.regularRed);
        three.setColors(phoneGeneralUtil.regularRed);
        four.setColors(phoneGeneralUtil.regularRed);
        five.setColors(phoneGeneralUtil.regularRed);
        six.setColors(phoneGeneralUtil.regularRed);
        seven.setColors(phoneGeneralUtil.regularRed);
        eight.setColors(phoneGeneralUtil.regularRed);
        nine.setColors(phoneGeneralUtil.regularRed);
        dialNumber.setColors(phoneGeneralUtil.regularRed);
        zero.setColors(phoneGeneralUtil.regularRed);
        back.setColors(phoneGeneralUtil.regularRed);

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

        one.setBackground(phoneGeneralUtil.calculatorOrange);
        one.setFont(phoneGeneralUtil.weatherFontBig);

        two.setFocusPainted(false);
        two.setBackground(phoneGeneralUtil.calculatorOrange);
        two.setFont(phoneGeneralUtil.weatherFontBig);

        three.setFocusPainted(false);
        three.setBackground(phoneGeneralUtil.calculatorOrange);
        three.setFont(phoneGeneralUtil.weatherFontBig);

        four.setFocusPainted(false);
        four.setBackground(phoneGeneralUtil.calculatorOrange);
        four.setFont(phoneGeneralUtil.weatherFontBig);

        five.setFocusPainted(false);
        five.setBackground(phoneGeneralUtil.calculatorOrange);
        five.setFont(phoneGeneralUtil.weatherFontBig);

        six.setFocusPainted(false);
        six.setBackground(phoneGeneralUtil.calculatorOrange);
        six.setFont(phoneGeneralUtil.weatherFontBig);

        seven.setFocusPainted(false);
        seven.setBackground(phoneGeneralUtil.calculatorOrange);
        seven.setFont(phoneGeneralUtil.weatherFontBig);

        eight.setFocusPainted(false);
        eight.setBackground(phoneGeneralUtil.calculatorOrange);
        eight.setFont(phoneGeneralUtil.weatherFontBig);

        nine.setFocusPainted(false);
        nine.setBackground(phoneGeneralUtil.calculatorOrange);
        nine.setFont(phoneGeneralUtil.weatherFontBig);

        zero.setFocusPainted(false);
        zero.setBackground(phoneGeneralUtil.calculatorOrange);
        zero.setFont(phoneGeneralUtil.weatherFontBig);

        back.setFocusPainted(false);
        back.setBackground(phoneGeneralUtil.calculatorOrange);
        back.setFont(phoneGeneralUtil.weatherFontBig);

        dialNumber.setFocusPainted(false);
        dialNumber.setBackground(phoneGeneralUtil.calculatorOrange);
        dialNumber.setFont(phoneGeneralUtil.weatherFontBig);
        dialNumber.addActionListener(e -> {
            if (phoneNum.length() > 0) {
                checkFor223();
                checkForSuicideHotline();

                if (checkForSuicideHotline()) {
                    phoneGeneralUtil.playMusic("src\\com\\cyder\\io\\audio\\1800.mp3");
                }

                else if (checkFor223()) {
                    phoneGeneralUtil.playMusic("src\\com\\cyder\\io\\audio\\223.mp3");
                }

                else {
                    phoneGeneralUtil.inform("Dialing: " + numberLabel.getText(),"", 700, 300);
                    phoneNum = "";
                }
            }
        });

        phoneFrame.setLocationRelativeTo(null);
        phoneFrame.setVisible(true);
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
            if (len > 15) {
                phoneNum = numberLabel.getText();
                return numberLabel.getText();
            }


            String leadingDigits = num.substring(0, len - 10);
            int offset = leadingDigits.length();

            return (leadingDigits + " (" + num.substring(offset,3 + offset) + ") " + num.substring(3 + offset,6 + offset) + " " + num.substring(6 + offset,len));
        }

        else {
            return null;
        }
    }

    private boolean checkForSuicideHotline() {
        String num = numberLabel.getText().replace("-","").replace("(","").replace(")","").replace(" ","").trim();
        return num.equals("18002738255");
    }

    private boolean checkFor223() {
        String num = numberLabel.getText().replace("-","").replace("(","").replace(")","").replace(" ","").trim();
        return num.equals("223");
    }
}
