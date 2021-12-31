package cyder.widgets;

import cyder.annotations.Widget;
import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.consts.CyderImages;
import cyder.consts.CyderStrings;
import cyder.genesis.GenesisShare;
import cyder.handlers.internal.ErrorHandler;
import cyder.ui.*;
import cyder.utilities.GetterUtil;
import cyder.utilities.NetworkUtil;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class FileSignatureWidget {
    private static File currentFile = null;
    private static CyderFrame signatureFrame;
    private static CyderTextField signatureField;
    private static CyderLabel resultLabel;

    private FileSignatureWidget() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    @Widget("file signature")
    public static void showGUI() {
        signatureFrame = new CyderFrame(400,420, CyderImages.defaultBackground);
        signatureFrame.setTitle("File Signature Checker");

        signatureField = new CyderTextField(0);
        signatureField.setBounds(50, 120, 300, 40);
        signatureField.setToolTipText("Enter the hex file signature of the file type you presume this file to be");
        signatureField.addActionListener(e -> validate());
        signatureFrame.getContentPane().add(signatureField);

        CyderLabel referenceLabel = new CyderLabel("File Signature Sheet");
        referenceLabel.setBounds(50,50,300,40);
        referenceLabel.setFont(CyderFonts.defaultFontSmall.deriveFont(28f));
        referenceLabel.setToolTipText("Click here to view a list of common file extensions");
        referenceLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                NetworkUtil.internetConnect("https://en.wikipedia.org/wiki/List_of_file_signatures");
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                referenceLabel.setForeground(CyderColors.regularRed);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                referenceLabel.setForeground(CyderColors.navy);
            }
        });
        signatureFrame.getContentPane().add(referenceLabel);

        CyderButton getFile = new CyderButton("Select File");
        getFile.setColors(CyderColors.intellijPink);
        getFile.setBounds(50,190,300,40);
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

        CyderButton checkFile = new CyderButton("Validate File Type");
        checkFile.setColors(CyderColors.regularBlue);
        checkFile.setBounds(50,260, 300, 40);
        checkFile.addActionListener(e -> validate());
        signatureFrame.getContentPane().add(checkFile);

        resultLabel = new CyderLabel("Comparison not performed yet");
        resultLabel.setBounds(50,300, 300, 120);
        signatureFrame.getContentPane().add(resultLabel);

        signatureFrame.setVisible(true);
        signatureFrame.setLocationRelativeTo(GenesisShare.getDominantFrame());
    }

    private static void validate() {
        try {
            if (currentFile == null) {
                signatureFrame.notify("Please choose a file");
            } else if (signatureField.getText().trim().length() == 0) {
                signatureFrame.notify("Please enter a file extension");
            } else {
                String byteSignature = signatureField.getText().trim()
                        .replace(" ","").replace("0x","");
                InputStream inputStream = new FileInputStream(currentFile);
                int numberOfColumns = (int) Math.ceil(byteSignature.length() / 2);
                StringBuilder sb = new StringBuilder();

                long streamPtr = 0;
                while (inputStream.available() > 0) {
                    long col = streamPtr ++ % numberOfColumns;
                    sb.append(String.format("%02x ",inputStream.read()));

                    if (col == (numberOfColumns - 1)) {
                        break;
                    }
                }

                String questionableByteSignature = sb.toString().replace(" ", "");

                if (byteSignature.equalsIgnoreCase(questionableByteSignature)) {
                    resultLabel.setText("File signature matches provided signature");
                } else {
                    resultLabel.setText("File signature does NOT match provided signature;" +
                            " If no more possible signature exist for this file, then it might be" +
                            " unsafe/corrupted");
                }
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }
}
