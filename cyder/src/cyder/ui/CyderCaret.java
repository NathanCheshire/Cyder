package cyder.ui;

import cyder.handlers.internal.Logger;
import cyder.utilities.ReflectionUtil;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.geom.Rectangle2D;

public class CyderCaret extends DefaultCaret {
    private final String mark = "<";
    private final Color caretColor;

    public CyderCaret(Color caretColor) {
        setBlinkRate(500);
        this.caretColor = caretColor;

        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    @Override
    protected synchronized void damage(Rectangle r) {
        if (r == null) {
            return;
        }

        JTextComponent comp = getComponent();
        FontMetrics fm = comp.getFontMetrics(comp.getFont());
        int textWidth = fm.stringWidth(">");
        int textHeight = fm.getHeight();
        x = r.x;
        y = r.y;
        width = textWidth;
        height = textHeight;
        repaint(); // calls getComponent().repaint(x, y, width, height)
    }

    @Override
    public void paint(Graphics g) {
        JTextComponent comp = getComponent();
        if (comp == null) {
            return;
        }

        int dot = getDot();
        Rectangle2D r = null;
        try {
            r = comp.modelToView2D(dot);
        } catch (BadLocationException e) {
            return;
        }

        if (r == null) {
            return;
        }

        if ((x != r.getX()) || (y != r.getY())) {
            repaint(); // erase previous location of caret

        }

        if (isVisible()) {
            FontMetrics fm = comp.getFontMetrics(comp.getFont());
            int textWidth = fm.stringWidth("WELCOME TO THE JUNGLE");
            int textHeight = fm.getHeight();

            g.setColor(caretColor);
            g.fillRect(x, y, width, height);
        }
    }

    @Override
    public String toString() {
        return ReflectionUtil.commonCyderToString(this);
    }
}