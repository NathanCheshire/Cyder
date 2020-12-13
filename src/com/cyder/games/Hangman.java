package com.cyder.games;

import com.cyder.ui.CyderButton;
import com.cyder.ui.CyderFrame;
import com.cyder.utilities.GeneralUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.FileReader;

public class Hangman {
    private CyderFrame HangmanFrame;
    private GeneralUtil hangmanGeneralUtil;
    private String HangmanWord;
    private CyderButton HangmanReset;
    private JTextField letterField;
    private JLabel HangmanImageLabel;
    private JLabel HangmanLabel;

    private int HangmanWrongGuesses = 1;
    private String chosenLetters = "";

    public void startHangman() {
        hangmanGeneralUtil = new GeneralUtil();

        if (HangmanFrame != null)
            HangmanFrame.closeAnimation();

        HangmanFrame = new CyderFrame(712,812,new ImageIcon("src/com/cyder/io/pictures/DebugBackground.png"));
        HangmanFrame.setTitlePosition(CyderFrame.CENTER_TITLE);
        HangmanFrame.setTitle("Hangman");

        HangmanLabel = new JLabel("<html>Nathan Was Here</html>",SwingConstants.CENTER);
        HangmanLabel.setFont(hangmanGeneralUtil.weatherFontSmall.deriveFont(22f));
        HangmanLabel.setForeground(hangmanGeneralUtil.navy);
        HangmanLabel.setBounds(60,60,600,60);
        HangmanFrame.getContentPane().add(HangmanLabel);

        HangmanImageLabel = new JLabel();
        HangmanImageLabel.setIcon(new ImageIcon("src/com/cyder/io/pictures/hangman.png"));
        HangmanImageLabel.setBounds(100,50,712,712);
        HangmanFrame.getContentPane().add(HangmanImageLabel);

        letterField = new JTextField(40);
        letterField.setBorder(new LineBorder(hangmanGeneralUtil.navy,5,false));
        letterField.setBackground(Color.WHITE);
        letterField.setSelectionColor(hangmanGeneralUtil.selectionColor);
        letterField.setForeground(hangmanGeneralUtil.navy);
        letterField.setFont(hangmanGeneralUtil.weatherFontSmall);
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
                    LetterChosen(code);
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
                    LetterChosen(code);
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
                    LetterChosen(code);
                }
            }
        });
        letterField.setBounds(80,700,712 - 80 - 80, 40);
        HangmanFrame.getContentPane().add(letterField);

        HangmanReset = new CyderButton("Reset");
        HangmanReset.setFocusPainted(false);
        HangmanReset.setBackground(hangmanGeneralUtil.regularRed);
        HangmanReset.setFont(hangmanGeneralUtil.weatherFontSmall);
        HangmanReset.addActionListener(e -> setup());
        HangmanReset.setBorder(new LineBorder(hangmanGeneralUtil.navy,5,false));
        HangmanReset.setColors(hangmanGeneralUtil.regularRed);
        HangmanReset.setBounds(80,750,712 - 80 - 80, 40);
        HangmanFrame.getContentPane().add(HangmanReset);

        HangmanFrame.setLocationRelativeTo(null);
        HangmanFrame.setVisible(true);
        HangmanFrame.requestFocus();

        setup();
    }

    private void setup() {
        HangmanLabel.setFont(hangmanGeneralUtil.weatherFontSmall);
        HangmanReset.setText("Reset");

        letterField.setEnabled(true);

        chosenLetters = "";

        try (BufferedReader br = new BufferedReader(new FileReader("src/com/cyder/io/text/hangman.csv"))) {
            String[] doc = br.readLine().split(",");
            HangmanWord = doc[hangmanGeneralUtil.randInt(0, doc.length - 1)].toLowerCase();

        }

        catch (Exception e) {
            hangmanGeneralUtil.handle(e);
        }

        HangmanLabel.setText("<html>" + hangmanGeneralUtil.fillString(HangmanWord.length(), " _ ") + "</html>");

        HangmanImageLabel.setIcon(new ImageIcon("src/com/cyder/io/pictures/hangman.png"));

        HangmanWrongGuesses = 0;
    }

    private void LetterChosen(char letter) {
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
                if (i != compArr.length - 1 && i != 0)
                    newLabelText += " ";
            }

            HangmanLabel.setText(newLabelText);

            if (!HangmanLabel.getText().contains("_")) {
                HangmanLabel.setFont(hangmanGeneralUtil.weatherFontSmall);
                HangmanLabel.setText("<html>Good job! You guessed the word \"" + HangmanWord + ".\" Would you like to play again?</html>");
                letterField.setEnabled(false);

                HangmanReset.setText("Play Again");
            }
        }

        else {
            if (HangmanWrongGuesses == 7) {
                HangmanImageLabel.setIcon(new ImageIcon("src/com/cyder/io/pictures/hangman8.png"));
                HangmanLabel.setFont(hangmanGeneralUtil.weatherFontSmall);
                HangmanLabel.setText("<html>Game over! You were unable to guess \"" + HangmanWord + ".\" Would you like to play again?</html>");

                HangmanReset.setText("Play Again");

                letterField.setEnabled(false);
            }

            else {
                HangmanWrongGuesses++;
                HangmanImageLabel.setIcon(new ImageIcon("src/com/cyder/io/pictures/hangman" + HangmanWrongGuesses + ".png"));
            }
        }
    }

}
