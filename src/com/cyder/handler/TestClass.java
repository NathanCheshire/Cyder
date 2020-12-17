package com.cyder.handler;

import com.cyder.utilities.InputUtil;
import com.cyder.utilities.NumberUtil;
import com.cyder.utilities.StringUtil;

import javax.swing.*;

public class TestClass {
    private StringUtil su;

    public TestClass(JTextPane outputArea) {
        try {
            String printMe = InputUtil.getInput("Enter a valid number to be converted to binary","Binary Converter",400,250);
            System.out.println(printMe + " converted to binary equals " + NumberUtil.toBinary(Integer.parseInt(printMe)));
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }
}
