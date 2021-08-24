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

    private static CyderCheckBox showStepsBox;
    private static CyderCheckBox diagonalBox;
    private static CyderCheckBox deleteWallsCheckBox;
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

    private static boolean eToggled;
    private static boolean sToggled;
    private static boolean deleteWallsMode;

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

                    //draw checked nodes in green
                   for (Node n : pathableNodes) {
                       if (n.getParent() != null && !n.equals(end)) {
                           g2d.setColor(CyderColors.regularGreen);
                           g2d.fillRect(2 + n.getX() * squareLen, 2 + n.getY() * squareLen,
                                   squareLen - 2, squareLen - 2);
                           gridLabel.repaint();
                       }
                   }

                    //draw path in blue
                    if (end != null && end.getParent() != null) {
                       Node currentRef = end.getParent();

                       while (currentRef != null && currentRef != start) {
                           g2d.setColor(CyderColors.regularBlue);
                           g2d.fillRect(2 + currentRef.getX() * squareLen, 2 + currentRef.getY() * squareLen,
                                   squareLen - 2, squareLen - 2);
                           currentRef = currentRef.getParent();
                       }
                    }

                    //draw start in pink
                    if (start != null) {
                        g2d.setColor(CyderColors.intellijPink);
                        g2d.fillRect(2 + start.getX() * squareLen, 2 + start.getY() * squareLen,
                                squareLen - 2, squareLen - 2);
                        gridLabel.repaint();
                    }

                    //draw in orange
                    if (end != null) {
                        g2d.setColor(CyderColors.calculatorOrange);
                        g2d.fillRect(2 + end.getX() * squareLen, 2 + end.getY() * squareLen,
                                squareLen - 2, squareLen - 2);
                        gridLabel.repaint();
                    }

                    //draw walls in black
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
            if (timer.isRunning())
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

        int condition = JComponent.WHEN_IN_FOCUSED_WINDOW;
        InputMap inputMap = gridLabel.getInputMap(condition);
        ActionMap actionMap = gridLabel.getActionMap();

        KeyStroke sStroke = KeyStroke.getKeyStroke(KeyEvent.VK_S, 0);
        String skey = "skey";

        KeyStroke eStroke = KeyStroke.getKeyStroke(KeyEvent.VK_E, 0);
        String ekey = "ekey";

        inputMap.put(sStroke, skey);
        actionMap.put(skey, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sToggled = !sToggled;
            }
        });

        inputMap.put(eStroke, ekey);
        actionMap.put(ekey, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                eToggled = !eToggled;
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

                if (sToggled) {
                    for (Node n : walls) {
                        if (n.equals(addNode)) {
                            walls.remove(n);
                            break;
                        }
                    }
                    System.out.println("here");
                    start = addNode;
                    sToggled = false;
                    gridLabel.repaint();
                } else if (eToggled) {
                    for (Node n : walls) {
                        if (n.equals(addNode)) {
                            walls.remove(n);
                            break;
                        }
                    }

                    end = addNode;
                    eToggled = false;
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

                if (deleteWallsMode) {
                    walls.remove(addNode);
                } else {
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
                }

                gridLabel.repaint();
            }
        });
        gridLabel.setSize(800,800);
        gridLabel.setLocation(100,80);
        gridLabel.setFocusable(true);
        pathFindingFrame.getContentPane().add(gridLabel);

        CyderLabel setEndLabel = new CyderLabel("Delete Walls");
        setEndLabel.setBounds(75 + 70,885,100,30);
        pathFindingFrame.getContentPane().add(setEndLabel);

        deleteWallsCheckBox = new CyderCheckBox();
        deleteWallsCheckBox.setNotSelected();
        deleteWallsCheckBox.setBounds(170, 920,50,50);
        pathFindingFrame.getContentPane().add(deleteWallsCheckBox);
        deleteWallsCheckBox.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                deleteWallsMode = !deleteWallsMode;
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
            startButton.setText("Start");
            diagonalBox.setNotSelected();
            showStepsBox.setNotSelected();
            deleteWallsCheckBox.setNotSelected();
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
            if (start == null || end == null) {
                pathFindingFrame.notify("Start/end nodes not set");
            } else if (!timer.isRunning()) {
                startButton.setText("Stop");

                diagonalBox.setEnabled(false);
                showStepsBox.setEnabled(false);
                deleteWallsCheckBox.setEnabled(false);

                searchSetup();
            } else {
                timer.stop();
                startButton.setText("Start");

                diagonalBox.setEnabled(true);
                showStepsBox.setEnabled(true);
                deleteWallsCheckBox.setEnabled(true);
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

    //open and closed list out side methods to allow access to a* inner
    private static LinkedList<Node> open;
    private static LinkedList<Node> closed;

    //performs the setup for the A* algorithm so that the timer can call update to interate over the next nodes
    private static void searchSetup() {
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

        open = new LinkedList<>();
        closed = new LinkedList<>();

        //put start in the open with f(start) = h(start)
        start.setF(heuristic(start));
        start.setH(heuristic(start));
        start.setG(0);

        timer.start();
    }

    //timer update action (while loop of a*)
    private static ActionListener pathFindAction = evt -> {
        if (open.isEmpty()) {
            pathNotFound();
        } else {

        }
    };

    //indicates a path was found and finished animating so takes the proper actions given this criteria
    private static void pathFound() {
        //end the simulation if still running, stop the simulation,
        // show path found text, animate drawing the path over and over until reset or start are pressed
    }

    //indicates a path was not found so takes the proper actions given this criteria
    private static void pathNotFound() {
        //end the simulation/animation
        //show no path found text until reset or start buttons are pressed
    }

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
