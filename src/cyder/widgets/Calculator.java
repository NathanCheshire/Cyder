package cyder.widgets;

import com.fathzer.soft.javaluator.DoubleEvaluator;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.handler.ErrorHandler;
import cyder.ui.CyderButton;
import cyder.ui.CyderFrame;
import cyder.ui.CyderTextField;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

import static cyder.constants.CyderStrings.DEFAULT_BACKGROUND_PATH;

public class Calculator {
    private String calculatorExpression = "";
    
    public Calculator() {
        calculatorExpression = "";

        CyderFrame calculatorFrame = new CyderFrame(600,600,new ImageIcon(DEFAULT_BACKGROUND_PATH));
        calculatorFrame.setTitle("Calculator");

        CyderTextField calculatorField = new CyderTextField(20);
        calculatorField.setBackground(Color.WHITE);
        calculatorField.setCharLimit(Integer.MAX_VALUE);
        calculatorField.setSelectionColor(CyderColors.selectionColor);
        calculatorField.setToolTipText("Use radians and not degree for any trig functions");
        calculatorField.setFont(CyderFonts.weatherFontSmall.deriveFont(26));
        calculatorField.setBounds(100,40,380,50);
        calculatorFrame.getContentPane().add(calculatorField);

        CyderButton calculatorAdd = new CyderButton("+");
        calculatorAdd.setColors(CyderColors.calculatorOrange);
        calculatorAdd.setBorder(new LineBorder(CyderColors.navy,5,false));
        calculatorAdd.setBounds(100,100,80,80);
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
        calculatorSubtract.setBounds(200,100,80,80);
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
        calculatorMultiply.setBounds(300,100,80,80);
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
        calculatorDivide.setBounds(400,100,80,80);
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
        calculatorSeven.setBounds(100,200,80,80);
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
        calculatorEight.setBounds(200,200,80,80);
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
        calculatorNine.setBounds(300,200,80,80);
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
        calculatorEquals.setBounds(400,200,80,80);
        calculatorFrame.getContentPane().add(calculatorEquals);
        calculatorEquals.setFocusPainted(false);
        calculatorEquals.setBackground(CyderColors.calculatorOrange);
        calculatorEquals.setFont(CyderFonts.weatherFontBig);
        calculatorEquals.addActionListener(e -> {
            try {
                 calculatorFrame.notify(String.valueOf(new DoubleEvaluator().evaluate(calculatorField.getText().trim())));
            } catch (Exception exc) {
                calculatorFrame.notify("<html>Could not parse expression. " +
                        "Please use multiplication signs after parenthesis " +
                        "and check the exact syntax of your expression for common" +
                        " errors such as missing delimiters. Note that this calculator " +
                        "does support typing in the" +
                        " Text Field and can handle more complicated" +
                        "expressions such as sin, cos, tan, log, ln, floor, etc. " +
                        "Use \"pi\" for pi.</html>");
                ErrorHandler.silentHandle(exc);
            }
        });
        calculatorField.addActionListener(e -> calculatorEquals.doClick());

        CyderButton calculatorFour = new CyderButton("4");
        calculatorFour.setColors(CyderColors.calculatorOrange);
        calculatorFour.setBorder(new LineBorder(CyderColors.navy,5,false));
        calculatorFour.setBounds(100,300,80,80);
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
        calculatorFive.setBounds(200,300,80,80);
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
        calculatorSix.setBounds(300,300,80,80);
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
        calculatorClear.setBounds(400,300,80,80);
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
        calculatorOne.setBounds(100,400,80,80);
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
        calculatorTwo.setBounds(200,400,80,80);
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
        calculatorThree.setBounds(300,400,80,80);
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
        calculatorUndo.setBounds(400,400,80,80);
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
        calculatorZero.setBounds(100,500,80,80);
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
        calculatorDecimal.setBounds(200,500,80,80);
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
        calculatorOpenP.setBounds(300,500,80,80);
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
        calculatorCloseP.setBounds(400,500,80,80);
        calculatorFrame.getContentPane().add(calculatorCloseP);
        calculatorCloseP.setFocusPainted(false);
        calculatorCloseP.setBackground(CyderColors.calculatorOrange);
        calculatorCloseP.setFont(CyderFonts.weatherFontBig);
        calculatorCloseP.addActionListener(e -> {
            calculatorExpression += ")";
            calculatorField.setText(calculatorExpression);
        });

        calculatorFrame.setLocationRelativeTo(null);
        calculatorFrame.setVisible(true);
    }
}
