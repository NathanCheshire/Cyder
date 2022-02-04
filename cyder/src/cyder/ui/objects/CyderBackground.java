package cyder.ui.objects;

import cyder.handlers.internal.ExceptionHandler;
import cyder.utilities.ImageUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * A background for the ConsoleFrame.
 */
public class CyderBackground {
    /**
     * The file associated with this background.
     */
    private File referenceFile;

    /**
     * Constructs a new CyderBackground from the provided file if it can be read as an image.
     *
     * @param referenceFile the file to use as the background
     * @throws IllegalArgumentException if the provided file is invalid
     */
    public CyderBackground(File referenceFile) {
        if (!referenceFile.exists())
            throw new IllegalArgumentException("Provided file is null");

        try {
            ImageIO.read(referenceFile);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
            throw new IllegalArgumentException("Provided file is not  valid image file");
        }

        this.referenceFile = referenceFile;
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
        return ImageUtil.getImageIcon(generateBufferedImage());
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        else if (!(o instanceof CyderBackground))
            return false;
        else {
            return ImageUtil.compareImage(referenceFile, ((CyderBackground) o).getReferenceFile());
        }
    }
}
