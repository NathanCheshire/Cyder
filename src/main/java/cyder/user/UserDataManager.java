package cyder.user;

import com.google.common.base.Preconditions;
import cyder.enums.Dynamic;
import cyder.handlers.internal.ExceptionHandler;
import cyder.utils.SerializationUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

/**
 * A managed for the current {@link NewUser}.
 * The current Cyder user is not exposed but instead proxied by this manager
 * for purposes of encapsulation, validation, and convenience methods.
 */
public enum UserDataManager {
    INSTANCE;

    /**
     * The current user object this manager is being a proxy for.
     */
    private NewUser user;

    /**
     * The file the current user object is written to periodically and on program closure.
     */
    private File userFile;

    /**
     * Sets the current Cyder user to the user with the provided uuid.
     *
     * @param uuid the uuid of the Cyder user to set for the current session
     * @throws NullPointerException     if the uuid is null
     * @throws IllegalArgumentException if the uuid is empty, user json does not exist, or the manager
     *                                  has already been initialized without {@link #removeManagement} being invoked
     */
    public synchronized void initialize(String uuid) {
        Preconditions.checkNotNull(uuid);
        Preconditions.checkArgument(!uuid.isEmpty());

        File jsonFile = Dynamic.buildDynamic(Dynamic.USERS.getFileName(), uuid, UserFile.USERDATA.getName());
        Preconditions.checkArgument(jsonFile.exists());

        userFile = jsonFile;
        user = NewUser.fromJson(jsonFile);
    }

    /**
     * Serializes the current Cyder user to the {@link #userFile}, after which the user and user file are set to null
     * allowing for the {@link #initialize(String)} method to be invoked again.
     */
    public synchronized void removeManagement() {
        // todo write user

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(userFile));
            SerializationUtil.toJson(user, writer);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
            // todo failed to write user to shouldn't this throw to caller?
        }

        userFile = null;
        user = null;
    }

    /**
     * Returns whether this manager is initialized with a user and userfile to manage.
     *
     * @return whether this manager is initialized with a user and userfile to manage
     */
    public synchronized boolean isInitialized() {
        return user != null;
    }

    /**
     * Returns the name of the current user.
     *
     * @return the name of the current user
     */
    public synchronized String getUsername() {
        Preconditions.checkState(isInitialized());

        return user.getUsername();
    }

    /**
     * Sets the username of the current user.
     *
     * @param username the new requested username
     */
    public synchronized void setUsername(String username) {
        Preconditions.checkState(isInitialized());
        Preconditions.checkNotNull(username);
        Preconditions.checkArgument(!username.isEmpty());
        Preconditions.checkArgument(!UserUtil.usernameInUse(username));

        user.setUsername(username);
    }
}
