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

            String setText = BoundsUtil.addCenteringToHTML(boundsString.getText());
            System.out.println(setText);
            textLabel.setText(setText);

            // todo move out of scope
            int xPadding = 10;
            int yOffset = CyderDragLabel.DEFAULT_HEIGHT;
            int yPadding = 10;
            int borderOffset = 2;

            // 10 - label - 10
            int frameW = boundsString.getWidth() + xPadding * 2;

            // dl - 10 - label - 10
            int frameH = boundsString.getHeight() + yOffset + 2 * yPadding;

            textLabel.setBounds(xPadding - borderOffset, yPadding + yOffset,
                    boundsString.getWidth() - borderOffset, boundsString.getHeight());

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
