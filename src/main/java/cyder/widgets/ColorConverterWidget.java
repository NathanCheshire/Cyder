package cyder.widgets;

import cyder.annotations.CyderAuthor;
import cyder.annotations.SuppressCyderInspections;
import cyder.annotations.Vanilla;
import cyder.annotations.Widget;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderIcons;
import cyder.constants.CyderRegexPatterns;
import cyder.enums.CyderInspection;
import cyder.layouts.CyderPartitionedLayout;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.strings.CyderStrings;
import cyder.ui.field.CyderTextField;
import cyder.ui.frame.CyderFrame;
import cyder.utils.ColorUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * A widget for converting between rgb and hex colors.
 */
@Vanilla
@CyderAuthor
public class ColorConverterWidget {
    /**
     * Returns a new instance of ColorConverterWidget.
     *
     * @return a new instance of ColorConverterWidget
     */
    public static ColorConverterWidget getInstance() {
        return new ColorConverterWidget();
    }

    /**
     * Creates a new Color Converter Widget.
     */
    private ColorConverterWidget() {
        Logger.log(LogTag.OBJECT_CREATION, this);
    }

    /**
     * The widget description.
     */
    private static final String description = "A color converter widget to convert from rgb to hex and vice versa";

    /**
     * A widget for converting between rgb and hex colors.
     */
    @SuppressCyderInspections(CyderInspection.WidgetInspection)
    @Widget(triggers = {"color converter", "color"}, description = description)
    public static void showGui() {
        getInstance().innerShowGui();
    }

    /**
     * Shows the gui of this widget.
     */
    public void innerShowGui() {
        innerShowGui(CyderFrame.getDominantFrame());
    }

    /**
     * The width of the frame.
     */
    private static final int FRAME_WIDTH = 300;

    /**
     * The height of the frame.
     */
    private static final int FRAME_HEIGHT = 400;

    /**
     * The color preview text.
     */
    private static final String COLOR_PREVIEW = "Color preview";

    /**
     * The hex value text.
     */
    private static final String HEX_VALUE = "Hex value";

    /**
     * The rgb value text.
     */
    private static final String RGB_VALUE = "Rgb value";

    /**
     * The color converter text.
     */
    private static final String TITLE = "Color Converter";

    /**
     * The limit of characters for the rgb field.
     */
    private static final int RGB_FIELD_LIMIT = 11;

    /**
     * The border for the color preview block.
     */
    private static final LineBorder COLOR_BLOCK_BORDER = new LineBorder(CyderColors.navy, 5);

    /**
     * The font for the rgb and hex fields.
     */
    private static final Font fieldFont = new Font(CyderFonts.SEGOE_UI_BLACK, Font.BOLD, 26);

    /**
     * The limit of characters for the hex field.
     */
    private static final int hexFieldLimit = 6;

    /**
     * The string formatter for the hex field.
     */
    private static final String hexFieldStringFormatter = "#%02X%02X%02X";

    /**
     * The partition size for the spacers.
     */
    private static final int spacerPartitionLength = 10;

    /**
     * The partition space for the color labels and color block.
     */
    private static final int colorLabelAndBlockPartitionLength = 15;

    /**
     * The partition space for the text fields.
     */
    private static final int fieldPartitionLength = 10;

    /**
     * The size of the text fields and color block.
     */
    private static final Dimension fieldAndBlockSize = new Dimension(220, 50);

    /**
     * The starting color for the fields.
     */
    private static final Color startingColor = CyderColors.navy;

    /**
     * The height of labels.
     */
    private static final int labelHeight = 30;

