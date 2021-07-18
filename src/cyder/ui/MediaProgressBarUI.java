package cyder.ui;

import cyder.consts.CyderColors;

import javax.swing.*;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class MediaProgressBarUI extends BasicProgressBarUI {

        public MediaProgressBarUI() {}

        @Override
        protected void installDefaults() {
            super.installDefaults();

            progressBar.setOpaque(false);
            progressBar.setBorder(null);
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            Graphics2D g2d = (Graphics2D) g.create();

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            //fill
            int oStrokeWidth = 3;
            g2d.setStroke(new BasicStroke(oStrokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.setColor(CyderColors.intellijPink);

            int outerWidth = c.getWidth();
            int outerHeight = c.getHeight();

            double progressPercent = progressBar.getValue() / (double) progressBar.getMaximum();
            double prog = (outerWidth - oStrokeWidth) * progressPercent;
            int fullW = (outerWidth - oStrokeWidth);
            int drawFill = (int) Math.min(fullW, prog);

            RoundRectangle2D fill = new RoundRectangle2D.Double(oStrokeWidth / 2, oStrokeWidth / 2,
                    drawFill, outerHeight - oStrokeWidth, outerHeight, outerHeight);

            g2d.fill(fill);

            //outline over fill
            int iStrokWidth = 3;
            g2d.setStroke(new BasicStroke(iStrokWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.setColor(CyderColors.navy);
            g2d.setBackground(CyderColors.navy);

            int width = c.getWidth();
            int height = c.getHeight();

            RoundRectangle2D outline = new RoundRectangle2D.Double(iStrokWidth / 2, iStrokWidth / 2,
                    width - iStrokWidth, height - iStrokWidth, height, height);

            g2d.draw(outline);

            //clip so nothing out of outer oval
            g2d.setClip(outline);

            g2d.dispose();
        }
    }