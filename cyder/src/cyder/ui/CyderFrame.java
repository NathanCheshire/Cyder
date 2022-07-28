package cyder.ui;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.console.Console;
import cyder.constants.*;
import cyder.enums.Direction;
import cyder.enums.NotificationDirection;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.InformHandler;
import cyder.handlers.internal.Logger;
import cyder.handlers.internal.LoginHandler;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;
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
import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * CyderFrame component is the primary backbone that all Cyder lies on.
 */
public class CyderFrame extends JFrame {
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
     * The possible button positions for a CyderFrame.
     */
    public enum ButtonPosition {
        LEFT,
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
     * This CyderFrame's button position.
     */
    private ButtonPosition buttonPosition = ButtonPosition.RIGHT;

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
    private JLayeredPane iconPane;

    /**
     * Speeds up performance by not repainting anything on t
     * he frame during animations such as minimize and close.
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

    /**
     * The degree angle increment used for the barrel roll animation.
     */
    public static final double BARREL_ROLL_DELTA = 2.0;

    /**
     * The maximum width of the title label ratio to the CyderFrame width.
     */
    public static final float MAX_TITLE_LENGTH_RATIO = 0.75f;

    /**
     * The default CyderFrame dimension.
     */
    public static final Dimension DEFAULT_DIMENSION = new Dimension(400, 400);

