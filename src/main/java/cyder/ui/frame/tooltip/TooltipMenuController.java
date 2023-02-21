package cyder.ui.frame.tooltip;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.HtmlTags;
import cyder.exceptions.FatalException;
import cyder.getter.GetInputBuilder;
import cyder.getter.GetterUtil;
import cyder.managers.ProgramModeManager;
import cyder.props.Props;
import cyder.strings.StringUtil;
import cyder.threads.CyderThreadFactory;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;
import cyder.ui.UiUtil;
import cyder.ui.drag.CyderDragLabel;
import cyder.ui.frame.CyderFrame;
import cyder.ui.frame.enumerations.FrameType;
import cyder.ui.pane.CyderOutputPane;
import cyder.ui.pane.CyderScrollPane;
import cyder.utils.ColorUtil;
import cyder.utils.SecurityUtil;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import static cyder.strings.CyderStrings.comma;
import static cyder.strings.CyderStrings.quote;

/**
 * A controller for the tooltip menu of a particular {@link CyderFrame}.
 */
public class TooltipMenuController {
    /**
     * The tooltip menu component border color.
     */
    private static final Color borderColor = Color.black;

    /**
     * The default tooltip menu width.
     */
    private static final int defaultWidth = 120;

    /**
     * The tooltip menu border length
     */
    private static final int borderLength = 5;

    /**
     * The height necessary for a single tooltip menu item.
     */
    private static final int itemLabelHeight = 30;

    /**
     * The opacity decrement for the tooltip menu label fade-out animation.
     */
    private static final int opacityAnimationDecrement = 2;

    /**
     * The animation timeout for the tooltip menu label fade-out animation.
     */
    private static final Duration opacityAnimationTimeout = Duration.ofMillis(2);

    /**
     * The thread name for the tooltip menu label fade-out animation.
     */
    private static final String animateOutThreadName = "CyderFrame tooltip menu fade-out animation";

    /**
     * The name of the thread which waits for the user input size to set the frame to. Triggered via
     * the tooltip menu item.
     */
    private static final String setFrameSizeTooltipMenuWaiterThreadName = "CyderFrame size setter waiter";

    /**
     * The name of the thread which waits for the user input location to set the frame to. Triggered via
     * the tooltip menu item.
     */
    private static final String setFrameLocationTooltipMenuWaiterThreadName = "CyderFrame location setter waiter";

    /**
     * The thread name for the tooltip menu fade-out waiter for when the
     * user does not interact with the tooltip menu label.
     */
    private static final String tooltipMenuFadeoutWaiterThreadName = "CyderFrame tooltip"
            + " menu fade-out if mouse never enters label waiter thread";

    /**
     * The name of the thread which waits for the mouse to be out of the tooltip menu label
     * for at least {@link #outOfTooltipMenuBeforeFadeOut}.
     */
    private static final String mouseOutOfTooltipMenuListenerThreadName = "Mouse out of tooltip menu listener";

    /**
     * The timeout before fading out the tooltip menu label if the user never interacts with the label.
     */
    private static final Duration noInteractionFadeOutTimeout = Duration.ofSeconds(2);

    /**
     * The duration a mouse must remain outside of the menu before the fade out animation is invoked.
     */
    private static final Duration outOfTooltipMenuBeforeFadeOut = Duration.ofMillis(1400);

    /**
     * The frame this controller has control over.
     */
    private final CyderFrame controlFrame;

    /**
     * The menu items for this tooltip menu controller.
     */
    private final ArrayList<JLabel> menuItems = new ArrayList<>();

    /**
     * The getter util for getting the frame location input from the user.
     */
    private final GetterUtil tooltipMenuItemFrameLocationGetterUtil = GetterUtil.getInstance();

    /**
     * The getter util for getting the frame size input from the user.
     */
    private final GetterUtil tooltipMenuItemFrameSizeGetterUtil = GetterUtil.getInstance();

    /**
     * Whether the mouse has entered the tooltip menu after it was generated.
     */
    private final AtomicBoolean mouseHasEnteredTooltipMenu = new AtomicBoolean();

    /**
     * The opacity the menu should be painted with.
     */
    private final AtomicInteger opacity = new AtomicInteger(ColorUtil.opacityRange.upperEndpoint());

    /**
     * The current control key to validate fade-out animation requests.
     */
    private final AtomicReference<String> currentFadeOutKey = new AtomicReference<>();

