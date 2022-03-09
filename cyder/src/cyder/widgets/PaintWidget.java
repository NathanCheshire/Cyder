package cyder.widgets;

import cyder.annotations.Widget;
import cyder.constants.CyderColors;
import cyder.constants.CyderIcons;
import cyder.constants.CyderRegexPatterns;
import cyder.constants.CyderStrings;
import cyder.enums.SliderShape;
import cyder.genesis.CyderCommon;
import cyder.handlers.internal.ExceptionHandler;
import cyder.layouts.CyderGridLayout;
import cyder.threads.CyderThreadRunner;
import cyder.ui.*;
import cyder.ui.objects.GridNode;
import cyder.ui.objects.NotificationBuilder;
import cyder.utilities.ColorUtil;
import cyder.utilities.GetterUtil;
import cyder.utilities.ImageUtil;
import cyder.utilities.OSUtil;
import cyder.utilities.objects.GetterBuilder;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

/**
 * A painting widget, not currently intended to be able to edit/markup images.
 */
public class PaintWidget {
    /**
     * The length of the frame.
     */
    public static final int frameLength = 800;

    /**
     * The master painting frame.
     */
    private static CyderFrame paintFrame;

    /**
     * The painting grid.
     */
    private static CyderGrid cyderGrid;

    /**
     * The button for selecting a region.
     */
    private static CyderIconButton selectionTool;

    /**
     * The button for selecting a color.
     */
    private static CyderIconButton selectColor;

    /**
     * Prevent illegal class instantiation.
     */
    private PaintWidget() {
        throw new IllegalStateException(CyderStrings.attemptedInstantiation);
    }

    /**
     * ShowGUI method standard.
     */
    @Widget(triggers = {"paint", "draw"}, description =
            "A painting widget")
    public static void showGUI() {
        if (paintFrame != null)
            paintFrame.dispose(true);

        int padding = 0;

        paintFrame = new CyderFrame(frameLength + padding * 2,
                frameLength + DragLabel.DEFAULT_HEIGHT + padding * 2);
        paintFrame.setTitle("Paint");
        paintFrame.setBackground(CyderIcons.defaultBackgroundLarge);
        paintFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (paintControlsFrame != null)
                    paintControlsFrame.dispose(true);
            }

