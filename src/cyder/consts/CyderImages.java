package cyder.consts;

import cyder.utilities.ImageUtil;

import javax.swing.*;
import java.awt.*;

public class CyderImages {
    public static final ImageIcon minimizeIcon = new ImageIcon("sys/pictures/windowicons/minimize1.png");
    public static final ImageIcon minimizeIconHover = new ImageIcon("sys/pictures/windowicons/minimize2.png");
    public static final ImageIcon closeIcon = new ImageIcon("sys/pictures/windowicons/Close1.png");
    public static final ImageIcon closeIconHover = new ImageIcon("sys/pictures/windowicons/Close2.png");
    public static final ImageIcon defaultBackground = new ImageIcon(ImageUtil.getImageGradient(1000,1000,
            new Color(224, 230, 244),
            new Color(249, 233, 241),
            new Color(253, 253, 253)));
    public static final ImageIcon defaultBackgroundLarge = new ImageIcon(ImageUtil.getImageGradient(2000,2000,
            new Color(224, 230, 244),
            new Color(249, 233, 241),
            new Color(253, 253, 253)));
}
