package cyder.handlers.input;

import cyder.annotations.Handle;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.user.Preference;
import cyder.user.Preferences;
import cyder.utilities.StringUtil;
import cyder.utilities.UserUtil;

/**
 * A handler for switching/toggling preferences.
 */
public class PreferenceHandler extends InputHandler {
    /**
     * Suppress default constructor.
     */
    private PreferenceHandler() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    @Handle()
    public static boolean handle() {
        String targetedPreference = getInputHandler().getCommand();
        String parsedArgs = getInputHandler().argsToString().replaceAll("\\s+", "");

        for (Preference pref : Preferences.getPreferences()) {
            if (targetedPreference.equalsIgnoreCase(pref.getID().trim())) {
                if (!pref.getDisplayName().equals("IGNORE")) {
                    boolean oldVal = UserUtil.getUserDataById(pref.getID()).equals("1");

                    String newVal;

                    if (StringUtil.in(parsedArgs, true, "true", "1")) {
                        newVal = "1";
                    } else if (StringUtil.in(parsedArgs, true, "false", "0")) {
                        newVal = "0";
                    } else {
                        newVal = oldVal ? "0" : "1";
                    }

                    UserUtil.setUserDataById(pref.getID(), newVal);

                    getInputHandler().println(pref.getDisplayName()
                            + " set to " + (newVal.equals("1") ? "true" : "false"));

                    Preferences.invokeRefresh(pref.getID());

                    return true;
                }
            }
        }

        return false;
    }
}
