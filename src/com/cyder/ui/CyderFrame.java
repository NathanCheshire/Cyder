package com.cyder.ui;

import com.cyder.utilities.GeneralUtil;
import com.cyder.utilities.ImageUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

public class CyderFrame extends JFrame {

    public static final int LEFT_TITLE = 0;
    public static final int CENTER_TITLE = 1;
    private int titlePosition = 0;

    private GeneralUtil fGeneralUtil = new GeneralUtil();
    private ImageUtil iu = new ImageUtil();
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
        setIconImage(fGeneralUtil.getCyderIcon().getImage());

        JLabel parentLabel = new JLabel();
        parentLabel.setBorder(new LineBorder(fGeneralUtil.navy, 5, false));
        parentLabel.setIcon(background);
        setContentPane(parentLabel);

        dl = new DragLabel(width, 30, this);
        dl.setBounds(0, 0, width, 30);
        parentLabel.add(dl);
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
        setIconImage(fGeneralUtil.getCyderIcon().getImage());

        JLabel parentLabel = new JLabel();
        parentLabel.setBorder(new LineBorder(fGeneralUtil.navy, 5, false));
        parentLabel.setIcon(background);
        setContentPane(parentLabel);

        dl = new DragLabel(width, 30, this);
        dl.setBounds(0, 0, width, 30);
        parentLabel.add(dl);
    }

    public void setTitlePosition(int titlePosition) {
        this.titlePosition = titlePosition;
    }

    @Override
    public void setTitle(String title) {
        super.setTitle(title);
        titleLabel = new JLabel(title);
        titleLabel.setFont(fGeneralUtil.weatherFontSmall.deriveFont(20f));
        titleLabel.setForeground(fGeneralUtil.vanila);

        if (titlePosition == 1) {
            int halfLen = ((int) Math.ceil(14 * title.length())) / 2;

            titleLabel.setBounds((int) Math.floor(5 + (width / 2.0)) - halfLen, 2, halfLen * 4, 25);
        } else {
            titleLabel.setBounds(5, 2, ((int) Math.ceil(14 * title.length())), 25);
        }

        dl.add(titleLabel);
    }

    public void notify(String htmltext, int delay, int arrowDir, int startDir, int vanishDir, int width) {
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

        text.setFont(fGeneralUtil.weatherFontSmall);
        text.setForeground(fGeneralUtil.navy);
        text.setBounds(14,10,w * 2,h);
        frameNotification.add(text);

        if (startDir == Notification.LEFT_START)
            frameNotification.setBounds(0,30,w * 2,h * 2);
        else if (startDir == Notification.RIGHT_START)
            frameNotification.setBounds(this.getContentPane().getWidth() - (w + 30),32,w * 2,h * 2);
        else
            frameNotification.setBounds(this.getContentPane().getWidth() / 2 - (w / 2),32,w * 2,h * 2);

        this.getContentPane().add(frameNotification,1,0);
        this.getContentPane().repaint();

        frameNotification.vanish(vanishDir, this.getContentPane(), delay);
    }

    public void inform(String text, String title, int width, int height) {
        try {
            BufferedImage back = iu.imageFromColor(width,height,iu.getDominantColor(ImageIO.read(fGeneralUtil.getCurrentBackground())));

            CyderFrame informFrame = new CyderFrame(width,height,new ImageIcon(back));
            informFrame.setTitle(title);

            JLabel desc = new JLabel("<html><div style='text-align: center;'>" + text + "</div></html>");

            desc.setHorizontalAlignment(JLabel.CENTER);
            desc.setVerticalAlignment(JLabel.CENTER);
            ImageUtil iu = new ImageUtil();
            desc.setForeground(iu.getDominantColorOpposite(back));
            desc.setFont(fGeneralUtil.weatherFontSmall.deriveFont(22f));
            desc.setBounds(10, 35, width - 20, height - 35 * 2);

            informFrame.getContentPane().add(desc);

            informFrame.setVisible(true);
            informFrame.setLocationRelativeTo(this);
            informFrame.setAlwaysOnTop(true);
        }

        catch (Exception e) {
            fGeneralUtil.handle(e);
        }
    }
}
