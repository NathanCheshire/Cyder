package cyder.cyderuser;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.consts.CyderImages;
import cyder.consts.CyderStrings;
import cyder.genesis.GenesisShare;
import cyder.handlers.internal.LoginHandler;
import cyder.handlers.internal.ErrorHandler;
import cyder.genesis.GenesisShare.Preference;
import cyder.handlers.internal.PopupHandler;
import cyder.ui.*;
import cyder.utilities.GetterUtil;
import cyder.utilities.SecurityUtil;
import cyder.utilities.StringUtil;
import cyder.utilities.UserUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class UserCreator {
    private static CyderFrame createUserFrame;
    private static CyderPasswordField newUserPasswordconf;
    private static CyderPasswordField newUserPassword;
    private static CyderTextField newUserName;
    private static CyderButton createNewUser;
    private static CyderButton chooseBackground;
    private static File createUserBackground;

    private UserCreator() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    public static void showGUI() {
        createUserBackground = null;

        if (createUserFrame != null)
            createUserFrame.dispose();

        createUserFrame = new CyderFrame(356, 473, CyderImages.defaultBackground);
        createUserFrame.setTitle("Create User");

        JLabel NameLabel = new JLabel("Username: ", SwingConstants.CENTER);
        NameLabel.setFont(CyderFonts.weatherFontSmall);
        NameLabel.setBounds(120, 30, 121, 30);
        createUserFrame.getContentPane().add(NameLabel);

        newUserName = new CyderTextField(0);
        newUserName.setBackground(Color.white);
        newUserName.setFont(CyderFonts.weatherFontSmall);
        newUserName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                if (newUserName.getText().length() > 15) {
                    evt.consume();
                }
            }
        });

        newUserName.setBorder(new LineBorder(new Color(0, 0, 0)));
        newUserName.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                createNewUser.setToolTipText("Finalize the user \"" + newUserName.getText() + "\"");

                if (newUserName.getText().length() == 1) {
                    newUserName.setText(newUserName.getText().toUpperCase());
                }
            }

            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                createNewUser.setToolTipText("Finalize the user \"" + newUserName.getText() + "\"");

                if (newUserName.getText().length() == 1) {
                    newUserName.setText(newUserName.getText().toUpperCase());
                }
            }

            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
                createNewUser.setToolTipText("Finalize the user \"" + newUserName.getText() + "\"");

                if (newUserName.getText().length() == 1) {
                    newUserName.setText(newUserName.getText().toUpperCase());
                }
            }
        });

        newUserName.setBorder(new LineBorder(CyderColors.navy, 5, false));
        newUserName.setBounds(60, 70, 240, 40);
        createUserFrame.getContentPane().add(newUserName);

        JLabel passwordLabel = new JLabel("Password: ", SwingConstants.CENTER);
        passwordLabel.setFont(CyderFonts.weatherFontSmall);
        passwordLabel.setForeground(CyderColors.navy);
        passwordLabel.setBounds(60, 120, 240, 30);
        createUserFrame.getContentPane().add(passwordLabel);

        JLabel matchPasswords = new JLabel("Passwords match", SwingConstants.CENTER);

        newUserPassword = new CyderPasswordField();
        newUserPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (Arrays.equals(newUserPassword.getPassword(), newUserPasswordconf.getPassword())) {
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
        passwordLabelConf.setFont(CyderFonts.weatherFontSmall);
        passwordLabelConf.setForeground(CyderColors.navy);
        passwordLabelConf.setBounds(60, 210, 240, 30);
        createUserFrame.getContentPane().add(passwordLabelConf);

        newUserPasswordconf = new CyderPasswordField();
        newUserPasswordconf.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (Arrays.equals(newUserPassword.getPassword(), newUserPasswordconf.getPassword())) {
                    matchPasswords.setText("Passwords match");
                    matchPasswords.setForeground(CyderColors.regularGreen);
                } else {
                    matchPasswords.setText("Passwords do not match");
                    matchPasswords.setForeground(CyderColors.regularRed);
                }
            }
        });
        newUserPasswordconf.setBounds(60, 250, 240, 40);
        createUserFrame.getContentPane().add(newUserPasswordconf);

        matchPasswords.setFont(CyderFonts.weatherFontSmall);
        matchPasswords.setForeground(CyderColors.regularGreen);
        matchPasswords.setBounds(32, 300, 300, 30);
        createUserFrame.getContentPane().add(matchPasswords);

        chooseBackground = new CyderButton("Choose background");
        chooseBackground.setToolTipText("ClickMe me to choose a background");
        chooseBackground.setFont(CyderFonts.weatherFontSmall);
        chooseBackground.setBackground(CyderColors.regularRed);
        chooseBackground.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                try {
                    new Thread(() -> {
                        try {
                            File temp = new GetterUtil().getFile("Choose new user's background file");
                            if (temp != null) {
                                createUserBackground = temp;
                                chooseBackground.setText(createUserBackground.getName());
                            }

                            if (temp == null ||
                                    !Files.probeContentType(Paths.get(createUserBackground.getAbsolutePath())).endsWith("png")) {
                                createUserBackground = null;
                            }
                        } catch (Exception ex) {
                            ErrorHandler.handle(ex);
                        }
                    }, "wait thread for GetterUtil().getFile()").start();
                } catch (Exception exc) {
                    ErrorHandler.handle(exc);
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
                    ErrorHandler.handle(ex);
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
                    ErrorHandler.handle(ex);
                }
            }
        });

        chooseBackground.setBorder(new LineBorder(CyderColors.navy, 5, false));
        chooseBackground.setBounds(60, 340, 240, 40);
        createUserFrame.getContentPane().add(chooseBackground);

        createNewUser = new CyderButton("Create User");
        createNewUser.setFont(CyderFonts.weatherFontSmall);
        createNewUser.setBackground(CyderColors.regularRed);
        createNewUser.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                try {
                    String uuid = SecurityUtil.generateUUID();
                    File folder = new File("dynamic/users/" + uuid);

                    while (folder.exists()) {
                        uuid = SecurityUtil.generateUUID();
                        folder = new File("dynamic/users/" + uuid);
                    }

                    char[] pass = newUserPassword.getPassword();
                    char[] passconf = newUserPasswordconf.getPassword();

                    boolean userNameExists = false;

                    for (File f : folder.getParentFile().listFiles()) {
                        File jsonFile = new File(f.getAbsolutePath() + "/userdata.json");

                        if (!jsonFile.exists())
                            continue;

                        String currentName = UserUtil.extractUserData(f, "name");

                        if (currentName.equalsIgnoreCase(newUserName.getText())) {
                            userNameExists = true;
                            break;
                        }
                    }

                    if (userNameExists) {
                        createUserFrame.inform("Sorry, but that username is already in use." +
                                "\nPlease choose a different one.", "");
                        newUserName.setText("");
                        newUserPassword.setText("");
                        newUserPasswordconf.setText("");
                    } else if (StringUtil.empytStr(newUserName.getText()) || pass == null || passconf == null
                            || uuid.equals("") || pass.equals("") || passconf.equals("") || uuid.length() == 0) {
                        createUserFrame.inform("Sorry, but one of the required fields was left blank.\nPlease try again.", "");
                        newUserPassword.setText("");
                        newUserPasswordconf.setText("");
                    } else if (!Arrays.equals(pass, passconf) && pass.length > 0) {
                        createUserFrame.inform("Sorry, but your passwords did not match. Please try again.", "");
                        newUserPassword.setText("");
                        newUserPasswordconf.setText("");
                    } else if (pass.length < 5) {
                        createUserFrame.inform("Sorry, but your password length should be greater than\n"
                                + "four characters for security reasons. Please add more characters.", "");

                        newUserPassword.setText("");
                        newUserPasswordconf.setText("");
                    } else {
                        if (createUserBackground == null) {
                            Image img = CyderImages.defaultBackground.getImage();

                            BufferedImage bi = new BufferedImage(img.getWidth(null),
                                    img.getHeight(null),BufferedImage.TYPE_INT_RGB);

                            Graphics2D g2 = bi.createGraphics();
                            g2.drawImage(img, 0, 0, null);
                            g2.dispose();
                            File backgroundFile = new File("dynamic/users/" + uuid + "/Backgrounds/Default.png");
                            backgroundFile.mkdirs();
                            ImageIO.write(bi, "png", backgroundFile);
                        }

                        File NewUserFolder = new File("dynamic/users/" + uuid);
                        File backgrounds = new File("dynamic/users/" + uuid + "/Backgrounds");
                        File music = new File("dynamic/users/" + uuid + "/Music");
                        File notes = new File("dynamic/users/" + uuid + "/Notes");
                        File files = new File("dynamic/users/" + uuid + "/Files");

                        NewUserFolder.mkdirs();
                        backgrounds.mkdir();
                        music.mkdir();
                        notes.mkdir();
                        files.mkdir();

                        if (createUserBackground != null) {
                            ImageIO.write(ImageIO.read(createUserBackground), "png",
                                    new File("dynamic/users/" + uuid + "/Backgrounds/" + createUserBackground.getName()));
                        }

                        File dataFile = new File("dynamic/users/" + uuid + "/userdata.json");
                        dataFile.createNewFile();

                        User user = new User();

                        user.setName(newUserName.getText().trim());
                        user.setPass(SecurityUtil.toHexString(SecurityUtil.getSHA256(
                                SecurityUtil.toHexString(SecurityUtil.getSHA256(pass)).toCharArray())));

                        for (Preference pref : GenesisShare.getPrefs()) {
                            //as per convention, IGNORE for tooltip means ignore when creating user
                            // whilst IGNORE for default value means ignore for edit user
                            if (!pref.getTooltip().equals("IGNORE"))
                                UserUtil.setUserData(user, pref.getID(), pref.getDefaultValue());
                        }

                        user.setExecutables(null);
                        UserUtil.setUserData(dataFile, user);

                        createUserFrame.dispose();
                        PopupHandler.inform("The new user \"" + newUserName.getText().trim() + "\" has been created successfully.", "", GenesisShare.getDominantFrame());
                        createUserFrame.dispose();

                        //attempt to log in new user if it's the only user
                        if (new File("dynamic/users/").listFiles().length == 1) {
                            LoginHandler.getLoginFrame().dispose();
                            LoginHandler.recognize(newUserName.getText().trim(), SecurityUtil.toHexString(SecurityUtil.getSHA256(pass)));
                        }
                    }

                    for (char c : pass)
                        c = '\0';

                    for (char c : passconf)
                        c = '\0';

                } catch (Exception ex) {
                    ErrorHandler.handle(ex);
                }
            }
        });

        createNewUser.setBorder(new LineBorder(CyderColors.navy, 5, false));
        createNewUser.setFont(CyderFonts.weatherFontSmall);
        createNewUser.setBounds(60, 390, 240, 40);
        createUserFrame.getContentPane().add(createNewUser);

        createUserFrame.setLocationRelativeTo(GenesisShare.getDominantFrame());

        createUserFrame.setVisible(true);
        newUserName.requestFocus();
    }

    public static void close() {
        if (createUserFrame != null)
            createUserFrame.dispose();
    }
}
