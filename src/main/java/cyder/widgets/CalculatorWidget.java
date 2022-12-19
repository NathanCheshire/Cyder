package main.java.cyder.widgets;

import com.fathzer.soft.javaluator.DoubleEvaluator;
import main.java.cyder.annotations.CyderAuthor;
import main.java.cyder.annotations.Vanilla;
import main.java.cyder.annotations.Widget;
import main.java.cyder.constants.CyderColors;
import main.java.cyder.constants.CyderFonts;
import main.java.cyder.constants.CyderIcons;
import main.java.cyder.exceptions.IllegalMethodException;
import main.java.cyder.strings.CyderStrings;
import main.java.cyder.ui.button.CyderModernButton;
import main.java.cyder.ui.button.ThemeBuilder;
import main.java.cyder.ui.drag.CyderDragLabel;
import main.java.cyder.ui.field.CyderTextField;
import main.java.cyder.ui.frame.CyderFrame;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * A calculator widget for parsing mathematical expressions.
 */
@Vanilla
@CyderAuthor
public final class CalculatorWidget {
    /**
     * The field to display the most recent results in.
     */
    private static CyderTextField resultField;

    /**
     * The field in which the user may enter an expression
     */
    private static CyderTextField calculatorField;

    /**
     * The text to display to user if an expression could not be parsed.
     */
    private static final String ERROR_TEXT = "Could not parse expression";

    /**
     * The font to use for the results field.
     */
    private static final Font fieldFont = new Font("Agency FB", Font.BOLD, 25);

    /**
     * The theme for each calculator button.
     */
    private static final ThemeBuilder theme = new ThemeBuilder();

    static {
        theme.setFont(CyderFonts.SEGOE_30);
        theme.setBorderLength(5);
        theme.setBackgroundColor(CyderColors.regularOrange);
        theme.setHoverColor(CyderColors.regularOrange.darker());
        theme.setPressedColor(CyderColors.regularOrange.darker().darker());
    }

    /**
     * Suppress default constructor.
     */
    private CalculatorWidget() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * The width of the calculator frame.
     */
    private static final int FRAME_WIDTH = 400;

    /**
     * The height of the calculator frame.
     */
    private static final int FRAME_HEIGHT = 600;

    /**
     * The width and height of each calculator button.
     */
    private static final int buttonLength = 75;

    /**
     * The description for the widget annotation.
     */
    private static final String description = "A calculator widget capable of "
            + "performing complex expressions such as e^x, sin(x), cos(x), and so forth.";

