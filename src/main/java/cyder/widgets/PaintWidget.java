package cyder.widgets;

import com.google.common.collect.ImmutableList;
import cyder.annotations.CyderAuthor;
import cyder.annotations.Vanilla;
import cyder.annotations.Widget;
import cyder.console.Console;
import cyder.constants.CyderColors;
import cyder.constants.CyderIcons;
import cyder.constants.CyderRegexPatterns;
import cyder.enums.Dynamic;
import cyder.enums.Extension;
import cyder.exceptions.IllegalMethodException;
import cyder.files.FileUtil;
import cyder.getter.GetFileBuilder;
import cyder.getter.GetInputBuilder;
import cyder.getter.GetterUtil;
import cyder.handlers.external.PhotoViewer;
import cyder.handlers.internal.ExceptionHandler;
import cyder.layouts.CyderGridLayout;
import cyder.strings.CyderStrings;
import cyder.threads.CyderThreadRunner;
import cyder.ui.CyderPanel;
import cyder.ui.button.CyderIconButton;
import cyder.ui.drag.CyderDragLabel;
import cyder.ui.field.CyderTextField;
import cyder.ui.frame.CyderFrame;
import cyder.ui.frame.FrameType;
import cyder.ui.frame.NotificationBuilder;
import cyder.ui.grid.CyderGrid;
import cyder.ui.grid.GridNode;
import cyder.ui.label.CyderLabel;
import cyder.ui.selection.CyderCheckbox;
import cyder.ui.selection.CyderCheckboxGroup;
import cyder.ui.slider.CyderSliderUi;
import cyder.ui.slider.ThumbShape;
import cyder.user.UserUtil;
import cyder.utils.ColorUtil;
import cyder.utils.ImageUtil;
import cyder.utils.OsUtil;
import cyder.utils.UiUtil;

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
import java.util.LinkedList;
import java.util.Optional;

/**
 * A painting widget, not currently intended to be able to edit/markup images.
 */
@Vanilla
@CyderAuthor
public final class PaintWidget {
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
     * Suppress default constructor.
     */
    private PaintWidget() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * ShowGUI method standard.
     */
    @Widget(triggers = {"paint", "draw"}, description =
            "A painting widget")
    public static void showGui() {
        UiUtil.closeIfOpen(paintFrame);

        paintFrame = new CyderFrame(frameLength,
                frameLength + CyderDragLabel.DEFAULT_HEIGHT);
        paintFrame.setTitle("Paint");
        paintFrame.setBackground(CyderIcons.defaultBackgroundLarge);
        paintFrame.addPreCloseAction(() -> UiUtil.closeIfOpen(paintControlsFrame));
        paintFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                UiUtil.closeIfOpen(paintControlsFrame);
            }

