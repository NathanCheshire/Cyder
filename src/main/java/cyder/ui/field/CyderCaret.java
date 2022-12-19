package main.java.cyder.ui.field;

import com.google.common.base.Preconditions;
import main.java.cyder.exceptions.IllegalMethodException;
import main.java.cyder.logging.LogTag;
import main.java.cyder.logging.Logger;
import main.java.cyder.strings.CyderStrings;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * A custom caret used for {@link CyderTextField}s and other text containers.
 */
public class CyderCaret extends DefaultCaret {
    /**
     * The color of the caret.
     */
    private final Color caretColor;

    /**
     * The character to use for computing the width of the caret.
     */
    private static final String WIDTH_CHAR = ">";

    /**
     * The default blink rate of Cyder carets.
     */
    private static final int defaultBlinkRate = 500;

    /**
     * Suppress default constructor.
     */
    private CyderCaret() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Constructs a new CyderCaret.
     *
     * @param caretColor the color for the caret
     */
    public CyderCaret(Color caretColor) {
        Preconditions.checkNotNull(caretColor);

        this.caretColor = caretColor;

        setBlinkRate(defaultBlinkRate);

        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected synchronized void damage(Rectangle rectangle) {
        if (rectangle == null) return;

        JTextComponent comp = getComponent();
        FontMetrics fm = comp.getFontMetrics(comp.getFont());

        int textWidth = fm.stringWidth(WIDTH_CHAR);
        int textHeight = fm.getHeight();

        x = rectangle.x;
        y = rectangle.y;

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