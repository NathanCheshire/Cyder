package cyder.utilities;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.consts.CyderImages;
import cyder.genesis.GenesisShare;
import cyder.genobjects.BoundsString;
import cyder.handler.ErrorHandler;
import cyder.ui.CyderButton;
import cyder.ui.CyderFrame;
import cyder.ui.CyderLabel;
import cyder.ui.CyderTextField;

import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

public class GetterUtil {
    private String fileFrameTitle;
    private File returnFile = null;

    private String stringFrameTitle;
    private String stringTooltipText;
    private String stringButtonText;
    private String returnString = null;

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
        stringFrameTitle = title;
        stringTooltipText = tooltip;
        stringButtonText = buttonText;

        new Thread(() -> {
            try {
                CyderFrame inputFrame = new CyderFrame(400,170, CyderImages.defaultBackground);
                inputFrame.setFrameType(CyderFrame.FrameType.INPUT_GETTER);
                inputFrame.setTitle(getStringFrameTitle());

                CyderTextField inputField = new CyderTextField(0);
                inputField.setBackground(Color.white);
                inputField.setToolTipText(getStringTooltipText());
                inputField.setBounds(40,40,320,40);
                inputFrame.getContentPane().add(inputField);

                CyderButton submit = new CyderButton(getStringButtonText());
                submit.setBackground(CyderColors.regularRed);
                submit.setColors(CyderColors.regularRed);
                inputField.addActionListener(e1 -> submit.doClick());
                submit.setBorder(new LineBorder(CyderColors.navy,5,false));
                submit.setFont(CyderFonts.weatherFontSmall);
                submit.setForeground(CyderColors.navy);
                submit.addActionListener(e12 -> {
                    returnString = (inputField.getText() == null || inputField.getText().length() == 0 ? "NULL" : inputField.getText());
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
            while (returnString == null) {
                Thread.onSpinWait();
            }
        } catch (Exception ex) {
            ErrorHandler.handle(ex);
        } finally {
            String ret = returnString;
            clearString();
            return ret;
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
        stringFrameTitle = title;
        stringTooltipText = tooltip;
        stringButtonText = buttonText;

        new Thread(() -> {
            try {
                CyderFrame inputFrame = new CyderFrame(400,170, CyderImages.defaultBackground);
                inputFrame.setFrameType(CyderFrame.FrameType.INPUT_GETTER);
                inputFrame.setTitle(getStringFrameTitle());

                CyderTextField inputField = new CyderTextField(0);
                inputField.setBackground(Color.white);
                inputField.setText(initialString);
                inputField.setToolTipText(getStringTooltipText());
                inputField.setBounds(40,40,320,40);
                inputFrame.getContentPane().add(inputField);

                CyderButton submit = new CyderButton(getStringButtonText());
                submit.setBackground(CyderColors.regularRed);
                submit.setColors(CyderColors.regularRed);
                inputField.addActionListener(e1 -> submit.doClick());
                submit.setBorder(new LineBorder(CyderColors.navy,5,false));
                submit.setFont(CyderFonts.weatherFontSmall);
                submit.setForeground(CyderColors.navy);
                submit.addActionListener(e12 -> {
                    returnString = (inputField.getText() == null || inputField.getText().length() == 0 ? "NULL" : inputField.getText());
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
            while (returnString == null) {
                Thread.onSpinWait();
            }
        } catch (Exception ex) {
            ErrorHandler.handle(ex);
        } finally {
            String ret = returnString;
            clearString();
            return ret;
        }
    }

    private CyderFrame relativeFrame = null;

    public void setRelativeFrame(CyderFrame relativeFrame) {
        this.relativeFrame = relativeFrame;
    }

    public void setStringFrameTitle(String title) {
        stringFrameTitle = title;
    }

    public void setStringTooltipText(String text) {
        stringTooltipText = text;
    }

    public void setStringButtonText(String text) {
        stringButtonText = text;
    }

    public String getStringFrameTitle() {
        return stringFrameTitle;
    }

    public String getStringTooltipText() {
        return stringTooltipText;
    }

    public String getStringButtonText() {
        return stringButtonText;
    }

    public void clearString() {
        returnString = null;
        stringFrameTitle = null;
        stringTooltipText = null;
        stringButtonText = null;
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
        AtomicReference<File> ret = new AtomicReference<>();

        new Thread(() -> {
            try {
                //todo custom FileGetter
            } catch (Exception e) {
                ErrorHandler.handle(e);
            }
        }, this + " getFile thread").start();

        try {
            while (ret.get() == null)
                Thread.onSpinWait();
        } catch (Exception ex) {
            ErrorHandler.handle(ex);
        } finally {
            clearString();
            return ret.get().getName().equals("NULL") ? null : ret.get();
        }
    }

    public String getFileFrameTitle() {
        return this.fileFrameTitle;
    }

    public void setFileFrameTitle(String title) {
        this.fileFrameTitle = title;
    }

    public void clearFile() {
        returnFile = null;
        fileFrameTitle = null;
    }

    /**
     * Confirmation dialog
     */

    public boolean getConfirmation(String message) {
        return getConfirmation(message, null);
    }

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

    /*
    CLEAR ALL
     */

    public void clearAll() {
        clearFile();
        clearString();
    }
}
