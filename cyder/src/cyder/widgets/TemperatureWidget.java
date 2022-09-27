package cyder.widgets;

import com.google.common.base.Preconditions;
import cyder.annotations.CyderAuthor;
import cyder.annotations.ForReadability;
import cyder.annotations.Vanilla;
import cyder.annotations.Widget;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderIcons;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.ui.button.CyderModernButton;
import cyder.ui.field.CyderTextField;
import cyder.ui.frame.CyderFrame;
import cyder.ui.selection.CyderCheckbox;
import cyder.ui.selection.CyderCheckboxGroup;
import cyder.utils.UiUtil;

import javax.swing.*;
import java.text.DecimalFormat;
import java.util.Optional;

import static cyder.utils.TemperatureUtil.*;

@Vanilla
@CyderAuthor
public class TemperatureWidget {
    /**
     * The frame for this widget.
     */
    private CyderFrame temperatureFrame;

    /**
     * The field to receive the temperature input.
     */
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

    private static final String RESET_VALUES = "Reset Values";

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

    /**
     * The text for the calculate button.
     */
    private static final String CALCULATE = "Calculate";

    /**
     * The theme for the buttons.
     */
    private static final CyderModernButton.ThemeBuilder buttonTheme = new CyderModernButton.ThemeBuilder()
            .setBackgroundColor(CyderColors.regularRed)
            .setBorderLength(5)
            .setBorderColor(CyderColors.navy)
            .setForegroundColor(CyderColors.navy)
            .setFont(CyderFonts.SEGOE_20);

    /**
     * The regex for the value field.
     */
    private static final String valueFieldRegex = "-?(([0-9]*)\\.?[0-9]*)";

    @Widget(triggers = {"temperature", "temp", "fahrenheit", "celsius", "kelvin"}, description = description)
    public static void showGui() {
        getInstance().innerShowGUI();
    }

    public void innerShowGUI() {
        UiUtil.closeIfOpen(temperatureFrame);

        temperatureFrame = new CyderFrame(600, 340, CyderIcons.defaultBackground);
        temperatureFrame.setTitle("Temperature Converter");

        JLabel valueLabel = new JLabel("Measurement:");
        valueLabel.setFont(CyderFonts.SEGOE_20);
        valueLabel.setBounds(60, 40, 200, 30);
        temperatureFrame.getContentPane().add(valueLabel);

        startingValueField = new CyderTextField(0);
        startingValueField.setHorizontalAlignment(JTextField.CENTER);
        startingValueField.setKeyEventRegexMatcher(valueFieldRegex);
        startingValueField.setBounds(240, 40, 300, 35);
        temperatureFrame.getContentPane().add(startingValueField);

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

        CyderCheckboxGroup oldGroup = new CyderCheckboxGroup();

        oldFahrenheit = new CyderCheckbox();
        oldFahrenheit.setBounds(80, 100, 50, 50);
        oldGroup.addCheckbox(oldFahrenheit);
        temperatureFrame.getContentPane().add(oldFahrenheit);

        oldCelsius = new CyderCheckbox();
        oldCelsius.setBounds(80, 160, 50, 50);
        oldGroup.addCheckbox(oldCelsius);
        temperatureFrame.getContentPane().add(oldCelsius);

        oldKelvin = new CyderCheckbox();
        oldKelvin.setBounds(80, 220, 50, 50);
        oldGroup.addCheckbox(oldKelvin);
        temperatureFrame.getContentPane().add(oldKelvin);

        JLabel conversionToLabel = new JLabel("-2-");
        conversionToLabel.setFont(CyderFonts.SEGOE_30.deriveFont(45f));
        conversionToLabel.setForeground(CyderColors.navy);
        conversionToLabel.setBounds(260, 150, 150, 60);
        temperatureFrame.getContentPane().add(conversionToLabel);

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

        CyderCheckboxGroup newGroup = new CyderCheckboxGroup();

        newFahrenheit = new CyderCheckbox();
        newFahrenheit.setBounds(370, 100, 50, 50);
        newGroup.addCheckbox(newFahrenheit);
        temperatureFrame.getContentPane().add(newFahrenheit);

        newCelsius = new CyderCheckbox();
        newCelsius.setBounds(370, 160, 50, 50);
        newGroup.addCheckbox(newCelsius);
        temperatureFrame.getContentPane().add(newCelsius);

        newKelvin = new CyderCheckbox();
        newKelvin.setBounds(370, 220, 50, 50);
        newGroup.addCheckbox(newKelvin);
        temperatureFrame.getContentPane().add(newKelvin);

        CyderModernButton calculate = new CyderModernButton(CALCULATE);
        calculate.addClickRunnable(this::calculateButtonAction);
        calculate.setTheme(buttonTheme);
        calculate.setBounds(140, 280, 150, 40);
        temperatureFrame.getContentPane().add(calculate);

        CyderModernButton resetValues = new CyderModernButton(RESET_VALUES);
        resetValues.addClickRunnable(this::reset);
        resetValues.setTheme(buttonTheme);
        resetValues.setBounds(300, 280, 150, 40);
        temperatureFrame.getContentPane().add(resetValues);

        reset();
        temperatureFrame.finalizeAndShow();
    }

