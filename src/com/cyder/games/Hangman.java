package com.cyder.games;

import com.cyder.utilities.GeneralUtil;
import com.cyder.ui.CyderButton;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

public class Hangman {

    private JFrame HangmanFrame;
    private GeneralUtil hangmanGeneralUtil;

    private String HangmanWord;

    private CyderButton HangmanReset;
    private CyderButton HangmanZ;
    private CyderButton HangmanY;
    private CyderButton HangmanX;
    private CyderButton HangmanW;
    private CyderButton HangmanV;
    private CyderButton HangmanU;
    private CyderButton HangmanT;
    private CyderButton HangmanS;
    private CyderButton HangmanR;
    private CyderButton HangmanQ;
    private CyderButton HangmanP;
    private CyderButton HangmanO;
    private CyderButton HangmanN;
    private CyderButton HangmanM;
    private CyderButton HangmanL;
    private CyderButton HangmanK;
    private CyderButton HangmanJ;
    private CyderButton HangmanI;
    private CyderButton HangmanH;
    private CyderButton HangmanG;
    private CyderButton HangmanF;
    private CyderButton HangmanE;
    private CyderButton HangmanD;
    private CyderButton HangmanC;
    private CyderButton HangmanB;
    private CyderButton HangmanA;

    private JLabel HangmanImageLabel;
    private JLabel HangmanLabel;

    private int HangmanWrongGuesses = 1;

