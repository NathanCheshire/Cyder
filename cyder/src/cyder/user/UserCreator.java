package cyder.user;

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
import cyder.ui.*;
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
     * Suppress default constructor.
     */
    private UserCreator() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    // todo at some point the passwords match label should just be a general
    //  information label for if the username of password is invalid

    @Widget(triggers = {"create user", "create"}, description = "A widget for creating new users")
    public static void showGui() {
        if (createUserFrame != null) {
            createUserFrame.dispose();
        }

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

        newUserNameField.setBorder(new LineBorder(CyderColors.navy, 5, false));
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
        newUserPasswordField.addKeyListener(newUserNamePasswordFieldKeyListener);
        newUserPasswordField.setBorder(new LineBorder(CyderColors.navy, 5, false));
        newUserPasswordField.setSize(240, 40);
        newUserPasswordField.setCaret(new CyderCaret(CyderColors.navy));

        JLabel passwordLabelConf = new JLabel("Confirm Password: ", SwingConstants.CENTER);
        passwordLabelConf.setFont(CyderFonts.SEGOE_20);
        passwordLabelConf.setForeground(CyderColors.navy);
        passwordLabelConf.setSize(240, 30);

        newUserPasswordConfirmationField = new CyderPasswordField();
        newUserPasswordConfirmationField.addKeyListener(newUserPasswordConfirmationKeyListener);
        newUserPasswordConfirmationField.setSize(240, 40);

        informationLabel.setFont(CyderFonts.SEGOE_20);
        informationLabel.setForeground(CyderColors.regularGreen);
        informationLabel.setSize(300, 30);

        chooseBackgroundButton = new CyderButton("Choose background");
        chooseBackgroundButton.setToolTipText("Choose a background for the console");
        chooseBackgroundButton.setFont(CyderFonts.SEGOE_20);
        chooseBackgroundButton.setBackground(CyderColors.regularRed);
        chooseBackgroundButton.addMouseListener(chooseBackgroundButtonMouseListener);

        chooseBackgroundButton.setBorder(new LineBorder(CyderColors.navy, 5, false));
        chooseBackgroundButton.setSize(240, 40);

        createNewUserButton.setFont(CyderFonts.SEGOE_20);
        createNewUserButton.setBackground(CyderColors.regularRed);
        createNewUserButton.setToolTipText("Create");
        createNewUserButton.addMouseListener(createNewUserButtonActionListener);
        createNewUserButton.setBorder(new LineBorder(CyderColors.navy, 5, false));
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
    }

    /**
     * The key listener for the create user button.
     */
    private static final KeyListener newUserNameFieldListener = new KeyListener() {
        @Override
        public void keyPressed(java.awt.event.KeyEvent e) {
            createNewUserButton.setText("Create \"" + newUserNameField.getText().trim() + "\"");
        }

        @Override
        public void keyReleased(java.awt.event.KeyEvent e) {
            createNewUserButton.setText("Create \"" + newUserNameField.getText().trim() + "\"");
        }

        @Override
        public void keyTyped(java.awt.event.KeyEvent e) {
            createNewUserButton.setText("Create \"" + newUserNameField.getText().trim() + "\"");
        }
    };

    private static final KeyListener newUserNamePasswordFieldKeyListener = new KeyAdapter() {
        @Override
        public void keyReleased(KeyEvent e) {
            if (Arrays.equals(newUserPasswordField.getPassword(), newUserPasswordConfirmationField.getPassword())) {
                informationLabel.setText("Passwords match");
                informationLabel.setForeground(CyderColors.regularGreen);
            } else {
                informationLabel.setText("Passwords do not match");
                informationLabel.setForeground(CyderColors.regularRed);
            }
        }
    };

    private static final KeyListener newUserPasswordConfirmationKeyListener = new KeyAdapter() {
        @Override
        public void keyReleased(KeyEvent e) {
            if (Arrays.equals(newUserPasswordField.getPassword(), newUserPasswordConfirmationField.getPassword())) {
                informationLabel.setText("Passwords match");
                informationLabel.setForeground(CyderColors.regularGreen);
            } else {
                informationLabel.setText("Passwords do not match");
                informationLabel.setForeground(CyderColors.regularRed);
            }
        }
    };

    private static final MouseAdapter createNewUserButtonActionListener = new MouseAdapter() {
        @Override
        public void mouseReleased(MouseEvent e) {
            try {
                if (!createUser(newUserNameField.getText(), newUserPasswordField.getPassword(),
                        newUserPasswordConfirmationField.getPassword())) {
                    createUserFrame.notify("Failed to create user");

                    if (lastGeneratedUUID != null) {
                        File deleteMe = OSUtil.buildFile(Dynamic.PATH,
                                Dynamic.USERS.getDirectoryName(), lastGeneratedUUID);

                        OSUtil.deleteFile(deleteMe);
                    }
                } else {
                    createUserFrame.dispose();

                    InformHandler.inform(new InformHandler.Builder("The new user \""
                            + newUserNameField.getText().trim()
                            + "\" has been created successfully.").setTitle("Creation Success")
                            .setRelativeTo(CyderFrame.getDominantFrame()));

                    File[] userFiles = OSUtil.buildFile(Dynamic.PATH,
                            Dynamic.USERS.getDirectoryName()).listFiles();

                    // attempt to log in new user if it's the only user
                    if (userFiles != null && userFiles.length == 1) {
                        LoginHandler.getLoginFrame().dispose();
                        LoginHandler.recognize(newUserNameField.getText().trim(),
                                SecurityUtil.toHexString(SecurityUtil.getSha256(
                                        newUserPasswordField.getPassword())), false);
                    }
                }
            } catch (Exception ex) {
                ExceptionHandler.silentHandle(ex);
            }
        }
    };

    private static final MouseAdapter chooseBackgroundButtonMouseListener = new MouseAdapter() {
        @Override
        public void mouseReleased(MouseEvent e) {
            try {
                chooseBackground();
            } catch (Exception exc) {
                ExceptionHandler.handle(exc);
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            try {
                if (newUserBackgroundFile != null) {
                    chooseBackgroundButton.setText(newUserBackgroundFile.getName());
                } else {
                    chooseBackgroundButton.setText("No File Chosen");
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
     * The last generated UUID.
     */
    private static String lastGeneratedUUID;

    /**
     * Initializes the new user's background.
     */
    private static void chooseBackground() {
        CyderThreadRunner.submit(() -> {
            try {
                File temp = GetterUtil.getInstance().getFile(
                        new GetterUtil.Builder("Choose new user's background file")
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
     * Closes the createUserFrame if open.
     */
    public static void close() {
        if (createUserFrame != null) {
            createUserFrame.dispose();
        }
    }

    /**
     * Attempts to create a user based off of the provided necessary initial data.
     *
     * @param name         the requested name of the new user
     * @param password     the password of the new user
     * @param passwordConf the password confirmation of the new user
     * @return whether the user was created
     */
    public static boolean createUser(String name, char[] password, char[] passwordConf) {
        // validate data for basic correctness
        if (StringUtil.isNull(name)) {
            return false;
        }

        if (password == null || passwordConf == null) {
            return false;
        }

        if (!Arrays.equals(password, passwordConf)) {
            createUserFrame.notify("Passwords are not equal");
            newUserPasswordField.setText("");
            newUserPasswordConfirmationField.setText("");
            return false;
        }

        if (password.length < 5 || passwordConf.length < 5) {
            createUserFrame.notify("Password length must be at least 5 characters");
            newUserPasswordField.setText("");
            newUserPasswordConfirmationField.setText("");
            return false;
        }

        boolean alphabet = false;
        boolean number = false;

        for (char c : password) {
            if (Character.isDigit(c)) {
                number = true;
            } else if (Character.isAlphabetic(c)) {
                alphabet = true;
            }

            if (number && alphabet) {
                break;
            }
        }

        if (!number || !alphabet) {
            createUserFrame.notify("Password must contain at least one number," +
                    " one letter, and be 5 characters long");
            newUserPasswordField.setText("");
            newUserPasswordConfirmationField.setText("");
            return false;
        }

        // generate the user uuid and ensure it is unique
        String uuid = SecurityUtil.generateUuid();
        File folder = OSUtil.buildFile(Dynamic.PATH,
                Dynamic.USERS.getDirectoryName(), uuid);

        while (folder.exists()) {
            uuid = SecurityUtil.generateUuid();
            folder = OSUtil.buildFile(Dynamic.PATH,
                    Dynamic.USERS.getDirectoryName(), uuid);
        }

        // set the uuid so that we can delete the folder if something fails later
        lastGeneratedUUID = uuid;

        // ensure that the username doesn't already exist
        boolean userNameExists = false;

        File[] files = folder.getParentFile().listFiles();

        if (files == null || files.length == 0)
            return false;

        for (File f : files) {
            File jsonFile = new File(OSUtil.buildPath(f.getAbsolutePath(), UserFile.USERDATA.getName()));

            // user files might remain without a user json
            if (!jsonFile.exists())
                continue;

            // just to be safe
            if (!f.isDirectory())
                continue;
            try {
                if (UserUtil.extractUser(f).getName().equalsIgnoreCase(newUserNameField.getText().trim())) {
                    userNameExists = true;
                    break;
                }
            } catch (Exception ignored) {
            }
        }

        if (userNameExists) {
            createUserFrame.inform("Sorry, but that username is already in use. "
                    + "Please choose a different one.", "");
            newUserNameField.setText("");
            return false;
        }

        // create the user folder
        File userFolder = OSUtil.buildFile(Dynamic.PATH,
                Dynamic.USERS.getDirectoryName(), uuid);

        if (!userFolder.mkdir()) {
            return false;
        }

        // create the default user files
        for (UserFile f : UserFile.values()) {
            File makeMe = OSUtil.buildFile(Dynamic.PATH,
                    Dynamic.USERS.getDirectoryName(), uuid, f.getName());

            if (f.isFile()) {
                try {
                    if (!makeMe.createNewFile())
                        return false;
                } catch (Exception e) {
                    ExceptionHandler.handle(e);
                    return false;
                }
            } else {
                if (!makeMe.mkdir()) {
                    return false;
                }
            }
        }

        if (newUserBackgroundFile == null) {
            newUserBackgroundFile = UserUtil.createDefaultBackground(uuid);
        }

        // create the user background in the directory
        try {
            File destination = OSUtil.buildFile(Dynamic.PATH,
                    Dynamic.USERS.getDirectoryName(),
                    uuid, UserFile.BACKGROUNDS.getName(), newUserBackgroundFile.getName());
            Files.copy(Paths.get(newUserBackgroundFile.getAbsolutePath()), destination.toPath());
        } catch (Exception e) {
            ExceptionHandler.handle(e);
            return false;
        }

        // build the user
        User user = new User();

        //name and password
        user.setName(newUserNameField.getText().trim());
        user.setPass(SecurityUtil.toHexString(SecurityUtil.getSha256(
                SecurityUtil.toHexString(SecurityUtil.getSha256(password)).toCharArray())));

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

        // screen stat initializing
        try {
            background = ImageIO.read(newUserBackgroundFile);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
            return false;
        }

        int monitorNum = -1;
        int x;
        int y;

        // figure out the monitor we should be using for the user's screen stats
        if (createUserFrame != null) {
            GraphicsConfiguration gc = createUserFrame.getGraphicsConfiguration();
            String monitorID = gc.getDevice().getIDstring().replaceAll("[^0-9]", "");

            try {
                monitorNum = Integer.parseInt(monitorID);
                int monitorWidth = (int) gc.getBounds().getWidth();
                int monitorHeight = (int) gc.getBounds().getHeight();
                int monitorX = (int) gc.getBounds().getX();
                int monitorY = (int) gc.getBounds().getY();

                x = monitorX + (monitorWidth - background.getWidth()) / 2;
                y = monitorY + (monitorHeight - background.getHeight()) / 2;
            } catch (Exception e) {
                ExceptionHandler.handle(e);

                // error so default the screen stats
                x = (ScreenUtil.getScreenWidth() - background.getWidth()) / 2;
                y = (ScreenUtil.getScreenHeight() - background.getHeight()) / 2;
            }
        } else {
            x = (ScreenUtil.getScreenWidth() - background.getWidth()) / 2;
            y = (ScreenUtil.getScreenHeight() - background.getHeight()) / 2;
        }

        user.setScreenStat(new ScreenStat(x, y, background.getWidth(),
                background.getHeight(), monitorNum, false, Direction.TOP));
        user.setExecutables(new LinkedList<>());

        UserUtil.setUserData(OSUtil.buildFile(
                Dynamic.PATH,
                Dynamic.USERS.getDirectoryName(),
                uuid, UserFile.USERDATA.getName()), user);

        Arrays.fill(password, '\0');
        Arrays.fill(passwordConf, '\0');

        return true;
    }
}
