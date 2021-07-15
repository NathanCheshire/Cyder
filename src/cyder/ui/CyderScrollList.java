package cyder.ui;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.utilities.StringUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;

public class CyderScrollList {
    private int width;
    private int height;

    private int itemAlignemnt = StyleConstants.ALIGN_LEFT;

    public void setItemAlignemnt(int itemAlignment) {
        this.itemAlignemnt = itemAlignment;
    }

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

    private Font scrollFont = CyderFonts.weatherFontSmall;

    public Font getScrollFont() {
        return this.scrollFont;
    }

    public void setScrollFont(Font f) {
        this.scrollFont = f;
    }

    private Border border = new LineBorder(CyderColors.navy,5,false);

    public void setBorder(Border border) {
        this.border = border;
    }

    public JLabel generateScrollList() {
        Font menuFont = scrollFont;
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

        //item alignment is 0?
        printingUtil.setItemAlignment(itemAlignemnt);

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
        scrollPane.setBorder(border);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBounds(0, 0, this.width, this.height);
        retLabel.add(scrollPane);

        //set list location to top
        listPane.setCaretPosition(0);

        return retLabel;
    }

    public void addElement(String labelText, ScrollAction sa) {
        JLabel add = new JLabel(labelText);
        add.setForeground(CyderColors.navy);
        add.setFont(CyderFonts.weatherFontSmall);
        add.setVerticalAlignment(JLabel.CENTER);
        add.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2 && sa != null) {
                    sa.fire();
                    add.setForeground(CyderColors.navy);
                } else {
                    handleElementClick(add.getText());
                }
            }
        });

        elements.add(add);
    }

    public void addElementWithSingleCLickAction(String labelText, @NotNull ScrollAction sa) {
        JLabel add = new JLabel(labelText);
        add.setForeground(CyderColors.navy);
        add.setFont(CyderFonts.weatherFontSmall);
        add.setVerticalAlignment(JLabel.CENTER);
        add.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                sa.fire();
                handleElementClick(add.getText());
            }
        });

        elements.add(add);
    }

    public interface ScrollAction {
        void fire();
    }

    public void removeAllElements() {
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

    public String getSelectedElement() {
        LinkedList<String> ret = new LinkedList<>();

        for (JLabel element : elements) {
            if (element.getForeground() == CyderColors.regularRed) {
                ret.add(element.getText());
            }
        }

        String retString = "null";

        if (!ret.isEmpty() && ret.get(0) != null)
            retString = ret.get(0);
        return retString;
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

    public void clearSelectedElements() {
        for (JLabel element : elements) {
            element.setForeground(CyderColors.navy);
        }
    }
}
