package cyder.widgets;

import cyder.annotations.CyderAuthor;
import cyder.annotations.SuppressCyderInspections;
import cyder.annotations.Vanilla;
import cyder.annotations.Widget;
import cyder.common.CyderInspection;
import cyder.handlers.internal.Logger;
import cyder.ui.CyderButton;
import cyder.ui.CyderDragLabel;
import cyder.ui.CyderFrame;
import cyder.utilities.UserUtil;

/**
 * An example widget for new Cyder developers to learn the standard widget construction.
 */
@Vanilla
@CyderAuthor
@SuppressWarnings("unused")
public class ExampleWidget {
    /**
     * Suppress default constructor from access outside of this class and log
     * when objects are created by the static factory method.
     */
    private ExampleWidget() {
        Logger.log(Logger.Tag.OBJECT_CREATION, this);
    }

    /**
     * Returns an instance of ExampleWidget.
     *
     * @return an instance of ExampleWidget
     */
    public static ExampleWidget getInstance() {
        return new ExampleWidget();
    }

    @SuppressCyderInspections(CyderInspection.WidgetInspection)
    @Widget(triggers = "example widget", description = "An example base widget for new Cyder developers")
    public static void showGui() {
        getInstance().innerShowGui();
    }

    public void innerShowGui() {
        CyderFrame cyderFrame = new CyderFrame(600, 600);
        cyderFrame.setTitle("Example Widget");

        CyderButton cyderButton = new CyderButton("Button");
        cyderButton.setBounds((600 - 200) / 2,
                (600 - 40 + CyderDragLabel.DEFAULT_HEIGHT) / 2, 200, 40);
        cyderButton.addActionListener(e -> {
            // your logic here or a lambda to a class level private (possibly static) method
            cyderFrame.notify("Hello " + UserUtil.getCyderUser().getName() + "!");
        });
        cyderFrame.getContentPane().add(cyderButton);

        cyderFrame.finalizeAndShow();
    }
}
