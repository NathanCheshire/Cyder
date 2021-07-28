package cyder.threads;

import cyder.handler.ErrorHandler;
import cyder.obj.SystemData;
import cyder.ui.ConsoleFrame;
import cyder.ui.CyderFrame;
import cyder.utilities.IOUtil;
import cyder.utilities.NetworkUtil;
import cyder.utilities.StringUtil;
import cyder.utilities.TimeUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.URL;

public class YoutubeThread {
    private boolean exit = false;

    private StringUtil su;
    private String UUID;
    public static final char[] validChars = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
            'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
            'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2',
            '3', '4', '5', '6', '7', '8', '9', '-', '_'};

    public YoutubeThread(JTextPane jTextPane) {
        su = new StringUtil(jTextPane);

        new Thread(() -> {
            String Start = "https://www.youtube.com/watch?v=";
            String thumbnailURL = "https://img.youtube.com/vi/REPLACE/hqdefault.jpg";
            UUID = IOUtil.getSystemData().getYtt();

            try {
                if (UUID.length() != 11)
                    throw new IllegalArgumentException("Youtube Thread UUID not length 11");
                else if (UUID.length() == 0 || UUID == null)
                    throw new IllegalArgumentException("Youtube Thread UUID length 0 or null");
            } catch (Exception e) {
                ErrorHandler.handle(e);
            }

            long accTime = 0;
            int runs = 0;

            while (!exit) {
                long start = System.currentTimeMillis();

                try {
                    if (UUID == null)
                        throw new Exception("UUID is null");
                    else if (UUID.length() != 11)
                        throw new Exception("UUID length is not 11");

                    if (su.getLastTextLine().contains("Checked UUID: "))
                        su.removeLastLine();
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
                    ConsoleFrame.getConsoleFrame().setFrameRelative(thumbnailFrame);

                    this.kill();
                } catch (Exception ignored) {
                    //invalid UUID so we ingnore and increment the UUID here to ensure we checked it
                    try {
                        UUID = String.valueOf(incrementUUID(UUID.toCharArray(), 10));
                    } catch (Exception e) {
                        ErrorHandler.handle(e);
                    }
                }

                //todo why does it stop here
                if (runs <= 10) {
                    long time = System.currentTimeMillis() - start;
                    accTime += time;
                    runs++;

                    if (runs == 10) {
                        //accTime is avg time, this is what we will divide the remaining UUIDs by
                        accTime /= 10;
                        //array to work backwards through
                        char[] uuidArr = UUID.toCharArray();

                        //subtract completed UUIDS from this
                        double totalUUIDs = Math.pow(64,11);
                        double completedUUIDs = 0;

                        for (int i = 10 ; i >= 0 ; i--) {
                            int weight = Math.abs(i - 10);
                            char currentDigit = uuidArr[i];

                            for (int j = 0 ; j < 64 ; j++) {
                                if (validChars[j] == currentDigit) {
                                    completedUUIDs += j * Math.pow(64, weight);
                                    break;
                                }
                            }
                        }

                        int avgMsPerCheck = 200; //we could make this actually calculate but...
                        double msTimeLeft = (totalUUIDs - completedUUIDs) / avgMsPerCheck;
                        ConsoleFrame.getConsoleFrame().notify("Time left: " + TimeUtil.milisToFormattedString(msTimeLeft));
                    }
                }
            }
        },"Random youtube thread").start();
    }

    private char[] incrementUUID(char[] uuid, int pos)  {
        //init ret array
        char[] ret = uuid.clone();

        //get the character at the position we ant
        char charac = uuid[pos];

        //is it equal to the last in the master list of chars?
        if (charac == validChars[validChars.length - 1]) {
            //use recursion to add to next column
            if (pos - 1 < 0)
                throw new IllegalArgumentException("YouTube thread overflow");
            else
                ret = incrementUUID(uuid, pos - 1);
        } else { //otherwise we just add to it and return
            //find the char's position in the master list of chars
            int index = findIndex(validChars, charac);
            //add to index
            index++;
            //set charac equal to new char
            charac = validChars[index];
            //sub in charac in array
            char[] cp = uuid.clone();
            cp[pos] = charac;

            //if rolling to new column, reset this column
            if (pos + 1 <= 10)
                cp[pos + 1] = validChars[0];

            ret = cp;
        }

        return ret;
    }

    private int findIndex(char arr[], char c) {
        if (arr == null)
            return -1;

        int i = 0;

        while (i < arr.length)
            if (arr[i] == c) {
                return i;
            } else {
                i = i + 1;
            }

        return -1;
    }

    /**
     * Kills the YouTube thread and writes the last checked UUID to system data
     */
    public void kill() {
        this.exit = true;
        SystemData sd = IOUtil.getSystemData();
        sd.setYtt(UUID);
        IOUtil.setSystemData(sd);
    }

    @Override
    public String toString() {
        return "YouTube Thread object, hash=" + this.hashCode();
    }
}