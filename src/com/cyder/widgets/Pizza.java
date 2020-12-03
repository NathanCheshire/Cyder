package com.cyder.widgets;

import com.cyder.ui.CyderButton;
import com.cyder.ui.CyderFrame;
import com.cyder.ui.CyderScrollPane;
import com.cyder.utilities.GeneralUtil;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

public class Pizza {

    private GeneralUtil pizzaGeneralUtil = new GeneralUtil();

    private JFrame pizzaFrame;
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
            new GeneralUtil().closeAnimation(pizzaFrame);

        pizzaFrame = new CyderFrame(600,800, new ImageIcon("src/com/cyder/io/pictures/DebugBackground.png"));
        pizzaFrame.setTitle("Pizza");

        JLabel CustomerName = new JLabel("Name");
        CustomerName.setFont(pizzaGeneralUtil.weatherFontSmall);
        CustomerName.setForeground(pizzaGeneralUtil.navy);
        //todo bounds and add

        nameField = new JTextField(20);
        nameField.setSelectionColor(pizzaGeneralUtil.selectionColor);
        nameField.setForeground(pizzaGeneralUtil.navy);
        nameField.setFont(pizzaGeneralUtil.weatherFontSmall);
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
        nameField.setBorder(new LineBorder(pizzaGeneralUtil.navy,5,false));
        //todo bounds and add

        JLabel pizzaSizeLabel = new JLabel("Size");
        pizzaSizeLabel.setFont(pizzaGeneralUtil.weatherFontSmall);
        pizzaSizeLabel.setForeground(pizzaGeneralUtil.navy);
        //todo bounds and add

        //todo size group

        JLabel crustLabel = new JLabel("Crust Type");
        crustLabel.setFont(pizzaGeneralUtil.weatherFontSmall);
        crustLabel.setForeground(pizzaGeneralUtil.navy);
        //todo bounds and add

        String[] CrustTypeChoice = {"Thin", "Thick", "Deep Dish", "Classic", "Tavern", "Seasonal"};
        JList crustTopingsJList = new JList(CrustTypeChoice);
        crustTypeList = crustTopingsJList;
        crustTypeList.setForeground(pizzaGeneralUtil.navy);
        crustTypeList.setFont(pizzaGeneralUtil.weatherFontSmall);
        crustTypeList.setSelectionBackground(pizzaGeneralUtil.selectionColor);
        crustTypeList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        CyderScrollPane crustTopicsListScroll = new CyderScrollPane(crustTypeList,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        crustTopicsListScroll.setThumbColor(pizzaGeneralUtil.regularRed);
        crustTopicsListScroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        crustTopicsListScroll.getViewport().setBorder(null);
        crustTopicsListScroll.setViewportBorder(null);
        crustTopicsListScroll.setBorder(new LineBorder(pizzaGeneralUtil.navy,5,false));
        //todo pizzatopicsscroll bounds and add

        JLabel Topings = new JLabel("Topings");
        Topings.setFont(pizzaGeneralUtil.weatherFontSmall);
        Topings.setForeground(pizzaGeneralUtil.navy);
        //todo bounds and add

        String[] pizzaTopingsStrList = {"Pepperoni", "Sausage", "Green Peppers",
                "Onions", "Tomatoes", "Anchovies", "Bacon", "Chicken", "Beef",
                "Olives", "Mushrooms"};
        JList pizzaToppingsJList = new JList(pizzaTopingsStrList);
        pizzaTopingsList = pizzaToppingsJList;
        pizzaTopingsList.setForeground(pizzaGeneralUtil.navy);
        pizzaTopingsList.setFont(pizzaGeneralUtil.weatherFontSmall);
        pizzaTopingsList.setSelectionBackground(pizzaGeneralUtil.selectionColor);
        pizzaTopingsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        CyderScrollPane PizzaTopingsListScroll = new CyderScrollPane(pizzaTopingsList,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        PizzaTopingsListScroll.setThumbColor(pizzaGeneralUtil.regularRed);
        PizzaTopingsListScroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        PizzaTopingsListScroll.getViewport().setBorder(null);
        PizzaTopingsListScroll.setViewportBorder(null);
        PizzaTopingsListScroll.setBorder(new LineBorder(pizzaGeneralUtil.navy,5,false));
        //todo pizzatopicsscroll bounds and add

        JLabel Extra = new JLabel("Extras");
        Extra.setForeground(pizzaGeneralUtil.navy);
        Extra.setFont(pizzaGeneralUtil.weatherFontSmall);
        //todo bounds and add

        //todo bounds and add for extras

        JLabel orderCommentsLabel = new JLabel("Order Comments");
        orderCommentsLabel.setFont(pizzaGeneralUtil.weatherFontSmall);
        orderCommentsLabel.setForeground(pizzaGeneralUtil.navy);
        //todo bounds and add

        orderComments = new JTextArea(5,20);
        orderComments.setFont(pizzaGeneralUtil.weatherFontSmall);
        orderComments.setAutoscrolls(true);
        orderComments.setLineWrap(true);
        orderComments.setWrapStyleWord(true);
        orderComments.setSelectedTextColor(pizzaGeneralUtil.selectionColor);
        orderComments.setBorder(new LineBorder(new Color(0, 0, 0)));

        CyderScrollPane orderCommentsScroll = new CyderScrollPane(orderComments,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        orderCommentsScroll.setThumbColor(pizzaGeneralUtil.regularRed);
        orderCommentsScroll.getVerticalScrollBar().setPreferredSize(new Dimension(0, 0));
        orderCommentsScroll.getViewport().setBorder(null);
        orderCommentsScroll.setViewportBorder(null);
        orderCommentsScroll.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10),
                new LineBorder(pizzaGeneralUtil.navy,5,false)));
        orderCommentsScroll.setPreferredSize(new Dimension(400,200));
        //todo bounds and add scroll

