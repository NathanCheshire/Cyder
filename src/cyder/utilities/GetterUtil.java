package cyder.utilities;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.consts.CyderImages;
import cyder.genesis.GenesisShare;
import cyder.genobjects.BoundsString;
import cyder.handler.ErrorHandler;
import cyder.ui.*;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicReference;

public class GetterUtil {
    public GetterUtil() {}
    //instantiation does nothing but we still want to allow object creation for multiple instances
    //should we require multiple string/file getteres at the same time.

    /*
    STRING GETTER
     */

    /** Custom getInput method, see usage below for how to setup so that the program doesn't
     * spin wait on the main GUI thread forever. Ignoring the below setup
     * instructions is fine only for the getString method, the getFile method must be surrounded by
     * a thread whenever called.
     *
     * USAGE:
     *  <pre>
     *  {@code
     *  new Thread(() -> {
     *      try {
     *          String input = new GetterUtil().getString("title","tooltip","button text");
     *          //other operations using input
     *      } catch (Exception e) {
     *          ErrorHandler.handle(e);
     *      }
     *  }, "wait thread for GetterUtil().getString()").start();
     *  }
     *  </pre>
     * @param title the title of the frame
     * @param tooltip the tooltip of the input field
     * @param buttonText the text of the submit button
     * @return the user entered input string. NOTE: if any improper input is ateempted to be returned,
     *  this function returns the string literal of "NULL" instead of {@code null}
     */
    public String getString(String title, String tooltip, String buttonText) {
        AtomicReference<String> returnString = new AtomicReference<>();

        new Thread(() -> {
            try {
                CyderFrame inputFrame = new CyderFrame(400,170, CyderImages.defaultBackground);
                inputFrame.setFrameType(CyderFrame.FrameType.INPUT_GETTER);
                inputFrame.setTitle(title);

                CyderTextField inputField = new CyderTextField(0);
                inputField.setBackground(Color.white);
                inputField.setToolTipText(tooltip);
                inputField.setBounds(40,40,320,40);
                inputFrame.getContentPane().add(inputField);

                CyderButton submit = new CyderButton(buttonText);
                submit.setBackground(CyderColors.regularRed);
                submit.setColors(CyderColors.regularRed);
                inputField.addActionListener(e1 -> submit.doClick());
                submit.setBorder(new LineBorder(CyderColors.navy,5,false));
                submit.setFont(CyderFonts.weatherFontSmall);
                submit.setForeground(CyderColors.navy);
                submit.addActionListener(e12 -> {
                    returnString.set((inputField.getText() == null || inputField.getText().length() == 0 ?
                            "NULL" : inputField.getText()));
                    inputFrame.dispose();
                });
                submit.setBounds(40,100,320,40);
                inputFrame.getContentPane().add(submit);

                inputFrame.addPreCloseAction(submit::doClick);

                inputFrame.setVisible(true);
                inputFrame.setAlwaysOnTop(true);
                inputFrame.setLocationRelativeTo(GenesisShare.getDominantFrame());
            } catch (Exception e) {
                ErrorHandler.handle(e);
            }
        }, this + "getString thread").start();

        try {
            while (returnString.get() == null) {
                Thread.onSpinWait();
            }
        } catch (Exception ex) {
            ErrorHandler.handle(ex);
        } finally {
            return returnString.get();
        }
    }