    @Widget(triggers = {"calculator", "calc", "math"}, description = description)
    public static void showGui() {
        CyderFrame calculatorFrame = new CyderFrame(FRAME_WIDTH, FRAME_HEIGHT, CyderIcons.defaultBackground);
        calculatorFrame.setTitle("Calculator");

        resultField = new CyderTextField();
        resultField.setBorder(null);
        resultField.setEditable(false);
        resultField.setFocusable(true);
        resultField.setSelectionColor(CyderColors.selectionColor);
        resultField.setHorizontalAlignment(JTextField.RIGHT);
        resultField.setFont(fieldFont);
        resultField.setBounds(25, CyderDragLabel.DEFAULT_HEIGHT + 10, 350, 30);
        calculatorFrame.getContentPane().add(resultField);

        calculatorField = new CyderTextField();
        calculatorField.setBorder(null);
        calculatorField.setHorizontalAlignment(JTextField.LEFT);
        calculatorField.setSelectionColor(CyderColors.selectionColor);
        calculatorField.setToolTipText("Use radians and not degrees for any trig functions");
        calculatorField.setFont(fieldFont);
        calculatorField.setBounds(25,
                CyderDragLabel.DEFAULT_HEIGHT + 5 + 30 + 5, 350, 25);
        calculatorFrame.getContentPane().add(calculatorField);

        JLabel borderLabel = new JLabel();
        borderLabel.setBounds(20, CyderDragLabel.DEFAULT_HEIGHT + 5, 360, 65);
        borderLabel.setBorder(new LineBorder(CyderColors.navy, 5));
        borderLabel.setOpaque(false);
        calculatorFrame.getContentPane().add(borderLabel);

        CyderModernButton calculatorAdd = new CyderModernButton("+");
        calculatorAdd.addClickRunnable(() -> calculatorField.setText(calculatorField.getText() + "+"));
        calculatorAdd.setTheme(theme);
        calculatorAdd.setBounds(20, 120, buttonLength, buttonLength);
        calculatorFrame.getContentPane().add(calculatorAdd);

        CyderModernButton calculatorSubtract = new CyderModernButton(CyderStrings.dash);
        calculatorSubtract.addClickRunnable(() -> calculatorField.setText(calculatorField.getText()
                + CyderStrings.dash));
        calculatorSubtract.setTheme(theme);
        calculatorSubtract.setBounds(115, 120, buttonLength, buttonLength);
        calculatorFrame.getContentPane().add(calculatorSubtract);

        CyderModernButton calculatorMultiply = new CyderModernButton("*");
        calculatorMultiply.addClickRunnable(() -> calculatorField.setText(calculatorField.getText() + "*"));
        calculatorMultiply.setTheme(theme);
        calculatorMultiply.setBounds(210, 120, buttonLength, buttonLength);
        calculatorFrame.getContentPane().add(calculatorMultiply);

        CyderModernButton calculatorDivide = new CyderModernButton(CyderStrings.forwardSlash);
        calculatorDivide.addClickRunnable(() -> calculatorField.setText(calculatorField.getText()
                + CyderStrings.forwardSlash));
        calculatorDivide.setTheme(theme);
        calculatorDivide.setBounds(305, 120, buttonLength, buttonLength);
        calculatorFrame.getContentPane().add(calculatorDivide);

        CyderModernButton calculatorSeven = new CyderModernButton("7");
        calculatorSeven.addClickRunnable(() -> calculatorField.setText(calculatorField.getText() + "7"));
        calculatorSeven.setTheme(theme);
        calculatorSeven.setBounds(20, 215, buttonLength, buttonLength);
        calculatorFrame.getContentPane().add(calculatorSeven);

        CyderModernButton calculatorEight = new CyderModernButton("8");
        calculatorEight.addClickRunnable(() -> calculatorField.setText(calculatorField.getText() + "8"));
        calculatorEight.setBounds(115, 215, buttonLength, buttonLength);
        calculatorEight.setTheme(theme);
        calculatorFrame.getContentPane().add(calculatorEight);

        CyderModernButton calculatorNine = new CyderModernButton("9");
        calculatorNine.addClickRunnable(() -> calculatorField.setText(calculatorField.getText() + "9"));
        calculatorNine.setTheme(theme);
        calculatorNine.setBounds(210, 215, buttonLength, buttonLength);
        calculatorFrame.getContentPane().add(calculatorNine);

        CyderModernButton calculatorEquals = new CyderModernButton("=");
        calculatorEquals.addClickRunnable(CalculatorWidget::computeExpression);
        calculatorEquals.setTheme(theme);
        calculatorEquals.setBounds(305, 215, buttonLength, buttonLength);
        calculatorFrame.getContentPane().add(calculatorEquals);

        CyderModernButton calculatorFour = new CyderModernButton("4");
        calculatorFour.addClickRunnable(() -> calculatorField.setText(calculatorField.getText() + "4"));
        calculatorFour.setTheme(theme);
        calculatorFour.setBounds(20, 310, buttonLength, buttonLength);
        calculatorFrame.getContentPane().add(calculatorFour);

        CyderModernButton calculatorFive = new CyderModernButton("5");
        calculatorFive.addClickRunnable(() -> calculatorField.setText(calculatorField.getText() + "5"));
        calculatorFive.setTheme(theme);
        calculatorFive.setBounds(115, 310, buttonLength, buttonLength);
        calculatorFrame.getContentPane().add(calculatorFive);

        CyderModernButton calculatorSix = new CyderModernButton("6");
        calculatorSix.addClickRunnable(() -> calculatorField.setText(calculatorField.getText() + "6"));
        calculatorSix.setTheme(theme);
        calculatorSix.setBounds(210, 310, buttonLength, buttonLength);
        calculatorFrame.getContentPane().add(calculatorSix);

        CyderModernButton calculatorClear = new CyderModernButton("CE");
        calculatorClear.addClickRunnable(CalculatorWidget::clearFields);
        calculatorClear.setTheme(theme);
        calculatorClear.setBounds(305, 310, buttonLength, buttonLength);
        calculatorFrame.getContentPane().add(calculatorClear);

        CyderModernButton calculatorOne = new CyderModernButton("1");
        calculatorOne.addClickRunnable(() -> calculatorField.setText(calculatorField.getText() + "1"));
        calculatorOne.setTheme(theme);
        calculatorOne.setBounds(20, 405, buttonLength, buttonLength);
        calculatorFrame.getContentPane().add(calculatorOne);

        CyderModernButton calculatorTwo = new CyderModernButton("2");
        calculatorTwo.addClickRunnable(() -> calculatorField.setText(calculatorField.getText() + "2"));
        calculatorTwo.setTheme(theme);
        calculatorTwo.setBounds(115, 405, buttonLength, buttonLength);
        calculatorFrame.getContentPane().add(calculatorTwo);

        CyderModernButton calculatorThree = new CyderModernButton("3");
        calculatorThree.addClickRunnable(() -> calculatorField.setText(calculatorField.getText() + "3"));
        calculatorThree.setTheme(theme);
        calculatorThree.setBounds(210, 405, buttonLength, buttonLength);
        calculatorFrame.getContentPane().add(calculatorThree);

        CyderModernButton undo = new CyderModernButton("<<");
        undo.addClickRunnable(CalculatorWidget::undoAction);
        undo.setTheme(theme);
        undo.setBounds(305, 405, buttonLength, buttonLength);
        calculatorFrame.getContentPane().add(undo);

        CyderModernButton calculatorZero = new CyderModernButton("0");
        calculatorZero.addClickRunnable(() -> calculatorField.setText(calculatorField.getText() + "0"));
        calculatorZero.setTheme(theme);
        calculatorZero.setBounds(20, 500, buttonLength, buttonLength);
        calculatorFrame.getContentPane().add(calculatorZero);

        CyderModernButton calculatorDecimal = new CyderModernButton(".");
        calculatorDecimal.addClickRunnable(() -> calculatorField.setText(calculatorField.getText() + "."));
        calculatorDecimal.setTheme(theme);
        calculatorDecimal.setBounds(115, 500, buttonLength, buttonLength);
        calculatorFrame.getContentPane().add(calculatorDecimal);

        CyderModernButton calculatorOpenP = new CyderModernButton(CyderStrings.openingParenthesis);
        calculatorOpenP.addClickRunnable(() -> calculatorField.setText(calculatorField.getText()
                + CyderStrings.openingParenthesis));
        calculatorOpenP.setTheme(theme);
        calculatorOpenP.setBounds(210, 500, buttonLength, buttonLength);
        calculatorFrame.getContentPane().add(calculatorOpenP);

        CyderModernButton calculatorCloseP = new CyderModernButton(CyderStrings.closingParenthesis);
        calculatorCloseP.addClickRunnable(() -> calculatorField.setText(calculatorField.getText()
                + CyderStrings.closingParenthesis));
        calculatorCloseP.setTheme(theme);
        calculatorCloseP.setBounds(305, 500, buttonLength, buttonLength);
        calculatorFrame.getContentPane().add(calculatorCloseP);

        calculatorFrame.finalizeAndShow();
    }

