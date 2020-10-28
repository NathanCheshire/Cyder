package com.cyder.handler;

import com.cyder.ui.CyderButton;
import com.cyder.ui.DragLabel;
import com.cyder.utilities.Util;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.LinkedList;

public class PhotoViewer {

    private LinkedList<File> validImages = new LinkedList<>();
    private File startDir;
    private int currentIndex;

    private Util imageUtil = new Util();

    private JFrame renameFrame;
    private DragLabel dl;
    private JFrame pictureFrame;

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
            imageUtil.closeAnimation(pictureFrame);

        BufferedImage Image = null;

        try {
            Image = ImageIO.read(ImageName);
        } catch (Exception ex) {
            imageUtil.handle(ex);
        }

        pictureFrame = new JFrame();
        pictureFrame.setUndecorated(true);
        pictureFrame.setTitle(ImageName.getName().replace(".png", ""));
        pictureFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        ImageIcon size = checkImage(ImageName);
        pictureLabel = new JLabel(size);
        pictureFrame.setContentPane(pictureLabel);
        pictureLabel.setBorder(new LineBorder(imageUtil.navy,8,false));

        dl = new DragLabel(size.getIconWidth(), 30, pictureFrame);
        pictureLabel.add(dl);

        rename = new JLabel("Rename");
        rename.setForeground(imageUtil.vanila);
        rename.setFont(imageUtil.weatherFontSmall);
        rename.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (renameFrame != null)
                    imageUtil.closeAnimation(renameFrame);

                renameFrame = new JFrame();
                renameFrame.setResizable(false);
                renameFrame.setIconImage(imageUtil.getCyderIcon().getImage());
                renameFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                renameFrame.setTitle("Rename");

                JPanel pan = new JPanel();
                pan.setLayout(new BoxLayout(pan, BoxLayout.Y_AXIS));

                JTextField renameField = new JTextField(20);
                renameField.setFont(imageUtil.weatherFontSmall);
                renameField.setForeground(imageUtil.navy);
                renameField.setBorder(new LineBorder(imageUtil.navy,5,false));

                CyderButton attemptRen = new CyderButton("Rename");
                attemptRen.setBackground(imageUtil.regularRed);
                attemptRen.setColors(imageUtil.regularRed);
                renameField.addActionListener(e1 -> attemptRen.doClick());

                JPanel a = new JPanel();
                a.add(renameField, SwingConstants.CENTER);
                pan.add(a);

                attemptRen.setBorder(new LineBorder(imageUtil.navy,5,false));
                attemptRen.setFont(imageUtil.weatherFontSmall);
                attemptRen.setForeground(imageUtil.navy);
                attemptRen.addActionListener(e12 -> {
                    String name = renameField.getText();

                    File oldName = new File(validImages.get(currentIndex).getAbsolutePath());
                    File newName = new File(validImages.get(currentIndex).getAbsolutePath().replace(validImages.get(currentIndex).getName().replace(".png",""),name));
                    oldName.renameTo(newName);
                    imageUtil.inform("Successfully renamed to " + name,"",400,300);

                    initFiles();

                    for (int i = 0 ; i < validImages.size() ; i++) {
                        if (validImages.get(i).getName().equals(name)) {
                            currentIndex = i;
                        }
                    }

                    pictureFrame.setTitle(name);

                    imageUtil.closeAnimation(renameFrame);

                    System.out.println("index: " + currentIndex + "\nFiles:\n");
                    for (File f : validImages) {
                        System.out.println(f);
                    }
                });

                JPanel b = new JPanel();
                b.add(attemptRen, SwingConstants.CENTER);
                pan.add(b);
                pan.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

