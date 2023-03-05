package cyder.ui.frame;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.annotations.ForReadability;
import cyder.console.Console;
import cyder.constants.*;
import cyder.getter.GetConfirmationBuilder;
import cyder.getter.GetterUtil;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.InformHandler;
import cyder.layouts.CyderLayout;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.login.LoginHandler;
import cyder.props.Props;
import cyder.strings.CyderStrings;
import cyder.strings.StringUtil;
import cyder.strings.ToStringUtil;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;
import cyder.ui.UiConstants;
import cyder.ui.UiUtil;
import cyder.ui.drag.CyderDragLabel;
import cyder.ui.drag.DragLabelType;
import cyder.ui.drag.button.MenuButton;
import cyder.ui.drag.button.PinButton;
import cyder.ui.frame.enumerations.FrameType;
import cyder.ui.frame.enumerations.MenuType;
import cyder.ui.frame.enumerations.ScreenPosition;
import cyder.ui.frame.enumerations.TitlePosition;
import cyder.ui.frame.notification.NotificationBuilder;
import cyder.ui.frame.notification.NotificationController;
import cyder.ui.frame.tooltip.TooltipMenuController;
import cyder.ui.pane.CyderOutputPane;
import cyder.ui.pane.CyderPanel;
import cyder.ui.pane.CyderScrollPane;
import cyder.ui.resizing.CyderComponentResizer;
import cyder.user.UserDataManager;
import cyder.utils.ColorUtil;
import cyder.utils.ImageUtil;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static cyder.strings.CyderStrings.closingBracket;
import static cyder.strings.CyderStrings.openingBracket;

/**
 * A custom frame component.
 */
public class CyderFrame extends JFrame {
    /**
     * The area exposed to allow frame resizing. The maximum is 5 since
     * 5 is the border of the frame.
     */
    public static final int FRAME_RESIZING_LEN = 2;

    /**
     * The size of the border drawn around the frame.
     */
    public static final int BORDER_LEN = Props.frameBorderLength.getValue();

    /**
     * The value used for {@link #restoreX} and {@link #restoreY} to indicate a drag has not yet occurred.
     */
    public static final int FRAME_NOT_YET_DRAGGED = Integer.MAX_VALUE;

    /**
     * The default index for adding things to the content pane.
     */
    private static final int defaultContentLabelAddingIndex = 0;

    /**
     * Allowable indices to add components to the contentLabel
     * which is a {@link JLayeredPane} and the content pane for CyderFrames.
     */
    public static final ImmutableList<Integer> allowableContentLabelIndices = ImmutableList.of(
            defaultContentLabelAddingIndex, /* Default */
            JLayeredPane.DRAG_LAYER, /* Drag labels */
            JLayeredPane.POPUP_LAYER /* Notifications */
    );

    /**
     * The index of the pin button.
     */
    private static final int PIN_BUTTON_DEFAULT_INDEX = 1;

    /**
     * The index of the minimize button.
     */
    private static final int MINIMIZE_BUTTON_DEFAULT_INDEX = 0;

    /**
     * The minimum allowable width for a CyderFrame.
     */
    private static final int minimumWidth = 200;

    /**
     * The maximum allowable height for a CyderFrame.
     */
    private static final int minimumHeight = 100;

    /**
     * The increment for minimize opacity animations.
     */
    private static final float opacityAnimationDelta = 0.05f;

    /**
     * The delay between minimize animation frames.
     */
    private static final int opacityAnimationDelay = 5;

    /**
     * The maximum opacity the frame can be set to for minimize in/out animations.
     */
    private static final float opacityAnimationMax = 1.0f;

    /**
     * The minimum opacity the frame can be set to for minimize in/out animations.
     */
    private static final float opacityAnimationMin = 0.0f;

    /**
     * The delay surrounding the initial opacity correction set call when
     * animating in a frame after an opacity minimize out animation.
     */
    private static final int restoreAfterMinimizeAnimationDelay = 25;

    /**
     * The font used for the title label (typically equivalent to agencyFB22).
     */
    private static final Font DEFAULT_FRAME_TITLE_FONT = new Font("Agency FB", Font.BOLD, 22);

    /**
     * The length of the border for the content label.
     */
    private static final int contentLabelBorderLength = 3;

    /**
     * The thread name for the frame fade in animation.
     */
    private static final String FADE_IN_ANIMATION_THREAD_NAME = "Frame Fade-in Animation";

    /**
     * Whether threads that were spawned by this instance of CyderFrame have been killed yet.
     * Examples include notifications and dancing.
     */
    private final AtomicBoolean threadsKilled = new AtomicBoolean();

    /**
     * Actions to be invoked before dispose is invoked.
     */
    private final ArrayList<Runnable> preCloseActions = new ArrayList<>();

    /**
     * Actions to be invoked after dispose is invoked.
     */
    private final ArrayList<Runnable> postCloseActions = new ArrayList<>();

    /**
     * The foreground color of the title label
     */
    private Color titleLabelColor = CyderColors.vanilla;

    /**
     * The font for the title label.
     */
    private Font titleLabelFont = DEFAULT_FRAME_TITLE_FONT;

    /**
     * This CyderFrame's frame type.
     */
    private FrameType frameType = FrameType.DEFAULT;

    /**
     * This CyderFrame's title position.
     */
    private TitlePosition titlePosition = TitlePosition.LEFT;

    /**
     * This CyderFrame's width.
     */
    private int width;

    /**
     * This CyderFrame's height.
     */
    private int height;

    /**
     * The background image for this CyderFrame.
     */
    private ImageIcon background;

    /**
     * The label that hides the exposed area on the top to allow frame resizing.
     */
    private JLabel topDragCover;

    /**
     * The label that hides the exposed area on the bottom to allow frame resizing.
     */
    private JLabel bottomDragCover;

    /**
     * The label that hides the exposed area on the left to allow frame resizing.
     */
    private JLabel leftDragCover;

    /**
     * The label that hides the exposed area on the right to allow frame resizing.
     */
    private JLabel rightDragCover;

    /**
     * The top drag label of this CyderFrame which contains the
     * button list, the title, and any custom components.
     */
    private CyderDragLabel topDrag;

    /**
     * The bottom component responsible for frame location changes on the bottom.
     */
    private CyderDragLabel bottomDrag;

    /**
     * The left component responsible for frame location changes on the left.
     */
    private CyderDragLabel leftDrag;

    /**
     * The right component responsible for frame location changes on the right.
     */
    private CyderDragLabel rightDrag;

    /**
     * The x position of the frame to set to after frame de-iconification actions.
     */
    private int restoreX = FRAME_NOT_YET_DRAGGED;

    /**
     * The y position of the frame to set to after frame de-iconification actions.
     */
    private int restoreY = FRAME_NOT_YET_DRAGGED;

    /**
     * The title of the CyderFrame controlled by the position enum.
     */
    private JLabel titleLabel;

    /**
     * The text displayed on the title label.
     */
    private String title;

    /**
     * The "content pane" of the CyderFrame. This is what is returned
     * when a getContentPane() call is invoked and is what components are added to.
     */
    private JLabel iconLabel;

    /**
     * The true content pane of the CyderFrame. This is necessary so we can do layering
     * between the components, the background, the background image, notifications,
     * drag labels, etc.
     */
    private JLayeredPane contentLabel;

    /**
     * Another layered pane that the content label is added to for layering purposes.
     */
    private JLayeredPane iconPane;

    /**
     * Speeds up performance by not repainting anything on the
     * frame during animations such as minimize and close.
     */
    private boolean disableContentRepainting;

    /**
     * The background color of our CyderFrame.
     * This is the color behind the image if there is one.
     */
    private Color backgroundColor = CyderColors.navy;

    /**
     * The tooltip menu controller for this frame.
     */
    private TooltipMenuController tooltipMenuController;

    /**
     * The notification controller for this frame.
     */
    private NotificationController notificationController;

    /**
     * Whether to paint the title label on the top drag label when {@link #setTitle(String)} is called.
     */
    private boolean paintCyderFrameTitleOnSuperCall = true;

    /**
     * A message to display before the frame is actually disposed.
     */
    private String closingConfirmationMessage;

    /**
     * Constructs a new CyderFrame using the provided builder.
     * Note: use of this constructor is only accessible for overriding
     * methods/extending them in an anonymous-inner fashion. If this is not
     * needed for an instance, use {@link Builder#build()} for construction
     * of a CyderFrame instance.
     *
     * @param builder the builder to construct the frame with
     */
    public CyderFrame(Builder builder) {
        Preconditions.checkNotNull(builder);

        this.width = builder.width;
        this.height = builder.height;
        setSize(width, height);
        setResizable(false);
        setUndecorated(true);
        setBackground(builder.backgroundColor);
        setIconImage(CyderIcons.CYDER_ICON.getImage());
        addListeners(builder.borderless);
        setupFrameBackground(builder);
        setupContentLabel(builder.borderless);
        setupIconLabel();
        setupIconPane();
        contentLabel.add(iconPane, defaultContentLabelAddingIndex);
        setContentPane(contentLabel);
        setupDragLabels(builder);

        if (!builder.borderless) {
            taskbarIconBorderColor = UiUtil.getTaskbarBorderColor();
            setupTitleLabel();
            initializeControllers();
        }

        setFrameType(builder.type);
        revalidateFrameShape();
        setTitle(builder.title);

        threadsKilled.set(false);
        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Sets up the drag labels depending on the state of {@link Builder#borderless}.
     *
     * @param builder the builder
     */
    private void setupDragLabels(Builder builder) {
        if (builder.borderless) {
            setupFullDragLabel(builder);
        } else {
            setupTopDragLabel();
            setupLeftDragLabel();
            setupRightDragLabel();
            setupBottomDragLabel();
        }
    }

    /**
     * Sets up the bottom drag label.
     */
    private void setupBottomDragLabel() {
        int w = width - 2 * FRAME_RESIZING_LEN;
        int h = BORDER_LEN - FRAME_RESIZING_LEN;
        bottomDrag = new CyderDragLabel(w, h, this, DragLabelType.BOTTOM);
        int x = FRAME_RESIZING_LEN;
        int y = height - BORDER_LEN;
        bottomDrag.setBounds(x, y, w, h);
        bottomDrag.setXOffset(x);
        bottomDrag.setYOffset(y);
        bottomDrag.setFocusable(false);
        contentLabel.add(bottomDrag, JLayeredPane.DRAG_LAYER);

        bottomDragCover = new JLabel();
        bottomDragCover.setBounds(0, height - FRAME_RESIZING_LEN, width, FRAME_RESIZING_LEN);
        bottomDragCover.setBackground(CyderColors.getGuiThemeColor());
        bottomDragCover.setOpaque(true);
        contentLabel.add(bottomDragCover, JLayeredPane.DRAG_LAYER);
    }

    /**
     * Sets up the right drag label.
     */
    private void setupRightDragLabel() {
        int w = BORDER_LEN - FRAME_RESIZING_LEN;
        int h = height - CyderDragLabel.DEFAULT_HEIGHT - FRAME_RESIZING_LEN;
        rightDrag = new CyderDragLabel(w, h, this, DragLabelType.RIGHT);
        int x = width - BORDER_LEN;
        int y = CyderDragLabel.DEFAULT_HEIGHT;
        rightDrag.setBounds(x, y, w, h);
        rightDrag.setXOffset(x);
        rightDrag.setYOffset(y);
        rightDrag.setFocusable(false);
        contentLabel.add(rightDrag, JLayeredPane.DRAG_LAYER);

        rightDragCover = new JLabel();
        rightDragCover.setBounds(width - FRAME_RESIZING_LEN, 0, FRAME_RESIZING_LEN, height);
        rightDragCover.setBackground(CyderColors.getGuiThemeColor());
        rightDragCover.setOpaque(true);
        contentLabel.add(rightDragCover, JLayeredPane.DRAG_LAYER);
    }

    /**
     * Sets up the left drag label.
     */
    private void setupLeftDragLabel() {
        int w = BORDER_LEN - FRAME_RESIZING_LEN;
        int h = height - CyderDragLabel.DEFAULT_HEIGHT - FRAME_RESIZING_LEN;
        leftDrag = new CyderDragLabel(w, h, this, DragLabelType.LEFT);
        int x = FRAME_RESIZING_LEN;
        int y = CyderDragLabel.DEFAULT_HEIGHT;
        leftDrag.setBounds(x, y, w, h);
        leftDrag.setXOffset(x);
        leftDrag.setYOffset(y);
        leftDrag.setFocusable(false);
        contentLabel.add(leftDrag, JLayeredPane.DRAG_LAYER);

        leftDragCover = new JLabel();
        leftDragCover.setBounds(0, 0, FRAME_RESIZING_LEN, height);
        leftDragCover.setBackground(CyderColors.getGuiThemeColor());
        leftDragCover.setOpaque(true);
        contentLabel.add(leftDragCover, JLayeredPane.DRAG_LAYER);
    }

    /**
     * Sets up the top drag label.
     */
    private void setupTopDragLabel() {
        int w = width - 2 * FRAME_RESIZING_LEN;
        int h = CyderDragLabel.DEFAULT_HEIGHT - FRAME_RESIZING_LEN;
        topDrag = new CyderDragLabel(w, h, this, DragLabelType.TOP);
        int x = FRAME_RESIZING_LEN;
        int y = FRAME_RESIZING_LEN;
        topDrag.setBounds(x, y, w, h);
        topDrag.setXOffset(x);
        topDrag.setYOffset(y);
        topDrag.setFocusable(false);
        contentLabel.add(topDrag, JLayeredPane.DRAG_LAYER);

        topDragCover = new JLabel();
        topDragCover.setBounds(0, 0, width, FRAME_RESIZING_LEN);
        topDragCover.setBackground(CyderColors.getGuiThemeColor());
        topDragCover.setOpaque(true);
        contentLabel.add(topDragCover, JLayeredPane.DRAG_LAYER);
    }

    /**
     * Sets up a full drag label for this frame.
     *
     * @param builder the builder
     */
    private void setupFullDragLabel(Builder builder) {
        fullDragLabel = new CyderDragLabel(width, height, this, DragLabelType.FULL);
        fullDragLabel.setBackground(builder.backgroundColor);
        fullDragLabel.setBounds(0, 0, width, height);
        contentLabel.add(fullDragLabel, JLayeredPane.DRAG_LAYER);
        fullDragLabel.setFocusable(false);
        contentLabel.add(fullDragLabel);
    }

    /**
     * Initializes and set up the icon pane.
     */
    private void setupIconPane() {
        iconPane = new JLayeredPane();
        int w = width - 2 * FRAME_RESIZING_LEN;
        int h = height - 2 * FRAME_RESIZING_LEN;
        iconPane.setBounds(FRAME_RESIZING_LEN, FRAME_RESIZING_LEN, w, h);
        iconPane.add(iconLabel, defaultContentLabelAddingIndex);
        iconPane.setFocusable(false);
    }

    /**
     * Adds the necessary listeners to this frame.
     *
     * @param borderless whether the frame is borderless
     */
    private void addListeners(boolean borderless) {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose(!borderless);
            }
        });

