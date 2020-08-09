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

public class YouTubeThumbnail {
    private JFrame yttnFrame;
    private CyderButton getYTTN;
    private JTextField yttnField;

    private int xMouse;
    private int yMouse;

    private Util yttnUtil = new Util();

    public YouTubeThumbnail() {
        if (yttnFrame != null)
            yttnUtil.closeAnimation(yttnFrame);

        yttnFrame = new JFrame();

        yttnFrame.setResizable(false);

        yttnFrame.setTitle("YouTube Thumbnail");

        yttnFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        yttnFrame.setIconImage(yttnUtil.getCyderIcon().getImage());

        JPanel ParentPanel = new JPanel();

        ParentPanel.setLayout(new BoxLayout(ParentPanel,BoxLayout.Y_AXIS));

        JLabel VideoID = new JLabel("Enter a valid YouTube video ID");

        VideoID.setFont(yttnUtil.weatherFontBig);

        VideoID.setForeground(yttnUtil.navy);

        JPanel TopPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        TopPanel.add(VideoID);

        ParentPanel.add(TopPanel);

        yttnField = new JTextField(30);

        yttnField.setBorder(new LineBorder(yttnUtil.navy,5,false));

        yttnField.addActionListener(e -> getYTTN.doClick());

        yttnField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                if(yttnField.getText().length() >= 11 && !(evt.getKeyChar()== KeyEvent.VK_DELETE || evt.getKeyChar() == KeyEvent.VK_BACK_SPACE)) {
                    yttnUtil.beep();
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

        yttnField.setFont(yttnUtil.weatherFontSmall);

        yttnField.setBorder(new LineBorder(yttnUtil.navy,5,false));

        JPanel MiddlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        MiddlePanel.add(yttnField);

        ParentPanel.add(MiddlePanel);

        getYTTN = new CyderButton("Get Thumbnail");

        getYTTN.setBorder(new LineBorder(yttnUtil.navy,5,false));

        getYTTN.setColors(yttnUtil.regularRed);

        getYTTN.setFocusPainted(false);

        getYTTN.setBackground(yttnUtil.regularRed);

        getYTTN.setFont(yttnUtil.weatherFontSmall);

        getYTTN.addActionListener(e -> {
            yttnUtil.closeAnimation(yttnFrame);

            yttnFrame.dispose();

            String YouTubeID = yttnField.getText();

            if (YouTubeID.length() < 11) {
                yttnUtil.inform("Sorry, " + yttnUtil.getUsername() + ", but that's not a valid YouTube video ID.","", 400, 200);
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

                    parentPanel.setBorder(new LineBorder(yttnUtil.navy,10,false));

                    parentPanel.setLayout(new BorderLayout());

                    thumbnailFrame.setContentPane(parentPanel);

                    JLabel PictureLabel = new JLabel(new ImageIcon(Thumbnail));

                    PictureLabel.setToolTipText("Open video " + YouTubeID);

                    PictureLabel.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            yttnUtil.internetConnect("https://www.youtube.com/watch?v=" + YouTubeID);
                        }
                    });

                    parentPanel.add(PictureLabel, BorderLayout.PAGE_START);

                    CyderButton closeYT = new CyderButton("Close");

                    closeYT.setColors(yttnUtil.regularRed);

                    closeYT.setBorder(new LineBorder(yttnUtil.navy,5,false));

                    closeYT.setFocusPainted(false);

                    closeYT.setBackground(yttnUtil.regularRed);

                    closeYT.setFont(yttnUtil.weatherFontSmall);

                    closeYT.addActionListener(ev -> yttnUtil.closeAnimation(thumbnailFrame));

                    closeYT.setSize(thumbnailFrame.getX(),20);

                    parentPanel.add(closeYT,BorderLayout.PAGE_END);

                    parentPanel.repaint();

                    thumbnailFrame.pack();

                    thumbnailFrame.setVisible(true);

                    thumbnailFrame.setLocationRelativeTo(null);

                    thumbnailFrame.setResizable(false);

                    thumbnailFrame.setIconImage(yttnUtil.getCyderIcon().getImage());
                }

                catch (Exception exc) {
                    yttnUtil.inform("Sorry, " + yttnUtil.getUsername() + ", but that's not a valid YouTube video ID.","", 400, 200);
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
