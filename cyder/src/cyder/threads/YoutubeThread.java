package cyder.threads;

import cyder.genesis.CyderCommon;
import cyder.handlers.internal.ExceptionHandler;
import cyder.ui.ConsoleFrame;
import cyder.ui.CyderFrame;
import cyder.ui.CyderOutputPane;
import cyder.utilities.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.URL;

public class YoutubeThread {
    /**
     * Boolean used for killing the YouTube threads.
     */
    private boolean exit = false;

    /**
     * StringUtil to append text to the linked JTextPane.
     */
    private final StringUtil stringUtil;

    /**
     * The uuid we are currently on
     */
    private String uuid;

    /**
     * Base URL for a YouTube video without the uuid.
     */
    String baseURL = "https://www.youtube.com/watch?v=";

    /**
     * Base URL for a YouTube video's HQ default thumbnail.
     */
    String thumbnailBaseURL = "https://img.youtube.com/vi/REPLACE/hqdefault.jpg";

    /**
     * YouTube's base 64 system used for UUID construction.
     */
    public static final char[] validChars = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
            'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
            'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2',
            '3', '4', '5', '6', '7', '8', '9', '-', '_'};

    /**
     * Starts generating UUIDs and checking them against YouTube for a valid uuid.
     * Text is appended to the provided JTextPane.
     *
     * @param jTextPane the JTextPane to print to
     * @param threadNumber the number this thread is in the YouTube thread list
     */
    public YoutubeThread(JTextPane jTextPane, int threadNumber) {
        this.stringUtil = new StringUtil(new CyderOutputPane(jTextPane));

        CyderThreadRunner.submit(() -> {
            //init as user's stored value
            uuid = UserUtil.getCyderUser().getYoutubeuuid();

            try {
                if (uuid.length() != 11)
                    throw new IllegalArgumentException("Youtube Thread UUID not length 11");
                else if (uuid.length() == 0 || uuid == null)
                    throw new IllegalArgumentException("Youtube Thread UUID length 0 or null");
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }

            int runs = 0;
            long start = System.currentTimeMillis();

            while (!exit) {
                try {
                    if (uuid == null)
                        throw new Exception("UUID is null");
                    else if (uuid.length() != 11)
                        throw new Exception("UUID length is not 11");

                    MasterYoutubeThread.getSemaphore().acquire();
                    stringUtil.println("Checked UUID: " + uuid);
                    MasterYoutubeThread.getSemaphore().release();
                    //noinspection StringConcatenationInLoop
                    baseURL = baseURL + uuid;

                    BufferedImage Thumbnail = ImageIO.read(new URL(thumbnailBaseURL.replace("REPLACE", uuid)));

                    //end all scripts since this one was found
                    MasterYoutubeThread.killAll();

                    MasterYoutubeThread.getSemaphore().acquire();
                    stringUtil.println("YouTube script found valid video with UUID: " + uuid);
                    MasterYoutubeThread.getSemaphore().release();

                    CyderFrame thumbnailFrame = new CyderFrame(Thumbnail.getWidth(), Thumbnail.getHeight(), new ImageIcon(Thumbnail));
                    thumbnailFrame.setTitlePosition(CyderFrame.TitlePosition.CENTER);
                    thumbnailFrame.setTitle(uuid);

                    JLabel pictureLabel = new JLabel();
                    pictureLabel.setToolTipText("Open video " + uuid);
                    String video = baseURL + uuid;
                    pictureLabel.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            NetworkUtil.openUrl(video);
                        }
                    });

                    pictureLabel.setBounds(0, 0, thumbnailFrame.getWidth(), thumbnailFrame.getHeight());
                    thumbnailFrame.getContentPane().add(pictureLabel);

                    thumbnailFrame.setVisible(true);
                    thumbnailFrame.setLocationRelativeTo(CyderCommon.getDominantFrame());
                } catch (Exception ignored) {
                    //invalid UUID, so we ignore the exception and increment the UUID here to ensure we checked it
                    try {
                        uuid = String.valueOf(incrementUUID(uuid.toCharArray(), 10));
                    } catch (Exception e) {
                        ExceptionHandler.handle(e);
                    }
                }

                if (runs <= 10) {
                    runs++;
                    if (runs == 10) {
                        //accTime is avg time, this is what we will divide the remaining UUIDs by
                        //array to work backwards through
                        char[] uuidArr = uuid.toCharArray();

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

                        long time = System.currentTimeMillis() - start;
                        double avgMsPerCheck = time / 10.0;
                        long msTimeLeft = (long) ((totalUUIDs - completedUUIDs) / avgMsPerCheck);

                        ConsoleFrame.getConsoleFrame().getConsoleCyderFrame().notify("Time left: "
                                + TimeUtil.millisToFormattedString(msTimeLeft));
                    }
                }
            }
        },"Random youtube thread #" + threadNumber);
    }

    /**
     * Complex logic to increment the provided UUID based on YouTube's base 64 character set.
     *
     * @param uuid the uuid to increment in char array form
     * @param pos the position to add to (needed since this method is recursive)
     * @return the provided uuid incremented starting at the provided position
     *          (ripples down the array if overflow)
     */
    private char[] incrementUUID(char[] uuid, int pos)  {
        //init ret array
        char[] ret;

        //get the character at the position we want
        char charizard = uuid[pos];

        //is it equal to the last in the master list of chars?
        if (charizard == validChars[validChars.length - 1]) {
            //use recursion to add to next column
            if (pos - 1 < 0)
                throw new IllegalArgumentException("YouTube thread overflow");
            else
                ret = incrementUUID(uuid, pos - 1);
        } else { //otherwise, we just add to it and return
            //find the char's position in the master list of chars
            int index = findIndex(charizard);
            //add to index
            index++;
            //set charizard equal to new char
            charizard = validChars[index];
            //sub in charizard in array
            char[] cp = uuid.clone();
            cp[pos] = charizard;

            //if rolling to new column, reset this column
            if (pos + 1 <= 10)
                cp[pos + 1] = validChars[0];

            ret = cp;
        }

        return ret;
    }

    /**
     * Finds the index of the provided char in the provided array.
     *
     * @param c the character to find in the provided array
     * @return the index of the provided char in the provided array
     */
    private int findIndex(char c) {
        if (YoutubeThread.validChars == null)
            return -1;

        int i = 0;

        while (i < YoutubeThread.validChars.length)
            if (YoutubeThread.validChars[i] == c) {
                return i;
            } else {
                i = i + 1;
            }

        return -1;
    }

    /**
     * Kills this YouTube thread and writes the last checked UUID to system data.
     * In the future if more threads are ever allowed to execute concurrently,
     * there will need to be more logic here to figure out which uuid was actually
     * last checked.
     */
    public void kill() {
        exit = true;
        UserUtil.setUserData("youtubeuuid", uuid);
    }

    @Override
    public String toString() {
        return ReflectionUtil.commonCyderToString(this);
    }
}