    /**
     * The generated menu label this controller uses for the tooltip menu label.
     */
    private JLabel tooltipMenuLabel;

    /**
     * The scroll pane on the menu label containing the menu items.
     */
    private CyderScrollPane menuScroll;

    /**
     * The fade-out animation.
     */
    private ListenableFuture<Void> fadeOutAnimation;

    /**
     * The no interaction fade out future. If the user triggers the tooltip menu but does not interact
     * after {@link #noInteractionFadeOutTimeout}, the menu is animated out.
     */
    private ListenableFuture<Void> noInteractionFadeOutWaiter;

    /**
     * The waiter for waiting for a mouse to be out of the menu label.
     */
    private ListenableFuture<Void> mouseOutOfMenuWaiter;

    /**
     * Constructs a new tooltip menu controller.
     *
     * @param controlFrame the frame this controller will control
     */
    public TooltipMenuController(CyderFrame controlFrame) {
        Preconditions.checkNotNull(controlFrame);
        Preconditions.checkArgument(controlFrame.getFrameType() == FrameType.DEFAULT);
        Preconditions.checkArgument(!controlFrame.isBorderlessFrame());

        this.controlFrame = controlFrame;
        initializeMenuItems();
        constructTooltipMenuLabel();
    }

    /**
     * Returns the frame this controller controls.
     *
     * @return the frame this controller controls
     */
    public CyderFrame getControlFrame() {
        return controlFrame;
    }

    /**
     * Shows the tooltip menu label on the control frame, using the provided
     * trigger point to calculate the point to place the menu.
     *
     * @param triggerPoint the point at which the user clicked to trigger showing the tooltip menu label
     * @param triggerLabel the drag label which triggered this show event
     */
    public void show(Point triggerPoint, CyderDragLabel triggerLabel) {
        Preconditions.checkNotNull(triggerPoint);
        double x = triggerPoint.getX();
        double y = triggerPoint.getY();
        Preconditions.checkArgument(x >= 0 && x <= controlFrame.getWidth());
        Preconditions.checkArgument(y >= 0 && y <= controlFrame.getHeight());

        String localKey = SecurityUtil.generateUuid();
        synchronized (controlFrame) {
            currentFadeOutKey.set(localKey);
            cancelAllTasks();
        }

        mouseHasEnteredTooltipMenu.set(false);

        revalidateLabelAndScrollSize();
        opacity.set(ColorUtil.opacityRange.upperEndpoint());
        tooltipMenuLabel.repaint();
        tooltipMenuLabel.setLocation(calculateLocation(triggerPoint, triggerLabel));
        tooltipMenuLabel.setVisible(true);

        startNewNoInteractionFadeOutWaiter(localKey);
        startNewMouseOutOfMenuWaiter(localKey);
    }

    /**
     * Cancels the following tasks if running:
     * <ul>
     *     <li>The mouse out of menu waiter</li>
     *     <li>The no interaction waiter</li>
     *     <li>The fade out animation</li>
     * </ul>
     */
    public void cancelAllTasks() {
        cancelMouseOutOfMenuWaiter();
        cancelNoInteractionFadeOutWaiter();
        cancelFadeOutAnimation();
    }

    /**
     * Cancels the {@link #noInteractionFadeOutWaiter} if running.
     */
    private void cancelNoInteractionFadeOutWaiter() {
        if (noInteractionFadeOutWaiter != null) {
            noInteractionFadeOutWaiter.cancel(true);
            noInteractionFadeOutWaiter = null;
        }
    }

    /**
     * Starts a new {@link #noInteractionFadeOutWaiter} task.
     *
     * @param controlKey the key necessary to perform a fade out animation
     */
    private void startNewNoInteractionFadeOutWaiter(String controlKey) {
        Preconditions.checkNotNull(controlKey);
        Preconditions.checkArgument(!controlKey.isEmpty());

        noInteractionFadeOutWaiter = Futures.submit(() -> {
            ThreadUtil.sleep(noInteractionFadeOutTimeout.toMillis());
            if (!mouseHasEnteredTooltipMenu.get()) {
                if (!controlKey.equals(currentFadeOutKey.get())) return;
                fadeOut();
            }
        }, Executors.newSingleThreadExecutor(new CyderThreadFactory(tooltipMenuFadeoutWaiterThreadName)));
    }

    /**
     * Cancels the mouse out of menu waiter if running.
     */
    private void cancelMouseOutOfMenuWaiter() {
        if (mouseOutOfMenuWaiter != null) {
            mouseOutOfMenuWaiter.cancel(true);
            mouseOutOfMenuWaiter = null;
        }
    }

