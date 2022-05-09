package cyder.ui;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderIcons;
import cyder.constants.CyderNumbers;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.ConsoleFrame;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.InformHandler;
import cyder.handlers.internal.Logger;
import cyder.handlers.internal.LoginHandler;
import cyder.handlers.internal.objects.InformBuilder;
import cyder.threads.CyderThreadRunner;
import cyder.ui.objects.NotificationBuilder;
import cyder.utilities.*;
import cyder.utilities.enums.NotificationType;
import cyder.utilities.objects.BoundsString;
import cyder.utilities.objects.GetterBuilder;
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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * CyderFrame component is the primary backbone that all of Cyder lays on.
 */
public class CyderFrame extends JFrame {
    /**
     * The maximum allowable frame dimension to notification dimension before
     * the notification is turned into a popup pane.
     */
    public static final float NOTIFICATION_TO_FRAME_RATIO = 0.9f;

    /**
     * The default width for a CyderFrame.
     */
    public static final int DEFAULT_WIDTH = 800;

    /**
     * The default height for a CyderFrame.
     */
    public static final int DEFAULT_HEIGHT = 800;

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
     * Allowable indicies to add components to the contentLabel
     * which is a JLayeredPane and the content pane.
     */
    public static final ArrayList<Integer> allowableContentLabelIndicies = new ArrayList<>(){{
        // Drag labels
        add(JLayeredPane.DRAG_LAYER);
        // notifications
        add(JLayeredPane.POPUP_LAYER);
    }};

    /**
     * Restrict default constructor.
     */
    private CyderFrame() {
        throw new IllegalMethodException("Illegal CyderFrame constructor");
    }

