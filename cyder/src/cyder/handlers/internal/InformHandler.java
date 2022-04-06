package cyder.handlers.internal;

import cyder.constants.CyderColors;
import cyder.constants.CyderStrings;
import cyder.enums.LoggerTag;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.objects.InformBuilder;
import cyder.ui.CyderDragLabel;
import cyder.ui.CyderFrame;
import cyder.ui.CyderLabel;
import cyder.utilities.BoundsUtil;
import cyder.utilities.UserUtil;
import cyder.utilities.objects.BoundsString;

import javax.swing.*;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Information frames throughout Cyder.
 */
public class InformHandler {
    /**
     * Prevent illegal instantiation.
     */
    private InformHandler() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    /**
     * A quick information pane.
     *
     * @param text the possibly html styled text to display.
     */
    public static void inform(String text) {
        InformBuilder builder = new InformBuilder(text);
        builder.setTitle(InformBuilder.DEFAULT_TITLE);
        inform(builder);
    }

    /**
     * The width padding on each side of the information pane.
     */
    private static final int xPadding = 10;

    /**
     * The offset to translate the text label on the information pane down by.
     */
    private static final int yOffset = CyderDragLabel.DEFAULT_HEIGHT;

    /**
     * THe height padding on each side of the information pane.
     */
    private static final int yPadding = 10;

    /**
     * The offset to account for a discrepancy in the bounds calculation.
     */
    private static final int borderOffset = 2;

    /**
     * Opens an information using the information provided by builder.
     *
     * @param builder the InformBuilder to use for the construction of the information pane
     * @throws IllegalArgumentException if the provided builder is null
     */
    public static void inform(InformBuilder builder) {
        checkNotNull(builder);

        boolean darkMode = UserUtil.getCyderUser().getDarkmode().equals("1");

        try {
            int containerWidth = 0;
            int containerHeight = 0;

            if (builder.getContainer() == null) {
                CyderLabel textLabel = new CyderLabel(builder.getHtmlText());
                textLabel.setOpaque(false);
                textLabel.setForeground(darkMode
                        ? CyderColors.defaultDarkModeTextColor : CyderColors.defaultLightModeTextColor);

                BoundsString boundsString = BoundsUtil.widthHeightCalculation(builder.getHtmlText());

                containerWidth = boundsString.getWidth();
                containerHeight = boundsString.getHeight();

                textLabel.setText(BoundsUtil.addCenteringToHTML(boundsString.getText()));

                builder.setContainer(textLabel);
            } else {
                containerWidth = builder.getContainer().getWidth();
                containerHeight = builder.getContainer().getHeight();
            }

            // 10 -> label -> 10
            int frameW = containerWidth + xPadding * 2;

            // DragLabel -> 10 -> label -> 10
            int frameH = containerHeight + yOffset + 2 * yPadding;

            CyderFrame informFrame = new CyderFrame(frameW, frameH,
                    (darkMode ? CyderColors.darkModeBackgroundColor : CyderColors.regularBackgroundColor));
            informFrame.setFrameType(CyderFrame.FrameType.POPUP);
            informFrame.setTitle(builder.getTitle());

            int x = informFrame.getWidth() / 2 - containerWidth / 2;
            int y = informFrame.getHeight() / 2 - containerHeight / 2 + 5;

            JLabel container = builder.getContainer();
            container.setBounds(x, y, containerWidth, containerHeight);
            informFrame.add(container);

            if (builder.getPreCloseAction() != null)
                informFrame.addPreCloseAction(builder.getPreCloseAction());

            if (builder.getPostCloseAction() != null)
                informFrame.addPostCloseAction(builder.getPostCloseAction());

            informFrame.setVisible(true);
            informFrame.setAlwaysOnTop(true);
            informFrame.setLocationRelativeTo(builder.getRelativeTo());

            // log inform call
            Logger.log(LoggerTag.UI_ACTION, "[INFORMATION PANE] text = \""
                    + builder.getHtmlText() + "\", relativeTo = " + builder.getRelativeTo());
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }
}
