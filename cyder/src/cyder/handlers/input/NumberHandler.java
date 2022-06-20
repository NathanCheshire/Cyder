package cyder.handlers.input;

import cyder.annotations.Handle;
import cyder.constants.CyderRegexPatterns;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;
import cyder.threads.CyderThreadRunner;
import cyder.utils.FileUtil;
import cyder.utils.IOUtil;
import cyder.utils.NumberUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * A handler to handle things involving numbers.
 */
public class NumberHandler extends InputHandler {
    /**
     * Suppress default constructor.
     */
    private NumberHandler() {
        throw new IllegalMethodException(CyderStrings.attemptedInstantiation);
    }

    @Handle({"binary", "prime", "bindump", "hexdump", "number2string"})
    public static boolean handle() {
        boolean ret = true;

        if (getInputHandler().commandIs("binary")) {
            if (getInputHandler().checkArgsLength(1)
                    && CyderRegexPatterns.numberPattern.matcher(getInputHandler().getArg(0)).matches()) {
                CyderThreadRunner.submit(() -> {
                    try {
                        getInputHandler().println(getInputHandler().getArg(0) + " converted to binary equals: "
                                + Integer.toBinaryString(Integer.parseInt(getInputHandler().getArg(0))));
                    } catch (Exception ignored) {
                    }
                }, "Binary Converter");
            } else {
                getInputHandler().println("Your value must only contain numbers.");
            }
        } else if (getInputHandler().commandIs("prime")) {
            if (getInputHandler().checkArgsLength(1)) {
                int num = Integer.parseInt(getInputHandler().getArg(0));

                if (NumberUtil.isPrime(num)) {
                    getInputHandler().println(num + " is a prime");
                } else {
                    getInputHandler().println(
                            num + " is not a prime because it is divisible by: "
                                    + NumberUtil.primeFactors(num));
                }
            } else {
                getInputHandler().println("Prime usage: prime NUMBER");
            }
        } else if (getInputHandler().commandIs("bindump")) {
            if (getInputHandler().checkArgsLength(2)) {
                if (!getInputHandler().getArg(0).equals("-f")) {
                    getInputHandler().println("Bindump usage: bindump -f /path/to/binary/file");
                } else {
                    File f = new File(getInputHandler().getArg(1));

                    if (f.exists()) {
                        getInputHandler().printlnPriority("0b" + IOUtil.getBinaryString(f));
                    } else {
                        getInputHandler().println("File: " + getInputHandler().getArg(0) + " does not exist.");
                    }
                }
            } else {
                getInputHandler().println("Bindump usage: bindump -f /path/to/binary/file");
            }
        } else if (getInputHandler().commandIs("hexdump")) {
            if (getInputHandler().checkArgsLength(2)) {
                if (!getInputHandler().getArg(0).equals("-f")) {
                    getInputHandler().println("Hexdump usage: hexdump -f /path/to/binary/file");
                } else {
                    File f = new File(getInputHandler().getArg(1));

                    if (!f.exists())
                        throw new IllegalArgumentException("File does not exist");

                    if (FileUtil.getExtension(f).equalsIgnoreCase(".bin")) {
                        if (f.exists()) {
                            getInputHandler().printlnPriority("0x" + IOUtil.getHexString(f).toUpperCase());
                        } else {
                            getInputHandler().println("File: " + getInputHandler().getArg(1) + " does not exist.");
                        }
                    } else {
                        try {
                            InputStream inputStream = new FileInputStream(f);
                            int numberOfColumns = 10;

                            StringBuilder sb = new StringBuilder();

                            long streamPtr = 0;
                            while (inputStream.available() > 0) {
                                long col = streamPtr++ % numberOfColumns;
                                sb.append(String.format("%02x ", inputStream.read()));
                                if (col == (numberOfColumns - 1)) {
                                    sb.append("\n");
                                }
                            }

                            inputStream.close();

                            getInputHandler().printlnPriority(sb.toString());
                        } catch (Exception e) {
                            ExceptionHandler.handle(e);
                        }
                    }
                }
            } else {
                getInputHandler().println("Hexdump usage: hexdump -f /path/to/binary/file");
            }
        } else if (getInputHandler().commandIs("number2string")
                || getInputHandler().commandIs("number2word")) {
            if (getInputHandler().checkArgsLength(1)) {
                if (CyderRegexPatterns.numberPattern.matcher(getInputHandler().getArg(0)).matches()) {
                    getInputHandler().println(NumberUtil.toWords(getInputHandler().getArg(0)));
                } else {
                    getInputHandler().println("Could not parse input as number: "
                            + getInputHandler().getArg(0));
                }
            } else {
                getInputHandler().println("Command usage: number2string [integer]");
            }
        } else {
            ret = false;
        }

        return ret;
    }
}
