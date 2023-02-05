package cyder.youtube;

import com.google.common.base.Preconditions;
import cyder.audio.AudioPlayer;
import cyder.audio.AudioUtil;
import cyder.console.Console;
import cyder.constants.CyderColors;
import cyder.constants.CyderRegexPatterns;
import cyder.constants.HtmlTags;
import cyder.enums.Dynamic;
import cyder.enums.Extension;
import cyder.exceptions.FatalException;
import cyder.exceptions.YoutubeException;
import cyder.files.FileUtil;
import cyder.handlers.input.BaseInputHandler;
import cyder.handlers.internal.ExceptionHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.network.NetworkUtil;
import cyder.props.Props;
import cyder.strings.CyderStrings;
import cyder.strings.StringUtil;
import cyder.threads.CyderThreadRunner;
import cyder.ui.UiUtil;
import cyder.ui.button.CyderButton;
import cyder.ui.progress.AnimationDirection;
import cyder.ui.progress.CyderProgressBar;
import cyder.ui.progress.CyderProgressUI;
import cyder.user.UserFile;
import cyder.utils.ImageUtil;
import cyder.utils.OsUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.regex.Matcher;

import static cyder.youtube.YouTubeConstants.*;

/**
 * An object to download an audio and thumbnail file from a singular YouTube video.
 * An instance of this class can represent a singular YouTube video.
 */
public class YouTubeAudioDownload {
    /**
     * The magic number to denote the thumbnail dimensions were not
     * specified by the caller and thus may remain whatever the default is.
     */
    private static final int DIMENSION_TO_BE_DETERMINED = Integer.MAX_VALUE;

    /**
     * The string which could be a link, id, or query.
     */
    private String providedDownloadString;

    /**
     * The name to save the audio download as.
     */
    private String audioDownloadName;

    /**
     * The name to save the thumbnail download as.
     */
    private String thumbnailDownloadName;

    /**
     * The handler to use for printing updates and ui elements.
     */
    private BaseInputHandler printOutputHandler;

    /**
     * The width to crop the thumbnail to.
     */
    private int requestedThumbnailWidth = DIMENSION_TO_BE_DETERMINED;

    /**
     * The height to crop the thumbnail to.
     */
    private int requestedThumbnailHeight = DIMENSION_TO_BE_DETERMINED;

    /**
     * The runnable to invoke when the download is canceled.
     */
    private Runnable onCanceledCallback;

    /**
     * The runnable to invoke when the download completes successfully.
     */
    private Runnable onDownloadedCallback;

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
     * The audio file this object downloaded from YouTube.
     */
    private File audioDownloadFile;

    /**
     * The exit code for the internal download process.
     */
    private int processExitCode = DOWNLOAD_NOT_FINISHED;

    /**
     * The download progress bar to print and update if a valid input handler is provided.
     */
    private CyderProgressBar downloadProgressBar;

    /**
     * The progress bar ui to use for the download progress bar.
     */
    private CyderProgressUI downloadProgressBarUi;

