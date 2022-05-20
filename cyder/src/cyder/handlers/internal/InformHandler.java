package cyder.handlers.internal;

import com.google.common.base.Preconditions;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import cyder.constants.CyderColors;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.ui.CyderDragLabel;
import cyder.ui.CyderFrame;
import cyder.ui.CyderLabel;
import cyder.utilities.BoundsUtil;
import cyder.utilities.UserUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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
     * @return a reference to the shown inform frame
     * @throws IllegalArgumentException if the provided text is null
     */
    @CanIgnoreReturnValue /* calls don't always need the reference */
    public static CyderFrame inform(String text) {
        Preconditions.checkNotNull(text);

        InformBuilder builder = new InformBuilder(text);
        builder.setTitle(InformBuilder.DEFAULT_TITLE);
        return inform(builder);
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
     * @return a reference to the shown inform frame
     * @throws IllegalArgumentException if the provided builder is null
     */
    @CanIgnoreReturnValue /* calls don't always need the reference  */
    public static CyderFrame inform(InformBuilder builder) {
        Preconditions.checkNotNull(builder);

        CyderFrame informFrame = null;

        // custom container
        if (builder.getContainer() != null) {
            int containerWidth = builder.getContainer().getWidth();
            int containerHeight = builder.getContainer().getHeight();

            int xPadding = 1;
            int yTopPadding = CyderDragLabel.DEFAULT_HEIGHT - 4;
            int yBottomPadding = 2;

            informFrame = new CyderFrame(containerWidth + 2 * xPadding,
                    containerHeight + yTopPadding + yBottomPadding);
            informFrame.setFrameType(CyderFrame.FrameType.POPUP);
            informFrame.setTitle(builder.getTitle());

            JLabel container = builder.getContainer();
            container.setBounds(xPadding, yTopPadding,
                    containerWidth, containerHeight);
            informFrame.getContentPane().add(container);

            if (builder.getPreCloseAction() != null) {
                informFrame.addPreCloseAction(builder.getPreCloseAction());
            }

            if (builder.getPostCloseAction() != null) {
                informFrame.addPostCloseAction(builder.getPostCloseAction());
            }
        }
        // intended to genreate a text inform pane
        else {
            boolean darkMode = UserUtil.getCyderUser().getDarkmode().equals("1");

            CyderLabel textLabel = new CyderLabel(builder.getHtmlText());
            textLabel.setOpaque(false);
            textLabel.setForeground(darkMode
                    ? CyderColors.defaultDarkModeTextColor : CyderColors.defaultLightModeTextColor);

            BoundsUtil.BoundsString boundsString = BoundsUtil.widthHeightCalculation(builder.getHtmlText());

            int containerWidth = boundsString.getWidth();
            int containerHeight = boundsString.getHeight();

            textLabel.setText(BoundsUtil.addCenteringToHTML(boundsString.getText()));

            builder.setContainer(textLabel);

            informFrame = new CyderFrame(containerWidth + xPadding * 2,
                    containerHeight + yOffset + 2 * yPadding,
                    (darkMode ? CyderColors.darkModeBackgroundColor : CyderColors.regularBackgroundColor));
            informFrame.setFrameType(CyderFrame.FrameType.POPUP);
            informFrame.setTitle(builder.getTitle());

            int containerX = informFrame.getWidth() / 2 - containerWidth / 2;
            int containerY = informFrame.getHeight() / 2 - containerHeight / 2 + 5;

            JLabel container = builder.getContainer();
            container.setBounds(containerX, containerY, containerWidth, containerHeight);
            informFrame.add(container);

            if (builder.getPreCloseAction() != null) {
                informFrame.addPreCloseAction(builder.getPreCloseAction());
            }

            if (builder.getPostCloseAction() != null) {
                informFrame.addPostCloseAction(builder.getPostCloseAction());
            }
        }

        Component relativeTo = builder.getRelativeTo();

        // if intended to disable relative to
        if (relativeTo != null && builder.isDisableRelativeTo()) {
            relativeTo.setEnabled(false);
            informFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    relativeTo.setEnabled(true);
                }
            });
        }

        informFrame.setVisible(true);
        informFrame.setAlwaysOnTop(true);
        informFrame.setLocationRelativeTo(relativeTo);

        Logger.log(Logger.Tag.UI_ACTION, "[INFORMATION PANE] text = \""
                + builder.getHtmlText() + "\", relativeTo = " + builder.getRelativeTo());

        return informFrame;
    }
}
