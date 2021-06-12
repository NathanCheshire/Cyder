package cyder.ui;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.enums.Direction;
import cyder.handler.ErrorHandler;
import cyder.obj.Gluster;
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

//todo layering for compoents as follows:
//  layer 0: the content label
//  layer 1: the components on the content label all share the same priority
//  layer 2: notifications
//  layer 3: the drag label and the frame border

//todo better method names

//override get content pane for already in place widgets to return the label
//add a get actual content pane method to return the actual one?

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
    private int width;
    private int height;

    private boolean threadsKilled;

    private ImageIcon background;

    private DragLabel dl;

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
        setSize(new Dimension(width, height));

        setResizable(false);
        setUndecorated(true);
        setBackground(backgroundColor);
        setIconImage(SystemUtil.getCyderIcon().getImage());

        contentLabel = new JLabel();
        contentLabel.setBorder(new LineBorder(CyderColors.navy, 5, false));
        contentLabel.setIcon(background);

        setContentPane(contentLabel);

        dl = new DragLabel(width, 30, this);
        dl.setBounds(0, 0, width, 30);
        dl.setRestoreX(SystemUtil.getScreenWidth() / 2 - this.getWidth() / 2);
        dl.setRestoreY(SystemUtil.getScreenHeight() / 2 - this.getHeight() / 2);
        contentLabel.add(dl);

        titleLabel = new JLabel("");
        titleLabel.setFont(new Font("Agency FB", Font.BOLD, 22));
        titleLabel.setForeground(CyderColors.vanila);

        dl.add(titleLabel);

        this.threadsKilled = false;

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
                        w = (int) notificationFont.getStringBounds(parsedHTML, frc).getWidth() + 10;

                        //get height of a line and set it as height increment too
                        int h = (int) notificationFont.getStringBounds(parsedHTML, frc).getHeight();
                        FontMetrics metrics = getGraphics().getFontMetrics();

                        //if too much width, take half away and add back in height
                        while (w > 0.9 * getWidth()) {
                            w /= 2;
                            h = h * 2;
                            h += metrics.getAscent();
                        }

                        if (h != (int) notificationFont.getStringBounds(parsedHTML, frc).getHeight())
                            h -= metrics.getAscent();

                        //now we have min width and height for string bounds, no more no less than this

                        //set the text bounds to the proper x,y and the calculated width and height
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
                            currentNotification.setLocation(-currentNotification.getWidth() + 5, dl.getHeight());
                        else if (currentGluster.getStartDir() == Direction.RIGHT)
                            currentNotification.setLocation(getContentPane().getWidth() - 5, dl.getHeight());
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
                                enterTime = (getHeight() - currentNotification.getHeight() + 5) * 4;
                                break;
                            case TOP:
                                enterTime = DragLabel.getDefaultHeight() * 4;
                                break;
                            case LEFT:
                                enterTime = (currentNotification.getWidth() + 5) * 4;
                                break;
                            case RIGHT:
                                enterTime = (currentNotification.getHeight() + 5) * 4;
                                break;
                        }

                        switch (currentGluster.getVanishDir()) {
                            case BOTTOM:
                                exitTime = (getHeight() - currentNotification.getHeight() + 5) * 4;
                                break;
                            case TOP:
                                exitTime = DragLabel.getDefaultHeight() * 4;
                                break;
                            case LEFT:
                                exitTime = (currentNotification.getWidth() + 5) * 4;
                                break;
                            case RIGHT:
                                exitTime = (currentNotification.getHeight() + 5) * 4;
                                break;
                        }

                        Thread.sleep(duration + enterTime + exitTime);
                    }

                    //wait 500ms
                    Thread.sleep(500);
                }
            } catch (Exception e) {
                ErrorHandler.handle(e);
            }
        }, this + " CyderFrame notification queue checker").start();
    }

    //todo rotate the background label which we will place on the contentlabel,
    // then we'll place components on the backgroundlabel?
    // some kind of similar trick could be used for layering between
    // components, notifications, drag label and borders, etc.

    /**
     * returns an instance of a cyderframe which extends JFrame with the specified width and height
     * and a drag label with minimize and close buttons
     *
     * @param width  - the specified width of the cyder frame
     * @param height - the specified height of the cyder frame
     */
    public CyderFrame(int width, int height) {
        this(width, height, ImageUtil.imageIconFromColor(CyderColors.vanila));
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
        if (titlePosition == null || this.titlePosition == null)
            return;

        boolean different = titlePosition != this.titlePosition;
        this.titlePosition = titlePosition;
        long timeout = 2;

        if (different && isVisible()) {
            if (titlePosition != CyderFrame.TitlePosition.CENTER) {
                new Thread(() -> {
                    for (int i = (getDragLabel().getWidth() / 2) - (getMinWidth(titleLabel.getText()) / 2); i > 4; i--) {
                        if (this.threadsKilled)
                            break;

                        titleLabel.setLocation(i, 2);

                        try {
                            Thread.sleep(timeout);
                        } catch (Exception e) {
                            ErrorHandler.handle(e);
                        }
                    }
                }).start();
            } else {
                new Thread(() -> {
                    for (int i = 5; i < (getDragLabel().getWidth() / 2) - (getMinWidth(titleLabel.getText()) / 2) + 1; i++) {
                        if (this.threadsKilled)
                            break;

                        titleLabel.setLocation(i, 2);

                        try {
                            Thread.sleep(timeout);
                        } catch (Exception e) {
                            ErrorHandler.handle(e);
                        }
                    }
                }).start();
            }
        }
    }


    /**
     * Getter for the title position
     *
     * @return - position representing the title position
     */
    public TitlePosition getTitlePosition() {
        return this.titlePosition;
    }


    private boolean paintSuperTitle = true;

    /**
     * Determines whether or not to paint the windowed title. The CyderFrame label title is always painted.
     *
     * @param enable - boolean variable of your chosen value for paintSuperTitle
     */
    public void paintSuperTitle(boolean enable) {
        paintSuperTitle = enable;
    }

    /**
     * Returns the value of paintSuperTitle which determines whether ot not the windowed title is painted.
     *
     * @return - boolean describing the value of paintSuperTitle
     */
    public boolean getPaintSuperTitle() {
        return paintSuperTitle;
    }

    /**
     * Set the title of the label painted on the drag label of the CyderFrame instance. You can also configure the instance
     * to paint the windowed title as well.
     *
     * @param title - the String representing the chosen CyderFrame title
     */
    @Override
    public void setTitle(String title) {
        super.setTitle(paintSuperTitle ? title : "");

        titleLabel.setText(title);

        switch (titlePosition) {
            case CENTER:
                titleLabel.setBounds((getDragLabel().getWidth() / 2) - (getMinWidth(title) / 2), 2, getMinWidth(title), 25);
                break;

            default:
                titleLabel.setBounds(5, 2, getMinWidth(title), 25);
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
    }

    /**
     * Getter for the drag label associated with this CyderFrame instance. Used for frame resizing.
     *
     * @return - The associated DragLabel
     */
    public DragLabel getDragLabel() {
        return dl;
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
     */
    public void enterAnimation() {
        if (this == null)
            return;

        dl.disableDragging();

        this.setVisible(false);
        this.setLocationRelativeTo(null);

        int to = this.getY();
        this.setLocation(this.getX(), 0 - this.getHeight());

        this.setVisible(true);

        for (int i = 0 - this.getHeight(); i < to; i += 15) {
            this.setLocation(this.getX(), i);
            try {
                Thread.sleep(1);
            } catch (Exception e) {
                ErrorHandler.handle(e);
            }
        }

        this.setLocationRelativeTo(null);

        dl.enableDragging();
    }

    /**
     * Close animation moves the window up until the CyderFrame is off screen. this.dispose() is then invoked
     * perhaps you might re-write this to override dispose so that closeanimation is always called and you can
     * simply dispose of a frame like normal.
     */
    public void closeAnimation() {
        if (this == null)
            return;

        dl.disableDragging();

        try {
            if (this != null && this.isVisible()) {
                Point point = this.getLocationOnScreen();
                int x = (int) point.getX();
                int y = (int) point.getY();

                for (int i = y; i >= 0 - this.getHeight(); i -= 15) {
                    Thread.sleep(1);
                    this.setLocation(x, i);
                }

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

        //todo commented since logging in as another user from one user makes the user not load
        // figure out new logic for this such as an executor to check for frames every 5 or so seconds
//        Frame[] frames = Frame.getFrames();
//        int validFrames = 0;
//
//        for (Frame f : frames)
//            if (f.isShowing())
//                validFrames++;
//
//        if (validFrames < 1)
//            System.exit(120);
    }

    /**
     * Moves the window down until it is off screen before setting the state to ICONIFIED.
     * The original position of the frame will be remembered and set when the window is deiconified.
     */
    public void minimizeAnimation() {
        if (this == null)
            return;

        dl.disableDragging();

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

        dl.enableDragging();
    }

    /**
     * Allow or disable moving the window.
     *
     * @param relocatable - the boolean value determining if the window may be repositioned
     */
    public void setRelocatable(boolean relocatable) {
        if (relocatable)
            dl.enableDragging();
        else
            dl.disableDragging();
    }

    /**
     * moves the frame around the user's monitor before returning to the initial location.
     */
    public void dance() {
        int delay = 10;
        Thread DanceThread = new Thread(() -> {
            try {
                boolean wasEnabled = false;

                if (dl.isDraggingEnabled()) {
                    dl.disableDragging();
                    wasEnabled = true;
                }

                setAlwaysOnTop(true);
                int restoreX = this.getX();
                int restoreY = this.getY();

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
                    if (this.threadsKilled)
                        break;
                    Thread.sleep(delay);
                    setLocation(this.getX(), y);
                }

                //move from right to left
                for (int x = getX(); x >= 0; x -= 10) {
                    if (this.threadsKilled)
                        break;
                    Thread.sleep(delay);
                    setLocation(x, this.getY());
                }

                //move from top to bottom
                for (int y = getY(); y <= SystemUtil.getScreenHeight() - this.getHeight(); y += 10) {
                    if (this.threadsKilled)
                        break;
                    Thread.sleep(delay);
                    setLocation(this.getX(), y);
                }

                //move from left to right
                for (int x = getX(); x <= SystemUtil.getScreenWidth() - this.getWidth(); x += 10) {
                    if (this.threadsKilled)
                        break;
                    Thread.sleep(delay);
                    setLocation(x, this.getY());
                }

                //move from bottom to top
                for (int y = getY(); y > 0; y -= 10) {
                    if (this.threadsKilled)
                        break;
                    Thread.sleep(delay);
                    setLocation(this.getX(), y);
                }

                //move from top to restoreX
                for (int x = getX(); x >= restoreX; x -= 10) {
                    if (this.threadsKilled)
                        break;
                    Thread.sleep(delay);
                    setLocation(x, this.getY());
                }

                setLocation(restoreX, this.getY());

                //move from top to restoreY
                for (int y = getY(); y <= restoreY; y += 10) {
                    if (this.threadsKilled)
                        break;
                    Thread.sleep(delay);
                    setLocation(this.getX(), y);
                }

                setLocation(restoreX, restoreY);

                setLocation(restoreX, restoreY);
                setAlwaysOnTop(false);

                if (wasEnabled)
                    dl.enableDragging();

            } catch (Exception e) {
                ErrorHandler.handle(e);
            }
        });

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
        System.out.println(xoff + "," + yoff);

        //draw our rotated image on the padded image
        g.drawImage(rotated, -xoff, -yoff, null);

        ((JLabel) getContentPane()).setIcon(new ImageIcon(paddedBi));
    }

    /**
     * Sets the frame bounds and also changes the underlying drag label's bounds which is why this method is overridden.
     */
    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);

        if (getDragLabel() != null) {
            getDragLabel().setWidth(width);
            setTitle(getTitle());
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
     *
     * @param allowed - the value determining background resizing
     */
    public void setBackgroundResizing(Boolean allowed) {
        cr.enableBackgroundResize(allowed);
    }

    /**
     * This method should be called first when attempting to allow resizing of a frame.
     * Procedural calls: init component resizer, set resizing to true, set min, max, and snap sizes to default.
     */
    public void initializeBackgroundResizing() {
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
     *
     * @param icon - the ImageIcon you want the frame background to be
     */
    public void setBackground(ImageIcon icon) {
        try {
            currentOrigIcon = icon;
            contentLabel.setIcon(new ImageIcon(currentOrigIcon.getImage()
                    .getScaledInstance(contentLabel.getWidth(), contentLabel.getHeight(), Image.SCALE_DEFAULT)));
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    /**
     * Sets the background color of the Frame's content pane.
     *
     * @param background - the Color object value of the content pane's desired background
     */
    @Override
    public void setBackground(Color background) {
        super.setBackground(background);
        backgroundColor = background;
        this.repaint();
    }

    /**
     * Returns the background color of the contentPane.
     *
     * @return - Color object of the background color
     */
    @Override
    public Color getBackground() {
        return this.backgroundColor;
    }

    @Override
    public String toString() {
        return "title: " + this.getTitle() + "[" + this.getTitlePosition() + ",(" +
                this.getX() + "," + this.getY() + "," + this.getWidth() + "x" + this.getHeight() + ")]";
    }

    /**
     * Kills all threads associated with this CyderFrame instance. An irreversible action. This
     * method is actomatically called when {@link CyderFrame#closeAnimation()} or {@link CyderFrame#dispose()} is invokekd.
     */
    public void killThreads() {
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

    //overridden so we can set the drag label's restore point
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        dl.setRestoreX(this.getX());
        dl.setRestoreY(this.getY());
    }
}