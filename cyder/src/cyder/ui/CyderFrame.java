package cyder.ui;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.builders.GetterBuilder;
import cyder.builders.InformBuilder;
import cyder.builders.NotificationBuilder;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderIcons;
import cyder.constants.CyderNumbers;
import cyder.handlers.ConsoleFrame;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.InformHandler;
import cyder.handlers.internal.Logger;
import cyder.handlers.internal.LoginHandler;
import cyder.threads.CyderThreadRunner;
import cyder.utilities.*;
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
import java.awt.image.RescaleOp;
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
    private static final int frameResizingLen = 2;

    /**
     * The size of the border drawn around the frame.
     */
    public static final int borderLen = 5;

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

        //border color for ConsoleFrame menu pane set in instantiation of object
        taskbarIconBorderColor = getTaskbarBorderColor();

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
        iconLabel.setBounds(frameResizingLen, frameResizingLen,
                width - 2 * frameResizingLen, height - 2 * frameResizingLen);
        iconLabel.setIcon(background);
        iconLabel.setFocusable(false);

        iconPane = new JLayeredPane();
        iconPane.setBounds(frameResizingLen, frameResizingLen,
                width - 2 * frameResizingLen, height - 2 * frameResizingLen);
        iconPane.add(iconLabel, JLayeredPane.DEFAULT_LAYER);
        iconPane.setFocusable(false);
        contentLabel.add(iconPane, JLayeredPane.DEFAULT_LAYER);

        contentLabel.setBorder(new LineBorder(CyderColors.getGuiThemeColor(), 3, false));
        setContentPane(contentLabel);

        //top frame drag and cover
        topDrag = new CyderDragLabel(width - 2 * frameResizingLen,
                CyderDragLabel.DEFAULT_HEIGHT - 2, this);
        topDrag.setBounds(frameResizingLen, frameResizingLen,
                width - 2 * frameResizingLen, CyderDragLabel.DEFAULT_HEIGHT - 2);
        topDrag.setXOffset(frameResizingLen);
        topDrag.setYOffset(frameResizingLen);
        contentLabel.add(topDrag, JLayeredPane.DRAG_LAYER);
        topDrag.setFocusable(false);

        topDragCover = new JLabel();
        topDragCover.setBounds(0, 0, width, 2);
        topDragCover.setBackground(CyderColors.getGuiThemeColor());
        topDragCover.setOpaque(true);
        contentLabel.add(topDragCover, JLayeredPane.DRAG_LAYER);

        //left frame drag  and cover
        leftDrag = new CyderDragLabel(5 - frameResizingLen,
                height - frameResizingLen - CyderDragLabel.DEFAULT_HEIGHT, this);
        leftDrag.setBounds(frameResizingLen, CyderDragLabel.DEFAULT_HEIGHT,
                5 - frameResizingLen, height - CyderDragLabel.DEFAULT_HEIGHT - frameResizingLen);
        leftDrag.setXOffset(frameResizingLen);
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
        rightDrag = new CyderDragLabel(5 - frameResizingLen,
                height - frameResizingLen - CyderDragLabel.DEFAULT_HEIGHT, this);
        rightDrag.setBounds(width - 5, CyderDragLabel.DEFAULT_HEIGHT,
                5 - frameResizingLen, height - CyderDragLabel.DEFAULT_HEIGHT - frameResizingLen);
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
        bottomDrag = new CyderDragLabel(width - 2 * frameResizingLen, 5 - frameResizingLen, this);
        bottomDrag.setBounds(frameResizingLen, height - 5, width - 4, 5 - frameResizingLen);
        bottomDrag.setXOffset(frameResizingLen);
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
        titleLabel.setFont(CyderFonts.frameTitleFont);
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
        iconLabel.setBounds(frameResizingLen, frameResizingLen,
                width - 2 * frameResizingLen, height - 2 * frameResizingLen);
        iconLabel.setFocusable(false);

        iconPane = new JLayeredPane();
        iconPane.setBounds(frameResizingLen, frameResizingLen,
                width - 2 * frameResizingLen, height - 2 * frameResizingLen);
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
     * Currently, this is necessary for ConsoleFrame's audio menu and taskbar menu.
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
        cyderPanel.setBounds(borderLen, CyderDragLabel.DEFAULT_HEIGHT, getWidth() - 2 * borderLen,
                getHeight() - CyderDragLabel.DEFAULT_HEIGHT - borderLen);
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
                            Thread.sleep(timeout);
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

                                try {
                                    Thread.sleep(timeout);
                                } catch (Exception e) {
                                    ExceptionHandler.handle(e);
                                }
                            }
                            break;
                        case LEFT:
                            for (int i = titleLabel.getX() ; i < (topDrag.getWidth() / 2)
                                    - (StringUtil.getMinWidth(title, titleLabel.getFont()) / 2) ; i++) {
                                titleLabel.setLocation(i, 2);

                                try {
                                    Thread.sleep(timeout);
                                } catch (Exception e) {
                                    ExceptionHandler.handle(e);
                                }
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

                        try {
                            Thread.sleep(timeout);
                        } catch (Exception e) {
                            ExceptionHandler.handle(e);
                        }
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
    public boolean getPaintSuperTitle() {
        return paintSuperTitle;
    }

    /**
     * Set the title of the label painted on the top drag label of the CyderFrame instance.
     * You can also configure the instance to paint/not paint both
     * the windowed title, and the title label title.
     *
     * @param title the String representing the chosen CyderFrame title
     */
    @Override
    public void setTitle(String title) {
        // super call, super title will always be provided title
        super.setTitle(paintSuperTitle ? title : "");

        if (paintCyderFrameTitle && !StringUtil.isNull(title) && titleLabel != null) {
            // inner title needs to have ascii parsed away
            String innerTitle = StringUtil.getTrimmedText(StringUtil.parseNonAscii(title));

            // get the width and the shortened title if we need to shorten it
            int titleWidth = StringUtil.getMinWidth(innerTitle, titleLabel.getFont());
            String shortenedTitle = innerTitle;

            // while the title is too long, remove a char and add ... and calculate new length
            while (titleWidth > width * MAX_TITLE_LENGTH_RATIO) {
                shortenedTitle = shortenedTitle.substring(0, shortenedTitle.length() - 1);
                titleWidth = StringUtil.getMinWidth(shortenedTitle + "...", titleLabel.getFont());
            }

            // if the width is not equal to the original one
            if (StringUtil.getMinWidth(title, titleLabel.getFont()) > width * MAX_TITLE_LENGTH_RATIO
                    && !shortenedTitle.equalsIgnoreCase(title)) {
                shortenedTitle += "...";
            }

            this.title = shortenedTitle;
            titleLabel.setText(this.title);

            titleWidth = StringUtil.getMinWidth(this.title, titleLabel.getFont());

            switch (titlePosition) {
                case CENTER -> titleLabel.setBounds((topDrag.getWidth() / 2) - (titleWidth / 2), 2,
                        titleWidth, 25);
                case RIGHT -> titleLabel.setBounds(width - titleWidth, 2, titleWidth, 25);
                case LEFT -> titleLabel.setBounds(5, 2, titleWidth, 25);
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
                    CyderFonts.notificationFont, (int) Math.ceil(width * 0.8));
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
                    InformBuilder informBuilder = new InformBuilder("NULL");
                    informBuilder.setContainer(currentBuilder.getContainer());
                    informBuilder.setTitle(getTitle() + " Notification");
                    informBuilder.setRelativeTo(this);

                    InformHandler.inform(informBuilder);

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
                textContainerLabel.setFont(CyderFonts.notificationFont);
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
    @SuppressWarnings("unused")
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
        InformBuilder builder = new InformBuilder(text);
        builder.setTitle(title);
        builder.setRelativeTo(this);
        InformHandler.inform(builder);
    }

    // -------------
    // animations
    // -------------

    /**
     * The number of frames to use for animations.
     */
    private static final double animationFrames = 15.0;

    /**
     * Animates away this frame by moving it down until it is offscreen at which point the frame
     * becomes iconified.
     */
    public void minimizeAnimation() {
        try {
            // if we are the ConsoleFrame, save position vars
            if (this == ConsoleFrame.INSTANCE.getConsoleCyderFrame())
                ConsoleFrame.INSTANCE.saveScreenStat();

            //set restore vars here
            setRestoreX(getX());
            setRestoreY(getY());

            if (UserUtil.getCyderUser().getDoAnimations().equals("1")) {
                setDisableContentRepainting(true);

                // figure out increment for frame num
                int distanceToTravel = ScreenUtil.getScreenHeight() - getY();

                // 25 frames to animate
                int animationInc = (int) ((double) distanceToTravel / animationFrames);

                int initialY = getY();

                int end = ScreenUtil.getScreenHeight();

                for (int i = initialY ; i <= end ; i += animationInc) {
                    Thread.sleep(1);
                    setLocation(getX(), i);
                }
            }

            setState(Frame.ICONIFIED);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    // overridden to disable/enable content area repainting for optimization.

    /**
     * {@inheritDoc}
     */
    @Override
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
    @SuppressWarnings("unused")
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
        // thread since possible confirmation
        CyderThreadRunner.submit(() -> {
            try {
                if (disposed) {
                    return;
                }

                if (closingConfirmationMessage != null) {
                    GetterBuilder builder = new GetterBuilder("Confirmation");
                    builder.setInitialString(closingConfirmationMessage);
                    builder.setRelativeTo(this);
                    builder.setDisableRelativeTo(true);
                    boolean exit = GetterUtil.getInstance().getConfirmation(builder);

                    if (!exit) {
                        return;
                    }
                }

                disposed = true;

                // confirmation passed so log
                Logger.log(Logger.Tag.UI_ACTION, "CyderFrame disposed with fastclose: "
                        + fastClose + ", " + this);

                //run all preCloseActions if any exists, this is performed after the confirmation check
                // since now we are sure that we wish to close the frame
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
                    int animationInc = (int) ((double) distanceToTravel / animationFrames);

                    disableDragging();

                    int startY = getY();
                    int height = getHeight();

                    for (int i = startY ; i >= -height ; i -= animationInc) {
                        Thread.sleep(1);
                        setLocation(x, i);
                    }
                }

                //remove from ConsoleFrame
                ConsoleFrame.INSTANCE.removeTaskbarIcon(this);

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
     * Fast disposes the frame if active and returned if the frame was indeed active before disposing.
     *
     * @return whether the frame was active before disposing
     */
    @CanIgnoreReturnValue
    public boolean disposeIfActive() {
        boolean ret = !disposed;
        dispose(true);
        return ret;
    }

    /**
     * Whether to allow the frame to be relocated via dragging.
     *
     * @param relocatable whether to allow the frame to be relocated via dragging.
     */
    @SuppressWarnings("unused")
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

                    // now dancing is done, will be reset to false in ConsoleFrame method
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
        BufferedImage master = ImageUtil.getBi(masterIcon);
        BufferedImage rotated = ImageUtil.rotateImageByDegrees(master, degrees);
        ((JLabel) getContentPane()).setIcon(new ImageIcon(rotated));
    }

    /**
     * transforms the content pane by an incremental angle of 2 degrees
     * emulating Google's barrel roll easter egg.
     */
    public void barrelRoll() {
        ImageIcon masterIcon = (ImageIcon) ((JLabel) getContentPane()).getIcon();
        BufferedImage master = ImageUtil.getBi(masterIcon);

        new Timer(10, new ActionListener() {
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

                rotated = ImageUtil.rotateImageByDegrees(master, angle);
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
        if (width < MINIMUM_WIDTH) {
            Logger.log(Logger.Tag.DEBUG, "CyderFrame \"" + getTitle()
                    + "\" was attempted to be set to invalid width: " + width);
            width = MINIMUM_WIDTH;
        }

        if (height < MINIMUM_HEIGHT) {
            Logger.log(Logger.Tag.DEBUG, "CyderFrame \"" + getTitle()
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

    // todo when on high dpi things, says resizing image but does not work, fix this,
    // background/frame should NEVER be bigger than window
    // todo moving frames between monitors break?

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
                CyderColors.getGuiThemeColor(), 5 - frameResizingLen, false));

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
        checkTitleOverflow();
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
        checkTitleOverflow();
    }

    /**
     * Revalidates the drag labels and their covers and offsets if present.
     */
    private void revalidateDragLabels() {
        if (isBorderlessFrame())
            return;

        topDrag.setWidth(width - 2 * frameResizingLen);
        topDrag.setHeight(CyderDragLabel.DEFAULT_HEIGHT - frameResizingLen);
        topDragCover.setBounds(0, 0, width, 2);

        leftDrag.setWidth(5 - frameResizingLen);
        leftDrag.setHeight(height - CyderDragLabel.DEFAULT_HEIGHT - frameResizingLen);
        leftDragCover.setBounds(0, 0, frameResizingLen, height);

        rightDrag.setWidth(5 - frameResizingLen);
        rightDrag.setHeight(height - CyderDragLabel.DEFAULT_HEIGHT - frameResizingLen);
        rightDragCover.setBounds(width - frameResizingLen, 0, frameResizingLen, height);

        bottomDrag.setWidth(width - frameResizingLen * 2);
        bottomDrag.setHeight(5 - frameResizingLen);
        bottomDragCover.setBounds(0, height - frameResizingLen, width, frameResizingLen);

        revalidateTitleAndButtonPosition();

        topDrag.setBounds(frameResizingLen, frameResizingLen, width - 2 * frameResizingLen,
                CyderDragLabel.DEFAULT_HEIGHT - frameResizingLen);
        leftDrag.setBounds(frameResizingLen, CyderDragLabel.DEFAULT_HEIGHT, 5 - frameResizingLen,
                height - CyderDragLabel.DEFAULT_HEIGHT - frameResizingLen);
        rightDrag.setBounds(width - 5, CyderDragLabel.DEFAULT_HEIGHT,
                5 - frameResizingLen, height - CyderDragLabel.DEFAULT_HEIGHT - 2);
        bottomDrag.setBounds(frameResizingLen, height - 5,
                width - 2 * frameResizingLen, 5 - frameResizingLen);

        topDrag.setXOffset(frameResizingLen);
        topDrag.setYOffset(frameResizingLen);

        leftDrag.setXOffset(frameResizingLen);
        leftDrag.setYOffset(CyderDragLabel.DEFAULT_HEIGHT);

        rightDrag.setXOffset(width - 5);
        rightDrag.setYOffset(CyderDragLabel.DEFAULT_HEIGHT);

        bottomDrag.setXOffset(frameResizingLen);
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
            if (isBorderlessFrame() || (cr == null && ConsoleFrame.INSTANCE.getUUID() != null
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
     * Checks for the title label overflowing onto the drag
     * label buttons and clips the label if it does extend.
     */
    private void checkTitleOverflow() {
        if (isBorderlessFrame() || topDrag.getButtonList() == null)
            return;

        LinkedList<JButton> buttons = topDrag.getButtonList();

        switch (buttonPosition) {
            case LEFT -> {
                // left buttons so find max x button plus width

                int maxX = 0;

                for (JButton button : buttons) {
                    maxX = Math.max(maxX, button.getX() + button.getWidth());
                }

                // ensure title label doesn't start before maxX + gap
                if (maxX + TITLE_BUTTONS_MIN_GAP >= titleLabel.getX()) {
                    titleLabel.setBounds(maxX + TITLE_BUTTONS_MIN_GAP, titleLabel.getY(),
                            this.getWidth() - maxX + TITLE_BUTTONS_MIN_GAP - 4, titleLabel.getHeight());
                }
            }
            case RIGHT -> {
                // right buttons so find min x, easier

                int minX = this.getWidth();

                for (JButton button : buttons) {
                    minX = Math.min(minX, button.getX());
                }

                // ensure title label doesn't end inside of button's area
                if (titleLabel.getX() + titleLabel.getWidth() + TITLE_BUTTONS_MIN_GAP >= minX) {
                    titleLabel.setBounds(titleLabel.getX(), titleLabel.getY(),
                            this.getWidth() - minX - TITLE_BUTTONS_MIN_GAP - titleLabel.getX(),
                            titleLabel.getHeight());
                }
            }
            default -> throw new IllegalArgumentException("Invalid button position: " + buttonPosition);
        }
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
     * Sets the maximum window size if resizing is allowed.
     *
     * @param maxSize the Dimension of the minimum allowed size
     */
    public void setMaximumSize(Dimension maxSize) {
        maximumSize = maxSize;
        cr.setMaximumSize(maximumSize);
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
            iconLabel.setBounds(frameResizingLen, frameResizingLen, width - 2 * frameResizingLen,
                    height - 2 * frameResizingLen);
        }

        if (iconPane != null) {
            iconPane.setBounds(frameResizingLen, frameResizingLen, width - 2 * frameResizingLen,
                    height - 2 * frameResizingLen);
        }

        if (cyderPanel != null) {
            cyderPanel.setBounds(borderLen, CyderDragLabel.DEFAULT_HEIGHT, getWidth() - 2 * borderLen,
                    getHeight() - CyderDragLabel.DEFAULT_HEIGHT - borderLen);
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
            iconLabel.setBounds(frameResizingLen, frameResizingLen, width - 2 * frameResizingLen,
                    height - 2 * frameResizingLen);
            iconPane.setBounds(frameResizingLen, frameResizingLen, width - 2 * frameResizingLen,
                    height - 2 * frameResizingLen);

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
        return ReflectionUtil.commonCyderUIReflection(this);
    }

    /**
     * Kills all threads associated with this CyderFrame instance. This
     * method is automatically called when {@link CyderFrame#dispose()} is invoked.
     * As such, correct features should not be expected to function properly after this method
     * or dispose() are called.
     */
    public void killThreads() {
        threadsKilled = true;
    }

    /**
     * Whether threads have been killed.
     *
     * @return whether threads have been killed
     */
    @SuppressWarnings("unused")
    public boolean threadsKilled() {
        return threadsKilled;
    }

    /**
     * Set the background of {@code this} to the current ConsoleFrame background.
     */
    @SuppressWarnings("unused")
    public void stealConsoleBackground() {
        if (ConsoleFrame.INSTANCE.getCurrentBackground() == null)
            return;

        iconLabel.setIcon(new ImageIcon(ConsoleFrame.INSTANCE.getCurrentBackground()
                .generateImageIcon().getImage().getScaledInstance(
                        getWidth(), getHeight(), Image.SCALE_DEFAULT)));
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
        if (isBorderlessFrame())
            return;

        topDrag.disableDragging();
        bottomDrag.disableDragging();
        rightDrag.disableDragging();
        leftDrag.disableDragging();
    }

    /**
     * Enables dragging for this frame.
     */
    public void enableDragging() {
        if (isBorderlessFrame())
            return;

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
     * @param ml the listener to add to the drag labels
     */
    public void addDragLabelMouseListener(MouseListener ml) {
        topDrag.addMouseListener(ml);
        bottomDrag.addMouseListener(ml);
        leftDrag.addMouseListener(ml);
        rightDrag.addMouseListener(ml);
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
     * Whether the frame is pinned on top AND pinned to the console frame.
     */
    private boolean consolePinned;

    /**
     * Returns whether the frame should be pinned to the console frame.
     *
     * @return whether the frame should be pinned to the console frame
     */
    public boolean isConsolePinned() {
        return consolePinned;
    }

    /**
     * Sets whether the frame should be pinned to the console frame.
     *
     * @param consolePinned whether the frame should be pinned to the console frame
     */
    public void setConsolePinned(boolean consolePinned) {
        this.consolePinned = consolePinned;
        setAlwaysOnTop(this.consolePinned);
    }

    /**
     * The relative x value of this frame to the console frame, used for console pin dragging actions.
     */
    private int relativeX;

    /**
     * The relative y value of this frame to the console frame, used for console pin dragging actions.
     */
    private int relativeY;

    /**
     * Returns the relative x of this frame to the console frame.
     *
     * @return the relative x of this frame to the console frame
     */
    public int getRelativeX() {
        return relativeX;
    }

    /**
     * Returns the relative y of this frame to the console frame.
     *
     * @return the relative y of this frame to the console frame
     */
    public int getRelativeY() {
        return relativeY;
    }

    /**
     * Sets the relative x of this frame to the console frame.
     *
     * @param relativeX the relative x of this frame to the console frame
     */
    public void setRelativeX(int relativeX) {
        this.relativeX = relativeX;
    }

    /**
     * Sets the relative y of this frame to the console frame.
     *
     * @param relativeY the relative y of this frame to the console frame
     */
    public void setRelativeY(int relativeY) {
        this.relativeY = relativeY;
    }

    // -----------------
    // taskbar logic
    // -----------------

    /**
     * Sets the taskbar image of the CyderFrame to the provided image.
     * If the frame's dispose() method has been invoked, this will
     * prevent the image from being set for optimization purposes.
     *
     * @param image the image to use for the taskbar
     */
    @Override
    public void setIconImage(Image image) {
        if (!threadsKilled)
            super.setIconImage(image);
    }

    /**
     * The possible border colors to use for the taskbar icon
     */
    public static final ImmutableList<Color> TASKBAR_BORDER_COLORS = ImmutableList.of(
            new Color(22, 124, 237),
            new Color(254, 49, 93),
            new Color(249, 122, 18)
    );

    /**
     * The index which determines which color to choose for the border color.
     */
    private static int colorIndex;

    /**
     * The taskbar icon border color for this CyderFrame instance.
     */
    private Color taskbarIconBorderColor;

    /**
     * Whether to use the custom taskbar icon if it exists.
     */
    private boolean useCustomTaskbarIcon;

    /**
     * The custom ImageIcon to use for the taskbar icon if enabled.
     */
    private ImageIcon customTaskbarIcon;

    /**
     * Returns whether to use the default taskbar component or the custom one.
     *
     * @return whether to use the default taskbar component or the custom one
     */
    public boolean shouldUseCustomTaskbarIcon() {
        return useCustomTaskbarIcon;
    }

    /**
     * Whether to use a custom taskbar icon or the default one.
     *
     * @param useCustomTaskbarIcon whether to use a custom taskbar icon or the default one
     */
    public void setUseCustomTaskbarIcon(boolean useCustomTaskbarIcon) {
        if (this.useCustomTaskbarIcon == useCustomTaskbarIcon)
            return;

        this.useCustomTaskbarIcon = useCustomTaskbarIcon;

        if (!useCustomTaskbarIcon)
            customTaskbarIcon = null;

        ConsoleFrame.INSTANCE.revalidateMenu();
    }

    /**
     * Constructs the custom taskbar button based on the currently set custom taskbar ImageIcon.
     *
     * @return the custom taskbar button based on the currently set custom taskbar ImageIcon
     */
    public JLabel getCustomTaskbarButton() {
        JLabel customLabel = new JLabel();
        customLabel.setSize(CyderFrame.taskbarIconLength, CyderFrame.taskbarIconLength);

        int len = CyderFrame.taskbarIconLength;
        int borderLen = CyderFrame.taskbarBorderLength;

        BufferedImage resizedImage = ImageUtil.resizeImage(len, len, customTaskbarIcon);

        //drawing a border over the image
        Graphics g = resizedImage.createGraphics();
        g.setColor(Color.black);
        g.fillRect(0, 0, len, borderLen);
        g.fillRect(0, 0, borderLen, len);
        g.fillRect(len - borderLen, 0, len, len);
        g.fillRect(0, len - borderLen, len, len);

        float darkenFactor = 0.7f;
        RescaleOp op = new RescaleOp(darkenFactor, 0, null);
        BufferedImage resizedImageHover = op.filter(resizedImage, null);

        customLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (getState() == 0) {
                    minimizeAnimation();
                } else {
                    setState(Frame.NORMAL);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                customLabel.setIcon(new ImageIcon(resizedImageHover));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                customLabel.setIcon(new ImageIcon(resizedImage));
            }
        });

        customLabel.setToolTipText(getTitle());
        customLabel.setIcon(new ImageIcon(resizedImage));

        return customLabel;
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
     * Returns the color to be associated with this CyderFrame's taskbar border color.
     *
     * @return the color to be associated with this CyderFrame's taskbar border color
     */
    private Color getTaskbarBorderColor() {
        Color ret = TASKBAR_BORDER_COLORS.get(colorIndex);
        colorIndex++;

        if (colorIndex > TASKBAR_BORDER_COLORS.size() - 1)
            colorIndex = 0;

        return ret;
    }

    /**
     * The length of the taskbar icons to be generated.
     */
    public static final int taskbarIconLength = 75;

    /**
     * The border length of the taskbar icons to be generated.
     */
    public static final int taskbarBorderLength = 5;

    /**
     * Returns a compact taskbar component for this CyderFrame instance.
     *
     * @param focused whether the compact taskbar component should be in a focused state
     * @return a compact taskbar component for this CyderFrame instance
     */
    public JLabel getCompactTaskbarButton(boolean focused) {
        return generateDefaultCompactTaskbarComponent(getTitle(), () -> {
            if (getState() == 0) {
                minimizeAnimation();
            } else {
                setState(Frame.NORMAL);
            }
        }, focused);
    }

    /**
     * Returns a taskbar component with the currently set border color.
     *
     * @return a taskbar component with the currently set border color
     */
    public JLabel getTaskbarButton() {
        checkNotNull(getTitle(), "CyderFrame title not yet set");
        checkArgument(!getTitle().isEmpty(), "CyderFrame title is empty");

        return getTaskbarButton(taskbarIconBorderColor, false);
    }

    /**
     * Returns a taskbar component with the currently set border color in a focused state.
     *
     * @return a taskbar component with the currently set border color in a focused state
     */
    public JLabel getFocusedTaskbarButton() {
        checkNotNull(getTitle());
        checkArgument(!getTitle().isEmpty());

        return getTaskbarButton(taskbarIconBorderColor, true);
    }

    /**
     * Returns a taskbar component with the specified border
     * color which minimizes the frame upon click actions.
     *
     * @param borderColor the color of the taskbar border
     * @param focused     whether to generate the button in a focused state
     * @return a taskbar component with the specified border color
     */
    public JLabel getTaskbarButton(Color borderColor, boolean focused) {
        Preconditions.checkNotNull(borderColor);

        Runnable runnable = () -> {
            if (getState() == 0) {
                minimizeAnimation();
            } else {
                setState(Frame.NORMAL);
            }
        };

        if (focused) {
            return generateTaskbarFocusedComponent(getTitle(), runnable, borderColor);
        } else {
            return generateTaskbarComponent(getTitle(), runnable, borderColor);
        }
    }

    /**
     * The maximum number of chars to display when compact mode for taskbar icons is active.
     */
    public static final int MAX_COMPACT_MENU_CHARS = 11;

    /**
     * A factory method which generates a default taskbar component for compact mode.
     *
     * @param title       the title of the compact taskbar component
     * @param clickAction the action to invoke upon clicking the compact component
     * @param focused     whether the label should be painted as focused/hovered
     * @return the compact taskbar component
     */
    public static JLabel generateDefaultCompactTaskbarComponent(String title, Runnable clickAction, boolean focused) {
        Preconditions.checkNotNull(title);
        Preconditions.checkNotNull(clickAction);

        String usageTitle = title.substring(0, Math.min(MAX_COMPACT_MENU_CHARS, title.length()));

        JLabel ret = new JLabel(usageTitle);
        ret.setForeground(focused ? CyderColors.regularRed : CyderColors.vanilla);
        ret.setFont(CyderFonts.defaultFontSmall);
        ret.setVerticalAlignment(SwingConstants.CENTER);

        ret.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                clickAction.run();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                ret.setForeground(focused ? CyderColors.vanilla : CyderColors.regularRed);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ret.setForeground(focused ? CyderColors.regularRed : CyderColors.vanilla);
            }
        });

        // if had to cut off text, make tooltip show full
        if (!ret.getText().equalsIgnoreCase(usageTitle)) {
            ret.setToolTipText(title);
        }

        return ret;
    }

    // todo util methods for icon generation
    // todo lots of duplicate code and confusing names here

    /**
     * A factory method which generates a default focused/hovered taskbar component with the provided title, click
     * action, and border
     * color.
     *
     * @param title       the title of the component
     * @param clickAction the action to invoke when the component is clicked
     * @param borderColor the color of the border around the component
     * @return the taskbar component
     */
    public static JLabel generateTaskbarFocusedComponent(String title, Runnable clickAction, Color borderColor) {
        Preconditions.checkNotNull(title);
        Preconditions.checkNotNull(clickAction);

        JLabel ret = new JLabel();

        BufferedImage bufferedImage = new BufferedImage(taskbarIconLength,
                taskbarIconLength, BufferedImage.TYPE_INT_RGB);
        Graphics g = bufferedImage.getGraphics();

        //set border color
        g.setColor(borderColor);
        g.fillRect(0, 0, taskbarIconLength, taskbarIconLength);

        //draw center color
        g.setColor(Color.black);
        g.fillRect(taskbarBorderLength, taskbarBorderLength,
                taskbarIconLength - taskbarBorderLength * 2,
                taskbarIconLength - taskbarBorderLength * 2);

        //draw darker image
        Font labelFont = new Font("Agency FB", Font.BOLD, 28);

        BufferedImage darkerBufferedImage =
                new BufferedImage(taskbarIconLength, taskbarIconLength, BufferedImage.TYPE_INT_RGB);
        Graphics g2 = darkerBufferedImage.getGraphics();

        //set border color
        g2.setColor(borderColor.darker());
        g2.fillRect(0, 0, taskbarIconLength, taskbarIconLength);

        //draw center color
        g2.setColor(Color.black);
        g2.fillRect(taskbarBorderLength, taskbarBorderLength,
                taskbarIconLength - taskbarBorderLength * 2,
                taskbarIconLength - taskbarBorderLength * 2);

        g2.setColor(CyderColors.vanilla);
        g2.setFont(labelFont);
        g2.setColor(CyderColors.vanilla);

        String iconTitle = title.substring(0, Math.min(4, title.length())).trim();
        CyderLabel titleLabel = new CyderLabel(iconTitle);
        titleLabel.setFont(labelFont);
        titleLabel.setForeground(CyderColors.vanilla);
        titleLabel.setBounds(0, 0, taskbarIconLength, taskbarIconLength);
        titleLabel.setFocusable(false);
        ret.add(titleLabel);
        titleLabel.setToolTipText(title);
        titleLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                clickAction.run();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                ret.setIcon(new ImageIcon(bufferedImage));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ret.setIcon(new ImageIcon(darkerBufferedImage));
            }
        });

        ret.setIcon(new ImageIcon(darkerBufferedImage));

        return ret;
    }

    /**
     * A factory method which generates a default taskbar component with the provided title, click action, and border
     * color.
     *
     * @param title       the title of the component
     * @param clickAction the action to invoke when the component is clicked
     * @param borderColor the color of the border around the component
     * @return the taskbar component
     */
    public static JLabel generateTaskbarComponent(String title, Runnable clickAction, Color borderColor) {
        Preconditions.checkNotNull(title);
        Preconditions.checkNotNull(clickAction);

        JLabel ret = new JLabel();

        BufferedImage bufferedImage =
                new BufferedImage(taskbarIconLength, taskbarIconLength, BufferedImage.TYPE_INT_RGB);
        Graphics g = bufferedImage.getGraphics();

        //set border color
        g.setColor(borderColor);
        g.fillRect(0, 0, taskbarIconLength, taskbarIconLength);

        //draw center color
        g.setColor(Color.black);
        g.fillRect(taskbarBorderLength, taskbarBorderLength,
                taskbarIconLength - taskbarBorderLength * 2,
                taskbarIconLength - taskbarBorderLength * 2);

        //draw darker image
        Font labelFont = new Font("Agency FB", Font.BOLD, 28);

        BufferedImage darkerBufferedImage =
                new BufferedImage(taskbarIconLength, taskbarIconLength, BufferedImage.TYPE_INT_RGB);
        Graphics g2 = darkerBufferedImage.getGraphics();

        //set border color
        g2.setColor(borderColor.darker());
        g2.fillRect(0, 0, taskbarIconLength, taskbarIconLength);

        //draw center color
        g2.setColor(Color.black);
        g2.fillRect(taskbarBorderLength, taskbarBorderLength,
                taskbarIconLength - taskbarBorderLength * 2,
                taskbarIconLength - taskbarBorderLength * 2);

        g2.setColor(CyderColors.vanilla);
        g2.setFont(labelFont);
        g2.setColor(CyderColors.vanilla);

        String iconTitle = title.substring(0, Math.min(4, title.length())).trim();
        CyderLabel titleLabel = new CyderLabel(iconTitle);
        titleLabel.setFont(labelFont);
        titleLabel.setForeground(CyderColors.vanilla);
        titleLabel.setBounds(0, 0, taskbarIconLength, taskbarIconLength);
        titleLabel.setFocusable(false);
        ret.add(titleLabel);
        titleLabel.setToolTipText(title);
        titleLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                clickAction.run();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                ret.setIcon(new ImageIcon(darkerBufferedImage));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ret.setIcon(new ImageIcon(bufferedImage));
            }
        });

        ret.setIcon(new ImageIcon(bufferedImage));

        return ret;
    }

    /**
     * Generates a default taskbar component for this frame based on its current title.
     *
     * @param title       the title of the component
     * @param clickAction the action to invoke when the icon is clicked
     * @return the taskbar component
     */
    public static JLabel generateDefaultTaskbarComponent(String title, Runnable clickAction) {
        return generateTaskbarComponent(title, clickAction, CyderColors.taskbarDefaultColor);
    }

    /**
     * Sets the frame's visibility attribute and adds the frame to the ConsoleFrame menu list.
     *
     * @param visible whether to set the frame to be visible
     */
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);

        if (visible) {
            ConsoleFrame.INSTANCE.addTaskbarIcon(this);
        }

        // if the console is set to always on top, then we need this frame
        // to be automatically set on top as well so that new frames are not behind the console
        if (visible && ConsoleFrame.INSTANCE.getConsoleCyderFrame() != null &&
                ConsoleFrame.INSTANCE.getConsoleCyderFrame().isAlwaysOnTop()) {
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

    /**
     * Sets whether debug lines should be drawn for this frame.
     *
     * @param b whether debug lines should be drawn for this frame
     */
    public void drawDebugLines(boolean b) {
        drawDebugLines = b;

        if (b) {
            Color lineColor = ColorUtil.getOppositeColor(backgroundColor);

            if (background != null) {
                lineColor = ColorUtil.getDominantColorOpposite(background);
            }

            ImageIcon neffex = new ImageIcon("static/pictures/print/neffex.png");
            debugImageLabel = new JLabel();
            debugImageLabel.setIcon(neffex);
            debugImageLabel.setBounds(
                    getWidth() / 2 - neffex.getIconWidth() / 2,
                    getHeight() / 2 - neffex.getIconHeight() / 2,
                    neffex.getIconWidth(), neffex.getIconHeight());
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
            remove(debugXLabel);
            remove(debugYLabel);
            remove(debugImageLabel);
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
    @SuppressWarnings("unused")
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

                try {
                    Thread.sleep(DRAG_OPACITY_ANIMATION_DELAY);
                } catch (InterruptedException ignored) {
                }
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

                try {
                    Thread.sleep(DRAG_OPACITY_ANIMATION_DELAY);
                } catch (InterruptedException ignored) {
                }
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTitle() {
        if (titleLabel != null && !titleLabel.getText().isEmpty()) {
            return titleLabel.getText();
        } else {
            return super.getTitle();
        }
    }

    // ----------------
    // frame menu logic
    // ----------------

    /**
     * The types of menus that a frame can use for its menu.
     */
    public enum MenuType {
        /**
         * The default which mimics the console frame.
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
    private MenuType currentMenuType = MenuType.PANEL;

    /**
     * Sets the menu type to the provided and revalidates the menu depending on the old state.
     *
     * @param currentMenuType the new menu type
     */
    public void setCurrentMenuType(MenuType currentMenuType) {
        if (currentMenuType == this.currentMenuType)
            return;

        this.currentMenuType = currentMenuType;

        if (menuEnabled) {
            boolean wasVisible = menuLabel != null && menuLabel.isVisible();
            generateMenu();

            if (wasVisible) {
                showMenu();
            }
        }
    }

    /**
     * Regenerates the menu and shows it if it was visible.
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
     * Returns the frame's current menu type.
     *
     * @return the frame's current menu type
     */
    public MenuType getCurrentMenuType() {
        return currentMenuType;
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
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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
     * @param text    the label text
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
        newLabel.setFont(CyderFonts.defaultFontSmall);
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
    @SuppressWarnings("unused")
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
     * Shows the menu label in the proper spot.
     */
    private void showMenu() {
        if (menuLabel == null) {
            generateMenu();
        }

        menuLabel.setLocation(animateMenuToPoint);
        menuLabel.setVisible(true);
    }

    /**
     * Hides the menu label.
     */
    private void hideMenu() {
        if (menuLabel != null && menuLabel.isVisible()) {
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
        if (!menuLabel.isVisible())
            generateMenu();

        CyderThreadRunner.submit(() -> {
            try {
                if (currentMenuType == MenuType.PANEL) {
                    menuLabel.setLocation(-menuLabel.getWidth(), animateMenuToPoint.getLocation().y);
                    menuLabel.setVisible(true);
                    for (int x = menuLabel.getX() ; x < animateMenuToPoint.x ; x += menuAnimationInc) {
                        menuLabel.setLocation(x, menuLabel.getY());
                        Thread.sleep(menuAnimationDelay);
                    }
                } else {
                    menuLabel.setLocation(animateMenuToPoint.x,
                            animateMenuToPoint.y - menuLabel.getHeight());
                    menuLabel.setVisible(true);
                    for (int y = menuLabel.getY() ; y <= animateMenuToPoint.y ; y += menuAnimationInc) {
                        menuLabel.setLocation(animateMenuToPoint.x, y);
                        Thread.sleep(menuAnimationDelay);
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

        if (menuLabel.getY() + menuLabel.getWidth() < 0)
            return;

        CyderThreadRunner.submit(() -> {
            try {
                if (currentMenuType == MenuType.PANEL) {
                    for (int x = menuLabel.getX() ; x > -menuLabel.getWidth() ; x -= menuAnimationInc) {
                        menuLabel.setLocation(x, menuLabel.getY());
                        Thread.sleep(menuAnimationDelay);
                    }
                } else {
                    menuLabel.setLocation(animateMenuToPoint.x, animateMenuToPoint.y);
                    for (int y = menuLabel.getY() ; y >= animateMenuToPoint.y - menuLabel.getHeight()
                            ; y -= menuAnimationInc) {
                        menuLabel.setLocation(animateMenuToPoint.x, y);
                        Thread.sleep(menuAnimationDelay);
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

        if (currentMenuType == MenuType.PANEL) {
            int menuHeight = 2 * paddingHeight + (menuItems.size() * (StringUtil.getAbsoluteMinHeight(
                    String.valueOf(CyderNumbers.JENNY), CyderFonts.defaultFontSmall))) + 5;

            int sub = 5;

            if (menuHeight > getHeight() - topDrag.getHeight() - sub) {
                menuHeight = getHeight() - topDrag.getHeight() - sub;
            }

            menuLabel.setSize(menuWidth, menuHeight);
        } else {
            menuLabel.setSize(getWidth() - 10,
                    (StringUtil.getMinHeight(String.valueOf(CyderNumbers.JENNY), CyderFonts.defaultFontSmall)));
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

        if (currentMenuType == MenuType.PANEL) {
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

        if (currentMenuType == MenuType.PANEL) {
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
     * The delay before setting the always on top mode to
     * the original value after invoking finalizeAndShow();
     */
    private static final int FINALIZE_AND_SHOW_DELAY = 500;

    /**
     * Sets the frame's location relative to the dominant frame,
     * the visibility to true, and sets always on top mode to true
     * temporarily to ensure the frame is placed on top of other possible frames.
     */
    public void finalizeAndShow() {
        setLocationRelativeTo(getDominantFrame());
        setVisible(true);

        boolean wasOnTop = isAlwaysOnTop();
        setAlwaysOnTop(true);

        if (!wasOnTop) {
            CyderThreadRunner.submit(() -> {
                try {
                    Thread.sleep(FINALIZE_AND_SHOW_DELAY);
                    setAlwaysOnTop(false);
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }
            }, "[" + getTitle() + "] finalizeAndShow()");
        }
    }

    /**
     * Returns the current dominant frame for Cyder.
     *
     * @return the current dominant frame for Cyder
     */
    @Nullable
    public static CyderFrame getDominantFrame() {
        if (!ConsoleFrame.INSTANCE.isClosed()) {
            if (ConsoleFrame.INSTANCE.getConsoleCyderFrame().getState() == ICONIFIED) {
                return null;
            } else
                return ConsoleFrame.INSTANCE.getConsoleCyderFrame();
        } else if (!LoginHandler.isLoginFrameClosed() && LoginHandler.getLoginFrame() != null) {
            return LoginHandler.getLoginFrame();
        }
        // other possibly dominant/stand-alone frame checks here
        else
            return null;
    }

    /**
     * The valid screen positions for a frame object.
     */
    public enum ScreenPosition {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT, CENTER
    }

    /**
     * Sets the console frame to a provided ScreenPosition and moves any pinned CyderFrame windows with it.
     *
     * @param screenPos the screen position to move the ConsoleFrame to
     */
    public void setLocationOnScreen(ScreenPosition screenPos) {
        switch (screenPos) {
            case CENTER -> setLocationRelativeTo(null);
            case TOP_LEFT -> setLocation(0, 0);
            case TOP_RIGHT -> setLocation(ScreenUtil.getScreenWidth()
                    - getWidth(), 0);
            case BOTTOM_LEFT -> setLocation(0, ScreenUtil.getScreenHeight()
                    - getHeight());
            case BOTTOM_RIGHT -> setLocation(ScreenUtil.getScreenWidth() - getWidth(),
                    ScreenUtil.getScreenHeight() - getHeight());
        }
    }

    /**
     * A CyderFrame menu item.
     * This class is a wrapper to associate a label with a possible
     * AtomicBoolean which dictates the state of the menu item.
     * <p>
     * Instances of this class are immutable.
     */
    private static record MenuItem(JLabel label, AtomicBoolean state) {
    }
}