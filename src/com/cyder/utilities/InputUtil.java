package com.cyder.utilities;

import com.cyder.Constants.CyderColors;
import com.cyder.Constants.CyderFonts;
import com.cyder.ui.CyderFrame;

import javax.swing.*;
import java.util.LinkedList;
import java.util.List;

public class InputUtil {
    public static boolean confirmation(String input) {
        return (input.toLowerCase().contains("yes") || input.equalsIgnoreCase("y"));
    }

    public static String getInput(String message, String title, int width, int height) throws Exception {
        final List<String> holder = new LinkedList<>();

        CyderFrame inputFrame = new CyderFrame(width,height,new ImageIcon(new ImageUtil().imageFromColor(width,height + 50,CyderColors.vanila)));
        inputFrame.setTitle(title);

        JLabel desc = new JLabel("<html><div style='text-align: center;'>" + message + "</div></html>");

        desc.setHorizontalAlignment(JLabel.CENTER);
        desc.setVerticalAlignment(JLabel.CENTER);
        desc.setForeground(CyderColors.navy);
        desc.setFont(CyderFonts.weatherFontSmall.deriveFont(22f));
        desc.setBounds(10, 5, width - 20, height - 35 * 2);
        inputFrame.getContentPane().add(desc);

        JTextField inputField = new JTextField(40);
        inputField.setForeground(CyderColors.navy);
        inputField.setFont(CyderFonts.weatherFontSmall);
        inputField.setSelectionColor(CyderColors.selectionColor);
        inputField.setBackground(CyderColors.vanila);
        inputField.setBounds(40,height - 35 * 2, width - 80, 40);
        inputField.setBorder(BorderFactory.createLineBorder(CyderColors.navy,5,false));
        inputFrame.getContentPane().add(inputField);

        inputField.addActionListener(e -> {
            synchronized (holder) {
                holder.add(inputField.getText());
                holder.notify();
            }

            inputFrame.closeAnimation();
        });

        inputFrame.setVisible(true);
        inputFrame.setLocationRelativeTo(null);
        inputFrame.setAlwaysOnTop(true);

        inputFrame.setVisible(true);
        inputFrame.setLocationRelativeTo(null);
        inputFrame.requestFocus();

        synchronized (holder) {
            while (holder.isEmpty())
                holder.wait();

            return holder.remove(0);
        }
    }

    //this works, make work for above method
    public static void main(String... args) throws Exception {
        final List<Integer> holder = new LinkedList<>();

        final JFrame frame = new JFrame("Test Title");

        final JTextField field = new JTextField("");
        field.setToolTipText("Enter an int and press enter");
        frame.add(field);
        field.addActionListener(e -> {
            synchronized (holder) {
                holder.add(Integer.parseInt(field.getText()));
                holder.notify();
            }
            frame.dispose();
        });

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        synchronized (holder) {
            while (holder.isEmpty())
                holder.wait();

            int nextInt = holder.remove(0);
            System.out.println(nextInt);
        }
    }
}
