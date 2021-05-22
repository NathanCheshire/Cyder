package cyder.widgets;

import cyder.enums.TitlePosition;
import cyder.ui.CyderFrame;
import cyder.utilities.NetworkUtil;
import cyder.utilities.SystemUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Minecraft {
    private CyderFrame minecraftFrame;
    private JLabel realmsLabel;
    private JLabel chestLabel;
    private JLabel hamLabel;
    private JLabel blockLabel;

    public Minecraft() {
        if (minecraftFrame != null)
            minecraftFrame.closeAnimation();

        minecraftFrame = new CyderFrame(1263,160, new ImageIcon("sys/pictures/Minecraft.png"));
        minecraftFrame.setTitlePosition(TitlePosition.CENTER);
        minecraftFrame.setTitle("Minecraft Widget");

        blockLabel = new JLabel(new ImageIcon("sys/pictures/Block.png"));
        blockLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                NetworkUtil.internetConnect("https://my.minecraft.net/en-us/store/minecraft/");
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                ImageIcon BlockIcon = new ImageIcon("sys/pictures/BlockEnter.gif");
                BlockIcon.getImage().flush();
                blockLabel.setIcon(BlockIcon);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ImageIcon BlockIcon = new ImageIcon("sys/pictures/BlockExit.gif");
                BlockIcon.getImage().flush();
                blockLabel.setIcon(BlockIcon);
            }
        });

        blockLabel.setBounds(83, 46, 50, 45);
        minecraftFrame.getContentPane().add(blockLabel);

        realmsLabel = new JLabel(new ImageIcon("sys/pictures/Realms.png"));

        realmsLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) { NetworkUtil.internetConnect("https://minecraft.net/en-us/realms/?ref=m");
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                ImageIcon RealmsIcon = new ImageIcon("sys/pictures/RealmsEnter.gif");
                RealmsIcon.getImage().flush();
                realmsLabel.setIcon(RealmsIcon);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ImageIcon RealmsIcon = new ImageIcon("sys/pictures/RealmsExit.gif");
                RealmsIcon.getImage().flush();
                realmsLabel.setIcon(RealmsIcon);
            }
        });

        realmsLabel.setBounds(196, 51, 70, 45);

        minecraftFrame.getContentPane().add(realmsLabel);

        chestLabel = new JLabel(new ImageIcon("sys/pictures/Chest.png"));

        chestLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                NetworkUtil.internetConnect("https://minecraft.net/en-us/store/?ref=m");
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                ImageIcon ChestIcon = new ImageIcon("sys/pictures/ChestEnter.gif");
                ChestIcon.getImage().flush();
                chestLabel.setIcon(ChestIcon);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ImageIcon ChestIcon = new ImageIcon("sys/pictures/ChestExit.gif");
                ChestIcon.getImage().flush();
                chestLabel.setIcon(ChestIcon);
            }
        });

        chestLabel.setBounds(1009, 44, 60, 50);

        minecraftFrame.getContentPane().add(chestLabel);

        hamLabel = new JLabel(new ImageIcon("sys/pictures/Hamburger.png"));

        hamLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                NetworkUtil.internetConnect("https://minecraft.net/en-us/?ref=m");
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                ImageIcon HamIcon = new ImageIcon("sys/pictures/HamburgerEnter.gif");
                HamIcon.getImage().flush();
                hamLabel.setIcon(HamIcon);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ImageIcon HamIcon = new ImageIcon("sys/pictures/HamburgerExit.gif");
                HamIcon.getImage().flush();
                hamLabel.setIcon(HamIcon);
            }
        });

        hamLabel.setBounds(1135, 52, 42, 40);

        minecraftFrame.getContentPane().add(hamLabel);

        minecraftFrame.setVisible(true);

        Dimension size = SystemUtil.getScreenSize();

        minecraftFrame.setLocation((int) (size.getWidth() / 2 - (1263 / 2)), (int) size.getHeight() - 240);

        minecraftFrame.setIconImage(new ImageIcon("sys/pictures/Block.png").getImage());
    }
}