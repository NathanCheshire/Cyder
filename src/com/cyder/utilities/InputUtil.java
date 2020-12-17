package com.cyder.utilities;

import com.cyder.Constants.CyderColors;
import com.cyder.Constants.CyderFonts;
import com.cyder.enums.TitlePosition;
import com.cyder.ui.CyderFrame;

import javax.swing.*;
import java.util.LinkedList;
import java.util.List;

public class InputUtil {
    public static boolean confirmation(String input) {
        return (input.toLowerCase().contains("yes") || input.equalsIgnoreCase("y"));
    }

    public static String getInput(String message, String title, int width, int height) {
        return "";
    }

    //this works, make work for above method
    public static void main(String... args) throws Exception {
        System.setProperty("sun.java2d.uiScale","1.0");
        System.out.println(getInt("Test message to enter in any string here for usage in program","Binary Converter"));
    }

    private static String getInt(String message, String title) throws Exception {
        final List<String> holder = new LinkedList<>();

        final CyderFrame inputFrame = new CyderFrame(400,200,new ImageIcon(new ImageUtil().imageFromColor(400,200 + 50,CyderColors.vanila)));
        inputFrame.setTitle(title);
        inputFrame.setTitlePosition(TitlePosition.CENTER);

        JLabel desc = new JLabel("<html><div style='text-align: center;'>" + message + "</div></html>");

        desc.setHorizontalAlignment(JLabel.CENTER);
        desc.setVerticalAlignment(JLabel.CENTER);
        desc.setForeground(CyderColors.navy);
        desc.setFont(CyderFonts.weatherFontSmall.deriveFont(22f));
        desc.setBounds(10, 10, 400 - 20, 200 - 35 * 2);
        inputFrame.getContentPane().add(desc);

        JTextField inputField = new JTextField(40);
        inputField.setForeground(CyderColors.navy);
        inputField.setFont(CyderFonts.weatherFontSmall);
        inputField.setSelectionColor(CyderColors.selectionColor);
        inputField.setBackground(CyderColors.vanila);
        inputField.setBounds(40,200 - 35 * 2, 400 - 80, 40);
        inputField.setBorder(BorderFactory.createLineBorder(CyderColors.navy,5,false));
        inputFrame.getContentPane().add(inputField);

        inputField.addActionListener(e -> {
            synchronized (holder) {
                holder.add(inputField.getText());
                holder.notify();
            }
            inputFrame.dispose();
        });

        inputFrame.setLocationRelativeTo(null);
        inputFrame.setVisible(true);

        synchronized (holder) {
            while (holder.isEmpty())
                holder.wait();

            return  holder.remove(0);
        }
    }
}
