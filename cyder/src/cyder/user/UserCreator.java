package cyder.user;

import com.google.common.base.Preconditions;
import cyder.annotations.CyderAuthor;
import cyder.annotations.SuppressCyderInspections;
import cyder.annotations.Vanilla;
import cyder.annotations.Widget;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderIcons;
import cyder.constants.CyderStrings;
import cyder.enums.CyderInspection;
import cyder.enums.Direction;
import cyder.enums.Dynamic;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.InformHandler;
import cyder.handlers.internal.LoginHandler;
import cyder.layouts.CyderPartitionedLayout;
import cyder.threads.CyderThreadRunner;
import cyder.ui.CyderButton;
import cyder.ui.CyderFrame;
import cyder.ui.CyderPasswordField;
import cyder.ui.CyderTextField;
import cyder.utils.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * A widget to create a Cyder user.
 */
@Vanilla
@CyderAuthor
@SuppressCyderInspections(CyderInspection.VanillaInspection)
public final class UserCreator {
    /**
     * The user creator frame.
     */
    private static CyderFrame createUserFrame;

    /**
     * The password field for the user's password.
     */
    private static CyderPasswordField newUserPasswordField;

    /**
     * The password field to confirm the new user's password.
     */
    private static CyderPasswordField newUserPasswordConfirmationField;

    /**
     * The field for the new user's name.
     */
    private static CyderTextField newUserNameField;

    /**
     * The background chosen by the user as their initial background.
     */
    private static File newUserBackgroundFile;

    /**
     * The button to finalize the creation of a new user.
     */
    private static CyderButton createNewUserButton;

    /**
     * The label to display information on to help the user with their account creation.
     */
    private static JLabel informationLabel;

    /**
     * The button to choose a background file for the proposed user.
     */
    private static CyderButton chooseBackgroundButton;

    /**
     * The border used for fields and buttons.
     */
    private static final LineBorder BORDER = new LineBorder(CyderColors.navy, 5, false);

