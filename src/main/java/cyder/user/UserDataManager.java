package cyder.user;

import com.google.common.base.Preconditions;
import cyder.enums.Dynamic;
import cyder.utils.SerializationUtil;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * A managed for the current {@link NewUser}.
 * The current Cyder user is not exposed but instead proxied by this manager
 * for purposes of encapsulation, validation, and convenience methods.
 */
public enum UserDataManager {
    /**
     * The user data manager instance.
     */
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
     * @throws IllegalArgumentException if the uuid is empty, the user json does not exist, or the manager
     *                                  is current initialized
     */
    public synchronized void initialize(String uuid) {
        Preconditions.checkState(!isInitialized());
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
     *
     * @throws IOException if an IO error occurs when writing the current user to the user file
     */
    public synchronized void removeManagement() throws IOException {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(userFile));
            SerializationUtil.toJson(user, writer);
        } catch (Exception e) {
            throw new IOException("Failed to write current user to file. Exception: " + e.getMessage());
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
        boolean userPresent = user != null;
        boolean filePresent = userFile != null && userFile.exists();

        return userPresent && filePresent;
    }

    // -----------------------
    // UI element construction
    // -----------------------

    public Component getEditorComponent(String propertyName) {
        return null;
    }

    // -----------------
    // Getter hook logic
    // -----------------

    /**
     * A common method that should be invoked on all accessor methods contained within
     * this data manger.
     *
     * @param dataId the id of the data being accessed
     */
    private void getterInvoked(String dataId) {
        Preconditions.checkNotNull(dataId);
        Preconditions.checkArgument(!dataId.isEmpty());


    }

    // -----------------------------
    // Proxy methods for user object
    // -----------------------------

    /**
     * Returns the name of the current user.
     *
     * @return the name of the current user
     */
    public synchronized String getUsername() {
        Preconditions.checkState(isInitialized());

        getterInvoked("username");
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
