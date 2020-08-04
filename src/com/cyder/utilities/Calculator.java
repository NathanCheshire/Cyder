package com.cyder.utilities;

import com.cyder.ui.CyderButton;
import com.fathzer.soft.javaluator.DoubleEvaluator;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class Calculator {
    private JFrame calculatorFrame;
    private JTextField calculatorField;
    private String calculatorExpression = "";
    
    private Util calculatorUtil = new Util();
    
    
    public Calculator() {
        calculatorExpression = "";

        if (calculatorFrame != null) {
            calculatorUtil.closeAnimation(calculatorFrame);
            calculatorFrame.dispose();
        }

        calculatorFrame = new JFrame();

        calculatorFrame.setResizable(false);

        calculatorFrame.setTitle("Calculator");

        calculatorFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        calculatorFrame.setResizable(false);

        calculatorFrame.setIconImage(calculatorUtil.getCyderIcon().getImage());

        JPanel ParentPanel = new JPanel();

        ParentPanel.setLayout(new BoxLayout(ParentPanel, BoxLayout.Y_AXIS));

        calculatorField = new JTextField(20);

        calculatorField.setSelectionColor(calculatorUtil.selectionColor);

        calculatorField.setToolTipText("(rad not deg)");

        calculatorField.setText("");

        calculatorField.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10),
                new LineBorder(calculatorUtil.navy,5,false)));

        calculatorField.setFont(calculatorUtil.weatherFontBig);

        ParentPanel.add(calculatorField);

        GridLayout ButtonLayout = new GridLayout(5, 4, 5, 5);

        JPanel CalcButtonPanel = new JPanel();

        CalcButtonPanel.setLayout(ButtonLayout);

        CyderButton calculatorAdd = new CyderButton("+");

        calculatorAdd.setColors(calculatorUtil.calculatorOrange);

        calculatorAdd.setBorder(new LineBorder(calculatorUtil.navy,5,false));

        CalcButtonPanel.add(calculatorAdd);

        calculatorAdd.setFocusPainted(false);

        calculatorAdd.setBackground(calculatorUtil.calculatorOrange);

        calculatorAdd.setFont(calculatorUtil.weatherFontSmall);

        calculatorAdd.addActionListener(e -> {
            calculatorExpression += "+";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorSubtract = new CyderButton("-");

        calculatorSubtract.setColors(calculatorUtil.calculatorOrange);

        calculatorSubtract.setBorder(new LineBorder(calculatorUtil.navy,5,false));

        CalcButtonPanel.add(calculatorSubtract);

        calculatorSubtract.setFocusPainted(false);

        calculatorSubtract.setBackground(calculatorUtil.calculatorOrange);

        calculatorSubtract.setFont(calculatorUtil.weatherFontSmall);

        calculatorSubtract.addActionListener(e -> {
            calculatorExpression += "-";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorMultiply = new CyderButton("*");

        calculatorMultiply.setColors(calculatorUtil.calculatorOrange);

        calculatorMultiply.setBorder(new LineBorder(calculatorUtil.navy,5,false));

        CalcButtonPanel.add(calculatorMultiply);

        calculatorMultiply.setFocusPainted(false);

        calculatorMultiply.setBackground(calculatorUtil.calculatorOrange);

        calculatorMultiply.setFont(calculatorUtil.weatherFontSmall);

        calculatorMultiply.addActionListener(e -> {
            calculatorExpression += "*";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorDivide = new CyderButton("/");

        calculatorDivide.setColors(calculatorUtil.calculatorOrange);

        calculatorDivide.setBorder(new LineBorder(calculatorUtil.navy,5,false));

        CalcButtonPanel.add(calculatorDivide);

        calculatorDivide.setFocusPainted(false);

        calculatorDivide.setBackground(calculatorUtil.calculatorOrange);

        calculatorDivide.setFont(calculatorUtil.weatherFontSmall);

        calculatorDivide.addActionListener(e -> {
            calculatorExpression += "/";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorSeven = new CyderButton("7");

        calculatorSeven.setColors(calculatorUtil.calculatorOrange);

        calculatorSeven.setBorder(new LineBorder(calculatorUtil.navy,5,false));

        CalcButtonPanel.add(calculatorSeven);

        calculatorSeven.setFocusPainted(false);

        calculatorSeven.setBackground(calculatorUtil.calculatorOrange);

        calculatorSeven.setFont(calculatorUtil.weatherFontSmall);

        calculatorSeven.addActionListener(e -> {
            calculatorExpression += "7";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorEight = new CyderButton("8");

        calculatorEight.setColors(calculatorUtil.calculatorOrange);

        calculatorEight.setBorder(new LineBorder(calculatorUtil.navy,5,false));

        CalcButtonPanel.add(calculatorEight);

        calculatorEight.setFocusPainted(false);

        calculatorEight.setBackground(calculatorUtil.calculatorOrange);

        calculatorEight.setFont(calculatorUtil.weatherFontSmall);

        calculatorEight.addActionListener(e -> {
            calculatorExpression += "8";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorNine = new CyderButton("9");

        calculatorNine.setColors(calculatorUtil.calculatorOrange);

        calculatorNine.setBorder(new LineBorder(calculatorUtil.navy,5,false));

        CalcButtonPanel.add(calculatorNine);

        calculatorNine.setFocusPainted(false);

        calculatorNine.setBackground(calculatorUtil.calculatorOrange);

        calculatorNine.setFont(calculatorUtil.weatherFontSmall);

        calculatorNine.addActionListener(e -> {
            calculatorExpression += "9";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorEquals = new CyderButton("=");

        calculatorEquals.setColors(calculatorUtil.calculatorOrange);

        calculatorEquals.setBorder(new LineBorder(calculatorUtil.navy,5,false));

        CalcButtonPanel.add(calculatorEquals);

        calculatorEquals.setFocusPainted(false);

        calculatorEquals.setBackground(calculatorUtil.calculatorOrange);

        calculatorEquals.setFont(calculatorUtil.weatherFontSmall);

        calculatorEquals.addActionListener(e -> {
            try {
                 calculatorUtil.inform("Answer:<br/>" + new DoubleEvaluator().evaluate(calculatorField.getText().trim()), "Result", calculatorFrame.getWidth(),calculatorFrame.getHeight());
            }

            catch (Exception exc) {
                calculatorUtil.inform("Unrecognized expression. Please use multiplication signs after parenthesis and check the exact syntax of your expression for common" +
                        " errors such as missing delimiters.<br/>Note that this calculator does support typing in the Text Field and can handle more complicated" +
                        "<br/>expressions such as sin, cos, tan, log, ln, floor, etc.","", calculatorFrame.getWidth(), calculatorFrame.getHeight());
                calculatorUtil.handle(exc);
            }
        });

        calculatorField.addActionListener(e -> calculatorEquals.doClick());

        CyderButton calculatorFour = new CyderButton("4");

        calculatorFour.setColors(calculatorUtil.calculatorOrange);

        calculatorFour.setBorder(new LineBorder(calculatorUtil.navy,5,false));

        CalcButtonPanel.add(calculatorFour);

        calculatorFour.setFocusPainted(false);

        calculatorFour.setBackground(calculatorUtil.calculatorOrange);

        calculatorFour.setFont(calculatorUtil.weatherFontSmall);

        calculatorFour.addActionListener(e -> {
            calculatorExpression += "4";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorFive = new CyderButton("5");

        calculatorFive.setColors(calculatorUtil.calculatorOrange);

        calculatorFive.setBorder(new LineBorder(calculatorUtil.navy,5,false));

        CalcButtonPanel.add(calculatorFive);

        calculatorFive.setFocusPainted(false);

        calculatorFive.setBackground(calculatorUtil.calculatorOrange);

        calculatorFive.setFont(calculatorUtil.weatherFontSmall);

        calculatorFive.addActionListener(e -> {
            calculatorExpression += "5";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorSix = new CyderButton("6");

        calculatorSix.setColors(calculatorUtil.calculatorOrange);

        calculatorSix.setBorder(new LineBorder(calculatorUtil.navy,5,false));

        CalcButtonPanel.add(calculatorSix);

        calculatorSix.setFocusPainted(false);

        calculatorSix.setBackground(calculatorUtil.calculatorOrange);

        calculatorSix.setFont(calculatorUtil.weatherFontSmall);

        calculatorSix.addActionListener(e -> {
            calculatorExpression += "6";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorClear = new CyderButton("CE");

        calculatorClear.setColors(calculatorUtil.calculatorOrange);

        calculatorClear.setBorder(new LineBorder(calculatorUtil.navy,5,false));

        CalcButtonPanel.add(calculatorClear);

        calculatorClear.setFocusPainted(false);

        calculatorClear.setBackground(calculatorUtil.calculatorOrange);

        calculatorClear.setFont(calculatorUtil.weatherFontSmall);

        calculatorClear.addActionListener(e -> {
            calculatorExpression = "";
            calculatorField.setText("");
        });

        CyderButton calculatorOne = new CyderButton("1");

        calculatorOne.setColors(calculatorUtil.calculatorOrange);

        calculatorOne.setBorder(new LineBorder(calculatorUtil.navy,5,false));

        CalcButtonPanel.add(calculatorOne);

        calculatorOne.setFocusPainted(false);

        calculatorOne.setBackground(calculatorUtil.calculatorOrange);

        calculatorOne.setFont(calculatorUtil.weatherFontSmall);

        calculatorOne.addActionListener(e -> {
            calculatorExpression += "1";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorTwo = new CyderButton("2");

        calculatorTwo.setColors(calculatorUtil.calculatorOrange);

        calculatorTwo.setBorder(new LineBorder(calculatorUtil.navy,5,false));

        CalcButtonPanel.add(calculatorTwo);

        calculatorTwo.setFocusPainted(false);

        calculatorTwo.setBackground(calculatorUtil.calculatorOrange);

        calculatorTwo.setFont(calculatorUtil.weatherFontSmall);

        calculatorTwo.addActionListener(e -> {
            calculatorExpression += "2";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorThree = new CyderButton("3");

        calculatorThree.setColors(calculatorUtil.calculatorOrange);

        calculatorThree.setBorder(new LineBorder(calculatorUtil.navy,5,false));

        CalcButtonPanel.add(calculatorThree);

        calculatorThree.setFocusPainted(false);

        calculatorThree.setBackground(calculatorUtil.calculatorOrange);

        calculatorThree.setFont(calculatorUtil.weatherFontSmall);

        calculatorThree.addActionListener(e -> {
            calculatorExpression += "3";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorUndo = new CyderButton("<--");

        calculatorUndo.setColors(calculatorUtil.calculatorOrange);

        calculatorUndo.setBorder(new LineBorder(calculatorUtil.navy,5,false));

        CalcButtonPanel.add(calculatorUndo);

        calculatorUndo.setFocusPainted(false);

        calculatorUndo.setBackground(calculatorUtil.calculatorOrange);

        calculatorUndo.setFont(calculatorUtil.weatherFontSmall);

        calculatorUndo.addActionListener(e -> {
            calculatorExpression = (calculatorExpression == null || calculatorExpression.length() == 0)
                    ? "" : (calculatorExpression.substring(0, calculatorExpression.length() - 1));
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorZero = new CyderButton("0");

        calculatorZero.setColors(calculatorUtil.calculatorOrange);

        calculatorZero.setBorder(new LineBorder(calculatorUtil.navy,5,false));

        CalcButtonPanel.add(calculatorZero);

        calculatorZero.setFocusPainted(false);

        calculatorZero.setBackground(calculatorUtil.calculatorOrange);

        calculatorZero.setFont(calculatorUtil.weatherFontSmall);

        calculatorZero.addActionListener(e -> {
            calculatorExpression += "0";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorDecimal = new CyderButton(".");

        calculatorDecimal.setColors(calculatorUtil.calculatorOrange);

        calculatorDecimal.setBorder(new LineBorder(calculatorUtil.navy,5,false));

        CalcButtonPanel.add(calculatorDecimal);

        calculatorDecimal.setFocusPainted(false);

        calculatorDecimal.setBackground(calculatorUtil.calculatorOrange);

        calculatorDecimal.setFont(calculatorUtil.weatherFontSmall);

        calculatorDecimal.addActionListener(e -> {
            calculatorExpression += ".";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorOpenP = new CyderButton("(");

        calculatorOpenP.setColors(calculatorUtil.calculatorOrange);

        calculatorOpenP.setBorder(new LineBorder(calculatorUtil.navy,5,false));

        CalcButtonPanel.add(calculatorOpenP);

        calculatorOpenP.setFocusPainted(false);

        calculatorOpenP.setBackground(calculatorUtil.calculatorOrange);

        calculatorOpenP.setFont(calculatorUtil.weatherFontSmall);

        calculatorOpenP.addActionListener(e -> {
            calculatorExpression += "(";
            calculatorField.setText(calculatorExpression);
        });

        CyderButton calculatorCloseP = new CyderButton(")");

        calculatorCloseP.setColors(calculatorUtil.calculatorOrange);

        calculatorCloseP.setBorder(new LineBorder(calculatorUtil.navy,5,false));

        CalcButtonPanel.add(calculatorCloseP);

        calculatorCloseP.setFocusPainted(false);

        calculatorCloseP.setBackground(calculatorUtil.calculatorOrange);

        calculatorCloseP.setFont(calculatorUtil.weatherFontSmall);

        calculatorCloseP.addActionListener(e -> {
            calculatorExpression += ")";
            calculatorField.setText(calculatorExpression);
        });

        CalcButtonPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        ParentPanel.add(CalcButtonPanel);

        calculatorFrame.add(ParentPanel);

        calculatorFrame.pack();

        calculatorFrame.setLocationRelativeTo(null);

        calculatorFrame.setVisible(true);

        calculatorFrame.setAlwaysOnTop(true);

        calculatorFrame.setAlwaysOnTop(false);

        calculatorFrame.requestFocus();
    }
}
