package cyder.handlers.internal;

import cyder.consts.CyderImages;
import cyder.genesis.GenesisShare;
import cyder.ui.CyderFrame;
import cyder.ui.CyderLabel;
import cyder.utilities.BoundsUtil;

import java.awt.*;

public class PopupHandler {
    public static void inform() {
        inform("Empty text", "Empty title");
    }

    public static void inform(String text, String title) {
        inform(text, title, GenesisShare.getDominantFrame());
    }

    public static void inform(String text, String title, Component relativeTo) {
       inform(text, title, relativeTo, null);
    }

    public static void inform(String text, String title, Component relativeTo, CyderFrame.PostCloseAction postCloseAction) {
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

            if (postCloseAction != null)
                informFrame.addPostCloseAction(postCloseAction);

            informFrame.setVisible(true);
            informFrame.setLocationRelativeTo(relativeTo);
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }
}
