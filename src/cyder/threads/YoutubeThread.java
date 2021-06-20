package cyder.threads;


import cyder.exception.FatalException;
import cyder.handler.ErrorHandler;
import cyder.ui.CyderFrame;
import cyder.utilities.IOUtil;
import cyder.utilities.NetworkUtil;
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
    private String UUID;
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
        //todo will be passed a inputhandler which we will call inputHandler.getStringUtil().println(String);
        su = new StringUtil(jTextPane);

        new Thread(() -> {
            String Start = "https://www.youtube.com/watch?v=";
            String thumbnailURL = "https://img.youtube.com/vi/REPLACE/hqdefault.jpg";
            UUID = IOUtil.getSystemData("YTT");

            try {
                if (UUID.length() != 11)
                    throw new FatalException("Youtube Thread UUID not length 11");
                else if (UUID.length() == 0 || UUID == null)
                    throw new FatalException("Youtube Thread UUID length 0 or null");
            } catch (Exception e) {
                ErrorHandler.handle(e);
            }

            while (true) {
                try {
                    if (UUID == null)
                        throw new Exception("UUID is null");
                    else if (UUID.length() != 11)
                        throw new Exception("UUID length is not 11");

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
                } catch (Exception ignored) {
                    //invalid UUID so we ingnore and increment the UUID here to ensure we checked it
                    try {
                        UUID = String.valueOf(incrementUUID(UUID.toCharArray(), 11));
                    } catch (FatalException e) {
                        ErrorHandler.handle(e);
                    }
                }
            }
        },"Random youtube thread").start();
    }

    private char[] incrementUUID(char[] uuid,int pos) throws FatalException {
        //init ret array
        char[] ret = uuid.clone();

        //get the character at the position we ant
        char charac = uuid[pos];

        //is it equal to the last in the master list of chars?
        if (charac == urlChars.get(urlChars.size() - 1)) {
            //use recursion to add to next column
            if (pos - 1 < 0)
                throw new FatalException("YouTube thread overflow");
            else
                ret = incrementUUID(uuid, pos - 1);
        } else { //otherwise we just add to it and return
            //find the char's position in the master list of chars
            int index = urlChars.indexOf(charac);
            //add to index
            index++;
            //set charac equal to new char
            charac = urlChars.get(index);
            //sub in charac in array
            char[] cp = uuid.clone();
            cp[pos] = charac;
            ret = cp;
        }

        return ret;
    }

    /**
     * Kills the YouTube thread and writes the last checked UUID to system data
     */
    public void kill() {
        this.exit = true;
        IOUtil.writeSystemData("YTT",UUID);
    }

    @Override
    public String toString() {
        return "YouTube Thread object, hash=" + this.hashCode();
    }
}