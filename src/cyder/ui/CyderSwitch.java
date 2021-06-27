package cyder.ui;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;

public class CyderSwitch extends JLabel {
    public enum state {
        ON,OFF,INDETERMINITE
    }

    private int width = 400;
    private int height = 120;

    private Color backgroundColor = Color.red;
    private Color switchBackgroundColor = CyderColors.regularRed;
    private Color switchForegroundColor = CyderColors.navy;
    private Font switchFont = CyderFonts.weatherFontBig;

    public CyderSwitch() {
        this(400,120);
    }

    public CyderSwitch(int width, int height) {
        addMouseMotionListener(new CyderDraggableComponent());
        setBackground(this.backgroundColor);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

        repaint();
    }

    @Override
    public void paint(Graphics g) {
        final Graphics2D graphics2D = (Graphics2D) g;

        RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics2D.setRenderingHints(qualityHints);

        graphics2D.setPaint(backgroundColor);
        GeneralPath outlinePath = new GeneralPath();
        outlinePath.moveTo(0, 0);
        outlinePath.lineTo(width,0);
        outlinePath.lineTo(width,width);
        outlinePath.lineTo(0,50);
        outlinePath.lineTo(0,0);
        outlinePath.closePath();
        graphics2D.fill(outlinePath);

        graphics2D.setPaint(Color.white);
        GeneralPath checkPath = new GeneralPath();
        graphics2D.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        graphics2D.drawLine(5, 5, width - 5, width - 5);
        graphics2D.drawLine(width - 5, 5, 5, width - 5);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public Color getSwitchBackgroundColor() {
        return switchBackgroundColor;
    }

    public Color getSwitchForegroundColor() {
        return switchForegroundColor;
    }

    public Font getSwitchFont() {
        return switchFont;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public void setSwitchBackgroundColor(Color switchBackgroundColor) {
        this.switchBackgroundColor = switchBackgroundColor;
    }

    public void setSwitchForegroundColor(Color switchForegroundColor) {
        this.switchForegroundColor = switchForegroundColor;
    }

    public void setSwitchFont(Font switchFont) {
        this.switchFont = switchFont;
    }
}
