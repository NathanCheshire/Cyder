package cyder.ui;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.enums.Direction;
import cyder.handler.ErrorHandler;
import cyder.obj.Gluster;
import cyder.utilities.AnimationUtil;
import cyder.utilities.ImageUtil;
import cyder.utilities.StringUtil;
import cyder.utilities.SystemUtil;
import cyder.widgets.GenericInform;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
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

    private TitlePosition titlePosition = TitlePosition.LEFT;
    private ButtonPosition buttonPosition = ButtonPosition.RIGHT;
    private int width;
    private int height;

    private boolean threadsKilled;

    private ImageIcon background;

    private DragLabel topDrag;
    private DragLabel bottomDrag;
    private DragLabel leftDrag;
    private DragLabel rightDrag;

    private int restoreX = Integer.MAX_VALUE;
    private int restoreY = Integer.MIN_VALUE;

    private JLabel titleLabel;
    private JLabel contentLabel;

    private Color backgroundColor = CyderColors.navy;

    private LinkedList<Gluster> notificationList = new LinkedList<>();

    /**
     * returns an instance of a cyderframe which extends JFrame with the specified width and height
     * and a drag label with minimize and close buttons
     * the specified ImageIcon is used for the background (you can enable resizing and rescaling of the image should you choose)
     *
     * @param width      - the specified width of the cyder frame
     * @param height     - the specified height of the cyder frame
     * @param background - the specified background image. You can choose to leave the image in the same place upon
     *                   frame resizing events or you can configure the frame instance to rescale the original background
     *                   image to fit to the new frame dimensions.
     */
    public CyderFrame(int width, int height, ImageIcon background) {
        this.width = width;
        this.height = height;
        this.background = background;
        currentOrigIcon = background;
        setSize(new Dimension(width, height));

        setResizable(false);
        setUndecorated(true);
        setBackground(backgroundColor);
        setIconImage(SystemUtil.getCyderIcon().getImage());

        contentLabel = new JLabel();
        contentLabel.setIcon(background);
        setContentPane(contentLabel);
        contentLabel.setBorder(new LineBorder(CyderColors.navy, 1, false));

        topDrag = new DragLabel(width, DragLabel.getDefaultHeight() - 1, this);
        topDrag.setBounds(0, 1, width, DragLabel.getDefaultHeight() - 1);
        topDrag.setxOffset(0);
        topDrag.setyOffset(1);
        contentLabel.add(topDrag);

        leftDrag = new DragLabel(4, height - DragLabel.getDefaultHeight() - 2, this);
        leftDrag.setBounds(1, DragLabel.getDefaultHeight(), 4, height - DragLabel.getDefaultHeight() - 2);
        leftDrag.setxOffset(1);
        leftDrag.setyOffset(DragLabel.getDefaultHeight());
        contentLabel.add(leftDrag);

        rightDrag = new DragLabel(4, height - DragLabel.getDefaultHeight() - 2, this);
        rightDrag.setBounds(width - 5, DragLabel.getDefaultHeight(), 4, height - DragLabel.getDefaultHeight() - 2);
        rightDrag.setxOffset(width - 5);
        rightDrag.setyOffset(DragLabel.getDefaultHeight());
        contentLabel.add(rightDrag);

        bottomDrag = new DragLabel(width, 4, this);
        bottomDrag.setBounds(0, height - 5, width, 4);
        bottomDrag.setxOffset(0);
        bottomDrag.setyOffset(height - 5);
        contentLabel.add(bottomDrag);

        //todo really we're just back to where we started since with adding the border to make it draggable,
        // we have reintroduced the layering issues

        //todo now just make sure notifications are put over components but not over the drag labels
        //todo override get content pane to return content label and make a getTrueContentPane method?

        titleLabel = new JLabel("");
        titleLabel.setFont(new Font("Agency FB", Font.BOLD, 22));
        titleLabel.setForeground(CyderColors.vanila);

        topDrag.add(titleLabel);

        this.threadsKilled = false;
    }

    /**
     * returns an instance of a cyderframe which extends JFrame with the specified width and height
     * and a drag label with minimize and close buttons
     *
     * @param width  - the specified width of the cyder frame
     * @param height - the specified height of the cyder frame
     */
    public CyderFrame(int width, int height) {
        this(width, height, new ImageIcon(""));
    }

    /**
     * returns an instance of a cyderframe which extends JFrame with
     * a width of 400 and a height of 400 and a drag label with minimize and close buttons
     */
    public CyderFrame() {
        this(400, 400);
    }

    /**
     * This method will change the title position to the specified value. If the frame is visible to the user,
     * we will animate the change via a smooth slide transition
     *
     * @param titlePosition the position for the title to be: left, center
     */
    public void setTitlePosition(TitlePosition titlePosition) {
        if (titlePosition == null || this.titlePosition == null || this.titlePosition == titlePosition)
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
                //left or right since center
                new Thread(() -> {
                    switch (oldPosition) {
                        case RIGHT:
                            for (int i = titleLabel.getX() ; i > (getTopDragLabel().getWidth() / 2) - (getMinWidth(titleLabel.getText()) / 2); i--) {
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
                            for (int i = titleLabel.getX(); i < (getTopDragLabel().getWidth() / 2) - (getMinWidth(titleLabel.getText()) / 2); i++) {
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
                    titleLabel.setLocation((getTopDragLabel().getWidth() / 2) - (getMinWidth(titleLabel.getText()) / 2), 2);
                    this.titlePosition = TitlePosition.CENTER;
                    //set final bounds
                },"title position animater").start();
            } else {
                //right
                new Thread(() -> {
                    for (int i = titleLabel.getX() ; i < this.width - getMinWidth(titleLabel.getText()) - 8; i++) {
                        if (this.control_c_threads)
                            break;

                        titleLabel.setLocation(i, 2);

                        try {
                            Thread.sleep(timeout);
                        } catch (Exception e) {
                            ErrorHandler.handle(e);
                        }
                    }
                    titleLabel.setLocation(this.width - getMinWidth(titleLabel.getText()) + 8, 2);
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
        }
    }

    /**
     * Getter for the title position
     * @return - position representing the title position
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
            titleLabel.setLocation(this.width - getMinWidth(titleLabel.getText()) + 8, 2);
        }
    }

    private boolean paintWindowTitle = true;

    /**
     * Determines whether or not to paint the default windows title. The CyderFrame label title is always painted.
     * @param enable - boolean variable of your chosen value for paintWindowTitle
     */
    public void setPaintWindowTitle(boolean enable) {
        paintWindowTitle = enable;
    }

    /**
     * Returns the value of paintWindowTitle which determines whether ot not the windows title is painted.
     * @return - boolean describing the value of paintWindowTitle
     */
    public boolean getPaintWindowTitle() {
        return paintWindowTitle;
    }

    private boolean paintSuperTitle = true;

    /**
     * Determines whether or not to paint the windowed title. The CyderFrame label title is always painted.
     * @param enable - boolean variable of your chosen value for paintSuperTitle
     */
    public void paintSuperTitle(boolean enable) {
        paintSuperTitle = enable;
    }

    /**
     * Returns the value of paintSuperTitle which determines whether ot not the windowed title is painted.
     * @return - boolean describing the value of paintSuperTitle
     */
    public boolean getPaintSuperTitle() {
        return paintSuperTitle;
    }

    /**
     * Set the title of the label painted on the drag label of the CyderFrame instance. You can also configure the instance
     * to paint the windowed title as well.
     * @param title - the String representing the chosen CyderFrame title
     */
    @Override
    public void setTitle(String title) {
        super.setTitle(paintSuperTitle ? title : "");

        if (paintWindowTitle) {
            titleLabel.setText(title);

            switch (titlePosition) {
                case CENTER:
                    titleLabel.setBounds((getTopDragLabel().getWidth() / 2) - (getMinWidth(title) / 2), 2, getMinWidth(title), 25);
                    break;

                default:
                    titleLabel.setBounds(5, 2, getMinWidth(title), 25);
            }
        }
    }

    /**
     * Returns the minimum width required for the given String using the font of the title label.
     *
     * @param title - the text you want to determine the width of
     * @return - an interger value determining the minimum width of a string of text (10 is added to avoid ... bug)
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
     * @param title - the text you want to determine the width of
     * @return - an interger value determining the minimum width of a string of text (10 is added to avoid ... bug)
     */
    public static int getMinWidth(String title, Font f) {
        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform, true, true);
        return (int) f.getStringBounds(title, frc).getWidth() + 10;
    }

    /**
     * Returns the minimum height required for the given String using the given font.
     *
     * @param title - the text you want to determine the height of
     * @return - an interger value determining the minimum height of a string of text (10 is added to avoid ... bug)
     */
    public static int getMinHeight(String title, Font f) {
        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform, true, true);
        return (int) f.getStringBounds(title, frc).getHeight() + 10;
    }

    /**
     * This method is to be used for a quick notify. view direction is five seconds
     *
     * @param htmlText - the text you want to notify on the callilng from
     */
    public void notify(String htmlText) {
        notify(htmlText, 5000, Direction.TOP);
    }

    /**
     * This method is to be used for a more controled notify. You may choose the duration and the arrow direction
     *
     * @param htmltext     - the text you want to display (may include HTML tags)
     * @param viewDuration - time in ms that the notification should stay on screen
     * @param direction    - the enter and vanish direction for the notification
     */
    public void notify(String htmltext, int viewDuration, Direction direction) {
        Notification frameNotification = new Notification();

        Direction startDir;
        Direction vanishDir;

        switch (direction) {
            case LEFT:
                startDir = Direction.LEFT;
                vanishDir = Direction.LEFT;
                break;
            case RIGHT:
                startDir = Direction.RIGHT;
                vanishDir = Direction.RIGHT;
                break;
            case BOTTOM:
                startDir = Direction.BOTTOM;
                vanishDir = Direction.BOTTOM;
                break;
            default:
                startDir = Direction.TOP;
                vanishDir = Direction.TOP;
                break;
        }

        notify(htmltext, viewDuration, direction, startDir, vanishDir);
    }

    private Notification currentNotification;
    private boolean notificationCheckerStarted = false;

    public Notification getCurrentNotification() {
        return currentNotification;
    }

    /**
     * Full control over the notification function of a {@link CyderFrame}.
     * See {@link CyderFrame#notify(String, int, Direction)} for a simpler notify function
     *
     * @param htmltext     - the text you want to display (may include HTML tags)
     * @param viewDuration - the time in ms the notification should be visible for. Pass in 0
     *                     to be auto calculated based on word count
     * @param arrowDir     - the direction of the arrow on the notification
     * @param startDir     - the enter direction of the notification
     * @param vanishDir    - the exit direction of the notification
     */
    public void notify(String htmltext, int viewDuration, Direction arrowDir, Direction startDir, Direction vanishDir) {
        //make a gluster and add to queue, queue will automatically process any notifications so no further actions needed
        notificationList.add(new Gluster(htmltext, viewDuration, arrowDir, startDir, vanishDir));

        if (!notificationCheckerStarted) {
            notificationCheckerStarted = true;

            new Thread(() -> {
                try {
                    while (this != null && !threadsKilled) {
                        if (notificationList.size() > 0) {
                            Gluster currentGluster = notificationList.poll();

                            //init notification object
                            currentNotification = new Notification();

                            //set the arrow direction
                            currentNotification.setArrow(currentGluster.getArrowDir());

                            //create text label to go on top of notification label
                            JLabel text = new JLabel();
                            //use html so that it can line break when we need it to
                            text.setText("<html>" + currentGluster.getHtmlText() + "</html>");

                            //start of font width and height calculation
                            int w = 0;
                            Font notificationFont = CyderFonts.weatherFontSmall;
                            AffineTransform affinetransform = new AffineTransform();
                            FontRenderContext frc = new FontRenderContext(affinetransform, notificationFont.isItalic(), true);

                            //parse away html
                            String parsedHTML = Jsoup.clean(currentGluster.getHtmlText(), Whitelist.none());

                            //get minimum width for whole parsed string
                            w = (int) notificationFont.getStringBounds(parsedHTML, frc).getWidth() + 5;

                            //get height of a line and set it as height increment too
                            int h = (int) notificationFont.getStringBounds(parsedHTML, frc).getHeight();
                            int heightInc = h;
                            FontMetrics metrics = getGraphics().getFontMetrics();

                            while (w > 0.9 * this.width) {
                                int area = w * h;
                                w /= 2;
                                h = area / w;
                            }

                            if (h != heightInc)
                                h += heightInc + metrics.getAscent();

                            //set the text bounds to the proper x,y and theest
                            // calculated width and height
                            text.setBounds(currentNotification.getTextXOffset(), currentNotification.getTextYOffset(), w, h);

                            currentNotification.setWidth(w);
                            currentNotification.setHeight(h);

                            text.setFont(notificationFont);
                            text.setForeground(CyderColors.navy);
                            currentNotification.add(text);

                            JLabel disposeLabel = new JLabel();
                            disposeLabel.setBounds(currentNotification.getTextXOffset(), currentNotification.getTextYOffset(), w, h);
                            disposeLabel.setToolTipText("Click to dismiss");
                            disposeLabel.addMouseListener(new MouseAdapter() {
                                @Override
                                public void mouseClicked(MouseEvent e) {
                                currentNotification.kill();
                                }
                            });
                            currentNotification.add(disposeLabel);

                            if (currentGluster.getStartDir() == Direction.LEFT)
                                currentNotification.setLocation(-currentNotification.getWidth() + 5, topDrag.getHeight());
                            else if (currentGluster.getStartDir() == Direction.RIGHT)
                                currentNotification.setLocation(getContentPane().getWidth() - 5, topDrag.getHeight());
                            else if (currentGluster.getStartDir() == Direction.BOTTOM)
                                currentNotification.setLocation(getContentPane().getWidth() / 2 - (w / 2) - currentNotification.getTextXOffset(),
                                        getHeight());
                            else
                                currentNotification.setLocation(getContentPane().getWidth() / 2 - (w / 2) - currentNotification.getTextYOffset(),
                                        DragLabel.getDefaultHeight() - currentNotification.getHeight());

                            contentLabel.add(currentNotification);
                            getContentPane().repaint();

                            //duration is always 300ms per word unless less than 5 seconds
                            int duration = 300 * StringUtil.countWords(parsedHTML);
                            duration = duration < 5000 ? 5000 : duration;
                            duration = currentGluster.getDuration() == 0 ?
                                    duration : currentGluster.getDuration();
                            currentNotification.appear(currentGluster.getStartDir(), currentGluster.getVanishDir(),
                                    getContentPane(), duration);

                            int enterTime = 0;
                            int exitTime = 0;

                            switch (currentGluster.getStartDir()) {
                                case BOTTOM:
                                    enterTime = (getHeight() - currentNotification.getHeight() + 5) * Notification.getIncrement();
                                    break;
                                case TOP:
                                    enterTime = DragLabel.getDefaultHeight() * Notification.getIncrement();
                                    break;
                                case LEFT:
                                    enterTime = 5 * Notification.getIncrement();
                                    break;
                                case RIGHT:
                                    enterTime = (currentNotification.getWidth() + 5) * Notification.getIncrement();
                                    break;
                            }

                            switch (currentGluster.getVanishDir()) {
                                case BOTTOM:
                                    exitTime = (getHeight() - currentNotification.getHeight() + 5) * Notification.getIncrement();
                                    break;
                                case TOP:
                                    exitTime = DragLabel.getDefaultHeight() * Notification.getIncrement();
                                    break;
                                case LEFT:
                                    exitTime = 5 * Notification.getIncrement();
                                    break;
                                case RIGHT:
                                    exitTime = (currentNotification.getWidth() + 5) * Notification.getIncrement();
                                    break;
                            }

                            //sleep the enter time, duration, exit time, and an extra 500ms
                            Thread.sleep(enterTime + duration + exitTime + 500);
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
     * Getter for the top drag label associated with this CyderFrame instance. Used for frame resizing.
     * @return - The associated DragLabel
     */
    public DragLabel getTopDragLabel() {
        return topDrag;
    }

    /**
     * Getter for the bottom drag label associated with this CyderFrame instance. Used for frame resizing.
     * @return - The associated DragLabel
     */
    public DragLabel getBottomDragLabel() {
        return bottomDrag;
    }

    /**
     * Getter for the left drag label associated with this CyderFrame instance. Used for frame resizing.
     * @return - The associated DragLabel
     */
    public DragLabel getLeftDragLabel() {
        return leftDrag;
    }

    /**
     * Getter for the right drag label associated with this CyderFrame instance. Used for frame resizing.
     * @return - The associated DragLabel
     */
    public DragLabel getRightDragLabel() {
        return rightDrag;
    }

    /**
     * Pops open a window relative to this with the provided text
     *
     * @param text  - the String you wish to display
     * @param title - The title of the CyderFrame which will be opened to display the text
     */
    public void inform(String text, String title) {
        GenericInform.informRelative(text, title, this);
    }

    /**
     * Animates the window from offscreen top to the center of the screen.
     * This should be called in place of {@code this.setLocationRelativeTo(null);}
     */
    public void enterAnimation() {
        AnimationUtil.enterAnimation(this);
    }

    @Override
    public void setLocationRelativeTo(Component c) {
        if (c == null) {
            this.enterAnimation();
        } else {
            super.setLocationRelativeTo(c);
        }
    }

    /**
     * Close animation moves the window up until the CyderFrame is off screen. this.dispose() is then invoked
     * perhaps you might re-write this to override dispose so that closeanimation is always called and you can
     * simply dispose of a frame like normal.
     */
    public void closeAnimation() {
        if (this == null)
            return;

        topDrag.disableDragging();
        bottomDrag.disableDragging();
        leftDrag.disableDragging();
        rightDrag.disableDragging();

        try {
            if (this != null && this.isVisible()) {
                AnimationUtil.closeAnimation(this);
                this.dispose();
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    @Override
    public void dispose() {
        killThreads();
        super.dispose();

        if (currentNotification != null)
            currentNotification.kill();
    }

    /**
     * Moves the window down until it is off screen before setting the state to ICONIFIED.
     * The original position of the frame will be remembered and set when the window is deiconified.
     */
    public void minimizeAnimation() {
        if (this == null)
            return;

        topDrag.disableDragging();
        bottomDrag.disableDragging();
        leftDrag.disableDragging();
        rightDrag.disableDragging();

        Point point = this.getLocationOnScreen();
        int x = (int) point.getX();
        int y = (int) point.getY();

        try {
            for (int i = y; i <= SystemUtil.getScreenHeight(); i += 15) {
                Thread.sleep(1);
                this.setLocation(x, i);
            }

            this.setState(JFrame.ICONIFIED);
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }

        topDrag.enableDragging();
        bottomDrag.enableDragging();
        leftDrag.enableDragging();
        rightDrag.enableDragging();
    }

    /**
     * Allow or disable moving the window.
     *
     * @param relocatable - the boolean value determining if the window may be repositioned
     */
    public void setRelocatable(boolean relocatable) {
        if (relocatable) {
            topDrag.enableDragging();
            bottomDrag.enableDragging();
            leftDrag.enableDragging();
            rightDrag.enableDragging();
        } else {
            topDrag.disableDragging();
            bottomDrag.disableDragging();
            leftDrag.disableDragging();
            rightDrag.disableDragging();
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

    /**
     * moves the frame around the user's monitor before returning to the initial location.
     */
    public void dance() {
        int delay = 10;
        Thread DanceThread = new Thread(() -> {
            boolean wasEnabled = false;

            if (topDrag.isDraggingEnabled()) {
                topDrag.disableDragging();
                bottomDrag.disableDragging();
                leftDrag.disableDragging();
                rightDrag.disableDragging();
                wasEnabled = true;
            }

            setAlwaysOnTop(true);
            int restoreX = this.getX();
            int restoreY = this.getY();

            control_c_exit:
                try {
                    //if out of bounds, bring just in bounds
                    if (restoreY < 0)
                        setLocation(restoreX, 0);
                    else if (restoreY > SystemUtil.getScreenHeight())
                        setLocation(restoreX, SystemUtil.getScreenHeight() - this.getHeight());
                    if (restoreX < 0)
                        setLocation(0, restoreY);
                    else if (restoreX > SystemUtil.getScreenWidth())
                        setLocation(SystemUtil.getScreenWidth() - this.getWidth(), restoreY);

                    //moves frame up to top of screen
                    for (int y = getY(); y >= 0; y -= 10) {
                        if (this.control_c_threads)
                            break control_c_exit;
                        Thread.sleep(delay);
                        setLocation(this.getX(), y);
                    }

                    //move from right to left
                    for (int x = getX(); x >= 0; x -= 10) {
                        if (this.control_c_threads)
                            break control_c_exit;
                        Thread.sleep(delay);
                        setLocation(x, this.getY());
                    }

                    //move from top to bottom
                    for (int y = getY(); y <= SystemUtil.getScreenHeight() - this.getHeight(); y += 10) {
                        if (this.control_c_threads)
                            break control_c_exit;
                        Thread.sleep(delay);
                        setLocation(this.getX(), y);
                    }

                    //move from left to right
                    for (int x = getX(); x <= SystemUtil.getScreenWidth() - this.getWidth(); x += 10) {
                        if (this.control_c_threads)
                            break control_c_exit;
                        Thread.sleep(delay);
                        setLocation(x, this.getY());
                    }

                    //move from bottom to top
                    for (int y = getY(); y > 0; y -= 10) {
                        if (this.control_c_threads)
                            break control_c_exit;
                        Thread.sleep(delay);
                        setLocation(this.getX(), y);
                    }

                    //move from top to restoreX
                    for (int x = getX(); x >= restoreX; x -= 10) {
                        if (this.control_c_threads)
                            break control_c_exit;
                        Thread.sleep(delay);
                        setLocation(x, this.getY());
                    }

                    setLocation(restoreX, this.getY());

                    //move from top to restoreY
                    for (int y = getY(); y <= restoreY; y += 10) {
                        if (this.control_c_threads)
                            break control_c_exit;
                        Thread.sleep(delay);
                        setLocation(this.getX(), y);
                    }

                } catch (Exception e) {
                    ErrorHandler.handle(e);
                }

            setLocation(restoreX, restoreY);

            setLocation(restoreX, restoreY);
            setAlwaysOnTop(false);

            if (wasEnabled) {
                topDrag.enableDragging();
                bottomDrag.enableDragging();
                leftDrag.enableDragging();
                rightDrag.enableDragging();
            }
        },this + " [dance thread]");

        DanceThread.start();
    }

    /**
     * transforms the content pane by an incremental angle of 2 degrees emulating Google's barrel roll easter egg
     */
    public void barrelRoll() {
        setBackground(CyderColors.navy);

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
     *
     * @param degrees - the degrees to be rotated by; 360deg = 0deg.
     */
    public void rotateBackground(int degrees) {
        ImageIcon masterIcon = (ImageIcon) ((JLabel) getContentPane()).getIcon();
        BufferedImage master = ImageUtil.getBi(masterIcon);
        BufferedImage rotated = ImageUtil.rotateImageByDegrees(master, degrees);
        ((JLabel) getContentPane()).setIcon(new ImageIcon(rotated));
    }

    /**
     * Attempts to rotate the background about the center using trigonometry.
     * This method is mostly a joke and shouldn't be called seriously.
     * See {@link CyderFrame#rotateBackground(int)} to rotate the content pane about the top left.
     *
     * @param degrees - the degrees to rotate by. Follow polar coordinate rules for figuring out
     *                equivalent angles
     */
    public void rotateBackgroundFromCenter(int degrees) {
        //get our current imageicon
        ImageIcon masterIcon = (ImageIcon) ((JLabel) getContentPane()).getIcon();
        //rotate the imageicon
        BufferedImage rotated = ImageUtil.rotateImageByDegrees(ImageUtil.getBi(masterIcon), degrees);
        //init a buffered image with the necessary dimensions
        BufferedImage paddedBi = new BufferedImage(rotated.getWidth(), rotated.getHeight(), BufferedImage.TYPE_INT_RGB);
        //create graphics for the image
        Graphics g = paddedBi.createGraphics();
        g.setColor(CyderColors.navy);
        g.fillRect(0, 0, rotated.getWidth(), rotated.getHeight());

        double rads = degrees * Math.PI / 180.0;
        int x0 = (rotated.getWidth()) / 2;
        int y0 = (rotated.getHeight()) / 2;
        int sin = (int) Math.sin(rads);
        int cos = (int) Math.cos(rads);
        int xoff = Math.abs((x0 - x0 * cos + y0 * sin)) - masterIcon.getIconWidth() / 2;
        int yoff = Math.abs((y0 - x0 * sin - y0 * cos)) - masterIcon.getIconHeight() / 2;
        xoff /= 2;
        yoff /= 2;

        //draw our rotated image on the padded image
        g.drawImage(rotated, -xoff, -yoff, null);

        ((JLabel) getContentPane()).setIcon(new ImageIcon(paddedBi));
    }

    /**
     * Repaints the title position in the correct location. To be used on resize events where we don't
     * want to set button position or title position differently. We simply want to revalidate the title position
     */
    public void refreshTitlePosition() {
        switch (titlePosition) {
            case LEFT:
                titleLabel.setLocation(4,2);
                break;
            case RIGHT:
                titleLabel.setLocation(this.width - getMinWidth(titleLabel.getText()) + 8, 2);
                break;
            case CENTER:
                titleLabel.setLocation((getTopDragLabel().getWidth() / 2) - (getMinWidth(titleLabel.getText()) / 2), 2);
                break;
        }
    }

    /**
     * Sets the frame bounds and also changes the underlying drag label's bounds which is why this method is overridden.
     */
    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);

        if (getTopDragLabel() != null) {
            getTopDragLabel().setWidth(width);
            getBottomDragLabel().setWidth(width);
            getLeftDragLabel().setHeight(height);
            getRightDragLabel().setHeight(height);

            refreshTitlePosition();

            topDrag.setBounds(0, 1, width, DragLabel.getDefaultHeight() - 1);
            leftDrag.setBounds(1, DragLabel.getDefaultHeight(), 4, height - DragLabel.getDefaultHeight() - 2);
            rightDrag.setBounds(width - 5, DragLabel.getDefaultHeight(), 4, height - DragLabel.getDefaultHeight() - 2);
            bottomDrag.setBounds(0, height - 5, width, 4);

            topDrag.setxOffset(0);
            topDrag.setyOffset(1);

            leftDrag.setxOffset(1);
            leftDrag.setyOffset(DragLabel.getDefaultHeight());

            rightDrag.setxOffset(width - 5);
            rightDrag.setyOffset(DragLabel.getDefaultHeight());

            bottomDrag.setxOffset(0);
            bottomDrag.setyOffset(height - 5);
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

    private Dimension minimumSize = new Dimension(200, 200);
    private Dimension maximumSize = new Dimension(800, 800);
    private Dimension snapSize = new Dimension(1, 1);

    /**
     * Sets the minimum window size if resizing is allowed.
     *
     * @param minSize - the Dimension of the minimum allowed size
     */
    public void setMinimumSize(Dimension minSize) {
        this.minimumSize = minSize;
        cr.setMinimumSize(minimumSize);
    }

    /**
     * Sets the maximum window size if resizing is allowed.
     *
     * @param maxSize - the Dimension of the minimum allowed size
     */
    public void setMaximumSize(Dimension maxSize) {
        this.maximumSize = maxSize;
        cr.setMaximumSize(maximumSize);
    }

    /**
     * Sets the snap size for the window if resizing is allowed.
     *
     * @param snap - the dimension of the snap size
     */
    public void setSnapSize(Dimension snap) {
        this.snapSize = snap;
        cr.setSnapSize(snapSize);
    }

    /**
     * @return - the minimum window size if resizing is allowed
     */
    public Dimension getMinimumSize() {
        return minimumSize;
    }

    /**
     * @return - the maximum window size if resizing is allowed
     */
    public Dimension getMaximumSize() {
        return maximumSize;
    }

    /**
     * @return - the snap size for the window if resizing is allowed
     */
    public Dimension getSnapSize() {
        return snapSize;
    }

    ComponentResizer cr;

    /**
     * Choose to allow/disable background image reisizing if window resizing is allowed.
     * @param allowed - the value determining background resizing
     */
    public void setBackgroundResizing(Boolean allowed) {
        cr.enableBackgroundResize(allowed);
    }

    /**
     * This method should be called first when attempting to allow resizing of a frame.
     * Procedural calls: init component resizer, set resizing to true, set min, max, and snap sizes to default.
     */
    public void initializeResizing() {
        cr = new ComponentResizer();
        cr.registerComponent(this);
        cr.setResizing(true);
        cr.setMinimumSize(getMinimumSize());
        cr.setMaximumSize(getMaximumSize());
        cr.setSnapSize(getSnapSize());
    }

    /**
     * @param allow - sets/disables resizing of the frame.
     */
    public void setFrameResizing(Boolean allow) {
        cr.setResizing(allow);
    }

    ImageIcon currentOrigIcon;


    //todo pull from an old commit before last night changes
    /**
     * Refresh the background in the event of a frame size change or background image change.
     */
    public void refreshBackground() {
        try {
            contentLabel.setIcon(new ImageIcon(currentOrigIcon.getImage()
                    .getScaledInstance(contentLabel.getWidth(), contentLabel.getHeight(), Image.SCALE_DEFAULT)));
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    /**
     * Set the background to a new icon and refresh the frame.
     * @param icon - the ImageIcon you want the frame background to be
     */
    public void setBackground(ImageIcon icon) {
        try {
            currentOrigIcon = icon;
            contentLabel.setIcon(new ImageIcon(currentOrigIcon.getImage()
                    .getScaledInstance(getWidth(), getHeight(), Image.SCALE_DEFAULT)));
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    /**
     * Sets the background color of the Frame's content pane.
     * @param background - the Color object value of the content pane's desired background
     */
    @Override
    public void setBackground(Color background) {
        super.setBackground(background);
        backgroundColor = background;
        currentOrigIcon = null;
        this.repaint();
    }

    /**
     * Returns the background color of the contentPane.
     * @return - Color object of the background color
     */
    @Override
    public Color getBackground() {
        return this.backgroundColor;
    }

    @Override
    public String toString() {
        String title = titleLabel.getText() == null || titleLabel.getText().length() == 0 ? super.getTitle() : titleLabel.getText();
        return "Name: " + title + "[" + this.getTitlePosition() + "],(" +
                this.getX() + "," + this.getY() + "," + this.getWidth() + "x" + this.getHeight() + ")";
    }

    /**
     * Kills all threads associated with this CyderFrame instance. An irreversible action. This
     * method is actomatically called when {@link CyderFrame#closeAnimation()} or {@link CyderFrame#dispose()} is invokekd.
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
        if (ConsoleFrame.getCurrentBackgroundImageIcon() == null)
            return;

        currentOrigIcon = ConsoleFrame.getCurrentBackgroundImageIcon();

        contentLabel.setIcon(new ImageIcon(currentOrigIcon.getImage()
                .getScaledInstance(getWidth(), getHeight(), Image.SCALE_DEFAULT)));
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
}