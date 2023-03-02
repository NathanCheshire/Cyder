package cyder.ui.list;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.strings.CyderStrings;
import cyder.strings.ToStringUtil;
import cyder.ui.UiUtil;
import cyder.ui.frame.CyderFrame;
import cyder.ui.label.CyderLabel;
import cyder.ui.pane.CyderOutputPane;
import cyder.ui.pane.CyderScrollPane;
import cyder.user.UserDataManager;
import org.jsoup.internal.StringUtil;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Optional;

/**
 * A scroll list with clickable elements.
 * The elements may be separated by a bar or simply a new line.
 * Each element may have a single click and double click action.
 */
public class CyderScrollList {
    /**
     * The color selected items are given.
     */
    public static final Color selectedColor = CyderColors.regularRed;

    /**
     * The default length of the scroll list.
     */
    public static final int DEFAULT_LEN = 400;

    /**
     * The string for sep labels. This text is irrelevant as it is not rendered but may not be an empty string.
     */
    private static final String SEP_LABEL_TEXT = ";)";

    /**
     * The y value of separation labels.
     */
    private static final int SEP_LABEL_Y = 10;

    /**
     * The height of separation labels.
     */
    private static final int SEP_LABEL_HEIGHT = 5;

    /**
     * The width of this scroll list.
     */
    private int width;

    /**
     * The height of this scroll list.
     */
    private int height;

    /**
     * Whether dark mode is active for this scroll list.
     */
    private final boolean darkMode;

    /**
     * The color non-selected items are given.
     */
    private Color nonSelectedColor = CyderColors.navy;

    /**
     * The inner JTextPane object to hold our printed components.
     */
    private JTextPane listPane;

    /**
     * The list of elements of for this scroll list.
     */
    private final ArrayList<JLabel> elements;

    /**
     * The selection policies for the scroll list.
     */
    public enum SelectionPolicy {
        SINGLE,
        MULTIPLE
    }

    /**
     * The currently set selection policy for the scroll list.
     */
    private SelectionPolicy selectionPolicy;

    /**
     * The font to use for the scroll list.
     */
    private Font scrollFont = CyderFonts.SEGOE_20;

    /**
     * The list of scroll lists created during the current instance of Cyder.
     */
    private static final ArrayList<CyderScrollList> scrollLists = new ArrayList<>();

    /**
     * The inner scroll pane object.
     */
    private CyderScrollPane scrollPane;

    /**
     * Constructs a new scroll list object.
     */
    public CyderScrollList() {
        this(DEFAULT_LEN, DEFAULT_LEN);
    }

    /**
     * Constructs a new scroll list object.
     *
     * @param width  the width of the component
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
        this.selectionPolicy = Preconditions.checkNotNull(selectionPolicy);
        this.darkMode = darkMode;

        elements = new ArrayList<>();

        if (darkMode) {
            nonSelectedColor = CyderColors.defaultDarkModeTextColor;
        }

        border = new LineBorder(darkMode
                ? CyderColors.defaultDarkModeTextColor
                : CyderColors.navy, 5, false);

        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Returns the font for this scroll list.
     *
     * @return the font for this scroll list
     */
    public Font getScrollFont() {
        return scrollFont;
    }

    /**
     * Sets the font for this scroll list.
     *
     * @param font the font for this scroll list
     */
    public void setScrollFont(Font font) {
        scrollFont = Preconditions.checkNotNull(font);
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
    public void setBorder(
            @Nullable
                    Border border) {
        this.border = border;
    }

    /**
     * Refreshes all CyderScrollLists that have been created during the current instance of Cyder.
     */
    public static void refreshAllLists() {
        scrollLists.forEach(list -> {
            Component parent = SwingUtilities.getRoot(list.getScrollPane());

            if (parent instanceof CyderFrame parentFrame) {
                if (!parentFrame.isDisposed()) {
                    list.refreshList();
                }
            }
        });
    }

    /**
     * Refreshes this instance of scroll list.
     * Actions performed:
     * <ul>
     *     <li>Revalidating based on compact mode</li>
     * </ul>
     */
    public void refreshList() {
        boolean compactMode = UserDataManager.INSTANCE.compactTextMode();

        CyderOutputPane outputPane = new CyderOutputPane(listPane);
        outputPane.getJTextPane().setText("");

        for (int i = 0 ; i < elements.size() ; i++) {
            outputPane.getStringUtil().printlnComponent(elements.get(i));

            if (i != elements.size() - 1 && !compactMode) {
                outputPane.getStringUtil().printlnComponent(generateSepLabel());
            }
        }

    }

    /**
     * Generates the CyderScrollList component based on the constructed and set properties.
     *
     * @return the CyderScrollList component based on the constructed and set properties
     */
    public JLabel generateScrollList() {
        JLabel retLabel = new JLabel();
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

        UiUtil.setJTextPaneDocumentAlignment(listPane, UiUtil.JTextPaneAlignment.LEFT);

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

        listPane.setCaretPosition(0);

        return retLabel;
    }

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
            if (element.getText().equals(text)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Adds a new element to the scroll list.
     *
     * @param labelText the text for the element to have
     */
    public void addElement(String labelText) {
        Preconditions.checkNotNull(labelText);
        Preconditions.checkArgument(!labelText.isEmpty());

        JLabel addElement = new JLabel(labelText);
        addElement.setForeground(nonSelectedColor);
        addElement.setFont(scrollFont);
        addElement.setVerticalAlignment(SwingConstants.CENTER);
        addElement.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onElementClicked(addElement.getText(), e);
            }
        });

