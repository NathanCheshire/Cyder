package cyder.widgets;

import com.fathzer.soft.javaluator.DoubleEvaluator;
import cyder.annotations.Widget;
import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.consts.CyderIcons;
import cyder.consts.CyderStrings;
import cyder.enums.NotificationDirection;
import cyder.genesis.GenesisShare;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.SessionHandler;
import cyder.ui.CyderButton;
import cyder.ui.CyderFrame;
import cyder.ui.CyderTextField;

import javax.swing.border.LineBorder;
import java.awt.*;

public class CalculatorWidget implements WidgetBase {
    private static String calculatorExpression = "";

    private CalculatorWidget() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    @Widget(trigger = "calculator", description = "A calculator widget capable of performing complex expressions such as e^x, sinx, cosx, and so forth.")
    public static void showGUI() {
        SessionHandler.log(SessionHandler.Tag.WIDGET_OPENED, "CALCULATOR");

        calculatorExpression = "";

        CyderFrame calculatorFrame = new CyderFrame(400,595, CyderIcons.defaultBackground);
        calculatorFrame.setTitle("Calculator");

        CyderTextField calculatorField = new CyderTextField(0);
        calculatorField.setBackground(Color.WHITE);
        calculatorField.setSelectionColor(CyderColors.selectionColor);
        calculatorField.setToolTipText("Use radians and not degrees for any trig functions");
        calculatorField.setFont(CyderFonts.segoe30);
        calculatorField.setBounds(50,50,300,50);
        calculatorFrame.getContentPane().add(calculatorField);

        CyderButton calculatorAdd = new CyderButton("+");
        calculatorAdd.setColors(CyderColors.regularOrange);
        calculatorAdd.setBorder(new LineBorder(CyderColors.navy,5,false));
        calculatorAdd.setBounds(20,120,75,75);
        calculatorFrame.getContentPane().add(calculatorAdd);
        calculatorAdd.setFocusPainted(false);
        calculatorAdd.setBackground(CyderColors.regularOrange);
        calculatorAdd.setFont(CyderFonts.segoe30);
        calculatorAdd.addActionListener(e -> {
            calculatorExpression += "+";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorSubtract = new CyderButton("-");
        calculatorSubtract.setColors(CyderColors.regularOrange);
        calculatorSubtract.setBorder(new LineBorder(CyderColors.navy,5,false));
        calculatorSubtract.setBounds(115,120,75,75);
        calculatorFrame.getContentPane().add(calculatorSubtract);
        calculatorSubtract.setFocusPainted(false);
        calculatorSubtract.setBackground(CyderColors.regularOrange);
        calculatorSubtract.setFont(CyderFonts.segoe30);
        calculatorSubtract.addActionListener(e -> {
            calculatorExpression += "-";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorMultiply = new CyderButton("*");
        calculatorMultiply.setColors(CyderColors.regularOrange);
        calculatorMultiply.setBorder(new LineBorder(CyderColors.navy,5,false));
        calculatorMultiply.setBounds(210,120,75,75);
        calculatorFrame.getContentPane().add(calculatorMultiply);
        calculatorMultiply.setFocusPainted(false);
        calculatorMultiply.setBackground(CyderColors.regularOrange);
        calculatorMultiply.setFont(CyderFonts.segoe30);
        calculatorMultiply.addActionListener(e -> {
            calculatorExpression += "*";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorDivide = new CyderButton("/");
        calculatorDivide.setColors(CyderColors.regularOrange);
        calculatorDivide.setBorder(new LineBorder(CyderColors.navy,5,false));
        calculatorDivide.setBounds(305,120,75,75);
        calculatorFrame.getContentPane().add(calculatorDivide);
        calculatorDivide.setFocusPainted(false);
        calculatorDivide.setBackground(CyderColors.regularOrange);
        calculatorDivide.setFont(CyderFonts.segoe30);
        calculatorDivide.addActionListener(e -> {
            calculatorExpression += "/";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorSeven = new CyderButton("7");
        calculatorSeven.setColors(CyderColors.regularOrange);
        calculatorSeven.setBorder(new LineBorder(CyderColors.navy,5,false));
        calculatorSeven.setBounds(20,215,75,75);
        calculatorFrame.getContentPane().add(calculatorSeven);
        calculatorSeven.setFocusPainted(false);
        calculatorSeven.setBackground(CyderColors.regularOrange);
        calculatorSeven.setFont(CyderFonts.segoe30);
        calculatorSeven.addActionListener(e -> {
            calculatorExpression += "7";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorEight = new CyderButton("8");
        calculatorEight.setColors(CyderColors.regularOrange);
        calculatorEight.setBorder(new LineBorder(CyderColors.navy,5,false));
        calculatorEight.setBounds(115,215,75,75);
        calculatorFrame.getContentPane().add(calculatorEight);
        calculatorEight.setFocusPainted(false);
        calculatorEight.setBackground(CyderColors.regularOrange);
        calculatorEight.setFont(CyderFonts.segoe30);
        calculatorEight.addActionListener(e -> {
            calculatorExpression += "8";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorNine = new CyderButton("9");
        calculatorNine.setColors(CyderColors.regularOrange);
        calculatorNine.setBorder(new LineBorder(CyderColors.navy,5,false));
        calculatorNine.setBounds(210,215,75,75);
        calculatorFrame.getContentPane().add(calculatorNine);
        calculatorNine.setFocusPainted(false);
        calculatorNine.setBackground(CyderColors.regularOrange);
        calculatorNine.setFont(CyderFonts.segoe30);
        calculatorNine.addActionListener(e -> {
            calculatorExpression += "9";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorEquals = new CyderButton("=");
        calculatorEquals.setColors(CyderColors.regularOrange);
        calculatorEquals.setBorder(new LineBorder(CyderColors.navy,5,false));
        calculatorEquals.setBounds(305,215,75,75);
        calculatorFrame.getContentPane().add(calculatorEquals);
        calculatorEquals.setFocusPainted(false);
        calculatorEquals.setBackground(CyderColors.regularOrange);
        calculatorEquals.setFont(CyderFonts.segoe30);
        calculatorEquals.addActionListener(e -> {
            try {
                String calcText = calculatorField.getText().trim();
                double result = new DoubleEvaluator().evaluate(calcText);

                if (result == Double.MAX_VALUE) {
                    calculatorFrame.notify("Positive Inf",-1, NotificationDirection.TOP_RIGHT, null);
                } else if (result == Double.MIN_VALUE) {
                    calculatorFrame.notify("Negative Inf",-1,NotificationDirection.TOP_RIGHT, null);
                } else {
                    calculatorFrame.notify(String.valueOf(result),-1,NotificationDirection.TOP_RIGHT, null);
                }
            } catch (Exception exc) {
                calculatorFrame.notify("Could not parse expression",2000, NotificationDirection.TOP_RIGHT, null);
                ExceptionHandler.silentHandle(exc);
            }
        });
        calculatorField.addActionListener(e -> calculatorEquals.doClick());

        CyderButton calculatorFour = new CyderButton("4");
        calculatorFour.setColors(CyderColors.regularOrange);
        calculatorFour.setBorder(new LineBorder(CyderColors.navy,5,false));
        calculatorFour.setBounds(20,310,75,75);
        calculatorFrame.getContentPane().add(calculatorFour);
        calculatorFour.setFocusPainted(false);
        calculatorFour.setBackground(CyderColors.regularOrange);
        calculatorFour.setFont(CyderFonts.segoe30);
        calculatorFour.addActionListener(e -> {
            calculatorExpression += "4";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorFive = new CyderButton("5");
        calculatorFive.setColors(CyderColors.regularOrange);
        calculatorFive.setBorder(new LineBorder(CyderColors.navy,5,false));
        calculatorFive.setBounds(115,310,75,75);
        calculatorFrame.getContentPane().add(calculatorFive);
        calculatorFive.setFocusPainted(false);
        calculatorFive.setBackground(CyderColors.regularOrange);
        calculatorFive.setFont(CyderFonts.segoe30);
        calculatorFive.addActionListener(e -> {
            calculatorExpression += "5";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorSix = new CyderButton("6");
        calculatorSix.setColors(CyderColors.regularOrange);
        calculatorSix.setBorder(new LineBorder(CyderColors.navy,5,false));
        calculatorSix.setBounds(210,310,75,75);
        calculatorFrame.getContentPane().add(calculatorSix);
        calculatorSix.setFocusPainted(false);
        calculatorSix.setBackground(CyderColors.regularOrange);
        calculatorSix.setFont(CyderFonts.segoe30);
        calculatorSix.addActionListener(e -> {
            calculatorExpression += "6";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorClear = new CyderButton("CE");
        calculatorClear.setColors(CyderColors.regularOrange);
        calculatorClear.setBorder(new LineBorder(CyderColors.navy,5,false));
        calculatorClear.setBounds(305,310,75,75);
        calculatorFrame.getContentPane().add(calculatorClear);
        calculatorClear.setFocusPainted(false);
        calculatorClear.setBackground(CyderColors.regularOrange);
        calculatorClear.setFont(CyderFonts.segoe30);
        calculatorClear.addActionListener(e -> {
            calculatorExpression = "";
            calculatorField.setText("");
        });

        CyderButton calculatorOne = new CyderButton("1");
        calculatorOne.setColors(CyderColors.regularOrange);
        calculatorOne.setBorder(new LineBorder(CyderColors.navy,5,false));
        calculatorOne.setBounds(20,405,75,75);
        calculatorFrame.getContentPane().add(calculatorOne);
        calculatorOne.setFocusPainted(false);
        calculatorOne.setBackground(CyderColors.regularOrange);
        calculatorOne.setFont(CyderFonts.segoe30);
        calculatorOne.addActionListener(e -> {
            calculatorExpression += "1";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorTwo = new CyderButton("2");
        calculatorTwo.setColors(CyderColors.regularOrange);
        calculatorTwo.setBorder(new LineBorder(CyderColors.navy,5,false));
        calculatorTwo.setBounds(115,405,75,75);
        calculatorFrame.getContentPane().add(calculatorTwo);
        calculatorTwo.setFocusPainted(false);
        calculatorTwo.setBackground(CyderColors.regularOrange);
        calculatorTwo.setFont(CyderFonts.segoe30);
        calculatorTwo.addActionListener(e -> {
            calculatorExpression += "2";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorThree = new CyderButton("3");
        calculatorThree.setColors(CyderColors.regularOrange);
        calculatorThree.setBorder(new LineBorder(CyderColors.navy,5,false));
        calculatorThree.setBounds(210,405,75,75);
        calculatorFrame.getContentPane().add(calculatorThree);
        calculatorThree.setFocusPainted(false);
        calculatorThree.setBackground(CyderColors.regularOrange);
        calculatorThree.setFont(CyderFonts.segoe30);
        calculatorThree.addActionListener(e -> {
            calculatorExpression += "3";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorUndo = new CyderButton("<<");
        calculatorUndo.setColors(CyderColors.regularOrange);
        calculatorUndo.setBorder(new LineBorder(CyderColors.navy,5,false));
        calculatorUndo.setBounds(305,405,75,75);
        calculatorFrame.getContentPane().add(calculatorUndo);
        calculatorUndo.setFocusPainted(false);
        calculatorUndo.setBackground(CyderColors.regularOrange);
        calculatorUndo.setFont(CyderFonts.segoe30);
        calculatorUndo.addActionListener(e -> {
            calculatorExpression = (calculatorExpression == null || calculatorExpression.length() == 0)
                    ? "" : (calculatorExpression.substring(0, calculatorExpression.length() - 1));
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorZero = new CyderButton("0");
        calculatorZero.setColors(CyderColors.regularOrange);
        calculatorZero.setBorder(new LineBorder(CyderColors.navy,5,false));
        calculatorZero.setBounds(20,500,75,75);
        calculatorFrame.getContentPane().add(calculatorZero);
        calculatorZero.setFocusPainted(false);
        calculatorZero.setBackground(CyderColors.regularOrange);
        calculatorZero.setFont(CyderFonts.segoe30);
        calculatorZero.addActionListener(e -> {
            calculatorExpression += "0";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorDecimal = new CyderButton(".");
        calculatorDecimal.setColors(CyderColors.regularOrange);
        calculatorDecimal.setBorder(new LineBorder(CyderColors.navy,5,false));
        calculatorDecimal.setBounds(115,500,75,75);
        calculatorFrame.getContentPane().add(calculatorDecimal);
        calculatorDecimal.setFocusPainted(false);
        calculatorDecimal.setBackground(CyderColors.regularOrange);
        calculatorDecimal.setFont(CyderFonts.segoe30);
        calculatorDecimal.addActionListener(e -> {
            calculatorExpression += ".";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorOpenP = new CyderButton("(");
        calculatorOpenP.setColors(CyderColors.regularOrange);
        calculatorOpenP.setBorder(new LineBorder(CyderColors.navy,5,false));
        calculatorOpenP.setBounds(210,500,75,75);
        calculatorFrame.getContentPane().add(calculatorOpenP);
        calculatorOpenP.setFocusPainted(false);
        calculatorOpenP.setBackground(CyderColors.regularOrange);
        calculatorOpenP.setFont(CyderFonts.segoe30);
        calculatorOpenP.addActionListener(e -> {
            calculatorExpression += "(";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorCloseP = new CyderButton(")");
        calculatorCloseP.setColors(CyderColors.regularOrange);
        calculatorCloseP.setBorder(new LineBorder(CyderColors.navy,5,false));
        calculatorCloseP.setBounds(305,500,75,75);
        calculatorFrame.getContentPane().add(calculatorCloseP);
        calculatorCloseP.setFocusPainted(false);
        calculatorCloseP.setBackground(CyderColors.regularOrange);
        calculatorCloseP.setFont(CyderFonts.segoe30);
        calculatorCloseP.addActionListener(e -> {
            calculatorExpression += ")";
            calculatorField.setText(calculatorExpression);
        });

        calculatorFrame.setVisible(true);
        calculatorFrame.setLocationRelativeTo(GenesisShare.getDominantFrame());
    }
}
