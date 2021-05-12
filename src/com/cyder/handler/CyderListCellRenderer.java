package com.cyder.handler;

import com.cyder.constants.CyderColors;

import javax.swing.*;
import java.awt.*;

public class CyderListCellRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (isSelected) {
            c.setBackground(CyderColors.selectionColor);
        }
        return c;
    }
}