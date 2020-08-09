package com.cyder.utilities;

import com.cyder.ui.DragLabel;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class MinecraftWidget {
    private JFrame minecraftFrame;
    private JLabel realmsLabel;
    private JLabel chestLabel;
    private JLabel hamLabel;
    private JLabel blockLabel;

    private Util mcUtil = new Util();

    public MinecraftWidget() {
        if (minecraftFrame != null)
            mcUtil.closeAnimation(minecraftFrame);

        minecraftFrame = new JFrame();

        minecraftFrame.setTitle("Minecraft Widget");

        minecraftFrame.setSize(1263, 160);

        minecraftFrame.setUndecorated(true);

        minecraftFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        minecraftFrame.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {

            }

            @Override
            public void windowClosing(WindowEvent e) {

            }

            @Override
            public void windowClosed(WindowEvent e) {

            }

            @Override
            public void windowIconified(WindowEvent e) {

            }

            @Override
            public void windowDeiconified(WindowEvent e) {

            }

            @Override
            public void windowActivated(WindowEvent e) {

            }

            @Override
            public void windowDeactivated(WindowEvent e) {

            }
        });

        JLabel minecraftLabel = new JLabel(new ImageIcon("src\\com\\cyder\\io\\pictures\\Minecraft.png"));

        minecraftFrame.setContentPane(minecraftLabel);

        DragLabel minecraftDragLabel = new DragLabel(1263,27,minecraftFrame);

        minecraftDragLabel.setBounds(0, 0, 1263, 27);

        minecraftLabel.add(minecraftDragLabel);

        blockLabel = new JLabel(new ImageIcon("src\\com\\cyder\\io\\pictures\\Block.png"));

        blockLabel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {
                mcUtil.internetConnect("https://my.minecraft.net/en-us/store/minecraft/");
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                ImageIcon BlockIcon = new ImageIcon("src\\com\\cyder\\io\\pictures\\BlockEnter.gif");
                BlockIcon.getImage().flush();
                blockLabel.setIcon(BlockIcon);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ImageIcon BlockIcon = new ImageIcon("src\\com\\cyder\\io\\pictures\\BlockExit.gif");
                BlockIcon.getImage().flush();
                blockLabel.setIcon(BlockIcon);
            }
        });

        blockLabel.setBounds(83, 46, 50, 45);

        minecraftLabel.add(blockLabel);

        realmsLabel = new JLabel(new ImageIcon("src\\com\\cyder\\io\\pictures\\Realms.png"));

        realmsLabel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) { mcUtil.internetConnect("https://minecraft.net/en-us/realms/?ref=m");
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                ImageIcon RealmsIcon = new ImageIcon("src\\com\\cyder\\io\\pictures\\RealmsEnter.gif");
                RealmsIcon.getImage().flush();
                realmsLabel.setIcon(RealmsIcon);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ImageIcon RealmsIcon = new ImageIcon("src\\com\\cyder\\io\\pictures\\RealmsExit.gif");
                RealmsIcon.getImage().flush();
                realmsLabel.setIcon(RealmsIcon);
            }
        });

        realmsLabel.setBounds(196, 51, 70, 45);

        minecraftLabel.add(realmsLabel);

        chestLabel = new JLabel(new ImageIcon("src\\com\\cyder\\io\\pictures\\Chest.png"));

        chestLabel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {
                mcUtil.internetConnect("https://minecraft.net/en-us/store/?ref=m");
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                ImageIcon ChestIcon = new ImageIcon("src\\com\\cyder\\io\\pictures\\ChestEnter.gif");
                ChestIcon.getImage().flush();
                chestLabel.setIcon(ChestIcon);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ImageIcon ChestIcon = new ImageIcon("src\\com\\cyder\\io\\pictures\\ChestExit.gif");
                ChestIcon.getImage().flush();
                chestLabel.setIcon(ChestIcon);
            }
        });

        chestLabel.setBounds(1009, 44, 60, 50);

        minecraftLabel.add(chestLabel);

        hamLabel = new JLabel(new ImageIcon("src\\com\\cyder\\io\\pictures\\Hamburger.png"));

        hamLabel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {
                mcUtil.internetConnect("https://minecraft.net/en-us/?ref=m");
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                ImageIcon HamIcon = new ImageIcon("src\\com\\cyder\\io\\pictures\\HamburgerEnter.gif");
                HamIcon.getImage().flush();
                hamLabel.setIcon(HamIcon);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ImageIcon HamIcon = new ImageIcon("src\\com\\cyder\\io\\pictures\\HamburgerExit.gif");
                HamIcon.getImage().flush();
                hamLabel.setIcon(HamIcon);
            }
        });

        hamLabel.setBounds(1135, 52, 42, 40);

        minecraftLabel.add(hamLabel);

        minecraftFrame.setVisible(true);

        mcUtil.getScreenSize();

        minecraftFrame.setLocation((int) mcUtil.getScreenSize().getWidth() / 2 - (1263 / 2), (int) mcUtil.getScreenSize().getHeight() - 240);

        minecraftFrame.setAlwaysOnTop(true);

        minecraftFrame.setResizable(false);

        minecraftFrame.setIconImage(new ImageIcon("src\\com\\cyder\\io\\pictures\\Block.png").getImage());
    }
}