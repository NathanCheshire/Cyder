package cyder.genesis;

import cyder.consts.CyderColors;
import cyder.exception.FatalException;
import cyder.handler.ErrorHandler;
import cyder.handler.SessionLogger;
import cyder.ui.ConsoleFrame;
import cyder.ui.CyderCaret;
import cyder.ui.CyderFrame;
import cyder.ui.CyderScrollPane;
import cyder.utilities.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.LinkedList;

public class Entry {
    private static CyderFrame loginFrame;
    private static JPasswordField loginField;
    private static boolean doLoginAnimations;
    private static int loginMode;
    private static String username;
    private static final String bashString = SystemUtil.getWindowsUsername() + "@Cyder:~$ ";
    private static String consoleBashString;

    private static boolean autoCypherAttempt;

    private static LinkedList<String> printingList = new LinkedList<>();
    private static LinkedList<String> priorityPrintingList = new LinkedList<>();

    private static void loginTypingAnimation(JTextPane refArea) {
        printingList.clear();
        printingList.add("Cyder version: " + IOUtil.getSystemData("Date") + "\n");
        printingList.add("Type \"h\" for a list of valid commands\n");
        printingList.add("Build: Soultree\n");
        printingList.add("Author: Nathan Cheshire\n");
        printingList.add("Design OS: Windows 10+\n");
        printingList.add("Design JVM: 8+\n");
        printingList.add("Description: A programmer's swiss army knife\n");

        int charTimeout = 40;
        int lineTimeout = 500;

        new Thread(() -> {
            StringUtil su = new StringUtil(refArea);

            try {
                while (doLoginAnimations && loginFrame != null)  {
                    if (priorityPrintingList.size() > 0) {
                        String line = priorityPrintingList.removeFirst();

                        for (char c : line.toCharArray()) {
                            su.print(String.valueOf(c));
                            Thread.sleep(charTimeout);
                        }
                    } else if (printingList.size() > 0) {
                        String line = printingList.removeFirst();

                        for (char c : line.toCharArray()) {
                            su.print(String.valueOf(c));
                            Thread.sleep(charTimeout);
                        }
                    }

                    Thread.sleep(lineTimeout);
                }
            }

            catch (Exception e) {
                ErrorHandler.handle(e);
            }
        },"login printing animation").start();

        new Thread(() -> {
            try {
                while (doLoginAnimations && loginFrame != null) {
                    if (loginField.getCaretPosition() < bashString.length()) {
                        loginField.setCaretPosition(loginField.getPassword().length);
                    }

                    //if it doesn't start with bash string, reset it to it
                    if (!String.valueOf(loginField.getPassword()).startsWith(bashString) && loginMode != 2) {
                        loginField.setText(bashString);
                        loginField.setCaretPosition(loginField.getPassword().length);
                    }

                    Thread.sleep(50);
                }
            }

            catch (Exception e) {
                ErrorHandler.handle(e);
            }
        },"Login Input Caret Position Updater").start();
    }

