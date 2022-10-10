package cyder.test;

import com.google.common.collect.ImmutableList;
import cyder.annotations.GuiTest;
import cyder.console.Console;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.network.UsbDevice;
import cyder.network.UsbUtil;

import java.util.concurrent.Future;

/**
 * A class for calling test methods manually.
 */
public final class Test {
    /**
     * Suppress default constructor.
     */
    private Test() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * A method to invoke via the "test" keyword.
     */
    @GuiTest
    @SuppressWarnings({"EmptyTryBlock", "RedundantSuppression"})
    public static void test() {
        try {
            Future<ImmutableList<UsbDevice>> futureDevices = UsbUtil.getUsbDevices();
            while (!futureDevices.isDone()) Thread.onSpinWait();
            ImmutableList<UsbDevice> devices = futureDevices.get();
            devices.forEach(Console.INSTANCE.getInputHandler()::println);
        } catch (Exception e) {
            ExceptionHandler.handle(e);
        }
    }
}
