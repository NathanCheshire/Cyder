package com.cyder.ui;

import com.cyder.Constants.CyderColors;
import com.cyder.Constants.CyderFonts;
import com.cyder.enums.*;
import com.cyder.handler.ErrorHandler;
import com.cyder.utilities.ImageUtil;
import com.cyder.utilities.SystemUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

public class CyderFrame extends JFrame {

    private TitlePosition titlePosition = TitlePosition.LEFT;

    private SystemUtil systemUtil = new SystemUtil();

    private int width;
    private int height;

    private ImageIcon background;
    private DragLabel dl;
    private JLabel titleLabel;

    public CyderFrame(int width, int height, ImageIcon background) {
        this.width = width;
        this.height = height;
        this.background = background;
        setSize(new Dimension(width, height));

        setResizable(false);
        setUndecorated(true);
        setIconImage(systemUtil.getCyderIcon().getImage());

        JLabel parentLabel = new JLabel();
        parentLabel.setBorder(new LineBorder(CyderColors.navy, 5, false));
        parentLabel.setIcon(background);
        setContentPane(parentLabel);

        dl = new DragLabel(width, 30, this);
        dl.setBounds(0, 0, width, 30);
        parentLabel.add(dl);

        titleLabel = new JLabel("");
        titleLabel.setFont(CyderFonts.weatherFontSmall.deriveFont(20f));
        titleLabel.setForeground(CyderColors.vanila);

        dl.add(titleLabel);
    }

    public CyderFrame(int width, int height) {
        BufferedImage im = new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB);
        Graphics2D g = im.createGraphics();
        g.setPaint(new Color(238,238,238));
        g.fillRect(0,0,1,1);

        this.width = width;
        this.height = height;
        this.background = new ImageIcon(im);
        setSize(new Dimension(width, height));

        setResizable(false);
        setUndecorated(true);
        setIconImage(systemUtil.getCyderIcon().getImage());

        JLabel parentLabel = new JLabel();
        parentLabel.setBorder(new LineBorder(CyderColors.navy, 5, false));
        parentLabel.setIcon(background);
        setContentPane(parentLabel);

