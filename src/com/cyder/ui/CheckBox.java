package com.cyder.ui;

import javax.swing.*;

public class CheckBox extends JLabel {

    private ImageIcon selected = new ImageIcon("src/com/cyder/io/pictures/checkbox1.png");
    private ImageIcon notSelected = new ImageIcon("src/com/cyder/io/pictures/checkbox2.png");

    public CheckBox() {
        this.setSize(100,100);
        this.setNotSelected();
        this.setHorizontalAlignment(JLabel.CENTER);
    }

    public void setSelected() {
        this.setIcon(selected);
    }

    public void setNotSelected() {
        this.setIcon(notSelected);
    }
}
