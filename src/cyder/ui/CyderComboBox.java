package cyder.ui;

import cyder.utilities.ReflectionUtil;

import javax.swing.*;

public class CyderComboBox extends JLabel {
    private int width;
    private int height;

    private CyderTextField comboTextField;
    private CyderButton comboSwitchButton;

    private int switchingIndex = 0;
    private String[] comboBoxOptions = {};

    public CyderComboBox(int width, int height) {
        this(width, height, new String[]{"NULL"});
    }

    public CyderComboBox(int width, int height, String[] comboBoxOptions) {
        if (width < 60 || height < 20)
            throw new IllegalArgumentException("Width must be at least 60 and height must be at least 20");

        this.width = width;
        this.height = height;
        this.comboBoxOptions = comboBoxOptions;

        setSize(width, height);

        comboTextField = new CyderTextField(0);
        comboTextField.setEditable(false);
        comboTextField.setBounds(0,0, width - 30, height);
        comboTextField.setFocusable(false);
        refreshText();
        this.add(comboTextField);

        comboSwitchButton = new CyderButton("▼");
        comboSwitchButton.setBounds(width - 40,0,40,40);
        add(comboSwitchButton);
        comboSwitchButton.addActionListener(e -> incIndex());
    }

    public void incIndex() {
        this.switchingIndex += 1;

        if (switchingIndex > comboBoxOptions.length - 1)
            switchingIndex = 0;

        refreshText();
    }

    public void decIndex() {
        this.switchingIndex -= 1;

        if (switchingIndex < 0)
            switchingIndex = comboBoxOptions.length - 1;

        refreshText();
    }

    public void setComboBoxOptions(String[] newOptions) {
        if (newOptions.length == 0)
            throw new IllegalArgumentException("Must provide a valid set of options");

        this.comboBoxOptions = newOptions;
        switchingIndex = 0;
        refreshText();
    }

    public int getIndex() {
        return switchingIndex;
    }

    public String getValue() {
        return comboBoxOptions[switchingIndex];
    }

    public void setIndex(int newIndex) {
        if (comboBoxOptions == null || comboBoxOptions.length == 0)
            throw new RuntimeException("ComboBoxOptions not yet set");

        if (newIndex < 0)
            newIndex += comboBoxOptions.length;

        if (newIndex > comboBoxOptions.length - 1)
            throw new IndexOutOfBoundsException("Index is out of bounds of provided comboBoxOptions");

        this.switchingIndex = newIndex;
        refreshText();
    }

    public void refreshText() {
        if (switchingIndex > comboBoxOptions.length - 1)
            throw new IndexOutOfBoundsException("Index is out of bounds");

        comboTextField.setText(comboBoxOptions[switchingIndex]);

        StringBuilder tooltipBuilder = new StringBuilder();

        for (int i = 0 ; i < Math.min(10, comboBoxOptions.length) ; i++) {
            tooltipBuilder.append(comboBoxOptions[i]);

            if (i != Math.min(10, comboBoxOptions.length) - 1)
                tooltipBuilder.append(" -> ");
        }

        comboTextField.setToolTipText(tooltipBuilder.toString());
    }

    public CyderButton getComboSwitchButton() {
        return this.comboSwitchButton;
    }

    @Override
    public String toString() {
        return ReflectionUtil.commonCyderUIReflection(this);
    }
}