        dl = new DragLabel(width, 30, this);
        dl.setBounds(0, 0, width, 30);
        parentLabel.add(dl);
    }

    public void setTitlePosition(TitlePosition titlePosition) {
        this.titlePosition = titlePosition;
        setTitle(this.getTitle());
    }

    @Override
    public void setTitle(String title) {
        super.setTitle(title);
        titleLabel.setText(title);

        if (titlePosition == TitlePosition.CENTER) {
            int halfLen = ((int) Math.ceil(14 * title.length())) / 2;

            titleLabel.setBounds((int) Math.floor(5 + (width / 2.0)) - halfLen, 2, halfLen * 4, 25);
        } else {
            titleLabel.setBounds(5, 2, ((int) Math.ceil(16 * title.length())), 25);
        }
    }

    public void notify(String htmltext, int viewDuration, ArrowDirection direction, int width) {
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

        int w = width;
        int h = 30;

        frameNotification.setArrow(direction);

        JLabel text = new JLabel();
        text.setText(htmltext);

        int lastIndex = 0;

        while(lastIndex != -1){

            lastIndex = text.getText().indexOf("<br/>",lastIndex);

            if(lastIndex != -1){
                h += 30;
                lastIndex += "<br/>".length();
            }
        }

        frameNotification.setWidth(w);
        frameNotification.setHeight(h);

        text.setFont(CyderFonts.weatherFontSmall);
        text.setForeground(CyderColors.navy);
        text.setBounds(14,10,w * 2,h);
        frameNotification.add(text);

        if (startDir == StartDirection.LEFT)
            frameNotification.setBounds(0,30,w * 2,h * 2);
        else if (startDir == StartDirection.RIGHT)
            frameNotification.setBounds(this.getContentPane().getWidth() - (w + 30),32,w * 2,h * 2);
        else
            frameNotification.setBounds(this.getContentPane().getWidth() / 2 - (w / 2),32,w * 2,h * 2);

        this.getContentPane().add(frameNotification,1,0);
        this.getContentPane().repaint();

        frameNotification.appear(startDir, this.getContentPane());
        frameNotification.vanish(vanishDir, this.getContentPane(), viewDuration);
    }

    public void notify(String htmltext, int viewDuration, ArrowDirection arrowDir, StartDirection startDir, VanishDirection vanishDir, int width) {
        Notification frameNotification = new Notification();

        int w = width;
        int h = 30;

        frameNotification.setArrow(arrowDir);

        JLabel text = new JLabel();
        text.setText(htmltext);

        int lastIndex = 0;

        while(lastIndex != -1){

            lastIndex = text.getText().indexOf("<br/>",lastIndex);

            if(lastIndex != -1){
                h += 30;
                lastIndex += "<br/>".length();
            }
        }

        frameNotification.setWidth(w);
        frameNotification.setHeight(h);

        text.setFont(CyderFonts.weatherFontSmall);
        text.setForeground(CyderColors.navy);
        text.setBounds(14,10,w * 2,h);
        frameNotification.add(text);

        if (startDir == StartDirection.LEFT)
            frameNotification.setBounds(0,30,w * 2,h * 2);
        else if (startDir == StartDirection.RIGHT)
            frameNotification.setBounds(this.getContentPane().getWidth() - (w + 30),32,w * 2,h * 2);
        else
            frameNotification.setBounds(this.getContentPane().getWidth() / 2 - (w / 2),32,w * 2,h * 2);

        this.getContentPane().add(frameNotification,1,0);
        this.getContentPane().repaint();

        frameNotification.appear(startDir, this.getContentPane());
        frameNotification.vanish(vanishDir, this.getContentPane(), viewDuration);
    }

    public DragLabel getDragLabel() {
        return dl;
    }

    public void inform(String text, String title, int width, int height) {
        try {
            CyderFrame informFrame = new CyderFrame(width,height,new ImageIcon(new ImageUtil().imageFromColor(width,height,CyderColors.vanila)));
            informFrame.setTitle(title);

            JLabel desc = new JLabel("<html><div style='text-align: center;'>" + text + "</div></html>");

            desc.setHorizontalAlignment(JLabel.CENTER);
            desc.setVerticalAlignment(JLabel.CENTER);
            ImageUtil iu = new ImageUtil();
            desc.setForeground(CyderColors.navy);
            desc.setFont(CyderFonts.weatherFontSmall.deriveFont(22f));
            desc.setBounds(10, 35, width - 20, height - 35 * 2);

            informFrame.getContentPane().add(desc);

            informFrame.setVisible(true);
            informFrame.setLocationRelativeTo(this);
            informFrame.setAlwaysOnTop(true);
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void enterAnimation() {
        if (this == null)
            return;

        dl.disableDragging();

        this.setVisible(false);
        this.setLocationRelativeTo(null);

        int to = this.getY();
        this.setLocation(this.getX(), 0 - this.getHeight());

        this.setVisible(true);

        for (int i = 0 - this.getHeight() ; i < to ; i+= 15) {
            this.setLocation(this.getX(), i);
            try {
                Thread.sleep(1);
            }

            catch (Exception e) {
                ErrorHandler.handle(e);
            }
        }

        this.setLocationRelativeTo(null);

        dl.enableDragging();
    }

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
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    public void minimizeAnimation() {
        if (this == null)
            return;

        dl.disableDragging();

        Point point = this.getLocationOnScreen();
        int x = (int) point.getX();
        int y = (int) point.getY();

        try {
            for (int i = y; i <= systemUtil.getScreenHeight(); i += 15) {
                Thread.sleep(1);
                this.setLocation(x, i);
            }

            this.setState(JFrame.ICONIFIED);
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }

        dl.enableDragging();
    }

    public void setRelocatable(boolean relocatable) {
        if (relocatable)
            dl.enableDragging();
        else
            dl.disableDragging();
    }

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

                setLocation(systemUtil.getScreenWidth() / 2 - getWidth() / 2, systemUtil.getScreenHeight() - getHeight());
                point = getLocationOnScreen();
                x = (int) point.getX();
                y = (int) point.getY();

                for (int i = x; i <= (systemUtil.getScreenWidth() - getWidth()); i += 10) {
                    Thread.sleep(delay);
                    setLocation(i, y);
                }

                setLocation(systemUtil.getScreenWidth() - getWidth(), systemUtil.getScreenHeight() - getHeight());
                point = getLocationOnScreen();
                x = (int) point.getX();
                y = (int) point.getY();

                for (int i = y; i >= -10; i -= 10) {
                    Thread.sleep(delay);
                    setLocation(x, i);
                }

                setLocation(systemUtil.getScreenWidth() - getWidth(), 0);
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

                for (int i = y; i <= (systemUtil.getScreenHeight() - getHeight()); i += 10) {
                    Thread.sleep(delay);
                    setLocation(x, i);
                }

                setLocation(0, systemUtil.getScreenHeight() - getHeight());
                point = getLocationOnScreen();
                x = (int) point.getX();
                y = (int) point.getY();

                for (int i = x; i <= (systemUtil.getScreenWidth() / 2 - getWidth() / 2); i += 10) {
                    Thread.sleep(delay);
                    setLocation(i, y);
                }

                setLocation(systemUtil.getScreenWidth() / 2 - getWidth() / 2, systemUtil.getScreenHeight() - getHeight());
                int acc = getY();
                x = getX();

                while (getY() >= (systemUtil.getScreenHeight() / 2 - getHeight() / 2)) {
                    Thread.sleep(delay);
                    acc -= 10;
                    setLocation(x, acc);
                }

                setLocation(restoreX, restoreY);
                setAlwaysOnTop(false);

            }

            catch (Exception e) {
                ErrorHandler.handle(e);
            }
        });

        DanceThread.start();
    }

    //todo remove barrel rolling from anywhere else

    //todo when setting background for cyderframe, resize image to size first
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
}