    /**
     * Suppress default constructor.
     */
    private UserCreator() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    @Widget(triggers = {"create user", "create"}, description = "A widget for creating new users")
    public static void showGui() {
        closeIfOpen();

        newUserBackgroundFile = null;

        createUserFrame = new CyderFrame(350, 500, CyderIcons.defaultBackground);
        createUserFrame.setTitle("Create User");

        JLabel nameLabel = new JLabel("Username: ", SwingConstants.CENTER);
        nameLabel.setFont(CyderFonts.SEGOE_20);
        nameLabel.setForeground(CyderColors.navy);
        nameLabel.setSize(120, 30);

        createNewUserButton = new CyderButton("Create User");

        newUserNameField = new CyderTextField(0);
        newUserNameField.setHorizontalAlignment(JTextField.CENTER);
        newUserNameField.setBackground(Color.white);
        newUserNameField.setFont(CyderFonts.SEGOE_20);
        newUserNameField.setBorder(new LineBorder(Color.black));
        newUserNameField.addKeyListener(newUserNameFieldListener);

        newUserNameField.setBorder(BORDER);
        newUserNameField.setSize(240, 40);

        if (!defaultCyderUserAlreadyExists()) {
            newUserNameField.setText(OSUtil.getOsUsername());
        }

        JLabel passwordLabel = new JLabel("Password: ", SwingConstants.CENTER);
        passwordLabel.setFont(CyderFonts.SEGOE_20);
        passwordLabel.setForeground(CyderColors.navy);
        passwordLabel.setSize(240, 30);

        informationLabel = new JLabel("Passwords match", SwingConstants.CENTER);

        newUserPasswordField = new CyderPasswordField();
        newUserPasswordField.addKeyListener(passwordFieldKeyListener);
        newUserPasswordField.setSize(240, 40);
        newUserPasswordField.setToolTipText("Shift shows password");

        JLabel passwordLabelConf = new JLabel("Confirm Password: ", SwingConstants.CENTER);
        passwordLabelConf.setFont(CyderFonts.SEGOE_20);
        passwordLabelConf.setForeground(CyderColors.navy);
        passwordLabelConf.setSize(240, 30);

        newUserPasswordConfirmationField = new CyderPasswordField();
        newUserPasswordConfirmationField.addKeyListener(passwordFieldKeyListener);
        newUserPasswordConfirmationField.setSize(240, 40);
        newUserPasswordConfirmationField.setToolTipText("Shift shows password");

        informationLabel.setFont(CyderFonts.SEGOE_20);
        informationLabel.setForeground(CyderColors.regularGreen);
        informationLabel.setSize(300, 30);

        chooseBackgroundButton = new CyderButton("Choose Background");
        chooseBackgroundButton.setToolTipText("Choose a background for the console");
        chooseBackgroundButton.setFont(CyderFonts.SEGOE_20);
        chooseBackgroundButton.setBackground(CyderColors.regularRed);
        chooseBackgroundButton.addMouseListener(chooseBackgroundButtonMouseListener);
        chooseBackgroundButton.addActionListener(chooseBackgroundButtonActionListener);

        chooseBackgroundButton.setBorder(BORDER);
        chooseBackgroundButton.setSize(240, 40);

        createNewUserButton.setFont(CyderFonts.SEGOE_20);
        createNewUserButton.setBackground(CyderColors.regularRed);
        createNewUserButton.setToolTipText("Create");
        createNewUserButton.addActionListener(createNewUserActionListener);
        createNewUserButton.setBorder(BORDER);
        createNewUserButton.setFont(CyderFonts.SEGOE_20);
        createNewUserButton.setSize(240, 40);

        CyderPartitionedLayout cyderPartitionedLayout = new CyderPartitionedLayout();

        cyderPartitionedLayout.addComponent(new JLabel(), 10);
        cyderPartitionedLayout.addComponent(nameLabel, 5);
        cyderPartitionedLayout.addComponent(newUserNameField, 8);
        cyderPartitionedLayout.addComponent(new JLabel(), 5);
        cyderPartitionedLayout.addComponent(passwordLabel, 5);
        cyderPartitionedLayout.addComponent(newUserPasswordField, 8);
        cyderPartitionedLayout.addComponent(new JLabel(), 5);
        cyderPartitionedLayout.addComponent(passwordLabelConf, 5);
        cyderPartitionedLayout.addComponent(newUserPasswordConfirmationField, 8);
        cyderPartitionedLayout.addComponent(new JLabel(), 5);
        cyderPartitionedLayout.addComponent(informationLabel, 5);
        cyderPartitionedLayout.addComponent(new JLabel(), 5);
        cyderPartitionedLayout.addComponent(chooseBackgroundButton, 10);
        cyderPartitionedLayout.addComponent(createNewUserButton, 10);

        createUserFrame.setCyderLayout(cyderPartitionedLayout);
        createUserFrame.finalizeAndShow();
        newUserNameField.requestFocus();

        updateInformationLabel();
    }

    /**
     * The key listener for the create user button.
     */
    private static final KeyListener newUserNameFieldListener = new KeyListener() {
        @Override
        public void keyPressed(java.awt.event.KeyEvent e) {
            createNewUserButton.setText("Create " + newUserNameField.getText().trim());
            updateInformationLabel();
        }

        @Override
        public void keyReleased(java.awt.event.KeyEvent e) {
            createNewUserButton.setText("Create " + newUserNameField.getText().trim());
            updateInformationLabel();
        }

        @Override
        public void keyTyped(java.awt.event.KeyEvent e) {
            createNewUserButton.setText("Create " + newUserNameField.getText().trim());
            updateInformationLabel();
        }
    };

    /**
     * Whether the currently entered new user credentials are valid
     * and can be used for construction of a new user.
     */
    private static boolean validCredentails = false;

