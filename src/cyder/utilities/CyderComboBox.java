package cyder.utilities;

import cyder.ui.CyderTextField;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class CyderComboBox extends JComponent {
    private CyderComboBox() {}

    private ArrayList<String> values;
    private int width;
    private int height;
    private CyderTextField textField;
    private JButton dropDownButton;
    private JLabel dropDownLabel;

    public CyderComboBox(ArrayList<String> values) {
        this.values = values;
    }

    public void setValues(ArrayList<String> values) {
        this.values = values;
    }

    public ArrayList<String> getValues() {
        return values;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public void paint(Graphics g) {

    }
}
