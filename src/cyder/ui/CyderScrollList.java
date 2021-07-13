package cyder.ui;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.utilities.StringUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;

public class CyderScrollList {
    private int width;
    private int height;

    private LinkedList<JLabel> elements;

    public enum SelectionPolicy {
        SINGLE, MULTIPLE
    }

    private SelectionPolicy selectionPolicy;

    public CyderScrollList() {
        this(400,400);
    }

    public CyderScrollList(int width, int height) {
        this(width, height, SelectionPolicy.SINGLE);
    }

    public CyderScrollList(int width, int height, SelectionPolicy selectionPolicy) {
        this.width = width;
        this.height = height;
        this.selectionPolicy = selectionPolicy;
        elements = new LinkedList<>();
    }

    public JLabel generateScrollList() {
        Font menuFont = CyderFonts.weatherFontSmall; //todo be able to change this default font
        int fontHeight = CyderFrame.getMinHeight("TURNED MYSELF INTO A PICKLE MORTY!", menuFont);

        JLabel retLabel = new JLabel("");
        retLabel.setSize(this.width, this.height);
        retLabel.setBackground(CyderColors.vanila);
        retLabel.setOpaque(true);
        retLabel.setVisible(true);

        JTextPane listPane = new JTextPane();
        listPane.setEditable(false);
        listPane.setAutoscrolls(false);
        listPane.setBounds(0, 0, this.width , this.height);
        listPane.setFocusable(true);
        listPane.setOpaque(false);
        listPane.setBackground(CyderColors.vanila);

        //used to add the CyderScrollListLabels
        StringUtil printingUtil = new StringUtil(listPane);

        for (int i = 0 ; i < elements.size() ; i++) {
            printingUtil.printlnComponent(elements.get(i));

            if (i != elements.size() - 1)
                printingUtil.printlnComponent(getSepLabel());
        }


        CyderScrollPane scrollPane = new CyderScrollPane(listPane);
        scrollPane.setThumbSize(5);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setFocusable(true);
        scrollPane.setOpaque(false);
        scrollPane.setThumbColor(CyderColors.intellijPink);
        scrollPane.setBackground(CyderColors.vanila);
        scrollPane.setBorder(new LineBorder(CyderColors.navy,5,false));
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBounds(0, 0, this.width, this.height);
        retLabel.add(scrollPane);

        //set list location to top
        listPane.setCaretPosition(0);

        return retLabel;
    }

    public void addElement(String labelText) {
        JLabel add = new JLabel(labelText);
        add.setForeground(CyderColors.navy);
        add.setFont(CyderFonts.weatherFontSmall);
        add.setVerticalAlignment(JLabel.CENTER);
        add.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleElementClick(add.getText());
            }
        });

        elements.add(add);
    }

    public void clearElements() {
        this.elements = new LinkedList<>();
    }

    public void removeElement(String labelText) {
        for (JLabel element : elements) {
            if (element.getText().equals(labelText)) {
                elements.remove(element);
                break;
            }
        }
    }

    private void handleElementClick(String clickedText) {
        if (selectionPolicy == SelectionPolicy.SINGLE) {
            for (JLabel element : elements) {
                if (element.getText().equals(clickedText)) {
                    if (element.getForeground() == CyderColors.regularRed) {
                        element.setForeground(CyderColors.navy);
                    } else {
                        element.setForeground(CyderColors.regularRed);
                    }
                } else {
                    element.setForeground(CyderColors.navy);
                }
            }
        } else {
            for (JLabel element : elements) {
                if (element.getText().equals(clickedText)) {
                    if (element.getForeground() == CyderColors.regularRed) {
                        element.setForeground(CyderColors.navy);
                    } else {
                        element.setForeground(CyderColors.regularRed);
                    }
                }
            }
        }
    }

    public LinkedList<String> getSelectedElements() {
        LinkedList<String> ret = new LinkedList<>();

        for (JLabel element : elements) {
            if (element.getForeground() == CyderColors.regularRed) {
                ret.add(element.getText());
            }
        }

        return ret;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }


    public SelectionPolicy getSelectionPolicy() {
        return selectionPolicy;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }


    public void setSelectionPolicy(SelectionPolicy selectionPolicy) {
        this.selectionPolicy = selectionPolicy;
    }

    private JLabel getSepLabel() {
        CyderLabel sepLabel = new CyderLabel(";)") {
            @Override
            public void paintComponent(Graphics g) {
                g.setColor(CyderColors.navy);
                g.fillRect(0, 10, this.getWidth(), 5);
                g.dispose();
            }
        };
        sepLabel.setForeground(CyderColors.navy);
        return sepLabel;
    }
}
