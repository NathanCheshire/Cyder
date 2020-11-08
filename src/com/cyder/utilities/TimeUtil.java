package com.cyder.utilities;

import javax.swing.*;
import javax.swing.text.StyledDocument;

public class TimeUtil {
    //todo make a method where you can pass it any datetime format and it
    // will return a date in that rep, other methods too like correction for timezones and such
    public void test(JTextPane pane) {
        try {
            StyledDocument document = (StyledDocument) pane.getDocument();
            document.insertString(document.getLength(), "test fuck you" + "\n", null);
            pane.setCaretPosition(pane.getDocument().getLength());
        }

        catch (Exception e) {
            //mainUtil.handle(e);
        }
    }
}
