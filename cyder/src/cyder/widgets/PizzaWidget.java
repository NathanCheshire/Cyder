package cyder.widgets;

import com.google.common.collect.ImmutableList;
import cyder.annotations.CyderAuthor;
import cyder.annotations.ForReadability;
import cyder.annotations.Vanilla;
import cyder.annotations.Widget;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderIcons;
import cyder.constants.CyderStrings;
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
import java.util.LinkedList;
import java.util.Optional;

@Vanilla
@CyderAuthor
public final class PizzaWidget {
    private static CyderFrame pizzaFrame;

    private static CyderTextField nameField;

    private static CyderCheckboxGroup sizeGroup;

    private static CyderCheckbox smallCheckbox;
    private static CyderCheckbox mediumCheckbox;
    private static CyderCheckbox largeCheckbox;

    private static CyderScrollList pizzaToppingsScroll;
    private static CyderScrollList crustTypeScroll;

    private static JTextArea orderComments;

    private static CyderCheckbox breadSticks;
    private static CyderCheckbox salad;
    private static CyderCheckbox soda;

    /**
     * Suppress default constructor.
     */
    private PizzaWidget() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    private static final String FRAME_TITLE = "Pizza";
    private static final int FRAME_WIDTH = 600;
    private static final int FRAME_HEIGHT = 800;
    private static final String NAME = "Name:";
    private static final String SIZE = "Size:";
    private static final String SMALL = "Small";
    private static final String MEDIUM = "Medium";
    private static final String LARGE = "Large";
    private static final String CRUST_TYPE = "Crust Type";
    private static final String TOPPINGS = "Toppings";
    private static final String RESET = "Reset";
    private static final String BREAK_TAG = "<br/>";
    private static final String PLACE_ORDER = "Place Order";
    private static final String EXTRAS = "Extras:";
    private static final String BREAD_STICKS = "Bread Sticks";
    private static final String SALAD = "Salad";
    private static final String SODA = "Soda";
    private static final String ORDER_COMMENTS = "Order Comments";

    private static final ImmutableList<String> crustTypes = ImmutableList.of(
            "Thin",
            "Thick",
            "Deep dish",
            "Classic",
            "Tavern",
            "Seasonal");

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
    private static final int pizzaToppingsScrollLength = 200;

    private static final int crustScrollWidth = 160;
    private static final int crustScrollHeight = 200;

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

        nameField = new CyderTextField(0);
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
        mediumCheckbox.setChecked();
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
        crustTypes.forEach(crustType -> crustTypeScroll.addElement(crustType, null));

        JLabel crustTypeLabel = crustTypeScroll.generateScrollList();
        crustTypeLabel.setBounds(80, 250, 160, 200);
        pizzaFrame.getContentPane().add(crustTypeLabel);

        pizzaToppingsScroll = new CyderScrollList(pizzaToppingsScrollLength, pizzaToppingsScrollLength,
                CyderScrollList.SelectionPolicy.MULTIPLE);
        pizzaToppings.forEach(topping -> pizzaToppingsScroll.addElement(topping, null));

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
        String size = optionalSize.get() + BREAK_TAG;

        String crust;
        if (crustTypeScroll.getSelectedElementCount() == 0) {
            crust = "Thin";
        } else {
            crust = crustTypeScroll.getSelectedElement();
        }

        // todo method
        StringBuilder toppingsChosen = new StringBuilder();
        LinkedList<String> selectedToppings = pizzaToppingsScroll.getSelectedElements();
        if (selectedToppings.isEmpty()) {
            toppingsChosen.append("Plain");
        } else {
            selectedToppings.forEach(topping -> toppingsChosen.append(topping).append(BREAK_TAG));
        }


        // todo get extras method
        String extras = "";
        if (breadSticks.isChecked()) {
            extras += "Bread Sticks" + BREAK_TAG;
        }
        if (salad.isChecked()) {
            extras += "Salad" + BREAK_TAG;
        }
        if (soda.isChecked()) {
            extras += "Soda" + BREAK_TAG;
        }

        if (extras.isEmpty()) {
            extras = "";
        } else {
            extras = BREAK_TAG + "Extras: " + BREAK_TAG + extras;
        }


        String comments = StringUtil.getTrimmedText(orderComments.getText());
        if (comments.isEmpty()) {
            comments = "No comments";
        }

        String informTitle = "Order summary";
        pizzaFrame.inform("Name: " + BREAK_TAG + name + BREAK_TAG
                + "Size: " + BREAK_TAG + size + BREAK_TAG
                + "Crust: " + BREAK_TAG + crust + BREAK_TAG + BREAK_TAG
                + "Toppings: " + BREAK_TAG + toppingsChosen + BREAK_TAG
                + "Extras: " + BREAK_TAG + extras + BREAK_TAG
                + "Comments: " + BREAK_TAG + comments + BREAK_TAG, informTitle);
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

        crustTypeScroll.clearSelectedElements();
        pizzaToppingsScroll.clearSelectedElements();

        breadSticks.setNotChecked();
        salad.setNotChecked();
        soda.setNotChecked();

        orderComments.setText("");
    }
}
