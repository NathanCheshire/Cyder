package cyder.widgets;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.consts.CyderImages;
import cyder.genesis.GenesisShare;
import cyder.ui.*;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.LinkedList;

public class Pizza {
    private static CyderFrame pizzaFrame;
    private static CyderTextField nameField;
    private static CyderCheckBox smallPizza;
    private static CyderCheckBox mediumPizza;
    private static CyderCheckBox largePizza;

    private static CyderScrollList pizzaToppingsScroll;
    private static CyderScrollList crustTypeScroll;
    private static JLabel pizzaToppingsLabel;
    private static JLabel crustTypeLabel;

    private static LinkedList<String> pizzaToppingsList;
    private static LinkedList<String> crustTypeList;

    private static JTextArea orderComments;

    private static CyderCheckBox breadSticks;
    private static CyderCheckBox salad;
    private static CyderCheckBox soda;

    private static CyderButton placeOrder;
    private static CyderButton resetValues;

    private Pizza() {}

    public static void showGUI() {
        if (pizzaFrame != null)
            pizzaFrame.dispose();

        pizzaFrame = new CyderFrame(600,800, CyderImages.defaultBackground);
        pizzaFrame.setTitle("Pizza");

        JLabel CustomerName = new JLabel("Name:");
        CustomerName.setFont(CyderFonts.weatherFontSmall);
        CustomerName.setForeground(CyderColors.navy);
        CustomerName.setBounds(40,45,100,30);
        pizzaFrame.getContentPane().add(CustomerName);

        nameField = new CyderTextField(0);
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
        pizzaSizeLabel.setFont(CyderFonts.weatherFontSmall);
        pizzaSizeLabel.setForeground(CyderColors.navy);
        pizzaSizeLabel.setBounds(40,140,50,30);
        pizzaFrame.getContentPane().add(pizzaSizeLabel);

        JLabel smallLabel = new JLabel("Small");
        smallLabel.setFont(CyderFonts.weatherFontSmall);
        smallLabel.setForeground(CyderColors.navy);
        smallLabel.setBounds(180,100,100,30);
        pizzaFrame.getContentPane().add(smallLabel);

        JLabel mediumLabel = new JLabel("Medium");
        mediumLabel.setFont(CyderFonts.weatherFontSmall);
        mediumLabel.setForeground(CyderColors.navy);
        mediumLabel.setBounds(285,100,100,30);
        pizzaFrame.getContentPane().add(mediumLabel);

        JLabel largeLabel = new JLabel("Large");
        largeLabel.setFont(CyderFonts.weatherFontSmall);
        largeLabel.setForeground(CyderColors.navy);
        largeLabel.setBounds(420,100,100,30);
        pizzaFrame.getContentPane().add(largeLabel);

        smallPizza = new CyderCheckBox();
        smallPizza.setHorizontalAlignment(JLabel.CENTER);
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

        mediumPizza = new CyderCheckBox();
        mediumPizza.setHorizontalAlignment(JLabel.CENTER);
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

        largePizza = new CyderCheckBox();
        largePizza.setHorizontalAlignment(JLabel.CENTER);
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
        crustLabel.setFont(CyderFonts.weatherFontSmall);
        crustLabel.setForeground(CyderColors.navy);
        crustLabel.setBounds(90,210,130,30);
        pizzaFrame.getContentPane().add(crustLabel);

        JLabel Toppings = new JLabel("Toppings");
        Toppings.setFont(CyderFonts.weatherFontSmall);
        Toppings.setForeground(CyderColors.navy);
        Toppings.setBounds(370,210,130,30);
        pizzaFrame.getContentPane().add(Toppings);

        String[] crustTypes = {"Thin", "Thick", "Deep Dish", "Classic", "Tavern", "Seasonal"};
        crustTypeScroll = new CyderScrollList(160, 200, CyderScrollList.SelectionPolicy.SINGLE);

        for (String crustType : crustTypes) {
            crustTypeScroll.addElement(crustType, null);
        }

        crustTypeLabel = crustTypeScroll.generateScrollList();
        crustTypeLabel.setBounds(80,250,160,200);
        pizzaFrame.getContentPane().add(crustTypeLabel);

        String[] pizzaToppings = {"Pepperoni", "Sausage", "Green Peppers",
                "Onions", "Tomatoes", "Anchovies", "Bacon", "Chicken", "Beef",
                "Olives", "Mushrooms"};
        pizzaToppingsScroll = new CyderScrollList(200, 200, CyderScrollList.SelectionPolicy.MULTIPLE);

        for (String pizzaTopping : pizzaToppings) {
            pizzaToppingsScroll.addElement(pizzaTopping, null);
        }

        pizzaToppingsLabel = pizzaToppingsScroll.generateScrollList();
        pizzaToppingsLabel.setBounds(320,250,200,200);
        pizzaFrame.getContentPane().add(pizzaToppingsLabel);

        JLabel Extra = new JLabel("Extras:");
        Extra.setForeground(CyderColors.navy);
        Extra.setFont(CyderFonts.weatherFontSmall);
        Extra.setBounds(40,510,130,30);
        pizzaFrame.getContentPane().add(Extra);

        JLabel breadsticksLabel = new JLabel("Breadsticks");
        breadsticksLabel.setFont(CyderFonts.weatherFontSmall);
        breadsticksLabel.setForeground(CyderColors.navy);
        breadsticksLabel.setBounds(130,470,150,30);
        pizzaFrame.getContentPane().add(breadsticksLabel);

        breadSticks = new CyderCheckBox();
        breadSticks.setHorizontalAlignment(JLabel.CENTER);
        breadSticks.setNotSelected();
        breadSticks.setBounds(165,505,50,50);
        pizzaFrame.getContentPane().add(breadSticks);

        JLabel saladLabel = new JLabel("Salad");
        saladLabel.setFont(CyderFonts.weatherFontSmall);
        saladLabel.setForeground(CyderColors.navy);
        saladLabel.setBounds(310,470,150,30);
        pizzaFrame.getContentPane().add(saladLabel);

        salad = new CyderCheckBox();
        salad.setHorizontalAlignment(JLabel.CENTER);
        salad.setNotSelected();
        salad.setBounds(315,505,50,50);
        pizzaFrame.getContentPane().add(salad);

        JLabel sodaLabel = new JLabel("Soda");
        sodaLabel.setFont(CyderFonts.weatherFontSmall);
        sodaLabel.setForeground(CyderColors.navy);
        sodaLabel.setBounds(445,470,150,30);
        pizzaFrame.getContentPane().add(sodaLabel);

        soda = new CyderCheckBox();
        soda.setHorizontalAlignment(JLabel.CENTER);
        soda.setNotSelected();
        soda.setBounds(445,505,50,50);
        pizzaFrame.getContentPane().add(soda);

        JLabel orderCommentsLabel = new JLabel("Order Comments");
        orderCommentsLabel.setFont(CyderFonts.weatherFontSmall);
        orderCommentsLabel.setForeground(CyderColors.navy);
        orderCommentsLabel.setBounds(210,565,200,30);
        pizzaFrame.getContentPane().add(orderCommentsLabel);

        orderComments = new JTextArea(5,20);
        orderComments.setFont(CyderFonts.weatherFontSmall);
        orderComments.setAutoscrolls(true);
        orderComments.setLineWrap(true);
        orderComments.setWrapStyleWord(true);
        orderComments.setSelectionColor(CyderColors.selectionColor);
        orderComments.setBorder(new LineBorder(new Color(0, 0, 0)));

        CyderScrollPane orderCommentsScroll = new CyderScrollPane(orderComments,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        orderCommentsScroll.setThumbColor(CyderColors.regularRed);
        orderCommentsScroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        orderCommentsScroll.getViewport().setBorder(null);
        orderCommentsScroll.setViewportBorder(null);
        orderCommentsScroll.setBorder(new LineBorder(CyderColors.navy,5,false));
        orderCommentsScroll.setPreferredSize(new Dimension(400,200));
        orderCommentsScroll.setBounds(80,600,600 - 160,120);
        pizzaFrame.getContentPane().add(orderCommentsScroll);

        CyderButton placeOrder = new CyderButton("Place Order");
        placeOrder.setFont(CyderFonts.weatherFontSmall);
        placeOrder.setColors(CyderColors.regularRed);
        placeOrder.setBackground(CyderColors.regularRed);
        placeOrder.addActionListener(e -> {
            if (nameField.getText().length() <= 0)
                GenericInformer.inform("Sorry, but you must enter a name.","");

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
                    Extras += "Breadsticks<br/>";

                if (salad.isSelected())
                    Extras += "Salad<br/>";

                if (soda.isSelected())
                    Extras += "Soda<br/>";

                String Comments = orderComments.getText().trim();

                if (Extras.length() == 0) {
                    Extras = "";
                } else {
                    Extras = "<br/>Extras: " + "<br/>" + Extras;
                }

                Comments = Comments.trim().length() == 0 ? "" : "<br/>Comments: " + "<br/>" + Comments;

                GenericInformer.inform("Customer Name: " + "<br/>" + Name + "<br/>" + "Size: "
                    + "<br/>" + Size + "<br/>" + "Crust: " + "<br/>" + Crust + "<br/><br/>" + "Toppings: " + "<br/>" + ToppingsChosen
                        + Extras + Comments,"");

            }
        });
        placeOrder.setBounds(80,740,200,40);
        pizzaFrame.getContentPane().add(placeOrder);

        CyderButton resetPizza = new CyderButton("Reset");
        resetPizza.setBackground(CyderColors.regularRed);
        resetPizza.setColors(CyderColors.regularRed);
        resetPizza.setFont(CyderFonts.weatherFontSmall);
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

        pizzaFrame.setVisible(true);
        pizzaFrame.setLocationRelativeTo(GenesisShare.getDominantFrame());
    }
}
