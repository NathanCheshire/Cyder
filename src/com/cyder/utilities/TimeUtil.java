package com.cyder.utilities;

import javax.swing.*;
import javax.swing.text.StyledDocument;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class TimeUtil {

    //todo correction for timezone method

    public String formatDate(LocalDateTime now, DateTimeFormatter dtf) {
        return now.format(dtf);
    }

    public String formatCurrentDate(DateTimeFormatter dtf) {
        return dtf.format(LocalDate.now());
    }




    //this proves we can move all the printing stuff out of main
    public void test(JTextPane tp) {
        try {
            StyledDocument document = (StyledDocument) tp.getDocument();
            document.insertString(document.getLength(), "fuck you bitch", null);
            tp.setCaretPosition(tp.getDocument().getLength());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
