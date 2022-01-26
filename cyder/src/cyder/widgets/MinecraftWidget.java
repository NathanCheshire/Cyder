package cyder.widgets;


import cyder.annotations.Widget;
import cyder.constants.CyderStrings;
import cyder.user.User;
import cyder.genesis.CyderCommon;
import cyder.handlers.internal.SessionHandler;
import cyder.ui.CyderFrame;
import cyder.utilities.IOUtil;
import cyder.utilities.NetworkUtil;
import cyder.utilities.UserUtil;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MinecraftWidget implements WidgetBase {
    private static CyderFrame minecraftFrame;
    private static JLabel realmsLabel;
    private static JLabel chestLabel;
    private static JLabel hamLabel;
    private static JLabel blockLabel;

    private MinecraftWidget() {
        throw new IllegalStateException(CyderStrings.attemptedClassInstantiation);
    }

    @Widget(trigger = "minecraft", description = "A minecraft widget that copies from the Mojang home page")
    public static void showGUI() {
        SessionHandler.log(SessionHandler.Tag.WIDGET_OPENED, "MINECRAFT");

        if (minecraftFrame != null)
            minecraftFrame.dispose();

        minecraftFrame = new CyderFrame(1263,160, new ImageIcon("static/pictures/minecraft/Minecraft.png"));
        minecraftFrame.setTitlePosition(CyderFrame.TitlePosition.CENTER);
        minecraftFrame.setTitle("Minecraft Widget");

        blockLabel = new JLabel(new ImageIcon("static/pictures/minecraft/Block.png"));
        blockLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                NetworkUtil.internetConnect("https://my.minecraft.net/en-us/store/minecraft/");
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                ImageIcon BlockIcon = new ImageIcon("static/pictures/minecraft/BlockEnter.gif");
                BlockIcon.getImage().flush();
                blockLabel.setIcon(BlockIcon);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ImageIcon BlockIcon = new ImageIcon("static/pictures/minecraft/BlockExit.gif");
                BlockIcon.getImage().flush();
                blockLabel.setIcon(BlockIcon);
            }
        });

        blockLabel.setBounds(83, 46, 50, 45);
        minecraftFrame.getContentPane().add(blockLabel);

        realmsLabel = new JLabel(new ImageIcon("static/pictures/minecraft/Realms.png"));
        realmsLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) { NetworkUtil.internetConnect("https://minecraft.net/en-us/realms/?ref=m");
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                ImageIcon RealmsIcon = new ImageIcon("static/pictures/minecraft/RealmsEnter.gif");
                RealmsIcon.getImage().flush();
                realmsLabel.setIcon(RealmsIcon);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ImageIcon RealmsIcon = new ImageIcon("static/pictures/minecraft/RealmsExit.gif");
                RealmsIcon.getImage().flush();
                realmsLabel.setIcon(RealmsIcon);
            }
        });
        realmsLabel.setBounds(196, 51, 70, 45);
        minecraftFrame.getContentPane().add(realmsLabel);

        chestLabel = new JLabel(new ImageIcon("static/pictures/minecraft/Chest.png"));
        chestLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                NetworkUtil.internetConnect("https://minecraft.net/en-us/store/?ref=m");
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                ImageIcon ChestIcon = new ImageIcon("static/pictures/minecraft/ChestEnter.gif");
                ChestIcon.getImage().flush();
                chestLabel.setIcon(ChestIcon);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ImageIcon ChestIcon = new ImageIcon("static/pictures/minecraft/ChestExit.gif");
                ChestIcon.getImage().flush();
                chestLabel.setIcon(ChestIcon);
            }
        });
        chestLabel.setBounds(1009, 44, 60, 50);
        minecraftFrame.getContentPane().add(chestLabel);

        hamLabel = new JLabel(new ImageIcon("static/pictures/minecraft/Hamburger.png"));
        hamLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                NetworkUtil.internetConnect("https://minecraft.net/en-us/?ref=m");
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                ImageIcon HamIcon = new ImageIcon("static/pictures/minecraft/HamburgerEnter.gif");
                HamIcon.getImage().flush();
                hamLabel.setIcon(HamIcon);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                ImageIcon HamIcon = new ImageIcon("static/pictures/minecraft/HamburgerExit.gif");
                HamIcon.getImage().flush();
                hamLabel.setIcon(HamIcon);
            }
        });
        hamLabel.setBounds(1135, 52, 42, 40);
        minecraftFrame.getContentPane().add(hamLabel);

        minecraftFrame.setVisible(true);
        minecraftFrame.setLocationRelativeTo(CyderCommon.getDominantFrame());
        minecraftFrame.setIconImage(new ImageIcon("static/pictures/minecraft/Block.png").getImage());

        //open minecraft if map exists
        for (User.MappedExecutable exe : UserUtil.extractUser().getExecutables()) {
            if (exe.getName().equalsIgnoreCase("minecraft") ||
                exe.getName().equalsIgnoreCase("lunar") ||
                exe.getName().equalsIgnoreCase("badlion")) {
                IOUtil.openFileOutsideProgram(exe.getFilepath());
                break;
            }
        }
    }
}