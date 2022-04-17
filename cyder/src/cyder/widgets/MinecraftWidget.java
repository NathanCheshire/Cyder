package cyder.widgets;


import cyder.annotations.Vanilla;
import cyder.annotations.Widget;
import cyder.constants.CyderStrings;
import cyder.constants.CyderUrls;
import cyder.exceptions.IllegalMethodException;
import cyder.ui.CyderFrame;
import cyder.user.objects.MappedExecutable;
import cyder.utilities.IOUtil;
import cyder.utilities.NetworkUtil;
import cyder.utilities.UserUtil;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

@Vanilla
public class MinecraftWidget {
    private static CyderFrame minecraftFrame;
    private static JLabel realmsLabel;
    private static JLabel chestLabel;
    private static JLabel hamLabel;
    private static JLabel blockLabel;

    private MinecraftWidget() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    @Widget(triggers = "minecraft", description = "A minecraft widget that copies from the Mojang home page")
    public static void showGUI() {
        if (minecraftFrame != null)
            minecraftFrame.dispose();

        minecraftFrame = new CyderFrame(1263,160, new ImageIcon("static/pictures/minecraft/Minecraft.png"));
        minecraftFrame.setTitlePosition(CyderFrame.TitlePosition.CENTER);
        minecraftFrame.setTitle("Minecraft Widget");

        blockLabel = new JLabel(new ImageIcon("static/pictures/minecraft/Block.png"));
        blockLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                NetworkUtil.openUrl(CyderUrls.MINECRAFT_BLOCK);
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
            public void mouseReleased(MouseEvent e) {
                NetworkUtil.openUrl(CyderUrls.MINECRAFT_REALMS);
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
                NetworkUtil.openUrl(CyderUrls.MINECRAFT_CHEST);
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
                NetworkUtil.openUrl(CyderUrls.MINECRAFT_HAMBURGER);
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

        minecraftFrame.finalizeAndShow();
        minecraftFrame.setIconImage(new ImageIcon("static/pictures/minecraft/Block.png").getImage());

        //open minecraft if map exists
        for (MappedExecutable exe : UserUtil.getCyderUser().getExecutables()) {
            if (exe.getName().equalsIgnoreCase("minecraft") ||
                exe.getName().equalsIgnoreCase("lunar") ||
                exe.getName().equalsIgnoreCase("badlion")) {
                IOUtil.openFileOutsideProgram(exe.getFilepath());
                break;
            }
        }
    }
}