package cyder.widgets;

import cyder.annotations.Widget;
import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.consts.CyderImages;
import cyder.genesis.GenesisShare;
import cyder.ui.CyderButton;
import cyder.ui.CyderCheckBox;
import cyder.ui.CyderFrame;
import cyder.ui.CyderTextField;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;

public class TemperatureWidget {
    private CyderFrame temperatureFrame;
    private CyderTextField startingValue;

    private CyderCheckBox oldFahrenheit;
    private CyderCheckBox newFahrenheit;
    private CyderCheckBox oldCelsius;
    private CyderCheckBox newCelsius;
    private CyderCheckBox oldKelvin;
    private CyderCheckBox newKelvin;

    public TemperatureWidget() {
        //multiple instances are allowed
    }

    @Widget(trigger = "temperature", description = "A temperature conversion widget for the three standard temperature units")
    public void showGUI() {
        if (temperatureFrame != null)
            temperatureFrame.dispose();

        temperatureFrame = new CyderFrame(600,340, CyderImages.defaultBackground);
        temperatureFrame.setTitle("Temperature Converter");

        JLabel ValueLabel = new JLabel("Measurement: ");
        ValueLabel.setFont(CyderFonts.weatherFontSmall);
        
        startingValue = new CyderTextField(0);
        startingValue.setRegexMatcher("[0-9.\\-]+");
        
        ValueLabel.setBounds(60,40, 200, 30);
        
        temperatureFrame.getContentPane().add(ValueLabel);
        startingValue.setBounds(240,40, 300, 35);
        temperatureFrame.getContentPane().add(startingValue);

        oldFahrenheit =  new CyderCheckBox();
        oldCelsius =  new CyderCheckBox();
        oldKelvin = new CyderCheckBox();

        JLabel oldFahrenheitLabel = new JLabel("Fahrenheit");
        oldFahrenheitLabel.setFont(CyderFonts.weatherFontBig.deriveFont(22f));
        oldFahrenheitLabel.setForeground(CyderColors.navy);
        oldFahrenheitLabel.setBounds(140,110,250,30);
        temperatureFrame.getContentPane().add(oldFahrenheitLabel);

        JLabel oldCelsiusLabel = new JLabel("Celsius");
        oldCelsiusLabel.setFont(CyderFonts.weatherFontBig.deriveFont(22f));
        oldCelsiusLabel.setForeground(CyderColors.navy);
        oldCelsiusLabel.setBounds(140,170,250,30);
        temperatureFrame.getContentPane().add(oldCelsiusLabel);

        JLabel oldKelvinLabel = new JLabel("Kelvin");
        oldKelvinLabel.setFont(CyderFonts.weatherFontBig.deriveFont(22f));
        oldKelvinLabel.setForeground(CyderColors.navy);
        oldKelvinLabel.setBounds(140,230,250,30);
        temperatureFrame.getContentPane().add(oldKelvinLabel);
        
        oldFahrenheit.setBounds(80,100,50,50);
        oldCelsius.setBounds(80,160,50,50);
        oldKelvin.setBounds(80,220,50,50);
        
        oldFahrenheit.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                oldCelsius.setNotSelected();
                oldKelvin.setNotSelected();
            }
        });

        oldCelsius.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                oldFahrenheit.setNotSelected();
                oldKelvin.setNotSelected();
            }
        });

        oldKelvin.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                oldCelsius.setNotSelected();
                oldFahrenheit.setNotSelected();
            }
        });
        
        temperatureFrame.getContentPane().add(oldFahrenheit);
        temperatureFrame.getContentPane().add(oldCelsius);
        temperatureFrame.getContentPane().add(oldKelvin);

        JLabel twoLabel = new JLabel("-2-");
        twoLabel.setFont(CyderFonts.weatherFontBig.deriveFont(45f));
        twoLabel.setForeground(CyderColors.navy);
        twoLabel.setBounds(260,150,150,60);
        temperatureFrame.getContentPane().add(twoLabel);

        newFahrenheit =  new CyderCheckBox();
        newCelsius =  new CyderCheckBox();
        newKelvin = new CyderCheckBox();

        JLabel newFahrenheitLabel = new JLabel("Fahrenheit");
        newFahrenheitLabel.setFont(CyderFonts.weatherFontBig.deriveFont(22f));
        newFahrenheitLabel.setForeground(CyderColors.navy);
        newFahrenheitLabel.setBounds(430,110,250,30);
        temperatureFrame.getContentPane().add(newFahrenheitLabel);

        JLabel newCelsiusLabel = new JLabel("Celsius");
        newCelsiusLabel.setFont(CyderFonts.weatherFontBig.deriveFont(22f));
        newCelsiusLabel.setForeground(CyderColors.navy);
        newCelsiusLabel.setBounds(430,170,250,30);
        temperatureFrame.getContentPane().add(newCelsiusLabel);

        JLabel newKelvinLabel = new JLabel("Kelvin");
        newKelvinLabel.setFont(CyderFonts.weatherFontBig.deriveFont(22f));
        newKelvinLabel.setForeground(CyderColors.navy);
        newKelvinLabel.setBounds(430,230,250,30);
        temperatureFrame.getContentPane().add(newKelvinLabel);
        
        newFahrenheit.setBounds(370,100,50,50);
        newCelsius.setBounds(370,160,50,50);
        newKelvin.setBounds(370,220,50,50);

        newFahrenheit.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                newCelsius.setNotSelected();
                newKelvin.setNotSelected();
            }
        });

        newCelsius.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                newFahrenheit.setNotSelected();
                newKelvin.setNotSelected();
            }
        });

        newKelvin.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                newCelsius.setNotSelected();
                newFahrenheit.setNotSelected();
            }
        });
        
        temperatureFrame.getContentPane().add(newFahrenheit);
        temperatureFrame.getContentPane().add(newCelsius);
        temperatureFrame.getContentPane().add(newKelvin);

        CyderButton calculate = new CyderButton("Calculate");
        calculate.setBorder(new LineBorder(CyderColors.navy,5,false));

        calculate.addActionListener(e -> {
            try {
                DecimalFormat tempFormat = new DecimalFormat("#.####");
                double CalculationValue = Double.parseDouble(startingValue.getText());

                if (oldKelvin.isSelected() && CalculationValue <= 0) {
                    temperatureFrame.notify("Temperatures below absolute zero are imposible.");
                } else {
                    if (oldFahrenheit.isSelected()) {
                        if (newFahrenheit.isSelected()) {
                            temperatureFrame.notify("Get out of here with that. Your value is already in Fahrenheit.");
                        } else if (newCelsius.isSelected()) {
                            double CelsiusFromFahrenheit;

                            CelsiusFromFahrenheit = (CalculationValue - 32.0) / 1.8;

                            temperatureFrame.notify( CalculationValue + " Fahrenheit converted to Celsius equals: "
                                    + tempFormat.format(CelsiusFromFahrenheit));

                            startingValue.setText("");
                        } else if (newKelvin.isSelected()) {
                            double KelvinFromFahrenheit;
                            KelvinFromFahrenheit = (CalculationValue + 459.67) * 5/9;

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
                    } else if (oldCelsius.isSelected()) {
                        if (newFahrenheit.isSelected()) {
                            double FahrenheitFromCelsius;

                            FahrenheitFromCelsius = (CalculationValue *1.8) + 32;

                            temperatureFrame.notify(CalculationValue + " Celsius converted to Fahrenheit equals: "
                                    + tempFormat.format(FahrenheitFromCelsius));

                            startingValue.setText("");
                        } else if (newCelsius.isSelected()) {
                            temperatureFrame.notify("Get out of here with that. Your value is already in Celsius.");
                        } else if (newKelvin.isSelected()) {
                            double KelvinFromCelsius;
                            KelvinFromCelsius = CalculationValue + 273.15 ;

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
                    } else if (oldKelvin.isSelected()) {
                        if (newFahrenheit.isSelected()) {
                            double FahrenheitFromKelvin;

                            FahrenheitFromKelvin = CalculationValue * 1.8 - 459.67;

                            temperatureFrame.notify(CalculationValue + " Kelvin converted to Fahrenheit equals: "
                                    + tempFormat.format(FahrenheitFromKelvin));

                            startingValue.setText("");
                        } else if (newCelsius.isSelected()) {
                            double CelsiusFromKelvin;

                            CelsiusFromKelvin = CalculationValue - 273.15;

                            temperatureFrame.notify( CalculationValue + " Kelvin converted to Celsius equals: "
                                    + tempFormat.format(CelsiusFromKelvin));

                            startingValue.setText("");
                        } else if (newKelvin.isSelected()) {
                            temperatureFrame.notify("Get out of here with that. Your value is already in Kelvin");
                        } else {
                            temperatureFrame.notify("Please select the unit to convert to.");
                        }
                    }

                    else
                        temperatureFrame.notify("Please select your current temperature unit and the one you want to convet to.");
                }
            }

            catch (Exception ex) {
                if (startingValue.getText().length() == 0) {
                    temperatureFrame.notify("Please enter a starting value.");
                } else {
                    temperatureFrame.notify("Your value must only contain numbers.");
                }
            }
        });

        CyderButton resetValues = new CyderButton("Reset Values");
        resetValues.setBorder(new LineBorder(CyderColors.navy,5,false));
        resetValues.addActionListener(e -> {
            startingValue.setText("");
            oldCelsius.setNotSelected();
            oldFahrenheit.setNotSelected();
            oldKelvin.setNotSelected();
            newCelsius.setNotSelected();
            newFahrenheit.setNotSelected();
            newKelvin.setNotSelected();
        });

        calculate.setBackground(CyderColors.regularRed);
        calculate.setFont(CyderFonts.weatherFontSmall);
        resetValues.setFocusPainted(false);
        resetValues.setBackground(CyderColors.regularRed);
        resetValues.setFont(CyderFonts.weatherFontSmall);

        calculate.setBounds(140,280,150,40);
        resetValues.setBounds(300,280,150,40);

        temperatureFrame.getContentPane().add(calculate);
        temperatureFrame.getContentPane().add(resetValues);
        temperatureFrame.setVisible(true);
        temperatureFrame.setLocationRelativeTo(GenesisShare.getDominantFrame());
    }
}
