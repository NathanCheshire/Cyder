package cyder.games;

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
import cyder.threads.CyderThreadRunner;
import cyder.ui.CyderButton;
import cyder.ui.CyderFrame;
import cyder.utils.GetterUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

/**
 * A tic tac toe game widget.
 */
@CyderAuthor
@Vanilla
public final class TttGame {
    /**
     * The CyderFrame instance to use to ensure no other games exist with one Cyder instance.
     */
    private static CyderFrame tttFrame;

    /**
     * The buttons for the board.
     */
    private static BoardButton[][] boardButtons;

    /**
     * The foreground color used for the buttons.
     */
    public static final Color blueForeground = new Color(71, 81, 117);

    /**
     * Player enums.
     */
    private enum Player {
        X, O
    }

    /**
     * The current player.
     */
    private static Player currentPlayer;

    /**
     * The information label.
     */
    private static JLabel infoLabel;

    /**
     * Prevent instantiation of class.
     */
    private TttGame() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    private static final Dimension buttonSize = new Dimension(60, 60);
    private static int boardLength = 5;
    private static final LineBorder buttonBorder = new LineBorder(CyderColors.navy, 5, false);
    private static final int buttonPadding = 10;

    @SuppressCyderInspections(CyderInspection.WidgetInspection)
    @Widget(triggers = {"ttt", "tic tac toe"}, description = "A TicTacToe widget")
    public static void showGui() {
        if (tttFrame != null) {
            tttFrame.dispose(true);
        }

        int frameLen = buttonSize.width * boardLength + buttonPadding * (boardLength + 2);

        tttFrame = new CyderFrame(frameLen, frameLen + 100, CyderIcons.defaultBackground);
        tttFrame.setTitle("TicTacToe");
        tttFrame.addMenuItem("Board Size", () -> CyderThreadRunner.submit(() -> {
            try {
                String sizeString = GetterUtil.getInstance().getString(
                        new GetterUtil.Builder("Board Size")
                                .setInitialString(String.valueOf(boardLength))
                                .setFieldRegex("[0-7]")
                                .setRelativeTo(tttFrame));

                try {
                    boardLength = Integer.parseInt(sizeString);
                    showGui();
                } catch (Exception ignored) {
                    tttFrame.notify("Unable to parse input as an integer");
                }
            } catch (Exception e) {
                ExceptionHandler.handle(e);
            }
        }, "Board Size Changer"));
        tttFrame.addMenuItem("Reset Board", TttGame::resetBoard);
        tttFrame.setMenuType(CyderFrame.MenuType.RIBBON);
        tttFrame.lockMenuOut();

        infoLabel = new JLabel();
        infoLabel.setHorizontalAlignment(JLabel.CENTER);
        infoLabel.setFont(CyderFonts.DEFAULT_FONT);
        infoLabel.setForeground(CyderColors.navy);
        infoLabel.setBounds(0, 60, frameLen, 50);
        tttFrame.getContentPane().add(infoLabel);

        int startingX = buttonPadding;
        int startingY = buttonPadding + 100;

        boardButtons = new BoardButton[boardLength][boardLength];

        for (int y = 0 ; y < boardLength ; y++) {
            for (int x = 0 ; x < boardLength ; x++) {
                CyderButton button = new CyderButton("");
                button.setPreferredSize(buttonSize);
                button.setColors(CyderColors.vanilla);
                button.setFocusPainted(false);
                button.setBackground(CyderColors.vanilla);
                button.setFont(CyderFonts.SEGOE_30);
                button.setBorder(buttonBorder);
                button.addActionListener(e -> {
                    if (button.getText().isEmpty()) {
                        if (currentPlayer == Player.X) {
                            button.setText("X");
                            button.setFont(CyderFonts.SEGOE_30);
                            currentPlayer = Player.O;

                        } else {
                            button.setText("O");
                            button.setForeground(blueForeground);
                            button.setFont(CyderFonts.SEGOE_30);
                            currentPlayer = Player.X;
                        }

                        updateTurnLabel();
                        checkForWin();
                    }
                });

                button.setBounds(startingX, startingY, buttonSize.width, buttonSize.height);

                startingX += buttonPadding + buttonSize.width;

                tttFrame.getContentPane().add(button);
                boardButtons[y][x] = new BoardButton(x, y, button);
            }

            startingY += buttonPadding + buttonSize.height;
            startingX = buttonPadding;
        }

        tttFrame.finalizeAndShow();
        currentPlayer = Player.X;

        updateTurnLabel();
    }

