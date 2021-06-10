package cyder.handler;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.ui.*;
import cyder.utilities.AnimationUtil;
import cyder.utilities.SystemUtil;
import cyder.widgets.GenericInform;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedList;

import static cyder.consts.CyderStrings.DEFAULT_BACKGROUND_PATH;

public class PhotoViewer {

    private LinkedList<File> validImages = new LinkedList<>();
    private File startDir;
    private int currentIndex;

    private DragLabel dl;
    private JFrame pictureFrame;
    private CyderFrame renameFrame;

    private JLabel pictureLabel;

    private JLabel nextImage;
    private JLabel lastImage;
    private JLabel rename;

    public PhotoViewer(File startDir) {
        this.startDir = startDir;
    }

    public void start() {
        initFiles();

        File ImageName = validImages.get(currentIndex);

        if (pictureFrame != null)
            AnimationUtil.closeAnimation(pictureFrame);

        BufferedImage Image = null;

        try {
            Image = ImageIO.read(ImageName);
        } catch (Exception ex) {
            ErrorHandler.handle(ex);
        }

        pictureFrame = new JFrame();
        pictureFrame.setUndecorated(true);
        pictureFrame.setTitle(ImageName.getName().replace(".png", ""));
        pictureFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        ImageIcon size = checkImage(ImageName);
        pictureLabel = new JLabel(size);
        pictureFrame.setContentPane(pictureLabel);
        pictureLabel.setBorder(new LineBorder(CyderColors.navy,8,false));

        dl = new DragLabel(size.getIconWidth(), 30, pictureFrame);
        pictureLabel.add(dl);

        rename = new JLabel("Rename");
        rename.setForeground(CyderColors.vanila);
        rename.setFont(CyderFonts.weatherFontSmall);
        rename.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                rename();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                rename.setForeground(CyderColors.regularRed);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                rename.setForeground(CyderColors.vanila);
            }
        });

        rename.setBounds(size.getIconWidth() / 2 - 50, 5, 90, 20);
        dl.add(rename);

        nextImage = new JLabel(new ImageIcon("sys/pictures/icons/nextPicture1.png"));
        nextImage.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                scrollFoward();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                nextImage.setIcon(new ImageIcon("sys/pictures/icons/nextPicture2.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                nextImage.setIcon(new ImageIcon("sys/pictures/icons/nextPicture1.png"));
            }
        });
        nextImage.setBounds(size.getIconWidth() / 2 - 50 + 100, 5, 22, 22);
        dl.add(nextImage);

        lastImage = new JLabel(new ImageIcon("sys/pictures/icons/lastPicture1.png"));
        lastImage.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                scrollBack();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                lastImage.setIcon(new ImageIcon("sys/pictures/icons/lastPicture2.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                lastImage.setIcon(new ImageIcon("sys/pictures/icons/lastPicture1.png"));
            }
        });
        lastImage.setBounds(size.getIconWidth() / 2 - 50 - 40, 5, 22, 22);
        dl.add(lastImage);

        pictureFrame.setSize(size.getIconWidth(),size.getIconHeight());
        pictureFrame.setVisible(true);
        pictureFrame.setLocationRelativeTo(null);
        pictureFrame.setIconImage(SystemUtil.getCyderIcon().getImage());
    }

    private void initFiles() {
        validImages.clear();

        File[] possibles = null;

        if (startDir.isDirectory()) {
            possibles = startDir.listFiles();
        } else {
            possibles = startDir.getParentFile().listFiles();
        }

        for (File f : possibles) {
            if (f.getName().endsWith(".png"))
                validImages.add(f);
        }

        for (int i = 0 ; i < validImages.size() ; i++) {
           if (validImages.get(i).getAbsolutePath().equalsIgnoreCase(startDir.getAbsolutePath())) {
               currentIndex = i;
           }
        }
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int type, int img_width, int img_height) {
        BufferedImage resizedImage = new BufferedImage(img_width, img_height, type);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, img_width, img_height, null);
        g.dispose();

        return resizedImage;
    }

    private void scrollFoward() {
        try {
            if (currentIndex + 1 < validImages.size()) {
                currentIndex += 1;
            }

            else {
                currentIndex = 0;
            }

            ImageIcon size = checkImage(validImages.get(currentIndex));
            pictureLabel = new JLabel(size);
            pictureFrame.setContentPane(pictureLabel);
            pictureLabel.setBorder(new LineBorder(CyderColors.navy,8,false));

            dl = new DragLabel(size.getIconWidth(), 30, pictureFrame);
            pictureLabel.add(dl);

            rename = new JLabel("Rename");
            rename.setForeground(CyderColors.vanila);
            rename.setFont(CyderFonts.weatherFontSmall);
            rename.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    rename();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    rename.setForeground(CyderColors.regularRed);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    rename.setForeground(CyderColors.vanila);
                }
            });

            rename.setBounds(size.getIconWidth() / 2 - 50, 5, 90, 20);
            dl.add(rename);

            nextImage = new JLabel(new ImageIcon("sys/pictures/icons/nextPicture1.png"));
            nextImage.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    scrollFoward();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    nextImage.setIcon(new ImageIcon("sys/pictures/icons/nextPicture2.png"));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    nextImage.setIcon(new ImageIcon("sys/pictures/icons/nextPicture1.png"));
                }
            });
            nextImage.setBounds(size.getIconWidth() / 2 - 50 + 100, 5, 22, 22);
            dl.add(nextImage);

            lastImage = new JLabel(new ImageIcon("sys/pictures/icons/lastPicture1.png"));
            lastImage.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    scrollBack();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    lastImage.setIcon(new ImageIcon("sys/pictures/icons/lastPicture2.png"));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    lastImage.setIcon(new ImageIcon("sys/pictures/icons/lastPicture1.png"));
                }
            });
            lastImage.setBounds(size.getIconWidth() / 2 - 50 - 40, 5, 22, 22);
            dl.add(lastImage);

            pictureLabel.setSize(size.getIconWidth(), size.getIconHeight());
            pictureLabel.setIcon(size);
            pictureLabel.revalidate();

            pictureFrame.setSize(size.getIconWidth(), size.getIconHeight());
            pictureFrame.revalidate();
            pictureFrame.setTitle(validImages.get(currentIndex).getName().replace(".png", ""));
            pictureFrame.setLocationRelativeTo(null);
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }

    private void scrollBack() {
        try {
            if (currentIndex - 1 >= 0) {
                currentIndex--;
            }

            else {
                currentIndex = validImages.size() - 1;
            }

            ImageIcon size = checkImage(validImages.get(currentIndex));
            pictureLabel = new JLabel(size);
            pictureFrame.setContentPane(pictureLabel);
            pictureLabel.setBorder(new LineBorder(CyderColors.navy,8,false));

            dl = new DragLabel(size.getIconWidth(), 30, pictureFrame);
            pictureLabel.add(dl);

            rename = new JLabel("Rename");
            rename.setForeground(CyderColors.vanila);
            rename.setFont(CyderFonts.weatherFontSmall);
            rename.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                   rename();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    rename.setForeground(CyderColors.regularRed);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    rename.setForeground(CyderColors.vanila);
                }
            });

            rename.setBounds(size.getIconWidth() / 2 - 50, 5, 90, 20);
            dl.add(rename);

            nextImage = new JLabel(new ImageIcon("sys/pictures/icons/nextPicture1.png"));
            nextImage.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    scrollFoward();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    nextImage.setIcon(new ImageIcon("sys/pictures/icons/nextPicture2.png"));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    nextImage.setIcon(new ImageIcon("sys/pictures/icons/nextPicture1.png"));
                }
            });
            nextImage.setBounds(size.getIconWidth() / 2 - 50 + 100, 5, 22, 22);
            dl.add(nextImage);

            lastImage = new JLabel(new ImageIcon("sys/pictures/icons/lastPicture1.png"));
            lastImage.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    scrollBack();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    lastImage.setIcon(new ImageIcon("sys/pictures/icons/lastPicture2.png"));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    lastImage.setIcon(new ImageIcon("sys/pictures/icons/lastPicture1.png"));
                }
            });
            lastImage.setBounds(size.getIconWidth() / 2 - 50 - 40, 5, 22, 22);
            dl.add(lastImage);

            pictureLabel.setSize(size.getIconWidth(), size.getIconHeight());
            pictureLabel.setIcon(size);
            pictureLabel.revalidate();

            pictureFrame.setSize(size.getIconWidth(), size.getIconHeight());
            pictureFrame.revalidate();
            pictureFrame.setTitle(validImages.get(currentIndex).getName().replace(".png", ""));
            pictureFrame.setLocationRelativeTo(null);
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }

    }

    //returns a scaled down imageicon if the image file is too big
    private ImageIcon checkImage(File im) {
        try {
            Dimension dim = SystemUtil.getScreenSize();
            double screenX = dim.getWidth();
            double screenY = dim.getHeight();

            double aspectRatio = getAspectRatio(new ImageIcon(ImageIO.read(im)));
            aspectRatio = (aspectRatio == 1.0 ? 2.0 : aspectRatio);
            ImageIcon originalIcon = new ImageIcon(ImageIO.read(im));
            BufferedImage bi = ImageIO.read(im);

            int width = originalIcon.getIconWidth();
            int height = originalIcon.getIconHeight();

            while (width > screenX || height > screenY) {
                width = (int) (width / aspectRatio);
                height = (int) (height / aspectRatio);
            }

            while (width  < 600 || height  < 600) {
                width = (int) (width * aspectRatio);
                height = (int) (height * aspectRatio);
            }

            return new ImageIcon(bi.getScaledInstance(width, height, Image.SCALE_SMOOTH));
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }

        return null;
    }

    private double getAspectRatio(ImageIcon im) {
        return ((double) im.getIconWidth() / (double) im.getIconHeight());
    }

    private void rename() {
        if (renameFrame != null)
            AnimationUtil.closeAnimation(renameFrame);

        File currentRename = new File(validImages.get(currentIndex).getAbsolutePath());
        File currentBackground = ConsoleFrame.getCurrentBackgroundFile().getAbsoluteFile();

        if (currentRename.getAbsolutePath().equals( currentBackground.getAbsolutePath())) {
            GenericInform.inform("Sorry, " + ConsoleFrame.getUsername() + ", but you're not allowed to" +
                    " rename the background you are currently using","");
            return;
        }

        renameFrame = new CyderFrame(400,170,new ImageIcon(DEFAULT_BACKGROUND_PATH));
        renameFrame.setTitle("Rename");

        JTextField renameField = new JTextField(20);
        renameField.setSelectionColor(CyderColors.selectionColor);
        renameField.setFont(CyderFonts.weatherFontSmall);
        renameField.setForeground(CyderColors.navy);
        renameField.setCaretColor(CyderColors.navy);
        renameField.setCaret(new CyderCaret(CyderColors.navy));
        renameField.setBorder(new LineBorder(CyderColors.navy,5,false));
        renameField.setBounds(40,40,320,40);
        renameFrame.getContentPane().add(renameField);

        CyderButton attemptRen = new CyderButton("Rename");
        attemptRen.setBackground(CyderColors.regularRed);
        attemptRen.setColors(CyderColors.regularRed);
        renameField.addActionListener(e1 -> attemptRen.doClick());
        attemptRen.setBorder(new LineBorder(CyderColors.navy,5,false));
        attemptRen.setFont(CyderFonts.weatherFontSmall);
        attemptRen.setForeground(CyderColors.navy);
        attemptRen.addActionListener(e12 -> {
            String name = renameField.getText();

            File oldName = new File(validImages.get(currentIndex).getAbsolutePath());
            File newName = new File(validImages.get(currentIndex).getAbsolutePath().replace(validImages.get(currentIndex).getName().replace(".png",""),name));
            oldName.renameTo(newName);
            GenericInform.inform("Successfully renamed to " + name,"");

            initFiles();

            for (int i = 0 ; i < validImages.size() ; i++) {
                if (validImages.get(i).getName().equals(name)) {
                    currentIndex = i;
                }
            }

            pictureFrame.setTitle(name);

            AnimationUtil.closeAnimation(renameFrame);
        });
        attemptRen.setBounds(40,100,320,40);
        renameFrame.getContentPane().add(attemptRen);

        renameFrame.setVisible(true);
        renameFrame.setLocationRelativeTo(pictureFrame);
    }

    @Override
    public String toString() {
        return "PhotoViewer object, hash=" + this.hashCode();
    }
}
