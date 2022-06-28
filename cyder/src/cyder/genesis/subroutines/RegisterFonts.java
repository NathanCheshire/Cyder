package cyder.genesis.subroutines;

import cyder.enums.ExitCondition;
import cyder.exceptions.FatalException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.Logger;
import cyder.utils.FileUtil;
import cyder.utils.OSUtil;

import java.awt.*;
import java.io.File;

/**
 * A startup subroutine to ensure all the fonts from the fonts directory are loaded.
 */
public class RegisterFonts implements StartupSubroutine {
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean ensure() {
        File[] fontFiles = OSUtil.buildFile("static", "fonts").listFiles();

        if (fontFiles == null || fontFiles.length == 0) {
            return false;
        }

        for (File fontFile : fontFiles) {
            if (FileUtil.isSupportedFontExtension(fontFile)) {
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                try {
                    if (!ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, fontFile))) {
                        return false;
                    }

                    Logger.log(Logger.Tag.FONT_LOADED, FileUtil.getFilename(fontFile));
                } catch (Exception e) {
                    ExceptionHandler.silentHandle(e);
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SubroutinePriority getPriority() {
        return SubroutinePriority.NECESSARY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getErrorMessage() {
        return "Font required by system could not be loaded";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exit() {
        Logger.log(Logger.Tag.EXCEPTION, "Font Exception");
        ExceptionHandler.exceptionExit(getErrorMessage(), "Font failure", ExitCondition.CorruptedSystemFiles);
        throw new FatalException(getErrorMessage());
    }
}
