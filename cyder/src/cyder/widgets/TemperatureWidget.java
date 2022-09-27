package cyder.widgets;

import cyder.annotations.CyderAuthor;
import cyder.annotations.ForReadability;
import cyder.annotations.Vanilla;
import cyder.annotations.Widget;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderIcons;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.ui.button.CyderButton;
import cyder.ui.field.CyderTextField;
import cyder.ui.frame.CyderFrame;
import cyder.ui.selection.CyderCheckbox;
import cyder.ui.selection.CyderCheckboxGroup;
import cyder.utils.UiUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.text.DecimalFormat;

@Vanilla
@CyderAuthor
public class TemperatureWidget {
    private CyderFrame temperatureFrame;
    private CyderTextField startingValueField;

    private CyderCheckbox oldFahrenheit;
    private CyderCheckbox newFahrenheit;
    private CyderCheckbox oldCelsius;
    private CyderCheckbox newCelsius;
    private CyderCheckbox oldKelvin;
    private CyderCheckbox newKelvin;

    /**
     * Returns a new instance of the temperature converter widget.
     *
     * @return a new instance of the temperature converter widget
     */
    public static TemperatureWidget getInstance() {
        return new TemperatureWidget();
    }

    /**
     * Temperature converter widget to convert between kelvin, fahrenheit, and celsius
     */
    private TemperatureWidget() {
        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    private static final String description
            = "A temperature conversion widget for the three standard temperature units";

    private static final DecimalFormat resultFormatter = new DecimalFormat("#.####");

    /**
     * The temperature units supported by Cyder for conversion.
     */
    private enum Unit {
        /**
         * The imperial temperature unit.
         */
        FAHRENHEIT("Fahrenheit"),

        /**
         * The SI temperature unit.
         */
        CELSIUS("Celsius"),

        /**
         * The primary temperature uit
         */
        KELVIN("Kelvin");

        private final String name;

        Unit(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    private static final String CALCULATE = "Calculate";

    @Widget(triggers = {"temperature", "temp", "fahrenheit", "celsius", "kelvin"}, description = description)
    public static void showGui() {
        getInstance().innerShowGUI();
    }

    public void innerShowGUI() {
        UiUtil.closeIfOpen(temperatureFrame);

        temperatureFrame = new CyderFrame(600, 340, CyderIcons.defaultBackground);
        temperatureFrame.setTitle("Temperature Converter");

        JLabel valueLabel = new JLabel("Measurement: ");
        valueLabel.setFont(CyderFonts.SEGOE_20);

        startingValueField = new CyderTextField(0);
        startingValueField.setHorizontalAlignment(JTextField.CENTER);
        // todo regex

        valueLabel.setBounds(60, 40, 200, 30);

        temperatureFrame.getContentPane().add(valueLabel);
        startingValueField.setBounds(240, 40, 300, 35);
        temperatureFrame.getContentPane().add(startingValueField);

        oldFahrenheit = new CyderCheckbox();
        oldCelsius = new CyderCheckbox();
        oldKelvin = new CyderCheckbox();

        JLabel oldFahrenheitLabel = new JLabel(Unit.FAHRENHEIT.getName());
        oldFahrenheitLabel.setFont(CyderFonts.SEGOE_30.deriveFont(22f));
        oldFahrenheitLabel.setForeground(CyderColors.navy);
        oldFahrenheitLabel.setBounds(140, 110, 250, 30);
        temperatureFrame.getContentPane().add(oldFahrenheitLabel);

        JLabel oldCelsiusLabel = new JLabel(Unit.CELSIUS.getName());
        oldCelsiusLabel.setFont(CyderFonts.SEGOE_30.deriveFont(22f));
        oldCelsiusLabel.setForeground(CyderColors.navy);
        oldCelsiusLabel.setBounds(140, 170, 250, 30);
        temperatureFrame.getContentPane().add(oldCelsiusLabel);

        JLabel oldKelvinLabel = new JLabel(Unit.KELVIN.getName());
        oldKelvinLabel.setFont(CyderFonts.SEGOE_30.deriveFont(22f));
        oldKelvinLabel.setForeground(CyderColors.navy);
        oldKelvinLabel.setBounds(140, 230, 250, 30);
        temperatureFrame.getContentPane().add(oldKelvinLabel);

        oldFahrenheit.setBounds(80, 100, 50, 50);
        oldCelsius.setBounds(80, 160, 50, 50);
        oldKelvin.setBounds(80, 220, 50, 50);

        CyderCheckboxGroup oldGroup = new CyderCheckboxGroup();
        oldGroup.addCheckbox(oldFahrenheit);
        oldGroup.addCheckbox(oldCelsius);
        oldGroup.addCheckbox(oldKelvin);

        temperatureFrame.getContentPane().add(oldFahrenheit);
        temperatureFrame.getContentPane().add(oldCelsius);
        temperatureFrame.getContentPane().add(oldKelvin);

        JLabel conversionToLabel = new JLabel("-2-");
        conversionToLabel.setFont(CyderFonts.SEGOE_30.deriveFont(45f));
        conversionToLabel.setForeground(CyderColors.navy);
        conversionToLabel.setBounds(260, 150, 150, 60);
        temperatureFrame.getContentPane().add(conversionToLabel);

        newFahrenheit = new CyderCheckbox();
        newCelsius = new CyderCheckbox();
        newKelvin = new CyderCheckbox();

        JLabel newFahrenheitLabel = new JLabel(Unit.FAHRENHEIT.getName());
        newFahrenheitLabel.setFont(CyderFonts.SEGOE_30.deriveFont(22f));
        newFahrenheitLabel.setForeground(CyderColors.navy);
        newFahrenheitLabel.setBounds(430, 110, 250, 30);
        temperatureFrame.getContentPane().add(newFahrenheitLabel);

        JLabel newCelsiusLabel = new JLabel(Unit.CELSIUS.getName());
        newCelsiusLabel.setFont(CyderFonts.SEGOE_30.deriveFont(22f));
        newCelsiusLabel.setForeground(CyderColors.navy);
        newCelsiusLabel.setBounds(430, 170, 250, 30);
        temperatureFrame.getContentPane().add(newCelsiusLabel);

        JLabel newKelvinLabel = new JLabel(Unit.KELVIN.getName());
        newKelvinLabel.setFont(CyderFonts.SEGOE_30.deriveFont(22f));
        newKelvinLabel.setForeground(CyderColors.navy);
        newKelvinLabel.setBounds(430, 230, 250, 30);
        temperatureFrame.getContentPane().add(newKelvinLabel);

        newFahrenheit.setBounds(370, 100, 50, 50);
        newCelsius.setBounds(370, 160, 50, 50);
        newKelvin.setBounds(370, 220, 50, 50);

        CyderCheckboxGroup newGroup = new CyderCheckboxGroup();
        newGroup.addCheckbox(newFahrenheit);
        newGroup.addCheckbox(newCelsius);
        newGroup.addCheckbox(newKelvin);

        temperatureFrame.getContentPane().add(newFahrenheit);
        temperatureFrame.getContentPane().add(newCelsius);
        temperatureFrame.getContentPane().add(newKelvin);

        CyderButton calculate = new CyderButton(CALCULATE);
        calculate.setBorder(new LineBorder(CyderColors.navy, 5, false));
        calculate.addActionListener(e -> {
            String startingValueText = startingValueField.getTrimmedText();
            if (startingValueText.isEmpty()) return;

            double value;
            try {
                value = Double.parseDouble(startingValueText);
            } catch (NumberFormatException ex) {
                temperatureFrame.notify("Could not parse input");
                return;
            }

            Unit oldUnit;
            if (oldFahrenheit.isChecked()) {
                oldUnit = Unit.FAHRENHEIT;
            } else if (oldCelsius.isChecked()) {
                oldUnit = Unit.CELSIUS;
            } else if (oldKelvin.isChecked()) {
                oldUnit = Unit.KELVIN;
            } else {
                return;
            }

            Unit newUnit;
            if (newFahrenheit.isChecked()) {
                newUnit = Unit.FAHRENHEIT;
            } else if (newCelsius.isChecked()) {
                newUnit = Unit.CELSIUS;
            } else if (newKelvin.isChecked()) {
                newUnit = Unit.KELVIN;
            } else {
                return;
            }

            if (newUnit == oldUnit) {
                temperatureFrame.notify("Get out of here with that, Your value is already in "
                        + oldUnit.getName());
                return;
            }

            double oldValueInKelvin = switch (oldUnit) {
                case FAHRENHEIT -> (value - 32) * (5.0 / 9.0) + 273.15;
                case CELSIUS -> value + 273.15;
                case KELVIN -> value;
            };

            switch (newUnit) {
                case FAHRENHEIT -> {
                    double fahrenheitFromKelvin = 1.8 * (oldValueInKelvin - 273.15) + 32;
                    startingValueField.setText(resultFormatter.format(fahrenheitFromKelvin));
                    oldFahrenheit.setChecked();
                }
                case CELSIUS -> {
                    double celsiusFromKelvin = oldValueInKelvin - 273.15;
                    startingValueField.setText(resultFormatter.format(celsiusFromKelvin));
                    oldCelsius.setChecked();
                }
                case KELVIN -> {
                    startingValueField.setText(resultFormatter.format(oldValueInKelvin));
                    oldKelvin.setChecked();
                }
            }

            startingValueField.flashField();
        });
        calculate.setBackground(CyderColors.regularRed);
        calculate.setFont(CyderFonts.SEGOE_20);
        calculate.setBounds(140, 280, 150, 40);
        temperatureFrame.getContentPane().add(calculate);

        CyderButton resetValues = new CyderButton("Reset Values");
        resetValues.setBorder(new LineBorder(CyderColors.navy, 5, false));
        resetValues.addActionListener(e -> reset());
        resetValues.setFocusPainted(false);
        resetValues.setBackground(CyderColors.regularRed);
        resetValues.setFont(CyderFonts.SEGOE_20);
        resetValues.setBounds(300, 280, 150, 40);
        temperatureFrame.getContentPane().add(resetValues);

        reset();
        temperatureFrame.finalizeAndShow();
    }

    @ForReadability
    private void clearFieldInput() {
        startingValueField.setText("");
    }

    @ForReadability
    private void reset() {
        clearFieldInput();

        oldFahrenheit.setChecked();
        oldCelsius.setNotChecked();
        oldKelvin.setNotChecked();

        newFahrenheit.setChecked();
        newCelsius.setNotChecked();
        newKelvin.setNotChecked();
    }
}