    // Error messages
    private static final String NO_USERNAME = "No username";
    private static final String INVALID_NAME = "Invalid name";
    private static final String NAME_IN_USE = "Username already in use";

    private static final String NO_PASSWORD = "No password";
    private static final String NO_CONFIRMATION = "No confirmation password";
    private static final String PASSWORDS_DO_NOT_MATCH = "Passwords do not match";
    private static final String NO_LETTER = "Password needs a letter";
    private static final String INVALID_LENGTH = "Password is not > 4";
    private static final String NO_NUMBER = "Password needs a number";

    private static final String VALID = "Valid details";

    /**
     * Updates the information label based off of the current field values.
     */
    private static void updateInformationLabel() {
        informationLabel.setForeground(CyderColors.regularRed);
        validCredentails = false;

        String name = newUserNameField.getText().trim();
        char[] password = newUserPasswordField.getPassword();
        char[] passwordConfirmation = newUserPasswordConfirmationField.getPassword();

        if (name.isEmpty()) {
            informationLabel.setText(NO_USERNAME);
        } else if (!StringUtil.parseNonAscii(name).equals(name)) {
            informationLabel.setText(INVALID_NAME);
        } else if (usernameInUse(name)) {
            informationLabel.setText(NAME_IN_USE);
        } else if (password.length == 0) {
            informationLabel.setText(NO_PASSWORD);
        } else if (passwordConfirmation.length == 0) {
            informationLabel.setText(NO_CONFIRMATION);
        } else if (!Arrays.equals(password, passwordConfirmation)) {
            informationLabel.setText(PASSWORDS_DO_NOT_MATCH);
        } else if (password.length < 4) {
            informationLabel.setText(INVALID_LENGTH);
        } else if (!StringUtil.containsLetter(password)) {
            informationLabel.setText(NO_LETTER);
        } else if (!StringUtil.containsNumber(password)) {
            informationLabel.setText(NO_NUMBER);
        } else {
            informationLabel.setText(VALID);
            informationLabel.setForeground(CyderColors.regularGreen);

            validCredentails = true;
        }

        Arrays.fill(password, '\0');
        Arrays.fill(passwordConfirmation, '\0');
    }

