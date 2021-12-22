package cyder.ui;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.consts.CyderImages;
import cyder.enums.Direction;
import cyder.enums.NotificationDirection;
import cyder.handlers.internal.ErrorHandler;
import cyder.handlers.internal.SessionHandler;
import cyder.utilities.*;
import cyder.handlers.internal.PopupHandler;
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
import java.util.LinkedList;

public class CyderFrame extends JFrame {

    public enum TitlePosition {
        LEFT,
        CENTER,
        RIGHT,
    }

    public enum ButtonPosition {
        LEFT,
        RIGHT,
    }

    public enum FrameType {
        DEFAULT,
        INPUT_GETTER,
        POPUP,
    }

    private FrameType frameType = FrameType.DEFAULT;

    private TitlePosition titlePosition = TitlePosition.LEFT;
    private ButtonPosition buttonPosition = ButtonPosition.RIGHT;
    private int width = 1;
    private int height = 1;

    //threads belonging to this instance: notification queuer and dance thread may be ctrl + c'd away
    private boolean threadsKilled;

    private ImageIcon background;

    private DragLabel topDrag;
    private DragLabel bottomDrag;
    private DragLabel leftDrag;
    private DragLabel rightDrag;

    private int restoreX = Integer.MAX_VALUE;
    private int restoreY = Integer.MIN_VALUE;

    private JLabel titleLabel;
    private JLabel iconLabel;
    private JLayeredPane contentLabel;
    private JLayeredPane iconPane;

    //upon disposing this will be set to true so the inner content pane is not repainted to speed up any animations
    private boolean disableContentRepainting = false;

    private Color backgroundColor = CyderColors.navy;

    private LinkedList<WaitingNotification> notificationList = new LinkedList<>();

    private String title = "";

    //determines area of resizing trigger vs dragging
    private static int frameResizingLen = 2;

    /**
     * returns an instance of a cyderframe which extends JFrame with the specified width and height
     * and a drag label with minimize and close buttons
     * the specified ImageIcon is used for the background (you can enable resizing and rescaling of the image should you choose)
     *
     * @param width the specified width of the cyder frame
     * @param height the specified height of the cyder frame
     * @param background the specified background image. You can choose to leave the image in the same place upon
     *                   frame resizing events or you can configure the frame instance to rescale the original background
     *                   image to fit to the new frame dimensions.
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
        setIconImage(SystemUtil.getCyderIcon().getImage());

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

        contentLabel.setBorder(new LineBorder(CyderColors.guiThemeColor, 3, false));
        setContentPane(contentLabel);

        //top frame drag
        topDrag = new DragLabel(width - 2 * frameResizingLen, DragLabel.getDefaultHeight() - 2, this);
        topDrag.setBounds(frameResizingLen, frameResizingLen, width - 2 * frameResizingLen, DragLabel.getDefaultHeight() - 2);
        topDrag.setxOffset(frameResizingLen);
        topDrag.setyOffset(frameResizingLen);
        contentLabel.add(topDrag, JLayeredPane.DRAG_LAYER);
        topDrag.setFocusable(false);

        //left frame drag
        leftDrag = new DragLabel(5 - frameResizingLen, height - frameResizingLen - DragLabel.getDefaultHeight(), this);
        leftDrag.setBounds(frameResizingLen, DragLabel.getDefaultHeight(), 5 - frameResizingLen, height - DragLabel.getDefaultHeight() - frameResizingLen);
        leftDrag.setxOffset(frameResizingLen);
        leftDrag.setyOffset(DragLabel.getDefaultHeight());
        contentLabel.add(leftDrag, JLayeredPane.DRAG_LAYER);
        leftDrag.setFocusable(false);
        leftDrag.setButtonsList(null);

        //right frame drag
        rightDrag = new DragLabel(5 - frameResizingLen, height - frameResizingLen - DragLabel.getDefaultHeight(), this);
        rightDrag.setBounds(width - 5, DragLabel.getDefaultHeight(), 5 - frameResizingLen, height - DragLabel.getDefaultHeight() - frameResizingLen);
        rightDrag.setxOffset(width - 5);
        rightDrag.setyOffset(DragLabel.getDefaultHeight());
        contentLabel.add(rightDrag, JLayeredPane.DRAG_LAYER);
        rightDrag.setFocusable(false);
        rightDrag.setButtonsList(null);

        //bottom frame drag
        bottomDrag = new DragLabel(width - 2 * frameResizingLen, 5 - frameResizingLen, this);
        bottomDrag.setBounds(frameResizingLen, height - 5, width - 4, 5 - frameResizingLen);
        bottomDrag.setxOffset(frameResizingLen);
        bottomDrag.setyOffset(height - 5);
        contentLabel.add(bottomDrag, JLayeredPane.DRAG_LAYER);
        bottomDrag.setFocusable(false);
        bottomDrag.setButtonsList(null);

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

    //bordreless frame type
    private CyderFrame(String borderlessID, int width, int height) {
        if (!borderlessID.equals("BORDERLESS"))
            throw new IllegalArgumentException("Incorrect ID");

        this.width = width;
        this.height = height;

        //this . methods
        setSize(new Dimension(width, height));
        setResizable(false);
        setUndecorated(true);
        setBackground(CyderColors.navy);
        setIconImage(SystemUtil.getCyderIcon().getImage());

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

    @Override
    public Container getContentPane() {
        return iconLabel;
    }

    public Container getIconPane() {
        return iconPane;
    }

    public Container getTrueContentPane() {
        return contentLabel;
    }

    /**
     * returns an instance of a cyderframe which extends JFrame with the specified width and height
     * and a drag label with minimize and close buttons
     *
     * @param width the specified width of the cyder frame
     * @param height the specified height of the cyder frame
     */
    public CyderFrame(int width, int height) {
        this(width, height, CyderImages.defaultBackground);
    }

