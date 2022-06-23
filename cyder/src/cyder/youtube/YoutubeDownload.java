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
import cyder.threads.CyderThreadRunner;
import cyder.ui.CyderProgressBar;
import cyder.ui.CyderProgressUI;
import cyder.user.UserFile;
import cyder.utils.NetworkUtil;
import cyder.utils.OSUtil;
import cyder.utils.SecurityUtil;
import cyder.utils.StringUtil;

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
     * Downloads this object's YouTube video.
     *
     * @param inputHandler the base input handler to output updates to. Pass null for no updates
     */
    public void download(BaseInputHandler inputHandler) {
        boolean shouldPrint = inputHandler != null;

        // todo extract logic to smaller methods, need boolean to not interact with consoleFrame
        //  should also be given an input handler if desired to print
    }

    /**
     * Downloads this object's youtube video without any output to the Console.
     */
    public void download() {
        download(null);

        String saveDir = OSUtil.buildPath(Dynamic.PATH,
                Dynamic.USERS.getDirectoryName(), ConsoleFrame.INSTANCE.getUUID(),
                UserFile.MUSIC.getName());

        String extension = "." + PropLoader.getString("ffmpeg_audio_output_format");

        AtomicReference<String> parsedAsciiSaveName = new AtomicReference<>(
                StringUtil.parseNonAscii(NetworkUtil.getUrlTitle(url))
                        .replace("- YouTube", "")
                        .replaceAll(CyderRegexPatterns.windowsInvalidFilenameChars.pattern(), "").trim());

        ConsoleFrame.INSTANCE.getInputHandler().println("Downloading audio as: "
                + parsedAsciiSaveName + extension);

        // remove trailing periods
        while (parsedAsciiSaveName.get().endsWith(".")) {
            parsedAsciiSaveName.set(parsedAsciiSaveName.get().substring(0, parsedAsciiSaveName.get().length() - 1));
        }

        // if for some reason this case happens, account for it
        if (parsedAsciiSaveName.get().isEmpty()) {
            parsedAsciiSaveName.set(SecurityUtil.generateUUID());
        }

        String[] command = {
                AudioUtil.getYoutubeDlCommand(), url,
                FFMPEG_EXTRACT_AUDIO_FLAG,
                FFMPEG_AUDIO_FORMAT_FLAG, PropLoader.getString("ffmpeg_audio_output_format"),
                FFMPEG_OUTPUT_FLAG, new File(saveDir).getAbsolutePath() + OSUtil.FILE_SEP
                + parsedAsciiSaveName + ".%(ext)s"
        };

        downloaded = false;

        CyderProgressUI ui = new CyderProgressUI();
        CyderThreadRunner.submit(() -> {
            try {
                Process proc = Runtime.getRuntime().exec(command);

                BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

                // progress label for this download to update
                CyderProgressBar audioProgress = new CyderProgressBar(
                        CyderProgressBar.HORIZONTAL, 0, 10000);

                ui.setColors(CyderColors.regularPink, CyderColors.regularBlue);
                ui.setAnimationDirection(CyderProgressUI.AnimationDirection.LEFT_TO_RIGHT);
                audioProgress.setUI(ui);
                audioProgress.setMinimum(0);
                audioProgress.setMaximum(10000);
                audioProgress.setBorder(new LineBorder(Color.black, 2));
                audioProgress.setBounds(0, 0, 400, 40);
                audioProgress.setVisible(true);
                audioProgress.setValue(0);
                audioProgress.setOpaque(false);
                audioProgress.setFocusable(false);
                audioProgress.repaint();

                printLabel = new JLabel("\"" + parsedAsciiSaveName + "\"");
                printLabel.setFont(ConsoleFrame.INSTANCE.generateUserFont());
                printLabel.setForeground(CyderColors.vanilla);
                printLabel.setHorizontalAlignment(JLabel.LEFT);
                printLabel.setForeground(ConsoleFrame.INSTANCE.getInputField().getForeground());
                printLabel.setFont(ConsoleFrame.INSTANCE.getInputField().getFont());

                // todo not always, no need to construct if not printing too
                ConsoleFrame.INSTANCE.getInputHandler().println(audioProgress);
                ConsoleFrame.INSTANCE.getInputHandler().println(printLabel);

                String fileSize = null;

                String outputString;

                while ((outputString = stdInput.readLine()) != null) {
                    Matcher updateMatcher = CyderRegexPatterns.updatePattern.matcher(outputString);

                    if (updateMatcher.find()) {
                        float progress = Float.parseFloat(updateMatcher.group(1)
                                .replaceAll("[^0-9.]", ""));
                        audioProgress.setValue((int) ((progress / 100.0) * audioProgress.getMaximum()));

                        fileSize = fileSize == null ? updateMatcher.group(2) : fileSize;

                        this.downloadableName = parsedAsciiSaveName.get();
                        this.downloadableFileSize = fileSize;
                        this.downloadableProgress = progress;
                        this.downloadableRate = updateMatcher.group(3);
                        this.downloadableEta = updateMatcher.group(4);
                        updateLabel();
                    }
                }

                YoutubeUtil.downloadThumbnail(url);

                downloaded = true;

                // todo not always
                ConsoleFrame.INSTANCE.getInputHandler().println("Download complete: saved as "
                        + parsedAsciiSaveName + extension + " and added to audio queue");
                AudioPlayer.addAudioNext(new File(OSUtil.buildPath(
                        saveDir, parsedAsciiSaveName + extension)));
                ui.setColors(CyderColors.regularBlue, CyderColors.regularBlue);
                audioProgress.repaint();
                ui.stopAnimationTimer();
            } catch (Exception e) {
                ExceptionHandler.handle(e);
                ConsoleFrame.INSTANCE.getInputHandler().println("An exception occurred while "
                        + "attempting to download: " + url);
                ui.stopAnimationTimer();
            } finally {
                YoutubeUtil.removeActiveDownload(this);
            }
        }, "YouTube Downloader: " + parsedAsciiSaveName.get());
    }
}
