package cyder.threads;

import cyder.enums.TitlePosition;
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


//todo master youtube method to kill all instead of kill all being in a consoleframe's handle method
public class YoutubeThread {
    private boolean exit = false;

    private StringUtil su;

    public YoutubeThread(JTextPane jTextPane) {
        su = new StringUtil(jTextPane);

        new Thread(() -> {
            while (!exit) {
                String Start;
                String UUID;

                char[] ValidChars = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
                        'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
                        'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2',
                        '3', '4', '5', '6', '7', '8', '9', '-', '_'};

                for (int j = 1; j < Integer.MAX_VALUE; j++) {
                    try {
                        if (exit)
                            break;

                        Start = "https://www.youtube.com/watch?v=";
                        UUID = "";
                        StringBuilder UUIDBuilder = new StringBuilder(UUID);

                        for (int i = 1; i < 12; i++)
                            UUIDBuilder.append(ValidChars[NumberUtil.randInt(0, 63)]);

                        UUID = UUIDBuilder.toString();
                        su.println("Checked UUID: " + UUID);
                        Start = Start + UUID;
                        String YouTubeURL = "https://img.youtube.com/vi/REPLACE/hqdefault.jpg";

                        BufferedImage Thumbnail = ImageIO.read(new URL(YouTubeURL.replace("REPLACE", UUID)));
                        su.println("YouTube script found valid video with UUID: " + UUID);

                        CyderFrame thumbnailFrame = new CyderFrame(Thumbnail.getWidth(),Thumbnail.getHeight(),new ImageIcon(Thumbnail));
                        thumbnailFrame.setTitlePosition(TitlePosition.CENTER);
                        thumbnailFrame.setTitle(UUID);

                        JLabel pictureLabel = new JLabel();
                        pictureLabel.setToolTipText("Open video " + UUID);
                        String video = "https://www.youtube.com/watch?v=" + UUID;
                        pictureLabel.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mouseClicked(MouseEvent e) {
                                NetworkUtil.internetConnect(video);
                            }
                        });

                        pictureLabel.setBounds(0,0,thumbnailFrame.getWidth(),thumbnailFrame.getHeight());
                        thumbnailFrame.getContentPane().add(pictureLabel);

                        thumbnailFrame.setVisible(true);
                        thumbnailFrame.setLocationRelativeTo(null);

                        return;
                    }

                    catch (Exception ignored) {}
                }
            }
        }).start();
    }

    public void kill() {
        this.exit = true;
    }
}