        if (borderless) {
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowDeiconified(WindowEvent e) {
                    requestFocus();
                    if (getOpacity() > opacityAnimationMax / 2.0f) return;
                    CyderThreadRunner.submit(() -> {
                        /*
                        Note to maintainers: the following three lines exists to avoid the bug of seeing the
                        old Windows XP/95 style frame icons when restoring a frame from iconification.
                         */
                        ThreadUtil.sleep(restoreAfterMinimizeAnimationDelay);
                        setOpacity(opacityAnimationMin);
                        ThreadUtil.sleep(restoreAfterMinimizeAnimationDelay);

                        for (float i = getOpacity() ; i <= opacityAnimationMax ; i += opacityAnimationDelta) {
                            setOpacity(i);
                            ThreadUtil.sleep(opacityAnimationDelay);
                        }
                        setOpacity(opacityAnimationMax);
                    }, "Deiconify Animation");
                }
            });
        }
    }

    /**
     * Sets up the background for the frame.
     *
     * @param builder the builder
     */
    private void setupFrameBackground(Builder builder) {
        this.background = builder.background;
        if (width != background.getIconWidth() || height != background.getIconHeight()) {
            this.background = ImageUtil.resizeImage(background, width, height);
        }
        unalteredBackgroundIcon = background;
    }

    /**
     * Sets up the {@link #contentLabel} for this frame.
     *
     * @param borderless whether the frame is borderless
     */
    private void setupContentLabel(boolean borderless) {
        contentLabel = new JLayeredPane() {
            @Override
            public Component add(Component comp, int index) {
                Preconditions.checkNotNull(comp);
                Preconditions.checkArgument(index >= defaultContentLabelAddingIndex);

                index = allowableContentLabelIndices.contains(index) ? index : defaultContentLabelAddingIndex;
                return super.add(comp, index);
            }
        };
        contentLabel.setFocusable(false);

        if (!borderless) {
            contentLabel.setBorder(new LineBorder(CyderColors.getGuiThemeColor(),
                    contentLabelBorderLength, false));
        }
    }

    /**
     * Sets up the {@link #iconLabel} for this frame.
     */
    private void setupIconLabel() {
        iconLabel = new JLabel() {
            @Override
            public void repaint() {
                if (!disableContentRepainting) {
                    super.repaint();
                }
            }
        };
        int w = width - 2 * FRAME_RESIZING_LEN;
        int h = height - 2 * FRAME_RESIZING_LEN;
        iconLabel.setBounds(FRAME_RESIZING_LEN, FRAME_RESIZING_LEN, w, h);
        iconLabel.setIcon(background);
        iconLabel.setFocusable(false);
    }

    /**
     * Sets up the title label and adds it to the top drag label.
     */
    @ForReadability
    private void setupTitleLabel() {
        titleLabel = new JLabel();
        titleLabel.setFont(titleLabelFont);
        titleLabel.setForeground(titleLabelColor);
        titleLabel.setOpaque(false);
        titleLabel.setFocusable(false);
        titleLabel.setVisible(true);
        topDrag.add(titleLabel);
    }

    /**
     * Initializes the controllers for this frame:
     *
     * <ul>
     *     <li>The tooltip menu controller</li>
     *     <li>The notification controller</li>
     * </ul>
     */
    @ForReadability
    private void initializeControllers() {
        tooltipMenuController = new TooltipMenuController(this);
        notificationController = new NotificationController(this);
    }

    /**
     * A builder for a {@link CyderFrame}.
     */
    public static final class Builder {
        /**
         * The default length of a frame.
         */
        private static final int defaultFrameLength = 400;

        /**
         * The default title of a frame.
         */
        private static final String defaultFrameTitle = "CyderFrame";

        /**
         * The title for the frame.
         */
        private String title = defaultFrameTitle;

        /**
         * The height for the frame.
         */
        private int height = defaultFrameLength;

        /**
         * The width for the frame.
         */
        private int width = defaultFrameLength;

        /**
         * The background for the frame.
         */
        private ImageIcon background = CyderIcons.defaultBackground;

        /**
         * The background color for the frame.
         */
        private Color backgroundColor = CyderColors.vanilla;

        /**
         * The type of frame.
         */
        private FrameType type = FrameType.DEFAULT;

        /**
         * Whether the frame should be borderless.
         */
        private boolean borderless = false;

        /**
         * Constructs a new builder.
         */
        public Builder() {
            Logger.log(LogTag.OBJECT_CREATION, this);
        }

        /**
         * Sets the frame title.
         *
         * @param title the frame title
         * @return this builder
         */
        public Builder setTitle(String title) {
            Preconditions.checkNotNull(title);
            Preconditions.checkArgument(!title.isEmpty());
            this.title = title;
            return this;
        }

        /**
         * Sets the height for this frame.
         *
         * @param height the height for this frame
         * @return this builder
         */
        public Builder setHeight(int height) {
            height = attemptInitialHeightCorrectionIfEnabled(height);
            Preconditions.checkArgument(height >= minimumHeight);
            this.height = height;
            return this;
        }

        /**
         * Sets the width for this frame.
         *
         * @param width the width for this frame
         * @return this builder
         */
        public Builder setWidth(int width) {
            width = CyderFrame.attemptInitialWidthCorrectionIfEnabled(width);
            Preconditions.checkArgument(width >= minimumWidth);
            this.width = width;
            return this;
        }

        /**
         * Sets the size for the frame.
         *
         * @param size the size for the frame
         * @return this builder
         */
        public Builder setSize(Dimension size) {
            int width = (int) Preconditions.checkNotNull(size).getWidth();
            width = attemptInitialWidthCorrectionIfEnabled(width);
            Preconditions.checkArgument(width >= minimumWidth);

            int height = (int) Preconditions.checkNotNull(size).getHeight();
            height = attemptInitialHeightCorrectionIfEnabled(height);
            Preconditions.checkArgument(height >= minimumHeight);

            this.width = width;
            this.height = height;

            return this;
        }

        /**
         * Sets the background icon for this frame.
         *
         * @param background the background icon for this frame
         * @return this builder
         */
        public Builder setBackgroundIcon(ImageIcon background) {
            Preconditions.checkNotNull(background);
            this.background = background;
            return this;
        }

        /**
         * Sets the background icon to one of the provided color and of the current size.
         *
         * @param color the color
         * @return this builder
         */
        public Builder setBackgroundIconFromColor(Color color) {
            Preconditions.checkNotNull(color);
            this.background = ImageUtil.imageIconFromColor(color, width, height);
            this.backgroundColor = color;
            return this;
        }

        /**
         * Sets the background color for this frame.
         *
         * @param backgroundColor the background color for this frame
         * @return this builder
         */
        public Builder setBackgroundColor(Color backgroundColor) {
            Preconditions.checkNotNull(backgroundColor);
            this.backgroundColor = backgroundColor;
            return this;
        }

        /**
         * Sets the type of this frame.
         *
         * @param type the type of this frame
         * @return this builder
         */
        public Builder setType(FrameType type) {
            Preconditions.checkNotNull(type);
            this.type = type;
            return this;
        }

        /**
         * Sets whether this frame is borderless.
         *
         * @param borderless whether this frame is borderless
         * @return this builder
         */
        public Builder setBorderless(boolean borderless) {
            this.borderless = borderless;
            return this;
        }

        /**
         * Builds and returns a new cyder frame using this builder.
         *
         * @return a new cyder frame
         */
        public CyderFrame build() {
            return new CyderFrame(this);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "Builder{"
                    + "title=\"" + title + "\""
                    + ", height=" + height
                    + ", width=" + width
                    + ", background=" + background
                    + ", backgroundColor=" + backgroundColor
                    + ", type=" + type
                    + ", borderless=" + borderless
                    + "}";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            int ret = Objects.hashCode(title);
            ret = 31 * ret + Objects.hashCode(width);
            ret = 31 * ret + Objects.hashCode(height);
            ret = 31 * ret + Objects.hashCode(background);
            ret = 31 * ret + Objects.hashCode(backgroundColor);
            ret = 31 * ret + Objects.hashCode(type);
            ret = 31 * ret + Boolean.hashCode(borderless);
            return ret;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (!(o instanceof Builder)) {
                return false;
            }

            Builder other = (Builder) o;
            return Objects.equals(title, other.title)
                    && width == other.width
                    && height == other.height
                    && Objects.equals(background, other.background)
                    && Objects.equals(backgroundColor, other.backgroundColor)
                    && Objects.equals(type, other.type)
                    && borderless == other.borderless;
        }
    }

    /**
     * The master drag label for borderless frames.
     */
    private CyderDragLabel fullDragLabel;

    /**
     * Returns the full drag label, only present on borderless frames.
     *
     * @return the full drag label
     */
    public CyderDragLabel getFullDragLabel() {
        Preconditions.checkArgument(isBorderlessFrame());

        return fullDragLabel;
    }

    /**
     * Returns the content pane of the CyderFrame of which components should be added to.
     *
     * @return the content pane of the CyderFrame of which components should be added to
     */
    @Override
    public Container getContentPane() {
        return iconLabel;
    }

    /**
     * Returns the icon pane of this CyderFrame.
     * Currently, this is necessary for Console's audio menu and taskbar menu.
     *
     * @return the icon pane of this CyderFrame
     */
    public Container getIconPane() {
        return iconPane;
    }

    /**
     * Returns the actual content pane of this CyderFrame.
     *
     * @return the actual content pane of this CyderFrame
     */
    public Container getTrueContentPane() {
        return contentLabel;
    }

    // -------------
    // Frame layouts
    // -------------

    /**
     * The CyderPanel associated with this CyderFrame which dictates
     * how the components on the content pane are laid out.
     */
    private CyderPanel cyderPanel;

    /**
     * Returns whether the frame is using a layout for it's content
     * pane as opposed to the default absolute layout.
     *
     * @return whether the frame is using a layout for it's content
     * pane as opposed to the default absolute layout
     */
    public boolean isUsingCyderLayout() {
        return cyderPanel != null;
    }

    /**
     * Creates a {@link CyderPanel} with the provided layout and sets that as the content pane.
     *
     * @param layout the layout to manage the components
     */
    public void setCyderLayout(CyderLayout layout) {
        Preconditions.checkNotNull(layout);

        setCyderLayoutPanel(new CyderPanel(layout));
    }

    /**
     * Adds the provided CyderPanel on top of the content pane which
     * is also resized on CyderFrame resize events.
     *
     * @param cyderPanel the CyderPanel with an appropriate CyderLayout
     */
    public void setCyderLayoutPanel(CyderPanel cyderPanel) {
        if (this.cyderPanel != null) removeCyderLayoutPanel();

        if (cyderPanel == null) return;
        this.cyderPanel = cyderPanel;

        // Panel literally sits on top of contentPane() (iconLabel in CyderFrame's case)
        cyderPanel.setBounds(BORDER_LEN, CyderDragLabel.DEFAULT_HEIGHT,
                getWidth() - 2 * BORDER_LEN,
                getHeight() - CyderDragLabel.DEFAULT_HEIGHT - BORDER_LEN);
        iconLabel.add(cyderPanel);
        cyderPanel.repaint();
    }

    /**
     * Removes the current cyder panel from the content pane.
     */
    public void removeCyderLayoutPanel() {
        Preconditions.checkNotNull(cyderPanel);

        iconLabel.remove(cyderPanel);
        cyderPanel = null;
    }

    /**
     * Returns the components managed by the layout.
     *
     * @return the components managed by the layout
     * @throws IllegalStateException if no layout is associated with the name
     */
    public Collection<Component> getLayoutComponents() {
        checkNotNull(cyderPanel);
        checkNotNull(cyderPanel.getLayoutComponents());

        return cyderPanel.getLayoutComponents();
    }

    // ------------------------------
    // Frame positions based on enums
    // ------------------------------

    /**
     * Sets the title position of the title label. If the frame is visible, and the location
     * can be accommodated, the label is animated to its destination.
     *
     * @param newPosition the new position for the title
     * @throws IllegalStateException if the requested title position cannot be accommodated
     * @see CyderFrame#titlePosition
     */
    public void setTitlePosition(TitlePosition newPosition) {
        Preconditions.checkNotNull(newPosition);

        setTitlePosition(newPosition, true);
    }

    /**
     * Sets the title position of the title label. If the frame is visible, and the location
     * can be accommodated, the label is animated to its destination if requested.
     *
     * @param newPosition the new position for the title
     * @param animate     whether the animation should be performed
     * @throws IllegalStateException if the requested title position cannot be accommodated
     * @see CyderFrame#titlePosition
     */
    public void setTitlePosition(TitlePosition newPosition, boolean animate) {
        Preconditions.checkNotNull(newPosition);

        if (this.titlePosition == null || this.titlePosition == newPosition || isBorderlessFrame()) return;

        if (newPosition == TitlePosition.LEFT && topDrag.hasLeftButtons()) {
            throw new IllegalStateException("Cannot place title position to the left"
                    + " as the left button list contains buttons");
        }

        if (newPosition == TitlePosition.RIGHT && topDrag.hasRightButtons()) {
            throw new IllegalStateException("Cannot place title position to the right"
                    + " as the right button list contains buttons");
        }

        int dragWidth = topDrag.getWidth();
        int dragHeight = topDrag.getHeight();

        Font titleFont = titleLabel.getFont();
        int titleWidth = StringUtil.getAbsoluteMinWidth(title, titleFont);
        int titleHeight = CyderDragLabel.DEFAULT_HEIGHT;

        int animationDelay = 1;
        int y = Math.max(dragHeight / 2 - titleHeight / 2, 0);

        if (isVisible()) {
            if (animate) {
                int animateFrom = titleLabel.getX();
                int animateTo = switch (newPosition) {
                    case LEFT -> titleLabelPadding;
                    case RIGHT -> dragWidth - titleWidth - titleLabelPadding;
                    case CENTER -> dragWidth / 2 - titleWidth / 2;
                };

                String threadName = "Title position animator, frame: " + this;
                if (animateFrom < animateTo) {
                    CyderThreadRunner.submit(() -> {
                        for (int x = animateFrom ; x <= animateTo ; x++) {
                            titleLabel.setBounds(x, y, titleWidth, titleHeight);
                            ThreadUtil.sleep(animationDelay);
                        }

                        titlePosition = newPosition;
                        revalidateTitleLocationAlignmentAndLength();
                    }, threadName);
                } else {
                    CyderThreadRunner.submit(() -> {
                        for (int x = animateFrom ; x >= animateTo ; x--) {
                            titleLabel.setBounds(x, y, titleWidth, titleHeight);
                            ThreadUtil.sleep(animationDelay);
                        }

                        titlePosition = newPosition;
                        revalidateTitleLocationAlignmentAndLength();
                    }, threadName);
                }
            } else {
                titlePosition = newPosition;
                revalidateTitleLocationAlignmentAndLength();
            }
        } else {
            titlePosition = newPosition;
            revalidateTitleLocationAlignmentAndLength();
            titleLabel.setVisible(true);
        }
    }

    /**
     * This method is used only by the above method {@link #setTitlePosition(TitlePosition)}.
     * Invokes the following:
     * <ul>
     *     <li>{@link #revalidateTitlePositionLocation()}</li>
     *     <li>{@link #correctTitleLabelAlignment()}</li>
     *     <li>{@link #correctTitleLength()}</li>
     * </ul>
     */
    @ForReadability
    private void revalidateTitleLocationAlignmentAndLength() {
        revalidateTitlePositionLocation();
        correctTitleLabelAlignment();
        correctTitleLength();
    }

    /**
     * Sets the alignment of the title label based on the currently set title position.
     */
    private void correctTitleLabelAlignment() {
        switch (titlePosition) {
            case LEFT -> titleLabel.setHorizontalAlignment(JLabel.LEFT);
            case CENTER -> titleLabel.setHorizontalAlignment(JLabel.CENTER);
            case RIGHT -> titleLabel.setHorizontalAlignment(JLabel.RIGHT);
        }
    }

    /**
     * The value to separate the start/end of the title label from the start/end of the drag label.
     */
    private static final int titleLabelPadding = 5;

    /**
     * Revalidates the location the title label is anchored to based off of the currently set title position.
     */
    private void revalidateTitlePositionLocation() {
        if (isBorderlessFrame()) return;

        int dragWidth = topDrag.getWidth();
        int titleWidth = titleLabel.getWidth();

        switch (titlePosition) {
            case LEFT -> titleLabel.setLocation(titleLabelPadding, 0);
            case RIGHT -> titleLabel.setLocation(width - titleWidth - titleLabelPadding, 0);
            case CENTER -> titleLabel.setLocation(dragWidth / 2 - titleWidth / 2, 0);
        }
    }

    /**
     * Returns the tile position of this frame.
     *
     * @return the tile position of this frame
     */
    public TitlePosition getTitlePosition() {
        return titlePosition;
    }

    /**
     * Returns the frame type of this CyderFrame.
     *
     * @return the frame type of this CyderFrame. See {@link CyderFrame#frameType}
     */
    public FrameType getFrameType() {
        return frameType;
    }

    /**
     * Sets the frame type of this CyderFrame.
     *
     * @param frameType the frame type of this frame
     */
    public void setFrameType(FrameType frameType) {
        this.frameType = Preconditions.checkNotNull(frameType);

        switch (frameType) {
            case DEFAULT -> {}
            case POPUP -> {
                if (!isBorderlessFrame()) {
                    topDrag.removeRightButton(PIN_BUTTON_DEFAULT_INDEX);
                    topDrag.removeRightButton(MINIMIZE_BUTTON_DEFAULT_INDEX);
                }
            }
            case INPUT_GETTER -> topDrag.removeRightButton(PIN_BUTTON_DEFAULT_INDEX);
            default -> throw new IllegalStateException("Unimplemented state: " + frameType);
        }

        refreshAlwaysOnTop();
    }

    /**
     * Refreshes whether this frame is always on top based on the currently
     * set frame state and the state of the pinned button.
     */
    public void refreshAlwaysOnTop() {
        switch (frameType) {
            case DEFAULT -> {
                boolean notDefaultState = getTopDragLabel().getPinButton()
                        .getCurrentState() != PinButton.PinState.DEFAULT;
                setAlwaysOnTop(notDefaultState);
            }
            case INPUT_GETTER, POPUP -> setAlwaysOnTop(true);
        }
    }

    /**
     * Whether to paint the CyderFrame's title label when {@link #setTitle(String)} is called.
     *
     * @param enable whether ot paint CyderFrame's title label when {@link #setTitle(String)} is called
     */
    public void setPaintCyderFrameTitleOnSuperCall(boolean enable) {
        paintCyderFrameTitleOnSuperCall = enable;
    }

    /**
     * Returns whether the title label will be painted/updated when {@link #setTitle(String)} is called.
     *
     * @return whether the title label will be painted/updated when {@link #setTitle(String)} is called
     */
    public boolean getPaintCyderFrameTitleOnSuperCall() {
        return paintCyderFrameTitleOnSuperCall;
    }

    /**
     * Whether to set the title for the underlying abstract frame object.
     */
    private boolean paintSuperTitle = true;

    /**
     * Whether to paint the window title (this is the title that is displayed on your OS' taskbar).
     *
     * @param enable whether to paint the window title
     */
    public void setPaintSuperTitle(boolean enable) {
        paintSuperTitle = enable;
    }

    /**
     * Returns whether the window title will be set.
     *
     * @return whether the window title will be set
     */
    public boolean getPaintSuperTitle() {
        return paintSuperTitle;
    }

    /**
     * Sets the title of the frame of the OS' taskbar if {@link #paintSuperTitle}
     * is true as well as the painted label on the CyderFrame
     * if {@link #paintCyderFrameTitleOnSuperCall} is set to true.
     *
     * @param title the String representing the chosen CyderFrame title
     */
    @Override
    public void setTitle(String title) {
        Preconditions.checkNotNull(title);

        title = StringUtil.getTrimmedText(title);

        if (paintSuperTitle) {
            super.setTitle(title);
            this.title = title;
        } else {
            super.setTitle("");
        }

        if (paintCyderFrameTitleOnSuperCall) setCyderFrameTitle(title);
        correctTitleLength();
        if (!isConsole()) Console.INSTANCE.revalidateConsoleTaskbarMenu();
    }

    /**
     * Sets the painted title on the top drag label to the provided text.
     *
     * @param title the painted title text
     */
    public void setCyderFrameTitle(String title) {
        Preconditions.checkNotNull(title);

        if (titleLabel == null) return;
        titleLabel.setText(StringUtil.getTrimmedText(title));
        correctTitleLength();
    }

    /**
     * Simple, quick, and easy way to show a notification on the frame without using
     * a builder.
     *
     * @param htmlText the text containing possibly formatted text to display
     */
    public void notify(String htmlText) {
        Preconditions.checkArgument(!StringUtil.isNullOrEmpty(htmlText));

        notify(new NotificationBuilder(htmlText));
    }

    /**
     * Notifies the user with a custom notification built from the provided builder.
     * See {@link NotificationBuilder} for more information.
     *
     * @param builder the builder used to construct the notification
     */
    public void notify(NotificationBuilder builder) {
        Preconditions.checkNotNull(builder);

        notificationController.borderNotify(builder);
    }

    /**
     * Displays a simple toast with the provided text.
     *
     * @param htmlText the styled text to use for the toast
     */
    public void toast(String htmlText) {
        Preconditions.checkArgument(!StringUtil.isNullOrEmpty(htmlText));

        toast(new NotificationBuilder(htmlText));
    }

    /**
     * Displays a toast.
     *
     * @param builder the builder for the toast
     */
    public void toast(NotificationBuilder builder) {
        Preconditions.checkNotNull(builder);

        notificationController.toast(builder);
    }

    /**
     * Ends the current notification on screen.
     * If more are behind it, the queue will immediately pull and display
     *
     * @return whether a notification was revoked
     */
    @CanIgnoreReturnValue
    public boolean revokeCurrentNotification() {
        return notificationController.revokeCurrentNotification();
    }

    /**
     * Revokes the current notification on screen.
     *
     * @param animate whether to kill the notification
     *                immediately or to smoothly animate it away first
     * @return whether a notification was revoked
     */
    @CanIgnoreReturnValue
    public boolean revokeCurrentNotification(boolean animate) {
        return notificationController.revokeCurrentNotification(animate);
    }

    /**
     * Revokes the notification currently active or in
     * the queue that matches the provided text.
     *
     * @param expectedText the text of the notification to revoke
     * @return whether a notification was revoked
     */
    @CanIgnoreReturnValue
    public boolean revokeNotification(String expectedText) {
        Preconditions.checkArgument(!StringUtil.isNullOrEmpty(expectedText));

        return notificationController.revokeNotification(expectedText);
    }

    /**
     * Removes all currently displayed notifications and wipes the notification queue.
     */
    public void revokeAllNotifications() {
        notificationController.revokeAllNotifications();
    }

    // ----------------
    // Drag label logic
    // ----------------

    /**
     * Returns the top drag label.
     *
     * @return the top drag label
     */
    public CyderDragLabel getTopDragLabel() {
        return topDrag;
    }

    /**
     * Returns the bottom drag label.
     *
     * @return the bottom drag label
     */
    public CyderDragLabel getBottomDragLabel() {
        return bottomDrag;
    }

    /**
     * Returns the left drag label.
     *
     * @return the left drag label
     */
    public CyderDragLabel getLeftDragLabel() {
        return leftDrag;
    }

    /**
     * Returns the right drag label.
     *
     * @return the right drag label
     */
    public CyderDragLabel getRightDragLabel() {
        return rightDrag;
    }

    /**
     * Pops open a window relative to this CyderFrame with the provided text.
     *
     * @param text  the String you wish to display
     * @param title The title of the CyderFrame which will be opened to display the text
     */
    public void inform(String text, String title) {
        Preconditions.checkNotNull(text);
        Preconditions.checkArgument(!text.isEmpty());
        Preconditions.checkNotNull(title);
        Preconditions.checkArgument(!title.isEmpty());

        new InformHandler.Builder(text).setTitle(title).setRelativeTo(this).inform();
    }

    /**
     * Adds the provided runnable to be invoked before a minimize and iconify invocation.
     *
     * @param runnable the runnable
     */
    public void addPreMinimizeAndIconifyAction(Runnable runnable) {
        preMinimizeAndIconifyActions.add(Preconditions.checkNotNull(runnable));
    }

    /**
     * Adds the provided runnable to be invoked after a minimize and iconify invocation.
     *
     * @param runnable the runnable
     */
    public void addPostMinimizeAndIconifyAction(Runnable runnable) {
        postMinimizeAndIconifyActions.add(Preconditions.checkNotNull(runnable));
    }

    /**
     * Animates away this frame by moving it down until it is offscreen at which point the frame
     * is set to {@link Frame#ICONIFIED}.
     */
    public void minimizeAndIconify() {
        try {
            preMinimizeAndIconifyActions.forEach(Runnable::run);
            setRestorePoint(new Point(getX(), getY()));

            if (UserDataManager.INSTANCE.shouldDoAnimations()) {
                setDisableContentRepainting(true);
                disableDragging();

                for (float i = opacityAnimationMax ; i >= opacityAnimationMin ; i -= opacityAnimationDelta) {
                    if (animatingOut) break;
                    setOpacity(i);
                    repaint();

                    ThreadUtil.sleep(opacityAnimationDelay);
                }

                setOpacity(opacityAnimationMin);
                enableDragging();
            }

            setState(UiConstants.FRAME_ICONIFIED);
            postMinimizeAndIconifyActions.forEach(Runnable::run);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setState(int state) {
        if (state == UiConstants.FRAME_ICONIFIED) {
            setDisableContentRepainting(true);
        } else if (state == UiConstants.FRAME_NORMAL) {
            setDisableContentRepainting(false);
        }

        super.setState(state);
    }

    /**
     * The actions to invoke prior to a minimize and iconify invocation.
     */
    private final ArrayList<Runnable> preMinimizeAndIconifyActions = new ArrayList<>();

    /**
     * The actions to invoke after a minimize and iconify invocation.
     */
    private final ArrayList<Runnable> postMinimizeAndIconifyActions = new ArrayList<>();

    /**
     * Whether this frame's dispose() method has been invoked before.
     */
    private final AtomicBoolean disposed = new AtomicBoolean();

    /**
     * Returns whether this frame's dispose() method has been invoked before.
     *
     * @return whether this frame's dispose() method has been invoked before
     */
    public boolean isDisposed() {
        return disposed.get();
    }

    /**
     * Whether to disable content label repainting.
     *
     * @param enabled whether content label repainting is enabled
     */
    public void setDisableContentRepainting(boolean enabled) {
        disableContentRepainting = enabled;

        if (iconLabel instanceof CyderPanel) {
            ((CyderPanel) iconLabel).setDisableContentRepainting(true);
        }
    }

    /**
     * The hash used to remove this frame from the taskbar exceptions if it was added to it.
     */
    private String removeFromFrameTaskbarExceptionsHash;

    /**
     * Returns the hash used to remove this frame from the taskbar exceptions.
     *
     * @return the hash used to remove this frame from the taskbar exceptions
     */
    public String getRemoveFromFrameTaskbarExceptionsHash() {
        return removeFromFrameTaskbarExceptionsHash;
    }

    /**
     * Sets the hash used to remove this frame from the taskbar exceptions.
     *
     * @param removeFromFrameTaskbarExceptionsHash the hash used to remove this frame from the taskbar exceptions
     */
    public void setRemoveFromFrameTaskbarExceptionsHash(String removeFromFrameTaskbarExceptionsHash) {
        Preconditions.checkNotNull(removeFromFrameTaskbarExceptionsHash);
        Preconditions.checkArgument(!removeFromFrameTaskbarExceptionsHash.isEmpty());

        this.removeFromFrameTaskbarExceptionsHash = removeFromFrameTaskbarExceptionsHash;
    }

    /**
     * Whether this frame should fast close when the default dispose is invoked.
     */
    private boolean shouldFastClose;

    /**
     * Returns whether this frame should fast close when the default dispose is invoked.
     *
     * @return this frame should fast close when the default dispose is invoked
     */
    public boolean isShouldFastClose() {
        return shouldFastClose;
    }

    /**
     * Sets whether this frame should fast close when the default dispose is invoked.
     *
     * @param shouldFastClose whether this frame should fast close when the default dispose is invoked
     */
    public void setShouldFastClose(boolean shouldFastClose) {
        this.shouldFastClose = shouldFastClose;
    }

    /**
     * Disposes the frame.
     *
     * @param fastClose whether to animate the frame away or immediately dispose the frame
     */
    public synchronized void dispose(boolean fastClose) {
        String threadName = openingBracket + getTitle() + closingBracket + " Dispose Thread";
        CyderThreadRunner.submit(() -> {
            try {
                if (disposed.get()) return;

                if (closingConfirmationMessage != null) {
                    boolean exit = GetterUtil.getInstance().getConfirmation(
                            new GetConfirmationBuilder("Confirmation", closingConfirmationMessage)
                                    .setRelativeTo(this)
                                    .setDisableRelativeTo(true));

                    if (!exit) return;
                }

                disposed.set(true);

                Logger.log(LogTag.UI_ACTION, "CyderFrame disposed with fastclose="
                        + fastClose + ", getTitle=" + getTitle());

                preCloseActions.forEach(Runnable::run);
                if (!isBorderlessFrame()) tooltipMenuController.cancelAllTasks();
                if (!isBorderlessFrame()) notificationController.kill();

                killThreads();
                disableDragging();
                setDisableContentRepainting(true);

                boolean closingAnimation = UserDataManager.INSTANCE.shouldDoAnimations();
                if (isVisible() && (!fastClose && !shouldFastClose) && closingAnimation) {
                    for (float i = getOpacity() ; i >= opacityAnimationMin ; i -= opacityAnimationDelta) {
                        setOpacity(i);
                        ThreadUtil.sleep(opacityAnimationDelay);
                    }
                }

                Console.INSTANCE.removeTaskbarIcon(this);
                if (!StringUtil.isNullOrEmpty(removeFromFrameTaskbarExceptionsHash)) {
                    Console.INSTANCE.removeFrameTaskbarException(removeFromFrameTaskbarExceptionsHash);
                }

                super.dispose();
                postCloseActions.forEach(Runnable::run);
            } catch (Exception e) {
                ExceptionHandler.handle(e);
                super.dispose();
            }
        }, threadName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        dispose(autoFastClose);
    }

    /**
     * Whether to allow the frame to be relocated via dragging.
     *
     * @param relocatable whether to allow the frame to be relocated via dragging
     */
    public void setRelocatable(boolean relocatable) {
        if (relocatable) {
            enableDragging();
        } else {
            disableDragging();
        }
    }

    // -------
    // Dancing
    // -------

    /**
     * The directions for frame dancing.
     */
    private enum DancingDirection {
        INITIAL_UP,
        LEFT,
        DOWN,
        RIGHT,
        UP
    }

    /**
     * The direction the frame is currently going in its dance routine.
     */
    private DancingDirection dancingDirection = DancingDirection.INITIAL_UP;

    /**
     * Whether dancing has finished for this frame.
     */
    private boolean dancingFinished;

    /**
     * Returns whether dancing has concluded for this frame.
     *
     * @return whether dancing has concluded for this frame
     */
    public boolean isDancingFinished() {
        return dancingFinished;
    }

    /**
     * Sets whether dancing has concluded.
     *
     * @param dancingFinished whether dancing has concluded
     */
    @SuppressWarnings("SameParameterValue")
    public void setDancingFinished(boolean dancingFinished) {
        this.dancingFinished = dancingFinished;
    }

    /**
     * The increment in pixels a singular dance steps takes.
     */
    private static final int dancingIncrement = 10;

    /**
     * Takes a step in the current dancing direction for a dance routine.
     */
    public void danceStep() {
        switch (dancingDirection) {
            case INITIAL_UP -> {
                setLocation(getX(), getY() - dancingIncrement);
                if (getY() < 0) {
                    setLocation(getX(), 0);
                    dancingDirection = DancingDirection.LEFT;
                }
            }
            case LEFT -> {
                setLocation(getX() - dancingIncrement, getY());
                if (getX() < 0) {
                    setLocation(0, 0);
                    dancingDirection = DancingDirection.DOWN;
                }
            }
            case DOWN -> {
                Rectangle bounds = getMonitorBounds();
                int rectangleHeight = (int) bounds.getHeight();

                setLocation(getX(), getY() + dancingIncrement);
                if (getY() > rectangleHeight - getHeight()) {
                    setLocation(getX(), rectangleHeight - getHeight());
                    dancingDirection = DancingDirection.RIGHT;
                }
            }
            case RIGHT -> {
                Rectangle bounds = getMonitorBounds();
                int rectangleWidth = (int) bounds.getWidth();

                setLocation(getX() + dancingIncrement, getY());
                if (getX() > rectangleWidth - getWidth()) {
                    setLocation(rectangleWidth - getWidth(), getY());
                    dancingDirection = DancingDirection.UP;
                }
            }
            case UP -> {
                setLocation(getX(), getY() - dancingIncrement);
                if (getY() < 0) {
                    setLocation(getX(), 0);

                    // now dancing is done, will be reset to false in Console method
                    dancingFinished = true;
                    dancingDirection = DancingDirection.LEFT;
                }
            }
        }
    }

    /**
     * Rotates the currently content pane by the specified degrees from the top left corner.
     *
     * @param degrees the degrees to be rotated by (360deg <==> 0deg)
     */
    public void rotateBackground(int degrees) {
        ImageIcon masterIcon = unalteredBackgroundIcon;
        BufferedImage master = ImageUtil.toBufferedImage(masterIcon);
        BufferedImage rotated = ImageUtil.rotateImage(master, degrees);
        ((JLabel) getContentPane()).setIcon(new ImageIcon(rotated));
    }

    /**
     * Revalidates the title position.
     */
    public void revalidateTitlePosition() {
        if (isBorderlessFrame()) return;

        boolean leftTitleAndLeftButtons = titlePosition == TitlePosition.LEFT && topDrag.hasLeftButtons();
        boolean rightTitleAndRightButtons = titlePosition == TitlePosition.RIGHT && topDrag.hasRightButtons();
        if (leftTitleAndLeftButtons || rightTitleAndRightButtons) setTitlePosition(TitlePosition.CENTER);
    }

    /**
     * Returns whether this frame is a borderless frame meaning
     * there are no top/left/bottom/right drag labels.
     *
     * @return whether this frame is a borderless frame
     */
    public boolean isBorderlessFrame() {
        return topDrag == null;
    }

    /**
     * Performs repaint actions necessary for a borderless frame.
     */
    private void repaintBorderlessFrame() {
        Preconditions.checkState(isBorderlessFrame());

        if (getContentPane() != null) getContentPane().repaint();
        if (getTrueContentPane() != null) getTrueContentPane().repaint();

        super.repaint();
    }

    /**
     * Repaints the frame, associated shape, and objects using
     * the {@link CyderColors#getGuiThemeColor()} attribute.
     */
    @Override
    public void repaint() {
        if (isBorderlessFrame()) {
            repaintBorderlessFrame();
            return;
        }

        revalidateFrameShape();

        // Update the border covering the resize area
        contentLabel.setBorder(new LineBorder(CyderColors.getGuiThemeColor(),
                BORDER_LEN - FRAME_RESIZING_LEN, false));

        if (!isBorderlessFrame()) {
            topDrag.setBackground(CyderColors.getGuiThemeColor());
            bottomDrag.setBackground(CyderColors.getGuiThemeColor());
            leftDrag.setBackground(CyderColors.getGuiThemeColor());
            rightDrag.setBackground(CyderColors.getGuiThemeColor());

            topDragCover.setBackground(CyderColors.getGuiThemeColor());
            bottomDragCover.setBackground(CyderColors.getGuiThemeColor());
            leftDragCover.setBackground(CyderColors.getGuiThemeColor());
            rightDragCover.setBackground(CyderColors.getGuiThemeColor());

            topDrag.repaint();
            leftDrag.repaint();
            bottomDrag.repaint();
            rightDrag.repaint();
        }

        getContentPane().repaint();
        getTrueContentPane().repaint();

        if (menuLabel != null) {
            menuLabel.setBackground(CyderColors.getGuiThemeColor());
        }

        super.repaint();
    }

    /**
     * Sets the size of this frame ensuring that the sizing is not below
     * {@link CyderFrame#minimumWidth} by {@link CyderFrame#minimumHeight}
     *
     * @param width  width of frame
     * @param height height of frame
     */
    @Override
    public void setSize(int width, int height) {
        Preconditions.checkArgument(width >= minimumWidth);
        Preconditions.checkArgument(height >= minimumHeight);

        width = attemptInitialWidthCorrectionIfEnabled(width);
        height = attemptInitialHeightCorrectionIfEnabled(height);

        boolean sameSizes = this.width == width && this.height == height;
        super.setSize(width, height);
        this.width = width;
        this.height = height;

        postSetSizeSetBounds(sameSizes);
    }

    /**
     * Sets the bounds of the CyderFrame and refreshes all components on the frame.
     */
    @Override
    public void setBounds(int x, int y, int width, int height) {
        Preconditions.checkArgument(width >= minimumWidth);
        Preconditions.checkArgument(height >= minimumHeight);

        width = attemptInitialWidthCorrectionIfEnabled(width);
        height = attemptInitialHeightCorrectionIfEnabled(height);

        boolean sameSizes = this.width == width && this.height == height;
        super.setBounds(x, y, width, height);
        this.width = width;
        this.height = height;

        postSetSizeSetBounds(sameSizes);
    }

    /**
     * Validates the provided width if {@link Props#autoCorrectInvalidFrameSizes} is enabled.
     *
     * @param width the provided width
     * @return the width value to use, possibly the same as the one provided
     */
    private static int attemptInitialWidthCorrectionIfEnabled(int width) {
        if (!Props.autoTriggerSimilarCommands.getValue()) return width;

        return Math.max(width, minimumWidth);
    }

    /**
     * Validates the provided height if {@link Props#autoCorrectInvalidFrameSizes} is enabled.
     *
     * @param height the provided height
     * @return the height value to use, possibly the same as the one provided
     */
    private static int attemptInitialHeightCorrectionIfEnabled(int height) {
        if (!Props.autoTriggerSimilarCommands.getValue()) return height;

        return Math.max(height, minimumHeight);
    }

    /**
     * The actions to invoke following a {@link #setSize(int, int)} or {@link #setBounds(int, int, int, int)} call.
     *
     * @param sameSize whether the new size is the same as the currently set size
     */
    private void postSetSizeSetBounds(boolean sameSize) {
        revalidateCardinalDragLabels();
        revalidateFrameShape();
        if (sameSize) return;
        revalidateLayout();

        if (UiUtil.notNullAndVisible(menuLabel)) {
            generateMenu();
            menuLabel.setLocation(menuAnimateToPoint);
            menuLabel.setVisible(true);
        }

        correctTitleLength();
        notificationController.revalidateCurrentNotificationPosition();
    }

    /**
     * Revalidates the drag labels, covers, and offsets for standard frames.
     * This method has no effect on borderless frames.
     */
    private void revalidateCardinalDragLabels() {
        if (isBorderlessFrame()) return;

        topDrag.setWidth(width - 2 * FRAME_RESIZING_LEN);
        topDrag.setHeight(CyderDragLabel.DEFAULT_HEIGHT - FRAME_RESIZING_LEN);
        topDragCover.setBounds(0, 0, width, FRAME_RESIZING_LEN);

        leftDrag.setWidth(BORDER_LEN - FRAME_RESIZING_LEN);
        leftDrag.setHeight(height - CyderDragLabel.DEFAULT_HEIGHT - FRAME_RESIZING_LEN);
        leftDragCover.setBounds(0, 0, FRAME_RESIZING_LEN, height);

        rightDrag.setWidth(BORDER_LEN - FRAME_RESIZING_LEN);
        rightDrag.setHeight(height - CyderDragLabel.DEFAULT_HEIGHT - FRAME_RESIZING_LEN);
        rightDragCover.setBounds(width - FRAME_RESIZING_LEN, 0, FRAME_RESIZING_LEN, height);

        bottomDrag.setWidth(width - FRAME_RESIZING_LEN * 2);
        bottomDrag.setHeight(BORDER_LEN - FRAME_RESIZING_LEN);
        bottomDragCover.setBounds(0, height - FRAME_RESIZING_LEN, width, FRAME_RESIZING_LEN);

        topDrag.setBounds(FRAME_RESIZING_LEN, FRAME_RESIZING_LEN,
                width - 2 * FRAME_RESIZING_LEN,
                CyderDragLabel.DEFAULT_HEIGHT - FRAME_RESIZING_LEN);
        leftDrag.setBounds(FRAME_RESIZING_LEN, CyderDragLabel.DEFAULT_HEIGHT,
                BORDER_LEN - FRAME_RESIZING_LEN,
                height - CyderDragLabel.DEFAULT_HEIGHT - FRAME_RESIZING_LEN);
        rightDrag.setBounds(width - BORDER_LEN, CyderDragLabel.DEFAULT_HEIGHT,
                BORDER_LEN - FRAME_RESIZING_LEN,
                height - CyderDragLabel.DEFAULT_HEIGHT - FRAME_RESIZING_LEN);
        bottomDrag.setBounds(FRAME_RESIZING_LEN, height - BORDER_LEN,
                width - 2 * FRAME_RESIZING_LEN,
                BORDER_LEN - FRAME_RESIZING_LEN);

        topDrag.setXOffset(FRAME_RESIZING_LEN);
        topDrag.setYOffset(FRAME_RESIZING_LEN);
        topDrag.revalidate();
        topDrag.repaint();

        leftDrag.setXOffset(FRAME_RESIZING_LEN);
        leftDrag.setYOffset(CyderDragLabel.DEFAULT_HEIGHT);
        leftDrag.revalidate();
        leftDrag.repaint();

        rightDrag.setXOffset(width - BORDER_LEN);
        rightDrag.setYOffset(CyderDragLabel.DEFAULT_HEIGHT);
        rightDrag.revalidate();
        rightDrag.repaint();

        bottomDrag.setXOffset(FRAME_RESIZING_LEN);
        bottomDrag.setYOffset(height - BORDER_LEN);
        bottomDrag.revalidate();
        bottomDrag.repaint();
    }

    /**
     * The arc length of the arc for rounded window shapes.
     */
    private static final int ROUNDED_ARC = 20;

    /**
     * Revalidates and updates the frame's shape, that of being rounded or square.
     */
    private void revalidateFrameShape() {
        if (!isUndecorated()) return;
        Shape shape = null;

        try {
            // Borderless frames are by default rounded
            boolean resizerNotPresent = cyderComponentResizer == null;
            boolean userLoggedIn = Console.INSTANCE.getUuid() != null;
            boolean roundedFramesEnabled = UserDataManager.INSTANCE.shouldDrawRoundedFrameBorders();
            if (isBorderlessFrame() || (resizerNotPresent && userLoggedIn && roundedFramesEnabled)) {
                shape = new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), ROUNDED_ARC, ROUNDED_ARC);
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        } finally {
            setShape(shape);
        }
    }

    /**
     * The gap to keep between the drag label buttons and the start/end of the title label.
     */
    private static final int titleLabelComponentGap = 10;

    /**
     * Revalidates the title label width based to ensure that the
     * most is shown but the title does not overlap any buttons.
     */
    private void correctTitleLength() {
        if (isBorderlessFrame() || topDrag.getRightButtonList() == null || isBorderlessFrame()) return;

        ImmutableList<Component> leftButtons = topDrag.getLeftButtonList();
        ImmutableList<Component> rightButtons = topDrag.getRightButtonList();

        int leftButtonsStart = Integer.MAX_VALUE;
        int leftButtonsEnd = Integer.MIN_VALUE;

        int rightButtonsStart = Integer.MAX_VALUE;
        int rightButtonsEnd = Integer.MIN_VALUE;

        for (Component leftButton : leftButtons) {
            leftButtonsStart = Math.min(leftButtonsStart, leftButton.getX());
            leftButtonsEnd = Math.max(leftButtonsEnd, leftButton.getX() + leftButton.getWidth());
        }

        for (Component rightButton : rightButtons) {
            rightButtonsStart = Math.min(rightButtonsStart, rightButton.getX());
            rightButtonsEnd = Math.max(rightButtonsEnd, rightButton.getX() + rightButton.getWidth());
        }

        int necessaryTitleWidth = StringUtil.getMinWidth(title, titleLabel.getFont());
        int necessaryTitleHeight = CyderDragLabel.DEFAULT_HEIGHT;
        int y = 0;

        // Reset default bounds, will be trimmed away below
        switch (titlePosition) {
            case LEFT -> titleLabel.setBounds(titleLabelPadding, y, necessaryTitleWidth, necessaryTitleHeight);
            case CENTER -> titleLabel.setBounds(width / 2 - necessaryTitleWidth / 2, y,
                    necessaryTitleWidth, necessaryTitleHeight);
            case RIGHT -> titleLabel.setBounds(width - titleLabelPadding - necessaryTitleWidth, y,
                    necessaryTitleWidth, necessaryTitleHeight);
        }

        if (topDrag.hasLeftButtons() && topDrag.hasRightButtons()) {
            if (titlePosition != TitlePosition.CENTER) {
                setTitlePosition(TitlePosition.CENTER, false);
            }
            if (titleLabel.getX() - titleLabelComponentGap < leftButtonsEnd
                    || titleLabel.getX() + titleLabel.getWidth() + titleLabelComponentGap > rightButtonsStart) {
                int leftDeviation = getWidth() / 2 - leftButtonsEnd - titleLabelComponentGap;
                int rightDeviation = rightButtonsStart - getWidth() / 2 - titleLabelComponentGap;
                int w = 2 * Math.min(leftDeviation, rightDeviation);
                titleLabel.setBounds(width / 2 - w / 2, y, w, necessaryTitleHeight);
            }
        } else if (topDrag.hasLeftButtons()) {
            if (titlePosition == TitlePosition.CENTER) {
                if (width / 2 - necessaryTitleWidth / 2 - titleLabelComponentGap < leftButtonsEnd) {
                    int w = 2 * (width / 2 - leftButtonsEnd - titleLabelComponentGap);
                    titleLabel.setBounds(width / 2 - w / 2, y, w, necessaryTitleHeight);
                }
            } else {
                if (width - titleLabelPadding - necessaryTitleWidth - titleLabelComponentGap < leftButtonsEnd) {
                    int w = width - titleLabelComponentGap - leftButtonsEnd - titleLabelPadding;
                    titleLabel.setBounds(width - titleLabelPadding - w, y, w, necessaryTitleHeight);
                }
            }
        } else if (topDrag.hasRightButtons()) {
            if (titlePosition == TitlePosition.CENTER) {
                if (width / 2 + necessaryTitleWidth / 2 + titleLabelComponentGap > rightButtonsStart) {
                    int w = 2 * (rightButtonsStart - titleLabelComponentGap - width / 2);
                    titleLabel.setBounds(width / 2 - w / 2, y, w, necessaryTitleHeight);
                }
            } else {
                if (titleLabelPadding + necessaryTitleWidth + titleLabelComponentGap > rightButtonsStart) {
                    int w = rightButtonsStart - titleLabelPadding - titleLabelComponentGap;
                    titleLabel.setBounds(titleLabelPadding, y, w, necessaryTitleHeight);
                }
            }
        }

        // double check to ensure title isn't bigger than the frame
        if (titleLabel.getWidth() > width - 2 * titleLabelPadding) {
            titleLabel.setBounds(titleLabelPadding, titleLabel.getY(),
                    width - 2 * titleLabelPadding, titleLabel.getHeight());
        }

        if (leftButtonsStart == Integer.MAX_VALUE
                || leftButtonsEnd == Integer.MIN_VALUE
                || rightButtonsStart == Integer.MAX_VALUE
                || rightButtonsEnd == Integer.MIN_VALUE) {
            return;
        }

        // Super rare cases the title label will still be over the buttons
        int titleLabelStart = titleLabel.getX();
        int titleLabelEnd = titleLabel.getX() + titleLabel.getWidth();

        Range<Integer> leftButtonRange = Range.closed(leftButtonsStart, leftButtonsEnd);
        Range<Integer> rightButtonRange = Range.closed(rightButtonsStart, rightButtonsEnd);

        if (leftButtonRange.contains(titleLabelStart) || rightButtonRange.contains(titleLabelEnd)) {
            titleLabel.setVisible(false);
        }
    }

    /**
     * The minimum size dimension.
     */
    private Dimension minimumFrameSize = new Dimension(minimumWidth, minimumHeight);

    /**
     * The default maximum length of a frame.
     */
    private static final int defaultMaxFrameLength = 800;

    /**
     * The maximum size of a CyderFrame.
     */
    private Dimension maximumFrameSize = new Dimension(defaultMaxFrameLength, defaultMaxFrameLength);

    /**
     * The increment to snap to on resize events.
     */
    private Dimension frameSnapSize = new Dimension(1, 1);

    /**
     * Sets the minimum window size if resizing is allowed.
     *
     * @param minSize the Dimension of the minimum allowed size
     */
    public void setMinimumSize(Dimension minSize) {
        minimumFrameSize = minSize;
        cyderComponentResizer.setMinimumSize(minimumFrameSize);
    }

    /**
     * Sets the minimum window size if resizing is allowed.
     *
     * @param width  the minimum width of the frame
     * @param height the minimum height of the frame
     */
    public void setMinimumSize(int width, int height) {
        setMinimumSize(new Dimension(width, height));
    }

    /**
     * Sets the maximum window size if resizing is allowed.
     *
     * @param maxSize the Dimension of the minimum allowed size
     */
    public void setMaximumSize(Dimension maxSize) {
        maximumFrameSize = maxSize;
        cyderComponentResizer.setMaximumSize(maximumFrameSize);
    }

    /**
     * Sets the maximum window size if resizing is allowed.
     *
     * @param width  the minimum width of the frame
     * @param height the minimum height of the frame
     */
    public void setMaximumSize(int width, int height) {
        setMaximumSize(new Dimension(width, height));
    }

    /**
     * Sets the snap size for the window if resizing is allowed.
     *
     * @param snap the dimension of the snap size
     */
    public void setSnapSize(Dimension snap) {
        frameSnapSize = snap;
        cyderComponentResizer.setSnapSize(frameSnapSize);
    }

    /**
     * Sets the snap size for the window if resizing is allowed.
     *
     * @param xLen the snap size in pixels for the x dimension
     * @param yLen the snap size in pixels for the y dimension
     */
    public void setSnapSize(int xLen, int yLen) {
        setSnapSize(new Dimension(xLen, yLen));
    }

    /**
     * Returns the minimum window size if resizing is allowed.
     *
     * @return the minimum window size if resizing is allowed
     */
    public Dimension getMinimumSize() {
        return minimumFrameSize;
    }

    /**
     * Returns the maximum window size if resizing is allowed.
     *
     * @return the maximum window size if resizing is allowed
     */
    public Dimension getMaximumSize() {
        return maximumFrameSize;
    }

    /**
     * Returns the snap size for the window if resizing is allowed.
     *
     * @return the snap size for the window if resizing is allowed
     */
    public Dimension getSnapSize() {
        return frameSnapSize;
    }

    /**
     * The component resizing object for this CyderFrame.
     */
    private CyderComponentResizer cyderComponentResizer;

    /**
     * Whether to allow background resizing on CyderFrame resize events.
     *
     * @param allowed whether to allow background resizing on CyderFrame resize events
     */
    public void setBackgroundResizing(Boolean allowed) {
        cyderComponentResizer.setBackgroundResizing(allowed);
    }

    /**
     * Sets up necessary objects needed to allow the frame to be
     * resizable such as registering the min/max sizes.
     */
    public void initializeResizing() {
        if (cyderComponentResizer != null) return;

        cyderComponentResizer = new CyderComponentResizer();
        cyderComponentResizer.registerComponent(this);
        cyderComponentResizer.setResizingAllowed(true);
        cyderComponentResizer.setMinimumSize(minimumFrameSize);
        cyderComponentResizer.setMaximumSize(maximumFrameSize);
        cyderComponentResizer.setSnapSize(getSnapSize());

        setShape(null);
    }

    /**
     * Sets whether frame resizing is allowed.
     *
     * @param allow whether frame resizing is allowed
     */
    public void setFrameResizing(boolean allow) {
        cyderComponentResizer.setResizingAllowed(allow);
    }

    /**
     * Returns whether resizing is currently permitted by this frame.
     *
     * @return whether resizing is currently permitted by this frame
     */
    public boolean isResizingAllowed() {
        return cyderComponentResizer != null && cyderComponentResizer.isResizingEnabled();
    }

    /**
     * The background icon for the frame. The image shown on the frame background is not
     * this exact image as this is the image which is copied and resized on frame resize events.
     */
    private ImageIcon unalteredBackgroundIcon;

    /**
     * Sets whether the frame is resizable via the component resizer.
     * Note, {@link #initializeResizing()} must be called prior to enabling resizing.
     *
     * @param resizable whether the frame should be resizable
     */
    @Override
    public void setResizable(boolean resizable) {
        if (cyderComponentResizer != null) cyderComponentResizer.setResizingAllowed(resizable);
        if (tooltipMenuController != null) tooltipMenuController.revalidateMenuItems();
    }

    /**
     * Refresh the background in the event of a frame size change or a background image change.
     */
    public void refreshBackground() {
        try {
            if (iconLabel == null) return;
            revalidateLayout();

            if (cyderComponentResizer != null && cyderComponentResizer.backgroundResizingEnabled()) {
                Image scaledImage = unalteredBackgroundIcon.getImage().getScaledInstance(
                        iconLabel.getWidth(), iconLabel.getHeight(), Image.SCALE_DEFAULT);
                iconLabel.setIcon(new ImageIcon(scaledImage));
            }

            revalidate();
            repaint();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Revalidates the iconLabel, iconPane, and associated CyderPanel if present.
     */
    public void revalidateLayout() {
        int w = width - 2 * FRAME_RESIZING_LEN;
        int h = height - 2 * FRAME_RESIZING_LEN;

        if (iconLabel != null) iconLabel.setBounds(FRAME_RESIZING_LEN, FRAME_RESIZING_LEN, w, h);
        if (iconPane != null) iconPane.setBounds(FRAME_RESIZING_LEN, FRAME_RESIZING_LEN, w, h);

        if (cyderPanel != null) {
            int panelWidth = getWidth() - 2 * BORDER_LEN;
            int panelHeight = getHeight() - CyderDragLabel.DEFAULT_HEIGHT - BORDER_LEN;
            cyderPanel.setBounds(BORDER_LEN, CyderDragLabel.DEFAULT_HEIGHT, panelWidth, panelHeight);
        }
    }

    /**
     * Set the background to a new image and revalidates and repaints the frame.
     *
     * @param image the buffered image for the frame's background
     */
    public void setBackground(BufferedImage image) {
        Preconditions.checkNotNull(image);

        setBackground(ImageUtil.toImageIcon(image));
    }

    /**
     * Set the background to a new icon and revalidates and repaints the frame.
     *
     * @param icon the ImageIcon for the frame's background
     */
    public void setBackground(ImageIcon icon) {
        Preconditions.checkNotNull(icon);
        if (iconLabel == null) return;

        unalteredBackgroundIcon = icon;
        Image scaledImage = unalteredBackgroundIcon.getImage()
                .getScaledInstance(iconLabel.getWidth(), iconLabel.getHeight(), Image.SCALE_DEFAULT);
        iconLabel.setIcon(new ImageIcon(scaledImage));
        int x = FRAME_RESIZING_LEN;
        int y = FRAME_RESIZING_LEN;
        int w = width - 2 * FRAME_RESIZING_LEN;
        int h = height - 2 * FRAME_RESIZING_LEN;
        iconLabel.setBounds(x, y, w, h);
        iconPane.setBounds(x, y, w, h);

        revalidate();
        repaint();
    }

    /**
     * Sets the background color of the frame's content pane.
     *
     * @param background the new color of the frame's background
     */
    @Override
    public void setBackground(Color background) {
        super.setBackground(background);
        backgroundColor = background;
        revalidate();
    }

    /**
     * Returns the background color of the contentPane.
     *
     * @return Color the background color of the contentPane
     */
    @Override
    public Color getBackground() {
        return backgroundColor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return ToStringUtil.commonUiComponentToString(this);
    }

    /**
     * Kills all threads associated with this instance.
     * Features should not be expected to function properly after this method is invoked.
     */
    private void killThreads() {
        threadsKilled.set(true);
    }

    /**
     * Whether threads have been killed.
     *
     * @return whether threads have been killed
     */
    public boolean threadsKilled() {
        return threadsKilled.get();
    }

    /**
     * Set the background of {@code this} to the current Console background.
     */
    public void replicateConsoleBackground() {
        if (Console.INSTANCE.getCurrentBackground() == null) return;
        iconLabel.setIcon(Console.INSTANCE.getCurrentBackground().generateScaledImageIcon(getWidth(), getHeight()));
    }

    /**
     * Returns the restore point for this frame.
     * See {@link UiUtil#frameNotYetDragged(CyderFrame)} to determine if this point
     * is indicative of the frame never being dragged throughout its life-cycle
     *
     * @return the restore point for this frame
     */
    public Point getRestorePoint() {
        return new Point(restoreX, restoreY);
    }

    /**
     * Sets the restore point to the provided point.
     *
     * @param point the restore point to the provided point
     */
    public void setRestorePoint(Point point) {
        Preconditions.checkNotNull(point);

        this.restoreX = (int) point.getX();
        this.restoreY = (int) point.getY();
    }

    /**
     * Whether dragging is permitted for this frame.
     *
     * @return whether dragging is permitted for this frame
     */
    public boolean isDraggingEnabled() {
        if (isBorderlessFrame()) return false;

        return topDrag.isDraggingEnabled()
                && bottomDrag.isDraggingEnabled()
                && leftDrag.isDraggingEnabled()
                && rightDrag.isDraggingEnabled();
    }

    /**
     * Disables dragging for this frame.
     * The effect is ignored if this is a borderless frame.
     */
    public void disableDragging() {
        if (isBorderlessFrame()) return;

        topDrag.disableDragging();
        bottomDrag.disableDragging();
        rightDrag.disableDragging();
        leftDrag.disableDragging();
    }

    /**
     * Enables dragging for this frame.
     * The effect is ignored if this is a borderless frame.
     */
    public void enableDragging() {
        if (isBorderlessFrame()) return;

        topDrag.enableDragging();
        bottomDrag.enableDragging();
        rightDrag.enableDragging();
        leftDrag.enableDragging();
    }

    /**
     * Sets whether dragging is enabled for this frame.
     *
     * @param allowed whether dragging is enabled for this frame
     */
    public void setDraggingEnabled(boolean allowed) {
        if (allowed) {
            enableDragging();
        } else {
            disableDragging();
        }
    }

    /**
     * Removes all pre close actions.
     */
    public void removePreCloseActions() {
        preCloseActions.clear();
    }

    /**
     * Removes all post close actions.
     */
    public void removePostCloseActions() {
        postCloseActions.clear();
    }

    /**
     * Performs the given action right before closing the frame. This action is invoked right before an animation
     * and sequential dispose call.
     *
     * @param action the action to perform before closing/disposing
     */
    public void addPreCloseAction(Runnable action) {
        Preconditions.checkNotNull(action);

        preCloseActions.add(action);
    }

    /**
     * Performs the given action right after closing the frame. This action is invoked right after an animation
     * and sequential dispose call.
     *
     * @param action the action to perform before closing/disposing
     */
    public void addPostCloseAction(Runnable action) {
        Preconditions.checkNotNull(action);

        postCloseActions.add(action);
    }

    /**
     * Displays a confirmation dialog to the user to confirm
     * whether they intended to exit the frame.
     *
     * @param message the message to display to the user
     */
    public void setClosingConfirmation(String message) {
        Preconditions.checkArgument(!StringUtil.isNullOrEmpty(message));

        closingConfirmationMessage = message;
    }

    /**
     * Removes any closing confirmation messages set.
     */
    public void removeClosingConfirmation() {
        closingConfirmationMessage = null;
    }

    // -------------
    // Pinning logic
    // -------------

    /**
     * Adds the provided {@link MouseMotionListener} to the top, bottom, left, and right drag labels.
     *
     * @param actionListener the listener to add to the top, bottom, left, and right drag labels
     */
    public void addDragListener(MouseMotionListener actionListener) {
        Preconditions.checkNotNull(actionListener);

        topDrag.addMouseMotionListener(actionListener);
        bottomDrag.addMouseMotionListener(actionListener);
        leftDrag.addMouseMotionListener(actionListener);
        rightDrag.addMouseMotionListener(actionListener);
    }

    /**
     * Adds any {@link MouseListener}s to the drag labels.
     *
     * @param mouseListener the listener to add to the drag labels
     */
    public void addDragLabelMouseListener(MouseListener mouseListener) {
        Preconditions.checkNotNull(mouseListener);

        topDrag.addMouseListener(mouseListener);
        bottomDrag.addMouseListener(mouseListener);
        leftDrag.addMouseListener(mouseListener);
        rightDrag.addMouseListener(mouseListener);
    }

    /**
     * Returns whether the frame should be pinned to the console meaning
     * the top drop label's pin button state is equal to {@link PinButton.PinState#PINNED_TO_CONSOLE}.
     *
     * @return whether the frame's pin state if equal to {@link PinButton.PinState#PINNED_TO_CONSOLE}
     */
    public boolean isPinnedToConsole() {
        CyderDragLabel dragLabel = getTopDragLabel();
        if (dragLabel == null) return false;
        return dragLabel.getPinButton().getCurrentState() == PinButton.PinState.PINNED_TO_CONSOLE;
    }

    /**
     * The relative x value of this frame to the console, used for console pin dragging actions.
     */
    private int relativeX;

    /**
     * The relative y value of this frame to the console, used for console pin dragging actions.
     */
    private int relativeY;

    /**
     * Returns the relative x of this frame to the console.
     *
     * @return the relative x of this frame to the console
     */
    public int getRelativeX() {
        return relativeX;
    }

    /**
     * Returns the relative y of this frame to the console.
     *
     * @return the relative y of this frame to the console
     */
    public int getRelativeY() {
        return relativeY;
    }

    /**
     * Sets the relative x of this frame to the console.
     *
     * @param relativeX the relative x of this frame to the console
     */
    public void setRelativeX(int relativeX) {
        this.relativeX = relativeX;
    }

    /**
     * Sets the relative y of this frame to the console.
     *
     * @param relativeY the relative y of this frame to the console
     */
    public void setRelativeY(int relativeY) {
        this.relativeY = relativeY;
    }

    /**
     * Sets the taskbar image of the CyderFrame to the provided image.
     * If {@link #dispose()} has been invoked, this call will have no effect.
     *
     * @param image the image to use for the taskbar
     */
    @Override
    public void setIconImage(Image image) {
        if (disposed.get()) return;
        super.setIconImage(Preconditions.checkNotNull(image));
    }

    /**
     * The taskbar icon border color for this CyderFrame instance.
     */
    private Color taskbarIconBorderColor;

    /**
     * Returns the taskbar border color for this cyder frame.
     *
     * @return the taskbar border color for this cyder frame
     */
    public synchronized Color getTaskbarIconBorderColor() {
        if (taskbarIconBorderColor == null) taskbarIconBorderColor = UiUtil.getTaskbarBorderColor();
        return taskbarIconBorderColor;
    }

    /**
     * The custom ImageIcon to use for the taskbar icon if enabled.
     */
    private ImageIcon customTaskbarIcon;

    /**
     * Returns the custom taskbar icon for this frame's taskbar icon.
     *
     * @return the custom taskbar icon for this frame's taskbar icon
     */
    public Optional<ImageIcon> getCustomTaskbarIcon() {
        return Optional.ofNullable(customTaskbarIcon);
    }

    /**
     * Sets the taskbar image icon to use.
     *
     * @param customTaskbarIcon the taskbar image icon to use
     */
    public void setCustomTaskbarIcon(@Nullable ImageIcon customTaskbarIcon) {
        this.customTaskbarIcon = customTaskbarIcon;
    }

    /**
     * Sets the frame's visibility attribute and adds the frame to the Console menu list.
     *
     * @param visible whether to set the frame to be visible
     */
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);

        if (visible) {
            Console.INSTANCE.addTaskbarIcon(this);
            initializePinState();
        }
    }

    /**
     * Initializes the state of the pin button based on the Console's value.
     */
    private void initializePinState() {
        if (isBorderlessFrame()) return;
        CyderFrame console = Console.INSTANCE.getConsoleCyderFrame();
        if (console == null) return;

        if (console.isAlwaysOnTop() && !console.equals(this)) {
            getTopDragLabel().getPinButton().setState(PinButton.PinState.FRAME_PINNED);
        }
    }

    // -----------
    // Debug lines
    // -----------

    /**
     * Whether drawing debug lines should be performed.
     */
    private boolean drawDebugLines;

    /**
     * The label for the horizontal line.
     */
    private JLabel debugXLabel;

    /**
     * The label for the vertical line.
     */
    private JLabel debugYLabel;

    /**
     * The image to display at the center of the debug lines.
     */
    private JLabel debugImageLabel;

    /**
     * The minor axis length of a debug line.
     */
    private static final int debugLineLength = 4;

    /**
     * The length of the debug icon.
     */
    private static final int debugIconLength = 175;

    /**
     * The stroke width for the debug icon.
     */
    private static final int debugIconStrokeWidth = 10;

    /**
     * The color to use for the debug icon.
     */
    private static final Color debugIconColor = CyderColors.navy;

    /**
     * Sets whether debug lines should be drawn for this frame.
     *
     * @param draw whether debug lines should be drawn for this frame
     */
    public void toggleDebugLines(boolean draw) {
        drawDebugLines = draw;

        if (draw) {
            Color lineColor = background == null
                    ? ColorUtil.getInverseColor(backgroundColor)
                    : ColorUtil.getDominantColorInverse(background);

            int centerX = getWidth() / 2 - debugLineLength / 2;
            int centerY = (getHeight() - BORDER_LEN - CyderDragLabel.DEFAULT_HEIGHT) / 2
                    + CyderDragLabel.DEFAULT_HEIGHT - debugLineLength / 2;

            if (Props.drawDebugIcon.getValue()) {
                debugImageLabel = UiUtil.generateNeffexLabel(debugIconLength, debugIconColor, debugIconStrokeWidth);
                debugImageLabel.setLocation(centerX - debugIconLength / 2, centerY - debugIconLength / 2);
                add(debugImageLabel);
            }

            debugXLabel = new JLabel();
            debugXLabel.setBounds(centerX, 0, debugLineLength, getHeight());
            debugXLabel.setOpaque(true);
            debugXLabel.setBackground(lineColor);
            add(debugXLabel);

            debugYLabel = new JLabel();
            debugYLabel.setBounds(0, centerY, getWidth(), debugLineLength);
            debugYLabel.setOpaque(true);
            debugYLabel.setBackground(lineColor);
            add(debugYLabel);
        } else {
            if (this.equals(SwingUtilities.getRoot(debugXLabel))) remove(debugXLabel);
            if (this.equals(SwingUtilities.getRoot(debugYLabel))) remove(debugYLabel);
            if (this.equals(SwingUtilities.getRoot(debugImageLabel))) remove(debugImageLabel);
        }

        revalidate();
        repaint();
    }

    /**
     * Returns whether debug lines should be drawn for this frame.
     *
     * @return whether debug lines should be drawn for this frame
     */
    public boolean areDebugLinesDrawn() {
        return drawDebugLines;
    }

    /**
     * Returns the integer ID of the monitor this frame is on.
     *
     * @return the integer ID of the monitor this frame is on
     */
    public int getMonitor() {
        return Integer.parseInt(getGraphicsConfiguration().getDevice()
                .getIDstring().replaceAll(CyderRegexPatterns.nonNumberRegex, ""));
    }

    /**
     * Returns the bounds of the monitor this frame is on.
     *
     * @return the bounds of the monitor this frame is on
     */
    public Rectangle getMonitorBounds() {
        return getGraphicsConfiguration().getDevice().getDefaultConfiguration().getBounds();
    }

    /**
     * Returns the center point of this frame on the current monitor.
     *
     * @return the center point of this frame on the current monitor
     */
    public Point getCenterPointOnScreen() {
        return new Point(getX() + (getWidth() / 2), getY() + (getHeight() / 2));
    }

    /**
     * Returns the center point of this frame.
     *
     * @return the center point of this frame
     */
    public Point getCenterPointOnFrame() {
        return new Point(getWidth() / 2, getHeight() / 2);
    }

    /**
     * Sets the center point of the frame on the screen to the provided point.
     *
     * @param point the center point of the frame
     */
    public void setCenterPoint(Point point) {
        checkNotNull(point);

        setLocation(point.x - getWidth() / 2, point.y - getHeight() / 2);
    }

    // -------------------------------
    // Transparency during drag events
    // -------------------------------

    /**
     * Whether the opacity should be animated in and out on drag events.
     */
    private boolean shouldAnimateOpacity = true;

    /**
     * The opacity value to set the frame to on drag events.
     */
    public static final float DRAG_OPACITY = 0.70f;

    /**
     * The default frame opacity
     */
    public static final float DEFAULT_OPACITY = 1.0f;

    /**
     * The opacity to grow/shrink by during animations.
     */
    public static final float OPACITY_DELTA = 0.01f;

    /**
     * The delay between opacity sets when transitioning.
     */
    public static final int DRAG_OPACITY_ANIMATION_DELAY = 5;

    /**
     * Whether the animater to transition the frame back to DEFAULT_OPACITY is underway.
     */
    private boolean animatingOut = false;

    /**
     * Sets the opacity of the frame to {@link CyderFrame#DRAG_OPACITY}.
     */
    public void startDragEvent() {
        if (!shouldAnimateOpacity) return;

        CyderThreadRunner.submit(() -> {
            for (float i = DEFAULT_OPACITY ; i >= DRAG_OPACITY ; i -= OPACITY_DELTA) {
                if (animatingOut) break;
                setOpacity(i);
                repaint();

                ThreadUtil.sleep(DRAG_OPACITY_ANIMATION_DELAY);
            }

            setOpacity(DRAG_OPACITY);
        }, getTitle() + " Opacity Decrement Animator");
    }

    /**
     * Sets the opacity of the frame to {@link CyderFrame#DEFAULT_OPACITY}.
     */
    public void endDragEvent() {
        if (!shouldAnimateOpacity) {
            setOpacity(DEFAULT_OPACITY);
            return;
        }

        executeEndDragEventCallbacks();

        CyderThreadRunner.submit(() -> {
            animatingOut = true;

            for (float i = DRAG_OPACITY ; i <= DEFAULT_OPACITY ; i += OPACITY_DELTA) {
                setOpacity(i);
                repaint();

                ThreadUtil.sleep(DRAG_OPACITY_ANIMATION_DELAY);
            }

            setOpacity(DEFAULT_OPACITY);

            animatingOut = false;
        }, getTitle() + " Opacity Increment Animator");
    }

    /**
     * The list of callbacks to invoke when {@link #endDragEvent()} is invoked.
     */
    private final ArrayList<Runnable> endDragEventCallbacks = new ArrayList<>();

    /**
     * Executes all callbacks registered in {@link #endDragEventCallbacks}.
     */
    private void executeEndDragEventCallbacks() {
        endDragEventCallbacks.forEach(Runnable::run);
    }

    /**
     * Adds the runnable to {@link #endDragEventCallbacks}.
     *
     * @param runnable the runnable to add
     */
    public void addEndDragEventCallback(Runnable runnable) {
        Preconditions.checkNotNull(runnable);
        Preconditions.checkArgument(!endDragEventCallbacks.contains(runnable));

        endDragEventCallbacks.add(runnable);
    }

    /**
     * Removes the runnable from {@link #endDragEventCallbacks}.
     *
     * @param runnable the runnable to remove
     */
    public void removeEndDragEventCallback(Runnable runnable) {
        Preconditions.checkNotNull(runnable);
        Preconditions.checkArgument(endDragEventCallbacks.contains(runnable));

        endDragEventCallbacks.remove(runnable);
    }

    /**
     * Returns whether the opacity should be animated on drag events.
     *
     * @return whether the opacity should be animated on drag events
     */
    public boolean shouldAnimateOpacity() {
        return shouldAnimateOpacity;
    }

    /**
     * Sets whether the opacity should be animated on drag events.
     *
     * @param shouldAnimateOpacity whether the opacity should be animated on drag events
     */
    public void setShouldAnimateOpacity(boolean shouldAnimateOpacity) {
        this.shouldAnimateOpacity = shouldAnimateOpacity;
    }

    // ----------------
    // Frame menu logic
    // ----------------

    /**
     * The menu type for the frame's menu.
     */
    private MenuType menuType = MenuType.PANEL;

    /**
     * Sets the menu type to the provided and revalidates the menu depending on the old state.
     *
     * @param currentMenuType the new menu type
     */
    public void setMenuType(MenuType currentMenuType) {
        if (currentMenuType == this.menuType) return;
        this.menuType = currentMenuType;
        revalidateMenu();
    }

    /**
     * Regenerates the menu and sets the visibility to the state before this method was invoked.
     */
    public void revalidateMenu() {
        if (menuEnabled) {
            boolean wasVisible = UiUtil.notNullAndVisible(menuLabel);
            generateMenu();
            if (wasVisible) showMenu();
        }
    }

    /**
     * Revalidates the menu and shows it only if it was visible.
     */
    public void revalidateMenuIfVisible() {
        if (UiUtil.notNullAndVisible(menuLabel)) revalidateMenu();
    }

    /**
     * Returns the frame's current menu type.
     *
     * @return the frame's current menu type
     */
    public MenuType getMenuType() {
        return menuType;
    }

    /**
     * Shows the menu and locks it in the place it was set to.
     */
    public void lockMenuOut() {
        generateMenu();
        showMenu();
        setMenuButtonShown(false);
    }

    /**
     * Ensures the menu isn't visible and cannot be triggered.
     */
    public void lockMenuIn() {
        hideMenu();
        setMenuButtonShown(false);
    }

    /**
     * The menu button for this frame.
     */
    private MenuButton menuButton;

    /**
     * Adds the menu button to the drag label.
     */
    private void addMenuButton() {
        if (menuButton != null) return;

        menuButton = new MenuButton();
        menuButton.addClickAction(() -> {
            if (menuEnabled) {
                if (menuLabel == null) generateMenu();

                if (menuLabel.isVisible()) {
                    animateMenuOut();
                } else {
                    animateMenuIn();
                }
            }
        });
        topDrag.addLeftButton(menuButton, 0);
    }

    /**
     * Removes the menu button from the drag label
     */
    private void removeMenuButton() {
        if (topDrag.hasLeftButtons()) topDrag.removeLeftButton(menuButton);
    }

    /**
     * Whether the menu is accessible via clicking on the frame painted title.
     */
    private boolean menuEnabled;

    /**
     * The label to add the menu components to.
     */
    private JLabel menuLabel;

    /**
     * The list of menu items. The listener to show/hide the menu
     * is added/removed depending on the length of this list.
     */
    private final ArrayList<MenuItem> menuItems = new ArrayList<>() {
        @Override
        public boolean add(MenuItem menuItem) {
            boolean ret = false;
            if (!menuItems.contains(menuItem)) ret = super.add(menuItem);
            if (menuItems.size() == 1) addMenuButton();
            return ret;
        }

        @Override
        public boolean remove(Object o) {
            boolean ret = super.remove(o);
            if (menuItems.isEmpty()) removeMenuButton();
            return ret;
        }
    };

    /**
     * Clears all menu items from the frame menu.
     */
    public void clearMenuItems() {
        menuItems.clear();
        removeMenuButton();
    }

    /**
     * The maximum text length allowable for menu items.
     */
    private static final int MAXIMUM_MENU_ITEM_TEXT_LENGTH = 13;

    /**
     * Removes the menu item with the provided text.
     * If multiple menu items are found with the same text,
     * the first one is removed.
     *
     * @param text the text of the menu item to remove
     */
    public void removeMenuItem(String text) {
        Preconditions.checkNotNull(text);
        Preconditions.checkArgument(!text.isEmpty());

        menuItems.removeIf(menuItem -> menuItem.label().getText().equals(text));
    }

    /**
     * Removes the menu item from the provided index.
     *
     * @param index the index of the menu item to remove
     */
    public void removeMenuItem(int index) {
        checkArgument(index >= 0);
        checkArgument(index < menuItems.size());

        menuItems.remove(index);
    }

    /**
     * Adds a new menu item to the menu and revalidates the menu.
     *
     * @param text    the label text
     * @param onClick the function to run upon clicking
     */
    public void addMenuItem(String text, Runnable onClick) {
        addMenuItem(text, onClick, null);
    }

    /**
     * Adds a new menu item to the menu and revalidates the menu.
     *
     * @param text      the text for the menu label
     * @param onClick   the function to run upon clicking
     * @param isToggled the atomic boolean used to dictate the toggled/not toggled state of the menu item if necessary
     */
    public void addMenuItem(String text, Runnable onClick, AtomicBoolean isToggled) {
        checkNotNull(text);
        checkArgument(!text.isEmpty());
        checkNotNull(onClick);

        text = text.trim();

        // Overflow
        if (text.length() > MAXIMUM_MENU_ITEM_TEXT_LENGTH) {
            text = (text.substring(0, MAXIMUM_MENU_ITEM_TEXT_LENGTH - 3).trim() + CyderStrings.dots);
        }

        JLabel newLabel = new JLabel(text);
        newLabel.setFont(CyderFonts.DEFAULT_FONT_SMALL);
        newLabel.setForeground(CyderColors.vanilla);
        newLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onClick.run();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                newLabel.setForeground(
                        isToggled != null && isToggled.get() ? menuItemForeground : menuItemHoverForeground);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                newLabel.setForeground(
                        isToggled != null && isToggled.get() ? menuItemHoverForeground : menuItemForeground);
            }
        });
        menuItems.add(new MenuItem(newLabel, isToggled));

        // Regenerate if needed
        if (UiUtil.notNullAndVisible(menuLabel)) {
            generateMenu();
            menuLabel.setVisible(true);
            menuLabel.setLocation(menuAnimateToPoint);
        }
    }

    /**
     * The foreground color for menu items.
     */
    private static final Color menuItemForeground = CyderColors.vanilla;

    /**
     * The foreground color for menu items when hovered.
     */
    private static final Color menuItemHoverForeground = CyderColors.regularRed;

    /**
     * The point at which the menu is placed when visible.
     */
    private static final Point menuAnimateToPoint = new Point(
            BORDER_LEN - FRAME_RESIZING_LEN, CyderDragLabel.DEFAULT_HEIGHT - 2);

    /**
     * The increment/decrement value when animating the frame menu.
     */
    private static final int menuAnimationInc = 2;

    /**
     * The delay between animation increments when animating the frame menu.
     */
    private static final int menuAnimationDelay = 1;

    /**
     * The frame menu width.
     */
    private static final int menuWidth = 120;

    /**
     * The height to add in addition to each menu component.
     */
    private static final int menuItemHeightPadding = 5;

    /**
     * The menu x and y padding.
     */
    private static final int menuPadding = 5;

    /**
     * The thickness of the menu label border.
     */
    private static final int menuBorderThickness = 4;

    /**
     * The offset value for setting the menu label y value.
     */
    private static final int menuYOffset = 5;

    /**
     * A CyderFrame menu item.
     * This record is to associate a label with a possible
     * AtomicBoolean which dictates the state of the menu item.
     */
    private record MenuItem(JLabel label, AtomicBoolean state) {}

    /**
     * Returns whether the menu is accessible
     *
     * @return whether the menu is accessible
     */
    public boolean isMenuEnabled() {
        return menuEnabled;
    }

    /**
     * Sets whether the menu button should be shown.
     *
     * @param showMenuButton whether the menu button should be shown
     */
    public void setMenuButtonShown(boolean showMenuButton) {
        this.menuEnabled = showMenuButton;

        if (showMenuButton) {
            addMenuButton();
        } else {
            removeMenuButton();
        }
    }

    /**
     * Shows the menu in the currently set location as defined by {@link MenuType}.
     */
    public void showMenu() {
        if (menuLabel == null) generateMenu();
        menuLabel.setLocation(menuAnimateToPoint);
        menuLabel.setVisible(true);
    }

    /**
     * Hides the menu.
     */
    public void hideMenu() {
        if (menuLabel != null) {
            menuLabel.setVisible(false);
        }
    }

    /**
     * Animates the menu label in.
     */
    private void animateMenuIn() {
        if (!menuLabel.isVisible()) generateMenu();

        String threadName = getTitle() + " menu label animator";
        CyderThreadRunner.submit(() -> {
            try {
                if (menuType == MenuType.PANEL) {
                    menuLabel.setLocation(-menuLabel.getWidth(), menuAnimateToPoint.getLocation().y);
                    menuLabel.setVisible(true);

                    for (int x = menuLabel.getX() ; x < menuAnimateToPoint.x ; x += menuAnimationInc) {
                        menuLabel.setLocation(x, menuLabel.getY());
                        ThreadUtil.sleep(menuAnimationDelay);
                    }
                } else {
                    menuLabel.setLocation(menuAnimateToPoint.x, menuAnimateToPoint.y - menuLabel.getHeight());
                    menuLabel.setVisible(true);

                    for (int y = menuLabel.getY() ; y <= menuAnimateToPoint.y ; y += menuAnimationInc) {
                        menuLabel.setLocation(menuAnimateToPoint.x, y);
                        ThreadUtil.sleep(menuAnimationDelay);
                    }
                }

                menuLabel.setLocation(menuAnimateToPoint);
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, threadName);
    }

    /**
     * Animates out the audio controls.
     */
    private void animateMenuOut() {
        checkNotNull(menuLabel);

        if (menuLabel.getX() + menuLabel.getWidth() < 0
                && menuLabel.getY() + menuLabel.getHeight() < 0) return;

        String threadName = getTitle() + " menu label animator";
        CyderThreadRunner.submit(() -> {
            try {
                if (menuType == MenuType.PANEL) {
                    for (int x = menuLabel.getX() ; x > -menuLabel.getWidth() ; x -= menuAnimationInc) {
                        menuLabel.setLocation(x, menuLabel.getY());
                        ThreadUtil.sleep(menuAnimationDelay);
                    }
                } else {
                    menuLabel.setLocation(menuAnimateToPoint.x, menuAnimateToPoint.y);
                    for (int y = menuLabel.getY() ; y >= menuAnimateToPoint.y - menuLabel.getHeight()
                            ; y -= menuAnimationInc) {
                        menuLabel.setLocation(menuAnimateToPoint.x, y);
                        ThreadUtil.sleep(menuAnimationDelay);
                    }
                }

                menuLabel.setVisible(false);
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, threadName);
    }

    /**
     * Generates the menu based off of the current menu components
     * and sets the location to the starting point in preparation for an {@link #animateMenuIn()} invocation.
     */
    private void generateMenu() {
        if (menuLabel != null) menuLabel.setVisible(false);

        menuLabel = new JLabel();
        menuLabel.setOpaque(true);
        menuLabel.setBackground(CyderColors.getGuiThemeColor());

        if (menuType == MenuType.PANEL) {
            int menuHeight = 2 * menuItemHeightPadding + (menuItems.size() * (StringUtil.getAbsoluteMinHeight(
                    String.valueOf(CyderNumbers.JENNY), CyderFonts.DEFAULT_FONT_SMALL))) + menuYOffset;

            if (menuHeight > getHeight() - topDrag.getHeight() - menuYOffset) {
                menuHeight = getHeight() - topDrag.getHeight() - menuYOffset;
            }

            menuLabel.setSize(menuWidth, menuHeight);
        } else {
            menuLabel.setSize(getWidth() - 10, StringUtil.getMinHeight(CyderStrings.JENNY,
                    CyderFonts.DEFAULT_FONT_SMALL));
        }

        menuLabel.setBorder(new LineBorder(Color.black, menuBorderThickness));

        JTextPane menuPane = UiUtil.generateJTextPaneWithInvisibleHorizontalScrollbar();
        menuPane.setEditable(false);
        menuPane.setFocusable(false);
        menuPane.setOpaque(false);
        menuPane.setAutoscrolls(false);

        CyderScrollPane menuScroll = new CyderScrollPane(menuPane);
        menuScroll.setThumbSize(5);
        menuScroll.getViewport().setOpaque(false);
        menuScroll.setFocusable(true);
        menuScroll.setOpaque(false);
        menuScroll.setAutoscrolls(false);
        menuScroll.setThumbColor(CyderColors.regularPink);
        menuScroll.setBackground(CyderColors.getGuiThemeColor());

        if (menuType == MenuType.PANEL) {
            menuScroll.setBounds(menuPadding, menuPadding, menuWidth - 2 * menuPadding,
                    menuLabel.getHeight() - 2 * menuPadding);

            menuScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            menuScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        } else {
            menuScroll.setBounds(menuPadding, menuPadding,
                    getWidth() - menuPadding * 2 - 10, menuLabel.getHeight() - menuPadding * 2);

            menuScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            menuScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        }

        menuLabel.add(menuScroll);

        UiUtil.setJTextPaneDocumentAlignment(menuPane, UiUtil.JTextPaneAlignment.LEFT);

        StringUtil printingUtil = new StringUtil(new CyderOutputPane(menuPane));
        menuPane.setText("");

        // update externally synced label foregrounds
        for (MenuItem menuItem : menuItems) {
            if (menuItem.state() != null) {
                menuItem.label().setForeground(menuItem.state().get()
                        ? CyderColors.regularRed : CyderColors.vanilla);
            }
        }

        if (menuType == MenuType.PANEL) {
            for (int i = 0 ; i < menuItems.size() ; i++) {
                printingUtil.printComponent(menuItems.get(i).label());

                if (i != menuItems.size() - 1) {
                    printingUtil.print(CyderStrings.newline);
                }
            }
        } else {
            for (int i = 0 ; i < menuItems.size() ; i++) {
                printingUtil.printComponent(menuItems.get(i).label());

                if (i != menuItems.size() - 1) {
                    printingUtil.print(StringUtil.generateSpaces(4));
                }
            }
        }

        menuPane.setCaretPosition(0);
        menuLabel.setVisible(false);
        menuLabel.setLocation(-menuWidth, menuAnimateToPoint.y);
        getIconPane().add(menuLabel, JLayeredPane.MODAL_LAYER);
    }

    /**
     * Sets the frame's location relative to the dominant frame,
     * the visibility to true, and fades in the frame.
     */
    public void finalizeAndShow() {
        finalizeAndShow(getDominantFrame());
    }

    /**
     * Invokes {@link #finalizeAndShow(Point)} using {@link #getLocation()} as the point.
     */
    public void finalizeAndShowCurrentLocation() {
        finalizeAndShow(new Point(getX() + getWidth() / 2, getY() + getHeight() / 2));
    }

    /**
     * Sets the frame's location relative to the provided component,
     * the visibility to true, and fades in the frame.
     *
     * @param component the component to set the frame relative to
     */
    public void finalizeAndShow(Component component) {
        setLocationRelativeTo(component);
        innerFinalizeAndShow();
    }

    /**
     * Sets the frame's location to the provided point,
     * the visibility to true, and fades in the frame.
     *
     * @param centerPoint the point to set the center of the frame at
     */
    public void finalizeAndShow(Point centerPoint) {
        Preconditions.checkNotNull(centerPoint);
        setCenterPoint(centerPoint);
        innerFinalizeAndShow();
    }

    /**
     * The actions to invoke after other necessary calls of different finalize and show invocations.
     */
    private void innerFinalizeAndShow() {
        setOpacity(opacityAnimationMin);
        toFront();
        setVisible(true);

        CyderThreadRunner.submit(() -> {
            for (float i = opacityAnimationMin ; i < opacityAnimationMax ; i += opacityAnimationDelta) {
                setOpacity(i);
                ThreadUtil.sleep(opacityAnimationDelay);
            }

            setOpacity(opacityAnimationMax);
        }, FADE_IN_ANIMATION_THREAD_NAME);
    }

    /**
     * Returns the current dominant frame for Cyder.
     *
     * @return the current dominant frame for Cyder
     */
    @Nullable
    public static CyderFrame getDominantFrame() {
        if (!Console.INSTANCE.isClosed()) {
            CyderFrame referenceFrame = Console.INSTANCE.getConsoleCyderFrame();
            return (referenceFrame != null && referenceFrame.getState() != ICONIFIED) ? referenceFrame : null;
        } else if (!LoginHandler.isLoginFrameClosed()
                && LoginHandler.getLoginFrame() != null
                && LoginHandler.getLoginFrame().isVisible()) {
            return LoginHandler.getLoginFrame();
        }

        return null;
    }

    /**
     * Sets the console to a provided ScreenPosition and moves any pinned CyderFrame windows with it.
     *
     * @param screenPosition the screen position to move the frame to
     */
    public void setLocationOnScreen(ScreenPosition screenPosition) {
        Rectangle ourMonitorBounds = getMonitorBounds();

        Insets monitorInsets = UiUtil.getDefaultScreenInsets();
        int horizontalInsetsAvg = (monitorInsets.left + monitorInsets.right) / 2;
        int verticalInsetsAvg = (monitorInsets.top + monitorInsets.bottom) / 2;

        switch (screenPosition) {
            case TRUE_CENTER, null -> setLocation(
                    ourMonitorBounds.x + ourMonitorBounds.width / 2 - getWidth() / 2,
                    ourMonitorBounds.y + ourMonitorBounds.height / 2 - getHeight() / 2);
            case CENTER -> setLocation(
                    ourMonitorBounds.x + ourMonitorBounds.width / 2 - getWidth() / 2 + horizontalInsetsAvg,
                    ourMonitorBounds.y + ourMonitorBounds.height / 2 - getHeight() / 2 + verticalInsetsAvg
            );
            case TRUE_TOP_LEFT -> setLocation(
                    ourMonitorBounds.x,
                    ourMonitorBounds.y);
            case TOP_LEFT -> setLocation(
                    ourMonitorBounds.x + monitorInsets.left,
                    ourMonitorBounds.y + monitorInsets.top
            );
            case TRUE_TOP_RIGHT -> setLocation(
                    ourMonitorBounds.x + ourMonitorBounds.width - getWidth(),
                    ourMonitorBounds.y);
            case TOP_RIGHT -> setLocation(
                    ourMonitorBounds.x + ourMonitorBounds.width - getWidth() - monitorInsets.right,
                    ourMonitorBounds.y + monitorInsets.top
            );
            case TRUE_BOTTOM_LEFT -> setLocation(
                    ourMonitorBounds.x,
                    ourMonitorBounds.y + ourMonitorBounds.height - getHeight());
            case BOTTOM_LEFT -> setLocation(
                    ourMonitorBounds.x + monitorInsets.left,
                    ourMonitorBounds.y + ourMonitorBounds.height - getHeight() - monitorInsets.bottom
            );
            case TRUE_BOTTOM_RIGHT -> setLocation(
                    ourMonitorBounds.x + ourMonitorBounds.width - getWidth(),
                    ourMonitorBounds.y + ourMonitorBounds.height - getHeight());

            case BOTTOM_RIGHT -> setLocation(
                    ourMonitorBounds.x + ourMonitorBounds.width - getWidth() - monitorInsets.right,
                    ourMonitorBounds.y + ourMonitorBounds.height - getHeight() - monitorInsets.bottom);
        }
    }

    /**
     * Sets the default color of the title label.
     *
     * @param color the default color of the title label
     */
    public void setTitleLabelColor(Color color) {
        Preconditions.checkNotNull(color);

        titleLabelColor = color;
        titleLabel.setForeground(color);
        titleLabel.repaint();
    }

    /**
     * Returns the foreground color used for the title label.
     *
     * @return the foreground color used for the title label
     */
    public Color getTitleLabelColor() {
        return titleLabelColor;
    }

    /**
     * Returns the font for the title label.
     *
     * @return the font for the title label
     */
    public Font getTitleLabelFont() {
        return titleLabelFont;
    }

    /**
     * Sets font for the title label.
     *
     * @param titleLabelFont font for the title label
     */
    public void setTitleLabelFont(Font titleLabelFont) {
        Preconditions.checkNotNull(titleLabelFont);

        this.titleLabelFont = titleLabelFont;
        titleLabel.setFont(titleLabelFont);
        titleLabel.repaint();
    }

    /**
     * Returns whether this frame is the console frame.
     *
     * @return whether this frame is the console frame
     */
    public boolean isConsole() {
        return equals(Console.INSTANCE.getConsoleCyderFrame());
    }

    /**
     * Resets the dancing members and state variables.
     */
    public void resetDancing() {
        this.dancingDirection = DancingDirection.INITIAL_UP;
        setDancingFinished(false);
    }

    /**
     * Whether this frame should auto fast-close.
     */
    private boolean autoFastClose = false;

    /**
     * Returns whether this frame will fast close on disposal.
     *
     * @return whether this frame will fast close on disposal
     */
    public boolean isAutoFastClose() {
        return autoFastClose;
    }

    /**
     * Sets whether this frame will fast close on disposal.
     *
     * @param autoFastClose whether this frame will fast close on disposal
     */
    public void setAutoFastClose(boolean autoFastClose) {
        this.autoFastClose = autoFastClose;
    }

    /**
     * Returns the tooltip menu controller if present.
     *
     * @return the tooltip menu controller if present
     */
    public TooltipMenuController getTooltipMenuController() {
        Preconditions.checkState(!isBorderlessFrame());

        return tooltipMenuController;
    }
}