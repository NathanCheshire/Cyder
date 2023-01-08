package cyder.ui.pane;

import com.google.common.base.Preconditions;
import cyder.annotations.ForReadability;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.strings.ToStringUtil;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;
import cyder.ui.UiUtil;

import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A custom implementation of a ScrollPane to give it a more modern feel.
 * <p>
 * See <a href="https://stackoverflow.com/questions/16373459/java-jscrollbar-design/16375805">Stackoverflow</a>
 */
public class CyderScrollPane extends JScrollPane {
    /**
     * The default thumb color for a scroll pane.
     */
    private static final Color DEFAULT_THUMB_COLOR = CyderColors.regularPink;

    /**
     * The maximum alpha of the scroll bar (the value for when the mouse is hovering).
     */
    private int maxScrollbarAlpha = 140;

    /**
     * The minimum alpha of the scroll bar (the default value).
     */
    private int minScrollbarAlpha = 60;

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
    private static Color thumbColor = DEFAULT_THUMB_COLOR;

    /**
     * Constructs a new scroll pane for the provided component.
     *
     * @param view the component to wrap with the scroll pane
     */
    public CyderScrollPane(Component view) {
        this(Preconditions.checkNotNull(view), VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_AS_NEEDED);

        setThumbColor(CyderColors.regularPink);
        setFont(CyderFonts.SEGOE_20);
        setBackground(CyderColors.empty);
        getViewport().setBackground(CyderColors.empty);

        addMouseListener(UiUtil.generateCommonUiLogMouseAdapter());

        Logger.log(LogTag.OBJECT_CREATION, this);
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

        setBackground(CyderColors.empty);
        getViewport().setBackground(CyderColors.empty);

        addMouseListener(UiUtil.generateCommonUiLogMouseAdapter());

        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Sets the alpha rollover color.
     *
     * @param alpha the alpha rollover color
     */
    public void setMaxScrollbarAlpha(int alpha) {
        maxScrollbarAlpha = alpha;
    }

    /**
     * Sets the alpha color.
     *
     * @param alpha the alpha color
     */
    public void setMinScrollbarAlpha(int alpha) {
        minScrollbarAlpha = alpha;
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
     * The view this scrollpane is for.
     */
    private final Component view;

    /**
     * Constructs a new scrollpane.
     *
     * @param view      the component to surround with a scrollpane
     * @param vsbPolicy the vertical policy
     * @param hsbPolicy the horizontal policy
     */
    public CyderScrollPane(Component view, int vsbPolicy, int hsbPolicy) {
        this.view = Preconditions.checkNotNull(view);

        setBorder(null);
        verticalScrollBarPolicy = vsbPolicy;
        horizontalScrollBarPolicy = hsbPolicy;
        JScrollBar verticalScrollBar = getVerticalScrollBar();
        verticalScrollBar.setOpaque(false);
        ModernScrollBarUI verticalUi = new ModernScrollBarUI(this);
        verticalUi.setMaxScrollbarAlpha(maxScrollbarAlpha);
        verticalUi.setMinScrollBarAlpha(minScrollbarAlpha);
        verticalUi.setParent(this);
        verticalScrollBar.setUI(verticalUi);

        JScrollBar horizontalScrollBar = getHorizontalScrollBar();
        horizontalScrollBar.setOpaque(false);
        ModernScrollBarUI horizontalUi = new ModernScrollBarUI(this);
        horizontalUi.setMaxScrollbarAlpha(maxScrollbarAlpha);
        horizontalUi.setMinScrollBarAlpha(minScrollbarAlpha);
        horizontalUi.setParent(this);
        horizontalScrollBar.setUI(horizontalUi);

        setLayout(new ScrollPaneLayout() {
            @Override
            public void layoutContainer(Container parent) {
                Rectangle parentBounds = parent.getBounds();
                parentBounds.x = parentBounds.y = 0;

                Insets insets = parent.getInsets();
                parentBounds.x = insets.left;
                parentBounds.y = insets.top;
                parentBounds.width -= insets.left + insets.right;
                parentBounds.height -= insets.top + insets.bottom;
                if (viewport != null) {
                    viewport.setBounds(parentBounds);
                }

                boolean vsbNeeded = isVerticalScrollBarNecessary();
                boolean hsbNeeded = isHorizontalScrollBarNecessary();

                if (vsbNeeded) {
                    Rectangle vsbR = new Rectangle();
                    vsbR.width = scrollbarSize;
                    vsbR.height = parentBounds.height - (hsbNeeded ? vsbR.width : 0);
                    vsbR.x = parentBounds.x + parentBounds.width - vsbR.width;
                    vsbR.y = parentBounds.y;
                    if (vsb != null) {
                        vsb.setBounds(vsbR);
                    }
                }
                if (hsbNeeded) {
                    Rectangle hsbR = new Rectangle();
                    hsbR.height = scrollbarSize;
                    hsbR.width = parentBounds.width - (vsbNeeded ? hsbR.height : 0);
                    hsbR.x = parentBounds.x;
                    hsbR.y = parentBounds.y + parentBounds.height - hsbR.height;
                    if (hsb != null) {
                        hsb.setBounds(hsbR);
                    }
                }
            }
        });

        setComponentZOrder(getVerticalScrollBar(), 0);
        setComponentZOrder(getHorizontalScrollBar(), 1);
        setComponentZOrder(getViewport(), 2);

        getVerticalScrollBar().revalidate();
        getVerticalScrollBar().repaint();
        getHorizontalScrollBar().revalidate();
        getHorizontalScrollBar().repaint();

        viewport.setView(view);
    }

    /**
     * Returns the view this scroll pane is for.
     *
     * @return the view this scroll pane is for
     */
    public Component getView() {
        return view;
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
        /**
         * The scrollpane this ui is controlling.
         */
        private final JScrollPane scrollPane;

        /**
         * The current alpha value.
         */
        private final AtomicInteger currentAlpha = new AtomicInteger();

        /**
         * The maximum alpha value for the scroll bar's default state.
         */
        private int maxScrollbarAlpha;

        /**
         * The minimum alpha value for the scroll bar's hover state.
         */
        private int minScrollbarAlpha;

        /**
         * The scroll pane this ui is for.
         */
        private Component parent;

        /**
         * Sets the scroll pane this ui is for.
         *
         * @param parent the scroll pane this ui is for
         */
        public void setParent(Component parent) {
            this.parent = parent;
        }

        /**
         * Sets the minimum scrollbar alpha.
         *
         * @param minScrollbarAlpha the minimum scrollbar alpha
         */
        public void setMinScrollBarAlpha(int minScrollbarAlpha) {
            this.minScrollbarAlpha = minScrollbarAlpha;
            currentAlpha.set(minScrollbarAlpha);
        }

        /**
         * Sets the scrollbar alpha rollover value.
         *
         * @param maxScrollBarAlphaRollover the scrollbar alpha rollover value
         */
        public void setMaxScrollbarAlpha(int maxScrollBarAlphaRollover) {
            this.maxScrollbarAlpha = maxScrollBarAlphaRollover;
        }

        /**
         * Constructs a new modern scrollbar ui.
         *
         * @param scrollpane the scroll pane
         */
        public ModernScrollBarUI(CyderScrollPane scrollpane) {
            scrollPane = Preconditions.checkNotNull(scrollpane);
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
        protected void paintTrack(Graphics graphics, JComponent component, Rectangle trackBounds) {}

        /**
         * The boolean for knowing when a state changes in the rollover.
         */
        private final AtomicBoolean mouseInsideThumb = new AtomicBoolean();

        /**
         * {@inheritDoc}
         */
        @Override
        protected void paintThumb(Graphics graphics, JComponent component, Rectangle thumbBounds) {
            boolean currentThumbRollover = isThumbRollover();

            // Entering for first time
            if (currentThumbRollover && !mouseInsideThumb.get()) {
                mouseInsideThumb.set(true);
                endAnimations();
                startAlphaDecrementAnimation();
            }
            // Exiting for first time
            else if (!currentThumbRollover && mouseInsideThumb.get()) {
                mouseInsideThumb.set(false);
                endAnimations();
                startAlphaIncrementAnimation();
            }

            int orientation = scrollbar.getOrientation();
            int x = thumbBounds.x;
            int y = thumbBounds.y;

            int width = orientation == JScrollBar.VERTICAL
                    ? thumbSize : thumbBounds.width;
            width = Math.max(width, thumbSize);

            int height = orientation == JScrollBar.VERTICAL
                    ? thumbBounds.height : thumbSize;
            height = Math.max(height, thumbSize);

            Graphics2D graphics2D = (Graphics2D) graphics.create();

            Color scrollPaneThumbColor = CyderScrollPane.thumbColor;
            Color scrollBarColor = new Color(scrollPaneThumbColor.getRed(), scrollPaneThumbColor.getGreen(),
                    scrollPaneThumbColor.getBlue(), currentAlpha.get());
            graphics2D.setColor(scrollBarColor);
            graphics2D.fillRect(x, y, width, height);
            graphics2D.dispose();
        }

        /**
         * Ends the increment and decrement animations if on-going.
         */
        @ForReadability
        private void endAnimations() {
            alphaIncrementAnimationRunning.set(false);
            alphaDecrementAnimationRunning.set(false);
        }

        /**
         * The delay between alpha animation decrements.
         */
        private final int alphaAnimationTimeout = 5;

        /**
         * The value to change when incrementing or decrementing alpha for the alpha animation.
         */
        private final int alphaAnimationDelta = 2;

        /**
         * Whether the alpha decrement animation thread is running.
         */
        private final AtomicBoolean alphaDecrementAnimationRunning = new AtomicBoolean();

        /**
         * The alpha animation decrementer thread name.
         */
        private static final String ALPHA_DECREMENT_ANIMATION_THREAD_NAME = "Scrollbar Alpha Animation Decrementer";

        /**
         * Starts the alpha decrement animation thread for mouse enter events.
         */
        private void startAlphaDecrementAnimation() {
            if (animationRunning()) return;

            CyderThreadRunner.submit(() -> {
                alphaDecrementAnimationRunning.set(true);
                currentAlpha.set(minScrollbarAlpha);

                while (currentAlpha.get() < maxScrollbarAlpha) {
                    // Told to stop by external source
                    if (!alphaDecrementAnimationRunning.get()) break;

                    int current = currentAlpha.get();
                    currentAlpha.set(current + alphaAnimationDelta);
                    parent.repaint();

                    ThreadUtil.sleep(alphaAnimationTimeout);
                }

                currentAlpha.set(maxScrollbarAlpha);
                parent.repaint();
                alphaDecrementAnimationRunning.set(false);
            }, ALPHA_DECREMENT_ANIMATION_THREAD_NAME);
        }

        /**
         * Whether the alpha increment animation thread is running.
         */
        private final AtomicBoolean alphaIncrementAnimationRunning = new AtomicBoolean();

        /**
         * The alpha animation incrementer thread name.
         */
        private static final String ALPHA_INCREMENT_ANIMATION_THREAD_NAME = "Scrollbar Alpha Animation Incrementer";

        /**
         * Starts the alpha increment animation thread for mouse exit events.
         */
        private void startAlphaIncrementAnimation() {
            if (animationRunning()) return;

            CyderThreadRunner.submit(() -> {
                alphaIncrementAnimationRunning.set(true);
                currentAlpha.set(maxScrollbarAlpha);

                while (currentAlpha.get() > minScrollbarAlpha) {
                    // Told to stop by external source
                    if (!alphaIncrementAnimationRunning.get()) break;

                    int current = currentAlpha.get();
                    currentAlpha.set(current - alphaAnimationDelta);
                    parent.repaint();

                    ThreadUtil.sleep(alphaAnimationTimeout);
                }

                currentAlpha.set(minScrollbarAlpha);
                parent.repaint();
                alphaIncrementAnimationRunning.set(false);
            }, ALPHA_INCREMENT_ANIMATION_THREAD_NAME);
        }

        /**
         * Returns whether an animation is running.
         *
         * @return whether an animation is running
         */
        @ForReadability
        private boolean animationRunning() {
            return alphaIncrementAnimationRunning.get() || alphaDecrementAnimationRunning.get();
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
     * An invisible button to use for the scroll bar so that the buttons to navigate left/right/up/down are invisible.
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
        return ToStringUtil.commonUiComponentToString(this);
    }
}