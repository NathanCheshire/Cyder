package com.cyder.utilities;

import com.cyder.ui.CyderButton;
import com.cyder.ui.CyderFrame;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;

public class ImageUtil {

    private GeneralUtil giu;
    private CyderFrame pixelFrame;

    public ImageUtil() {
        giu = new GeneralUtil();
    }

    public static BufferedImage pixelate(BufferedImage imageToPixelate, int pixelSize) {
        BufferedImage pixelateImage = new BufferedImage(
                imageToPixelate.getWidth(),
                imageToPixelate.getHeight(),
                imageToPixelate.getType());

        for (int y = 0; y < imageToPixelate.getHeight(); y += pixelSize) {
            for (int x = 0; x < imageToPixelate.getWidth(); x += pixelSize) {
                BufferedImage croppedImage = getCroppedImage(imageToPixelate, x, y, pixelSize, pixelSize);
                Color dominantColor = getDominantColor(croppedImage);

                for (int yd = y; (yd < y + pixelSize) && (yd < pixelateImage.getHeight()); yd++)
                    for (int xd = x; (xd < x + pixelSize) && (xd < pixelateImage.getWidth()); xd++)
                        pixelateImage.setRGB(xd, yd, dominantColor.getRGB());

            }
        }

        return pixelateImage;
    }

    public static BufferedImage getCroppedImage(BufferedImage image, int startx, int starty, int width, int height) {
        if (startx < 0)
            startx = 0;

        if (starty < 0)
            starty = 0;

        if (startx > image.getWidth())
            startx = image.getWidth();

        if (starty > image.getHeight())
            starty = image.getHeight();

        if (startx + width > image.getWidth())
            width = image.getWidth() - startx;

        if (starty + height > image.getHeight())
            height = image.getHeight() - starty;

        return image.getSubimage(startx, starty, width, height);
    }

