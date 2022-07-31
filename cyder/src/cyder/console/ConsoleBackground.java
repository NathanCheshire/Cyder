package cyder.console;

import com.google.common.base.Preconditions;
import cyder.exceptions.FatalException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;
import cyder.utils.ImageUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * A background for the Console.
 */
public class ConsoleBackground {
    /**
     * the file referenced by this object.
     */
    private File referenceFile;

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
     * Returns the file referenced by this object.
     *
     * @return the file referenced by this object
     */
    public File getReferenceFile() {
        return referenceFile;
    }

    /**
     * Sets the file referenced by this object.
     *
     * @param referenceFile the file referenced by this object
     */
    public void setReferenceFile(File referenceFile) {
        Preconditions.checkNotNull(referenceFile);
        Preconditions.checkArgument(referenceFile.exists());
        Preconditions.checkArgument(ImageUtil.isValidImage(referenceFile));

        this.referenceFile = referenceFile;
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

        throw new FatalException("Could not general buffered image from reference file: "
                + referenceFile.getAbsolutePath());
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