            @Override
            public void windowClosed(WindowEvent e) {
                if (paintControlsFrame != null)
                    paintControlsFrame.dispose(true);
            }
        });

        cyderGrid = new CyderGrid(200, frameLength);
        cyderGrid.setBounds(padding,DragLabel.DEFAULT_HEIGHT + padding - 5, frameLength, frameLength);
        paintFrame.getContentPane().add(cyderGrid);
        cyderGrid.setDrawExtendedBorder(true);
        cyderGrid.setResizable(true);
        cyderGrid.setDrawGridLines(false);
        cyderGrid.installClickPlacer();
        cyderGrid.installDragPlacer();
        cyderGrid.setSmoothScrolling(true);
        cyderGrid.setDrawWidth(DEFAULT_BRUSH_WIDTH);
        cyderGrid.setNodeColor(currentPaintColor);

        paintFrame.setVisible(true);
        paintFrame.setLocationRelativeTo(CyderCommon.getDominantFrame());

        paintFrame.setMenuEnabled(true);
        paintFrame.addMenuItem("Export png", () -> CyderThreadRunner.submit(() -> {
            if (cyderGrid.getGridNodes().size() == 0) {
                paintFrame.notify("Please place at least one node before saving");
                return;
            }

            String base = "image";
            int increment = 0;
            String defaultFilename = base + increment + ".png";

            String path = OSUtil.buildPath("dynamic","users",
                    ConsoleFrame.getConsoleFrame().getUUID(), "Files");

            while (new File(path + OSUtil.FILE_SEP + defaultFilename).exists()) {
                increment++;
                defaultFilename = base + increment + ".png";
            }

            //todo don't backup a json unless it can be parsed as a user with ALL getters called
            // and not return null

            //todo pref injection passes but comment better?

            // todo double comma fucks it up somewhere and it unparses it? but leaves window color out
            // but doesn't continue with pref injections which trips up later

            // maybe re-use it to validate getters and setters

            // todo why is it parsing AWAY A DAMN VALUE THAT IS IN THE USER CLASS?

            // attempt to backup the most recent state of the json
            //todo userJsonBackupSubroutine(f);

            GetterBuilder builder = new GetterBuilder("Filename");
            builder.setRelativeTo(paintFrame);
            builder.setInitialString(defaultFilename);
            builder.setSubmitButtonColor(CyderColors.regularPink);
            builder.setSubmitButtonText("Save Image");
            builder.setFieldTooltip("The filename to save the image");
            String filename = GetterUtil.getInstance().getString(builder);
            if (OSUtil.isValidFilename(filename)) {
                BufferedImage image = new BufferedImage(cyderGrid.getNodeDimensionLength(),
                        cyderGrid.getNodeDimensionLength(), BufferedImage.TYPE_INT_ARGB);

                Graphics2D g2d = (Graphics2D) image.getGraphics();

                for (GridNode node : cyderGrid.getGridNodes()) {
                    g2d.setColor(node.getColor());
                    g2d.fillRect(node.getX(), node.getY(), 1, 1);
                }

                try {
                    ImageIO.write(image, "png", OSUtil.createFileInUserSpace(filename));

                    NotificationBuilder notifBuilder = new NotificationBuilder(
                            "Successfully saved grid as \"" + filename
                            + "\" to your Files/ directory. Click me to open it");
                    notifBuilder.setOnKillAction(() -> ImageUtil.drawBufferedImage(image, filename));
                    paintFrame.notify(notifBuilder);
                } catch (Exception exception) {
                    ExceptionHandler.handle(exception);
                    paintFrame.notify("Could not save image at this time");
                }
            } else {
                paintFrame.notify("Sorry, but \"" + filename + "\" is not a valid filename");
            }
        }, "Paint Grid Exporter"));
        paintFrame.addMenuItem("Layer Image", () -> CyderThreadRunner.submit(() -> {
            try {
                //todo
            } catch (Exception e) {
                ExceptionHandler.handle(e);
                paintFrame.notify("Could not add image at this time");
            }
        }, "Paint Grid Image Layerer"));
        paintFrame.addMenuItem("Pixelate", () -> CyderThreadRunner.submit(() -> {
            try {
                //todo
            } catch (Exception e) {
                ExceptionHandler.handle(e);
                paintFrame.notify("Could not pixelate image at this time");
            }
        }, "Paint Grid Pixelator"));
        paintFrame.addMenuItem("Resize", () -> CyderThreadRunner.submit(() -> {
            try {
                //todo
            } catch (Exception e) {
                ExceptionHandler.handle(e);
                paintFrame.notify("Could not resize at this time");
            }
        }, "Paint Grid Resizer"));

        installControlFrames();
    }

    /**
     * The controls frame.
     */
    private static CyderFrame paintControlsFrame;

    /**
     * The list of recently used colors.
     */
    private static ArrayList<Color> recentColors;

    /**
     * The custom component with an overriden paint component.
     */
    private static JLabel recentColorsBlock;

    /**
     * The current color.
     */
    private static Color currentPaintColor = CyderColors.regularPink;

    /**
     * The hex field that displays the current color value.
     */
    private static CyderTextField colorHexField;

    /**
     * The add nodes checkbox.
     */
    private static CyderCheckbox add;

    /**
     * Opens the paint controls frame.
     */
    private static void installControlFrames() {
        if (paintControlsFrame != null)
            paintControlsFrame.dispose();

        recentColors = new ArrayList<>();

        paintControlsFrame = new CyderFrame(frameLength,230);
        paintControlsFrame.setTitle("Paint Controls");
        paintControlsFrame.setResizable(true);

        CyderGridLayout parentLayout = new CyderGridLayout(1,2);

        CyderGridLayout topLayout = new CyderGridLayout(5, 1);
        CyderPanel topLayoutPanel = new CyderPanel(topLayout);
        parentLayout.addComponent(topLayoutPanel, 0, 0);

        CyderGridLayout bottomLayout = new CyderGridLayout(6,1);
        CyderPanel bottomLayoutPanel = new CyderPanel(bottomLayout);
        parentLayout.addComponent(bottomLayoutPanel, 0, 1);

        // vars used for drawing custom component
        final int colorRows = 2;
        final int colorsPerRow = 6;
        final int colorBlockLen = 20;
        final int padding = 5;

        recentColorsBlock = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(Color.BLACK);
                g.fillRect(0,0,colorsPerRow * colorBlockLen + 2 * padding,50);
                g.setColor(CyderColors.vanila);
                g.fillRect(padding,padding,colorsPerRow * colorBlockLen,40);

                int numColorsPainted = 0;
                int currentX = padding;
                int currentY = padding;

                // paint 10 colors at most
                while (numColorsPainted < Math.min(colorRows * colorsPerRow, recentColors.size())) {
                    g.setColor(recentColors.get(recentColors.size() - numColorsPainted - 1));
                    g.fillRect(currentX, currentY, colorBlockLen, colorBlockLen);

                    currentX += colorBlockLen;

                    if (currentX >= padding + colorsPerRow * colorBlockLen) {
                        currentX = padding;
                        currentY += colorBlockLen;
                    }

                    numColorsPainted++;
                }

                // draw sep lines between colors
                g.setColor(Color.BLACK);
                // horizontal lines
                for (int i = 0 ; i < colorRows ; i++) {
                    g.drawLine(0, padding + colorBlockLen,
                            2 * padding + colorsPerRow * colorBlockLen, padding + colorBlockLen);
                }
                // vertical lines
                for (int i = 0 ; i < colorsPerRow ; i++) {
                    g.drawLine(padding + i * colorBlockLen, 0, padding + i * colorBlockLen,
                            2 * padding + colorRows * colorBlockLen);
                }
            }
        };
        recentColorsBlock.setSize(130,50);
        recentColorsBlock.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int x = e.getX();
                int y = e .getY();

                // sub padding from both
                x -= padding;
                y -= padding;

                // figure out grid points
                int xGrid = x / colorBlockLen;
                int yGrid = y / colorBlockLen;
                int revIndex = xGrid + yGrid * colorsPerRow;

                // make sure in bounds
                if (recentColors.size() < 1 + revIndex)
                    return;

                // get clicked color and set
                setNewPaintColor(recentColors.get(recentColors.size() - 1 - revIndex));
            }
        });
        topLayout.addComponent(recentColorsBlock, 0, 0);

        colorHexField = new CyderTextField(11);
        colorHexField.setHorizontalAlignment(JTextField.CENTER);
        colorHexField.setToolTipText("Format: 45FF00 for hex or 255,255,255 for rgb");
        colorHexField.setSize(110, 40);
        colorHexField.setRegexMatcher(CyderRegexPatterns.rgbOrHex);
        colorHexField.addActionListener(e -> {
            String text = colorHexField.getText();

            if (text.contains(",")) {
                String[] parts = text.split(",");

                if (parts.length != 3) {
                    paintControlsFrame.notify("Could not parse color");
                } else {
                    try {
                        int r = Integer.parseInt(parts[0]);
                        int g = Integer.parseInt(parts[1]);
                        int b = Integer.parseInt(parts[2]);

                        Color newColor = new Color(r, g, b);
                        setNewPaintColor(newColor);
                    } catch (Exception ignored) {
                        paintControlsFrame.notify("Could not parse color");
                    }
                }
            } else {
                try {
                    Color newColor = ColorUtil.hexToRgb(colorHexField.getText());
                    setNewPaintColor(newColor);
                } catch (Exception ignored) {
                    paintControlsFrame.notify("Could not parse color");
                }
            }
        });
        colorHexField.setText(ColorUtil.rgbToHexString(currentPaintColor));
        topLayout.addComponent(colorHexField, 1, 0);

        JLabel historyLabel = new JLabel();
        historyLabel.setSize(120, 100);

        CyderLabel undoLabel = new CyderLabel("Undo");
        undoLabel.setBounds(5,5,50, 30);
        historyLabel.add(undoLabel);

        CyderLabel redoLabel = new CyderLabel("Redo");
        redoLabel.setBounds(5 + 52 + 10,5,50, 30);
        historyLabel.add(redoLabel);

        CyderIconButton undo = new CyderIconButton("Undo",
                new ImageIcon("static/pictures/paint/undo.png"),
                new ImageIcon("static/pictures/paint/undo_hover.png"), null);
        undo.setBounds(5, 100 - 60, 52, 49);
        undo.addActionListener(e -> {
            cyderGrid.backwardState();
            cyderGrid.revalidate();
            cyderGrid.repaint();
            paintFrame.revalidate();
            paintFrame.repaint();
        });
        historyLabel.add(undo);

        CyderIconButton redo = new CyderIconButton("Redo",
                new ImageIcon("static/pictures/paint/redo.png"),
                new ImageIcon("static/pictures/paint/redo_hover.png"), null);
        redo.setBounds(5 + 52 + 10, 100 - 60,52, 49);
        redo.addActionListener(e -> {
            cyderGrid.forwardState();
            cyderGrid.revalidate();
            cyderGrid.repaint();
            paintFrame.revalidate();
            paintFrame.repaint();
        });
        historyLabel.add(redo);

        topLayout.addComponent(historyLabel,2,0);

        JLabel checkBoxLabel = new JLabel();
        checkBoxLabel.setSize(120, 100);

        CyderCheckboxGroup group = new CyderCheckboxGroup();

        CyderLabel addLabel = new CyderLabel("Add");
        addLabel.setBounds(5,5,50, 30);
        checkBoxLabel.add(addLabel);

        add = new CyderCheckbox();
        add.setToolTipText("Paint cells");
        add.setBounds(5, 100 - 55, 50, 50);
        add.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cyderGrid.setMode(CyderGrid.Mode.ADD);
            }
        });
        group.addCheckbox(add);
        add.setSelected();
        checkBoxLabel.add(add);

        CyderLabel deletelabel = new CyderLabel("Delete");
        deletelabel.setBounds(5 + 50 + 10,5,50, 30);
        checkBoxLabel.add(deletelabel);

        CyderCheckbox delete = new CyderCheckbox();
        delete.setBounds(5 + 50 + 10, 100 - 55, 50, 50);
        delete.setToolTipText("Delete cells");
        delete.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                cyderGrid.setMode(CyderGrid.Mode.DELETE);
            }
        });
        group.addCheckbox(delete);
        checkBoxLabel.add(delete);

        topLayout.addComponent(checkBoxLabel, 3, 0);

        JLabel sliderLabel = new JLabel();
        sliderLabel.setSize(140, 80);

        brushWidth = DEFAULT_BRUSH_WIDTH;

        CyderLabel brushLabel = new CyderLabel("Brush width: " + brushWidth);
        brushLabel.setBounds(10, 5, 120, 40);
        sliderLabel.add(brushLabel);

        JSlider brushWidthSlider = new JSlider(JSlider.HORIZONTAL, MIN_BRUSH_WIDTH,
                MAX_BRUSH_WIDTH, brushWidth);
        CyderSliderUI UI = new CyderSliderUI(brushWidthSlider);
        UI.setThumbStroke(new BasicStroke(2.0f));
        UI.setSliderShape(SliderShape.RECT);
        UI.setFillColor(Color.black);
        UI.setOutlineColor(CyderColors.navy);
        UI.setNewValColor(CyderColors.regularBlue);
        UI.setOldValColor(CyderColors.regularPink);
        UI.setTrackStroke(new BasicStroke(3.0f));
        brushWidthSlider.setUI(UI);
        brushWidthSlider.setSize(250, 40);
        brushWidthSlider.setPaintTicks(false);
        brushWidthSlider.setPaintLabels(false);
        brushWidthSlider.setVisible(true);
        brushWidthSlider.setValue(brushWidth);
        brushWidthSlider.addChangeListener(e -> {
            int newWidth = brushWidthSlider.getValue();
            brushWidth = newWidth;
            brushLabel.setText("Brush width: " + newWidth);
            cyderGrid.setDrawWidth(brushWidth);
        });
        brushWidthSlider.setOpaque(false);
        brushWidthSlider.setToolTipText("Brush Width");
        brushWidthSlider.setFocusable(false);
        brushWidthSlider.repaint();
        brushWidthSlider.setBounds(10, 40, 120, 40);
        sliderLabel.add(brushWidthSlider);

        topLayout.addComponent(sliderLabel, 4, 0);

        selectionTool = new CyderIconButton("Select Region",
                new ImageIcon("static/pictures/paint/select.png"),
                new ImageIcon("static/pictures/paint/select_hover.png"), null);
        selectionTool.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (cyderGrid.getMode() == CyderGrid.Mode.SELECTION) {
                    selectionTool.setIcon(new ImageIcon("static/pictures/paint/select.png"));
                } else {
                    selectionTool.setIcon(new ImageIcon("static/pictures/paint/select_hover.png"));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (cyderGrid.getMode() != CyderGrid.Mode.SELECTION) {
                    selectionTool.setIcon(new ImageIcon("static/pictures/paint/select.png"));
                } else {
                    selectionTool.setIcon(new ImageIcon("static/pictures/paint/select_hover.png"));
                }
            }
        });
        selectionTool.addActionListener(e -> toggleSelectionMode());
        selectionTool.setSize(50, 50);
        bottomLayout.addComponent(selectionTool, 0,0);

        CyderIconButton cropToRegion = new CyderIconButton("Crop Region",
                new ImageIcon("static/pictures/paint/crop.png"),
                new ImageIcon("static/pictures/paint/crop_hover.png"), null);
        cropToRegion.setSize(50, 50);
        cropToRegion.addActionListener(e -> cyderGrid.cropToRegion());
        bottomLayout.addComponent(cropToRegion, 1,0);

        CyderIconButton deleteRegion = new CyderIconButton("Delete Region",
                new ImageIcon("static/pictures/paint/cut.png"),
                new ImageIcon("static/pictures/paint/cut_hover.png"), null);
        deleteRegion.setSize(66, 50);
        deleteRegion.setToolTipText("Cut region");
        deleteRegion.addActionListener(e -> cyderGrid.deleteRegion());
        bottomLayout.addComponent(deleteRegion, 2,0);

        selectColor = new CyderIconButton("Select Color",
                new ImageIcon("static/pictures/paint/select_color.png"),
                new ImageIcon("static/pictures/paint/select_color_hover.png"), null);
        selectColor.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (cyderGrid.getMode() == CyderGrid.Mode.COLOR_SELECTION) {
                    selectColor.setIcon(new ImageIcon("static/pictures/paint/select_color.png"));
                } else {
                    selectColor.setIcon(new ImageIcon("static/pictures/paint/select_color_hover.png"));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (cyderGrid.getMode() != CyderGrid.Mode.COLOR_SELECTION) {
                    selectColor.setIcon(new ImageIcon("static/pictures/paint/select_color.png"));
                } else {
                    selectColor.setIcon(new ImageIcon("static/pictures/paint/select_color_hover.png"));
                }
            }
        });
        selectColor.setSize(50, 50);
        selectColor.addActionListener(e -> toggleColorSelection());
        bottomLayout.addComponent(selectColor, 3,0);

        CyderIconButton rotate = new CyderIconButton("Rotate Region",
                new ImageIcon("static/pictures/paint/rotate.png"),
                new ImageIcon("static/pictures/paint/rotate_hover.png"), null);
        rotate.setSize(50, 50);
        rotate.addActionListener(e -> cyderGrid.rotateRegion());
        bottomLayout.addComponent(rotate, 4,0);

        // selection region reflecting
        CyderIconButton reflect = new CyderIconButton("Reflect Region",
                new ImageIcon("static/pictures/paint/reflect.png"),
                new ImageIcon("static/pictures/paint/reflect_hover.png"), null);
        reflect.setSize(51, 50);
        reflect.addActionListener(e -> cyderGrid.reflectRegionHorizontally());
        bottomLayout.addComponent(reflect, 5,0);

        // use master layout as content pane
        CyderPanel panel = new CyderPanel(parentLayout);
        paintControlsFrame.setContentPanel(panel);

        // init resizing since we can due to the layout
        paintControlsFrame.initializeResizing();
        paintControlsFrame.setResizable(true);
        paintControlsFrame.setMinimumSize(paintControlsFrame.getSize());
        paintControlsFrame.setMaximumSize(new Dimension(
                (int) (paintControlsFrame.getWidth() * 1.5),
                paintControlsFrame.getHeight()));
        paintControlsFrame.setBackgroundResizing(true);

        // initial colors
        setNewPaintColor(CyderColors.navy);
        setNewPaintColor(CyderColors.regularPink);
        setNewPaintColor(CyderColors.regularOrange);
        setNewPaintColor(CyderColors.regularGreen);
        setNewPaintColor(CyderColors.regularBlue);
        setNewPaintColor(CyderColors.tooltipForegroundColor);

        // set visibility
        paintControlsFrame.setVisible(true);

        // place controls right below grid frame
        int y = paintFrame.getY() + paintFrame.getHeight();
        if (y + paintControlsFrame.getHeight() > paintFrame.getMonitorBounds().getHeight())
            y = paintFrame.getMonitorBounds().getBounds().height - paintControlsFrame.getHeight();
        paintControlsFrame.setLocation(paintFrame.getX(), y);
    }

    /**
     * The default brush width.
     */
    public static final int DEFAULT_BRUSH_WIDTH = 2;

    /**
     * The maximum brush width.
     */
    public static final int MAX_BRUSH_WIDTH = 20;

    /**
     * The minimum brush width.
     */
    public static final int MIN_BRUSH_WIDTH = 1;

    /**
     * The default brush width.
     */
    private static int brushWidth = DEFAULT_BRUSH_WIDTH;

    /**
     * Sets the current color and updates the recent colors block
     *
     * @param newColor the new color
     */
    public static void setNewPaintColor(Color newColor) {
        // if no change, ignore
        if (newColor.equals(currentPaintColor))
            return;

        // set the current paint
        currentPaintColor = newColor;

        // update the hex field with our current color
        if (colorHexField != null)
            colorHexField.setText(ColorUtil.rgbToHexString(newColor));

        // ensure if list contains color, it's pulled to the front
        // of recent colors and is not duplicated in the list
        if (recentColors.contains(newColor)) {
            ArrayList<Color> newRecentColors = new ArrayList<>();

            // add all colors that aren't the new one, remove possible duplicates somehow
            for (Color recentColor : recentColors) {
                if (!recentColor.equals(newColor) && !newRecentColors.contains(recentColor))
                    newRecentColors.add(recentColor);
            }

            // add the new one to the end
            newRecentColors.add(newColor);

            // set recent colors to new object
            recentColors = newRecentColors;
        } else {
            recentColors.add(newColor);
        }

        // repaint block to update colors
        recentColorsBlock.repaint();

        // set grid's paint
        cyderGrid.setNodeColor(currentPaintColor);

        // ensure everything reflects the mode being adding cells
        resetToAdding();
    }

    /**
     * Handles the button press for selection mode.
     */
    private static void toggleSelectionMode() {
        CyderGrid.Mode newMode = cyderGrid.getMode() == CyderGrid.Mode.SELECTION
                ? CyderGrid.Mode.ADD : CyderGrid.Mode.SELECTION;

        resetToAdding();

        if (newMode == CyderGrid.Mode.SELECTION) {
            paintFrame.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            cyderGrid.setMode(CyderGrid.Mode.SELECTION);
        } else {
            paintFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

            if (add.isEnabled()) {
                cyderGrid.setMode(CyderGrid.Mode.ADD);
            } else {
                cyderGrid.setMode(CyderGrid.Mode.DELETE);
            }
        }

        Toolkit.getDefaultToolkit().sync();
    }

    /**
     * The icon used for color selection mode
     */
    private static final ImageIcon colorSelectionIcon =
            new ImageIcon("static/pictures/paint/select_color.png");

    /**
     * The cursor used when color selection is toggled on.
     */
    private static final Cursor eyedropperCursor = Toolkit.getDefaultToolkit()
            .createCustomCursor(colorSelectionIcon.getImage(), new Point(0, 30), "eyedropper");

    /**
     * Toggles between states for color mode selection.
     */
    private static void toggleColorSelection() {
        CyderGrid.Mode newMode = cyderGrid.getMode() == CyderGrid.Mode.COLOR_SELECTION
                ? CyderGrid.Mode.ADD : CyderGrid.Mode.COLOR_SELECTION;

        resetToAdding();

        if (newMode == CyderGrid.Mode.COLOR_SELECTION) {
            cyderGrid.setMode(newMode);
            paintFrame.setCursor(eyedropperCursor);
        } else {
            paintFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

            if (add.isEnabled()) {
                cyderGrid.setMode(CyderGrid.Mode.ADD);
            } else {
                cyderGrid.setMode(CyderGrid.Mode.DELETE);
            }
        }

        Toolkit.getDefaultToolkit().sync();
    }

    /**
     * Resets all icons to their default state, the mode to adding,
     * and refreshes the checkboxes.
     */
    private static void resetToAdding() {
        // refresh add/delete buttons
        add.setSelected();

        // de-select toggle-able buttons
        selectColor.setIcon(new ImageIcon("static/pictures/paint/select_color.png"));
        selectionTool.setIcon(new ImageIcon("static/pictures/paint/select.png"));

        // reset grid mode
        cyderGrid.setMode(CyderGrid.Mode.ADD);

        // reset cursor
        paintFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
}