    public CyderFrame(int width, int height, Color c) {
        this(width, height, ImageUtil.imageIconFromColor(c, width, height));
    }


    /**
     * This method will change the title position to the specified value. If the frame is visible to the user,
     * we will animate the change via a smooth slide transition
     *
     * @param titlePosition the position for the title to be: left, center
     */
    public void setTitlePosition(TitlePosition titlePosition) {
        this.control_c_threads = false;

        if (titlePosition == null || this.titlePosition == null)
            return;

        TitlePosition oldPosition = this.titlePosition;
        long timeout = 2;

        if (isVisible()) {
            if (titlePosition == CyderFrame.TitlePosition.LEFT) {
                new Thread(() -> {
                    //left
                    for (int i = titleLabel.getX() ; i > 4; i--) {
                        if (this.control_c_threads)
                            break;

                        titleLabel.setLocation(i, 2);

                        try {
                            Thread.sleep(timeout);
                        } catch (Exception e) {
                            ErrorHandler.handle(e);
                        }
                    }
                    titleLabel.setLocation(4, 2);
                    this.titlePosition = TitlePosition.LEFT;
                },"title position animater").start();
            } else if (titlePosition == TitlePosition.CENTER){
                new Thread(() -> {
                    switch (oldPosition) {
                        case RIGHT:
                            for (int i = titleLabel.getX() ; i > (getTopDragLabel().getWidth() / 2) - (getMinWidth(this.title) / 2); i--) {
                                if (this.control_c_threads)
                                    break;

                                titleLabel.setLocation(i, 2);

                                try {
                                    Thread.sleep(timeout);
                                } catch (Exception e) {
                                    ErrorHandler.handle(e);
                                }
                            }
                            break;
                        case LEFT:
                            for (int i = titleLabel.getX(); i < (getTopDragLabel().getWidth() / 2) - (getMinWidth(this.title) / 2); i++) {
                                if (this.control_c_threads)
                                    break;

                                titleLabel.setLocation(i, 2);

                                try {
                                    Thread.sleep(timeout);
                                } catch (Exception e) {
                                    ErrorHandler.handle(e);
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
                    for (int i = titleLabel.getX() ; i < this.width -getMinWidth(this.title) - 8; i++) {
                        if (this.control_c_threads)
                            break;

                        titleLabel.setLocation(i, 2);

                        try {
                            Thread.sleep(timeout);
                        } catch (Exception e) {
                            ErrorHandler.handle(e);
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
     * Getter for the title position
     * @return position representing the title position
     */
    public TitlePosition getTitlePosition() {
        return this.titlePosition;
    }

    public ButtonPosition getButtonPosition() {
        return this.buttonPosition;
    }

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

    public FrameType getFrameType() {
        return frameType;
    }

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

    private boolean paintWindowTitle = true;

    /**
     * Determines whether or not to paint the default windows title. The CyderFrame label title is always painted.
     * @param enable boolean variable of your chosen value for paintWindowTitle
     */
    public void paintWindowTitle(boolean enable) {
        paintWindowTitle = enable;
    }

    /**
     * Returns the value of paintWindowTitle which determines whether ot not the windows title is painted.
     * @return boolean describing the value of paintWindowTitle
     */
    public boolean getPaintWindowTitle() {
        return paintWindowTitle;
    }

    private boolean paintSuperTitle = true;

    /**
     * Determines whether or not to paint the windowed title. The CyderFrame label title is always painted.
     * @param enable boolean variable of your chosen value for paintSuperTitle
     */
    public void paintSuperTitle(boolean enable) {
        paintSuperTitle = enable;
    }

    /**
     * Returns the value of paintSuperTitle which determines whether ot not the windowed title is painted.
     * @return boolean describing the value of paintSuperTitle
     */
    public boolean getPaintSuperTitle() {
        return paintSuperTitle;
    }

    /**
     * Set the title of the label painted on the drag label of the CyderFrame instance. You can also configure the instance
     * to paint the windowed title as well.
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

    /**
     * Returns the minimum width required for the given String using notificationFont.
     * @param title the text you want to determine the width of
     * @return an interger value determining the minimum width of a string of text (10 is added to avoid ... bug)
     */
    private int getMinWidth(String title) {
        Font notificationFont = titleLabel.getFont();
        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform, true, true);
        return (int) notificationFont.getStringBounds(title, frc).getWidth() + 10;
    }

    /**
     * Returns the minimum width required for the given String using the given font.
     * @param title the text you want to determine the width of
     * @param f the font for the text
     * @return an interger value determining the minimum width of a string of text (10 is added to avoid ... bug)
     */
    public static int getMinWidth(String title, Font f) {
        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform, true, true);
        return (int) f.getStringBounds(title, frc).getWidth() + 10;
    }

    /**
     * Returns the minimum width required for the given String using the given font without adding 10 to the result.
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
     * @param title the text you want to determine the height of
     * @return an interger value determining the minimum height of a string of text (10 is added to avoid ... bug)
     */
    public static int getMinHeight(String title, Font f) {
        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform, true, true);
        return (int) f.getStringBounds(title, frc).getHeight() + 10;
    }

    /**
     * Returns the minimum height required for the given String using the given font without adding 10.
     * @param title the text you want to determine the height of
     * @return an interger value determining the minimum height of a string of text
     */
    public static int getAbsoluteMinHeight(String title, Font f) {
        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform, true, true);
        return (int) f.getStringBounds(title, frc).getHeight();
    }

    /**
     * This method is to be used for a quick notify. view direction is five seconds
     * @param htmlText the text you want to notify on the callilng from
     */
    public void notify(String htmlText) {
        notify(htmlText, 5000, NotificationDirection.TOP,null);
    }

    /**
     * This method is to be used for a more controled notify. You may choose the duration and the arrow direction
     * @param htmltext the text you want to display (may include HTML tags)
     * @param viewDuration time in ms that the notification should stay on screen
     * @param direction the enter and vanish direction for the notification
     */
    public void notify(String htmltext, int viewDuration, NotificationDirection direction) {
        notify(htmltext, viewDuration, direction, null);
    }

    /**
     * This method is to be used for a more controled notify. You may choose the duration and the arrow direction
     * @param htmltext the text you want to display (may include HTML tags)
     * @param viewDuration time in ms that the notification should stay on screen
     * @param direction the enter and vanish direction for the notification
     * @param onKillAction the action to invoke upon user disposal of the notification
     */
    public void notify(String htmltext, int viewDuration, NotificationDirection direction, ClickAction onKillAction) {
        Direction arrowDir;

        switch (direction) {
            case TOP_LEFT:
            case CENTER_LEFT:
            case BOTTOM_LEFT:
                arrowDir = Direction.LEFT;
                break;
            case TOP_RIGHT:
            case CENTER_RIGHT:
            case BOTTOM_RIGHT:
                arrowDir = Direction.RIGHT;
                break;
            case BOTTOM:
                arrowDir =Direction.BOTTOM;
                break;
            default: //top
                arrowDir = Direction.TOP;
        }

        notify(htmltext, viewDuration, arrowDir, direction, onKillAction);
    }

    private Notification currentNotification;
    private WaitingNotification currentWaitingNotification;
    private boolean notificationCheckerStarted = false;

    public Notification getCurrentNotification() {
        return currentNotification;
    }

    /**
     * Full control over the notification function of a {@link CyderFrame}.
     * See {@link CyderFrame#notify(String, int, NotificationDirection, ClickAction)} for a simpler notify function
     *
     * @param htmltext the text you want to display (may include HTML tags)
     * @param viewDuration the time in ms the notification should be visible for. Pass in 0
     *                     to be auto calculated based on word count
     * @param arrowDir the direction of the arrow on the notification
     * @param notificationDirection the enter/exit direction of the notification
     */
    public void notify(String htmltext, int viewDuration, Direction arrowDir, NotificationDirection notificationDirection, ClickAction onKillAction) {
        notify(htmltext, viewDuration, arrowDir, notificationDirection, onKillAction, null);
    }

    /**
     * Full control over the notification function of a {@link CyderFrame}.
     * See {@link CyderFrame#notify(String, int, NotificationDirection, ClickAction)} for a simpler notify function
     *
     * @param htmltext the text you want to display (may include HTML tags)
     * @param viewDuration the time in ms the notification should be visible for. Pass in 0
     *        to be auto calculated based on word count, pass in -1 to persist indefinitely until the user clicks
     * @param arrowDir the direction of the arrow on the notification
     * @param notificationDirection the enter/exit direction of the notification
     * @param container container to show on the notification instead of the typical text, pass null to ignore
     */
    public void notify(String htmltext, int viewDuration, Direction arrowDir,
                       NotificationDirection notificationDirection, ClickAction onKillAction, Container container) {
        notify(htmltext, viewDuration, arrowDir, notificationDirection, onKillAction, container, null);
    }

    /**
     * Full control over the notification function of a {@link CyderFrame}.
     * See {@link CyderFrame#notify(String, int, NotificationDirection, ClickAction)} for a simpler notify function
     *
     * @param htmltext the text you want to display (may include HTML tags)
     * @param viewDuration the time in ms the notification should be visible for. Pass in 0
     *        to be auto calculated based on word count, pass in -1 to persist indefinitely until the user clicks
     * @param arrowDir the direction of the arrow on the notification
     * @param notificationDirection the enter/exit direction of the notification
     * @param container container to show on the notification instead of the typical text, pass null to ignore
     * @param notificationBackground the color to set the notification background to. Pass null for default
     */
    public void notify(String htmltext, int viewDuration, Direction arrowDir,
                       NotificationDirection notificationDirection, ClickAction onKillAction,
                       Container container, Color notificationBackground) {
        //make a WaitingNotification and add to queue, queue will automatically process any notifications so no further actions needed
        notificationList.add(new WaitingNotification(htmltext, viewDuration, arrowDir, notificationDirection, onKillAction));

        if (!notificationCheckerStarted) {
            notificationCheckerStarted = true;

            new Thread(() -> {
                try {
                    while (this != null && !threadsKilled) {
                        if (notificationList.size() > 0) {
                            WaitingNotification currentWaitingNotification = notificationList.poll();
                            this.currentWaitingNotification = currentWaitingNotification;

                            //init notification object
                            currentNotification = new Notification();

                            if (notificationBackground != null)
                                currentNotification.setBackgroundColor(notificationBackground);

                            //set the arrow direction
                            currentNotification.setArrow(currentWaitingNotification.getArrowDir());

                            //create text label to go on top of notification label
                            JLabel text = new JLabel();
                            text.setText(currentWaitingNotification.getHtmlText());

                            Font notificationFont = new Font("Segoe UI Black", Font.BOLD, 20);

                            BoundsUtil.BoundsString bs = BoundsUtil.widthHeightCalculation(text.getText(),
                                    (int) (this.width * 0.8), notificationFont);

                            int w = bs.getWidth();
                            int h = bs.getHeight();
                            text.setText(bs.getText());

                            if (h > this.height) {
                                this.inform(text.getText(),"Notification");
                                continue;
                            }

                            if (container == null) {
                                //set the text bounds to the proper x,y and theest
                                // calculated width and height
                                text.setBounds(currentNotification.getTextXOffset(), currentNotification.getTextYOffset(), w, h);

                                currentNotification.setWidth(w);
                                currentNotification.setHeight(h);

                                text.setFont(notificationFont);
                                text.setForeground(CyderColors.tooltipForegroundColor);
                                currentNotification.add(text);

                                JLabel disposeLabel = new JLabel();
                                disposeLabel.setBounds(currentNotification.getTextXOffset(), currentNotification.getTextYOffset(), w, h);
                                disposeLabel.setToolTipText(TimeUtil.logTime());
                                disposeLabel.addMouseListener(new MouseAdapter() {
                                    @Override
                                    public void mouseClicked(MouseEvent e) {
                                        //fire any on kill actions if it's not null
                                        if (currentWaitingNotification.getOnKillAction() != null)
                                            currentWaitingNotification.getOnKillAction().fire();

                                        //smoothly animate notification away
                                        currentNotification.vanish(currentWaitingNotification.getNotificationDirection(), getContentPane(), 0);
                                    }
                                });
                                currentNotification.add(disposeLabel);
                            } else {
                                currentNotification.setWidth(container.getWidth());
                                currentNotification.setHeight(container.getHeight());
                                currentNotification.add(container);
                            }

                            switch (currentWaitingNotification.getNotificationDirection()) {
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
                                case CENTER_LEFT:
                                    currentNotification.setLocation(-currentNotification.getWidth() + 5,
                                        getContentPane().getHeight() / 2 - (h / 2) - currentNotification.getTextYOffset());
                                    break;
                                case CENTER_RIGHT:
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
                            SessionHandler.log(SessionHandler.Tag.ACTION, "[" +
                                    this.getTitle() + "] [NOTIFICATION] " + currentWaitingNotification.getHtmlText());

                            //duration is always 300ms per word unless less than 5 seconds
                            int duration = 300 * StringUtil.countWords(
                                    Jsoup.clean(bs.getText(), Safelist.none())
                            );
                            duration = Math.max(duration, 5000);
                            duration = currentWaitingNotification.getDuration() == 0 ?
                                    duration : currentWaitingNotification.getDuration();
                            currentNotification.appear(currentWaitingNotification.getNotificationDirection(), getContentPane(), duration);

                            while (getCurrentNotification().isVisible())
                                Thread.onSpinWait();
                        } else {
                            notificationCheckerStarted = false;
                            break;
                        }
                    }
                } catch (Exception e) {
                    ErrorHandler.handle(e);
                }
            }, this + " notification queue checker").start();
        }
    }

    /**
     * Ends the current notification on screen. If more are behind it, the queue will immediately pull and display.
     */
    public void revokeCurrentNotification() {
        revokeCurrentNotification(false);
    }

    public void revokeCurrentNotification(boolean animate) {
        if (animate) {
            currentNotification.vanish(currentWaitingNotification.getNotificationDirection(), this, 0);
        } else {
            currentNotification.kill();
        }
    }

    /**
     * Removes all currently displayed notifications and wipes the notification queue.
     */
    public void revokeAllNotifications() {
        currentNotification.kill();
        notificationList.clear();
        notificationCheckerStarted = false;
    }

    /**
     * Getter for the top drag label associated with this CyderFrame instance. Used for frame resizing.
     * @return The associated DragLabel
     */
    public DragLabel getTopDragLabel() {
        return topDrag;
    }

    /**
     * Getter for the bottom drag label associated with this CyderFrame instance. Used for frame resizing.
     * @return The associated DragLabel
     */
    public DragLabel getBottomDragLabel() {
        return bottomDrag;
    }

    /**
     * Getter for the left drag label associated with this CyderFrame instance. Used for frame resizing.
     * @return The associated DragLabel
     */
    public DragLabel getLeftDragLabel() {
        return leftDrag;
    }

    /**
     * Getter for the right drag label associated with this CyderFrame instance. Used for frame resizing.
     * @return The associated DragLabel
     */
    public DragLabel getRightDragLabel() {
        return rightDrag;
    }

    /**
     * Pops open a window relative to this with the provided text
     * @param text  the String you wish to display
     * @param title The title of the CyderFrame which will be opened to display the text
     */
    public void inform(String text, String title) {
        PopupHandler.informRelative(text, title, this);
    }

    //frames for animations such as dispose and minimize
    private static final double animationFrames = 15.0;

    /**
     * Moves the window down until it is off screen before setting the state to ICONIFIED.
     * The original position of the frame will be remembered and set when the window is deiconified.
     */
    public void minimizeAnimation() {
        try {
            //set restore vars here
            setRestoreX(getX());
            setRestoreY(getY());

            if (UserUtil.getUserData("minimizeanimation").equals("1")) {
                //figure out increment for frame num
                int distanceToTravel = SystemUtil.getScreenHeight() - this.getY();
                //25 frames to animate
                int animationInc = (int) ((double ) distanceToTravel / animationFrames);

                for (int i = this.getY(); i <= SystemUtil.getScreenHeight(); i += animationInc) {
                    Thread.sleep(1);
                    setLocation(this.getX(), i);
                }
            }

            setState(JFrame.ICONIFIED);
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    //dispose calls --------------------------------------------------------------------

    //for whatever reason, this should help avoid bug calls in the future,
    // there is no setter since it will be set to true upon dispose() being invoked
    private boolean disposed;

    public boolean isDispoed() {
        return this.disposed;
    }

    /**
     * Same as the regular overridden dispose method but you have the option to not animate the frame
     * and practically dispose it immediately
     * @param fastClose boolean describing whether or not fast close should be invoked
     */
    public void dispose(boolean fastClose) {
        new Thread(() -> {
            try {
                if (this == null)
                    return;

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

                if (!fastClose && UserUtil.getUserData("closeanimation").equals("1")) {
                    //disable dragging
                    disableDragging();

                    //disable content pane REPAINTING not paint to speed up the animation
                    disableContentRepainting = true;

                    if (this != null && isVisible()) {
                        Point point = getLocationOnScreen();
                        int x = (int) point.getX();
                        int y = (int) point.getY();

                        //remove from consoleframe
                        ConsoleFrame.getConsoleFrame().removeTaskbarIcon(this);

                        //figure out increment for frames
                        int distanceToTravel = Math.abs(this.getY()) + Math.abs(this.getHeight());
                        //25 frames to animate
                        int animationInc = (int) ((double) distanceToTravel / animationFrames);

                        for (int i = this.getY(); i >= -this.getHeight() ; i -= animationInc) {
                            Thread.sleep(1);
                            setLocation(this.getX(), i);
                        }
                    }
                }

                super.dispose();
                disposed = true;

                for (PostCloseAction action : postCloseActions)
                    action.invokeAction();
            } catch (Exception e) {
                ErrorHandler.handle(e);
            }
        }, "frame dispose thread").start();
    }

    @Override
    public void dispose() {
        dispose(false);
    }

    //----------------------------------------------------------------------------------

    /**
     * Allow or disable moving the window.
     * @param relocatable the boolean value determining if the window may be repositioned
     */
    public void setRelocatable(boolean relocatable) {
        if (relocatable) {
            this.enableDragging();
        } else {
           this.disableDragging();
        }
    }

    //used for a ctrl + c even to stop dancing or anything else we might want to stop
    private boolean control_c_threads = false;

    public void setControl_c_threads(boolean b) {
        this.control_c_threads = b;
    }

    public boolean getControl_c_threads() {
        return this.control_c_threads;
    }

    //dancing ------------------------------------------------------------------------------

    public enum DancingDirection {
        INITIAL_UP, LEFT, DOWN, RIGHT, UP
    }

    private DancingDirection dancingDirection = DancingDirection.INITIAL_UP;
    private int dancingIncrement = 10;
    private boolean dancingFinished = false;

    public void setDancingDirection(DancingDirection dancingDirection) {
        this.dancingDirection = dancingDirection;
    }

    public boolean isDancingFinished() {
        return dancingFinished;
    }

    public void setDancingFinished(boolean dancingFinished) {
        this.dancingFinished = dancingFinished;
    }

    /**
     * Takes a step in the right direction for the dance routine
     */
    public void danceStep() {
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

                if (this.getY() > SystemUtil.getScreenHeight() - this.getHeight()) {
                    this.setLocation(this.getX(), SystemUtil.getScreenHeight() - this.getHeight());
                    dancingDirection = DancingDirection.RIGHT;
                }
                break;
            case RIGHT:
                this.setLocation(this.getX() + 10, this.getY());

                if (this.getX() > SystemUtil.getScreenWidth() - this.getWidth()) {
                    this.setLocation(SystemUtil.getScreenWidth() - this.getWidth(), this.getY());
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

    // ------------------------------------------------------------------------------

    /**
     * transforms the content pane by an incremental angle of 2 degrees emulating Google's barrel roll easter egg
     */
    public void barrelRoll() {
        ImageIcon masterIcon = (ImageIcon) ((JLabel) getContentPane()).getIcon();
        BufferedImage master = ImageUtil.getBi(masterIcon);

        Timer timer = null;
        Timer finalTimer = timer;
        timer = new Timer(10, new ActionListener() {
            private double angle = 0;
            private double delta = 2.0;

            BufferedImage rotated;

            @Override
            public void actionPerformed(ActionEvent e) {
                angle += delta;
                if (angle > 360) {
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
     * @param degrees the degrees to be rotated by; 360deg = 0deg.
     */
    public void rotateBackground(int degrees) {
        ImageIcon masterIcon = currentOrigIcon;
        BufferedImage master = ImageUtil.getBi(masterIcon);
        BufferedImage rotated = ImageUtil.rotateImageByDegrees(master, degrees);
        ((JLabel) getContentPane()).setIcon(new ImageIcon(rotated));
    }

    /**
     * Repaints the title position and button positions in the correct location.
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
     * Overriden setSize method to ensure the bounds are never less than 100x100
     * @param width width of frame
     * @param height height of frame
     */
    @Override
    public void setSize(int width, int height) {
        width = Math.max(100, width);
        height = Math.max(100, height);
        super.setSize(width, height);
    }

    /**
     * Sets the frame bounds and also changes the underlying drag label's bounds which is why this method is overridden.
     */
    @Override
    public void setBounds(int x, int y, int width, int height) {
        width = Math.max(100, width);
        height = Math.max(100, height);
        super.setBounds(x, y, width, height);

        this.width = width;
        this.height = height;

        if (getTopDragLabel() != null) {
            topDrag.setWidth(this.width - 2 * frameResizingLen);
            topDrag.setHeight(DragLabel.getDefaultHeight() - frameResizingLen);
            leftDrag.setWidth(5 - frameResizingLen);
            leftDrag.setHeight(this.height - DragLabel.getDefaultHeight() - frameResizingLen);
            rightDrag.setWidth(5 - frameResizingLen);
            rightDrag.setHeight(this.height - DragLabel.getDefaultHeight() - frameResizingLen);
            bottomDrag.setWidth(this.width - frameResizingLen * 2);
            bottomDrag.setHeight(5 - frameResizingLen);

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

        if (isVisible())
            refreshBackground();
    }

    private Dimension minimumSize = new Dimension(100, 100);
    private Dimension maximumSize = new Dimension(800, 800);
    private Dimension snapSize = new Dimension(1, 1);

    /**
     * Sets the minimum window size if resizing is allowed.
     * @param minSize the Dimension of the minimum allowed size
     */
    public void setMinimumSize(Dimension minSize) {
        this.minimumSize = minSize;
        cr.setMinimumSize(minimumSize);
    }

    /**
     * Sets the maximum window size if resizing is allowed.
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
     * @return the minimum window size if resizing is allowed
     */
    public Dimension getMinimumSize() {
        return minimumSize;
    }

    /**
     * @return the maximum window size if resizing is allowed
     */
    public Dimension getMaximumSize() {
        return maximumSize;
    }

    /**
     * @return the snap size for the window if resizing is allowed
     */
    public Dimension getSnapSize() {
        return snapSize;
    }

    ComponentResizer cr;

    /**
     * Choose to allow/disable background image reisizing if window resizing is allowed.
     * @param allowed the value determining background resizing
     */
    public void setBackgroundResizing(Boolean allowed) {
        cr.enableBackgroundResize(allowed);
    }

    /**
     * This method should be called first when attempting to allow resizing of a frame.
     * Procedural calls: init component resizer, set resizing to true, set min, max, and snap sizes to default.
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
     * @param allow sets/disables resizing of the frame.
     */
    public void setFrameResizing(boolean allow) {
        cr.setResizing(allow);
    }
    ImageIcon currentOrigIcon;

    @Override
    public void setResizable(boolean allow) {
        if (cr != null)
            cr.setResizing(allow);
    }

    /**
     * Refresh the background in the event of a frame size change or background image change.
     */
    public void refreshBackground() {
        try {
            if (iconLabel == null)
                return;

            if (cr != null && cr.getBackgroundRefreshOnResize()) {
                iconLabel.setIcon(new ImageIcon(currentOrigIcon.getImage()
                        .getScaledInstance(iconLabel.getWidth(), iconLabel.getHeight(), Image.SCALE_DEFAULT)));
            }

            iconLabel.setBounds(frameResizingLen,frameResizingLen,width - 2 * frameResizingLen,height - 2 * frameResizingLen);
            iconPane.setBounds(frameResizingLen,frameResizingLen, width - 2 * frameResizingLen, height - 2 * frameResizingLen);

            revalidate();
            repaint();
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    /**
     * Set the background to a new icon and refresh the frame.
     * @param icon the ImageIcon you want the frame background to be
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
            ErrorHandler.handle(e);
        }
    }

    /**
     * Sets the background color of the Frame's content pane
     * @param background the Color object value of the content pane's desired background
     */
    @Override
    public void setBackground(Color background) {
        super.setBackground(background);
        this.backgroundColor = background;
        revalidate();
    }

    /**
     * Returns the background color of the contentPane.
     * @return Color object of the background color
     */
    @Override
    public Color getBackground() {
        return this.backgroundColor;
    }

    @Override
    public String toString() {
        String title = this.title == null ||
                this.title.length() == 0 ? super.getTitle() : this.title;
        return "Name: " + title + "[" + this.getTitlePosition() + "],(" +
                this.getX() + "," + this.getY() + "," + this.getWidth() + "x" + this.getHeight() + ")";
    }

    /**
     * Kills all threads associated with this CyderFrame instance. An irreversible action. This
     * method is actomatically called when {@link CyderFrame#dispose()} is invokekd.
     */
    public void killThreads() {
        this.control_c_threads = true;
        this.threadsKilled = true;
    }

    public boolean threadsKilled() {
        return this.threadsKilled;
    }

    /**
     * Set the background of {@code this} to the current ConsoleFrame background.
     * Note: the background is not updated when the ConsoleFrame background is, I have plans
     *  to add a method to enable this
     */
    public void stealConsoleBackground() {
        if (ConsoleFrame.getConsoleFrame().getCurrentBackgroundImageIcon() == null)
            return;

        currentOrigIcon = ConsoleFrame.getConsoleFrame().getCurrentBackgroundImageIcon();

        iconLabel.setIcon(new ImageIcon(currentOrigIcon.getImage()
                .getScaledInstance(iconLabel.getWidth(), iconLabel.getHeight(), Image.SCALE_DEFAULT)));
        iconLabel.setBounds(frameResizingLen,frameResizingLen,width - 2 * frameResizingLen,height - 2 * frameResizingLen);
        iconPane.setBounds(frameResizingLen,frameResizingLen, width - 2 * frameResizingLen, height - 2 * frameResizingLen);

    }

    public int getRestoreX() {
        return this.restoreX;
    }

    public int getRestoreY() {
        return this.restoreY;
    }

    public void setRestoreX(int x) {
        this.restoreX = x;
    }

    public void setRestoreY(int y) {
        this.restoreY = y;
    }

    public boolean draggingEnabled() {
        return getTopDragLabel().isDraggingEnabled() &&
                getBottomDragLabel().isDraggingEnabled() &&
                getLeftDragLabel().isDraggingEnabled() &&
                getRightDragLabel().isDraggingEnabled();
    }
    
    public void disableDragging() {
        if (topDrag == null)
            return;

        getTopDragLabel().disableDragging();
        getBottomDragLabel().disableDragging();
        getRightDragLabel().disableDragging();
        getLeftDragLabel().disableDragging();
    }

    public void enableDragging() {
        getTopDragLabel().enableDragging();
        getBottomDragLabel().enableDragging();
        getRightDragLabel().enableDragging();
        getLeftDragLabel().enableDragging();
    }

    @Override
    public void repaint() {
        if (topDrag == null) {
            //update content panes
            getContentPane().repaint();
            getTrueContentPane().repaint();

            //finally super call
            super.repaint();

            return;
        }

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

        //update the border covering the resize area
        contentLabel.setBorder(new LineBorder(
                CyderColors.guiThemeColor, 5 - frameResizingLen, false));

        //update drag labels
        topDrag.setBackground(CyderColors.guiThemeColor);
        bottomDrag.setBackground(CyderColors.guiThemeColor);
        leftDrag.setBackground(CyderColors.guiThemeColor);
        rightDrag.setBackground(CyderColors.guiThemeColor);

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

    private LinkedList<PreCloseAction> preCloseActions = new LinkedList<>();
    private LinkedList<PostCloseAction> postCloseActions = new LinkedList<>();

    public void removePreCloseActions() {
        preCloseActions = new LinkedList<>();
    }

    public void removePostCloseActions() {
        postCloseActions = new LinkedList<>();
    }

    /**
     * Performs the given action right before closing the frame. This action is invoked right before an animation
     * and sequential dispose call.
     * @param action the action to perform before closing/disposing
     */
    public void addPreCloseAction(PreCloseAction action) {
        preCloseActions.add(action);
    }

    /**
     * Performs the given action right after closing the frame. This action is invoked right after an animation
     * and sequential dispose call.
     * @param action the action to perform before closing/disposing
     */
    public void addPostCloseAction(PostCloseAction action) {
        postCloseActions.add(action);
    }

    //interface to be used for preCloseActions
    public interface PreCloseAction {
        void invokeAction();
    }

    //interface to be used for postCloseActions
    public interface PostCloseAction {
        void invokeAction();
    }

    private String closingConfirmationMessage = null;

    /**
     * Displays a confirmation dialog to the user to confirm whether or not they intended to exit the frame
     * @param message the message to display to the user
     */
    public void setClosingConfirmation(String message) {
       this.closingConfirmationMessage = message;
    }

    public void removeClosingConfirmation() {
        this.closingConfirmationMessage = null;
    }

    public void addDragListener(MouseMotionListener actionListener) {
        topDrag.addMouseMotionListener(actionListener);
        bottomDrag.addMouseMotionListener(actionListener);
        leftDrag.addMouseMotionListener(actionListener);
        rightDrag.addMouseMotionListener(actionListener);
    }

    public void addDragMouseListener(MouseListener ml) {
        topDrag.addMouseListener(ml);
        bottomDrag.addMouseListener(ml);
        leftDrag.addMouseListener(ml);
        rightDrag.addMouseListener(ml);
    }

    private boolean pinned;

    /**
     * Sets the value for pinning the frame on top.
     * @param b the value determining whether or not the frame is always on top
     */
    public void setPinned(boolean b) {
        this.pinned = b;
        setAlwaysOnTop(this.pinned);
    }

    /**
     * Standard getter for pinned boolean.
     * @return the boolean of pinned
     */
    public boolean getPinned() {
        return this.pinned;
    }

    //pinned to console, second mode of pinning

    private boolean consolePinned;

    /**
     * Determines if the frame should be pinned to the console.
     * @return boolean describing if the frame should move with the console
     */
    public boolean isConsolePinned() {
        return consolePinned;
    }

    /**
     * Setter for the state of the provided boolean.
     * @param consolePinned whether or not the frame should stick to the console
     */
    public void setConsolePinned(boolean consolePinned) {
        this.consolePinned = consolePinned;
        setAlwaysOnTop(this.consolePinned);
    }

    //relativeX, relativeY are used for frame pinning and dragging on the consoleFrame

    private int relativeX = 0;
    private int relativeY = 0;

    public int getRelativeX() {
        return relativeX;
    }

    public int getRelativeY() {
        return relativeY;
    }

    public void setRelativeX(int relativeX) {
        this.relativeX = relativeX;
    }

    public void setRelativeY(int relativeY) {
        this.relativeY = relativeY;
    }

    //used for frame consolidation points

    private double xPercent = 0;
    private double yPercent = 0;

    public double getxPercent() {
        return xPercent;
    }

    public double getyPercent() {
        return yPercent;
    }

    public void setxPercent(double xPercent) {
        this.xPercent = xPercent;
    }

    public void setyPercent(double yPercent) {
        this.yPercent = yPercent;
    }

    //console menu taskbar logic
    private static Color blueBorderColor = new Color(22,124,237);
    private static Color redBorderColor = new Color(254,49,93);
    private static Color orangeBorderColor = new Color(249,122,18);

    private static int colorIndex = 0;
    private Color taskbarIconBorderColor;

    //initializes this frame instance's border color to be used for drawing/redrawing until object is disposed
    private Color getTaskbarBorderColor() {
        switch (colorIndex) {
            case 0:
                colorIndex++;
                return blueBorderColor;
            case 1:
                colorIndex++;
                return redBorderColor;
            default:
                colorIndex = 0;
                return orangeBorderColor;
        }
    }

    public static void incrementColorIndex() {
        colorIndex++;

        if (colorIndex == 3)
            colorIndex = 0;
    }

    public static final int taskbarIconLength = 75;
    public static final int taskbarBorderLength = 5;

    public JLabel getTaskbarButton() {
        if (this.getTitle() == null || this.getTitle().length() == 0)
            throw new IllegalArgumentException("Title not set or long enough");

        return getTaskbarButton(taskbarIconBorderColor);
    }

    public JLabel getTaskbarButton(Color borderColor) {
        String title = this.getTitle().substring(0, Math.min(4, this.getTitle().length()));

        return generateDefaultTaskbarComponent(title, () -> {
            if (getState() == 0) {
                minimizeAnimation();
            } else {
                setState(Frame.NORMAL);
            }
        }, borderColor);
    }

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

        String iconTitle = title.substring(0, Math.min(4, title.length()));
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

    public static JLabel generateDefaultTaskbarComponent(String title, ClickAction clickAction) {
        return generateDefaultTaskbarComponent(title, clickAction, CyderColors.taskbarDefaultColor);
    }

    //used for icon frame actions in ConsoleFrame
    public interface ClickAction {
        void fire();
    }

    //overridden so we can add to ConsoleFrame's taskbar menu
    @Override
    public void setVisible(boolean b) {
        super.setVisible(b);

        if (b && !ConsoleFrame.getConsoleFrame().isClosed() && this != ConsoleFrame.getConsoleFrame().getConsoleCyderFrame()) {
            ConsoleFrame.getConsoleFrame().addTaskbarIcon(this);
        }
    }

    /**
     * Makes a borderless frame with no drag labels, the content label itself can move the frame.
     * This frame, however, can never exist as any other state,
     */
    public static CyderFrame getBorderlessFrame(int width, int height) {
        return new CyderFrame("BORDERLESS", width, height);
    }

    //inner classes

    private static class WaitingNotification {
        private String htmlText;
        private int duration;
        private Direction arrowDir;
        private NotificationDirection notificationDirection;
        private ClickAction onKillAction;

        /**
         * A notification that hasn't been notified to the user yet and is waiting in a CyderFrame's queue.
         * @param text the html text for the eventual notification to display
         * @param dur the duration in miliseconds the notification should last for. Use 0 for auto-calculation
         * @param arrowDir the arrow direction
         * @param notificationDirection the notification direction
         * @param onKillAction the action to perform if the notification is dismissed by the user
         */
        public WaitingNotification(String text, int dur, Direction arrowDir, NotificationDirection notificationDirection, ClickAction onKillAction) {
            this.htmlText = text;
            this.duration = dur;
            this.arrowDir = arrowDir;
            this.notificationDirection = notificationDirection;
            this.onKillAction = onKillAction;
        }

        public void setHtmlText(String htmlText) {
            this.htmlText = htmlText;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }

        public void setArrowDir(Direction arrowDir) {
            this.arrowDir = arrowDir;
        }

        public void setNotificationDirection(NotificationDirection notificationDirection) {
            this.notificationDirection = notificationDirection;
        }

        public String getHtmlText() {
            return htmlText;
        }

        public int getDuration() {
            return duration;
        }

        public Direction getArrowDir() {
            return arrowDir;
        }

        public NotificationDirection getNotificationDirection() {
            return notificationDirection;
        }

        public ClickAction getOnKillAction() {
            return onKillAction;
        }

        public void setOnKillAction(ClickAction onKillAction) {
            this.onKillAction = onKillAction;
        }

        @Override
        public String toString() {
            return "WaitingNotification object: (" +
                    this.getHtmlText() + "," +
                    this.getDuration() + "," +
                    this.getArrowDir() + "," +
                    this.getNotificationDirection() + "," +
                    "), hash=" + this.hashCode();
        }
    }
}