    public static void showEntryGUI() {
        doLoginAnimations = true;
        loginMode = 0;

        if (loginFrame != null)
            loginFrame.closeAnimation();

        IOUtil.cleanUsers();

        loginFrame = new CyderFrame(600, 400,
                ImageUtil.imageIconFromColor(new Color(21,23,24)),true) {
            @Override
            public void dispose() {
                doLoginAnimations = false;
                super.dispose();
            }
        };
        loginFrame.setTitlePosition(CyderFrame.TitlePosition.LEFT);
        loginFrame.setTitle(IOUtil.getSystemData("Version") + " login");
        loginFrame.setBackground(new Color(21,23,24));

        if (ConsoleFrame.getConsoleFrame().isClosed()) {
            loginFrame.addCloseListener(e -> GenesisShare.exit(25));
        }

        JTextPane loginArea = new JTextPane();
        loginArea.setBounds(20, 40, 560, 280);
        loginArea.setBackground(new Color(21,23,24));
        loginArea.setBorder(null);
        loginArea.setFocusable(false);
        loginArea.setEditable(false);
        loginArea.setFont(new Font("Agency FB",Font.BOLD, 26));
        loginArea.setForeground(new Color(85,181,219));
        loginArea.setCaretColor(loginArea.getForeground());

        CyderScrollPane loginScroll = new CyderScrollPane(loginArea,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        loginScroll.setThumbColor(CyderColors.intellijPink);
        loginScroll.setBounds(20, 40, 560, 280);
        loginScroll.getViewport().setOpaque(false);
        loginScroll.setOpaque(false);
        loginScroll.setBorder(null);
        loginArea.setAutoscrolls(true);

        loginFrame.getContentPane().add(loginScroll);

        loginField = new JPasswordField(20);
        loginField.setEchoChar((char)0);
        loginField.setText(bashString);
        loginField.setBounds(20, 340, 560, 40);
        loginField.setBackground(new Color(21,23,24));
        loginField.setBorder(null);
        loginField.setCaret(new CyderCaret(loginArea.getForeground()));
        loginField.setSelectionColor(CyderColors.selectionColor);
        loginField.setFont(new Font("Agency FB",Font.BOLD, 26));
        loginField.setForeground(new Color(85,181,219));
        loginField.setCaretColor(new Color(85,181,219));
        loginField.addActionListener(e -> loginField.requestFocusInWindow());
        loginField.addKeyListener(new KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                if (evt.getKeyChar() == KeyEvent.VK_BACK_SPACE && loginMode != 2) {
                    if (loginField.getPassword().length < bashString.toCharArray().length) {
                        evt.consume();
                        loginField.setText(bashString);
                    }
                }

                else if (evt.getKeyChar() == '\n') {
                    char[] input = loginField.getPassword();

                    if (loginMode != 2) {
                        char[] newInput = new char[input.length - bashString.toCharArray().length];

                        //copy input to new input with offset
                        if (input.length - bashString.length() >= 0) {
                            System.arraycopy(input, bashString.length(), newInput, 0,
                                    input.length - bashString.length());
                        }

                        input = newInput.clone();
                    }

                    switch (loginMode) {
                        case 0:
                            try {
                                if (Arrays.equals(input,"create".toCharArray())) {
                                    UserCreator.createGUI();
                                    loginField.setText(bashString);
                                    loginMode = 0;
                                } else if (Arrays.equals(input,"login".toCharArray())) {
                                    loginField.setText(bashString);
                                    priorityPrintingList.add("Awaiting Username\n");
                                    loginMode = 1;
                                } else if (Arrays.equals(input,"login admin".toCharArray())) {
                                    loginField.setText(bashString);
                                    priorityPrintingList.add("Feature not yet implemented\n");
                                    loginMode = 0;
                                } else if (Arrays.equals(input,"quit".toCharArray())) {
                                    loginFrame.closeAnimation();
                                    GenesisShare.exit(25);
                                } else if (Arrays.equals(input,"h".toCharArray())) {
                                    loginField.setText(bashString);
                                    priorityPrintingList.add("Valid commands: create, login, login admin, quit, h\n");
                                } else {
                                    loginField.setText(bashString);
                                    priorityPrintingList.add("Unknown command; See \"h\" for help\n");
                                }
                            } catch (Exception e) {
                                ErrorHandler.handle(e);
                            }

                            break;
                        case 1:
                            username = new String(input);
                            loginMode = 2;
                            loginField.setEchoChar('*');
                            loginField.setText("");
                            priorityPrintingList.add("Awaiting Password\n");

                            break;

                        case 2:
                            loginField.setEchoChar((char)0);

                            try {
                                Robot rob = new Robot();
                                rob.keyPress(KeyEvent.VK_BACK_SPACE);
                                rob.keyRelease(KeyEvent.VK_BACK_SPACE);
                            } catch (Exception e) {
                                ErrorHandler.handle(e);
                            }

                            recognize(username,input);
                            priorityPrintingList.add("Could not recognize user\n");

                            if (input != null)
                                for (char c: input)
                                    c = '\0';

                            loginMode = 0;
                            break;

                        default:
                            loginField.setText(bashString);
                            try {
                                throw new FatalException("Error resulting from login shell");
                            } catch (FatalException e) {
                                ErrorHandler.handle(e);
                            }
                    }
                }
            }
        });

