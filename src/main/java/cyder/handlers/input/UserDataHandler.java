package cyder.handlers.input;

import cyder.annotations.Handle;
import cyder.annotations.SuppressCyderInspections;
import cyder.constants.CyderRegexPatterns;
import cyder.enums.CyderInspection;
import cyder.exceptions.IllegalMethodException;
import cyder.strings.CyderStrings;
import cyder.strings.StringUtil;
import cyder.user.UserData;
import cyder.user.UserDataManager;
import cyder.user.UserEditor;

import java.util.Optional;

/**
 * A handler for switching/toggling user data.
 */
public class UserDataHandler extends InputHandler {
    /**
     * Suppress default constructor.
     */
    private UserDataHandler() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    @SuppressCyderInspections(CyderInspection.HandleInspection)
    @Handle({"userdata-files", "userdata-fonts", "userdata-colors", "userdata-prefs", "userdata-fields"})
    public static boolean handle() {
        if (getInputHandler().inputIgnoringSpacesMatches("userdata-files")) {
            UserEditor.showGui(UserEditor.Page.FILES);
            return true;
        } else if (getInputHandler().inputIgnoringSpacesMatches("userdata-fonts")) {
            UserEditor.showGui(UserEditor.Page.FONT_AND_COLOR);
            return true;
        } else if (getInputHandler().inputIgnoringSpacesMatches("userdata-colors")) {
            UserEditor.showGui(UserEditor.Page.FONT_AND_COLOR);
            return true;
        } else if (getInputHandler().inputIgnoringSpacesMatches("userdata-prefs")) {
            UserEditor.showGui(UserEditor.Page.BOOLEANS); // todo rename
            return true;
        } else if (getInputHandler().inputIgnoringSpacesMatches("userdata-fields")) {
            UserEditor.showGui(UserEditor.Page.FIELDS);
            return true;
        }

        return attemptUserDataToggle();
    }

    /**
     * Attempts to find a user data with the user's input and toggle the parity of it.
     *
     * @return whether a user data could be found and toggled
     */
    private static boolean attemptUserDataToggle() {
        String targetedUserData = getInputHandler().getCommand();
        String parsedArgs = getInputHandler().argsToString()
                .replaceAll(CyderRegexPatterns.whiteSpaceRegex, "");

        for (UserData<?> userdata : UserData.getUserDatas()) {
            if (targetedUserData.equalsIgnoreCase(userdata.getId())) {
                if (userdata.getType().equals(Boolean.class) && !userdata.shouldIgnoreForToggleSwitches()) {
                    Optional<Boolean> optionalOldValue =
                            UserDataManager.INSTANCE.getUserDataById(userdata.getId(), Boolean.class);
                    if (optionalOldValue.isEmpty()) return false;

                    boolean oldValue = optionalOldValue.get();

                    boolean newValue;

                    // todo methods for is true / is false maybe name weak?
                    if (StringUtil.in(parsedArgs, true, "true", "1")) {
                        newValue = true;
                    } else if (StringUtil.in(parsedArgs, true, "false", "0")) {
                        newValue = false;
                    } else {
                        newValue = !oldValue;
                    }

                    boolean toggled = UserDataManager.INSTANCE.setUserDataById(userdata.getId(), newValue);
                    if (toggled) {
                        getInputHandler().println(userdata.getId() + " set to " + newValue);
                        UserData.invokeRefresh(userdata.getId());
                    } else {
                        getInputHandler().println("Failed to set " + userdata.getId());
                    }

                    return true;
                }
            }
        }

        return false;
    }
}
