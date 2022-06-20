package cyder.records;

import com.google.common.base.Preconditions;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;
import cyder.utils.ImageUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * A background for the ConsoleFrame.
 */
public record ConsoleBackground(File referenceFile) {
    /**
     * Constructs a new CyderBackground from the provided file if it can be read as an image.
     *
     * @param referenceFile the file to use as the background
     * @throws IllegalArgumentException if the provided file is invalid, null, or does not exist
     */
    public ConsoleBackground(File referenceFile) {
        Preconditions.checkNotNull(referenceFile);
        Preconditions.checkArgument(referenceFile.exists());
        Preconditions.checkArgument(ImageUtil.isValidImage(referenceFile));

        this.referenceFile = referenceFile;
        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    /**
     * Returns a generated buffered image from the reference file.
     *
     * @return a generated buffered image from the reference file
     */
    public BufferedImage generateBufferedImage() {
        try {
            return ImageIO.read(referenceFile);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return null;
    }

    /**
     * Returns a generated image icon from the background file.
     *
     * @return a generated image icon from the background file
     */
    public ImageIcon generateImageIcon() {
        return ImageUtil.toImageIcon(generateBufferedImage());
    }

    /**
     * Returns whether the reference file still exists, can be read, and is an image.
     *
     * @return whether the reference file still exists, can be read, and is an image
     */
    public boolean validate() {
        return referenceFile.exists() && generateBufferedImage() != null;
    }
}
