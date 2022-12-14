package cyder.youtube;

import com.google.common.base.Preconditions;
import cyder.audio.AudioPlayer;
import cyder.audio.AudioUtil;
import cyder.console.Console;
import cyder.constants.CyderColors;
import cyder.constants.CyderRegexPatterns;
import cyder.constants.HtmlTags;
import cyder.enums.Dynamic;
import cyder.exceptions.IllegalMethodException;
import cyder.files.FileUtil;
import cyder.handlers.input.BaseInputHandler;
import cyder.handlers.internal.ExceptionHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.props.Props;
import cyder.strings.CyderStrings;
import cyder.strings.StringUtil;
import cyder.threads.CyderThreadRunner;
import cyder.ui.button.CyderButton;
import cyder.ui.progress.CyderProgressBar;
import cyder.ui.progress.CyderProgressUI;
import cyder.user.UserFile;
import cyder.utils.OsUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.regex.Matcher;

import static cyder.youtube.YoutubeConstants.*;

/**
 * A utility class for downloading a video's audio and/or video from YouTube.
 */
public class YoutubeDownload {
    /**
     * The width of the printed progress bar if enabled. This actually doesn't matter since
     * the progress bar printed to a JTextPane will take the entire width of the pane.
     */
    private static final int processBarWidth = 400;

    /**
     * The name of this download object.
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
     * The label this class will print and update with statistics about the download.
     */
    private JLabel downloadProgressLabel;

    /**
     * The button used to cancel the download.
     */
    private CyderButton cancelButton;

    /**
     * The url of the youtube video to download.
     */
    private final String url;

    /**
     * The runnable to invoke when the download is canceled.
     */
    private Runnable onCanceledCallback;

    /**
     * The runnable to invoke when the download completes successfully.
     */
    private Runnable onDownloadedCallback;

    /**
     * The audio file this object downloaded from YouTube.
     */
    private File audioDownloadFile;

    /**
     * The exit code for the internal download process.
     */
    private int processExitCode = DOWNLOAD_NOT_FINISHED;

    /**
     * The type of this download.
     */
    private final DownloadType downloadType;

    /**
     * Suppress default constructor.
     */
    private YoutubeDownload() {
        throw new IllegalMethodException(CyderStrings.ILLEGAL_CONSTRUCTOR);
    }

    /**
     * Constructs a new YoutubeDownload object.
     *
     * @param url the url of the video to download
     */
    public YoutubeDownload(String url, DownloadType downloadType) {
        Preconditions.checkNotNull(url);
        Preconditions.checkArgument(!url.isEmpty());
        Preconditions.checkState(AudioUtil.youtubeDlInstalled());
        Preconditions.checkNotNull(downloadType);

        this.url = url;
        this.downloadType = downloadType;
    }

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
     * Sets the callback to invoke when/if a cancel action is invoked.
     *
     * @param onCanceledCallback the callback to invoke when/if a cancel action is invoked
     */
    public void setOnCanceledCallback(Runnable onCanceledCallback) {
        Preconditions.checkNotNull(onCanceledCallback);

        this.onCanceledCallback = onCanceledCallback;
    }

    /**
     * Sets the callback to invoke when a download completes.
     *
     * @param onDownloadedCallback the callback to invoke when a download completes
     */
    public void setOnDownloadedCallback(Runnable onDownloadedCallback) {
        Preconditions.checkNotNull(onDownloadedCallback);

        this.onDownloadedCallback = onDownloadedCallback;
    }

    /**
     * Returns the file this object downloaded from YouTube.
     *
     * @return the file this object downloaded from YouTube
     */
    public File getAudioDownloadFile() {
        Preconditions.checkState(!isCanceled());
        Preconditions.checkState(isDone());
        Preconditions.checkState(isDownloaded());
        Preconditions.checkNotNull(audioDownloadFile);

        return audioDownloadFile;
    }

