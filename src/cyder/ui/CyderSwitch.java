package cyder.ui;

import cyder.consts.CyderColors;
import cyder.consts.CyderFonts;
import cyder.utilities.AnimationUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.event.ActionListener;

public class CyderSwitch extends JLabel {
    public enum State {
        ON,OFF,INDETERMINITE
    }

    private int width;
    private int height;
    private State state;
    private CyderButton switchButton;
    private int buttonPercent = 25;
    private int animationDelay = 4;

    public CyderSwitch(int width, int height, State startingState) {
        this.width = width;
        this.height = height;
        this.state = startingState;

        setSize(width, height);
        setBorder(new LineBorder(CyderColors.navy, 5, false));

        switchButton = new CyderButton() {
          @Override
          public void addActionListener(ActionListener actionListner) {
              super.addActionListener(actionListner);
              super.addActionListener(defaultActionListener);
          }
        };

        switchButton.setForeground(CyderColors.navy);
        switchButton.setColors(CyderColors.regularRed);
        switchButton.setFont(CyderFonts.defaultFontSmall);
        switchButton.setSize((int) (this.width * ((double) this.buttonPercent / 100)) - 10, this.height - 20);
        switchButton.setLocation(10, 10);
        add(switchButton);
    }

    public CyderSwitch(int width, int height) {
        this(width, height, State.OFF);
    }

    public CyderSwitch () {
        this(400,120);
    }

    public ActionListener defaultActionListener = e -> {
        if (this.state == State.OFF)
            setState(State.ON);
        else
            setState(State.OFF);
    };

    public void setState(State state) {
        this.state = state;

        boolean shouldAniamte = (this.isVisible() && this.getParent() != null);

        switch(state) {
            case ON:
                switchButton.setText("ON");
                if (shouldAniamte)
                    AnimationUtil.componentRight(switchButton.getX(), this.width - switchButton.getWidth() - 10,
                            animationDelay, 8, switchButton);
                break;
            case OFF:
                switchButton.setText("OFF");
                if (shouldAniamte)
                    AnimationUtil.componentLeft(switchButton.getX(), 10, animationDelay, 8, switchButton);
                break;
            case INDETERMINITE:
                switchButton.setText("?");
                if (switchButton.getX() > 10) {
                    if (shouldAniamte) {
                        switchButton.setLocation(this.width - switchButton.getWidth() - 10, 10);
                        AnimationUtil.componentLeft(switchButton.getX(),
                                this.width / 2 - switchButton.getWidth() / 2, animationDelay, 8, switchButton);
                    }
                    switchButton.setLocation(this.width / 2 - switchButton.getWidth() / 2,10);
                } else {
                    if (shouldAniamte) {
                        switchButton.setLocation(10,10);
                        AnimationUtil.componentRight(10, this.width / 2 - switchButton.getWidth() / 2,
                                animationDelay, 8, switchButton);
                    }
                    switchButton.setLocation(10,10);
                }
                break;
        }

        repaint();
    }

    public CyderButton getSwitchButton() {
        return this.switchButton;
    }

    public State getState() {
        return this.state;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getButtonPercent() {
        return buttonPercent;
    }

    public int getAnimationDelay() {
        return animationDelay;
    }

    public void setButtonPercent(int buttonPercent) {
        this.buttonPercent = buttonPercent;
        switchButton.setSize((int) (this.width * ((double) this.buttonPercent / 100)) - 10, this.height - 20);

        switch (this.state) {
            case ON:
                switchButton.setLocation(this.width - switchButton.getWidth() - 10,10);
                break;
            case OFF:
                switchButton.setLocation(10,10);
                break;
            case INDETERMINITE:
                switchButton.setLocation(this.width / 2 - switchButton.getWidth() / 2, 10);
                break;
        }
    }

    public void setAnimationDelay(int animationDelay) {
        this.animationDelay = animationDelay;
    }
}
