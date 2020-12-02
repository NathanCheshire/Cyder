package com.cyder.games;

import com.cyder.ui.CyderFrame;
import com.cyder.utilities.GeneralUtil;
import com.cyder.ui.CyderButton;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class TicTacToe {

    private CyderFrame tttFrame;
    private GeneralUtil tttGeneralUtil = new GeneralUtil();

    private CyderButton ttt9;
    private CyderButton ttt8;
    private CyderButton ttt7;
    private CyderButton ttt6;
    private CyderButton ttt5;
    private CyderButton ttt4;
    private CyderButton ttt3;
    private CyderButton ttt2;
    private CyderButton ttt1;
    private CyderButton tttReset;

    private int CurrentPlayerTurn;

    private final int PlayerX = 0;
    private final int PlayerO = 1;

    private JLabel tttLabel;

    public void startTicTacToe() {
        if (tttFrame != null)
            tttGeneralUtil.closeAnimation(tttFrame);

        tttFrame = new CyderFrame(400,500, new ImageIcon("src/com/cyder/io/pictures/DebugBackground.png"));
        tttFrame.setTitlePosition(tttFrame.CENTER_TITLE);
        tttFrame.setTitle("Tic Tac Toe");

        tttLabel = new JLabel();
        tttLabel.setFont(tttGeneralUtil.weatherFontSmall);
        tttLabel.setForeground(tttGeneralUtil.navy);
        tttLabel.setBounds(tttGeneralUtil.xOffsetForCenterJLabel(400,"Tic Tac Toe"),30,
                tttGeneralUtil.xOffsetForCenterJLabel(400,tttFrame.getTitle()) * 2,30);
        tttFrame.getContentPane().add(tttLabel);

        ttt1 = new CyderButton("");
        ttt1.setPreferredSize(new Dimension(60, 60));
        ttt1.setColors(tttGeneralUtil.vanila);
        ttt1.setFocusPainted(false);
        ttt1.setBackground(tttGeneralUtil.vanila);
        ttt1.setFont(tttGeneralUtil.weatherFontBig);
        ttt1.setBorder(new LineBorder(tttGeneralUtil.navy,5,false));
        ttt1.addActionListener(e -> {
            if (ttt1.getText().isEmpty()) {
                if (CurrentPlayerTurn == PlayerX) {
                    ttt1.setText("X");

                    ttt1.setForeground(tttGeneralUtil.regularRed);

                    ttt1.setFont(tttGeneralUtil.weatherFontBig);

                    CurrentPlayerTurn = PlayerO;

                    UpdatePlayerTurnLabel();

                    TTTCheckWin();
                }

                else {
                    ttt1.setText("O");

                    ttt1.setForeground(tttGeneralUtil.tttblue);

                    ttt1.setFont(tttGeneralUtil.weatherFontBig);

                    CurrentPlayerTurn = PlayerX;

                    UpdatePlayerTurnLabel();

                    TTTCheckWin();
                }
            }
        });

        ttt1.setBounds(25, 75, 100, 100);
        tttFrame.getContentPane().add(ttt1);

        ttt2 = new CyderButton("");
        ttt2.setColors(tttGeneralUtil.vanila);
        ttt2.setPreferredSize(new Dimension(60, 60));
        ttt2.setFocusPainted(false);
        ttt2.setBackground(tttGeneralUtil.vanila);
        ttt2.setFont(tttGeneralUtil.weatherFontBig);
        ttt2.setBorder(new LineBorder(tttGeneralUtil.navy,5,false));
        ttt2.addActionListener(e -> {
            if (ttt2.getText().isEmpty()) {
                if (CurrentPlayerTurn == PlayerX) {
                    ttt2.setText("X");

                    ttt2.setForeground(tttGeneralUtil.regularRed);

                    ttt2.setFont(tttGeneralUtil.weatherFontBig);

                    CurrentPlayerTurn = PlayerO;

                    UpdatePlayerTurnLabel();

                    TTTCheckWin();
                }

                else {
                    ttt2.setText("O");

                    ttt2.setForeground(tttGeneralUtil.tttblue);

                    ttt2.setFont(tttGeneralUtil.weatherFontBig);

                    CurrentPlayerTurn = PlayerX;

                    UpdatePlayerTurnLabel();

                    TTTCheckWin();
                }
            }
        });

        ttt2.setBounds(150, 75, 100, 100);
        tttFrame.getContentPane().add(ttt2);

        ttt3 = new CyderButton("");

        ttt3.setColors(tttGeneralUtil.vanila);

        ttt3.setPreferredSize(new Dimension(60, 60));

        ttt3.setFocusPainted(false);

        ttt3.setBackground(tttGeneralUtil.vanila);

        ttt3.setFont(tttGeneralUtil.weatherFontBig);

        ttt3.setBorder(new LineBorder(tttGeneralUtil.navy,5,false));

        ttt3.addActionListener(e -> {
            if (ttt3.getText().isEmpty()) {
                if (CurrentPlayerTurn == PlayerX) {
                    ttt3.setText("X");

                    ttt3.setForeground(tttGeneralUtil.regularRed);

                    ttt3.setFont(tttGeneralUtil.weatherFontBig);

                    CurrentPlayerTurn = PlayerO;

                    UpdatePlayerTurnLabel();

                    TTTCheckWin();
                }

                else {
                    ttt3.setText("O");

                    ttt3.setForeground(tttGeneralUtil.tttblue);

                    ttt3.setFont(tttGeneralUtil.weatherFontBig);

                    CurrentPlayerTurn = PlayerX;

                    UpdatePlayerTurnLabel();

                    TTTCheckWin();
                }
            }
        });

        ttt3.setBounds(275, 75, 100, 100);
        tttFrame.getContentPane().add(ttt3);

        ttt4 = new CyderButton("");
        ttt4.setPreferredSize(new Dimension(60, 60));
        ttt4.setFocusPainted(false);
        ttt4.setColors(tttGeneralUtil.vanila);
        ttt4.setBackground(tttGeneralUtil.vanila);
        ttt4.setBorder(new LineBorder(tttGeneralUtil.navy,5,false));
        ttt4.setFont(tttGeneralUtil.weatherFontBig);
        ttt4.addActionListener(e -> {
            if (ttt4.getText().isEmpty()) {
                if (CurrentPlayerTurn == PlayerX) {
                    ttt4.setText("X");

                    ttt4.setForeground(tttGeneralUtil.regularRed);

                    ttt4.setFont(tttGeneralUtil.weatherFontBig);

                    CurrentPlayerTurn = PlayerO;

                    UpdatePlayerTurnLabel();

                    TTTCheckWin();
                }

                else {
                    ttt4.setText("O");

                    ttt4.setForeground(tttGeneralUtil.tttblue);

                    ttt4.setFont(tttGeneralUtil.weatherFontBig);

                    CurrentPlayerTurn = PlayerX;

                    UpdatePlayerTurnLabel();

                    TTTCheckWin();
                }
            }
        });

        ttt4.setBounds(25, 200, 100, 100);
        tttFrame.getContentPane().add(ttt4);

        ttt5 = new CyderButton("");
        ttt5.setColors(tttGeneralUtil.vanila);
        ttt5.setPreferredSize(new Dimension(60, 60));
        ttt5.setFocusPainted(false);
        ttt5.setBackground(tttGeneralUtil.vanila);
        ttt5.setBorder(new LineBorder(tttGeneralUtil.navy,5,false));
        ttt5.setFont(tttGeneralUtil.weatherFontBig);
        ttt5.addActionListener(e -> {
            if (ttt5.getText().isEmpty()) {
                if (CurrentPlayerTurn == PlayerX) {
                    ttt5.setText("X");

                    ttt5.setForeground(tttGeneralUtil.regularRed);

                    ttt5.setFont(tttGeneralUtil.weatherFontBig);

                    CurrentPlayerTurn = PlayerO;

                    UpdatePlayerTurnLabel();

                    TTTCheckWin();
                }

                else {
                    ttt5.setText("O");

                    ttt5.setForeground(tttGeneralUtil.tttblue);

                    ttt5.setFont(tttGeneralUtil.weatherFontBig);

                    CurrentPlayerTurn = PlayerX;

                    UpdatePlayerTurnLabel();

                    TTTCheckWin();
                }
            }
        });

        ttt5.setBounds(150, 200, 100, 100);
        tttFrame.getContentPane().add(ttt5);

        ttt6 = new CyderButton("");
        ttt6.setPreferredSize(new Dimension(60, 60));
        ttt6.setFocusPainted(false);
        ttt6.setBackground(tttGeneralUtil.vanila);
        ttt6.setColors(tttGeneralUtil.vanila);
        ttt6.setBorder(new LineBorder(tttGeneralUtil.navy,5,false));
        ttt6.setFont(tttGeneralUtil.weatherFontBig);
        ttt6.addActionListener(e -> {
            if (ttt6.getText().isEmpty()) {
                if (CurrentPlayerTurn == PlayerX) {
                    ttt6.setText("X");

                    ttt6.setForeground(tttGeneralUtil.regularRed);

                    ttt6.setFont(tttGeneralUtil.weatherFontBig);

                    CurrentPlayerTurn = PlayerO;

                    UpdatePlayerTurnLabel();

                    TTTCheckWin();
                }

                else {
                    ttt6.setText("O");

                    ttt6.setForeground(tttGeneralUtil.tttblue);

                    ttt6.setFont(tttGeneralUtil.weatherFontBig);

                    CurrentPlayerTurn = PlayerX;

                    UpdatePlayerTurnLabel();

                    TTTCheckWin();
                }
            }
        });

        ttt6.setBounds(275, 200, 100, 100);
        tttFrame.getContentPane().add(ttt6);

        ttt7 = new CyderButton("");
        ttt7.setPreferredSize(new Dimension(60, 60));
        ttt7.setFocusPainted(false);
        ttt7.setColors(tttGeneralUtil.vanila);
        ttt7.setBackground(tttGeneralUtil.vanila);
        ttt7.setBorder(new LineBorder(tttGeneralUtil.navy,5,false));
        ttt7.setFont(tttGeneralUtil.weatherFontBig);
        ttt7.addActionListener(e -> {
            if (ttt7.getText().isEmpty()) {
                if (CurrentPlayerTurn == PlayerX) {
                    ttt7.setText("X");

                    ttt7.setForeground(tttGeneralUtil.regularRed);

                    ttt7.setFont(tttGeneralUtil.weatherFontBig);

                    CurrentPlayerTurn = PlayerO;

                    UpdatePlayerTurnLabel();

                    TTTCheckWin();
                }

                else {
                    ttt7.setText("O");

                    ttt7.setForeground(tttGeneralUtil.tttblue);

                    ttt7.setFont(tttGeneralUtil.weatherFontBig);

                    CurrentPlayerTurn = PlayerX;

                    UpdatePlayerTurnLabel();

                    TTTCheckWin();
                }
            }
        });

        ttt7.setBounds(25, 325, 100, 100);
        tttFrame.getContentPane().add(ttt7);

        ttt8 = new CyderButton("");
        ttt8.setPreferredSize(new Dimension(60, 60));
        ttt8.setFocusPainted(false);
        ttt8.setBackground(tttGeneralUtil.vanila);
        ttt8.setColors(tttGeneralUtil.vanila);
        ttt8.setFont(tttGeneralUtil.weatherFontBig);
        ttt8.setBorder(new LineBorder(tttGeneralUtil.navy,5,false));
        ttt8.addActionListener(e -> {
            if (ttt8.getText().isEmpty()) {
                if (CurrentPlayerTurn == PlayerX) {
                    ttt8.setText("X");

                    ttt8.setForeground(tttGeneralUtil.regularRed);

                    ttt8.setFont(tttGeneralUtil.weatherFontBig);

                    CurrentPlayerTurn = PlayerO;

                    UpdatePlayerTurnLabel();

                    TTTCheckWin();
                }

                else {
                    ttt8.setText("O");

                    ttt8.setForeground(tttGeneralUtil.tttblue);

                    ttt8.setFont(tttGeneralUtil.weatherFontBig);

                    CurrentPlayerTurn = PlayerX;

                    UpdatePlayerTurnLabel();

                    TTTCheckWin();
                }
            }
        });

        ttt8.setBounds(150, 325, 100, 100);
        tttFrame.getContentPane().add(ttt8);

        ttt9 = new CyderButton("");
        ttt9.setColors(tttGeneralUtil.vanila);
        ttt9.setPreferredSize(new Dimension(60, 60));
        ttt9.setFocusPainted(false);
        ttt9.setBackground(tttGeneralUtil.vanila);
        ttt9.setBorder(new LineBorder(tttGeneralUtil.navy,5,false));
        ttt9.setFont(tttGeneralUtil.weatherFontBig);
        ttt9.addActionListener(e -> {
            if (ttt9.getText().isEmpty()) {
                if (CurrentPlayerTurn == PlayerX) {
                    ttt9.setText("X");

                    ttt9.setForeground(tttGeneralUtil.regularRed);

                    ttt9.setFont(tttGeneralUtil.weatherFontBig);

                    CurrentPlayerTurn = PlayerO;

                    UpdatePlayerTurnLabel();

                    TTTCheckWin();
                }

                else {
                    ttt9.setText("O");

                    ttt9.setForeground(tttGeneralUtil.tttblue);

                    ttt9.setFont(tttGeneralUtil.weatherFontBig);

                    CurrentPlayerTurn = PlayerX;

                    UpdatePlayerTurnLabel();

                    TTTCheckWin();
                }
            }
        });

        ttt9.setBounds(275, 325, 100, 100);
        tttFrame.getContentPane().add(ttt9);

        tttReset = new CyderButton("Reset");
        tttReset.setFocusPainted(false);
        tttReset.setBackground(tttGeneralUtil.regularRed);
        tttReset.setFont(tttGeneralUtil.weatherFontBig);
        tttReset.setColors(tttGeneralUtil.regularRed);
        tttReset.setBorder(new LineBorder(tttGeneralUtil.navy,5,false));
        tttReset.addActionListener(e -> TTTBoardReset());

        tttReset.setBounds(20, 440, 360, 40);
        tttFrame.getContentPane().add(tttReset);

        tttFrame.setVisible(true);
        tttGeneralUtil.startAnimation(tttFrame);

        CurrentPlayerTurn = PlayerX;

        UpdatePlayerTurnLabel();
    }

    private void UpdatePlayerTurnLabel() {
        if (CurrentPlayerTurn == PlayerX) {
            tttLabel.setText("Player Turn: X");
        }

        else {
            tttLabel.setText("Player Turn: O");
        }
    }

    private void TTTBoardReset() {
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

    private void TTTCheckWin() {
        if (HasPlayerWon("X")) {
            tttFrame.inform("X's have won the game! Congratulations!","",200,200);
            tttReset.doClick();
        }

        else if (HasPlayerWon("O")) {
            tttFrame.inform("O's have won the game! Congratulations!","",200,200);
            tttReset.doClick();
        }

        else if (TTTBoardIsFull()) {
            tttFrame.inform("The game ended with no winners.","",200,200);
            tttReset.doClick();
        }
    }

    private boolean HasPlayerWon(String Player) {
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

    private boolean TTTBoardIsFull() {
        return !ttt1.getText().isEmpty() && !ttt2.getText().isEmpty() &&
                !ttt3.getText().isEmpty() && !ttt4.getText().isEmpty() &&
                !ttt5.getText().isEmpty() && !ttt6.getText().isEmpty() &&
                !ttt7.getText().isEmpty() && !ttt8.getText().isEmpty() && !ttt9.getText().isEmpty();
    }
}
