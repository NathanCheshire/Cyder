package cyder.ui.field;

import com.google.common.base.Preconditions;
import cyder.logging.LogTag;
import cyder.logging.Logger;

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
        this.caretColor = Preconditions.checkNotNull(caretColor);
        setBlinkRate(500);

        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * The character to use for computing the width of the caret.
     */
    private static final String WIDTH_CHAR = ">";

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

        int textWidth = fm.stringWidth(WIDTH_CHAR);
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

        if (comp == null) return;

        int currentPosition = getDot();
        Rectangle2D rectangle;

        try {
            rectangle = comp.modelToView2D(currentPosition);
        } catch (BadLocationException e) {
            return;
        }

        if (rectangle == null) return;

        // Remove previous caret
        if (x != rectangle.getX() || y != rectangle.getY()) {
            repaint();
        }

        if (isVisible()) {
            g.setColor(caretColor);
            g.fillRect(x, y, width, height);
        }
    }
}