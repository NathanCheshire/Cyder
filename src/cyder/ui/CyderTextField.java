package cyder.ui;

import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class CyderTextField extends JTextField {
    private int limit = 1;

    public CyderTextField(int colnum) {
        super(colnum);
        this.addMouseListener(new CyderMouseDraggable() {
            @Override
            public void mousePressed(MouseEvent me) {
                super.mousePressed(me);
            }

            @Override
            public void mouseReleased(MouseEvent me) {
                super.mouseReleased(me);
            }
        });

        this.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
            if (getText().length() > limit) {
                evt.consume();
            }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        this.setForeground(CyderColors.navy);
        this.setFont(CyderFonts.weatherFontSmall);
        this.setSelectionColor(CyderColors.selectionColor);
        this.setBackground(CyderColors.vanila);
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
