package com.cyder.handler;

import com.cyder.utilities.StringUtil;

import javax.swing.*;

public class TestClass {
    private StringUtil su;

    public TestClass(JTextPane outputArea) {
        try {
            su = new StringUtil();
            su.setOutputArea(outputArea);

            long start = System.currentTimeMillis();

            Thread.sleep((long) (Math.random() * 100));

            su.println("Finished tests in: " + (System.currentTimeMillis() - start) + "ms");
        }

        catch (Exception e) {
            ErrorHandler.handle(e);
        }
    }
}
