package cyder.widgets;

import cyder.annotations.CyderAuthor;
import cyder.annotations.Vanilla;
import cyder.annotations.Widget;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderIcons;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.network.NetworkUtil;
import cyder.threads.CyderThreadRunner;
import cyder.ui.button.CyderButton;
import cyder.ui.field.CyderTextField;
import cyder.ui.frame.CyderFrame;
import cyder.ui.label.CyderLabel;
import cyder.utils.GetterUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

@Vanilla
@CyderAuthor
public final class FileSignatureWidget {
    /**
     * The file to validate.
     */
    private static File currentFile;

    /**
     * The widget frame.
     */
    private static CyderFrame signatureFrame;

    /**
     * The expected byte signature input field.
     */
    private static CyderTextField signatureField;

    /**
     * The label to which the results are displayed on.
     */
    private static CyderLabel resultLabel;

    /**
     * The label for the files signatures sheet.
     */
    private static CyderLabel fileSignaturesSheetLabel;

    /**
     * The get file button.
     */
    private static CyderButton getFile;

    /**
     * The check file button.
     */
    private static CyderButton checkFile;

    private static final String FILE_SIGNATURE_FILE_CHOOSER = "File Signature File Chooser";

    /**
     * A link for common file signatures.
     */
    public static final String WIKIPEDIA_FILE_SIGNATURES = "https://en.wikipedia.org/wiki/List_of_file_signatures";

