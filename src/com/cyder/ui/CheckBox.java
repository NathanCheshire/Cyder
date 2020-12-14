package com.cyder.ui;

import javax.swing.*;
import java.awt.event.MouseEvent;

public class CheckBox extends JLabel {

    private ImageIcon selected = new ImageIcon("src/com/cyder/io/pictures/checkbox1.png");
    private ImageIcon notSelected = new ImageIcon("src/com/cyder/io/pictures/checkbox2.png");

    public CheckBox() {
        this.setSize(100,100);
        this.setNotSelected();
        this.setHorizontalAlignment(JLabel.CENTER);
        this.addMouseListener(new CyderMouseDraggable() {
            @Override
            public void mousePressed(MouseEvent me) {
                super.mousePressed(me);
            }

            @Override
            public void mouseReleased(MouseEvent me) {
                super.mouseReleased(me);
            }
        });
    }

    public void setSelected() {
        this.setIcon(selected);
    }

    public void setNotSelected() {
        this.setIcon(notSelected);
    }
}
