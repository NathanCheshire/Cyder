package cyder.handlers.internal;

import cyder.constants.CyderColors;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.objects.InformBuilder;
import cyder.ui.CyderDragLabel;
import cyder.ui.CyderFrame;
import cyder.ui.CyderLabel;
import cyder.utilities.BoundsUtil;
import cyder.utilities.objects.BoundsString;

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

    public static void inform(InformBuilder builder) {
        try {
            boolean darkMode = false;

            CyderLabel textLabel = new CyderLabel(builder.getHtmlText());
            textLabel.setOpaque(false);
            textLabel.setForeground((darkMode ? CyderColors.defaultDarkModeTextColor : CyderColors.defaultLightModeTextColor));
            BoundsString boundsString = BoundsUtil.widthHeightCalculation(builder.getHtmlText());
            textLabel.setText(BoundsUtil.addCenteringToHTML(boundsString.getText()));

            int xOffset = 10;
            int yOffset = CyderDragLabel.DEFAULT_HEIGHT;
            int yBottomPadding = 10;
            int widthOffsetFromBoundsCalc = 8;

            // if the width we would set is too small, center it
            if (boundsString.getWidth() + xOffset * 2 < CyderFrame.MINIMUM_WIDTH) {
                xOffset += (CyderFrame.MINIMUM_WIDTH - (boundsString.getWidth() + xOffset * 2)) / 2 - widthOffsetFromBoundsCalc;
            }

            // if the height we would set is too small for the CF's min height, center it vertically
            if (boundsString.getHeight() + yOffset + yBottomPadding < CyderFrame.MINIMUM_HEIGHT) {
                yOffset += (CyderFrame.MINIMUM_HEIGHT - (boundsString.getHeight() + yOffset + yBottomPadding)) / 2;
            }

            int frameW = boundsString.getWidth() + xOffset * 2;
            int frameH = boundsString.getHeight() + yOffset + yBottomPadding;

            System.out.println(frameW + "," + frameH);

            textLabel.setBounds(xOffset, yOffset, boundsString.getWidth(), boundsString.getHeight());

            CyderFrame informFrame = new CyderFrame(frameW, frameH,
                    (darkMode ? CyderColors.darkModeBackgroundColor : CyderColors.regularBackgroundColor));
            informFrame.setFrameType(CyderFrame.FrameType.POPUP);
            informFrame.setTitle(builder.getTitle());
            informFrame.add(textLabel);

            if (builder.getPreCloseAction() != null)
                informFrame.addPreCloseAction(builder.getPreCloseAction());

            if (builder.getPostCloseAction() != null)
                informFrame.addPostCloseAction(builder.getPostCloseAction());

            informFrame.setVisible(true);
            informFrame.setAlwaysOnTop(true);
            informFrame.setLocationRelativeTo(builder.getRelativeTo());
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }
}
