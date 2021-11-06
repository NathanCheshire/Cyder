package cyder.widgets;

import cyder.annotations.Widget;
import cyder.consts.CyderColors;
import cyder.consts.CyderImages;
import cyder.enums.SliderShape;
import cyder.genesis.GenesisShare;
import cyder.handlers.internal.ErrorHandler;
import cyder.ui.*;
import cyder.utilities.ColorUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;

@Widget("path")
public class PathFinderWidget {
    private static int squareLen = 30;
    private static int numSquares;
    private static JLabel gridLabel;

    private static CyderCheckBox showStepsBox;
    private static CyderCheckBox diagonalBox;
    private static CyderCheckBox deleteWallsCheckBox;
    private static CyderCheckBox optimalPathCheckBox;
    private static CyderFrame pathFindingFrame;
    private static CyderButton reset;
    private static CyderButton startButton;
    private static JSlider speedSlider;

    private static Node start;
    private static Node end;
    private static LinkedList<Node> walls;
    private static LinkedList<Node> pathableNodes;

    private static LinkedList<Node> path;
    private static int pathIndex;

    private static Timer timer;
    private static int timeoutMS = 50;
    private static int maxTimeoutMs = 100;

    private static boolean eToggled;
    private static boolean sToggled;
    private static boolean deleteWallsMode;
    private static boolean optimalPath;

    private static String pathText = "";
    private static Color pathColor = Color.darkGray;

    private static CyderButton algorithmSwitcher;
    private static CyderTextField algorithmField;
    private static int algorithmIndex;
    private static String[] algorithms = {"Manhattan","Euclidean"};

