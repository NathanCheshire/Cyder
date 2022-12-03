package cyder.files;

import com.google.common.base.Preconditions;
import cyder.audio.AudioPlayer;
import cyder.enums.Extension;
import cyder.handlers.external.DirectoryViewer;
import cyder.handlers.external.PhotoViewer;
import cyder.handlers.external.TextViewer;

import java.io.File;
import java.util.function.Consumer;
import java.util.function.Function;

/** A Cyder handler for a specific file type. */
public enum CyderFileHandler {
    TEXT(file -> {
        return FileUtil.getExtension(file).equals(Extension.TXT.getExtension());
    }, file -> {
        TextViewer.getInstance(file).showGui();
    }),
    AUDIO(FileUtil::isSupportedAudioExtension, AudioPlayer::showGui),
    IMAGE(FileUtil::isSupportedImageExtension, file -> PhotoViewer.getInstance(file).showGui()),
    DIRECTORY(File::isDirectory, DirectoryViewer::showGui);

    /** The function to determine whether a Cyder file handler exists for the provided file type. */
    private final Function<File, Boolean> cyderHandlerExists;

    /** The consumer to open the provided file using this Cyder file handler if the file is valid and exists. */
    private final Consumer<File> openFileUsingCyderHandler;

    CyderFileHandler(Function<File, Boolean> cyderHandlerExists, Consumer<File> openFileUsingCyderHandler) {
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
     */
    public void open(File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());
        Preconditions.checkState(cyderHandlerExists.apply(file));

        openFileUsingCyderHandler.accept(file);
    }
}
