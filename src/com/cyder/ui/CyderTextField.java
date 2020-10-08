package com.cyder.ui;

import com.cyder.utilities.Util;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class CyderTextField extends JTextField {
    public CyderTextField(int colnum) {
        super(colnum);
    }

    //todo be able to add a character limit and a regex for filtering when initing component

    @Override
    protected void paintComponent(Graphics g) {
        this.setBorder(new LineBorder(new Color(26, 32, 51),5,false));
        this.setForeground(new Util().navy);
        this.setFont(new Util().weatherFontSmall);
        this.setBackground(new Color(0,0,0,0));
        super.paintComponent(g);
    }
}