    /**
     * Constructs a new CyderFrame object with the specified width and height.
     * Note that if the width or height falls below the minimum size, the returned
     * frame object will have the minimum width and/or height, not the width and
     * height provided.
     *
     * @param width the specified width of the CyderFrame
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
     * @param width the width of the CyderFrame
     * @param height the height of the CyderFrame
     * @param c the color of the content pane background
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
     * @param width the specified width of the cyder frame
     * @param height the specified height of the cyder frame
     * @param background the specified background image (you may
     *                   enable rescaling of this background on fram resize events should you choose)
     */
    public CyderFrame(int width, int height, ImageIcon background) {
        // ensure non null backgrond
        checkNotNull(background);

        // correct possibly too small width and heights
        if (width < MINIMUM_WIDTH) {
            Logger.log(Logger.Tag.DEBUG, "CyderFrame was"
                    + " attempted to be set to invalid width: " + width);
            width = MINIMUM_WIDTH;
        }

        if (height < MINIMUM_HEIGHT) {
            Logger.log(Logger.Tag.DEBUG, "CyderFrame was"
                    + " attempted to be set to invalid height: " + height);
            height = MINIMUM_HEIGHT;
        }

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
        setBackground(CyderColors.vanila);
        setIconImage(CyderIcons.CYDER_ICON.getImage());

        //try and get preference for frame shape
        if (ConsoleFrame.INSTANCE.getUUID() != null) {
            if (UserUtil.getCyderUser().getRoundedwindows().equals("1")) {
                setShape(new RoundRectangle2D.Double(0, 0,
                        getWidth(), getHeight(), 20, 20));
            } else {
                setShape(null);
            }
        }

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
                if (allowableContentLabelIndicies.contains(index)) {
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
        iconLabel.setBounds(frameResizingLen,frameResizingLen,
                width - 2 * frameResizingLen,height - 2 * frameResizingLen);
        iconLabel.setIcon(background);
        iconLabel.setFocusable(false);

        iconPane = new JLayeredPane();
        iconPane.setBounds(frameResizingLen,frameResizingLen,
                width - 2 * frameResizingLen, height - 2 * frameResizingLen);
        iconPane.add(iconLabel,JLayeredPane.DEFAULT_LAYER);
        iconPane.setFocusable(false);
        contentLabel.add(iconPane,JLayeredPane.DEFAULT_LAYER);

        contentLabel.setBorder(new LineBorder(CyderColors.getGuiThemeColor(), 3, false));
        setContentPane(contentLabel);

        //top frame drag and cover
        topDrag = new CyderDragLabel(width - 2 * frameResizingLen,
                CyderDragLabel.DEFAULT_HEIGHT - 2, this);
        topDrag.setBounds(frameResizingLen, frameResizingLen,
                width - 2 * frameResizingLen, CyderDragLabel.DEFAULT_HEIGHT - 2);
        topDrag.setxOffset(frameResizingLen);
        topDrag.setyOffset(frameResizingLen);
        contentLabel.add(topDrag, JLayeredPane.DRAG_LAYER);
        topDrag.setFocusable(false);

        topDragCover = new JLabel();
        topDragCover.setBounds(0, 0 , width, 2);
        topDragCover.setBackground(CyderColors.getGuiThemeColor());
        topDragCover.setOpaque(true);
        contentLabel.add(topDragCover, JLayeredPane.DRAG_LAYER);

        //left frame drag  and cover
        leftDrag = new CyderDragLabel(5 - frameResizingLen,
                height - frameResizingLen - CyderDragLabel.DEFAULT_HEIGHT, this);
        leftDrag.setBounds(frameResizingLen, CyderDragLabel.DEFAULT_HEIGHT,
                5 - frameResizingLen, height - CyderDragLabel.DEFAULT_HEIGHT - frameResizingLen);
        leftDrag.setxOffset(frameResizingLen);
        leftDrag.setyOffset(CyderDragLabel.DEFAULT_HEIGHT);
        contentLabel.add(leftDrag, JLayeredPane.DRAG_LAYER);
        leftDrag.setFocusable(false);
        leftDrag.setButtonList(null);

        leftDragCover = new JLabel();
        leftDragCover.setBounds(0, 0 , 2, height);
        leftDragCover.setBackground(CyderColors.getGuiThemeColor());
        leftDragCover.setOpaque(true);
        contentLabel.add(leftDragCover, JLayeredPane.DRAG_LAYER);

        //right frame drag and cover
        rightDrag = new CyderDragLabel(5 - frameResizingLen,
                height - frameResizingLen - CyderDragLabel.DEFAULT_HEIGHT, this);
        rightDrag.setBounds(width - 5, CyderDragLabel.DEFAULT_HEIGHT,
                5 - frameResizingLen, height - CyderDragLabel.DEFAULT_HEIGHT - frameResizingLen);
        rightDrag.setxOffset(width - 5);
        rightDrag.setyOffset(CyderDragLabel.DEFAULT_HEIGHT);
        contentLabel.add(rightDrag, JLayeredPane.DRAG_LAYER);
        rightDrag.setFocusable(false);
        rightDrag.setButtonList(null);

        rightDragCover = new JLabel();
        rightDragCover.setBounds(width - 2, 0 , 2, height);
        rightDragCover.setBackground(CyderColors.getGuiThemeColor());
        rightDragCover.setOpaque(true);
        contentLabel.add(rightDragCover, JLayeredPane.DRAG_LAYER);

        //bottom frame drag  and cover
        bottomDrag = new CyderDragLabel(width - 2 * frameResizingLen, 5 - frameResizingLen, this);
        bottomDrag.setBounds(frameResizingLen, height - 5, width - 4, 5 - frameResizingLen);
        bottomDrag.setxOffset(frameResizingLen);
        bottomDrag.setyOffset(height - 5);
        contentLabel.add(bottomDrag, JLayeredPane.DRAG_LAYER);
        bottomDrag.setFocusable(false);
        bottomDrag.setButtonList(null);

        bottomDragCover = new JLabel();
        bottomDragCover.setBounds(0, height - 2 , width, 2);
        bottomDragCover.setBackground(CyderColors.getGuiThemeColor());
        bottomDragCover.setOpaque(true);
        contentLabel.add(bottomDragCover, JLayeredPane.DRAG_LAYER);

        //title label on drag label
        titleLabel = new JLabel("");
        titleLabel.setFont(CyderFonts.frameTitleFont);
        titleLabel.setForeground(CyderColors.vanila);
        titleLabel.setOpaque(false);
        titleLabel.setFocusable(false);
        topDrag.add(titleLabel);

        threadsKilled = false;
        setFrameType(frameType);

        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    // ------------------
    // Borderless frames
    // ------------------

    /**
     * Generates and returns a borderless CyderFrame.
     * A drag listener is already attached to this but
     * the caller needs to handle how the frame will be disposed.
     *
     * @param width the width of the frame.
     * @param height the height of the frame.
     * @return the borderless frame
     */
    public static CyderFrame generateBorderlessFrame(int width, int height, Color backgroundColor) {
        return new CyderFrame(width, height, backgroundColor, "borderless");
    }

    /**
     * Constructs a CyderFrame object that exists without
     * surrounding drag labels, the title label, and the button list.
     *
     * @param width the width of this borderless frame
     * @param height the height of this borderless frame
     * @param background the background color of the borderless frame
     */
    private CyderFrame(int width, int height, Color background, String borderless) {
        // borderless param still here since I haven't thought of a better way
        // to achieve this functionality in a clean, elegant way

        this.width = width;
        this.height = height;

        setSize(new Dimension(width, height));
        setResizable(false);
        setUndecorated(true);
        setBackground(Color.BLACK);
        setIconImage(CyderIcons.CYDER_ICON.getImage());

        //listener to ensure the close button was always pressed which ensures
        // things like closeAnimation are always performed
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        setShape(new RoundRectangle2D.Double(0, 0,
                getWidth(), getHeight(), 30, 30));

        //master contentLabel
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
        iconLabel.setBounds(frameResizingLen,frameResizingLen,
                width - 2 * frameResizingLen,height - 2 * frameResizingLen);
        iconLabel.setFocusable(false);

        iconPane = new JLayeredPane();
        iconPane.setBounds(frameResizingLen,frameResizingLen,
                width - 2 * frameResizingLen, height - 2 * frameResizingLen);
        iconPane.add(iconLabel,JLayeredPane.DEFAULT_LAYER);
        iconPane.setFocusable(false);
        contentLabel.add(iconPane,JLayeredPane.DEFAULT_LAYER);
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
     * Currently this is necessary for ConsoleFrame's audio menu and taskbar menu.
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
                iconLabel.remove(cyderPanel);
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
     * @throws IllegalStateException if no layout is associated with the rame
     */
    public ArrayList<Component> getLayoutComponents() {
        checkNotNull(cyderPanel);
        checkNotNull(cyderPanel).getLayout();
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
                    for (int i = titleLabel.getX() ; i > 4; i--) {
                        titleLabel.setLocation(i, 2);

                        try {
                            Thread.sleep(timeout);
                        } catch (Exception e) {
                            ExceptionHandler.handle(e);
                        }
                    }
                    titleLabel.setLocation(4, 2);
                    this.titlePosition = TitlePosition.LEFT;
                },"title position animator");
            } else if (titlePosition == TitlePosition.CENTER){
                CyderThreadRunner.submit(() -> {
                    switch (oldPosition) {
                        case RIGHT:
                            for (int i = titleLabel.getX(); i > (getTopDragLabel().getWidth() / 2)
                                    - (StringUtil.getMinWidth(title, titleLabel.getFont()) / 2); i--) {
                                titleLabel.setLocation(i, 2);

                                try {
                                    Thread.sleep(timeout);
                                } catch (Exception e) {
                                    ExceptionHandler.handle(e);
                                }
                            }
                            break;
                        case LEFT:
                            for (int i = titleLabel.getX(); i < (getTopDragLabel().getWidth() / 2)
                                    - (StringUtil.getMinWidth(title, titleLabel.getFont()) / 2); i++) {
                                titleLabel.setLocation(i, 2);

                                try {
                                    Thread.sleep(timeout);
                                } catch (Exception e) {
                                    ExceptionHandler.handle(e);
                                }
                            }
                            break;
                    }
                    titleLabel.setLocation((getTopDragLabel().getWidth() / 2)
                            - (StringUtil.getMinWidth(title, titleLabel.getFont()) / 2), 2);
                    this.titlePosition = TitlePosition.CENTER;
                    //set final bounds
                },"title position animator");
            } else {
                //right
                CyderThreadRunner.submit(() -> {
                    for (int i = titleLabel.getX(); i < width
                            - StringUtil.getMinWidth(title, titleLabel.getFont()) - 8; i++) {
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
                },"title position animator");
            }

            if (buttonPosition == ButtonPosition.RIGHT && titlePosition == TitlePosition.RIGHT) {
                buttonPosition = ButtonPosition.LEFT;
                getTopDragLabel().setButtonPosition(CyderDragLabel.ButtonPosition.LEFT);
            } else if (buttonPosition == ButtonPosition.LEFT && titlePosition == TitlePosition.LEFT) {
                buttonPosition = ButtonPosition.RIGHT;
                getTopDragLabel().setButtonPosition(CyderDragLabel.ButtonPosition.RIGHT);
            }
        } else {
            this.titlePosition = titlePosition;

            switch (titlePosition) {
                case LEFT:
                    titleLabel.setLocation(4, 2);
                    setButtonPosition(ButtonPosition.RIGHT);
                    break;
                case RIGHT:
                    titleLabel.setLocation(width
                            - StringUtil.getMinWidth(title, titleLabel.getFont()), 2);
                    setButtonPosition(ButtonPosition.LEFT);
                    break;
                case CENTER:
                    titleLabel.setLocation((getTopDragLabel().getWidth() / 2)
                            - (StringUtil.getMinWidth(title, titleLabel.getFont()) / 2), 2);
            }
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
            titlePosition = TitlePosition.LEFT;
            titleLabel.setLocation(4, 2);
        } else if (buttonPosition == ButtonPosition.LEFT && titlePosition == TitlePosition.LEFT) {
            titlePosition = TitlePosition.RIGHT;
            titleLabel.setLocation(width
                    - StringUtil.getMinWidth(title, titleLabel.getFont()), 2);
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
            case DEFAULT:
                setAlwaysOnTop(false);
                break;
            case POPUP:
                setAlwaysOnTop(true);
                //remove minimize
                topDrag.removeButton(0);
                //remove pin
                topDrag.removeButton(0);
                break;
            case INPUT_GETTER:
                setAlwaysOnTop(true);
                //remove pin
                topDrag.removeButton(1);
                break;
            default:
                throw new IllegalStateException("Unimplemented state");
        }
    }

