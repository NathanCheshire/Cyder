package cyder.ui;

import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.handlers.internal.SessionHandler;
import cyder.utilities.ReflectionUtil;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CyderTextField extends JTextField {
    private int limit = 10;
    private Color backgroundColor = CyderColors.vanila;
    private String regex;

    public CyderTextField(int charLimit) {
        super(charLimit == 0 ? Integer.MAX_VALUE : charLimit);

        if (charLimit == 0)
            charLimit = Integer.MAX_VALUE;

        this.limit = charLimit;
        regex = null;

        this.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent evt) {
                if (getText().length() > limit) {
                    setText(getText().substring(0,getText().length() - 1));
                    Toolkit.getDefaultToolkit().beep();
                } else if (regex != null && regex.length() != 0 && getText() != null && getText().length() > 0 ) {
                    if (!getText().matches(regex)) {
                        setText(getText().substring(0,getText().length() - 1));
                        Toolkit.getDefaultToolkit().beep();
                    }
                }
            }

            public void keyPressed(KeyEvent evt) {
                if (getText().length() > limit) {
                    setText(getText().substring(0,getText().length() - 1));
                    Toolkit.getDefaultToolkit().beep();
                } else if (regex != null && regex.length() != 0 && getText() != null && getText().length() > 0 ) {
                    if (!getText().matches(regex)) {
                        setText(getText().substring(0,getText().length() - 1));
                        Toolkit.getDefaultToolkit().beep();
                    }
                }
            }

            public void keyReleased(KeyEvent evt) {
                if (getText().length() > limit) {
                    setText(getText().substring(0,getText().length() - 1));
                    Toolkit.getDefaultToolkit().beep();
                } else if (regex != null && regex.length() != 0 && getText() != null && getText().length() > 0 ) {
                    if (!getText().matches(regex)) {
                        setText(getText().substring(0,getText().length() - 1));
                        Toolkit.getDefaultToolkit().beep();
                    }
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                SessionHandler.log(SessionHandler.Tag.ACTION, e.getComponent());
            }
        });

        this.setBackground(backgroundColor);
        this.setSelectionColor(CyderColors.selectionColor);
        this.setFont(CyderFonts.segoe20);
        this.setForeground(CyderColors.navy);
        this.setCaretColor(CyderColors.navy);
        this.setCaret(new CyderCaret(CyderColors.navy));
        this.setBorder(new LineBorder(CyderColors.navy, 5, false));
        this.setOpaque(true);
    }

    public void setBlackBorder() {
        this.setBorder(new LineBorder(CyderColors.navy, 5, false));
    }

    @Override
    public void setBackground(Color newBackgroundColor) {
        super.setBackground(newBackgroundColor);
        backgroundColor = newBackgroundColor;
        this.setOpaque(true);
    }

    @Override
    public Color getBackground() {
        return backgroundColor;
    }

    public void setRegexMatcher(String regex) {
        this.regex = regex;
    }

    public void removeRegexMatcher() {
        this.regex = null;
    }

    public String getRegexMatcher() {
        return regex;
    }

    public void setCharLimit(int limit) {
        this.limit = limit;
        if (getText().length() > limit) {
            setText(getText().substring(0,limit + 1));
        }
    }

    public int getCharLimit() {
        return this.limit;
    }

    @Override
    public String toString() {
        return ReflectionUtil.commonCyderUIReflection(this);
    }

    public void informValidData() {
        this.setBorder(new LineBorder(CyderColors.regularGreen, 5, false));
    }

    public void informInvalidData() {
        this.setBorder(new LineBorder(CyderColors.regularRed, 5, false));
    }

    private KeyAdapter autoCapitalizerListener = new KeyAdapter() {
        @Override
        public void keyTyped(KeyEvent e) {
            if (getText().length() == 1) {
                setText(getText().toUpperCase());
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (getText().length() == 1) {
                setText(getText().toUpperCase());
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (getText().length() == 1) {
                setText(getText().toUpperCase());
            }
        }
    };

    private boolean autoCapitalize = false;

    public void setAutoCapitalization(boolean enable) {
        if (enable && !autoCapitalize) {
            addKeyListener(autoCapitalizerListener);
            autoCapitalize = true;
        } else {
            removeKeyListener(autoCapitalizerListener);
            autoCapitalize = false;
        }
    }

    public static void addAutoCapitalizationAdapter(JTextField tf) {
        tf.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (tf.getText().length() == 1) {
                    tf.setText(tf.getText().toUpperCase());
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (tf.getText().length() == 1) {
                    tf.setText(tf.getText().toUpperCase());
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (tf.getText().length() == 1) {
                    tf.setText(tf.getText().toUpperCase());
                }
            }
        });
    }

    /**
     * Returns the TextField text but trimmed and with multiple occurences
     * of whitespace in the String replaced with one whitespace char.
     *
     * @return the TextField text with trimming performed
     */
    public String getTrimmedText() {
        return this.getText().trim().replaceAll("\\s+"," ");
    }
}