    /**
     * Updates the player turn label.
     */
    private static void updateTurnLabel() {
        infoLabel.setText("Player Turn: " + (currentPlayer == Player.X ? "X" : "O"));
    }

    /**
     * Resets the ttt board
     */
    private static void resetBoard() {
        currentPlayer = Player.X;
        updateTurnLabel();

        for (BoardButton[] buttonRow : boardButtons) {
            for (BoardButton button : buttonRow) {
                button.getButton().setText("");
            }
        }
    }

    /**
     * Checks for a game win.
     */
    private static void checkForWin() {
        if (checkPlayerWin("X")) {
            tttFrame.notify("X's have won the game! Congratulations!");
            resetBoard();
        } else if (checkPlayerWin("O")) {
            tttFrame.notify("O's have won the game! Congratulations!");
            resetBoard();
        } else if (isBoardFull()) {
            tttFrame.notify("The game ended with no winners.");
            resetBoard();
        }
    }

    /**
     * Returns whether the provided player has won the game.
     *
     * @param player the player to check for winning
     * @return whether the provided player has won
     */
    private static boolean checkPlayerWin(String player) {
        return isHorizontalWin(player) || isVerticalWin(player) || isDiagonalWin(player);
    }

    /**
     * Returns whether the provided player has won via a horizontal win.
     *
     * @param player the player to test for a horizontal win
     * @return whether the provided player has won via a horizontal win
     */
    private static boolean isHorizontalWin(String player) {
        for (int y = 0 ; y < boardLength ; y++) {
            boolean line = true;

            for (int x = 0 ; x < boardLength ; x++) {
                line = line && boardButtons[y][x].getButton().getText().equals(player);
            }

            if (line) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns whether the provided player has won via a vertical win.
     *
     * @param player the player to test for a vertical win
     * @return whether the provided player has won via a vertical win
     */
    private static boolean isVerticalWin(String player) {
        BoardButton[][] rotated = new BoardButton[boardLength][boardLength];

        for (int i = 0 ; i < boardButtons[0].length ; i++) {
            for (int j = boardButtons.length - 1 ; j >= 0 ; j--) {
                rotated[i][j] = boardButtons[j][i];
            }
        }

        for (int y = 0 ; y < boardLength ; y++) {
            boolean line = true;

            for (int x = 0 ; x < boardLength ; x++) {
                line = line && rotated[y][x].getButton().getText().equals(player);
            }

            if (line) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns whether the provided player has won via a diagonal win.
     *
     * @param player the player to test for a diagonal win
     * @return whether the provided player has won via a diagonal win
     */
    private static boolean isDiagonalWin(String player) {
        boolean topLeftBottomRight = true;

        for (int i = 0 ; i < boardLength ; i++) {
            topLeftBottomRight = topLeftBottomRight && boardButtons[i][i].getButton().getText().equals(player);
        }

        if (topLeftBottomRight) {
            return true;
        }

        boolean topRightBottomLeft = true;

        for (int i = 0 ; i < boardLength ; i++) {
            topRightBottomLeft =
                    topRightBottomLeft && boardButtons[boardLength - i - 1][i].getButton().getText().equals(player);
        }

        return topRightBottomLeft;
    }

    /**
     * Returns whether the board is full.
     *
     * @return whether the board is full
     */
    private static boolean isBoardFull() {
        for (BoardButton[] buttonRow : boardButtons) {
            for (BoardButton button : buttonRow) {
                if (button.getButton().getText().isEmpty()) {
                    return false;
                }
            }
        }

        return true;
    }

    private static final class BoardButton {
        private int x;
        private int y;
        private CyderButton button;

        public BoardButton(int x, int y, CyderButton button) {
            this.x = x;
            this.y = y;
            this.button = button;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public CyderButton getButton() {
            return button;
        }

        public void setButton(CyderButton button) {
            this.button = button;
        }
    }
}
