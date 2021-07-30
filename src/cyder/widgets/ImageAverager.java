package cyder.widgets;

import cyder.consts.CyderColors;
import cyder.handler.ErrorHandler;
import cyder.ui.ConsoleFrame;
import cyder.ui.CyderButton;
import cyder.ui.CyderFrame;
import cyder.ui.CyderScrollList;
import cyder.utilities.GetterUtil;
import cyder.utilities.IOUtil;
import cyder.utilities.StringUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.io.File;
import java.util.LinkedList;

public class ImageAverager {
    private LinkedList<File> files;
    private JLabel imagesScrollLabel;
    private CyderScrollList imagseScroll;
    private CyderFrame cf;
    private JLabel imageScrollLabelHolder;

    public ImageAverager() {
        files = new LinkedList<>();

        cf = new CyderFrame(500,830);
        cf.setTitle("Image Averager");

        imagseScroll = new CyderScrollList(680, 360, CyderScrollList.SelectionPolicy.SINGLE);
        imagseScroll.setBorder(null);

        imageScrollLabelHolder = new JLabel();
        imageScrollLabelHolder.setForeground(Color.white);
        imageScrollLabelHolder.setBackground(Color.white);
        imageScrollLabelHolder.setBounds(90,90,320,620);
        imageScrollLabelHolder.setBorder(new LineBorder(CyderColors.navy, 5));
        cf.getContentPane().add(imageScrollLabelHolder);

        imagesScrollLabel = imagseScroll.generateScrollList();
        imagesScrollLabel.setBackground(Color.white);
        imagesScrollLabel.setBounds(10, 10, 300, 600);
        imagesScrollLabel.setBackground(Color.white);
        imageScrollLabelHolder.add(imagesScrollLabel);

        CyderButton addButton = new CyderButton("Add Image");
        addButton.setBounds(100,40,300,40);
        cf.getContentPane().add(addButton);
        addButton.addActionListener(e -> new Thread(() -> {
            try {
                File input = new GetterUtil().getFile("select any png file");

                if (StringUtil.getExtension(input).equals(".png")) {
                    files.add(input);
                    revalidateScroll();
                } else {
                    cf.notify("Selected file is not a png");
                }
            } catch (Exception ex) {
                ErrorHandler.handle(ex);
            }
        }, "wait thread for GetterUtil().getFile()").start());

        CyderButton remove = new CyderButton("Remove Image");
        remove.setBounds(100,720,300,40);
        cf.getContentPane().add(remove);
        remove.addActionListener(e -> {
            String matchName = imagseScroll.getSelectedElement();
            int removeIndex = -1;

            for (int i = 0 ; i < files.size() ; i++) {
                if (files.get(i).getName().equalsIgnoreCase(matchName)) {
                    removeIndex = i;
                    break;
                }
            }

            if (removeIndex != -1) {
                System.out.println(removeIndex);
                files.remove(removeIndex);
                revalidateScroll();
            }
        });

        CyderButton average = new CyderButton("Average Images");
        average.setBounds(100,780,300,40);
        cf.getContentPane().add(average);
        average.addActionListener(e -> compute());

        cf.setVisible(true);
        ConsoleFrame.getConsoleFrame().setFrameRelative(cf);
    }

    private void revalidateScroll() {
        imagseScroll.removeAllElements();
        imageScrollLabelHolder.remove(imagesScrollLabel);

        for (int j = 0 ; j < files.size() ; j++) {
            int finalJ = j;
            class thisAction implements CyderScrollList.ScrollAction {
                @Override
                public void fire() {
                    IOUtil.openFile(files.get(finalJ).getAbsolutePath());
                }
            }

            thisAction action = new thisAction();
            imagseScroll.addElement(files.get(j).getName(), action);
        }

        imagseScroll.setItemAlignemnt(StyleConstants.ALIGN_LEFT);
        imagesScrollLabel = imagseScroll.generateScrollList();
        imagesScrollLabel.setBackground(new Color(255,255,255));
        imagesScrollLabel.setBounds(10, 10, 300, 600);
        imageScrollLabelHolder.setBackground(CyderColors.vanila);

        imageScrollLabelHolder.add(imagesScrollLabel);
        imageScrollLabelHolder.revalidate();
        cf.revalidate();
    }

    private void compute() {

    }
}
