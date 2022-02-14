package cyder.games;

import cyder.annotations.Widget;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.constants.CyderIcons;
import cyder.constants.CyderStrings;
import cyder.genesis.CyderCommon;
import cyder.handlers.internal.Logger;
import cyder.ui.CyderButton;
import cyder.ui.CyderFrame;
import cyder.ui.CyderLabel;
import cyder.utilities.ReflectionUtil;

import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * A tic tac toe game widget.
 */
public class TTTGame {
    /**
     * The CyderFrame instancer to use to ensure no other games exist with one Cyder instance.
     */
    private static CyderFrame tttFrame;
    
    // The tic tac toe buttons
    private static CyderButton ttt9;
    private static CyderButton ttt8;
    private static CyderButton ttt7;
    private static CyderButton ttt6;
    private static CyderButton ttt5;
    private static CyderButton ttt4;
    private static CyderButton ttt3;
    private static CyderButton ttt2;
    private static CyderButton ttt1;
    private static CyderButton tttReset;

    /**
     * The foreground color used for teh tic tac toe buttons.
     */
    public static final Color tttblue = new Color(71, 81, 117);

    /**
     * Player enums.
     */
    private enum Player {
        EXES, OHS
    }

    /**
     * The current player.
     */
    private static Player currentPlayer;

    /**
     * The information label.
     */
    private static CyderLabel infoLabel;