    /**
     * Updates the download progress label text.
     */
    public void updateProgressLabelText() {
        downloadProgressLabel.setText(HtmlTags.openingHtml + downloadableName
                + HtmlTags.breakTag + "File size: " + downloadableFileSize
                + HtmlTags.breakTag + "Progress: " + downloadableProgress + "%"
                + HtmlTags.breakTag + "Rate: " + downloadableRate
                + HtmlTags.breakTag + "Eta: " + downloadableEta
                + HtmlTags.closingHtml);
        downloadProgressLabel.revalidate();
        downloadProgressLabel.repaint();
        downloadProgressLabel.setHorizontalAlignment(JLabel.LEFT);
    }

    /**
     * The download progress bar to print and update if a valid input handler is provided.
     */
    private CyderProgressBar downloadProgressBar;

    /**
     * The progress bar ui to use for the download progress bar.
     */
    private CyderProgressUI downloadProgressBarUi;

    /**
     * The input handler to print updates to if not null;
     */
    private BaseInputHandler inputHandler;

    /**
     * Returns whether ui elements should be printed for this download.
     *
     * @return whether ui elements should be printed for this download
     */
    private boolean shouldPrintUiElements() {
        return inputHandler != null;
    }

    /**
     * Sets the input handler set for this YouTube download.
     *
     * @param inputHandler the input handler set for this YouTube download
     */
    public void setInputHandler(BaseInputHandler inputHandler) {
        Preconditions.checkNotNull(inputHandler);

        this.inputHandler = inputHandler;
    }

    /**
     * Downloads this object's YouTube video audio and/or video.
     */
    public void downloadAudioAndThumbnail() {
        Preconditions.checkState(!done, "Object attempted to download previously");

        File userMusicDir = Dynamic.buildDynamic(
                Dynamic.USERS.getFileName(),
                Console.INSTANCE.getUuid(),
                UserFile.MUSIC.getName());

        String ffmpegAudioOutputFormat = Props.ffmpegAudioOutputFormat.getValue();
        String outputExtension = "." + ffmpegAudioOutputFormat;

        String downloadSaveName = YoutubeUtil.getDownloadSaveName(url);

        String youtubeDlOutputName = userMusicDir.getAbsolutePath()
                + OsUtil.FILE_SEP + downloadSaveName + ".%(ext)s";

        String[] command = {
                AudioUtil.getYoutubeDlCommand(), url,
                YoutubeDlFlag.EXTRACT_AUDIO.getFlag(),
                YoutubeDlFlag.AUDIO_FORMAT.getFlag(), ffmpegAudioOutputFormat,
                YoutubeDlFlag.OUTPUT.getFlag(), youtubeDlOutputName
        };

        YoutubeDownloadManager.INSTANCE.addActiveDownload(this);

        downloadableName = downloadSaveName;

        String threadName = "YouTube"
                + CyderStrings.space + downloadType.getRepresentation()
                + CyderStrings.space + "Downloader, saveName"
                + CyderStrings.colon + CyderStrings.space + downloadSaveName
                + CyderStrings.comma + CyderStrings.space + "uuid"
                + CyderStrings.colon + CyderStrings.space + YoutubeUtil.extractUuid(url);

        CyderThreadRunner.submit(() -> {
            try {
                if (shouldPrintUiElements()) {
                    String types = downloadType.getRepresentation();
                    String audioName = downloadableName + outputExtension;
                    inputHandler.println("Downloading"
                            + CyderStrings.space + types
                            + CyderStrings.space + "as"
                            + CyderStrings.colon + CyderStrings.space + audioName);

                    createAndPrintUiElements();
                }

                downloading = true;

                Process process = Runtime.getRuntime().exec(command);
                BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String outputString;

                while ((outputString = stdInput.readLine()) != null) {
                    if (isCanceled()) {
                        process.destroy();
                        cleanUpFromCancel(userMusicDir, downloadableName);
                        if (onDownloadedCallback != null) onCanceledCallback.run();
                        break;
                    }

                    updateUiElementsFromDownloadOutputString(outputString);
                }

                processExitCode = process.waitFor();

                audioDownloadFile = OsUtil.buildFile(userMusicDir.getAbsolutePath(),
                        downloadableName + outputExtension);
                onDownloadProcessFinished(audioDownloadFile);
            } catch (Exception e) {
                onDownloadProcessException(e);
            } finally {
                YoutubeDownloadManager.INSTANCE.removeActiveDownload(this);

                done = true;
                downloading = false;

                if (shouldPrintUiElements()) {
                    cleanUpPrintedUiElements();
                }
            }
        }, threadName);
    }

