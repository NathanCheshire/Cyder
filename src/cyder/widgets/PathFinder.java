package cyder.widgets;

import cyder.consts.CyderColors;
import cyder.enums.SliderShape;
import cyder.ui.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
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

    private static long timeoutMS = 500;
    private static long maxTimeoutMs = 1000;

    private static boolean simulationRunning;

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
                        g2d.setColor(CyderColors.calculatorOrange);
                        g2d.fillRect(2 + end.getX() * squareLen, 2 + end.getY() * squareLen,
                                squareLen - 2, squareLen - 2);
                        gridLabel.repaint();
                    }

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
            if (simulationRunning)
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
                if (simulationRunning)
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
                if (simulationRunning)
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
            simulationRunning = false; //to cause possible pathfinding/animation loop to terminate
            diagonalBox.setNotSelected();
            setEndBox.setNotSelected();
            setStartBox.setNotSelected();
            showStepsBox.setNotSelected();
            speedSlider.setValue(500);
            start = null;
            end = null;
            walls = new LinkedList<>();
            gridLabel.repaint();
        });
        pathFindingFrame.getContentPane().add(reset);

        startButton = new CyderButton("Start");
        startButton.setBounds(420,935, 150, 40);
        startButton.addActionListener(e -> {
            if (start == null || end == null) {
                pathFindingFrame.notify("Start/end nodes not set");
            } else {
                //todo disable box checking
                simulationRunning = true;
                findPath();
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
        speedSlider.addChangeListener(e -> timeoutMS = (long) (maxTimeoutMs *
                ((double) speedSlider.getValue() /  (double) speedSlider.getMaximum())));
        speedSlider.setOpaque(false);
        speedSlider.setToolTipText("Speed");
        speedSlider.setFocusable(false);
        speedSlider.repaint();
        pathFindingFrame.getContentPane().add(speedSlider);

        pathFindingFrame.setVisible(true);
        ConsoleFrame.getConsoleFrame().setFrameRelative(pathFindingFrame);
    }

    private static void findPath() {
        //todo found path will be in blue
        //todo checked nodes in green

        //todo implement algorithm now and worry about diagonals in addition
        // to orthogonals after, also worry about showing steps after

        //todo if end has no parent then no path found

        //make grid of pathable nodes that we will use to draw the path
        pathableNodes = new LinkedList<>();
        for (int x = 0 ; x < numSquares ; x++) {
            for (int y = 0 ; y < numSquares ; y++) {
                Node addNode = new Node(x, y);
                boolean isStart = addNode.equals(start);
                boolean isEnd = addNode.equals(end);

                boolean isWall = false;

                for (Node n : walls) {
                    if (n.equals(addNode)) {
                        isWall = true;
                        break;
                    }
                }

                if (!isStart && !isEnd && !isWall) {
                    pathableNodes.add(addNode);
                }
            }
        }


        //init lists
        LinkedList<Node> open = new LinkedList<>();
        LinkedList<Node> closed = new LinkedList<>();

        //add start to the open list and leave it's f cost at 0
        open.add(start);

        //loop until we find a node
        while (!open.isEmpty()) {
            //get node from open with least f cost
            Node minPQ = null;

            for (Node n: open) {
               if (minPQ == null)
                   minPQ = n;
               else if (n.getF() < minPQ.getF())
                   minPQ = n;
            }

            //remove minPQ from open and add to closed
            for (Node n : open) {
                if (n.equals(minPQ)) {
                    open.remove(n);
                    break;
                }
            }
            closed.add(minPQ);

            //if goal, then return and construct path using parent
            if (minPQ.equals(end)) {
                end.setParent(minPQ.getParent());
                return;
            }

            //generate neighbors of node
            LinkedList<Node> neighbors = new LinkedList<>();

            for (int x = -1 ; x < 2 ; x++) {
                for (int y = -1 ; y < 2 ; y++) {
                    //if in bounds
                    if (minPQ.getX() + x >= 0 && minPQ.getX() + x < numSquares &&
                            minPQ.getY() + y >= 0 && minPQ.getY() + y < numSquares) {
                        //if not self
                        if (x == 0 && y == 0)
                            continue;

                        Node addNode = new Node(x + minPQ.getX(), y + minPQ.getY());

                        //if in pathable nodes
                        boolean isPathable = false;

                        for (Node n : pathableNodes) {
                            if (n.equals(addNode)) {
                                isPathable = true;
                                break;
                            }
                        }

                        if (!isPathable)
                            continue;

                        boolean diagonalNeighbor = addNode.getX() != minPQ.getX() && addNode.getY() != minPQ.getY();

                        //if orthogonal neighbor
                        if (!diagonalNeighbor) {
                            neighbors.add(addNode);
                            addNode.setParent(minPQ);
                        }
                        //if diagonal neighbor
                        else if (diagonalBox.isSelected()) {
                            neighbors.add(addNode);
                            addNode.setParent(minPQ);
                        }
                    }
                }
            }

            //for all neighbors
            for (Node neighbor : neighbors) {
                //continue if neighbor is in the closed list
                boolean inClosed = false;

                for (Node c: closed) {
                    if (c.equals(neighbor)) {
                        inClosed = true;
                        break;
                    }
                }

                if (inClosed)
                    return;

                //otherwise, set costs
                neighbor.setH(calcHCost(neighbor));
                neighbor.setG(calcGCost(neighbor));

                //if neighbor is in the open list
                boolean inOpen = false;

                for (Node c: open) {
                    if (c.equals(neighbor)) {
                        inOpen = true;
                        break;
                    }
                }

                if (inOpen) {
                    //if the neighbor's G is higher than minPQ's G, continue
                    if (neighbor.getG() > minPQ.getG())
                        continue;
                }

                //add neighbor to open list
                System.out.println(neighbors.size());
                open.add(neighbor);
            }

            System.out.println(end.getParent());
        }

    }

    private static double calcHCost(Node n) {
        return euclideanDistance(n, end);
    }

    private static double calcGCost(Node n) {
        return euclideanDistance(n, start);
    }

    private static double euclideanDistance(Node n1, Node n2) {
        return Math.sqrt((n1.getX() - n2.getX()) ^ 2 + (n1.getY() - n2.getY()) ^ 2);
    }

    private static class Node {
       private int x;
       private int y;
       private double h;
       private double g;
       private Node parent;

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public double getF() {
            return h + g;
        }

        public double getH() {
            return h;
        }

        public void setH(double h) {
            this.h = h;
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

       private boolean isPath;

        public boolean isPath() {
            return isPath;
        }

        public void setPath(boolean path) {
            isPath = path;
        }
    }
}
