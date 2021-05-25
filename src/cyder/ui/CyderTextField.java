package cyder.ui;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class CyderTextField extends JTextField {
    private int limit = 10;
    private Color backgroundColor = CyderColors.vanila;

    public CyderTextField(int colnum) {
        super(colnum);

        this.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent evt) {
            if (getText().length() >= limit) {
                evt.consume();
            }
            }
        });
    }

    @Override
    public void setBackground(Color newBackgroundColor) {
        super.setBackground(newBackgroundColor);
        backgroundColor = newBackgroundColor;
    }

    @Override
    public Color getBackground() {
        return backgroundColor;
    }

    @Override
    protected void paintComponent(Graphics g) {
        this.setForeground(CyderColors.navy);
        this.setFont(CyderFonts.weatherFontSmall);
        this.setSelectionColor(CyderColors.selectionColor);
        this.setBackground(backgroundColor);
        this.setBorder(BorderFactory.createLineBorder(CyderColors.navy,5,false));

        super.paintComponent(g);
    }

    public void setRegexMatcher(String regex) {
        //todo
    }

    public void setCharLimit(int limit) {
        this.limit = limit;
        if (getText().length() > limit) {
            setText(getText().substring(0,limit + 1));
        }
    }

    public int getCharLimit() {
        return this.limit;
    }
}