    /**
     * The actions to invoke when an exception is thrown inside of the download thread.
     *
     * @param e the thrown exception
     */
    private void onDownloadProcessException(Exception e) {
        ExceptionHandler.handle(e);

        if (shouldPrintUiElements()) {
            inputHandler.println("An exception occurred while attempting to download, url: " + url);
        }
    }

    /**
     * The actions to invoke when the download process exits.
     * This does not indicate success or failure of the process.
     *
     * @param audioDownloadFile the file pointer the download file would be saved to
     */
    private void onDownloadProcessFinished(File audioDownloadFile) {
        if (processExitCode != SUCCESSFUL_EXIT_CODE) {
            onDownloadFailed();
        } else if (!isCanceled()) {
            downloaded = true;

            // todo thumbnail download somewhere
            AudioPlayer.addAudioNext(audioDownloadFile);

            if (onDownloadedCallback != null) {
                onDownloadedCallback.run();
            }

            if (shouldPrintUiElements()) {
                inputHandler.println("Download complete: saved as"
                        + CyderStrings.space + downloadableName + CyderStrings.space
                        + "and added to audio queue");
            }
        }
    }

    /**
     * Updates the ui elements and encapsulated variables from the process download output.
     * Parsed members from the output string include:
     * <ul>
     *     <li>Download progress</li>
     *     <li>Download file size</li>
     *     <li>Download rate</li>
     *     <li>Download eta</li>
     * </ul>
     *
     * @param outputString the string output to the process' standard output
     */
    private void updateUiElementsFromDownloadOutputString(String outputString) {
        Matcher updateMatcher = CyderRegexPatterns.updatePattern.matcher(outputString);

        if (updateMatcher.find()) {
            String progressPart = updateMatcher.group(progressIndex);
            float progress = Float.parseFloat(progressPart
                    .replaceAll(CyderRegexPatterns.nonNumberAndPeriodRegex, ""));
            downloadableProgress = progress;

            downloadableFileSize = updateMatcher.group(sizeIndex);
            downloadableRate = updateMatcher.group(rateIndex);
            downloadableEta = updateMatcher.group(etaIndex);

            if (shouldPrintUiElements() && downloadProgressBar != null) {
                int value = (int) ((progress / 100.0f) * downloadProgressBar.getMaximum());
                downloadProgressBar.setValue(value);
                updateProgressLabelText();
            }
        }
    }

    /**
     * The actions to invoke when the download process ends without a successful exit code.
     */
    private void onDownloadFailed() {
        if (shouldPrintUiElements()) {
            if (isCanceled()) {
                inputHandler.println("Canceled download due to user request");
            } else {
                inputHandler.println("Failed to download audio");
            }
        }
    }

