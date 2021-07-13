package cyder.ui;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.utilities.StringUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
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

    public JLabel generateScrollList(Color background, Color sepColor) {
        Font menuFont = CyderFonts.defaultFontSmall;
        int fontHeight = CyderFrame.getMinHeight("TURNED MYSELF INTO A PICKLE MORTY!", menuFont);

        JLabel retLabel = new JLabel("");
        retLabel.setSize(this.width, this.height);
        retLabel.setBackground(background);
        retLabel.setOpaque(true);
        retLabel.setVisible(true);

        JTextPane listPane = new JTextPane();
        listPane.setEditable(false);
        listPane.setAutoscrolls(false);
        listPane.setBounds(0, 0, this.width , this.height);
        listPane.setFocusable(true);
        listPane.setOpaque(false);
        listPane.setBackground(background);

        //used to add the CyderScrollListLabels
        StringUtil printingUtil = new StringUtil(listPane);

        for (int i = 0 ; i < elements.size() ; i++) {
            printingUtil.printlnComponent(elements.get(i));

            if (i != elements.size() - 1)
                printingUtil.printlnComponent(getSepLabel(CyderColors.navy, ";)"));
        }


        CyderScrollPane scrollPane = new CyderScrollPane(listPane);
        scrollPane.setThumbSize(5);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setFocusable(true);
        scrollPane.setOpaque(false);
        scrollPane.setThumbColor(CyderColors.intellijPink);
        scrollPane.setBackground(background);
        scrollPane.setBorder(new LineBorder(CyderColors.navy,5,false));
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBounds(0, 0, this.width, this.height);
        retLabel.add(scrollPane);

        //set list location to top
        listPane.setCaretPosition(0);

        return retLabel;
    }

    public void addElement(JLabel label) {
        elements.add(label);
    }

    public void removeElement(JLabel label) {
        elements.remove(label);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public LinkedList<JLabel> getElements() {
        return elements;
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

    public void setElements(LinkedList<JLabel> elements) {
        this.elements = elements;
    }

    public void setSelectionPolicy(SelectionPolicy selectionPolicy) {
        this.selectionPolicy = selectionPolicy;
    }

    private JLabel getSepLabel(Color c, String associatedText) {
        CyderLabel sepLabel = new CyderLabel(associatedText) {
            @Override
            public void paintComponent(Graphics g) {
                g.setColor(c);
                g.fillRect(0, 10, this.getWidth(), 5);
                g.dispose();
            }
        };
        sepLabel.setForeground(c);
        return sepLabel;
    }

    public static JLabel generateLabel(String text) {
        JLabel ret = new JLabel(text);
        ret.setForeground(CyderColors.navy);
        ret.setFont(CyderFonts.defaultFontSmall);
        ret.setVerticalAlignment(JLabel.CENTER);
        return ret;
    }
}
