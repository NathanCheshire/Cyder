package cyder.widgets;

import cyder.consts.CyderColors;
import cyder.ui.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;

public class PathFinder {
    private static int squareLen = 30;
    private static int numSquares;
    private static JLabel gridLabel;
    private static CyderCheckBox setStartBox;
    private static CyderCheckBox setEndBox;
    private static CyderCheckBox showStepsBox;
    private static CyderCheckBox diagonalBox;
    private static CyderFrame pathFindingFrame;
    private static CyderButton reset;

    private static Node start;
    private static Node end;
    private static LinkedList<Node> walls;

    public static void showGUI() {
        if (pathFindingFrame != null)
            pathFindingFrame.closeAnimation();

        walls = new LinkedList<>();

        pathFindingFrame = new CyderFrame(1000,1000);
        pathFindingFrame.setTitle("Path finding visualizer");

        gridLabel = new JLabel() {
            @Override
            public void paint(Graphics g) {
                super.paint(g);

                if (gridLabel != null) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setColor(CyderColors.navy);
                    g2d.setStroke(new BasicStroke(2));

                    int labelWidth = gridLabel.getWidth();
                    int labelHeight = gridLabel.getHeight();

                    numSquares = (int) (Math.floor(labelWidth / squareLen));
                    int drawTo = (int) ((Math.floor(labelWidth / squareLen)) * squareLen);

                    for (int x = 1 ; x <= drawTo - 2 ; x += squareLen) {
                        g2d.drawLine(x, 1, x, drawTo - 2);
                    }

                    for (int y = 1 ; y <= drawTo - 2 ; y += squareLen) {
                        g2d.drawLine(1, y, drawTo - 2, y);
                    }

                    g2d.drawLine(drawTo, 1, drawTo, drawTo);
                    g2d.drawLine(1, drawTo, drawTo, drawTo);

                    if (start != null) {
                        g2d.setColor(CyderColors.intellijPink);
                        g2d.fillRect(2 + start.getX() * squareLen, 2 + start.getY() * squareLen,
                                squareLen - 2, squareLen - 2);
                        gridLabel.repaint();
                    }

                    if (end != null) {
                        g2d.setColor(CyderColors.regularBlue);
                        g2d.fillRect(2 + end.getX() * squareLen, 2 + end.getY() * squareLen,
                                squareLen - 2, squareLen - 2);
                        gridLabel.repaint();
                    }

                    for (Node wall : walls) {
                        g2d.setColor(CyderColors.navy);
                        g2d.fillRect(2 + wall.getX() * squareLen, 2 + wall.getY() * squareLen,
                                squareLen - 2, squareLen - 2);
                        gridLabel.repaint();
                    }
                }
            }
        };
        gridLabel.addMouseWheelListener(e -> {
            if (e.isControlDown()) {
                if (e.getWheelRotation() == -1 && squareLen + 1 < 50) {
                    squareLen += 1;
                } else if (squareLen -1 > 0){
                    squareLen -= 1;
                }
                gridLabel.repaint();
            }
        });
        gridLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int x = (int) Math.floor((1 + e.getX()) / squareLen);
                int y = (int) Math.floor((1 + e.getY()) / squareLen);

                if (x >= numSquares || y >= numSquares)
                    return;

                Node addNode = new Node(x, y);

                if (addNode.equals(start)) {
                    start = null;
                } else if (addNode.equals(end)) {
                    end = null;
                }

                if (setStartBox.isSelected()) {
                    for (Node n : walls) {
                        if (n.equals(addNode)) {
                            walls.remove(n);
                            break;
                        }
                    }

                    start = addNode;
                    setStartBox.setNotSelected();
                    gridLabel.repaint();
                } else if (setEndBox.isSelected()) {
                    for (Node n : walls) {
                        if (n.equals(addNode)) {
                            walls.remove(n);
                            break;
                        }
                    }

                    end = addNode;
                    setEndBox.setNotSelected();
                    gridLabel.repaint();
                } else {
                    boolean contains = false;
                    for (Node wall : walls) {
                        if (wall.equals(addNode)) {
                            contains = true;
                            break;
                        }
                    }

                    if (!contains) {
                        walls.add(addNode);
                    } else {
                        for (Node n : walls) {
                            if (n.equals(addNode)) {
                                walls.remove(n);
                                break;
                            }
                        }
                    }

                    gridLabel.repaint();
                }
            }
        });
        gridLabel.setSize(800,800);
        gridLabel.setLocation(100,100);
        pathFindingFrame.getContentPane().add(gridLabel);

        CyderLabel setStartLabel = new CyderLabel("Set start");
        setStartLabel.setBounds(75,885,100,30);
        pathFindingFrame.getContentPane().add(setStartLabel);

        setStartBox = new CyderCheckBox();
        setStartBox.setNotSelected();
        setStartBox.setBounds(100, 920,50,50);
        pathFindingFrame.getContentPane().add(setStartBox);
        setStartBox.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                setEndBox.setNotSelected();
            }
        });

        CyderLabel setEndLabel = new CyderLabel("Set end");
        setEndLabel.setBounds(75 + 70,885,100,30);
        pathFindingFrame.getContentPane().add(setEndLabel);

        setEndBox = new CyderCheckBox();
        setEndBox.setNotSelected();
        setEndBox.setBounds(170, 920,50,50);
        pathFindingFrame.getContentPane().add(setEndBox);
        setEndBox.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                setStartBox.setNotSelected();
            }
        });

        CyderLabel showStepsLabel = new CyderLabel("Steps");
        showStepsLabel.setBounds(75 + 70 + 70,885,100,30);
        pathFindingFrame.getContentPane().add(showStepsLabel);

        showStepsBox = new CyderCheckBox();
        showStepsBox.setNotSelected();
        showStepsBox.setBounds(240, 920,50,50);
        pathFindingFrame.getContentPane().add(showStepsBox);

        CyderLabel diagonalStepsLabel = new CyderLabel("Diagonals");
        diagonalStepsLabel.setBounds(75 + 70 + 75 + 75,885,100,30);
        pathFindingFrame.getContentPane().add(diagonalStepsLabel);

        diagonalBox = new CyderCheckBox();
        diagonalBox.setNotSelected();
        diagonalBox.setBounds(310, 920,50,50);
        pathFindingFrame.getContentPane().add(diagonalBox);

        reset = new CyderButton("Reset");
        reset.setBounds(420,925, 200, 40);
        pathFindingFrame.getContentPane().add(reset);

        //speed slider

        pathFindingFrame.setVisible(true);
        ConsoleFrame.getConsoleFrame().setFrameRelative(pathFindingFrame);
    }

    private static class Node {
       private int x;
       private int y;
       private double h;
       private double f;
       private double g;
       private Node parent;

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

        public double getH() {
            return h;
        }

        public void setH(double h) {
            this.h = h;
        }

        public double getF() {
            return f;
        }

        public void setF(double f) {
            this.f = f;
        }

        public double getG() {
            return g;
        }

        public void setG(double g) {
            this.g = g;
        }

        public Node getParent() {
            return parent;
        }

        public void setParent(Node parent) {
            this.parent = parent;
        }

        public Node(int x, int y) {
           this.x = x;
           this.y = y;
       }

       public boolean equals(Node n) {
           return n != null && n.getX() == this.x && n.getY() == this.y;
       }
    }
}
