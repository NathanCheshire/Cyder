package cyder.ui;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.handlers.internal.SessionHandler;
import cyder.utilities.ReflectionUtil;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/*
    Credit: Philipp Danner from Stack Overflow
    https://stackoverflow.com/questions/16373459/java-jscrollbar-design/16375805
*/
public class CyderScrollPane extends JScrollPane {
    private static int SCROLL_BAR_ALPHA_ROLLOVER = 100;
    private static int SCROLL_BAR_ALPHA = 50;
    private static int THUMB_SIZE = 8;
    private static int SB_SIZE = 10;
    private static Color THUMB_COLOR = new Color(120,120,120);

    public CyderScrollPane(Component view) {
        this(view, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);
        setThumbColor(CyderColors.regularPink);
        setFont(CyderFonts.segoe20);
        setBackground(new Color(0,0,0,0));
        getViewport().setBackground(new Color(0,0,0,0));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                SessionHandler.log(SessionHandler.Tag.ACTION, e.getComponent());
            }
        });
    }

    public CyderScrollPane(int vsbPolicy, int hsbPolicy) {
        this(null, vsbPolicy, hsbPolicy);
        setThumbColor(CyderColors.regularPink);
        setFont(CyderFonts.segoe20);
        setBackground(new Color(0,0,0,0));
        getViewport().setBackground(new Color(0,0,0,0));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                SessionHandler.log(SessionHandler.Tag.ACTION, e.getComponent());
            }
        });
    }

    public void setScrollBarAlphaRollover(int alpha) {
        this.SCROLL_BAR_ALPHA_ROLLOVER = alpha;
    }

    public void setScrollBarAlpha(int alpha) {
        this.SCROLL_BAR_ALPHA = alpha;
    }

    public void setThumbSize(int size) {
        this.THUMB_SIZE = size;
    }

    public void setSBSize(int size) {
        this.SB_SIZE = size;
    }

    public void setThumbColor(Color c) {
        this.THUMB_COLOR = c;
    }

    public CyderScrollPane(Component view, int vsbPolicy, int hsbPolicy) {

        setBorder(null);

        // Set ScrollBar UI
        JScrollBar verticalScrollBar = getVerticalScrollBar();
        verticalScrollBar.setOpaque(false);
        verticalScrollBar.setUI(new ModernScrollBarUI(this));

        JScrollBar horizontalScrollBar = getHorizontalScrollBar();
        horizontalScrollBar.setOpaque(false);
        horizontalScrollBar.setUI(new ModernScrollBarUI(this));

        setLayout(new ScrollPaneLayout() {
            private static final long serialVersionUID = 5740408979909014146L;

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

            boolean vsbNeeded = isVerticalScrollBarfNecessary();
            boolean hsbNeeded = isHorizontalScrollBarNecessary();

            // vertical scroll bar
            Rectangle vsbR = new Rectangle();
            vsbR.width = SB_SIZE;
            vsbR.height = availR.height - (hsbNeeded ? vsbR.width : 0);
            vsbR.x = availR.x + availR.width - vsbR.width;
            vsbR.y = availR.y;
            if (vsb != null) {
                vsb.setBounds(vsbR);
            }

            // horizontal scroll bar
            Rectangle hsbR = new Rectangle();
            hsbR.height = SB_SIZE;
            hsbR.width = availR.width - (vsbNeeded ? hsbR.height : 0);
            hsbR.x = availR.x;
            hsbR.y = availR.y + availR.height - hsbR.height;
            if (hsb != null) {
                hsb.setBounds(hsbR);
            }
            }
        });

        // Layering
        setComponentZOrder(getVerticalScrollBar(), 0);
        setComponentZOrder(getHorizontalScrollBar(), 1);
        setComponentZOrder(getViewport(), 2);

        viewport.setView(view);
    }
    private boolean isVerticalScrollBarfNecessary() {
        Rectangle viewRect = viewport.getViewRect();
        Dimension viewSize = viewport.getViewSize();
        return viewSize.getHeight() > viewRect.getHeight();
    }

    private boolean isHorizontalScrollBarNecessary() {
        Rectangle viewRect = viewport.getViewRect();
        Dimension viewSize = viewport.getViewSize();
        return viewSize.getWidth() > viewRect.getWidth();
    }

    /**
     * Class extending the BasicScrollBarUI and overrides all necessary methods
     */
    private static class ModernScrollBarUI extends BasicScrollBarUI {

        private JScrollPane sp;

        public ModernScrollBarUI(CyderScrollPane sp) {
            this.sp = sp;
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return new InvisibleScrollBarButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return new InvisibleScrollBarButton();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) { }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            int alpha = isThumbRollover() ? SCROLL_BAR_ALPHA_ROLLOVER : SCROLL_BAR_ALPHA;
            int orientation = scrollbar.getOrientation();
            int x = thumbBounds.x;
            int y = thumbBounds.y;

            int width = orientation == JScrollBar.VERTICAL ? THUMB_SIZE : thumbBounds.width;
            width = Math.max(width, THUMB_SIZE);

            int height = orientation == JScrollBar.VERTICAL ? thumbBounds.height : THUMB_SIZE;
            height = Math.max(height, THUMB_SIZE);

            Graphics2D graphics2D = (Graphics2D) g.create();
            graphics2D.setColor(new Color(THUMB_COLOR.getRed(), THUMB_COLOR.getGreen(), THUMB_COLOR.getBlue(), alpha));
            graphics2D.fillRect(x, y, width, height);
            graphics2D.dispose();
        }

        @Override
        protected void setThumbBounds(int x, int y, int width, int height) {
            super.setThumbBounds(x, y, width, height);
            sp.repaint();
        }

        //invisible buttons to hide scroll bar buttons - inner class
        private static class InvisibleScrollBarButton extends JButton {
            private InvisibleScrollBarButton() {
                setOpaque(false);
                setFocusable(false);
                setFocusPainted(false);
                setBorderPainted(false);
                setBorder(BorderFactory.createEmptyBorder());
            }
        }
    }

    @Override
    public String toString() {
        return ReflectionUtil.commonCyderUIReflection(this);
    }
}