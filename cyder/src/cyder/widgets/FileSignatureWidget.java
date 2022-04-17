package cyder.widgets;

import cyder.annotations.Vanilla;
import cyder.annotations.Widget;
import cyder.constants.*;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.CyderThreadRunner;
import cyder.ui.CyderButton;
import cyder.ui.CyderFrame;
import cyder.ui.CyderLabel;
import cyder.ui.CyderTextField;
import cyder.utilities.GetterUtil;
import cyder.utilities.NetworkUtil;
import cyder.utilities.objects.GetterBuilder;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

@Vanilla
public class FileSignatureWidget {
    private static File currentFile;
    private static CyderFrame signatureFrame;
    private static CyderTextField signatureField;
    private static CyderLabel resultLabel;

    private FileSignatureWidget() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    @Widget(triggers = "filesignature", description = "A widget to read the raw file " +
            "hex data and determine if the file signature matches the provided extension")
    public static void showGUI() {
        

        signatureFrame = new CyderFrame(400,420, CyderIcons.defaultBackground);
        signatureFrame.setTitle("File Signature Checker");

        signatureField = new CyderTextField(0);
        signatureField.setHorizontalAlignment(JTextField.CENTER);
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
                NetworkUtil.openUrl(CyderUrls.WIKIPEDIA_FILE_SIGNATURES);
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
        getFile.setColors(CyderColors.regularPink);
        getFile.setBounds(50,190,300,40);
        getFile.addActionListener(e -> {
            try {
                CyderThreadRunner.submit(() -> {
                    try {
                        GetterBuilder builder = new GetterBuilder("Choose file to validate");
                        builder.setRelativeTo(signatureFrame);
                        File temp = GetterUtil.getInstance().getFile(builder);

                        if (temp != null) {
                            currentFile = temp;
                            getFile.setText(currentFile.getName());
                        }
                    } catch (Exception ex) {
                        ExceptionHandler.handle(ex);
                    }
                }, "wait thread for GetterUtil().getFile()");
            } catch (Exception ex) {
                ExceptionHandler.handle(ex);
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

        signatureFrame.finalizeAndShow();
    }

    private static void validate() {
        try {
            if (currentFile == null) {
                signatureFrame.notify("Please choose a file");
            } else if (signatureField.getText().trim().isEmpty()) {
                signatureFrame.notify("Please enter a file extension");
            } else {
                String byteSignature = signatureField.getText().trim()
                        .replace(" ","").replace("0x","");
                InputStream inputStream = new FileInputStream(currentFile);
                int numberOfColumns = (int) Math.ceil(byteSignature.length() / 2.0);
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
            ExceptionHandler.handle(e);
        }
    }
}
