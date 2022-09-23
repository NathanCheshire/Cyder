package cyder.props;

import cyder.constants.CyderStrings;
import cyder.exceptions.IllegalMethodException;

/**
 * Constants necessary for the prop loader.
 */
public final class PropConstants {
    /**
     * Lines which start with this are marked as a comment and not parsed as props.
     */
    static final String COMMENT_PATTERN = "#";

    /**
     * A prop object mapping a key to a value of the props.ini file.
     */
    public static record Prop(String key, String value) {}

    /**
     * The possible annotations for props.
     */
    enum Annotation {
        /**
         * An annotation used to indicate the prop's value should not be logged.
         */
        NO_LOG("no_log");

        /**
         * The annotation name for this annotation without the @.
         */
        private final String annotation;

        Annotation(String annotation) {
            this.annotation = annotation;
        }

        /**
         * Returns this annotation.
         *
         * @return this annotation
         */
        public String getAnnotation() {
            return "@" + annotation;
        }
    }

    /**
     * The suffix a key must have for a no log annotation to be injected.
     */
    static final String KEY_PROP_SUFFIX = "_key";

    /**
     * The name of the props directory.
     */
    static final String PROPS_DIR_NAME = "props";

    /**
     * Suppress default constructor.
     */
    private PropConstants() {
        throw new IllegalMethodException(CyderStrings.ATTEMPTED_INSTANTIATION);
    }
}