    /**
     * The key listener for password fields to update the information label.
     */
    private static final KeyListener passwordFieldKeyListener = new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
            updateInformationLabel();
        }

        @Override
        public void keyReleased(KeyEvent e) {
            updateInformationLabel();
        }

        @Override
        public void keyTyped(KeyEvent e) {
            updateInformationLabel();
        }
    };

    /**
     * The last generated uuid so that in the event of an account creation
     * failure, we can delete the created user file.
     */
    private static String lastGeneratedUuid;

    /**
     * The action user for the create user button.
     */
    private static final ActionListener createNewUserActionListener = e -> {
        try {
            String name = newUserNameField.getText().trim();
            char[] password = newUserPasswordField.getPassword();

            if (!attemptToCreateUser(name, password)) {
                if (lastGeneratedUuid != null) {
                    OSUtil.deleteFile(OSUtil.buildFile(
                            Dynamic.PATH, Dynamic.USERS.getDirectoryName(), lastGeneratedUuid));
                }
            } else {
                createUserFrame.dispose();

                InformHandler.inform(new InformHandler.Builder("The new user \"" + name
                        + "\" has been created successfully.").setTitle("User Created")
                        .setRelativeTo(CyderFrame.getDominantFrame()));

                if (onlyOneUser()) {
                    LoginHandler.getLoginFrame().dispose();
                    LoginHandler.recognize(name, SecurityUtil.toHexString(SecurityUtil.getSha256(password)), false);
                }
            }

            Arrays.fill(password, '\0');
        } catch (Exception ex) {
            ExceptionHandler.silentHandle(ex);
        }
    };

    /**
     * Returns whether only one valid user exists within Cyder.
     *
     * @return whether only one valid user exists within Cyder
     */
    private static boolean onlyOneUser() {
        File[] userFiles = OSUtil.buildFile(
                Dynamic.PATH, Dynamic.USERS.getDirectoryName()).listFiles();

        return userFiles != null && userFiles.length == 1;
    }

    /**
     * The action listener for the choose background button.
     */
    private static final ActionListener chooseBackgroundButtonActionListener = e -> chooseBackground();

    /**
     * The mouse listener for changing the text of the choose background button.
     */
    private static final MouseAdapter chooseBackgroundButtonMouseListener = new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
            try {
                if (newUserBackgroundFile != null) {
                    chooseBackgroundButton.setText(newUserBackgroundFile.getName());
                } else {
                    chooseBackgroundButton.setText("No Background");
                }
            } catch (Exception ex) {
                ExceptionHandler.handle(ex);
            }
        }

        @Override
        public void mouseExited(MouseEvent e) {
            try {
                if (newUserBackgroundFile != null) {
                    chooseBackgroundButton.setText(newUserBackgroundFile.getName());
                } else {
                    chooseBackgroundButton.setText("Choose Background");
                }
            } catch (Exception ex) {
                ExceptionHandler.handle(ex);
            }
        }
    };

    /**
     * Returns whether the user with the user name of {@link  OSUtil#getOsUsername()} exists.
     *
     * @return whether the user with the user name of {@link  OSUtil#getOsUsername()} exists
     */
    private static boolean defaultCyderUserAlreadyExists() {
        String osUsername = OSUtil.getOsUsername();

        for (File userJson : UserUtil.getUserJsons()) {
            User user = UserUtil.extractUser(userJson);
            if (user.getName().equalsIgnoreCase(osUsername)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Initializes the new user's background.
     */
    private static void chooseBackground() {
        CyderThreadRunner.submit(() -> {
            try {
                File temp = GetterUtil.getInstance()
                        .getFile(new GetterUtil.Builder("Choose new user's background file")
                                .setRelativeTo(CyderFrame.getDominantFrame()));
                if (temp != null) {
                    newUserBackgroundFile = temp;
                    chooseBackgroundButton.setText(newUserBackgroundFile.getName());
                }

                if (temp == null || !FileUtil.isSupportedImageExtension(temp)) {
                    newUserBackgroundFile = null;
                }
            } catch (Exception ex) {
                ExceptionHandler.handle(ex);
            }
        }, "wait thread for GetterUtil().getFile()");
    }

    /**
     * Closes the widget if open.
     */
    public static void closeIfOpen() {
        if (createUserFrame != null) {
            createUserFrame.dispose();
        }
    }

    /**
     * Returns whether the provided username is already in use.
     *
     * @param username the username to determine if in use
     * @return whether the provided username is already in use
     */
    private static boolean usernameInUse(String username) {
        Preconditions.checkNotNull(username);
        Preconditions.checkArgument(!username.isEmpty());

        if (UserUtil.getUserCount() == 0) {
            return false;
        }

        for (File userFile : UserUtil.getUserJsons()) {
            if (UserUtil.extractUser(userFile).getName().equalsIgnoreCase(username)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Creates the default screen stat object based on the current monitor and the provided default background.
     *
     * @param background the default background the user will be using
     * @return the default screen stat
     */
    private static ScreenStat createDefaultScreenStat(BufferedImage background) {
        int monitorNum = -1;
        int w = background.getWidth();
        int h = background.getHeight();
        int x;
        int y;

        int screenWidth = ScreenUtil.getScreenWidth();
        int screenHeight = ScreenUtil.getScreenHeight();

        if (createUserFrame != null) {
            GraphicsConfiguration gc = createUserFrame.getGraphicsConfiguration();
            String monitorID = gc.getDevice().getIDstring().replaceAll("[^0-9]", "");

            try {
                monitorNum = Integer.parseInt(monitorID);
                int monitorWidth = (int) gc.getBounds().getWidth();
                int monitorHeight = (int) gc.getBounds().getHeight();
                int monitorX = (int) gc.getBounds().getX();
                int monitorY = (int) gc.getBounds().getY();

                x = monitorX + (monitorWidth - w) / 2;
                y = monitorY + (monitorHeight - h) / 2;
            } catch (Exception e) {
                x = (screenWidth - w) / 2;
                y = (screenHeight - h) / 2;
            }
        } else {
            x = (screenWidth - w) / 2;
            y = (screenHeight - h) / 2;
        }

        return new ScreenStat(x, y, w, h, monitorNum, false, Direction.TOP);
    }

    /**
     * Attempts to create a user based off of the provided necessary initial data.
     *
     * @param name     the requested name of the new user
     * @param password the password of the new user
     * @return whether the user was created
     */
    private static boolean attemptToCreateUser(String name, char[] password) {
        if (!validCredentails) {
            createUserFrame.toast(informationLabel.getText());
            return false;
        }

        String uuid = SecurityUtil.generateUuidForUser();
        lastGeneratedUuid = uuid;

        if (!OSUtil.buildFile(Dynamic.PATH, Dynamic.USERS.getDirectoryName(), uuid).mkdir()) {
            createUserFrame.toast("Failed to create user folder");
            return false;
        }

        for (UserFile userFile : UserFile.values()) {
            File makeMe = OSUtil.buildFile(Dynamic.PATH, Dynamic.USERS.getDirectoryName(), uuid, userFile.getName());

            if (userFile.isFile()) {
                try {
                    if (!makeMe.createNewFile()) {
                        createUserFrame.toast("Failed to create file: " + userFile.getName());
                        return false;
                    }
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                    createUserFrame.toast("Failed to create file");
                    return false;
                }
            } else {
                if (!makeMe.mkdir()) {
                    createUserFrame.toast("Failed to create folder: " + userFile.getName());
                    return false;
                }
            }
        }

        if (newUserBackgroundFile == null) {
            newUserBackgroundFile = UserUtil.createDefaultBackground(uuid);
        }

        try {
            File destination = OSUtil.buildFile(Dynamic.PATH, Dynamic.USERS.getDirectoryName(),
                    uuid, UserFile.BACKGROUNDS.getName(), newUserBackgroundFile.getName());
            Files.copy(Paths.get(newUserBackgroundFile.getAbsolutePath()), destination.toPath());
        } catch (Exception e) {
            ExceptionHandler.handle(e);
            createUserFrame.toast("Failed to create default background");
            return false;
        }

        User user = new User();
        user.setName(name);
        user.setPass(SecurityUtil.doubleHashToHex(password));

        // default preferences
        for (Preference pref : Preferences.getPreferences()) {
            // as per convention, IGNORE for tooltip means ignore when creating user
            // whilst IGNORE for default value means ignore for edit user
            if (!pref.getTooltip().equals("IGNORE")) {
                for (Method m : user.getClass().getMethods()) {
                    if (m.getName().startsWith("set")
                            && m.getParameterTypes().length == 1
                            && m.getName().replace("set", "").equalsIgnoreCase(pref.getID())) {
                        try {
                            m.invoke(user, pref.getDefaultValue());
                        } catch (Exception e) {
                            ExceptionHandler.handle(e);
                        }

                        break;
                    }
                }
            }
        }

        BufferedImage background;
        try {
            background = ImageIO.read(newUserBackgroundFile);
        } catch (Exception e) {
            background = ImageUtil.toBufferedImage(ImageUtil.imageIconFromColor(Color.black, 800, 800));
        }

        user.setScreenStat(createDefaultScreenStat(background));
        user.setExecutables(new LinkedList<>());

        UserUtil.setUserData(OSUtil.buildFile(Dynamic.PATH,
                Dynamic.USERS.getDirectoryName(), uuid, UserFile.USERDATA.getName()), user);

        return true;
    }
}
