package cyder.ui;

import cyder.consts.CyderColors;
import cyder.consts.CyderImages;
import cyder.utilities.StringUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CyderScrollList {

    public CyderScrollList() {
        //todo make the class return a label of width and height that everything is on with accessors
        //todo improve this and use it for dir, pizza scroll, and the other ones
        CyderFrame testFrame = new CyderFrame(600,600, CyderImages.defaultBackground);
        testFrame.setTitle("CyderScrollList Test");

        JTextPane menuPane = new JTextPane();
        menuPane.setEditable(false);
        menuPane.setAutoscrolls(false);
        menuPane.setBounds(0,0,400,400);
        menuPane.setFocusable(true);
        menuPane.setOpaque(false);
        menuPane.setBackground(CyderColors.navy);

        CyderScrollPane menuScroll = new CyderScrollPane(menuPane);
        menuScroll.setThumbSize(5);
        menuScroll.getViewport().setOpaque(false);
        menuScroll.setFocusable(true);
        menuScroll.setOpaque(false);
        menuScroll.setThumbColor(CyderColors.intellijPink);
        menuScroll.setBorder(new LineBorder(CyderColors.navy, 5, false));
        menuScroll.setBackground(CyderColors.navy);
        menuScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        menuScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        menuScroll.setBounds(100,100,200,200);
        testFrame.getContentPane().add(menuScroll);

        CyderLabel label = new CyderLabel("Clickable one");
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (label.getForeground() == CyderColors.regularRed) {
                    label.setForeground(CyderColors.navy);
                } else {
                    label.setForeground(CyderColors.regularRed);
                }
            }
        });
        label.setHorizontalAlignment(SwingConstants.LEFT);
        new StringUtil(menuPane).printlnComponent(label);

        testFrame.setVisible(true);
        testFrame.setLocationRelativeTo(null);
    }
}
