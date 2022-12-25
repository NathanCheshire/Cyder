package cyder.games;

import com.google.common.collect.ImmutableList;
import cyder.annotations.*;
import cyder.constants.CyderFonts;
import cyder.constants.CyderRegexPatterns;
import cyder.enums.CyderInspection;
import cyder.enums.Extension;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.layouts.CyderPartitionedLayout;
import cyder.math.NumberUtil;
import cyder.strings.CyderStrings;
import cyder.strings.StringUtil;
import cyder.ui.button.CyderButton;
import cyder.ui.field.CyderTextField;
import cyder.ui.frame.CyderFrame;
import cyder.ui.label.CyderLabel;
import cyder.utils.StaticUtil;
import cyder.utils.UiUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * A java implementation of the classic Hangman game.
 */
@CyderAuthor
@Vanilla
@SuppressCyderInspections(CyderInspection.VanillaInspection)
public final class HangmanGame {
    /**
     * The frame object.
     */
    private static CyderFrame hangmanFrame;

    /**
     * The current word.
     */
    private static String hangmanWord;

    /**
     * The reset button.
     */
    private static CyderButton resetButton;

    /**
     * The field the user enters a letter in.
     */
    private static CyderTextField letterField;

    /**
     * The label the current hangman image is appended to
     */
    private static JLabel imageLabel;

    /**
     * The label displaying the current hangman word.
     */
    private static CyderLabel currentWordLabel;

    /**
     * The number of wrong guesses.
     */
    private static int numWrongGuesses;

    /**
     * The letters that have been already guessed.
     */
    private static final ArrayList<String> chosenLetters = new ArrayList<>();

    /**
     * The placeholder used for the characters on the current word label.
     */
    private static final String wordLabelCharPlaceholder = " _ ";

    /**
     * The name of the file containing the hangman words.
     */
    private static final String wordsFile = "hangman.txt";

    /**
     * The list of words used for hangman.
     */
    private static final ImmutableList<String> words;

    static {
        ArrayList<String> ret = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(StaticUtil.getStaticPath(wordsFile)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    ret.add(line);
                }
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }

