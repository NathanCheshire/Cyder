package cyder.widgets;

import cyder.annotations.CyderAuthor;
import cyder.annotations.Vanilla;
import cyder.annotations.Widget;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderIcons;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.ui.button.CyderButton;
import cyder.ui.frame.CyderFrame;
import cyder.utils.IoUtil;
import cyder.utils.StaticUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;

@Vanilla
@CyderAuthor
public final class PhoneWidget {
    /**
     * The label numbers are appended to.
     */
    private static JLabel numberLabel;

    /**
     * The current number.
     */
    private static String phoneNum;

    /**
     * Suppress default constructor.
     */
    private PhoneWidget() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    @Widget(triggers = "phone", description = "A phone emulating widget")
    public static void showGui() {
        CyderFrame phoneFrame = new CyderFrame(320, 500, CyderIcons.defaultBackground);
        phoneFrame.setTitle("Phone");

        numberLabel = new JLabel("#");
        numberLabel.setFont(CyderFonts.SEGOE_20);
        numberLabel.setBorder(new LineBorder(CyderColors.navy, 5, false));

        CyderButton zero = new CyderButton("0");
        zero.setBorder(new LineBorder(CyderColors.navy, 5, false));
        CyderButton one = new CyderButton("1");
        one.setBorder(new LineBorder(CyderColors.navy, 5, false));
        CyderButton two = new CyderButton("2");
        two.setBorder(new LineBorder(CyderColors.navy, 5, false));
        CyderButton three = new CyderButton("3");
        three.setBorder(new LineBorder(CyderColors.navy, 5, false));
        CyderButton four = new CyderButton("4");
        four.setBorder(new LineBorder(CyderColors.navy, 5, false));
        CyderButton five = new CyderButton("5");
        five.setBorder(new LineBorder(CyderColors.navy, 5, false));
        CyderButton six = new CyderButton("6");
        six.setBorder(new LineBorder(CyderColors.navy, 5, false));
        CyderButton seven = new CyderButton("7");
        seven.setBorder(new LineBorder(CyderColors.navy, 5, false));
        CyderButton eight = new CyderButton("8");
        eight.setBorder(new LineBorder(CyderColors.navy, 5, false));
        CyderButton nine = new CyderButton("9");
        nine.setBorder(new LineBorder(CyderColors.navy, 5, false));
        CyderButton back = new CyderButton("<<");
        back.setBorder(new LineBorder(CyderColors.navy, 5, false));
        CyderButton dialNumber = new CyderButton("Call");
        dialNumber.setBorder(new LineBorder(CyderColors.navy, 5, false));

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
            if (!phoneNum.isEmpty()) {
                phoneNum = phoneNum.substring(0, phoneNum.length() - 1);
                numberLabel.setText(phoneNumFormat(phoneNum));
            }
        });

        one.setBackground(CyderColors.regularOrange);
        one.setFont(CyderFonts.SEGOE_30);

        two.setFocusPainted(false);
        two.setBackground(CyderColors.regularOrange);
        two.setFont(CyderFonts.SEGOE_30);

        three.setFocusPainted(false);
        three.setBackground(CyderColors.regularOrange);
        three.setFont(CyderFonts.SEGOE_30);

        four.setFocusPainted(false);
        four.setBackground(CyderColors.regularOrange);
        four.setFont(CyderFonts.SEGOE_30);

        five.setFocusPainted(false);
        five.setBackground(CyderColors.regularOrange);
        five.setFont(CyderFonts.SEGOE_30);

        six.setFocusPainted(false);
        six.setBackground(CyderColors.regularOrange);
        six.setFont(CyderFonts.SEGOE_30);

        seven.setFocusPainted(false);
        seven.setBackground(CyderColors.regularOrange);
        seven.setFont(CyderFonts.SEGOE_30);

        eight.setFocusPainted(false);
        eight.setBackground(CyderColors.regularOrange);
        eight.setFont(CyderFonts.SEGOE_30);

        nine.setFocusPainted(false);
        nine.setBackground(CyderColors.regularOrange);
        nine.setFont(CyderFonts.SEGOE_30);

        zero.setFocusPainted(false);
        zero.setBackground(CyderColors.regularOrange);
        zero.setFont(CyderFonts.SEGOE_30);

        back.setFocusPainted(false);
        back.setBackground(CyderColors.regularOrange);
        back.setFont(CyderFonts.SEGOE_30);

        dialNumber.setFocusPainted(false);
        dialNumber.setBackground(CyderColors.regularOrange);
        dialNumber.setFont(CyderFonts.SEGOE_30);
        dialNumber.addActionListener(e -> {
            if (!phoneNum.isEmpty()) {
                // check for easter egg numbers
                if (checkForSuicideHotline()) {
                    IoUtil.playGeneralAudio(StaticUtil.getStaticResource("1800.mp3"));
                    return;
                } else if (checkForNumber("223")) {
                    IoUtil.playGeneralAudio(StaticUtil.getStaticResource("223.mp3"));
                    return;
                }

                phoneFrame.notify("Dialing: " + numberLabel.getText());
                phoneNum = "";
                numberLabel.setText(phoneNumFormat(phoneNum));
            }
        });

        phoneFrame.finalizeAndShow();
    }

    /**
     * Returns the number formatted based on the current number
     * of digits contained in the phone number.
     *
     * @param num the current phone number
     * @return the phone number formatted
     */
    private static String phoneNumFormat(String num) {
        num = num.replaceAll("[^\\d.]", "");
        int len = num.length();

        if (len == 0) {
            return "#";
        } else if (len <= 4) {
            return num;
        } else if (len == 5) {
            return (num.charAt(0) + "-" + num.substring(1, 5));
        } else if (len == 6) {
            return (num.substring(0, 2) + "-" + num.substring(2, 6));
        } else if (len == 7) {
            return (num.substring(0, 3) + "-" + num.substring(3, 7));
        } else if (len == 8) {
            return ("(" + num.charAt(0) + ") " + num.substring(1, 4) + " " + num.substring(4, 8));
        } else if (len == 9) {
            return ("(" + num.substring(0, 2) + ") " + num.substring(2, 5) + " " + num.substring(5, 9));
        } else if (len == 10) {
            return ("(" + num.substring(0, 3) + ") " + num.substring(3, 6) + " " + num.substring(6, 10));
        } else {
            if (len > 15) {
                phoneNum = numberLabel.getText();
                return numberLabel.getText();
            }

            String leadingDigits = num.substring(0, len - 10);
            int offset = leadingDigits.length();

            return (leadingDigits + " (" + num.substring(offset, 3 + offset) + ") "
                    + num.substring(3 + offset, 6 + offset) + " " + num.substring(6 + offset, len));
        }
    }

    /**
     * Checks for the current number equaling the provided raw number string.
     * The number passed in should only contain digits.
     *
     * @param numberString the digit string of the expected field text
     * @return whether the field is currently a form of the provided number string
     */
    private static boolean checkForNumber(String numberString) {
        String num = numberLabel.getText().replace("-", "")
                .replace("(", "").replace(")", "")
                .replace(" ", "").trim();
        return num.equals(numberString);
    }

    /**
     * Returns whether the current number is the USA's suicide hotline.
     *
     * @return whether the current number is the USA's suicide hotline
     */
    private static boolean checkForSuicideHotline() {
        return checkForNumber("18002738255");
    }
}
