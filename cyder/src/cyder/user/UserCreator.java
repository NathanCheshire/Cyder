package cyder.user;

import cyder.annotations.Widget;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderIcons;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.genesis.CyderShare;
import cyder.handlers.internal.ExceptionHandler;
import cyder.handlers.internal.InformHandler;
import cyder.handlers.internal.LoginHandler;
import cyder.handlers.internal.objects.InformBuilder;
import cyder.threads.CyderThreadRunner;
import cyder.ui.*;
import cyder.user.objects.Preference;
import cyder.user.objects.ScreenStat;
import cyder.utilities.*;
import cyder.utilities.objects.GetterBuilder;

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

public class UserCreator {
    /**
     * The user creator frame.
     */
    private static CyderFrame createUserFrame;

    /**
     * The password field to confirm the new user's password.
     */
    private static CyderPasswordField newUserPasswordConf;

    /**
     * The password field for the user's password.
     */
    private static CyderPasswordField newUserPassword;

    /**
     * The field for the new user's name.
     */
    private static CyderTextField newUserName;


    private static File createUserBackground;

    /**
     * No instances of user creator allowed.
     */
    private UserCreator() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    @Widget(triggers = {"createuser", "create"}, description = "A user creating widget")
    public static void showGUI() {
        if (createUserFrame != null)
            createUserFrame.dispose();
        createUserBackground = null;

        createUserFrame = new CyderFrame(356, 473, CyderIcons.defaultBackground);
        createUserFrame.setTitle("Create User");


        JLabel nameLabel = new JLabel("Username: ", SwingConstants.CENTER);
        nameLabel.setFont(CyderFonts.segoe20);
        nameLabel.setForeground(CyderColors.navy);
        nameLabel.setBounds(120, 30, 121, 30);
        createUserFrame.getContentPane().add(nameLabel);

        //initialize here since we need to update its tooltip
        CyderButton createNewUser = new CyderButton("Create User");

        newUserName = new CyderTextField(0);
        newUserName.setHorizontalAlignment(JTextField.CENTER);
        newUserName.setBackground(Color.white);
        newUserName.setFont(CyderFonts.segoe20);
        newUserName.setBorder(new LineBorder(new Color(0, 0, 0)));
        newUserName.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                createNewUser.setToolTipText("Finalize the user \"" + newUserName.getText() + "\"");
            }

            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                createNewUser.setToolTipText("Finalize the user \"" + newUserName.getText() + "\"");
            }

            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
                createNewUser.setToolTipText("Finalize the user \"" + newUserName.getText() + "\"");
            }
        });

        newUserName.setBorder(new LineBorder(CyderColors.navy, 5, false));
        newUserName.setBounds(60, 70, 240, 40);
        createUserFrame.getContentPane().add(newUserName);

        // set with windows username if not already used
        String osUserName = OSUtil.getSystemUsername();
        boolean exists = false;

        for (File userJson : UserUtil.getUserJsons()) {
            User u = UserUtil.extractUser(userJson);
            if (u.getName().equalsIgnoreCase(osUserName)) {
                exists = true;
                break;
            }
        }

        if (!exists) {
            newUserName.setText(osUserName);
        }

        JLabel passwordLabel = new JLabel("Password: ", SwingConstants.CENTER);
        passwordLabel.setFont(CyderFonts.segoe20);
        passwordLabel.setForeground(CyderColors.navy);
        passwordLabel.setBounds(60, 120, 240, 30);
        createUserFrame.getContentPane().add(passwordLabel);

        //initialize here since we need to update it for both fields
        JLabel matchPasswords = new JLabel("Passwords match", SwingConstants.CENTER);

        newUserPassword = new CyderPasswordField();
        newUserPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (Arrays.equals(newUserPassword.getPassword(), newUserPasswordConf.getPassword())) {
                    matchPasswords.setText("Passwords match");
                    matchPasswords.setForeground(CyderColors.regularGreen);
                } else {
                    matchPasswords.setText("Passwords do not match");
                    matchPasswords.setForeground(CyderColors.regularRed);
                }
            }
        });
        newUserPassword.setBorder(new LineBorder(CyderColors.navy, 5, false));
        newUserPassword.setBounds(60, 160, 240, 40);
        newUserPassword.setCaret(new CyderCaret(CyderColors.navy));
        createUserFrame.getContentPane().add(newUserPassword);

        JLabel passwordLabelConf = new JLabel("Confirm Password: ", SwingConstants.CENTER);
        passwordLabelConf.setFont(CyderFonts.segoe20);
        passwordLabelConf.setForeground(CyderColors.navy);
        passwordLabelConf.setBounds(60, 210, 240, 30);
        createUserFrame.getContentPane().add(passwordLabelConf);

        newUserPasswordConf = new CyderPasswordField();
        newUserPasswordConf.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (Arrays.equals(newUserPassword.getPassword(), newUserPasswordConf.getPassword())) {
                    matchPasswords.setText("Passwords match");
                    matchPasswords.setForeground(CyderColors.regularGreen);
                } else {
                    matchPasswords.setText("Passwords do not match");
                    matchPasswords.setForeground(CyderColors.regularRed);
                }
            }
        });
        newUserPasswordConf.setBounds(60, 250, 240, 40);
        createUserFrame.getContentPane().add(newUserPasswordConf);

        matchPasswords.setFont(CyderFonts.segoe20);
        matchPasswords.setForeground(CyderColors.regularGreen);
        matchPasswords.setBounds(32, 300, 300, 30);
        createUserFrame.getContentPane().add(matchPasswords);

        CyderButton chooseBackground = new CyderButton("Choose background");
        chooseBackground.setToolTipText("ClickMe me to choose a background");
        chooseBackground.setFont(CyderFonts.segoe20);
        chooseBackground.setBackground(CyderColors.regularRed);
        chooseBackground.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                try {
                    chooseBackground(chooseBackground);
                } catch (Exception exc) {
                    ExceptionHandler.handle(exc);
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                try {
                    if (createUserBackground != null) {
                        chooseBackground.setText(createUserBackground.getName());
                    } else {
                        chooseBackground.setText("No File Chosen");
                    }
                } catch (Exception ex) {
                    ExceptionHandler.handle(ex);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                try {
                    if (createUserBackground != null) {
                        chooseBackground.setText(createUserBackground.getName());
                    } else {
                        chooseBackground.setText("Choose Background");
                    }
                } catch (Exception ex) {
                    ExceptionHandler.handle(ex);
                }
            }
        });

        chooseBackground.setBorder(new LineBorder(CyderColors.navy, 5, false));
        chooseBackground.setBounds(60, 340, 240, 40);
        createUserFrame.getContentPane().add(chooseBackground);

        createNewUser.setFont(CyderFonts.segoe20);
        createNewUser.setBackground(CyderColors.regularRed);
        createNewUser.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                try {
                    if (!createUser(newUserName.getText(), newUserPassword.getPassword(),
                            newUserPasswordConf.getPassword())) {
                        createUserFrame.notify("Failed to create user");

                        if (lastGeneratedUUID != null) {
                            File deleteMe = new File(OSUtil.buildPath("dynamic","users",lastGeneratedUUID));

                            if (deleteMe.exists() && !deleteMe.delete()) {
                                throw new RuntimeException("Failed to delete failed user creation folder");
                            }
                        }
                    } else {
                        createUserFrame.dispose();

                        InformBuilder builder = new InformBuilder("The new user \"" + newUserName.getText().trim()
                                + "\" has been created successfully.");
                        builder.setTitle("Creation Success");
                        builder.setRelativeTo(CyderShare.getDominantFrame());
                        InformHandler.inform(builder);

                        //attempt to log in new user if it's the only user
                        if (new File(OSUtil.buildPath("dynamic","users")).listFiles().length == 1) {
                            LoginHandler.getLoginFrame().dispose();
                            LoginHandler.recognize(newUserName.getText().trim(),
                                    SecurityUtil.toHexString(SecurityUtil.getSHA256(
                                            newUserPassword.getPassword())), false);
                        }
                    }
                } catch (Exception ex) {
                    ExceptionHandler.silentHandle(ex);
                }
            }
        });

        createNewUser.setBorder(new LineBorder(CyderColors.navy, 5, false));
        createNewUser.setFont(CyderFonts.segoe20);
        createNewUser.setBounds(60, 390, 240, 40);
        createUserFrame.getContentPane().add(createNewUser);

        createUserFrame.finalizeAndShow();
        newUserName.requestFocus();
    }

    /**
     * The last generated UUID.
     */
    private static String lastGeneratedUUID;

    /**
     * Initializes the new user's background.
     *
     * @param referenceButton the button to set to the tooltip
     *                       of the chosen background if a valid one is chosen
     */
    private static void chooseBackground(CyderButton referenceButton) {
        CyderThreadRunner.submit(() -> {
            try {
                GetterBuilder builder = new GetterBuilder("Choose new user's background file");
                builder.setRelativeTo(CyderShare.getDominantFrame());
                File temp = GetterUtil.getInstance().getFile(builder);
                if (temp != null) {
                    createUserBackground = temp;
                    referenceButton.setText(createUserBackground.getName());
                }

                if (temp == null || !FileUtil.isSupportedImageExtension(temp)) {
                    createUserBackground = null;
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
        if (createUserFrame != null)
            createUserFrame.dispose();
    }

    /**
     * Attempts to create a user based off of the provided necessary initial data.
     *
     * @param name the requested name of the new user
     * @param password the password of the new user
     * @param passwordConf the password confirmation of the new user
     * @return whether the user was created
     */
    public static boolean createUser(String name, char[] password, char[] passwordConf) {
        //validate data for basic correctness
        if (StringUtil.isNull(name) ) {
            return false;
        }

        if (password == null || passwordConf == null) {
            return false;
        }

        if (!Arrays.equals(password, passwordConf)) {
            createUserFrame.notify("Passwords are not equal");
            newUserPassword.setText("");
            newUserPasswordConf.setText("");
            return false;
        }

        if (password.length < 5 || passwordConf.length < 5) {
            createUserFrame.notify("Password length must be at least 5 characters");
            newUserPassword.setText("");
            newUserPasswordConf.setText("");
            return false;
        }

        boolean alphabet = false;
        boolean number = false;

        for (char c : password) {
            if (Character.isDigit(c))
                number = true;
            else if (Character.isAlphabetic(c))
                alphabet = true;

            if (number && alphabet)
                break;
        }

        if (!number || !alphabet) {
            createUserFrame.notify("Password must contain at least one number," +
                    " one letter, and be 5 characters long");
            newUserPassword.setText("");
            newUserPasswordConf.setText("");
            return false;
        }

        //generate the user uuid and ensure it is unique
        String uuid = SecurityUtil.generateUUID();
        File folder = new File(OSUtil.buildPath("dynamic", "users",uuid));

        while (folder.exists()) {
            uuid = SecurityUtil.generateUUID();
            folder = new File(OSUtil.buildPath("dynamic", "users",uuid));
        }

        //set the uuid so that we can delete the folder if something fails later
        lastGeneratedUUID = uuid;

        //ensure that the username doesn't already exist
        boolean userNameExists = false;

        for (File f : folder.getParentFile().listFiles()) {
            File jsonFile = new File(OSUtil.buildPath(f.getAbsolutePath(), UserFile.USERDATA.getName()));

            // user files might remain without a user json
            if (!jsonFile.exists())
                continue;

            // just to be safe
            if (!f.isDirectory())
                continue;
            try {
                if (UserUtil.extractUser(f).getName().equalsIgnoreCase(newUserName.getText().trim())) {
                    userNameExists = true;
                    break;
                }
            } catch (Exception ignored) {}
        }

        if (userNameExists) {
            createUserFrame.inform("Sorry, but that username is already in use. " +
                    "Please choose a different one.", "");
            newUserName.setText("");
            return false;
        }

        //create the user folder
        File userFolder = new File(OSUtil.buildPath("dynamic","users",uuid));

        if (!userFolder.mkdir())
            return false;

        //create the default user files
        for (UserFile f : UserFile.values()) {
            File makeMe = new File(OSUtil.buildPath("dynamic","users",uuid,f.getName()));

            if (f.isFile()) {
                try {
                   if (!makeMe.createNewFile())
                       return false;
               } catch (Exception e) {
                   ExceptionHandler.handle(e);
                   return false;
               }
            } else {
                if (!makeMe.mkdir())
                    return false;
            }
        }

        if (createUserBackground == null) {
            createUserBackground = UserUtil.createDefaultBackground(uuid);
        }

        // create the user background in the directory
        try {
            File destination = new File(OSUtil.buildPath("dynamic", "users", uuid,
                    UserFile.BACKGROUNDS.getName(), createUserBackground.getName()));
            Files.copy(Paths.get(createUserBackground.getAbsolutePath()), destination.toPath());
        } catch (Exception e) {
            ExceptionHandler.handle(e);
            return false;
        }

        // build the user
        User user = new User();

        //name and password
        user.setName(newUserName.getText().trim());
        user.setPass(SecurityUtil.toHexString(SecurityUtil.getSHA256(
                SecurityUtil.toHexString(SecurityUtil.getSHA256(password)).toCharArray())));

        // default preferences
        for (Preference pref : Preferences.getPreferences()) {
            //as per convention, IGNORE for tooltip means ignore when creating user
            // whilst IGNORE for default value means ignore for edit user
            if (!pref.getTooltip().equals("IGNORE")) {
                for (Method m : user.getClass().getMethods()) {
                    if (m.getName().startsWith("set")
                            && m.getParameterTypes().length == 1
                            && m.getName().replace("set","").equalsIgnoreCase(pref.getID())) {
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

        //screen stat initializing
        try {
            background = ImageIO.read(createUserBackground);
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
            String monitorID = gc.getDevice().getIDstring().replaceAll("[^0-9]","");

            try {
                monitorNum = Integer.parseInt(monitorID);
                int monitorWidth = (int) gc.getBounds().getWidth();
                int monitorHeight = (int) gc.getBounds().getHeight();
                int monitorX = (int) gc.getBounds().getX();
                int monitorY = (int) gc.getBounds().getY();

                x = monitorX + (monitorWidth - background.getWidth()) / 2;
                y = monitorY + (monitorHeight - background.getHeight()) / 2;
            } catch (Exception e) {
                ExceptionHandler.silentHandle(e);

                //error so default the screen stats
                x = (ScreenUtil.getScreenWidth() - background.getWidth()) / 2;
                y = (ScreenUtil.getScreenHeight() - background.getHeight()) / 2;
            }
        }
        // otherwise, default monitor stats
        else {
            x = (ScreenUtil.getScreenWidth() - background.getWidth()) / 2;
            y = (ScreenUtil.getScreenHeight() - background.getHeight()) / 2;
        }

        user.setScreenStat(new ScreenStat(x, y, background.getWidth(),
                background.getHeight(), monitorNum, false));

        // executables
        user.setExecutables(new LinkedList<>());

        // write all data
        UserUtil.setUserData(new File(OSUtil.buildPath(
                "dynamic","users",uuid,UserFile.USERDATA.getName())), user);

        //password security
        Arrays.fill(password, '\0');
        Arrays.fill(passwordConf, '\0');

        return true;
    }
}
