package cyder.widgets;

import com.google.common.collect.ImmutableList;
import cyder.annotations.CyderAuthor;
import cyder.annotations.Vanilla;
import cyder.annotations.Widget;
import cyder.constants.CyderStrings;
import cyder.constants.CyderUrls;
import cyder.exceptions.IllegalMethodException;
import cyder.ui.CyderFrame;
import cyder.user.MappedExecutable;
import cyder.utils.*;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

@Vanilla
@CyderAuthor
public final class MinecraftWidget {
    /**
     * The minecraft frame.
     */
    private static CyderFrame minecraftFrame;

    /**
     * The label to use for the realms icon.
     */
    private static JLabel realmsLabel;

    /**
     * The label to use for the chest.
     */
    private static JLabel chestLabel;

    /**
     * The label to use for the hamburger menu.
     */
    private static JLabel hamLabel;

    /**
     * The label to use for the minecraft block.
     */
    private static JLabel blockLabel;

    /**
     * Suppress default constructor.
     */
    private MinecraftWidget() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * The block image icon.
     */
    private static final ImageIcon BLOCK = new ImageIcon("static/pictures/minecraft/Block.png");

    /**
     * The block enter animation.
     */
    private static final ImageIcon BLOCK_ENTER = new ImageIcon("static/pictures/minecraft/BlockEnter.gif");

    /**
     * The block exit animation.
     */
    private static final ImageIcon BLOCK_EXIT = new ImageIcon("static/pictures/minecraft/BlockExit.gif");

    /**
     * The realms icon.
     */
    private static final ImageIcon REALMS = new ImageIcon("static/pictures/minecraft/Realms.png");

    /**
     * The realms enter animation.
     */
    private static final ImageIcon REALMS_ENTER = new ImageIcon("static/pictures/minecraft/RealmsEnter.gif");

    /**
     * The realms exit animation.
     */
    private static final ImageIcon REALMS_EXIT = new ImageIcon("static/pictures/minecraft/RealmsExit.gif");

    /**
     * The chest icon.
     */
    private static final ImageIcon CHEST = new ImageIcon("static/pictures/minecraft/Chest.png");

    /**
     * The chest enter animation.
     */
    private static final ImageIcon CHEST_ENTER = new ImageIcon("static/pictures/minecraft/ChestEnter.gif");

    /**
     * The chest exit animation.
     */
    private static final ImageIcon CHEST_EXIT = new ImageIcon("static/pictures/minecraft/ChestExit.gif");

    /**
     * The hamburger icon.
     */
    private static final ImageIcon HAMBURGER = new ImageIcon("static/pictures/minecraft/Hamburger.png");
    /**
     * The hamburger enter animation.
     */
    private static final ImageIcon HAMBURGER_ENTER
            = new ImageIcon("static/pictures/minecraft/HamburgerEnter.gif");

    /**
     * The hamburger exit animation.
     */
    private static final ImageIcon HAMBURGER_EXIT
            = new ImageIcon("static/pictures/minecraft/HamburgerExit.gif");

    @Widget(triggers = "minecraft", description = "A minecraft widget that copies from the Mojang home page")
    public static void showGui() {
        if (minecraftFrame != null)
            minecraftFrame.dispose();

        minecraftFrame = new CyderFrame(1263, 160,
                new ImageIcon("static/pictures/minecraft/Minecraft.png"));
        minecraftFrame.setTitlePosition(CyderFrame.TitlePosition.CENTER);
        minecraftFrame.setTitle("Minecraft Widget");

        blockLabel = new JLabel(BLOCK);
        blockLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                NetworkUtil.openUrl(CyderUrls.MINECRAFT_BLOCK);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                BLOCK_ENTER.getImage().flush();
                blockLabel.setIcon(BLOCK_ENTER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                BLOCK_EXIT.getImage().flush();
                blockLabel.setIcon(BLOCK_EXIT);
            }
        });

        blockLabel.setBounds(83, 46, 50, 45);
        minecraftFrame.getContentPane().add(blockLabel);

        realmsLabel = new JLabel(REALMS);
        realmsLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                NetworkUtil.openUrl(CyderUrls.MINECRAFT_REALMS);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                REALMS_ENTER.getImage().flush();
                realmsLabel.setIcon(REALMS_ENTER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                REALMS_EXIT.getImage().flush();
                realmsLabel.setIcon(REALMS_EXIT);
            }
        });
        realmsLabel.setBounds(196, 51, 70, 45);
        minecraftFrame.getContentPane().add(realmsLabel);

        chestLabel = new JLabel(CHEST);
        chestLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                NetworkUtil.openUrl(CyderUrls.MINECRAFT_CHEST);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                CHEST_ENTER.getImage().flush();
                chestLabel.setIcon(CHEST_ENTER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                CHEST_EXIT.getImage().flush();
                chestLabel.setIcon(CHEST_EXIT);
            }
        });
        chestLabel.setBounds(1009, 44, 60, 50);
        minecraftFrame.getContentPane().add(chestLabel);

        hamLabel = new JLabel(HAMBURGER);
        hamLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                NetworkUtil.openUrl(CyderUrls.MINECRAFT_HAMBURGER);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                HAMBURGER_ENTER.getImage().flush();
                hamLabel.setIcon(HAMBURGER_ENTER);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                HAMBURGER_EXIT.getImage().flush();
                hamLabel.setIcon(HAMBURGER_EXIT);
            }
        });
        hamLabel.setBounds(1135, 52, 42, 40);
        minecraftFrame.getContentPane().add(hamLabel);

        minecraftFrame.finalizeAndShow();
        minecraftFrame.setIconImage(BLOCK.getImage());

        checkMappedExes();
    }

    /**
     * Names of mapped exes which may reference a Minecraft executable.
     */
    private static final ImmutableList<String> MINECRAFT_NAMES = ImmutableList.of(
            "Minecraft",
            "Lunar",
            "Badlion",
            "Optifine",
            "ATLauncher",
            "Technic",
            "Forge");

    /**
     * Checks the current user's mapped executables to determine if any might reference a Minecraft launcher.
     */
    private static void checkMappedExes() {
        for (MappedExecutable exe : UserUtil.getCyderUser().getExecutables()) {
            File refFile = new File(exe.getFilepath());

            if (refFile.exists() && refFile.isFile()) {
                String name = FileUtil.getFilename(refFile);

                if (StringUtil.in(name, true, MINECRAFT_NAMES)) {
                    IOUtil.openFileOutsideProgram(exe.getFilepath());
                    return;
                }
            }
        }
    }
}