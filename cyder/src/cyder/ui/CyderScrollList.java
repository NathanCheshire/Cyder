package cyder.ui;

import com.google.common.base.Preconditions;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.handlers.internal.Logger;
import cyder.user.UserUtil;
import cyder.utils.ReflectionUtil;

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

/**
 * A scroll list with clickable elements.
 * The elements may be separated by a bar or simply a new line.
 * Each element may have a single click and double click action.
 */
public class CyderScrollList {
    /**
     * The width of this scroll list.
     */
    private int width;

    /**
     * The height of this scroll list
     */
    private int height;

    /**
     * Whether dark mode is active for this scroll ist
     */
    private final boolean darkMode;

    /**
     * The color selected items are given.
     */
    public static final Color selectedColor = CyderColors.regularRed;

    /**
     * The color non-selected items are given.
     */
    private Color nonSelectedColor = CyderColors.navy;

    /**
     * The inner JTextPane object to hold our printed components.
     */
    private JTextPane listPane;

    /**
     * The alignment property to give to the list pane.
     */
    private int itemAlignment = StyleConstants.ALIGN_LEFT;

    /**
     * The list of elements of for this scroll list.
     */
    private final LinkedList<JLabel> elements;

    /**
     * The selection policies for the scroll list.
     */
    public enum SelectionPolicy {
        SINGLE, MULTIPLE
    }

    /**
     * The currently set selection policy for the scroll list.
     */
    private SelectionPolicy selectionPolicy;

    /**
     * Constructs a new scroll list object.
     */
    public CyderScrollList() {
        this(400, 400);
    }

    /**
     * Constructs a new scroll list object.
     *
     * @param width  the width of the component.
     * @param height the height of the component
     */
    public CyderScrollList(int width, int height) {
        this(width, height, SelectionPolicy.SINGLE);
    }

    /**
     * Constructs a new scroll list object.
     *
     * @param width           the width of the component
     * @param height          the height of the component
     * @param selectionPolicy the selection policy of the scroll list
     */
    public CyderScrollList(int width, int height, SelectionPolicy selectionPolicy) {
        this(width, height, selectionPolicy, false);
    }

