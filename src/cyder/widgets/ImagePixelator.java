package cyder.widgets;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.consts.CyderImages;
import cyder.handler.ErrorHandler;
import cyder.ui.*;
import cyder.utilities.StringUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;

public class ImagePixelator {
    private ImageIcon displayIcon;

    public ImagePixelator(File startPNG) {
        CyderFrame pixelFrame = new CyderFrame(800,800, CyderImages.defaultBackground);
        pixelFrame.setTitle("Image Pixelator");

        CyderLabel pixelSize = new CyderLabel("Pixel Size");
        pixelSize.setFont(CyderFonts.defaultFontSmall.deriveFont(28f));
        int w = CyderFrame.getMinWidth(pixelSize.getText(), pixelSize.getFont());
        int h = CyderFrame.getMinHeight(pixelSize.getText(), pixelSize.getFont());
        pixelSize.setBounds(400 - w / 2, 30 + 20, w, h);
        pixelFrame.getContentPane().add(pixelSize);

        CyderButton chooseImage = new CyderButton("Choose Image");
        chooseImage.setToolTipText("PNGs");
        chooseImage.setBounds(50,100,200,40);
        pixelFrame.getContentPane().add(chooseImage);
        chooseImage.addActionListener(e -> {

        });

        CyderTextField integerField = new CyderTextField(0);
        integerField.setBounds(300,100,200,40);
        integerField.setToolTipText("How many old pixels should be combined into a new pixel?");
        pixelFrame.getContentPane().add(integerField);
        integerField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {

            }
        });

        CyderButton approveImage = new CyderButton("Approve Image");
        approveImage.setToolTipText("Saves to downloads folder");
        approveImage.setBounds(800 - 50 - 200,100,200,40);
        pixelFrame.getContentPane().add(approveImage);
        approveImage.addActionListener(e -> {

        });

        JLabel previewLabel = new JLabel();
        previewLabel.setBounds(50,170, 800 - 100, 610);
        previewLabel.setBorder(new LineBorder(CyderColors.navy, 5, false));
        pixelFrame.getContentPane().add(previewLabel);

        pixelFrame.setVisible(true);
        ConsoleFrame.getConsoleFrame().setFrameRelativeTo(pixelFrame);

        if (startPNG != null && StringUtil.getExtension(startPNG).equalsIgnoreCase(".png")) {
            try {
                displayIcon = new ImageIcon(ImageIO.read(startPNG));
                previewLabel.setIcon(displayIcon);
                previewLabel.revalidate();
            } catch (Exception e) {
                ErrorHandler.handle(e);
            }
        }
    }
}