        elements.add(addElement);
    }

    /**
     * Adds a new element to the scroll list.
     *
     * @param labelText         the text for the element to have
     * @param singleClickAction the action to invoke when the element is clicked once
     */
    public void addElementWithSingleClickAction(String labelText, Runnable singleClickAction) {
        Preconditions.checkNotNull(labelText);
        Preconditions.checkArgument(!labelText.isEmpty());
        Preconditions.checkNotNull(singleClickAction);
        Preconditions.checkArgument(!elementInList(labelText));

        JLabel addElement = new JLabel(labelText);
        addElement.setForeground(nonSelectedColor);
        addElement.setFont(scrollFont);
        addElement.setVerticalAlignment(SwingConstants.CENTER);
        addElement.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                singleClickAction.run();
                onElementClicked(addElement.getText(), e);
            }
        });

        elements.add(addElement);
    }

    /**
     * Adds a new element to the scroll list.
     *
     * @param labelText         the text for the element to have
     * @param doubleClickAction the action to invoke when the element is double clicked
     */
    public void addElementWithDoubleClickAction(String labelText, Runnable doubleClickAction) {
        Preconditions.checkNotNull(labelText);
        Preconditions.checkArgument(!labelText.isEmpty());
        Preconditions.checkNotNull(doubleClickAction);
        Preconditions.checkArgument(!elementInList(labelText),
                labelText + " already in: " + StringUtil.join(elements, CyderStrings.comma));

        JLabel addElement = new JLabel(labelText);
        addElement.setForeground(nonSelectedColor);
        addElement.setFont(scrollFont);
        addElement.setVerticalAlignment(SwingConstants.CENTER);
        addElement.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    doubleClickAction.run();
                    addElement.setForeground(nonSelectedColor);
                } else {
                    onElementClicked(addElement.getText(), e);
                }
            }
        });

        elements.add(addElement);
    }

    /**
     * Adds a new element to the scroll list.
     *
     * @param labelText         the text for the element to have
     * @param singleClickAction the action to invoke when the element is pressed
     * @param doubleClickAction the action to invoke when the element is double clicked
     */
    public void addElementWithSingleAndDoubleClickAction(String labelText,
                                                         Runnable singleClickAction,
                                                         Runnable doubleClickAction) {
        Preconditions.checkNotNull(labelText);
        Preconditions.checkArgument(!labelText.isEmpty());
        Preconditions.checkNotNull(singleClickAction);
        Preconditions.checkNotNull(doubleClickAction);
        Preconditions.checkArgument(!elementInList(labelText));

        JLabel addElement = new JLabel(labelText);
        addElement.setForeground(nonSelectedColor);
        addElement.setFont(scrollFont);
        addElement.setVerticalAlignment(SwingConstants.CENTER);
        addElement.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1) {
                    doubleClickAction.run();
                    addElement.setForeground(nonSelectedColor);
                } else {
                    singleClickAction.run();
                    onElementClicked(addElement.getText(), e);
                }
            }
        });

        elements.add(addElement);
    }

    /**
     * Selects the element with the provided text.
     *
     * @param elementText the element to select
     */
    public void selectElement(String elementText) {
        Preconditions.checkNotNull(elementText);
        Preconditions.checkArgument(!elementText.isEmpty());

        for (JLabel element : elements) {
            if (element.getText().equals(elementText)) {
                element.setForeground(selectedColor);
            }
        }
    }

    /**
     * Removes all elements from the scroll list.
     */
    public void removeAllElements() {
        elements.clear();
    }

    /**
     * Removes the specified element from the scroll list.
     *
     * @param labelText the element to remove from the scroll list
     */
    public void removeElement(String labelText) {
        Preconditions.checkNotNull(labelText);
        Preconditions.checkArgument(!labelText.isEmpty());

        for (JLabel element : elements) {
            if (element.getText().equals(labelText)) {
                elements.remove(element);
                break;
            }
        }
    }

    /**
     * The action to invoke on a single-click of the element with the provided text.
     *
     * @param clickedText the text of the singly-clicked element
     */
    private void onElementClicked(String clickedText, MouseEvent mouseEvent) {
        Preconditions.checkNotNull(clickedText);
        Preconditions.checkNotNull(mouseEvent);
        Preconditions.checkArgument(!clickedText.isEmpty());

        JLabel element = elements.stream().filter(e -> e.getText().equals(clickedText)).findFirst().orElseThrow();
        switch (selectionPolicy) {
            case SINGLE -> {
                boolean wasSelected = element.getForeground().equals(selectedColor);
                deselectAllElements();
                element.setForeground(wasSelected ? nonSelectedColor : selectedColor);
            }
            case MULTIPLE -> {
                boolean wasSelected = element.getForeground().equals(selectedColor);
                if (!mouseEvent.isControlDown()) {
                    deselectAllElements();
                    element.setForeground(selectedColor);
                } else {
                    element.setForeground(wasSelected ? nonSelectedColor : selectedColor);
                }
            }
        }

        element.repaint();
    }

    /**
     * Returns a list of all the currently selected elements.
     *
     * @return a list of all currently selected elements
     */
    public ImmutableList<String> getSelectedElements() {
        ArrayList<String> ret = new ArrayList<>();

        elements.forEach(element -> {
            if (element.getForeground().equals(selectedColor)) {
                ret.add(element.getText());
            }
        });

        return ImmutableList.copyOf(ret);
    }

    /**
     * Returns the currently selected element.
     *
     * @return the currently selected element
     */
    public Optional<String> getSelectedElement() {
        for (JLabel element : elements) {
            if (element.getForeground().equals(selectedColor)) {
                return Optional.of(element.getText());
            }
        }

        return Optional.empty();
    }

    /**
     * Removes all selected elements on this scroll list.
     */
    public void removeSelectedElements() {
        elements.removeIf(element -> element.getForeground().equals(selectedColor));
    }

    /**
     * Removes the first element from this scroll list.
     */
    public void removeSelectedElement() {
        Optional<JLabel> remove = elements.stream().filter(element -> element.getForeground()
                .equals(selectedColor)).findFirst();
        remove.ifPresent(elements::remove);
    }

    /**
     * Returns the number of currently selected elements.
     *
     * @return the number of currently selected elements
     */
    public int getSelectedElementCount() {
        return (int) elements.stream().filter(element -> element.getForeground().equals(selectedColor)).count();
    }

    /**
     * Returns the width of this scroll list.
     *
     * @return the width of this scroll list
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the height of this component.
     *
     * @return the height of this component
     */
    public int getHeight() {
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
    public SelectionPolicy getSelectionPolicy() {
        return selectionPolicy;
    }

    /**
     * Sets the width of this scroll list.
     *
     * @param width the width of this scroll list
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * Sets the height of this scroll list.
     *
     * @param height the height of this scroll list
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * Returns the inner text pane object.
     *
     * @return the inner text pane object
     */
    public JTextPane getListPane() {
        return listPane;
    }

    /**
     * Sets the selection policy for the scroll list.
     *
     * @param selectionPolicy the selection policy to use for the scroll list
     */
    public void setSelectionPolicy(SelectionPolicy selectionPolicy) {
        this.selectionPolicy = Preconditions.checkNotNull(selectionPolicy);
    }

    /**
     * Generates a separation label to use for the scroll list when compact mode is not active.
     *
     * @return a separation label to use for the scroll list
     */
    private JLabel generateSepLabel() {
        CyderLabel sepLabel = new CyderLabel(SEP_LABEL_TEXT) {
            @Override
            public void paintComponent(Graphics g) {
                g.setColor(darkMode ? CyderColors.defaultDarkModeTextColor : nonSelectedColor);
                g.fillRect(0, SEP_LABEL_Y, getWidth(), SEP_LABEL_HEIGHT);
                g.dispose();
            }
        };
        sepLabel.setForeground(nonSelectedColor);
        return sepLabel;
    }

    /**
     * Deselects all selected elements from the scroll list.
     */
    public void deselectAllElements() {
        elements.forEach(element -> element.setForeground(nonSelectedColor));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return ToStringUtil.commonCyderToString(this);
    }
}
