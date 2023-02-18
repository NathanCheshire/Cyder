package cyder.widgets;

import cyder.annotations.CyderAuthor;
import cyder.annotations.SuppressCyderInspections;
import cyder.annotations.Vanilla;
import cyder.annotations.Widget;
import cyder.enumerations.CyderInspection;
import cyder.layouts.CyderGridLayout;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.ui.button.CyderButton;
import cyder.ui.frame.CyderFrame;
import cyder.user.UserDataManager;

/**
 * An example widget for new Cyder developers to learn the standard widget construction.
 */
@Vanilla
@CyderAuthor
public final class ExampleWidget {
    /**
     * Suppress default constructor from access outside of this class and log
     * when objects are created by the static factory method.
     */
    private ExampleWidget() {
        Logger.log(LogTag.OBJECT_CREATION, this);
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

    /**
     * Shows the widget.
     */
    private void innerShowGui() {
        CyderFrame cyderFrame = new CyderFrame(600, 600);
        cyderFrame.setTitle("Example Widget");

        CyderButton cyderButton = new CyderButton("Button");
        cyderButton.setSize(200, 40);
        cyderButton.addActionListener(e -> {
            // Your logic here or a method reference to a local method (See EJ items 42 and 43)
            cyderFrame.notify("Hello " + UserDataManager.INSTANCE.getUsername() + "!");
        });

        CyderGridLayout gridLayout = new CyderGridLayout(1, 1);
        gridLayout.addComponent(cyderButton);

        cyderFrame.setCyderLayout(gridLayout);

        cyderFrame.finalizeAndShow();
    }
}
