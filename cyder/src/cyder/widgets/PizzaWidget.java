package cyder.widgets;

import cyder.annotations.CyderAuthor;
import cyder.annotations.Vanilla;
import cyder.annotations.Widget;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderIcons;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.ui.*;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedList;

@Vanilla
@CyderAuthor(author = "Nathan Cheshire")
public class PizzaWidget {
    private static CyderFrame pizzaFrame;
    private static CyderTextField nameField;
    private static CyderCheckbox smallPizza;
    private static CyderCheckbox mediumPizza;
    private static CyderCheckbox largePizza;

    private static CyderScrollList pizzaToppingsScroll;
    private static CyderScrollList crustTypeScroll;

    private static JTextArea orderComments;

    private static CyderCheckbox breadSticks;
    private static CyderCheckbox salad;
    private static CyderCheckbox soda;

    /**
     * Restrict default constructor.
     */
    private PizzaWidget() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    @Widget(triggers = "pizza", description = "A very old widget I built using Swing in 2017 for AP Comp. Sci. " +
            "that I rewrote using the Cyder toolkit")
    public static void showGUI() {
        

        if (pizzaFrame != null)
            pizzaFrame.dispose();

        pizzaFrame = new CyderFrame(600,800, CyderIcons.defaultBackground);
        pizzaFrame.setTitle("Pizza");

        JLabel CustomerName = new JLabel("Name:");
        CustomerName.setFont(CyderFonts.segoe20);
        CustomerName.setForeground(CyderColors.navy);
        CustomerName.setBounds(40,45,100,30);
        pizzaFrame.getContentPane().add(CustomerName);

        nameField = new CyderTextField(0);
        nameField.setHorizontalAlignment(JTextField.CENTER);
        nameField.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (nameField.getText().length() == 1) {
                    nameField.setText(nameField.getText().toUpperCase());
                }
            }

            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                if (nameField.getText().length() == 1) {
                    nameField.setText(nameField.getText().toUpperCase());
                }
            }

            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
                if (nameField.getText().length() == 1) {
                    nameField.setText(nameField.getText().toUpperCase());
                }
            }
        });
        nameField.setBackground(Color.white);
        nameField.setBounds(140,40,400,40);
        pizzaFrame.getContentPane().add(nameField);

        JLabel pizzaSizeLabel = new JLabel("Size:");
        pizzaSizeLabel.setFont(CyderFonts.segoe20);
        pizzaSizeLabel.setForeground(CyderColors.navy);
        pizzaSizeLabel.setBounds(40,140,50,30);
        pizzaFrame.getContentPane().add(pizzaSizeLabel);

        JLabel smallLabel = new JLabel("Small");
        smallLabel.setFont(CyderFonts.segoe20);
        smallLabel.setForeground(CyderColors.navy);
        smallLabel.setBounds(180,100,100,30);
        pizzaFrame.getContentPane().add(smallLabel);

        JLabel mediumLabel = new JLabel("Medium");
        mediumLabel.setFont(CyderFonts.segoe20);
        mediumLabel.setForeground(CyderColors.navy);
        mediumLabel.setBounds(285,100,100,30);
        pizzaFrame.getContentPane().add(mediumLabel);

        JLabel largeLabel = new JLabel("Large");
        largeLabel.setFont(CyderFonts.segoe20);
        largeLabel.setForeground(CyderColors.navy);
        largeLabel.setBounds(420,100,100,30);
        pizzaFrame.getContentPane().add(largeLabel);

        smallPizza = new CyderCheckbox();
        smallPizza.setHorizontalAlignment(SwingConstants.CENTER);
        smallPizza.setNotSelected();
        smallPizza.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                mediumPizza.setNotSelected();
                largePizza.setNotSelected();
            }
        });
        smallPizza.setBounds(185,135,50,50);
        pizzaFrame.getContentPane().add(smallPizza);

        mediumPizza = new CyderCheckbox();
        mediumPizza.setHorizontalAlignment(SwingConstants.CENTER);
        mediumPizza.setSelected();
        mediumPizza.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                smallPizza.setNotSelected();
                largePizza.setNotSelected();
            }
        });
        mediumPizza.setBounds(305,135,50,50);
        pizzaFrame.getContentPane().add(mediumPizza);

        largePizza = new CyderCheckbox();
        largePizza.setHorizontalAlignment(SwingConstants.CENTER);
        largePizza.setNotSelected();
        largePizza.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                smallPizza.setNotSelected();
                mediumPizza.setNotSelected();
            }
        });
        largePizza.setBounds(425,135,50,50);
        pizzaFrame.getContentPane().add(largePizza);

        JLabel crustLabel = new JLabel("Crust Type");
        crustLabel.setFont(CyderFonts.segoe20);
        crustLabel.setForeground(CyderColors.navy);
        crustLabel.setBounds(90,210,130,30);
        pizzaFrame.getContentPane().add(crustLabel);

        JLabel Toppings = new JLabel("Toppings");
        Toppings.setFont(CyderFonts.segoe20);
        Toppings.setForeground(CyderColors.navy);
        Toppings.setBounds(370,210,130,30);
        pizzaFrame.getContentPane().add(Toppings);

        String[] crustTypes = {"Thin", "Thick", "Deep Dish", "Classic", "Tavern", "Seasonal"};
        crustTypeScroll = new CyderScrollList(160, 200, CyderScrollList.SelectionPolicy.SINGLE);

        for (String crustType : crustTypes) {
            crustTypeScroll.addElement(crustType, null);
        }

        JLabel crustTypeLabel = crustTypeScroll.generateScrollList();
        crustTypeLabel.setBounds(80,250,160,200);
        pizzaFrame.getContentPane().add(crustTypeLabel);

        String[] pizzaToppings = {"Pepperoni", "Sausage", "Green Peppers",
                "Onions", "Tomatoes", "Anchovies", "Bacon", "Chicken", "Beef",
                "Olives", "Mushrooms"};
        pizzaToppingsScroll = new CyderScrollList(200, 200, CyderScrollList.SelectionPolicy.MULTIPLE);

        for (String pizzaTopping : pizzaToppings) {
            pizzaToppingsScroll.addElement(pizzaTopping, null);
        }

        JLabel pizzaToppingsLabel = pizzaToppingsScroll.generateScrollList();
        pizzaToppingsLabel.setBounds(320,250,200,200);
        pizzaFrame.getContentPane().add(pizzaToppingsLabel);

        JLabel Extra = new JLabel("Extras:");
        Extra.setForeground(CyderColors.navy);
        Extra.setFont(CyderFonts.segoe20);
        Extra.setBounds(40,510,130,30);
        pizzaFrame.getContentPane().add(Extra);

        JLabel breadSticksLabel = new JLabel("Bread Sticks");
        breadSticksLabel.setFont(CyderFonts.segoe20);
        breadSticksLabel.setForeground(CyderColors.navy);
        breadSticksLabel.setBounds(130,470,150,30);
        pizzaFrame.getContentPane().add(breadSticksLabel);

        breadSticks = new CyderCheckbox();
        breadSticks.setHorizontalAlignment(SwingConstants.CENTER);
        breadSticks.setNotSelected();
        breadSticks.setBounds(165,505,50,50);
        pizzaFrame.getContentPane().add(breadSticks);

        JLabel saladLabel = new JLabel("Salad");
        saladLabel.setFont(CyderFonts.segoe20);
        saladLabel.setForeground(CyderColors.navy);
        saladLabel.setBounds(310,470,150,30);
        pizzaFrame.getContentPane().add(saladLabel);

        salad = new CyderCheckbox();
        salad.setHorizontalAlignment(SwingConstants.CENTER);
        salad.setNotSelected();
        salad.setBounds(315,505,50,50);
        pizzaFrame.getContentPane().add(salad);

        JLabel sodaLabel = new JLabel("Soda");
        sodaLabel.setFont(CyderFonts.segoe20);
        sodaLabel.setForeground(CyderColors.navy);
        sodaLabel.setBounds(445,470,150,30);
        pizzaFrame.getContentPane().add(sodaLabel);

        soda = new CyderCheckbox();
        soda.setHorizontalAlignment(SwingConstants.CENTER);
        soda.setNotSelected();
        soda.setBounds(445,505,50,50);
        pizzaFrame.getContentPane().add(soda);

        JLabel orderCommentsLabel = new JLabel("Order Comments");
        orderCommentsLabel.setFont(CyderFonts.segoe20);
        orderCommentsLabel.setForeground(CyderColors.navy);
        orderCommentsLabel.setBounds(210,565,200,30);
        pizzaFrame.getContentPane().add(orderCommentsLabel);

        orderComments = new JTextArea(5,20);
        orderComments.setFont(CyderFonts.segoe20);
        orderComments.setAutoscrolls(true);
        orderComments.setLineWrap(true);
        orderComments.setWrapStyleWord(true);
        orderComments.setSelectionColor(CyderColors.selectionColor);
        orderComments.setBorder(new LineBorder(new Color(0, 0, 0)));

        CyderScrollPane orderCommentsScroll = new CyderScrollPane(orderComments,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        orderCommentsScroll.setThumbColor(CyderColors.regularRed);
        orderCommentsScroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        orderCommentsScroll.getViewport().setBorder(null);
        orderCommentsScroll.setViewportBorder(null);
        orderCommentsScroll.setBorder(new LineBorder(CyderColors.navy,5,false));
        orderCommentsScroll.setPreferredSize(new Dimension(400,200));
        orderCommentsScroll.setBounds(80,600,600 - 160,120);
        pizzaFrame.getContentPane().add(orderCommentsScroll);

        CyderButton placeOrder = new CyderButton("Place Order");
        placeOrder.setFont(CyderFonts.segoe20);
        placeOrder.addActionListener(e -> {
            if (nameField.getText().length() <= 0)
                pizzaFrame.notify("Sorry, but you must enter a name.");

            else {
                String Name = nameField.getText().substring(0, 1).toUpperCase() + nameField.getText().substring(1) + "<br/>";
                String Size;

                if (smallPizza.isSelected())
                    Size = "Small<br/>";

                else if (mediumPizza.isSelected())
                    Size = "Medium<br/>";

                else
                    Size = "Large<br/>";

                String Crust = crustTypeScroll.getSelectedElement();

                if (Crust == null)
                    Crust = "Thin";

                LinkedList<String> ToppingsList = pizzaToppingsScroll.getSelectedElements();
                ArrayList<String> ToppingsArrList = new ArrayList<>();

                for (Object o : ToppingsList)
                    ToppingsArrList.add(o.toString());

                if (ToppingsArrList.isEmpty())
                    ToppingsArrList.add("Plain");

                StringBuilder ToppingsChosen = new StringBuilder();

                for (String s : ToppingsArrList)
                    ToppingsChosen.append(s).append("<br/>");

                String Extras = "";

                if (breadSticks.isSelected())
                    Extras += "Bread Sticks<br/>";

                if (salad.isSelected())
                    Extras += "Salad<br/>";

                if (soda.isSelected())
                    Extras += "Soda<br/>";

                String Comments = orderComments.getText().trim();

                if (Extras.isEmpty()) {
                    Extras = "";
                } else {
                    Extras = "<br/>Extras: " + "<br/>" + Extras;
                }

                Comments = Comments.trim().isEmpty() ? "" : "<br/>Comments: " + "<br/>" + Comments;

                pizzaFrame.inform("Customer Name: " + "<br/>" + Name + "<br/>" + "Size: "
                    + "<br/>" + Size + "<br/>" + "Crust: " + "<br/>" + Crust + "<br/><br/>" + "Toppings: " + "<br/>" + ToppingsChosen
                        + Extras + Comments,"");

            }
        });
        placeOrder.setBounds(80,740,200,40);
        pizzaFrame.getContentPane().add(placeOrder);

        CyderButton resetPizza = new CyderButton("Reset");
        resetPizza.setFont(CyderFonts.segoe20);
        resetPizza.addActionListener(e -> {
            nameField.setText("");
            smallPizza.setNotSelected();
            mediumPizza.setNotSelected();
            largePizza.setNotSelected();
            crustTypeScroll.clearSelectedElements();
            pizzaToppingsScroll.clearSelectedElements();
            breadSticks.setNotSelected();
            salad.setNotSelected();
            soda.setNotSelected();
            orderComments.setText("");

        });
        resetPizza.setBounds(180 + 100 + 40,740,200,40);
        pizzaFrame.getContentPane().add(resetPizza);

        pizzaFrame.finalizeAndShow();
    }
}
