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
import cyder.strings.StringUtil;
import cyder.ui.UiUtil;
import cyder.user.creation.InputValidation;
import cyder.utils.ImageUtil;
import cyder.utils.OsUtil;
import cyder.utils.SerializationUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

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
     * Suppress default constructor.
     */
    private UserUtil() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Writes the provided user to the provided file.
     *
     * @param file the file to write to
     * @param user the user object to serialize and write to the file
     */
    public static void writeUserToFile(File file, User user) {
        Preconditions.checkNotNull(file);
        Preconditions.checkNotNull(user);
        Preconditions.checkArgument(file.exists());
        Preconditions.checkArgument(FileUtil.validateExtension(file, Extension.JSON.getExtension()));

        try {
            String json = SerializationUtil.toJson(user);
            FileUtil.writeLinesToFile(file, ImmutableList.of(json), false);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
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
            File currentUserBackgrounds = Dynamic.buildDynamic(
                    Dynamic.USERS.getFileName(), uuid, UserFile.BACKGROUNDS.getName());

            if (!currentUserBackgrounds.exists()) return;

            File[] files = currentUserBackgrounds.listFiles();

            if (files != null && files.length > 0) {
                for (File backgroundFile : files) {
                    boolean valid = true;

                    try (FileInputStream fis = new FileInputStream(backgroundFile)) {
                        ImageUtil.read(fis).getWidth();
                    } catch (Exception ignored) {
                        valid = false;
                    }

                    if (!valid) {
                        OsUtil.deleteFile(backgroundFile);
                    }
                }
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
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

        return User.fromJson(file);
    }

    /**
     * Returns the setter method for the provided user object's data piece with the provided name.
     *
     * @param dataName the name of the data piece such as "username"
     * @param user     the user object
     * @return the setter method for a user object data piece with the provided name
     */
    public static Optional<Method> getSetterMethodForDataWithName(String dataName, User user) {
        Preconditions.checkNotNull(dataName);
        Preconditions.checkArgument(!dataName.isEmpty());

        for (Method method : user.getClass().getMethods()) {
            if (method.getParameterCount() == 1
                    && method.getName().toLowerCase().contains(dataName.toLowerCase())) {
                return Optional.of(method);
            }
        }

        return Optional.empty();
    }

    /**
     * Returns the getter method for the provided user object's data piece with the provided name.
     *
     * @param dataName the name of the data piece such as "username"
     * @param user     the user object
     * @return the getter method for a user object data piece with the provided name
     */
    public static Optional<Method> getGetterMethodForDataWithName(String dataName, User user) {
        for (Method method : user.getClass().getMethods()) {
            if (method.getParameterCount() == 0
                    && method.getName().toLowerCase().contains(dataName.toLowerCase())) {
                return Optional.of(method);
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
            Arrays.stream(users).forEach(user -> uuids.add(user.getName()));
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
            Arrays.stream(users).forEach(user -> userFiles.add(
                    OsUtil.buildFile(user.getAbsolutePath(), UserFile.USERDATA.getName())));
        }

        return ImmutableList.copyOf(userFiles);
    }

    /**
     * Logs out all users.
     */
    public static void logoutAllUsers() {
        getUserJsons().forEach(jsonFile -> {
            User user = extractUser(jsonFile);
            user.setLoggedIn(false);
            SerializationUtil.toJson(user, jsonFile);
        });
    }

    /**
     * Searches through the users directory and finds the first logged-in user.
     *
     * @return the uuid of the first logged-in user
     */
    public static Optional<String> getFirstLoggedInUser() {
        for (File userJson : getUserJsons()) {
            if (extractUser(userJson).isLoggedIn()) {
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
            if (extractUser(userFile).getUsername().equalsIgnoreCase(username)) {
                return true;
            }
        }

        return false;
    }

    /**
     * The all string to indicate all user data should be ignored when logging.
     */
    private static final String ALL = "all";

    /**
     * Returns whether a getter for the user data with the provided ID should be ignored when logging.
     *
     * @param dataId the data is
     * @return whether a getter for the user data with the provided ID should be ignored when logging
     */
    public static boolean shouldIgnoreForLogging(String dataId) {
        ImmutableList<String> ignoreDatas = Props.ignoreData.getValue().getList();
        return ignoreDatas.contains(ALL) || StringUtil.in(dataId, true, ignoreDatas);
    }
}
