package cyder.widgets;

import cyder.consts.CyderColors;
import cyder.enums.SliderShape;
import cyder.obj.Node;
import cyder.ui.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
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
    private static CyderButton startButton;
    private static JSlider speedSlider;

    private static Node start;
    private static Node end;
    private static LinkedList<Node> walls;
    private static LinkedList<Node> pathableNodes;

    private static Timer timer;
    private static int timeoutMS = 500;
    private static int maxTimeoutMs = 1000;

    public static void showGUI() {
        if (pathFindingFrame != null)
            pathFindingFrame.closeAnimation();

        timer = new Timer(timeoutMS, pathFindAction);
        timer.setDelay(timeoutMS);
        walls = new LinkedList<>();
        pathableNodes = new LinkedList<>();
        start = new Node(0,0);
        end = new Node(25, 25);

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

                    //draw green squres
                   for (Node n : pathableNodes) {
                       if (n.getParent() != null && !n.equals(end)) {
                           g2d.setColor(CyderColors.regularGreen);
                           g2d.fillRect(2 + n.getX() * squareLen, 2 + n.getY() * squareLen,
                                   squareLen - 2, squareLen - 2);
                           gridLabel.repaint();
                       }
                   }

                    //draw path
                    if (end != null && end.getParent() != null) {
                       Node currentRef = end.getParent();

                       while (currentRef != null && currentRef != start) {
                           g2d.setColor(CyderColors.regularBlue);
                           g2d.fillRect(2 + currentRef.getX() * squareLen, 2 + currentRef.getY() * squareLen,
                                   squareLen - 2, squareLen - 2);
                           currentRef = currentRef.getParent();
                       }
                    }

                    //draw start
                    if (start != null) {
                        g2d.setColor(CyderColors.intellijPink);
                        g2d.fillRect(2 + start.getX() * squareLen, 2 + start.getY() * squareLen,
                                squareLen - 2, squareLen - 2);
                        gridLabel.repaint();
                    }

                    //draw end
                    if (end != null) {
                        g2d.setColor(CyderColors.calculatorOrange);
                        g2d.fillRect(2 + end.getX() * squareLen, 2 + end.getY() * squareLen,
                                squareLen - 2, squareLen - 2);
                        gridLabel.repaint();
                    }

                    //draw walls last
                    for (Node wall : walls) {
                        if (wall.getX() >= numSquares || wall.getY() >= numSquares) {
                            walls.remove(wall);
                        } else {
                            g2d.setColor(CyderColors.navy);
                            g2d.fillRect(2 + wall.getX() * squareLen, 2 + wall.getY() * squareLen,
                                    squareLen - 2, squareLen - 2);
                            gridLabel.repaint();
                        }
                    }
                }
            }
        };
        gridLabel.addMouseWheelListener(e -> {
            if (timer.isRepeats())
                return;

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
                if (timer.isRunning())
                    return;

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
        gridLabel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (timer.isRunning())
                    return;

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

                boolean contains = false;
                for (Node wall : walls) {
                    if (wall.equals(addNode)) {
                        contains = true;
                        break;
                    }
                }

                if (!contains) {
                    walls.add(addNode);
                }

                gridLabel.repaint();
            }
        });
        gridLabel.setSize(800,800);
        gridLabel.setLocation(100,80);
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
        diagonalStepsLabel.setBounds(75 + 70 + 75 + 70,885,100,30);
        pathFindingFrame.getContentPane().add(diagonalStepsLabel);

        diagonalBox = new CyderCheckBox();
        diagonalBox.setNotSelected();
        diagonalBox.setBounds(310, 920,50,50);
        pathFindingFrame.getContentPane().add(diagonalBox);

        reset = new CyderButton("Reset");
        reset.setBounds(420,880, 150, 40);
        reset.addActionListener(e -> {
            timer.stop();
            diagonalBox.setNotSelected();
            setEndBox.setNotSelected();
            setStartBox.setNotSelected();
            showStepsBox.setNotSelected();
            speedSlider.setValue(500);
            start = null;
            end = null;
            walls = new LinkedList<>();
            pathableNodes = new LinkedList<>();
            gridLabel.repaint();
        });
        pathFindingFrame.getContentPane().add(reset);

        startButton = new CyderButton("Start");
        startButton.setBounds(420,935, 150, 40);
        startButton.addActionListener(e -> {
            //todo toggle between starting and stopping, stopping should exit pathfinding and leave painting where it is
            // to be reset on start again
            if (start == null || end == null) {
                pathFindingFrame.notify("Start/end nodes not set");
            } else {
                diagonalBox.setEnabled(false);
                showStepsBox.setEnabled(false);
                setStartBox.setEnabled(false);
                setEndBox.setEnabled(false);

                timer.start();

                diagonalBox.setEnabled(true);
                showStepsBox.setEnabled(true);
                setStartBox.setEnabled(true);
                setEndBox.setEnabled(true);
            }
        });
        pathFindingFrame.getContentPane().add(startButton);

        speedSlider = new JSlider(JSlider.HORIZONTAL, 0, 1000, 500);
        CyderSliderUI UI = new CyderSliderUI(speedSlider);
        UI.setThumbStroke(new BasicStroke(2.0f));
        UI.setSliderShape(SliderShape.RECT);
        UI.setFillColor(Color.black);
        UI.setOutlineColor(CyderColors.navy);
        UI.setNewValColor(CyderColors.regularBlue);
        UI.setOldValColor(CyderColors.intellijPink);
        UI.setTrackStroke(new BasicStroke(3.0f));
        speedSlider.setUI(UI);
        speedSlider.setBounds(600, 925, 290, 40);
        speedSlider.setPaintTicks(false);
        speedSlider.setPaintLabels(false);
        speedSlider.setVisible(true);
        speedSlider.setValue(500);
        speedSlider.addChangeListener(e -> {
            timeoutMS = (int) (maxTimeoutMs *
                    ((double) speedSlider.getValue() /  (double) speedSlider.getMaximum()));
            timer.setDelay(timeoutMS);
        });
        speedSlider.setOpaque(false);
        speedSlider.setToolTipText("Speed");
        speedSlider.setFocusable(false);
        speedSlider.repaint();
        pathFindingFrame.getContentPane().add(speedSlider);

        pathFindingFrame.setVisible(true);
        ConsoleFrame.getConsoleFrame().setFrameRelative(pathFindingFrame);
    }

    private static LinkedList<Node> open;
    private static LinkedList<Node> closed;

    private static ActionListener pathFindAction = evt -> {
        //todo found path will be in blue
        //todo checked nodes in green

        //todo if end has no parent then no path found

        //todo: generic request: add parent frame of button text to log so you know what frame the button is from

        //todo heuristic changer box to come

        pathableNodes = new LinkedList<>();
        for (int x = 0 ; x < numSquares ; x++) {
            for (int y = 0 ; y < numSquares ; y++) {
                Node addNode = new Node(x, y);

                boolean isWall = false;

                for (Node n : walls) {
                    if (n.equals(addNode)) {
                        isWall = true;
                        break;
                    }
                }

                if (!isWall) {
                    pathableNodes.add(addNode);
                }
            }
        }

        //algorithm here in steps using timer
    };

    private static boolean areDiagonalNeighbors(Node n1, Node n2) {
        return (n1.getX() == n2.getX() + 1 && n1.getY() == n2.getY() + 1) ||
                (n1.getX() == n2.getX() + 1 && n1.getY() == n2.getY() - 1) ||
                (n1.getX() == n2.getX() - 1 && n1.getY() == n2.getY() - 1) ||
                (n1.getX() == n2.getX() - 1 && n1.getY() == n2.getY() + 1);
    }

    private static boolean areOrthogonalNeighbors(Node n1, Node n2) {
        return (n1.getX() == n2.getX() && n1.getY() == n2.getY() + 1) ||
                (n1.getX() == n2.getX() && n1.getY() == n2.getY() - 1) ||
                (n1.getX() == n2.getX() + 1 && n1.getY() == n2.getY()) ||
                (n1.getX() == n2.getX() - 1 && n1.getY() == n2.getY());
    }

    //distance from node to end
    private static double heuristic(Node n) {
        return euclideanDistance(n, end);
    }

    //distance from node to start
    private static double calcGCost(Node n) {
        return euclideanDistance(n, start);
    }

    private static double euclideanDistance(Node n1, Node n2) {
        return Math.sqrt((n1.getX() - n2.getX()) ^ 2 + (n1.getY() - n2.getY()) ^ 2);
    }

    private static double manhattanDistance(Node n1, Node n2) {
        return Math.abs(n1.getX() - n2.getX()) + Math.abs(n1.getY() - n2.getY());
    }
}
