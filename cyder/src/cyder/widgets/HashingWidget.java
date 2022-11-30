package cyder.widgets;

import com.google.common.collect.ImmutableList;
import cyder.annotations.CyderAuthor;
import cyder.annotations.ForReadability;
import cyder.annotations.Vanilla;
import cyder.annotations.Widget;
import cyder.constants.CyderFonts;
import cyder.constants.CyderStrings;
import cyder.constants.HtmlTags;
import cyder.handlers.internal.InformHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.ui.button.CyderButton;
import cyder.ui.field.CyderPasswordField;
import cyder.ui.frame.CyderFrame;
import cyder.ui.label.CyderLabel;
import cyder.ui.selection.CyderCheckbox;
import cyder.ui.selection.CyderComboBox;
import cyder.utils.OsUtil;
import cyder.utils.SecurityUtil;

/** A widget for computing the hash of strings. */
@Vanilla
@CyderAuthor
public class HashingWidget {
    /** The hash field. */
    private CyderPasswordField hashField;

    /** The hash algorithm combo box. */
    private CyderComboBox comboBox;

    /** The widget description. */
    private static final String description = "A hashing widget to hash any string using"
            + " multiple algorithms such as MD5, SHA256, and SHA1";

    /**
     * Constructs and returns a new hashing widget.
     *
     * @return a new hashing widget
     */
    public static HashingWidget getInstance() {
        return new HashingWidget();
    }

    /** Constructs a new hashing widget. */
    private HashingWidget() {
        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /** Shows a new instance of the hashing widget. */
    @Widget(triggers = {HASH, "hasher"}, description = description)
    public static void showGui() {
        getInstance().innerShowGui();
    }

    /** The hash string. */
    private static final String HASH = "Hash";

    /** The id of the sha256 algorithm. */
    private static final String SHA_256 = "SHA-256";

    /** The id of the sha1 algorithm. */
    private static final String SHA_1 = "SHA-1";

    /** The id of the md5 algorithm. */
    private static final String MD5 = "MD5";

    /** The valid hashing algorithms for the combo box chooser. */
    private static final ImmutableList<CyderComboBox.ComboItem> HASH_ALGORITHMS = ImmutableList.of(
            new CyderComboBox.ComboItem(SHA_256, "SHA256 Algorithm"),
            new CyderComboBox.ComboItem(SHA_1, "SHA-1 Algorithm"),
            new CyderComboBox.ComboItem(MD5, "MD5 Algorithm (Do not use for passwords)")
    );

    /** The text of the hash result popup. */
    private static final String HASH_RESULT = HASH + CyderStrings.space + "Result";

    /** The title of the frame. */
    private static final String TITLE = "Hasher";

    /** The width of the frame. */
    private static final int FRAME_WIDTH = 500;

    /** The height of the frame. */
    private static final int FRAME_HEIGHT = 260;

    /** The checkbox for whether to save the hash result to the clipboard. */
    private CyderCheckbox saveToClipboardCheckbox;

    /** Shows the gui for this instance of the hashing widget. */
    public void innerShowGui() {
        CyderFrame hashFrame = new CyderFrame(FRAME_WIDTH, FRAME_HEIGHT);
        hashFrame.setTitle(TITLE);

        CyderLabel Instructions = new CyderLabel("Enter input to be hashed");
        Instructions.setFont(CyderFonts.SEGOE_20);
        Instructions.setBounds(65, 40, 400, 30);
        hashFrame.getContentPane().add(Instructions);

        hashField = new CyderPasswordField();
        hashField.setToolTipText("Hold shift to reveal contents");
        hashField.addActionListener(e -> hashButtonAction());
        hashField.setBounds(50, 90, 400, 40);
        hashFrame.getContentPane().add(hashField);

        CyderButton hashButton = new CyderButton(HASH);
        hashButton.addActionListener(e -> hashButtonAction());
        hashButton.setBounds(50, 140, 180, 40);
        hashFrame.getContentPane().add(hashButton);

        saveToClipboardCheckbox = new CyderCheckbox();
        saveToClipboardCheckbox.setChecked();
        saveToClipboardCheckbox.setLocation(hashFrame.getWidth() / 2 + 70, 190);
        hashFrame.getContentPane().add(saveToClipboardCheckbox);

        CyderLabel saveToClipBoardLabel = new CyderLabel("Copy hash to clipboard:");
        saveToClipBoardLabel.setBounds(120, 200, 200, 30);
        hashFrame.getContentPane().add(saveToClipBoardLabel);

        comboBox = new CyderComboBox(210, 40, HASH_ALGORITHMS, HASH_ALGORITHMS.get(0));
        comboBox.setBounds(240, 140, 210, 40);
        hashFrame.getContentPane().add(comboBox);

        hashFrame.finalizeAndShow();
    }

    /** The action to invoke when the hash button is pressed. */
    private void hashButtonAction() {
        char[] hashFieldContents = hashField.getPassword();
        if (hashFieldContents.length == 0) return;

        String algorithm = comboBox.getCurrentState().displayValue();
        String hashResult = switch (algorithm) {
            case SHA_256 -> SecurityUtil.toHexString(SecurityUtil.getSha256(hashFieldContents));
            case SHA_1 -> SecurityUtil.toHexString(SecurityUtil.getSha1(hashFieldContents));
            case MD5 -> SecurityUtil.toHexString(SecurityUtil.getMd5(hashFieldContents));
            default -> throw new IllegalStateException("Unimplemented hash algorithm: " + algorithm);
        };

        String informText = "Your hashed input is:" + HtmlTags.breakTag + hashResult
                + HtmlTags.breakTag + "Provided by " + algorithm;
        if (saveToClipboardCheckbox.isChecked()) {
            OsUtil.setClipboard(hashResult);
        }

        InformHandler.inform(new InformHandler.Builder(informText)
                .setTitle(algorithm + CyderStrings.space + HASH_RESULT)
                .setRelativeTo(hashField));

        reset();
    }

    /** Resets the widget. */
    @ForReadability
    private void reset() {
        hashField.setText("");
    }
}
