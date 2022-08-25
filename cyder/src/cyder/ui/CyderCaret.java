package cyder.ui;

import cyder.handlers.internal.Logger;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * A custom caret used for CyderTextFields and CyderPasswordFields.
 */
public class CyderCaret extends DefaultCaret {
    /**
     * The color of the caret.
     */
    private final Color caretColor;

    /**
     * Constructs a new CyderCaret.
     *
     * @param caretColor the color for the caret
     */
    public CyderCaret(Color caretColor) {
        setBlinkRate(500);
        this.caretColor = caretColor;

        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    /**
     * {@inheritDoc}
     */
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

        repaint();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void paint(Graphics g) {
        JTextComponent comp = getComponent();
        if (comp == null) {
            return;
        }

        int dot = getDot();
        Rectangle2D r;

        try {
            r = comp.modelToView2D(dot);
        } catch (BadLocationException e) {
            return;
        }

        if (r == null) {
            return;
        }

        if (x != r.getX() || y != r.getY()) {
            repaint(); // erase previous location of caret

        }

        if (isVisible()) {
            g.setColor(caretColor);
            g.fillRect(x, y, width, height);
        }
    }
}