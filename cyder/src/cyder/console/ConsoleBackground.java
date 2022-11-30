package cyder.console;

import com.google.common.base.Preconditions;
import cyder.exceptions.FatalException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.ui.frame.CyderFrame;
import cyder.utils.ImageUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/** A background for the Console. */
public class ConsoleBackground {
    /** the file referenced by this object. */
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

        setReferenceFile(referenceFile);
        Logger.log(LogTag.OBJECT_CREATION, this);
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

    /** The error message for the fatal exception to throw if a buffered image cannot be generated. */
    private static final String COULD_NOT_GENERATE = "Could not general buffered image from reference file: ";

    /**
     * Returns a generated buffered image from the reference file.
     *
     * @return a generated buffered image from the reference file
     */
    public BufferedImage generateBufferedImage() {
        BufferedImage image = null;

        try {
            image = ImageUtil.read(referenceFile);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        if (image == null) {
            throw new FatalException(COULD_NOT_GENERATE + referenceFile.getAbsolutePath());
        }

        CyderFrame console = Console.INSTANCE.getConsoleCyderFrame();
        if (console != null) {
            Rectangle monitorDimensions = Console.INSTANCE.getConsoleCyderFrame().getMonitorBounds();
            return ImageUtil.ensureFitsInBounds(image,
                    new Dimension((int) monitorDimensions.getWidth(), (int) monitorDimensions.getHeight()));
        } else {
            return image;
        }
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
    public boolean isValid() {
        return referenceFile.exists() && generateBufferedImage() != null;
    }
}
