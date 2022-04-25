package cyder.ui.objects;

import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;
import cyder.utilities.ImageUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;

/**
 * A background for the ConsoleFrame.
 */
public class CyderBackground {
    /**
     * The file associated with this background.
     */
    private final File referenceFile;

    /**
     * Constructs a new CyderBackground from the provided file if it can be read as an image.
     *
     * @param referenceFile the file to use as the background
     * @throws IllegalArgumentException if the provided file is invalid
     */
    public CyderBackground(File referenceFile) {
        if (!referenceFile.exists())
            throw new IllegalArgumentException("Provided file is null");
        if (!ImageUtil.isValidImage(referenceFile))
            throw new IllegalArgumentException("Provided file is not a valid image file");

        this.referenceFile = referenceFile;
        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    /**
     * Returns the reference file associated with this CyderBackground.
     *
     * @return the reference file associated with this CyderBackground
     */
    public File getReferenceFile() {
        return referenceFile;
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

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        else if (!(o instanceof CyderBackground))
            return false;
        else {
            try {
                return Arrays.equals(Files.readAllBytes(getReferenceFile().toPath()),
                        Files.readAllBytes(((CyderBackground) o).getReferenceFile().toPath()));
            } catch (Exception e) {
                ExceptionHandler.handle(e);
                return false;
            }
        }
    }
}
