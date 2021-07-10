package cyder.widgets;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.consts.CyderImages;
import cyder.ui.ConsoleFrame;
import cyder.ui.CyderButton;
import cyder.ui.CyderFrame;
import cyder.utilities.IOUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;

public class Phone {
    private JLabel numberLabel;
    private String phoneNum;

    public Phone(JTextPane outputArea) {
        CyderFrame phoneFrame = new CyderFrame(320,500, CyderImages.defaultBackground);
        phoneFrame.setTitle("Phone");

        numberLabel = new JLabel("#");
        numberLabel.setFont(CyderFonts.weatherFontSmall);
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

        one.setColors(CyderColors.regularRed);
        two.setColors(CyderColors.regularRed);
        three.setColors(CyderColors.regularRed);
        four.setColors(CyderColors.regularRed);
        five.setColors(CyderColors.regularRed);
        six.setColors(CyderColors.regularRed);
        seven.setColors(CyderColors.regularRed);
        eight.setColors(CyderColors.regularRed);
        nine.setColors(CyderColors.regularRed);
        dialNumber.setColors(CyderColors.regularRed);
        zero.setColors(CyderColors.regularRed);
        back.setColors(CyderColors.regularRed);

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

        one.setBackground(CyderColors.calculatorOrange);
        one.setFont(CyderFonts.weatherFontBig);

        two.setFocusPainted(false);
        two.setBackground(CyderColors.calculatorOrange);
        two.setFont(CyderFonts.weatherFontBig);

        three.setFocusPainted(false);
        three.setBackground(CyderColors.calculatorOrange);
        three.setFont(CyderFonts.weatherFontBig);

        four.setFocusPainted(false);
        four.setBackground(CyderColors.calculatorOrange);
        four.setFont(CyderFonts.weatherFontBig);

        five.setFocusPainted(false);
        five.setBackground(CyderColors.calculatorOrange);
        five.setFont(CyderFonts.weatherFontBig);

        six.setFocusPainted(false);
        six.setBackground(CyderColors.calculatorOrange);
        six.setFont(CyderFonts.weatherFontBig);

        seven.setFocusPainted(false);
        seven.setBackground(CyderColors.calculatorOrange);
        seven.setFont(CyderFonts.weatherFontBig);

        eight.setFocusPainted(false);
        eight.setBackground(CyderColors.calculatorOrange);
        eight.setFont(CyderFonts.weatherFontBig);

        nine.setFocusPainted(false);
        nine.setBackground(CyderColors.calculatorOrange);
        nine.setFont(CyderFonts.weatherFontBig);

        zero.setFocusPainted(false);
        zero.setBackground(CyderColors.calculatorOrange);
        zero.setFont(CyderFonts.weatherFontBig);

        back.setFocusPainted(false);
        back.setBackground(CyderColors.calculatorOrange);
        back.setFont(CyderFonts.weatherFontBig);

        dialNumber.setFocusPainted(false);
        dialNumber.setBackground(CyderColors.calculatorOrange);
        dialNumber.setFont(CyderFonts.weatherFontBig);
        dialNumber.addActionListener(e -> {
            if (phoneNum.length() > 0) {
                checkFor223();
                checkForSuicideHotline();

                if (checkForSuicideHotline()) {
                    IOUtil.playAudio("sys/audio/1800.mp3", outputArea);
                }

                else if (checkFor223()) {
                    IOUtil.playAudio("sys/audio/223.mp3", outputArea);
                }

                else {
                    GenericInform.inform("Dialing: " + numberLabel.getText(),"");
                    phoneNum = "";
                }
            }
        });

        ConsoleFrame.getConsoleFrame().setFrameRelativeTo(phoneFrame);
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
