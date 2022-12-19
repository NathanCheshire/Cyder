package animation;

import main.java.cyder.animation.AnimationUtil;
import main.java.cyder.enums.Direction;
import main.java.cyder.threads.ThreadUtil;
import main.java.cyder.ui.frame.CyderFrame;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the {@link main.java.cyder.animation.AnimationUtil}s.
 */
public class AnimationUtilTest {
    /**
     * Default constructor for JUnit.
     */
    public AnimationUtilTest() {}

    /**
     * Tests for the close animation method.
     */
    @Test
    void testCloseAnimation() {
        Frame frame = new Frame();
        assertDoesNotThrow(() -> AnimationUtil.closeAnimation(frame));

        JFrame jframe = new JFrame();
        assertDoesNotThrow(() -> AnimationUtil.closeAnimation(jframe));

        CyderFrame cyderFrame = new CyderFrame();
        assertDoesNotThrow(() -> AnimationUtil.closeAnimation(cyderFrame));
    }

    /**
     * Tests for the minimize animation method.
     */
    @Test
    void testMinimizeAnimation() {
        JFrame jframe = new JFrame();
        jframe.setVisible(true);
        assertDoesNotThrow(() -> AnimationUtil.minimizeAnimation(jframe));
        assertEquals(JFrame.ICONIFIED, jframe.getState());

        CyderFrame cyderFrame = new CyderFrame();
        cyderFrame.setVisible(true);
        assertDoesNotThrow(() -> AnimationUtil.minimizeAnimation(cyderFrame));
        assertEquals(JFrame.ICONIFIED, jframe.getState());
    }

    /**
     * Tests for animating a component moving in a cardinal direction.
     */
    @Test
    void testAnimateComponentMovement() {
        assertThrows(NullPointerException.class, () -> AnimationUtil.animateComponentMovement(null,
                0, 0, 0, 0, new CyderFrame()));
        assertThrows(NullPointerException.class, () -> AnimationUtil.animateComponentMovement(Direction.LEFT,
                0, 0, 0, 0, null));
        assertThrows(IllegalArgumentException.class, () -> AnimationUtil.animateComponentMovement(Direction.LEFT,
                0, 0, 0, 0, new CyderFrame()));

        int increment = 2;
        int delay = 2;

        CyderFrame upFrame = new CyderFrame();
        upFrame.setLocation(0, 100);
        assertDoesNotThrow(() -> AnimationUtil.animateComponentMovement(Direction.TOP,
                100, 0, delay, increment, upFrame));
        ThreadUtil.sleep(delay * 100);
        assertEquals(0, upFrame.getY());

        CyderFrame downFrame = new CyderFrame();
        downFrame.setLocation(0, 0);
        assertDoesNotThrow(() -> AnimationUtil.animateComponentMovement(Direction.BOTTOM,
                0, 100, delay, increment, downFrame));
        ThreadUtil.sleep(delay * 100);
        assertEquals(100, downFrame.getY());

        CyderFrame leftFrame = new CyderFrame();
        leftFrame.setLocation(100, 0);
        assertDoesNotThrow(() -> AnimationUtil.animateComponentMovement(Direction.LEFT,
                100, 0, delay, increment, leftFrame));
        ThreadUtil.sleep(delay * 100);
        assertEquals(0, leftFrame.getX());

        CyderFrame rightFrame = new CyderFrame();
        rightFrame.setLocation(0, 0);
        assertDoesNotThrow(() -> AnimationUtil.animateComponentMovement(Direction.RIGHT,
                0, 100, delay, increment, rightFrame));
        ThreadUtil.sleep(delay * 100);
        assertEquals(100, rightFrame.getX());
    }
}