    /**
     * Prevent instantiation of class.
     */
    private TTTGame() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    @Widget(trigger = {"ttt", "tic tac toe"}, description = "A TicTacToe widget")
    public static void showGUI() {
        Logger.log(Logger.Tag.WIDGET_OPENED, "TTT");

        if (tttFrame != null)
            tttFrame.dispose();

        tttFrame = new CyderFrame(400,500, CyderIcons.defaultBackground);
        tttFrame.setTitlePosition(CyderFrame.TitlePosition.CENTER);
        tttFrame.setTitle("TicTacToe");

        infoLabel = new CyderLabel();
        infoLabel.setFont(CyderFonts.segoe20);
        infoLabel.setForeground(CyderColors.navy);
        infoLabel.setBounds(20,30, tttFrame.getWidth(),30);
        tttFrame.getContentPane().add(infoLabel);

        ttt1 = new CyderButton("");
        ttt1.setPreferredSize(new Dimension(60, 60));
        ttt1.setColors(CyderColors.vanila);
        ttt1.setFocusPainted(false);
        ttt1.setBackground(CyderColors.vanila);
        ttt1.setFont(CyderFonts.segoe30);
        ttt1.setBorder(new LineBorder(CyderColors.navy,5,false));
        ttt1.addActionListener(e -> {
            if (ttt1.getText().isEmpty()) {
                if (currentPlayer == Player.EXES) {
                    ttt1.setText("X");
                    ttt1.setFont(CyderFonts.segoe30);
                    currentPlayer = Player.OHS;

                } else {
                    ttt1.setText("O");
                    ttt1.setForeground(tttblue);
                    ttt1.setFont(CyderFonts.segoe30);
                    currentPlayer = Player.EXES;
                }

                updateTurnLabel();
                checkForWin();
            }
        });

        ttt1.setBounds(25, 75, 100, 100);
        tttFrame.getContentPane().add(ttt1);

        ttt2 = new CyderButton("");
        ttt2.setColors(CyderColors.vanila);
        ttt2.setPreferredSize(new Dimension(60, 60));
        ttt2.setFocusPainted(false);
        ttt2.setBackground(CyderColors.vanila);
        ttt2.setFont(CyderFonts.segoe30);
        ttt2.setBorder(new LineBorder(CyderColors.navy,5,false));
        ttt2.addActionListener(e -> {
            if (ttt2.getText().isEmpty()) {
                if (currentPlayer == Player.EXES) {
                    ttt2.setText("X");
                    ttt2.setFont(CyderFonts.segoe30);
                    currentPlayer = Player.OHS;

                } else {
                    ttt2.setText("O");
                    ttt2.setForeground(tttblue);
                    ttt2.setFont(CyderFonts.segoe30);
                    currentPlayer = Player.EXES;

                }

                updateTurnLabel();
                checkForWin();
            }
        });

        ttt2.setBounds(150, 75, 100, 100);
        tttFrame.getContentPane().add(ttt2);

        ttt3 = new CyderButton("");
        ttt3.setColors(CyderColors.vanila);
        ttt3.setPreferredSize(new Dimension(60, 60));
        ttt3.setFocusPainted(false);
        ttt3.setBackground(CyderColors.vanila);
        ttt3.setFont(CyderFonts.segoe30);
        ttt3.setBorder(new LineBorder(CyderColors.navy,5,false));
        ttt3.addActionListener(e -> {
            if (ttt3.getText().isEmpty()) {
                if (currentPlayer == Player.EXES) {
                    ttt3.setText("X");
                    ttt3.setFont(CyderFonts.segoe30);
                    currentPlayer = Player.OHS;

                } else {
                    ttt3.setText("O");
                    ttt3.setForeground(tttblue);
                    ttt3.setFont(CyderFonts.segoe30);
                    currentPlayer = Player.EXES;

                }

                updateTurnLabel();
                checkForWin();
            }
        });

        ttt3.setBounds(275, 75, 100, 100);
        tttFrame.getContentPane().add(ttt3);

        ttt4 = new CyderButton("");
        ttt4.setPreferredSize(new Dimension(60, 60));
        ttt4.setFocusPainted(false);
        ttt4.setColors(CyderColors.vanila);
        ttt4.setBackground(CyderColors.vanila);
        ttt4.setBorder(new LineBorder(CyderColors.navy,5,false));
        ttt4.setFont(CyderFonts.segoe30);
        ttt4.addActionListener(e -> {
            if (ttt4.getText().isEmpty()) {
                if (currentPlayer == Player.EXES) {
                    ttt4.setText("X");
                    ttt4.setFont(CyderFonts.segoe30);
                    currentPlayer = Player.OHS;
                } else {
                    ttt4.setText("O");
                    ttt4.setForeground(tttblue);
                    ttt4.setFont(CyderFonts.segoe30);
                    currentPlayer = Player.EXES;
                }

                updateTurnLabel();
                checkForWin();
            }
        });

        ttt4.setBounds(25, 200, 100, 100);
        tttFrame.getContentPane().add(ttt4);

        ttt5 = new CyderButton("");
        ttt5.setColors(CyderColors.vanila);
        ttt5.setPreferredSize(new Dimension(60, 60));
        ttt5.setFocusPainted(false);
        ttt5.setBackground(CyderColors.vanila);
        ttt5.setBorder(new LineBorder(CyderColors.navy,5,false));
        ttt5.setFont(CyderFonts.segoe30);
        ttt5.addActionListener(e -> {
            if (ttt5.getText().isEmpty()) {
                if (currentPlayer == Player.EXES) {
                    ttt5.setText("X");
                    ttt5.setFont(CyderFonts.segoe30);
                    currentPlayer = Player.OHS;
                } else {
                    ttt5.setText("O");
                    ttt5.setForeground(tttblue);
                    ttt5.setFont(CyderFonts.segoe30);
                    currentPlayer = Player.EXES;
                }

                updateTurnLabel();
                checkForWin();
            }
        });

        ttt5.setBounds(150, 200, 100, 100);
        tttFrame.getContentPane().add(ttt5);

        ttt6 = new CyderButton("");
        ttt6.setPreferredSize(new Dimension(60, 60));
        ttt6.setFocusPainted(false);
        ttt6.setBackground(CyderColors.vanila);
        ttt6.setColors(CyderColors.vanila);
        ttt6.setBorder(new LineBorder(CyderColors.navy,5,false));
        ttt6.setFont(CyderFonts.segoe30);
        ttt6.addActionListener(e -> {
            if (ttt6.getText().isEmpty()) {
                if (currentPlayer == Player.EXES) {
                    ttt6.setText("X");
                    ttt6.setFont(CyderFonts.segoe30);
                    currentPlayer = Player.OHS;

                } else {
                    ttt6.setText("O");
                    ttt6.setForeground(tttblue);
                    ttt6.setFont(CyderFonts.segoe30);
                    currentPlayer = Player.EXES;
                }

                updateTurnLabel();
                checkForWin();
            }
        });

        ttt6.setBounds(275, 200, 100, 100);
        tttFrame.getContentPane().add(ttt6);

        ttt7 = new CyderButton("");
        ttt7.setPreferredSize(new Dimension(60, 60));
        ttt7.setFocusPainted(false);
        ttt7.setColors(CyderColors.vanila);
        ttt7.setBackground(CyderColors.vanila);
        ttt7.setBorder(new LineBorder(CyderColors.navy,5,false));
        ttt7.setFont(CyderFonts.segoe30);
        ttt7.addActionListener(e -> {
            if (ttt7.getText().isEmpty()) {
                if (currentPlayer == Player.EXES) {
                    ttt7.setText("X");
                    ttt7.setFont(CyderFonts.segoe30);
                    currentPlayer = Player.OHS;
                } else {
                    ttt7.setText("O");
                    ttt7.setForeground(tttblue);
                    ttt7.setFont(CyderFonts.segoe30);
                    currentPlayer = Player.EXES;
                }

                updateTurnLabel();
                checkForWin();
            }
        });

        ttt7.setBounds(25, 325, 100, 100);
        tttFrame.getContentPane().add(ttt7);

        ttt8 = new CyderButton("");
        ttt8.setPreferredSize(new Dimension(60, 60));
        ttt8.setFocusPainted(false);
        ttt8.setBackground(CyderColors.vanila);
        ttt8.setColors(CyderColors.vanila);
        ttt8.setFont(CyderFonts.segoe30);
        ttt8.setBorder(new LineBorder(CyderColors.navy,5,false));
        ttt8.addActionListener(e -> {
            if (ttt8.getText().isEmpty()) {
                if (currentPlayer == Player.EXES) {
                    ttt8.setText("X");
                    ttt8.setFont(CyderFonts.segoe30);
                    currentPlayer = Player.OHS;
                } else {
                    ttt8.setText("O");
                    ttt8.setForeground(tttblue);
                    ttt8.setFont(CyderFonts.segoe30);
                    currentPlayer = Player.EXES;
                }

                updateTurnLabel();
                checkForWin();
            }
        });

        ttt8.setBounds(150, 325, 100, 100);
        tttFrame.getContentPane().add(ttt8);

        ttt9 = new CyderButton("");
        ttt9.setColors(CyderColors.vanila);
        ttt9.setPreferredSize(new Dimension(60, 60));
        ttt9.setFocusPainted(false);
        ttt9.setBackground(CyderColors.vanila);
        ttt9.setBorder(new LineBorder(CyderColors.navy,5,false));
        ttt9.setFont(CyderFonts.segoe30);
        ttt9.addActionListener(e -> {
            if (ttt9.getText().isEmpty()) {
                if (currentPlayer == Player.EXES) {
                    ttt9.setText("X");
                    currentPlayer = Player.OHS;
                } else {
                    ttt9.setText("O");
                    ttt9.setForeground(tttblue);
                    ttt9.setFont(CyderFonts.segoe30);
                    currentPlayer = Player.OHS;
                }

                updateTurnLabel();
                checkForWin();
            }
        });

        ttt9.setBounds(275, 325, 100, 100);
        tttFrame.getContentPane().add(ttt9);

        tttReset = new CyderButton("Reset");
        tttReset.setFocusPainted(false);
        tttReset.setBackground(CyderColors.regularRed);
        tttReset.setFont(CyderFonts.segoe30);
        tttReset.setBorder(new LineBorder(CyderColors.navy,5,false));
        tttReset.addActionListener(e -> resetBoard());

        tttReset.setBounds(20, 440, 360, 40);
        tttFrame.getContentPane().add(tttReset);

        tttFrame.setVisible(true);
        tttFrame.setLocationRelativeTo(CyderCommon.getDominantFrame());

        currentPlayer = Player.EXES;

        updateTurnLabel();
    }

