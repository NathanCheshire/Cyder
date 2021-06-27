package cyder.ui;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.utilities.AnimationUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CyderSwitch extends JLabel {
    public enum state {
        ON,OFF,INDETERMINITE
    }

    public void setSwitchState(state switchState) {
        this.switchState = switchState;
        repaint();

        switch (switchState) {
            case ON:
                switchButton.setText("1");
                switchButton.setLocation(this.getWidth() - switchButton.getWidth() - 10, 10);
                break;
            case OFF:
                switchButton.setText("0");
                switchButton.setLocation(10, 10);
                break;
            case INDETERMINITE:
                switchButton.setText("?");
                switchButton.setLocation(this.getWidth() / 2 - (this.width / 2 - 20) / 2, 10);
                break;
        }
    }

    public state getSwitchState() {
        return switchState;
    }

    private state switchState = state.INDETERMINITE;
    private int width = 400;
    private int height = 120;

    private Color backgroundColor = Color.red;
    private Color switchBackgroundColor = CyderColors.regularRed;
    private Color switchForegroundColor = CyderColors.navy;
    private Font switchFont = CyderFonts.weatherFontBig;

    private CyderButton switchButton;

    public CyderSwitch() {
        this(400,120);
    }

    public CyderSwitch(int width, int height) {
        this.width = width;
        this.height = height;

        addMouseMotionListener(new CyderDraggableComponent());
        setBackground(this.backgroundColor);
        switchButton = new CyderButton(switchState == state.ON ? "1" : switchState == state.OFF ? "0" : "?"){
            @Override
            public void addActionListener(ActionListener actionListener) {
                super.addActionListener(actionListener);
                super.addActionListener(defaultAction);
            }
        };
        switchButton.setColors(switchBackgroundColor);
        switchButton.setFont(switchFont);
        switchButton.setForeground(switchForegroundColor);
        //todo change to /4 for quarter width, /2 for half width, etc. Make enum for this

        int xOff = 10;
        switch (this.switchState) {
            case ON:
                xOff = this.getWidth() - switchButton.getWidth() - 10;
                break;
            case OFF:
                break;
            case INDETERMINITE:
                xOff = this.getWidth() / 2 - (this.width / 2 - 20) / 2;
                break;
        }

        switchButton.setBounds(xOff ,10,this.width / 2 - 20, this.height - 20);
        add(switchButton);
        switchButton.addActionListener(defaultAction);

        setBorder(new LineBorder(CyderColors.navy, 5, false));
        repaint();
    }

    private ActionListener defaultAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            switch (switchState) {
                case OFF:
                    switchState = state.ON;
                    switchButton.setText("1");
                    AnimationUtil.componentRight(switchButton.getX(), getWidth() - switchButton.getWidth() - 10, 5, 8, switchButton);
                    break;
                case ON:
                    switchState = state.OFF;
                    switchButton.setText("0");
                    AnimationUtil.componentLeft(switchButton.getX(), 10, 5, 8, switchButton);
                    break;
                case INDETERMINITE:
                    switchState = state.OFF;
                    switchButton.setText("0");
                    AnimationUtil.componentLeft(switchButton.getX(), 10, 5, 8, switchButton);
                    break;
            }
        }
    };

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
        repaint();
    }

    public void setHeight(int height) {
        this.height = height;
        repaint();
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

    public CyderButton getSwitchButton() {
        return this.switchButton;
    }
}