    /**
     * Allowable indices to add components to the contentLabel
     * which is a JLayeredPane and the content pane.
     */
    public static final ArrayList<Integer> allowableContentLabelIndices = new ArrayList<>() {{
        // Drag labels
        add(JLayeredPane.DRAG_LAYER);
        // notifications
        add(JLayeredPane.POPUP_LAYER);
    }};

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
     * @param size the size of the CyderFrame.
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
     * @param c      the color of the content pane background
     */
    public CyderFrame(int width, int height, Color c) {
        this(width, height, ImageUtil.imageIconFromColor(c,
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
        // ensure non null background
        checkNotNull(background);

        // correct possibly too small width and heights
        Dimension dimension = validateRequestedSize(width, height);

        width = dimension.width;
        height = dimension.height;

        this.width = width;
        this.height = height;

        // check to ensure background is same size as frame
        if (width > background.getIconWidth() || height > background.getIconHeight()) {
            background = ImageUtil.resizeImage(background, this.width, this.height);
        }

        this.background = background;
        currentOrigIcon = background;

        taskbarIconBorderColor = FrameUtil.getTaskbarBorderColor();

        setSize(new Dimension(width, height));
        setResizable(false);
        setUndecorated(true);
        setBackground(CyderColors.vanilla);
        setIconImage(CyderIcons.CYDER_ICON.getImage());

        //listener to ensure the close button was always pressed which ensures
        // things like closeAnimation are always performed
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        // master ContentLabel
        contentLabel = new JLayeredPane() {
            @Override
            public Component add(Component comp, int index) {
                if (allowableContentLabelIndices.contains(index)) {
                    return super.add(comp, index);
                }

                return super.add(comp, 0);
            }
        };
        contentLabel.setFocusable(false);

        //adding pane, this is what is returned when getContentPane() is called
        iconLabel = new JLabel() {
            @Override
            public void repaint() {
                //as long as we should repaint, repaint it
                if (!disableContentRepainting) {
                    super.repaint();
                }
            }
        };
        iconLabel.setBounds(FRAME_RESIZING_LEN, FRAME_RESIZING_LEN,
                width - 2 * FRAME_RESIZING_LEN, height - 2 * FRAME_RESIZING_LEN);
        iconLabel.setIcon(background);
        iconLabel.setFocusable(false);

        iconPane = new JLayeredPane();
        iconPane.setBounds(FRAME_RESIZING_LEN, FRAME_RESIZING_LEN,
                width - 2 * FRAME_RESIZING_LEN, height - 2 * FRAME_RESIZING_LEN);
        iconPane.add(iconLabel, JLayeredPane.DEFAULT_LAYER);
        iconPane.setFocusable(false);
        contentLabel.add(iconPane, JLayeredPane.DEFAULT_LAYER);

        contentLabel.setBorder(new LineBorder(CyderColors.getGuiThemeColor(), 3, false));
        setContentPane(contentLabel);

        //top frame drag and cover
        topDrag = new CyderDragLabel(width - 2 * FRAME_RESIZING_LEN,
                CyderDragLabel.DEFAULT_HEIGHT - 2, this);
        topDrag.setBounds(FRAME_RESIZING_LEN, FRAME_RESIZING_LEN,
                width - 2 * FRAME_RESIZING_LEN, CyderDragLabel.DEFAULT_HEIGHT - 2);
        topDrag.setXOffset(FRAME_RESIZING_LEN);
        topDrag.setYOffset(FRAME_RESIZING_LEN);
        contentLabel.add(topDrag, JLayeredPane.DRAG_LAYER);
        topDrag.setFocusable(false);

        topDragCover = new JLabel();
        topDragCover.setBounds(0, 0, width, 2);
        topDragCover.setBackground(CyderColors.getGuiThemeColor());
        topDragCover.setOpaque(true);
        contentLabel.add(topDragCover, JLayeredPane.DRAG_LAYER);

        //left frame drag  and cover
        leftDrag = new CyderDragLabel(5 - FRAME_RESIZING_LEN,
                height - FRAME_RESIZING_LEN - CyderDragLabel.DEFAULT_HEIGHT, this);
        leftDrag.setBounds(FRAME_RESIZING_LEN, CyderDragLabel.DEFAULT_HEIGHT,
                5 - FRAME_RESIZING_LEN, height - CyderDragLabel.DEFAULT_HEIGHT - FRAME_RESIZING_LEN);
        leftDrag.setXOffset(FRAME_RESIZING_LEN);
        leftDrag.setYOffset(CyderDragLabel.DEFAULT_HEIGHT);
        contentLabel.add(leftDrag, JLayeredPane.DRAG_LAYER);
        leftDrag.setFocusable(false);
        leftDrag.setButtonList(null);

        leftDragCover = new JLabel();
        leftDragCover.setBounds(0, 0, 2, height);
        leftDragCover.setBackground(CyderColors.getGuiThemeColor());
        leftDragCover.setOpaque(true);
        contentLabel.add(leftDragCover, JLayeredPane.DRAG_LAYER);

        //right frame drag and cover
        rightDrag = new CyderDragLabel(5 - FRAME_RESIZING_LEN,
                height - FRAME_RESIZING_LEN - CyderDragLabel.DEFAULT_HEIGHT, this);
        rightDrag.setBounds(width - 5, CyderDragLabel.DEFAULT_HEIGHT,
                5 - FRAME_RESIZING_LEN, height - CyderDragLabel.DEFAULT_HEIGHT - FRAME_RESIZING_LEN);
        rightDrag.setXOffset(width - 5);
        rightDrag.setYOffset(CyderDragLabel.DEFAULT_HEIGHT);
        contentLabel.add(rightDrag, JLayeredPane.DRAG_LAYER);
        rightDrag.setFocusable(false);
        rightDrag.setButtonList(null);

        rightDragCover = new JLabel();
        rightDragCover.setBounds(width - 2, 0, 2, height);
        rightDragCover.setBackground(CyderColors.getGuiThemeColor());
        rightDragCover.setOpaque(true);
        contentLabel.add(rightDragCover, JLayeredPane.DRAG_LAYER);

        //bottom frame drag  and cover
        bottomDrag = new CyderDragLabel(width - 2 * FRAME_RESIZING_LEN, 5 - FRAME_RESIZING_LEN, this);
        bottomDrag.setBounds(FRAME_RESIZING_LEN, height - 5, width - 4, 5 - FRAME_RESIZING_LEN);
        bottomDrag.setXOffset(FRAME_RESIZING_LEN);
        bottomDrag.setYOffset(height - 5);
        contentLabel.add(bottomDrag, JLayeredPane.DRAG_LAYER);
        bottomDrag.setFocusable(false);
        bottomDrag.setButtonList(null);

        bottomDragCover = new JLabel();
        bottomDragCover.setBounds(0, height - 2, width, 2);
        bottomDragCover.setBackground(CyderColors.getGuiThemeColor());
        bottomDragCover.setOpaque(true);
        contentLabel.add(bottomDragCover, JLayeredPane.DRAG_LAYER);

        //title label on drag label
        titleLabel = new JLabel("");
        titleLabel.setFont(DEFAULT_FRAME_TITLE_FONT);
        titleLabel.setForeground(CyderColors.vanilla);
        titleLabel.setOpaque(false);
        titleLabel.setFocusable(false);
        titleLabel.setVisible(true);
        topDrag.add(titleLabel);

        threadsKilled = false;
        setFrameType(frameType);

        revalidateFrameShape();

        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    // -----------------------------
    // Borderless frame constructors
    // -----------------------------

    /**
     * Generates and returns a borderless CyderFrame.
     * A drag listener is already attached to this but
     * the caller needs to handle how the frame will be disposed.
     *
     * @param width  the width of the frame.
     * @param height the height of the frame.
     * @return the borderless frame
     */
    public static CyderFrame generateBorderlessFrame(int width, int height, Color backgroundColor) {
        return new CyderFrame(new Dimension(width, height), backgroundColor);
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

        // master contentLabel
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

        //adding pane (getContentPane().add(component))
        iconLabel = new JLabel() {
            @Override
            public void repaint() {
                //as long as we should repaint, repaint it
                if (!disableContentRepainting) {
                    super.repaint();
                }
            }
        };
        iconLabel.setBounds(FRAME_RESIZING_LEN, FRAME_RESIZING_LEN,
                width - 2 * FRAME_RESIZING_LEN, height - 2 * FRAME_RESIZING_LEN);
        iconLabel.setFocusable(false);

        iconPane = new JLayeredPane();
        iconPane.setBounds(FRAME_RESIZING_LEN, FRAME_RESIZING_LEN,
                width - 2 * FRAME_RESIZING_LEN, height - 2 * FRAME_RESIZING_LEN);
        iconPane.add(iconLabel, JLayeredPane.DEFAULT_LAYER);
        iconPane.setFocusable(false);
        contentLabel.add(iconPane, JLayeredPane.DEFAULT_LAYER);
        setContentPane(contentLabel);

        CyderDragLabel masterDrag = new CyderDragLabel(width, height, this);
        masterDrag.setButtonList(null);
        masterDrag.setBackground(background);
        masterDrag.setBounds(0, 0, width, height);
        contentLabel.add(masterDrag, JLayeredPane.DRAG_LAYER);
        masterDrag.setFocusable(false);

        contentLabel.add(masterDrag);

        //default boolean values
        threadsKilled = false;

        revalidateFrameShape();

        Logger.log(Logger.Tag.OBJECT_CREATION, this);
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

    // --------------------------------
    // frame layouts
    // --------------------------------

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
     * Adds the provided CyderPanel on top of the content pane which is also resized on
     * CyderFrame resize events.
     *
     * @param cyderPanel the CyderPanel with an appropriate CyderLayout
     */
    public void setLayoutPanel(CyderPanel cyderPanel) {
        //removing a panel and setting it to null
        if (cyderPanel == null) {
            if (this.cyderPanel != null) {
                iconLabel.remove(this.cyderPanel);
            }

            this.cyderPanel = null;
            return;
        }

        this.cyderPanel = cyderPanel;
        //panel literally sits on top of contentPane() (iconLabel in CyderFrame's case)
        cyderPanel.setBounds(BORDER_LEN, CyderDragLabel.DEFAULT_HEIGHT, getWidth() - 2 * BORDER_LEN,
                getHeight() - CyderDragLabel.DEFAULT_HEIGHT - BORDER_LEN);
        iconLabel.add(cyderPanel);
        cyderPanel.repaint();
    }

    /**
     * Returns the components managed by the layout.
     *
     * @return the components managed by the layout
     * @throws IllegalStateException if no layout is associated with the name
     */
    public ArrayList<Component> getLayoutComponents() {
        checkNotNull(cyderPanel);
        checkNotNull(cyderPanel.getLayoutComponents());

        return cyderPanel.getLayoutComponents();
    }

    // ------------------------------------
    // frame positions based on enums
    // ------------------------------------

    /**
     * Sets the title position of the title label.
     * If the frame is visible, the label is animated to its destination.
     *
     * @param titlePosition the position for the title. See {@link CyderFrame#titlePosition}
     */
    public void setTitlePosition(TitlePosition titlePosition) {
        if (titlePosition == null || this.titlePosition == null)
            return;

        TitlePosition oldPosition = this.titlePosition;
        long timeout = 2;

        if (isVisible()) {
            if (titlePosition == CyderFrame.TitlePosition.LEFT) {
                CyderThreadRunner.submit(() -> {
                    //left
                    for (int i = titleLabel.getX() ; i > 4 ; i--) {
                        titleLabel.setLocation(i, 2);

                        try {
                            ThreadUtil.sleep(timeout);
                        } catch (Exception e) {
                            ExceptionHandler.handle(e);
                        }
                    }
                    titleLabel.setLocation(4, 2);
                    this.titlePosition = TitlePosition.LEFT;
                }, "title position animator");
            } else if (titlePosition == TitlePosition.CENTER) {
                CyderThreadRunner.submit(() -> {
                    switch (oldPosition) {
                        case RIGHT:
                            for (int i = titleLabel.getX() ; i > (topDrag.getWidth() / 2)
                                    - (StringUtil.getMinWidth(title, titleLabel.getFont()) / 2) ; i--) {
                                titleLabel.setLocation(i, 2);

                                ThreadUtil.sleep(timeout);
                            }
                            break;
                        case LEFT:
                            for (int i = titleLabel.getX() ; i < (topDrag.getWidth() / 2)
                                    - (StringUtil.getMinWidth(title, titleLabel.getFont()) / 2) ; i++) {
                                titleLabel.setLocation(i, 2);

                                ThreadUtil.sleep(timeout);
                            }
                            break;
                    }
                    titleLabel.setLocation((topDrag.getWidth() / 2)
                            - (StringUtil.getMinWidth(title, titleLabel.getFont()) / 2), 2);
                    this.titlePosition = TitlePosition.CENTER;
                    //set final bounds
                }, "title position animator");
            } else {
                //right
                CyderThreadRunner.submit(() -> {
                    for (int i = titleLabel.getX() ; i < width
                            - StringUtil.getMinWidth(title, titleLabel.getFont()) - 8 ; i++) {
                        titleLabel.setLocation(i, 2);

                        ThreadUtil.sleep(timeout);
                    }
                    titleLabel.setLocation(width
                            - StringUtil.getMinWidth(title, titleLabel.getFont()), 2);
                    this.titlePosition = TitlePosition.RIGHT;
                }, "title position animator");
            }

            if (buttonPosition == ButtonPosition.RIGHT && titlePosition == TitlePosition.RIGHT) {
                buttonPosition = ButtonPosition.LEFT;
                topDrag.setButtonPosition(CyderDragLabel.ButtonPosition.LEFT);
            } else if (buttonPosition == ButtonPosition.LEFT && titlePosition == TitlePosition.LEFT) {
                buttonPosition = ButtonPosition.RIGHT;
                topDrag.setButtonPosition(CyderDragLabel.ButtonPosition.RIGHT);
            }
        } else {
            this.titlePosition = titlePosition;

            switch (titlePosition) {
                case LEFT -> {
                    titleLabel.setLocation(4, 2);
                    setButtonPosition(ButtonPosition.RIGHT);
                }
                case RIGHT -> {
                    titleLabel.setLocation(width
                            - StringUtil.getMinWidth(title, titleLabel.getFont()), 2);
                    setButtonPosition(ButtonPosition.LEFT);
                }
                case CENTER -> titleLabel.setLocation((topDrag.getWidth() / 2)
                        - (StringUtil.getMinWidth(title, titleLabel.getFont()) / 2), 2);
            }

            titleLabel.setVisible(true);
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
     * Returns the button position of this frame.
     *
     * @return the button position of this frame
     */
    public ButtonPosition getButtonPosition() {
        return buttonPosition;
    }

    /**
     * Sets the button position of this frame.
     *
     * @param pos the position to set the button list to. See {@link CyderFrame#buttonPosition}
     */
    public void setButtonPosition(ButtonPosition pos) {
        if (pos == buttonPosition)
            return;

        buttonPosition = pos;
        topDrag.setButtonPosition(pos == ButtonPosition.LEFT ?
                CyderDragLabel.ButtonPosition.LEFT : CyderDragLabel.ButtonPosition.RIGHT);

        if (buttonPosition == ButtonPosition.RIGHT && titlePosition == TitlePosition.RIGHT) {
            setTitlePosition(TitlePosition.LEFT);
            titleLabel.setLocation(4, 2);
        } else if (buttonPosition == ButtonPosition.LEFT && titlePosition == TitlePosition.LEFT) {
            setTitlePosition(TitlePosition.RIGHT);
        }
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
        this.frameType = frameType;

        switch (this.frameType) {
            case DEFAULT -> setAlwaysOnTop(false);
            case POPUP -> {
                setAlwaysOnTop(true);
                //remove minimize
                topDrag.removeButton(0);
                //remove pin
                topDrag.removeButton(0);
            }
            case INPUT_GETTER -> {
                setAlwaysOnTop(true);
                //remove pin
                topDrag.removeButton(1);
            }
            default -> throw new IllegalStateException("Unimplemented state");
        }
    }

    // -------------
    // frame title
    // -------------

    /**
     * Whether to paint the title label on the top drag label.
     */
    private boolean paintCyderFrameTitle = true;

    /**
     * Whether to paint the CyderFrame's title label
     *
     * @param enable whether ot paint CyderFrame's title label
     */
    public void setPaintCyderFrameTitle(boolean enable) {
        paintCyderFrameTitle = enable;
    }

    /**
     * Returns whether the title label will be painted.
     *
     * @return whether the title label will be painted
     */
    public boolean getPaintCyderFrameTitle() {
        return paintCyderFrameTitle;
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
     * A triple dot to use for the title if it overflows
     */
    private static final String DOTS = "...";

    /**
     * Set the title of the label painted on the top drag label of the CyderFrame instance.
     * You can also configure the frame to paint/not paint both
     * the windowed title, and the title label title via {@link #setPaintSuperTitle(boolean)}
     * and {@link #setPaintCyderFrameTitle(boolean)}.
     *
     * @param title the String representing the chosen CyderFrame title
     */
    @Override
    public void setTitle(String title) {
        super.setTitle(paintSuperTitle ? title : "");

        if (titleLabel == null || StringUtil.isNull(title))
            return;

        if (paintCyderFrameTitle) {
            String parsedTitle = StringUtil.getTrimmedText(StringUtil.parseNonAscii(title));
            this.title = parsedTitle;

            Font titleLabelFont = titleLabel.getFont();

            int requestedWidth = StringUtil.getAbsoluteMinWidth(parsedTitle, titleLabelFont);
            int titleWidth = requestedWidth;
            String shortenedTitle = parsedTitle;

            while (titleWidth > width * MAX_TITLE_LENGTH_RATIO) {
                shortenedTitle = shortenedTitle.substring(0, shortenedTitle.length() - 1);
                titleWidth = StringUtil.getAbsoluteMinWidth(shortenedTitle + DOTS, titleLabelFont);
            }

            if (requestedWidth != titleWidth && !shortenedTitle.equals(parsedTitle)) {
                shortenedTitle = shortenedTitle.trim() + DOTS;
                titleWidth = StringUtil.getAbsoluteMinWidth(shortenedTitle, titleLabelFont);
            }

            titleLabel.setText(shortenedTitle);

            int titleLabelHeight = StringUtil.getAbsoluteMinHeight(shortenedTitle, titleLabelFont);

            switch (titlePosition) {
                case CENTER -> titleLabel.setBounds((topDrag.getWidth() / 2) - (titleWidth / 2),
                        2, titleWidth, titleLabelHeight);
                case RIGHT -> titleLabel.setBounds(width - titleWidth, 2, titleWidth, titleLabelHeight);
                case LEFT -> {
                    int start = 5;
                    int offset = 10;

                    int leftMostButtonX = Integer.MAX_VALUE;

                    if (topDrag != null) {
                        leftMostButtonX = topDrag.getButton(0).getX();
                    }

                    while (start + titleWidth + offset > leftMostButtonX) {
                        titleWidth -= offset;
                    }

                    titleLabel.setBounds(start, 2, titleWidth, titleLabelHeight);
                }
            }

            titleLabel.setVisible(true);
        }
    }

    // ------------------
    // Notifications
    // ------------------

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
        notify(new NotificationBuilder(htmlText));
    }

    /**
     * Notifies the user with a custom notification built from the provided builder.
     * See {@link NotificationBuilder} for more information.
     *
     * @param notificationBuilder the builder used to construct the notification
     */
    public void notify(NotificationBuilder notificationBuilder) {
        checkArgument(StringUtil.getRawTextLength(notificationBuilder.getHtmlText())
                > NotificationBuilder.MINIMUM_TEXT_LENGTH, "Raw text must be 3 characters or greater");

        notificationList.add(notificationBuilder);

        if (!notificationCheckerStarted) {
            notificationCheckerStarted = true;
            CyderThreadRunner.submit(notificationQueueRunnable, getTitle() + " notification queue checker");
        }
    }

    /**
     * Displays a simple toast with the provided text.
     *
     * @param htmlText the styled text to use for the toast
     */
    public void toast(String htmlText) {
        checkArgument(StringUtil.getRawTextLength(htmlText)
                > NotificationBuilder.MINIMUM_TEXT_LENGTH, "Raw text must be 3 characters or greater");

        NotificationBuilder toastBuilder = new NotificationBuilder(htmlText);
        toastBuilder.setNotificationType(CyderNotification.NotificationType.TOAST);

        notificationList.add(toastBuilder);

        if (!notificationCheckerStarted) {
            notificationCheckerStarted = true;
            CyderThreadRunner.submit(notificationQueueRunnable, getTitle() + " notification queue checker");
        }
    }

    /**
     * The semaphore used to lock the notification queue
     * so that only one may ever be present at a time.
     */
    private final Semaphore notificationConstructionLock = new Semaphore(1);

    /**
     * The number of pixels to add to the width and height of a notification's calculated bounds.
     */
    private static final int notificationExcessLen = 5;

    /**
     * The notification queue for internal frame notifications/toasts.
     */
    private final Runnable notificationQueueRunnable = () -> {
        // as long as threads aren't killed and we have notifications
        // to pull, loop
        while (!threadsKilled && !notificationList.isEmpty()) {
            // lock so that only one notification is visible at a time
            try {
                notificationConstructionLock.acquire();
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }

            // pull next notification to build
            NotificationBuilder currentBuilder = notificationList.remove(0);

            // init current notification object, needed
            // for builder access and to kill via revokes
            CyderNotification toBeCurrentNotification = new CyderNotification(currentBuilder);

            toBeCurrentNotification.setVisible(false);
            // ensure invisible to start

            // generate label for notification
            BoundsUtil.BoundsString bs = BoundsUtil.widthHeightCalculation(
                    currentBuilder.getHtmlText(),
                    NOTIFICATION_FONT, (int) Math.ceil(width * 0.8));
            int notificationWidth = bs.width() + notificationExcessLen;
            int notificationHeight = bs.height() + notificationExcessLen;
            String brokenText = bs.text();

            // if too wide, cannot notify so inform
            if (notificationHeight > height * NOTIFICATION_TO_FRAME_RATIO
                    || notificationWidth > width * NOTIFICATION_TO_FRAME_RATIO) {
                // inform original text
                inform(currentBuilder.getHtmlText(), "Notification ("
                        + currentBuilder.getNotifyTime() + ")");

                // release and continue with queue
                notificationConstructionLock.release();
                continue;
            }

            // if container specified, ensure it can fit
            if (currentBuilder.getContainer() != null) {
                int containerWidth = currentBuilder.getContainer().getWidth();
                int containerHeight = currentBuilder.getContainer().getHeight();

                // can't fit so we need to do a popup with the custom component
                if (containerWidth > width * NOTIFICATION_TO_FRAME_RATIO
                        || containerHeight > height * NOTIFICATION_TO_FRAME_RATIO) {
                    InformHandler.inform(new InformHandler.Builder("NULL")
                            .setContainer(currentBuilder.getContainer())
                            .setTitle(getTitle() + " Notification")
                            .setRelativeTo(this));

                    // done with actions so release and continue
                    notificationConstructionLock.release();
                    continue;
                }

                // we can show a custom container on the notification so add the dispose label
                JLabel interactionLabel = new JLabel();
                interactionLabel.setSize(containerWidth, containerHeight);
                interactionLabel.setToolTipText("Notified at: "
                        + toBeCurrentNotification.getBuilder().getNotifyTime());
                interactionLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        // fire the on kill actions
                        if (currentBuilder.getOnKillAction() != null) {
                            toBeCurrentNotification.kill();
                            currentBuilder.getOnKillAction().run();
                        }
                        // smoothly animate notification away
                        else {
                            toBeCurrentNotification.vanish(currentBuilder.getNotificationDirection(),
                                    getContentPane(), 0);
                        }
                    }
                });

                currentBuilder.getContainer().add(interactionLabel);
            }
            // if the container is empty, we are intended to generate a text label
            else {
                JLabel textContainerLabel = new JLabel(brokenText);
                textContainerLabel.setSize(notificationWidth, notificationHeight);
                textContainerLabel.setFont(NOTIFICATION_FONT);
                textContainerLabel.setForeground(CyderColors.notificationForegroundColor);

                JLabel interactionLabel = new JLabel();
                interactionLabel.setSize(notificationWidth, notificationHeight);
                interactionLabel.setToolTipText("Notified at: "
                        + toBeCurrentNotification.getBuilder().getNotifyTime());
                interactionLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        // fire the on kill action
                        if (currentBuilder.getOnKillAction() != null) {
                            toBeCurrentNotification.kill();
                            currentBuilder.getOnKillAction().run();
                        }
                        // smoothly animate notification away
                        else {
                            toBeCurrentNotification.vanish(currentBuilder.getNotificationDirection(),
                                    getContentPane(), 0);
                        }
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        textContainerLabel.setForeground(
                                CyderColors.notificationForegroundColor.darker());
                        toBeCurrentNotification.setHovered(true);
                        toBeCurrentNotification.repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        textContainerLabel.setForeground(CyderColors.notificationForegroundColor);
                        toBeCurrentNotification.setHovered(false);
                        toBeCurrentNotification.repaint();
                    }
                });

                textContainerLabel.add(interactionLabel);

                // now when building the notification component, we'll use
                // this as our container that we must build around
                toBeCurrentNotification.getBuilder().setContainer(textContainerLabel);
            }

            // add notification component to proper layer
            iconPane.add(toBeCurrentNotification, JLayeredPane.POPUP_LAYER);
            getContentPane().repaint();

            int duration = currentBuilder.getViewDuration();

            // if duration of 0 was passed, we should calculate it based on words
            if (duration == 0) {
                duration = 300 * StringUtil.countWords(
                        Jsoup.clean(bs.text(), Safelist.none()));
            }

            // failsafe to ensure notifications are at least four seconds
            duration = Math.max(duration, 4000);

            Logger.log(Logger.Tag.UI_ACTION, "[" +
                    getTitle() + "] [NOTIFICATION] \"" + brokenText + "\"");

            // notification itself handles itself appearing, pausing, and vanishing
            toBeCurrentNotification.appear(currentBuilder.getNotificationDirection(),
                    getContentPane(), duration);

            currentNotification = toBeCurrentNotification;

            // when the notification is killed/vanishes, it sets itself
            // to killed; this loop will exit after
            while (!currentNotification.isKilled()) {
                Thread.onSpinWait();
            }

            notificationConstructionLock.release();
        }

        // the above while isn't checking so it needs
        // to be started again for new notifications
        notificationCheckerStarted = false;
    };

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
     * @param expectedText the text of the notification to revoke.
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
        if (currentNotification != null)
            currentNotification.kill();

