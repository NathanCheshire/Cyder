package cyder.youtube;

import com.google.common.base.Preconditions;
import cyder.constants.CyderColors;
import cyder.constants.CyderRegexPatterns;
import cyder.enums.Dynamic;
import cyder.exceptions.IllegalMethodException;
import cyder.genesis.PropLoader;
import cyder.handlers.ConsoleFrame;
import cyder.handlers.external.audio.AudioPlayer;
import cyder.handlers.external.audio.AudioUtil;
import cyder.handlers.input.BaseInputHandler;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;
import cyder.threads.CyderThreadRunner;
import cyder.ui.CyderProgressBar;
import cyder.ui.CyderProgressUI;
import cyder.user.UserFile;
import cyder.utils.*;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;

import static cyder.youtube.YoutubeConstants.*;

/**
 * A utility class for downloading a single video's audio from YouTube.
 */
public class YoutubeDownload {
    /**
     * The url of the youtube video to download.
     */
    private final String url;

    /**
     * Suppress default constructor.
     */
    @SuppressWarnings("unused")
    private YoutubeDownload() {
        throw new IllegalMethodException("Illegal use of constructor without download url");
    }

    /**
     * Constructs a new YoutubeDownload object.
     *
     * @param url the url of the video to download
     */
    public YoutubeDownload(String url) {
        Preconditions.checkNotNull(url);
        Preconditions.checkArgument(!url.isEmpty());

        this.url = url;
    }

    /**
     * The label this class will print and update with statistics about the download.
     */
    private JLabel printLabel;

    /**
     * Updates the download progress labels.
     */
    public void updateLabel() {
        String updateText = "<html>" + downloadableName
                + "<br/>File size: " + downloadableFileSize
                + "<br/>Progress: " + downloadableProgress
                + "%<br/>Rate: " + downloadableRate
                + "<br/>Eta: " + downloadableEta + "</html>";

        printLabel.setText(updateText);
        printLabel.revalidate();
        printLabel.repaint();
        printLabel.setHorizontalAlignment(JLabel.LEFT);
    }

    /**
     * Refreshes the font of the label containing the download information.
     */
    public void refreshLabelFont() {
        if (isDownloaded()) {
            return;
        }

        printLabel.setFont(ConsoleFrame.INSTANCE.generateUserFont());
    }

    /**
     * The download name of this download object.
     */
    private String downloadableName;

    /**
     * The download file size of this download object.
     */
    private String downloadableFileSize;

    /**
     * The download progress of this download object.
     */
    private float downloadableProgress;

    /**
     * The download rate of this download object.
     */
    private String downloadableRate;

    /**
     * The download eta of this download object.
     */
    private String downloadableEta;

    /**
     * Whether this download has completed downloading.
     */
    private boolean downloaded;

    /**
     * Whether this download has completed, not necessarily downloaded.
     */
    private boolean done;

    /**
     * Whether this download is currently underway.
     */
    private boolean downloading;

    /**
     * Whether this download was canceled externally.
     */
    private boolean canceled;

    /**
     * Returns the download name of this download.
     *
     * @return the download name of this download
     */
    public String getDownloadableName() {
        return downloadableName;
    }

    /**
     * Returns the download file size of this download.
     *
     * @return the download file size of this download
     */
    public String getDownloadableFileSize() {
        return downloadableFileSize;
    }

    /**
     * Returns the download progress of this download.
     *
     * @return the download progress of this download
     */
    public float getDownloadableProgress() {
        return downloadableProgress;
    }

    /**
     * Returns the download rate of this download.
     *
     * @return the download rate of this download
     */
    public String getDownloadableRate() {
        return downloadableRate;
    }

    /**
     * Returns the download eta of this download.
     *
     * @return the download eta of this download
     */
    public String getDownloadableEta() {
        return downloadableEta;
    }

    /**
     * Returns whether this download has completed.
     *
     * @return whether this download has completed
     */
    public boolean isDownloaded() {
        return downloaded;
    }

