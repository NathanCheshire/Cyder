package cyder.ui;

import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.enums.LoggerTag;
import cyder.handlers.internal.Logger;
import cyder.utilities.ReflectionUtil;
import cyder.utilities.StringUtil;
import cyder.utilities.UserUtil;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedList;

public class CyderScrollList {
    private int width;
    private int height;

    public static final Color selectedColor = CyderColors.regularRed;
    private Color nonSelectedColor = CyderColors.navy;

    private JTextPane listPane;

    private int itemAlignment = StyleConstants.ALIGN_LEFT;

    public final void setItemAlignment(int itemAlignment) {
        this.itemAlignment = itemAlignment;
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
        this(width, height, selectionPolicy, false);
    }

    private final boolean darkMode;

    public CyderScrollList(int width, int height, SelectionPolicy selectionPolicy, boolean darkMode) {
        this.width = width;
        this.height = height;
        this.selectionPolicy = selectionPolicy;
        this.darkMode = darkMode;
        elements = new LinkedList<>();

        if (darkMode)
            nonSelectedColor = CyderColors.defaultDarkModeTextColor;

        border = new LineBorder(darkMode ? CyderColors.defaultDarkModeTextColor
                : CyderColors.navy,5,false);

        Logger.log(LoggerTag.OBJECT_CREATION, this);
    }

    private Font scrollFont = CyderFonts.segoe20;

    public final Font getScrollFont() {
        return scrollFont;
    }

    public final void setScrollFont(Font f) {
        scrollFont = f;
    }

    private Border border;

    public final void setBorder(Border border) {
        this.border = border;
    }

    /**
     * The list of scroll lists created in the current instance of Cyder.
     */
    private static final ArrayList<CyderScrollList> scrollLists = new ArrayList<>();

    /**
     * Refreshes all CyderScrollLists that have been created this instance of Cyder.
     */
    public static void refreshAllLists() {
        for (CyderScrollList list : scrollLists) {
            Component parent = SwingUtilities.getRoot(list.getScrollPane());

            if (parent instanceof CyderFrame) {
                CyderFrame parentFrame = (CyderFrame) parent;

                if (list != null && !parentFrame.isDisposed()) {
                    list.refreshList();
                }
            }
        }
    }

    /**
     * Refreshes the list belonging to this instance of CyderScrollList.
     */
    public final void refreshList() {
        boolean compactMode = UserUtil.getCyderUser().getCompactTextMode().equals("1");

        CyderOutputPane cop = new CyderOutputPane(listPane);
        cop.getJTextPane().setText("");

        for (int i = 0 ; i < elements.size() ; i++) {
            cop.getStringUtil().printlnComponent(elements.get(i));

            if (i != elements.size() - 1 && !compactMode)
                cop.getStringUtil().printlnComponent(generateSepLabel());
        }

    }

    /**
     * Generates a label based on the currently set width and height containing the CyderScrollList.
     *
     * @return a label based on the currently set width and height containing the CyderScrollList
     */
    public final JLabel generateScrollList() {
        Font menuFont = scrollFont;
        int fontHeight = StringUtil.getMinHeight("TURNED MYSELF INTO A PICKLE MORTY!", menuFont);

        JLabel retLabel = new JLabel("");
        retLabel.setSize(width, height);
        retLabel.setBackground(darkMode ? CyderColors.darkModeBackgroundColor : CyderColors.vanila);
        retLabel.setOpaque(true);
        retLabel.setVisible(true);

        listPane = new JTextPane();
        listPane.setEditable(false);
        listPane.setAutoscrolls(false);
        listPane.setBounds(0, 0, width , height);
        listPane.setFocusable(true);
        listPane.setOpaque(false);
        listPane.setBackground(darkMode ? CyderColors.darkModeBackgroundColor : CyderColors.vanila);

        SimpleAttributeSet simpleAttributeSet = new SimpleAttributeSet();
        StyleConstants.setAlignment(simpleAttributeSet, itemAlignment);
        listPane.setParagraphAttributes(simpleAttributeSet, true);

        refreshList();
        scrollLists.add(this);

        scrollPane = new CyderScrollPane(listPane);
        scrollPane.setThumbSize(5);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setFocusable(true);
        scrollPane.setOpaque(false);
        scrollPane.setThumbColor(CyderColors.regularPink);
        scrollPane.setBackground(darkMode ? CyderColors.darkModeBackgroundColor : CyderColors.vanila);
        scrollPane.setBorder(border);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBounds(0, 0, width, height);
        retLabel.add(scrollPane);

        //set list location to top
        listPane.setCaretPosition(0);

        return retLabel;
    }

