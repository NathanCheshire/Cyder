package com.cyder.ui;

import com.cyder.constants.CyderImages;

import javax.swing.*;
import java.awt.event.MouseEvent;

public class CheckBox extends JLabel {

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
        this.setIcon(CyderImages.checkboxSelected);
    }

    public void setNotSelected() {
        this.setIcon(CyderImages.checkboxNotSelected);
    }
}
