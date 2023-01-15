package cyder.user;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.console.Console;
import cyder.constants.CyderIcons;
import cyder.constants.CyderUrls;
import cyder.enums.Dynamic;
import cyder.enums.Extension;
import cyder.exceptions.FatalException;
import cyder.exceptions.IllegalMethodException;
import cyder.files.FileUtil;
import cyder.handlers.internal.ExceptionHandler;
import cyder.logging.LogTag;
import cyder.logging.Logger;
import cyder.meta.CyderSplash;
import cyder.network.LatencyManager;
import cyder.props.Props;
import cyder.strings.CyderStrings;
import cyder.strings.LevenshteinUtil;
import cyder.strings.StringUtil;
import cyder.ui.UiUtil;
import cyder.user.creation.InputValidation;
import cyder.user.data.ScreenStat;
import cyder.utils.ImageUtil;
import cyder.utils.OsUtil;
import cyder.utils.SerializationUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.Semaphore;

/**
 * Utilities regarding a user, their json file, and IO to/from that json file.
 */
public final class UserUtil {
    /**
     * The minimum allowable password length.
     */
    public static final int MIN_PASSWORD_LENGTH = 5;

    /**
     * The maximum latency to allow when attempting to download the default user background.
     */
    private static final int maxLatencyToDownloadDefaultBackground = 2000;

    /**
     * The name of the default background, if generation is required.
     */
    private static final String defaultBackgroundName = "Default";

    /**
     * The method prefix to locate mutator methods reflectively.
     */
    private static final String SET = "set";

    /**
     * The json write tag.
     */
    private static final String JSON_WRITE = "[JSON WRITE]";

    /**
     * The semaphore to use when reading or writing user data.
     */
    private static final Semaphore userIoSemaphore = new Semaphore(1);

    /**
     * The current Cyder user stored in memory and written to the
     * current user file whenever time data changes.
     */
    private static User cyderUser;

    /**
     * The corresponding file for cyderUser.
     */
    private static File cyderUserFile;

    /**
     * The last serialized string that was written to the current user file.
     */
    private static String previousSerializedUser = "";

    /**
     * The current levenshtein distance between the last and current write to the user json file.
     */
    private static int currentLevenshteinDistance;

