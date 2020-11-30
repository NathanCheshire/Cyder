package com.cyder.utilities;

import com.cyder.ui.CyderFrame;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MinecraftWidget {
    private CyderFrame minecraftFrame;
    private JLabel realmsLabel;
    private JLabel chestLabel;
    private JLabel hamLabel;
    private JLabel blockLabel;

    private GeneralUtil mcGeneralUtil = new GeneralUtil();

    public MinecraftWidget() {
        if (minecraftFrame != null)
            mcGeneralUtil.closeAnimation(minecraftFrame);

        minecraftFrame = new CyderFrame(1263,160, new ImageIcon("src/com/cyder/io/pictures/Minecraft.png"));
        minecraftFrame.setTitlePosition(CyderFrame.CENTER_TITLE);
        minecraftFrame.setTitle("Minecraft Widget");

        blockLabel = new JLabel(new ImageIcon("src/com/cyder/io/pictures/Block.png"));
        blockLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                mcGeneralUtil.internetConnect("https://my.minecraft.net/en-us/store/minecraft/");
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                ImageIcon BlockIcon = new ImageIcon("src/com/cyder/io/pictures/BlockEnter.gif");
                BlockIcon.getImage().flush();
                blockLabel.setIcon(BlockIcon);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ImageIcon BlockIcon = new ImageIcon("src/com/cyder/io/pictures/BlockExit.gif");
                BlockIcon.getImage().flush();
                blockLabel.setIcon(BlockIcon);
            }
        });

        blockLabel.setBounds(83, 46, 50, 45);
        minecraftFrame.getContentPane().add(blockLabel);

        realmsLabel = new JLabel(new ImageIcon("src/com/cyder/io/pictures/Realms.png"));

        realmsLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) { mcGeneralUtil.internetConnect("https://minecraft.net/en-us/realms/?ref=m");
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                ImageIcon RealmsIcon = new ImageIcon("src/com/cyder/io/pictures/RealmsEnter.gif");
                RealmsIcon.getImage().flush();
                realmsLabel.setIcon(RealmsIcon);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ImageIcon RealmsIcon = new ImageIcon("src/com/cyder/io/pictures/RealmsExit.gif");
                RealmsIcon.getImage().flush();
                realmsLabel.setIcon(RealmsIcon);
            }
        });

        realmsLabel.setBounds(196, 51, 70, 45);

        minecraftFrame.getContentPane().add(realmsLabel);

        chestLabel = new JLabel(new ImageIcon("src/com/cyder/io/pictures/Chest.png"));

        chestLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                mcGeneralUtil.internetConnect("https://minecraft.net/en-us/store/?ref=m");
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                ImageIcon ChestIcon = new ImageIcon("src/com/cyder/io/pictures/ChestEnter.gif");
                ChestIcon.getImage().flush();
                chestLabel.setIcon(ChestIcon);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ImageIcon ChestIcon = new ImageIcon("src/com/cyder/io/pictures/ChestExit.gif");
                ChestIcon.getImage().flush();
                chestLabel.setIcon(ChestIcon);
            }
        });

        chestLabel.setBounds(1009, 44, 60, 50);

        minecraftFrame.getContentPane().add(chestLabel);

        hamLabel = new JLabel(new ImageIcon("src/com/cyder/io/pictures/Hamburger.png"));

        hamLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                mcGeneralUtil.internetConnect("https://minecraft.net/en-us/?ref=m");
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                ImageIcon HamIcon = new ImageIcon("src/com/cyder/io/pictures/HamburgerEnter.gif");
                HamIcon.getImage().flush();
                hamLabel.setIcon(HamIcon);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ImageIcon HamIcon = new ImageIcon("src/com/cyder/io/pictures/HamburgerExit.gif");
                HamIcon.getImage().flush();
                hamLabel.setIcon(HamIcon);
            }
        });

        hamLabel.setBounds(1135, 52, 42, 40);

        minecraftFrame.getContentPane().add(hamLabel);

        minecraftFrame.setVisible(true);

        mcGeneralUtil.getScreenSize();

        minecraftFrame.setLocation((int) mcGeneralUtil.getScreenSize().getWidth() / 2 - (1263 / 2), (int) mcGeneralUtil.getScreenSize().getHeight() - 240);

        minecraftFrame.setIconImage(new ImageIcon("src/com/cyder/io/pictures/Block.png").getImage());
    }
}