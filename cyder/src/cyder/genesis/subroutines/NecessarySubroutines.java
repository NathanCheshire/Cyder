package cyder.genesis.subroutines;

import cyder.constants.CyderStrings;
import cyder.enums.IgnoreThread;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.genesis.CyderSplash;
import cyder.handlers.internal.ExceptionHandler;
import cyder.logging.Logger;
import cyder.threads.CyderThreadRunner;
import cyder.threads.ThreadUtil;
import cyder.user.UserUtil;
import cyder.utils.FileUtil;
import cyder.utils.OsUtil;
import cyder.utils.ReflectionUtil;
import cyder.utils.StaticUtil;

import java.awt.*;
import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

@SuppressWarnings("BooleanMethodIsAlwaysInverted") /* readability */
public final class NecessarySubroutines {
    /**
     * The socket used to ensure only one instance of Cyder ever exists.
     */
    private static Socket serverSocket;

    /**
     * The timeout to wait for the server socket to connect/fail.
     */
    private static final long SINGLE_INSTANCE_ENSURER_TIMEOUT = 500;

    /**
     * The port to ensure one instance of Cyder is ever active.
     */
    private static final int INSTANCE_SOCKET_PORT = 5150;

    /**
     * Returns the server socket used to ensure only one instance of Cyder exists.
     *
     * @return the server socket used to ensure only one instance of Cyder exists
     */
    public static Socket getServerSocket() {
        return serverSocket;
    }

    /**
     * Suppress default constructor.
     */
    private NecessarySubroutines() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Executes the necessary subroutines on the main thread.
     *
     * @throws FatalException if any necessary subroutines fail
     */
    public static void execute() {
        CyderSplash.INSTANCE.setLoadingMessage("Registering fonts");
        if (!registerFonts()) {
            throw new FatalException("Registering fonts failed");
        }

        CyderSplash.INSTANCE.setLoadingMessage("Ensuring singular instance");
        if (!ensureSingularInstance()) {
            throw new FatalException("Multiple instances detected");
        }

        CyderSplash.INSTANCE.setLoadingMessage("Ensuring OS is supported");
        if (!ensureSupportedOs()) {
            throw new FatalException("Unsupported OS");
        }

        CyderSplash.INSTANCE.setLoadingMessage("Creating dynamics");
        OsUtil.ensureDynamicsCreated();

        CyderSplash.INSTANCE.setLoadingMessage("Validating users");
        UserUtil.validateUsers();

        CyderSplash.INSTANCE.setLoadingMessage("Cleaning users");
        UserUtil.cleanUsers();

        CyderSplash.INSTANCE.setLoadingMessage("Validating props");
        ReflectionUtil.ensureNoDuplicateProps();

        CyderSplash.INSTANCE.setLoadingMessage("Validating Widgets");
        ReflectionUtil.validateWidgets();

        CyderSplash.INSTANCE.setLoadingMessage("Validating Tests");
        ReflectionUtil.validateTests();

        CyderSplash.INSTANCE.setLoadingMessage("Validating Vanilla annotations");
        ReflectionUtil.validateVanillaWidgets();

        CyderSplash.INSTANCE.setLoadingMessage("Validating Handles");
        ReflectionUtil.validateHandles();
    }

    /**
     * Registers the fonts within static/fonts.
     *
     * @return whether the fonts could be loaded
     */
    private static boolean registerFonts() {
        File[] fontFiles = StaticUtil.getStaticDirectory("fonts").listFiles();

        if (fontFiles == null || fontFiles.length == 0) {
            return false;
        }

        for (File fontFile : fontFiles) {
            if (FileUtil.isSupportedFontExtension(fontFile)) {
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                try {
                    ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, fontFile));
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
     * Returns whether this instance of Cyder is the only instance on the host OS.
     *
     * @return whether this instance of Cyder is the only instance on the host OS
     */
    private static boolean ensureSingularInstance() {
        AtomicBoolean singularInstance = new AtomicBoolean(true);

        CyderThreadRunner.submit(() -> {
            try {
                Logger.log(Logger.Tag.DEBUG, "Starting instance socket on port " + INSTANCE_SOCKET_PORT);
                serverSocket = new ServerSocket(INSTANCE_SOCKET_PORT).accept();
            } catch (Exception ignored) {
                singularInstance.set(false);
                Logger.log(Logger.Tag.DEBUG, "Failed to start singular instance socket");
            }
        }, IgnoreThread.SingularInstanceEnsurer.getName());

        // started blocking method in above thread but need to wait for it to either bind or fail
        ThreadUtil.sleep(SINGLE_INSTANCE_ENSURER_TIMEOUT);

        return singularInstance.get();
    }

    /**
     * Returns whether the host operating system is supported by Cyder.
     *
     * @return whether the host operating system is supported by Cyder
     */
    private static boolean ensureSupportedOs() {
        return !OsUtil.isOsx();
    }
}