    /** Custom getInput method, see usage below for how to setup so that the program doesn't
     * spin wait on the main GUI thread forever. Ignoring the below setup
     * instructions is fine only for the getString method, the getFile method must be surrounded by
     * a thread whenever called.
     *
     * USAGE:
     *  <pre>
     *  {@code
     *  new Thread(() -> {
     *      try {
     *          String input = new GetterUtil().getString("title","tooltip","button text","initial field value");
     *          //other operations using input
     *      } catch (Exception e) {
     *          ErrorHandler.handle(e);
     *      }
     *  }, "wait thread for GetterUtil().getString()").start();
     *  }
     *  </pre>
     * @param title the title of the frame
     * @param tooltip the tooltip of the input field
     * @param buttonText the text of the submit button
     * @param initialString the initial text in the input field
     * @return the user entered input string. NOTE: if any improper input is ateempted to be returned,
     *  this function returns the string literal of "NULL" instead of {@code null}
     */
    public String getString(String title, String tooltip, String buttonText, String initialString) {
        AtomicReference<String> returnString = new AtomicReference<>();

        new Thread(() -> {
            try {
                CyderFrame inputFrame = new CyderFrame(400,170, CyderImages.defaultBackground);
                inputFrame.setFrameType(CyderFrame.FrameType.INPUT_GETTER);
                inputFrame.setTitle(title);

                CyderTextField inputField = new CyderTextField(0);
                inputField.setBackground(Color.white);
                inputField.setText(initialString);
                inputField.setToolTipText(tooltip);
                inputField.setBounds(40,40,320,40);
                inputFrame.getContentPane().add(inputField);

                CyderButton submit = new CyderButton(buttonText);
                submit.setBackground(CyderColors.regularRed);
                submit.setColors(CyderColors.regularRed);
                inputField.addActionListener(e1 -> submit.doClick());
                submit.setBorder(new LineBorder(CyderColors.navy,5,false));
                submit.setFont(CyderFonts.weatherFontSmall);
                submit.setForeground(CyderColors.navy);
                submit.addActionListener(e12 -> {
                    returnString.set((inputField.getText() == null || inputField.getText().length() == 0 ?
                            "NULL" : inputField.getText()));
                    inputFrame.dispose();
                });
                submit.setBounds(40,100,320,40);
                inputFrame.getContentPane().add(submit);

                inputFrame.addPreCloseAction(submit::doClick);

                inputFrame.setVisible(true);
                inputFrame.setAlwaysOnTop(true);
                inputFrame.setLocationRelativeTo(relativeFrame);
            } catch (Exception e) {
                ErrorHandler.handle(e);
            }
        }, this + "getString thread").start();

        try {
            while (returnString.get() == null) {
                Thread.onSpinWait();
            }
        } catch (Exception ex) {
            ErrorHandler.handle(ex);
        } finally {
            return returnString.get();
        }
    }

    private CyderFrame relativeFrame = null;

    public void setRelativeFrame(CyderFrame relativeFrame) {
        this.relativeFrame = relativeFrame;
    }


    /*
    FILE GETTER
     */

    /** Custom getInput method, see usage below for how to setup so that the program doesn't
     * spin wait on the main GUI thread forever. Ignoring the below setup
     * instructions will make the application spin wait possibly forever.
     *
     * USAGE:
     * <pre>
     * {@code
     *   new Thread(() -> {
     *         try {
     *             File input = new GetterUtil().getFile("FileChooser title");
     *             //other operations using input
     *         } catch (Exception e) {
     *             ErrorHandler.handle(e);
     *         }
     *  }, "wait thread for GetterUtil().getFile()").start();
     * }
     * </pre>
     * @param title the title of the JavaFX FileChooser
     * @return the user-chosen file
     */
    public File getFile(String title) {
        AtomicReference<File> setOnFileChosen = new AtomicReference<>();

        new Thread(() -> {
            try {
                //reset needed vars in case an instance was already ran
                backward = new Stack<>();
                forward = new Stack<>();
                directoryFileList.clear();
                directoryNameList.clear();
                currentDirectory = new File("c:\\users\\"
                        + SystemUtil.getWindowsUsername() + "\\");

                //code copied from dir search widget
                if (dirFrame != null)
                    dirFrame.dispose();

                //frame setup
                dirFrame = new CyderFrame(630,510, CyderImages.defaultBackground);
                dirFrame.setTitle(currentDirectory.getName());

                //field setup
                dirField = new CyderTextField(0);
                dirField.setBackground(Color.white);
                dirField.setText(currentDirectory.getAbsolutePath());
                dirField.addActionListener(e -> {
                    File ChosenDir = new File(dirField.getText());

                    if (ChosenDir.isDirectory()) {
                        refreshBasedOnDir(ChosenDir,setOnFileChosen);
                    } else if (ChosenDir.isFile()) {
                        setOnFileChosen.set(ChosenDir);
                    }
                });
                dirField.setBounds(60,40,500,40);
                dirFrame.getContentPane().add(dirField);

                //last setup
                last = new CyderButton(" < ");
                last.setFocusPainted(false);
                last.setForeground(CyderColors.navy);
                last.setBackground(CyderColors.regularRed);
                last.setFont(CyderFonts.weatherFontSmall);
                last.setBorder(new LineBorder(CyderColors.navy,5,false));
                last.setColors(CyderColors.regularRed);
                last.addActionListener(e -> {
                    //we may only go back if there's something in the back and it's different from where we are now
                    if (backward != null && !backward.isEmpty() && !backward.peek().equals(currentDirectory)) {
                        //traversing so push where we are to forward
                        forward.push(currentDirectory);

                        //get where we're going
                        currentDirectory = backward.pop();

                        //now simply refresh based on currentDir
                        refreshFromTraversalButton(setOnFileChosen);
                    }
                });
                last.setBounds(10,40,40,40);
                dirFrame.getContentPane().add(last);

                //next setup
                next = new CyderButton(" > ");
                next.setFocusPainted(false);
                next.setForeground(CyderColors.navy);
                next.setBackground(CyderColors.regularRed);
                next.setFont(CyderFonts.weatherFontSmall);
                next.setBorder(new LineBorder(CyderColors.navy,5,false));
                next.setColors(CyderColors.regularRed);
                next.addActionListener(e -> {
                    //only traverse forward if the stack is not empty and forward is different from where we are
                    if (forward != null && !forward.isEmpty() && !forward.peek().equals(currentDirectory)) {
                        //push where we are
                        backward.push(currentDirectory);

                        //figure out where we need to go
                        currentDirectory = forward.pop();

                        //refresh based on where we should go
                        refreshFromTraversalButton(setOnFileChosen);
                    }
                });
                next.setBounds(620 - 50,40,40, 40);
                dirFrame.getContentPane().add(next);

                File chosenDir = new File("c:/users/"
                        + SystemUtil.getWindowsUsername() + "/");
                File[] startDir = chosenDir.listFiles();

                Collections.addAll(directoryFileList, startDir);

                for (File file : directoryFileList) {
                    directoryNameList.add(file.getName());
                }

                //files scroll list setup
                cyderScrollList = new CyderScrollList(600, 400, CyderScrollList.SelectionPolicy.SINGLE);
                cyderScrollList.setScrollFont(CyderFonts.weatherFontSmall.deriveFont(16f));

                //adding things to the list and setting up actions for what to do when an element is clicked
                for (int i = 0 ; i < directoryNameList.size() ; i++) {
                    int finalI = i;
                    class thisAction implements CyderScrollList.ScrollAction {
                        @Override
                        public void fire() {
                            if (directoryFileList.get(finalI).isDirectory()) {
                                refreshBasedOnDir(directoryFileList.get(finalI), setOnFileChosen);
                            } else {
                                setOnFileChosen.set(directoryFileList.get(finalI));
                            }
                        }
                    }

                    thisAction action = new thisAction();
                    cyderScrollList.addElement(directoryNameList.get(i), action);
                }

                //generate the scroll label
                dirScrollLabel = cyderScrollList.generateScrollList();
                dirScrollLabel.setBounds(10,90,600, 400);
                dirFrame.getContentPane().add(dirScrollLabel);

                //final frame setup
                dirFrame.setLocationRelativeTo(GenesisShare.getDominantFrame());
                dirFrame.setVisible(true);
                dirField.requestFocus();
            } catch (Exception e) {
                ErrorHandler.handle(e);
            }
        }, this + " getFile thread").start();

        try {
            while (setOnFileChosen.get() == null)
                Thread.onSpinWait();
        } catch (Exception ex) {
            ErrorHandler.handle(ex);
        } finally {
            dirFrame.dispose();
            return setOnFileChosen.get().getName().equals("NULL") ? null : setOnFileChosen.get();
        }
    }

