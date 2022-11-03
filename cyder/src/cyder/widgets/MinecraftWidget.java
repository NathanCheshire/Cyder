package cyder.widgets;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.annotations.CyderAuthor;
import cyder.annotations.ForReadability;
import cyder.annotations.Vanilla;
import cyder.annotations.Widget;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.files.FileUtil;
import cyder.network.NetworkUtil;
import cyder.ui.frame.CyderFrame;
import cyder.user.MappedExecutable;
import cyder.user.UserUtil;
import cyder.utils.IoUtil;
import cyder.utils.StringUtil;
import cyder.utils.UiUtil;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;

/**
 * A widget emulating the Minecraft front page.
 */
@Vanilla
@CyderAuthor
public final class MinecraftWidget {
    /**
     * The minecraft frame.
     */
    private static CyderFrame minecraftFrame;

    /**
     * The minecraft.net link that redirects to the hamburger icon's result.
     */
    public static final String MINECRAFT_HAMBURGER = "https://minecraft.net/en-us/?ref=m";

    /**
     * The minecraft.net link that redirects to the store icon's result.
     */
    public static final String MINECRAFT_CHEST = "https://minecraft.net/en-us/store/?ref=m";

    /**
     * The minecraft.net link that redirects to the realm icon's result.
     */
    public static final String MINECRAFT_REALMS = "https://minecraft.net/en-us/realms/?ref=m";

    /**
     * The minecraft.net link that redirects to the block icon's result.
     */
    public static final String MINECRAFT_BLOCK = "https://my.minecraft.net/en-us/store/minecraft/";

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

    /**
     * The title of the widget frame.
     */
    private static final String FRAME_TITLE = "Minecraft Widget";

    /**
     * The width of the widget frame.
     */
    private static final int FRAME_WIDTH = 1263;

    /**
     * The height of the image frame.
     */
    private static final int FRAME_HEIGHT = 160;

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
            "Forge"
    );

    /**
     * The background of the frame.
     */
    private static final ImageIcon background = new ImageIcon("static/pictures/minecraft/Minecraft.png");

    @Widget(triggers = "minecraft", description = "A minecraft widget that copies from the Mojang home page")
    public static void showGui() {
        UiUtil.closeIfOpen(minecraftFrame);
        minecraftFrame = new CyderFrame(FRAME_WIDTH, FRAME_HEIGHT, background);
        minecraftFrame.setTitlePosition(CyderFrame.TitlePosition.CENTER);
        minecraftFrame.setTitle(FRAME_TITLE);

        JLabel blockLabel = new JLabel(BLOCK);
        blockLabel.addMouseListener(generateMouseListener(blockLabel, MINECRAFT_BLOCK,
                BLOCK_ENTER, BLOCK_EXIT));
        blockLabel.setBounds(83, 46, 50, 45);
        minecraftFrame.getContentPane().add(blockLabel);

        JLabel realmsLabel = new JLabel(REALMS);
        realmsLabel.addMouseListener(generateMouseListener(realmsLabel, MINECRAFT_REALMS,
                REALMS_ENTER, REALMS_EXIT));
        realmsLabel.setBounds(196, 51, 70, 45);
        minecraftFrame.getContentPane().add(realmsLabel);

        JLabel chestLabel = new JLabel(CHEST);
        chestLabel.addMouseListener(generateMouseListener(chestLabel, MINECRAFT_CHEST,
                CHEST_ENTER, CHEST_EXIT));
        chestLabel.setBounds(1009, 44, 60, 50);
        minecraftFrame.getContentPane().add(chestLabel);

        JLabel hamLabel = new JLabel(HAMBURGER);
        hamLabel.addMouseListener(generateMouseListener(hamLabel, MINECRAFT_HAMBURGER,
                HAMBURGER_ENTER, HAMBURGER_EXIT));
        hamLabel.setBounds(1135, 52, 42, 40);
        minecraftFrame.getContentPane().add(hamLabel);

        int x = (UiUtil.getDefaultMonitorWidth() - FRAME_WIDTH) / 2;
        int y = UiUtil.getDefaultMonitorHeight() - FRAME_HEIGHT - UiUtil.getWindowsTaskbarHeight();
        minecraftFrame.finalizeAndShow(new Point(x, y));
        minecraftFrame.setIconImage(BLOCK.getImage());

        checkMappedExes();
    }

    /**
     * Generates a mouse adapter for a minecraft gif label.
     *
     * @param label    the label
     * @param url      the url to open on click
     * @param enterGif the enter gif
     * @param exitGif  the exit gif
     * @return the mouse listener
     */
    @ForReadability
    private static MouseListener generateMouseListener(JLabel label,
                                                       String url,
                                                       ImageIcon enterGif,
                                                       ImageIcon exitGif) {
        Preconditions.checkNotNull(label);
        Preconditions.checkNotNull(url);
        Preconditions.checkArgument(!url.isEmpty());
        Preconditions.checkNotNull(enterGif);
        Preconditions.checkNotNull(exitGif);

        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                NetworkUtil.openUrl(url);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                enterGif.getImage().flush();
                label.setIcon(enterGif);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                exitGif.getImage().flush();
                label.setIcon(exitGif);
            }
        };
    }

    /**
     * Checks the current user's mapped executables to determine if any might reference a Minecraft launcher.
     */
    private static void checkMappedExes() {
        for (MappedExecutable exe : UserUtil.getCyderUser().getExecutables()) {
            File refFile = new File(exe.getFilepath());

            if (refFile.exists() && refFile.isFile()) {
                String name = FileUtil.getFilename(refFile);

                if (StringUtil.in(name, true, MINECRAFT_NAMES)) {
                    IoUtil.openFileOutsideProgram(exe.getFilepath());
                    return;
                }
            }
        }
    }
}