package cyder.games;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;

import cyder.consts.CyderImages;
import cyder.consts.CyderStrings;
import cyder.genesis.GenesisShare;
import cyder.ui.CyderFrame;
import cyder.ui.CyderButton;
import cyder.utilities.ImageUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class TTTGame {
    private static CyderFrame tttFrame;

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

    private static int CurrentPlayerTurn;

    public static final Color tttblue = new Color(71, 81, 117);

    private static final int PlayerX = 0;
    private static final int PlayerO = 1;

    private static JLabel tttLabel;

    private TTTGame() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    public static void showGUI() {
        if (tttFrame != null)
            tttFrame.dispose();

        tttFrame = new CyderFrame(400,500, CyderImages.defaultBackground);
        tttFrame.setTitlePosition(CyderFrame.TitlePosition.CENTER);
        tttFrame.setTitle("TicTacToe");

        tttLabel = new JLabel();
        tttLabel.setFont(CyderFonts.segoe20);
        tttLabel.setForeground(CyderColors.navy);
        tttLabel.setBounds(ImageUtil.xOffsetForCenterJLabel(400,"Tic Tac Toe"),30,
                ImageUtil.xOffsetForCenterJLabel(400,tttFrame.getTitle()) * 2,30);
        tttFrame.getContentPane().add(tttLabel);

        ttt1 = new CyderButton("");
        ttt1.setPreferredSize(new Dimension(60, 60));
        ttt1.setColors(CyderColors.vanila);
        ttt1.setFocusPainted(false);
        ttt1.setBackground(CyderColors.vanila);
        ttt1.setFont(CyderFonts.segoe30);
        ttt1.setBorder(new LineBorder(CyderColors.navy,5,false));
        ttt1.addActionListener(e -> {
            if (ttt1.getText().isEmpty()) {
                if (CurrentPlayerTurn == PlayerX) {
                    ttt1.setText("X");

                    ttt1.setFont(CyderFonts.segoe30);

                    CurrentPlayerTurn = PlayerO;

                }

                else {
                    ttt1.setText("O");

                    ttt1.setForeground(tttblue);

                    ttt1.setFont(CyderFonts.segoe30);

                    CurrentPlayerTurn = PlayerX;

                }
                UpdatePlayerTurnLabel();
                TTTCheckWin();
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
                if (CurrentPlayerTurn == PlayerX) {
                    ttt2.setText("X");

                    ttt2.setFont(CyderFonts.segoe30);

                    CurrentPlayerTurn = PlayerO;

                }

                else {
                    ttt2.setText("O");

                    ttt2.setForeground(tttblue);

                    ttt2.setFont(CyderFonts.segoe30);

                    CurrentPlayerTurn = PlayerX;

                }
                UpdatePlayerTurnLabel();
                TTTCheckWin();
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
                if (CurrentPlayerTurn == PlayerX) {
                    ttt3.setText("X");

                    ttt3.setFont(CyderFonts.segoe30);

                    CurrentPlayerTurn = PlayerO;

                }

                else {
                    ttt3.setText("O");

                    ttt3.setForeground(tttblue);

                    ttt3.setFont(CyderFonts.segoe30);

                    CurrentPlayerTurn = PlayerX;

                }
                UpdatePlayerTurnLabel();
                TTTCheckWin();
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
                if (CurrentPlayerTurn == PlayerX) {
                    ttt4.setText("X");

                    ttt4.setFont(CyderFonts.segoe30);

                    CurrentPlayerTurn = PlayerO;

                }

                else {
                    ttt4.setText("O");

                    ttt4.setForeground(tttblue);

                    ttt4.setFont(CyderFonts.segoe30);

                    CurrentPlayerTurn = PlayerX;

                }
                UpdatePlayerTurnLabel();
                TTTCheckWin();
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
                if (CurrentPlayerTurn == PlayerX) {
                    ttt5.setText("X");

                    ttt5.setFont(CyderFonts.segoe30);

                    CurrentPlayerTurn = PlayerO;

                }

                else {
                    ttt5.setText("O");

                    ttt5.setForeground(tttblue);

                    ttt5.setFont(CyderFonts.segoe30);

                    CurrentPlayerTurn = PlayerX;

                }
                UpdatePlayerTurnLabel();
                TTTCheckWin();
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
                if (CurrentPlayerTurn == PlayerX) {
                    ttt6.setText("X");

                    ttt6.setFont(CyderFonts.segoe30);

                    CurrentPlayerTurn = PlayerO;

                }

                else {
                    ttt6.setText("O");

                    ttt6.setForeground(tttblue);

                    ttt6.setFont(CyderFonts.segoe30);

                    CurrentPlayerTurn = PlayerX;

                }
                UpdatePlayerTurnLabel();
                TTTCheckWin();
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
                if (CurrentPlayerTurn == PlayerX) {
                    ttt7.setText("X");

                    ttt7.setFont(CyderFonts.segoe30);

                    CurrentPlayerTurn = PlayerO;

                }

                else {
                    ttt7.setText("O");

                    ttt7.setForeground(tttblue);

                    ttt7.setFont(CyderFonts.segoe30);

                    CurrentPlayerTurn = PlayerX;

                }
                UpdatePlayerTurnLabel();
                TTTCheckWin();
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
                if (CurrentPlayerTurn == PlayerX) {
                    ttt8.setText("X");

                    ttt8.setFont(CyderFonts.segoe30);

                    CurrentPlayerTurn = PlayerO;

                }

                else {
                    ttt8.setText("O");

                    ttt8.setForeground(tttblue);

                    ttt8.setFont(CyderFonts.segoe30);

                    CurrentPlayerTurn = PlayerX;

                }
                UpdatePlayerTurnLabel();
                TTTCheckWin();
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
                if (CurrentPlayerTurn == PlayerX) {
                    ttt9.setText("X");

                    CurrentPlayerTurn = PlayerO;

                }

                else {
                    ttt9.setText("O");

                    ttt9.setForeground(tttblue);

                    ttt9.setFont(CyderFonts.segoe30);

                    CurrentPlayerTurn = PlayerX;

                }
                UpdatePlayerTurnLabel();
                TTTCheckWin();
            }
        });

        ttt9.setBounds(275, 325, 100, 100);
        tttFrame.getContentPane().add(ttt9);

        tttReset = new CyderButton("Reset");
        tttReset.setFocusPainted(false);
        tttReset.setBackground(CyderColors.regularRed);
        tttReset.setFont(CyderFonts.segoe30);
        tttReset.setBorder(new LineBorder(CyderColors.navy,5,false));
        tttReset.addActionListener(e -> TTTBoardReset());

        tttReset.setBounds(20, 440, 360, 40);
        tttFrame.getContentPane().add(tttReset);

        tttFrame.setVisible(true);
        tttFrame.setLocationRelativeTo(GenesisShare.getDominantFrame());

        CurrentPlayerTurn = PlayerX;

        UpdatePlayerTurnLabel();
    }

    private static void UpdatePlayerTurnLabel() {
        if (CurrentPlayerTurn == PlayerX) {
            tttLabel.setText("Player Turn: X");
        }

        else {
            tttLabel.setText("Player Turn: O");
        }
    }

    private static void TTTBoardReset() {
        CurrentPlayerTurn = PlayerX;
        UpdatePlayerTurnLabel();

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

    private static void TTTCheckWin() {
        if (HasPlayerWon("X")) {
            tttFrame.notify("X's have won the game! Congratulations!");
            tttReset.doClick();
        }

        else if (HasPlayerWon("O")) {
            tttFrame.notify("O's have won the game! Congratulations!");
            tttReset.doClick();
        }

        else if (TTTBoardIsFull()) {
            tttFrame.notify("The game ended with no winners.");
            tttReset.doClick();
        }
    }

    private static boolean HasPlayerWon(String Player) {
        if (ttt1.getText().equals(Player) && ttt2.getText().equals(Player) && ttt3.getText().equals(Player)) {
            return true;
        }

        if (ttt4.getText().equals(Player) && ttt5.getText().equals(Player) && ttt6.getText().equals(Player)) {
            return true;
        }

        if (ttt7.getText().equals(Player) && ttt8.getText().equals(Player) && ttt9.getText().equals(Player)) {
            return true;
        }

        if (ttt1.getText().equals(Player) && ttt4.getText().equals(Player) && ttt7.getText().equals(Player)) {
            return true;
        }

        if (ttt2.getText().equals(Player) && ttt5.getText().equals(Player) && ttt8.getText().equals(Player))
        {
            return true;
        }

        if (ttt3.getText().equals(Player) && ttt6.getText().equals(Player) && ttt9.getText().equals(Player)) {
            return true;
        }

        if (ttt1.getText().equals(Player) && ttt5.getText().equals(Player) && ttt9.getText().equals(Player)) {
            return true;
        }

        return ttt3.getText().equals(Player) && ttt5.getText().equals(Player) && ttt7.getText().equals(Player);
    }

    private static boolean TTTBoardIsFull() {
        return !ttt1.getText().isEmpty() && !ttt2.getText().isEmpty() &&
                !ttt3.getText().isEmpty() && !ttt4.getText().isEmpty() &&
                !ttt5.getText().isEmpty() && !ttt6.getText().isEmpty() &&
                !ttt7.getText().isEmpty() && !ttt8.getText().isEmpty() && !ttt9.getText().isEmpty();
    }

    @Override
    public String toString() {
        return "TicTacToe object, hash=" + this.hashCode();
    }
}
