package cyder.ui.frame;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.annotations.ForReadability;
import cyder.console.Console;
import cyder.console.ConsoleConstants;
import cyder.constants.*;
import cyder.enums.Direction;
import cyder.getter.GetConfirmationBuilder;
import cyder.getter.GetterUtil;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.InformHandler;
import cyder.layouts.CyderLayout;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.login.LoginHandler;
import cyder.math.AngleUtil;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;
import cyder.time.TimeUtil;
import cyder.ui.CyderComponentResizer;
import cyder.ui.CyderPanel;
import cyder.ui.drag.CyderDragLabel;
import cyder.ui.drag.button.MenuButton;
import cyder.ui.drag.button.PinButton;
import cyder.ui.pane.CyderOutputPane;
import cyder.ui.pane.CyderScrollPane;
import cyder.user.UserUtil;
import cyder.utils.*;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static cyder.constants.CyderStrings.*;

/**
 * CyderFrame component is the primary backbone that all Cyder lies on.
 */
public class CyderFrame extends JFrame {
    /**
     * The minimum allowable width for a CyderFrame.
     */
    public static final int MINIMUM_WIDTH = 200;

    /**
     * The maximum allowable height for a CyderFrame.
     */
    public static final int MINIMUM_HEIGHT = 100;

    /**
     * The font used for the title label (typically equivalent to agencyFB22).
     */
    public static final Font DEFAULT_FRAME_TITLE_FONT = new Font("Agency FB", Font.BOLD, 22);

    /**
     * The font used for CyderFrame notifications (typically equivalent to segoe20)
     */
    public static final Font NOTIFICATION_FONT = new Font("Segoe UI Black", Font.BOLD, 20);

    /**
     * The maximum allowable frame dimension to notification dimension before
     * the notification is turned into a popup pane.
     */
    public static final float NOTIFICATION_TO_FRAME_RATIO = 0.9f;

    /**
     * The possible title positions for a CyderFrame title.
     */
    public enum TitlePosition {
        LEFT,
        CENTER,
        RIGHT,
    }

    /**
     * The possible frame types for a CyderFrame.
     */
    public enum FrameType {
        DEFAULT,
        INPUT_GETTER,
        POPUP,
    }

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
     * Whether threads that were spawned by this instance of CyderFrame have been killed yet.
     * Examples include notifications and dancing.
     */
    private boolean threadsKilled;

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
    private int restoreX = Integer.MAX_VALUE;

    /**
     * The y position of the frame to set to after frame de-iconification actions.
     */
    private int restoreY = Integer.MIN_VALUE;

    /**
     * The title of the CyderFrame controlled by the position enum.
     */
    private JLabel titleLabel;

    /**
     * The text displayed on the title label.
     */
    private String title = "CyderFrame";

    /**
     * The "content pane" of the CyderFrame. This is what is returned
     * when a getContentPane() call is invoked and is what components are added to.
     */
    private final JLabel iconLabel;

    /**
     * The true content pane of the CyderFrame. This is necessary so we can do layering
     * between the components, the background, the background image, notifications,
     * drag labels, etc.
     */
    private final JLayeredPane contentLabel;

    /**
     * Another layered pane that the content label is added to for layering purposes.
     */
    private final JLayeredPane iconPane;

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
     * The list of notifications that have yet to be pulled and notified via this frame.
     */
    private final ArrayList<NotificationBuilder> notificationList = new ArrayList<>();

    /**
     * The area exposed to allow frame resizing. The maximum is 5 since
     * 5 is the border of the frame.
     */
    public static final int FRAME_RESIZING_LEN = 2;

    /**
     * The size of the border drawn around the frame.
     */
    public static final int BORDER_LEN = 5;

    public static final int DEFAULT_FRAME_LEN = 400;

    /**
     * The default CyderFrame dimension.
     */
    public static final Dimension DEFAULT_DIMENSION = new Dimension(DEFAULT_FRAME_LEN, DEFAULT_FRAME_LEN);

    /**
     * Allowable indices to add components to the contentLabel
     * which is a JLayeredPane and the content pane.
     */
    public static final ImmutableList<Integer> allowableContentLabelIndices = ImmutableList.of(
            JLayeredPane.DRAG_LAYER, /* Drag labels */
            JLayeredPane.POPUP_LAYER /* Notifications */
    );

    /**
     * Constructs a new CyderFrame object with default dimensions.
     */
    public CyderFrame() {
        this(DEFAULT_DIMENSION);
    }

    /**
     * Constructs a new CyderFrame with the provided size.
     * Note that if the width or height falls below the minimum size, the returned
     * frame object will have the minimum width and/or height, not the width and
     * height provided.
     *
     * @param size the size of the CyderFrame
     */
    public CyderFrame(Dimension size) {
        this(size.width, size.height);
    }

    /**
     * Constructs a new CyderFrame object with the specified width and height.
     * Note that if the width or height falls below the minimum size, the returned
     * frame object will have the minimum width and/or height, not the width and
     * height provided.
     *
     * @param width  the specified width of the CyderFrame
     * @param height the specified height of the CyderFrame
     */
    public CyderFrame(int width, int height) {
        this(width, height, CyderIcons.defaultBackground);
    }

    /**
     * Constructs a new CyderFrame object with the specified width and height.
     * Note that if the width or height falls below the minimum size, the returned
     * frame object will have the minimum width and/or height, not the width and
     * height provided.
     *
     * @param width  the width of the CyderFrame
     * @param height the height of the CyderFrame
     * @param color  the color of the content pane background
     */
    public CyderFrame(int width, int height, Color color) {
        this(width, height, ImageUtil.imageIconFromColor(color,
                Math.max(MINIMUM_WIDTH, width), Math.max(MINIMUM_HEIGHT, height)));
    }

