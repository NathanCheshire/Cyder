package cyder.meta;

import com.google.common.collect.ImmutableList;
import cyder.exceptions.IllegalMethodException;
import cyder.logging.Logger;
import cyder.login.LoginHandler;
import cyder.props.PropLoader;
import cyder.session.InstanceSocketUtil;
import cyder.session.SessionManager;
import cyder.strings.CyderStrings;
import cyder.subroutines.NecessarySubroutines;
import cyder.subroutines.SufficientSubroutines;
import cyder.ui.UiUtil;
import cyder.utils.JvmUtil;
import cyder.utils.StaticUtil;
import cyder.watchdog.CyderWatchdog;

/**
 * The main Cyder entry point that performs checks, subroutines, validations, and
 * other operations to ensure Cyder can properly start on this operating system.
 */
public final class Cyder {
    /**
     * Suppress default constructor.
     */
    private Cyder() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * The main Cyder entry point.
     *
     * @param arguments the command line arguments passed in
     */
    public static void main(String[] arguments) {
        Logger.onInitialJvmEntry();
        SessionManager.INSTANCE.initializeSessionId();
        StaticUtil.loadStaticResources();
        JvmUtil.setAndParseJvmMainMethodArgs(ImmutableList.copyOf(arguments));
        PropLoader.reloadProps();
        JvmUtil.logMainMethodArgs(JvmUtil.getJvmMainMethodArgs());
        JvmUtil.addExitHooks();
        Logger.initialize();
        UiUtil.initializeUiAndSystemProps();
        CyderWatchdog.initializeWatchDog();
        NecessarySubroutines.executeSubroutines();
        InstanceSocketUtil.bindToInstanceSocket();
        CyderSplash.INSTANCE.showSplash();
        SufficientSubroutines.executeSubroutines();
        LoginHandler.showProperStartupFrame();
    }
}