    // -------------
    // frame title
    // -------------

    /**
     * Whether to paint the title label on the top drag label.
     */
    private boolean paintWindowTitle = true;

    /**
     * Whether to paint the CyderFrame's title label
     *
     * @param enable whether ot paint CyderFrame's title label
     */
    public void setPaintWindowTitle(boolean enable) {
        paintWindowTitle = enable;
    }

    /**
     * Returns whether the title label will be painted.
     *
     * @return whether the title label will be painted
     */
    public boolean getPaintWindowTitle() {
        return paintWindowTitle;
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
     * Set the title of the label painted on the top drag label of the CyderFrame instance.
     * You can also configure the instance to paint/not paint both
     * the windowed title, and the title label title.
     *
     * @param title the String representing the chosen CyderFrame title
     */
    @Override
    public void setTitle(String title) {
        // super call, super title will alway be provided title
        super.setTitle(paintSuperTitle ? title : "");

        if (paintWindowTitle && !StringUtil.isNull(title) && titleLabel != null) {
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
                case CENTER:
                    titleLabel.setBounds((getTopDragLabel().getWidth() / 2) - (titleWidth / 2), 2, titleWidth, 25);
                    break;
                case RIGHT:
                    titleLabel.setBounds(width - titleWidth, 2, titleWidth, 25);
                    break;
                case LEFT:
                    titleLabel.setBounds(5, 2, titleWidth, 25);
                    break;
            }
        }
    }

    // ------------------
    // Notifications
    // ------------------

    /**
     * The notification that is currently being displayed.
     */
    private CyderNotification currentNotif;

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
        return currentNotif;
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
        toastBuilder.setNotificationType(NotificationType.TOAST);

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
    private final Semaphore constructionLock = new Semaphore(1);