    public void startHangman() {
        hangmanGeneralUtil = new GeneralUtil();

        if (HangmanFrame != null)
            hangmanGeneralUtil.closeAnimation(HangmanFrame);

        HangmanFrame = new JFrame();

        HangmanFrame.setTitle("Hangman");

        HangmanFrame.setResizable(false);

        HangmanFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        ImageIcon img = new ImageIcon("src/com/cyder/io/pictures/hangman.png");

        HangmanFrame.setIconImage(img.getImage());

        JPanel parentPanel = new JPanel();

        parentPanel.setLayout(new BoxLayout(parentPanel,BoxLayout.Y_AXIS));

        HangmanLabel = new JLabel("Hangman");

        HangmanLabel.setFont(new Font("sans serif", Font.BOLD, 30));

        JPanel LabelPanel = new JPanel();

        LabelPanel.add(HangmanLabel, SwingConstants.CENTER);

        LabelPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        parentPanel.add(LabelPanel);

        HangmanImageLabel = new JLabel();

        HangmanImageLabel.setIcon(new ImageIcon("src/com/cyder/io/pictures/hangman.png"));

        JPanel ImagePanel = new JPanel();

        ImagePanel.add(HangmanImageLabel, SwingConstants.CENTER);

        ImagePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        parentPanel.add(ImagePanel);

        JPanel ButtonPanel = new JPanel();

        GridLayout ButtonLayout = new GridLayout(2,13,5,5);

        ButtonPanel.setLayout(ButtonLayout);

        HangmanA = new CyderButton("A");

        HangmanA.setPreferredSize(new Dimension(60, 60));

        HangmanA.setFocusPainted(false);

        HangmanA.setBackground(hangmanGeneralUtil.vanila);

        HangmanA.setFont(hangmanGeneralUtil.weatherFontSmall);

        HangmanA.addActionListener(e -> {
            LetterChosen("a");
            HangmanA.setEnabled(false);
            HangmanA.setForeground(hangmanGeneralUtil.regularRed);
        });

        ButtonPanel.add(HangmanA);

        HangmanB = new CyderButton("B");

        HangmanB.setPreferredSize(new Dimension(60, 60));

        HangmanB.setFocusPainted(false);

        HangmanB.setBackground(hangmanGeneralUtil.vanila);

        HangmanB.setFont(hangmanGeneralUtil.weatherFontSmall);

        HangmanB.addActionListener(e -> {
            LetterChosen("b");
            HangmanB.setEnabled(false);
            HangmanB.setForeground(hangmanGeneralUtil.regularRed);
        });

        ButtonPanel.add(HangmanB);

        HangmanC = new CyderButton("C");

        HangmanC.setPreferredSize(new Dimension(60, 60));

        HangmanC.setFocusPainted(false);

        HangmanC.setBackground(hangmanGeneralUtil.vanila);

        HangmanC.setFont(hangmanGeneralUtil.weatherFontSmall);

        HangmanC.addActionListener(e -> {
            LetterChosen("c");
            HangmanC.setEnabled(false);
            HangmanC.setForeground(hangmanGeneralUtil.regularRed);
        });

        ButtonPanel.add(HangmanC);

        HangmanD = new CyderButton("D");

        HangmanD.setPreferredSize(new Dimension(60, 60));

        HangmanD.setFocusPainted(false);

        HangmanD.setBackground(hangmanGeneralUtil.vanila);

        HangmanD.setFont(hangmanGeneralUtil.weatherFontSmall);

        HangmanD.addActionListener(e -> {
            LetterChosen("d");
            HangmanD.setEnabled(false);
            HangmanD.setForeground(hangmanGeneralUtil.regularRed);
        });

        ButtonPanel.add(HangmanD);

        HangmanE = new CyderButton("E");

        HangmanE.setPreferredSize(new Dimension(60, 60));

        HangmanE.setFocusPainted(false);

        HangmanE.setBackground(hangmanGeneralUtil.vanila);

        HangmanE.setFont(hangmanGeneralUtil.weatherFontSmall);

        HangmanE.addActionListener(e -> {
            LetterChosen("e");

            HangmanE.setEnabled(false);

            HangmanE.setForeground(hangmanGeneralUtil.regularRed);
        });

        ButtonPanel.add(HangmanE);

        HangmanF = new CyderButton("F");

        HangmanF.setPreferredSize(new Dimension(60, 60));

        HangmanF.setFocusPainted(false);

        HangmanF.setBackground(hangmanGeneralUtil.vanila);

        HangmanF.setFont(hangmanGeneralUtil.weatherFontSmall);

        HangmanF.addActionListener(e -> {
            LetterChosen("f");
            HangmanF.setEnabled(false);
            HangmanF.setForeground(hangmanGeneralUtil.regularRed);
        });

        ButtonPanel.add(HangmanF);

        HangmanG = new CyderButton("G");

        HangmanG.setPreferredSize(new Dimension(60, 60));

        HangmanG.setFocusPainted(false);

        HangmanG.setBackground(hangmanGeneralUtil.vanila);

        HangmanG.setFont(hangmanGeneralUtil.weatherFontSmall);

        HangmanG.addActionListener(e -> {
            LetterChosen("g");
            HangmanG.setEnabled(false);
            HangmanG.setForeground(hangmanGeneralUtil.regularRed);
        });

        ButtonPanel.add(HangmanG);

        HangmanH = new CyderButton("H");

        HangmanH.setPreferredSize(new Dimension(60, 60));

        HangmanH.setFocusPainted(false);

        HangmanH.setBackground(hangmanGeneralUtil.vanila);

        HangmanH.setFont(hangmanGeneralUtil.weatherFontSmall);

        HangmanH.addActionListener(e -> {
            LetterChosen("h");
            HangmanH.setEnabled(false);
            HangmanH.setForeground(hangmanGeneralUtil.regularRed);
        });

        ButtonPanel.add(HangmanH);

        HangmanI = new CyderButton("I");

        HangmanI.setPreferredSize(new Dimension(60, 60));

        HangmanI.setFocusPainted(false);

        HangmanI.setBackground(hangmanGeneralUtil.vanila);

        HangmanI.setFont(hangmanGeneralUtil.weatherFontSmall);

        HangmanI.addActionListener(e -> {
            LetterChosen("i");
            HangmanI.setEnabled(false);
            HangmanI.setForeground(hangmanGeneralUtil.regularRed);
        });

        ButtonPanel.add(HangmanI);

        HangmanJ = new CyderButton("J");

        HangmanJ.setPreferredSize(new Dimension(60, 60));

        HangmanJ.setFocusPainted(false);

        HangmanJ.setBackground(hangmanGeneralUtil.vanila);

        HangmanJ.setFont(hangmanGeneralUtil.weatherFontSmall);

        HangmanJ.addActionListener(e -> {
            LetterChosen("j");
            HangmanJ.setEnabled(false);
            HangmanJ.setForeground(hangmanGeneralUtil.regularRed);
        });

        ButtonPanel.add(HangmanJ);

        HangmanK = new CyderButton("K");

        HangmanK.setPreferredSize(new Dimension(60, 60));

        HangmanK.setFocusPainted(false);

        HangmanK.setBackground(hangmanGeneralUtil.vanila);

        HangmanK.setFont(hangmanGeneralUtil.weatherFontSmall);

        HangmanK.addActionListener(e -> {
            LetterChosen("k");
            HangmanK.setEnabled(false);
            HangmanK.setForeground(hangmanGeneralUtil.regularRed);
        });

        ButtonPanel.add(HangmanK);

        HangmanL = new CyderButton("L");

        HangmanL.setPreferredSize(new Dimension(60, 60));

        HangmanL.setFocusPainted(false);

        HangmanL.setBackground(hangmanGeneralUtil.vanila);

        HangmanL.setFont(hangmanGeneralUtil.weatherFontSmall);

        HangmanL.addActionListener(e -> {
            LetterChosen("l");
            HangmanL.setEnabled(false);
            HangmanL.setForeground(hangmanGeneralUtil.regularRed);
        });

        ButtonPanel.add(HangmanL);

        HangmanM = new CyderButton("M");

        HangmanM.setPreferredSize(new Dimension(60, 60));

        HangmanM.setFocusPainted(false);

        HangmanM.setBackground(hangmanGeneralUtil.vanila);

        HangmanM.setFont(hangmanGeneralUtil.weatherFontSmall);

        HangmanM.addActionListener(e -> {
            LetterChosen("m");
            HangmanM.setEnabled(false);
            HangmanM.setForeground(hangmanGeneralUtil.regularRed);
        });

        ButtonPanel.add(HangmanM);

        HangmanN = new CyderButton("N");

        HangmanN.setPreferredSize(new Dimension(60, 60));

        HangmanN.setFocusPainted(false);

        HangmanN.setBackground(hangmanGeneralUtil.vanila);

        HangmanN.setFont(hangmanGeneralUtil.weatherFontSmall);

        HangmanN.addActionListener(e -> {
            LetterChosen("n");
            HangmanN.setEnabled(false);
            HangmanN.setForeground(hangmanGeneralUtil.regularRed);
        });

        ButtonPanel.add(HangmanN);

        HangmanO = new CyderButton("O");

        HangmanO.setPreferredSize(new Dimension(60, 60));

        HangmanO.setFocusPainted(false);

        HangmanO.setBackground(hangmanGeneralUtil.vanila);

        HangmanO.setFont(hangmanGeneralUtil.weatherFontSmall);

        HangmanO.addActionListener(e -> {
            LetterChosen("o");
            HangmanO.setEnabled(false);
            HangmanO.setForeground(hangmanGeneralUtil.regularRed);
        });

        ButtonPanel.add(HangmanO);

        HangmanP = new CyderButton("P");

        HangmanP.setPreferredSize(new Dimension(60, 60));

        HangmanP.setFocusPainted(false);

        HangmanP.setBackground(hangmanGeneralUtil.vanila);

        HangmanP.setFont(hangmanGeneralUtil.weatherFontSmall);

        HangmanP.addActionListener(e -> {
            LetterChosen("p");
            HangmanP.setEnabled(false);
            HangmanP.setForeground(hangmanGeneralUtil.regularRed);
        });

        ButtonPanel.add(HangmanP);

        HangmanQ = new CyderButton("Q");

        HangmanQ.setPreferredSize(new Dimension(60, 60));

        HangmanQ.setFocusPainted(false);

        HangmanQ.setBackground(hangmanGeneralUtil.vanila);

        HangmanQ.setFont(hangmanGeneralUtil.weatherFontSmall);

        HangmanQ.addActionListener(e -> {
            LetterChosen("q");
            HangmanQ.setEnabled(false);
            HangmanQ.setForeground(hangmanGeneralUtil.regularRed);
        });

        ButtonPanel.add(HangmanQ);

        HangmanR = new CyderButton("R");

        HangmanR.setPreferredSize(new Dimension(60, 60));

        HangmanR.setFocusPainted(false);

        HangmanR.setBackground(hangmanGeneralUtil.vanila);

        HangmanR.setFont(hangmanGeneralUtil.weatherFontSmall);

        HangmanR.addActionListener(e -> {
            LetterChosen("r");
            HangmanR.setEnabled(false);
            HangmanR.setForeground(hangmanGeneralUtil.regularRed);
        });

        ButtonPanel.add(HangmanR);

        HangmanS = new CyderButton("S");

        HangmanS.setPreferredSize(new Dimension(60, 60));

        HangmanS.setFocusPainted(false);

        HangmanS.setBackground(hangmanGeneralUtil.vanila);

        HangmanS.setFont(hangmanGeneralUtil.weatherFontSmall);

        HangmanS.addActionListener(e -> {
            LetterChosen("s");
            HangmanS.setEnabled(false);
            HangmanS.setForeground(hangmanGeneralUtil.regularRed);
        });

        ButtonPanel.add(HangmanS);

        HangmanT = new CyderButton("T");

        HangmanT.setPreferredSize(new Dimension(60, 60));

        HangmanT.setFocusPainted(false);

        HangmanT.setBackground(hangmanGeneralUtil.vanila);

        HangmanT.setFont(hangmanGeneralUtil.weatherFontSmall);

        HangmanT.addActionListener(e -> {
            LetterChosen("t");
            HangmanT.setEnabled(false);
            HangmanT.setForeground(hangmanGeneralUtil.regularRed);
        });

        ButtonPanel.add(HangmanT);

        HangmanU = new CyderButton("U");

        HangmanU.setPreferredSize(new Dimension(60, 60));

        HangmanU.setFocusPainted(false);

        HangmanU.setBackground(hangmanGeneralUtil.vanila);

        HangmanU.setFont(hangmanGeneralUtil.weatherFontSmall);

        HangmanU.addActionListener(e -> {
            LetterChosen("u");
            HangmanU.setEnabled(false);
            HangmanU.setForeground(hangmanGeneralUtil.regularRed);
        });

        ButtonPanel.add(HangmanU);

        HangmanV = new CyderButton("V");

        HangmanV.setPreferredSize(new Dimension(60, 60));

        HangmanV.setFocusPainted(false);

        HangmanV.setBackground(hangmanGeneralUtil.vanila);

        HangmanV.setFont(hangmanGeneralUtil.weatherFontSmall);

        HangmanV.addActionListener(e -> {
            LetterChosen("v");
            HangmanV.setEnabled(false);
            HangmanV.setForeground(hangmanGeneralUtil.regularRed);
        });

        ButtonPanel.add(HangmanV);

        HangmanW = new CyderButton("W");

        HangmanW.setPreferredSize(new Dimension(60, 60));

        HangmanW.setFocusPainted(false);

        HangmanW.setBackground(hangmanGeneralUtil.vanila);

        HangmanW.setFont(hangmanGeneralUtil.weatherFontSmall);

        HangmanW.addActionListener(e -> {
            LetterChosen("w");
            HangmanW.setEnabled(false);
            HangmanW.setForeground(hangmanGeneralUtil.regularRed);
        });

        ButtonPanel.add(HangmanW);

        HangmanX = new CyderButton("X");

        HangmanX.setPreferredSize(new Dimension(60, 60));

        HangmanX.setFocusPainted(false);

        HangmanX.setBackground(hangmanGeneralUtil.vanila);

        HangmanX.setFont(hangmanGeneralUtil.weatherFontSmall);

        HangmanX.addActionListener(e -> {
            LetterChosen("x");
            HangmanX.setEnabled(false);
            HangmanX.setForeground(hangmanGeneralUtil.regularRed);
        });

        ButtonPanel.add(HangmanX);

        HangmanY = new CyderButton("Y");

        HangmanY.setPreferredSize(new Dimension(60, 60));

        HangmanY.setFocusPainted(false);

        HangmanY.setBackground(hangmanGeneralUtil.vanila);

        HangmanY.setFont(hangmanGeneralUtil.weatherFontSmall);

        HangmanY.addActionListener(e -> {
            LetterChosen("y");
            HangmanY.setEnabled(false);
            HangmanY.setForeground(hangmanGeneralUtil.regularRed);
        });

        ButtonPanel.add(HangmanY);

        HangmanZ = new CyderButton("Z");

        HangmanZ.setPreferredSize(new Dimension(60, 60));

        HangmanZ.setFocusPainted(false);

        HangmanZ.setBackground(hangmanGeneralUtil.vanila);

        HangmanZ.setFont(hangmanGeneralUtil.weatherFontSmall);

        HangmanZ.addActionListener(e -> {
            LetterChosen("z");
            HangmanZ.setEnabled(false);
            HangmanZ.setForeground(hangmanGeneralUtil.regularRed);
        });

        ButtonPanel.add(HangmanZ);

        parentPanel.add(ButtonPanel);

        JPanel ResetPanel = new JPanel();

        ResetPanel.setLayout(new BorderLayout());

        HangmanReset = new CyderButton("Reset");

        HangmanReset.setFocusPainted(false);

        HangmanReset.setBackground(hangmanGeneralUtil.regularRed);

        HangmanReset.setFont(hangmanGeneralUtil.weatherFontSmall);

        HangmanReset.addActionListener(e -> setup());

        HangmanReset.setBorder(new LineBorder(hangmanGeneralUtil.navy,5,false));
        HangmanA.setBorder(new LineBorder(hangmanGeneralUtil.navy,5,false));
        HangmanB.setBorder(new LineBorder(hangmanGeneralUtil.navy,5,false));
        HangmanC.setBorder(new LineBorder(hangmanGeneralUtil.navy,5,false));
        HangmanD.setBorder(new LineBorder(hangmanGeneralUtil.navy,5,false));
        HangmanE.setBorder(new LineBorder(hangmanGeneralUtil.navy,5,false));
        HangmanF.setBorder(new LineBorder(hangmanGeneralUtil.navy,5,false));
        HangmanG.setBorder(new LineBorder(hangmanGeneralUtil.navy,5,false));
        HangmanH.setBorder(new LineBorder(hangmanGeneralUtil.navy,5,false));
        HangmanI.setBorder(new LineBorder(hangmanGeneralUtil.navy,5,false));
        HangmanJ.setBorder(new LineBorder(hangmanGeneralUtil.navy,5,false));
        HangmanK.setBorder(new LineBorder(hangmanGeneralUtil.navy,5,false));
        HangmanL.setBorder(new LineBorder(hangmanGeneralUtil.navy,5,false));
        HangmanM.setBorder(new LineBorder(hangmanGeneralUtil.navy,5,false));
        HangmanN.setBorder(new LineBorder(hangmanGeneralUtil.navy,5,false));
        HangmanO.setBorder(new LineBorder(hangmanGeneralUtil.navy,5,false));
        HangmanP.setBorder(new LineBorder(hangmanGeneralUtil.navy,5,false));
        HangmanQ.setBorder(new LineBorder(hangmanGeneralUtil.navy,5,false));
        HangmanR.setBorder(new LineBorder(hangmanGeneralUtil.navy,5,false));
        HangmanS.setBorder(new LineBorder(hangmanGeneralUtil.navy,5,false));
        HangmanT.setBorder(new LineBorder(hangmanGeneralUtil.navy,5,false));
        HangmanU.setBorder(new LineBorder(hangmanGeneralUtil.navy,5,false));
        HangmanV.setBorder(new LineBorder(hangmanGeneralUtil.navy,5,false));
        HangmanW.setBorder(new LineBorder(hangmanGeneralUtil.navy,5,false));
        HangmanX.setBorder(new LineBorder(hangmanGeneralUtil.navy,5,false));
        HangmanY.setBorder(new LineBorder(hangmanGeneralUtil.navy,5,false));
        HangmanZ.setBorder(new LineBorder(hangmanGeneralUtil.navy,5,false));

        HangmanReset.setColors(hangmanGeneralUtil.regularRed);
        HangmanA.setColors(hangmanGeneralUtil.vanila);
        HangmanB.setColors(hangmanGeneralUtil.vanila);
        HangmanC.setColors(hangmanGeneralUtil.vanila);
        HangmanD.setColors(hangmanGeneralUtil.vanila);
        HangmanE.setColors(hangmanGeneralUtil.vanila);
        HangmanF.setColors(hangmanGeneralUtil.vanila);
        HangmanG.setColors(hangmanGeneralUtil.vanila);
        HangmanH.setColors(hangmanGeneralUtil.vanila);
        HangmanI.setColors(hangmanGeneralUtil.vanila);
        HangmanJ.setColors(hangmanGeneralUtil.vanila);
        HangmanK.setColors(hangmanGeneralUtil.vanila);
        HangmanL.setColors(hangmanGeneralUtil.vanila);
        HangmanM.setColors(hangmanGeneralUtil.vanila);
        HangmanN.setColors(hangmanGeneralUtil.vanila);
        HangmanO.setColors(hangmanGeneralUtil.vanila);
        HangmanP.setColors(hangmanGeneralUtil.vanila);
        HangmanQ.setColors(hangmanGeneralUtil.vanila);
        HangmanR.setColors(hangmanGeneralUtil.vanila);
        HangmanS.setColors(hangmanGeneralUtil.vanila);
        HangmanT.setColors(hangmanGeneralUtil.vanila);
        HangmanU.setColors(hangmanGeneralUtil.vanila);
        HangmanV.setColors(hangmanGeneralUtil.vanila);
        HangmanW.setColors(hangmanGeneralUtil.vanila);
        HangmanX.setColors(hangmanGeneralUtil.vanila);
        HangmanY.setColors(hangmanGeneralUtil.vanila);
        HangmanZ.setColors(hangmanGeneralUtil.vanila);
        
        ResetPanel.add(HangmanReset, BorderLayout.CENTER);

        ResetPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        parentPanel.add(ResetPanel);

        parentPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        HangmanFrame.add(parentPanel);

        HangmanFrame.pack();

        HangmanFrame.setLocationRelativeTo(null);

        HangmanFrame.setVisible(true);

        HangmanFrame.setAlwaysOnTop(true);

        HangmanFrame.setAlwaysOnTop(false);

        HangmanFrame.requestFocus();

        setup();
    }