    /**
     * Returns whether this download has ended. Not necessarily whether it downloaded.
     * Use {@link #isDownloaded()} to check for downloaded.
     *
     * @return whether this download has ended
     */
    public boolean isDone() {
        return done;
    }

    /**
     * Returns whether this download is currently downloading.
     *
     * @return whether this download is currently downloading
     */
    public boolean isDownloading() {
        return downloading;
    }

    /**
     * Returns whether this download was canceled.
     *
     * @return whether this download was canceled
     */
    public boolean isCanceled() {
        return canceled;
    }

    /**
     * Cancels this download if downloading.
     */
    public void cancel() {
        if (isDownloading()) {
            canceled = true;
        }
    }

    /**
     * The audio progress bar to print and update if a valid input handler is provided.
     */
    private CyderProgressBar audioProgressBar;

    /**
     * The audio progress ui to use for the audio progress bar.
     */
    private CyderProgressUI audioProgressUi;

    /**
     * The input handler to print updates to if not null;
     */
    private BaseInputHandler inputHandler;

    /**
     * Returns the input handler set for this YouTube download.
     *
     * @return the input handler set for this YouTube download
     */
    public BaseInputHandler getInputHandler() {
        return inputHandler;
    }

    /**
     * Sets the input handler set for this YouTube download.
     *
     * @param inputHandler the input handler set for this YouTube download
     */
    public void setInputHandler(BaseInputHandler inputHandler) {
        this.inputHandler = inputHandler;
    }

    /**
     * Downloads this object's YouTube video.
     */
    public void download() {
        Preconditions.checkArgument(!done, "Object attempted to download previously");

        boolean shouldPrint = inputHandler != null;

        String saveDir = OSUtil.buildPath(
                Dynamic.PATH,
                Dynamic.USERS.getDirectoryName(),
                ConsoleFrame.INSTANCE.getUUID(),
                UserFile.MUSIC.getName());

        String extension = "." + PropLoader.getString("ffmpeg_audio_output_format");

        AtomicReference<String> parsedSaveName = new AtomicReference<>(
                StringUtil.parseNonAscii(NetworkUtil.getUrlTitle(url))
                        .replace("- YouTube", "")
                        .replaceAll(CyderRegexPatterns.windowsInvalidFilenameChars.pattern(), "").trim());

        if (shouldPrint) {
            inputHandler.println("Downloading audio as: " + parsedSaveName + extension);
        }

        // remove trailing periods
        while (parsedSaveName.get().endsWith(".")) {
            parsedSaveName.set(parsedSaveName.get().substring(0, parsedSaveName.get().length() - 1));
        }

        // if for some reason this case happens, account for it
        if (parsedSaveName.get().isEmpty()) {
            parsedSaveName.set(SecurityUtil.generateUUID());
        }

        String[] command = {
                AudioUtil.getYoutubeDlCommand(), url,
                FFMPEG_EXTRACT_AUDIO_FLAG,
                FFMPEG_AUDIO_FORMAT_FLAG, PropLoader.getString("ffmpeg_audio_output_format"),
                FFMPEG_OUTPUT_FLAG, new File(saveDir).getAbsolutePath() + OSUtil.FILE_SEP
                + parsedSaveName + ".%(ext)s"
        };

        YoutubeUtil.addActiveDownload(this);
        this.downloadableName = parsedSaveName.get();

        CyderThreadRunner.submit(() -> {
            try {
                if (shouldPrint) {
                    constructAndPrintUiElements();
                }

                downloading = true;
                Process proc = Runtime.getRuntime().exec(command);

                BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                String outputString;

                while ((outputString = stdInput.readLine()) != null) {
                    // todo test canceling
                    if (isCanceled()) {
                        cleanUpFromCancel(new File(saveDir), parsedSaveName);
                        break;
                    }

                    Matcher updateMatcher = CyderRegexPatterns.updatePattern.matcher(outputString);

                    if (updateMatcher.find()) {
                        float progress = Float.parseFloat(updateMatcher.group(1)
                                .replaceAll("[^0-9.]", ""));
                        audioProgressBar.setValue((int) ((progress / 100.0) * audioProgressBar.getMaximum()));
                        this.downloadableFileSize = updateMatcher.group(2);
                        this.downloadableProgress = progress;
                        this.downloadableRate = updateMatcher.group(3);
                        this.downloadableEta = updateMatcher.group(4);

                        updateLabel();
                    }
                }

                if (!isCanceled()) {
                    downloaded = true;

                    YoutubeUtil.downloadThumbnail(url);

                    AudioPlayer.addAudioNext(new File(OSUtil.buildPath(
                            saveDir, parsedSaveName + extension)));
                }

                if (shouldPrint) {
                    inputHandler.println("Download complete: saved as " + downloadableName
                            + " and added to audio queue");

                    cleanUpUi();
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);

                if (shouldPrint) {
                    inputHandler.println("An exception occurred while attempting to download, url=" + url);
                    cleanUpUi();
                }
            } finally {
                YoutubeUtil.removeActiveDownload(this);
                done = true;
                downloading = false;
            }
        }, "YouTube Downloader, saveName=" + parsedSaveName.get() + ", uuid=" + YoutubeUtil.getUuid(url));
    }

