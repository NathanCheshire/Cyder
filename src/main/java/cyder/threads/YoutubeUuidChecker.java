package cyder.threads;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.console.Console;
import cyder.constants.CyderUrls;
import cyder.exceptions.FatalException;
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
import cyder.youtube.YouTubeConstants;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Optional;

/**
 * A class for generating random YouTube UUIDs and attempting to parse the resulting url for a valid video.
 * Using a single thread, this has about a 1 in {@link #CHANCE_OF_SUCCESS} chance of succeeding every iteration.
 */
public class YoutubeUuidChecker {
    /**
     * The number of runs averaged together to notify the user of the expected time remaining before
     * all UUIDs have been checked.
     */
    private static final int runsToAverage = 10;

    /**
     * The chances of success for a singular thread running.
     */
    @SuppressWarnings("unused")
    public static final BigInteger CHANCE_OF_SUCCESS = new BigInteger("73786976294838206464");

    /**
     * YouTube's base 64 system used for UUID construction.
     */
    private static final ImmutableList<Character> uuidChars = ImmutableList.of(
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h',
            'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
            'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
            'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F',
            'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
            'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V',
            'W', 'X', 'Y', 'Z', '0', '1', '2', '3',
            '4', '5', '6', '7', '8', '9', '-', '_'
    );

    /**
     * Whether this uuid checker instance has been killed.
     */
    private boolean killed;

    /**
     * The uuid this checker is currently on.
     */
    private String youTubeUuid;

    /**
     * The output pane used for printing.
     */
    private final CyderOutputPane outputPane;

    /**
     * Suppress default constructor. Requires two parameters for instantiation.
     */
    private YoutubeUuidChecker() {
        throw new IllegalMethodException(CyderStrings.ILLEGAL_CONSTRUCTOR);
    }

    /**
     * Starts generating UUIDs and checking them against YouTube for a valid uuid.
     * Text is appended to the provided JTextPane.
     *
     * @param outputPane output pane to use for printing output to
     */
    public YoutubeUuidChecker(CyderOutputPane outputPane) {
        Preconditions.checkNotNull(outputPane);

        this.outputPane = outputPane;

        startChecking();
    }

    /**
     * Starts this instance checking for a valid UUID.
     */
    private void startChecking() {
        StringUtil stringUtil = outputPane.getStringUtil();

        String threadName = "YoutubeUuidChecker#" + YoutubeUuidCheckerManager.INSTANCE.getActiveUuidCheckersLength();
        CyderThreadRunner.submit(() -> {
            youTubeUuid = UserUtil.getCyderUser().getYouTubeUuid();

            Preconditions.checkNotNull(youTubeUuid);
            Preconditions.checkArgument(youTubeUuid.length() == YouTubeConstants.UUID_LENGTH);

            int numRuns = 0;
            long startTime = System.currentTimeMillis();

            while (!killed) {
                YoutubeUuidCheckerManager.INSTANCE.incrementUrlsChecked();

                try {
                    if (!YoutubeUuidCheckerManager.INSTANCE.acquireLock()) {
                        throw new FatalException("Failed to acquire lock");
                    }
                    stringUtil.println("Checked uuid: " + youTubeUuid);
                    YoutubeUuidCheckerManager.INSTANCE.releaseLock();

                    YoutubeUuidCheckerManager.INSTANCE.killAll();

                    if (!YoutubeUuidCheckerManager.INSTANCE.acquireLock()) {
                        throw new FatalException("Failed to acquire lock");
                    }
                    stringUtil.println("YouTube script found valid video with uuid: " + youTubeUuid);
                    YoutubeUuidCheckerManager.INSTANCE.releaseLock();

                    BufferedImage thumbnail = ImageUtil.read(CyderUrls.THUMBNAIL_BASE_URL
                            .replace("REPLACE", youTubeUuid));
                    showThumbnailFrame(thumbnail);
                } catch (Exception ignored) {
                    incrementUuid();
                }

                if (numRuns < runsToAverage) {
                    numRuns++;
                } else if (numRuns == runsToAverage) {
                    BigInteger completedUuids = new BigInteger("0");

                    char[] uuidArray = youTubeUuid.toCharArray();
                    for (int i = runsToAverage ; i >= 0 ; i--) {
                        int weight = Math.abs(i - runsToAverage);
                        char currentDigit = uuidArray[i];

                        for (int j = 0 ; j < uuidChars.size() ; j++) {
                            if (uuidChars.get(j) == currentDigit) {
                                BigInteger valueToAdd = new BigInteger(String.valueOf(j * Math.pow(64, weight)));
                                completedUuids = completedUuids.add(valueToAdd);
                                break;
                            }
                        }
                    }

                    long time = System.currentTimeMillis() - startTime;
                    BigInteger avgMsPerCheck = new BigInteger(String.valueOf(time / 10.0));
                    BigInteger msTimeLeft = CHANCE_OF_SUCCESS.subtract(completedUuids).divide(avgMsPerCheck);

                    Console.INSTANCE.getConsoleCyderFrame().notify("Time left: "
                            + TimeUtil.formatMillis(msTimeLeft.longValue()));
                }
            }
        }, threadName);
    }

    /**
     * Shows the thumbnail frame on the event of a successful random UUID generation.
     *
     * @param thumbnail the thumbnail image
     */
    private void showThumbnailFrame(BufferedImage thumbnail) {
        CyderFrame thumbnailFrame = new CyderFrame(
                thumbnail.getWidth(),
                thumbnail.getHeight(),
                new ImageIcon(thumbnail));
        thumbnailFrame.setTitlePosition(TitlePosition.CENTER);
        thumbnailFrame.setTitle(youTubeUuid);

        String videoUrl = CyderUrls.YOUTUBE_VIDEO_HEADER + youTubeUuid;
        String title = videoUrl;
        Optional<String> optionalTitle = NetworkUtil.getUrlTitle(videoUrl);
        if (optionalTitle.isPresent()) {
            title = optionalTitle.get();
        }

        JLabel pictureLabel = new JLabel();
        pictureLabel.setToolTipText("Open video " + title);
        pictureLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                NetworkUtil.openUrl(videoUrl);
            }
        });
        pictureLabel.setBounds(0, 0, thumbnailFrame.getWidth(), thumbnailFrame.getHeight());
        thumbnailFrame.getContentPane().add(pictureLabel);

        thumbnailFrame.finalizeAndShow();
    }

    /**
     * Increments the current UUID by one.
     */
    private void incrementUuid() {
        try {
            youTubeUuid = String.valueOf(incrementUuid(youTubeUuid.toCharArray(), 10));
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
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

        if (positionChar == uuidChars.get(uuidChars.size() - 1)) {
            if (pos - 1 < 0) {
                throw new IllegalArgumentException("YouTube uuid incrementer overflow, provided uuid: "
                        + Arrays.toString(uuid));
            } else {
                ret = incrementUuid(uuid, pos - 1);
            }
        } else {
            positionChar = uuidChars.get(findCharIndex(positionChar) + 1);
            char[] copy = uuid.clone();
            copy[pos] = positionChar;

            if (pos + 1 <= 10) {
                copy[pos + 1] = uuidChars.get(0);
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
    private int findCharIndex(char c) {
        int i = 0;

        while (i < uuidChars.size()) {
            if (uuidChars.get(i) == c) {
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
        killed = true;
        UserUtil.getCyderUser().setYouTubeUuid(youTubeUuid);
    }
}