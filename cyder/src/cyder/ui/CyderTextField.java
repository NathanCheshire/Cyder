package cyder.ui;

import com.google.common.base.Preconditions;
import cyder.constants.CyderColors;
import cyder.constants.CyderFonts;
import cyder.handlers.internal.Logger;
import cyder.utilities.ReflectionUtil;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A Cyder implementation of a text field.
 */
public class CyderTextField extends JTextField {
    /**
     * The character limit.
     */
    private int limit;

    /**
     * The background color of the field.
     */
    private Color backgroundColor = CyderColors.vanila;

    /**
     * The regex to restrict text to.
     */
    private String regex;

    /**
     * Constructs a new CyderTextField object with no character limit.
     */
    public CyderTextField() {
        this(0);
    }

    /**
     * Constructs a new CyderTExtField object.
     *
     * @param charLimit the character limit for the text field.
     */
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
                Logger.log(Logger.Tag.ACTION, e.getComponent());
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


    /**
     * {@inheritDoc}
     */
    @Override
    public void setBackground(Color newBackgroundColor) {
        super.setBackground(newBackgroundColor);
        backgroundColor = newBackgroundColor;
        this.setOpaque(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Color getBackground() {
        return backgroundColor;
    }

    /**
     * Sets the regex to restrict the input to.
     *
     * @param regex the regex to restrict the input to
     */
    public void setRegexMatcher(String regex) {
        this.regex = regex;
    }

    /**
     * Removes the regex from the text field.
     */
    public void removeRegexMatcher() {
        this.regex = null;
    }

    /**
     * Returns the regex matcher for the text field.
     *
     * @return the regex matcher for the text field
     */
    public String getRegexMatcher() {
        return regex;
    }

    /**
     * Sets the character limit. Any chars outside of the limit are trimmed away.
     *
     * @param limit the character limit
     */
    public void setCharLimit(int limit) {
        this.limit = limit;
        if (getText().length() > limit) {
            setText(getText().substring(0,limit + 1));
        }
    }

    /**
     * Returns the character limit for the text field.
     *
     * @return the character limit for the text field
     */
    public int getCharLimit() {
        return this.limit;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return ReflectionUtil.commonCyderUIReflection(this);
    }

    /**
     * The text field's border.
     */
    private LineBorder lineBorder;

    /**
     * The color used for valid form data.
     */
    private final Color validFormDataColor = CyderColors.regularGreen;

    /**
     * The data used for invalid form data.
     */
    private final Color invalidFormDataColor = CyderColors.regularRed;

    /**
     * {@inheritDoc}
     */
    @Override
    public void setBorder(Border border) {
        checkNotNull(border);
        Preconditions.checkArgument(border instanceof LineBorder,
                "Border must be an instance of LineBorder");

        // no need to cast since instanceof LineBorder is ensured
        super.setBorder(border);
    }

    /**
     * Sets the border to a green color to let the user know the provided input is valid.
     */
    public void informValidData() {
        this.setBorder(new LineBorder(validFormDataColor,
                lineBorder.getThickness(), lineBorder.getRoundedCorners()));
    }

    /**
     * Sets the border to a red color to let the user know the provided input is invalid.
     */
    public void informInvalidData() {
        this.setBorder(new LineBorder(invalidFormDataColor,
                lineBorder.getThickness(), lineBorder.getRoundedCorners()));
    }

    /**
     * The key listener used to auto-capitalize the first letter of the field.
     */
    private final KeyAdapter autoCapitalizerListener = new KeyAdapter() {
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

    /**
     * Whether auto capitalization is on.
     */
    private boolean autoCapitalize = false;

    /**
     * Sets whether to capitalize the first letter of the form.
     *
     * @param enable whether to capitalize the first letter of the form
     */
    public void setAutoCapitalization(boolean enable) {
        if (enable && !autoCapitalize) {
            addKeyListener(autoCapitalizerListener);
        } else {
            removeKeyListener(autoCapitalizerListener);
        }

        autoCapitalize = enable;
    }

    /**
     * Adds auto capitalization to the provided text field.
     *
     * @param tf the text field to add auto capitalization to
     */
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
     * Returns the text field text but trimmed and with multiple occurences
     * of whitespace in the String replaced with one whitespace char.
     *
     * @return the text with trimming performed
     */
    public String getTrimmedText() {
        return this.getText().replaceAll("\\s+"," ").trim();
    }
}
