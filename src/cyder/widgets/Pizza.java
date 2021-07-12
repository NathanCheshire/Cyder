package cyder.widgets;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.consts.CyderImages;
import cyder.ui.*;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class Pizza {

    private CyderFrame pizzaFrame;
    private CyderTextField nameField;
    private CyderCheckBox smallPizza;
    private CyderCheckBox mediumPizza;
    private CyderCheckBox largePizza;

    private JList<String> pizzaTopingsList;
    private JList<String> crustTypeList;

    private JTextArea orderComments;

    private CyderCheckBox breadSticks;
    private CyderCheckBox salad;
    private CyderCheckBox soda;

    private CyderButton placeOrder;
    private CyderButton resetValues;

    public Pizza() {
        if (pizzaFrame != null)
            pizzaFrame.closeAnimation();

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

        String[] CrustTypeChoice = {"Thin", "Thick", "Deep Dish", "Classic", "Tavern", "Seasonal"};
        JList crustTopingsJList = new JList(CrustTypeChoice);
        crustTypeList = crustTopingsJList;
        crustTypeList.setForeground(CyderColors.navy);
        crustTypeList.setFont(CyderFonts.weatherFontSmall);
        crustTypeList.setSelectionBackground(CyderColors.selectionColor);
        crustTypeList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        CyderScrollPane crustTopingsListScroll = new CyderScrollPane(crustTypeList,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        crustTopingsListScroll.setThumbColor(CyderColors.regularRed);
        crustTopingsListScroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        crustTopingsListScroll.getViewport().setBorder(null);
        crustTopingsListScroll.setViewportBorder(null);
        crustTopingsListScroll.setBorder(new LineBorder(CyderColors.navy,5,false));
        crustTopingsListScroll.setBounds(80,250,160,200);
        pizzaFrame.getContentPane().add(crustTopingsListScroll);

        JLabel Topings = new JLabel("Topings");
        Topings.setFont(CyderFonts.weatherFontSmall);
        Topings.setForeground(CyderColors.navy);
        Topings.setBounds(370,210,130,30);
        pizzaFrame.getContentPane().add(Topings);

        String[] pizzaTopingsStrList = {"Pepperoni", "Sausage", "Green Peppers",
                "Onions", "Tomatoes", "Anchovies", "Bacon", "Chicken", "Beef",
                "Olives", "Mushrooms"};
        JList pizzaToppingsJList = new JList(pizzaTopingsStrList);
        pizzaTopingsList = pizzaToppingsJList;
        pizzaTopingsList.setForeground(CyderColors.navy);
        pizzaTopingsList.setFont(CyderFonts.weatherFontSmall);
        pizzaTopingsList.setSelectionBackground(CyderColors.selectionColor);
        pizzaTopingsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        CyderScrollPane PizzaTopingsListScroll = new CyderScrollPane(pizzaTopingsList,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        PizzaTopingsListScroll.setThumbColor(CyderColors.regularRed);
        PizzaTopingsListScroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        PizzaTopingsListScroll.getViewport().setBorder(null);
        PizzaTopingsListScroll.setViewportBorder(null);
        PizzaTopingsListScroll.setBorder(new LineBorder(CyderColors.navy,5,false));
        PizzaTopingsListScroll.setBounds(320,250,600 - 80 - 320,200);
        pizzaFrame.getContentPane().add(PizzaTopingsListScroll);

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
                GenericInform.inform("Sorry, but you must enter a name.","");

            else {
                String Name = nameField.getText().substring(0, 1).toUpperCase() + nameField.getText().substring(1) + "<br/>";
                String Size;

                if (smallPizza.isSelected())
                    Size = "small<br/>";

                else if (mediumPizza.isSelected())
                    Size = "medium<br/>";

                else
                    Size = "Large<br/>";

                String Crust = crustTypeList.getSelectedValue();

                List<String> TopingsList = pizzaTopingsList.getSelectedValuesList();
                ArrayList<String> TopingsArrList = new ArrayList<>();

                for (Object o : TopingsList)
                    TopingsArrList.add(o.toString());

                if (TopingsArrList.isEmpty())
                    TopingsArrList.add("Plain");

                StringBuilder TopingsChosen = new StringBuilder();

                for (String s : TopingsArrList)
                    TopingsChosen.append(s).append("<br/>");

                String Extras = "";

                if (breadSticks.isSelected())
                    Extras += "Breadsticks<br/>";

                if (salad.isSelected())
                    Extras += "Salad<br/>";

                if (soda.isSelected())
                    Extras += "Soda<br/>";

                String Comments = orderComments.getText().trim();

                if (Extras.length() == 0)
                    Extras = "";

                else
                    Extras = "<br/>Extras: " + "<br/>" + Extras;

                if (Comments.length() == 0) {
                    GenericInform.inform("Customer Name: " + "<br/>" + Name + "<br/><br/>" + "Size: "
                        + "<br/>" + Size + "<br/><br/>" + "Crust: " + "<br/>" + Crust + "<br/><br/>" + "Topings: " + "<br/><br/>" + TopingsChosen
                        + "<br/><br/>" + Extras,"Order Summary");
                }

                else {
                    GenericInform.inform("Customer Name: " + "<br/>" + Name + "<br/><br/>" + "Size: "
                        + "<br/>" + Size + "<br/><br/>" + "Crust Type: " + "<br/>" + Crust + "<br/><br/>" + "Topings: " + "<br/>" + TopingsChosen
                        + "<br/>" + Extras + "<br/><br/>Comments: " + "<br/><br/>" + Comments,"Order Summary");
                }
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
            crustTypeList.clearSelection();
            pizzaTopingsList.clearSelection();
            breadSticks.setNotSelected();
            salad.setNotSelected();
            soda.setNotSelected();
            orderComments.setText("");

        });
        resetPizza.setBounds(180 + 100 + 40,740,200,40);
        pizzaFrame.getContentPane().add(resetPizza);

        pizzaFrame.setVisible(true);
        ConsoleFrame.getConsoleFrame().setFrameRelative(pizzaFrame);
    }
}
