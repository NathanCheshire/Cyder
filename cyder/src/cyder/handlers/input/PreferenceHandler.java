package cyder.handlers.input;

import cyder.annotations.Handle;
import cyder.annotations.SuppressCyderInspections;
import cyder.constants.CyderRegexPatterns;
import cyder.constants.CyderStrings;
import cyder.enums.CyderInspection;
import cyder.exceptions.IllegalMethodException;
import cyder.user.Preference;
import cyder.user.UserEditor;
import cyder.user.UserUtil;
import cyder.utils.StringUtil;

/**
 * A handler for switching/toggling preferences.
 */
public class PreferenceHandler extends InputHandler {
    /**
     * Suppress default constructor.
     */
    private PreferenceHandler() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    @SuppressCyderInspections(CyderInspection.HandleInspection)
    @Handle({"prefs-files", "prefs-fonts", "prefs-colors", "prefs-prefs", "prefs-fields"})
    public static boolean handle() {
        if (getInputHandler().inputIgnoringSpacesMatches("prefs-files")) {
            UserEditor.showGui(UserEditor.Page.FILES);
            return true;
        } else if (getInputHandler().inputIgnoringSpacesMatches("prefs-fonts")) {
            UserEditor.showGui(UserEditor.Page.FONT_AND_COLOR);
            return true;
        } else if (getInputHandler().inputIgnoringSpacesMatches("prefs-colors")) {
            UserEditor.showGui(UserEditor.Page.FONT_AND_COLOR);
            return true;
        } else if (getInputHandler().inputIgnoringSpacesMatches("prefs-prefs")) {
            UserEditor.showGui(UserEditor.Page.PREFERENCES);
            return true;
        } else if (getInputHandler().inputIgnoringSpacesMatches("prefs-fields")) {
            UserEditor.showGui(UserEditor.Page.FIELDS);
            return true;
        }

        return attemptPreferenceToggle();
    }

    /**
     * Attempts to find a preference with the user's input and toggle the state of it.
     *
     * @return whether a preference could be found and toggled from the current user's json
     */
    private static boolean attemptPreferenceToggle() {
        String targetedPreference = getInputHandler().getCommand();
        String parsedArgs = getInputHandler().argsToString()
                .replaceAll(CyderRegexPatterns.whiteSpaceRegex, "");

        for (Preference pref : Preference.getPreferences()) {
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

                    Preference.invokeRefresh(pref.getID());

                    return true;
                }
            }
        }

        return false;
    }
}
