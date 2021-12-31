package cyder.handlers.internal;

import cyder.consts.CyderImages;
import cyder.consts.CyderStrings;
import cyder.genesis.GenesisShare;
import cyder.ui.CyderFrame;
import cyder.ui.CyderLabel;
import cyder.utilities.BoundsUtil;

import java.awt.*;

public class PopupHandler {
    private PopupHandler() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    public static void inform() {
        inform("Empty text", "Empty title");
    }

    public static void inform(String text, String title) {
        inform(text, title, GenesisShare.getDominantFrame());
    }

    public static void inform(String text, String title, Component relativeTo) {
       inform(text, title, relativeTo, null, null);
    }

    public static void inform(String text, String title, Component relativeTo, CyderFrame.PreCloseAction preCloseAction) {
        inform(text, title, relativeTo, preCloseAction, null);
    }

    public static void inform(String text, String title, Component relativeTo, CyderFrame.PostCloseAction postCloseAction) {
        inform(text, title, relativeTo, null, postCloseAction);
    }

    public static void inform(String text, String title, Component relativeTo,
                              CyderFrame.PreCloseAction preCloseAction, CyderFrame.PostCloseAction postCloseAction) {
        try {
            CyderLabel textLabel = new CyderLabel(text);
            BoundsUtil.BoundsString boundsString = BoundsUtil.widthHeightCalculation(text);
            textLabel.setText(BoundsUtil.addCenteringToHTML(boundsString.getText()));
            textLabel.setBounds(10,30, boundsString.getWidth(), boundsString.getHeight());

            CyderFrame informFrame = new CyderFrame(boundsString.getWidth() + 20,
                    boundsString.getHeight() + 40, CyderImages.defaultBackgroundLarge);
            informFrame.setFrameType(CyderFrame.FrameType.POPUP);
            informFrame.setTitle(title);
            informFrame.add(textLabel);

            if (preCloseAction != null)
                informFrame.addPreCloseAction(preCloseAction);

            if (postCloseAction != null)
                informFrame.addPostCloseAction(postCloseAction);

            informFrame.setVisible(true);
            informFrame.setLocationRelativeTo(relativeTo);
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }
}
