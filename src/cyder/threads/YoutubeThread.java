package cyder.threads;


import cyder.ui.CyderFrame;
import cyder.utilities.NetworkUtil;
import cyder.utilities.NumberUtil;
import cyder.utilities.StringUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;

public class YoutubeThread {
    private boolean exit = false;

    private StringUtil su;
    public static final LinkedList<Character> urlChars = makeURLChars();

    public static LinkedList makeURLChars() {
        LinkedList<Character> ret = new LinkedList<>(Arrays.asList('0', '1', '2',
                '3', '4', '5', '6', '7', '8', '9', '-', '_', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
                'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'));

        for (int i = 11 ; i < 37 ; i++) {
            ret.add(Character.toUpperCase(ret.get(i)));
        }

        return ret;
    }

    public YoutubeThread(JTextPane jTextPane) {
        su = new StringUtil(jTextPane);

        new Thread(() -> {
            String Start = "https://www.youtube.com/watch?v=";
            String thumbnailURL = "https://img.youtube.com/vi/REPLACE/hqdefault.jpg";

            while (true) {
                try {
                    String UUID = "";
                    StringBuilder UUIDBuilder = new StringBuilder(UUID);

                    //don't do random, sequentially build up to and save place when canceled
                    for (int i = 0; i < 11; i++)
                        UUIDBuilder.append(urlChars.get(NumberUtil.randInt(0, 63)));

                    UUID = UUIDBuilder.toString();
                    su.println("Checked UUID: " + UUID);
                    Start = Start + UUID;

                    BufferedImage Thumbnail = ImageIO.read(new URL(thumbnailURL.replace("REPLACE", UUID)));
                    su.println("YouTube script found valid video with UUID: " + UUID);

                    CyderFrame thumbnailFrame = new CyderFrame(Thumbnail.getWidth(), Thumbnail.getHeight(), new ImageIcon(Thumbnail));
                    thumbnailFrame.setTitlePosition(CyderFrame.TitlePosition.CENTER);
                    thumbnailFrame.setTitle(UUID);

                    JLabel pictureLabel = new JLabel();
                    pictureLabel.setToolTipText("Open video " + UUID);
                    String video = Start + UUID;
                    pictureLabel.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            NetworkUtil.internetConnect(video);
                        }
                    });

                    pictureLabel.setBounds(0, 0, thumbnailFrame.getWidth(), thumbnailFrame.getHeight());
                    thumbnailFrame.getContentPane().add(pictureLabel);

                    thumbnailFrame.setVisible(true);
                    thumbnailFrame.setLocationRelativeTo(null);

                    this.kill();
                } catch (Exception ignored) {}
            }
        },"Random youtube thread #i").start();
    }

    public void kill() {
        this.exit = true;
    }

    @Override
    public String toString() {
        return "YouTube Thread object, hash=" + this.hashCode();
    }
}