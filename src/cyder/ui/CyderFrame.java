package cyder.ui;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.consts.CyderImages;
import cyder.enums.Direction;
import cyder.handler.ErrorHandler;
import cyder.handler.SessionLogger;
import cyder.genobjects.Gluster;
import cyder.utilities.*;
import cyder.widgets.GenericInform;
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

    private TitlePosition titlePosition = TitlePosition.LEFT;
    private ButtonPosition buttonPosition = ButtonPosition.RIGHT;
    private int width = 1;
    private int height = 1;

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

    private Color backgroundColor = CyderColors.navy;

    private LinkedList<Gluster> notificationList = new LinkedList<>();

    private String title = "";

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

        //try and get preference
        if (ConsoleFrame.getConsoleFrame().getUUID() != null) {
            if (UserUtil.extractUser().getRoundedwindows().equals("1")) {
                setShape(new RoundRectangle2D.Double(0, 0,
                        getWidth(), getHeight(), 20, 20));
            } else {
                setShape(null);
            }
        }

        //listener to ensure the close button was always pressed essentially
        this.addWindowListener(new WindowAdapter() {
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

        iconLabel = new JLabel();
        iconLabel.setIcon(background);
        iconLabel.setBounds(0,0,width - 2,height - 2);
        iconLabel.setFocusable(false);

        iconPane = new JLayeredPane();
        iconPane.setBounds(1,1, width - 2, height - 2);
        iconPane.add(iconLabel,JLayeredPane.DEFAULT_LAYER);
        iconPane.setFocusable(false);

        contentLabel.add(iconPane,JLayeredPane.DEFAULT_LAYER);

        contentLabel.setBorder(new LineBorder(CyderColors.guiThemeColor, 3, false));
        setContentPane(contentLabel);

        topDrag = new DragLabel(width, DragLabel.getDefaultHeight() - 1, this);
        topDrag.setBounds(0, 1, width, DragLabel.getDefaultHeight() - 1);
        topDrag.setxOffset(0);
        topDrag.setyOffset(1);
        contentLabel.add(topDrag, JLayeredPane.DRAG_LAYER);
        topDrag.setFocusable(false);

        leftDrag = new DragLabel(4, height - DragLabel.getDefaultHeight() - 2, this);
        leftDrag.setBounds(1, DragLabel.getDefaultHeight(), 4, height - DragLabel.getDefaultHeight() - 2);
        leftDrag.setxOffset(1);
        leftDrag.setyOffset(DragLabel.getDefaultHeight());
        contentLabel.add(leftDrag, JLayeredPane.DRAG_LAYER);
        leftDrag.setFocusable(false);
        leftDrag.setButtonsList(null);

        rightDrag = new DragLabel(4, height - DragLabel.getDefaultHeight() - 2, this);
        rightDrag.setBounds(width - 5, DragLabel.getDefaultHeight(), 4, height - DragLabel.getDefaultHeight() - 2);
        rightDrag.setxOffset(width - 5);
        rightDrag.setyOffset(DragLabel.getDefaultHeight());
        contentLabel.add(rightDrag, JLayeredPane.DRAG_LAYER);
        rightDrag.setFocusable(false);
        rightDrag.setButtonsList(null);

        bottomDrag = new DragLabel(width, 4, this);
        bottomDrag.setBounds(0, height - 5, width, 4);
        bottomDrag.setxOffset(0);
        bottomDrag.setyOffset(height - 5);
        contentLabel.add(bottomDrag, JLayeredPane.DRAG_LAYER);
        bottomDrag.setFocusable(false);
        bottomDrag.setButtonsList(null);

        titleLabel = new JLabel("");
        titleLabel.setFont(CyderFonts.frameTitleFont);
        titleLabel.setForeground(CyderColors.vanila);
        titleLabel.setFocusable(false);

        topDrag.add(titleLabel);

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
     * @param width  - the specified width of the cyder frame
     * @param height - the specified height of the cyder frame
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
                    titleLabel.setLocation(this.width -getMinWidth(this.title), 2);
                    setButtonPosition(ButtonPosition.LEFT);
                    break;
                case CENTER:
                    titleLabel.setLocation((getTopDragLabel().getWidth() / 2) - (getMinWidth(this.title) / 2), 2);
                    break;
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
            titleLabel.setLocation(this.width -getMinWidth(this.title), 2);
        }
    }

    private boolean paintWindowTitle = true;

    /**
     * Determines whether or not to paint the default windows title. The CyderFrame label title is always painted.
     * @param enable - boolean variable of your chosen value for paintWindowTitle
     */
    public void paintWindowTitle(boolean enable) {
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
     * @param title - the text you want to determine the width of
     * @param f - the font for the text
     * @return - an interger value determining the minimum width of a string of text (10 is added to avoid ... bug)
     */
    public static int getMinWidth(String title, Font f) {
        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform, true, true);
        return (int) f.getStringBounds(title, frc).getWidth() + 10;
    }

    /**
     * Returns the minimum width required for the given String using the given font without adding 10 to the result.
     * @param title - the text you want to determine the width of
     * @return - an interger value determining the minimum width of a string of text
     */
    public int getTitleWidth(String title) {
        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform, false, false);
        return (int) this.titleLabel.getFont().getStringBounds(title, frc).getWidth() + 10;
    }

    /**
     * Returns the minimum height required for the given String using the given font.
     * @param title - the text you want to determine the height of
     * @return - an interger value determining the minimum height of a string of text (10 is added to avoid ... bug)
     */
    public static int getMinHeight(String title, Font f) {
        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform, true, true);
        return (int) f.getStringBounds(title, frc).getHeight() + 10;
    }

    /**
     * Returns the minimum height required for the given String using the given font without adding 10.
     * @param title - the text you want to determine the height of
     * @return - an interger value determining the minimum height of a string of text
     */
    public static int getAbsoluteMinHeight(String title, Font f) {
        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform, true, true);
        return (int) f.getStringBounds(title, frc).getHeight();
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
                            String parsedHTML = Jsoup.clean(currentGluster.getHtmlText(), Safelist.none());

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

                            if (w > this.width * 0.9|| h > this.height * 0.9) {
                                GenericInform.informRelative(currentGluster.getHtmlText(),"Notification",this);
                                continue;
                            }

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
                                currentNotification.setLocation(getContentPane().getWidth() / 2 - (w / 2) - currentNotification.getTextXOffset(),
                                        DragLabel.getDefaultHeight() - currentNotification.getHeight());

                            iconPane.add(currentNotification, JLayeredPane.POPUP_LAYER);
                            getContentPane().repaint();

                            //log the notification
                            SessionLogger.log(SessionLogger.Tag.ACTION, "[" +
                                    this.getTitle() + "] [NOTIFICATION] " + currentGluster.getHtmlText());

                            //duration is always 300ms per word unless less than 5 seconds
                            int duration = 300 * StringUtil.countWords(parsedHTML);
                            duration = Math.max(duration, 5000);
                            duration = currentGluster.getDuration() == 0 ?
                                    duration : currentGluster.getDuration();
                            currentNotification.appear(currentGluster.getStartDir(), currentGluster.getVanishDir(),
                                    getContentPane(), duration);

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

    private int animationNano = 500;
    private int animationInc = 40;

    /**
     * Moves the window down until it is off screen before setting the state to ICONIFIED.
     * The original position of the frame will be remembered and set when the window is deiconified.
     */
    public void minimizeAnimation() {
        try {
            long start = System.currentTimeMillis();
            for (int i = this.getY(); i <= SystemUtil.getScreenHeight(); i += animationInc) {
                Thread.sleep(0, animationNano);
                setLocation(this.getX(), i);
            }
            setState(JFrame.ICONIFIED);
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    @Override
    public void dispose() {
        new Thread(() -> {
              try {
                  if (this == null)
                      return;

                  //if closing confirmation exists and the user decides they do not want to exit the frame
                  if (closingConfirmationMessage != null && new GetterUtil().getConfirmation(closingConfirmationMessage)) {
                      return;
                  }

                  //run all preCloseActions if any exists, this is performed after the confirmation check
                  // since now we are sure that we wish to close the frame
                  for (PreCloseAction action : preCloseActions)
                      action.invokeAction();

                  //todo remove me
                  System.out.println("Inside dispose: make there only be one print statement per frame close");

                  this.disableDragging();

                  if (this != null && isVisible()) {
                      Point point = getLocationOnScreen();
                      int x = (int) point.getX();
                      int y = (int) point.getY();

                      for (int i = y; i >= -getHeight(); i -= animationInc) {
                          Thread.sleep(0, animationNano);
                          setLocation(x, i);
                      }

                      killThreads();

                      if (currentNotification != null)
                          currentNotification.kill();

                      super.dispose();
                  }
              } catch (Exception e) {
                  ErrorHandler.handle(e);
              }
        }, "wait thread for GetterUtil().getConfirmation()").start();
    }

    /**
     * Allow or disable moving the window.
     * @param relocatable - the boolean value determining if the window may be repositioned
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

    /**
     * moves the frame around the user's monitor before returning to the initial location.
     */
    public void dance() {
        this.control_c_threads = false;
        int delay = 10;

        Thread DanceThread = new Thread(() -> {
            boolean wasEnabled = false;
            boolean wasOnTop = this.isAlwaysOnTop();

            if (topDrag.isDraggingEnabled()) {
                this.disableDragging();
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
            setAlwaysOnTop(false);

            if (wasEnabled) {
               this.enableDragging();
            }

            if (wasOnTop)
                this.setAlwaysOnTop(true);
        },this + " [dance thread]");

        DanceThread.start();
    }

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
     * @param degrees - the degrees to be rotated by; 360deg = 0deg.
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
                titleLabel.setLocation(4,2);
                break;
            case RIGHT:
                titleLabel.setLocation(this.width -getMinWidth(this.title), 2);
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
     * Sets the frame bounds and also changes the underlying drag label's bounds which is why this method is overridden.
     */
    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        this.width = width;
        this.height = height;

        if (getTopDragLabel() != null) {
            getTopDragLabel().setWidth(width);
            getBottomDragLabel().setWidth(width);
            getLeftDragLabel().setHeight(height);
            getRightDragLabel().setHeight(height);

            refreshTitleAndButtonPosition();

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

        if (isVisible())
            refreshBackground();
    }

    private Dimension minimumSize = new Dimension(200, 200);
    private Dimension maximumSize = new Dimension(800, 800);
    private Dimension snapSize = new Dimension(1, 1);

    /**
     * Sets the minimum window size if resizing is allowed.
     * @param minSize - the Dimension of the minimum allowed size
     */
    public void setMinimumSize(Dimension minSize) {
        this.minimumSize = minSize;
        cr.setMinimumSize(minimumSize);
    }

    /**
     * Sets the maximum window size if resizing is allowed.
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
     * @param allow - sets/disables resizing of the frame.
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

            iconLabel.setBounds(0,0,width - 2,height - 2);
            iconPane.setBounds(1,1, width - 2, height - 2);

            revalidate();
            repaint();
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
            //prevent errors before instantiation of ui objects
            if (iconLabel == null)
                return;

            currentOrigIcon = icon;
            iconLabel.setIcon(new ImageIcon(currentOrigIcon.getImage()
                    .getScaledInstance(iconLabel.getWidth(), iconLabel.getHeight(), Image.SCALE_DEFAULT)));
            iconLabel.setBounds(0,0,width - 2,height - 2);
            iconPane.setBounds(1,1, width - 2, height - 2);

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
     * @param background - the Color object value of the content pane's desired background
     */
    @Override
    public void setBackground(Color background) {
        super.setBackground(background);
        this.backgroundColor = background;
        revalidate();
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
        iconLabel.setBounds(0,0,width - 2,height - 2);
        iconPane.setBounds(1,1, width - 2, height - 2);
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
        //we need to fix the shape on repaints
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

        //possibly update the ui
        contentLabel.setBorder(new LineBorder(
                CyderColors.guiThemeColor, 3, false));
        getContentPane().repaint();
        getTrueContentPane().repaint();
        getTopDragLabel().repaint();
        getLeftDragLabel().repaint();
        getBottomDragLabel().repaint();
        getRightDragLabel().repaint();

        super.repaint();
    }

    /**
     * Action to perform upon a minimiztion request from the minimize button
     * @param actionListener - the action to take
     */
    public void addMinimizeListener(ActionListener actionListener) {
        actionListener.actionPerformed(null);
    }

    private LinkedList<PreCloseAction> preCloseActions = new LinkedList<>();

    /**
     * Performs the given action right before closing the frame. This action is invoked right before an animation
     * and sequential dispose call. Usage:
     *
     *   class action implements PreCloseAction {
     *         {@code @Override}
     *         public void invokeAction() {
     *             //logic here
     *         }
     *   }
     *
     *     addPreCloseAction(new action());
     *
     * @param action - the action to perform before closing/disposing
     */
    public void addPreCloseAction(PreCloseAction action) {
        preCloseActions.add(action);
    }

    //interface to be used for preCloseActions
    public interface PreCloseAction {
        void invokeAction();
    }

    private String closingConfirmationMessage = null;

    /**
     * Displays a confirmation dialog to the user to confirm whether or not they intended to exit the frame
     * @param message - the message to display to the user
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
     * @param b - the value determining whether or not the frame is always on top
     */
    public void setPinned(boolean b) {
        this.pinned = b;
        setAlwaysOnTop(this.pinned);
    }

    /**
     * Standard getter for pinned boolean.
     * @return - the boolean of pinned
     */
    public boolean getPinned() {
        return this.pinned;
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
}