    /**
     * Clears the calculator fields.
     */
    private static void clearFields() {
        calculatorField.setText("");
        resultField.setText("");
    }

    /**
     * Removes the last character from the calculator field.
     */
    private static void undoAction() {
        String text = calculatorField.getText();

        if (text.length() > 0) {
            calculatorField.setText(text.substring(0, text.length() - 1));
        }
    }

    /**
     * The evaluator for evaluating expressions.
     */
    private static final DoubleEvaluator evaluator = new DoubleEvaluator();

    /**
     * The positive infinity string.
     */
    private static final String POSITIVE_INFINITY = "+∞";

    /**
     * The negative infinity string.
     */
    private static final String NEGATIVE_INFINITY = "-∞";

    /**
     * Attempts to compute the expression from the calculator field.
     */
    private static void computeExpression() {
        try {
            double result = evaluator.evaluate(calculatorField.getText().trim());
            String resultString = String.valueOf(result);

            if (result == Double.POSITIVE_INFINITY) {
                resultString = POSITIVE_INFINITY;
            } else if (result == Double.NEGATIVE_INFINITY) {
                resultString = NEGATIVE_INFINITY;
            }

            setResultText(resultString);
        } catch (IllegalArgumentException e) {
            setResultText(ERROR_TEXT);
        }
    }

    /**
     * Animates in the results text to the results field by fading it from
     * {@link CyderColors#regularRed} to {@link CyderColors#navy} in 500ms.
     *
     * @param resultText the text to show in the results field
     */
    private synchronized static void setResultText(String resultText) {
        resultField.setText(resultText);
        resultField.flashField();
    }
}
