package cyder.ui;

import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.enums.*;
import cyder.handler.ErrorHandler;
import cyder.utilities.ImageUtil;
import cyder.utilities.SystemUtil;
import cyder.widgets.GenericInform;
import org.jsoup.Jsoup;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class CyderFrame extends JFrame {

    private TitlePosition titlePosition = TitlePosition.LEFT;

    private int width;
    private int height;

    private ImageIcon background;
    private DragLabel dl;
    private JLabel titleLabel;

    private JLabel contentLabel;

    /**
     * returns an instance of a cyderframe which extends JFrame with the specified with and height and a drag label with minimize and close buttons
     * the specified ImageIcon is used for the background (you can enable resizing and rescaling of the image should you choose)
     *
     * @param width  - the specified width of the cyder frame
     * @param height - the specified height of the cyder frame
     */
    public CyderFrame(int width, int height, ImageIcon background) {
        this.width = width;
        this.height = height;
        this.background = background;
        setSize(new Dimension(width, height));

        setResizable(false);
        setUndecorated(true);
        setBackground(CyderColors.navy);
        setIconImage(SystemUtil.getCyderIcon().getImage());

        contentLabel = new JLabel();
        contentLabel.setBorder(new LineBorder(CyderColors.navy, 5, false));
        contentLabel.setIcon(background);
        currentOrigIcon = background;
        setContentPane(contentLabel);

        dl = new DragLabel(width, 30, this);
        dl.setBounds(0, 0, width, 30);
        contentLabel.add(dl);

        titleLabel = new JLabel("");
        titleLabel.setFont(CyderFonts.weatherFontSmall.deriveFont(20f));
        titleLabel.setForeground(CyderColors.vanila);

        dl.add(titleLabel);
    }

    /**
     * returns an instance of a cyderframe which extends JFrame with the specified with and height and a drag label with minimize and close buttons
     *
     * @param width  - the specified width of the cyder frame
     * @param height - the specified height of the cyder frame
     */
    public CyderFrame(int width, int height) {
        BufferedImage im = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = im.createGraphics();
        g.setPaint(new Color(238, 238, 238));
        g.fillRect(0, 0, 1, 1);

        this.width = width;
        this.height = height;
        this.background = new ImageIcon(im);
        this.currentOrigIcon = this.background;
        setSize(new Dimension(width, height));

        setResizable(false);
        setUndecorated(true);
        setBackground(CyderColors.navy);
        setIconImage(SystemUtil.getCyderIcon().getImage());

        JLabel parentLabel = new JLabel();
        parentLabel.setBorder(new LineBorder(CyderColors.navy, 5, false));
        parentLabel.setIcon(background);
        setContentPane(parentLabel);

        dl = new DragLabel(width, 30, this);
        dl.setBounds(0, 0, width, 30);
        parentLabel.add(dl);
    }

    /**
     * returns an instance of a cyderframe which extends JFrame with a width of 400 and a height of 400 and a drag label with minimize and close buttons
     */
    public CyderFrame() {
        int width = 400, height = 400;

        BufferedImage im = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = im.createGraphics();
        g.setPaint(new Color(238, 238, 238));
        g.fillRect(0, 0, 1, 1);

        this.width = width;
        this.height = height;
        this.background = new ImageIcon(im);
        this.currentOrigIcon = this.background;
        setSize(new Dimension(width, height));

        setResizable(false);
        setUndecorated(true);
        setBackground(CyderColors.navy);
        setIconImage(SystemUtil.getCyderIcon().getImage());

        JLabel parentLabel = new JLabel();
        parentLabel.setBorder(new LineBorder(CyderColors.navy, 5, false));
        parentLabel.setIcon(background);
        setContentPane(parentLabel);

        dl = new DragLabel(width, 30, this);
        dl.setBounds(0, 0, width, 30);
        parentLabel.add(dl);
    }

    /**
     * This method will change the title position to the specified value. If the frame is visible to the user,
     * we will animate the change via a smooth slide transition
     * @param titlePosition the position for the title to be: left, center
     */
    public void setTitlePosition(TitlePosition titlePosition) {
        if (titlePosition == null || this.titlePosition == null)
            return;

        boolean different = titlePosition != this.titlePosition;
        this.titlePosition = titlePosition;
        long timeout = 2;

        if (different && isVisible()) {
            if (titlePosition != TitlePosition.CENTER) {
                new Thread(() -> {
                    for (int i = (getDragLabel().getWidth() / 2) - (getTitleWidth(titleLabel.getText()) / 2); i > 4; i--) {
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
                    for (int i = 5; i < (getDragLabel().getWidth() / 2) - (getTitleWidth(titleLabel.getText()) / 2) + 1; i++) {
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

    //getter for title position
    public TitlePosition getTitlePosition() {
        return this.titlePosition;
    }

    //getter and setter for paint windowed title

    private boolean paintSuperTitle = true;
    public void setPaintSuperTitle(boolean b) {
        paintSuperTitle = b;
    }

    public boolean getPaintSuperTitle() {
        return paintSuperTitle;
    }

    //override since we paint it on the window no matter what in whatever position we want
    //but also paint the windowed title if paintSuperTitle
    @Override
    public void setTitle(String title) {
        super.setTitle(paintSuperTitle ? title : "");

        titleLabel.setText(title);

        switch (titlePosition) {
            case CENTER:
                titleLabel.setBounds((getDragLabel().getWidth() / 2) - (getTitleWidth(title) / 2), 2, getTitleWidth(title), 25);
                break;

            default:
                titleLabel.setBounds(5, 2, getTitleWidth(title), 25);
        }
    }

    //using default (notification font) to determine the width and add 10 due to a bug
    private int getTitleWidth(String title) {
        Font notificationFont = titleLabel.getFont();
        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform, true, true);
        return (int) notificationFont.getStringBounds(title, frc).getWidth() + 10;
    }

    //using any font to determine the width and add 10 due to a bug
    public static int getTitleWidth(String title, Font f) {
        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform, true, true);
        return (int) f.getStringBounds(title, frc).getWidth() + 10;
    }

    /**
     * This method is to be used for a quick notify. view direction is five seconds
     * @param htmlText - the text you want to notify on the callilng from
     */
    public void notify(String htmlText) {
        notify(htmlText, 5000, ArrowDirection.TOP);
    }

    //notify method given text, duration, and direction (arrow direction determines start and vanish dir so a simplified method here)
    public void notify(String htmltext, int viewDuration, ArrowDirection direction) {
        Notification frameNotification = new Notification();

        StartDirection startDir;
        VanishDirection vanishDir;

        switch (direction) {
            case LEFT:
                startDir = StartDirection.LEFT;
                vanishDir = VanishDirection.LEFT;
                break;
            case RIGHT:
                startDir = StartDirection.RIGHT;
                vanishDir = VanishDirection.RIGHT;
                break;
            case BOTTOM:
                startDir = StartDirection.BOTTOM;
                vanishDir = VanishDirection.BOTTOM;
                break;
            default:
                startDir = StartDirection.TOP;
                vanishDir = VanishDirection.TOP;
                break;
        }

        frameNotification.setArrow(direction);

        JLabel text = new JLabel();
        text.setText(htmltext);

        int w = 0;

        Font notificationFont = CyderFonts.weatherFontSmall;
        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform, true, true);

        htmltext = Jsoup.parse(htmltext.replaceAll("(?i)<br[^>]*>", "br2n")).text().replaceAll("br2n", "\n");

        String[] parts = htmltext.split("\\r?\\n");

        for (String part : parts) {
            Rectangle2D stringBounds = notificationFont.getStringBounds(part.replaceAll("<[^>]+>", ""), frc);
            if ((int) stringBounds.getWidth() > w)
                w = (int) stringBounds.getWidth();
        }


        int heightIncrement = (int) notificationFont.getStringBounds("string", frc).getHeight();
        int h = heightIncrement;
        int lastIndex = 0;

        while (lastIndex != -1) {
            lastIndex = text.getText().indexOf("<br/>", lastIndex);

            if (lastIndex != -1) {
                h += heightIncrement;
                lastIndex += "<br/>".length();
            }
        }

        frameNotification.setWidth(w);
        frameNotification.setHeight(h);

        text.setFont(notificationFont);
        text.setForeground(CyderColors.navy);
        text.setBounds(14, 10, w * 2, h);
        frameNotification.add(text);

        if (startDir == StartDirection.LEFT)
            frameNotification.setBounds(0, 30, w * 2, h * 2);
        else if (startDir == StartDirection.RIGHT)
            frameNotification.setBounds(this.getContentPane().getWidth() - (w + 30), 32, w * 2, h * 2);
        else
            frameNotification.setBounds(this.getContentPane().getWidth() / 2 - (w / 2), 32, w * 2, h * 2);

        this.getContentPane().add(frameNotification, 1, 0);
        this.getContentPane().repaint();

        frameNotification.appear(startDir, this.getContentPane());
        frameNotification.vanish(vanishDir, this.getContentPane(), viewDuration);
    }

    //full control over notify method
    //html text - the text you want to display
    //view duration - timeout before notification fades away
    //arrowdir - where the arrow should go on the notification
    //startdir - where the notification should enter from
    //vanishdir - where the notification should exit
    public void notify(String htmltext, int viewDuration, ArrowDirection arrowDir, StartDirection startDir, VanishDirection vanishDir) {
        Notification frameNotification = new Notification();

        frameNotification.setArrow(arrowDir);

        JLabel text = new JLabel();
        text.setText(htmltext);

        int w = 0;

        Font notificationFont = CyderFonts.weatherFontSmall;
        AffineTransform affinetransform = new AffineTransform();
        FontRenderContext frc = new FontRenderContext(affinetransform, true, true);

        htmltext = Jsoup.parse(htmltext.replaceAll("(?i)<br[^>]*>", "br2n")).text().replaceAll("br2n", "\n");
        String[] parts = htmltext.split("\\r?\\n");

        for (String part : parts) {
            Rectangle2D stringBounds = notificationFont.getStringBounds(part.replaceAll("<[^>]+>", ""), frc);
            if ((int) stringBounds.getWidth() > w)
                w = (int) stringBounds.getWidth();
        }

        int heightIncrement = (int) notificationFont.getStringBounds("string", frc).getHeight();
        int h = heightIncrement;
        int lastIndex = 0;

        while (lastIndex != -1) {

            lastIndex = text.getText().indexOf("<br/>", lastIndex);

            if (lastIndex != -1) {
                h += heightIncrement;
                lastIndex += "<br/>".length();
            }
        }

        frameNotification.setWidth(w);
        frameNotification.setHeight(h);

        text.setFont(notificationFont);
        text.setForeground(CyderColors.navy);
        text.setBounds(14, 10, w * 2, h);
        frameNotification.add(text);

        if (startDir == StartDirection.LEFT)
            frameNotification.setBounds(0, 30, w * 2, h * 2);
        else if (startDir == StartDirection.RIGHT)
            frameNotification.setBounds(this.getContentPane().getWidth() - (w + 30), 32, w * 2, h * 2);
        else
            frameNotification.setBounds(this.getContentPane().getWidth() / 2 - (w / 2), 32, w * 2, h * 2);

        this.getContentPane().add(frameNotification, 1, 0);
        this.getContentPane().repaint();

        frameNotification.appear(startDir, this.getContentPane());
        frameNotification.vanish(vanishDir, this.getContentPane(), viewDuration);
    }

    //getter for drag label, needed for window resizing
    public DragLabel getDragLabel() {
        return dl;
    }

    //inform window relative to this
    public void inform(String text, String title) {
        GenericInform.informRelative(text, title, this);
    }

    //enter animation for this frame
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
     * close animation moves the window up until the CyderFrame is off screen. this.dispose() is then invoked
     * perhaps you might re-write this to override dispose so that closeanimation is always called and you can
     * simply dispose of a frame like normal
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

    //minimize animation for this
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

    //disable moving of the window
    public void setRelocatable(boolean relocatable) {
        if (relocatable)
            dl.enableDragging();
        else
            dl.disableDragging();
    }

    //move around the screen border
    public void dance() {
        Thread DanceThread = new Thread(() -> {
            try {
                int delay = 10;

                setAlwaysOnTop(true);
                setLocationRelativeTo(null);

                Point point = getLocationOnScreen();

                int x = (int) point.getX();
                int y = (int) point.getY();

                int restoreX = x;
                int restoreY = y;

                for (int i = y; i <= (-getHeight()); i += 10) {
                    Thread.sleep(delay);
                    setLocation(x, i);
                }

                setLocation(SystemUtil.getScreenWidth() / 2 - getWidth() / 2, SystemUtil.getScreenHeight() - getHeight());
                point = getLocationOnScreen();
                x = (int) point.getX();
                y = (int) point.getY();

                for (int i = x; i <= (SystemUtil.getScreenWidth() - getWidth()); i += 10) {
                    Thread.sleep(delay);
                    setLocation(i, y);
                }

                setLocation(SystemUtil.getScreenWidth() - getWidth(), SystemUtil.getScreenHeight() - getHeight());
                point = getLocationOnScreen();
                x = (int) point.getX();
                y = (int) point.getY();

                for (int i = y; i >= -10; i -= 10) {
                    Thread.sleep(delay);
                    setLocation(x, i);
                }

                setLocation(SystemUtil.getScreenWidth() - getWidth(), 0);
                point = getLocationOnScreen();
                x = (int) point.getX();
                y = (int) point.getY();

                for (int i = x; i >= 10; i -= 10) {
                    Thread.sleep(delay);
                    setLocation(i, y);
                }

                setLocation(0, 0);
                point = getLocationOnScreen();
                x = (int) point.getX();
                y = (int) point.getY();

                for (int i = y; i <= (SystemUtil.getScreenHeight() - getHeight()); i += 10) {
                    Thread.sleep(delay);
                    setLocation(x, i);
                }

                setLocation(0, SystemUtil.getScreenHeight() - getHeight());
                point = getLocationOnScreen();
                x = (int) point.getX();
                y = (int) point.getY();

                for (int i = x; i <= (SystemUtil.getScreenWidth() / 2 - getWidth() / 2); i += 10) {
                    Thread.sleep(delay);
                    setLocation(i, y);
                }

                setLocation(SystemUtil.getScreenWidth() / 2 - getWidth() / 2, SystemUtil.getScreenHeight() - getHeight());
                int acc = getY();
                x = getX();

                while (getY() >= (SystemUtil.getScreenHeight() / 2 - getHeight() / 2)) {
                    Thread.sleep(delay);
                    acc -= 10;
                    setLocation(x, acc);
                }

                setLocation(restoreX, restoreY);
                setAlwaysOnTop(false);

            } catch (Exception e) {
                ErrorHandler.handle(e);
            }
        });

        DanceThread.start();
    }

    //transform content pane in a barrel roll like fashion
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

    //similar to googling "askew"
    public void askew(int degrees) {
        ((JLabel) (this.getContentPane())).setIcon(new ImageIcon(ImageUtil.rotateImageByDegrees(
                ImageUtil.getRotatedImage(
                        ConsoleFrame.getCurrentBackgroundFile().getAbsolutePath(),
                        ConsoleFrame.getConsoleDirection()), degrees)));
    }

    //override since we also need to change drag label's bounds
    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        if (getDragLabel() != null) {
            getDragLabel().setWidth(width);
            setTitle(getTitle());
        }
    }

    //getters and setters for min size, max size, and snap size

    private Dimension minimumSize = new Dimension(200, 200);
    private Dimension maximumSize = new Dimension(800, 800);
    private Dimension snapSize = new Dimension(1, 1);

    public void setMinimumSize(Dimension minSize) {
        this.minimumSize = minSize;
        cr.setMinimumSize(minimumSize);
    }

    public void setMaximumSize(Dimension maxSize) {
        this.maximumSize = maxSize;
        cr.setMaximumSize(maximumSize);
    }

    public void setSnapSize(Dimension snap) {
        this.snapSize = snap;
        cr.setSnapSize(snapSize);
    }

    public Dimension getMinimumSize() {
        return minimumSize;
    }

    public Dimension getMaximumSize() {
        return maximumSize;
    }

    public Dimension getSnapSize() {
        return snapSize;
    }

    ComponentResizer cr;

    public void setResizing(Boolean b) {
        cr.setResizing(b);
    }

    public void setBackgroundResizing(Boolean b) {
        cr.setBackgroundRefreshOnResize(b);
    }

    //setup for resizing the window, generallythis is the order to follow
    public void initResizing() {
        cr = new ComponentResizer();
        cr.registerComponent(this);
        cr.setResizing(true);
        cr.setMinimumSize(getMinimumSize());
        cr.setMaximumSize(getMaximumSize());
        cr.setSnapSize(getSnapSize());
    }

    //disable or enable window resizing
    public void allowResizing(Boolean b) {
        cr.setResizing(b);
    }

    //should we resize the background when we resize the window or just keep it in the same place?
    public void backgroundRefreshOnResize(Boolean b) {
        cr.setBackgroundRefreshOnResize(b);
    }

    ImageIcon currentOrigIcon;

    //refreshing will cause the background image to scale to the width and height of this
    public void refreshBackground() {
        contentLabel.setIcon(new ImageIcon(currentOrigIcon.getImage()
                .getScaledInstance(contentLabel.getWidth(), contentLabel.getHeight(), Image.SCALE_DEFAULT)));
    }

    //changing the background will cause the background image to scale to the width and height of this
    public void setBackground(ImageIcon icon) {
        currentOrigIcon = icon;
        contentLabel.setIcon(new ImageIcon(currentOrigIcon.getImage()
                .getScaledInstance(contentLabel.getWidth(), contentLabel.getHeight(), Image.SCALE_DEFAULT)));
    }
}