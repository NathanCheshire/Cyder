package cyder.constants;

import com.google.common.base.Preconditions;
import com.google.common.collect.Range;
import cyder.exceptions.IllegalMethodException;
import cyder.handlers.internal.Logger;
import cyder.time.TimeUtil;

import java.awt.*;

/**
 * Common fonts used throughout Cyder.
 * <p>
 * Format for new fonts: NAME_SIZE unless there is a rare exception that applies.
 */
@SuppressWarnings("unused")
public final class CyderFonts {
    public static final String SEGOE_UI_BLACK = "Segoe UI Black";
    public static final String AGENCY_FB = "Agency FB";
    public static final String TAHOMA = "tahoma";

    public static final Font SEGOE_20 = new FontBuilder(SEGOE_UI_BLACK).setSize(20).generate();
    public static final Font SEGOE_30 = new FontBuilder(SEGOE_UI_BLACK).setSize(30).generate();

    public static final Font AGENCY_FB_22 = new FontBuilder(AGENCY_FB).setSize(22).generate();
    public static final Font AGENCY_FB_30 = new FontBuilder(AGENCY_FB).setSize(30).generate();
    public static final Font AGENCY_FB_35 = new FontBuilder(AGENCY_FB).setSize(35).generate();

    public static final Font DEFAULT_FONT_SMALL = AGENCY_FB_22;
    public static final Font DEFAULT_FONT = AGENCY_FB_30;
    public static final Font DEFAULT_FONT_LARGE = AGENCY_FB_35;

    /**
     * Suppress default constructor.
     */
    private CyderFonts() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }

    /**
     * A builder for a font.
     */
    public static class FontBuilder {
        /**
         * The default font metric for built fonts.
         */
        public static final int DEFAULT_METRIC = Font.BOLD;

        /**
         * The name of the font.
         */
        private final String name;

        /**
         * The metric of the font, that of {@link Font#PLAIN}, {@link Font#BOLD},
         * {@link Font#ITALIC}, or a combination of them.
         */
        private int metric = DEFAULT_METRIC;

        /**
         * The size of the font.
         */
        private int size;

        /**
         * Constructs a new font builder.
         *
         * @param name the name of the font
         */
        public FontBuilder(String name) {
            Preconditions.checkNotNull(name);
            Preconditions.checkArgument(!name.isEmpty());

            this.name = name;
        }

        /**
         * The valid font metric range.
         * This includes the following:
         * <ul>
         *     <li>{@link Font#PLAIN}</li>
         *     <li>{@link Font#BOLD}</li>
         *     <li>{@link Font#ITALIC}</li>
         *     <li>{@link Font#BOLD} + {@link Font#ITALIC}</li>
         * </ul>
         */
        private static final Range<Integer> FONT_METRIC_RANGE = Range.closed(0, 3);

        /**
         * The font metric, that of {@link Font#PLAIN}, {@link Font#BOLD},
         * {@link Font#ITALIC}, or a combination of them.
         *
         * @param metric font metric
         * @return this builder
         */
        public FontBuilder setMetric(int metric) {
            Preconditions.checkArgument(FONT_METRIC_RANGE.contains(metric));

            this.metric = metric;
            return this;
        }

        /**
         * Sets the size of this font.
         *
         * @param size the size of this font
         * @return this builder
         */
        public FontBuilder setSize(int size) {
            this.size = size;
            return this;
        }

        /**
         * Generates and returns a font based on the internally set
         * values of this builder.
         *
         * @return a new font
         */
        @SuppressWarnings("MagicConstant") /* font metric checked */
        public Font generate() {
            long start = System.currentTimeMillis();
            Font ret = new Font(name, metric, size);
            String constructionTime = TimeUtil.formatMillis(System.currentTimeMillis() - start);
            Logger.log(Logger.Tag.DEBUG, "Font generation of \"" + name + "\" took: " + constructionTime);
            return ret;
        }
    }
}
