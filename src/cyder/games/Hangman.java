package cyder.games;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.consts.CyderImages;
import cyder.genesis.GenesisShare;
import cyder.handler.ErrorHandler;
import cyder.ui.CyderButton;
import cyder.ui.CyderFrame;
import cyder.ui.CyderTextField;
import cyder.utilities.NumberUtil;
import cyder.utilities.StringUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.FileReader;

public class Hangman {
    private CyderFrame HangmanFrame;
    private String HangmanWord;
    private CyderButton HangmanReset;
    private CyderTextField letterField;
    private JLabel HangmanImageLabel;
    private JLabel HangmanLabel;

    private int HangmanWrongGuesses = 1;
    private String chosenLetters = "";

    public void startHangman() {
        if (HangmanFrame != null)
            HangmanFrame.closeAnimation();

        HangmanFrame = new CyderFrame(712,812, CyderImages.defaultBackground);
        HangmanFrame.setTitlePosition(CyderFrame.TitlePosition.CENTER);
        HangmanFrame.setTitle("Hangman");

        HangmanLabel = new JLabel("<html>Nathan Was Here</html>",SwingConstants.CENTER);
        HangmanLabel.setFont(CyderFonts.weatherFontSmall.deriveFont(22f));
        HangmanLabel.setForeground(CyderColors.navy);
        HangmanLabel.setBounds(60,60,600,60);
        HangmanFrame.getContentPane().add(HangmanLabel);

        HangmanImageLabel = new JLabel();
        HangmanImageLabel.setIcon(new ImageIcon("sys/pictures/hangman/hangman.png"));
        HangmanImageLabel.setBounds(100,50,712,712);
        HangmanFrame.getContentPane().add(HangmanImageLabel);

        letterField = new CyderTextField(0);
        letterField.setBackground(Color.white);
        letterField.setBorder(new LineBorder(CyderColors.navy,5,false));
        letterField.setRegexMatcher("[A-Za-z]*");
        letterField.setToolTipText("Enter your letter guess here [A-Z]");
        letterField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                char code = e.getKeyChar();

                if(!(Character.isAlphabetic(code) ||  (code == KeyEvent.VK_BACK_SPACE) ||  code == KeyEvent.VK_DELETE )) {
                    e.consume();
                    Toolkit.getDefaultToolkit().beep();
                }

                else if (letterField.getText().length() > 1) {
                    e.consume();
                    Toolkit.getDefaultToolkit().beep();
                }

                else {
                    letterField.setText("");
                    letterChosen(code);
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                char code = e.getKeyChar();

                if(!(Character.isAlphabetic(code) ||  (code == KeyEvent.VK_BACK_SPACE) ||  code == KeyEvent.VK_DELETE ))
                    e.consume();
                else if (letterField.getText().length() > 1) {
                    e.consume();
                }

                else {
                    letterField.setText("");
                    letterChosen(code);
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                char code = e.getKeyChar();

                if(!(Character.isAlphabetic(code) ||  (code == KeyEvent.VK_BACK_SPACE) ||  code == KeyEvent.VK_DELETE ))
                    e.consume();
                else if (letterField.getText().length() > 1) {
                    e.consume();
                }

                else {
                    letterField.setText("");
                    letterChosen(code);
                }
            }
        });
        letterField.setBounds(80,700,712 - 80 - 80, 40);
        HangmanFrame.getContentPane().add(letterField);

        HangmanReset = new CyderButton("Reset");
        HangmanReset.setFocusPainted(false);
        HangmanReset.setBackground(CyderColors.regularRed);
        HangmanReset.setFont(CyderFonts.weatherFontSmall);
        HangmanReset.addActionListener(e -> setup());
        HangmanReset.setBorder(new LineBorder(CyderColors.navy,5,false));
        HangmanReset.setColors(CyderColors.regularRed);
        HangmanReset.setBounds(80,750,712 - 80 - 80, 40);
        HangmanFrame.getContentPane().add(HangmanReset);

        HangmanFrame.setLocationRelativeTo(GenesisShare.getDominantFrame());
        HangmanFrame.setVisible(true);
        HangmanFrame.requestFocus();

        setup();
    }

    private void setup() {
        HangmanLabel.setFont(CyderFonts.weatherFontSmall);
        HangmanReset.setText("Reset");

        letterField.setEnabled(true);

        chosenLetters = "";

        try (BufferedReader br = new BufferedReader(new FileReader("sys/text/hangman.csv"))) {
            String[] doc = br.readLine().split(",");
            HangmanWord = doc[NumberUtil.randInt(0, doc.length - 1)].toLowerCase().trim();

        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }

        HangmanLabel.setText("<html>" + StringUtil.fillString(HangmanWord.length(), " _ ") + "</html>");

        HangmanImageLabel.setIcon(new ImageIcon("sys/pictures/hangman/hangman.png"));

        HangmanWrongGuesses = 0;
    }

    private void letterChosen(char letter) {
        if (chosenLetters.contains(String.valueOf(letter)))
            return;

        chosenLetters += String.valueOf(letter);

        if (HangmanWord.toLowerCase().contains(String.valueOf(letter))) {
            String currentLabelText = HangmanLabel.getText().replace(" ","").replaceAll("<.*?>", "");

            char[] wordArr = HangmanWord.toCharArray();
            char[] compArr = currentLabelText.toCharArray();

            for (int i = 0 ; i < wordArr.length ; i++) {
                if (wordArr[i] == letter)
                    compArr[i] = wordArr[i];
            }

            String newLabelText = "";

            for (int i = 0 ; i < compArr.length ; i++) {
                newLabelText += compArr[i];
                if (i != compArr.length - 1)
                    newLabelText += " ";
            }

            HangmanLabel.setText(newLabelText);

            if (!HangmanLabel.getText().contains("_")) {
                HangmanLabel.setFont(CyderFonts.weatherFontSmall);
                HangmanLabel.setText("<html>Good job! You guessed the word \"" + HangmanWord + "\" Would you like to startAudio again?</html>");
                letterField.setEnabled(false);

                HangmanReset.setText("Play Again");
            }
        }

        else {
            if (HangmanWrongGuesses == 7) {
                HangmanImageLabel.setIcon(new ImageIcon("sys/pictures/hangman/hangman8.png"));
                HangmanLabel.setFont(CyderFonts.weatherFontSmall);
                HangmanLabel.setText("<html>Game over! You were unable to guess \"" + HangmanWord + "\" Would you like to startAudio again?</html>");

                HangmanReset.setText("Play Again");

                letterField.setEnabled(false);
            }

            else {
                HangmanWrongGuesses++;
                HangmanImageLabel.setIcon(new ImageIcon("sys/pictures/hangman/hangman" + HangmanWrongGuesses + ".png"));
            }
        }
    }

    @Override
    public String toString() {
        return "Hangman object, hash=" + this.hashCode();
    }
}