            @Override
            public void windowClosed(WindowEvent e) {
                UiUtil.closeIfOpen(paintControlsFrame);
            }
        });

        cyderGrid = new CyderGrid(200, frameLength);
        cyderGrid.setBounds(0, CyderDragLabel.DEFAULT_HEIGHT - 5, frameLength, frameLength);
        paintFrame.getContentPane().add(cyderGrid);
        cyderGrid.setResizable(true);
        cyderGrid.setDrawGridLines(false);
        cyderGrid.installClickListener();
        cyderGrid.installDragListener();
        cyderGrid.setSmoothScrolling(true);
        cyderGrid.setDrawWidth(DEFAULT_BRUSH_WIDTH);
        cyderGrid.setNodeColor(currentPaintColor);

        paintFrame.setMenuEnabled(true);
        paintFrame.addMenuItem("Export png", () -> CyderThreadRunner.submit(() -> {
            if (cyderGrid.getGridNodes().isEmpty()) {
                paintFrame.notify("Please place at least one node before saving");
                return;
            }

            String base = "image";
            int increment = 0;
            String defaultFilename = base + increment + Extension.PNG.getExtension();

            String path = OsUtil.buildPath(Dynamic.PATH, Dynamic.USERS.getFileName(),
                    Console.INSTANCE.getUuid(), "Files");

            while (new File(path + OsUtil.FILE_SEP + defaultFilename).exists()) {
                increment++;
                defaultFilename = base + increment + Extension.PNG.getExtension();
            }

            Optional<String> optionalFilename = GetterUtil.getInstance().getInput(
                    new GetInputBuilder("Filename", "Enter the filename to save the image as")
                            .setRelativeTo(paintFrame)
                            .setInitialFieldText(defaultFilename)
                            .setSubmitButtonText("Save Image"));
            if (optionalFilename.isEmpty()) return;
            String filename = optionalFilename.get();

            if (!filename.endsWith(Extension.PNG.getExtension())) {
                filename += Extension.PNG.getExtension();
            }

            if (OsUtil.isValidFilename(filename)) {
                BufferedImage image = new BufferedImage(cyderGrid.getNodeDimensionLength(),
                        cyderGrid.getNodeDimensionLength(), BufferedImage.TYPE_INT_ARGB);

                Graphics2D g2d = (Graphics2D) image.getGraphics();

                for (GridNode node : cyderGrid.getGridNodes()) {
                    g2d.setColor(node.getColor());
                    g2d.fillRect(node.getX(), node.getY(), 1, 1);
                }

                try {
                    File referenceFile = UserUtil.createFileInUserSpace(filename);
                    ImageIO.write(image, Extension.PNG.getExtensionWithoutPeriod(), referenceFile);

                    paintFrame.notify(new NotificationBuilder(
                            "Successfully saved grid as \"" + filename
                                    + "\" to your Files/ directory. Click me to view it")
                            .setOnKillAction(() -> PhotoViewer.getInstance(referenceFile).showGui()));
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
                Optional<File> optionalFile = GetterUtil.getInstance().getFile(
                        new GetFileBuilder("Layer Image").setRelativeTo(paintFrame));
                if (optionalFile.isEmpty()) return;
                File chosenImage = optionalFile.get();

                if (FileUtil.validateExtension(chosenImage, FileUtil.SUPPORTED_IMAGE_EXTENSIONS)) {
                    // todo implement after figuring out solution to large grids
                    // todo figure ensure nodes aren't painted out of bounds and grid is always inside by 10 pixels

                    // todo need to tie grid size into a grid state object, as well as relative positions
                    // todo grid state objects should store the difference from the last state
                } else {
                    paintFrame.notify("Image type not supported");
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
                paintFrame.notify("Could not add image at this time");
            }
        }, "Paint Grid Image Layerer"));
        paintFrame.addMenuItem("Pixelate", () -> CyderThreadRunner.submit(() -> {
            try {
                Optional<String> optionalPixelSizeString = GetterUtil.getInstance().getInput(
                        new GetInputBuilder("Pixelator", "Enter the pixel size")
                                .setFieldHintText("Pixel size")
                                .setRelativeTo(paintFrame)
                                .setSubmitButtonText("Pixelate Grid")
                                .setInitialFieldText(String.valueOf(1)));
                if (optionalPixelSizeString.isEmpty()) return;
                String pixelSizeString = optionalPixelSizeString.get();

                int pixelSize = Integer.parseInt(pixelSizeString);

                // no change so continue
                if (pixelSize == 1) {
                    return;
                }

                // convert to image
                BufferedImage image = new BufferedImage(cyderGrid.getNodeDimensionLength(),
                        cyderGrid.getNodeDimensionLength(), BufferedImage.TYPE_INT_ARGB);

                Graphics2D g2d = (Graphics2D) image.getGraphics();

                for (GridNode node : cyderGrid.getGridNodes()) {
                    g2d.setColor(node.getColor());
                    g2d.fillRect(node.getX(), node.getY(), 1, 1);
                }

                BufferedImage newStateImage = ImageUtil.pixelateImage(image, pixelSize);

                LinkedList<GridNode> newState = new LinkedList<>();

                for (int x = 0 ; x < newStateImage.getWidth() ; x++) {
                    for (int y = 0 ; y < newStateImage.getHeight() ; y++) {
                        int color = newStateImage.getRGB(x, y);

                        Color newColor = new Color(
                                (color >> 16) & 0xFF,
                                (color >> 8) & 0xFF,
                                color & 0xFF);

                        // todo need new pixelation algorithm
                        // so you don't have to account for this
                        if (newColor.equals(Color.BLACK))
                            continue;

                        newState.add(new GridNode(newColor, x, y));
                    }
                }

                // set new state
                cyderGrid.setGridState(newState);
            } catch (Exception e) {
                ExceptionHandler.handle(e);
                paintFrame.notify("Could not pixelate image at this time");
            }
        }, "Paint Grid Pixelator"));
        paintFrame.addMenuItem("Scale", () -> CyderThreadRunner.submit(() -> {
            try {
                Optional<String> optionalDimension = GetterUtil.getInstance().getInput(
                        new GetInputBuilder("Enter length", "Enter the canvas length")
                                .setFieldHintText("Grid Length")
                                .setRelativeTo(paintFrame)
                                .setSubmitButtonText("Scale grid")
                                .setInitialFieldText(String.valueOf(cyderGrid.getNodeDimensionLength())));
                if (optionalDimension.isEmpty()) return;
                String dimension = optionalDimension.get();

                int dimensionInt = Integer.parseInt(dimension);

                if (dimensionInt > 0) {
                    // create reference image to resize
                    BufferedImage referenceImage = new BufferedImage(cyderGrid.getNodeDimensionLength(),
                            cyderGrid.getNodeDimensionLength(), BufferedImage.TYPE_INT_ARGB);

                    Graphics2D g2d = (Graphics2D) referenceImage.getGraphics();

                    for (GridNode node : cyderGrid.getGridNodes()) {
                        g2d.setColor(node.getColor());
                        g2d.fillRect(node.getX(), node.getY(), 1, 1);
                    }

                    double scaler = (double) dimensionInt / cyderGrid.getNodeDimensionLength();
                    int len = (int) (scaler * referenceImage.getWidth());

                    BufferedImage newStateImage = ImageUtil.resizeImage(
                            referenceImage, BufferedImage.TYPE_INT_ARGB, len, len);

                    cyderGrid.setNodeDimensionLength(len);

                    LinkedList<GridNode> newState = new LinkedList<>();

                    for (int x = 0 ; x < newStateImage.getWidth() ; x++) {
                        for (int y = 0 ; y < newStateImage.getHeight() ; y++) {
                            int color = newStateImage.getRGB(x, y);

                            // if alpha is empty, don't copy over
                            if (((color >> 24) & 0xFF) == 0)
                                continue;

                            newState.add(new GridNode(new Color(
                                    (color >> 16) & 0xFF,
                                    (color >> 8) & 0xFF,
                                    color & 0xFF), x, y));
                        }
                    }

                    cyderGrid.setGridState(newState);
                } else {
                    paintFrame.notify("Invalid dimensional length");
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, "Paint Grid Scaler"));
        paintFrame.addMenuItem("Controls", PaintWidget::installControlFrames);

        installControlFrames();
    }

    /**
     * Returns the aspect ratio of the provided buffered image.
     *
     * @param img the image to find the aspect ratio of
     * @return the aspect ratio of the provided buffered image
     */
    private static double getAspectRatio(BufferedImage img) {
        return ((double) img.getWidth() / (double) img.getHeight());
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
     * The custom component with an overridden paint component.
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
        UiUtil.closeIfOpen(paintControlsFrame);

        recentColors = new ArrayList<>();

        paintControlsFrame = new CyderFrame(frameLength, 230);
        paintControlsFrame.setTitle("Paint Controls");
        paintControlsFrame.setResizable(true);
        paintControlsFrame.setShouldFastClose(true);

        CyderGridLayout parentLayout = new CyderGridLayout(1, 2);

        CyderGridLayout topLayout = new CyderGridLayout(5, 1);
        CyderPanel topLayoutPanel = new CyderPanel(topLayout);
        parentLayout.addComponent(topLayoutPanel, 0, 0);

        CyderGridLayout bottomLayout = new CyderGridLayout(6, 1);
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
                g.translate(0, 10);
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, colorsPerRow * colorBlockLen + 2 * padding, 50);
                g.setColor(CyderColors.vanilla);
                g.fillRect(padding, padding, colorsPerRow * colorBlockLen, 40);

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
        recentColorsBlock.setSize(130, 60);
        recentColorsBlock.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();

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
        colorHexField.setBounds(0, 40, 110, 40);
        colorHexField.setKeyEventRegexMatcher(CyderRegexPatterns.rgbOrHex.pattern());
        colorHexField.addActionListener(e -> {
            String text = colorHexField.getText();

            if (text.contains(CyderStrings.comma)) {
                String[] parts = text.split(CyderStrings.comma);

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
                    Color newColor = ColorUtil.hexStringToColor(colorHexField.getText());
                    setNewPaintColor(newColor);
                } catch (Exception ignored) {
                    paintControlsFrame.notify("Could not parse color");
                }
            }
        });
        colorHexField.setText(ColorUtil.rgbToHexString(currentPaintColor));

        CyderGridLayout innerLayout = new CyderGridLayout(1, 2);

        CyderLabel colorTextLabel = new CyderLabel("New Color");
        colorTextLabel.setBounds(5, 5, 100, 40);
        innerLayout.addComponent(colorTextLabel, 0, 0);

        innerLayout.addComponent(colorHexField, 0, 1);
        CyderPanel innerPanel = new CyderPanel(innerLayout);

        topLayout.addComponent(innerPanel, 1, 0);

        JLabel historyLabel = new JLabel();
        historyLabel.setSize(120, 100);

        CyderLabel undoLabel = new CyderLabel("Undo");
        undoLabel.setBounds(5, 5, 50, 30);
        historyLabel.add(undoLabel);

        CyderLabel redoLabel = new CyderLabel("Redo");
        redoLabel.setBounds(5 + 52 + 10, 5, 50, 30);
        historyLabel.add(redoLabel);

        ImageIcon undoDefault = new ImageIcon("static/pictures/paint/undo.png");
        ImageIcon undoHoverAndFocus = new ImageIcon("static/pictures/paint/undo_hover.png");
        CyderIconButton undo = new CyderIconButton.Builder("Undo", undoDefault, undoHoverAndFocus)
                .setClickAction(() -> {
                    cyderGrid.backwardState();
                    cyderGrid.revalidate();
                    cyderGrid.repaint();
                    paintFrame.revalidate();
                    paintFrame.repaint();
                }).build();
        undo.setLocation(5, 100 - 60);
        historyLabel.add(undo);

        ImageIcon redoDefault = new ImageIcon("static/pictures/paint/redo.png");
        ImageIcon redoFocusHover = new ImageIcon("static/pictures/paint/redo_hover.png");
        CyderIconButton redo = new CyderIconButton.Builder("Redo", redoDefault, redoFocusHover)
                .setClickAction(() -> {
                    cyderGrid.forwardState();
                    cyderGrid.revalidate();
                    cyderGrid.repaint();
                    paintFrame.revalidate();
                    paintFrame.repaint();
                }).build();
        redo.setLocation(5 + 52 + 10, 100 - 60);
        historyLabel.add(redo);

        topLayout.addComponent(historyLabel, 2, 0);

        JLabel checkBoxLabel = new JLabel();
        checkBoxLabel.setSize(120, 100);

        CyderCheckboxGroup group = new CyderCheckboxGroup();

        CyderLabel addLabel = new CyderLabel("Add");
        addLabel.setBounds(5, 5, 50, 30);
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
        add.setChecked();
        checkBoxLabel.add(add);

        CyderLabel deleteLabel = new CyderLabel("Delete");
        deleteLabel.setBounds(5 + 50 + 10, 5, 50, 30);
        checkBoxLabel.add(deleteLabel);

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
        CyderSliderUi UI = new CyderSliderUi(brushWidthSlider);
        UI.setThumbStroke(new BasicStroke(2.0f));
        UI.setThumbShape(ThumbShape.RECTANGLE);
        UI.setThumbFillColor(Color.black);
        UI.setThumbOutlineColor(CyderColors.navy);
        UI.setRightThumbColor(CyderColors.regularBlue);
        UI.setLeftThumbColor(CyderColors.regularPink);
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

        ImageIcon selectionDefault = new ImageIcon("static/pictures/paint/select.png");
        ImageIcon selectionHoverFocus = new ImageIcon("static/pictures/paint/select_hover.png");
        selectionTool = new CyderIconButton.Builder("Select Region",
                selectionDefault, selectionHoverFocus)
                .setClickAction(PaintWidget::toggleSelectionMode).setToggleButton(true).build();
        selectionTool.setSize(50, 50);
        bottomLayout.addComponent(selectionTool, 0, 0);

        ImageIcon cropDefault = new ImageIcon("static/pictures/paint/crop.png");
        ImageIcon cropHoverFocus = new ImageIcon("static/pictures/paint/crop_hover.png");
        CyderIconButton cropToRegion = new CyderIconButton.Builder("Crop Region", cropDefault,
                cropHoverFocus).setClickAction(() -> cyderGrid.cropToSelectedRegion()).build();
        bottomLayout.addComponent(cropToRegion, 1, 0);

        ImageIcon cutDefault = new ImageIcon("static/pictures/paint/cut.png");
        ImageIcon cutHoverFocus = new ImageIcon("static/pictures/paint/cut_hover.png");
        CyderIconButton deleteRegion = new CyderIconButton.Builder("Cut Region",
                cutDefault, cutHoverFocus).setClickAction(() -> cyderGrid.deleteSelectedRegion()).build();
        bottomLayout.addComponent(deleteRegion, 2, 0);

        ImageIcon selectColorDefault = new ImageIcon("static/pictures/paint/select_color.png");
        ImageIcon selectColorHoverFocus = new ImageIcon("static/pictures/paint/select_color_hover.png");
        selectColor = new CyderIconButton.Builder("Select Color", selectColorDefault, selectColorHoverFocus)
                .setClickAction(PaintWidget::toggleColorSelection).setToggleButton(true).build();
        bottomLayout.addComponent(selectColor, 3, 0);

        ImageIcon rotateDefault = new ImageIcon("static/pictures/paint/rotate.png");
        ImageIcon rotateHoverFocus = new ImageIcon("static/pictures/paint/rotate_hover.png");
        CyderIconButton rotate = new CyderIconButton.Builder("Rotate Region", rotateDefault,
                rotateHoverFocus).setClickAction(() -> cyderGrid.rotateRegion()).build();
        bottomLayout.addComponent(rotate, 4, 0);

        // selection region reflecting
        ImageIcon reflectDefault = new ImageIcon("static/pictures/paint/reflect.png");
        ImageIcon reflectHoverFocus = new ImageIcon("static/pictures/paint/reflect_hover.png");
        CyderIconButton reflect = new CyderIconButton.Builder("Reflect Region", reflectDefault,
                reflectHoverFocus).setClickAction(() -> cyderGrid.reflectRegionHorizontally()).build();
        bottomLayout.addComponent(reflect, 5, 0);

        // use master layout as content pane
        CyderPanel panel = new CyderPanel(parentLayout);
        paintControlsFrame.setCyderLayoutPanel(panel);

        // init resizing since we can due to the layout
        paintControlsFrame.initializeResizing();
        paintControlsFrame.setResizable(true);
        paintControlsFrame.setMinimumSize(paintControlsFrame.getSize());
        paintControlsFrame.setMaximumSize(new Dimension(
                (int) (paintControlsFrame.getWidth() * 1.5),
                paintControlsFrame.getHeight()));
        paintControlsFrame.setBackgroundResizing(true);

        installDefaultPaintColors();

        Rectangle screen = Console.INSTANCE.getConsoleCyderFrame().getMonitorBounds();
        int x = (int) (screen.getX() + (screen.getWidth() - paintControlsFrame.getWidth()) / 2);
        int y = (int) (screen.getHeight() - paintControlsFrame.getHeight()
                - UiUtil.getWindowsTaskbarHeight());

        paintControlsFrame.setLocation(x, y);
        paintControlsFrame.setVisible(true);
        paintControlsFrame.setFrameType(FrameType.POPUP);

        if (paintFrame.isVisible()) return;

        paintFrame.setLocation(x, y - paintFrame.getHeight());
        paintFrame.setVisible(true);
    }

    /**
     * The default pallet colors.
     */
    private static final ImmutableList<Color> defaultPallet = ImmutableList.of(
            CyderColors.navy,
            CyderColors.regularPink,
            CyderColors.regularOrange,
            CyderColors.regularGreen,
            CyderColors.regularBlue,
            CyderColors.regularPurple
    );

    /**
     * Sets up the default paint colors in the pallet.
     */
    private static void installDefaultPaintColors() {
        for (Color paintColor : defaultPallet) {
            setNewPaintColor(paintColor);
        }
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
        selectionTool.reset();
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
        selectionTool.reset();

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
        add.setChecked();

        // de-select toggle-able buttons
        selectColor.setIcon(new ImageIcon("static/pictures/paint/select_color.png"));

        // reset grid mode
        cyderGrid.setMode(CyderGrid.Mode.ADD);

        // reset cursor
        paintFrame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }
}
