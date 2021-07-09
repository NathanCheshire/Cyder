package cyder.genesis;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.consts.CyderImages;
import cyder.handler.ErrorHandler;
import cyder.obj.Preference;
import cyder.ui.*;
import cyder.utilities.GetterUtil;
import cyder.utilities.SecurityUtil;
import cyder.utilities.StringUtil;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;

public class UserCreator {
    private static CyderFrame createUserFrame;
    private static JPasswordField newUserPasswordconf;
    private static JPasswordField newUserPassword;
    private static CyderTextField newUserName;
    private static CyderButton createNewUser;
    private static CyderButton chooseBackground;
    private static File createUserBackground;

    public static void createGUI() {
        createUserBackground = null;

        if (createUserFrame != null)
            createUserFrame.closeAnimation();

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

        newUserPassword = new JPasswordField(15);
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
        newUserPassword.setSelectionColor(CyderColors.selectionColor);
        newUserPassword.setFont(new Font("Agency FB",Font.BOLD, 20));
        newUserPassword.setForeground(CyderColors.navy);
        newUserPassword.setCaretColor(CyderColors.navy);
        newUserPassword.setCaret(new CyderCaret(CyderColors.navy));

        createUserFrame.getContentPane().add(newUserPassword);

        JLabel passwordLabelConf = new JLabel("Confirm Password: ", SwingConstants.CENTER);
        passwordLabelConf.setFont(CyderFonts.weatherFontSmall);
        passwordLabelConf.setForeground(CyderColors.navy);
        passwordLabelConf.setBounds(60, 210, 240, 30);
        createUserFrame.getContentPane().add(passwordLabelConf);

        newUserPasswordconf = new JPasswordField(15);
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

        newUserPasswordconf.setBorder(new LineBorder(CyderColors.navy, 5, false));
        newUserPasswordconf.setSelectionColor(CyderColors.selectionColor);
        newUserPasswordconf.setFont(new Font("Agency FB",Font.BOLD, 20));
        newUserPasswordconf.setForeground(CyderColors.navy);
        newUserPasswordconf.setCaretColor(CyderColors.navy);
        newUserPasswordconf.setCaret(new CyderCaret(CyderColors.navy));
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
        chooseBackground.setColors(CyderColors.regularRed);
        chooseBackground.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                try {
                    new Thread(() -> {
                        try {
                            File temp = new GetterUtil().getFile("Choose new user's background file");
                            if (temp != null) {
                                createUserBackground = temp;
                            }

                            if (temp != null && !Files.probeContentType(Paths.get(createUserBackground.getAbsolutePath())).endsWith("png")) {
                                createUserBackground = null;
                            }
                        } catch (Exception ex) {
                            ErrorHandler.handle(ex);
                        }
                    }, "wait thread for GetterUtil().getFile()").start();
                } catch (Exception exc) {
                    exc.printStackTrace();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                try {
                    if (createUserBackground != null) {
                        chooseBackground.setText(createUserBackground.getName());
                    } else {
                        chooseBackground.setToolTipText("No File Chosen");
                    }
                } catch (Exception ex) {
                    ErrorHandler.handle(ex);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                chooseBackground.setToolTipText("Choose background");
            }
        });

        chooseBackground.setBorder(new LineBorder(CyderColors.navy, 5, false));
        chooseBackground.setBounds(60, 340, 240, 40);
        createUserFrame.getContentPane().add(chooseBackground);

        createNewUser = new CyderButton("Create User");
        createNewUser.setFont(CyderFonts.weatherFontSmall);
        createNewUser.setBackground(CyderColors.regularRed);
        createNewUser.setColors(CyderColors.regularRed);
        createNewUser.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                try {
                    String uuid = SecurityUtil.generateUUID();
                    File folder = new File("users/" + uuid);

                    while (folder.exists()) {
                        uuid = SecurityUtil.generateUUID();
                        folder = new File("users/" + uuid);
                    }

                    char[] pass = newUserPassword.getPassword();
                    char[] passconf = newUserPasswordconf.getPassword();

                    if (StringUtil.empytStr(newUserName.getText()) || pass == null || passconf == null
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
                            File backgroundFile = new File("users/" + uuid + "/Backgrounds/Default.png");
                            backgroundFile.mkdirs();
                            ImageIO.write(bi, "png", backgroundFile);
                        }

                        File NewUserFolder = new File("users/" + uuid);
                        File backgrounds = new File("users/" + uuid + "/Backgrounds");
                        File music = new File("users/" + uuid + "/Music");
                        File notes = new File("users/" + uuid + "/Notes");

                        NewUserFolder.mkdirs();
                        backgrounds.mkdir();
                        music.mkdir();
                        notes.mkdir();

                        if (createUserBackground != null) {
                            ImageIO.write(ImageIO.read(createUserBackground), "png",
                                    new File("users/" + uuid + "/Backgrounds/" + createUserBackground.getName()));
                        }

                        //this will use binary writing when we switch so we'll change to .bin and such
                        BufferedWriter newUserWriter = new BufferedWriter(new FileWriter(
                                "users/" + uuid + "/Userdata.txt"));

                        LinkedList<String> data = new LinkedList<>();
                        data.add("Name:" + newUserName.getText().trim());
                        data.add("Password:" + SecurityUtil.toHexString(SecurityUtil.getSHA(pass)));

                        for (Preference pref : GenesisShare.getPrefs()) {
                            data.add(pref.getID() + ":" + pref.getDefaultValue());
                        }

                        for (String d : data) {
                            newUserWriter.write(d);
                            newUserWriter.newLine();
                        }

                        newUserWriter.close();

                        createUserFrame.closeAnimation();
                        createUserFrame.inform("The new user \"" + newUserName.getText().trim() + "\" has been created successfully.", "");
                        createUserFrame.closeAnimation();

                        //attempt to log in new user if it's the only user
                        if (new File("users/").length() == 1) {
                            Entry.getFrame().closeAnimation();
                            Entry.recognize(newUserName.getText().trim(), pass);
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

        if (!ConsoleFrame.getConsoleFrame().isClosed()) {
            ConsoleFrame.getConsoleFrame().setFrameRelativeTo(createUserFrame);
        } else if (Entry.getFrame() != null && Entry.getFrame().isActive() && Entry.getFrame().isVisible()) {
            createUserFrame.setLocationRelativeTo(Entry.getFrame());
        } else {
            createUserFrame.setLocationRelativeTo(null);
        }

        createUserFrame.setVisible(true);
        newUserName.requestFocus();
    }

    public static void close() {
        if (createUserFrame != null)
            createUserFrame.closeAnimation();
    }
}
