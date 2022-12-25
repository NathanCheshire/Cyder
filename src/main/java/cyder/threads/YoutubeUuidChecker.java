package cyder.threads;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.console.Console;
import cyder.constants.CyderUrls;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.network.NetworkUtil;
import cyder.strings.CyderStrings;
import cyder.strings.StringUtil;
import cyder.time.TimeUtil;
import cyder.ui.frame.CyderFrame;
import cyder.ui.frame.TitlePosition;
import cyder.ui.pane.CyderOutputPane;
import cyder.user.UserUtil;
import cyder.utils.ImageUtil;
import cyder.youtube.YoutubeConstants;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;

/**
 * A class for generating random YouTube UUIDs and attempting to parse the resulting url for a valid video.
 * Using a single thread, this has about a 1 in 92,233,720,368 chance of succeeding every iteration.
 */
public class YoutubeUuidChecker {
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
    public static final ImmutableList<Character> validChars = ImmutableList.of(
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
            'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
            'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '_');

    /**
     * Suppress default constructor. Requires two parameters for instantiation.
     */
    private YoutubeUuidChecker() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Starts generating UUIDs and checking them against YouTube for a valid uuid.
     * Text is appended to the provided JTextPane.
     *
     * @param jTextPane    the JTextPane to print to
     * @param threadNumber the number this thread is in the YouTube thread list
     */
    public YoutubeUuidChecker(JTextPane jTextPane, int threadNumber) {
        Preconditions.checkNotNull(jTextPane);

        stringUtil = new StringUtil(new CyderOutputPane(jTextPane));

        CyderThreadRunner.submit(() -> {
            youtubeUuid = UserUtil.getCyderUser().getYoutubeUuid();

            Preconditions.checkNotNull(youtubeUuid);
            Preconditions.checkArgument(youtubeUuid.length() == YoutubeConstants.UUID_LENGTH);

            int numRuns = 0;
            long startTime = System.currentTimeMillis();

            while (!exit) {
                MasterYoutubeThread.incrementUrlsChecked();

                try {
                    MasterYoutubeThread.getSemaphore().acquire();
                    stringUtil.println("Checked uuid: " + youtubeUuid);
                    MasterYoutubeThread.getSemaphore().release();
                    String baseURL = CyderUrls.YOUTUBE_VIDEO_HEADER + youtubeUuid;

                    BufferedImage Thumbnail = ImageUtil.read(
                            CyderUrls.THUMBNAIL_BASE_URL.replace("REPLACE", youtubeUuid));

                    // End all scripts since this one was found
                    MasterYoutubeThread.killAll();

                    MasterYoutubeThread.getSemaphore().acquire();
                    stringUtil.println("YouTube script found valid video with uuid: " + youtubeUuid);
                    MasterYoutubeThread.getSemaphore().release();

                    CyderFrame thumbnailFrame = new CyderFrame(Thumbnail.getWidth(),
                            Thumbnail.getHeight(), new ImageIcon(Thumbnail));
                    thumbnailFrame.setTitlePosition(TitlePosition.CENTER);
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
                                + TimeUtil.formatMillis(msTimeLeft));
                    }
                }
            }
        }, "Random youtube thread: " + threadNumber);
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

        if (positionChar == validChars.get(validChars.size() - 1)) {
            if (pos - 1 < 0) {
                throw new IllegalArgumentException("YouTube uuid incrementer overflow, provided uuid: "
                        + Arrays.toString(uuid));
            } else {
                ret = incrementUuid(uuid, pos - 1);
            }
        } else {
            positionChar = validChars.get(findIndex(positionChar) + 1);
            char[] copy = uuid.clone();
            copy[pos] = positionChar;

            if (pos + 1 <= 10) {
                copy[pos + 1] = validChars.get(0);
            }

            ret = copy;
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
        UserUtil.getCyderUser().setYoutubeUuid(youtubeUuid);
    }
}