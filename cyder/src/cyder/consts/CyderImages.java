package cyder.consts;

import cyder.utilities.ImageUtil;
import cyder.utilities.SystemUtil;

import javax.swing.*;
import java.awt.*;

public class CyderImages {
    public static final ImageIcon minimizeIcon = new ImageIcon("static/pictures/windowicons/minimize1.png");
    public static final ImageIcon minimizeIconHover = new ImageIcon("static/pictures/windowicons/minimize2.png");

    public static final ImageIcon changeSizeIcon = new ImageIcon("static/pictures/icons/changesize1.png");
    public static final ImageIcon changeSizeIconHover = new ImageIcon("static/pictures/icons/changesize2.png");

    public static final ImageIcon pinIcon = new ImageIcon("static/pictures/icons/pin1.png");
    public static final ImageIcon pinIconHover = new ImageIcon("static/pictures/icons/pin2.png");
    public static final ImageIcon pinIconHoverPink = new ImageIcon("static/pictures/icons/pin3.png");

    public static final ImageIcon closeIcon = new ImageIcon("static/pictures/windowicons/Close1.png");
    public static final ImageIcon closeIconHover = new ImageIcon("static/pictures/windowicons/Close2.png");

    /**
     * A default image with dimensions 1000x1000
     */
    public static final ImageIcon defaultBackground = generateDefaultBackground(1000,1000);

    /**
     * A default image that spans the size of the primar display
     */
    public static final ImageIcon defaultBackgroundLarge = generateDefaultBackground(
            Math.max(SystemUtil.getScreenWidth(), SystemUtil.getScreenHeight()),
            Math.max(SystemUtil.getScreenWidth(), SystemUtil.getScreenHeight()));

    /**
     * Instantiation of images class not allowed
     */
    private CyderImages() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    public static ImageIcon generateDefaultBackground(int width, int height) {
        return new ImageIcon(ImageUtil.getImageGradient(width, height,
                new Color(252,245,255),
                new Color(164,154,187),
                new Color(249, 233, 241)));
    }
}