        words = ImmutableList.copyOf(ret);
    }

    /**
     * The reset text for the reset button.
     */
    private static final String RESET = "Reset";

    /**
     * The play again text for the reset button.
     */
    private static final String PLAY_AGAIN = "Play again";

    /**
     * The default hangman icon.
     */
    private static final ImageIcon defaultHangmanIcon = new ImageIcon(StaticUtil.getStaticPath("hangman.png"));

    /**
     * The frame title.
     */
    private static final String HANGMAN = "Hangman";

    /**
     * The width of the hangman frame.
     */
    private static final int FRAME_WIDTH = 600;

    /**
     * The height of the hangman frame.
     */
    private static final int FRAME_HEIGHT = 800;

    /**
     * The font for the word label.
     */
    private static final Font wordLabelFont = CyderFonts.SEGOE_30.deriveFont(26f);

    /**
     * The length of the image label.
     */
    private static final int imageLabelLen = 512;

    /**
     * The spacing partition between components.
     */
    private static final int spacingLen = 3;

    /**
     * The height of the primary components, not including the image label.
     */
    private static final int componentHeight = 40;

    /**
     * Suppress default constructor.
     */
    private HangmanGame() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    @Widget(triggers = "hangman", description = "A hangman game")
    public static void showGui() {
        UiUtil.closeIfOpen(hangmanFrame);

        hangmanFrame = new CyderFrame(FRAME_WIDTH, FRAME_HEIGHT);
        hangmanFrame.setTitle(HANGMAN);

        CyderPartitionedLayout partitionedLayout = new CyderPartitionedLayout();

        currentWordLabel = new CyderLabel();
        currentWordLabel.setHorizontalAlignment(SwingConstants.CENTER);
        currentWordLabel.setFont(wordLabelFont);
        currentWordLabel.setSize(imageLabelLen, 2 * componentHeight);
        partitionedLayout.addComponentMaintainSize(currentWordLabel);

        partitionedLayout.spacer(spacingLen);

        imageLabel = new JLabel();
        imageLabel.setIcon(defaultHangmanIcon);
        imageLabel.setSize(imageLabelLen, imageLabelLen);
        partitionedLayout.addComponentMaintainSize(imageLabel);

        partitionedLayout.spacer(spacingLen);

        letterField = new CyderTextField();
        letterField.setHorizontalAlignment(JTextField.CENTER);
        letterField.setBackground(Color.white);
        letterField.setKeyEventRegexMatcher(CyderRegexPatterns.englishLettersRegex);
        letterField.setToolTipText("Enter your letter guess here");
        letterField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                onLetterFieldKeyAction(e);
            }

            @Override
            public void keyPressed(KeyEvent e) {
                onLetterFieldKeyAction(e);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                onLetterFieldKeyAction(e);
            }
        });
        letterField.setSize(imageLabelLen, componentHeight);
        partitionedLayout.addComponentMaintainSize(letterField);

        partitionedLayout.spacer(spacingLen);

        resetButton = new CyderButton(RESET);
        resetButton.addActionListener(e -> setup());
        resetButton.setSize(imageLabelLen, componentHeight);
        partitionedLayout.addComponentMaintainSize(resetButton);

        hangmanFrame.setCyderLayout(partitionedLayout);
        hangmanFrame.finalizeAndShow();
        letterField.requestFocus();

        setup();
    }

    /**
     * The logic to be invoked on any key event that occurs in the letter field.
     *
     * @param e the key event
     */
    @ForReadability
    private static void onLetterFieldKeyAction(KeyEvent e) {
        char code = e.getKeyChar();
        if (code == KeyEvent.VK_DELETE || code == KeyEvent.VK_BACK_SPACE) {
            e.consume();
            Toolkit.getDefaultToolkit().beep();
            return;
        } else if (!Character.isAlphabetic(code)) {
            e.consume();
            Toolkit.getDefaultToolkit().beep();
            return;
        }

        letterField.setText("");
        letterChosen(code);
    }

    /**
     * Sets up the hangman game.
     */
    private static void setup() {
        resetButton.setText(RESET);
        letterField.setEnabled(true);
        chosenLetters.clear();
        chooseHangmanWord();

        currentWordLabel.setText(StringUtil.fillString(hangmanWord.length(), wordLabelCharPlaceholder));
        imageLabel.setIcon(defaultHangmanIcon);

        numWrongGuesses = 0;
    }

    /**
     * Chooses a new hangman word.
     */
    private static void chooseHangmanWord() {
        hangmanWord = words.get(NumberUtil.randInt(words.size() - 1)).toLowerCase();
    }

    /**
     * Performs the actions necessary when a letter is chosen.
     *
     * @param chosenLetter the chosen letter
     */
    private static void letterChosen(char chosenLetter) {
        String letter = String.valueOf(chosenLetter);
        if (chosenLetters.contains(letter)) return;

        chosenLetters.add(letter);

        if (hangmanWord.toLowerCase().contains(letter)) {
            char[] wordChars = hangmanWord.toCharArray();
            StringBuilder labelTextBuilder = new StringBuilder();

            for (char currentLetter : wordChars) {
                if (chosenLetters.contains(String.valueOf(currentLetter))) {
                    labelTextBuilder.append(currentLetter);
                } else {
                    labelTextBuilder.append(wordLabelCharPlaceholder);
                }
            }

            String labelText = labelTextBuilder.toString();
            if (labelText.equalsIgnoreCase(hangmanWord)) {
                currentWordLabel.setText("You guessed the word \"" + hangmanWord + "\" Would you like to start again?");
                letterField.setEnabled(false);
                resetButton.setText(PLAY_AGAIN);
            } else {
                currentWordLabel.setText(labelText);
            }
        } else {
            numWrongGuesses++;
            imageLabel.setIcon(new ImageIcon("static/pictures/hangman/hangman"
                    + numWrongGuesses + Extension.PNG.getExtension()));
            if (numWrongGuesses == 8) {
                currentWordLabel.setText("Game over! You were unable to guess \"" + hangmanWord
                        + "\" Would you like to start again?");
                resetButton.setText(PLAY_AGAIN);
                letterField.setEnabled(false);
            }
        }
    }
}