    /**
     * Suppress default constructor.
     */
    private UserUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Blocks any future user IO by acquiring the semaphore and never releasing it.
     * This method blocks until the IO semaphore can be acquired.
     */
    public static synchronized void blockFutureIo() {
        try {
            userIoSemaphore.acquire();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Writes the current User, {@link UserUtil#cyderUser},
     * to the user's json if the json exists AND the provided user
     * object contains all the data required by a user object.
     * Upon a successful serialization/de-serialization, the json
     * is backed up and placed in dynamic/backup.
     */
    public static synchronized void writeUser() {
        if (cyderUserFile == null || !cyderUserFile.exists() || cyderUser == null) return;

        try {
            setUserData(cyderUserFile, cyderUser);

            if (currentLevenshteinDistance > 0) {
                String representation = JSON_WRITE + CyderStrings.space + CyderStrings.openingBracket
                        + "Levenshtein: " + currentLevenshteinDistance + CyderStrings.closingBracket
                        + CyderStrings.space + "User" + CyderStrings.space + CyderStrings.quote
                        + cyderUser.getName() + CyderStrings.quote + CyderStrings.space
                        + "was written to file" + CyderStrings.colon + CyderStrings.space
                        + OsUtil.buildPath(cyderUserFile.getParentFile().getName(), cyderUserFile.getName());
                Logger.log(LogTag.SYSTEM_IO, representation);
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Sets the given user to the current Cyder user. This method should only be called if the current contents
     * of the user, meaning possible writes within the past 100ms, can be discarded.
     * <p>
     * This method should only be called when setting the user due to a Cyder
     * login event, specifically via the console method {@link Console#setUuid(String)}.
     * <p>
     * If you are trying to set data for the current cyder user,
     * call {@link UserUtil#getCyderUser()} and use mutator methods on that object.
     * <p>
     * Common usages of this, such as setting an object instead of a primitive attribute such
     * as the user's {@link ScreenStat} would look like the following:
     * <pre>{@code UserUtil.getCyderUser().setScreenStat(myScreenStat);}</pre>
     *
     * @param uuid the user's uuid
     */
    public static void setCyderUser(String uuid) {
        Preconditions.checkNotNull(uuid);
        Preconditions.checkArgument(!uuid.isEmpty());

        File jsonFile = Dynamic.buildDynamic(Dynamic.USERS.getFileName(), uuid, UserFile.USERDATA.getName());

        Preconditions.checkArgument(jsonFile.exists());
        Preconditions.checkArgument(FileUtil.validateExtension(jsonFile, Extension.JSON.getExtension()));

        cyderUserFile = jsonFile;
        cyderUser = extractUser(jsonFile);
    }

    private static final NewUser defaultUser = new NewUser();

    /**
     * Returns the currently set Cyder user.
     * If not set, a default user is generated and returned.
     *
     * @return the currently set Cyder user
     */
    public static User getCyderUser() {
        if (cyderUser == null) return new User();
        return cyderUser;
    }

    /**
     * Writes the provided user to the provided file.
     *
     * @param file the file to write to
     * @param user the user object to serialize and write to the file
     */
    public static void setUserData(File file, User user) {
        Preconditions.checkNotNull(file);
        Preconditions.checkNotNull(user);
        Preconditions.checkArgument(file.exists());
        Preconditions.checkArgument(FileUtil.validateExtension(file, Extension.JSON.getExtension()));

        try {
            FileWriter writer = new FileWriter(file);
            userIoSemaphore.acquire();
            SerializationUtil.toJson(user, writer);

            String currentSerializedUser = SerializationUtil.toJson(user);

            if (previousSerializedUser.isEmpty()) {
                currentLevenshteinDistance = currentSerializedUser.length();
            } else {
                currentLevenshteinDistance = LevenshteinUtil.computeLevenshteinDistance(
                        currentSerializedUser, previousSerializedUser);
            }

            previousSerializedUser = currentSerializedUser;

            writer.close();
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        } finally {
            userIoSemaphore.release();
        }
    }

    /**
     * Attempts to read backgrounds that Cyder would use for a user.
     * If failure, the image is corrupted, so we delete it in the calling function.
     *
     * @param uuid the uuid of the user whose backgrounds to validate
     */
    public static void deleteInvalidBackgrounds(String uuid) {
        try {
            userIoSemaphore.acquire();

            File currentUserBackgrounds = Dynamic.buildDynamic(Dynamic.USERS.getFileName(),
                    uuid, UserFile.BACKGROUNDS.getName());

            if (!currentUserBackgrounds.exists()) return;

            File[] files = currentUserBackgrounds.listFiles();

            if (files != null && files.length > 0) {
                for (File f : files) {
                    boolean valid = true;

                    try (FileInputStream fis = new FileInputStream(f)) {
                        ImageUtil.read(fis).getWidth();
                    } catch (Exception ignored) {
                        valid = false;
                    }

                    if (!valid) {
                        OsUtil.deleteFile(f);
                    }
                }
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        } finally {
            userIoSemaphore.release();
        }
    }

    /**
     * Extracts the user from the provided json file.
     *
     * @param file the json file to extract a user object from
     * @return the resulting user object
     */
    public static User extractUser(File file) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());
        Preconditions.checkArgument(FileUtil.validateExtension(file, Extension.JSON.getExtension()));

        return SerializationUtil.fromJson(file, User.class);
    }

    /**
     * Sets the {@link UserUtil#cyderUser}'s data to the provided value.
     * This method exists purely for when indexing the preferences and user data
     * is required. The direct setter should be used if possible.
     *
     * @param name  the name of the data to set
     * @param value the new value
     */
    public static void setUserDataById(String name, String value) {
        Preconditions.checkNotNull(name);
        Preconditions.checkNotNull(value);
        Preconditions.checkArgument(!name.isEmpty());
        Preconditions.checkArgument(!value.isEmpty());

        try {
            for (Method m : cyderUser.getClass().getMethods()) {
                if (m.getName().startsWith(SET)
                        && m.getParameterTypes().length == 1
                        && m.getName().replace(SET, "").equalsIgnoreCase(name)) {
                    m.invoke(cyderUser, value);
                    writeUser();
                    break;
                }
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * The list of userdata to ignore when logging.
     */
    private static final ImmutableList<String> IGNORE_USER_DATA = ImmutableList.copyOf(
            Props.ignoreData.getValue().getList()
    );

    /**
     * Returns the list of user data keys to ignore when logging.
     *
     * @return the list of user data keys to ignore when logging
     */
    public static ImmutableList<String> getIgnoreUserData() {
        return IGNORE_USER_DATA;
    }

    /**
     * Returns the requested data from the currently logged-in user.
     * This method exists purely for when indexing the preferences and user data
     * is required. The direct getter should be used if possible.
     *
     * @param id the ID of the data we want to obtain
     * @return the resulting data
     */
    public static String getUserDataById(String id) {
        Preconditions.checkArgument(!StringUtil.isNullOrEmpty(id));

        String ret = null;
        boolean shouldIgnore = StringUtil.in(id, true, IGNORE_USER_DATA);

        if (!shouldIgnore) {
            Logger.log(LogTag.SYSTEM_IO, "Userdata requested: " + id);
        }

        try {
            for (Method m : cyderUser.getClass().getMethods()) {
                if (m.getName().startsWith("get")
                        && m.getParameterTypes().length == 0
                        && m.getName().toLowerCase().contains(id.toLowerCase())) {
                    Object r = m.invoke(cyderUser);
                    ret = (String) r;
                    break;
                }
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return ret;
    }

    /**
     * Returns the setter method for a user object data piece with the provided name.
     *
     * @param dataName the name of the data piece such as "username"
     * @param user     the user object
     * @return the setter method for a user object data piece with the provided name
     */
    public static Optional<Method> getSetterMethodForDataWithName(String dataName, User user) {
        Preconditions.checkNotNull(dataName);
        Preconditions.checkArgument(!dataName.isEmpty());

        for (Method m : user.getClass().getMethods()) {
            if (m.getName().startsWith("get") && m.getParameterTypes().length == 0
                    && m.getName().equalsIgnoreCase(dataName.toLowerCase())) {
                return Optional.of(m);
            }
        }

        return Optional.empty();
    }

    /**
     * Clean the user directories meaning the following actions are taken:
     *
     * <ul>
     *     <li>Ensuring the users directory is created</li>
     *     <li>Deleting non audio files from the Music/ directory</li>
     *     <li>Removing album art not linked to an audio file</li>
     *     <li>Removing backup json files which are not linked to any users</li>
     * </ul>
     */
    public static void cleanUsers() {
        File users = Dynamic.buildDynamic(Dynamic.USERS.getFileName());
        if (!users.exists()) {
            if (!users.mkdirs()) {
                throw new FatalException("Failed to create users directory");
            }

            return;
        }

        File[] uuids = users.listFiles();
        if (uuids != null && uuids.length > 0) {
            for (File user : uuids) {
                if (!user.isDirectory()) {
                    throw new FatalException("Found non-directory in users directory: " + user.getAbsolutePath());
                }

                File musicDir = OsUtil.buildFile(user.getAbsolutePath(), UserFile.MUSIC.getName());
                if (musicDir.exists()) {
                    CyderSplash.INSTANCE.setLoadingMessage("Cleaning user music directory: "
                            + FileUtil.getFilename(user));
                    cleanUserMusicDirectory(musicDir, user);
                } else {
                    if (!OsUtil.createFile(musicDir, false)) {
                        throw new FatalException("Failed to create user's music directory: "
                                + musicDir.getAbsolutePath());
                    }
                }

                File backgroundsDir = OsUtil.buildFile(user.getAbsolutePath(), UserFile.BACKGROUNDS.getName());
                if (backgroundsDir.exists()) {
                    CyderSplash.INSTANCE.setLoadingMessage("Resizing user backgrounds: "
                            + FileUtil.getFilename(user));
                    resizeUserBackgroundFiles(backgroundsDir);
                } else {
                    if (!OsUtil.createFile(backgroundsDir, false)) {
                        throw new FatalException("Failed to create user's backgrounds directory: "
                                + backgroundsDir.getAbsolutePath());
                    }
                }
            }
        }
    }

    /**
     * Resizes all the valid backgrounds in the provided user backgrounds directory.
     *
     * @param backgroundsDir the user backgrounds directory
     */
    private static void resizeUserBackgroundFiles(File backgroundsDir) {
        Preconditions.checkNotNull(backgroundsDir);
        Preconditions.checkArgument(backgroundsDir.exists());
        Preconditions.checkArgument(backgroundsDir.isDirectory());
        Preconditions.checkArgument(backgroundsDir.getName().equals(UserFile.BACKGROUNDS.getName()));

        File[] backgroundFiles = backgroundsDir.listFiles();
        if (backgroundFiles == null || backgroundFiles.length == 0) return;

        ArrayList<File> validBackgroundFiles = new ArrayList<>();
        Arrays.stream(backgroundFiles)
                .filter(FileUtil::isSupportedImageExtension)
                .forEach(validBackgroundFiles::add);

        Dimension maximumDimension = new Dimension(UiUtil.getDefaultMonitorWidth(), UiUtil.getDefaultMonitorHeight());

        for (File backgroundFile : validBackgroundFiles) {
            String filename = FileUtil.getFilename(backgroundFile);

            BufferedImage image = null;
            try {
                CyderSplash.INSTANCE.setLoadingMessage("Reading background: " + filename);
                image = ImageUtil.read(backgroundFile);
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }

            if (image == null) continue;
            CyderSplash.INSTANCE.setLoadingMessage("Checking if resize needed for background: " + filename);
            image = ImageUtil.ensureFitsInBounds(image, maximumDimension);

            try {
                if (!ImageIO.write(image, FileUtil.getExtensionWithoutPeriod(backgroundFile), backgroundFile)) {
                    throw new FatalException("Failed to downscale image: " + backgroundFile.getAbsolutePath());
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }
    }

    /**
     * Cleans the provided user music directory by removing non
     * audio files and album art not linked to current audio files.
     *
     * @param musicDirectory     the user music directory
     * @param userMusicDirectory the user music directory
     */
    private static void cleanUserMusicDirectory(File musicDirectory, File userMusicDirectory) {
        Preconditions.checkNotNull(musicDirectory);
        Preconditions.checkNotNull(musicDirectory);
        Preconditions.checkArgument(musicDirectory.exists());
        Preconditions.checkArgument(userMusicDirectory.exists());
        Preconditions.checkArgument(musicDirectory.isDirectory());
        Preconditions.checkArgument(userMusicDirectory.isDirectory());
        Preconditions.checkArgument(musicDirectory.getName().equals(UserFile.MUSIC.getName()));

        File[] files = musicDirectory.listFiles();
        ArrayList<String> validMusicFileNames = new ArrayList<>();

        // Remove non audio files from music directory
        if (files != null && files.length > 0) {
            Arrays.stream(files).forEach(musicFile -> {
                if (!FileUtil.isSupportedAudioExtension(musicFile) && !musicFile.isDirectory()) {
                    OsUtil.deleteFile(musicFile);
                } else {
                    validMusicFileNames.add(FileUtil.getFilename(musicFile));
                }
            });
        }

        File albumArtDirectory = OsUtil.buildFile(userMusicDirectory.getAbsolutePath(),
                UserFile.MUSIC.getName(), UserFile.ALBUM_ART);
        if (!albumArtDirectory.exists()) return;
        File[] albumArtFiles = albumArtDirectory.listFiles();

        if (albumArtFiles != null && albumArtFiles.length > 0) {
            Arrays.stream(albumArtFiles).forEach(albumArtFile -> {
                if (!StringUtil.in(FileUtil.getFilename(albumArtFile), true, validMusicFileNames)) {
                    OsUtil.deleteFile(albumArtFile);
                }
            });
        }
    }

    // todo manager for this
    /**
     * The linked list of invalid users which this instance of Cyder will ignore.
     */
    private static final LinkedList<String> invalidUuids = new LinkedList<>() {
        @Override
        public boolean remove(Object o) {
            throw new IllegalMethodException("Removing of invalid uuids not allowed");
        }
    };

    /**
     * Adds the provided uuid to the list of uuids to ignore throughout Cyder.
     *
     * @param uuid the uuid to ignore
     */
    public static void addInvalidUuid(String uuid) {
        if (!StringUtil.in(uuid, false, invalidUuids)) {
            invalidUuids.add(uuid);
        }
    }

    /**
     * Returns the provided user file after creating it if it did not exist.
     *
     * @param userFile the user file to return the file reference of
     * @return the provided user file reference
     */
    public static File getUserFile(UserFile userFile) {
        Preconditions.checkNotNull(userFile);
        Preconditions.checkState(Console.INSTANCE.getUuid() != null);

        File ret = Dynamic.buildDynamic(Dynamic.USERS.getFileName(),
                Console.INSTANCE.getUuid(), userFile.getName());

        if (!ret.exists()) {
            if (OsUtil.createFile(ret, false)) {
                return ret;
            } else {
                throw new FatalException("Failed to create user file: " + userFile);
            }
        }

        return ret;
    }

    /**
     * Returns whether there are no users created for Cyder.
     *
     * @return whether there are no users created for Cyder
     */
    public static boolean noCyderUsers() {
        return getUserUuids().isEmpty();
    }

    /**
     * Returns a list of valid uuids associated with Cyder users.
     *
     * @return a list of valid uuids associated with Cyder users
     */
    public static ImmutableList<String> getUserUuids() {
        File usersDir = Dynamic.buildDynamic(Dynamic.USERS.getFileName());
        File[] users = usersDir.listFiles();

        ArrayList<String> uuids = new ArrayList<>();

        if (users != null && users.length > 0) {
            Arrays.stream(users).forEach(user -> {
                File json = OsUtil.buildFile(user.getAbsolutePath(), UserFile.USERDATA.getName());

                if (json.exists() && !StringUtil.in(user.getName(), false, invalidUuids)) {
                    uuids.add(user.getName());
                }
            });
        }

        return ImmutableList.copyOf(uuids);
    }

    /**
     * Returns a list of valid user jsons associated with Cyder users.
     *
     * @return a list of valid user jsons associated with Cyder users
     */
    public static ImmutableList<File> getUserJsons() {
        ArrayList<File> userFiles = new ArrayList<>();

        File usersDir = Dynamic.buildDynamic(Dynamic.USERS.getFileName());
        File[] users = usersDir.listFiles();

        if (users != null && users.length > 0) {
            Arrays.stream(users).forEach(user -> {
                File json = OsUtil.buildFile(user.getAbsolutePath(), UserFile.USERDATA.getName());

                if (json.exists() && !StringUtil.in(user.getName(), false, invalidUuids)) {
                    userFiles.add(json);
                }
            });
        }

        return ImmutableList.copyOf(userFiles);
    }

    /**
     * Logs out all users.
     */
    public static void logoutAllUsers() {
        getUserJsons().forEach(jsonFile -> {
            User user = extractUser(jsonFile);
            user.setLoggedIn("0");
            setUserData(jsonFile, user);
        });
    }

    /**
     * Searches through the users directory and finds the first logged-in user.
     *
     * @return the uuid of the first logged-in user
     */
    public static Optional<String> getFirstLoggedInUser() {
        for (File userJson : getUserJsons()) {
            if (extractUser(userJson).getLoggedIn().equals("1")) {
                return Optional.of(FileUtil.getFilename(userJson.getParentFile().getName()));
            }
        }

        return Optional.empty();
    }

    /**
     * Creates the default background inside the user's Backgrounds/ directory.
     *
     * @param uuid the user's uuid to save the default background to
     * @return a reference to the file created
     */
    public static File createDefaultBackground(String uuid) {
        Preconditions.checkNotNull(uuid);
        Preconditions.checkArgument(!uuid.isEmpty());

        BufferedImage createMe = CyderIcons.DEFAULT_USER_SOLID_COLOR_BACKGROUND;

        int latency = LatencyManager.INSTANCE.getLatency(maxLatencyToDownloadDefaultBackground);
        if (latency < maxLatencyToDownloadDefaultBackground) {
            try {
                createMe = ImageUtil.read(CyderUrls.DEFAULT_BACKGROUND_URL);
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }

        File backgroundFile = Dynamic.buildDynamic(Dynamic.USERS.getFileName(),
                uuid, UserFile.BACKGROUNDS.getName(), defaultBackgroundName + Extension.PNG.getExtension());
        File backgroundFolder = Dynamic.buildDynamic(Dynamic.USERS.getFileName(),
                uuid, UserFile.BACKGROUNDS.getName());

        try {
            if (!backgroundFolder.exists()) {
                if (!backgroundFolder.mkdir()) {
                    throw new FatalException("Could not create user's background directory at: "
                            + backgroundFolder.getAbsolutePath());
                }
            }

            ImageIO.write(createMe, Extension.PNG.getExtensionWithoutPeriod(), backgroundFile);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        return backgroundFile;
    }

    /**
     * Creates a file with the provided name in the current user's files/ directory.
     *
     * @param name the filename + extension to create in the files/ directory
     * @return a File object representing the file that was created
     * @throws IllegalStateException if the file could not be created at this time
     */
    public static File createFileInUserSpace(String name) {
        Preconditions.checkNotNull(name);
        Preconditions.checkState(!name.isEmpty());
        Preconditions.checkState(!StringUtil.isNullOrEmpty(Console.INSTANCE.getUuid()));

        File saveDir = Dynamic.buildDynamic(Dynamic.USERS.getFileName(),
                Console.INSTANCE.getUuid(), UserFile.FILES.getName());
        File createFile = new File(saveDir, name);

        if (createFile.exists()) {
            Logger.log(LogTag.SYSTEM_IO, "File already exists in userspace: " + name);
            return createFile;
        }

        try {
            if (!saveDir.exists()) {
                if (!saveDir.mkdir()) {
                    throw new FatalException("Failed to create user files folder: " + saveDir.getAbsolutePath());
                }
            }

            if (OsUtil.createFile(createFile, true)) {
                Logger.log(LogTag.SYSTEM_IO, "Created file in userspace: " + name);
                return createFile;
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        throw new FatalException("Could not create file in user's file directory");
    }

    /**
     * Resets all data/preferences (preferences for which {@link Preference#getIgnoreForUserCreation()} returns true)
     * to their default values.
     *
     * @param user the user to reset to a default state
     */
    public static void resetUser(User user) {
        Preconditions.checkNotNull(user);

        Preference.getPreferences().stream().filter(preference -> !preference.getIgnoreForUserCreation())
                .forEach(preference -> {
                    for (Method method : user.getClass().getMethods()) {
                        boolean isSetter = method.getName().startsWith(SET);
                        boolean oneParameter = method.getParameterTypes().length == 1;
                        boolean methodNameMatchesPreferenceId = method.getName().replace(SET, "")
                                .equalsIgnoreCase(preference.getID());

                        if (isSetter && oneParameter && methodNameMatchesPreferenceId) {
                            try {
                                method.invoke(user, preference.getDefaultValue());
                            } catch (Exception e) {
                                ExceptionHandler.handle(e);
                            }

                            break;
                        }
                    }

                });
    }

    /**
     * Returns whether the provided username is valid.
     *
     * @param username the username to validate
     * @return whether the provided username is valid
     */
    public static InputValidation validateUsername(String username) {
        Preconditions.checkNotNull(username);

        if (username.isEmpty()) {
            return InputValidation.NO_USERNAME;
        } else if (!StringUtil.removeNonAscii(username).equals(username)) {
            return InputValidation.INVALID_USERNAME;
        } else if (usernameInUse(username)) {
            return InputValidation.USERNAME_IN_USE;
        } else {
            return InputValidation.VALID;
        }
    }

    /**
     * Returns whether the provided passwords match and are valid.
     *
     * @param password             the first password
     * @param passwordConfirmation the password confirmation
     * @return whether the provided passwords match and are valid
     */
    public static InputValidation validatePassword(char[] password, char[] passwordConfirmation) {
        Preconditions.checkNotNull(password);
        Preconditions.checkNotNull(passwordConfirmation);

        if (password.length == 0) {
            return InputValidation.NO_PASSWORD;
        } else if (passwordConfirmation.length == 0) {
            return InputValidation.NO_CONFIRMATION_PASSWORD;
        } else if (!Arrays.equals(password, passwordConfirmation)) {
            return InputValidation.PASSWORDS_DO_NOT_MATCH;
        } else if (password.length <= MIN_PASSWORD_LENGTH) {
            return InputValidation.INVALID_PASSWORD_LENGTH;
        } else if (!StringUtil.containsLetter(password)) {
            return InputValidation.NO_LETTER_IN_PASSWORD;
        } else if (!StringUtil.containsNumber(password)) {
            return InputValidation.NO_NUMBER_IN_PASSWORD;
        } else {
            return InputValidation.VALID;
        }
    }

    /**
     * Returns whether the provided username is already in use.
     *
     * @param username the username to determine if in use
     * @return whether the provided username is already in use
     */
    public static boolean usernameInUse(String username) {
        Preconditions.checkNotNull(username);
        Preconditions.checkArgument(!username.isEmpty());

        if (noCyderUsers()) {
            return false;
        }

        for (File userFile : getUserJsons()) {
            if (extractUser(userFile).getName().equalsIgnoreCase(username)) {
                return true;
            }
        }

        return false;
    }
}