                renameFrame.add(pan);
                renameFrame.pack();
                renameFrame.setVisible(true);
                renameFrame.setLocationRelativeTo(null);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                rename.setForeground(imageUtil.regularRed);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                rename.setForeground(imageUtil.vanila);
            }
        });

        rename.setBounds(size.getIconWidth() / 2 - 50, 5, 90, 20);
        dl.add(rename);

        nextImage = new JLabel(new ImageIcon("src\\com\\cyder\\io\\pictures\\nextPicture1.png"));
        nextImage.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                scrollFoward();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                nextImage.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\nextPicture2.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                nextImage.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\nextPicture1.png"));
            }
        });
        nextImage.setBounds(size.getIconWidth() / 2 - 50 + 100, 5, 22, 22);
        dl.add(nextImage);

        lastImage = new JLabel(new ImageIcon("src\\com\\cyder\\io\\pictures\\lastPicture1.png"));
        lastImage.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                scrollBack();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                lastImage.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\lastPicture2.png"));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                lastImage.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\lastPicture1.png"));
            }
        });
        lastImage.setBounds(size.getIconWidth() / 2 - 50 - 40, 5, 22, 22);
        dl.add(lastImage);

        pictureFrame.setSize(size.getIconWidth(),size.getIconHeight());
        pictureFrame.setVisible(true);
        pictureFrame.setLocationRelativeTo(null);
        pictureFrame.setIconImage(imageUtil.getCyderIcon().getImage());
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
            pictureLabel.setBorder(new LineBorder(imageUtil.navy,8,false));

            dl = new DragLabel(size.getIconWidth(), 30, pictureFrame);
            pictureLabel.add(dl);

            rename = new JLabel("Rename");
            rename.setForeground(imageUtil.vanila);
            rename.setFont(imageUtil.weatherFontSmall);
            rename.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (renameFrame != null)
                        imageUtil.closeAnimation(renameFrame);

                    renameFrame = new JFrame();
                    renameFrame.setResizable(false);
                    renameFrame.setIconImage(imageUtil.getCyderIcon().getImage());
                    renameFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    renameFrame.setTitle("Rename");

                    JPanel pan = new JPanel();
                    pan.setLayout(new BoxLayout(pan, BoxLayout.Y_AXIS));

                    JTextField renameField = new JTextField(20);
                    renameField.setFont(imageUtil.weatherFontSmall);
                    renameField.setForeground(imageUtil.navy);
                    renameField.setBorder(new LineBorder(imageUtil.navy,5,false));

                    CyderButton attemptRen = new CyderButton("Rename");
                    attemptRen.setBackground(imageUtil.regularRed);
                    attemptRen.setColors(imageUtil.regularRed);
                    renameField.addActionListener(e1 -> attemptRen.doClick());

                    JPanel a = new JPanel();
                    a.add(renameField, SwingConstants.CENTER);
                    pan.add(a);

                    attemptRen.setBorder(new LineBorder(imageUtil.navy,5,false));
                    attemptRen.setFont(imageUtil.weatherFontSmall);
                    attemptRen.setForeground(imageUtil.navy);
                    attemptRen.addActionListener(e12 -> {
                        String name = renameField.getText();

                        File oldName = new File(validImages.get(currentIndex).getAbsolutePath());
                        File newName = new File(validImages.get(currentIndex).getAbsolutePath().replace(validImages.get(currentIndex).getName().replace(".png",""),name));
                        oldName.renameTo(newName);
                        imageUtil.inform("Successfully renamed to " + name,"",400,300);

                        initFiles();

                        for (int i = 0 ; i < validImages.size() ; i++) {
                            if (validImages.get(i).getName().equals(name)) {
                                currentIndex = i;
                            }
                        }

                        pictureFrame.setTitle(name);

                        imageUtil.closeAnimation(renameFrame);

                        System.out.println("index: " + currentIndex + "\nFiles:\n");
                        for (File f : validImages) {
                            System.out.println(f);
                        }
                    });

                    JPanel b = new JPanel();
                    b.add(attemptRen, SwingConstants.CENTER);
                    pan.add(b);
                    pan.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

                    renameFrame.add(pan);
                    renameFrame.pack();
                    renameFrame.setVisible(true);
                    renameFrame.setLocationRelativeTo(null);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    rename.setForeground(imageUtil.regularRed);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    rename.setForeground(imageUtil.vanila);
                }
            });

            rename.setBounds(size.getIconWidth() / 2 - 50, 5, 90, 20);
            dl.add(rename);

            nextImage = new JLabel(new ImageIcon("src\\com\\cyder\\io\\pictures\\nextPicture1.png"));
            nextImage.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    scrollFoward();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    nextImage.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\nextPicture2.png"));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    nextImage.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\nextPicture1.png"));
                }
            });
            nextImage.setBounds(size.getIconWidth() / 2 - 50 + 100, 5, 22, 22);
            dl.add(nextImage);

            lastImage = new JLabel(new ImageIcon("src\\com\\cyder\\io\\pictures\\lastPicture1.png"));
            lastImage.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    scrollBack();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    lastImage.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\lastPicture2.png"));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    lastImage.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\lastPicture1.png"));
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
            imageUtil.handle(e);
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
            pictureLabel.setBorder(new LineBorder(imageUtil.navy,8,false));

            dl = new DragLabel(size.getIconWidth(), 30, pictureFrame);
            pictureLabel.add(dl);

            rename = new JLabel("Rename");
            rename.setForeground(imageUtil.vanila);
            rename.setFont(imageUtil.weatherFontSmall);
            rename.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (renameFrame != null)
                        imageUtil.closeAnimation(renameFrame);

                    renameFrame = new JFrame();
                    renameFrame.setResizable(false);
                    renameFrame.setIconImage(imageUtil.getCyderIcon().getImage());
                    renameFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    renameFrame.setTitle("Rename");

                    JPanel pan = new JPanel();
                    pan.setLayout(new BoxLayout(pan, BoxLayout.Y_AXIS));

                    JTextField renameField = new JTextField(20);
                    renameField.setFont(imageUtil.weatherFontSmall);
                    renameField.setForeground(imageUtil.navy);
                    renameField.setBorder(new LineBorder(imageUtil.navy,5,false));

                    CyderButton attemptRen = new CyderButton("Rename");
                    attemptRen.setBackground(imageUtil.regularRed);
                    attemptRen.setColors(imageUtil.regularRed);
                    renameField.addActionListener(e1 -> attemptRen.doClick());

                    JPanel a = new JPanel();
                    a.add(renameField, SwingConstants.CENTER);
                    pan.add(a);

                    attemptRen.setBorder(new LineBorder(imageUtil.navy,5,false));
                    attemptRen.setFont(imageUtil.weatherFontSmall);
                    attemptRen.setForeground(imageUtil.navy);
                    attemptRen.addActionListener(e12 -> {
                        String name = renameField.getText();

                        File oldName = new File(validImages.get(currentIndex).getAbsolutePath());
                        File newName = new File(validImages.get(currentIndex).getAbsolutePath().replace(validImages.get(currentIndex).getName().replace(".png",""),name));
                        oldName.renameTo(newName);
                        imageUtil.inform("Successfully renamed to " + name,"",400,300);

                        initFiles();

                        for (int i = 0 ; i < validImages.size() ; i++) {
                            if (validImages.get(i).getName().equals(name)) {
                                currentIndex = i;
                            }
                        }

                        pictureFrame.setTitle(name);

                        imageUtil.closeAnimation(renameFrame);

                        System.out.println("index: " + currentIndex + "\nFiles:\n");
                        for (File f : validImages) {
                            System.out.println(f);
                        }
                    });

                    JPanel b = new JPanel();
                    b.add(attemptRen, SwingConstants.CENTER);
                    pan.add(b);
                    pan.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

                    renameFrame.add(pan);
                    renameFrame.pack();
                    renameFrame.setVisible(true);
                    renameFrame.setLocationRelativeTo(null);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    rename.setForeground(imageUtil.regularRed);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    rename.setForeground(imageUtil.vanila);
                }
            });

            rename.setBounds(size.getIconWidth() / 2 - 50, 5, 90, 20);
            dl.add(rename);

            nextImage = new JLabel(new ImageIcon("src\\com\\cyder\\io\\pictures\\nextPicture1.png"));
            nextImage.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    scrollFoward();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    nextImage.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\nextPicture2.png"));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    nextImage.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\nextPicture1.png"));
                }
            });
            nextImage.setBounds(size.getIconWidth() / 2 - 50 + 100, 5, 22, 22);
            dl.add(nextImage);

            lastImage = new JLabel(new ImageIcon("src\\com\\cyder\\io\\pictures\\lastPicture1.png"));
            lastImage.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    scrollBack();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    lastImage.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\lastPicture2.png"));
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    lastImage.setIcon(new ImageIcon("src\\com\\cyder\\io\\pictures\\lastPicture1.png"));
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
            imageUtil.handle(e);
        }

    }

    private ImageIcon checkImage(File im) {
        try {
            Dimension dim = imageUtil.getScreenSize();
            double screenX = dim.getWidth();
            double screenY = dim.getHeight();

            double aspectRatio = getAspectRatio(new ImageIcon(ImageIO.read(im)));
            aspectRatio = (aspectRatio == 1.0 ? 2.0 : aspectRatio);
            ImageIcon originalIcon = new ImageIcon(ImageIO.read(im));
            BufferedImage bi = ImageIO.read(im);

            int width = originalIcon.getIconWidth();
            int height = originalIcon.getIconHeight();

            while (width + 400 > screenX || height + 400 > screenY) {
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
            imageUtil.handle(e);
        }

        return null;
    }

    private double getAspectRatio(ImageIcon im) {
        return ((double) im.getIconWidth() / (double) im.getIconHeight());
    }
}
