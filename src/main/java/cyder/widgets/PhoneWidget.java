package cyder.widgets;

import com.google.common.base.Preconditions;
import cyder.annotations.CyderAuthor;
import cyder.annotations.ForReadability;
import cyder.annotations.Vanilla;
import cyder.annotations.Widget;
import cyder.audio.GeneralAudioPlayer;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.exceptions.IllegalMethodException;
import cyder.strings.CyderStrings;
import cyder.strings.StringUtil;
import cyder.ui.UiUtil;
import cyder.ui.button.CyderModernButton;
import cyder.ui.button.ThemeBuilder;
import cyder.ui.field.CyderTextField;
import cyder.ui.frame.CyderFrame;
import cyder.utils.StaticUtil;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import static cyder.strings.CyderStrings.*;

/**
 * A phone number dialing widget.
 */
@Vanilla
@CyderAuthor
public final class PhoneWidget {
    /**
     * The dialing string.
     */
    private static final String DIALING = "Dialing: ";

    /**
     * The width of the widget frame.
     */
    private static final int FRAME_WIDTH = 320;

    /**
     * The height of the widget frame.
     */
    private static final int FRAME_HEIGHT = 500;

    /**
     * The string for the back button.
     */
    private static final String backText = "<<";

    /**
     * The string used for the call button.
     */
    private static final String CALL = "Call";

    /**
     * A regex for targeting anything that is not a digit.
     */
    private static final String NON_DIGITS_REGEX = "[^\\d.]";

    /**
     * The widget title.
     */
    private static final String TITLE = "Phone";

    /**
     * The button theme.
     */
    private static final ThemeBuilder theme = new ThemeBuilder()
            .setFont(CyderFonts.SEGOE_30)
            .setBackgroundColor(CyderColors.regularOrange)
            .setBorderColor(CyderColors.navy)
            .setBorderLength(5);

    /**
     * The field numbers are stored in.
     */
    private static CyderTextField numberField;

    /**
     * The current number.
     */
    private static String currentPhoneNumber;

    /**
     * The widget frame.
     */
    private static CyderFrame phoneFrame;

    /**
     * Suppress default constructor.
     */
    private PhoneWidget() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * Special numbers which when dialed trigger an audio file to play, generally with the same name as the number.
     */
    private enum SpecialNumber {
        /**
         * The 1800 number, plays 1800 by Logic.
         */
        SUICIDE_HOTLINE(18002738255L, StaticUtil.getStaticResource("1800.mp3")),

        /**
         * The 223 number, plays 223 by YNW Melly
         */
        TWO_TWO_THREE(223L, StaticUtil.getStaticResource("223.mp3"));

        /**
         * The number dialed to trigger this.
         */
        private final long number;

        /**
         * The audio file to play.
         */
        private final File audioFile;

        SpecialNumber(long number, File audioFile) {
            this.number = number;
            this.audioFile = audioFile;
        }

        /**
         * Returns the number dialed to trigger this.
         *
         * @return the number dialed to trigger this
         */
        public long getNumber() {
            return number;
        }

        /**
         * Returns the audio file to play.
         *
         * @return the audio file to play
         */
        public File getAudioFile() {
            return audioFile;
        }

        /**
         * Plays this audio file using the general audio player.
         */
        public void play() {
            GeneralAudioPlayer.playAudio(audioFile);
        }

        /**
         * Ends all special audio files if playing.
         */
        public static void endAllAudio() {
            Arrays.stream(values()).forEach(specialNumber ->
                    GeneralAudioPlayer.stopAudio(specialNumber.getAudioFile()));
        }
    }

