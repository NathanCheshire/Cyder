package cyder.games;

import com.google.common.collect.ImmutableList;
import cyder.annotations.CyderAuthor;
import cyder.annotations.SuppressCyderInspections;
import cyder.annotations.Vanilla;
import cyder.annotations.Widget;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderIcons;
import cyder.constants.CyderStrings;
import cyder.enums.CyderInspection;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.ui.button.CyderButton;
import cyder.ui.field.CyderTextField;
import cyder.ui.frame.CyderFrame;
import cyder.utils.BoundsUtil;
import cyder.utils.NumberUtil;
import cyder.utils.StringUtil;
import cyder.utils.UiUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.FileReader;

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
    private static CyderButton restButton;

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
    private static JLabel currentWordLabel;

    /**
     * The number of wrong guesses.
     */
    private static int numWrongGuesses = 1;

    /**
     * The letters that have been already guessed.
     */
    private static String chosenLetters = "";

    /**
     * Suppress default constructor.
     */
    private HangmanGame() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    @Widget(triggers = "hangman", description = "A hangman game")
    public static void showGui() {
        UiUtil.closeIfOpen(hangmanFrame);

        hangmanFrame = new CyderFrame(712, 812, CyderIcons.defaultBackground);
        hangmanFrame.setTitlePosition(CyderFrame.TitlePosition.CENTER);
        hangmanFrame.setTitle("Hangman");

        currentWordLabel = new JLabel("<html>Nathan Was Here</html>", SwingConstants.CENTER);
        currentWordLabel.setFont(CyderFonts.SEGOE_20.deriveFont(22f));
        currentWordLabel.setForeground(CyderColors.navy);
        currentWordLabel.setBounds(60, 60, 600, 60);
        hangmanFrame.getContentPane().add(currentWordLabel);

        imageLabel = new JLabel();
        imageLabel.setIcon(new ImageIcon("static/pictures/hangman/hangman.png"));
        imageLabel.setBounds(100, 50, 712, 712);
        hangmanFrame.getContentPane().add(imageLabel);

        letterField = new CyderTextField();
        letterField.setHorizontalAlignment(JTextField.CENTER);
        letterField.setBackground(Color.white);
        letterField.setBorder(new LineBorder(CyderColors.navy, 5, false));
        letterField.setKeyEventRegexMatcher("[A-Za-z]");
        letterField.setToolTipText("Enter your letter guess here [A-Z]");
        letterField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                char code = e.getKeyChar();

                if (!(Character.isAlphabetic(code) || (code == KeyEvent.VK_BACK_SPACE) || code == KeyEvent.VK_DELETE)) {
                    e.consume();
                    Toolkit.getDefaultToolkit().beep();
                } else if (letterField.getText().length() > 1) {
                    e.consume();
                    Toolkit.getDefaultToolkit().beep();
                } else {
                    letterField.setText("");
                    letterChosen(code);
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                char code = e.getKeyChar();

                if (!(Character.isAlphabetic(code) || (code == KeyEvent.VK_BACK_SPACE) || code == KeyEvent.VK_DELETE))
                    e.consume();
                else if (letterField.getText().length() > 1) {
                    e.consume();
                } else {
                    letterField.setText("");
                    letterChosen(code);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                char code = e.getKeyChar();

                if (!(Character.isAlphabetic(code)
                        || (code == KeyEvent.VK_BACK_SPACE) || code == KeyEvent.VK_DELETE))
                    e.consume();
                else if (letterField.getText().length() > 1) {
                    e.consume();
                } else {
                    letterField.setText("");
                    letterChosen(code);
                }
            }
        });
        letterField.setBounds(80, 700, 712 - 80 - 80, 40);
        hangmanFrame.getContentPane().add(letterField);

        restButton = new CyderButton("Reset");
        restButton.setFocusPainted(false);
        restButton.setBackground(CyderColors.regularRed);
        restButton.setFont(CyderFonts.SEGOE_20);
        restButton.addActionListener(e -> setup());
        restButton.setBorder(new LineBorder(CyderColors.navy, 5, false));
        restButton.setBounds(80, 750, 712 - 80 - 80, 40);
        hangmanFrame.getContentPane().add(restButton);

        hangmanFrame.finalizeAndShow();
        hangmanFrame.requestFocus();

        setup();
    }

    /**
     * The underscore used for the current word label.
     */
    private static final String UNDERSCORE = " _ ";

    /**
     * The list of words used for hangman.
     */
    private static ImmutableList<String> words;

    static {
        try (BufferedReader br = new BufferedReader(new FileReader("static/csv/hangman.csv"))) {
            String[] wordsArr = br.readLine().split(",");
            words = ImmutableList.copyOf(wordsArr);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }

    /**
     * Sets up the hangman game.
     */
    private static void setup() {
        currentWordLabel.setFont(CyderFonts.SEGOE_20);
        restButton.setText("Reset");

        letterField.setEnabled(true);

        chosenLetters = "";

        hangmanWord = words.get(NumberUtil.randInt(words.size() - 1)).toLowerCase().trim();

        currentWordLabel.setText(BoundsUtil.OPENING_HTML_TAG
                + StringUtil.fillString(hangmanWord.length(), UNDERSCORE)
                + BoundsUtil.CLOSING_HTML_TAG);

        imageLabel.setIcon(new ImageIcon("static/pictures/hangman/hangman.png"));

        numWrongGuesses = 0;
    }

    /**
     * Performs the actions necessary when a letter is chosen.
     *
     * @param letter the chosen letter
     */
    private static void letterChosen(char letter) {
        if (chosenLetters.contains(String.valueOf(letter)))
            return;

        chosenLetters += String.valueOf(letter);

        if (hangmanWord.toLowerCase().contains(String.valueOf(letter))) {
            String currentLabelText = currentWordLabel.getText().replace(" ", "")
                    .replaceAll("<.*?>", "");

            char[] wordArr = hangmanWord.toCharArray();
            char[] compArr = currentLabelText.toCharArray();

            for (int i = 0 ; i < wordArr.length ; i++) {
                if (wordArr[i] == letter)
                    compArr[i] = wordArr[i];
            }

            StringBuilder newLabelText = new StringBuilder();

            for (int i = 0 ; i < compArr.length ; i++) {
                newLabelText.append(compArr[i]);
                if (i != compArr.length - 1)
                    newLabelText.append(" ");
            }

            currentWordLabel.setText(newLabelText.toString());

            if (!currentWordLabel.getText().contains("_")) {
                currentWordLabel.setFont(CyderFonts.SEGOE_20);
                currentWordLabel.setText("<html>Good job! You guessed the word \"" + hangmanWord
                        + "\" Would you like to start again?</html>");
                letterField.setEnabled(false);

                restButton.setText("Play Again");
            }
        } else {
            if (numWrongGuesses == 7) {
                imageLabel.setIcon(new ImageIcon("static/pictures/hangman/hangman8.png"));
                currentWordLabel.setFont(CyderFonts.SEGOE_20);
                currentWordLabel.setText("<html>Game over! You were unable to guess \"" + hangmanWord
                        + "\" Would you like to start again?</html>");

                restButton.setText("Play Again");

                letterField.setEnabled(false);
            } else {
                numWrongGuesses++;
                imageLabel.setIcon(new ImageIcon("static/pictures/hangman/hangman"
                        + numWrongGuesses + ".png"));
            }
        }
    }
}