    /**
     * Starts a new {@link #mouseOutOfMenuWaiter} task.
     *
     * @param controlKey the key necessary to perform a fade out animation
     */
    private void startNewMouseOutOfMenuWaiter(String controlKey) {
        Preconditions.checkNotNull(controlKey);
        Preconditions.checkArgument(!controlKey.isEmpty());

        mouseOutOfMenuWaiter = Futures.submit(() -> {
            while (true) {
                if (tooltipMenuLabel.getMousePosition() == null) {
                    ThreadUtil.sleep(outOfTooltipMenuBeforeFadeOut.toMillis());
                    if (tooltipMenuLabel.getMousePosition() == null) {
                        if (!controlKey.equals(currentFadeOutKey.get())) return;
                        fadeOut();
                        return;
                    }
                }
            }
        }, Executors.newSingleThreadExecutor(new CyderThreadFactory(mouseOutOfTooltipMenuListenerThreadName)));
    }

    /**
     * Cancels the {@link #fadeOutAnimation} if currently running.
     */
    private void cancelFadeOutAnimation() {
        if (fadeOutAnimation != null) {
            fadeOutAnimation.cancel(true);
            fadeOutAnimation = null;
        }
    }

    /**
     * Animates out the tooltip menu label via an opacity decrement transition.
     */
    private void fadeOut() {
        if (fadeOutAnimation != null && !fadeOutAnimation.isCancelled()) return;

        fadeOutAnimation = Futures.submit(() -> {
            opacity.set(ColorUtil.opacityRange.upperEndpoint());

            while (opacity.get() >= opacityAnimationDecrement) {
                opacity.set(opacity.get() - opacityAnimationDecrement);
                tooltipMenuLabel.repaint();
                ThreadUtil.sleep(opacityAnimationTimeout.toMillis());
            }

            opacity.set(ColorUtil.opacityRange.lowerEndpoint());
            tooltipMenuLabel.repaint();
            tooltipMenuLabel.setVisible(false);
        }, Executors.newSingleThreadExecutor(new CyderThreadFactory(animateOutThreadName)));
    }

    /**
     * Initializes the menu items the tooltip menu label will show.
     */
    private void initializeMenuItems() {
        menuItems.add(new TooltipMenuItem(TooltipMenuItemType.TO_BACK.getLabelText())
                .addMouseClickAction(() -> tooltipMenuLabel.setVisible(false))
                .addMouseClickAction(controlFrame::toBack)
                .buildMenuItemLabel());
        menuItems.add(new TooltipMenuItem(TooltipMenuItemType.FRAME_LOCATION.getLabelText())
                .addMouseClickAction(() -> tooltipMenuLabel.setVisible(false))
                .addMouseClickAction(this::onFrameLocationTooltipMenuItemPressed)
                .buildMenuItemLabel());

        if (controlFrame.isResizingAllowed()) {
            menuItems.add(new TooltipMenuItem(TooltipMenuItemType.FRAME_SIZE.getLabelText())
                    .addMouseClickAction(() -> tooltipMenuLabel.setVisible(false))
                    .addMouseClickAction(this::onFrameSizeTooltipMenuItemPressed)
                    .buildMenuItemLabel());
        }
        if (ProgramModeManager.INSTANCE.getProgramMode().hasDeveloperPriorityLevel()) {
            menuItems.add(new TooltipMenuItem(TooltipMenuItemType.SCREENSHOT.getLabelText())
                    .addMouseClickAction(() -> tooltipMenuLabel.setVisible(false))
                    .addMouseClickAction(() -> {
                        UiUtil.screenshotCyderFrame(controlFrame);
                        controlFrame.notify("Saved screenshot to your user's Files directory");
                    })
                    .buildMenuItemLabel());
        }
    }

    // todo should think about synchronizing on something other than the control frame