    /**
     * Performs the logic for when the calculate button is pressed.
     */
    @ForReadability
    private void calculateButtonAction() {
        String startingValueText = startingValueField.getTrimmedText();
        if (startingValueText.isEmpty()) return;

        double value;
        try {
            value = Double.parseDouble(startingValueText);
        } catch (NumberFormatException ex) {
            temperatureFrame.notify("Could not parse input");
            return;
        }

        Optional<Unit> oldUnitOptional = getOldUnit();
        Optional<Unit> newUnitOptional = getNewUnit();

        if (oldUnitOptional.isEmpty() || newUnitOptional.isEmpty()) {
            return;
        }

        Unit oldUnit = oldUnitOptional.get();
        Unit newUnit = newUnitOptional.get();

        if (newUnit == oldUnit) {
            temperatureFrame.notify("Get out of here with that, your value is already in "
                    + oldUnit.getName());
            return;
        }

        double oldValueInKelvin = toKelvin(value, oldUnit);

        switch (newUnit) {
            case FAHRENHEIT -> {
                double fahrenheitFromKelvin = kelvinToFahrenheit(oldValueInKelvin);
                startingValueField.setText(resultFormatter.format(fahrenheitFromKelvin));
                oldFahrenheit.setChecked();
            }
            case CELSIUS -> {
                double celsiusFromKelvin = kelvinToCelsius(oldValueInKelvin);
                startingValueField.setText(resultFormatter.format(celsiusFromKelvin));
                oldCelsius.setChecked();
            }
            case KELVIN -> {
                startingValueField.setText(resultFormatter.format(oldValueInKelvin));
                oldKelvin.setChecked();
            }
        }

        startingValueField.flashField();
    }

    @ForReadability
    private double toKelvin(double value, Unit temperatureUnit) {
        Preconditions.checkNotNull(temperatureUnit);

        return switch (temperatureUnit) {
            case FAHRENHEIT -> fahrenheitToKelvin(value);
            case CELSIUS -> celsiusToKelvin(value);
            case KELVIN -> value;
        };
    }

    @ForReadability
    private double toFahrenheit(double value, Unit temperatureUnit) {
        Preconditions.checkNotNull(temperatureUnit);

        return switch (temperatureUnit) {
            case FAHRENHEIT -> value;
            case CELSIUS -> celsiusToFahrenheit(value);
            case KELVIN -> kelvinToFahrenheit(value);
        };
    }

    @ForReadability
    private double toCelsius(double value, Unit temperatureUnit) {
        Preconditions.checkNotNull(temperatureUnit);

        return switch (temperatureUnit) {
            case FAHRENHEIT -> fahrenheitToCelsius(value);
            case CELSIUS -> value;
            case KELVIN -> kelvinToCelsius(value);
        };
    }


    @ForReadability
    private Optional<Unit> getOldUnit() {
        if (oldFahrenheit.isChecked()) {
            return Optional.of(Unit.FAHRENHEIT);
        } else if (oldCelsius.isChecked()) {
            return Optional.of(Unit.CELSIUS);
        } else if (oldKelvin.isChecked()) {
            return Optional.of(Unit.KELVIN);
        } else {
            return Optional.empty();
        }
    }

    @ForReadability
    private Optional<Unit> getNewUnit() {
        if (newFahrenheit.isChecked()) {
            return Optional.of(Unit.FAHRENHEIT);
        } else if (newCelsius.isChecked()) {
            return Optional.of(Unit.CELSIUS);
        } else if (newKelvin.isChecked()) {
            return Optional.of(Unit.KELVIN);
        } else {
            return Optional.empty();
        }
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