    private CyderScrollPane scrollPane;

    public final CyderScrollPane getScrollPane() {
        return scrollPane;
    }

    public final void addElement(String labelText, Runnable sa) {
        JLabel add = new JLabel(labelText);
        add.setForeground(nonSelectedColor);
        add.setFont(CyderFonts.segoe20);
        add.setVerticalAlignment(SwingConstants.CENTER);
        add.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2 && sa != null) {
                    sa.run();
                    add.setForeground(nonSelectedColor);
                } else {
                    handleElementClick(add.getText());
                }
            }
        });

        elements.add(add);
    }

    public final void addElementWithSingleCLickAction(String labelText, @NotNull Runnable sa) {
        JLabel add = new JLabel(labelText);
        add.setForeground(nonSelectedColor);
        add.setFont(CyderFonts.segoe20);
        add.setVerticalAlignment(SwingConstants.CENTER);
        add.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                sa.run();
                handleElementClick(add.getText());
            }
        });

        elements.add(add);
    }

    public final void removeAllElements() {
        elements = new LinkedList<>();
    }

    public final void removeElement(String labelText) {
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
                    if (element.getForeground() == selectedColor) {
                        element.setForeground(nonSelectedColor);
                    } else {
                        element.setForeground(selectedColor);
                    }
                } else {
                    element.setForeground(nonSelectedColor);
                }
            }
        } else {
            for (JLabel element : elements) {
                if (element.getText().equals(clickedText)) {
                    if (element.getForeground() == selectedColor) {
                        element.setForeground(nonSelectedColor);
                    } else {
                        element.setForeground(selectedColor);
                    }
                }
            }
        }
    }

    public final LinkedList<String> getSelectedElements() {
        LinkedList<String> ret = new LinkedList<>();

        for (JLabel element : elements) {
            if (element.getForeground() == selectedColor) {
                ret.add(element.getText());
            }
        }

        return ret;
    }

    public final String getSelectedElement() {
        LinkedList<String> ret = new LinkedList<>();

        for (JLabel element : elements) {
            if (element.getForeground() == selectedColor) {
                ret.add(element.getText());
            }
        }

        String retString = "null";

        if (!ret.isEmpty() && ret.get(0) != null)
            retString = ret.get(0);
        return retString;
    }

    public final int getWidth() {
        return width;
    }

    public final int getHeight() {
        return height;
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    public final SelectionPolicy getSelectionPolicy() {
        return selectionPolicy;
    }

    public final void setWidth(int width) {
        this.width = width;
    }

    public final void setHeight(int height) {
        this.height = height;
    }

    public final JTextPane getListPane() {
        return listPane;
    }

    public final void setSelectionPolicy(SelectionPolicy selectionPolicy) {
        this.selectionPolicy = selectionPolicy;
    }

    private final JLabel generateSepLabel() {
        CyderLabel sepLabel = new CyderLabel(";)") {
            @Override
            public void paintComponent(Graphics g) {
                g.setColor(darkMode ? CyderColors.defaultDarkModeTextColor : nonSelectedColor);
                g.fillRect(0, 10, getWidth(), 5);
                g.dispose();
            }
        };
        sepLabel.setForeground(nonSelectedColor);
        return sepLabel;
    }

    public final void clearSelectedElements() {
        for (JLabel element : elements) {
            element.setForeground(nonSelectedColor);
        }
    }

    @Override
    public String toString() {
        return ReflectionUtil.commonCyderToString(this);
    }
}
