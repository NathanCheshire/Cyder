package cyder.ui;

import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.handlers.internal.Logger;
import cyder.utils.ReflectionUtil;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A custom implementation of a ScrollPane to give it a more modern feel.
 * <p>
 * See <a href="https://stackoverflow.com/questions/16373459/java-jscrollbar-design/16375805">Stackoverflow</a>
 */
public class CyderScrollPane extends JScrollPane {
    /**
     * The alpha value for mouse over events.
     */
    private static int scrollBarAlphaRollover = 100;

    /**
     * The default alpha of the scroll bar.
     */
    private static int scrollBarAlpha = 60;

    /**
     * The size of the thumb by default.
     */
    private static int thumbSize = 8;

    /**
     * The size of the scrollbar.
     */
    private static int scrollbarSize = 10;

    /**
     * The color the scroll bar.
     */
    private static Color thumbColor = CyderColors.regularPink;

    /**
     * An empty color used for the background and viewport background color.
     */
    private static final Color emptyColor = new Color(0, 0, 0, 0);

    /**
     * Constructs a new scroll pane for the provided component.
     *
     * @param view the component to wrap with the scroll pane.
     */
    public CyderScrollPane(Component view) {
        this(view, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);

        setThumbColor(CyderColors.regularPink);
        setFont(CyderFonts.SEGOE_20);
        setBackground(emptyColor);
        getViewport().setBackground(emptyColor);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Logger.log(Logger.Tag.UI_ACTION, e.getComponent());
            }
        });

        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    /**
     * Constructs a new scroll pane from the provided policies.
     *
     * @param vsbPolicy the vertical scrollbar policy
     * @param hsbPolicy the horizontal scrollbar policy
     */
    public CyderScrollPane(int vsbPolicy, int hsbPolicy) {
        this(null, vsbPolicy, hsbPolicy);
        verticalScrollBarPolicy = vsbPolicy;
        horizontalScrollBarPolicy = hsbPolicy;
        setThumbColor(CyderColors.regularPink);
        setFont(CyderFonts.SEGOE_20);
        setBackground(new Color(0, 0, 0, 0));
        getViewport().setBackground(new Color(0, 0, 0, 0));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Logger.log(Logger.Tag.UI_ACTION, e.getComponent());
            }
        });

        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    /**
     * Sets the alpha rollover color.
     *
     * @param alpha the alpha rollover color
     */
    public void setScrollBarAlphaRollover(int alpha) {
        scrollBarAlphaRollover = alpha;
    }

    /**
     * Sets the alpha color.
     *
     * @param alpha the alpha color
     */
    public void setScrollBarAlpha(int alpha) {
        scrollBarAlpha = alpha;
    }

    /**
     * Sets the thumb size.
     *
     * @param size the thumb size
     */
    public void setThumbSize(int size) {
        thumbSize = size;
    }

    /**
     * Sets the scrollbar size.
     *
     * @param size the scrollbar size
     */
    public void setScrollbarSize(int size) {
        scrollbarSize = size;
    }

    /**
     * Sets the thumb color.
     *
     * @param c the thumb color
     */
    public void setThumbColor(Color c) {
        thumbColor = c;
    }

    /**
     * Constructs a new scrollpane.
     *
     * @param view the component to surround with a scrollpane
     * @param vsbPolicy the vertical policy
     * @param hsbPolicy the horizontal policy
     */
    public CyderScrollPane(Component view, int vsbPolicy, int hsbPolicy) {
        setBorder(null);
        verticalScrollBarPolicy = vsbPolicy;
        horizontalScrollBarPolicy = hsbPolicy;

        JScrollBar verticalScrollBar = getVerticalScrollBar();
        verticalScrollBar.setOpaque(false);
        verticalScrollBar.setUI(new ModernScrollBarUI(this));

        JScrollBar horizontalScrollBar = getHorizontalScrollBar();
        horizontalScrollBar.setOpaque(false);
        horizontalScrollBar.setUI(new ModernScrollBarUI(this));

        setLayout(new ScrollPaneLayout() {
            @Override
            public void layoutContainer(Container parent) {
                Rectangle availR = parent.getBounds();
                availR.x = availR.y = 0;

                // viewport
                Insets insets = parent.getInsets();
                availR.x = insets.left;
                availR.y = insets.top;
                availR.width -= insets.left + insets.right;
                availR.height -= insets.top + insets.bottom;
                if (viewport != null) {
                    viewport.setBounds(availR);
                }

                boolean vsbNeeded = isVerticalScrollBarNecessary();
                boolean hsbNeeded = isHorizontalScrollBarNecessary();

                if (vsbNeeded) {
                    Rectangle vsbR = new Rectangle();
                    vsbR.width = scrollbarSize;
                    vsbR.height = availR.height - (hsbNeeded ? vsbR.width : 0);
                    vsbR.x = availR.x + availR.width - vsbR.width;
                    vsbR.y = availR.y;
                    if (vsb != null) {
                        vsb.setBounds(vsbR);
                    }
                }

                if (hsbNeeded) {
                    Rectangle hsbR = new Rectangle();
                    hsbR.height = scrollbarSize;
                    hsbR.width = availR.width - (vsbNeeded ? hsbR.height : 0);
                    hsbR.x = availR.x;
                    hsbR.y = availR.y + availR.height - hsbR.height;
                    if (hsb != null) {
                        hsb.setBounds(hsbR);
                    }
                }
            }
        });

        // Layering
        setComponentZOrder(getVerticalScrollBar(), 0);
        setComponentZOrder(getHorizontalScrollBar(), 1);
        setComponentZOrder(getViewport(), 2);

        viewport.setView(view);
    }

    /**
     * Returns whether a vertical scrollbar is necessary.
     *
     * @return whether a vertical scrollbar is necessary
     */
    public boolean isVerticalScrollBarNecessary() {
        Rectangle viewRect = viewport.getViewRect();
        Dimension viewSize = viewport.getViewSize();
        return viewSize.getHeight() > viewRect.getHeight()
                && verticalScrollBarPolicy != VERTICAL_SCROLLBAR_NEVER;
    }

    /**
     * Returns whether a horizontal scrollbar is necessary.
     *
     * @return whether a horizontal scrollbar is necessary
     */
    public boolean isHorizontalScrollBarNecessary() {
        Rectangle viewRect = viewport.getViewRect();
        Dimension viewSize = viewport.getViewSize();

        return viewSize.getWidth() > viewRect.getWidth()
                && horizontalScrollBarPolicy != HORIZONTAL_SCROLLBAR_NEVER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setHorizontalScrollBarPolicy(int policy) {
        horizontalScrollBarPolicy = policy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setVerticalScrollBarPolicy(int policy) {
        verticalScrollBarPolicy = policy;
    }

    /**
     * Inner class extending the BasicScrollBarUI that overrides all necessary methods.
     */
    private static class ModernScrollBarUI extends BasicScrollBarUI {
        private final JScrollPane scrollPane;

        /**
         * Constructs a new modern scrollbar ui.
         *
         * @param scrollpane the scroll pane
         */
        public ModernScrollBarUI(CyderScrollPane scrollpane) {
            scrollPane = scrollpane;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected JButton createDecreaseButton(int orientation) {
            return new InvisibleScrollBarButton();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected JButton createIncreaseButton(int orientation) {
            return new InvisibleScrollBarButton();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            int alpha = isThumbRollover() ? scrollBarAlphaRollover : scrollBarAlpha;
            int orientation = scrollbar.getOrientation();
            int x = thumbBounds.x;
            int y = thumbBounds.y;

            int width = orientation == JScrollBar.VERTICAL ? thumbSize : thumbBounds.width;
            width = Math.max(width, thumbSize);

            int height = orientation == JScrollBar.VERTICAL ? thumbBounds.height : thumbSize;
            height = Math.max(height, thumbSize);

            Graphics2D graphics2D = (Graphics2D) g.create();
            graphics2D.setColor(new Color(CyderScrollPane.thumbColor.getRed(), CyderScrollPane.thumbColor.getGreen(),
                    CyderScrollPane.thumbColor.getBlue(), alpha));
            graphics2D.fillRect(x, y, width, height);
            graphics2D.dispose();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void setThumbBounds(int x, int y, int width, int height) {
            super.setThumbBounds(x, y, width, height);
            scrollPane.repaint();
        }
    }

    /**
     * An invisible button to use for the scroll bar so that they are invisible.
     */
    private static class InvisibleScrollBarButton extends JButton {
        /**
         * Constructs a new invisible scrollbar button.
         */
        private InvisibleScrollBarButton() {
            setOpaque(false);
            setFocusable(false);
            setFocusPainted(false);
            setBorderPainted(false);
            setBorder(BorderFactory.createEmptyBorder());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return ReflectionUtil.commonCyderUiToString(this);
    }
}