    private void setup()
    {
        HangmanLabel.setFont(new Font("Dialog Font",Font.BOLD,30));

        HangmanReset.setText("Reset");

        String [] words = {"adult","advice","arrangement","attempt","August","Autumn","border","breeze","brick","calm","canal","Casey","cast","chose","claws","coach"
                ,"constantly","contrast","cookies","customs","damage","Danny","deeply","depth","discussion","doll","donkey","Egypt","Ellen","essential","exchange","exist"
                ,"explanation","facing","film","finest","fireplace","floating","folks","fort","garage","grabbed","grandmother","habit","happily","Harry","heading","hunter"
                ,"Illinois","image","independent","instant","January","kids","label","Lee","lungs","manufacturing","Martin","mathematics","melted","memory","mill","mission"
                ,"monkey","Mount","mysterious","neighborhood","Norway","nuts","occasionally","official","ourselves","palace","Pennsylvania","Philadelphia","plates","poetry"
                ,"policeman","positive","possibly","practical","pride","promised","recall","relationship","remarkable","require","rhyme","rocky","rubbed","rush","sale"
                ,"satellites","satisfied","scared","selection","shake","shaking","shallow","shout","silly","simplest","slight","slip","slope","soap","solar","species"
                ,"spin","stiff","swung","tales","thumb","tobacco","toy","trap","treated","tune","University","vapor","vessels","wealth","wolf","zoo"};

        HangmanWord = words[hangmanGeneralUtil.randInt(0, words.length - 1)].toLowerCase();

        HangmanLabel.setText(hangmanGeneralUtil.fillString(HangmanWord.length(), " _ "));

        HangmanImageLabel.setIcon(new ImageIcon("src/com/cyder/io/pictures/hangman.png"));

        HangmanWrongGuesses = 0;

        HangmanA.setEnabled(true);

        HangmanA.setForeground(Color.black);

        HangmanB.setEnabled(true);

        HangmanB.setForeground(Color.black);

        HangmanC.setEnabled(true);

        HangmanC.setForeground(Color.black);

        HangmanD.setEnabled(true);

        HangmanD.setForeground(Color.black);

        HangmanE.setEnabled(true);

        HangmanE.setForeground(Color.black);

        HangmanF.setEnabled(true);

        HangmanF.setForeground(Color.black);

        HangmanG.setEnabled(true);

        HangmanG.setForeground(Color.black);

        HangmanH.setEnabled(true);

        HangmanH.setForeground(Color.black);

        HangmanI.setEnabled(true);

        HangmanI.setForeground(Color.black);

        HangmanJ.setEnabled(true);

        HangmanJ.setForeground(Color.black);

        HangmanK.setEnabled(true);

        HangmanK.setForeground(Color.black);

        HangmanL.setEnabled(true);

        HangmanL.setForeground(Color.black);

        HangmanM.setEnabled(true);

        HangmanM.setForeground(Color.black);

        HangmanN.setEnabled(true);

        HangmanN.setForeground(Color.black);

        HangmanO.setEnabled(true);

        HangmanO.setForeground(Color.black);

        HangmanP.setEnabled(true);

        HangmanP.setForeground(Color.black);

        HangmanQ.setEnabled(true);

        HangmanQ.setForeground(Color.black);

        HangmanR.setEnabled(true);

        HangmanR.setForeground(Color.black);

        HangmanS.setEnabled(true);

        HangmanS.setForeground(Color.black);

        HangmanT.setEnabled(true);

        HangmanT.setForeground(Color.black);

        HangmanU.setEnabled(true);

        HangmanU.setForeground(Color.black);

        HangmanV.setEnabled(true);

        HangmanV.setForeground(Color.black);

        HangmanW.setEnabled(true);

        HangmanW.setForeground(Color.black);

        HangmanX.setEnabled(true);

        HangmanX.setForeground(Color.black);

        HangmanY.setEnabled(true);

        HangmanY.setForeground(Color.black);

        HangmanZ.setEnabled(true);

        HangmanZ.setForeground(Color.black);
    }