    /*
     * File getter inner methods
     */

    //general refresh method that doesn't clear the stacks
    private static void refreshFromTraversalButton(AtomicReference<File> setOnFileChosen) {
        //get files
        File[] files = currentDirectory.listFiles();

        //remove old files
        cyderScrollList.removeAllElements();
        dirFrame.remove(dirScrollLabel);

        //wipe name and files lists
        directoryFileList.clear();
        directoryNameList.clear();

        //add new files arr to LL
        Collections.addAll(directoryFileList, files);

        //get corresponding names for name list
        for (File file : directoryFileList) {
            directoryNameList.add(file.getName());
        }

        //setup scroll
        cyderScrollList = new CyderScrollList(600, 400, CyderScrollList.SelectionPolicy.SINGLE);
        cyderScrollList.setScrollFont(CyderFonts.weatherFontSmall.deriveFont(16f));

        //add new items to scroll and actions
        for (int i = 0 ; i < directoryNameList.size() ; i++) {
            int finalI = i;
            class thisAction implements CyderScrollList.ScrollAction {
                @Override
                public void fire() {
                    if (directoryFileList.get(finalI).isDirectory()) {
                        refreshBasedOnDir(directoryFileList.get(finalI), setOnFileChosen);
                    } else {
                        setOnFileChosen.set(directoryFileList.get(finalI));
                    }
                }
            }

            thisAction action = new thisAction();
            cyderScrollList.addElement(directoryNameList.get(i), action);
        }

        //regenerate scroll
        dirScrollLabel = cyderScrollList.generateScrollList();
        dirScrollLabel.setBounds(10,90,600, 400);
        dirFrame.getContentPane().add(dirScrollLabel);

        //frame revalidation
        dirFrame.revalidate();
        dirFrame.repaint();
        dirFrame.setTitle(currentDirectory.getName());
        dirField.setText(currentDirectory.getAbsolutePath());
    }