    /**
     * Constructs a new CyderFrame object with the specified width and height.
     * Note that if the width or height falls below the minimum size, the returned
     * frame object will have the minimum width and/or height, not the width and
     * height provided.
     *
     * @param width      the specified width of the cyder frame
     * @param height     the specified height of the cyder frame
     * @param background the specified background image (you may
     *                   enable rescaling of this background on frame resize events should you choose)
     */
    public CyderFrame(int width, int height, ImageIcon background) {
        checkNotNull(background);

        Dimension correctedSize = validateRequestedSize(width, height);
        width = correctedSize.width;
        height = correctedSize.height;

        this.width = width;
        this.height = height;

        // Ensure background same size as width and height
        if (width != background.getIconWidth() || height != background.getIconHeight()) {
            background = ImageUtil.resizeImage(background, width, height);
        }
        this.background = background;
        currentMasterIcon = background;

        taskbarIconBorderColor = UiUtil.getTaskbarBorderColor();

        setSize(new Dimension(width, height));
        setResizable(false);
        setUndecorated(true);
        setBackground(CyderColors.vanilla);
        setIconImage(CyderIcons.CYDER_ICON.getImage());

        // Ensure dispose actions are always invoked
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose(true);
            }
        });

        // True content pane
        contentLabel = new JLayeredPane() {
            @Override
            public Component add(Component comp, int index) {
                return super.add(comp, allowableContentLabelIndices.contains(index) ? index : 0);
            }
        };
        contentLabel.setFocusable(false);

        // getContentPane() result
        iconLabel = new JLabel() {
            @Override
            public void repaint() {
                if (!disableContentRepainting) {
                    super.repaint();
                }
            }
        };
        int iconLabelWidth = width - 2 * FRAME_RESIZING_LEN;
        int iconLabelHeight = height - 2 * FRAME_RESIZING_LEN;
        iconLabel.setBounds(FRAME_RESIZING_LEN, FRAME_RESIZING_LEN, iconLabelWidth, iconLabelHeight);
        iconLabel.setIcon(background);
        iconLabel.setFocusable(false);

        iconPane = new JLayeredPane();
        iconPane.setBounds(FRAME_RESIZING_LEN, FRAME_RESIZING_LEN, iconLabelWidth, iconLabelHeight);
        iconPane.add(iconLabel, JLayeredPane.DEFAULT_LAYER);
        iconPane.setFocusable(false);
        contentLabel.add(iconPane, JLayeredPane.DEFAULT_LAYER);

        int contentLabelWidth = 3;
        contentLabel.setBorder(new LineBorder(CyderColors.getGuiThemeColor(), contentLabelWidth, false));
        setContentPane(contentLabel);

        topDrag = new CyderDragLabel(width - 2 * FRAME_RESIZING_LEN,
                CyderDragLabel.DEFAULT_HEIGHT - FRAME_RESIZING_LEN,
                this, CyderDragLabel.Type.TOP);
        topDrag.setBounds(FRAME_RESIZING_LEN, FRAME_RESIZING_LEN,
                iconLabelWidth, CyderDragLabel.DEFAULT_HEIGHT - FRAME_RESIZING_LEN);
        topDrag.setXOffset(FRAME_RESIZING_LEN);
        topDrag.setYOffset(FRAME_RESIZING_LEN);
        contentLabel.add(topDrag, JLayeredPane.DRAG_LAYER);
        topDrag.setFocusable(false);

        topDragCover = new JLabel();
        topDragCover.setBounds(0, 0, width, FRAME_RESIZING_LEN);
        topDragCover.setBackground(CyderColors.getGuiThemeColor());
        topDragCover.setOpaque(true);
        contentLabel.add(topDragCover, JLayeredPane.DRAG_LAYER);

        leftDrag = new CyderDragLabel(BORDER_LEN - FRAME_RESIZING_LEN,
                height - FRAME_RESIZING_LEN - CyderDragLabel.DEFAULT_HEIGHT,
                this, CyderDragLabel.Type.LEFT);
        leftDrag.setBounds(FRAME_RESIZING_LEN, CyderDragLabel.DEFAULT_HEIGHT,
                BORDER_LEN - FRAME_RESIZING_LEN,
                height - CyderDragLabel.DEFAULT_HEIGHT - FRAME_RESIZING_LEN);
        leftDrag.setXOffset(FRAME_RESIZING_LEN);
        leftDrag.setYOffset(CyderDragLabel.DEFAULT_HEIGHT);
        contentLabel.add(leftDrag, JLayeredPane.DRAG_LAYER);
        leftDrag.setFocusable(false);
        leftDrag.setRightButtonList(null);

        leftDragCover = new JLabel();
        leftDragCover.setBounds(0, 0, FRAME_RESIZING_LEN, height);
        leftDragCover.setBackground(CyderColors.getGuiThemeColor());
        leftDragCover.setOpaque(true);
        contentLabel.add(leftDragCover, JLayeredPane.DRAG_LAYER);

        rightDrag = new CyderDragLabel(BORDER_LEN - FRAME_RESIZING_LEN,
                height - FRAME_RESIZING_LEN - CyderDragLabel.DEFAULT_HEIGHT,
                this, CyderDragLabel.Type.RIGHT);
        rightDrag.setBounds(width - BORDER_LEN, CyderDragLabel.DEFAULT_HEIGHT,
                BORDER_LEN - FRAME_RESIZING_LEN,
                height - CyderDragLabel.DEFAULT_HEIGHT - FRAME_RESIZING_LEN);
        rightDrag.setXOffset(width - BORDER_LEN);
        rightDrag.setYOffset(CyderDragLabel.DEFAULT_HEIGHT);
        contentLabel.add(rightDrag, JLayeredPane.DRAG_LAYER);
        rightDrag.setFocusable(false);
        rightDrag.setRightButtonList(null);

        rightDragCover = new JLabel();
        rightDragCover.setBounds(width - FRAME_RESIZING_LEN, 0, FRAME_RESIZING_LEN, height);
        rightDragCover.setBackground(CyderColors.getGuiThemeColor());
        rightDragCover.setOpaque(true);
        contentLabel.add(rightDragCover, JLayeredPane.DRAG_LAYER);

        bottomDrag = new CyderDragLabel(width - FRAME_RESIZING_LEN * FRAME_RESIZING_LEN,
                BORDER_LEN - FRAME_RESIZING_LEN,
                this, CyderDragLabel.Type.BOTTOM);
        bottomDrag.setBounds(FRAME_RESIZING_LEN, height - BORDER_LEN,
                width - 2 * FRAME_RESIZING_LEN, BORDER_LEN - FRAME_RESIZING_LEN);
        bottomDrag.setXOffset(FRAME_RESIZING_LEN);
        bottomDrag.setYOffset(height - BORDER_LEN);
        contentLabel.add(bottomDrag, JLayeredPane.DRAG_LAYER);
        bottomDrag.setFocusable(false);
        bottomDrag.setRightButtonList(null);

        bottomDragCover = new JLabel();
        bottomDragCover.setBounds(0, height - FRAME_RESIZING_LEN, width, FRAME_RESIZING_LEN);
        bottomDragCover.setBackground(CyderColors.getGuiThemeColor());
        bottomDragCover.setOpaque(true);
        contentLabel.add(bottomDragCover, JLayeredPane.DRAG_LAYER);

        titleLabel = new JLabel();
        titleLabel.setFont(titleLabelFont);
        titleLabel.setForeground(titleLabelColor);
        titleLabel.setOpaque(false);
        titleLabel.setFocusable(false);
        titleLabel.setVisible(true);
        topDrag.add(titleLabel);

        threadsKilled = false;

        setFrameType(frameType);

        revalidateFrameShape();

        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    // -----------------------------
    // Borderless frame constructors
    // -----------------------------

    /**
     * Generates and returns a borderless CyderFrame.
     * A drag listener is already attached to this but
     * the caller needs to handle how the frame will be disposed.
     *
     * @param width  the width of the frame
     * @param height the height of the frame
     * @return the borderless frame
     */
    public static CyderFrame generateBorderlessFrame(int width, int height, Color backgroundColor) {
        return new CyderFrame(new Dimension(width, height), backgroundColor);
    }

    /**
     * Generates and returns a borderless CyderFrame.
     * A drag listener is already attached to this but
     * the caller needs to handle how the frame will be disposed.
     *
     * @param dimension the dimension of the frame to construct
     * @return the borderless frame
     */
    public static CyderFrame generateBorderlessFrame(Dimension dimension, Color backgroundColor) {
        return new CyderFrame(dimension, backgroundColor);
    }

    /**
     * Constructs a CyderFrame object that exists without
     * surrounding drag labels, the title label, and the button list.
     *
     * @param size       the size of the frame to construct
     * @param background the background color of the borderless frame
     */
    private CyderFrame(Dimension size, Color background) {
        this.width = size.width;
        this.height = size.height;

        setSize(new Dimension(width, height));
        setResizable(false);
        setUndecorated(true);
        setBackground(Color.BLACK);
        setIconImage(CyderIcons.CYDER_ICON.getImage());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        contentLabel = new JLayeredPane() {
            @Override
            public Component add(Component comp, int index) {
                if (index == JLayeredPane.DRAG_LAYER) {
                    return super.add(comp, index);
                } else if (index == JLayeredPane.POPUP_LAYER) {
                    return super.add(comp, index);
                }

                return super.add(comp, 0);
            }
        };
        contentLabel.setFocusable(false);

        // this.getContentPane() return
        iconLabel = new JLabel() {
            @Override
            public void repaint() {
                if (!disableContentRepainting) {
                    super.repaint();
                }
            }
        };
        iconLabel.setBounds(FRAME_RESIZING_LEN, FRAME_RESIZING_LEN,
                width - 2 * FRAME_RESIZING_LEN,
                height - 2 * FRAME_RESIZING_LEN);
        iconLabel.setFocusable(false);

        iconPane = new JLayeredPane();
        iconPane.setBounds(FRAME_RESIZING_LEN, FRAME_RESIZING_LEN,
                width - 2 * FRAME_RESIZING_LEN, height - 2 * FRAME_RESIZING_LEN);
        iconPane.add(iconLabel, JLayeredPane.DEFAULT_LAYER);
        iconPane.setFocusable(false);
        contentLabel.add(iconPane, JLayeredPane.DEFAULT_LAYER);
        setContentPane(contentLabel);

        masterDrag = new CyderDragLabel(width, height, this, CyderDragLabel.Type.FULL);
        masterDrag.setRightButtonList(null);
        masterDrag.setBackground(background);
        masterDrag.setBounds(0, 0, width, height);
        contentLabel.add(masterDrag, JLayeredPane.DRAG_LAYER);
        masterDrag.setFocusable(false);

        contentLabel.add(masterDrag);

        threadsKilled = false;

        revalidateFrameShape();
        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * The master drag label for borderless frames.
     */
    private CyderDragLabel masterDrag;

    /**
     * Returns the borderless drag label.
     *
     * @return the borderless drag label
     */
    public CyderDragLabel getBorderlessDrag() {
        Preconditions.checkArgument(isBorderlessFrame());

        return masterDrag;
    }

    // ----------------
    // end constructors
    // ----------------

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
    // frame layouts
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

        if (this.titlePosition == null
                || this.titlePosition == newPosition
                || isBorderlessFrame()) {
            return;
        }

        boolean leftButtons = topDrag.getLeftButtonList().size() > 0;
        boolean rightButtons = topDrag.getRightButtonList().size() > 0;

        if (newPosition == TitlePosition.LEFT && leftButtons) {
            throw new IllegalStateException("Cannot place title position to the left"
                    + " as the left button list contains buttons");
        }

        if (newPosition == TitlePosition.RIGHT && rightButtons) {
            throw new IllegalStateException("Cannot place title position to the right"
                    + " as the right button list contains buttons");
        }

        int dragWidth = topDrag.getWidth();
        int dragHeight = topDrag.getHeight();

        Font titleFont = titleLabel.getFont();
        int titleWidth = StringUtil.getAbsoluteMinWidth(title, titleFont);
        int titleHeight = StringUtil.getAbsoluteMinHeight(title, titleFont);

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

                if (animateFrom < animateTo) {
                    CyderThreadRunner.submit(() -> {
                        for (int x = animateFrom ; x <= animateTo ; x++) {
                            titleLabel.setBounds(x, y, titleWidth, titleHeight);
                            ThreadUtil.sleep(animationDelay);
                        }

                        titlePosition = newPosition;
                        revalidateTitleLocationAlignmentAndLength();
                    }, "Title Position Animator");
                } else {
                    CyderThreadRunner.submit(() -> {
                        for (int x = animateFrom ; x >= animateTo ; x--) {
                            titleLabel.setBounds(x, y, titleWidth, titleHeight);
                            ThreadUtil.sleep(animationDelay);
                        }

                        titlePosition = newPosition;
                        revalidateTitleLocationAlignmentAndLength();
                    }, "Title Position Animator");
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
        if (topDrag == null) return;

        int dragWidth = topDrag.getWidth();
        int dragHeight = topDrag.getHeight();

        int titleWidth = titleLabel.getWidth();
        int titleHeight = titleLabel.getHeight();

        int y = Math.max(dragHeight / 2 - titleHeight / 2, 0);

        switch (titlePosition) {
            case LEFT -> titleLabel.setLocation(titleLabelPadding, y);
            case RIGHT -> titleLabel.setLocation(width - titleWidth - titleLabelPadding, y);
            case CENTER -> titleLabel.setLocation(dragWidth / 2 - titleWidth / 2, y);
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

    private static final int PIN_BUTTON_DEFAULT_INDEX = 1;
    private static final int MINIMIZE_BUTTON_DEFAULT_INDEX = 0;

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
     * Whether to paint the title label on the top drag label when {@link #setTitle(String)} is called.
     */
    private boolean paintCyderFrameTitleOnSuperCall = true;

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
        Preconditions.checkArgument(!title.isEmpty());

        title = StringUtil.getTrimmedText(StringUtil.removeNonAscii(title));

        if (paintSuperTitle) {
            super.setTitle(title);
            this.title = title;
        } else {
            super.setTitle("");
        }

        if (paintCyderFrameTitleOnSuperCall) {
            setCyderFrameTitle(title);
        }

        correctTitleLength();

        if (!isConsole() && !Console.INSTANCE.isClosed()) {
            Console.INSTANCE.revalidateMenu();
        }
    }

    /**
     * Sets the painted title on the top drag label to the provided text.
     *
     * @param title the painted title text
     */
    public void setCyderFrameTitle(String title) {
        Preconditions.checkNotNull(title);
        title = StringUtil.getTrimmedText(StringUtil.removeNonAscii(title));

        if (titleLabel == null) return;

        titleLabel.setText(title);
        correctTitleLength();
    }

    // -------------
    // Notifications
    // -------------

    /**
     * The notification that is currently being displayed.
     */
    private CyderNotification currentNotification;

    /**
     * Whether the notification thread has been started for this frame.
     */
    private boolean notificationCheckerStarted;

    /**
     * Returns the current notification.
     *
     * @return the current notification
     */
    public CyderNotification getCurrentNotification() {
        return currentNotification;
    }

    /**
     * Simple, quick, and easy way to show a notification on the frame without using
     * a builder.
     *
     * @param htmlText the text containing possibly formatted text to display
     */
    public void notify(String htmlText) {
        Preconditions.checkNotNull(htmlText);
        Preconditions.checkArgument(!htmlText.isEmpty());

        notify(new NotificationBuilder(htmlText));
    }

    /**
     * The name for the notification queue thread.
     */
    private static final String NOTIFICATION_QUEUE_THREAD_FOOTER = " Frame Notification Queue";

    /**
     * Notifies the user with a custom notification built from the provided builder.
     * See {@link NotificationBuilder} for more information.
     *
     * @param notificationBuilder the builder used to construct the notification
     */
    public void notify(NotificationBuilder notificationBuilder) {
        Preconditions.checkNotNull(notificationBuilder);
        Preconditions.checkNotNull(notificationBuilder.getHtmlText());
        Preconditions.checkArgument(!notificationBuilder.getHtmlText().isEmpty());

        notificationList.add(notificationBuilder);

        if (!notificationCheckerStarted) {
            notificationCheckerStarted = true;
            CyderThreadRunner.submit(getNotificationQueueRunnable(), getTitle() + NOTIFICATION_QUEUE_THREAD_FOOTER);
        }
    }

    /**
     * Displays a simple toast with the provided text.
     *
     * @param htmlText the styled text to use for the toast
     */
    public void toast(String htmlText) {
        Preconditions.checkNotNull(htmlText);
        Preconditions.checkArgument(!htmlText.isEmpty());

        NotificationBuilder toastBuilder = new NotificationBuilder(htmlText);
        toastBuilder.setNotificationType(CyderNotification.NotificationType.TOAST);
        notificationList.add(toastBuilder);

        if (!notificationCheckerStarted) {
            notificationCheckerStarted = true;
            CyderThreadRunner.submit(getNotificationQueueRunnable(), getTitle() + NOTIFICATION_QUEUE_THREAD_FOOTER);
        }
    }

    /**
     * Displays a toast.
     *
     * @param builder the b builder for the toast
     */
    public void toast(NotificationBuilder builder) {
        Preconditions.checkNotNull(builder);
        Preconditions.checkNotNull(builder.getHtmlText());
        Preconditions.checkArgument(!builder.getHtmlText().isEmpty());

        builder.setNotificationType(CyderNotification.NotificationType.TOAST);
        notificationList.add(builder);

        if (!notificationCheckerStarted) {
            notificationCheckerStarted = true;
            CyderThreadRunner.submit(getNotificationQueueRunnable(), getTitle() + NOTIFICATION_QUEUE_THREAD_FOOTER);
        }
    }

    /**
     * The semaphore used to lock the notification queue
     * so that only one may ever be present at a time.
     */
    private final Semaphore notificationConstructionLock = new Semaphore(1);

    private static final int notificationPadding = 5;

    /**
     * The milliseconds per word for a notification if the time calculation is left up to the method.
     */
    private static final int msPerNotificationWord = 300;

    /**
     * Returns the notification queue for internal frame notifications/toasts.
     *
     * @return the notification queue for internal frame notifications/toasts
     */
    private final Runnable getNotificationQueueRunnable() {
        return () -> {
            while (!threadsKilled && !notificationList.isEmpty()) {
                try {
                    notificationConstructionLock.acquire();
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }

                NotificationBuilder currentBuilder = notificationList.remove(0);
                CyderNotification toBeCurrentNotification = new CyderNotification(currentBuilder);

                toBeCurrentNotification.setVisible(false);

                int maxWidth = (int) Math.ceil(width * NOTIFICATION_TO_FRAME_RATIO);
                BoundsUtil.BoundsString bs = BoundsUtil.widthHeightCalculation(
                        currentBuilder.getHtmlText(),
                        NOTIFICATION_FONT, maxWidth);
                int notificationWidth = bs.width() + notificationPadding;
                int notificationHeight = bs.height() + notificationPadding;
                String brokenText = bs.text();

                // Sanity check for overflow
                if (notificationHeight > height * NOTIFICATION_TO_FRAME_RATIO
                        || notificationWidth > width * NOTIFICATION_TO_FRAME_RATIO) {
                    notifyAndReleaseNotificationSemaphore(currentBuilder.getHtmlText(), null,
                            currentBuilder.notifyTime);
                    continue;
                }

                // If container specified, ensure it can fit
                if (currentBuilder.getContainer() != null) {
                    int containerWidth = currentBuilder.getContainer().getWidth();
                    int containerHeight = currentBuilder.getContainer().getHeight();

                    // Custom component will not fit
                    if (containerWidth > width * NOTIFICATION_TO_FRAME_RATIO
                            || containerHeight > height * NOTIFICATION_TO_FRAME_RATIO) {
                        notifyAndReleaseNotificationSemaphore(null, currentBuilder.getContainer(),
                                currentBuilder.notifyTime);
                        continue;
                    }

                    // Custom container will fit so generate and add disposal label
                    JLabel interactionLabel = new JLabel();
                    interactionLabel.setSize(containerWidth, containerHeight);
                    interactionLabel.setToolTipText(
                            "Notified at: " + toBeCurrentNotification.getBuilder().getNotifyTime());
                    interactionLabel.addMouseListener(generateNotificationDisposalMouseListener(
                            currentBuilder, null, toBeCurrentNotification, false));
                    currentBuilder.getContainer().add(interactionLabel);
                } else {
                    // Empty container means use htmlText of builder
                    JLabel textContainerLabel = new JLabel(brokenText);
                    textContainerLabel.setSize(notificationWidth, notificationHeight);
                    textContainerLabel.setFont(NOTIFICATION_FONT);
                    textContainerLabel.setForeground(CyderColors.notificationForegroundColor);

                    JLabel interactionLabel = new JLabel();
                    interactionLabel.setSize(notificationWidth, notificationHeight);
                    interactionLabel.setToolTipText(
                            "Notified at: " + toBeCurrentNotification.getBuilder().getNotifyTime());
                    interactionLabel.addMouseListener(generateNotificationDisposalMouseListener(
                            currentBuilder, textContainerLabel, toBeCurrentNotification, true));

                    textContainerLabel.add(interactionLabel);
                    toBeCurrentNotification.getBuilder().setContainer(textContainerLabel);
                }

                iconPane.add(toBeCurrentNotification, JLayeredPane.POPUP_LAYER);
                getContentPane().repaint();

                int duration = currentBuilder.getViewDuration();
                if (currentBuilder.isCalculateViewDuration()) {
                    duration = msPerNotificationWord * StringUtil.countWords(Jsoup.clean(bs.text(), Safelist.none()));
                }

                Logger.log(LogTag.UI_ACTION, constructNotificationLogLine(getTitle(), brokenText));

                toBeCurrentNotification.appear(currentBuilder.getNotificationDirection(), getContentPane(), duration);
                currentNotification = toBeCurrentNotification;

                while (!currentNotification.isKilled()) {
                    Thread.onSpinWait();
                }

                notificationConstructionLock.release();
            }
            notificationCheckerStarted = false;
        };
    }

    @ForReadability
    private void notifyAndReleaseNotificationSemaphore(String text, JLabel container, String time) {
        InformHandler.Builder builder = new InformHandler.Builder(text == null ? "NULL" : text)
                .setContainer(container)
                .setTitle(generateNotificationTooltip(time))
                .setRelativeTo(this);

        InformHandler.inform(builder);
        notificationConstructionLock.release();
    }

    @ForReadability
    private String generateNotificationTooltip(String time) {
        return getTitle() + " Notification " + openingParenthesis + time + closingParenthesis;
    }

    /**
     * Generates the disposal mouse listener for a notification.
     *
     * @param builder        the notification builder
     * @param textLabel      the label the notification's text is placed on
     * @param notification   the current notification object under construction
     * @param doEnterAndExit whether to add the mouse entered/exited listeners
     * @return a disposal mouse listener for a notification
     */
    @ForReadability
    private MouseAdapter generateNotificationDisposalMouseListener(
            NotificationBuilder builder, JLabel textLabel, CyderNotification notification, boolean doEnterAndExit) {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (builder.getOnKillAction() != null) {
                    notification.kill();
                    builder.getOnKillAction().run();
                } else {
                    notification.vanish(builder.getNotificationDirection(), getContentPane(), 0);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (doEnterAndExit) {
                    textLabel.setForeground(CyderColors.notificationForegroundColor.darker());
                    notification.setHovered(true);
                    notification.repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (doEnterAndExit) {
                    textLabel.setForeground(CyderColors.notificationForegroundColor);
                    notification.setHovered(false);
                    notification.repaint();
                }
            }
        };
    }

    @ForReadability
    private static String constructNotificationLogLine(String title, String text) {
        return openingBracket + title + "] [NOTIFICATION] \"" + text + quote;
    }

    /**
     * Ends the current notification on screen.
     * If more are behind it, the queue will immediately pull and display
     */
    public void revokeCurrentNotification() {
        revokeCurrentNotification(false);
    }

    /**
     * Revokes the current notification on screen.
     *
     * @param animate whether to kill the notification
     *                immediately or to smoothly animate it away first
     */
    public void revokeCurrentNotification(boolean animate) {
        if (animate) {
            currentNotification.vanish(currentNotification.getBuilder()
                    .getNotificationDirection(), this, 0);
        } else {
            currentNotification.kill();
        }
    }

    /**
     * Revokes the notification currently active or in
     * the queue that matches the provided text.
     *
     * @param expectedText the text of the notification to revoke
     */
    public void revokeNotification(String expectedText) {
        if (currentNotification.getBuilder().getHtmlText().equals(expectedText)) {
            revokeCurrentNotification();
        } else {
            notificationList.removeIf(notificationBuilder
                    -> notificationBuilder.getHtmlText().equals(expectedText));
        }
    }

    /**
     * Removes all currently displayed notifications and wipes the notification queue.
     */
    public void revokeAllNotifications() {
        if (notificationList != null) {
            notificationList.clear();
        }

        if (currentNotification != null) {
            currentNotification.kill();
        }

        notificationCheckerStarted = false;
    }

    // -----------
    // drag labels
    // -----------

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

        InformHandler.inform(new InformHandler.Builder(text).setTitle(title).setRelativeTo(this));
    }

    // ----------
    // animations
    // ----------

    /**
     * The number of frames to use for animations.
     */
    private static final int ANIMATION_FRAMES = 15;

    /**
     * The animation delay for minimize and close animations.
     */
    private static final int MOVEMENT_ANIMATION_DELAY = 5;

    /**
     * Animates away this frame by moving it down until it is offscreen at which point the frame
     * is set to {@link Frame#ICONIFIED}.
     */
    public void minimizeAndIconify() {
        try {
            setRestoreX(getX());
            setRestoreY(getY());

            if (UserUtil.getCyderUser().getDoAnimations().equals("1")) {
                setDisableContentRepainting(true);

                int monitorHeight = UiUtil.getMonitorHeight(this);
                int animationInc = (int) ((double) (monitorHeight - getY()) / ANIMATION_FRAMES);

                for (int i = getY() ; i <= monitorHeight + getHeight() ; i += animationInc) {
                    ThreadUtil.sleep(MOVEMENT_ANIMATION_DELAY);
                    setLocation(getX(), i);

                    if (i >= monitorHeight) {
                        setVisible(false);
                    }
                }
            }

            setVisible(true);
            setState(Frame.ICONIFIED);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setState(int state) {
        if (state == ConsoleConstants.FRAME_ICONIFIED) {
            setDisableContentRepainting(true);
        } else if (state == ConsoleConstants.FRAME_NORMAL) {
            setDisableContentRepainting(false);
        }

        super.setState(state);
    }

    /**
     * Whether this frame's dispose() method has been invoked before.
     */
    private boolean disposed;

    /**
     * Returns whether this frame's dispose() method has been invoked before.
     *
     * @return whether this frame's dispose() method has been invoked before
     */
    public boolean isDisposed() {
        return disposed;
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
    public void dispose(boolean fastClose) {
        String threadName = openingBracket + getTitle() + closingBracket + " dispose() animation thread";
        CyderThreadRunner.submit(() -> {
            try {
                if (disposed) return;

                if (closingConfirmationMessage != null) {
                    boolean exit = GetterUtil.getInstance().getConfirmation(
                            new GetConfirmationBuilder("Confirmation", closingConfirmationMessage)
                                    .setRelativeTo(this)
                                    .setDisableRelativeTo(true));

                    if (!exit) return;
                }

                disposed = true;

                Logger.log(LogTag.UI_ACTION, "CyderFrame disposed with fastclose="
                        + fastClose + ", getTitle=" + getTitle());

                preCloseActions.forEach(Runnable::run);
                if (currentNotification != null) currentNotification.kill();

                killThreads();
                disableDragging();
                setDisableContentRepainting(true);

                boolean closingAnimation = UserUtil.getCyderUser().getDoAnimations().equals("1");
                if (isVisible() && (!fastClose && !shouldFastClose) && closingAnimation) {
                    Point point = getLocationOnScreen();
                    int x = (int) point.getX();
                    int y = (int) point.getY();

                    int distanceToTravel = Math.abs(y) + Math.abs(getHeight());
                    int animationInc = (int) ((double) distanceToTravel / ANIMATION_FRAMES);

                    disableDragging();

                    int startY = getY();
                    int height = getHeight();

                    for (int i = startY ; i >= -height ; i -= animationInc) {
                        ThreadUtil.sleep(MOVEMENT_ANIMATION_DELAY);
                        setLocation(x, i);
                    }
                }

                Console.INSTANCE.removeTaskbarIcon(this);
                Console.INSTANCE.removeFrameTaskbarException(this);

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
        INITIAL_UP, LEFT, DOWN, RIGHT, UP
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
     * Sets the direction the frame is currently dancing in.
     *
     * @param dancingDirection the direction the frame is currently dancing in
     */
    @SuppressWarnings("SameParameterValue")
    public void setDancingDirection(DancingDirection dancingDirection) {
        this.dancingDirection = dancingDirection;
    }

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
        ImageIcon masterIcon = currentMasterIcon;
        BufferedImage master = ImageUtil.toBufferedImage(masterIcon);
        BufferedImage rotated = ImageUtil.rotateImage(master, degrees);
        ((JLabel) getContentPane()).setIcon(new ImageIcon(rotated));
    }

    /**
     * The delay in ms between barrel roll increments.
     */
    private static final int BARREL_ROLL_DELAY = 10;

    /**
     * The increment in radians between barrel roll delays.
     */
    private static final int BARREL_ROLL_DELTA = 2;

    /**
     * transforms the content pane by an incremental angle of 2 degrees
     * emulating Google's barrel roll easter egg.
     */
    public void barrelRoll() {
        ImageIcon masterIcon = (ImageIcon) ((JLabel) getContentPane()).getIcon();
        BufferedImage master = ImageUtil.toBufferedImage(masterIcon);

        CyderThreadRunner.submit(() -> {
            float angle = 0.0f;

            for (int i = 0 ; i < (int) AngleUtil.THREE_SIXTY_DEGREES ; i += BARREL_ROLL_DELTA) {
                BufferedImage rotated = ImageUtil.rotateImage(master, angle);
                ((JLabel) getContentPane()).setIcon(new ImageIcon(rotated));

                ThreadUtil.sleep(BARREL_ROLL_DELAY);

                if (threadsKilled) {
                    break;
                }
            }

            ((JLabel) getContentPane()).setIcon(masterIcon);
        }, getTitle() + " Barrel Roll");
    }

    /**
     * Revalidates the title position.
     */
    public void revalidateTitlePosition() {
        if (topDrag == null) return;

        int leftButtonSize = topDrag.getLeftButtonList().size();
        int rightButtonSize = topDrag.getRightButtonList().size();

        if (titlePosition == TitlePosition.LEFT && leftButtonSize > 0) {
            setTitlePosition(TitlePosition.CENTER);
        }

        if (titlePosition == TitlePosition.RIGHT && rightButtonSize > 0) {
            setTitlePosition(TitlePosition.CENTER);
        }
    }

    /**
     * Validates the provided width and height to ensure it meets the minimum criteria and returns
     * the validated dimension.
     *
     * @param width  the requested width
     * @param height the requested height
     * @return the width and height to use for the frame
     */
    private Dimension validateRequestedSize(int width, int height) {
        String title = getTitle().length() < 1 ? "No title found" : getTitle();

        if (width < MINIMUM_WIDTH) {
            Logger.log(LogTag.DEBUG, "CyderFrame \"" + title
                    + "\" was attempted to be set to invalid width: " + width);
            width = MINIMUM_WIDTH;
        }

        if (height < MINIMUM_HEIGHT) {
            Logger.log(LogTag.DEBUG, "CyderFrame \"" + title
                    + "\" was attempted to be set to invalid height: " + height);
            height = MINIMUM_HEIGHT;
        }

        return new Dimension(width, height);
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
     * Performs repaint actions necessary for a borderless frame returned via
     * {@link CyderFrame#generateBorderlessFrame(int, int, Color)}.
     */
    private void repaintBorderlessFrame() {
        Preconditions.checkState(isBorderlessFrame());

        if (getContentPane() != null) {
            getContentPane().repaint();
        }

        if (getTrueContentPane() != null) {
            getTrueContentPane().repaint();
        }

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

        if (topDrag != null) {
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
     * {@link CyderFrame#MINIMUM_WIDTH} by {@link CyderFrame#MINIMUM_HEIGHT}
     *
     * @param width  width of frame
     * @param height height of frame
     */
    @Override
    public void setSize(int width, int height) {
        Dimension dimension = validateRequestedSize(width, height);

        width = dimension.width;
        height = dimension.height;

        boolean sameSizes = this.width == width && this.height == height;

        super.setSize(width, height);

        this.width = width;
        this.height = height;

        revalidateDragLabels();

        revalidateFrameShape();

        if (sameSizes) {
            return;
        }

        revalidateLayout();

        if (UiUtil.notNullAndVisible(menuLabel)) {
            generateMenu();
            menuLabel.setLocation(menuAnimateToPoint);
            menuLabel.setVisible(true);
        }

        revalidateNotificationPosition();
        correctTitleLength();
    }

    /**
     * Sets the bounds of the CyderFrame and refreshes all components on the frame.
     */
    @Override
    public void setBounds(int x, int y, int width, int height) {
        Dimension dimension = validateRequestedSize(width, height);

        width = dimension.width;
        height = dimension.height;

        boolean sameSizes = this.width == width && this.height == height;

        super.setBounds(x, y, width, height);

        this.width = width;
        this.height = height;

        revalidateDragLabels();

        revalidateFrameShape();

        if (sameSizes) {
            return;
        }

        revalidateLayout();

        if (UiUtil.notNullAndVisible(menuLabel)) {
            generateMenu();
            menuLabel.setLocation(menuAnimateToPoint);
            menuLabel.setVisible(true);
        }

        revalidateNotificationPosition();
        correctTitleLength();
    }

    /**
     * Revalidates the drag labels and their covers and offsets if present.
     */
    private void revalidateDragLabels() {
        if (isBorderlessFrame())
            return;

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

        revalidateTitlePositionLocation();

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

        leftDrag.setXOffset(FRAME_RESIZING_LEN);
        leftDrag.setYOffset(CyderDragLabel.DEFAULT_HEIGHT);

        rightDrag.setXOffset(width - BORDER_LEN);
        rightDrag.setYOffset(CyderDragLabel.DEFAULT_HEIGHT);

        bottomDrag.setXOffset(FRAME_RESIZING_LEN);
        bottomDrag.setYOffset(height - BORDER_LEN);
    }

    /**
     * The arc length of the arc for rounded window shapes.
     */
    private static final int ROUNDED_ARC = 20;

    /**
     * Revalidates and updates the frame's shape, that of being rounded or square.
     */
    private void revalidateFrameShape() {
        if (!isUndecorated()) {
            return;
        }

        Shape shape = null;

        try {
            // Borderless frames are by default rounded
            if (isBorderlessFrame() || (cr == null && Console.INSTANCE.getUuid() != null
                    && UserUtil.getCyderUser().getRoundedWindows().equals("1"))) {
                shape = new RoundRectangle2D.Double(0, 0,
                        getWidth(), getHeight(), ROUNDED_ARC, ROUNDED_ARC);
            }
        } catch (Exception e) {
            ExceptionHandler.silentHandle(e);
        } finally {
            setShape(shape);
        }
    }

    /**
     * Revalidates the current notification's position if existent.
     */
    private void revalidateNotificationPosition() {
        if (currentNotification != null) {
            int y = currentNotification.getY();
            int w = currentNotification.getWidth();

            switch (currentNotification.getBuilder().getArrowDir()) {
                case TOP, BOTTOM -> currentNotification.setLocation(width / 2 - w / 2, y);
                case RIGHT -> currentNotification.setLocation(width - w + titleLabelPadding, y);
                case LEFT -> currentNotification.setLocation(titleLabelPadding, y);
            }
        }
    }

    /**
     * The gap to keep between the drag label buttons and the start/end of the title label.
     */
    private static final int necessaryGap = 10;

    /**
     * Revalidates the title label width based to ensure that the
     * most is shown but the title does not overlap any buttons.
     */
    private void correctTitleLength() {
        if (isBorderlessFrame() || topDrag.getRightButtonList() == null || isBorderlessFrame()) {
            return;
        }

        LinkedList<Component> leftButtons = topDrag.getLeftButtonList();
        LinkedList<Component> rightButtons = topDrag.getRightButtonList();

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

        int necessaryTitleWidth = StringUtil.getAbsoluteMinWidth(title, titleLabel.getFont());
        int necessaryTitleHeight = StringUtil.getAbsoluteMinHeight(title, titleLabel.getFont());
        int y = Math.min(topDrag.getHeight() / 2 - necessaryTitleHeight / 2, 0);

        // Reset default bounds, will be trimmed away below
        switch (titlePosition) {
            case LEFT -> titleLabel.setBounds(titleLabelPadding, y, necessaryTitleWidth, necessaryTitleHeight);
            case CENTER -> titleLabel.setBounds(width / 2 - necessaryTitleWidth / 2, y,
                    necessaryTitleWidth, necessaryTitleHeight);
            case RIGHT -> titleLabel.setBounds(width - titleLabelPadding - necessaryTitleWidth, y,
                    necessaryTitleWidth, necessaryTitleHeight);
        }

        boolean areLeftButtons = leftButtons.size() > 0;
        boolean areRightButtons = rightButtons.size() > 0;

        if (areLeftButtons && areRightButtons) {
            if (titlePosition != TitlePosition.CENTER) {
                setTitlePosition(TitlePosition.CENTER, false);
            }
            if (titleLabel.getX() - necessaryGap < leftButtonsEnd
                    || titleLabel.getX() + titleLabel.getWidth() + necessaryGap > rightButtonsStart) {
                int leftDeviation = getWidth() / 2 - leftButtonsEnd - necessaryGap;
                int rightDeviation = rightButtonsStart - getWidth() / 2 - necessaryGap;
                int w = 2 * Math.min(leftDeviation, rightDeviation);
                titleLabel.setBounds(width / 2 - w / 2, y, w, necessaryTitleHeight);
            }
        } else if (areLeftButtons) {
            if (titlePosition == TitlePosition.CENTER) {
                if (width / 2 - necessaryTitleWidth / 2 - necessaryGap < leftButtonsEnd) {
                    int w = 2 * (width / 2 - leftButtonsEnd - necessaryGap);
                    titleLabel.setBounds(width / 2 - w / 2, y, w, necessaryTitleHeight);
                }
            } else {
                if (width - titleLabelPadding - necessaryTitleWidth - necessaryGap < leftButtonsEnd) {
                    int w = width - necessaryGap - leftButtonsEnd - titleLabelPadding;
                    titleLabel.setBounds(width - titleLabelPadding - w, y, w, necessaryTitleHeight);
                }
            }
        } else if (areRightButtons) {
            if (titlePosition == TitlePosition.CENTER) {
                if (width / 2 + necessaryTitleWidth / 2 + necessaryGap > rightButtonsStart) {
                    int w = 2 * (rightButtonsStart - necessaryGap - width / 2);
                    titleLabel.setBounds(width / 2 - w / 2, y, w, necessaryTitleHeight);
                }
            } else {
                if (titleLabelPadding + necessaryTitleWidth + necessaryGap > rightButtonsStart) {
                    int w = rightButtonsStart - titleLabelPadding - necessaryGap;
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
    private Dimension minimumSize = new Dimension(MINIMUM_WIDTH, MINIMUM_HEIGHT);

    /**
     * The maximum size of a CyderFrame.
     */
    private Dimension maximumSize = new Dimension(800, 800);

    /**
     * The increment to snap to on resize events.
     */
    private Dimension snapSize = new Dimension(1, 1);

    /**
     * Sets the minimum window size if resizing is allowed.
     *
     * @param minSize the Dimension of the minimum allowed size
     */
    public void setMinimumSize(Dimension minSize) {
        minimumSize = minSize;
        cr.setMinimumSize(minimumSize);
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
        maximumSize = maxSize;
        cr.setMaximumSize(maximumSize);
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
        snapSize = snap;
        cr.setSnapSize(snapSize);
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
        return minimumSize;
    }

    /**
     * Returns the maximum window size if resizing is allowed.
     *
     * @return the maximum window size if resizing is allowed
     */
    public Dimension getMaximumSize() {
        return maximumSize;
    }

    /**
     * Returns the snap size for the window if resizing is allowed.
     *
     * @return the snap size for the window if resizing is allowed
     */
    public Dimension getSnapSize() {
        return snapSize;
    }

    /**
     * The component resizing object for this CyderFrame.
     */
    private CyderComponentResizer cr;

    /**
     * Whether to allow background resizing on CyderFrame resize events.
     *
     * @param allowed whether to allow background resizing on CyderFrame resize events
     */
    public void setBackgroundResizing(Boolean allowed) {
        cr.setBackgroundResizing(allowed);
    }

    /**
     * Sets up necessary objects needed to allow the frame to be
     * resizable such as registering the min/max sizes.
     */
    public void initializeResizing() {
        if (cr != null) {
            return;
        }

        cr = new CyderComponentResizer();
        cr.registerComponent(this);
        cr.setResizingAllowed(true);
        cr.setMinimumSize(getMinimumSize());
        cr.setMaximumSize(getMaximumSize());
        cr.setSnapSize(getSnapSize());

        setShape(null);
    }

    /**
     * Sets whether frame resizing is allowed.
     *
     * @param allow whether frame resizing is allowed
     */
    public void setFrameResizing(boolean allow) {
        cr.setResizingAllowed(allow);
    }

    /**
     * The current master image icon to use for image resizing on resizing events if allowed.
     */
    private ImageIcon currentMasterIcon;

    /**
     * Whether the frame is resizable.
     *
     * @param allow whether the frame is resizable
     */
    @Override
    public void setResizable(boolean allow) {
        if (cr != null) {
            cr.setResizingAllowed(allow);
        }
    }

    /**
     * Refresh the background in the event of a frame size change or a background image change.
     */
    public void refreshBackground() {
        try {
            if (iconLabel == null) {
                return;
            }

            // Mainly needed for icon label and pane bounds, layout isn't usually expensive
            revalidateLayout();

            if (cr != null && cr.backgroundResizingEnabled()) {
                iconLabel.setIcon(new ImageIcon(currentMasterIcon.getImage()
                        .getScaledInstance(iconLabel.getWidth(), iconLabel.getHeight(), Image.SCALE_DEFAULT)));
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
        if (iconLabel != null) {
            iconLabel.setBounds(FRAME_RESIZING_LEN, FRAME_RESIZING_LEN, width - 2 * FRAME_RESIZING_LEN,
                    height - 2 * FRAME_RESIZING_LEN);
        }

        if (iconPane != null) {
            iconPane.setBounds(FRAME_RESIZING_LEN, FRAME_RESIZING_LEN, width - 2 * FRAME_RESIZING_LEN,
                    height - 2 * FRAME_RESIZING_LEN);
        }

        if (cyderPanel != null) {
            cyderPanel.setBounds(BORDER_LEN, CyderDragLabel.DEFAULT_HEIGHT, getWidth() - 2 * BORDER_LEN,
                    getHeight() - CyderDragLabel.DEFAULT_HEIGHT - BORDER_LEN);
        }
    }

    /**
     * Set the background to a new image and revalidates and repaints the frame.
     *
     * @param bi the buffered image for the frame's background
     */
    public void setBackground(BufferedImage bi) {
        Preconditions.checkNotNull(bi);

        setBackground(ImageUtil.toImageIcon(bi));
    }

    /**
     * Set the background to a new icon and revalidates and repaints the frame.
     *
     * @param icon the ImageIcon for the frame's background
     */
    public void setBackground(ImageIcon icon) {
        Preconditions.checkNotNull(icon);

        try {
            // Prevent errors before instantiation of ui objects
            if (iconLabel == null) {
                return;
            }

            currentMasterIcon = icon;
            iconLabel.setIcon(new ImageIcon(currentMasterIcon.getImage()
                    .getScaledInstance(iconLabel.getWidth(), iconLabel.getHeight(), Image.SCALE_DEFAULT)));
            iconLabel.setBounds(FRAME_RESIZING_LEN, FRAME_RESIZING_LEN, width - 2 * FRAME_RESIZING_LEN,
                    height - 2 * FRAME_RESIZING_LEN);
            iconPane.setBounds(FRAME_RESIZING_LEN, FRAME_RESIZING_LEN, width - 2 * FRAME_RESIZING_LEN,
                    height - 2 * FRAME_RESIZING_LEN);

            if (cr != null) {
                cr.setMinimumSize(new Dimension(600, 600));
                cr.setMaximumSize(new Dimension(background.getIconWidth(), background.getIconHeight()));
            }

            revalidate();
            repaint();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
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
        return ReflectionUtil.commonCyderUiToString(this);
    }

    /**
     * Kills all threads associated with this instance.
     * Features should not be expected to function properly after this method is invoked.
     */
    private void killThreads() {
        threadsKilled = true;
    }

    /**
     * Whether threads have been killed.
     *
     * @return whether threads have been killed
     */
    public boolean threadsKilled() {
        return threadsKilled;
    }

    /**
     * Set the background of {@code this} to the current Console background.
     */
    public void replicateConsoleBackground() {
        if (Console.INSTANCE.getCurrentBackground() == null) {
            return;
        }

        iconLabel.setIcon(new ImageIcon(Console.INSTANCE.getCurrentBackground()
                .generateImageIcon()
                .getImage()
                .getScaledInstance(getWidth(), getHeight(), Image.SCALE_DEFAULT)));
    }

    /**
     * Returns the restore x value.
     *
     * @return the restore x value
     */
    public int getRestoreX() {
        return restoreX;
    }

    /**
     * Returns the restore y value.
     *
     * @return the restore y value
     */
    public int getRestoreY() {
        return restoreY;
    }

    /**
     * Sets the restore x value.
     *
     * @param x the restore x value
     */
    public void setRestoreX(int x) {
        restoreX = x;
    }

    /**
     * Sets the restore y value.
     *
     * @param y the restore y value
     */
    public void setRestoreY(int y) {
        restoreY = y;
    }

    /**
     * Whether dragging is permitted for this frame.
     *
     * @return whether dragging is permitted for this frame
     */
    public boolean isDraggingEnabled() {
        if (isBorderlessFrame()) {
            return false;
        }

        return topDrag.isDraggingEnabled()
                && bottomDrag.isDraggingEnabled()
                && leftDrag.isDraggingEnabled()
                && rightDrag.isDraggingEnabled();
    }

    /**
     * Disables dragging for this frame.
     */
    public void disableDragging() {
        if (isBorderlessFrame()) {
            return;
        }

        topDrag.disableDragging();
        bottomDrag.disableDragging();
        rightDrag.disableDragging();
        leftDrag.disableDragging();
    }

    /**
     * Enables dragging for this frame.
     */
    public void enableDragging() {
        if (isBorderlessFrame()) {
            return;
        }

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
     * Actions to be invoked before dispose is invoked.
     */
    private LinkedList<Runnable> preCloseActions = new LinkedList<>();

    /**
     * Actions to be invoked after dispose is invoked.
     */
    private LinkedList<Runnable> postCloseActions = new LinkedList<>();

    /**
     * Removes all pre close actions.
     */
    public void removePreCloseActions() {
        preCloseActions = new LinkedList<>();
    }

    /**
     * Removes all post close actions.
     */
    public void removePostCloseActions() {
        postCloseActions = new LinkedList<>();
    }

    /**
     * Performs the given action right before closing the frame. This action is invoked right before an animation
     * and sequential dispose call.
     *
     * @param action the action to perform before closing/disposing
     */
    public void addPreCloseAction(Runnable action) {
        preCloseActions.add(action);
    }

    /**
     * Performs the given action right after closing the frame. This action is invoked right after an animation
     * and sequential dispose call.
     *
     * @param action the action to perform before closing/disposing
     */
    public void addPostCloseAction(Runnable action) {
        postCloseActions.add(action);
    }

    /**
     * A message to display before the frame is actually disposed.
     */
    private String closingConfirmationMessage;

    /**
     * Displays a confirmation dialog to the user to confirm
     * whether they intended to exit the frame.
     *
     * @param message the message to display to the user
     */
    public void setClosingConfirmation(String message) {
        Preconditions.checkNotNull(message);
        Preconditions.checkArgument(!message.isEmpty());

        closingConfirmationMessage = message;
    }

    /**
     * Removes any closing confirmation messages set.
     */
    public void removeClosingConfirmation() {
        closingConfirmationMessage = null;
    }

    // -------------
    // pinning logic
    // -------------

    /**
     * Adds any {@link MouseMotionListener}s to the drag labels.
     *
     * @param actionListener the listener to add to the drag labels
     */
    public void addDragListener(MouseMotionListener actionListener) {
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
        topDrag.addMouseListener(mouseListener);
        bottomDrag.addMouseListener(mouseListener);
        leftDrag.addMouseListener(mouseListener);
        rightDrag.addMouseListener(mouseListener);
    }

    /**
     * Returns whether the frame should be pinned to the console.
     *
     * @return whether the frame should be pinned to the console
     */
    public boolean isConsolePinned() {
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
     * If the frame's dispose() method has been invoked, this will
     * prevent the image from being set for optimization purposes.
     *
     * @param image the image to use for the taskbar
     */
    @Override
    public void setIconImage(Image image) {
        if (!disposed) {
            super.setIconImage(image);
        }
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
    public Color getTaskbarIconBorderColor() {
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
    public ImageIcon getCustomTaskbarIcon() {
        return customTaskbarIcon;
    }

    /**
     * Sets the taskbar image icon to use.
     *
     * @param customTaskbarIcon the taskbar image icon to use
     */
    public void setCustomTaskbarIcon(ImageIcon customTaskbarIcon) {
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
            addToTaskbar();
            determineInitialPinState();
        }
    }

    /**
     * Adds this frame to the console taskbar.
     */
    @ForReadability
    private void addToTaskbar() {
        Console.INSTANCE.addTaskbarIcon(this);
    }

    /**
     * Determines whether the drag label pin button should be enabled based on the console's pinned state.
     */
    @ForReadability
    private void determineInitialPinState() {
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
     * The image for the debug lines for the console.
     */
    private static final ImageIcon neffexIcon = new ImageIcon(
            OsUtil.buildPath("static", "pictures", "print", "neffex.png"));

    /**
     * The minor axis length of a debug line.
     */
    private static final int debugLineLen = 4;

    /**
     * Sets whether debug lines should be drawn for this frame.
     *
     * @param draw whether debug lines should be drawn for this frame
     */
    public void drawDebugLines(boolean draw) {
        drawDebugLines = draw;

        if (draw) {
            Color lineColor = ColorUtil.getInverseColor(backgroundColor);

            if (background != null) {
                lineColor = ColorUtil.getDominantColorInverse(background);
            }

            debugImageLabel = new JLabel();
            debugImageLabel.setIcon(neffexIcon);
            debugImageLabel.setBounds(
                    getWidth() / 2 - neffexIcon.getIconWidth() / 2,
                    getHeight() / 2 - neffexIcon.getIconHeight() / 2,
                    neffexIcon.getIconWidth(), neffexIcon.getIconHeight());
            add(debugImageLabel);

            debugXLabel = new JLabel();
            debugXLabel.setBounds(getWidth() / 2 - debugLineLen / 2, 0, debugLineLen, getHeight());
            debugXLabel.setOpaque(true);
            debugXLabel.setBackground(lineColor);
            add(debugXLabel);

            debugYLabel = new JLabel();
            debugYLabel.setBounds(0, getHeight() / 2 - debugLineLen / 2, getWidth(), debugLineLen);
            debugYLabel.setOpaque(true);
            debugYLabel.setBackground(lineColor);
            add(debugYLabel);
        } else {
            if (this.equals(SwingUtilities.getRoot(debugXLabel))) {
                remove(debugXLabel);
            }

            if (this.equals(SwingUtilities.getRoot(debugYLabel))) {
                remove(debugYLabel);
            }

            if (this.equals(SwingUtilities.getRoot(debugImageLabel))) {
                remove(debugImageLabel);
            }
        }

        revalidate();
        repaint();
    }

    /**
     * Returns whether debug lines should be drawn for this frame.
     *
     * @return whether debug lines should be drawn for this frame
     */
    public boolean isDrawDebugLines() {
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
        if (!shouldAnimateOpacity) {
            return;
        }

        CyderThreadRunner.submit(() -> {
            for (float i = DEFAULT_OPACITY ; i >= DRAG_OPACITY ; i -= OPACITY_DELTA) {
                if (animatingOut) {
                    break;
                }

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
    private final LinkedList<Runnable> endDragEventCallbacks = new LinkedList<>();

    /**
     * Executes all callbacks registered in {@link #endDragEventCallbacks}.
     */
    private void executeEndDragEventCallbacks() {
        for (Runnable runnable : endDragEventCallbacks) {
            runnable.run();
        }
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
    // frame menu logic
    // ----------------

    /**
     * The types of menus that a frame can use for its menu.
     */
    public enum MenuType {
        /**
         * The default which mimics the console.
         */
        PANEL,
        /**
         * Lays out the menu items in a horizontal scroll right below the drag label.
         */
        RIBBON,
    }

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

        if (menuEnabled) {
            boolean wasVisible = UiUtil.notNullAndVisible(menuLabel);

            generateMenu();

            if (wasVisible) {
                showMenu();
            }
        }
    }

    /**
     * Regenerates the menu and sets the visibility to the state before this method was invoked.
     */
    public void revalidateMenu() {
        if (menuEnabled) {
            boolean wasVisible = UiUtil.notNullAndVisible(menuLabel);
            generateMenu();

            if (wasVisible) {
                showMenu();
            }
        }
    }

    /**
     * Revalidates the menu and shows it only if it was visible.
     */
    public void revalidateMenuIfVisible() {
        if (UiUtil.notNullAndVisible(menuLabel)) {
            revalidateMenu();
        }
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
        setMenuEnabled(false);
    }

    /**
     * Ensures the menu isn't visible and cannot be triggered.
     */
    public void lockMenuIn() {
        hideMenu();
        setMenuEnabled(false);
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
        menuButton.addClickAction(menuLabelClickListener);
        topDrag.addLeftButton(menuButton, 0);
    }

    /**
     * Removes the menu button from the drag label
     */
    private void removeMenuButton() {
        if (!topDrag.getLeftButtonList().isEmpty()) {
            topDrag.removeLeftButton(menuButton);
        }
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
    private final LinkedList<MenuItem> menuItems = new LinkedList<>() {
        @Override
        public boolean add(MenuItem menuItem) {
            boolean ret = false;

            if (!menuItems.contains(menuItem)) {
                ret = super.add(menuItem);
            }

            if (menuItems.size() == 1) {
                addMenuButton();
            }

            return ret;
        }

        @Override
        public boolean remove(Object o) {
            boolean ret = super.remove(o);

            if (menuItems.isEmpty()) {
                removeMenuButton();
            }

            return ret;
        }
    };

    /**
     * The mouse listener for the menu if enabled and menu items are present.
     */
    private final Runnable menuLabelClickListener = () -> {
        if (menuEnabled) {
            if (menuLabel == null) {
                generateMenu();
            }

            if (menuLabel.isVisible()) {
                animateMenuOut();
            } else {
                animateMenuIn();
            }
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

        for (int i = 0 ; i < menuItems.size() ; i++) {
            if (menuItems.get(i).label().getText().equals(text)) {
                removeMenuItem(i);
                return;
            }
        }
    }

    /**
     * Removes the menu item from the provided index.
     *
     * @param index the index of the menu item to remove
     */
    public void removeMenuItem(int index) {
        checkNotNull(menuItems);
        checkArgument(!menuItems.isEmpty());
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
     * The dots for a title or menu item that is cut off due to being too long.
     */
    private static final String DOTS = "...";

    /**
     * The foreground color for menu items.
     */
    private static final Color menuItemForeground = CyderColors.vanilla;

    /**
     * The foreground color for menu items when hovered.
     */
    private static final Color menuItemHoverForeground = CyderColors.regularRed;

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
            text = (text.substring(0, MAXIMUM_MENU_ITEM_TEXT_LENGTH - 3).trim() + DOTS);
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
     * The point at which the menu is placed when visible.
     */
    private final Point menuAnimateToPoint = new Point(
            BORDER_LEN - FRAME_RESIZING_LEN, CyderDragLabel.DEFAULT_HEIGHT - 2);

    /**
     * Returns whether the menu is accessible
     *
     * @return whether the menu is accessible
     */
    public boolean isMenuEnabled() {
        return menuEnabled;
    }

    /**
     * Sets whether the menu should be enabled.
     *
     * @param menuEnabled whether the menu should be enabled
     */
    public void setMenuEnabled(boolean menuEnabled) {
        this.menuEnabled = menuEnabled;

        if (menuEnabled) {
            addMenuButton();
        } else {
            removeMenuButton();
        }
    }

    /**
     * Shows the menu in the currently set location as defined by {@link MenuType}.
     */
    public void showMenu() {
        if (menuLabel == null) {
            generateMenu();
        }

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
     * The increment/decrement value when animating the frame menu.
     */
    private static final int menuAnimationInc = 2;

    /**
     * The delay between animation increments when animating the frame menu.
     */
    private static final int menuAnimationDelay = 1;

    /**
     * Animates the menu label in.
     */
    private void animateMenuIn() {
        if (!menuLabel.isVisible()) {
            generateMenu();
        }

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
        }, getTitle() + " Menu Label Animator");
    }

    /**
     * Animates out the audio controls.
     */
    private void animateMenuOut() {
        checkNotNull(menuLabel);

        if (menuLabel.getX() + menuLabel.getWidth() < 0 && menuLabel.getY() + menuLabel.getHeight() < 0) {
            return;
        }

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
        }, getTitle() + " menu label animator");
    }

    /**
     * The frame menu width.
     */
    private static final int menuWidth = 120;

    /**
     * The height to add in addition to each menu component.
     */
    private static final int paddingHeight = 5;

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
     * Generates the menu based off of the current menu components
     * and sets the location to the starting point for inward animation.
     */
    private void generateMenu() {
        if (menuLabel != null) {
            menuLabel.setVisible(false);
        }

        menuLabel = new JLabel();
        menuLabel.setOpaque(true);
        menuLabel.setBackground(CyderColors.getGuiThemeColor());

        if (menuType == MenuType.PANEL) {
            int menuHeight = 2 * paddingHeight + (menuItems.size() * (StringUtil.getAbsoluteMinHeight(
                    String.valueOf(CyderNumbers.JENNY), CyderFonts.DEFAULT_FONT_SMALL))) + 5;

            if (menuHeight > getHeight() - topDrag.getHeight() - menuYOffset) {
                menuHeight = getHeight() - topDrag.getHeight() - menuYOffset;
            }

            menuLabel.setSize(menuWidth, menuHeight);
        } else {
            menuLabel.setSize(getWidth() - 10, StringUtil.getMinHeight(CyderStrings.JENNY,
                    CyderFonts.DEFAULT_FONT_SMALL));
        }

        menuLabel.setBorder(new LineBorder(Color.black, menuBorderThickness));

        JTextPane menuPane = new JTextPane() {
            /**
             * Overridden to disable vertical scrollbar since setting
             * the policy doesn't work apparently, thanks JDK devs
             */
            @Override
            public boolean getScrollableTracksViewportWidth() {
                return getUI().getPreferredSize(this).width <= getParent().getSize().width;
            }
        };
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

        StyledDocument doc = menuPane.getStyledDocument();
        SimpleAttributeSet alignment = new SimpleAttributeSet();
        StyleConstants.setAlignment(alignment, StyleConstants.ALIGN_LEFT);
        doc.setParagraphAttributes(0, doc.getLength(), alignment, false);

        StringUtil printingUtil = new StringUtil(new CyderOutputPane(menuPane));
        menuPane.setText("");

        // update externally synced label foregrounds
        for (MenuItem menuItem : menuItems) {
            if (menuItem.state() != null) {
                menuItem.label().setForeground(menuItem.state().get()
                        ? CyderColors.regularRed
                        : CyderColors.vanilla);
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
     * The increment of the fade in animation.
     */
    private static final float FADE_IN_ANIMATION_INCREMENT = 0.02f;

    /**
     * The starting minimum opacity of the frame for the fade in animation.
     */
    private static final float FADE_IN_STARTING_OPACITY = 0f;

    /**
     * The ending maximum opacity of the frame for the fade in animation.
     */
    private static final float FADE_IN_ENDING_OPACITY = 1f;

    /**
     * The delay between fade in increments for the fade in animation.
     */
    private static final int FADE_IN_ANIMATION_DELAY = 2;

    /**
     * The thread name for the frame fade in animation.
     */
    private static final String FADE_IN_ANIMATION_THREAD_NAME = "Frame Fade-in Animation";

    /**
     * Sets the frame's location relative to the dominant frame,
     * the visibility to true, and fades in the frame.
     */
    public void finalizeAndShow() {
        finalizeAndShow(getDominantFrame());
    }

    /**
     * Sets the frame's location relative to the provided component,
     * the visibility to true, and fades in the frame.
     *
     * @param component the component to set the frame relative to
     */
    public void finalizeAndShow(Component component) {
        setLocationRelativeTo(component);

        setOpacity(FADE_IN_STARTING_OPACITY);
        setVisible(true);
        toFront();

        CyderThreadRunner.submit(() -> {
            for (float i = FADE_IN_STARTING_OPACITY ; i < FADE_IN_ENDING_OPACITY ; i += FADE_IN_ANIMATION_INCREMENT) {
                setOpacity(i);
                ThreadUtil.sleep(FADE_IN_ANIMATION_DELAY);
            }
        }, FADE_IN_ANIMATION_THREAD_NAME);
    }

    /**
     * Sets the frame's location to the provided point,
     * the visibility to true, and fades in the frame.
     *
     * @param point the point to set the top left of the frame at
     */
    public void finalizeAndShow(Point point) {
        Preconditions.checkNotNull(point);
        setLocation(point);

        setOpacity(FADE_IN_STARTING_OPACITY);
        setVisible(true);
        toFront();

        CyderThreadRunner.submit(() -> {
            for (float i = FADE_IN_STARTING_OPACITY ; i < FADE_IN_ENDING_OPACITY ; i += FADE_IN_ANIMATION_INCREMENT) {
                setOpacity(i);
                ThreadUtil.sleep(FADE_IN_ANIMATION_DELAY);
            }
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
        } else {
            return null;
        }
    }

    /**
     * The valid screen positions for a frame object.
     */
    public enum ScreenPosition {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT,
        CENTER,
        MIDDLE
    }

    /**
     * Sets the console to a provided ScreenPosition and moves any pinned CyderFrame windows with it.
     *
     * @param screenPos the screen position to move the Console to
     */
    public void setLocationOnScreen(ScreenPosition screenPos) {
        Rectangle ourMonitorBounds = getMonitorBounds();

        switch (screenPos) {
            case CENTER, MIDDLE, null -> setLocation(
                    ourMonitorBounds.x + ourMonitorBounds.width / 2 - getWidth() / 2,
                    ourMonitorBounds.y + ourMonitorBounds.height / 2 - getHeight() / 2);
            case TOP_LEFT -> setLocation(
                    ourMonitorBounds.x,
                    ourMonitorBounds.y);
            case TOP_RIGHT -> setLocation(
                    ourMonitorBounds.x + ourMonitorBounds.width - getWidth(),
                    ourMonitorBounds.y);
            case BOTTOM_LEFT -> setLocation(
                    ourMonitorBounds.x,
                    ourMonitorBounds.y + ourMonitorBounds.height - getHeight());
            case BOTTOM_RIGHT -> setLocation(
                    ourMonitorBounds.x + ourMonitorBounds.width - getWidth(),
                    ourMonitorBounds.y + ourMonitorBounds.height - getHeight());
        }
    }

    /**
     * The foreground color of the title label
     */
    private Color titleLabelColor = CyderColors.vanilla;

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
     * The font for the title label.
     */
    private Font titleLabelFont = DEFAULT_FRAME_TITLE_FONT;

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
        setDancingDirection(CyderFrame.DancingDirection.INITIAL_UP);
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

    /*
    Inner classes such as builders.
     */

    /**
     * A builder for a CyderFrame notification.
     */
    @CanIgnoreReturnValue
    public static final class NotificationBuilder {
        /**
         * The html styled text to display.
         */
        private final String htmlText;

        // -------------------
        // Optional parameters
        // -------------------

        /**
         * The duration the notification should be visible for in ms not counting the animation period.
         */
        private int viewDuration = 5000;

        /**
         * The direction to draw the notification arrow.
         */
        private Direction arrowDir = Direction.TOP;

        /**
         * The runnable to invoke upon the notification being killed by a user.
         */
        private Runnable onKillAction;

        /**
         * The direction for the notification to appear/disappear from/to.
         */
        private NotificationDirection notificationDirection = NotificationDirection.TOP;

        /**
         * The type of notification, i.e. notification vs toast.
         */
        private CyderNotification.NotificationType notificationType = CyderNotification.NotificationType.NOTIFICATION;

        /**
         * The custom container for the notification. If this is not provided a label is generated
         * which holds the html styled text.
         */
        private JLabel container;

        /**
         * Whether the view duration should be auto-calculated.
         */
        private boolean calculateViewDuration;

        /**
         * The time the notification was originally constructed at.
         */
        private final String notifyTime;

        /**
         * Default constructor for a Notification with the required parameters for the Notification.
         *
         * @param htmlText the html styled text to display
         */
        public NotificationBuilder(String htmlText) {
            Preconditions.checkNotNull(htmlText);
            Preconditions.checkArgument(!htmlText.isEmpty());

            this.htmlText = htmlText;

            notifyTime = TimeUtil.notificationTime();

            Logger.log(LogTag.OBJECT_CREATION, this);
        }

        /**
         * Returns the html text for the notification.
         *
         * @return the html text for the notification
         */
        public String getHtmlText() {
            return htmlText;
        }

        /**
         * Returns the view duration for the notification.
         *
         * @return the view duration for the notification
         */
        public int getViewDuration() {
            return viewDuration;
        }

        /**
         * Sets the view duration for the notification.
         *
         * @param viewDuration the view duration for the notification
         * @return this NotificationBuilder
         */
        @CanIgnoreReturnValue
        public NotificationBuilder setViewDuration(int viewDuration) {
            this.viewDuration = viewDuration;
            return this;
        }

        /**
         * Returns the arrow direction for the notification.
         *
         * @return the arrow direction for the notification
         */
        public Direction getArrowDir() {
            return arrowDir;
        }

        /**
         * Sets the arrow direction for the notification.
         *
         * @param arrowDir the arrow direction for the notification
         * @return this NotificationBuilder
         */
        @CanIgnoreReturnValue
        public NotificationBuilder setArrowDir(Direction arrowDir) {
            this.arrowDir = arrowDir;
            return this;
        }

        /**
         * Returns the on kill action for this notification.
         *
         * @return the on kill action for this notification
         */
        public Runnable getOnKillAction() {
            return onKillAction;
        }

        /**
         * Sets the on kill action for this notification.
         *
         * @param onKillAction the on kill action for this notification
         * @return this NotificationBuilder
         */
        @CanIgnoreReturnValue
        public NotificationBuilder setOnKillAction(Runnable onKillAction) {
            this.onKillAction = onKillAction;
            return this;
        }

        /**
         * Returns the notification direction for this notification.
         *
         * @return the notification direction for this notification
         */
        public NotificationDirection getNotificationDirection() {
            return notificationDirection;
        }

        /**
         * Sets the notification direction for this notification.
         *
         * @param notificationDirection the notification direction for this notification
         * @return this NotificationBuilder
         */
        @CanIgnoreReturnValue
        public NotificationBuilder setNotificationDirection(NotificationDirection notificationDirection) {
            this.notificationDirection = notificationDirection;
            return this;
        }

        /**
         * Returns the container for this notification.
         * This takes the place of the text container.
         *
         * @return the container for this notification
         */
        public JLabel getContainer() {
            return container;
        }

        /**
         * Sets the custom container for this notification.
         * This takes the place of the text container.
         *
         * @param container the JLabel container for this notification
         * @return this NotificationBuilder
         */
        public NotificationBuilder setContainer(JLabel container) {
            Preconditions.checkNotNull(container);
            Preconditions.checkArgument(container.getWidth() > 0);
            Preconditions.checkArgument(container.getHeight() > 0);

            this.container = container;
            return this;
        }

        /**
         * Returns the time at which this object was created.
         *
         * @return the time at which this object was created
         */
        public String getNotifyTime() {
            return notifyTime;
        }

        /**
         * Returns the notification type of this notification.
         *
         * @return the notification type of this notification
         */
        public CyderNotification.NotificationType getNotificationType() {
            return notificationType;
        }

        /**
         * Sets the notification type of this notification.
         *
         * @param notificationType the notification type of this notification
         * @return this NotificationBuilder
         */
        @CanIgnoreReturnValue
        public NotificationBuilder setNotificationType(CyderNotification.NotificationType notificationType) {
            this.notificationType = notificationType;
            return this;
        }

        /**
         * Returns whether the view duration should be auto-calculated.
         *
         * @return whether the view duration should be auto-calculated
         */
        public boolean isCalculateViewDuration() {
            return calculateViewDuration;
        }

        /**
         * Sets whether the view duration should be auto-calculated.
         *
         * @param calculateViewDuration whether the view duration should be auto-calculated
         * @return this builder
         */
        @CanIgnoreReturnValue
        public NotificationBuilder setCalculateViewDuration(boolean calculateViewDuration) {
            this.calculateViewDuration = calculateViewDuration;
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (o == null || getClass() != o.getClass()) {
                return false;
            }

            NotificationBuilder other = (NotificationBuilder) o;

            return viewDuration == other.viewDuration
                    && notifyTime.equals(other.notifyTime)
                    && Objects.equal(htmlText, other.htmlText)
                    && Objects.equal(onKillAction, other.onKillAction)
                    && notificationDirection == other.notificationDirection
                    && calculateViewDuration == other.calculateViewDuration
                    && notificationType == other.notificationType
                    && Objects.equal(container, other.container);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            int ret = Integer.hashCode(viewDuration);
            ret = 31 * ret + notifyTime.hashCode();
            ret = 31 * ret + htmlText.hashCode();
            ret = 31 * ret + arrowDir.hashCode();
            ret = 31 * ret + notificationDirection.hashCode();
            ret = 31 * ret + notificationType.hashCode();
            return ret;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "NotificationBuilder{"
                    + "htmlText=" + quote + htmlText + quote
                    + ", viewDuration=" + viewDuration
                    + ", arrowDir=" + arrowDir
                    + ", onKillAction=" + onKillAction
                    + ", notificationDirection=" + notificationDirection
                    + ", notificationType=" + notificationType
                    + ", container=" + container
                    + ", calculateViewDuration=" + calculateViewDuration
                    + ", notifyTime=" + quote + notifyTime + quote
                    + "}";
        }
    }
}