package com.cyder.handler;

import com.cyder.utilities.GeneralUtil;
import com.cyder.utilities.StringUtil;

public class TestClass {
    private StringUtil su;
    private GeneralUtil gu;

    public TestClass() {
        try {
            su = new StringUtil();
            gu = new GeneralUtil();

            long start = System.currentTimeMillis();

            Thread.sleep(1000);
            //execute tests here

            long end = System.currentTimeMillis();
            su.println("Finished tests in: " + (end - start) + "ms");
        }

        catch (Exception e) {
            gu.handle(e);
        }
    }
}
