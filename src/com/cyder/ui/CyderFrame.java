package com.cyder.ui;

import com.cyder.utilities.Util;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

public class CyderFrame extends JFrame {

    public static final int LEFT_TITLE = 0;
    public static final int CENTER_TITLE = 1;
    private int titlePosition = 0;

    private Util fUtil = new Util();
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
        setIconImage(fUtil.getCyderIcon().getImage());

        JLabel parentLabel = new JLabel();
        parentLabel.setBorder(new LineBorder(fUtil.navy, 5, false));
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
        setIconImage(fUtil.getCyderIcon().getImage());

        JLabel parentLabel = new JLabel();
        parentLabel.setBorder(new LineBorder(fUtil.navy, 5, false));
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
        titleLabel = new JLabel(title);
        titleLabel.setFont(fUtil.weatherFontSmall.deriveFont(20f));
        titleLabel.setForeground(fUtil.vanila);

        if (titlePosition == 1) {
            int halfLen = ((int) Math.ceil(14 * title.length())) / 2;

            titleLabel.setBounds((int) Math.floor(5 + (width / 2.0)) - halfLen, 2, halfLen * 2, 25);
        } else {
            titleLabel.setBounds(5, 2, ((int) Math.ceil(14 * title.length())), 25);
        }

        dl.add(titleLabel);
    }
}
