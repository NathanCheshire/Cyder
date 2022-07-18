package cyder.threads;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.console.Console;
import cyder.constants.CyderStrings;
import cyder.constants.CyderUrls;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.ui.CyderFrame;
import cyder.ui.CyderOutputPane;
import cyder.user.UserUtil;
import cyder.utils.NetworkUtil;
import cyder.utils.StringUtil;
import cyder.utils.TimeUtil;

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
    private boolean exit;

    /**
     * StringUtil to append text to the linked JTextPane.
     */
    private final StringUtil stringUtil;

    /**
     * The uuid we are currently on
     */
    private String youtubeUuid;

    /**
     * YouTube's base 64 system used for UUID construction.
     */
    public static final ImmutableList<Character> validChars = ImmutableList.of('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
            'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
            'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0', '1', '2',
            '3', '4', '5', '6', '7', '8', '9', '-', '_');

    /**
     * Suppress default constructor. Requires two parameters for instantiation.D
     */
    private YoutubeThread() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Starts generating UUIDs and checking them against YouTube for a valid uuid.
     * Text is appended to the provided JTextPane.
     *
     * @param jTextPane    the JTextPane to print to
     * @param threadNumber the number this thread is in the YouTube thread list
     */
    public YoutubeThread(JTextPane jTextPane, int threadNumber) {
        Preconditions.checkNotNull(jTextPane);
        Preconditions.checkArgument(threadNumber > 0);

        stringUtil = new StringUtil(new CyderOutputPane(jTextPane));

        CyderThreadRunner.submit(() -> {
            youtubeUuid = UserUtil.getCyderUser().getYoutubeuuid();

            Preconditions.checkNotNull(youtubeUuid);
            Preconditions.checkArgument(youtubeUuid.length() == 11);

            int numRuns = 0;
            long startTime = System.currentTimeMillis();

            while (!exit) {
                try {
                    MasterYoutubeThread.getSemaphore().acquire();
                    stringUtil.println("Checked UUID: " + youtubeUuid);
                    MasterYoutubeThread.getSemaphore().release();
                    String baseURL = CyderUrls.YOUTUBE_VIDEO_HEADER + youtubeUuid;

                    BufferedImage Thumbnail = ImageIO.read(new URL(
                            CyderUrls.THUMBNAIL_BASE_URL.replace("REPLACE", youtubeUuid)));

                    //end all scripts since this one was found
                    MasterYoutubeThread.killAll();

                    MasterYoutubeThread.getSemaphore().acquire();
                    stringUtil.println("YouTube script found valid video with UUID: " + youtubeUuid);
                    MasterYoutubeThread.getSemaphore().release();

                    CyderFrame thumbnailFrame = new CyderFrame(Thumbnail.getWidth(),
                            Thumbnail.getHeight(), new ImageIcon(Thumbnail));
                    thumbnailFrame.setTitlePosition(CyderFrame.TitlePosition.CENTER);
                    thumbnailFrame.setTitle(youtubeUuid);

                    JLabel pictureLabel = new JLabel();
                    pictureLabel.setToolTipText("Open video " + youtubeUuid);
                    String video = baseURL + youtubeUuid;
                    pictureLabel.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            NetworkUtil.openUrl(video);
                        }
                    });

                    pictureLabel.setBounds(0, 0, thumbnailFrame.getWidth(), thumbnailFrame.getHeight());
                    thumbnailFrame.getContentPane().add(pictureLabel);

                    thumbnailFrame.finalizeAndShow();
                } catch (Exception ignored) {
                    //invalid UUID, so we ignore the exception and increment the UUID here to ensure we checked it
                    try {
                        youtubeUuid = String.valueOf(incrementUuid(youtubeUuid.toCharArray(), 10));
                    } catch (Exception e) {
                        ExceptionHandler.handle(e);
                    }
                }

                if (numRuns <= 10) {
                    numRuns++;

                    if (numRuns == 10) {
                        //accTime is avg time, this is what we will divide the remaining UUIDs by
                        //array to work backwards through
                        char[] uuidArr = youtubeUuid.toCharArray();

                        //subtract completed UUIDS from this
                        double totalUUIDs = Math.pow(64, 11);
                        double completedUUIDs = 0;

                        for (int i = 10 ; i >= 0 ; i--) {
                            int weight = Math.abs(i - 10);
                            char currentDigit = uuidArr[i];

                            for (int j = 0 ; j < 64 ; j++) {
                                if (validChars.get(j) == currentDigit) {
                                    completedUUIDs += j * Math.pow(64, weight);
                                    break;
                                }
                            }
                        }

                        long time = System.currentTimeMillis() - startTime;
                        double avgMsPerCheck = time / 10.0;
                        long msTimeLeft = (long) ((totalUUIDs - completedUUIDs) / avgMsPerCheck);

                        Console.INSTANCE.getConsoleCyderFrame().notify("Time left: "
                                + TimeUtil.millisToFormattedString(msTimeLeft));
                    }
                }
            }
        }, "Random youtube thread #" + threadNumber);
    }

    /**
     * Complex logic to increment the provided UUID based on YouTube's base 64 character set.
     *
     * @param uuid the uuid to increment in char array form
     * @param pos  the position to add to (needed since this method is recursive)
     * @return the provided uuid incremented starting at the provided position
     * (ripples down the array if overflow)
     */
    private char[] incrementUuid(char[] uuid, int pos) {
        char[] ret;

        char positionChar = uuid[pos];

        if (positionChar == validChars.get(validChars.size()) - 1) {
            // use recursion to add to next column
            if (pos - 1 < 0) {
                throw new IllegalArgumentException("YouTube thread overflow");
            } else {
                ret = incrementUuid(uuid, pos - 1);
            }
        } else {
            positionChar = validChars.get(findIndex(positionChar) + 1);
            char[] cp = uuid.clone();
            cp[pos] = positionChar;

            if (pos + 1 <= 10) {
                cp[pos + 1] = validChars.get(0);
            }

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
        int i = 0;

        while (i < validChars.size()) {
            if (validChars.get(i) == c) {
                return i;
            } else {
                i = i + 1;
            }
        }

        return -1;
    }

    /**
     * Kills this YouTube thread and writes the last checked UUID to system data.
     */
    public void kill() {
        exit = true;
        UserUtil.getCyderUser().setYoutubeuuid(youtubeUuid);
    }
}