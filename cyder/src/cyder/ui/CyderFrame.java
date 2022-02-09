package cyder.ui;

import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderIcons;
import cyder.constants.CyderNums;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;
import cyder.handlers.internal.PopupHandler;
import cyder.ui.objects.NotificationBuilder;
import cyder.ui.objects.QueuedNotification;
import cyder.utilities.*;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * CyderFrame component is the primary backbone that all of Cyder lays on.
 */
public class CyderFrame extends JFrame {
    /**
     * The maximum allowable frame dimension to notification dimension before
     * the notification is turned into a popup pane.
     */
    public static final float NOTIFICATION_MAX_RATIO = 0.9f;

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
    private int width = DEFAULT_WIDTH;

    /**
     * This CyderFrame's height.
     */
    private int height = DEFAULT_HEIGHT;

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
    private DragLabel topDrag;
    /**
     * The bottom component responsible for frame location changes on the bottom.
     */
    private DragLabel bottomDrag;

    /**
     * The left component responsible for frame location changes on the left.
     */
    private DragLabel leftDrag;

    /**
     * The right component responsible for frame location changes on the right.
     */
    private DragLabel rightDrag;

    /**
     * The x position of the frame to set to after frame deiconification actions.
     */
    private int restoreX = Integer.MAX_VALUE;

    /**
     * The y position of the frame to set to after frame deiconification actions.
     */
    private int restoreY = Integer.MIN_VALUE;

    /**
     * The title of the CyderFrame controlled by the position enum.
     */
    private JLabel titleLabel;

    /**
     * The text displayed on the title label.
     */
    private String title = "CyderFrame [" + this.hashCode() + "]";

    /**
     * The "content pane" of the CyderFrame. This is what is returned
     * when a getContentPane() call is invoked and is what components are added to.
     */
    private JLabel iconLabel;

    /**
     * The true content pane of the CyderFrame. This is necessary so we can do layering
     * between the components, the background, the background image, notfications,
     * drag labels, etc.
     */
    private JLayeredPane contentLabel;

    /**
     * Another layered pane that the content label is added to for layering purposes.
     */
    private JLayeredPane iconPane;

    /**
     * Speeds up performance by not repainting anything on t
     * he frame during animations such as minimize and close.
     */
    private boolean disableContentRepainting = false;

    /**
     * The background color of our CyderFrame.
     * This is the color behind the image if there is one.
     */
    private Color backgroundColor = CyderColors.navy;

    /**
     * The list of notifications that have yet to be pulled and notified via this frame.
     */
    private ArrayList<QueuedNotification> notificationList = new ArrayList<>();

    /**
     * The area exposed to allow frame resizing. The maximum is 5 since
     * 5 is the border of the frame.
     */
    private static int frameResizingLen = 2;

    /**
     * The size of the border drawn around the frame.
     */
    public static final int borderLen = 5;

    /**
     * The degree angle increment used for the barrel roll animation.
     */
    public static final  double BARREL_ROLL_DELTA = 2.0;

    /**
     * Constructs an instance of CyderFrame with the specified width, height, and
     * ImageIcon which is used for the background.
     * (you may enable resizing of the Frame and rescaling of the image should you choose)
     *
     * @param width the specified width of the cyder frame
     * @param height the specified height of the cyder frame
     * @param background the specified background image
     */
    public CyderFrame(int width, int height, ImageIcon background) {
        this.width = width;
        this.height = height;
        this.background = background;
        currentOrigIcon = background;

        //border color for consoleframe menu pane set in instantiation of object
        taskbarIconBorderColor = getTaskbarBorderColor();

        //this . methods
        setSize(new Dimension(width, height));
        setResizable(false);
        setUndecorated(true);
        setBackground(CyderColors.vanila);
        setIconImage(CyderIcons.CYDER_ICON.getImage());

        //try and get preference for frame shape
        if (ConsoleFrame.getConsoleFrame().getUUID() != null) {
            if (UserUtil.extractUser().getRoundedwindows().equals("1")) {
                setShape(new RoundRectangle2D.Double(0, 0,
                        getWidth(), getHeight(), 20, 20));
            } else {
                setShape(null);
            }
        }

        //listener to ensure the close button was always pressed which ensures
        // things like closeAnimation are always performed
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        //master contentlabel
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

        //adding pane, this is what is returned when getContentPane() is called
        iconLabel = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
               if (!chameleonActive)
                   super.paintComponent(g);
            }

            @Override
            public void repaint() {
                //as long as we should repaint, repaint it
                if (!disableContentRepainting) {
                    super.repaint();
                }
            }

