package cyder.handlers.input;

import cyder.annotations.Handle;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.ExceptionHandler;

/**
 * A handler for inner commands wrapped with arguments such as size(x), floor(x, y), etc.
 */
public class WrappedCommandHandler extends InputHandler {
    /**
     * Suppress default constructor.
     */
    private WrappedCommandHandler() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    @Handle
    public static boolean handle() {
        boolean ret = true;

        String command = getInputHandler().commandAndArgsToString();

        try {
            int firstParen = command.indexOf(CyderStrings.openingParenthesis);
            int comma = command.indexOf(",");
            int lastParen = command.indexOf(CyderStrings.closingParenthesis);

            String operation;
            String param1 = "";
            String param2 = "";

            if (firstParen != -1) {
                operation = command.substring(0, firstParen);

                if (comma != -1) {
                    param1 = command.substring(firstParen + 1, comma);

                    if (lastParen != -1) {
                        param2 = command.substring(comma + 1, lastParen);
                    }
                } else if (lastParen != -1) {
                    param1 = command.substring(firstParen + 1, lastParen);
                }

                if (operation.equalsIgnoreCase("abs")) {
                    getInputHandler().println(Math.abs(Double.parseDouble(param1)));
                } else if (operation.equalsIgnoreCase("ceil")) {
                    getInputHandler().println(Math.ceil(Double.parseDouble(param1)));
                } else if (operation.equalsIgnoreCase("floor")) {
                    getInputHandler().println(Math.floor(Double.parseDouble(param1)));
                } else if (operation.equalsIgnoreCase("log")) {
                    getInputHandler().println(Math.log(Double.parseDouble(param1)));
                } else if (operation.equalsIgnoreCase("log10")) {
                    getInputHandler().println(Math.log10(Double.parseDouble(param1)));
                } else if (operation.equalsIgnoreCase("max")) {
                    getInputHandler().println(Math.max(Double.parseDouble(param1), Double.parseDouble(param2)));
                } else if (operation.equalsIgnoreCase("min")) {
                    getInputHandler().println(Math.min(Double.parseDouble(param1), Double.parseDouble(param2)));
                } else if (operation.equalsIgnoreCase("pow")) {
                    getInputHandler().println(Math.pow(Double.parseDouble(param1), Double.parseDouble(param2)));
                } else if (operation.equalsIgnoreCase("round")) {
                    getInputHandler().println(Math.round(Double.parseDouble(param1)));
                } else if (operation.equalsIgnoreCase("sqrt")) {
                    getInputHandler().println(Math.sqrt(Double.parseDouble(param1)));
                } else if (operation.equalsIgnoreCase("tobinary")) {
                    getInputHandler().println(Integer.toBinaryString((int) (Double.parseDouble(param1))));
                } else if (operation.equalsIgnoreCase("size")) {
                    getInputHandler().println("Size: " + (lastParen - firstParen - 1));
                } else {
                    ret = false;
                }
            } else {
                ret = false;
            }
        } catch (Exception e) {
            ExceptionHandler.handle(e);
            ret = false;
        }

        return ret;
    }
}