    /**
     * Constructs a new YoutubeDownload object.
     */
    public YouTubeAudioDownload() {
        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * Sets the download type of this download to a video link.
     *
     * @param videoLink the video link
     */
    public void setVideoLink(String videoLink) {
        Preconditions.checkNotNull(videoLink);
        Preconditions.checkArgument(!videoLink.isEmpty());
        Preconditions.checkArgument(!NetworkUtil.readUrl(videoLink).isEmpty());

        this.providedDownloadString = videoLink;
    }

    /**
     * Sets the download type of this download to a video id.
     *
     * @param videoId the video id
     */
    public void setVideoId(String videoId) {
        Preconditions.checkNotNull(videoId);
        Preconditions.checkArgument(!videoId.isEmpty());
        Preconditions.checkArgument(videoId.length() == YouTubeConstants.UUID_LENGTH);

        String videoLink = YouTubeUtil.buildVideoUrl(videoId);
        Preconditions.checkArgument(!NetworkUtil.readUrl(videoLink).isEmpty());

        this.providedDownloadString = videoLink;
    }

    /**
     * Sets the download type of this download to a playlist id.
     *
     * @param playlistId the playlist id
     */
    public void setPlaylistId(String playlistId) {
        Preconditions.checkNotNull(playlistId);
        Preconditions.checkArgument(!playlistId.isEmpty());

        String videoLink = YouTubeConstants.YOUTUBE_PLAYLIST_HEADER + playlistId;
        Preconditions.checkArgument(!NetworkUtil.readUrl(videoLink).isEmpty());

        this.providedDownloadString = videoLink;
    }

    /**
     * Sets the download type of this download to a query.
     *
     * @param query the video link
     * @throws YoutubeException if a likely uuid for the provided query cannot be found
     */
    public void setVideoQuery(String query) throws YoutubeException {
        Preconditions.checkNotNull(query);
        Preconditions.checkArgument(!query.isEmpty());

        Future<String> futureUuid = YouTubeUtil.getMostLikelyUuid(query);
        while (!futureUuid.isDone()) Thread.onSpinWait();

        try {
            this.providedDownloadString = YouTubeUtil.buildVideoUrl(futureUuid.get());
        } catch (Exception e) {
            throw new YoutubeException(e.getMessage());
        }
    }

    /**
     * Sets the provided name as the name to save the .mp3 and .png audio and thumbnail downloads as.
     *
     * @param downloadNames the name to save the downloads as
     */
    public void setDownloadNames(String downloadNames) {
        setAudioDownloadName(downloadNames);
        setThumbnailDownloadName(downloadNames);
    }

    /**
     * Sets the name to save the .mp3 download as.
     *
     * @param audioDownloadName the name to save the .mp3 download as
     */
    public void setAudioDownloadName(String audioDownloadName) {
        Preconditions.checkNotNull(audioDownloadName);
        Preconditions.checkArgument(!audioDownloadName.isEmpty());
        Preconditions.checkArgument(OsUtil.isValidFilename(audioDownloadName));

        this.audioDownloadName = audioDownloadName;
    }

    /**
     * Sets the name to save the .png download as.
     *
     * @param thumbnailDownloadName the name to save the .png download as
     */
    public void setThumbnailDownloadName(String thumbnailDownloadName) {
        Preconditions.checkNotNull(thumbnailDownloadName);
        Preconditions.checkArgument(!thumbnailDownloadName.isEmpty());
        Preconditions.checkArgument(OsUtil.isValidFilename(thumbnailDownloadName));

        this.thumbnailDownloadName = thumbnailDownloadName;
    }

    /**
     * Sets the handler to use for printing updates and ui elements to the Console's.
     */
    public void setPrintOutputToConsole() {
        setPrintOutputHandler(Console.INSTANCE.getInputHandler());
    }

    /**
     * Sets the handler to use for printing updates and ui elements.
     *
     * @param inputHandler the handler to use for printing updates and ui elements
     */
    public void setPrintOutputHandler(BaseInputHandler inputHandler) {
        Preconditions.checkNotNull(inputHandler);

        this.printOutputHandler = inputHandler;
    }

    /**
     * Removes the handler to use for printing updates and ui elements.
     */
    public void removePrintOutputHandler() {
        this.printOutputHandler = null;
    }

    /**
     * Sets the request width to download the thumbnail.
     *
     * @param requestedThumbnailWidth the request width to download the thumbnail
     */
    public void setRequestedThumbnailWidth(int requestedThumbnailWidth) {
        this.requestedThumbnailWidth = requestedThumbnailWidth;
    }

    /**
     * Sets the request height to download the thumbnail.
     *
     * @param requestedThumbnailHeight the request height to download the thumbnail
     */
    public void setRequestedThumbnailHeight(int requestedThumbnailHeight) {
        this.requestedThumbnailHeight = requestedThumbnailHeight;
    }

    /**
     * Sets the requested thumbnail dimensions, that of width and height, to the provided integer value.
     * When the thumbnail is downloaded, the resulting image will have equal width and height.
     * If a dimensional length exceeds that of the original thumbnail, the thumbnail's maximum length is used
     * for both the width and height.
     *
     * @param sideLength the requested image side length
     */
    public void setThumbnailLength(int sideLength) {
        Preconditions.checkArgument(sideLength > 0);

        this.requestedThumbnailWidth = sideLength;
        this.requestedThumbnailHeight = sideLength;
    }

    /**
     * Sets the on download callback.
     *
     * @param onCanceledCallback the on download callback
     */
    public void setOnCanceledCallback(Runnable onCanceledCallback) {
        Preconditions.checkNotNull(onCanceledCallback);

        this.onCanceledCallback = onCanceledCallback;
    }

    /**
     * Sets the on download complete callback.
     *
     * @param onDownloadedCallback the on download complete callback
     */
    public void setOnDownloadedCallback(Runnable onDownloadedCallback) {
        Preconditions.checkNotNull(onDownloadedCallback);

        this.onDownloadedCallback = onDownloadedCallback;
    }

    /**
     * Starts the download of the audio and thumbnail file(s).
     */
    public void downloadAudioAndThumbnail() {
        if (audioDownloadName == null && thumbnailDownloadName == null) {
            initializeAudioAndThumbnailDownloadNames();
        } else {
            if (audioDownloadName == null) {
                initializeAudioDownloadNames();
            }
            if (thumbnailDownloadName == null) {
                initializeThumbnailDownloadName();
            }
        }

        downloadThumbnail();
        downloadAudio();
    }

    /**
     * Starts the download of the audio file(s).
     */
    public void downloadAudio() {
        if (audioDownloadName == null) {
            initializeAudioDownloadNames();
        }

        File userMusicDir = Dynamic.buildDynamic(
                Dynamic.USERS.getFileName(),
                Console.INSTANCE.getUuid(),
                UserFile.MUSIC.getName());

        String ffmpegAudioOutputFormat = Props.ffmpegAudioOutputFormat.getValue();
        String outputExtension = "." + ffmpegAudioOutputFormat;

        String downloadSaveName = YouTubeUtil.getDownloadSaveName(providedDownloadString);

        String youTubeDlOutputName = userMusicDir.getAbsolutePath()
                + OsUtil.FILE_SEP
                + downloadSaveName
                + ".%(ext)s";

        String[] command = {
                AudioUtil.getYoutubeDlCommand(), providedDownloadString,
                YouTubeDlFlag.EXTRACT_AUDIO.getFlag(),
                YouTubeDlFlag.AUDIO_FORMAT.getFlag(), ffmpegAudioOutputFormat,
                YouTubeDlFlag.OUTPUT.getFlag(), youTubeDlOutputName
        };

        YouTubeDownloadManager.INSTANCE.addActiveDownload(this);

        downloadableName = downloadSaveName;

        String threadName = "YouTube"
                + CyderStrings.space + "Downloader, saveName"
                + CyderStrings.colon + CyderStrings.space + downloadSaveName
                + CyderStrings.comma + CyderStrings.space + "uuid"
                + CyderStrings.colon + CyderStrings.space + YouTubeUtil.extractUuid(providedDownloadString);

        CyderThreadRunner.submit(() -> {
            try {
                if (shouldPrintUiElements()) {
                    String audioName = downloadableName + outputExtension;
                    printOutputHandler.println("Downloading audio as"
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
                YouTubeDownloadManager.INSTANCE.removeActiveDownload(this);

                done = true;
                downloading = false;

                if (shouldPrintUiElements()) {
                    cleanUpPrintedUiElements();
                }
            }
        }, threadName);
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
     * The downloaded thumbnail image if present, null otherwise.
     */
    private static BufferedImage downloadedThumbnailImage;

    /**
     * Starts the download of the thumbnail file(s).
     *
     * @throws YoutubeException if an exception occurs when attempting to download/save the thumbnail image file
     */
    public void downloadThumbnail() throws YoutubeException {
        if (thumbnailDownloadName == null) {
            initializeThumbnailDownloadName();
        }

        String uuid = YouTubeUtil.extractUuid(providedDownloadString);

        Optional<BufferedImage> optionalThumbnail = YouTubeUtil.getMaxResolutionThumbnail(uuid);
        BufferedImage thumbnailImage = optionalThumbnail.orElseThrow(
                () -> new FatalException("Could not get max resolution or standard resolution"
                        + " thumbnail for provided download string: " + providedDownloadString));

        if (requestedThumbnailWidth == DIMENSION_TO_BE_DETERMINED
                && requestedThumbnailHeight == DIMENSION_TO_BE_DETERMINED) {
            thumbnailImage = ImageUtil.cropToMaximumSizeSquare(thumbnailImage);
        } else {
            int w = Math.min(thumbnailImage.getWidth(), requestedThumbnailWidth);
            int h = Math.min(thumbnailImage.getHeight(), requestedThumbnailHeight);

            int xOffset = 0;
            if (w < requestedThumbnailWidth) {
                xOffset = (requestedThumbnailWidth - w) / 2;
            }

            int yOffset = 0;
            if (h < requestedThumbnailHeight) {
                yOffset = (requestedThumbnailHeight - h) / 2;
            }

            thumbnailImage = ImageUtil.cropImage(thumbnailImage, xOffset, yOffset, w, h);
        }

        File saveFile = Dynamic.buildDynamic(Dynamic.USERS.getFileName(), Console.INSTANCE.getUuid(),
                UserFile.MUSIC.getName(), UserFile.ALBUM_ART, thumbnailDownloadName + Extension.PNG.getExtension());

        downloadedThumbnailImage = thumbnailImage;

        try {
            if (!ImageIO.write(thumbnailImage, Extension.PNG.getExtensionWithoutPeriod(), saveFile)) {
                throw new IOException("Failed to write album art to file: " + saveFile);
            }
        } catch (IOException e) {
            throw new YoutubeException(e.getMessage());
        }
    }

    /**
     * Initializes the name(s) of the audio and thumbnail download(s).
     */
    private void initializeAudioAndThumbnailDownloadNames() {
        String safeDownloadName = YouTubeUtil.getDownloadSaveName(providedDownloadString);

        audioDownloadName = safeDownloadName;
        thumbnailDownloadName = safeDownloadName;
    }

    /**
     * Initializes the name(s) of the audio download(s).
     */
    private void initializeAudioDownloadNames() {
        audioDownloadName = YouTubeUtil.getDownloadSaveName(providedDownloadString);
    }

    /**
     * Initializes the name(s) of the thumbnail download(s).
     */
    private void initializeThumbnailDownloadName() {
        thumbnailDownloadName = YouTubeUtil.getDownloadSaveName(providedDownloadString);
    }

    /**
     * Returns whether ui elements should be printed for this Youtube video download.
     *
     * @return whether ui elements should be printed for this Youtube video download
     */
    private boolean shouldPrintUiElements() {
        return printOutputHandler != null;
    }

    /**
     * The actions to invoke when an exception is thrown inside of the download thread.
     *
     * @param e the thrown exception
     */
    private void onDownloadProcessException(Exception e) {
        ExceptionHandler.handle(e);

        if (shouldPrintUiElements()) {
            printOutputHandler.println("An exception occurred while attempting to download, url: "
                    + providedDownloadString);
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

            AudioPlayer.playAudioNext(audioDownloadFile);

            if (onDownloadedCallback != null) {
                onDownloadedCallback.run();
            }

            if (shouldPrintUiElements()) {
                printOutputHandler.println("Download complete: saved as"
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
                printOutputHandler.println("Canceled download due to user request");
            } else {
                printOutputHandler.println("Failed to download audio");
            }
        }
    }

    /**
     * Creates and prints the progress bar, label, and cancel button to the linked input handler.
     */
    private void createAndPrintUiElements() {
        Preconditions.checkState(printOutputHandler != null);

        downloadProgressBar = new CyderProgressBar(
                CyderProgressBar.HORIZONTAL,
                downloadProgressMin,
                downloadProgressMax);

        downloadProgressBarUi = new CyderProgressUI();
        downloadProgressBarUi.setAnimationColors(CyderColors.regularPink, CyderColors.regularBlue);
        downloadProgressBarUi.setAnimationDirection(AnimationDirection.LEFT_TO_RIGHT);

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

        printOutputHandler.println(downloadProgressBar);
        printOutputHandler.println(downloadProgressLabel);
        if (downloadedThumbnailImage != null) {
            printDownloadedThumbnail();
        }
        printOutputHandler.println(createCancelDownloadButton());

        printOutputHandler.addPrintedLabel(downloadProgressLabel);
    }

    private void printDownloadedThumbnail() {
        int downloadedThumbnailImagePrintLength = 150;
        int thumbnailPadding = 5;

        downloadedThumbnailImage = ImageUtil.ensureFitsInBounds(downloadedThumbnailImage,
                new Dimension(downloadedThumbnailImagePrintLength, downloadedThumbnailImagePrintLength));

        int parentWidth = downloadedThumbnailImage.getWidth() + 2 * thumbnailPadding;
        int parentHeight = downloadedThumbnailImage.getHeight() + 2 * thumbnailPadding;

        JLabel parent = new JLabel(UiUtil.generateTextForCustomComponent(10)) {
            @Override
            public void paint(Graphics g) {
                g.setColor(CyderColors.navy);
                g.fillRect(0, 0, parentWidth, parentHeight);

                super.paint(g);
            }
        };
        parent.setSize(parentWidth, parentHeight);

        JLabel imageLabel = new JLabel();
        imageLabel.setIcon(ImageUtil.toImageIcon(downloadedThumbnailImage));
        imageLabel.setBounds(thumbnailPadding, thumbnailPadding,
                downloadedThumbnailImage.getWidth(), downloadedThumbnailImage.getHeight());
        parent.add(imageLabel);

        printOutputHandler.println(parent);
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
        } else if (downloaded) {
            AudioPlayer.showGui(getAudioDownloadFile());
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
            buttonText = PLAY;
        } else if (canceled) {
            buttonText = CANCELED;
        } else {
            buttonText = FAILED;
        }
        cancelButton.setText(buttonText);
    }

    /**
     * Cleans up .part or any other files left over by YouTube-dl after the user canceled the download.
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
                Logger.log(LogTag.SYSTEM_IO, "Could not delete file resulting from YouTube "
                        + "download operation canceled, location=" + parentDirectory.getAbsolutePath()
                        + ", name=" + nameWithoutExtension);
            }
        }
    }
}
