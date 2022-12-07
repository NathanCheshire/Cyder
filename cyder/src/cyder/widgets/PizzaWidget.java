package cyder.widgets;

import com.google.common.collect.ImmutableList;
import cyder.annotations.CyderAuthor;
import cyder.annotations.ForReadability;
import cyder.annotations.Vanilla;
import cyder.annotations.Widget;
import cyder.constants.*;
import cyder.exceptions.IllegalMethodException;
import cyder.ui.button.CyderButton;
import cyder.ui.field.CyderTextField;
import cyder.ui.frame.CyderFrame;
import cyder.ui.pane.CyderScrollList;
import cyder.ui.pane.CyderScrollPane;
import cyder.ui.selection.CyderCheckbox;
import cyder.ui.selection.CyderCheckboxGroup;
import cyder.utils.StringUtil;
import cyder.utils.UiUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Optional;

/**
 * A widget for ordering pizza.
 */
@Vanilla
@CyderAuthor
public final class PizzaWidget {
    /**
     * The widget frame.
     */
    private static CyderFrame pizzaFrame;

    /**
     * The customer name field.
     */
    private static CyderTextField nameField;

    /**
     * The checkbox group for the pizza size checkboxes.
     */
    private static CyderCheckboxGroup sizeGroup;

    /**
     * The small pizza checkbox.
     */
    private static CyderCheckbox smallCheckbox;

    /**
     * The medium pizza checkbox.
     */
    private static CyderCheckbox mediumCheckbox;

    /**
     * The large pizza checkbox.
     */
    private static CyderCheckbox largeCheckbox;

    /**
     * The pizza toppings scroll list.
     */
    private static CyderScrollList pizzaToppingsScroll;

    /**
     * The crust type scroll list.
     */
    private static CyderScrollList crustTypeScroll;

    /**
     * The comments area.
     */
    private static JTextArea orderComments;

    /**
     * The bread sticks checkbox.
     */
    private static CyderCheckbox breadSticks;

    /**
     * The salad checkbox.
     */
    private static CyderCheckbox salad;

    /**
     * The soda checkbox.
     */
    private static CyderCheckbox soda;

    /**
     * The title of the widget frame.
     */
    private static final String FRAME_TITLE = "Pizza";

    /**
     * The width of the widget frame.
     */
    private static final int FRAME_WIDTH = 600;

    /**
     * The height of the widget frame.
     */
    private static final int FRAME_HEIGHT = 800;

    /**
     * The text of the name label.
     */
    private static final String NAME = "Name:";

    /**
     * The text of the size label.
     */
    private static final String SIZE = "Size:";

    /**
     * The small text.
     */
    private static final String SMALL = "Small";

    /**
     * The medium text.
     */
    private static final String MEDIUM = "Medium";

    /**
     * The large text.
     */
    private static final String LARGE = "Large";

    /**
     * The crust type label text.
     */
    private static final String CRUST_TYPE = "Crust Type";

    /**
     * The toppings label text.
     */
    private static final String TOPPINGS = "Toppings";

    /**
     * The rest button text.
     */
    private static final String RESET = "Reset";

    /**
     * The place order button text.
     */
    private static final String PLACE_ORDER = "Place Order";

    /**
     * The extras label text.
     */
    private static final String EXTRAS = "Extras:";

    /**
     * The bread sticks string.
     */
    private static final String BREAD_STICKS = "Bread Sticks";

    /**
     * The salad string.
     */
    private static final String SALAD = "Salad";

    /**
     * The soda string.
     */
    private static final String SODA = "Soda";

    /**
     * The order comments string.
     */
    private static final String ORDER_COMMENTS = "Order Comments";

    /**
     * The value for an empty topping list.
     */
    private static final String PLAIN = "Plain";

    /**
     * The default crust type.
     */
    private static final String THIN = "Thin";

    /**
     * The text for if no order comments are specified.
     */
    private static final String NO_COMMENTS = "No comments";

    /**
     * The title of the order confirmation inform pane.
     */
    private static final String informTitle = "Order";

    /**
     * The text for if no extras are specified.
     */
    private static final String NO_EXTRAS = "No extras";

    /**
     * The possible values for pizza crusts.
     */
    private static final ImmutableList<String> crustTypes = ImmutableList.of(
            "Thin",
            "Thick",
            "Deep dish",
            "Classic",
            "Tavern",
            "Seasonal");