    /**
     * The notification queue for internal frame notifications/toasts.
     */
    private final Runnable notificationQueueRunnable = () -> {
        // as long as threads aren't killed and we have notifications
        // to pull, loop
        while (!threadsKilled && !notificationList.isEmpty()) {
            // lock so that only one notification is visible at a time
            try {
                constructionLock.acquire();
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }

            // pull next notification to build
            NotificationBuilder currentBuilder = notificationList.remove(0);

            // init current notification object, needed
            // for builder access and to kill via revokes
            currentNotif = new CyderNotification(currentBuilder);

            currentNotif.setVisible(false);
            // ensure invisible to start

            // generate label for notification
            BoundsString bs = BoundsUtil.widthHeightCalculation(
                    currentBuilder.getHtmlText(),
                    CyderFonts.notificationFont, (int) Math.ceil(width * 0.8));
            int notificationWidth = bs.getWidth();
            int notificationHeight = bs.getHeight();
            String brokenText = bs.getText();

            // if too wide, cannot notify so inform
            if (notificationHeight > height * NOTIFICATION_TO_FRAME_RATIO
                    || notificationWidth > width * NOTIFICATION_TO_FRAME_RATIO) {
                // inform original text
                inform(currentBuilder.getHtmlText(), "Notification ("
                        + currentBuilder.getNotifyTime() + ")");

                // release and continue with queue
                constructionLock.release();
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
                    constructionLock.release();
                    continue;
                }

                // we can show a custom container on the notification so add the dispose label
                long notifiedAt = currentNotif.getBuilder().getNotifyTime();
                JLabel interactionLabel = new JLabel();
                interactionLabel.setSize(containerWidth, containerHeight);
                interactionLabel.setToolTipText("Notified at: " + notifiedAt);
                interactionLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        // fire the on kill actions
                        if (currentBuilder.getOnKillAction() != null) {
                            currentNotif.kill();
                            currentBuilder.getOnKillAction().run();
                        }
                        // smoothly animate notification away
                        else {
                            currentNotif.vanish(currentBuilder.getNotificationDirection(),
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

                long notifiedAt = currentNotif.getBuilder().getNotifyTime();
                JLabel interactionLabel = new JLabel();
                interactionLabel.setSize(notificationWidth, notificationHeight);
                interactionLabel.setToolTipText("Notified at: " + notifiedAt);
                interactionLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        // fire the on kill action
                        if (currentBuilder.getOnKillAction() != null) {
                            currentNotif.kill();
                            currentBuilder.getOnKillAction().run();
                        }
                        // smoothly animate notification away
                        else {
                            currentNotif.vanish(currentBuilder.getNotificationDirection(),
                                    getContentPane(), 0);
                        }
                    }

                    @Override
                    public void mouseEntered(MouseEvent e) {
                        textContainerLabel.setForeground(
                                CyderColors.notificationForegroundColor.darker());
                        currentNotif.setHovered(true);
                        currentNotif.repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        textContainerLabel.setForeground(CyderColors.notificationForegroundColor);
                        currentNotif.setHovered(false);
                        currentNotif.repaint();
                    }
                });

                textContainerLabel.add(interactionLabel);

                // now when building the notification component, we'll use
                // this as our container that we must build around
                currentBuilder.setContainer(textContainerLabel);
            }

            // add notification component to proper layer
            iconPane.add(currentNotif, JLayeredPane.POPUP_LAYER);
            getContentPane().repaint();

            int duration = currentBuilder.getViewDuration();

            // if duration of 0 was passed, we should calculate it based on words
            if (duration == 0) {
                duration = 300 * StringUtil.countWords(
                        Jsoup.clean(bs.getText(), Safelist.none()));
            }

            // failsafe to ensure notifications are at least four seconds
            duration = Math.max(duration, 4000);

            Logger.log(Logger.Tag.UI_ACTION, "[" +
                    getTitle() + "] [NOTIFICATION] \"" + brokenText + "\"");

            // notification itself handles itself appearing, pausing, and vanishing
            currentNotif.appear(currentBuilder.getNotificationDirection(),
                    getContentPane(), duration);

            // when the notification is killed/vanishes, it sets itself
            // to killed; this loop will exit after
            while (!currentNotif.isKilled()) {
                Thread.onSpinWait();
            }