    /**
     * Updates the player turn label.
     */
    private static void updateTurnLabel() {
        if (currentPlayer == Player.EXES) {
            infoLabel.setText("Player Turn: X");
        } else {
            infoLabel.setText("Player Turn: O");
        }
    }

    /**
     * Resets the ttt board
     */
    private static void resetBoard() {
        currentPlayer = Player.EXES;
        updateTurnLabel();

        ttt1.setText("");
        ttt2.setText("");
        ttt3.setText("");
        ttt4.setText("");
        ttt5.setText("");
        ttt6.setText("");
        ttt7.setText("");
        ttt8.setText("");
        ttt9.setText("");
    }

    /**
     * Checks for a game win.
     */
    private static void checkForWin() {
        if (checkPlayerWin("X")) {
            tttFrame.notify("X's have won the game! Congratulations!");
            tttReset.doClick();
        } else if (checkPlayerWin("O")) {
            tttFrame.notify("O's have won the game! Congratulations!");
            tttReset.doClick();
        } else if (isBoardFull()) {
            tttFrame.notify("The game ended with no winners.");
            tttReset.doClick();
        }
    }

    /**
     * Returns whether the provided player has won the game.
     * 
     * @param Player the player to check for winning
     * @return whether the provided player has won
     */
    private static boolean checkPlayerWin(String Player) {
        if (ttt1.getText().equals(Player) && ttt2.getText().equals(Player) && ttt3.getText().equals(Player)) {
            return true;
        } else if (ttt4.getText().equals(Player) && ttt5.getText().equals(Player) && ttt6.getText().equals(Player)) {
            return true;
        } else if (ttt7.getText().equals(Player) && ttt8.getText().equals(Player) && ttt9.getText().equals(Player)) {
            return true;
        } else if (ttt1.getText().equals(Player) && ttt4.getText().equals(Player) && ttt7.getText().equals(Player)) {
            return true;
        } else if (ttt2.getText().equals(Player) && ttt5.getText().equals(Player) && ttt8.getText().equals(Player)) {
            return true;
        } else if (ttt3.getText().equals(Player) && ttt6.getText().equals(Player) && ttt9.getText().equals(Player)) {
            return true;
        } else if (ttt1.getText().equals(Player) && ttt5.getText().equals(Player) && ttt9.getText().equals(Player)) {
            return true;
        } else return ttt3.getText().equals(Player) && ttt5.getText().equals(Player) && ttt7.getText().equals(Player);
    }

    private static boolean isBoardFull() {
        return !ttt1.getText().isEmpty() && !ttt2.getText().isEmpty() &&
                !ttt3.getText().isEmpty() && !ttt4.getText().isEmpty() &&
                !ttt5.getText().isEmpty() && !ttt6.getText().isEmpty() &&
                !ttt7.getText().isEmpty() && !ttt8.getText().isEmpty() && !ttt9.getText().isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return ReflectionUtil.commonCyderToString(this);
    }
}
