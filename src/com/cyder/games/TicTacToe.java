package com.cyder.games;

import com.cyder.utilities.Util;
import com.cyder.ui.CyderButton;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class TicTacToe {

    private JFrame TTTFrame;
    private Util TTTUtil = new Util();
    private CyderButton TTT9;
    private CyderButton TTT8;
    private CyderButton TTT7;
    private CyderButton TTT6;
    private CyderButton TTT5;
    private CyderButton TTT4;
    private CyderButton TTT3;
    private CyderButton TTT2;
    private CyderButton TTT1;
    private CyderButton TTTReset;

    private int CurrentPlayerTurn;

    private final int PlayerX = 0;
    private final int PlayerO = 1;

    private JLabel TTTLabel;



    public void startTicTacToe() {
        if (TTTFrame != null)
            TTTUtil.closeAnimation(TTTFrame);

        TTTFrame = new JFrame();

        TTTFrame.setTitle("Tic Tac Toe");

        TTTFrame.setResizable(false);

        TTTFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        TTTFrame.setResizable(false);

        TTTFrame.setIconImage(TTTUtil.getCyderIcon().getImage());

        JPanel parentPanel = new JPanel();

        parentPanel.setLayout(new BoxLayout(parentPanel,BoxLayout.Y_AXIS));

        JPanel LabelPanel = new JPanel();

        LabelPanel.setLayout(new BorderLayout());

        TTTLabel = new JLabel();

        TTTLabel.setFont(TTTUtil.weatherFontSmall);

        TTTLabel.setForeground(TTTUtil.navy);

        LabelPanel.add(TTTLabel, SwingConstants.CENTER);

        LabelPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        parentPanel.add(LabelPanel,SwingConstants.CENTER);

        JPanel ButtonPanel = new JPanel();

        GridLayout ButtonLayout = new GridLayout(3,3,5,5);

        ButtonPanel.setLayout(ButtonLayout);

        TTT1 = new CyderButton("");

        TTT1.setPreferredSize(new Dimension(60, 60));
        
        TTT1.setColors(TTTUtil.vanila);

        TTT1.setFocusPainted(false);

        TTT1.setBackground(TTTUtil.vanila);

        TTT1.setFont(TTTUtil.weatherFontSmall);

        TTT1.setBorder(new LineBorder(TTTUtil.navy,5,false));

        TTT1.addActionListener(e -> {
            if (TTT1.getText().isEmpty()) {
                if (CurrentPlayerTurn == PlayerX) {
                    TTT1.setText("X");

                    TTT1.setForeground(TTTUtil.regularRed);

                    TTT1.setFont(TTTUtil.weatherFontSmall);

                    CurrentPlayerTurn = PlayerO;

                    UpdatePlayerTurnLabel();

                    TTTCheckWin();
                }

                else {
                    TTT1.setText("O");

                    TTT1.setForeground(TTTUtil.tttblue);

                    TTT1.setFont(TTTUtil.weatherFontSmall);

                    CurrentPlayerTurn = PlayerX;

                    UpdatePlayerTurnLabel();

                    TTTCheckWin();
                }
            }
        });

        ButtonPanel.add(TTT1);

        TTT2 = new CyderButton("");

        TTT2.setColors(TTTUtil.vanila);

        TTT2.setPreferredSize(new Dimension(60, 60));

        TTT2.setFocusPainted(false);

        TTT2.setBackground(TTTUtil.vanila);

        TTT2.setFont(TTTUtil.weatherFontSmall);

        TTT2.setBorder(new LineBorder(TTTUtil.navy,5,false));

        TTT2.addActionListener(e -> {
            if (TTT2.getText().isEmpty()) {
                if (CurrentPlayerTurn == PlayerX) {
                    TTT2.setText("X");

                    TTT2.setForeground(TTTUtil.regularRed);

                    TTT2.setFont(TTTUtil.weatherFontSmall);

                    CurrentPlayerTurn = PlayerO;

                    UpdatePlayerTurnLabel();

                    TTTCheckWin();
                }

                else {
                    TTT2.setText("O");

                    TTT2.setForeground(TTTUtil.tttblue);

                    TTT2.setFont(TTTUtil.weatherFontSmall);

                    CurrentPlayerTurn = PlayerX;

                    UpdatePlayerTurnLabel();

                    TTTCheckWin();
                }
            }
        });

        ButtonPanel.add(TTT2);

        TTT3 = new CyderButton("");

        TTT3.setColors(TTTUtil.vanila);

        TTT3.setPreferredSize(new Dimension(60, 60));

        TTT3.setFocusPainted(false);

        TTT3.setBackground(TTTUtil.vanila);

        TTT3.setFont(TTTUtil.weatherFontSmall);

        TTT3.setBorder(new LineBorder(TTTUtil.navy,5,false));

        TTT3.addActionListener(e -> {
            if (TTT3.getText().isEmpty()) {
                if (CurrentPlayerTurn == PlayerX) {
                    TTT3.setText("X");

                    TTT3.setForeground(TTTUtil.regularRed);

                    TTT3.setFont(TTTUtil.weatherFontSmall);

                    CurrentPlayerTurn = PlayerO;

                    UpdatePlayerTurnLabel();

                    TTTCheckWin();
                }

                else {
                    TTT3.setText("O");

                    TTT3.setForeground(TTTUtil.tttblue);

                    TTT3.setFont(TTTUtil.weatherFontSmall);

                    CurrentPlayerTurn = PlayerX;

                    UpdatePlayerTurnLabel();

                    TTTCheckWin();
                }
            }
        });

        ButtonPanel.add(TTT3);

        TTT4 = new CyderButton("");

        TTT4.setPreferredSize(new Dimension(60, 60));

        TTT4.setFocusPainted(false);

        TTT4.setColors(TTTUtil.vanila);

        TTT4.setBackground(TTTUtil.vanila);

        TTT4.setBorder(new LineBorder(TTTUtil.navy,5,false));

        TTT4.setFont(TTTUtil.weatherFontSmall);

        TTT4.addActionListener(e -> {
            if (TTT4.getText().isEmpty()) {
                if (CurrentPlayerTurn == PlayerX) {
                    TTT4.setText("X");

                    TTT4.setForeground(TTTUtil.regularRed);

                    TTT4.setFont(TTTUtil.weatherFontSmall);

                    CurrentPlayerTurn = PlayerO;

                    UpdatePlayerTurnLabel();

                    TTTCheckWin();
                }

                else {
                    TTT4.setText("O");

                    TTT4.setForeground(TTTUtil.tttblue);

                    TTT4.setFont(TTTUtil.weatherFontSmall);

                    CurrentPlayerTurn = PlayerX;

                    UpdatePlayerTurnLabel();

                    TTTCheckWin();
                }
            }
        });

        ButtonPanel.add(TTT4);

        TTT5 = new CyderButton("");

        TTT5.setColors(TTTUtil.vanila);

        TTT5.setPreferredSize(new Dimension(60, 60));

        TTT5.setFocusPainted(false);

        TTT5.setBackground(TTTUtil.vanila);

        TTT5.setBorder(new LineBorder(TTTUtil.navy,5,false));

        TTT5.setFont(TTTUtil.weatherFontSmall);

        TTT5.addActionListener(e -> {
            if (TTT5.getText().isEmpty()) {
                if (CurrentPlayerTurn == PlayerX) {
                    TTT5.setText("X");

                    TTT5.setForeground(TTTUtil.regularRed);

                    TTT5.setFont(TTTUtil.weatherFontSmall);

                    CurrentPlayerTurn = PlayerO;

                    UpdatePlayerTurnLabel();

                    TTTCheckWin();
                }

                else {
                    TTT5.setText("O");

                    TTT5.setForeground(TTTUtil.tttblue);

                    TTT5.setFont(TTTUtil.weatherFontSmall);

                    CurrentPlayerTurn = PlayerX;

                    UpdatePlayerTurnLabel();

                    TTTCheckWin();
                }
            }
        });

        ButtonPanel.add(TTT5);

        TTT6 = new CyderButton("");

        TTT6.setPreferredSize(new Dimension(60, 60));

        TTT6.setFocusPainted(false);

        TTT6.setBackground(TTTUtil.vanila);

        TTT6.setColors(TTTUtil.vanila);

        TTT6.setBorder(new LineBorder(TTTUtil.navy,5,false));

        TTT6.setFont(TTTUtil.weatherFontSmall);

        TTT6.addActionListener(e -> {
            if (TTT6.getText().isEmpty()) {
                if (CurrentPlayerTurn == PlayerX) {
                    TTT6.setText("X");

                    TTT6.setForeground(TTTUtil.regularRed);

                    TTT6.setFont(TTTUtil.weatherFontSmall);

                    CurrentPlayerTurn = PlayerO;

                    UpdatePlayerTurnLabel();

                    TTTCheckWin();
                }

                else {
                    TTT6.setText("O");

                    TTT6.setForeground(TTTUtil.tttblue);

                    TTT6.setFont(TTTUtil.weatherFontSmall);

                    CurrentPlayerTurn = PlayerX;

                    UpdatePlayerTurnLabel();

                    TTTCheckWin();
                }
            }
        });

        ButtonPanel.add(TTT6);

        TTT7 = new CyderButton("");

        TTT7.setPreferredSize(new Dimension(60, 60));

        TTT7.setFocusPainted(false);

        TTT7.setColors(TTTUtil.vanila);

        TTT7.setBackground(TTTUtil.vanila);

        TTT7.setBorder(new LineBorder(TTTUtil.navy,5,false));

        TTT7.setFont(TTTUtil.weatherFontSmall);

        TTT7.addActionListener(e -> {
            if (TTT7.getText().isEmpty()) {
                if (CurrentPlayerTurn == PlayerX) {
                    TTT7.setText("X");

                    TTT7.setForeground(TTTUtil.regularRed);

                    TTT7.setFont(TTTUtil.weatherFontSmall);

                    CurrentPlayerTurn = PlayerO;

                    UpdatePlayerTurnLabel();

                    TTTCheckWin();
                }

                else {
                    TTT7.setText("O");

                    TTT7.setForeground(TTTUtil.tttblue);

                    TTT7.setFont(TTTUtil.weatherFontSmall);

                    CurrentPlayerTurn = PlayerX;

                    UpdatePlayerTurnLabel();

                    TTTCheckWin();
                }
            }
        });

        ButtonPanel.add(TTT7);

        TTT8 = new CyderButton("");

        TTT8.setPreferredSize(new Dimension(60, 60));

        TTT8.setFocusPainted(false);

        TTT8.setBackground(TTTUtil.vanila);

        TTT8.setColors(TTTUtil.vanila);

        TTT8.setFont(TTTUtil.weatherFontSmall);

        TTT8.setBorder(new LineBorder(TTTUtil.navy,5,false));

        TTT8.addActionListener(e -> {
            if (TTT8.getText().isEmpty()) {
                if (CurrentPlayerTurn == PlayerX) {
                    TTT8.setText("X");

                    TTT8.setForeground(TTTUtil.regularRed);

                    TTT8.setFont(TTTUtil.weatherFontSmall);

                    CurrentPlayerTurn = PlayerO;

                    UpdatePlayerTurnLabel();

                    TTTCheckWin();
                }

                else {
                    TTT8.setText("O");

                    TTT8.setForeground(TTTUtil.tttblue);

                    TTT8.setFont(TTTUtil.weatherFontSmall);

                    CurrentPlayerTurn = PlayerX;

                    UpdatePlayerTurnLabel();

                    TTTCheckWin();
                }
            }
        });

        ButtonPanel.add(TTT8);

        TTT9 = new CyderButton("");

        TTT9.setColors(TTTUtil.vanila);

        TTT9.setPreferredSize(new Dimension(60, 60));

        TTT9.setFocusPainted(false);

        TTT9.setBackground(TTTUtil.vanila);

        TTT9.setBorder(new LineBorder(TTTUtil.navy,5,false));

        TTT9.setFont(TTTUtil.weatherFontSmall);

        TTT9.addActionListener(e -> {
            if (TTT9.getText().isEmpty()) {
                if (CurrentPlayerTurn == PlayerX) {
                    TTT9.setText("X");

                    TTT9.setForeground(TTTUtil.regularRed);

                    TTT9.setFont(TTTUtil.weatherFontSmall);

                    CurrentPlayerTurn = PlayerO;

                    UpdatePlayerTurnLabel();

                    TTTCheckWin();
                }

                else {
                    TTT9.setText("O");

                    TTT9.setForeground(TTTUtil.tttblue);

                    TTT9.setFont(TTTUtil.weatherFontSmall);

                    CurrentPlayerTurn = PlayerX;

                    UpdatePlayerTurnLabel();

                    TTTCheckWin();
                }
            }
        });

        ButtonPanel.add(TTT9);

        ButtonPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        parentPanel.add(ButtonPanel);

        JPanel ResetPanel = new JPanel();

        ResetPanel.setLayout(new BorderLayout());

        TTTReset = new CyderButton("Reset");

        TTTReset.setFocusPainted(false);

        TTTReset.setBackground(TTTUtil.regularRed);

        TTTReset.setFont(TTTUtil.weatherFontSmall);

        TTTReset.setColors(TTTUtil.regularRed);

        TTTReset.setBorder(new LineBorder(TTTUtil.navy,5,false));

        TTTReset.addActionListener(e -> TTTBoardReset());

        ResetPanel.add(TTTReset, BorderLayout.CENTER);

        ResetPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        parentPanel.add(ResetPanel);

        parentPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        TTTFrame.add(parentPanel);

        TTTFrame.pack();

        TTTFrame.setLocationRelativeTo(null);

        TTTFrame.setVisible(true);

        TTTFrame.setAlwaysOnTop(true);

        TTTFrame.setAlwaysOnTop(false);

        TTTFrame.requestFocus();

        CurrentPlayerTurn = PlayerX;

        UpdatePlayerTurnLabel();

        TTTUtil.startAnimation(TTTFrame);
    }

    private void UpdatePlayerTurnLabel() {
        if (CurrentPlayerTurn == PlayerX) {
            TTTLabel.setText("Player Turn: X");
        }

        else {
            TTTLabel.setText("Player Turn: O");
        }
    }

    private void TTTBoardReset() {
        CurrentPlayerTurn = PlayerX;
        UpdatePlayerTurnLabel();

        TTT1.setText("");
        TTT2.setText("");
        TTT3.setText("");
        TTT4.setText("");
        TTT5.setText("");
        TTT6.setText("");
        TTT7.setText("");
        TTT8.setText("");
        TTT9.setText("");
    }

    private void TTTCheckWin() {
        if (HasPlayerWon("X")) {
            TTTUtil.inform("X's have won the game! Congratulations!","",200,200);
            TTTReset.doClick();
        }

        else if (HasPlayerWon("O")) {
            TTTUtil.inform("O's have won the game! Congratulations!","",200,200);
            TTTReset.doClick();
        }

        else if (TTTBoardIsFull()) {
            TTTUtil.inform("The game ended with no winners.","",200,200);
            TTTReset.doClick();
        }
    }

    private boolean HasPlayerWon(String Player) {
        if (TTT1.getText().equals(Player) && TTT2.getText().equals(Player) && TTT3.getText().equals(Player)) {
            return true;
        }

        if (TTT4.getText().equals(Player) && TTT5.getText().equals(Player) && TTT6.getText().equals(Player)) {
            return true;
        }

        if (TTT7.getText().equals(Player) && TTT8.getText().equals(Player) && TTT9.getText().equals(Player)) {
            return true;
        }

        if (TTT1.getText().equals(Player) && TTT4.getText().equals(Player) && TTT7.getText().equals(Player)) {
            return true;
        }

        if (TTT2.getText().equals(Player) && TTT5.getText().equals(Player) && TTT8.getText().equals(Player))
        {
            return true;
        }

        if (TTT3.getText().equals(Player) && TTT6.getText().equals(Player) && TTT9.getText().equals(Player)) {
            return true;
        }

        if (TTT1.getText().equals(Player) && TTT5.getText().equals(Player) && TTT9.getText().equals(Player)) {
            return true;
        }

        return TTT3.getText().equals(Player) && TTT5.getText().equals(Player) && TTT7.getText().equals(Player);
    }

    private boolean TTTBoardIsFull() {
        return !TTT1.getText().isEmpty() && !TTT2.getText().isEmpty() &&
                !TTT3.getText().isEmpty() && !TTT4.getText().isEmpty() &&
                !TTT5.getText().isEmpty() && !TTT6.getText().isEmpty() &&
                !TTT7.getText().isEmpty() && !TTT8.getText().isEmpty() && !TTT9.getText().isEmpty();
    }
}
