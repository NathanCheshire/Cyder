package com.cyder.handler;

import javax.swing.*;
import java.awt.*;

public class CyderListCellRenderer extends DefaultListCellRenderer {
    private Util rendUtil = new Util();

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (isSelected) {
            c.setBackground(rendUtil.selectionColor);
        }
        return c;
    }
}