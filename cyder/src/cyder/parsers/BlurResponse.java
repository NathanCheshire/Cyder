package cyder.parsers;

import com.google.gson.annotations.SerializedName;
import cyder.handlers.internal.ExceptionHandler;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Optional;

/**
 * A POST response parser for a blur request.
 */
public class BlurResponse {
    /**
     * The path to the blurred image.
     */
    @SerializedName("image")
    private String imagePath;

    /**
     * Constructs a new blur response object.
     *
     * @param imagePath the path to the blurred image.
     */
    public BlurResponse(String imagePath) {
        this.imagePath = imagePath;
    }

    /**
     * Returns the path to the blurred image.
     *
     * @return the path to the blurred image
     */
    public String getImagePath() {
        return imagePath;
    }

    /**
     * Sets the path to the blurred image.
     *
     * @param imagePath Sets the path to the blurred image
     */
    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    /**
     * Generates a buffered image based on the blurred image path.
     *
     * @return the blurred buffered image
     */
    public Optional<BufferedImage> generateImage() {
        Optional<BufferedImage> ret = Optional.empty();

        try {
            ret = Optional.of(ImageIO.read(new File(imagePath)));
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return ret;
    }

    /**
     * Generates a reference to the blurred file.
     *
     * @return a reference to the blurred file
     */
    public Optional<File> generateFileReference() {
        if (imagePath != null) {
            return Optional.of(new File(imagePath));
        } else {
            return Optional.empty();
        }
    }
}
