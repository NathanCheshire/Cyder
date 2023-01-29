package cyder.managers;

import java.awt.*;

/**
 * A manager for {@link java.awt.Robot}s.
 */
public enum RobotManager {
    /**
     * The robot manager instance.
     */
    INSTANCE;

    /**
     * The maximum number of times to attempt to create a robot before failing.
     */
    private static final int ROBOT_CREATION_ATTEMPTS = 10000;

    /**
     * The common shared robot.
     */
    private Robot commonSharedRobot;

    RobotManager() {
        attemptToCreateCommonSharedRobot();
    }

    /**
     * Returns the common shared robot.
     *
     * @return the common shared robot
     */
    public Robot getRobot() {
        if (commonSharedRobot == null) attemptToCreateCommonSharedRobot();

        return commonSharedRobot;
    }

    /**
     * Attempts to create the common shared robot.
     */
    private void attemptToCreateCommonSharedRobot() {
        int attempts = 0;
        while (attempts < ROBOT_CREATION_ATTEMPTS) {
            try {
                commonSharedRobot = new Robot();
                break;
            } catch (Exception e) {
                attempts++;
            }
        }
    }
}