    @Widget(triggers = "phone", description = "A phone emulating widget")
    public static void showGui() {
        UiUtil.closeIfOpen(phoneFrame);

        phoneFrame = new CyderFrame.Builder()
                .setWidth(FRAME_WIDTH)
                .setHeight(FRAME_HEIGHT)
                .setTitle(TITLE)
                .build();
        phoneFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                SpecialNumber.endAllAudio();
            }
        });

        numberField = new CyderTextField();
        numberField.setText(hash);
        numberField.setEditable(false);
        numberField.setFont(CyderFonts.SEGOE_20);
        numberField.setBounds(20, 40, 320 - 40, 40);
        phoneFrame.getContentPane().add(numberField);

        currentPhoneNumber = "";

        CyderModernButton zero = new CyderModernButton("0");
        zero.setBounds(120, 400, 80, 80);
        phoneFrame.getContentPane().add(zero);
        zero.addClickRunnable(() -> {
            currentPhoneNumber = currentPhoneNumber + zero.getText();
            refreshFieldText();
        });
        zero.setTheme(theme);

        CyderModernButton one = new CyderModernButton("1");
        one.setBounds(20, 100, 80, 80);
        phoneFrame.getContentPane().add(one);
        one.addClickRunnable(() -> {
            currentPhoneNumber = currentPhoneNumber + one.getText();
            refreshFieldText();
        });
        one.setTheme(theme);

        CyderModernButton two = new CyderModernButton("2");
        two.setBounds(120, 100, 80, 80);
        phoneFrame.getContentPane().add(two);
        two.addClickRunnable(() -> {
            currentPhoneNumber = currentPhoneNumber + two.getText();
            refreshFieldText();
        });
        two.setTheme(theme);

        CyderModernButton three = new CyderModernButton("3");
        three.setBounds(220, 100, 80, 80);
        phoneFrame.getContentPane().add(three);
        three.addClickRunnable(() -> {
            currentPhoneNumber = currentPhoneNumber + three.getText();
            refreshFieldText();
        });
        three.setTheme(theme);

        CyderModernButton four = new CyderModernButton("4");
        four.setBounds(20, 200, 80, 80);
        phoneFrame.getContentPane().add(four);
        four.addClickRunnable(() -> {
            currentPhoneNumber = currentPhoneNumber + four.getText();
            refreshFieldText();
        });
        four.setTheme(theme);

        CyderModernButton five = new CyderModernButton("5");
        five.setBounds(120, 200, 80, 80);
        phoneFrame.getContentPane().add(five);
        five.addClickRunnable(() -> {
            currentPhoneNumber = currentPhoneNumber + five.getText();
            refreshFieldText();
        });
        five.setTheme(theme);

        CyderModernButton six = new CyderModernButton("6");
        six.setBounds(220, 200, 80, 80);
        phoneFrame.getContentPane().add(six);
        six.addClickRunnable(() -> {
            currentPhoneNumber = currentPhoneNumber + six.getText();
            refreshFieldText();
        });
        six.setTheme(theme);

        CyderModernButton seven = new CyderModernButton("7");
        seven.setBounds(20, 300, 80, 80);
        phoneFrame.getContentPane().add(seven);
        seven.addClickRunnable(() -> {
            currentPhoneNumber = currentPhoneNumber + seven.getText();
            refreshFieldText();
        });
        seven.setTheme(theme);

        CyderModernButton eight = new CyderModernButton("8");
        eight.setBounds(120, 300, 80, 80);
        phoneFrame.getContentPane().add(eight);
        eight.addClickRunnable(() -> {
            currentPhoneNumber = currentPhoneNumber + eight.getText();
            refreshFieldText();
        });
        eight.setTheme(theme);

        CyderModernButton nine = new CyderModernButton("9");
        nine.setBounds(220, 300, 80, 80);
        phoneFrame.getContentPane().add(nine);
        nine.addClickRunnable(() -> {
            currentPhoneNumber = currentPhoneNumber + nine.getText();
            refreshFieldText();
        });
        nine.setTheme(theme);

        CyderModernButton back = new CyderModernButton(backText);
        back.setBounds(20, 400, 80, 80);
        phoneFrame.getContentPane().add(back);
        back.addClickRunnable(() -> {
            if (!currentPhoneNumber.isEmpty()) {
                currentPhoneNumber = currentPhoneNumber.substring(0, currentPhoneNumber.length() - 1);
                refreshFieldText();
            }
        });
        back.setTheme(theme);

        CyderModernButton dialNumber = new CyderModernButton(CALL);
        dialNumber.setBounds(220, 400, 80, 80);
        phoneFrame.getContentPane().add(dialNumber);
        dialNumber.addClickRunnable(PhoneWidget::dialNumberAction);
        dialNumber.setTheme(theme);

        phoneFrame.finalizeAndShow();
    }

    /**
     * The actions to take when the dial number button is pressed.
     */
    @ForReadability
    private static void dialNumberAction() {
        if (currentPhoneNumber.isEmpty()) return;
        if (checkForNumbers()) return;

        phoneFrame.toast(DIALING + numberField.getText());
        refreshFieldText();
    }

    /**
     * Refreshes the field text based on the current phone number.
     */
    private static void refreshFieldText() {
        numberField.setText(formatNumber(currentPhoneNumber));
    }

    /**
     * Checks the {@link SpecialNumber}s for a special number.
     *
     * @return whether a special number was found and the runnable invoked
     */
    @ForReadability
    private static boolean checkForNumbers() {
        AtomicBoolean ret = new AtomicBoolean();

        long dialedNumber = Long.parseLong(StringUtil.getTrimmedText(numberField.getText())
                .replaceAll(NON_DIGITS_REGEX, ""));
        Arrays.stream(SpecialNumber.values()).filter(specialNumber -> specialNumber.getNumber() == dialedNumber)
                .findFirst().ifPresent(specialNumber -> {
                    specialNumber.play();
                    ret.set(true);
                    currentPhoneNumber = "";
                    refreshFieldText();
                });

        return ret.get();
    }

    /**
     * Returns the number formatted based on the current number
     * of digits contained in the phone number.
     *
     * @param num the current phone number
     * @return the phone number formatted
     */
    private static String formatNumber(String num) {
        Preconditions.checkNotNull(num);

        if (num.isEmpty())

            num = num.replaceAll(NON_DIGITS_REGEX, "");
        int length = num.length();

        if (length == 0) {
            return hash;
        } else if (length < 5) {
            return num;
        } else if (length == 5) {
            return num.charAt(0) + dash + num.substring(1, 5);
        } else if (length == 6) {
            return num.substring(0, 2) + dash + num.substring(2, 6);
        } else if (length == 7) {
            return num.substring(0, 3) + dash + num.substring(3, 7);
        } else if (length == 8) {
            return openingParenthesis + num.charAt(0) + closingParenthesis + space
                    + num.substring(1, 4) + space + num.substring(4, 8);
        } else if (length == 9) {
            return openingParenthesis + num.substring(0, 2) + closingParenthesis + space
                    + num.substring(2, 5) + space + num.substring(5, 9);
        } else if (length == 10) {
            return openingParenthesis + num.substring(0, 3) + closingParenthesis + space
                    + num.substring(3, 6) + space + num.substring(6, 10);
        } else {
            if (length > 15) {
                currentPhoneNumber = numberField.getText();
                return numberField.getText();
            }

            String leadingDigits = num.substring(0, length - 10);
            int offset = leadingDigits.length();

            return (leadingDigits + space + openingParenthesis + num.substring(offset, 3 + offset) +
                    closingParenthesis +
                    space
                    + num.substring(3 + offset, 6 + offset) + space + num.substring(6 + offset, length));
        }
    }
}