    /**
     * Creates and prints the progress bar, label, and cancel button to the linked input handler.
     */
    private void createAndPrintUiElements() {
        Preconditions.checkState(inputHandler != null);

        downloadProgressBar = new CyderProgressBar(
                CyderProgressBar.HORIZONTAL,
                downloadProgressMin,
                downloadProgressMax);

        downloadProgressBarUi = new CyderProgressUI();
        downloadProgressBarUi.setAnimationColors(CyderColors.regularPink, CyderColors.regularBlue);
        downloadProgressBarUi.setAnimationDirection(CyderProgressUI.AnimationDirection.LEFT_TO_RIGHT);

        downloadProgressBar.setUI(downloadProgressBarUi);
        downloadProgressBar.setMinimum(downloadProgressMin);
        downloadProgressBar.setMaximum(downloadProgressMax);
        downloadProgressBar.setBorder(new LineBorder(Color.black, 2));
        downloadProgressBar.setBounds(0, 0, processBarWidth, 40);
        downloadProgressBar.setVisible(true);
        downloadProgressBar.setValue(0);
        downloadProgressBar.setOpaque(false);
        downloadProgressBar.setFocusable(false);
        downloadProgressBar.repaint();

        downloadProgressLabel = new JLabel(CyderStrings.quote + downloadableName + CyderStrings.quote);
        downloadProgressLabel.setFont(Console.INSTANCE.generateUserFont());
        downloadProgressLabel.setHorizontalAlignment(JLabel.LEFT);
        downloadProgressLabel.setForeground(Console.INSTANCE.getInputField().getForeground());

        inputHandler.println(downloadProgressBar);
        inputHandler.println(downloadProgressLabel);
        inputHandler.println(createCancelDownloadButton());

        inputHandler.addPrintedLabel(downloadProgressLabel);
    }

    /**
     * Returns a button which can be used to cancel and clean up the download.
     *
     * @return a button which can be used to cancel and clean up the download
     */
    private CyderButton createCancelDownloadButton() {
        cancelButton = new CyderButton();
        cancelButton.setLeftTextPadding(StringUtil.generateSpaces(5));
        cancelButton.setRightTextPadding(StringUtil.generateSpaces(4));
        cancelButton.setText(CANCEL);
        cancelButton.setFont(Console.INSTANCE.getInputField().getFont());
        cancelButton.addActionListener(e -> onCancelButtonPressed());

        return cancelButton;
    }

    /**
     * The actions to invoke when the cancel button is pressed.
     */
    private void onCancelButtonPressed() {
        if (!isCanceled() && isDownloading()) {
            cancel();
            cancelButton.setText(CANCELED);
        }
    }

    /**
     * Cleans up the printed ui elements.
     */
    private void cleanUpPrintedUiElements() {
        Color resultColor = downloaded
                ? CyderColors.regularBlue
                : CyderColors.regularRed;

        downloadProgressBarUi.setAnimationColor(resultColor);
        downloadProgressBarUi.stopAnimationTimer();

        downloadProgressBar.repaint();

        refreshCancelButtonText();
    }

    /**
     * Refreshes the cancel button text.
     */
    private void refreshCancelButtonText() {
        String buttonText;
        if (downloaded) {
            buttonText = DOWNLOADED;
        } else if (canceled) {
            buttonText = CANCELED;
        } else {
            buttonText = FAILED;
        }
        cancelButton.setText(buttonText);
    }

    /**
     * Cleans up .part or any other files left over by youtube-dl after the user canceled the download.
     *
     * @param parentDirectory      the directory the file would have been downloaded to
     * @param nameWithoutExtension the file name without the extension, anything that starts with this will be deleted
     */
    private void cleanUpFromCancel(File parentDirectory, String nameWithoutExtension) {
        Preconditions.checkNotNull(parentDirectory);
        Preconditions.checkArgument(parentDirectory.isDirectory());
        Preconditions.checkNotNull(nameWithoutExtension);
        Preconditions.checkArgument(!nameWithoutExtension.isEmpty());

        File[] children = parentDirectory.listFiles();
        if (children == null || children.length == 0) return;

        for (File child : children) {
            if (!FileUtil.getFilename(child).startsWith(nameWithoutExtension)) continue;

            if (!OsUtil.deleteFile(child)) {
                Logger.log(LogTag.SYSTEM_IO, "Could not delete file resulting from youtube "
                        + "download operation canceled, location=" + parentDirectory.getAbsolutePath()
                        + ", name=" + nameWithoutExtension);
            }
        }
    }
}