    /**
     * Constructs a new scroll list object.
     *
     * @param width           the width of the component
     * @param height          the height of the component
     * @param selectionPolicy the selection policy of the component
     * @param darkMode        whether the component should be constructed in a dark mode format
     */
    public CyderScrollList(int width, int height, SelectionPolicy selectionPolicy, boolean darkMode) {
        this.width = width;
        this.height = height;
        this.selectionPolicy = selectionPolicy;
        this.darkMode = darkMode;
        elements = new LinkedList<>();

        if (darkMode) {
            nonSelectedColor = CyderColors.defaultDarkModeTextColor;
        }

        border = new LineBorder(darkMode ? CyderColors.defaultDarkModeTextColor
                : CyderColors.navy, 5, false);

        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    /**
     * Sets the item alignment of this scroll list.
     *
     * @param itemAlignment the item alignment to set
     */
    public final void setItemAlignment(int itemAlignment) {
        this.itemAlignment = itemAlignment;
    }

    /**
     * The font to use for the scroll list.
     */
    private Font scrollFont = CyderFonts.SEGOE_20;

    /**
     * Returns the font for this scroll list
     *
     * @return the font for this scroll list
     */
    public Font getScrollFont() {
        return scrollFont;
    }

    /**
     * Sets the font for this scroll list.
     *
     * @param f the font for this scroll list
     */
    public void setScrollFont(Font f) {
        scrollFont = f;
    }

    /**
     * The border to surround the component with.
     */
    private Border border;

    /**
     * Sets the border to surround the component with.
     *
     * @param border the border to surround the component with
     */
    public void setBorder(Border border) {
        this.border = border;
    }

    /**
     * The list of scroll lists created during the current instance of Cyder.
     */
    private static final ArrayList<CyderScrollList> scrollLists = new ArrayList<>();

    /**
     * Refreshes all CyderScrollLists that have been created during the current instance of Cyder.
     */
    public static void refreshAllLists() {
        for (CyderScrollList list : scrollLists) {
            Component parent = SwingUtilities.getRoot(list.getScrollPane());

            if (parent instanceof CyderFrame parentFrame) {
                if (!parentFrame.isDisposed()) {
                    list.refreshList();
                }
            }
        }
    }

    /**
     * Refreshes this instance of scroll list.
     * Currently this entails revalidating based on compact mode.
     */
    public final void refreshList() {
        // compact mode refreshing
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
     * Generates the CyderScrollList component based on the constructed and set properties.
     *
     * @return the CyderScrollList component based on the constructed and set properties
     */
    public final JLabel generateScrollList() {
        JLabel retLabel = new JLabel("");
        retLabel.setSize(width, height);
        retLabel.setBackground(darkMode ? CyderColors.darkModeBackgroundColor : CyderColors.vanilla);
        retLabel.setOpaque(true);
        retLabel.setVisible(true);

        listPane = new JTextPane();
        listPane.setEditable(false);
        listPane.setAutoscrolls(false);
        listPane.setBounds(0, 0, width, height);
        listPane.setFocusable(true);
        listPane.setOpaque(false);
        listPane.setBackground(darkMode ? CyderColors.darkModeBackgroundColor : CyderColors.vanilla);

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
        scrollPane.setBackground(darkMode ? CyderColors.darkModeBackgroundColor : CyderColors.vanilla);
        scrollPane.setBorder(border);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBounds(0, 0, width, height);
        retLabel.add(scrollPane);

        // set location of scroll to top
        listPane.setCaretPosition(0);

        return retLabel;
    }

    /**
     * The inner scroll pane object.
     */
    private CyderScrollPane scrollPane;

    /**
     * Returns the inner scroll pane object.
     *
     * @return the inner scroll pane object
     */
    public CyderScrollPane getScrollPane() {
        return scrollPane;
    }

    /**
     * Returns whether an element with the provided text already exists.
     *
     * @param text the element to search for
     * @return whether the element exists in the the scroll list
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean elementInList(String text) {
        for (JLabel element : elements) {
            if (element.getText().equals(text))
                return true;
        }

        return false;
    }

    /**
     * Adds a new element to the scroll list.
     *
     * @param labelText the text for the element to have
     * @param action    the action to invoke when the element is double clicked
     */
    public final void addElement(String labelText, Runnable action) {
        Preconditions.checkNotNull(labelText);
        Preconditions.checkArgument(!labelText.isEmpty());
        Preconditions.checkArgument(!elementInList(labelText),
                "Element already exists in scroll list: " + labelText);

        JLabel add = new JLabel(labelText);
        add.setForeground(nonSelectedColor);
        add.setFont(scrollFont);
        add.setVerticalAlignment(SwingConstants.CENTER);
        add.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2 && action != null) {
                    action.run();
                    add.setForeground(nonSelectedColor);
                } else {
                    handleElementClick(add.getText());
                }
            }
        });

        elements.add(add);
    }

    /**
     * Adds a new element to the scroll list.
     *
     * @param labelText the text for the element to have
     * @param action    the action to invoke when the element is clicked once
     */
    public final void addElementWithSingleCLickAction(String labelText, Runnable action) {
        Preconditions.checkNotNull(labelText);
        Preconditions.checkArgument(!labelText.isEmpty());
        Preconditions.checkArgument(!elementInList(labelText),
                "Element already exists in scroll list: " + labelText);

        JLabel add = new JLabel(labelText);
        add.setForeground(nonSelectedColor);
        add.setFont(scrollFont);
        add.setVerticalAlignment(SwingConstants.CENTER);
        add.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (action != null) {
                    action.run();
                    handleElementClick(add.getText());
                }
            }
        });

        elements.add(add);
    }

    /**
     * Removes all elements from the scroll list.
     */
    public final void removeAllElements() {
        elements.clear();
    }

    /**
     * Removes the specified element from the scroll list.
     *
     * @param labelText the element to remove from the scroll list
     */
    public final void removeElement(String labelText) {
        for (JLabel element : elements) {
            if (element.getText().equals(labelText)) {
                elements.remove(element);
                break;
            }
        }
    }

    /**
     * Invokes the action linked to the element with the provided text
     *
     * @param clickedText the text to find the corresponding action of
     */
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

    /**
     * Returns a list of all the currently selected elements.
     *
     * @return a list of all currently selected elements
     */
    public final LinkedList<String> getSelectedElements() {
        LinkedList<String> ret = new LinkedList<>();

        for (JLabel element : elements) {
            if (element.getForeground() == selectedColor) {
                ret.add(element.getText());
            }
        }

        return ret;
    }

    /**
     * Returns the currently selected element.
     *
     * @return the currently selected element
     */
    public final String getSelectedElement() {
        LinkedList<String> ret = new LinkedList<>();

        for (JLabel element : elements) {
            if (element.getForeground() == selectedColor) {
                ret.add(element.getText());
            }
        }

        String retString = "null";

        if (!ret.isEmpty() && ret.get(0) != null) {
            retString = ret.get(0);
        }

        return retString;
    }

    /**
     * Returns the width of this scroll list.
     *
     * @return the width of this scroll list
     */
    public final int getWidth() {
        return width;
    }

    /**
     * Returns the height of this component.
     *
     * @return the height of this component
     */
    public final int getHeight() {
        return height;
    }

    /**
     * Returns whether dark mode is active for this scroll list.
     *
     * @return whether dark mode is active for this scroll list
     */
    public boolean isDarkMode() {
        return darkMode;
    }

    /**
     * Returns the selection policy for this scroll list.
     *
     * @return the selection policy for this scroll list
     */
    public final SelectionPolicy getSelectionPolicy() {
        return selectionPolicy;
    }

    /**
     * Sets the width of this scroll list.
     *
     * @param width the width of this scroll list
     */
    public final void setWidth(int width) {
        this.width = width;
    }

    /**
     * Sets the height of this scroll list.
     *
     * @param height the height of this scroll list
     */
    public final void setHeight(int height) {
        this.height = height;
    }

    /**
     * Returns the inner text pane object.
     *
     * @return the inner text pane object
     */
    public final JTextPane getListPane() {
        return listPane;
    }

    /**
     * Sets the selection policy for the scroll list.
     *
     * @param selectionPolicy the selection policy to use for the scroll list
     */
    public final void setSelectionPolicy(SelectionPolicy selectionPolicy) {
        this.selectionPolicy = selectionPolicy;
    }

    /**
     * Generates a separation label to use for the scroll list when compact mode is not active.
     *
     * @return a separation label to use for the scroll list
     */
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

    /**
     * Deselects all selected elements from the scroll list.
     */
    public final void clearSelectedElements() {
        for (JLabel element : elements) {
            element.setForeground(nonSelectedColor);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return ReflectionUtil.commonCyderToString(this);
    }
}
