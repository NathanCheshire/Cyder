package cyder.widgets;

import com.fathzer.soft.javaluator.DoubleEvaluator;
import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.consts.CyderImages;
import cyder.enums.Direction;
import cyder.genesis.GenesisShare;
import cyder.handler.ErrorHandler;
import cyder.ui.CyderButton;
import cyder.ui.CyderFrame;
import cyder.ui.CyderTextField;

import javax.swing.border.LineBorder;
import java.awt.*;

public class Calculator {
    private static String calculatorExpression = "";

    private Calculator() {}
    
    public static void showGUI() {
        calculatorExpression = "";

        CyderFrame calculatorFrame = new CyderFrame(400,595, CyderImages.defaultBackground);
        calculatorFrame.setTitle("Calculator");

        CyderTextField calculatorField = new CyderTextField(0);
        calculatorField.setBackground(Color.WHITE);
        calculatorField.setSelectionColor(CyderColors.selectionColor);
        calculatorField.setToolTipText("Use radians and not degrees for any trig functions");
        calculatorField.setFont(CyderFonts.weatherFontBig);
        calculatorField.setBounds(50,50,300,50);
        calculatorFrame.getContentPane().add(calculatorField);

        CyderButton calculatorAdd = new CyderButton("+");
        calculatorAdd.setColors(CyderColors.calculatorOrange);
        calculatorAdd.setBorder(new LineBorder(CyderColors.navy,5,false));
        calculatorAdd.setBounds(20,120,75,75);
        calculatorFrame.getContentPane().add(calculatorAdd);
        calculatorAdd.setFocusPainted(false);
        calculatorAdd.setBackground(CyderColors.calculatorOrange);
        calculatorAdd.setFont(CyderFonts.weatherFontBig);
        calculatorAdd.addActionListener(e -> {
            calculatorExpression += "+";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorSubtract = new CyderButton("-");
        calculatorSubtract.setColors(CyderColors.calculatorOrange);
        calculatorSubtract.setBorder(new LineBorder(CyderColors.navy,5,false));
        calculatorSubtract.setBounds(115,120,75,75);
        calculatorFrame.getContentPane().add(calculatorSubtract);
        calculatorSubtract.setFocusPainted(false);
        calculatorSubtract.setBackground(CyderColors.calculatorOrange);
        calculatorSubtract.setFont(CyderFonts.weatherFontBig);
        calculatorSubtract.addActionListener(e -> {
            calculatorExpression += "-";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorMultiply = new CyderButton("*");
        calculatorMultiply.setColors(CyderColors.calculatorOrange);
        calculatorMultiply.setBorder(new LineBorder(CyderColors.navy,5,false));
        calculatorMultiply.setBounds(210,120,75,75);
        calculatorFrame.getContentPane().add(calculatorMultiply);
        calculatorMultiply.setFocusPainted(false);
        calculatorMultiply.setBackground(CyderColors.calculatorOrange);
        calculatorMultiply.setFont(CyderFonts.weatherFontBig);
        calculatorMultiply.addActionListener(e -> {
            calculatorExpression += "*";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorDivide = new CyderButton("/");
        calculatorDivide.setColors(CyderColors.calculatorOrange);
        calculatorDivide.setBorder(new LineBorder(CyderColors.navy,5,false));
        calculatorDivide.setBounds(305,120,75,75);
        calculatorFrame.getContentPane().add(calculatorDivide);
        calculatorDivide.setFocusPainted(false);
        calculatorDivide.setBackground(CyderColors.calculatorOrange);
        calculatorDivide.setFont(CyderFonts.weatherFontBig);
        calculatorDivide.addActionListener(e -> {
            calculatorExpression += "/";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorSeven = new CyderButton("7");
        calculatorSeven.setColors(CyderColors.calculatorOrange);
        calculatorSeven.setBorder(new LineBorder(CyderColors.navy,5,false));
        calculatorSeven.setBounds(20,215,75,75);
        calculatorFrame.getContentPane().add(calculatorSeven);
        calculatorSeven.setFocusPainted(false);
        calculatorSeven.setBackground(CyderColors.calculatorOrange);
        calculatorSeven.setFont(CyderFonts.weatherFontBig);
        calculatorSeven.addActionListener(e -> {
            calculatorExpression += "7";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorEight = new CyderButton("8");
        calculatorEight.setColors(CyderColors.calculatorOrange);
        calculatorEight.setBorder(new LineBorder(CyderColors.navy,5,false));
        calculatorEight.setBounds(115,215,75,75);
        calculatorFrame.getContentPane().add(calculatorEight);
        calculatorEight.setFocusPainted(false);
        calculatorEight.setBackground(CyderColors.calculatorOrange);
        calculatorEight.setFont(CyderFonts.weatherFontBig);
        calculatorEight.addActionListener(e -> {
            calculatorExpression += "8";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorNine = new CyderButton("9");
        calculatorNine.setColors(CyderColors.calculatorOrange);
        calculatorNine.setBorder(new LineBorder(CyderColors.navy,5,false));
        calculatorNine.setBounds(210,215,75,75);
        calculatorFrame.getContentPane().add(calculatorNine);
        calculatorNine.setFocusPainted(false);
        calculatorNine.setBackground(CyderColors.calculatorOrange);
        calculatorNine.setFont(CyderFonts.weatherFontBig);
        calculatorNine.addActionListener(e -> {
            calculatorExpression += "9";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorEquals = new CyderButton("=");
        calculatorEquals.setColors(CyderColors.calculatorOrange);
        calculatorEquals.setBorder(new LineBorder(CyderColors.navy,5,false));
        calculatorEquals.setBounds(305,215,75,75);
        calculatorFrame.getContentPane().add(calculatorEquals);
        calculatorEquals.setFocusPainted(false);
        calculatorEquals.setBackground(CyderColors.calculatorOrange);
        calculatorEquals.setFont(CyderFonts.weatherFontBig);
        calculatorEquals.addActionListener(e -> {
            try {
                String calcText = calculatorField.getText().trim();
                double result = new DoubleEvaluator().evaluate(calcText);

                if (result == Double.MAX_VALUE) {
                    calculatorFrame.notify("Positive Inf",2000,Direction.RIGHT);
                } else if (result == Double.MIN_VALUE) {
                    calculatorFrame.notify("Negative Inf",2000,Direction.RIGHT);
                } else {
                    calculatorFrame.notify(String.valueOf(result),5000,Direction.RIGHT);
                }
            } catch (Exception exc) {
                calculatorFrame.notify("Could not parse expression",2000, Direction.RIGHT);
                ErrorHandler.silentHandle(exc);
            }
        });
        calculatorField.addActionListener(e -> calculatorEquals.doClick());

        CyderButton calculatorFour = new CyderButton("4");
        calculatorFour.setColors(CyderColors.calculatorOrange);
        calculatorFour.setBorder(new LineBorder(CyderColors.navy,5,false));
        calculatorFour.setBounds(20,310,75,75);
        calculatorFrame.getContentPane().add(calculatorFour);
        calculatorFour.setFocusPainted(false);
        calculatorFour.setBackground(CyderColors.calculatorOrange);
        calculatorFour.setFont(CyderFonts.weatherFontBig);
        calculatorFour.addActionListener(e -> {
            calculatorExpression += "4";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorFive = new CyderButton("5");
        calculatorFive.setColors(CyderColors.calculatorOrange);
        calculatorFive.setBorder(new LineBorder(CyderColors.navy,5,false));
        calculatorFive.setBounds(115,310,75,75);
        calculatorFrame.getContentPane().add(calculatorFive);
        calculatorFive.setFocusPainted(false);
        calculatorFive.setBackground(CyderColors.calculatorOrange);
        calculatorFive.setFont(CyderFonts.weatherFontBig);
        calculatorFive.addActionListener(e -> {
            calculatorExpression += "5";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorSix = new CyderButton("6");
        calculatorSix.setColors(CyderColors.calculatorOrange);
        calculatorSix.setBorder(new LineBorder(CyderColors.navy,5,false));
        calculatorSix.setBounds(210,310,75,75);
        calculatorFrame.getContentPane().add(calculatorSix);
        calculatorSix.setFocusPainted(false);
        calculatorSix.setBackground(CyderColors.calculatorOrange);
        calculatorSix.setFont(CyderFonts.weatherFontBig);
        calculatorSix.addActionListener(e -> {
            calculatorExpression += "6";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorClear = new CyderButton("CE");
        calculatorClear.setColors(CyderColors.calculatorOrange);
        calculatorClear.setBorder(new LineBorder(CyderColors.navy,5,false));
        calculatorClear.setBounds(305,310,75,75);
        calculatorFrame.getContentPane().add(calculatorClear);
        calculatorClear.setFocusPainted(false);
        calculatorClear.setBackground(CyderColors.calculatorOrange);
        calculatorClear.setFont(CyderFonts.weatherFontBig);
        calculatorClear.addActionListener(e -> {
            calculatorExpression = "";
            calculatorField.setText("");
        });

        CyderButton calculatorOne = new CyderButton("1");
        calculatorOne.setColors(CyderColors.calculatorOrange);
        calculatorOne.setBorder(new LineBorder(CyderColors.navy,5,false));
        calculatorOne.setBounds(20,405,75,75);
        calculatorFrame.getContentPane().add(calculatorOne);
        calculatorOne.setFocusPainted(false);
        calculatorOne.setBackground(CyderColors.calculatorOrange);
        calculatorOne.setFont(CyderFonts.weatherFontBig);
        calculatorOne.addActionListener(e -> {
            calculatorExpression += "1";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorTwo = new CyderButton("2");
        calculatorTwo.setColors(CyderColors.calculatorOrange);
        calculatorTwo.setBorder(new LineBorder(CyderColors.navy,5,false));
        calculatorTwo.setBounds(115,405,75,75);
        calculatorFrame.getContentPane().add(calculatorTwo);
        calculatorTwo.setFocusPainted(false);
        calculatorTwo.setBackground(CyderColors.calculatorOrange);
        calculatorTwo.setFont(CyderFonts.weatherFontBig);
        calculatorTwo.addActionListener(e -> {
            calculatorExpression += "2";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorThree = new CyderButton("3");
        calculatorThree.setColors(CyderColors.calculatorOrange);
        calculatorThree.setBorder(new LineBorder(CyderColors.navy,5,false));
        calculatorThree.setBounds(210,405,75,75);
        calculatorFrame.getContentPane().add(calculatorThree);
        calculatorThree.setFocusPainted(false);
        calculatorThree.setBackground(CyderColors.calculatorOrange);
        calculatorThree.setFont(CyderFonts.weatherFontBig);
        calculatorThree.addActionListener(e -> {
            calculatorExpression += "3";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorUndo = new CyderButton("<<");
        calculatorUndo.setColors(CyderColors.calculatorOrange);
        calculatorUndo.setBorder(new LineBorder(CyderColors.navy,5,false));
        calculatorUndo.setBounds(305,405,75,75);
        calculatorFrame.getContentPane().add(calculatorUndo);
        calculatorUndo.setFocusPainted(false);
        calculatorUndo.setBackground(CyderColors.calculatorOrange);
        calculatorUndo.setFont(CyderFonts.weatherFontBig);
        calculatorUndo.addActionListener(e -> {
            calculatorExpression = (calculatorExpression == null || calculatorExpression.length() == 0)
                    ? "" : (calculatorExpression.substring(0, calculatorExpression.length() - 1));
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorZero = new CyderButton("0");
        calculatorZero.setColors(CyderColors.calculatorOrange);
        calculatorZero.setBorder(new LineBorder(CyderColors.navy,5,false));
        calculatorZero.setBounds(20,500,75,75);
        calculatorFrame.getContentPane().add(calculatorZero);
        calculatorZero.setFocusPainted(false);
        calculatorZero.setBackground(CyderColors.calculatorOrange);
        calculatorZero.setFont(CyderFonts.weatherFontBig);
        calculatorZero.addActionListener(e -> {
            calculatorExpression += "0";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorDecimal = new CyderButton(".");
        calculatorDecimal.setColors(CyderColors.calculatorOrange);
        calculatorDecimal.setBorder(new LineBorder(CyderColors.navy,5,false));
        calculatorDecimal.setBounds(115,500,75,75);
        calculatorFrame.getContentPane().add(calculatorDecimal);
        calculatorDecimal.setFocusPainted(false);
        calculatorDecimal.setBackground(CyderColors.calculatorOrange);
        calculatorDecimal.setFont(CyderFonts.weatherFontBig);
        calculatorDecimal.addActionListener(e -> {
            calculatorExpression += ".";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorOpenP = new CyderButton("(");
        calculatorOpenP.setColors(CyderColors.calculatorOrange);
        calculatorOpenP.setBorder(new LineBorder(CyderColors.navy,5,false));
        calculatorOpenP.setBounds(210,500,75,75);
        calculatorFrame.getContentPane().add(calculatorOpenP);
        calculatorOpenP.setFocusPainted(false);
        calculatorOpenP.setBackground(CyderColors.calculatorOrange);
        calculatorOpenP.setFont(CyderFonts.weatherFontBig);
        calculatorOpenP.addActionListener(e -> {
            calculatorExpression += "(";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorCloseP = new CyderButton(")");
        calculatorCloseP.setColors(CyderColors.calculatorOrange);
        calculatorCloseP.setBorder(new LineBorder(CyderColors.navy,5,false));
        calculatorCloseP.setBounds(305,500,75,75);
        calculatorFrame.getContentPane().add(calculatorCloseP);
        calculatorCloseP.setFocusPainted(false);
        calculatorCloseP.setBackground(CyderColors.calculatorOrange);
        calculatorCloseP.setFont(CyderFonts.weatherFontBig);
        calculatorCloseP.addActionListener(e -> {
            calculatorExpression += ")";
            calculatorField.setText(calculatorExpression);
        });

        calculatorFrame.setVisible(true);
        calculatorFrame.setLocationRelativeTo(GenesisShare.getDominantFrame());
    }
}