    //refresh button that clears the back stack
    private static void refreshBasedOnDir(File directory, AtomicReference<File> setOnFileChosen) {
        //clear forward since a new path
        forward.clear();

        //before where we were is wiped, put it in backwards if it's not the last
        if (backward.isEmpty() || !backward.peek().equals(currentDirectory)) {
            backward.push(currentDirectory);
        }

        //this is our current now
        currentDirectory = directory;

        //get files to display
        File[] files = directory.listFiles();

        //remove old list
        cyderScrollList.removeAllElements();
        dirFrame.remove(dirScrollLabel);

        //clear display lists
        directoryFileList.clear();
        directoryNameList.clear();

        //add array files to LL files
        Collections.addAll(directoryFileList, files);

        //add corresponding names of files to names list
        for (File file : directoryFileList) {
            directoryNameList.add(file.getName());
        }

        //regenerate scroll
        cyderScrollList = new CyderScrollList(600, 400, CyderScrollList.SelectionPolicy.SINGLE);
        cyderScrollList.setScrollFont(CyderFonts.weatherFontSmall.deriveFont(16f));

        //add items with coresponding actions to scroll
        for (int i = 0 ; i < directoryNameList.size() ; i++) {
            int finalI = i;
            class thisAction implements CyderScrollList.ScrollAction {
                @Override
                public void fire() {
                    if (directoryFileList.get(finalI).isDirectory()) {
                        refreshBasedOnDir(directoryFileList.get(finalI), setOnFileChosen);
                    } else {
                        setOnFileChosen.set(directoryFileList.get(finalI));
                    }
                }
            }

            thisAction action = new thisAction();
            cyderScrollList.addElement(directoryNameList.get(i), action);
        }

        //generate scroll and add it
        dirScrollLabel = cyderScrollList.generateScrollList();
        dirScrollLabel.setBounds(10,90,600, 400);
        dirFrame.getContentPane().add(dirScrollLabel);

        //frame revalidation
        dirFrame.revalidate();
        dirFrame.repaint();
        dirFrame.setTitle(directory.getName());
        dirField.setText(directory.getAbsolutePath());
    }

    /*
     * File getter UI vars
     */

    //all ui elements
    private static CyderFrame dirFrame;
    private static CyderTextField dirField;
    private static CyderScrollList cyderScrollList;
    private static JLabel dirScrollLabel;
    private static CyderButton last;
    private static CyderButton next;

    //corresponding lists
    private static LinkedList<String> directoryNameList = new LinkedList<>();
    private static LinkedList<File> directoryFileList = new LinkedList<>();

    //stacks for traversal
    private static Stack<File> backward = new Stack<>();
    private static Stack<File> forward = new Stack<>();

    //where we currently are
    private static File currentDirectory = new File("c:\\users\\"
            + SystemUtil.getWindowsUsername() + "\\");

    /*
     * Confirmation getter
     */

    public boolean getConfirmation(String message, CyderFrame relativeFrame) {
        final String[] retString = {null};
        final CyderFrame[] confirmationFrame = {null};

        new Thread(() -> {
            try {
                CyderLabel textLabel = new CyderLabel();

                BoundsString bs = BoundsUtil.widthHeightCalculation(message, textLabel.getFont());
                int w = bs.getWidth();
                int h = bs.getHeight();
                textLabel.setText(bs.getText());

                confirmationFrame[0] = new CyderFrame(w + 40, h + 25 + 20 + 40 + 40);
                confirmationFrame[0].setFrameType(CyderFrame.FrameType.INPUT_GETTER);
                confirmationFrame[0].setTitle("Confirmation");
                confirmationFrame[0].addPreCloseAction(() -> retString[0] = "false");

                textLabel.setBounds(10,35, w, h);
                confirmationFrame[0].getContentPane().add(textLabel);

                //accounting for offset above
                w += 40;

                CyderButton yes = new CyderButton("Yes");
                yes.addActionListener(e -> retString[0] = "true");
                yes.setBounds(20,35 + h + 20, (w - 60) / 2, 40);
                confirmationFrame[0].getContentPane().add(yes);

                CyderButton no = new CyderButton("No");
                no.addActionListener(e -> retString[0] = "false");
                no.setBounds(20 + 20 + ((w - 60) / 2),35 + h + 20, (w - 60) / 2, 40);
                confirmationFrame[0].getContentPane().add(no);

                confirmationFrame[0].setVisible(true);
                confirmationFrame[0].setLocationRelativeTo(relativeFrame);

            } catch (Exception e) {
                ErrorHandler.handle(e);
            }
        }, this + "getConfirmation thread").start();

        try {
            while (retString[0] == null) {
                Thread.onSpinWait();
            }

        } catch (Exception ex) {
            ErrorHandler.handle(ex);
        } finally {
            confirmationFrame[0].dispose();
            return retString[0].equals("true");
        }
    }
}
