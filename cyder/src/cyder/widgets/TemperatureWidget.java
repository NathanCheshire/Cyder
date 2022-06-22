package cyder.widgets;

import cyder.annotations.CyderAuthor;
import cyder.annotations.Vanilla;
import cyder.annotations.Widget;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderIcons;
import cyder.handlers.internal.Logger;
import cyder.ui.CyderButton;
import cyder.ui.CyderCheckbox;
import cyder.ui.CyderFrame;
import cyder.ui.CyderTextField;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;

@Vanilla
@CyderAuthor
public class TemperatureWidget {
    private CyderFrame temperatureFrame;
    private CyderTextField startingValue;

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
        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    @Widget(triggers = {"temperature", "temp"},
            description = "A temperature conversion widget for the three standard temperature units")
    public static void showGui() {
        getInstance().innerShowGUI();
    }

    public void innerShowGUI() {
        if (temperatureFrame != null) {
            temperatureFrame.dispose();
        }

        temperatureFrame = new CyderFrame(600, 340, CyderIcons.defaultBackground);
        temperatureFrame.setTitle("Temperature Converter");

        JLabel ValueLabel = new JLabel("Measurement: ");
        ValueLabel.setFont(CyderFonts.segoe20);

        startingValue = new CyderTextField(0);
        startingValue.setHorizontalAlignment(JTextField.CENTER);
        startingValue.setKeyEventRegexMatcher(
                "(\\-?)" +
                        "|(\\-?[0-9]+)" +
                        "|(\\-?[0-9]+.)" +
                        "|(\\-?[0-9]+.[0-9]+)");

        ValueLabel.setBounds(60, 40, 200, 30);

        temperatureFrame.getContentPane().add(ValueLabel);
        startingValue.setBounds(240, 40, 300, 35);
        temperatureFrame.getContentPane().add(startingValue);

        oldFahrenheit = new CyderCheckbox();
        oldCelsius = new CyderCheckbox();
        oldKelvin = new CyderCheckbox();

        JLabel oldFahrenheitLabel = new JLabel("Fahrenheit");
        oldFahrenheitLabel.setFont(CyderFonts.segoe30.deriveFont(22f));
        oldFahrenheitLabel.setForeground(CyderColors.navy);
        oldFahrenheitLabel.setBounds(140, 110, 250, 30);
        temperatureFrame.getContentPane().add(oldFahrenheitLabel);

        JLabel oldCelsiusLabel = new JLabel("Celsius");
        oldCelsiusLabel.setFont(CyderFonts.segoe30.deriveFont(22f));
        oldCelsiusLabel.setForeground(CyderColors.navy);
        oldCelsiusLabel.setBounds(140, 170, 250, 30);
        temperatureFrame.getContentPane().add(oldCelsiusLabel);

        JLabel oldKelvinLabel = new JLabel("Kelvin");
        oldKelvinLabel.setFont(CyderFonts.segoe30.deriveFont(22f));
        oldKelvinLabel.setForeground(CyderColors.navy);
        oldKelvinLabel.setBounds(140, 230, 250, 30);
        temperatureFrame.getContentPane().add(oldKelvinLabel);

        oldFahrenheit.setBounds(80, 100, 50, 50);
        oldCelsius.setBounds(80, 160, 50, 50);
        oldKelvin.setBounds(80, 220, 50, 50);

        oldFahrenheit.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                oldCelsius.setNotChecked();
                oldKelvin.setNotChecked();
            }
        });

        oldCelsius.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                oldFahrenheit.setNotChecked();
                oldKelvin.setNotChecked();
            }
        });

        oldKelvin.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                oldCelsius.setNotChecked();
                oldFahrenheit.setNotChecked();
            }
        });

        temperatureFrame.getContentPane().add(oldFahrenheit);
        temperatureFrame.getContentPane().add(oldCelsius);
        temperatureFrame.getContentPane().add(oldKelvin);

        JLabel twoLabel = new JLabel("-2-");
        twoLabel.setFont(CyderFonts.segoe30.deriveFont(45f));
        twoLabel.setForeground(CyderColors.navy);
        twoLabel.setBounds(260, 150, 150, 60);
        temperatureFrame.getContentPane().add(twoLabel);

        newFahrenheit = new CyderCheckbox();
        newCelsius = new CyderCheckbox();
        newKelvin = new CyderCheckbox();

        JLabel newFahrenheitLabel = new JLabel("Fahrenheit");
        newFahrenheitLabel.setFont(CyderFonts.segoe30.deriveFont(22f));
        newFahrenheitLabel.setForeground(CyderColors.navy);
        newFahrenheitLabel.setBounds(430, 110, 250, 30);
        temperatureFrame.getContentPane().add(newFahrenheitLabel);

        JLabel newCelsiusLabel = new JLabel("Celsius");
        newCelsiusLabel.setFont(CyderFonts.segoe30.deriveFont(22f));
        newCelsiusLabel.setForeground(CyderColors.navy);
        newCelsiusLabel.setBounds(430, 170, 250, 30);
        temperatureFrame.getContentPane().add(newCelsiusLabel);

        JLabel newKelvinLabel = new JLabel("Kelvin");
        newKelvinLabel.setFont(CyderFonts.segoe30.deriveFont(22f));
        newKelvinLabel.setForeground(CyderColors.navy);
        newKelvinLabel.setBounds(430, 230, 250, 30);
        temperatureFrame.getContentPane().add(newKelvinLabel);

        newFahrenheit.setBounds(370, 100, 50, 50);
        newCelsius.setBounds(370, 160, 50, 50);
        newKelvin.setBounds(370, 220, 50, 50);

        newFahrenheit.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                newCelsius.setNotChecked();
                newKelvin.setNotChecked();
            }
        });

        newCelsius.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                newFahrenheit.setNotChecked();
                newKelvin.setNotChecked();
            }
        });

        newKelvin.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                newCelsius.setNotChecked();
                newFahrenheit.setNotChecked();
            }
        });

        temperatureFrame.getContentPane().add(newFahrenheit);
        temperatureFrame.getContentPane().add(newCelsius);
        temperatureFrame.getContentPane().add(newKelvin);

        CyderButton calculate = new CyderButton("Calculate");
        calculate.setBorder(new LineBorder(CyderColors.navy, 5, false));

        calculate.addActionListener(e -> {
            try {
                DecimalFormat tempFormat = new DecimalFormat("#.####");
                double CalculationValue = Double.parseDouble(startingValue.getText());

                if (oldKelvin.isChecked() && CalculationValue <= 0) {
                    temperatureFrame.notify("Temperatures below absolute zero are imposible.");
                } else {
                    if (oldFahrenheit.isChecked()) {
                        if (newFahrenheit.isChecked()) {
                            temperatureFrame.notify("Get out of here with that. Your value is already in Fahrenheit.");
                        } else if (newCelsius.isChecked()) {
                            double CelsiusFromFahrenheit;

                            CelsiusFromFahrenheit = (CalculationValue - 32.0) / 1.8;

                            temperatureFrame.notify(CalculationValue + " Fahrenheit converted to Celsius equals: "
                                    + tempFormat.format(CelsiusFromFahrenheit));

                            startingValue.setText("");
                        } else if (newKelvin.isChecked()) {
                            double KelvinFromFahrenheit;
                            KelvinFromFahrenheit = (CalculationValue + 459.67) * 5 / 9;

                            if (KelvinFromFahrenheit >= 0) {
                                temperatureFrame.notify(CalculationValue + " Fahrenheit converted to Kelvin equals: "
                                        + tempFormat.format(KelvinFromFahrenheit));

                                startingValue.setText("");

                            } else {
                                temperatureFrame.notify("Temperatures below absolute zero are imposible.");
                            }
                        } else {
                            temperatureFrame.notify("Please select the unit to convert to.");
                        }
                    } else if (oldCelsius.isChecked()) {
                        if (newFahrenheit.isChecked()) {
                            double FahrenheitFromCelsius;

                            FahrenheitFromCelsius = (CalculationValue * 1.8) + 32;

                            temperatureFrame.notify(CalculationValue + " Celsius converted to Fahrenheit equals: "
                                    + tempFormat.format(FahrenheitFromCelsius));

                            startingValue.setText("");
                        } else if (newCelsius.isChecked()) {
                            temperatureFrame.notify("Get out of here with that. Your value is already in Celsius.");
                        } else if (newKelvin.isChecked()) {
                            double KelvinFromCelsius;
                            KelvinFromCelsius = CalculationValue + 273.15;

                            if (KelvinFromCelsius >= 0) {
                                temperatureFrame.notify(CalculationValue + " Celsius converted to Kelvin equals: "
                                        + tempFormat.format(KelvinFromCelsius));

                                startingValue.setText("");
                            } else {
                                temperatureFrame.notify("Temperatures below absolute zero are imposible.");
                            }
                        } else {
                            temperatureFrame.notify("Please select the unit to convert to.");
                        }
                    } else if (oldKelvin.isChecked()) {
                        if (newFahrenheit.isChecked()) {
                            double FahrenheitFromKelvin;

                            FahrenheitFromKelvin = CalculationValue * 1.8 - 459.67;

                            temperatureFrame.notify(CalculationValue + " Kelvin converted to Fahrenheit equals: "
                                    + tempFormat.format(FahrenheitFromKelvin));

                            startingValue.setText("");
                        } else if (newCelsius.isChecked()) {
                            double CelsiusFromKelvin;

                            CelsiusFromKelvin = CalculationValue - 273.15;

                            temperatureFrame.notify(CalculationValue + " Kelvin converted to Celsius equals: "
                                    + tempFormat.format(CelsiusFromKelvin));

                            startingValue.setText("");
                        } else if (newKelvin.isChecked()) {
                            temperatureFrame.notify("Get out of here with that. Your value is already in Kelvin");
                        } else {
                            temperatureFrame.notify("Please select the unit to convert to.");
                        }
                    } else
                        temperatureFrame.notify("Please select your current temperature unit and the one you want to convet to.");
                }
            } catch (Exception ex) {
                if (startingValue.getText().isEmpty()) {
                    temperatureFrame.notify("Please enter a starting value.");
                } else {
                    temperatureFrame.notify("Your value must only contain numbers.");
                }
            }
        });

        CyderButton resetValues = new CyderButton("Reset Values");
        resetValues.setBorder(new LineBorder(CyderColors.navy, 5, false));
        resetValues.addActionListener(e -> {
            startingValue.setText("");
            oldCelsius.setNotChecked();
            oldFahrenheit.setNotChecked();
            oldKelvin.setNotChecked();
            newCelsius.setNotChecked();
            newFahrenheit.setNotChecked();
            newKelvin.setNotChecked();
        });

        calculate.setBackground(CyderColors.regularRed);
        calculate.setFont(CyderFonts.segoe20);
        resetValues.setFocusPainted(false);
        resetValues.setBackground(CyderColors.regularRed);
        resetValues.setFont(CyderFonts.segoe20);

        calculate.setBounds(140, 280, 150, 40);
        resetValues.setBounds(300, 280, 150, 40);

        temperatureFrame.getContentPane().add(calculate);
        temperatureFrame.getContentPane().add(resetValues);
        temperatureFrame.finalizeAndShow();
    }
}