    public static void showGUI() {
        if (pathFindingFrame != null)
            pathFindingFrame.dispose();

        timer = new Timer(timeoutMS, pathFindAction);
        timer.setDelay(timeoutMS);
        walls = new LinkedList<>();
        pathableNodes = new LinkedList<>();
        path = new LinkedList<>();
        start = new Node(0,0);
        end = new Node(25, 25);
        pathText = "";

        pathFindingFrame = new CyderFrame(1000,1050, CyderImages.defaultBackgroundLarge);
        pathFindingFrame.setTitle("Path finding visualizer");

        gridLabel = new JLabel() {
            @Override
            public void paint(Graphics g) {
                super.paint(g);

                if (gridLabel != null) {
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setColor(Color.darkGray);
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

                    for (Node n : pathableNodes) {
                       if (n.getParent() != null && !n.equals(end) && n.getH() != Integer.MAX_VALUE) {
                           if (outOfBounds(n))
                               continue;

                           if (open.contains(n))
                               g2d.setColor(new Color(254, 104, 88));
                           else
                               g2d.setColor(new Color(121, 236, 135));

                           g2d.fillRect(2 + n.getX() * squareLen, 2 + n.getY() * squareLen,
                                   squareLen - 2, squareLen - 2);
                           gridLabel.repaint();
                       }
                    }

                    //draw path in blue
                    if (end != null && end.getParent() != null
                            && !path.isEmpty() && !timer.isRunning()) {
                        Node currentRef = end.getParent();

                        while (currentRef != null && currentRef != start) {
                            if (outOfBounds(currentRef))
                                break;
                           g2d.setColor(CyderColors.regularBlue);
                           g2d.fillRect(2 + currentRef.getX() * squareLen, 2 + currentRef.getY() * squareLen,
                                   squareLen - 2, squareLen - 2);
                           currentRef = currentRef.getParent();
                       }
                    }

                    //path drawing
                    if (end != null && end.getParent() != null
                            && !path.isEmpty() && !timer.isRunning()) {
                        Node refNode = end.getParent();

                        while (refNode != start) {
                            if (refNode == null)
                                break;
                            if (outOfBounds(refNode))
                                break;

                            g2d.setColor(new Color(34,216,248));
                            g2d.fillRect(2 + refNode.getX() * squareLen, 2 + refNode.getY() * squareLen,
                                    squareLen - 2, squareLen - 2);
                            refNode = refNode.getParent();
                        }

                        gridLabel.repaint();
                    }

                    //path animation square draw
                    if (!path.isEmpty()) {
                        if (!outOfBounds(path.get(pathIndex))) {
                            g2d.setColor(CyderColors.regularBlue);
                            g2d.fillRect(2 + path.get(pathIndex).getX() * squareLen, 2 +
                                            path.get(pathIndex).getY() * squareLen,
                                    squareLen - 2, squareLen - 2);
                        }
                    }

                    //draw start in pink
                    if (start != null && !outOfBounds(start)) {
                        g2d.setColor(CyderColors.intellijPink);
                        g2d.fillRect(2 + start.getX() * squareLen, 2 + start.getY() * squareLen,
                                squareLen - 2, squareLen - 2);
                        gridLabel.repaint();
                    }

                    //draw end in orange
                    if (end != null && !outOfBounds(end)) {
                        g2d.setColor(CyderColors.calculatorOrange);
                        g2d.fillRect(2 + end.getX() * squareLen, 2 + end.getY() * squareLen,
                                squareLen - 2, squareLen - 2);
                        gridLabel.repaint();
                    }

                    //draw walls in black
                    for (Node wall : walls) {
                       if (outOfBounds(wall))
                           continue;

                        g2d.setColor(CyderColors.navy);
                        g2d.fillRect(2 + wall.getX() * squareLen, 2 + wall.getY() * squareLen,
                                squareLen - 2, squareLen - 2);
                        gridLabel.repaint();
                    }

                    //draw edges again so that nothing is over them
                    g2d.setColor(CyderColors.navy);

                    g2d.drawLine(1, 1, 1, drawTo);
                    g2d.drawLine(1, 1, drawTo, 1);
                    g2d.drawLine(drawTo, 1, drawTo, drawTo);
                    g2d.drawLine(1, drawTo, drawTo, drawTo);

                    //path label smaller black text
                    int centerX = gridLabel.getX() + gridLabel.getWidth() / 2;
                    int centerY = gridLabel.getY() + gridLabel.getHeight() / 2;

                    Font labelFont = new Font("Arial Black",Font.BOLD, 50);

                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2d.setFont(labelFont);
                    g2d.setColor(pathColor);

                    FontMetrics fm = g.getFontMetrics();
                    int x = (gridLabel.getWidth() - fm.stringWidth(pathText)) / 2;
                    int y = (fm.getAscent() + (gridLabel.getHeight() - (fm.getAscent() + fm.getDescent())) / 2);
                    g.drawString(pathText, x, y);
                }
            }
        };
        gridLabel.addMouseWheelListener(e -> {
            if (timer.isRunning())
                return;

            if (e.isControlDown()) {
                if (e.getWheelRotation() == -1 && squareLen + 1 < 50) {
                    squareLen += 1;
                } else if (squareLen - 1 > 9){
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

                if (sToggled || eToggled) {
                    for (Node n : walls) {
                        if (n.equals(addNode)) {
                            walls.remove(n);
                            break;
                        }
                    }

                    if (sToggled)
                        start = addNode;
                    else
                        end = addNode;

                    path = new LinkedList<>();
                    pathIndex = 0;
                    pathableNodes = new LinkedList<>();
                    pathText = "";

                    if (start != null)
                        start.setParent(null);
                    if (end != null)
                        end.setParent(null);

                    eToggled = false;
                    sToggled = false;
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

                }
                gridLabel.repaint();
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

        CyderLabel deleteWallsLabel = new CyderLabel("Delete Walls");
        deleteWallsLabel.setBounds(70,885,100,30);
        pathFindingFrame.getContentPane().add(deleteWallsLabel);

        deleteWallsCheckBox = new CyderCheckBox();
        deleteWallsCheckBox.setNotSelected();
        deleteWallsCheckBox.setBounds(100, 920,50,50);
        pathFindingFrame.getContentPane().add(deleteWallsCheckBox);
        deleteWallsCheckBox.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                deleteWallsMode = !deleteWallsMode;
            }
        });

        CyderLabel optimalPathLabel = new CyderLabel("Optimal");
        optimalPathLabel.setBounds(150,885,100,30);
        pathFindingFrame.getContentPane().add(optimalPathLabel);

        optimalPathCheckBox = new CyderCheckBox();
        optimalPathCheckBox.setNotSelected();
        optimalPathCheckBox.setBounds(175, 920,50,50);
        optimalPathCheckBox.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                optimalPath = !optimalPath;
            }
        });
        pathFindingFrame.getContentPane().add(optimalPathCheckBox);

        CyderLabel showStepsLabel = new CyderLabel("Steps");
        showStepsLabel.setBounds(75 + 70 + 67,885,100,30);
        pathFindingFrame.getContentPane().add(showStepsLabel);

        showStepsBox = new CyderCheckBox();
        showStepsBox.setNotSelected();
        showStepsBox.setBounds(240, 920,50,50);
        pathFindingFrame.getContentPane().add(showStepsBox);

        CyderLabel diagonalStepsLabel = new CyderLabel("Diagonals");
        diagonalStepsLabel.setBounds(75 + 70 + 75 + 65,885,100,30);
        pathFindingFrame.getContentPane().add(diagonalStepsLabel);

        diagonalBox = new CyderCheckBox();
        diagonalBox.setNotSelected();
        diagonalBox.setBounds(310, 920,50,50);
        pathFindingFrame.getContentPane().add(diagonalBox);

        reset = new CyderButton("Reset");
        reset.setBounds(410,890, 170, 40);
        reset.addActionListener(e -> {
            timer.stop();
            startButton.setText("Start");
            diagonalBox.setNotSelected();
            showStepsBox.setNotSelected();
            deleteWallsCheckBox.setNotSelected();
            diagonalBox.setEnabled(true);
            showStepsBox.setEnabled(true);
            deleteWallsCheckBox.setEnabled(true);
            speedSlider.setValue(50);
            start = null;
            end = null;
            walls = new LinkedList<>();
            pathableNodes = new LinkedList<>();
            path = new LinkedList<>();
            pathText = "";
            squareLen = 30;
            gridLabel.repaint();
        });
        pathFindingFrame.getContentPane().add(reset);

        startButton = new CyderButton("Start");
        startButton.setBounds(410,940, 170, 40);
        startButton.addActionListener(e -> {
            if (start == null || end == null) {
                pathFindingFrame.notify("Start/end nodes not set");
            } else if (!timer.isRunning()) {
                startButton.setText("Stop");
                pathText = "";

                diagonalBox.setEnabled(false);
                showStepsBox.setEnabled(false);
                deleteWallsCheckBox.setEnabled(false);

                searchSetup();
            } else {
                timer.stop();
                startButton.setText("Start");
                pathText = "";

                diagonalBox.setEnabled(true);
                showStepsBox.setEnabled(true);
                deleteWallsCheckBox.setEnabled(true);
            }
        });
        pathFindingFrame.getContentPane().add(startButton);

        algorithmField = new CyderTextField(0);
        algorithmField.setFocusable(false);
        algorithmField.setBounds(410,990, 140, 40);
        algorithmField.setEditable(false);
        pathFindingFrame.getContentPane().add(algorithmField);
        algorithmField.setText(algorithms[algorithmIndex]);

        algorithmSwitcher = new CyderButton("▼");
        algorithmSwitcher.setBounds(410 + 130,990,40,40);
        pathFindingFrame.getContentPane().add(algorithmSwitcher);
        algorithmSwitcher.addActionListener(e -> {
            algorithmIndex++;
            if (algorithmIndex == algorithms.length)
                algorithmIndex = 0;

            algorithmField.setText(algorithms[algorithmIndex]);
        });

        speedSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
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
        speedSlider.setValue(50);
        speedSlider.addChangeListener(e -> {
            timeoutMS = (int) (maxTimeoutMs *
                    ((double) speedSlider.getValue() /  (double) speedSlider.getMaximum()));
            timer.setDelay(timeoutMS);
        });
        speedSlider.setOpaque(false);
        speedSlider.setToolTipText("Pathfinding Animation Timeout");
        speedSlider.setFocusable(false);
        speedSlider.repaint();
        pathFindingFrame.getContentPane().add(speedSlider);

        pathFindingFrame.setVisible(true);
        pathFindingFrame.setLocationRelativeTo(GenesisShare.getDominantFrame());

        Timer pathDrawingTimer = new Timer(50, pathDrawingAnimation);
        pathDrawingTimer.start();

        Timer pathLabelTimer = new Timer(1000, pathLabelAnimation);
        pathLabelTimer.start();
    }

    private static ActionListener pathDrawingAnimation = evt -> {
        try {
            if (pathIndex + 1 < path.size())
                pathIndex++;
            else
                pathIndex = 0;

            gridLabel.repaint();
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    };

    private static ActionListener pathLabelAnimation = evt -> {
        try {
           if (pathColor == Color.darkGray)
               pathColor = ColorUtil.inverse(Color.darkGray);
           else
               pathColor = Color.darkGray;

            gridLabel.repaint();
        } catch (Exception e) {
            ErrorHandler.handle(e);
        }
    };

    //open list outside methods to allow access to a* inner
    private static PriorityQueue<Node> open;

    //performs the setup for the A* algorithm so that the timer can call update to interate over the next nodes
    private static void searchSetup() {
        //get rid of lingering path if exists
        path = new LinkedList<>();
        pathText = "";
        end.setParent(null);
        start.setParent(null);
        gridLabel.repaint();

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

        open = new PriorityQueue<>(new NodeComparator());

        //put start in the open
        start.setG(0);
        start.setH(heuristic(end));
        open.add(start);

        //reset path stuff
        path = new LinkedList<>();
        pathIndex = 0;

        //animation chosen
        if (showStepsBox.isSelected()) {
            timer.start();
            //spins off below action listener to update grid until path found or no path found or user intervention
        } else {
            //instantly solve and paint grid and animate path if found and show words PATH or NO PATH

            while (!open.isEmpty()) {
                Node min = open.poll();
                open.remove(min);

                if (min.equals(end) && optimalPath) {
                    end.setParent(min.getParent());
                } else if (min.equals(end)) {
                    end.setParent(min.getParent());
                    pathFound();
                    return;
                }

                //generate neihbors of this current node
                LinkedList<Node> neighbors = new LinkedList<>();

                for (Node possibleNeighbor : pathableNodes) {
                    if (areOrthogonalNeighbors(possibleNeighbor, min) ||
                            (areDiagonalNeighbors(possibleNeighbor, min) && diagonalBox.isSelected())) {
                        neighbors.add(possibleNeighbor);
                    }
                }

                for (Node neighbor: neighbors) {
                    //calculate new H
                    double newH = heuristic(neighbor);

                    if (newH < neighbor.getH()) {
                        neighbor.setH(newH);
                        neighbor.setParent(min);
                        neighbor.setG(min.getG() + euclideanDistance(min, neighbor));

                        if (!open.contains(neighbor)) {
                            open.add(neighbor);
                        }
                    }
                }
            }

            if (end.getParent() != null) {
                pathFound();
            } else {
                pathNotFound();
            }
        }
    }

    //timer update action (while loop of a*) for animation purposes
    private static ActionListener pathFindAction = evt -> {
        if (!open.isEmpty()) {
            Node min = open.poll();
            open.remove(min);

            if (min.equals(end) && optimalPath) {
                end.setParent(min.getParent());
            } else if (min.equals(end)) {
                end.setParent(min.getParent());
                pathFound();
                return;
            }

            //generate neihbors of this current node
            LinkedList<Node> neighbors = new LinkedList<>();

            for (Node possibleNeighbor : pathableNodes) {
                if (areOrthogonalNeighbors(possibleNeighbor, min) ||
                        (areDiagonalNeighbors(possibleNeighbor, min) && diagonalBox.isSelected())) {
                    neighbors.add(possibleNeighbor);
                }
            }

            for (Node neighbor: neighbors) {
                //calculate new H
                double newH = heuristic(neighbor);

                if (newH < neighbor.getH()) {
                    neighbor.setH(newH);
                    neighbor.setParent(min);
                    neighbor.setG(min.getG() + euclideanDistance(min, neighbor));

                    if (!open.contains(neighbor)) {
                        open.add(neighbor);
                    }
                }
            }
        } else {
            if (end.getParent() != null) {
                pathFound();
            } else {
                pathNotFound();
            }
        }
    };

    //indicates a path was found and finished animating so takes the proper actions given this criteria
    private static void pathFound() {
        timer.stop();
        startButton.setText("Start");
        diagonalBox.setEnabled(true);
        showStepsBox.setEnabled(true);
        deleteWallsCheckBox.setEnabled(true);

        pathText = "PATH FOUND";

        Node refNode = end.getParent();

        while (refNode != start) {
            path.add(refNode);
            refNode = refNode.getParent();
        }

        LinkedList<Node> pathReversed = new LinkedList<>();

        for (int i = path.size() - 1 ; i > -1 ; i--) {
            pathReversed.add(path.get(i));
        }

        path = pathReversed;

        gridLabel.repaint();
    }

    //indicates a path was not found so takes the proper actions given this criteria
    private static void pathNotFound() {
        timer.stop();
        startButton.setText("Start");
        diagonalBox.setEnabled(true);
        showStepsBox.setEnabled(true);
        deleteWallsCheckBox.setEnabled(true);

        pathText = "PATH NOT FOUND";

        path = new LinkedList<>();
        pathIndex = 0;

        gridLabel.repaint();
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
        if (algorithmIndex == 0) {
            return manhattanDistance(n, end);
        } else {
            return euclideanDistance(n, end);
        }
    }

    //distance from node to start
    private static double calcGCost(Node n) {
        return euclideanDistance(n, start);
    }

    private static double euclideanDistance(Node n1, Node n2) {
        double distnace =
                Math.sqrt(Math.pow((n1.getX() - n2.getX()), 2) + Math.pow((n1.getY() - n2.getY()), 2));
        return distnace;
    }

    private static double manhattanDistance(Node n1, Node n2) {
        return Math.abs(n1.getX() - n2.getX()) + Math.abs(n1.getY() - n2.getY());
    }

    private static class NodeComparator implements Comparator<Node> {
        @Override
        public int compare(Node node1, Node node2) {
            if (node1.getF() > node2.getF())
                return 1;
            else if (node1.getF() < node2.getF())
                return -1;
            else {
                if (node1.getH() > node2.getH())
                    return 1;
                else if (node1.getH() < node2.getH())
                    return -1;
                else
                    return 0;
            }
        }
    }

    private static boolean outOfBounds(Node n1) {
        return (n1.getX() >= numSquares || n1.getY() >= numSquares);
    }

    //node class

    private static class Node {
        private int x;
        private int y;
        private double g = Integer.MAX_VALUE;
        private double h = Integer.MAX_VALUE;
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

        public double getG() {
            return g;
        }

        public void setG(double g) {
            this.g = g;
        }

        public double getH() {
            return h;
        }

        public void setH(double h) {
            this.h = h;
        }

        public double getF() {
            return this.h + this.g;
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

        public Node() {
            this.x = 0;
            this.y = 0;
        }

        @Override
        public boolean equals(Object n) {
            if (n == null)
                return false;
            if (!(n instanceof Node))
                return false;
            else {
                return ((Node) n).getX() == x && ((Node) n).getY() == y;
            }
        }

        @Override
        public String toString() {
            return x + "," + y;
        }
    }

}
