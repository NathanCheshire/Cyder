package com.cyder.utilities;

import com.cyder.ui.CyderButton;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.net.URL;

//todo remove swing dependency

public class YouTubeThumbnail {
    private JFrame yttnFrame;
    private CyderButton getYTTN;
    private JTextField yttnField;

    private int xMouse;
    private int yMouse;

    private GeneralUtil yttnGeneralUtil = new GeneralUtil();

    public YouTubeThumbnail() {
        if (yttnFrame != null)
            yttnGeneralUtil.closeAnimation(yttnFrame);

        yttnFrame = new JFrame();

        yttnFrame.setResizable(false);

        yttnFrame.setTitle("YouTube Thumbnail");

        yttnFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        yttnFrame.setIconImage(yttnGeneralUtil.getCyderIcon().getImage());

        JPanel ParentPanel = new JPanel();

        ParentPanel.setLayout(new BoxLayout(ParentPanel,BoxLayout.Y_AXIS));

        JLabel VideoID = new JLabel("Enter a valid YouTube video ID");

        VideoID.setFont(yttnGeneralUtil.weatherFontBig);

        VideoID.setForeground(yttnGeneralUtil.navy);

        JPanel TopPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        TopPanel.add(VideoID);

        ParentPanel.add(TopPanel);

        yttnField = new JTextField(30);

        yttnField.setBorder(new LineBorder(yttnGeneralUtil.navy,5,false));

        yttnField.addActionListener(e -> getYTTN.doClick());

        yttnField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                if(yttnField.getText().length() >= 11 && !(evt.getKeyChar()== KeyEvent.VK_DELETE || evt.getKeyChar() == KeyEvent.VK_BACK_SPACE)) {
                    yttnGeneralUtil.beep();
                    evt.consume();
                }
            }
        });

        yttnField.addKeyListener(new java.awt.event.KeyAdapter() {
            char[] ValidChars = {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
                    'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','0','1','2',
                    '3','4','5','6','7','8','9','-','_'};

            public void keyTyped(java.awt.event.KeyEvent evt) {
                boolean InArray = false;

                for (char c : ValidChars) {
                    if (c == evt.getKeyChar()) {
                        InArray = true;
                        break;
                    }
                }

                if (!InArray) {
                    evt.consume();
                }
            }
        });

        yttnField.setFont(yttnGeneralUtil.weatherFontSmall);

        yttnField.setBorder(new LineBorder(yttnGeneralUtil.navy,5,false));

        JPanel MiddlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        MiddlePanel.add(yttnField);

        ParentPanel.add(MiddlePanel);

        getYTTN = new CyderButton("Get Thumbnail");

        getYTTN.setBorder(new LineBorder(yttnGeneralUtil.navy,5,false));

        getYTTN.setColors(yttnGeneralUtil.regularRed);

        getYTTN.setFocusPainted(false);

        getYTTN.setBackground(yttnGeneralUtil.regularRed);

        getYTTN.setFont(yttnGeneralUtil.weatherFontSmall);

        getYTTN.addActionListener(e -> {
            yttnGeneralUtil.closeAnimation(yttnFrame);

            yttnFrame.dispose();

            String YouTubeID = yttnField.getText();

            if (YouTubeID.length() < 11) {
                yttnGeneralUtil.inform("Sorry, " + yttnGeneralUtil.getUsername() + ", but that's not a valid YouTube video ID.","", 400, 200);
            }

            else {
                String YouTubeURL = "https://img.youtube.com/vi/REPLACE/hqdefault.jpg";
                YouTubeURL = YouTubeURL.replace("REPLACE", YouTubeID);
                URL url;

                try {
                    url = new URL(YouTubeURL);

                    BufferedImage Thumbnail = ImageIO.read(url);

                    JFrame thumbnailFrame = new JFrame();

                    thumbnailFrame.setUndecorated(true);

                    thumbnailFrame.setTitle(YouTubeID);

                    thumbnailFrame.addMouseMotionListener(new MouseMotionListener() {
                        @Override
                        public void mouseDragged(MouseEvent e) {
                            int x = e.getXOnScreen();
                            int y = e.getYOnScreen();

                            if (thumbnailFrame != null && thumbnailFrame.isFocused()) {
                                thumbnailFrame.setLocation(x - xMouse, y - yMouse);
                            }
                        }

                        @Override
                        public void mouseMoved(MouseEvent e) {
                            xMouse = e.getX();
                            yMouse = e.getY();
                        }
                    });

                    thumbnailFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

                    JPanel parentPanel = new JPanel();

                    parentPanel.setBorder(new LineBorder(yttnGeneralUtil.navy,10,false));

                    parentPanel.setLayout(new BorderLayout());

                    thumbnailFrame.setContentPane(parentPanel);

                    JLabel PictureLabel = new JLabel(new ImageIcon(Thumbnail));

                    PictureLabel.setToolTipText("Open video " + YouTubeID);

                    PictureLabel.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            yttnGeneralUtil.internetConnect("https://www.youtube.com/watch?v=" + YouTubeID);
                        }
                    });

                    parentPanel.add(PictureLabel, BorderLayout.PAGE_START);

                    CyderButton closeYT = new CyderButton("Close");

                    closeYT.setColors(yttnGeneralUtil.regularRed);

                    closeYT.setBorder(new LineBorder(yttnGeneralUtil.navy,5,false));

                    closeYT.setFocusPainted(false);

                    closeYT.setBackground(yttnGeneralUtil.regularRed);

                    closeYT.setFont(yttnGeneralUtil.weatherFontSmall);

                    closeYT.addActionListener(ev -> yttnGeneralUtil.closeAnimation(thumbnailFrame));

                    closeYT.setSize(thumbnailFrame.getX(),20);

                    parentPanel.add(closeYT,BorderLayout.PAGE_END);

                    parentPanel.repaint();

                    thumbnailFrame.pack();

                    thumbnailFrame.setVisible(true);

                    thumbnailFrame.setLocationRelativeTo(null);

                    thumbnailFrame.setResizable(false);

                    thumbnailFrame.setIconImage(yttnGeneralUtil.getCyderIcon().getImage());
                }

                catch (Exception exc) {
                    yttnGeneralUtil.inform("Sorry, " + yttnGeneralUtil.getUsername() + ", but that's not a valid YouTube video ID.","", 400, 200);
                }
            }
        });

        JPanel ButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        ButtonPanel.add(getYTTN);

        ParentPanel.add(ButtonPanel);

        ParentPanel.setBorder(new LineBorder(new Color(0, 0, 0)));

        yttnFrame.add(ParentPanel);

        yttnFrame.pack();

        yttnFrame.setLocationRelativeTo(null);

        yttnFrame.setVisible(true);

        yttnFrame.setAlwaysOnTop(true);

        yttnFrame.setAlwaysOnTop(false);

        yttnField.requestFocus();
    }
}