    /**
     * Constructs and prints the progress bar and labels to the provided input handler.
     */
    private void constructAndPrintUiElements() {
        audioProgressBar = new CyderProgressBar(CyderProgressBar.HORIZONTAL, 0, 10000);

        audioProgressUi = new CyderProgressUI();
        audioProgressUi.setColors(CyderColors.regularPink, CyderColors.regularBlue);
        audioProgressUi.setAnimationDirection(CyderProgressUI.AnimationDirection.LEFT_TO_RIGHT);
        audioProgressBar.setUI(audioProgressUi);
        audioProgressBar.setMinimum(0);
        audioProgressBar.setMaximum(10000);
        audioProgressBar.setBorder(new LineBorder(Color.black, 2));
        audioProgressBar.setBounds(0, 0, 400, 40);
        audioProgressBar.setVisible(true);
        audioProgressBar.setValue(0);
        audioProgressBar.setOpaque(false);
        audioProgressBar.setFocusable(false);
        audioProgressBar.repaint();

        printLabel = new JLabel("\"" + this.downloadableName + "\"");
        printLabel.setFont(ConsoleFrame.INSTANCE.generateUserFont());
        printLabel.setForeground(CyderColors.vanilla);
        printLabel.setHorizontalAlignment(JLabel.LEFT);
        printLabel.setForeground(ConsoleFrame.INSTANCE.getInputField().getForeground());
        printLabel.setFont(ConsoleFrame.INSTANCE.getInputField().getFont());

        inputHandler.println(audioProgressBar);
        inputHandler.println(printLabel);
    }

    /**
     * Cleans up the printed ui elements.
     */
    private void cleanUpUi() {
        Preconditions.checkNotNull(audioProgressUi);
        Preconditions.checkNotNull(audioProgressBar);

        audioProgressUi.setColors(CyderColors.regularBlue, CyderColors.regularBlue);
        audioProgressUi.stopAnimationTimer();

        audioProgressBar.repaint();
    }

    /**
     * Cleans up the .part or any other files left over by youtube-dl after the user canceled the download.
     *
     * @param parentDirectory      the directory the file would have been downloaded to
     * @param nameWithoutExtension the file name without the extension, anything that starts with this will be deleted
     */
    private void cleanUpFromCancel(File parentDirectory, AtomicReference<String> nameWithoutExtension) {
        Preconditions.checkNotNull(parentDirectory);
        Preconditions.checkNotNull(nameWithoutExtension);
        Preconditions.checkNotNull(nameWithoutExtension.get());

        File[] children = parentDirectory.listFiles();

        if (children != null && children.length > 0) {
            for (File child : children) {
                if (FileUtil.getFilename(child).startsWith(nameWithoutExtension.get())) {
                    if (!OSUtil.deleteFile(child)) {
                        Logger.log(Logger.Tag.DEBUG, "Could not delete file resulting from youtube "
                                + "download operation canceled, location=" + parentDirectory.getAbsolutePath()
                                + ", name=" + nameWithoutExtension);
                    }
                }
            }
        }
    }
}