    private void LetterChosen(String letter) {
        if (HangmanWord.toLowerCase().contains(letter.toLowerCase())) {
            char[] CurrentWordState = HangmanLabel.getText().replace(" ", "").toLowerCase().toCharArray();
            char[] CurrentWord = HangmanWord.toLowerCase().toCharArray();

            for (int i = 0 ; i < CurrentWordState.length; i++) {
                if (CurrentWord[i] == letter.charAt(0)) {
                    CurrentWordState[i] = CurrentWord[i];
                }
            }

            String NewHangmanWord = new String(CurrentWordState);
            StringBuilder result = new StringBuilder();

            for (int i = 0; i < NewHangmanWord.length(); i++) {
                if (i > 0) {
                    result.append(" ");
                }
                result.append(NewHangmanWord.charAt(i));
            }

            HangmanLabel.setText(result.toString());

            if (!HangmanLabel.getText().contains("_")) {
                HangmanLabel.setFont(new Font("Dialog Font",Font.BOLD,20));
                HangmanLabel.setText("Good job! You guessed the word \"" + HangmanWord + ".\" Would you like to play again?");

                HangmanA.setEnabled(false);
                HangmanB.setEnabled(false);
                HangmanC.setEnabled(false);
                HangmanD.setEnabled(false);
                HangmanE.setEnabled(false);
                HangmanF.setEnabled(false);
                HangmanG.setEnabled(false);
                HangmanH.setEnabled(false);
                HangmanI.setEnabled(false);
                HangmanJ.setEnabled(false);
                HangmanK.setEnabled(false);
                HangmanL.setEnabled(false);
                HangmanM.setEnabled(false);
                HangmanN.setEnabled(false);
                HangmanO.setEnabled(false);
                HangmanP.setEnabled(false);
                HangmanQ.setEnabled(false);
                HangmanR.setEnabled(false);
                HangmanS.setEnabled(false);
                HangmanT.setEnabled(false);
                HangmanU.setEnabled(false);
                HangmanV.setEnabled(false);
                HangmanW.setEnabled(false);
                HangmanX.setEnabled(false);
                HangmanY.setEnabled(false);
                HangmanZ.setEnabled(false);

                HangmanReset.setText("Play Again");
            }
        }

        else {
            if (HangmanWrongGuesses == 7) {
                HangmanImageLabel.setIcon(new ImageIcon("src/com/cyder/io/pictures/hangman8.png"));
                HangmanLabel.setFont(new Font("Dialog Font",Font.BOLD,20));
                HangmanLabel.setText("Game over! You were unable to guess \"" + HangmanWord + ".\" Would you like to play again?");

                HangmanA.setEnabled(false);
                HangmanB.setEnabled(false);
                HangmanC.setEnabled(false);
                HangmanD.setEnabled(false);
                HangmanE.setEnabled(false);
                HangmanF.setEnabled(false);
                HangmanG.setEnabled(false);
                HangmanH.setEnabled(false);
                HangmanI.setEnabled(false);
                HangmanJ.setEnabled(false);
                HangmanK.setEnabled(false);
                HangmanL.setEnabled(false);
                HangmanM.setEnabled(false);
                HangmanN.setEnabled(false);
                HangmanO.setEnabled(false);
                HangmanP.setEnabled(false);
                HangmanQ.setEnabled(false);
                HangmanR.setEnabled(false);
                HangmanS.setEnabled(false);
                HangmanT.setEnabled(false);
                HangmanU.setEnabled(false);
                HangmanV.setEnabled(false);
                HangmanW.setEnabled(false);
                HangmanX.setEnabled(false);
                HangmanY.setEnabled(false);
                HangmanZ.setEnabled(false);

                HangmanReset.setText("Play Again");
            }

            else {
                HangmanWrongGuesses++;

                HangmanImageLabel.setIcon(new ImageIcon("src/com/cyder/io/pictures/hangman" + HangmanWrongGuesses + ".png"));
            }
        }
    }
}
