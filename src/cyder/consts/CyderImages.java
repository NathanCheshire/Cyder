package cyder.consts;

import cyder.utilities.ImageUtil;
import cyder.utilities.SystemUtil;

import javax.swing.*;
import java.awt.*;

public class CyderImages {
    public static final ImageIcon minimizeIcon = new ImageIcon("static/pictures/windowicons/minimize1.png");
    public static final ImageIcon minimizeIconHover = new ImageIcon("static/pictures/windowicons/minimize2.png");
    public static final ImageIcon closeIcon = new ImageIcon("static/pictures/windowicons/Close1.png");
    public static final ImageIcon closeIconHover = new ImageIcon("static/pictures/windowicons/Close2.png");
    public static final ImageIcon defaultBackground = new ImageIcon(ImageUtil.getImageGradient(1000,1000,
            new Color(252,245,255),
            new Color(164,154,187),
            new Color(249, 233, 241)));
    public static final ImageIcon defaultBackgroundLarge = new ImageIcon(ImageUtil.getImageGradient(
            Math.max(SystemUtil.getScreenWidth(), SystemUtil.getScreenHeight()),
            Math.max(SystemUtil.getScreenWidth(), SystemUtil.getScreenHeight()),
            new Color(252,245,255),
            new Color(164,154,187),
            new Color(249, 233, 241)));

    private CyderImages() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }
}
