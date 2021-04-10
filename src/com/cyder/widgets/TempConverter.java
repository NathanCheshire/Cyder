package com.cyder.widgets;

import com.cyder.Constants.CyderColors;
import com.cyder.Constants.CyderFonts;
import com.cyder.ui.CyderButton;
import com.cyder.ui.CyderFrame;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.text.DecimalFormat;

import static com.cyder.Constants.CyderStrings.DEFAULT_BACKGROUND_PATH;

public class TempConverter {
    private CyderFrame temperatureFrame;
    private JTextField startingValue;
    private JRadioButton oldFahrenheit;
    private JRadioButton newFahrenheit;
    private JRadioButton oldCelsius;
    private JRadioButton newCelsius;
    private JRadioButton oldKelvin;
    private JRadioButton newKelvin;
    private ButtonGroup radioNewValueGroup;
    private ButtonGroup radioCurrentValueGroup;

    public TempConverter() {
        if (temperatureFrame != null)
            temperatureFrame.closeAnimation();

        temperatureFrame = new CyderFrame(600,320,new ImageIcon(DEFAULT_BACKGROUND_PATH));
        temperatureFrame.setTitle("Temperature Converter");

        JLabel ValueLabel = new JLabel("Measurement: ");
        ValueLabel.setFont(CyderFonts.weatherFontSmall);
        startingValue = new JTextField(20);
        startingValue.setBorder(new LineBorder(CyderColors.navy,5,false));
        startingValue.setForeground(CyderColors.navy);
        startingValue.setSelectionColor(CyderColors.selectionColor);
        startingValue.setFont(CyderFonts.weatherFontSmall);
        ValueLabel.setBounds(60,40, 200, 30);
        temperatureFrame.getContentPane().add(ValueLabel);
        startingValue.setBounds(240,40, 300, 35);
        temperatureFrame.getContentPane().add(startingValue);

        oldFahrenheit =  new JRadioButton("Fahrenheit");
        oldCelsius =  new JRadioButton("Celsius");
        oldKelvin = new JRadioButton("Kelvin");
        oldFahrenheit.setFont(CyderFonts.weatherFontBig);
        oldCelsius.setFont(CyderFonts.weatherFontBig);
        oldKelvin.setFont(CyderFonts.weatherFontBig);
        radioCurrentValueGroup = new ButtonGroup();
        radioCurrentValueGroup.add(oldFahrenheit);
        radioCurrentValueGroup.add(oldCelsius);
        radioCurrentValueGroup.add(oldKelvin);
        oldFahrenheit.setBounds(80,100,300,30);
        oldCelsius.setBounds(80,150,200,30);
        oldKelvin.setBounds(80,200,200,30);
        oldFahrenheit.setOpaque(false);
        oldCelsius.setOpaque(false);
        oldKelvin.setOpaque(false);
        oldFahrenheit.setFocusPainted(false);
        oldCelsius.setFocusPainted(false);
        oldKelvin.setFocusPainted(false);
        temperatureFrame.getContentPane().add(oldFahrenheit);
        temperatureFrame.getContentPane().add(oldCelsius);
        temperatureFrame.getContentPane().add(oldKelvin);

        JLabel NewValue = new JLabel("-2-");
        NewValue.setFont(CyderFonts.weatherFontBig.deriveFont(60f));
        NewValue.setBounds(260,150,150,60);
        temperatureFrame.getContentPane().add(NewValue);

        newFahrenheit =  new JRadioButton("Fahrenheit");
        newCelsius =  new JRadioButton("Celsius");
        newKelvin = new JRadioButton("Kelvin");
        newFahrenheit.setFont(CyderFonts.weatherFontBig);
        newCelsius.setFont(CyderFonts.weatherFontBig);
        newKelvin.setFont(CyderFonts.weatherFontBig);
        radioNewValueGroup = new ButtonGroup();
        radioNewValueGroup.add(newFahrenheit);
        radioNewValueGroup.add(newCelsius);
        radioNewValueGroup.add(newKelvin);
        newFahrenheit.setBounds(370,100,300,30);
        newCelsius.setBounds(370,150,200,30);
        newKelvin.setBounds(370,200,200,30);
        newFahrenheit.setOpaque(false);
        newCelsius.setOpaque(false);
        newKelvin.setOpaque(false);
        newFahrenheit.setFocusPainted(false);
        newCelsius.setFocusPainted(false);
        newKelvin.setFocusPainted(false);
        temperatureFrame.getContentPane().add(newFahrenheit);
        temperatureFrame.getContentPane().add(newCelsius);
        temperatureFrame.getContentPane().add(newKelvin);

        CyderButton calculate = new CyderButton("Calculate");
        calculate.setBorder(new LineBorder(CyderColors.navy,5,false));

        calculate.addActionListener(e -> {
            try {
                DecimalFormat tempFormat = new DecimalFormat(".####");
                double CalculationValue = Double.parseDouble(startingValue.getText());

                if (oldKelvin.isSelected() && CalculationValue <= 0) {
                    temperatureFrame.inform("Temperatures below absolute zero are imposible.","");
                }

                else {
                    if (oldFahrenheit.isSelected()) {
                        if (newFahrenheit.isSelected())
                            temperatureFrame.inform("Get out of here with that. Your value is already in Fahrenheit.","");

                        else if (newCelsius.isSelected()) {
                            double CelsiusFromFahrenheit;

                            CelsiusFromFahrenheit = (CalculationValue - 32.0) / 1.8;

                            temperatureFrame.inform( CalculationValue + " Fahrenheit converted to Celsius equals: "
                                    + tempFormat.format(CelsiusFromFahrenheit),"");

                            startingValue.setText("");

                            radioCurrentValueGroup.clearSelection();

                            radioNewValueGroup.clearSelection();
                        }

                        else if (newKelvin.isSelected()) {
                            double KelvinFromFahrenheit;
                            KelvinFromFahrenheit = (CalculationValue +459.67) * 5/9;

                            if (KelvinFromFahrenheit >= 0) {
                                temperatureFrame.inform(CalculationValue + " Fahrenheit converted to Kelvin equals: "
                                        + tempFormat.format(KelvinFromFahrenheit),"");

                                startingValue.setText("");

                                radioCurrentValueGroup.clearSelection();

                                radioNewValueGroup.clearSelection();
                            }

                            else
                                temperatureFrame.inform("Temperatures below absolute zero are imposible.","");
                        }
                    }

                    else if (oldCelsius.isSelected()) {
                        if (newFahrenheit.isSelected()) {
                            double FahrenheitFromCelsius;

                            FahrenheitFromCelsius = (CalculationValue *1.8) + 32;

                            temperatureFrame.inform(CalculationValue + " Celsius converted to Fahrenheit equals: "
                                    + tempFormat.format(FahrenheitFromCelsius),"");

                            startingValue.setText("");

                            radioCurrentValueGroup.clearSelection();

                            radioNewValueGroup.clearSelection();
                        }

                        else if (newCelsius.isSelected())
                            temperatureFrame.inform("Get out of here with that. Your value is already in Celsius.","");

                        else if (newKelvin.isSelected()) {
                            double KelvinFromCelsius;

                            KelvinFromCelsius = CalculationValue + 273.15 ;

                            if (KelvinFromCelsius >= 0) {
                                temperatureFrame.inform(CalculationValue + " Celsius converted to Kelvin equals: "
                                        + tempFormat.format(KelvinFromCelsius),"");

                                startingValue.setText("");

                                radioCurrentValueGroup.clearSelection();

                                radioNewValueGroup.clearSelection();
                            }

                            else
                                temperatureFrame.inform("Temperatures below absolute zero are imposible.","");
                        }
                    }

                    else if (oldKelvin.isSelected()) {
                        if (newFahrenheit.isSelected()) {
                            double FahrenheitFromKelvin;

                            FahrenheitFromKelvin = CalculationValue * 1.8 - 459.67;

                            temperatureFrame.inform(CalculationValue + " Kelvin converted to Fahrenheit equals: "
                                    + tempFormat.format(FahrenheitFromKelvin),"");

                            startingValue.setText("");

                            radioCurrentValueGroup.clearSelection();

                            radioNewValueGroup.clearSelection();
                        }

                        else if (newCelsius.isSelected()) {
                            double CelsiusFromKelvin;

                            CelsiusFromKelvin = CalculationValue - 273.15;

                            temperatureFrame.inform( CalculationValue + " Kelvin converted to Celsius equals: "
                                    + tempFormat.format(CelsiusFromKelvin),"");

                            startingValue.setText("");

                            radioCurrentValueGroup.clearSelection();

                            radioNewValueGroup.clearSelection();
                        }

                        else if (newKelvin.isSelected())
                            temperatureFrame.inform("Get out of here with that. Your value is already in Kelvin","");
                    }

                    else
                        temperatureFrame.inform("Please select your current temperature unit and the one you want to convet to.","");
                }
            }

            catch (Exception ex) {
                temperatureFrame.inform("Your value must only contain numbers.","");
            }
        });

        CyderButton resetValues = new CyderButton("Reset Values");
        resetValues.setBorder(new LineBorder(CyderColors.navy,5,false));
        resetValues.setColors(CyderColors.regularRed);
        calculate.setColors(CyderColors.regularRed);
        resetValues.addActionListener(e -> {
            startingValue.setText("");
            radioCurrentValueGroup.clearSelection();
            radioNewValueGroup.clearSelection();
        });

        calculate.setBackground(CyderColors.regularRed);
        calculate.setFont(CyderFonts.weatherFontSmall);
        resetValues.setFocusPainted(false);
        resetValues.setBackground(CyderColors.regularRed);
        resetValues.setFont(CyderFonts.weatherFontSmall);

        calculate.setBounds(140,260,150,40);
        resetValues.setBounds(300,260,150,40);

        temperatureFrame.getContentPane().add(calculate);
        temperatureFrame.getContentPane().add(resetValues);
        temperatureFrame.setVisible(true);
        temperatureFrame.setLocationRelativeTo(null);
    }
}
