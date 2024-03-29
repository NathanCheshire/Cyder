package cyder.widgets;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import cyder.annotations.CyderAuthor;
import cyder.annotations.ForReadability;
import cyder.annotations.Vanilla;
import cyder.annotations.Widget;
import cyder.exceptions.IllegalMethodException;
import cyder.files.FileUtil;
import cyder.network.NetworkUtil;
import cyder.strings.CyderStrings;
import cyder.strings.StringUtil;
import cyder.ui.UiUtil;
import cyder.ui.frame.CyderFrame;
import cyder.ui.frame.enumerations.TitlePosition;
import cyder.user.UserDataManager;
import cyder.user.data.MappedExecutable;
import cyder.utils.StaticUtil;

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
     * The block image icon.
     */
    private static final ImageIcon BLOCK = new ImageIcon(StaticUtil.getStaticPath("Block.png"));

    /**
     * The block enter animation.
     */
    private static final ImageIcon BLOCK_ENTER = new ImageIcon(StaticUtil.getStaticPath("BlockEnter.gif"));

    /**
     * The block exit animation.
     */
    private static final ImageIcon BLOCK_EXIT = new ImageIcon(StaticUtil.getStaticPath("BlockExit.gif"));

    /**
     * The realms icon.
     */
    private static final ImageIcon REALMS = new ImageIcon(StaticUtil.getStaticPath("Realms.png"));

    /**
     * The realms enter animation.
     */
    private static final ImageIcon REALMS_ENTER = new ImageIcon(StaticUtil.getStaticPath("RealmsEnter.gif"));

    /**
     * The realms exit animation.
     */
    private static final ImageIcon REALMS_EXIT = new ImageIcon(StaticUtil.getStaticPath("RealmsExit.gif"));

    /**
     * The chest icon.
     */
    private static final ImageIcon CHEST = new ImageIcon(StaticUtil.getStaticPath("Chest.png"));

    /**
     * The chest enter animation.
     */
    private static final ImageIcon CHEST_ENTER = new ImageIcon(StaticUtil.getStaticPath("ChestEnter.gif"));

    /**
     * The chest exit animation.
     */
    private static final ImageIcon CHEST_EXIT = new ImageIcon(StaticUtil.getStaticPath("ChestExit.gif"));

    /**
     * The hamburger icon.
     */
    private static final ImageIcon HAMBURGER = new ImageIcon(StaticUtil.getStaticPath("Hamburger.png"));
    /**
     * The hamburger enter animation.
     */
    private static final ImageIcon HAMBURGER_ENTER = new ImageIcon(
            StaticUtil.getStaticPath("HamburgerEnter.gif"));

    /**
     * The hamburger exit animation.
     */
    private static final ImageIcon HAMBURGER_EXIT = new ImageIcon(
            StaticUtil.getStaticPath("HamburgerExit.gif"));

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
    private static final ImageIcon background = new ImageIcon(StaticUtil.getStaticPath("Minecraft.png"));

    /**
     * Suppress default constructor.
     */
    private MinecraftWidget() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    @Widget(triggers = "minecraft", description = "A minecraft widget that copies from the Mojang home page")
    public static void showGui() {
        UiUtil.closeIfOpen(minecraftFrame);
        minecraftFrame = new CyderFrame.Builder()
                .setWidth(FRAME_WIDTH)
                .setHeight(FRAME_HEIGHT)
                .setBackgroundIcon(background)
                .build();
        minecraftFrame.setTitlePosition(TitlePosition.CENTER);
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
        int y = UiUtil.getDefaultMonitorHeight() - FRAME_HEIGHT - UiUtil.getWindowsTaskbarHeight() / 2;
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
    private static MouseListener generateMouseListener(JLabel label, String url,
                                                       ImageIcon enterGif, ImageIcon exitGif) {
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
        UserDataManager.INSTANCE.getMappedExecutables().getExecutables()
                .stream().filter(MinecraftWidget::mappedExeReferencesPossibleMinecraftLauncher)
                .findFirst().ifPresent(exe -> FileUtil.openResourceUsingNativeProgram(exe.getFilepath()));
    }

    /**
     * Returns whether the provided mapped executable likely references a Minecraft launcher.
     *
     * @param mappedExecutable the mapped executable
     * @return whether the provided mapped executable likely references a Minecraft launcher
     */
    private static boolean mappedExeReferencesPossibleMinecraftLauncher(MappedExecutable mappedExecutable) {
        Preconditions.checkNotNull(mappedExecutable);

        File refFile = new File(mappedExecutable.getFilepath());
        if (refFile.exists() && refFile.isFile()) {
            return StringUtil.in(FileUtil.getFilename(refFile), true, MINECRAFT_NAMES);
        }

        return false;
    }
}