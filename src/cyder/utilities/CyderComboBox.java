package cyder.utilities;

import cyder.ui.CyderTextField;

import javax.swing.*;
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
}
