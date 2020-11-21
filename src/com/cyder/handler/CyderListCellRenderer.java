package com.cyder.handler;

import com.cyder.utilities.GeneralUtil;

import javax.swing.*;
import java.awt.*;

public class CyderListCellRenderer extends DefaultListCellRenderer {
    private GeneralUtil rendGeneralUtil = new GeneralUtil();

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (isSelected) {
            c.setBackground(rendGeneralUtil.selectionColor);
        }
        return c;
    }
}