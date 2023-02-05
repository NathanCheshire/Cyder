package cyder.files;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.audio.player.AudioPlayer;
import cyder.enums.Extension;
import cyder.handlers.external.DirectoryViewer;
import cyder.handlers.external.ImageViewer;
import cyder.handlers.external.TextViewer;

import java.io.File;
import java.util.concurrent.Future;
import java.util.function.Function;

/**
 * A Cyder handler for a specific file type.
 */
public enum CyderFileHandler {
    TEXT(file -> {
        return FileUtil.getExtension(file).equals(Extension.TXT.getExtension());
    }, file -> {
        return TextViewer.getInstance(file).showGui();
    }),
    AUDIO(FileUtil::isSupportedAudioExtension, (file) -> {
        try {
            AudioPlayer.showGui(file);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }),
    IMAGE(FileUtil::isSupportedImageExtension, file -> {
        Future<Boolean> futureBoolean = ImageViewer.getInstance(file).showGui();
        while (!futureBoolean.isDone()) Thread.onSpinWait();
        try {
            return futureBoolean.get();
        } catch (Exception ignored) {
            return false;
        }
    }),
    DIRECTORY(File::isDirectory, DirectoryViewer::showGui);

    /**
     * The function to determine whether a Cyder file handler exists for the provided file type.
     */
    private final Function<File, Boolean> cyderHandlerExists;

    /**
     * The function to open the provided file using this Cyder file handler
     * if the file is valid and exists and return whether the opening action succeeded.
     */
    private final Function<File, Boolean> openFileUsingCyderHandler;

    CyderFileHandler(Function<File, Boolean> cyderHandlerExists, Function<File, Boolean> openFileUsingCyderHandler) {
        this.cyderHandlerExists = cyderHandlerExists;
        this.openFileUsingCyderHandler = openFileUsingCyderHandler;
    }

    /**
     * Returns whether this handler should be used for the provided file.
     *
     * @param file the file this handler might be used to open if valid
     * @return whether this handler should be used for the provided file
     */
    public boolean shouldUseForFile(File file) {
        Preconditions.checkNotNull(file);

        return cyderHandlerExists.apply(file);
    }

    /**
     * Opens the provided file using this Cyder handler.
     *
     * @param file the file to open
     * @return whether the file opening was successful
     */
    @CanIgnoreReturnValue
    public boolean open(File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());
        Preconditions.checkState(cyderHandlerExists.apply(file));

        return openFileUsingCyderHandler.apply(file);
    }
}
