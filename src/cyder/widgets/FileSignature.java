package cyder.widgets;

import cyder.consts.CyderColors;
import cyder.consts.CyderImages;
import cyder.handler.ErrorHandler;
import cyder.ui.ConsoleFrame;
import cyder.ui.CyderButton;
import cyder.ui.CyderFrame;
import cyder.ui.CyderTextField;
import cyder.utilities.GetterUtil;

import java.io.File;

public class FileSignature {
    private File currentFile = null;
    CyderFrame signatureFrame;
    CyderTextField extensionField;

    public FileSignature() {
        signatureFrame = new CyderFrame(400,400, CyderImages.defaultBackground);
        signatureFrame.setTitle("File Signature Checker");

        CyderButton getFile = new CyderButton("Select File");
        getFile.setColors(CyderColors.intellijPink);
        getFile.setBounds(50,50,300,40);
        getFile.addActionListener(e -> {
            try {
                new Thread(() -> {
                    try {
                        File temp = new GetterUtil().getFile("Choose file to resize");

                        if (temp != null) {
                            currentFile = temp;
                            getFile.setText(currentFile.getName());
                        }
                    } catch (Exception ex) {
                        ErrorHandler.handle(ex);
                    }
                }, "wait thread for GetterUtil().getFile()").start();
            } catch (Exception ex) {
                ErrorHandler.handle(ex);
            }
        });
        signatureFrame.getContentPane().add(getFile);

        extensionField = new CyderTextField(0);
        extensionField.setBounds(50,120,300,40);
        extensionField.setToolTipText("Enter the extension of the file you presume it to be");
        extensionField.addActionListener(e -> validate());
        signatureFrame.getContentPane().add(extensionField);

        CyderButton checkFile = new CyderButton("Validate File");
        checkFile.setColors(CyderColors.regularBlue);
        checkFile.setBounds(50,190, 300, 40);
        checkFile.addActionListener(e -> validate());
        signatureFrame.getContentPane().add(checkFile);

        signatureFrame.setVisible(true);
        ConsoleFrame.getConsoleFrame().setFrameRelative(signatureFrame);
    }

    private void validate() {
        if (currentFile == null) {
            signatureFrame.notify("Please choose a file");
        } else if (extensionField.getText().trim().length() == 0) {
            signatureFrame.notify("Please enter a file extension");
        } else {
            String extension = extensionField.getText().replace(".","");
            System.out.println("Validate " + currentFile.getName() + " against the extension " + extension);
        }
    }
}
