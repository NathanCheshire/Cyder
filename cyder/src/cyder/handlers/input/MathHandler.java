package cyder.handlers.input;

import com.fathzer.soft.javaluator.DoubleEvaluator;
import cyder.annotations.Handle;
import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;
import cyder.utils.StringUtil;

/** A handler for handling mathematical expressions. */
public class MathHandler extends InputHandler {
    /** Suppress default constructor. */
    private MathHandler() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /** The evaluator for evaluating mathematical expressions */
    private static final DoubleEvaluator evaluator;

    static {
        evaluator = new DoubleEvaluator();
    }

    @Handle
    public static boolean handle() {
        boolean ret = false;

        try {
            double result = evaluator.evaluate(StringUtil.firstCharToLowerCase(
                    getInputHandler().commandAndArgsToString()));
            getInputHandler().println(String.valueOf(result));
            ret = true;
        } catch (Exception ignored) {}

        return ret;
    }
}
