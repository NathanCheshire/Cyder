package com.cyder.widgets;

import com.cyder.constants.CyderColors;
import com.cyder.constants.CyderFonts;
import com.cyder.constants.CyderImages;
import com.cyder.ui.CyderButton;
import com.cyder.ui.CyderFrame;
import com.cyder.ui.CyderScrollPane;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import static com.cyder.constants.CyderStrings.DEFAULT_BACKGROUND_PATH;

public class Pizza {

    private CyderFrame pizzaFrame;
    private JTextField nameField;
    private JLabel smallPizza;
    private JLabel mediumPizza;
    private JLabel largePizza;

    private JList<String> pizzaTopingsList;
    private JList<String> crustTypeList;

    private JTextArea orderComments;

    private JLabel breadSticks;
    private JLabel salad;
    private JLabel soda;

    private CyderButton placeOrder;
    private CyderButton resetValues;

    public Pizza() {
        if (pizzaFrame != null)
            pizzaFrame.closeAnimation();

        pizzaFrame = new CyderFrame(600,800, new ImageIcon(DEFAULT_BACKGROUND_PATH));
        pizzaFrame.setTitle("Pizza");

        JLabel CustomerName = new JLabel("Name:");
        CustomerName.setFont(CyderFonts.weatherFontSmall);
        CustomerName.setForeground(CyderColors.navy);
        CustomerName.setBounds(40,45,100,30);
        pizzaFrame.getContentPane().add(CustomerName);

        nameField = new JTextField(20);
        nameField.setSelectionColor(CyderColors.selectionColor);
        nameField.setForeground(CyderColors.navy);
        nameField.setFont(CyderFonts.weatherFontSmall);
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
        nameField.setBorder(new LineBorder(CyderColors.navy,5,false));
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

        smallPizza = new JLabel();
        smallPizza.setHorizontalAlignment(JLabel.CENTER);
        smallPizza.setSize(100,100);
        smallPizza.setIcon(CyderImages.checkboxNotSelected);
        smallPizza.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                smallPizza.setIcon(CyderImages.checkboxSelected);
                mediumPizza.setIcon(CyderImages.checkboxNotSelected);
                largePizza.setIcon(CyderImages.checkboxNotSelected);
            }
        });
        smallPizza.setBounds(160,110,100,100);
        pizzaFrame.getContentPane().add(smallPizza);

        mediumPizza = new JLabel();
        mediumPizza.setHorizontalAlignment(JLabel.CENTER);
        mediumPizza.setSize(100,100);
        mediumPizza.setIcon(CyderImages.checkboxSelected);
        mediumPizza.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                smallPizza.setIcon(CyderImages.checkboxNotSelected);
                mediumPizza.setIcon(CyderImages.checkboxSelected);
                largePizza.setIcon(CyderImages.checkboxNotSelected);
            }
        });
        mediumPizza.setBounds(280,110,100,100);
        pizzaFrame.getContentPane().add(mediumPizza);

        largePizza = new JLabel();
        largePizza.setHorizontalAlignment(JLabel.CENTER);
        largePizza.setSize(100,100);
        largePizza.setIcon(CyderImages.checkboxNotSelected);
        largePizza.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                smallPizza.setIcon(CyderImages.checkboxNotSelected);
                mediumPizza.setIcon(CyderImages.checkboxNotSelected);
                largePizza.setIcon(CyderImages.checkboxSelected);
            }
        });
        largePizza.setBounds(400,110,100,100);
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

        breadSticks = new JLabel();
        breadSticks.setHorizontalAlignment(JLabel.CENTER);
        breadSticks.setSize(100,100);
        breadSticks.setIcon(CyderImages.checkboxNotSelected);
        breadSticks.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (breadSticks.getIcon() == CyderImages.checkboxSelected)
                    breadSticks.setIcon(CyderImages.checkboxNotSelected);
                else
                    breadSticks.setIcon(CyderImages.checkboxSelected);
            }
        });
        breadSticks.setBounds(140,480,100,100);
        pizzaFrame.getContentPane().add(breadSticks);

        JLabel saladLabel = new JLabel("Salad");
        saladLabel.setFont(CyderFonts.weatherFontSmall);
        saladLabel.setForeground(CyderColors.navy);
        saladLabel.setBounds(310,470,150,30);
        pizzaFrame.getContentPane().add(saladLabel);

        salad = new JLabel();
        salad.setHorizontalAlignment(JLabel.CENTER);
        salad.setSize(100,100);
        salad.setIcon(CyderImages.checkboxNotSelected);
        salad.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (salad.getIcon() == CyderImages.checkboxSelected)
                    salad.setIcon(CyderImages.checkboxNotSelected);
                else
                    salad.setIcon(CyderImages.checkboxSelected);
            }
        });
        salad.setBounds(290,480,100,100);
        pizzaFrame.getContentPane().add(salad);

        JLabel sodaLabel = new JLabel("Soda");
        sodaLabel.setFont(CyderFonts.weatherFontSmall);
        sodaLabel.setForeground(CyderColors.navy);
        sodaLabel.setBounds(445,470,150,30);
        pizzaFrame.getContentPane().add(sodaLabel);

        soda = new JLabel();
        soda.setHorizontalAlignment(JLabel.CENTER);
        soda.setSize(100,100);
        soda.setIcon(CyderImages.checkboxNotSelected);
        soda.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (soda.getIcon() == CyderImages.checkboxSelected)
                    soda.setIcon(CyderImages.checkboxNotSelected);
                else
                    soda.setIcon(CyderImages.checkboxSelected);
            }
        });
        soda.setBounds(420,480,100,100);
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
        orderComments.setSelectedTextColor(CyderColors.selectionColor);
        orderComments.setBorder(new LineBorder(new Color(0, 0, 0)));

        //todo this border on this scrollpane is messed up 
        CyderScrollPane orderCommentsScroll = new CyderScrollPane(orderComments,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        orderCommentsScroll.setThumbColor(CyderColors.regularRed);
        orderCommentsScroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        orderCommentsScroll.getViewport().setBorder(null);
        orderCommentsScroll.setViewportBorder(null);
        orderCommentsScroll.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10),
                new LineBorder(CyderColors.navy,5,false)));
        orderCommentsScroll.setPreferredSize(new Dimension(400,200));
        orderCommentsScroll.setBounds(80,600,600 - 160,140);
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

                if (smallPizza.getIcon() == CyderImages.checkboxSelected)
                    Size = "small<br/>";

                else if (mediumPizza.getIcon() == CyderImages.checkboxSelected)
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

                if (breadSticks.getIcon() == CyderImages.checkboxSelected)
                    Extras += "Breadsticks<br/>";

                if (salad.getIcon() == CyderImages.checkboxSelected)
                    Extras += "Salad<br/>";

                if (soda.getIcon() == CyderImages.checkboxSelected)
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
            smallPizza.setIcon(CyderImages.checkboxNotSelected);
            mediumPizza.setIcon(CyderImages.checkboxSelected);
            largePizza.setIcon(CyderImages.checkboxNotSelected);
            crustTypeList.clearSelection();
            pizzaTopingsList.clearSelection();
            breadSticks.setIcon(CyderImages.checkboxNotSelected);
            salad.setIcon(CyderImages.checkboxNotSelected);
            soda.setIcon(CyderImages.checkboxNotSelected);
            orderComments.setText("");

        });
        resetPizza.setBounds(180 + 100 + 40,740,200,40);
        pizzaFrame.getContentPane().add(resetPizza);

        pizzaFrame.setVisible(true);
        pizzaFrame.setLocationRelativeTo(null);
    }
}