    public static Color getDominantColor(BufferedImage image) {
        Map<Integer, Integer> colorCounter = new HashMap<>(100);

        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int currentRGB = image.getRGB(x, y);
                int count = colorCounter.getOrDefault(currentRGB, 0);
                colorCounter.put(currentRGB, count + 1);
            }
        }

        return getDominantColor(colorCounter);
    }

    public static Color getDominantColorOpposite(BufferedImage image) {
        Color c = getDominantColor(image);
        return new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue(), c.getAlpha());
    }

    public static Color getDominantColor(Map<Integer, Integer> colorCounter) {
        int dominantRGB = colorCounter.entrySet().stream()
                .max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1)
                .get()
                .getKey();

        return new Color(dominantRGB);
    }

    public void pixelate(File path, int pixelSize) {
        try {
            BufferedImage retImage = ImageUtil.pixelate(ImageIO.read(path), pixelSize);
            String NewName = path.getName().replace(".png", "") + "_Pixelated_Pixel_Size_" + pixelSize + ".png";

            if (pixelFrame != null)
                pixelFrame.closeAnimation();

            pixelFrame = new CyderFrame(retImage.getWidth(),retImage.getHeight(), new ImageIcon(retImage));
            pixelFrame.setTitle("Approve Pixelation");

            CyderButton approveImage = new CyderButton("Approve Image");
            approveImage.setFocusPainted(false);
            approveImage.setBackground(giu.regularRed);
            approveImage.setColors(giu.regularRed);
            approveImage.setBorder(new LineBorder(giu.navy,3,false));
            approveImage.setFont(giu.weatherFontSmall);

            approveImage.addActionListener(e -> {
                try {
                    ImageIO.write(retImage, "png", new File("C:\\Users\\" + new SystemUtil().getWindowsUsername() + "\\Downloads\\" + NewName));
                } catch (Exception exc) {
                    giu.handle(exc);
                }

                pixelFrame.closeAnimation();
                pixelFrame.inform("The pixelated image has been saved to your Downloads folder.","Saved", 400, 200);
            });
            approveImage.setBounds(20, retImage.getHeight() - 100,retImage.getWidth() - 40, 40);
            pixelFrame.getContentPane().add(approveImage);

            CyderButton rejectImage = new CyderButton("Reject Image");
            rejectImage.setFocusPainted(false);
            rejectImage.setBackground(giu.regularRed);
            rejectImage.setBorder(new LineBorder(giu.navy,3,false));
            rejectImage.setColors(giu.regularRed);
            rejectImage.setFont(giu.weatherFontSmall);
            rejectImage.addActionListener(e -> pixelFrame.closeAnimation());
            rejectImage.setSize(pixelFrame.getX(), 20);
            rejectImage.setBounds(20, retImage.getHeight() - 60,retImage.getWidth() - 40, 40);
            pixelFrame.getContentPane().add(rejectImage);

            pixelFrame.setVisible(true);
            pixelFrame.setLocationRelativeTo(null);
            pixelFrame.setAlwaysOnTop(true);
        }

        catch (Exception e) {
            giu.handle(e);
        }
    }

    public BufferedImage imageFromColor(int x, int y, Color c) {
        BufferedImage bi = new BufferedImage(x,y,BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = bi.createGraphics();

        graphics.setPaint(c);
        graphics.fillRect ( 0, 0, x, y);

        return bi;
    }

    public static BufferedImage resizeImage(int x, int y, File UneditedImage) {
        BufferedImage ReturnImage = null;

        try {
            File CurrentConsole = UneditedImage;
            Image ConsoleImage = ImageIO.read(CurrentConsole);
            Image TransferImage = ConsoleImage.getScaledInstance(x, y, Image.SCALE_SMOOTH);
            ReturnImage = new BufferedImage(TransferImage.getWidth(null), TransferImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics2D bGr = ReturnImage.createGraphics();

            bGr.drawImage(TransferImage, 0, 0, null);
            bGr.dispose();
            return ReturnImage;
        }

        catch (Exception e) {
            GeneralUtil.handle(e);
        }

        return ReturnImage;
    }

    public BufferedImage getBi(File imageFile) {
        try {
            return ImageIO.read(imageFile);
        }

        catch (Exception e) {
            GeneralUtil.handle(e);
        }

        return null;
    }

    public static BufferedImage getBi(String filename) {
        try {
            return ImageIO.read(new File(filename));
        }

        catch (Exception e) {
            GeneralUtil.handle(e);
        }

        return null;
    }

    //Used for barrel roll and flip screen hotkeys, credit: MadProgrammer from StackOverflow
    public static BufferedImage rotateImageByDegrees(BufferedImage img, double angle) {
        double rads = Math.toRadians(angle);

        double sin = Math.abs(Math.sin(rads));
        double cos = Math.abs(Math.cos(rads));

        int w = img.getWidth();
        int h = img.getHeight();

        int newWidth = (int) Math.floor(w * cos + h * sin);
        int newHeight = (int) Math.floor(h * cos + w * sin);

        BufferedImage rotated = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = rotated.createGraphics();
        AffineTransform at = new AffineTransform();
        at.translate((newWidth - w) / 2, (newHeight - h) / 2);

        at.rotate(rads, w / 2, h / 2);
        g2d.setTransform(at);
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();

        return rotated;
    }

    public static int xOffsetForCenterJLabel(int compWidth, String title) {
        return (int) Math.floor(5 + (compWidth / 2.0)) - (((int) Math.ceil(14 * title.length())) / 2);
    }



    static BufferedImage resizeImage(BufferedImage originalImage, int type, int img_width, int img_height) {
        BufferedImage resizedImage = new BufferedImage(img_width, img_height, type);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, img_width, img_height, null);
        g.dispose();

        return resizedImage;
    }

    static double getAspectRatio(BufferedImage im) {
        return ((double) im.getWidth() / (double) im.getHeight());
    }

    public int getScreenResolution() {
        return Toolkit.getDefaultToolkit().getScreenResolution();
    }
}