package com.cyder.handler;

import com.cyder.utilities.GeneralUtil;
import com.cyder.utilities.StringUtil;

import javax.swing.*;

public class TestClass {
    private StringUtil su;
    private GeneralUtil gu;

    public TestClass(JTextPane outputArea) {
        try {
            su = new StringUtil();
            su.setOutputArea(outputArea);
            gu = new GeneralUtil();

            long start = System.currentTimeMillis();

            Thread.sleep((long) (Math.random() * 100));

            su.println("Finished tests in: " + (System.currentTimeMillis() - start) + "ms");
        }

        catch (Exception e) {
            gu.handle(e);
        }
    }
}