    /**
     * Constructs the tooltip menu label for this controller.
     */
    private void constructTooltipMenuLabel() {
        synchronized (controlFrame) {
            tooltipMenuLabel = new JLabel() {
                @Override
                public void paint(Graphics g) {
                    int w = calculateWidth();
                    int h = calculateHeight();

                    g.setColor(ColorUtil.setColorOpacity(borderColor, opacity.get()));
                    g.fillRect(0, 0, w, h);
                    g.setColor(ColorUtil.setColorOpacity(CyderColors.getGuiThemeColor(), opacity.get()));
                    g.fillRect(borderLength, borderLength, w - 2 * borderLength, h - 2 * borderLength);
                    super.paint(g);
                }
            };
            tooltipMenuLabel.setVisible(false);

            JTextPane menuPane = UiUtil.generateJTextPaneWithInvisibleHorizontalScrollbar();
            menuPane.setEditable(false);
            menuPane.setFocusable(false);
            menuPane.setOpaque(false);
            menuPane.setAutoscrolls(false);
            menuPane.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    mouseHasEnteredTooltipMenu.set(true);
                }
            });

            StyledDocument doc = menuPane.getStyledDocument();
            SimpleAttributeSet alignment = new SimpleAttributeSet();
            StyleConstants.setAlignment(alignment, StyleConstants.ALIGN_CENTER);
            doc.setParagraphAttributes(0, doc.getLength(), alignment, false);

            menuScroll = new CyderScrollPane(menuPane);
            menuScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            menuScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            menuScroll.setThumbSize(5);
            menuScroll.getViewport().setOpaque(false);
            menuScroll.setFocusable(true);
            menuScroll.setOpaque(false);
            menuScroll.setAutoscrolls(false);
            menuScroll.setThumbColor(CyderColors.regularPink);
            menuScroll.setBackground(CyderColors.getGuiThemeColor());

            StringUtil stringUtil = new StringUtil(new CyderOutputPane(menuPane));
            IntStream.range(0, menuItems.size()).forEach(index -> {
                if (index == menuItems.size() - 1) {
                    stringUtil.printComponent(menuItems.get(index));
                } else {
                    stringUtil.printlnComponent(menuItems.get(index));
                }
            });

            tooltipMenuLabel.add(menuScroll);
            revalidateLabelAndScrollSize();

            controlFrame.getTrueContentPane().add(tooltipMenuLabel, JLayeredPane.DRAG_LAYER);
        }
    }

    /**
     * Revalidates the size of the label and the size and location of the menu scroll.
     */
    private void revalidateLabelAndScrollSize() {
        int w = calculateWidth();
        int h = calculateHeight();
        tooltipMenuLabel.setSize(w, h);
        menuScroll.setBounds(borderLength, borderLength, w - 2 * borderLength, h - 2 * borderLength);
    }

    /**
     * Returns the width to use for the tooltip menu label.
     *
     * @return the width to use for the tooltip menu label
     */
    private int calculateWidth() {
        int necessaryWidth = 2 * borderLength + defaultWidth;
        int maxWidthOnParent = controlFrame.getWidth() - 2 * CyderFrame.BORDER_LEN;
        return Math.min(necessaryWidth, maxWidthOnParent);
    }

    /**
     * Returns the height to use for the tooltip menu label.
     *
     * @return the height to use for the tooltip menu label
     */
    private int calculateHeight() {
        int necessaryHeight = 2 * borderLength + itemLabelHeight * menuItems.size();
        int maxHeightOnParent = controlFrame.getHeight() - Props.dragLabelHeight.getValue() - CyderFrame.BORDER_LEN;
        return Math.min(necessaryHeight, maxHeightOnParent);
    }

    /**
     * Calculates the point to place the tooltip menu label at based on the generating event and drag label.
     *
     * @param generatingPoint the point which caused the invocation of this method
     * @param generatingLabel the label which generated the generating event
     * @return the point to place the tooltip menu label at on this frame
     */
    private Point calculateLocation(Point generatingPoint, CyderDragLabel generatingLabel) {
        Preconditions.checkNotNull(generatingPoint);
        Preconditions.checkNotNull(generatingLabel);
        Preconditions.checkNotNull(tooltipMenuLabel);

        int frameWidth = controlFrame.getWidth();
        int frameHeight = controlFrame.getHeight();
        int tooltipMenuWidth = tooltipMenuLabel.getWidth();
        int tooltipMenuHeight = tooltipMenuLabel.getHeight();

        double x;
        double y;
        if (generatingLabel.equals(controlFrame.getTopDragLabel())) {
            x = generatingPoint.getX();

            if (x < CyderFrame.BORDER_LEN) {
                x = CyderFrame.BORDER_LEN;
            } else if (x + tooltipMenuWidth + CyderFrame.BORDER_LEN > frameWidth) {
                x = frameWidth - tooltipMenuWidth - CyderFrame.BORDER_LEN;
            }

            y = CyderDragLabel.DEFAULT_HEIGHT;
        } else if (generatingLabel.equals(controlFrame.getLeftDragLabel())) {
            x = CyderFrame.BORDER_LEN;

            y = generatingPoint.getY();
            if (y < CyderDragLabel.DEFAULT_HEIGHT) {
                y = CyderDragLabel.DEFAULT_HEIGHT;
            } else if (y + tooltipMenuHeight + CyderFrame.BORDER_LEN > frameHeight) {
                y = frameHeight - tooltipMenuHeight - CyderFrame.BORDER_LEN;
            }
        } else if (generatingLabel.equals(controlFrame.getRightDragLabel())) {
            x = frameWidth - tooltipMenuWidth - CyderFrame.BORDER_LEN;

            y = generatingPoint.getY();
            if (y < CyderDragLabel.DEFAULT_HEIGHT) {
                y = CyderDragLabel.DEFAULT_HEIGHT;
            } else if (y + tooltipMenuHeight + CyderFrame.BORDER_LEN > frameHeight) {
                y = frameHeight - tooltipMenuHeight - CyderFrame.BORDER_LEN;
            }
        } else if (generatingLabel.equals(controlFrame.getBottomDragLabel())) {
            x = generatingPoint.getX();

            if (x < CyderFrame.BORDER_LEN) {
                x = CyderFrame.BORDER_LEN;
            } else if (x + tooltipMenuWidth + CyderFrame.BORDER_LEN > frameWidth) {
                x = frameWidth - tooltipMenuWidth - CyderFrame.BORDER_LEN;
            }

            y = frameHeight - CyderFrame.BORDER_LEN - tooltipMenuHeight;
        } else {
            throw new FatalException("Generating drag label is not one of the border labels: " + generatingLabel);
        }

        return new Point((int) x, (int) y);
    }

    /**
     * The actions to invoke when the frame location tooltip menu item is pressed.
     */
    private void onFrameLocationTooltipMenuItemPressed() {
        CyderThreadRunner.submit(() -> {
            tooltipMenuItemFrameLocationGetterUtil.closeAllGetFrames();

            GetInputBuilder builder = new GetInputBuilder("Frame location",
                    "Enter the requested top left frame location in the format: "
                            + quote + "x,y" + quote + HtmlTags.breakTag
                            + "Note this is absolute meaning if multiple monitors are being used,"
                            + " they should be treated as a coalesced singular entity")
                    .setRelativeTo(controlFrame)
                    .setLabelFont(CyderFonts.DEFAULT_FONT_SMALL)
                    .setInitialFieldText(controlFrame.getX() + comma + controlFrame.getY());

            Optional<String> optionalLocation = tooltipMenuItemFrameLocationGetterUtil.getInput(builder);
            if (optionalLocation.isEmpty()) return;

            String location = optionalLocation.get();
            if (!location.contains(comma)) {
                controlFrame.notify("Could not parse location" + " from input: " + quote + location + quote);
                return;
            }

            String[] parts = location.split(comma);
            if (parts.length != 2) {
                controlFrame.notify("Could not parse x and y" + " from input: " + quote + location + quote);
                return;
            }

            String xString = parts[0].trim();
            String yString = parts[1].trim();

            int requestedX;
            try {
                requestedX = Integer.parseInt(xString);
            } catch (NumberFormatException e) {
                controlFrame.notify("Could not parse x from: " + quote + xString + quote);
                return;
            }

            int requestedY;
            try {
                requestedY = Integer.parseInt(yString);
            } catch (NumberFormatException e) {
                controlFrame.notify("Could not parse x from: " + quote + yString + quote);
                return;
            }

            Rectangle absoluteMonitorBounds = UiUtil.getMergedMonitors();

            if (requestedX < absoluteMonitorBounds.getX()) {
                controlFrame.notify("Requested x " + quote + requestedX + quote
                        + " is less than the absolute minimum: " + quote + absoluteMonitorBounds.getX() + quote);
                return;
            } else if (requestedY < absoluteMonitorBounds.getY()) {
                controlFrame.notify("Requested y " + quote + requestedY + quote
                        + " is less than the absolute minimum: " + quote + absoluteMonitorBounds.getY() + quote);
                return;
            } else if (requestedX > absoluteMonitorBounds.getX()
                    + absoluteMonitorBounds.getWidth() - controlFrame.getWidth()) {
                controlFrame.notify("Requested x " + quote + requestedX + quote
                        + " is greater than the absolute maximum: " + quote + (absoluteMonitorBounds.getX()
                        + absoluteMonitorBounds.getWidth() - controlFrame.getWidth()) + quote);
                return;
            } else if (requestedY > absoluteMonitorBounds.getY()
                    + absoluteMonitorBounds.getHeight() - controlFrame.getHeight()) {
                controlFrame.notify("Requested y " + quote + requestedY + quote
                        + " is greater than the absolute maximum: " + quote + (absoluteMonitorBounds.getY()
                        + absoluteMonitorBounds.getHeight() - controlFrame.getHeight()) + quote);
                return;
            }

            if (requestedX == controlFrame.getX() && requestedY == controlFrame.getY()) return;
            UiUtil.requestFramePosition(requestedX, requestedY, controlFrame);
            controlFrame.notify("Set frame location to request: "
                    + quote + requestedX + comma + requestedY + quote);
        }, setFrameLocationTooltipMenuWaiterThreadName);
    }

    /**
     * The actions to invoke when the frame size tooltip menu item is pressed.
     */
    private void onFrameSizeTooltipMenuItemPressed() {
        CyderThreadRunner.submit(() -> {
            int w = controlFrame.getWidth();
            int h = controlFrame.getHeight();

            tooltipMenuItemFrameSizeGetterUtil.closeAllGetFrames();

            GetInputBuilder builder = new GetInputBuilder("Frame size",
                    "Enter the requested frame size in the format: "
                            + quote + "width" + comma + "height" + quote)
                    .setRelativeTo(controlFrame)
                    .setLabelFont(CyderFonts.DEFAULT_FONT_SMALL)
                    .setInitialFieldText(w + comma + h);

            Optional<String> optionalWidthHeight = tooltipMenuItemFrameSizeGetterUtil.getInput(builder);
            if (optionalWidthHeight.isEmpty()) return;

            String widthHeight = optionalWidthHeight.get();
            if (!widthHeight.contains(comma)) {
                controlFrame.notify("Could not parse width and height"
                        + " from input: " + quote + widthHeight + quote);
                return;
            }

            String[] parts = widthHeight.split(comma);
            if (parts.length != 2) {
                controlFrame.notify("Could not parse width and height"
                        + " from input: " + quote + widthHeight + quote);
                return;
            }

            String widthString = parts[0].trim();
            String heightString = parts[1].trim();

            int requestedWidth;
            try {
                requestedWidth = Integer.parseInt(widthString);
            } catch (NumberFormatException e) {
                controlFrame.notify("Could not parse width from: " + quote + widthString + quote);
                return;
            }

            int requestedHeight;
            try {
                requestedHeight = Integer.parseInt(heightString);
            } catch (NumberFormatException e) {
                controlFrame.notify("Could not parse width from: " + quote + heightString + quote);
                return;
            }

            Dimension minimumFrameSize = controlFrame.getMinimumSize();
            Dimension maximumFrameSize = controlFrame.getMaximumSize();

            if (requestedWidth < minimumFrameSize.getWidth()) {
                controlFrame.notify("Requested width " + quote + requestedWidth + quote
                        + " is less than the minimum allowable width: "
                        + quote + minimumFrameSize.getWidth() + quote);
                return;
            } else if (requestedHeight < minimumFrameSize.getHeight()) {
                controlFrame.notify("Requested height " + quote + requestedHeight + quote
                        + " is less than the minimum allowable height: "
                        + quote + minimumFrameSize.getHeight() + quote);
                return;
            } else if (requestedWidth > maximumFrameSize.getWidth()) {
                controlFrame.notify("Requested width " + quote + requestedWidth + quote
                        + " is greater than the maximum allowable width: " + quote
                        + maximumFrameSize.getWidth() + quote);
                return;
            } else if (requestedHeight > maximumFrameSize.getHeight()) {
                controlFrame.notify("Requested height " + quote + requestedHeight + quote
                        + " is greater than the maximum allowable height: "
                        + quote + maximumFrameSize.getHeight() + quote);
                return;
            }

            if (requestedWidth == w && requestedHeight == h) return;

            Point center = controlFrame.getCenterPointOnScreen();
            controlFrame.setSize(requestedWidth, requestedHeight);
            controlFrame.setCenterPoint(center);
            controlFrame.refreshBackground();
            controlFrame.notify("Set frame size to request: "
                    + quote + requestedWidth + comma + requestedHeight + quote);
        }, setFrameSizeTooltipMenuWaiterThreadName);
    }
}
