package com.cyder.utilities;

import com.cyder.ui.CyderButton;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.text.DecimalFormat;

public class TempConverter {
    private JFrame temperatureFrame;
    private JTextField startingValue;
    private JRadioButton oldFahrenheit;
    private JRadioButton newFahrenheit;
    private JRadioButton oldCelsius;
    private JRadioButton newCelsius;
    private JRadioButton oldKelvin;
    private JRadioButton newKelvin;
    private ButtonGroup radioNewValueGroup;
    private ButtonGroup radioCurrentValueGroup;

    private Util tempUtil = new Util();

    public TempConverter() {
        if (temperatureFrame != null) {
            tempUtil.closeAnimation(temperatureFrame);
            temperatureFrame.dispose();
        }

        temperatureFrame = new JFrame();

        temperatureFrame.setTitle("Temperature Converter");

        temperatureFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel ParentPanel = (JPanel) temperatureFrame.getContentPane();

        ParentPanel.setLayout(new BoxLayout(ParentPanel,BoxLayout.Y_AXIS));

        ParentPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        JPanel Value = new JPanel();

        JLabel ValueLabel = new JLabel("Measurement: ");

        ValueLabel.setFont(tempUtil.weatherFontSmall);

        startingValue = new JTextField(20);

        startingValue.setBorder(new LineBorder(tempUtil.navy,5,false));

        startingValue.setForeground(tempUtil.navy);

        startingValue.setSelectionColor(tempUtil.selectionColor);

        startingValue.setFont(tempUtil.weatherFontSmall);

        Value.add(ValueLabel);

        Value.add(startingValue);

        ParentPanel.add(Value);

        JPanel RadioCurrentValue = new JPanel();

        JLabel CurrentValue = new JLabel("Current temperature Unit: ");

        CurrentValue.setFont(tempUtil.weatherFontSmall);

        RadioCurrentValue.add(CurrentValue);

        oldFahrenheit =  new JRadioButton("Fahrenheit");

        oldCelsius =  new JRadioButton("Celsius");

        oldKelvin = new JRadioButton("Kelvin");

        oldFahrenheit.setFont(tempUtil.weatherFontSmall);

        oldCelsius.setFont(tempUtil.weatherFontSmall);

        oldKelvin.setFont(tempUtil.weatherFontSmall);

        radioCurrentValueGroup = new ButtonGroup();

        radioCurrentValueGroup.add(oldFahrenheit);

        radioCurrentValueGroup.add(oldCelsius);

        radioCurrentValueGroup.add(oldKelvin);

        ParentPanel.add(RadioCurrentValue);

        RadioCurrentValue.add(oldFahrenheit);

        RadioCurrentValue.add(oldCelsius);

        RadioCurrentValue.add(oldKelvin);

        JPanel RadioNewValue = new JPanel();

        JLabel NewValue = new JLabel("Conversion temperature Unit: ");

        NewValue.setFont(tempUtil.weatherFontSmall);

        RadioNewValue.add(NewValue);

        newFahrenheit =  new JRadioButton("Fahrenheit");

        newCelsius =  new JRadioButton("Celsius");

        newKelvin = new JRadioButton("Kelvin");

        newFahrenheit.setFont(tempUtil.weatherFontSmall);

        newCelsius.setFont(tempUtil.weatherFontSmall);

        newKelvin.setFont(tempUtil.weatherFontSmall);

        radioNewValueGroup = new ButtonGroup();

        radioNewValueGroup.add(newFahrenheit);

        radioNewValueGroup.add(newCelsius);

        radioNewValueGroup.add(newKelvin);

        ParentPanel.add(RadioNewValue);

        RadioNewValue.add(newFahrenheit);

        RadioNewValue.add(newCelsius);

        RadioNewValue.add(newKelvin);

        JPanel BottomButtons = new JPanel();

        CyderButton calculate = new CyderButton("Calculate");

        calculate.setBorder(new LineBorder(tempUtil.navy,5,false));

        calculate.addActionListener(e -> {
            try {
                DecimalFormat tempFormat = new DecimalFormat(".####");
                double CalculationValue = Double.parseDouble(startingValue.getText());

                if (oldKelvin.isSelected() && CalculationValue <= 0) {
                    tempUtil.inform("Temperatures below absolute zero are imposible.","", 400, 200);
                }

                else {
                    if (oldFahrenheit.isSelected()) {
                        if (newFahrenheit.isSelected()) {
                            tempUtil.inform("Get out of here with that. Your value is already in Fahrenheit.","", 400, 200);
                        }

                        else if (newCelsius.isSelected()) {
                            double CelsiusFromFahrenheit;

                            CelsiusFromFahrenheit = (CalculationValue - 32.0) / 1.8;

                            tempUtil.inform( CalculationValue + " Fahrenheit converted to Celsius equals: "
                                    + tempFormat.format(CelsiusFromFahrenheit),"", 400, 200);

                            startingValue.setText("");

                            radioCurrentValueGroup.clearSelection();

                            radioNewValueGroup.clearSelection();
                        }

                        else if (newKelvin.isSelected()) {
                            double KelvinFromFahrenheit;

                            KelvinFromFahrenheit = (CalculationValue +459.67) * 5/9;

                            if (KelvinFromFahrenheit >= 0) {
                                tempUtil.inform(CalculationValue + " Fahrenheit converted to Kelvin equals: "
                                        + tempFormat.format(KelvinFromFahrenheit),"", 400, 200);

                                startingValue.setText("");

                                radioCurrentValueGroup.clearSelection();

                                radioNewValueGroup.clearSelection();
                            }

                            else {
                                tempUtil.inform("Temperatures below absolute zero are imposible.","", 400, 200);
                            }
                        }

                    }

                    else if (oldCelsius.isSelected()) {
                        if (newFahrenheit.isSelected()) {
                            double FahrenheitFromCelsius;

                            FahrenheitFromCelsius = (CalculationValue *1.8) + 32;

                            tempUtil.inform(CalculationValue + " Celsius converted to Fahrenheit equals: "
                                    + tempFormat.format(FahrenheitFromCelsius),"", 400, 200);

                            startingValue.setText("");

                            radioCurrentValueGroup.clearSelection();

                            radioNewValueGroup.clearSelection();
                        }

                        else if (newCelsius.isSelected()) {
                            tempUtil.inform("Get out of here with that. Your value is already in Celsius.","", 400, 200);
                        }

                        else if (newKelvin.isSelected()) {
                            double KelvinFromCelsius;

                            KelvinFromCelsius = CalculationValue + 273.15 ;

                            if (KelvinFromCelsius >= 0) {
                                tempUtil.inform(CalculationValue + " Celsius converted to Kelvin equals: "
                                        + tempFormat.format(KelvinFromCelsius),"", 400, 200);

                                startingValue.setText("");

                                radioCurrentValueGroup.clearSelection();

                                radioNewValueGroup.clearSelection();
                            }

                            else {
                                tempUtil.inform("Temperatures below absolute zero are imposible.","", 400, 200);
                            }
                        }

                    }

                    else if (oldKelvin.isSelected()) {
                        if (newFahrenheit.isSelected()) {
                            double FahrenheitFromKelvin;

                            FahrenheitFromKelvin = CalculationValue * 1.8 - 459.67;

                            tempUtil.inform(CalculationValue + " Kelvin converted to Fahrenheit equals: "
                                    + tempFormat.format(FahrenheitFromKelvin),"", 400, 200);

                            startingValue.setText("");

                            radioCurrentValueGroup.clearSelection();

                            radioNewValueGroup.clearSelection();
                        }

                        else if (newCelsius.isSelected()) {
                            double CelsiusFromKelvin;

                            CelsiusFromKelvin = CalculationValue - 273.15;

                            tempUtil.inform( CalculationValue + " Kelvin converted to Celsius equals: "
                                    + tempFormat.format(CelsiusFromKelvin),"", 400, 200);

                            startingValue.setText("");

                            radioCurrentValueGroup.clearSelection();

                            radioNewValueGroup.clearSelection();
                        }

                        else if (newKelvin.isSelected()) {
                            tempUtil.inform("Get out of here with that. Your value is already in Kelvin","", 400, 200);
                        }
                    }

                    else {
                        tempUtil.inform("Please select your current temperature unit and the one you want to convet to.","", 400, 200);
                    }
                }
            }

            catch (Exception ex) {
                tempUtil.inform("Your value must only contain numbers.","", 400, 200);
            }
        });

        CyderButton resetValues = new CyderButton("Reset Values");

        resetValues.setBorder(new LineBorder(tempUtil.navy,5,false));

        resetValues.setColors(tempUtil.regularRed);
        calculate.setColors(tempUtil.regularRed);

        resetValues.addActionListener(e -> {
            startingValue.setText("");
            radioCurrentValueGroup.clearSelection();
            radioNewValueGroup.clearSelection();
        });

        calculate.setFocusPainted(false);

        calculate.setBackground(tempUtil.regularRed);

        calculate.setFont(tempUtil.weatherFontSmall);

        resetValues.setFocusPainted(false);

        resetValues.setBackground(tempUtil.regularRed);

        resetValues.setFont(tempUtil.weatherFontSmall);

        BottomButtons.add(calculate);

        BottomButtons.add(resetValues);

        ParentPanel.add(BottomButtons);

        temperatureFrame.pack();

        temperatureFrame.setVisible(true);

        temperatureFrame.setLocationRelativeTo(null);

        temperatureFrame.setIconImage(tempUtil.getCyderIcon().getImage());

        temperatureFrame.setAlwaysOnTop(true);

        temperatureFrame.setAlwaysOnTop(false);
    }
}