        CyderButton placeOrder = new CyderButton("Place Order");
        placeOrder.setFont(pizzaGeneralUtil.weatherFontSmall);
        placeOrder.setColors(pizzaGeneralUtil.regularRed);
        placeOrder.addActionListener(e -> {
            if (nameField.getText().length() <= 0)
                pizzaGeneralUtil.inform("Sorry, but you must enter a name.","", 300, 200);

            else {
                String Name = nameField.getText().substring(0, 1).toUpperCase() + nameField.getText().substring(1) + "<br/>";
                String Size;

//                if (small.isSelected())
//                    Size = "small<br/>";
//
//                else if (medium.isSelected())
//                    Size = "medium<br/>";
//
//                else
//                    Size = "Large<br/>";

//                String Crust = pizzaCrustCombo.getSelectedItem() + "<br/>";
//                List<?> TopingsList = pizzaTopingsList.getSelectedValuesList();
//                ArrayList<String> TopingsArrList = new ArrayList<>();
//
//                for (Object o : TopingsList)
//                    TopingsArrList.add(o.toString());
//
//                if (TopingsArrList.isEmpty())
//                    TopingsArrList.add("Plain");
//
//                StringBuilder TopingsChosen = new StringBuilder();
//
//                for (String s : TopingsArrList)
//                    TopingsChosen.append(s).append("<br/>");
//
//                String Extras = "";
//
//                if (breadSticks.isSelected())
//                    Extras += "Breadsticks<br/>";
//
//                if (salad.isSelected())
//                    Extras += "Salad<br/>";
//
//                if (soda.isSelected())
//                    Extras += "Soda<br/>";
//
//                String Comments = orderCommentsTextArea.getText().trim();
//
//                if (Extras.length() == 0)
//                    Extras = "";
//
//                else
//                    Extras = "<br/>Extras: " + "<br/>" + Extras;
//
//                if (Comments.length() == 0) {
//                    pizzaGeneralUtil.inform("Customer Name: " + "<br/>" + Name + "<br/><br/>" + "Size: "
//                            + "<br/>" + Size + "<br/><br/>" + "Crust: " + "<br/>" + Crust + "<br/><br/>" + "Topings: " + "<br/><br/>" + TopingsChosen
//                            + "<br/><br/>" + Extras,"", 500, 1200);
//                }
//
//                else {
//                    pizzaGeneralUtil.inform("Customer Name: " + "<br/>" + Name + "<br/><br/>" + "Size: "
//                            + "<br/>" + Size + "<br/><br/>" + "Crust Type: " + "<br/>" + Crust + "<br/><br/>" + "Topings: " + "<br/>" + TopingsChosen
//                            + "<br/>" + Extras + "<br/><br/>Comments: " + "<br/><br/>" + Comments,"", 500, 1200);
//                }
            }
        });
        //todo bounds and add

        CyderButton resetPizza = new CyderButton("Reset");
        resetPizza.setColors(pizzaGeneralUtil.regularRed);
        resetPizza.setFont(pizzaGeneralUtil.weatherFontSmall);
        resetPizza.addActionListener(e -> {
//            nameField.setText("");
//            pizzaSizeGroup.clearSelection();
//            pizzaCrustCombo.setSelectedItem("Thin");
//            pizzaTopingsList.clearSelection();
//            breadSticks.setSelected(false);
//            salad.setSelected(false);
//            soda.setSelected(false);
//            orderCommentsTextArea.setText("");
        });
        //todo bounds and add

        pizzaFrame.setVisible(true);
        pizzaFrame.setLocationRelativeTo(null);
    }
}