        if (notificationList != null)
            notificationList.clear();

        notificationCheckerStarted = false;
    }

    // -------------
    // drag labels
    // -------------

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
     * is set to the {@link Frame#ICONIFIED} state.
     */
    public void minimizeAnimation() {
        try {
            // if we are the Console, save position vars
            if (this == Console.INSTANCE.getConsoleCyderFrame())
                Console.INSTANCE.saveScreenStat();

            //set restore vars here
            setRestoreX(getX());
            setRestoreY(getY());

            if (UserUtil.getCyderUser().getDoAnimations().equals("1")) {
                setDisableContentRepainting(true);

                int animationInc = (int) ((double) (ScreenUtil.getScreenHeight() - getY()) / ANIMATION_FRAMES);

                for (int i = getY() ; i <= ScreenUtil.getScreenHeight() + getHeight() ; i += animationInc) {
                    ThreadUtil.sleep(MOVEMENT_ANIMATION_DELAY);
                    setLocation(getX(), i);

                    if (i >= ScreenUtil.getScreenHeight()) {
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
    @Override // disable/enable content area repainting for optimization
    public void setState(int state) {
        if (state == JFrame.ICONIFIED) {
            setDisableContentRepainting(true);
        } else if (state == JFrame.NORMAL) {
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
        CyderThreadRunner.submit(() -> {
            try {
                if (disposed) {
                    return;
                }

                if (closingConfirmationMessage != null) {
                    boolean exit = GetterUtil.getInstance().getConfirmation(
                            new GetterUtil.Builder("Confirmation")
                                    .setInitialString(closingConfirmationMessage)
                                    .setRelativeTo(this)
                                    .setDisableRelativeTo(true));

                    if (!exit) {
                        return;
                    }
                }

                disposed = true;

                Logger.log(Logger.Tag.UI_ACTION, "CyderFrame disposed with fastclose="
                        + fastClose + ", getTitle=" + getTitle());

                for (Runnable action : preCloseActions) {
                    action.run();
                }

                if (currentNotification != null) {
                    currentNotification.kill();
                }

                //kill all threads
                killThreads();

                //disable dragging
                disableDragging();

                // disable content pane repainting not paint to speed up the animation
                setDisableContentRepainting(true);

                if (isVisible() && (!fastClose && !shouldFastClose)
                        && UserUtil.getCyderUser().getDoAnimations().equals("1")) {
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

                //remove from Console
                Console.INSTANCE.removeTaskbarIcon(this);

                super.dispose();

                for (Runnable action : postCloseActions) {
                    action.run();
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);

                // failsafe
                super.dispose();
            }
        }, "[" + getTitle() + "] dispose() animation thread");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        dispose(false);
    }

    /**
     * Whether to allow the frame to be relocated via dragging.
     *
     * @param relocatable whether to allow the frame to be relocated via dragging.
     */
    public void setRelocatable(boolean relocatable) {
        if (relocatable) {
            enableDragging();
        } else {
            disableDragging();
        }
    }

    // -----------
    // dancing
    // -----------

    /**
     * The directions for frame dancing.
     */
    public enum DancingDirection {
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
     * Takes a step in the current dancing direction for a dance routine.
     */
    public void danceStep() {
        int dancingIncrement = 10;
        switch (dancingDirection) {
            case INITIAL_UP -> {
                setLocation(getX(), getY() - dancingIncrement);
                if (getY() < 0) {
                    setLocation(getX(), 0);
                    dancingDirection = DancingDirection.LEFT;
                }
            }
            case LEFT -> {
                setLocation(getX() - 10, getY());
                if (getX() < 0) {
                    setLocation(0, 0);
                    dancingDirection = DancingDirection.DOWN;
                }
            }
            case DOWN -> {
                setLocation(getX(), getY() + 10);
                if (getY() > ScreenUtil.getScreenHeight() - getHeight()) {
                    setLocation(getX(), ScreenUtil.getScreenHeight() - getHeight());
                    dancingDirection = DancingDirection.RIGHT;
                }
            }
            case RIGHT -> {
                setLocation(getX() + 10, getY());
                if (getX() > ScreenUtil.getScreenWidth() - getWidth()) {
                    setLocation(ScreenUtil.getScreenWidth() - getWidth(), getY());
                    dancingDirection = DancingDirection.UP;
                }
            }
            case UP -> {
                setLocation(getX(), getY() - 10);
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
        ImageIcon masterIcon = currentOrigIcon;
        BufferedImage master = ImageUtil.getBufferedImage(masterIcon);
        BufferedImage rotated = ImageUtil.rotateImage(master, degrees);
        ((JLabel) getContentPane()).setIcon(new ImageIcon(rotated));
    }

    /**
     * The delay in ms between barrel roll increments.
     */
    private static final int BARREL_ROLL_DELAY = 10;

    /**
     * transforms the content pane by an incremental angle of 2 degrees
     * emulating Google's barrel roll easter egg.
     */
    public void barrelRoll() {
        ImageIcon masterIcon = (ImageIcon) ((JLabel) getContentPane()).getIcon();
        BufferedImage master = ImageUtil.getBufferedImage(masterIcon);

        new Timer(BARREL_ROLL_DELAY, new ActionListener() {
            private double angle;

            BufferedImage rotated;

            @Override
            public void actionPerformed(ActionEvent e) {
                angle += BARREL_ROLL_DELTA;

                if (angle >= 360) {
                    setBackground(masterIcon);
                    return;
                }

                if (threadsKilled) {
                    return;
                }

                rotated = ImageUtil.rotateImage(master, angle);
                ((JLabel) getContentPane()).setIcon(new ImageIcon(rotated));
            }
        }).start();
    }

    /**
     * Revalidates the title and button positions by the currently set enum locations.
     */
    public void revalidateTitleAndButtonPosition() {
        switch (titlePosition) {
            case LEFT -> titleLabel.setLocation(4, 2);
            case RIGHT -> titleLabel.setLocation(width -
                    StringUtil.getMinWidth(title, titleLabel.getFont()), 2);
            case CENTER -> titleLabel.setLocation((topDrag.getWidth() / 2)
                    - (StringUtil.getMinWidth(title, titleLabel.getFont()) / 2), 2);
        }

        switch (buttonPosition) {
            case LEFT -> topDrag.setButtonPosition(CyderDragLabel.ButtonPosition.LEFT);
            case RIGHT -> topDrag.setButtonPosition(CyderDragLabel.ButtonPosition.RIGHT);
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
            Logger.log(Logger.Tag.DEBUG, "CyderFrame \"" + title
                    + "\" was attempted to be set to invalid width: " + width);
            width = MINIMUM_WIDTH;
        }

        if (height < MINIMUM_HEIGHT) {
            Logger.log(Logger.Tag.DEBUG, "CyderFrame \"" + title
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

        //update the border covering the resize area
        contentLabel.setBorder(new LineBorder(
                CyderColors.getGuiThemeColor(), 5 - FRAME_RESIZING_LEN, false));

        if (topDrag != null) {
            //update drag labels
            topDrag.setBackground(CyderColors.getGuiThemeColor());
            bottomDrag.setBackground(CyderColors.getGuiThemeColor());
            leftDrag.setBackground(CyderColors.getGuiThemeColor());
            rightDrag.setBackground(CyderColors.getGuiThemeColor());
            topDragCover.setBackground(CyderColors.getGuiThemeColor());
            bottomDragCover.setBackground(CyderColors.getGuiThemeColor());
            leftDragCover.setBackground(CyderColors.getGuiThemeColor());
            rightDragCover.setBackground(CyderColors.getGuiThemeColor());

            //repaint drag labels
            topDrag.repaint();
            leftDrag.repaint();
            bottomDrag.repaint();
            rightDrag.repaint();
        }

        // update content panes
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

        if (menuLabel != null && menuLabel.isVisible()) {
            generateMenu();
            menuLabel.setLocation(animateMenuToPoint);
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

        if (menuLabel != null && menuLabel.isVisible()) {
            generateMenu();
            menuLabel.setLocation(animateMenuToPoint);
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
        topDragCover.setBounds(0, 0, width, 2);

        leftDrag.setWidth(5 - FRAME_RESIZING_LEN);
        leftDrag.setHeight(height - CyderDragLabel.DEFAULT_HEIGHT - FRAME_RESIZING_LEN);
        leftDragCover.setBounds(0, 0, FRAME_RESIZING_LEN, height);

        rightDrag.setWidth(5 - FRAME_RESIZING_LEN);
        rightDrag.setHeight(height - CyderDragLabel.DEFAULT_HEIGHT - FRAME_RESIZING_LEN);
        rightDragCover.setBounds(width - FRAME_RESIZING_LEN, 0, FRAME_RESIZING_LEN, height);

        bottomDrag.setWidth(width - FRAME_RESIZING_LEN * 2);
        bottomDrag.setHeight(5 - FRAME_RESIZING_LEN);
        bottomDragCover.setBounds(0, height - FRAME_RESIZING_LEN, width, FRAME_RESIZING_LEN);

        revalidateTitleAndButtonPosition();

        topDrag.setBounds(FRAME_RESIZING_LEN, FRAME_RESIZING_LEN, width - 2 * FRAME_RESIZING_LEN,
                CyderDragLabel.DEFAULT_HEIGHT - FRAME_RESIZING_LEN);
        leftDrag.setBounds(FRAME_RESIZING_LEN, CyderDragLabel.DEFAULT_HEIGHT, 5 - FRAME_RESIZING_LEN,
                height - CyderDragLabel.DEFAULT_HEIGHT - FRAME_RESIZING_LEN);
        rightDrag.setBounds(width - 5, CyderDragLabel.DEFAULT_HEIGHT,
                5 - FRAME_RESIZING_LEN, height - CyderDragLabel.DEFAULT_HEIGHT - 2);
        bottomDrag.setBounds(FRAME_RESIZING_LEN, height - 5,
                width - 2 * FRAME_RESIZING_LEN, 5 - FRAME_RESIZING_LEN);

        topDrag.setXOffset(FRAME_RESIZING_LEN);
        topDrag.setYOffset(FRAME_RESIZING_LEN);

        leftDrag.setXOffset(FRAME_RESIZING_LEN);
        leftDrag.setYOffset(CyderDragLabel.DEFAULT_HEIGHT);

        rightDrag.setXOffset(width - 5);
        rightDrag.setYOffset(CyderDragLabel.DEFAULT_HEIGHT);

        bottomDrag.setXOffset(FRAME_RESIZING_LEN);
        bottomDrag.setYOffset(height - 5);
    }

    /**
     * The arc length of the arc for rounded window shapes.
     */
    private static final int ROUNDED_ARC = 20;

    /**
     * Revalidates and updates the frame's shape, that of being rounded or square.
     */
    private void revalidateFrameShape() {
        if (!isUndecorated())
            return;

        Shape shape = null;

        try {
            // borderless frames are by default rounded
            if (isBorderlessFrame() || (cr == null && Console.INSTANCE.getUuid() != null
                    && UserUtil.getCyderUser().getRoundedwindows().equals("1"))) {
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
        if (getCurrentNotification() != null) {
            switch (getCurrentNotification().getBuilder().getArrowDir()) {
                // center on frame
                case TOP, BOTTOM -> currentNotification.setLocation(
                        getWidth() / 2 - currentNotification.getWidth() / 2,
                        currentNotification.getY());

                // maintain right of frame
                case RIGHT -> currentNotification.setLocation(
                        getWidth() - currentNotification.getWidth() + 5,
                        currentNotification.getY());

                // maintain left of frame
                case LEFT -> currentNotification.setLocation(5, currentNotification.getY());
            }
        }
    }

    /**
     * The minimum gap between the title label and the button list.
     */
    private static final int TITLE_BUTTONS_MIN_GAP = 10;

    /**
     * Checks for the title label overflowing onto the drag label buttons and clips the label
     * if the width is too long. If the label does not overflow and the entire title is
     * not shown on the title label, an attempt is made to fit more of the set title on the title label.
     */
    private void correctTitleLength() {
        if (isBorderlessFrame() || topDrag.getButtonList() == null) {
            return;
        }

        LinkedList<JButton> buttons = topDrag.getButtonList();

        int buttonRightBoundsEnd = 0;
        int buttonLeftBoundsStart = 0;

        for (JButton button : buttons) {
            buttonLeftBoundsStart = Math.min(buttonLeftBoundsStart, button.getX());
            buttonRightBoundsEnd = Math.max(buttonRightBoundsEnd, button.getX() + button.getWidth());
        }

        switch (buttonPosition) {
            case LEFT -> {
                // Ensure title label doesn't start before buttonRightBoundsEnd + gap
                if (buttonRightBoundsEnd + TITLE_BUTTONS_MIN_GAP >= titleLabel.getX()) {
                    int x = buttonRightBoundsEnd + TITLE_BUTTONS_MIN_GAP;
                    int width = this.getWidth() - buttonRightBoundsEnd + TITLE_BUTTONS_MIN_GAP - 4;

                    titleLabel.setBounds(x, titleLabel.getY(), width, titleLabel.getHeight());
                }
            }
            case RIGHT -> {
                // Ensure title label doesn't end inside of button's area
                if (titleLabel.getX() + titleLabel.getWidth() + TITLE_BUTTONS_MIN_GAP >= buttonLeftBoundsStart) {
                    int width = this.getWidth() - buttonLeftBoundsStart - TITLE_BUTTONS_MIN_GAP - titleLabel.getX();
                    titleLabel.setBounds(titleLabel.getX(), titleLabel.getY(), width, titleLabel.getHeight());
                }
            }
            default -> throw new IllegalArgumentException("Invalid button position: " + buttonPosition);
        }

        setTitle(this.title);
    }

    /**
     * Returns the absolute minimum size for the title label based on the currently set text and font.
     *
     * @return the absolute minimum size for the title label based on the currently set text and font
     */
    public Dimension getTitleLabelSize() {
        String text = titleLabel.getText();
        Font font = titleLabel.getFont();
        return new Dimension(StringUtil.getAbsoluteMinWidth(text, font), StringUtil.getAbsoluteMinHeight(title, font));
    }

    /**
     * The minimum allowable width for a CyderFrame.
     */
    public static final int MINIMUM_WIDTH = 200;

    /**
     * The maximum allowable height for a CyderFrame.
     */
    public static final int MINIMUM_HEIGHT = 100;

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
        if (cr != null)
            cr.deregisterComponent(this);

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
     * The original image icon to use for image resizing on resizing events if allowed.
     */
    private ImageIcon currentOrigIcon;

    /**
     * Whether the frame is resizable.
     *
     * @param allow whether the frame is resizable
     */
    @Override
    public void setResizable(boolean allow) {
        if (cr != null)
            cr.setResizingAllowed(allow);
    }

    /**
     * Refresh the background in the event of a frame size change or a background image change.
     */
    public void refreshBackground() {
        try {
            if (iconLabel == null)
                return;

            // mainly needed for icon label and pane bounds, layout isn't usually expensive
            revalidateLayout();

            if (cr != null && cr.backgroundResizingEnabled()) {
                iconLabel.setIcon(new ImageIcon(currentOrigIcon.getImage()
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
     * Set the background to a new icon and revalidates and repaints the frame.
     *
     * @param icon the ImageIcon of the frame's background
     */
    public void setBackground(ImageIcon icon) {
        try {
            //prevent errors before instantiation of ui objects
            if (iconLabel == null)
                return;

            currentOrigIcon = icon;
            iconLabel.setIcon(new ImageIcon(currentOrigIcon.getImage()
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
        closingConfirmationMessage = message;
    }

    /**
     * Removes any closing confirmation messages set.
     */
    public void removeClosingConfirmation() {
        closingConfirmationMessage = null;
    }

    // ---------------
    // pinning logic
    // ---------------

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
     * Whether the frame should be pinned on top.
     */
    private boolean pinned;

    /**
     * Sets the value for pinning the frame on top.
     *
     * @param pinWindow whether the frame is always on top
     */
    public void setPinned(boolean pinWindow) {
        pinned = pinWindow;
        setAlwaysOnTop(pinned);
    }

    /**
     * Returns whether the frame is pinned.
     *
     * @return whether the frame is pinned
     */
    public boolean getPinned() {
        return pinned;
    }

    /**
     * Whether the frame is pinned on top AND pinned to the console.
     */
    private boolean consolePinned;

    /**
     * Returns whether the frame should be pinned to the console.
     *
     * @return whether the frame should be pinned to the console
     */
    public boolean isConsolePinned() {
        return consolePinned;
    }

    /**
     * Sets whether the frame should be pinned to the console.
     *
     * @param consolePinned whether the frame should be pinned to the console
     */
    public void setConsolePinned(boolean consolePinned) {
        this.consolePinned = consolePinned;
        setAlwaysOnTop(this.consolePinned);
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
            Console.INSTANCE.addTaskbarIcon(this);
        }

        // if the console is set to always on top, then we need this frame
        // to be automatically set on top as well so that new frames are not behind the console
        if (visible && Console.INSTANCE.getConsoleCyderFrame() != null &&
                Console.INSTANCE.getConsoleCyderFrame().isAlwaysOnTop()) {
            setAlwaysOnTop(true);

            if (topDrag != null) {
                topDrag.refreshPinButton();
            }
        }
    }

    // -----------
    // debug lines
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

    private static final ImageIcon neffexIcon = new ImageIcon("static/pictures/print/neffex.png");

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
            debugXLabel.setBounds(getWidth() / 2 - 2, 0, 4, getHeight());
            debugXLabel.setOpaque(true);
            debugXLabel.setBackground(lineColor);
            add(debugXLabel);

            debugYLabel = new JLabel();
            debugYLabel.setBounds(0, getHeight() / 2 - 2, getWidth(), 4);
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
                .getIDstring().replaceAll("[^0-9]", ""));
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
     * Sets the center point of the frame to the provided point.
     *
     * @param p the center point of the frame
     */
    public void setCenterPoint(Point p) {
        checkNotNull(p);
        setLocation(p.x - getWidth() / 2, p.y - getHeight() / 2);
    }

    // ---------------------------------
    // Transparency during drag events
    // ---------------------------------

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
    protected void startDragEvent() {
        if (!shouldAnimateOpacity) {
            return;
        }

        CyderThreadRunner.submit(() -> {
            for (float i = DEFAULT_OPACITY ; i >= DRAG_OPACITY ; i -= OPACITY_DELTA) {
                if (animatingOut)
                    break;

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
    protected void endDragEvent() {
        if (!shouldAnimateOpacity) {
            // just to be sure...
            setOpacity(DEFAULT_OPACITY);
            return;
        }

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
        if (currentMenuType == this.menuType)
            return;

        this.menuType = currentMenuType;

        if (menuEnabled) {
            boolean wasVisible = menuLabel != null && menuLabel.isVisible();
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
            boolean wasVisible = menuLabel != null && menuLabel.isVisible();
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
        if (menuLabel != null && menuLabel.isVisible()) {
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

    private final MouseListener titleLabelListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (menuEnabled) {
                if (menuLabel == null)
                    generateMenu();

                if (menuLabel.isVisible()) {
                    animateMenuOut();
                } else {
                    animateMenuIn();
                }
            } else {
                super.mouseClicked(e);
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            if (menuEnabled) {
                titleLabel.setForeground(CyderColors.regularRed);
            } else {
                super.mouseEntered(e);
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            if (menuEnabled) {
                titleLabel.setForeground(CyderColors.vanilla);
            } else {
                super.mouseExited(e);
            }
        }
    };

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
                titleLabel.addMouseListener(titleLabelListener);
            }

            return ret;
        }

        @Override
        public boolean remove(Object o) {
            boolean ret = super.remove(o);

            if (menuItems.isEmpty()) {
                titleLabel.removeMouseListener(titleLabelListener);
            }

            return ret;
        }
    };

    /**
     * Clears all menu items from the frame menu.
     */
    public void clearMenuItems() {
        menuItems.clear();
        titleLabel.removeMouseListener(titleLabelListener);
    }

    /**
     * The maximum text length allowable for menu items.
     */
    private static final int maxTextLength = 13;

    /**
     * Removes the menu item with the provided text.
     * If multiple menu items are found with the same text,
     * the first one is removed.
     *
     * @param text the text of the menu item to remove
     */
    public void removeMenuItem(String text) {
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
     * Adds a new menu item to the menu and revalidates the menu.
     *
     * @param text    the text for the menu label
     * @param onClick the function to run upon clicking
     * @param state   the atomic boolean used to dictate the toggled/not toggled state of the menu item if necessary
     */
    public void addMenuItem(String text, Runnable onClick, AtomicBoolean state) {
        checkNotNull(text);
        checkArgument(!text.isEmpty());
        checkNotNull(onClick);

        // just to be safe
        text = text.trim();

        // account for possible overflow in clean way
        if (text.length() > maxTextLength) {
            text = (text.substring(0, maxTextLength - 3).trim() + "...");
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
                newLabel.setForeground(state != null && state.get() ? CyderColors.vanilla : CyderColors.regularRed);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                newLabel.setForeground(state != null && state.get() ? CyderColors.regularRed : CyderColors.vanilla);
            }
        });
        menuItems.add(new MenuItem(newLabel, state));

        // regenerate if menu is already visible
        if (menuLabel != null && menuLabel.isVisible()) {
            generateMenu();
            menuLabel.setVisible(true);
            menuLabel.setLocation(animateMenuToPoint);
        }
    }

    /**
     * The point at which the menu is placed when visible.
     */
    private final Point animateMenuToPoint = new Point(5 - 2, CyderDragLabel.DEFAULT_HEIGHT - 2);

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
            titleLabel.addMouseListener(titleLabelListener);
        } else {
            titleLabel.removeMouseListener(titleLabelListener);
        }
    }

    /**
     * Shows the menu in the currently set location as defined by {@link MenuType}.
     */
    public void showMenu() {
        if (menuLabel == null) {
            generateMenu();
        }

        menuLabel.setLocation(animateMenuToPoint);
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
                    menuLabel.setLocation(-menuLabel.getWidth(), animateMenuToPoint.getLocation().y);
                    menuLabel.setVisible(true);
                    for (int x = menuLabel.getX() ; x < animateMenuToPoint.x ; x += menuAnimationInc) {
                        menuLabel.setLocation(x, menuLabel.getY());
                        ThreadUtil.sleep(menuAnimationDelay);
                    }
                } else {
                    menuLabel.setLocation(animateMenuToPoint.x,
                            animateMenuToPoint.y - menuLabel.getHeight());
                    menuLabel.setVisible(true);
                    for (int y = menuLabel.getY() ; y <= animateMenuToPoint.y ; y += menuAnimationInc) {
                        menuLabel.setLocation(animateMenuToPoint.x, y);
                        ThreadUtil.sleep(menuAnimationDelay);
                    }
                }

                menuLabel.setLocation(animateMenuToPoint);
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

        if (menuLabel.getX() + menuLabel.getWidth() < 0
                && menuLabel.getY() + menuLabel.getHeight() < 0) {
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
                    menuLabel.setLocation(animateMenuToPoint.x, animateMenuToPoint.y);
                    for (int y = menuLabel.getY() ; y >= animateMenuToPoint.y - menuLabel.getHeight()
                            ; y -= menuAnimationInc) {
                        menuLabel.setLocation(animateMenuToPoint.x, y);
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
            // overridden to disable vertical scrollbar since setting
            // the policy doesn't work apparently, thanks JDK devs
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
                    printingUtil.print("\n");
                }
            }
        } else {
            for (int i = 0 ; i < menuItems.size() ; i++) {
                printingUtil.printComponent(menuItems.get(i).label());

                if (i != menuItems.size() - 1) {
                    printingUtil.print(StringUtil.generateNSpaces(4));
                }
            }
        }

        menuPane.setCaretPosition(0);
        menuLabel.setVisible(false);
        menuLabel.setLocation(-menuWidth, animateMenuToPoint.y);
        getIconPane().add(menuLabel, JLayeredPane.MODAL_LAYER);
    }

    /**
     * The increment between y values for the enter animation.
     */
    public static final int ENTER_ANIMATION_INC = 25;

    /**
     * The delay in nanoseconds between enter animation increments.
     */
    public static final int ENTER_ANIMATION_DELAY = 75;

    /**
     * Sets the frame's location relative to the dominant frame,
     * the visibility to true, and sets always on top mode to true
     * temporarily to ensure the frame is placed on top of other possible frames.
     */
    public void finalizeAndShow() {
        finalizeAndShow(false);
    }

    /**
     * Sets the frame's location relative to the dominant frame,
     * the visibility to true, and sets always on top mode to true
     * temporarily to ensure the frame is placed on top of other possible frames.
     *
     * @param enterAnimation whether to perform an enter animation on the frame
     */
    public void finalizeAndShow(boolean enterAnimation) {
        CyderThreadRunner.submit(() -> {
            CyderFrame dominantFrame = getDominantFrame();

            if (enterAnimation) {
                if (dominantFrame == null) {
                    setLocation(ScreenUtil.getScreenWidth() / 2 - getWidth() / 2, -getHeight());
                } else {
                    setLocation(dominantFrame.getX() + dominantFrame.getWidth() / 2 - getWidth() / 2, -getHeight());
                }

                setVisible(true);

                int toY;
                if (dominantFrame == null) {
                    toY = ScreenUtil.getScreenHeight() / 2 - getHeight() / 2;
                } else {
                    toY = dominantFrame.getY() + dominantFrame.getHeight() / 2 - getHeight() / 2;
                }

                enterAnimation(new Point(getX(), toY));
            } else {
                setVisible(true);
            }

            setLocationRelativeTo(dominantFrame);

        }, "Enter animation, frame=" + getTitle());
    }

    // todo console location saving doesn't work and sometimes messes up still, only save is not being disposed too

    /**
     * Performs an enter animation to the point.
     *
     * @param point the point to position the frame at after performing the enter animation
     */
    public void enterAnimation(Point point) {
        Preconditions.checkNotNull(point);
        setLocation(point.x, -getHeight());

        setLocation(getX(), -getHeight());
        setVisible(true);

        for (int i = -getHeight() ; i < point.getY() ; i += ENTER_ANIMATION_INC) {
            setLocation(point.x, i);
            ThreadUtil.sleep(0, ENTER_ANIMATION_DELAY);
        }
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
            return referenceFrame.getState() == ICONIFIED ? null : referenceFrame;
        } else if (!LoginHandler.isLoginFrameClosed()
                && LoginHandler.getLoginFrame() != null
                && LoginHandler.getLoginFrame().isVisible()) {
            return LoginHandler.getLoginFrame();
        }
        // other possibly dominant/stand-alone frame checks here
        else {
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
        switch (screenPos) {
            case CENTER, MIDDLE -> setLocationRelativeTo(null);
            case TOP_LEFT -> setLocation(0, 0);
            case TOP_RIGHT -> setLocation(ScreenUtil.getScreenWidth() - getWidth(), 0);
            case BOTTOM_LEFT -> setLocation(0, ScreenUtil.getScreenHeight() - getHeight());
            case BOTTOM_RIGHT -> setLocation(ScreenUtil.getScreenWidth() - getWidth(),
                    ScreenUtil.getScreenHeight() - getHeight());
        }
    }

    /**
     * A CyderFrame menu item.
     * This record is to associate a label with a possible
     * AtomicBoolean which dictates the state of the menu item.
     */
    private record MenuItem(JLabel label, AtomicBoolean state) {}

    /**
     * A builder for a CyderFrame notification.
     */
    @CanIgnoreReturnValue
    public static final class NotificationBuilder {
        /**
         * The minimum allowable char length for a notification.
         */
        public static final int MINIMUM_TEXT_LENGTH = 2;

        // -------------------
        // Required parameters
        // -------------------

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
            Preconditions.checkArgument(htmlText.length() >= MINIMUM_TEXT_LENGTH,
                    "HTML text length is less than " + MINIMUM_TEXT_LENGTH);

            this.htmlText = htmlText;

            notifyTime = TimeUtil.notificationTime();

            Logger.log(Logger.Tag.OBJECT_CREATION, this);
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
        public NotificationBuilder setNotificationType(CyderNotification.NotificationType notificationType) {
            this.notificationType = notificationType;
            return this;
        }

        // -----------------------------------------------
        // Methods to override according to Effective Java
        // -----------------------------------------------

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

            NotificationBuilder that = (NotificationBuilder) o;

            return viewDuration == that.viewDuration
                    && notifyTime.equals(that.notifyTime)
                    && Objects.equal(htmlText, that.htmlText)
                    && Objects.equal(onKillAction, that.onKillAction)
                    && notificationDirection == that.notificationDirection
                    && notificationType == that.notificationType
                    && Objects.equal(container, that.container);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return Objects.hashCode(
                    viewDuration,
                    notifyTime,
                    htmlText,
                    arrowDir,
                    onKillAction,
                    notificationDirection,
                    notificationType,
                    container);
        }
    }
}