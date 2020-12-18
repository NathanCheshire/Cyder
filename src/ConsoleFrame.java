import com.cyder.enums.Direction;
import com.cyder.ui.CyderFrame;

import javax.swing.*;
import java.io.File;

public class ConsoleFrame extends CyderFrame {

    //current user vars
    private static String UUID;
    private static String username;
    private static String userColor;
    private static String userFont;

    //background vars

    //command list vars


    public ConsoleFrame(int width, int height) {
        super(width, height);

    }

    public static void setUUID() {
        //this will setuuid and set username also
    }

    public static String getUUID() {
        return UUID;
    }

    public static String getUsername() {
        return username;
    }

    public static void resizeBackgrounds() {

    }

    public static void initBackgrounds() {

    }

    public static File[] getBackgrounds() {
        return null;
    }

    public static File getCurrentBackgroundFile() {
        return null;
    }

    public static ImageIcon getCurrentBackgroundImageIcon() {
        return null;
    }

    private static Direction lastSlideDirection = Direction.LEFT;

    public static void switchBackground() {
        switch (lastSlideDirection) {
            case LEFT:

                break;

            case RIGHT:

                break;
        }
    }

    public static int getBackgroundWidth() {
        return 0;
    }

    public static int getBackgroundHeight() {
        return 0;
    }

    public static void enableClock() {

    }

    public static void disableClock() {

    }

    public static void rotateConsole() {

    }

    public static void toggleFullScreen() {

    }

    public static void barrelRoll() {

    }
}