    /**
     * The possible values for pizza toppings.
     */
    private static final ImmutableList<String> pizzaToppings = ImmutableList.of(
            "Pepperoni",
            "Sausage",
            "Green peppers",
            "Onions",
            "Tomatoes",
            "Anchovies",
            "Bacon",
            "Chicken",
            "Beef",
            "Olives",
            "Mushrooms");

    /**
     * The length of the pizza topping scroll (width and height).
     */
    private static final int pizzaToppingsScrollLength = 200;

    /**
     * The width of the crust type scroll.
     */
    private static final int crustScrollWidth = 160;

    /**
     * The height of the crust type scroll.
     */
    private static final int crustScrollHeight = 200;

    /**
     * Suppress default constructor.
     */
    private PizzaWidget() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    @Widget(triggers = "pizza", description = "A fake pizza ordering widget")
    public static void showGui() {
        UiUtil.closeIfOpen(pizzaFrame);

        pizzaFrame = new CyderFrame(FRAME_WIDTH, FRAME_HEIGHT, CyderIcons.defaultBackground);
        pizzaFrame.setTitle(FRAME_TITLE);

        JLabel nameLabel = new JLabel(NAME);
        nameLabel.setFont(CyderFonts.SEGOE_20);
        nameLabel.setForeground(CyderColors.navy);
        nameLabel.setBounds(40, 45, 100, 30);
        pizzaFrame.getContentPane().add(nameLabel);

        nameField = new CyderTextField();
        nameField.setHorizontalAlignment(JTextField.CENTER);
        nameField.setAutoCapitalization(true);
        nameField.setBackground(Color.white);
        nameField.setBounds(140, 40, 400, 40);
        pizzaFrame.getContentPane().add(nameField);

        JLabel sizeLabel = new JLabel(SIZE);
        sizeLabel.setFont(CyderFonts.SEGOE_20);
        sizeLabel.setForeground(CyderColors.navy);
        sizeLabel.setBounds(40, 140, 50, 30);
        pizzaFrame.getContentPane().add(sizeLabel);

        JLabel smallLabel = new JLabel(SMALL);
        smallLabel.setFont(CyderFonts.SEGOE_20);
        smallLabel.setForeground(CyderColors.navy);
        smallLabel.setBounds(180, 100, 100, 30);
        pizzaFrame.getContentPane().add(smallLabel);

        JLabel mediumLabel = new JLabel(MEDIUM);
        mediumLabel.setFont(CyderFonts.SEGOE_20);
        mediumLabel.setForeground(CyderColors.navy);
        mediumLabel.setBounds(285, 100, 100, 30);
        pizzaFrame.getContentPane().add(mediumLabel);

        JLabel largeLabel = new JLabel(LARGE);
        largeLabel.setFont(CyderFonts.SEGOE_20);
        largeLabel.setForeground(CyderColors.navy);
        largeLabel.setBounds(420, 100, 100, 30);
        pizzaFrame.getContentPane().add(largeLabel);

        sizeGroup = new CyderCheckboxGroup();

        smallCheckbox = new CyderCheckbox();
        smallCheckbox.setHorizontalAlignment(SwingConstants.CENTER);
        smallCheckbox.setNotChecked();
        smallCheckbox.setBounds(185, 135, 50, 50);
        pizzaFrame.getContentPane().add(smallCheckbox);
        sizeGroup.addCheckbox(smallCheckbox);

        mediumCheckbox = new CyderCheckbox();
        mediumCheckbox.setHorizontalAlignment(SwingConstants.CENTER);
        mediumCheckbox.setBounds(305, 135, 50, 50);
        pizzaFrame.getContentPane().add(mediumCheckbox);
        sizeGroup.addCheckbox(mediumCheckbox);

        largeCheckbox = new CyderCheckbox();
        largeCheckbox.setHorizontalAlignment(SwingConstants.CENTER);
        largeCheckbox.setNotChecked();
        largeCheckbox.setBounds(425, 135, 50, 50);
        pizzaFrame.getContentPane().add(largeCheckbox);
        sizeGroup.addCheckbox(largeCheckbox);

        JLabel crustLabel = new JLabel(CRUST_TYPE);
        crustLabel.setFont(CyderFonts.SEGOE_20);
        crustLabel.setForeground(CyderColors.navy);
        crustLabel.setBounds(90, 210, 130, 30);
        pizzaFrame.getContentPane().add(crustLabel);

        JLabel Toppings = new JLabel(TOPPINGS);
        Toppings.setFont(CyderFonts.SEGOE_20);
        Toppings.setForeground(CyderColors.navy);
        Toppings.setBounds(370, 210, 130, 30);
        pizzaFrame.getContentPane().add(Toppings);

        crustTypeScroll = new CyderScrollList(crustScrollWidth, crustScrollHeight,
                CyderScrollList.SelectionPolicy.SINGLE);
        crustTypes.forEach(crustType -> crustTypeScroll.addElement(crustType));

        JLabel crustTypeLabel = crustTypeScroll.generateScrollList();
        crustTypeLabel.setBounds(80, 250, 160, 200);
        pizzaFrame.getContentPane().add(crustTypeLabel);

        pizzaToppingsScroll = new CyderScrollList(pizzaToppingsScrollLength, pizzaToppingsScrollLength,
                CyderScrollList.SelectionPolicy.MULTIPLE);
        pizzaToppings.forEach(topping -> pizzaToppingsScroll.addElement(topping));

        JLabel pizzaToppingsLabel = pizzaToppingsScroll.generateScrollList();
        pizzaToppingsLabel.setBounds(320, 250, 200, 200);
        pizzaFrame.getContentPane().add(pizzaToppingsLabel);

        JLabel Extra = new JLabel(EXTRAS);
        Extra.setForeground(CyderColors.navy);
        Extra.setFont(CyderFonts.SEGOE_20);
        Extra.setBounds(40, 510, 130, 30);
        pizzaFrame.getContentPane().add(Extra);

        JLabel breadSticksLabel = new JLabel(BREAD_STICKS);
        breadSticksLabel.setFont(CyderFonts.SEGOE_20);
        breadSticksLabel.setForeground(CyderColors.navy);
        breadSticksLabel.setBounds(130, 470, 150, 30);
        pizzaFrame.getContentPane().add(breadSticksLabel);

        breadSticks = new CyderCheckbox();
        breadSticks.setHorizontalAlignment(SwingConstants.CENTER);
        breadSticks.setNotChecked();
        breadSticks.setBounds(165, 505, 50, 50);
        pizzaFrame.getContentPane().add(breadSticks);

        JLabel saladLabel = new JLabel(SALAD);
        saladLabel.setFont(CyderFonts.SEGOE_20);
        saladLabel.setForeground(CyderColors.navy);
        saladLabel.setBounds(310, 470, 150, 30);
        pizzaFrame.getContentPane().add(saladLabel);

        salad = new CyderCheckbox();
        salad.setHorizontalAlignment(SwingConstants.CENTER);
        salad.setNotChecked();
        salad.setBounds(315, 505, 50, 50);
        pizzaFrame.getContentPane().add(salad);

        JLabel sodaLabel = new JLabel(SODA);
        sodaLabel.setFont(CyderFonts.SEGOE_20);
        sodaLabel.setForeground(CyderColors.navy);
        sodaLabel.setBounds(445, 470, 150, 30);
        pizzaFrame.getContentPane().add(sodaLabel);

        soda = new CyderCheckbox();
        soda.setHorizontalAlignment(SwingConstants.CENTER);
        soda.setNotChecked();
        soda.setBounds(445, 505, 50, 50);
        pizzaFrame.getContentPane().add(soda);

        JLabel orderCommentsLabel = new JLabel(ORDER_COMMENTS);
        orderCommentsLabel.setFont(CyderFonts.SEGOE_20);
        orderCommentsLabel.setForeground(CyderColors.navy);
        orderCommentsLabel.setBounds(210, 565, 200, 30);
        pizzaFrame.getContentPane().add(orderCommentsLabel);

        orderComments = new JTextArea(5, 20);
        orderComments.setFont(CyderFonts.SEGOE_20);
        orderComments.setAutoscrolls(true);
        orderComments.setLineWrap(true);
        orderComments.setWrapStyleWord(true);
        orderComments.setSelectionColor(CyderColors.selectionColor);
        orderComments.setBorder(new LineBorder(new Color(0, 0, 0)));

        CyderScrollPane orderCommentsScroll = new CyderScrollPane(orderComments,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        orderCommentsScroll.setThumbColor(CyderColors.regularRed);
        orderCommentsScroll.setBorder(new LineBorder(CyderColors.navy, 5, false));
        orderCommentsScroll.setBounds(80, 600, 600 - 2 * 80, 120);
        pizzaFrame.getContentPane().add(orderCommentsScroll);

        CyderButton placeOrder = new CyderButton(PLACE_ORDER);
        placeOrder.setFont(CyderFonts.SEGOE_20);
        placeOrder.addActionListener(e -> placeOrderAction());
        placeOrder.setBounds(80, 740, 200, 40);
        pizzaFrame.getContentPane().add(placeOrder);

        CyderButton resetPizza = new CyderButton(RESET);
        resetPizza.setFont(CyderFonts.SEGOE_20);
        resetPizza.addActionListener(e -> reset());
        resetPizza.setBounds(180 + 100 + 40, 740, 200, 40);
        pizzaFrame.getContentPane().add(resetPizza);

        pizzaFrame.finalizeAndShow();
    }

    /**
     * The action to run when the place order button is clicked.
     */
    @ForReadability
    private static void placeOrderAction() {
        String name = nameField.getTrimmedText();
        if (name.isEmpty()) {
            pizzaFrame.notify("Please enter a valid name");
            return;
        }
        name = StringUtil.capsFirstWords(name);

        Optional<String> optionalSize = getSize();
        if (optionalSize.isEmpty()) {
            pizzaFrame.notify("Please specify a size");
            return;
        }
        String size = optionalSize.get() + HtmlTags.breakTag;

        String crust;
        LinkedList<String> selectedElements = crustTypeScroll.getSelectedElements();
        if (selectedElements.isEmpty()) {
            crust = THIN;
        } else {
            crust = selectedElements.get(0);
        }

        StringBuilder toppingsChosen = new StringBuilder();
        LinkedList<String> selectedToppings = pizzaToppingsScroll.getSelectedElements();
        if (selectedToppings.isEmpty()) {
            toppingsChosen.append(PLAIN);
        } else {
            selectedToppings.forEach(topping -> toppingsChosen.append(topping).append(HtmlTags.breakTag));
        }

        ImmutableList<String> extrasList = getExtras();
        String extras;
        if (extrasList.isEmpty()) {
            extras = NO_EXTRAS;
        } else {
            StringBuilder extraBuilder = new StringBuilder();
            extrasList.forEach(extra -> extraBuilder.append(extra).append(HtmlTags.breakTag));
            extras = extraBuilder.toString();
        }

        String comments = StringUtil.getTrimmedText(orderComments.getText());
        if (comments.isEmpty()) {
            comments = NO_COMMENTS;
        }

        pizzaFrame.inform("Name: " + HtmlTags.breakTag + name + HtmlTags.breakTag + HtmlTags.breakTag
                + "Size: " + HtmlTags.breakTag + size + HtmlTags.breakTag + HtmlTags.breakTag
                + "Crust: " + HtmlTags.breakTag + crust + HtmlTags.breakTag + HtmlTags.breakTag
                + "Toppings: " + HtmlTags.breakTag + toppingsChosen + HtmlTags.breakTag
                + "Extras: " + HtmlTags.breakTag + extras + HtmlTags.breakTag
                + "Comments: " + HtmlTags.breakTag + comments + HtmlTags.breakTag, informTitle);
    }

    /**
     * Returns a list of extras.
     *
     * @return a list of extras
     */
    private static ImmutableList<String> getExtras() {
        ArrayList<String> ret = new ArrayList<>();

        if (breadSticks.isChecked()) {
            ret.add(BREAD_STICKS);
        }
        if (salad.isChecked()) {
            ret.add(SALAD);
        }
        if (soda.isChecked()) {
            ret.add(SODA);
        }

        return ImmutableList.copyOf(ret);
    }

    /**
     * Returns the pizza size if present. Empty optional else.
     *
     * @return the pizza size if present. Empty optional else
     */
    @ForReadability
    private static Optional<String> getSize() {
        if (smallCheckbox.isChecked()) {
            return Optional.of(SMALL);
        } else if (mediumCheckbox.isChecked()) {
            return Optional.of(MEDIUM);
        } else if (largeCheckbox.isChecked()) {
            return Optional.of(LARGE);
        } else {
            return Optional.empty();
        }
    }

    /**
     * Resets the state of the pizza widget.
     */
    @ForReadability
    private static void reset() {
        nameField.setText("");

        sizeGroup.clearSelection();

        crustTypeScroll.deselectAllElements();
        crustTypeScroll.getScrollPane().getHorizontalScrollBar().setValue(0);
        pizzaToppingsScroll.deselectAllElements();
        pizzaToppingsScroll.getScrollPane().getHorizontalScrollBar().setValue(0);

        breadSticks.setNotChecked();
        salad.setNotChecked();
        soda.setNotChecked();

        orderComments.setText("");
    }
}