            constructionLock.release();
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
            currentNotif.vanish(currentNotif.getBuilder()
                    .getNotificationDirection(), this, 0);
        } else {
            currentNotif.kill();
        }
    }

    /**
     * Revokes the notification currently active or in
     * the queue that matches the provided text.
     *
     * @param expectedText the text of the notification to revoke.
     */
    public void revokeNotification(String expectedText) {
        // if it's the current one, revoke it
        if (currentNotif.getBuilder().getHtmlText().equals(expectedText)) {
            revokeCurrentNotification();
        } else {
            // if in the queue
            Iterator<NotificationBuilder> iter = notificationList.iterator();

            while (iter.hasNext()) {
                if (iter.next().getHtmlText().equals(expectedText)) {
                   iter.remove();
                }
            }
        }
    }

    /**
     * Removes all currently displayed notifications and wipes the notification queue.
     */
    public void revokeAllNotifications() {
        if (currentNotif != null)
            currentNotif.kill();

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
                int animationInc = (int) ((double ) distanceToTravel / animationFrames);

                for (int i = getY(); i <= ScreenUtil.getScreenHeight(); i += animationInc) {
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

                if (currentNotif != null) {
                    currentNotif.kill();
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

                    //figure out increment for frames
                    int distanceToTravel = Math.abs(getY()) + Math.abs(getHeight());
                    //25 frames to animate
                    int animationInc = (int) ((double) distanceToTravel / animationFrames);

                    for (int i = getY(); i >= -getHeight() ; i -= animationInc) {
                        Thread.sleep(1);
                        setLocation(getX(), i);
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
     * How much the frame location is incremented each dance step.
     */
    private final int dancingIncrement = 10;

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
        switch (dancingDirection) {
            case INITIAL_UP:
                setLocation(getX(), getY() - dancingIncrement);

                if (getY() < 0) {
                    setLocation(getX(), 0);
                    dancingDirection = DancingDirection.LEFT;
                }
                break;
            case LEFT:
                setLocation(getX() - 10, getY());

                if (getX() < 0) {
                    setLocation(0, 0);
                    dancingDirection = DancingDirection.DOWN;
                }
                break;
            case DOWN:
                setLocation(getX(), getY() + 10);

                if (getY() > ScreenUtil.getScreenHeight() - getHeight()) {
                    setLocation(getX(), ScreenUtil.getScreenHeight() - getHeight());
                    dancingDirection = DancingDirection.RIGHT;
                }
                break;
            case RIGHT:
                setLocation(getX() + 10, getY());

                if (getX() > ScreenUtil.getScreenWidth() - getWidth()) {
                    setLocation(ScreenUtil.getScreenWidth() - getWidth(), getY());
                    dancingDirection = DancingDirection.UP;
                }
                break;
            case UP:
                setLocation(getX(), getY() - 10);

                if (getY() < 0) {
                    setLocation(getX(), 0);

                    // now dancing is done, will be reset to false in ConsoleFrame method
                    dancingFinished = true;
                    dancingDirection = DancingDirection.LEFT;
                }
                break;
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
     * Repaints the title position and button positions in the currently set enum locations.
     */
    public void refreshTitleAndButtonPosition() {
        switch (titlePosition) {
            case LEFT:
                titleLabel.setLocation(4, 2);
                break;
            case RIGHT:
                titleLabel.setLocation(width -
                        StringUtil.getMinWidth(title, titleLabel.getFont()), 2);
                break;
            case CENTER:
                titleLabel.setLocation((getTopDragLabel().getWidth() / 2)
                        - (StringUtil.getMinWidth(title, titleLabel.getFont()) / 2), 2);
                break;
        }

        switch (buttonPosition) {
            case LEFT:
                getTopDragLabel().setButtonPosition(CyderDragLabel.ButtonPosition.LEFT);
                break;
            case RIGHT:
                getTopDragLabel().setButtonPosition(CyderDragLabel.ButtonPosition.RIGHT);
                break;
        }
    }

    /**
     * Sets the size of this frame ensuring that the sizing is not below
     * {@link CyderFrame#MINIMUM_WIDTH} by {@link CyderFrame#MINIMUM_HEIGHT}
     *
     * @param width width of frame
     * @param height height of frame
     */
    @Override
    public void setSize(int width, int height) {
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

        super.setSize(width, height);

        if (isVisible() && UserUtil.getCyderUser().getRoundedwindows().equals("1")) {
            setShape(new RoundRectangle2D.Double(0, 0,
                    getWidth(), getHeight(), 20, 20));
        } else {
            setShape(null);
        }
    }

    /**
     * Sets the bounds of the CyderFrame and refreshes all components on the frame.
     */
    @Override
    public void setBounds(int x, int y, int width, int height) {
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

        super.setBounds(x, y, width, height);

        this.width = width;
        this.height = height;

        // drag labels if present
        if (getTopDragLabel() != null) {
            topDrag.setWidth(this.width - 2 * frameResizingLen);
            topDrag.setHeight(CyderDragLabel.DEFAULT_HEIGHT - frameResizingLen);

            topDragCover.setBounds(0, 0 , width, 2);
            topDragCover.setBackground(CyderColors.getGuiThemeColor());

            leftDrag.setWidth(5 - frameResizingLen);
            leftDrag.setHeight(this.height - CyderDragLabel.DEFAULT_HEIGHT - frameResizingLen);

            leftDragCover.setBounds(0, 0 , 2, height);
            leftDragCover.setBackground(CyderColors.getGuiThemeColor());

            rightDrag.setWidth(5 - frameResizingLen);
            rightDrag.setHeight(this.height - CyderDragLabel.DEFAULT_HEIGHT - frameResizingLen);

            rightDragCover.setBounds(width - 2, 0 , 2, height);
            rightDragCover.setBackground(CyderColors.getGuiThemeColor());

            bottomDrag.setWidth(this.width - frameResizingLen * 2);
            bottomDrag.setHeight(5 - frameResizingLen);

            bottomDragCover.setBounds(0, height - 2 , width, 2);
            bottomDragCover.setBackground(CyderColors.getGuiThemeColor());

            refreshTitleAndButtonPosition();

            topDrag.setBounds(frameResizingLen, frameResizingLen, this.width - 2 * frameResizingLen,
                    CyderDragLabel.DEFAULT_HEIGHT - frameResizingLen);
            leftDrag.setBounds(frameResizingLen, CyderDragLabel.DEFAULT_HEIGHT, 5 - frameResizingLen,
                    this.height - CyderDragLabel.DEFAULT_HEIGHT - frameResizingLen);
            rightDrag.setBounds(this.width - 5, CyderDragLabel.DEFAULT_HEIGHT,
                    5 - frameResizingLen, this.height - CyderDragLabel.DEFAULT_HEIGHT - 2);
            bottomDrag.setBounds(frameResizingLen, this.height - 5, this.width - 4, 5 - frameResizingLen);

            topDrag.setxOffset(frameResizingLen);
            topDrag.setyOffset(frameResizingLen);

            leftDrag.setxOffset(frameResizingLen);
            leftDrag.setyOffset(CyderDragLabel.DEFAULT_HEIGHT);

            rightDrag.setxOffset(this.width - 5);
            rightDrag.setyOffset(CyderDragLabel.DEFAULT_HEIGHT);

            bottomDrag.setxOffset(frameResizingLen);
            bottomDrag.setyOffset(this.height - 5);

            refreshLayout();

            if (menuLabel != null && menuLabel.isVisible()) {
                generateMenu();
                menuLabel.setLocation(animateMenuToPoint);
                menuLabel.setVisible(true);
            }
        }

        if (getCurrentNotification() != null)
            switch (getCurrentNotification().getBuilder().getArrowDir()) {
                // center on frame
                case TOP:
                case BOTTOM:
                    currentNotif.setLocation(getWidth() / 2 - currentNotif.getWidth() / 2,
                        currentNotif.getY());
                    break;
                // maintain right of frame
                case RIGHT:
                    currentNotif.setLocation(getWidth() - currentNotif.getWidth() + 5,
                        currentNotif.getY());
                    break;
                // maintain left of frame
                case LEFT:
                    currentNotif.setLocation(5, currentNotif.getY());
                    break;
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
            refreshLayout();

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
     * Refreshes the icon label, icon pane, and associated CyderPanel if present.
     */
    public void refreshLayout() {
        iconLabel.setBounds(frameResizingLen,frameResizingLen,width - 2 * frameResizingLen,
                height - 2 * frameResizingLen);
        iconPane.setBounds(frameResizingLen,frameResizingLen, width - 2 * frameResizingLen,
                height - 2 * frameResizingLen);

        if (cyderPanel != null) {
            cyderPanel.setBounds(borderLen, CyderDragLabel.DEFAULT_HEIGHT, getWidth() - 2 * borderLen,
                    getHeight() - CyderDragLabel.DEFAULT_HEIGHT - borderLen);
        }
    }

    /**
     * Set the background to a new icon and refresh the frame.
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
            iconLabel.setBounds(frameResizingLen,frameResizingLen,width - 2 * frameResizingLen,height - 2 * frameResizingLen);
            iconPane.setBounds(frameResizingLen,frameResizingLen, width - 2 * frameResizingLen, height - 2 * frameResizingLen);

            if (cr != null) {
                cr.setMinimumSize(new Dimension(600,600));
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
    public boolean threadsKilled() {
        return threadsKilled;
    }

    /**
     * Set the background of {@code this} to the current ConsoleFrame background.
     */
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
        if (getTopDragLabel() == null)
            return false;

        return getTopDragLabel().isDraggingEnabled() &&
                getBottomDragLabel().isDraggingEnabled() &&
                getLeftDragLabel().isDraggingEnabled() &&
                getRightDragLabel().isDraggingEnabled();
    }

    /**
     * Disables dragging for this frame.
     */
    public void disableDragging() {
        if (topDrag == null)
            return;

        getTopDragLabel().disableDragging();
        getBottomDragLabel().disableDragging();
        getRightDragLabel().disableDragging();
        getLeftDragLabel().disableDragging();
    }

    /**
     * Enables dragging for this frame.
     */
    public void enableDragging() {
        if (topDrag == null)
            return;

        getTopDragLabel().enableDragging();
        getBottomDragLabel().enableDragging();
        getRightDragLabel().enableDragging();
        getLeftDragLabel().enableDragging();
    }

    /**
     * Repaints the frame, associated shape, and objects using
     * the {@link CyderColors#getGuiThemeColor()} attribute.
     */
    @Override
    public void repaint() {
        if (topDrag == null) {
            //update content panes
            if (getContentPane() != null)
                getContentPane().repaint();
            if (getTrueContentPane() != null)
                getTrueContentPane().repaint();

            //finally super call
            super.repaint();
            return;
        }

        try {
            //fix shape
            if (cr == null) {
                if (ConsoleFrame.INSTANCE.getUUID() != null) {
                    if (UserUtil.getCyderUser().getRoundedwindows().equals("1")) {
                        setShape(new RoundRectangle2D.Double(0, 0,
                                getWidth(), getHeight(), 20, 20));
                    } else {
                        setShape(null);
                    }
                }
            } else {
                setShape(null);
            }
        } catch (Exception e) {
            ExceptionHandler.silentHandle(e);
        }

        //update the border covering the resize area
        contentLabel.setBorder(new LineBorder(
                CyderColors.getGuiThemeColor(), 5 - frameResizingLen, false));

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

        //update content panes
        getContentPane().repaint();
        getTrueContentPane().repaint();

        if (menuLabel != null) {
            menuLabel.setBackground(CyderColors.getGuiThemeColor());
            menuLabel.repaint();
        }

        //finally super call
        super.repaint();
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
    public void addDragMouseListener(MouseListener ml) {
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
    public static final ArrayList<Color> TASKBAR_BORDER_COLORS = new ArrayList<>() {
        @Override
        public ArrayList<Color> clone() throws AssertionError {
            throw new AssertionError();
        }

        {
        add(new Color(22,124,237));
        add(new Color(254,49,93));
        add(new Color(249,122,18));
    }};

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
    public boolean isUseCustomTaskbarIcon() {
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
     * Constructs the custom taskbar icon based on the currently set custom taskbar ImageIcon.
     *
     * @return the custom taskbar icon based on the currently set custom taskbar ImageIcon
     */
    public JLabel getCustomTaskbarIcon() {
        JLabel customLabel = new JLabel();
        customLabel.setSize(CyderFrame.taskbarIconLength, CyderFrame.taskbarIconLength);

        int len = CyderFrame.taskbarIconLength;
        int borderLen = CyderFrame.taskbarBorderLength;

        BufferedImage resizedImage = ImageUtil.resizeImage(len, len, customTaskbarIcon);

        //drawing a border over the image
        Graphics g = resizedImage.createGraphics();
        g.setColor(Color.black);
        g.fillRect(0,0, len, borderLen);
        g.fillRect(0,0, borderLen, len);
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
     * Increments the color index to use for border colors for CyderFrame objects.
     */
    public static void incrementColorIndex() {
        colorIndex++;

        if (colorIndex == 3)
            colorIndex = 0;
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
     * @return a compact taskbar component for this CyderFrame instance
     */
    public JLabel getCompactTaskbarButton() {
       return generateDefaultCompactTaskbarComponent(getTitle(), () -> {
           if (getState() == 0) {
               minimizeAnimation();
           } else {
               setState(Frame.NORMAL);
           }
       });
    }

    /**
     * Returns a taskbar component with the currently set border color.
     *
     * @return a taskbar component with the currently set border color
     */
    public JLabel getTaskbarButton() {
        checkNotNull(getTitle(), "CyderFrame title not yet set");
        checkArgument(!getTitle().isEmpty(), "CyderFrame title is empty");

        return getTaskbarButton(taskbarIconBorderColor);
    }

    /**
     * Returns taskbar component with the specified border
     * color which minimizes the frame upon click actions.
     *
     * @param borderColor the color of the taskbar border
     * @return a taskbar component with the specified border color
     */
    public JLabel getTaskbarButton(Color borderColor) {
        return generateDefaultTaskbarComponent(getTitle(), () -> {
            if (getState() == 0) {
                minimizeAnimation();
            } else {
                setState(Frame.NORMAL);
            }
        }, borderColor);
    }

    public static final int MAX_COMPACT_MENU_CHARS = 11;

    /**
     * Generates a default taskbar component for compact mode.
     *
     * @param title the title of the compact taskbar component
     * @param clickAction the action to invoke upon clicking the compact component
     * @return the compact taskbar component
     */
    public static JLabel generateDefaultCompactTaskbarComponent(String title, Runnable clickAction) {
        JLabel ret = new JLabel(title.substring(0, Math.min(MAX_COMPACT_MENU_CHARS, title.length())));
        ret.setForeground(CyderColors.vanila);
        ret.setFont(CyderFonts.defaultFontSmall);
        ret.setVerticalAlignment(SwingConstants.CENTER);
        ret.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                clickAction.run();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                ret.setForeground(CyderColors.regularRed);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ret.setForeground(CyderColors.vanila);
            }
        });

        //if the label was too long even for compact text mode, set the tooltip to show the full name
        if (!ret.getText().equalsIgnoreCase(title))
            ret.setToolTipText(title);

        return ret;
    }

    /**
     * Generates a default taskbar component with the provided title, click action, and border color.
     *
     * @param title the title of the component
     * @param clickAction the action to invoke when the component is clicked
     * @param borderColor the color of the border around the component
     * @return the taskbar component
     */
    public static JLabel generateDefaultTaskbarComponent(String title, Runnable clickAction, Color borderColor) {
        JLabel ret = new JLabel();

        BufferedImage bufferedImage = new BufferedImage(taskbarIconLength, taskbarIconLength, BufferedImage.TYPE_INT_RGB);
        Graphics g = bufferedImage.getGraphics();

        //set border color
        g.setColor(borderColor);
        g.fillRect(0,0,taskbarIconLength,taskbarIconLength);

        //draw center color
        g.setColor(Color.black);
        g.fillRect(taskbarBorderLength,taskbarBorderLength,
                taskbarIconLength - taskbarBorderLength * 2,
                taskbarIconLength - taskbarBorderLength * 2);

        //draw darker image
        Font labelFont = new Font("Agency FB",Font.BOLD, 28);

        BufferedImage darkerBufferedImage = new BufferedImage(taskbarIconLength, taskbarIconLength, BufferedImage.TYPE_INT_RGB);
        Graphics g2 = darkerBufferedImage.getGraphics();

        //set border color
        g2.setColor(borderColor.darker());
        g2.fillRect(0,0,taskbarIconLength,taskbarIconLength);

        //draw center color
        g2.setColor(Color.black);
        g2.fillRect(taskbarBorderLength,taskbarBorderLength,
                taskbarIconLength - taskbarBorderLength * 2,
                taskbarIconLength - taskbarBorderLength * 2);

        g2.setColor(CyderColors.vanila);
        g2.setFont(labelFont);
        g2.setColor(CyderColors.vanila);

        String iconTitle = title.substring(0, Math.min(4, title.length())).trim();
        CyderLabel titleLabel = new CyderLabel(iconTitle);
        titleLabel.setFont(labelFont);
        titleLabel.setForeground(CyderColors.vanila);
        titleLabel.setBounds(0,0, taskbarIconLength, taskbarIconLength);
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
     * @param title the title of the component
     * @param clickAction the action to invoke when the icon is clicked
     * @return the taskbar component
     */
    public static JLabel generateDefaultTaskbarComponent(String title, Runnable clickAction) {
        return generateDefaultTaskbarComponent(title, clickAction, CyderColors.taskbarDefaultColor);
    }

    /**
     * Sets the frame's visibility attribute and adds the frame to the ConsoleFrame taskbar list.
     *
     * @param b whether to set the frame to be visible
     */
    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);

        //add to console frame's taskbar as long as it's not an exception
        if (b && !ConsoleFrame.INSTANCE.isClosed() && this != ConsoleFrame.INSTANCE.getConsoleCyderFrame()) {
            ConsoleFrame.INSTANCE.addTaskbarIcon(this);
        }

        //if the console is set to always on top, then we need this frame to be automatically set on top as well
        // so that new frames are not behind the console
        if (b && ConsoleFrame.INSTANCE.getConsoleCyderFrame() != null &&
                ConsoleFrame.INSTANCE.getConsoleCyderFrame().isAlwaysOnTop()) {
            setAlwaysOnTop(true);

            if (topDrag != null)
                topDrag.refreshPinButton();
        }
    }

    // ----------------
    // debug lines
    // ----------------

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
                .getIDstring().replaceAll("[^0-9]",""));
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
     * Returns the center point of this frame.
     *
     * @return the center point of this frame
     */
    public Point getCenterPoint() {
        if (this == null)
            throw new IllegalStateException("This frame is null");

        int centerX = getX() + (getWidth() / 2);
        int centerY = getY() + (getHeight() / 2);

        return new Point(centerX, centerY);
    }

    // -----------
    // Transparency during drag events
    // -----------

    /**
     * The opacity value to set the frame to on drag events.
     */
    public static final float DRAG_OPACITY = 0.7f;

    /**
     * Sets the opacity of the frame to {@link CyderFrame#DRAG_OPACITY}.
     */
    protected void startDragEvent() {
        setOpacity(DRAG_OPACITY);
    }

    /**
     * Sets the opacity of the frame to 1.0f.
     */
    protected void endDragEvent() {
        setOpacity(1.0f);
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
                titleLabel.setForeground(CyderColors.vanila);
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
    private final LinkedList<JLabel> menuItems = new LinkedList<>() {
        @Override
        public boolean add(JLabel label) {
            boolean ret = false;

            if (!menuItems.contains(label)) {
                ret = super.add(label);
            }

            if (menuItems.size() == 1)
                titleLabel.addMouseListener(titleLabelListener);

            return ret;
        }

        @Override
        public boolean remove(Object o) {
            if (!(o instanceof JLabel))
                return false;

            boolean ret = false;

            if (menuItems.contains(o)) {
                ret = super.remove(o);
            }

            if (menuItems.isEmpty())
                titleLabel.removeMouseListener(titleLabelListener);

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
     * Adds a new menu item to the menu and revalidates the menu.
     *
     * @param text the label text
     * @param onClick the function to run upon clicking
     */
    public void addMenuItem(String text, Runnable onClick) {
        checkArgument(text != null, "Text is null");
        checkNotNull(!text.isEmpty(), "Provided text is empty");
        checkNotNull(onClick != null, "onClick runnable action is null");

        // just to be safe
        text = text.trim();

        // account for possible overflow in clean way
        if (text.length() > maxTextLength)
            text = (text.substring(0, maxTextLength - 3).trim() + "...");

        JLabel newLabel = new JLabel(text);
        newLabel.setFont(CyderFonts.defaultFontSmall);
        newLabel.setForeground(CyderColors.vanila);
        newLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onClick.run();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                newLabel.setForeground(CyderColors.regularRed);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                newLabel.setForeground(CyderColors.vanila);
            }
        });
        menuItems.add(newLabel);

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
     * Animatesthe menu label in.
     */
    private void animateMenuIn() {
        if (!menuLabel.isVisible())
            generateMenu();

        CyderThreadRunner.submit(() -> {
            try {
                if (currentMenuType == MenuType.PANEL) {
                    menuLabel.setLocation(- menuLabel.getWidth(), animateMenuToPoint.getLocation().y);
                    menuLabel.setVisible(true);
                    for (int x = menuLabel.getX(); x < animateMenuToPoint.x ; x += 1) {
                        menuLabel.setLocation(x, menuLabel.getY());
                        Thread.sleep(2);
                    }
                } else {
                    menuLabel.setLocation(animateMenuToPoint.x,
                            animateMenuToPoint.y - menuLabel.getHeight());
                    menuLabel.setVisible(true);
                    for (int y = menuLabel.getY() ; y <= animateMenuToPoint.y; y += 1) {
                        menuLabel.setLocation(animateMenuToPoint.x, y);
                        Thread.sleep(2);
                    }
                }

                menuLabel.setLocation(animateMenuToPoint);
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, getTitle() + " menu label animator");
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
                    for (int x = menuLabel.getX() ; x > - menuLabel.getWidth() ; x -= 1) {
                        menuLabel.setLocation(x, menuLabel.getY());
                        Thread.sleep(2);
                    }
                } else {
                    menuLabel.setLocation(animateMenuToPoint.x, animateMenuToPoint.y);
                    for (int y = menuLabel.getY() ; y >= animateMenuToPoint.y - menuLabel.getHeight(); y -= 1) {
                        menuLabel.setLocation(animateMenuToPoint.x, y);
                        Thread.sleep(2);
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
     * Generates the menu based off of the current menu components
     * and sets the location to the starting point for inward animation.
     */
    private void generateMenu() {
        if (menuLabel != null)
            menuLabel.setVisible(false);

        menuLabel = new JLabel();
        menuLabel.setOpaque(true);
        menuLabel.setBackground(CyderColors.getGuiThemeColor());

        if (currentMenuType == MenuType.PANEL) {
            int menuHeight = 2 * paddingHeight +
                    (menuItems.size() * (StringUtil.getAbsoluteMinHeight(String.valueOf(CyderNumbers.JENNY),
                            CyderFonts.defaultFontSmall))) + 5;

            int sub = 5;

            if (menuHeight > getHeight() - topDrag.getHeight() - sub) {
                menuHeight = getHeight() - topDrag.getHeight() - sub;
            }

            menuLabel.setSize(menuWidth, menuHeight);
            menuLabel.setBorder(new LineBorder(Color.black, 4));
        } else {
            menuLabel.setSize(getWidth() - 10,
                    (StringUtil.getMinHeight(String.valueOf(CyderNumbers.JENNY), CyderFonts.defaultFontSmall)));
            menuLabel.setBorder(new LineBorder(Color.black, 4));
        }

        Dimension menuSize = new Dimension(menuLabel.getWidth(), menuLabel.getHeight());

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

        if (currentMenuType == MenuType.PANEL) {
            for (int i = 0 ; i < menuItems.size() ; i++) {
                printingUtil.printComponent(menuItems.get(i));

                if (i != menuItems.size() - 1) {
                    printingUtil.print("\n");
                }
            }
        } else {
            for (int i = 0 ; i < menuItems.size() ; i++) {
                printingUtil.printComponent(menuItems.get(i));

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
     * Sets the frame's location relative to the dominant frame,
     * the visibility to true, and sets always on top mode to true
     * temporarily to ensure the frame is on top.
     */
    public void finalizeAndShow() {
        setLocationRelativeTo(getDominantFrame());
        setVisible(true);

        boolean wasOnTop = isAlwaysOnTop();
        setAlwaysOnTop(true);

        // if it wasn't always on top, set to false after half a second
        if (!wasOnTop) {
            CyderThreadRunner.submit(() -> {
                try {
                    Thread.sleep(715);
                    setAlwaysOnTop(wasOnTop);
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                }
            }, "[" + getTitle() + "]  finalizeAndShow()");
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
            } else return ConsoleFrame.INSTANCE.getConsoleCyderFrame();
        } else if (!LoginHandler.isLoginFrameClosed() && LoginHandler.getLoginFrame() != null){
            return LoginHandler.getLoginFrame();
        }
        // other possibly dominant/stand-alone frame checks here
        else return null;
    }
}