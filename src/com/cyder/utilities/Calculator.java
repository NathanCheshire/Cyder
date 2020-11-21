package com.cyder.utilities;

import com.cyder.ui.CyderButton;
import com.cyder.ui.CyderFrame;
import com.fathzer.soft.javaluator.DoubleEvaluator;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;

public class Calculator {
    private String calculatorExpression = "";
    private GeneralUtil calculatorGeneralUtil = new GeneralUtil();
    
    public Calculator() {
        calculatorExpression = "";

        CyderFrame calculatorFrame = new CyderFrame(600,600,new ImageIcon("src\\com\\cyder\\io\\pictures\\DebugBackground.png"));
        calculatorFrame.setTitle("Calculator");

        JTextField calculatorField = new JTextField(20);
        calculatorField.setSelectionColor(calculatorGeneralUtil.selectionColor);
        calculatorField.setToolTipText("(rad not deg)");
        calculatorField.setOpaque(false);
        calculatorField.setText("");
        calculatorField.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10),
                new LineBorder(calculatorGeneralUtil.navy,5,false)));
        calculatorField.setFont(calculatorGeneralUtil.weatherFontBig);
        calculatorField.setBounds(10,30,580,60);
        calculatorFrame.getContentPane().add(calculatorField);

        CyderButton calculatorAdd = new CyderButton("+");
        calculatorAdd.setColors(calculatorGeneralUtil.calculatorOrange);
        calculatorAdd.setBorder(new LineBorder(calculatorGeneralUtil.navy,5,false));
        calculatorAdd.setBounds(100,100,80,80);
        calculatorFrame.getContentPane().add(calculatorAdd);
        calculatorAdd.setFocusPainted(false);
        calculatorAdd.setBackground(calculatorGeneralUtil.calculatorOrange);
        calculatorAdd.setFont(calculatorGeneralUtil.weatherFontBig);
        calculatorAdd.addActionListener(e -> {
            calculatorExpression += "+";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorSubtract = new CyderButton("-");
        calculatorSubtract.setColors(calculatorGeneralUtil.calculatorOrange);
        calculatorSubtract.setBorder(new LineBorder(calculatorGeneralUtil.navy,5,false));
        calculatorSubtract.setBounds(200,100,80,80);
        calculatorFrame.getContentPane().add(calculatorSubtract);
        calculatorSubtract.setFocusPainted(false);
        calculatorSubtract.setBackground(calculatorGeneralUtil.calculatorOrange);
        calculatorSubtract.setFont(calculatorGeneralUtil.weatherFontBig);
        calculatorSubtract.addActionListener(e -> {
            calculatorExpression += "-";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorMultiply = new CyderButton("*");
        calculatorMultiply.setColors(calculatorGeneralUtil.calculatorOrange);
        calculatorMultiply.setBorder(new LineBorder(calculatorGeneralUtil.navy,5,false));
        calculatorMultiply.setBounds(300,100,80,80);
        calculatorFrame.getContentPane().add(calculatorMultiply);
        calculatorMultiply.setFocusPainted(false);
        calculatorMultiply.setBackground(calculatorGeneralUtil.calculatorOrange);
        calculatorMultiply.setFont(calculatorGeneralUtil.weatherFontBig);
        calculatorMultiply.addActionListener(e -> {
            calculatorExpression += "*";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorDivide = new CyderButton("/");
        calculatorDivide.setColors(calculatorGeneralUtil.calculatorOrange);
        calculatorDivide.setBorder(new LineBorder(calculatorGeneralUtil.navy,5,false));
        calculatorDivide.setBounds(400,100,80,80);
        calculatorFrame.getContentPane().add(calculatorDivide);
        calculatorDivide.setFocusPainted(false);
        calculatorDivide.setBackground(calculatorGeneralUtil.calculatorOrange);
        calculatorDivide.setFont(calculatorGeneralUtil.weatherFontBig);
        calculatorDivide.addActionListener(e -> {
            calculatorExpression += "/";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorSeven = new CyderButton("7");
        calculatorSeven.setColors(calculatorGeneralUtil.calculatorOrange);
        calculatorSeven.setBorder(new LineBorder(calculatorGeneralUtil.navy,5,false));
        calculatorSeven.setBounds(100,200,80,80);
        calculatorFrame.getContentPane().add(calculatorSeven);
        calculatorSeven.setFocusPainted(false);
        calculatorSeven.setBackground(calculatorGeneralUtil.calculatorOrange);
        calculatorSeven.setFont(calculatorGeneralUtil.weatherFontBig);
        calculatorSeven.addActionListener(e -> {
            calculatorExpression += "7";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorEight = new CyderButton("8");
        calculatorEight.setColors(calculatorGeneralUtil.calculatorOrange);
        calculatorEight.setBorder(new LineBorder(calculatorGeneralUtil.navy,5,false));
        calculatorEight.setBounds(200,200,80,80);
        calculatorFrame.getContentPane().add(calculatorEight);
        calculatorEight.setFocusPainted(false);
        calculatorEight.setBackground(calculatorGeneralUtil.calculatorOrange);
        calculatorEight.setFont(calculatorGeneralUtil.weatherFontBig);
        calculatorEight.addActionListener(e -> {
            calculatorExpression += "8";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorNine = new CyderButton("9");
        calculatorNine.setColors(calculatorGeneralUtil.calculatorOrange);
        calculatorNine.setBorder(new LineBorder(calculatorGeneralUtil.navy,5,false));
        calculatorNine.setBounds(300,200,80,80);
        calculatorFrame.getContentPane().add(calculatorNine);
        calculatorNine.setFocusPainted(false);
        calculatorNine.setBackground(calculatorGeneralUtil.calculatorOrange);
        calculatorNine.setFont(calculatorGeneralUtil.weatherFontBig);
        calculatorNine.addActionListener(e -> {
            calculatorExpression += "9";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorEquals = new CyderButton("=");
        calculatorEquals.setColors(calculatorGeneralUtil.calculatorOrange);
        calculatorEquals.setBorder(new LineBorder(calculatorGeneralUtil.navy,5,false));
        calculatorEquals.setBounds(400,200,80,80);
        calculatorFrame.getContentPane().add(calculatorEquals);
        calculatorEquals.setFocusPainted(false);
        calculatorEquals.setBackground(calculatorGeneralUtil.calculatorOrange);
        calculatorEquals.setFont(calculatorGeneralUtil.weatherFontBig);
        calculatorEquals.addActionListener(e -> {
            try {
                 calculatorGeneralUtil.inform("Answer:<br/>" + new DoubleEvaluator().evaluate(calculatorField.getText().trim()), "Result", calculatorFrame.getWidth(),calculatorFrame.getHeight());
            }

            catch (Exception exc) {
                calculatorGeneralUtil.inform("Unrecognized expression. Please use multiplication signs after parenthesis and check the exact syntax of your expression for common" +
                        " errors such as missing delimiters.<br/>Note that this calculator does support typing in the Text Field and can handle more complicated" +
                        "<br/>expressions such as sin, cos, tan, log, ln, floor, etc.","", calculatorFrame.getWidth(), calculatorFrame.getHeight());
                calculatorGeneralUtil.handle(exc);
            }
        });
        calculatorField.addActionListener(e -> calculatorEquals.doClick());

        CyderButton calculatorFour = new CyderButton("4");
        calculatorFour.setColors(calculatorGeneralUtil.calculatorOrange);
        calculatorFour.setBorder(new LineBorder(calculatorGeneralUtil.navy,5,false));
        calculatorFour.setBounds(100,300,80,80);
        calculatorFrame.getContentPane().add(calculatorFour);
        calculatorFour.setFocusPainted(false);
        calculatorFour.setBackground(calculatorGeneralUtil.calculatorOrange);
        calculatorFour.setFont(calculatorGeneralUtil.weatherFontBig);
        calculatorFour.addActionListener(e -> {
            calculatorExpression += "4";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorFive = new CyderButton("5");
        calculatorFive.setColors(calculatorGeneralUtil.calculatorOrange);
        calculatorFive.setBorder(new LineBorder(calculatorGeneralUtil.navy,5,false));
        calculatorFive.setBounds(200,300,80,80);
        calculatorFrame.getContentPane().add(calculatorFive);
        calculatorFive.setFocusPainted(false);
        calculatorFive.setBackground(calculatorGeneralUtil.calculatorOrange);
        calculatorFive.setFont(calculatorGeneralUtil.weatherFontBig);
        calculatorFive.addActionListener(e -> {
            calculatorExpression += "5";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorSix = new CyderButton("6");
        calculatorSix.setColors(calculatorGeneralUtil.calculatorOrange);
        calculatorSix.setBorder(new LineBorder(calculatorGeneralUtil.navy,5,false));
        calculatorSix.setBounds(300,300,80,80);
        calculatorFrame.getContentPane().add(calculatorSix);
        calculatorSix.setFocusPainted(false);
        calculatorSix.setBackground(calculatorGeneralUtil.calculatorOrange);
        calculatorSix.setFont(calculatorGeneralUtil.weatherFontBig);
        calculatorSix.addActionListener(e -> {
            calculatorExpression += "6";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorClear = new CyderButton("CE");
        calculatorClear.setColors(calculatorGeneralUtil.calculatorOrange);
        calculatorClear.setBorder(new LineBorder(calculatorGeneralUtil.navy,5,false));
        calculatorClear.setBounds(400,300,80,80);
        calculatorFrame.getContentPane().add(calculatorClear);
        calculatorClear.setFocusPainted(false);
        calculatorClear.setBackground(calculatorGeneralUtil.calculatorOrange);
        calculatorClear.setFont(calculatorGeneralUtil.weatherFontBig);
        calculatorClear.addActionListener(e -> {
            calculatorExpression = "";
            calculatorField.setText("");
        });

        CyderButton calculatorOne = new CyderButton("1");
        calculatorOne.setColors(calculatorGeneralUtil.calculatorOrange);
        calculatorOne.setBorder(new LineBorder(calculatorGeneralUtil.navy,5,false));
        calculatorOne.setBounds(100,400,80,80);
        calculatorFrame.getContentPane().add(calculatorOne);
        calculatorOne.setFocusPainted(false);
        calculatorOne.setBackground(calculatorGeneralUtil.calculatorOrange);
        calculatorOne.setFont(calculatorGeneralUtil.weatherFontBig);
        calculatorOne.addActionListener(e -> {
            calculatorExpression += "1";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorTwo = new CyderButton("2");
        calculatorTwo.setColors(calculatorGeneralUtil.calculatorOrange);
        calculatorTwo.setBorder(new LineBorder(calculatorGeneralUtil.navy,5,false));
        calculatorTwo.setBounds(200,400,80,80);
        calculatorFrame.getContentPane().add(calculatorTwo);
        calculatorTwo.setFocusPainted(false);
        calculatorTwo.setBackground(calculatorGeneralUtil.calculatorOrange);
        calculatorTwo.setFont(calculatorGeneralUtil.weatherFontBig);
        calculatorTwo.addActionListener(e -> {
            calculatorExpression += "2";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorThree = new CyderButton("3");
        calculatorThree.setColors(calculatorGeneralUtil.calculatorOrange);
        calculatorThree.setBorder(new LineBorder(calculatorGeneralUtil.navy,5,false));
        calculatorThree.setBounds(300,400,80,80);
        calculatorFrame.getContentPane().add(calculatorThree);
        calculatorThree.setFocusPainted(false);
        calculatorThree.setBackground(calculatorGeneralUtil.calculatorOrange);
        calculatorThree.setFont(calculatorGeneralUtil.weatherFontBig);
        calculatorThree.addActionListener(e -> {
            calculatorExpression += "3";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorUndo = new CyderButton("<<");
        calculatorUndo.setColors(calculatorGeneralUtil.calculatorOrange);
        calculatorUndo.setBorder(new LineBorder(calculatorGeneralUtil.navy,5,false));
        calculatorUndo.setBounds(400,400,80,80);
        calculatorFrame.getContentPane().add(calculatorUndo);
        calculatorUndo.setFocusPainted(false);
        calculatorUndo.setBackground(calculatorGeneralUtil.calculatorOrange);
        calculatorUndo.setFont(calculatorGeneralUtil.weatherFontBig);
        calculatorUndo.addActionListener(e -> {
            calculatorExpression = (calculatorExpression == null || calculatorExpression.length() == 0)
                    ? "" : (calculatorExpression.substring(0, calculatorExpression.length() - 1));
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorZero = new CyderButton("0");
        calculatorZero.setColors(calculatorGeneralUtil.calculatorOrange);
        calculatorZero.setBorder(new LineBorder(calculatorGeneralUtil.navy,5,false));
        calculatorZero.setBounds(100,500,80,80);
        calculatorFrame.getContentPane().add(calculatorZero);
        calculatorZero.setFocusPainted(false);
        calculatorZero.setBackground(calculatorGeneralUtil.calculatorOrange);
        calculatorZero.setFont(calculatorGeneralUtil.weatherFontBig);
        calculatorZero.addActionListener(e -> {
            calculatorExpression += "0";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorDecimal = new CyderButton(".");
        calculatorDecimal.setColors(calculatorGeneralUtil.calculatorOrange);
        calculatorDecimal.setBorder(new LineBorder(calculatorGeneralUtil.navy,5,false));
        calculatorDecimal.setBounds(200,500,80,80);
        calculatorFrame.getContentPane().add(calculatorDecimal);
        calculatorDecimal.setFocusPainted(false);
        calculatorDecimal.setBackground(calculatorGeneralUtil.calculatorOrange);
        calculatorDecimal.setFont(calculatorGeneralUtil.weatherFontBig);
        calculatorDecimal.addActionListener(e -> {
            calculatorExpression += ".";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorOpenP = new CyderButton("(");
        calculatorOpenP.setColors(calculatorGeneralUtil.calculatorOrange);
        calculatorOpenP.setBorder(new LineBorder(calculatorGeneralUtil.navy,5,false));
        calculatorOpenP.setBounds(300,500,80,80);
        calculatorFrame.getContentPane().add(calculatorOpenP);
        calculatorOpenP.setFocusPainted(false);
        calculatorOpenP.setBackground(calculatorGeneralUtil.calculatorOrange);
        calculatorOpenP.setFont(calculatorGeneralUtil.weatherFontBig);
        calculatorOpenP.addActionListener(e -> {
            calculatorExpression += "(";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorCloseP = new CyderButton(")");
        calculatorCloseP.setColors(calculatorGeneralUtil.calculatorOrange);
        calculatorCloseP.setBorder(new LineBorder(calculatorGeneralUtil.navy,5,false));
        calculatorCloseP.setBounds(400,500,80,80);
        calculatorFrame.getContentPane().add(calculatorCloseP);
        calculatorCloseP.setFocusPainted(false);
        calculatorCloseP.setBackground(calculatorGeneralUtil.calculatorOrange);
        calculatorCloseP.setFont(calculatorGeneralUtil.weatherFontBig);
        calculatorCloseP.addActionListener(e -> {
            calculatorExpression += ")";
            calculatorField.setText(calculatorExpression);
        });

        calculatorFrame.setLocationRelativeTo(null);
        calculatorFrame.setVisible(true);
    }
}