    /**
     * The mouse listener for the files signature sheet.
     */
    private static final MouseListener fileSignaturesSheetLabelMouseListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            NetworkUtil.openUrl(WIKIPEDIA_FILE_SIGNATURES);
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            fileSignaturesSheetLabel.setForeground(CyderColors.regularRed);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            fileSignaturesSheetLabel.setForeground(CyderColors.navy);
        }
    };

    /**
     * The widget description.
     */
    private static final String description = "A widget to read the raw file"
            + " hex data and determine if the file signature matches the provided signature";

    /**
     * The widget title.
     */
    private static final String TITLE = "File Signature Checker";

    /**
     * The font for the files signature sheet label.
     */
    private static final Font FILE_SIGNATURES_SHEET_LABEL_FONT = new Font(CyderFonts.AGENCY_FB, Font.BOLD, 28);

    /**
     * The select file text.
     */
    private static final String SELECT_FILE = "Select File";

    /**
     * The validate file type text.
     */
    private static final String VALIDATE_FILE_TYPE = "Validate File Type";

    /**
     * The comparison not yet performed text.
     */
    private static final String COMPARISON_NOT_YET_PERFORMED = "Comparison not performed yet";

    /**
     * The file signature sheet label text.
     */
    private static final String FILE_SIGNATURE_SHEET = "File Signature Sheet";

    /**
     * The choose file button text.
     */
    private static final String CHOOSE_FILE_TO_VALIDATE = "Choose file to validate";

    /**
     * The default label text if a file is not chosen.
     */
    private static final String PLEASE_CHOOSE_A_FILE = "Please choose a file";

    /**
     * The label text if a file signature is not entered.
     */
    private static final String PLEASE_ENTER_A_FILE_SIGNATURE = "Please enter a file signature";

    /**
     * The width of the frame.
     */
    private static final int FRAME_WIDTH = 400;

    /**
     * The height of the frame.
     */
    private static final int FRAME_HEIGHT = 420;

    /**
     * The text for the validate button if the validation is valid.
     */
    private static final String VALID = "Valid";

    /**
     * The text for the validate button if the validation is invalid.
     */
    private static final String INVALID = "Invalid";

    /**
     * The valid file label text.
     */
    private static final String validFileSignatureText = "File signature matches provided signature";

    /**
     * The invalid file label text.
     */
    private static final String invalidFileSignatureText = "File signature does NOT match provided signature; "
            + "If no more possible signature exist for this file, then it might be "
            + "unsafe/corrupted";

    /**
     * The tooltip for the validate button.
     */
    private static final String VALIDATE = "Validate";

    /**
     * The tooltip for the choose file button.
     */
    private static final String CHOOSE_FILE = "Choose file";

    /**
     * Suppress default constructor.
     */
    private FileSignatureWidget() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    @Widget(triggers = {"file signature", "signature"}, description = description)
    public static void showGui() {
        signatureFrame = new CyderFrame(FRAME_WIDTH, FRAME_HEIGHT, CyderIcons.defaultBackground);
        signatureFrame.setTitle(TITLE);

        signatureField = new CyderTextField();
        signatureField.setHorizontalAlignment(JTextField.CENTER);
        signatureField.setBounds(50, 120, 300, 40);
        signatureField.setToolTipText("Enter the hex file signature of the file type you presume this file to be");
        signatureField.addActionListener(e -> validate());
        signatureFrame.getContentPane().add(signatureField);

        fileSignaturesSheetLabel = new CyderLabel(FILE_SIGNATURE_SHEET);
        fileSignaturesSheetLabel.setBounds(50, 50, 300, 40);
        fileSignaturesSheetLabel.setFont(FILE_SIGNATURES_SHEET_LABEL_FONT);
        fileSignaturesSheetLabel.setToolTipText("Click here to view a list of common file signatures");
        fileSignaturesSheetLabel.addMouseListener(fileSignaturesSheetLabelMouseListener);
        signatureFrame.getContentPane().add(fileSignaturesSheetLabel);

        getFile = new CyderButton(SELECT_FILE);
        getFile.setColors(CyderColors.regularPink);
        getFile.setBounds(50, 190, 300, 40);
        getFile.addActionListener(e -> getFileAction());
        getFile.setToolTipText(CHOOSE_FILE);
        signatureFrame.getContentPane().add(getFile);

        checkFile = new CyderButton(VALIDATE_FILE_TYPE);
        checkFile.setColors(CyderColors.regularBlue);
        checkFile.setBounds(50, 260, 300, 40);
        checkFile.addActionListener(e -> validate());
        checkFile.setToolTipText(VALIDATE);
        signatureFrame.getContentPane().add(checkFile);

        resultLabel = new CyderLabel(COMPARISON_NOT_YET_PERFORMED);
        resultLabel.setBounds(50, 300, 300, 120);
        signatureFrame.getContentPane().add(resultLabel);

        signatureFrame.finalizeAndShow();
    }

    /**
     * The action to perform when the choose file button is clicked.
     */
    private static void getFileAction() {
        try {
            CyderThreadRunner.submit(() -> {
                try {
                    File chosenFile = GetterUtil.getInstance().getFile(
                            new GetterUtil.Builder(CHOOSE_FILE_TO_VALIDATE)
                                    .setRelativeTo(signatureFrame));

                    if (chosenFile != null) {
                        currentFile = chosenFile;
                        resetResults();
                    }
                } catch (Exception ex) {
                    ExceptionHandler.handle(ex);
                }
            }, FILE_SIGNATURE_FILE_CHOOSER);
        } catch (Exception ex) {
            ExceptionHandler.handle(ex);
        }
    }

    /**
     * Resets any results from a file validation including:
     * <ul>
     *     <li>Setting the text of the get file button to the current file's name</li>
     *     <li>Setting the text of the check file button</li>
     *     <li>Resetting the results label text</li>
     * </ul>
     */
    private static void resetResults() {
        getFile.setText(currentFile.getName());
        checkFile.setText(VALIDATE_FILE_TYPE);
        resultLabel.setText(COMPARISON_NOT_YET_PERFORMED);
    }

    /**
     * Attempts to validate the chosen file if present with the provided file signature if present.
     */
    private static void validate() {
        if (currentFile == null) {
            signatureFrame.notify(PLEASE_CHOOSE_A_FILE);
            return;
        } else if (signatureField.getTrimmedText().isEmpty()) {
            signatureFrame.notify(PLEASE_ENTER_A_FILE_SIGNATURE);
            return;
        }

        // Remove any byte identifiers and whitespace
        // Text such as "0xFF 0xA0" becomes "FFA0"
        String expectedByteSignature = signatureField.getTrimmedText()
                .replaceAll("0x", "")
                .replaceAll("\\s+", "");

        StringBuilder stringBuilder = new StringBuilder();

        try {
            InputStream inputStream = new FileInputStream(currentFile);
            int columns = (int) Math.ceil(expectedByteSignature.length() / 2.0);

            long streamPointer = 0;
            while (inputStream.available() > 0) {
                long col = streamPointer++ % columns;
                stringBuilder.append(String.format("%02x ", inputStream.read()));

                if (col == (columns - 1)) break;
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        String byteSignatureString = stringBuilder.toString().replaceAll("\\s+", "");

        if (expectedByteSignature.equalsIgnoreCase(byteSignatureString)) {
            resultLabel.setText(validFileSignatureText);
            checkFile.setText(VALID);
        } else {
            resultLabel.setText(invalidFileSignatureText);
            checkFile.setText(INVALID);
        }

        signatureField.flashField();
    }
}
