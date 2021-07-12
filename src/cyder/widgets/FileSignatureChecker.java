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
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileSignatureChecker {
    private File currentFile = null;
    CyderFrame signatureFrame;
    CyderTextField signatureField;

    public FileSignatureChecker() {
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

        signatureField = new CyderTextField(0);
        signatureField.setBounds(50,120,300,40);
        signatureField.setToolTipText("Enter the hex file signature of the file type you presume this file to be");
        signatureField.addActionListener(e -> validate());
        signatureFrame.getContentPane().add(signatureField);

        CyderButton checkFile = new CyderButton("Validate File Type");
        checkFile.setColors(CyderColors.regularBlue);
        checkFile.setBounds(50,190, 300, 40);
        checkFile.addActionListener(e -> validate());
        signatureFrame.getContentPane().add(checkFile);

        signatureFrame.setVisible(true);
        ConsoleFrame.getConsoleFrame().setFrameRelative(signatureFrame);
    }

    private void validate() {
        try {
            if (currentFile == null) {
                signatureFrame.notify("Please choose a file");
            } else if (signatureField.getText().trim().length() == 0) {
                signatureFrame.notify("Please enter a file extension");
            } else {
                String byteSignature = signatureField.getText().trim();
                int byteLen = byteSignature.length();

                InputStream inputStream = new FileInputStream(currentFile);
                int numberOfColumns = 10;

                long streamPtr=0;
                while (inputStream.available() > 0) {
                    final long col = streamPtr++ % numberOfColumns;
                    System.out.printf("%02x ",inputStream.read());
                    if (col == (numberOfColumns-1)) {
                        System.out.printf("\n");
                    }
                }
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }
}
