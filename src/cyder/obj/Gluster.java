package cyder.obj;

import cyder.enums.Direction;

public class Gluster {
    private String htmlText;
    private int duration;
    private Direction arrowDir;
    private Direction startDir;
    private Direction vanishDir;

    /**
     * A gluster is a notification that hasn't been notified to the user yet and is waiting in a CyderFrame's queue.
     * @param text - the html text for the eventual notification to display
     * @param dur - the duration in miliseconds the notification should last for. Use 0 for auto-calculation
     * @param arrow - the arrow direction
     * @param start - the start direction
     * @param vanish - the vanish direction
     */
    public Gluster(String text, int dur, Direction arrow, Direction start, Direction vanish) {
        this.htmlText = text;
        this.duration = dur;
        this.arrowDir = arrow;
        this.startDir = start;
        this.vanishDir = vanish;
    }

    public void setHtmlText(String htmlText) {
        this.htmlText = htmlText;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setArrowDir(Direction arrowDir) {
        this.arrowDir = arrowDir;
    }

    public void setStartDir(Direction startDir) {
        this.startDir = startDir;
    }

    public void setVanishDir(Direction vanishDir) {
        this.vanishDir = vanishDir;
    }

    public String getHtmlText() {
        return htmlText;
    }

    public int getDuration() {
        return duration;
    }

    public Direction getArrowDir() {
        return arrowDir;
    }

    public Direction getStartDir() {
        return startDir;
    }

    public Direction getVanishDir() {
        return vanishDir;
    }
}