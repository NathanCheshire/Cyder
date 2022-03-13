package cyder.user.objects;

import cyder.enums.LoggerTag;
import cyder.handlers.internal.Logger;
import cyder.utilities.ReflectionUtil;

/**
 * A class to store statistics about the ConsoleFrame and where it is.
 */
public class ScreenStat {
    private int consoleX;
    private int consoleY;
    private int consoleWidth;
    private int consoleHeight;
    private int monitor;
    private boolean consoleOnTop;

    public ScreenStat(int consoleX, int consoleY, int consoleWidth,
                      int consoleHeight, int monitor, boolean consoleOnTop) {
        this.consoleX = consoleX;
        this.consoleY = consoleY;
        this.consoleWidth = consoleWidth;
        this.consoleHeight = consoleHeight;
        this.monitor = monitor;
        this.consoleOnTop = consoleOnTop;

        Logger.log(LoggerTag.OBJECT_CREATION, this);
    }

    public int getConsoleX() {
        return consoleX;
    }

    public void setConsoleX(int consoleX) {
        this.consoleX = consoleX;
    }

    public int getConsoleY() {
        return consoleY;
    }

    public void setConsoleY(int consoleY) {
        this.consoleY = consoleY;
    }

    public int getConsoleWidth() {
        return consoleWidth;
    }

    public void setConsoleWidth(int consoleWidth) {
        this.consoleWidth = consoleWidth;
    }

    public int getConsoleHeight() {
        return consoleHeight;
    }

    public void setConsoleHeight(int consoleHeight) {
        this.consoleHeight = consoleHeight;
    }

    public int getMonitor() {
        return monitor;
    }

    public void setMonitor(int monitor) {
        this.monitor = monitor;
    }

    public boolean isConsoleOnTop() {
        return consoleOnTop;
    }

    public void setConsoleOnTop(boolean consoleOnTop) {
        this.consoleOnTop = consoleOnTop;
    }

    @Override
    public String toString() {
        return ReflectionUtil.commonCyderToString(this);
    }
}
