package cyder.handlers.external.audio;

import com.google.common.base.Preconditions;
import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.CyderThreadRunner;
import cyder.utils.StringUtil;
import cyder.utils.TimeUtil;

import javax.swing.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Private inner class for the scrolling audio label.
 */
public class ScrollingTitleLabel {
    /**
     * The minimum width of the title label.
     */
    public static final int MIN_WIDTH = 100;

    /**
     * Whether this scrolling title label object has been killed.
     */
    private final AtomicBoolean killed = new AtomicBoolean();

    /**
     * The timeout to sleep for before checking for title scroll label being terminated.
     */
    private static final int SLEEP_WITH_CHECKS_TIMEOUT = 50;

    /**
     * The timeout between moving the label from one side to the opposite side.
     */
    private static final int SIDE_TO_SIDE_TIMEOUT = 5000;

    /**
     * The timeout between starting the initial timeout.
     */
    private static final int INITIAL_TIMEOUT = 3000;

    /**
     * The timeout between movement increments of the title label.
     */
    private static final int MOVEMENT_TIMEOUT = 25;

    /**
     * The label this scrolling label is controlling.
     */
    private final JLabel effectLabel;

    /**
     * Constructs and begins the scrolling title label animation using the
     * provided label, its parent, and the provided text as the title.
     *
     * @param effectLabel the label to move in its parent container.
     * @param localTitle  the title of the label
     */
    public ScrollingTitleLabel(JLabel effectLabel, String localTitle) {
        Preconditions.checkNotNull(effectLabel);
        Preconditions.checkNotNull(localTitle);

        this.effectLabel = effectLabel;

        effectLabel.setText(localTitle);

        setupScrolling(localTitle);
    }

    /**
     * Starts the scrolling animation if necessary.
     * Otherwise, the label is centered in the parent container.
     *
     * @param localTitle the title to display
     */
    private void setupScrolling(String localTitle) {
        try {
            int parentWidth = effectLabel.getParent().getWidth();
            int parentHeight = effectLabel.getParent().getHeight();

            int textWidth = StringUtil.getMinWidth(localTitle, effectLabel.getFont());
            int textHeight = StringUtil.getMinHeight(localTitle, effectLabel.getFont());

            effectLabel.setSize(Math.max(textWidth, MIN_WIDTH), parentHeight);

            if (textWidth > parentWidth) {
                effectLabel.setLocation(0, 0);

                startScrollingThread(textWidth, parentWidth, localTitle);
            } else {
                effectLabel.setLocation(
                        parentWidth / 2 - Math.max(textWidth, MIN_WIDTH) / 2,
                        parentHeight / 2 - textHeight / 2);
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Returns the text the label being controlled contains.
     *
     * @return the text the label being controlled contains
     */
    public String localTitle() {
        return effectLabel.getText();
    }

    /**
     * Kills the current scrolling title label.
     */
    public void kill() {
        killed.set(true);
    }

    /**
     * Starts the thread to animate the scrolling.
     *
     * @param textWidth   the full width of text displayed in the label
     * @param parentWidth the width (should be less than textWidth) of the parent container
     * @param labelText   the text on the label
     */
    private void startScrollingThread(int textWidth, int parentWidth, String labelText) {
        CyderThreadRunner.submit(() -> {
            try {
                TimeUtil.sleepWithChecks(INITIAL_TIMEOUT, SLEEP_WITH_CHECKS_TIMEOUT, killed);

                while (!killed.get()) {
                    int translatedDistance = 0;

                    while (translatedDistance < textWidth - parentWidth) {
                        if (killed.get()) {
                            break;
                        }

                        effectLabel.setLocation(effectLabel.getX() - 1, effectLabel.getY());
                        Thread.sleep(MOVEMENT_TIMEOUT);
                        translatedDistance++;
                    }

                    TimeUtil.sleepWithChecks(SIDE_TO_SIDE_TIMEOUT, SLEEP_WITH_CHECKS_TIMEOUT, killed);

                    while (translatedDistance > 0) {
                        if (killed.get()) {
                            break;
                        }

                        effectLabel.setLocation(effectLabel.getX() + 1, effectLabel.getY());
                        Thread.sleep(MOVEMENT_TIMEOUT);
                        translatedDistance--;
                    }

                    TimeUtil.sleepWithChecks(SIDE_TO_SIDE_TIMEOUT, SLEEP_WITH_CHECKS_TIMEOUT, killed);
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, "Scrolling title label animator [" + labelText + "]");
    }
}