    /**
     * Shows the gui of this widget.
     *
     * @param relativeTo the component to set the widget relative to
     */
    public void innerShowGui(Component relativeTo) {
        CyderFrame colorFrame = new CyderFrame(FRAME_WIDTH, FRAME_HEIGHT, CyderIcons.defaultBackground);
        colorFrame.setTitle(TITLE);

        colorFrame.initializeResizing();
        colorFrame.setResizable(true);
        colorFrame.setBackgroundResizing(true);
        colorFrame.setMinimumSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
        colorFrame.setMaximumSize(new Dimension(FRAME_WIDTH, 2 * FRAME_HEIGHT));

        CyderPartitionedLayout layout = new CyderPartitionedLayout();

        JLabel colorLabel = new JLabel(COLOR_PREVIEW);
        colorLabel.setHorizontalAlignment(JLabel.CENTER);
        colorLabel.setFont(CyderFonts.SEGOE_20);
        colorLabel.setForeground(CyderColors.navy);
        colorLabel.setSize(FRAME_WIDTH, labelHeight);

        JLabel hexLabel = new JLabel(HEX_VALUE);
        hexLabel.setHorizontalAlignment(JLabel.CENTER);
        hexLabel.setFont(CyderFonts.SEGOE_20);
        hexLabel.setForeground(CyderColors.navy);
        hexLabel.setSize(FRAME_WIDTH, labelHeight);

        JLabel rgbLabel = new JLabel(RGB_VALUE);
        rgbLabel.setHorizontalAlignment(JLabel.CENTER);
        rgbLabel.setFont(CyderFonts.SEGOE_20);
        rgbLabel.setForeground(CyderColors.navy);
        rgbLabel.setSize(FRAME_WIDTH, labelHeight);

        JTextField colorBlock = new JTextField();
        colorBlock.setBackground(CyderColors.navy);
        colorBlock.setFocusable(false);
        colorBlock.setCursor(null);
        colorBlock.setToolTipText(COLOR_PREVIEW);
        colorBlock.setBorder(COLOR_BLOCK_BORDER);
        colorBlock.setSize(fieldAndBlockSize);

        CyderTextField rgbField = new CyderTextField(RGB_FIELD_LIMIT);
        rgbField.setHorizontalAlignment(JTextField.CENTER);
        rgbField.setText(startingColor.getRed() + CyderStrings.comma + startingColor.getGreen()
                + CyderStrings.comma + startingColor.getBlue());

        CyderTextField hexField = new CyderTextField(hexFieldLimit);
        hexField.setKeyEventRegexMatcher(CyderRegexPatterns.hexPattern.pattern());
        hexField.setHorizontalAlignment(JTextField.CENTER);
        hexField.setText(String.format(hexFieldStringFormatter, startingColor.getRed(),
                        startingColor.getGreen(), startingColor.getBlue())
                .replace(CyderStrings.hash, ""));
        hexField.setBackground(CyderColors.empty);
        hexField.setToolTipText(HEX_VALUE);
        hexField.setFont(fieldFont);
        hexField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                try {
                    Color hexFieldColor = ColorUtil.hexStringToColor(hexField.getText());
                    rgbField.setText(hexFieldColor.getRed() + CyderStrings.comma + hexFieldColor.getGreen()
                            + CyderStrings.comma + hexFieldColor.getBlue());
                    colorBlock.setBackground(hexFieldColor);
                } catch (Exception ignored) {}
            }
        });
        hexField.setSize(fieldAndBlockSize);
        hexField.setOpaque(false);

        rgbField.setBackground(CyderColors.empty);
        rgbField.setKeyEventRegexMatcher(CyderRegexPatterns.rgbPattern.pattern());
        rgbField.setToolTipText(RGB_VALUE);
        rgbField.setFont(fieldFont);
        rgbField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                try {
                    String text = rgbField.getText();
                    if (!text.contains(CyderStrings.comma)) return;

                    String[] parts = text.split(CyderStrings.comma);
                    if (parts.length != 3) return;

                    int r = Integer.parseInt(parts[0]);
                    int g = Integer.parseInt(parts[1]);
                    int b = Integer.parseInt(parts[2]);

                    Color color = new Color(r, g, b);
                    hexField.setText(ColorUtil.rgbToHexString(color));
                    colorBlock.setBackground(color);
                } catch (Exception ignored) {}
            }
        });
        rgbField.setSize(fieldAndBlockSize);
        rgbField.setOpaque(false);

        layout.spacer(spacerPartitionLength);
        layout.addComponent(hexLabel, colorLabelAndBlockPartitionLength);
        layout.addComponent(hexField, fieldPartitionLength);
        layout.addComponent(rgbLabel, colorLabelAndBlockPartitionLength);
        layout.addComponent(rgbField, fieldPartitionLength);
        layout.addComponent(colorLabel, colorLabelAndBlockPartitionLength);
        layout.addComponent(colorBlock, colorLabelAndBlockPartitionLength);
        layout.spacer(spacerPartitionLength);

        colorFrame.setCyderLayout(layout);

        colorFrame.finalizeAndShow(relativeTo);
    }
}
