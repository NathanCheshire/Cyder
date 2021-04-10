package com.cyder.widgets;

import com.cyder.Constants.CyderColors;
import com.cyder.Constants.CyderFonts;
import com.cyder.enums.TitlePosition;
import com.cyder.ui.ConsoleFrame;
import com.cyder.ui.CyderButton;
import com.cyder.ui.CyderFrame;
import com.cyder.utilities.NetworkUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.URL;

import static com.cyder.Constants.CyderStrings.DEFAULT_BACKGROUND_PATH;

public class YouTubeThumbnail {
    private CyderFrame yttnFrame;
    private CyderButton getYTTN;
    private JTextField yttnField;

    private int xMouse;
    private int yMouse;

    private NetworkUtil networkUtil = new NetworkUtil();

    public YouTubeThumbnail() {
        if (yttnFrame != null)
            yttnFrame.closeAnimation();

        yttnFrame = new CyderFrame(600,250,new ImageIcon(DEFAULT_BACKGROUND_PATH));
        yttnFrame.setTitle("YouTube Thumbnail");

        JLabel VideoID = new JLabel("Enter a valid YouTube video ID");
        VideoID.setFont(CyderFonts.weatherFontBig);
        VideoID.setForeground(CyderColors.navy);
        VideoID.setBounds(40,40,600,40);
        yttnFrame.getContentPane().add(VideoID);

        yttnField = new JTextField(30);
        yttnField.setSelectionColor(CyderColors.selectionColor);
        yttnField.setBorder(new LineBorder(CyderColors.navy,5,false));
        yttnField.addActionListener(e -> getYTTN.doClick());
        yttnField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
            if(yttnField.getText().length() >= 11 && !(evt.getKeyChar()== KeyEvent.VK_DELETE || evt.getKeyChar() == KeyEvent.VK_BACK_SPACE)) {
                Toolkit.getDefaultToolkit().beep();
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

        yttnField.setFont(CyderFonts.weatherFontSmall);
        yttnField.setBorder(new LineBorder(CyderColors.navy,5,false));
        yttnField.setBounds(150,120,300,40);
        yttnFrame.getContentPane().add(yttnField);

        getYTTN = new CyderButton("Get Thumbnail");
        getYTTN.setBorder(new LineBorder(CyderColors.navy,5,false));
        getYTTN.setColors(CyderColors.regularRed);
        getYTTN.setFocusPainted(false);
        getYTTN.setBackground(CyderColors.regularRed);
        getYTTN.setFont(CyderFonts.weatherFontSmall);
        getYTTN.addActionListener(e -> getYTTNAction());
        getYTTN.setBounds(150,170,300,40);
        yttnFrame.getContentPane().add(getYTTN);

        yttnFrame.setLocationRelativeTo(null);
        yttnFrame.setVisible(true);
        yttnField.requestFocus();
    }

    private void getYTTNAction() {
        String YouTubeID = yttnField.getText();

        if (YouTubeID.length() < 11)
            yttnFrame.inform("Sorry, " + ConsoleFrame.getUsername() + ", but that's not a valid YouTube video ID.","Invalid");

        else {
            String YouTubeURL = "https://img.youtube.com/vi/REPLACE/hqdefault.jpg";
            YouTubeURL = YouTubeURL.replace("REPLACE", YouTubeID);
            URL url;

            try {
                url = new URL(YouTubeURL);

                BufferedImage Thumbnail = ImageIO.read(url);

                CyderFrame thumbnailFrame = new CyderFrame(Thumbnail.getWidth(), Thumbnail.getHeight(), new ImageIcon(Thumbnail));
                thumbnailFrame.setTitlePosition(TitlePosition.CENTER);
                thumbnailFrame.setTitle(YouTubeID);

                JLabel PictureLabel = new JLabel();
                PictureLabel.setToolTipText("Open video " + YouTubeID);
                PictureLabel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                    networkUtil.internetConnect("https://www.youtube.com/watch?v=" + YouTubeID);
                    }
                });
                PictureLabel.setBounds(0, 0, Thumbnail.getWidth(), Thumbnail.getHeight());
                thumbnailFrame.getContentPane().add(PictureLabel);

                thumbnailFrame.setVisible(true);
                thumbnailFrame.setLocationRelativeTo(yttnFrame);
                yttnFrame.closeAnimation();
            }

            catch (Exception exc) {
                yttnFrame.inform("Sorry, " + ConsoleFrame.getUsername() + ", but that's not a valid YouTube video ID.","Invalid");
            }
        }
    }
}