        loginField.setCaretPosition(bashString.length());
        loginFrame.getContentPane().add(loginField);

        loginFrame.addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) {
                loginField.requestFocus();
            }
        });

        File Users = new File("users/");
        String[] directories = Users.list((current, name) -> new File(current, name).isDirectory());

        loginFrame.addCloseListener(e -> {
            if (ConsoleFrame.getConsoleFrame().isClosed()) {
                UserCreator.close();
            }
        });

        loginFrame.setVisible(true);
        loginFrame.enterAnimation();

        if (directories != null && directories.length == 0)
            priorityPrintingList.add("No users found; please type \"create\"\n");

        loginTypingAnimation(loginArea);

        //in case this is after a corruption, start frame checker again
        GenesisShare.cancelFrameSuspention();
    }

    public static void recognize(String Username, char[] Password) {
        try {
            if (loginFrame != null) {
                loginField.setEchoChar((char)0);
                loginField.setText(bashString);
            }

            if (SecurityUtil.checkPassword(Username, SecurityUtil.toHexString(SecurityUtil.getSHA(Password)))) {
                doLoginAnimations = false;
                SessionLogger.log(SessionLogger.Tag.LOGIN, "AUTOCYPHER PASS");

                if (!ConsoleFrame.getConsoleFrame().isClosed()) {
                    ConsoleFrame.getConsoleFrame().close();
                }

                SessionLogger.log(SessionLogger.Tag.LOGIN, "STD LOGIN");
                ConsoleFrame.getConsoleFrame().start();

                //dispose login frame now to avoid final frame disposed checker seeing that there are no frames
                // and exiting the program when we have just logged in
                if (loginFrame != null) {
                    loginFrame.closeAnimation();
                }

                //this if block needs to be in console, stuff to do specifically for user on first login
                if (IOUtil.getUserData("IntroMusic").equals("1")) {
                    LinkedList<String> musicList = new LinkedList<>();

                    File userMusicDir = new File("users/" + ConsoleFrame.getConsoleFrame().getUUID() + "/Music");

                    String[] fileNames = userMusicDir.list();

                    if (fileNames != null) {
                        for (String fileName : fileNames) {
                            if (fileName.endsWith(".mp3")) {
                                musicList.add(fileName);
                            }
                        }
                    }

                    if (!musicList.isEmpty()) {
                        IOUtil.playAudio("users/" + ConsoleFrame.getConsoleFrame().getUUID() + "/Music/" +
                                        (fileNames[NumberUtil.randInt(0, fileNames.length - 1)]),
                                ConsoleFrame.getConsoleFrame().getInputHandler());
                    } else {
                        IOUtil.playAudio("sys/audio/Ride.mp3",
                                ConsoleFrame.getConsoleFrame().getInputHandler());
                    }
                }
            } else if (loginFrame != null && loginFrame.isVisible()) {
                loginField.setText("");

                for (char c: Password) {
                    c = '\0';
                }

                SessionLogger.log(SessionLogger.Tag.LOGIN, "AUTOCYPHER FAIL");
                username = "";
                loginField.requestFocusInWindow();
            }
        } catch (Exception e) {
            ErrorHandler.silentHandle(e);
        }
    }

    /**
     * Used for debugging, automatically logs me in if my account exists,
     * otherwise the program continues as normal
     */
    public static void autoCypher() {
        try {
            File autoCypher = new File("../autocypher.txt");
            File Users = new File("users/");

            if (autoCypher.exists() && Users.listFiles().length != 0) {
                BufferedReader ac = new BufferedReader(new FileReader(autoCypher));

                String line = ac.readLine();
                String[] parts = line.split(":");

                if (parts.length == 2 && !parts[0].equals("") && !parts[1].equals("")) {
                    ac.close();
                    autoCypherAttempt = true;
                    recognize(parts[0], parts[1].toCharArray());
                }
            } else {
                showEntryGUI();
            }
        } catch (Exception e) {
            ErrorHandler.handle(e);
            showEntryGUI();
        }
    }

    public static CyderFrame getFrame() {
        return loginFrame;
    }
}
