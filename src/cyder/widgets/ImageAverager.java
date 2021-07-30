package cyder.widgets;

import cyder.handler.ErrorHandler;
import cyder.ui.ConsoleFrame;
import cyder.ui.CyderButton;
import cyder.ui.CyderFrame;
import cyder.utilities.GetterUtil;
import cyder.utilities.StringUtil;

import java.io.File;
import java.util.LinkedList;

public class ImageAverager {
    private LinkedList<File> files;

    public ImageAverager() {
        files = new LinkedList<>();

        CyderFrame cf = new CyderFrame(500,800);
        cf.setTitle("Image Averager");

        CyderButton addButton = new CyderButton("Add Images");
        addButton.setBounds(100,50,300,40);
        cf.getContentPane().add(addButton);
        addButton.addActionListener(e -> {
            new Thread(() -> {
                try {
                    File input = new GetterUtil().getFile("select any png file");

                    if (StringUtil.getExtension(input).equals(".png")) {
                        files.add(input);
                        //todo update selection list
                    } else {
                        cf.notify("Selected file is not a png");
                    }
                } catch (Exception ex) {
                    ErrorHandler.handle(ex);
                }
            }, "wait thread for GetterUtil().getFile()").start();
        });



        CyderButton average = new CyderButton("Average Images");
        average.setBounds(100,740,300,40);
        cf.getContentPane().add(average);
        average.addActionListener(e -> {

        });

        cf.setVisible(true);
        ConsoleFrame.getConsoleFrame().setFrameRelative(cf);
    }
}