            @Override
            public void setBounds(int x, int y, int w, int h) {
                super.setBounds(x, y, w, h);

                //if there's a panel we need to extend its bounds too
                if (cyderPanel != null && w > 0 && h > 0) {
                    cyderPanel.setBounds(borderLen, DragLabel.getDefaultHeight(), getWidth() - 2 * borderLen,
                            getHeight() - DragLabel.getDefaultHeight() - borderLen);
                }
            }
        };
        iconLabel.setIcon(background);
        iconLabel.setBounds(frameResizingLen,frameResizingLen,
                width - 2 * frameResizingLen,height - 2 * frameResizingLen);
        iconLabel.setFocusable(false);

        iconPane = new JLayeredPane();
        iconPane.setBounds(frameResizingLen,frameResizingLen,
                width - 2 * frameResizingLen, height - 2 * frameResizingLen);
        iconPane.add(iconLabel,JLayeredPane.DEFAULT_LAYER);
        iconPane.setFocusable(false);
        contentLabel.add(iconPane,JLayeredPane.DEFAULT_LAYER);

        contentLabel.setBorder(new LineBorder(CyderColors.guiThemeColor, 3, false));
        setContentPane(contentLabel);

        //top frame drag and cover
        topDrag = new DragLabel(width - 2 * frameResizingLen, DragLabel.getDefaultHeight() - 2, this);
        topDrag.setBounds(frameResizingLen, frameResizingLen, width - 2 * frameResizingLen, DragLabel.getDefaultHeight() - 2);
        topDrag.setxOffset(frameResizingLen);
        topDrag.setyOffset(frameResizingLen);
        contentLabel.add(topDrag, JLayeredPane.DRAG_LAYER);
        topDrag.setFocusable(false);

        topDragCover = new JLabel();
        topDragCover.setBounds(0, 0 , width, 2);
        topDragCover.setBackground(CyderColors.guiThemeColor);
        topDragCover.setOpaque(true);
        contentLabel.add(topDragCover, JLayeredPane.DRAG_LAYER);

        //left frame drag  and cover
        leftDrag = new DragLabel(5 - frameResizingLen, height - frameResizingLen - DragLabel.getDefaultHeight(), this);
        leftDrag.setBounds(frameResizingLen, DragLabel.getDefaultHeight(), 5 - frameResizingLen, height - DragLabel.getDefaultHeight() - frameResizingLen);
        leftDrag.setxOffset(frameResizingLen);
        leftDrag.setyOffset(DragLabel.getDefaultHeight());
        contentLabel.add(leftDrag, JLayeredPane.DRAG_LAYER);
        leftDrag.setFocusable(false);
        leftDrag.setButtonsList(null);

        leftDragCover = new JLabel();
        leftDragCover.setBounds(0, 0 , 2, height);
        leftDragCover.setBackground(CyderColors.guiThemeColor);
        leftDragCover.setOpaque(true);
        contentLabel.add(leftDragCover, JLayeredPane.DRAG_LAYER);

        //right frame drag and cover
        rightDrag = new DragLabel(5 - frameResizingLen, height - frameResizingLen - DragLabel.getDefaultHeight(), this);
        rightDrag.setBounds(width - 5, DragLabel.getDefaultHeight(), 5 - frameResizingLen, height - DragLabel.getDefaultHeight() - frameResizingLen);
        rightDrag.setxOffset(width - 5);
        rightDrag.setyOffset(DragLabel.getDefaultHeight());
        contentLabel.add(rightDrag, JLayeredPane.DRAG_LAYER);
        rightDrag.setFocusable(false);
        rightDrag.setButtonsList(null);

        rightDragCover = new JLabel();
        rightDragCover.setBounds(width - 2, 0 , 2, height);
        rightDragCover.setBackground(CyderColors.guiThemeColor);
        rightDragCover.setOpaque(true);
        contentLabel.add(rightDragCover, JLayeredPane.DRAG_LAYER);

        //bottom frame drag  and cover
        bottomDrag = new DragLabel(width - 2 * frameResizingLen, 5 - frameResizingLen, this);
        bottomDrag.setBounds(frameResizingLen, height - 5, width - 4, 5 - frameResizingLen);
        bottomDrag.setxOffset(frameResizingLen);
        bottomDrag.setyOffset(height - 5);
        contentLabel.add(bottomDrag, JLayeredPane.DRAG_LAYER);
        bottomDrag.setFocusable(false);
        bottomDrag.setButtonsList(null);

        bottomDragCover = new JLabel();
        bottomDragCover.setBounds(0, height - 2 , width, 2);
        bottomDragCover.setBackground(CyderColors.guiThemeColor);
        bottomDragCover.setOpaque(true);
        contentLabel.add(bottomDragCover, JLayeredPane.DRAG_LAYER);

        //title label on drag label
        titleLabel = new JLabel("");
        titleLabel.setFont(CyderFonts.frameTitleFont);
        titleLabel.setForeground(CyderColors.vanila);
        titleLabel.setFocusable(false);
        topDrag.add(titleLabel);

        //default boolean values
        this.threadsKilled = false;

        //frame type handling
        setFrameType(this.frameType);
    }

    /**
     * Returns an instance of a CyderFrame lacking any borders and drag labels. The content label
     * itself has mouselisteners which allow for movement so it is simply up to you to determine
     * how the frame is filled with components and most importantly, how it is disposed properly.
     */
    public static CyderFrame getBorderlessFrame(int width, int height) {
        return new CyderFrame(NumberUtil.randInt(0, CyderNums.INFINITY - 1), width, height);
    }

    /**
     * Constructs a CyderFrame object as a CyderFrame that exists without
     * surrounding drag labels, the title label, and the button list.
     *
     * @param borderlessID the integer ID of this frame instance
     * @param width the width of this CyderFrame
     * @param height the height of this CyderFrame
     */
    private CyderFrame(int borderlessID, int width, int height) {
        this.width = width;
        this.height = height;

        //this . methods
        setSize(new Dimension(width, height));
        setResizable(false);
        setUndecorated(true);
        setBackground(CyderColors.navy);
        setIconImage(CyderIcons.CYDER_ICON.getImage());

        //listener to ensure the close button was always pressed which ensures
        // things like closeAnimation are always performed
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });

        //master contentlabel
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
        iconLabel.setIcon(background);
        iconLabel.setBounds(frameResizingLen,frameResizingLen,
                width - 2 * frameResizingLen,height - 2 * frameResizingLen);
        iconLabel.setFocusable(false);

        iconPane = new JLayeredPane();
        iconPane.setBounds(frameResizingLen,frameResizingLen,
                width - 2 * frameResizingLen, height - 2 * frameResizingLen);
        iconPane.add(iconLabel,JLayeredPane.DEFAULT_LAYER);
        iconPane.setFocusable(false);
        contentLabel.add(iconPane,JLayeredPane.DEFAULT_LAYER);

        contentLabel.setBorder(new LineBorder(Color.black, 3, false));
        setContentPane(contentLabel);

        final int[] xMouse = {0};
        final int[] yMouse = {0};

        //contentLabel drag listener for frame moving around
        addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int x = e.getXOnScreen();
                int y = e.getYOnScreen();

                if (this != null && isFocused()) {
                    setLocation(x - xMouse[0], y - yMouse[0]);
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                xMouse[0] = e.getX();
                yMouse[0] = e.getY();
            }
        });

        //default boolean values
        this.threadsKilled = false;
    }

    /**
     * Default constructor for CyderFrame using the {@link CyderFrame#DEFAULT_WIDTH}
     * and {@link CyderFrame#DEFAULT_HEIGHT} for dimensions
     */
    public CyderFrame() {
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Default constructor for a CyderFrame with specified width and height.
     *
     * @param width the specified width of the CyderFrame
     * @param height the specified height of the CyderFrame
     */
    public CyderFrame(int width, int height) {
        this(width, height, CyderIcons.defaultBackground);
    }

    /**
     * Default CyderFrame constructor for width, height, and content label background color.
     *
     * @param width the width of the CyderFrame
     * @param height the height of the CyderFrame
     * @param c the color of the content pane background
     */
    public CyderFrame(int width, int height, Color c) {
        this(width, height, ImageUtil.imageIconFromColor(c, width, height));
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
     * how the components on the content pane are layed out.
     */
    private CyderPanel cyderPanel;

    /**
     * Adds the provided CyderPanel on top of the content pane which is also resized on
     * CyderFrame resize events.
     * 
     * @param cyderPanel the CyderPanel with an approprite CyderLayout
     */
    public void setContentPanel(CyderPanel cyderPanel) {
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
        cyderPanel.setBounds(borderLen, DragLabel.getDefaultHeight(), getWidth() - 2 * borderLen,
                getHeight() - DragLabel.getDefaultHeight() - borderLen);
        iconLabel.add(cyderPanel);
        cyderPanel.repaint();
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
                new Thread(() -> {
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
                },"title position animater").start();
            } else if (titlePosition == TitlePosition.CENTER){
                new Thread(() -> {
                    switch (oldPosition) {
                        case RIGHT:
                            for (int i = titleLabel.getX() ; i > (getTopDragLabel().getWidth() / 2)
                                    - (getMinWidth(this.title) / 2); i--) {
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
                                    - (getMinWidth(this.title) / 2); i++) {
                                titleLabel.setLocation(i, 2);

                                try {
                                    Thread.sleep(timeout);
                                } catch (Exception e) {
                                    ExceptionHandler.handle(e);
                                }
                            }
                            break;
                    }
                    titleLabel.setLocation((getTopDragLabel().getWidth() / 2) - (getMinWidth(this.title) / 2), 2);
                    this.titlePosition = TitlePosition.CENTER;
                    //set final bounds
                },"title position animater").start();
            } else {
                //right
                new Thread(() -> {
                    for (int i = titleLabel.getX() ; i < this.width - getMinWidth(this.title) - 8; i++) {
                        titleLabel.setLocation(i, 2);

                        try {
                            Thread.sleep(timeout);
                        } catch (Exception e) {
                            ExceptionHandler.handle(e);
                        }
                    }
                    titleLabel.setLocation(this.width -getMinWidth(this.title), 2);
                    this.titlePosition = TitlePosition.RIGHT;
                },"title position animater").start();
            }

            if (buttonPosition == ButtonPosition.RIGHT && titlePosition == TitlePosition.RIGHT) {
                buttonPosition = ButtonPosition.LEFT;
                getTopDragLabel().setButtonPosition(DragLabel.ButtonPosition.LEFT);
            } else if (buttonPosition == ButtonPosition.LEFT && titlePosition == TitlePosition.LEFT) {
                buttonPosition = ButtonPosition.RIGHT;
                getTopDragLabel().setButtonPosition(DragLabel.ButtonPosition.RIGHT);
            }
        } else {
            this.titlePosition = titlePosition;

            switch (titlePosition) {
                case LEFT:
                    titleLabel.setLocation(4, 2);
                    setButtonPosition(ButtonPosition.RIGHT);
                    break;
                case RIGHT:
                    titleLabel.setLocation(this.width - getMinWidth(this.title), 2);
                    setButtonPosition(ButtonPosition.LEFT);
                    break;
                case CENTER:
                    titleLabel.setLocation((getTopDragLabel().getWidth() / 2) - (getMinWidth(this.title) / 2), 2);
            }
        }
    }

    /**
     * Returns the ttile position of this frame.
     *
     * @return the ttile position of this frame
     */
    public TitlePosition getTitlePosition() {
        return this.titlePosition;
    }

    /**
     * Returns the button position of this frame.
     *
     * @return the button position of this frame
     */
    public ButtonPosition getButtonPosition() {
        return this.buttonPosition;
    }

    /**
     * Sets the button position of this frame.
     *
     * @param pos the position to set the button list to. See {@link CyderFrame#buttonPosition}
     */
    public void setButtonPosition(ButtonPosition pos) {
        if (pos == this.buttonPosition)
            return;

        ButtonPosition old = this.buttonPosition;
        this.buttonPosition = pos;
        topDrag.setButtonPosition(pos == ButtonPosition.LEFT ?
                DragLabel.ButtonPosition.LEFT : DragLabel.ButtonPosition.RIGHT);

        if (buttonPosition == ButtonPosition.RIGHT && titlePosition == TitlePosition.RIGHT) {
            this.titlePosition = TitlePosition.LEFT;
            titleLabel.setLocation(4, 2);
        } else if (buttonPosition == ButtonPosition.LEFT && titlePosition == TitlePosition.LEFT) {
            this.titlePosition = TitlePosition.RIGHT;
            titleLabel.setLocation(this.width -getMinWidth(this.title), 2);
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
                this.setAlwaysOnTop(false);
                break;
            case POPUP:
                this.setAlwaysOnTop(true);
                //remove minimize
                topDrag.removeButton(0);
                //remove pin
                topDrag.removeButton(0);
                break;
            case INPUT_GETTER:
                this.setAlwaysOnTop(true);
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
        super.setTitle(paintSuperTitle ? title : "");

        if (paintWindowTitle && title != null && title.length() != 0 && titleLabel != null) {
            int titleWidth = getTitleWidth(title);
            String shortenedTitle = title;

            while (titleWidth > this.width * 0.70) {
                shortenedTitle = shortenedTitle.substring(0, shortenedTitle.length() - 1);
                titleWidth = getTitleWidth(shortenedTitle + "...");
            }

            if (titleWidth != getTitleWidth(title))
                shortenedTitle += "...";

            this.title = shortenedTitle;
            titleLabel.setText(this.title);

            titleWidth = getTitleWidth(this.title);

            switch (titlePosition) {
                case CENTER:
                    titleLabel.setBounds((getTopDragLabel().getWidth() / 2) - (titleWidth / 2), 2, titleWidth, 25);
                    break;
                case RIGHT:
                    titleLabel.setBounds(this.width - titleWidth, 2, titleWidth, 25);
                    break;
                case LEFT:
                    titleLabel.setBounds(5, 2, titleWidth, 25);
                    break;
            }
        }
    }

    // ------------------------------------
    // bounds calculations for text
    // ------------------------------------

    /**
     * Returns the minimum width required for the given String using notificationFont.
     *
     * @param title the text you want to determine the width of
     * @return an interger value determining the minimum width of
     * a string of text (10 is added to avoid ... bug)
     */
    private int getMinWidth(String title) {
        Font notificationFont = titleLabel.getFont();
        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform, true, true);
        return (int) notificationFont.getStringBounds(title, frc).getWidth() + 10;
    }

    /**
     * Returns the minimum width required for the given String using the given font.
     *
     * @param title the text you want to determine the width of
     * @param f the font for the text
     * @return an interger value determining the minimum width of
     * a string of text (10 is added to avoid ... bug)
     */
    public static int getMinWidth(String title, Font f) {
        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform, true, true);
        return (int) f.getStringBounds(title, frc).getWidth() + 10;
    }

    /**
     * Returns the minimum width required for the given String using the given font.
     *
     * @param title the text you want to determine the width of
     * @param f the font for the text
     * @return an interger value determining the minimum width of a string of text
     */
    public static int getAbsoluteMinWidth(String title, Font f) {
        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform, true, true);
        return (int) f.getStringBounds(title, frc).getWidth();
    }

    /**
     * Returns the minimum width required for the given String
     * using the given font without adding 10 to the result.
     *
     * @param title the text you want to determine the width of
     * @return an interger value determining the minimum width of a string of text
     */
    public int getTitleWidth(String title) {
        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform, false, false);
        return (int) this.titleLabel.getFont().getStringBounds(title, frc).getWidth() + 10;
    }

    /**
     * Returns the minimum height required for the given String using the given font.
     *
     * @param title the text you want to determine the height of
     * @return an interger value determining the minimum height
     * of a string of text (10 is added to avoid ... bug)
     */
    public static int getMinHeight(String title, Font f) {
        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform, true, true);
        return (int) f.getStringBounds(title, frc).getHeight() + 10;
    }
    //todo these belong in bounds util, maybe all of bounds util should be in string util?
    /**
     * Returns the minimum height required for the given String
     * using the given font without adding 10.
     *
     * @param title the text you want to determine the height of
     * @return an interger value determining the minimum height of a string of text
     */
    public static int getAbsoluteMinHeight(String title, Font f) {
        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform, true, true);
        return (int) f.getStringBounds(title, frc).getHeight();
    }

    // ------------------
    // notifications
    // ------------------

    /**
     * The notification that is currently being displayed.
     */
    private Notification currentNotification;

    //todo need both? eliminate one it feels like you could do this

    /**
     * The current queued notification that is currently being displayed.
     */
    private QueuedNotification currentQueuedNotification;

    /**
     * Whether the notification thread has been started for this frame.
     */
    private boolean notificationCheckerStarted = false;

    /**
     * Returns the current notification.
     *
     * @return the current notification
     */
    public Notification getCurrentNotification() {
        return currentNotification;
    }

    /**
     * Simple, quick, and easy way to show a notification on the frame without using
     * a builder.
     *
     * @param htmlText the text containing possibly formatted text to display
     */
    public void notify(String htmlText) {
        this.notify(new NotificationBuilder(htmlText));
    }

    /**
     * Notifies the user with a custom notification built from the provided builder.
     * See {@link NotificationBuilder} for more information.
     *
     * @param notificationBuilder the builder used to construct the notification
     */
    public void notify(NotificationBuilder notificationBuilder) {
        if (StringUtil.getRawTextLength(notificationBuilder.getHtmltext()) < 3)
            throw new IllegalArgumentException("Raw text must be 3 characters or greater");

        notificationList.add(new QueuedNotification(
                notificationBuilder.getHtmltext(),
                notificationBuilder.getViewDuration(),
                notificationBuilder.getArrowDir(),
                notificationBuilder.getNotificationDirection(),
                notificationBuilder.getOnKillAction(),
                notificationBuilder.getContainer(),
                notificationBuilder.getNotificationBackground(),
                TimeUtil.notificationTime()));

        if (!notificationCheckerStarted) {
            new Thread(notificationQueue, this + " notification queue checker").start();
            notificationCheckerStarted = true;
        }
    }

    /**
     * The notification queue for internal frame notifications.
     */
    private final Runnable notificationQueue = () -> {
        try {
            while (this != null && !threadsKilled) {
                if (notificationList.size() > 0) {
                    QueuedNotification currentQueuedNotification = notificationList.remove(0);
                    this.currentQueuedNotification = currentQueuedNotification;

                    //init notification object
                    currentNotification = new Notification();

                    if (currentQueuedNotification.getNotificationBackground() != null)
                        currentNotification.setBackgroundColor(currentQueuedNotification.getNotificationBackground());

                    //set the arrow direction
                    currentNotification.setArrow(currentQueuedNotification.getArrowDir());

                    //create text label to go on top of notification label
                    JLabel text = new JLabel(currentQueuedNotification.getHtmlText());

                    //get dimensions and formatted text for the notification
                    BoundsUtil.BoundsString bs = BoundsUtil.widthHeightCalculation(text.getText(),
                            (int) (this.width * 0.8), CyderFonts.notificationFont);
                    int w = bs.getWidth();
                    int h = bs.getHeight();
                    text.setText(bs.getText());

                    //if too big for the frame, turn it into an external frame popup
                    if (h > this.height * NOTIFICATION_MAX_RATIO
                            || w > this.width * NOTIFICATION_MAX_RATIO) {
                        this.inform(text.getText(),"Notification");
                        continue;
                    }

                    if (currentQueuedNotification.getContianer() == null) {
                        //set the text bounds to the proper x,y and theest
                        // calculated width and height
                        text.setBounds(currentNotification.getTextXOffset(), currentNotification.getTextYOffset(), w, h);

                        currentNotification.setWidth(w);
                        currentNotification.setHeight(h);

                        text.setFont(CyderFonts.notificationFont);
                        text.setForeground(CyderColors.notificationForegroundColor);
                        currentNotification.add(text);

                        JLabel disposeLabel = new JLabel();
                        disposeLabel.setBounds(currentNotification.getTextXOffset(), currentNotification.getTextYOffset(), w, h);

                        disposeLabel.setToolTipText("Notified at: " + currentQueuedNotification.getTime());
                        disposeLabel.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                //fire any on kill actions if it's not null
                                if (currentQueuedNotification.getOnKillAction() != null) {
                                    currentNotification.kill();
                                    currentQueuedNotification.getOnKillAction().fire();
                                } else {
                                    //smoothly animate notification away
                                    currentNotification.vanish(currentQueuedNotification.getNotificationDirection(),
                                            getContentPane(), 0);

                                }
                            }
                        });
                        currentNotification.add(disposeLabel);
                    } else {
                        currentNotification.setWidth(currentQueuedNotification.getContianer().getWidth());
                        currentNotification.setHeight(currentQueuedNotification.getContianer().getHeight());

                        //container should have things on it already so no need to place text here
                        currentNotification.add(currentQueuedNotification.getContianer());
                    }

                    switch (currentQueuedNotification.getNotificationDirection()) {
                        case TOP_LEFT:
                            currentNotification.setLocation(-currentNotification.getWidth() + 5, topDrag.getHeight());
                            break;
                        case TOP_RIGHT:
                            currentNotification.setLocation(getContentPane().getWidth() - 5 + currentNotification.getWidth(),
                                    topDrag.getHeight());
                            break;
                        case BOTTOM:
                            currentNotification.setLocation(getContentPane().getWidth() / 2 - (w / 2) - currentNotification.getTextXOffset(),
                                    getHeight() - 5);
                            break;
                        case LEFT:
                            currentNotification.setLocation(-currentNotification.getWidth() + 5,
                                    getContentPane().getHeight() / 2 - (h / 2) - currentNotification.getTextYOffset());
                            break;
                        case RIGHT:
                            currentNotification.setLocation(getContentPane().getWidth() - 5 + currentNotification.getWidth(),
                                    getContentPane().getHeight() / 2 - (h / 2) - currentNotification.getTextYOffset());
                            break;
                        case BOTTOM_LEFT:
                            //parent.getHeight() - this.getHeight() + 10
                            currentNotification.setLocation(-currentNotification.getWidth() + 5,
                                    getHeight() - currentNotification.getHeight() + 5);
                            break;
                        case BOTTOM_RIGHT:
                            currentNotification.setLocation(getContentPane().getWidth() - 5 + currentNotification.getWidth(),
                                    getHeight() - currentNotification.getHeight() + 5);
                            break;
                        default:  //top
                            currentNotification.setLocation(getContentPane().getWidth() / 2 - (w / 2) - currentNotification.getTextXOffset(),
                                    DragLabel.getDefaultHeight() - currentNotification.getHeight());
                    }

                    iconPane.add(currentNotification, JLayeredPane.POPUP_LAYER);
                    getContentPane().repaint();

                    //log the notification
                    Logger.log(Logger.Tag.ACTION, "[" +
                            this.getTitle() + "] [NOTIFICATION] " + currentQueuedNotification.getHtmlText());

                    //duration is always 300ms per word unless less than 5 seconds
                    int duration = 300 * StringUtil.countWords(
                            Jsoup.clean(bs.getText(), Safelist.none())
                    );
                    duration = Math.max(duration, 5000);
                    duration = currentQueuedNotification.getDuration() == 0 ?
                            duration : currentQueuedNotification.getDuration();
                    currentNotification.appear(currentQueuedNotification.getNotificationDirection(), getContentPane(), duration);

                    while (getCurrentNotification().isVisible())
                        Thread.onSpinWait();
                } else {
                    //for optimization purposes, end queue thread
                    notificationCheckerStarted = false;
                    break;
                }
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
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
     * @param animate whether or not to kill the notification
     *                immediately or to smoothly animate it away first
     */
    public void revokeCurrentNotification(boolean animate) {
        if (animate) {
            currentNotification.vanish(currentQueuedNotification.getNotificationDirection(), this, 0);
        } else {
            currentNotification.kill();
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
    public DragLabel getTopDragLabel() {
        return topDrag;
    }

    /**
     * Returns the bottom drag label.
     *
     * @return the bottom drag label
     */
    public DragLabel getBottomDragLabel() {
        return bottomDrag;
    }

    /**
     * Returns the left drag label.
     *
     * @return the left drag label
     */
    public DragLabel getLeftDragLabel() {
        return leftDrag;
    }

    /**
     * Returns the right drag label.
     *
     * @return the right drag label
     */
    public DragLabel getRightDragLabel() {
        return rightDrag;
    }

    /**
     * Pops open a window relative to this CyderFrame with the provided text.
     *
     * @param text  the String you wish to display
     * @param title The title of the CyderFrame which will be opened to display the text
     */
    public void inform(String text, String title) {
        PopupHandler.inform(text, title, this);
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
            //set restore vars here
            setRestoreX(getX());
            setRestoreY(getY());

            if (UserUtil.getUserData("minimizeanimation").equals("1")) {
                setDisableContentRepainting(true);

                //figure out increment for frame num
                int distanceToTravel = ScreenUtil.getScreenHeight() - this.getY();
                //25 frames to animate
                int animationInc = (int) ((double ) distanceToTravel / animationFrames);

                for (int i = this.getY(); i <= ScreenUtil.getScreenHeight(); i += animationInc) {
                    Thread.sleep(1);
                    setLocation(this.getX(), i);
                }

                setDisableContentRepainting(false);
            }

            setState(JFrame.ICONIFIED);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
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
        return this.disposed;
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
     * Disposes the frame.
     *
     * @param fastClose whether to animate the frame away or immediately dispose the frame
     */
    public void dispose(boolean fastClose) {
        Logger.log(Logger.Tag.ACTION, "CyderFrame disposed with fastclose: " + fastClose + ", CyderFrame: " + this);

        new Thread(() -> {
            try {
                if (this == null || this.disposed)
                    return;

                disposed = true;

                //if closing confirmation exists and the user decides they do not want to exit the frame
                if (closingConfirmationMessage != null) {
                    boolean exit = new GetterUtil().getConfirmation(closingConfirmationMessage, this);

                    if (!exit)
                        return;
                }

                //run all preCloseActions if any exists, this is performed after the confirmation check
                // since now we are sure that we wish to close the frame
                for (PreCloseAction action : preCloseActions)
                    action.invokeAction();

                if (currentNotification != null)
                    currentNotification.kill();

                //kill all threads
                killThreads();

                //disable dragging
                disableDragging();

                //disable content pane REPAINTING not paint to speed up the animation
                setDisableContentRepainting(true);

                if (this != null && isVisible() && !fastClose
                        && UserUtil.getUserData("closeanimation").equals("1")) {
                    Point point = getLocationOnScreen();
                    int x = (int) point.getX();
                    int y = (int) point.getY();

                    //figure out increment for frames
                    int distanceToTravel = Math.abs(this.getY()) + Math.abs(this.getHeight());
                    //25 frames to animate
                    int animationInc = (int) ((double) distanceToTravel / animationFrames);

                    for (int i = this.getY(); i >= -this.getHeight() ; i -= animationInc) {
                        Thread.sleep(1);
                        setLocation(this.getX(), i);
                    }
                }

                //remove from consoleframe
                ConsoleFrame.getConsoleFrame().removeTaskbarIcon(this);

                super.dispose();

                for (PostCloseAction action : postCloseActions)
                    action.invokeAction();
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, this.getTitle() + " CyderFrame dispose thread").start();
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
            this.enableDragging();
        } else {
           this.disableDragging();
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
    private int dancingIncrement = 10;

    /**
     * Whether dancing has finished for this frame.
     */
    private boolean dancingFinished = false;

    /**
     * Sets the direction the frame is currently dancing in.
     *
     * @param dancingDirection the direction the frame is currently dancing in
     */
    protected void setDancingDirection(DancingDirection dancingDirection) {
        this.dancingDirection = dancingDirection;
    }

    /**
     * Returns whether dancing has concluded for this frame.
     *
     * @return whether dancing has concluded for this frame
     */
    protected boolean isDancingFinished() {
        return dancingFinished;
    }

    /**
     * Sets whether dancing has concluded.
     *
     * @param dancingFinished whether dancing has concluded
     */
    protected void setDancingFinished(boolean dancingFinished) {
        this.dancingFinished = dancingFinished;
    }

    /**
     * Takes a step in the current dancing direction for a dance routine.
     */
    protected void danceStep() {
        switch (dancingDirection) {
            case INITIAL_UP:
                this.setLocation(this.getX(), this.getY() - dancingIncrement);

                if (this.getY() < 0) {
                    this.setLocation(this.getX(), 0);
                    dancingDirection = DancingDirection.LEFT;
                }
                break;
            case LEFT:
                this.setLocation(this.getX() - 10, this.getY());

                if (this.getX() < 0) {
                    this.setLocation(0, 0);
                    dancingDirection = DancingDirection.DOWN;
                }
                break;
            case DOWN:
                this.setLocation(this.getX(), this.getY() + 10);

                if (this.getY() > ScreenUtil.getScreenHeight() - this.getHeight()) {
                    this.setLocation(this.getX(), ScreenUtil.getScreenHeight() - this.getHeight());
                    dancingDirection = DancingDirection.RIGHT;
                }
                break;
            case RIGHT:
                this.setLocation(this.getX() + 10, this.getY());

                if (this.getX() > ScreenUtil.getScreenWidth() - this.getWidth()) {
                    this.setLocation(ScreenUtil.getScreenWidth() - this.getWidth(), this.getY());
                    dancingDirection = DancingDirection.UP;
                }
                break;
            case UP:
                this.setLocation(this.getX(), this.getY() - 10);

                if (this.getY() < 0) {
                    this.setLocation(this.getX(), 0);
                    dancingFinished = true;
                    dancingDirection = DancingDirection.LEFT;
                }
                break;
        }
    }

    /**
     * transforms the content pane by an incremental angle of 2 degrees
     * emulating Google's barrel roll easter egg.
     */
    public void barrelRoll() {
        ImageIcon masterIcon = (ImageIcon) ((JLabel) getContentPane()).getIcon();
        BufferedImage master = ImageUtil.getBi(masterIcon);

        Timer timer = null;
        Timer finalTimer = timer;

        timer = new Timer(10, new ActionListener() {
            private double angle = 0;

            BufferedImage rotated;

            @Override
            public void actionPerformed(ActionEvent e) {
                angle += BARREL_ROLL_DELTA;
                if (angle > 360) {
                    rotated = ImageUtil.rotateImageByDegrees(master, 0);
                    ((JLabel) getContentPane()).setIcon(new ImageIcon(rotated));
                    return;
                }
                rotated = ImageUtil.rotateImageByDegrees(master, angle);
                ((JLabel) getContentPane()).setIcon(new ImageIcon(rotated));
            }
        });
        timer.start();
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
     * Repaints the title position and button positions in the currently set enum locations.
     */
    public void refreshTitleAndButtonPosition() {
        switch (titlePosition) {
            case LEFT:
                titleLabel.setLocation(4, 2);
                break;
            case RIGHT:
                titleLabel.setLocation(this.width - getMinWidth(this.title), 2);
                break;
            case CENTER:
                titleLabel.setLocation((getTopDragLabel().getWidth() / 2) - (getMinWidth(this.title) / 2), 2);
                break;
        }

        switch (buttonPosition) {
            case LEFT:
                getTopDragLabel().setButtonPosition(DragLabel.ButtonPosition.LEFT);
                break;
            case RIGHT:
                getTopDragLabel().setButtonPosition(DragLabel.ButtonPosition.RIGHT);
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
        width = Math.max(MINIMUM_WIDTH, width);
        height = Math.max(MINIMUM_HEIGHT, height);
        super.setSize(width, height);
    }

    /**
     * Sets the bounds of the CyderFrame and refreshes all components on the frame.
     */
    @Override
    public void setBounds(int x, int y, int width, int height) {
        width = Math.max(MINIMUM_WIDTH, width);
        height = Math.max(MINIMUM_HEIGHT, height);
        super.setBounds(x, y, width, height);

        this.width = width;
        this.height = height;

        if (getTopDragLabel() != null) {
            topDrag.setWidth(this.width - 2 * frameResizingLen);
            topDrag.setHeight(DragLabel.getDefaultHeight() - frameResizingLen);

            topDragCover.setBounds(0, 0 , width, 2);
            topDragCover.setBackground(CyderColors.guiThemeColor);

            leftDrag.setWidth(5 - frameResizingLen);
            leftDrag.setHeight(this.height - DragLabel.getDefaultHeight() - frameResizingLen);

            leftDragCover.setBounds(0, 0 , 2, height);
            leftDragCover.setBackground(CyderColors.guiThemeColor);

            rightDrag.setWidth(5 - frameResizingLen);
            rightDrag.setHeight(this.height - DragLabel.getDefaultHeight() - frameResizingLen);

            rightDragCover.setBounds(width - 2, 0 , 2, height);
            rightDragCover.setBackground(CyderColors.guiThemeColor);

            bottomDrag.setWidth(this.width - frameResizingLen * 2);
            bottomDrag.setHeight(5 - frameResizingLen);

            bottomDragCover.setBounds(0, height - 2 , width, 2);
            bottomDragCover.setBackground(CyderColors.guiThemeColor);

            refreshTitleAndButtonPosition();

            topDrag.setBounds(frameResizingLen, frameResizingLen, this.width - 2 * frameResizingLen,
                    DragLabel.getDefaultHeight() - frameResizingLen);
            leftDrag.setBounds(frameResizingLen, DragLabel.getDefaultHeight(), 5 - frameResizingLen,
                    this.height - DragLabel.getDefaultHeight() - frameResizingLen);
            rightDrag.setBounds(this.width - 5, DragLabel.getDefaultHeight(),
                    5 - frameResizingLen, this.height - DragLabel.getDefaultHeight() - 2);
            bottomDrag.setBounds(frameResizingLen, this.height - 5, this.width - 4, 5 - frameResizingLen);

            topDrag.setxOffset(frameResizingLen);
            topDrag.setyOffset(frameResizingLen);

            leftDrag.setxOffset(frameResizingLen);
            leftDrag.setyOffset(DragLabel.getDefaultHeight());

            rightDrag.setxOffset(this.width - 5);
            rightDrag.setyOffset(DragLabel.getDefaultHeight());

            bottomDrag.setxOffset(frameResizingLen);
            bottomDrag.setyOffset(this.height - 5);
        }

        if (getCurrentNotification() != null)
            switch (getCurrentNotification().getArrow()) {
                case TOP:
                    currentNotification.setLocation(getWidth() / 2 - currentNotification.getWidth() / 2,
                        currentNotification.getY());
                    break;
                case RIGHT:
                    currentNotification.setLocation(getWidth() - currentNotification.getWidth() + 5,
                        currentNotification.getY());
                    break;
                case LEFT:
                    currentNotification.setLocation(5, currentNotification.getY());
                    break;
                case BOTTOM:
                    currentNotification.setLocation(getWidth() / 2 - currentNotification.getWidth() / 2,
                        currentNotification.getY());
                    break;
            }
    }

    /**
     * The minimum allowable width for a CydreFrame.
     */
    public static final int MINIMUM_WIDTH = 100;

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
        this.minimumSize = minSize;
        cr.setMinimumSize(minimumSize);
    }

    /**
     * Sets the maximum window size if resizing is allowed.
     *
     * @param maxSize the Dimension of the minimum allowed size
     */
    public void setMaximumSize(Dimension maxSize) {
        this.maximumSize = maxSize;
        cr.setMaximumSize(maximumSize);
    }

    /**
     * Sets the snap size for the window if resizing is allowed.
     *
     * @param snap the dimension of the snap size
     */
    public void setSnapSize(Dimension snap) {
        this.snapSize = snap;
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
    private ComponentResizer cr;

    /**
     * Whether to allow background resizing on CyderFrame resize events.
     *
     * @param allowed whether to allow background resizing on CyderFrame resize events
     */
    public void setBackgroundResizing(Boolean allowed) {
        cr.enableBackgroundResize(allowed);
    }

    /**
     * Sets up necessary objects needed to allow the frame to be
     * resizable such as registering the min/max sizes.
     */
    public void initializeResizing() {
        if (cr != null)
            cr.deregisterComponent(this);

        cr = new ComponentResizer();
        cr.registerComponent(this);
        cr.setResizing(true);
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
        cr.setResizing(allow);
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
            cr.setResizing(allow);
    }

    /**
     * Refresh the background in the event of a frame size change or a background image change.
     */
    public void refreshBackground() {
        try {
            if (iconLabel == null)
                return;

            iconLabel.setBounds(frameResizingLen,frameResizingLen,width - 2 * frameResizingLen,
                    height - 2 * frameResizingLen);
            iconPane.setBounds(frameResizingLen,frameResizingLen, width - 2 * frameResizingLen,
                    height - 2 * frameResizingLen);

            if (cr != null && cr.getBackgroundRefreshOnResize()) {
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
        this.backgroundColor = background;
        revalidate();
    }

    /**
     * Returns the background color of the contentPane.
     *
     * @return Color the background color of the contentPane
     */
    @Override
    public Color getBackground() {
        return this.backgroundColor;
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
     * method is actomatically called when {@link CyderFrame#dispose()} is invoked.
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
        return this.threadsKilled;
    }

    /**
     * Set the background of {@code this} to the current ConsoleFrame background.
     */
    public void stealConsoleBackground() {
        if (ConsoleFrame.getConsoleFrame().getCurrentBackgroundImageIcon() == null)
            return;

        currentOrigIcon = ConsoleFrame.getConsoleFrame().getCurrentBackgroundImageIcon();

        iconLabel.setIcon(new ImageIcon(currentOrigIcon.getImage()
                .getScaledInstance(this.getWidth(), this.getHeight(), Image.SCALE_DEFAULT)));
    }

    /**
     * Returns the restore x value.
     *
     * @return the restore x value
     */
    public int getRestoreX() {
        return this.restoreX;
    }

    /**
     * Returns the restore y value.
     *
     * @return the restore y value
     */
    public int getRestoreY() {
        return this.restoreY;
    }

    /**
     * Sets the restore x value.
     *
     * @param x the restore x value
     */
    public void setRestoreX(int x) {
        this.restoreX = x;
    }

    /**
     * Sets the restore y value.
     *
     * @param y the restore y value
     */
    public void setRestoreY(int y) {
        this.restoreY = y;
    }

    /**
     * Whether dragging is permitted for this frame.
     *
     * @return whether dragging is permitted for this frame
     */
    public boolean draggingEnabled() {
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
        getTopDragLabel().enableDragging();
        getBottomDragLabel().enableDragging();
        getRightDragLabel().enableDragging();
        getLeftDragLabel().enableDragging();
    }

    /**
     * Repaints the frame, associated shape, and objects using
     * the {@link CyderColors#guiThemeColor} attribute.
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
                if (ConsoleFrame.getConsoleFrame().getUUID() != null) {
                    if (UserUtil.extractUser().getRoundedwindows().equals("1")) {
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
                CyderColors.guiThemeColor, 5 - frameResizingLen, false));

        //update drag labels
        topDrag.setBackground(CyderColors.guiThemeColor);
        bottomDrag.setBackground(CyderColors.guiThemeColor);
        leftDrag.setBackground(CyderColors.guiThemeColor);
        rightDrag.setBackground(CyderColors.guiThemeColor);
        topDragCover.setBackground(CyderColors.guiThemeColor);
        bottomDragCover.setBackground(CyderColors.guiThemeColor);
        leftDragCover.setBackground(CyderColors.guiThemeColor);
        rightDragCover.setBackground(CyderColors.guiThemeColor);

        //repaint drag labels
        topDrag.repaint();
        leftDrag.repaint();
        bottomDrag.repaint();
        rightDrag.repaint();

        //update content panes
        getContentPane().repaint();
        getTrueContentPane().repaint();

        //finally super call
        super.repaint();
    }

    /**
     * Actions to be invoked before dispose is invoked.
     */
    private LinkedList<PreCloseAction> preCloseActions = new LinkedList<>();

    /**
     * Actions to be invoked after dispose is invoked.
     */
    private LinkedList<PostCloseAction> postCloseActions = new LinkedList<>();

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
    public void addPreCloseAction(PreCloseAction action) {
        preCloseActions.add(action);
    }

    /**
     * Performs the given action right after closing the frame. This action is invoked right after an animation
     * and sequential dispose call.
     *
     * @param action the action to perform before closing/disposing
     */
    public void addPostCloseAction(PostCloseAction action) {
        postCloseActions.add(action);
    }

    /**
     * An action to invoke before a dispose call.
     */
    public interface PreCloseAction {
        void invokeAction();
    }

    /**
     * An action to invoke after a dispose call.
     */
    public interface PostCloseAction {
        void invokeAction();
    }

    /**
     * A message to display before the frame is actually disposed.
     */
    private String closingConfirmationMessage = null;

    /**
     * Displays a confirmation dialog to the user to confirm
     * whether they intended to exit the frame.
     *
     * @param message the message to display to the user
     */
    public void setClosingConfirmation(String message) {
       this.closingConfirmationMessage = message;
    }

    /**
     * Removes any closing confirmation messages set.
     */
    public void removeClosingConfirmation() {
        this.closingConfirmationMessage = null;
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
        this.pinned = pinWindow;
        setAlwaysOnTop(this.pinned);
    }

    /**
     * Returns whether the frame is pinned.
     *
     * @return whether the frame is pinned
     */
    public boolean getPinned() {
        return this.pinned;
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
    private int relativeX = 0;

    /**
     * The relative y value of this frame to the console frame, used for console pin dragging actions.
     */
    private int relativeY = 0;

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
    public static final ArrayList<Color> TASKBAR_BORDER_COLORS = new ArrayList<>() {{
        add(new Color(22,124,237));
        add(new Color(254,49,93));
        add(new Color(249,122,18));
    }};

    /**
     * The index which determines which color to choose for the border color.
     */
    private static int colorIndex = 0;

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
     * @return whether to use the default taskbar component or the custom onec
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

        ConsoleFrame.getConsoleFrame().revalidateMenu();
    }

    /**
     * Constructs the custom taskbar icon based on the currently set custom taskbar imageicon.
     *
     * @return the custom taskbar icon based on the currently set custom taskbar imageicon
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

        customLabel.setToolTipText(this.getTitle());
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
     * Returns the color to be associated with this CydrFrame's taskbar border color.
     *
     * @return the color to be associated with this CydrFrame's taskbar border color
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
     * Returns a compact taskbar component for this cyderframe instance.
     *
     * @return a compact taskbar component for this cyderframe instance
     */
    public JLabel getComapctTaskbarButton() {
       return generateDefaultCompactTaskbarComponent(this.getTitle(), () -> {
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
        if (this.getTitle() == null || this.getTitle().length() == 0)
            throw new IllegalArgumentException("Title not set or long enough");

        return getTaskbarButton(taskbarIconBorderColor);
    }

    /**
     * Returns taskbar component with the specifiecd border
     * color which minimizes the frame upon click actions.
     *
     * @param borderColor the color of the taskbar border
     * @return a taskbar component with the specified border color
     */
    public JLabel getTaskbarButton(Color borderColor) {
        return generateDefaultTaskbarComponent(this.getTitle(), () -> {
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
    public static JLabel generateDefaultCompactTaskbarComponent(String title, ClickAction clickAction) {
        JLabel ret = new JLabel(title.substring(0, Math.min(MAX_COMPACT_MENU_CHARS, title.length())));
        ret.setForeground(CyderColors.vanila);
        ret.setFont(CyderFonts.defaultFontSmall);
        ret.setVerticalAlignment(JLabel.CENTER);
        ret.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                clickAction.fire();
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
    public static JLabel generateDefaultTaskbarComponent(String title, ClickAction clickAction, Color borderColor) {
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

        g.setColor(CyderColors.vanila);

        Font labelFont = new Font("Agency FB",Font.BOLD, 28);
        g.setFont(labelFont);
        g.setColor(CyderColors.vanila);

        String iconTitle = title.substring(0, Math.min(4, title.length())).trim();
        FontMetrics fm = g.getFontMetrics();
        int x = (taskbarIconLength - fm.stringWidth(iconTitle)) / 2;
        int y = (fm.getAscent() + (taskbarIconLength - (fm.getAscent() + fm.getDescent())) / 2);
        g.drawString(iconTitle, x, y);

        //draw darker image

        BufferedImage darkerBufferdImage = new BufferedImage(taskbarIconLength, taskbarIconLength, BufferedImage.TYPE_INT_RGB);
        Graphics g2 = darkerBufferdImage.getGraphics();

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

        FontMetrics fm2 = g2.getFontMetrics();
        int x2 = (taskbarIconLength - fm2.stringWidth(iconTitle)) / 2;
        int y2 = (fm.getAscent() + (taskbarIconLength - (fm2.getAscent() + fm2.getDescent())) / 2);
        g2.drawString(iconTitle, x2, y2);

        ret.setToolTipText(title);
        ret.setIcon(new ImageIcon(bufferedImage));
        ret.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                clickAction.fire();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                ret.setIcon(new ImageIcon(darkerBufferdImage));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ret.setIcon(new ImageIcon(bufferedImage));
            }
        });

        return ret;
    }

    /**
     * Generates a default taskbar component for this frame based on its current title.
     *
     * @param title the title of the component
     * @param clickAction the action to invoke when the icon is clicked
     * @return the taskbar component
     */
    public static JLabel generateDefaultTaskbarComponent(String title, ClickAction clickAction) {
        return generateDefaultTaskbarComponent(title, clickAction, CyderColors.taskbarDefaultColor);
    }

    /**
     * An action performed when this frame is clicked in ConsoleFrame's taskbar.
     */
    public interface ClickAction {
        void fire();
    }

    /**
     * Sets the frame's visibility attrbite and adds the frame to the ConsoleFrame taskbar list.
     *
     * @param b whether to set the frame to be visible
     */
    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);

        //add to console frame's taskbar as long as it's not an exception
        if (b && !ConsoleFrame.getConsoleFrame().isClosed() && this != ConsoleFrame.getConsoleFrame().getConsoleCyderFrame()) {
            ConsoleFrame.getConsoleFrame().addTaskbarIcon(this);
        }

        //if the console is set to always on top, then we need this frame to be automatically set on top as well
        // so that new frames are not behind the console
        if (b && ConsoleFrame.getConsoleFrame().getConsoleCyderFrame() != null &&
                ConsoleFrame.getConsoleFrame().getConsoleCyderFrame().isAlwaysOnTop()) {
            this.setAlwaysOnTop(true);

            if (topDrag != null)
                this.topDrag.refreshPinButton();
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
     * The iamge to display at the center of the debug lines.
     */
    private JLabel debugImageLabel;

    /**
     * Sets whether or not debug lines should be drawn for this frame.
     *
     * @param b whether or not debug lines should be drawn for this frame
     */
    public void drawDebugLines(boolean b) {
        this.drawDebugLines = b;

        if (b) {
            Color lineColor = ColorUtil.getOppositeColor(this.backgroundColor);

            if (this.background != null) {
                lineColor = ColorUtil.getDominantColorOpposite(this.background);
            }

            ImageIcon neffex = new ImageIcon("static/pictures/print/neffex.png");
            debugImageLabel = new JLabel();
            debugImageLabel.setIcon(neffex);
            debugImageLabel.setBounds(
                    getWidth() / 2 - neffex.getIconWidth() / 2,
                    getHeight() / 2 - neffex.getIconHeight() / 2,
                    neffex.getIconWidth(), neffex.getIconHeight());
            this.add(debugImageLabel);

            debugXLabel = new JLabel();
            debugXLabel.setBounds(getWidth() / 2 - 2, 0, 4, getHeight());
            debugXLabel.setOpaque(true);
            debugXLabel.setBackground(lineColor);
            this.add(debugXLabel);

            debugYLabel = new JLabel();
            debugYLabel.setBounds(0, getHeight() / 2 - 2, getWidth(), 4);
            debugYLabel.setOpaque(true);
            debugYLabel.setBackground(lineColor);
            this.add(debugYLabel);
        } else {
            this.remove(debugXLabel);
            this.remove(debugYLabel);
            this.remove(debugImageLabel);
        }

        this.revalidate();
        this.repaint();
    }

    /**
     * Returns whether or not debug lines should be drawn for this frame.
     *
     * @return whether or not debug lines should be drawn for this frame
     */
    public boolean isDrawDebugLines() {
        return this.drawDebugLines;
    }

    /**
     * Returns the integer ID of the monitor this frame is on.
     *
     * @return the integer ID of the monitor this frame is on
     */
    public int getMonitor() {
        return Integer.parseInt(this.getGraphicsConfiguration().getDevice()
                .getIDstring().replaceAll("[^0-9]",""));
    }

    /**
     * Returns the bounds of the monitor this frame is on.
     *
     * @return the bounds of the monitor this frame is on
     */
    public Rectangle getMonitorBounds() {
        return this.getGraphicsConfiguration().getDevice().getDefaultConfiguration().getBounds();
    }

    /**
     * Returns the center point of this frame.
     *
     * @return the center point of this frame
     */
    public Point getCenterPoint() {
        if (this == null)
            throw new IllegalStateException("This frame is null");

        int centerX = this.getX() + (this.getWidth() / 2);
        int centerY = this.getY() + (this.getHeight() / 2);

        return new Point(centerX, centerY);
    }

    // -----------
    // chams mode
    // -----------

    private boolean chameleonActive = false;

    /**
     * Sets the background of the console frame to whatever is behind it.
     */
    public void toggleChameleon() {
        try {
            if (chameleonActive) {
                setBackground(new Color(0,0,0,0));
                revalidate();
                repaint();
            }

            chameleonActive = !chameleonActive;
            revalidate();
            repaint();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }
}